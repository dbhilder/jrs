/*
 * ServerGameState.java
 *
 */

package rps.server;

import rps.common.*;
import java.io.Serializable;
import java.util.*;
import jrs.*;


/** An extension of a game's state to include information useful to the game
  * server. This includes interaction with the ranking service, and calculation
  * of scores, game phases, and other things it is best not to leave to the 
  * clients to manage.
  *
  * @author dhilder
  */
public class ServerGameState extends GameState {

    private RankingService rankingService;
    private long startTime;
    
    /**
     * Creates a new instance of ServerGameState
     * @param title 
     * @param numPlayers 
     * @param teamNames 
     * @param type 
     * @param endGameLimit 
     * @param rankingService 
     */
    public ServerGameState(String title, int numPlayers, String[] teamNames, int type, 
                           int endGameLimit, RankingService rankingService) 
    {
        super(title, numPlayers, teamNames, type, endGameLimit);
        this.rankingService = rankingService;
        
        hands = new TreeMap();
        currentHandNumber = 0;
    }

    /**
     * 
     * @param playerId 
     * @throws java.lang.Exception 
     */
    public void addPlayer(String playerId) throws Exception {
        if (isFull()) {
            // TO DO: This should be a specific exception, so join knows how
            // to handle it.
            throw new GameFullException("Cannot add player to the game. It is full.");
        }
        jrs.PlayerRating playerRating = rankingService.getPlayerRating(playerId);
        double rating = playerRating.getRating();
        double ratingDeviation = playerRating.getRatingDeviation();
        Player player = new Player(playerId, rating, ratingDeviation);
        players.put(playerId, player);
    }
    
    /**
     * 
     * @param playerId 
     * @param teamName 
     * @throws java.lang.Exception 
     */
    public void addPlayer(String playerId, String teamName) throws Exception {
        
        // Ensure the team is not full
        Team team = (Team)teams.get(teamName);
        if (team.isFull()) {
            // TO DO: This should be a specific exception, so join knows how
            // to handle it.
            throw new TeamFullException("Cannot add player to the team. It is full.");
        }
        
        // Add the player to the game
        addPlayer(playerId);

        // Add the player to the team
        team.addPlayer(playerId);
    }
    
    /**
     * 
     * @param playerId 
     * @param choice 
     */
    void playHand(Object playerId, int choice) {
        playHand(currentHandNumber, playerId, choice);
    }
    
    /**
     * 
     * @param handNumber 
     * @param playerId 
     * @param choice 
     */
    void playHand(int handNumber, Object playerId, int choice) {
        Integer handNum = new Integer(handNumber);
        if (!hands.containsKey(handNum)) {
            hands.put(handNum, new TreeMap());
        }
        Map playerChoices = (Map)hands.get(handNum);
        playerChoices.put(playerId, new Integer(choice));
        getPlayer(playerId).setState(Player.WAITING_FOR_RESULTS);
    }

    /**
     * 
     * @param currentHandNumber 
     */
    void setCurrentHandNumber(int currentHandNumber) {
        this.currentHandNumber = currentHandNumber;
    }

    /**
     * 
     * @param gamePhase 
     */
    public void setGamePhase(int gamePhase) {
        this.gamePhase = gamePhase;
        if (gamePhase == PLAYING) {
            startTime = System.currentTimeMillis();
        }
        else if (gamePhase == FINISHED) {
            GameResults gameResults = new GameResults();
            if (isTeamGame()) {
                // TO DO: gameResults.addPlayerResults(Object teamId, Object playerId, double score)
                // for each player in the game.
                Iterator teamNames = teamScores.keySet().iterator();
                while (teamNames.hasNext()) {
                    String teamName = (String)teamNames.next();
                    Iterator playerIds = getTeam(teamName).getPlayerIds().iterator();
                    while (playerIds.hasNext()) {
                        String playerId = (String)playerIds.next();
                        double playerScore = ((Integer)playerScores.get(playerId)).doubleValue();
                        gameResults.addPlayerResults(teamName, playerId, playerScore);
                    }
                    // Note - this is not strictly necessary since, by default, the
                    // ranking service computes the team score to be the sum of the
                    // members' score.
                    double teamScore = ((Integer)teamScores.get(teamName)).doubleValue();
                    gameResults.setTeamResults(teamName, teamScore);
                }
            }
            else {
                Iterator playerIds = playerScores.keySet().iterator();
                while (playerIds.hasNext()) {
                    String playerId = (String)playerIds.next();
                    double playerScore = ((Integer)playerScores.get(playerId)).doubleValue();
                    gameResults.addPlayerResults(playerId, playerScore);
                }
            }
            rankingService.postResults(gameResults);
            
            // Calculate new rankings. Ideally, this is done after each player
            // has posted an average of 15 results, but this gives more
            // immediate results. The ratings change over time, too, not just
            // when results are posted, so a TimerBasedRankingService would
            // be more appropriate. If the rankingService were an instance of
            // ResultsBasedRankingService or TimerBasedRankingService,
            // this call would not be needed.
            if (!(rankingService instanceof ResultsBasedRankingService) &&
                !(rankingService instanceof TimerBasedRankingService))
            {
                rankingService.endPeriod();
            }
        }
    }
    
    // scoreMatrix[player's choice][opponent's choice] = points awarded to player
    // rr=0,rp=0,rs=1;pr=1,pp=0,ps=0;sr=0,sp=1,ss=0
    // eg. scoreMatrix[ROCK][SCISSORS] = 1
    private static final int[][] scoreMatrix = {
        {0, 0, 1},
        {1, 0, 0},
        {0, 1, 0}
    };
    
    /** Update each player's score based on the results of the hands played.
      */
    void calculateScores() {
        
        // Calculate the player scores
        playerScores = new TreeMap();
        Iterator handsIter = hands.keySet().iterator();
        while (handsIter.hasNext()) {
            Integer handNumber = (Integer)handsIter.next();
            TreeMap hand = (TreeMap)hands.get(handNumber);
            Iterator playerIds = hand.keySet().iterator();
            while (playerIds.hasNext()) {
                String playerId = (String)playerIds.next();
                if (!playerScores.containsKey(playerId)) {
                    playerScores.put(playerId, new Integer(0));
                }
                int playerChoice = ((Integer)hand.get(playerId)).intValue();
                Iterator opponentIds = hand.keySet().iterator();
                while (opponentIds.hasNext()) {
                    String opponentId = (String)opponentIds.next();
                    if (!playerId.equals(opponentId)) {
                        int opponentChoice = ((Integer)hand.get(opponentId)).intValue();
                        int playerScore = ((Integer)playerScores.get(playerId)).intValue();
                        playerScore += scoreMatrix[playerChoice][opponentChoice];
                        playerScores.put(playerId, new Integer(playerScore));
                    } 
                }
            }
        }
        
        // Calculate the team scores
        teamScores = new TreeMap();
        Iterator teamNames = teams.keySet().iterator();
        while (teamNames.hasNext()) {
            String teamName = (String)teamNames.next();
            Team team = getTeam(teamName);
            if (!teamScores.containsKey(teamName)) {
                teamScores.put(teamName, new Integer(0));
            }
            int teamScore = ((Integer)teamScores.get(teamName)).intValue();
            Iterator members = team.getPlayers().iterator();
            while (members.hasNext()) {
                Player member = (Player)members.next();
                int memberScore = ((Integer)playerScores.get(member.getPlayerId())).intValue();
                teamScore += memberScore;
            }
            teamScores.put(teamName, new Integer(teamScore));
        }
    }
    
    /** Check if the end game condition has been met. This condition depends on
      * the type of game:
      * <ul>
      *     <li><code>HANDS</code> currentHandNumber >= endGameLimit</li>
      *     <li><code>TIMED</code> current time >= start time + (endGameLimit * 60 * 1000)</li>
      *     <li><code>POINTS</code> max player score >= endGameLimit</li>
      * </ul>
      *
      * @returns
      *     <code>true</code> if the game is finished.
      */
    boolean isDone() {
        boolean isDone = false;
        switch (gameType) {
            case HANDS:
                isDone = (currentHandNumber+1 >= endGameLimit);
                break;
            case TIMED:
                long currentTime = System.currentTimeMillis();
                isDone = (currentTime >= startTime + (endGameLimit * 60 * 1000));
                break;
            case POINTS:
                int highScore = ((Integer)Collections.max(playerScores.values())).intValue();
                isDone = (highScore >= endGameLimit);
                break;
        }
        return isDone;
    }
}
