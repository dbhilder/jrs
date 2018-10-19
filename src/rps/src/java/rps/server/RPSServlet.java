/*
 * RPSServlet.java
 *
 */

package rps.server;

import rps.common.*;
import rps.common.GameState.*;
import jrs.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
  *
  * @author Derek Hilder
  * @version $rev$
  */
public class RPSServlet extends HttpServlet {
    
    // Map<Object uid, String password>
    private HashMap userCredentials;
    
    private RankingService rankingService;
    
    // Map<String title, GameState game>
    private HashMap games;
    
    
    /**
      * 
      * @throws javax.servlet.ServletException 
      */
    public void init() throws ServletException {

        userCredentials = new HashMap();
        games = new HashMap();
        
        // TO DO: Set type of ranking service based on property or init
        // parameter.
        rankingService = new TimerBasedRankingService(60);
        
        if (rankingService instanceof TimerBasedRankingService) {
            ((TimerBasedRankingService)rankingService).startUpdating();
        }

        // Load the user credentials and player ratings from persistent store.
        try {
            // TO DO: Get this from a servlet parameter
            String dataStorePath = System.getProperty("user.home") + "/rps-datastore.txt";
            File dataStoreFile = new File(dataStorePath);
            if (dataStoreFile.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(dataStorePath));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] fields = line.split("\\|");
                    String playerId = fields[0];
                    String password = fields[1];
                    double rating = Double.parseDouble(fields[2]);
                    double ratingDeviation = Double.parseDouble(fields[3]);
                    double ratingVolatility = Double.parseDouble(fields[4]);
                    userCredentials.put(playerId, password);
                    PlayerRating playerRating = new PlayerRating(playerId, rating, ratingDeviation, ratingVolatility);
                    rankingService.registerPlayer(playerId, playerRating);
                }
            }
        }
        catch (Exception e) {
            throw new ServletException("Failed to load the persisted player ratings from the datastore.", e);
        }
    }
    
    /** Perform shutdown activities prior to the servlet being destroyed. This
      * includes stopping the ranking service and persiting its data.
      */
    public void destroy() {

        if (rankingService instanceof TimerBasedRankingService) {
            ((TimerBasedRankingService)rankingService).stopUpdating();
        }
        
        // Persist the user credentials and player ratings to a data store.
        PrintWriter pw = null;
        try {
            // TO DO: Get this from a servlet parameter
            String dataStorePath = System.getProperty("user.home") + "/rps-datastore.txt";
            File dataStoreFile = new File(dataStorePath);
            pw = new PrintWriter(new FileWriter(dataStorePath));
            Iterator playerIds = userCredentials.keySet().iterator();
            while (playerIds.hasNext()) {
                String playerId = (String)playerIds.next();
                String password = (String)userCredentials.get(playerId);
                jrs.PlayerRating playerRating = rankingService.getPlayerRating(playerId);
                StringBuffer record = new StringBuffer();
                record.append(playerId).append("|");
                record.append(password).append("|");
                record.append(playerRating.getRating()).append("|");
                record.append(playerRating.getRatingDeviation()).append("|");
                record.append(playerRating.getRatingVolatility());
                System.out.println("Writing '" + record + "' to '" + dataStoreFile.getAbsolutePath() + "'");
                pw.println(record);
            }
        }
        catch (Exception e) {
            System.err.println("Failed to persist the player credentials and ratings to the datastore." +
                "\nException: " + e);
        }
        finally {
            if (pw != null) {
                pw.flush();
                pw.close();
            }
        }
    }
    
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      *
      * @param request
      *     The servlet request
      * @param response 
      *     The servlet response
      */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
    {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = request.getInputStream();
            RPSMessage requestMsg = RPSMessage.createMessage(is);

            RPSMessage responseMessage = null;
            
            Class[] parameterTypes = {requestMsg.getClass()};
            Method processRequestMessageMethod = getClass().getDeclaredMethod("processRequestMessage", parameterTypes);
            Object[] args = {requestMsg};
            responseMessage = (RPSMessage)processRequestMessageMethod.invoke(this, args);

            if (responseMessage != null) {
                os = response.getOutputStream();
                responseMessage.writeMessage(os);
            }
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
        finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }
    
    protected RPSMessage processRequestMessage(LoginRequest loginRequest) {
        log("processing LoginRequest");
        LoginResponse loginResponse = new LoginResponse(false);
        String userId = loginRequest.getUserId();
        String password = loginRequest.getPassword();
        if (userCredentials.containsKey(userId) &&
            userCredentials.get(userId).equals(password))
        {
            loginResponse = new LoginResponse(true);
        }
        return loginResponse;
    }
    
    protected RPSMessage processRequestMessage(RegistrationRequest registrationRequest) {
        log("processing RegistrationRequest");
        String userId = registrationRequest.getUserId();
        String password = registrationRequest.getPassword();
        RegistrationResponse registrationResponse = new RegistrationResponse();
        if (userCredentials.containsKey(userId)) {
            registrationResponse = new RegistrationResponse(RegistrationResponse.USERID_IN_USE);
        }
        else {
            userCredentials.put(userId, password);
            // TO DO: Persist userCredentials (or is it sufficient to do this in destroy()?)
            rankingService.registerPlayer(userId);
            // TO DO: Persist player ratings (or is it sufficient to do this in destroy()?)
            registrationResponse = new RegistrationResponse(RegistrationResponse.SUCCESSFUL);
        }
        return registrationResponse;
    }
    
    /**
     * 
     * @param startGatheringRequest 
     * @return 
     */
    protected RPSMessage processRequestMessage(StartGatheringRequest startGatheringRequest) {
        
        log("processing StartGatheringRequest");
        
        String gatheringUserId = startGatheringRequest.getGatheringUserId();
        String gameTitle = startGatheringRequest.getGameTitle();
        int numPlayers = startGatheringRequest.getNumPlayers();
        int numTeams = startGatheringRequest.getNumTeams();
        String[] teamNames = startGatheringRequest.getTeamNames();
        int gameType = startGatheringRequest.getGameType();
        int endGameLimit = startGatheringRequest.getEndGameLimit();
        
        ServerGameState game = new ServerGameState(gameTitle, numPlayers, teamNames, gameType, endGameLimit, rankingService);
        try {
            if (game.isTeamGame()) {
                game.addPlayer(gatheringUserId, teamNames[0]);
            }
            else {
                game.addPlayer(gatheringUserId);
            }
            game.setGamePhase(GameState.GATHERING);
            games.put(game.getTitle(), game);
        }
        catch (Exception e) {
            // This should never happen as the only exceptions are team or game
            // full, but have to handle it
            // TO DO: set an error message text field and make it visible.
            System.out.println("Exception occurred while creating the game.\n" +
                    "Exception: " + e);
            e.printStackTrace();
            return null;
        }
        
        StartGatheringResponse startGatheringResponse = new StartGatheringResponse(game);
        return startGatheringResponse;
    }
    
    /**
     * 
     * @param gameStateRequest 
     * @return 
     */
    protected RPSMessage processRequestMessage(GameStateRequest gameStateRequest) {
        
        log("processing GameStateRequest");
        
        String gameTitle = gameStateRequest.getGameTitle();
        GameState game = (GameState)games.get(gameTitle);
        GameStateResponse gameStateResponse = new GameStateResponse(game);
        return gameStateResponse;
    }
    
    /**
     * 
     * @param gatheringGamesRequest 
     * @return 
     */
    protected RPSMessage processRequestMessage(GatheringGamesRequest gatheringGamesRequest) {
        
        log("processing GatheringGamesRequest");
        
        String requestorId = gatheringGamesRequest.getRequestorId();
        
        // Get the list of games that are in the gathering phase
        LinkedHashSet gatheringGames = new LinkedHashSet();
        Iterator gamesIter = games.values().iterator();
        while (gamesIter.hasNext()) {
            ServerGameState game = (ServerGameState)gamesIter.next();
            if (game.getGamePhase() == GameState.GATHERING) {
                gatheringGames.add(game);
            }
        }

        // TO DO: Try getting game matches using the 
        // RankingService.getGameMatches(Object playerId, Map playerLists)
        // method. Is there somewhere else where we can use this method? If
        // not, just add it here, try it, then comment it out and document
        // how it could be used.
        
        // Order the list of gathering games based on the best match for the
        // requesting player
        gatheringGames = rankingService.orderByBestMatch(requestorId, gatheringGames);
        
        GatheringGamesResponse gatheringGamesResponse = new GatheringGamesResponse(gatheringGames);
        return gatheringGamesResponse;
    }
    
    /**
     * 
     * @param joinRequest 
     * @return 
     */
    protected RPSMessage processRequestMessage(JoinRequest joinRequest) {
        
        log("processing JoinRequest");
        
        String userId = joinRequest.getUserId();
        String gameTitle = joinRequest.getGameTitle();
        String teamName = joinRequest.getTeamName();
        int resultCode = JoinResponse.UNDEFINED;
        
        ServerGameState game = (ServerGameState)games.get(gameTitle);
        try {
            if (teamName == null) {
                game.addPlayer(userId);
            }
            else {
                game.addPlayer(userId, teamName);
            }
            resultCode = JoinResponse.PLAYER_JOINED;
        }
        catch (TeamFullException tfe) {
            resultCode = JoinResponse.TEAM_FULL;
        }
        catch (GameFullException gfe) {
            resultCode = JoinResponse.GAME_FULL;
        }
        catch (Exception e) {
            // TO DO: What could cause this? Should the response be sent back?
            e.printStackTrace();
        }
        
        JoinResponse joinResponse = new JoinResponse(resultCode);
        return joinResponse;
    }
    
    /**
     * 
     * @param startGameRequest 
     * @return 
     */
    protected RPSMessage processRequestMessage(StartGameRequest startGameRequest) {
        
        log("processing StartGameRequest");
        
        String gameTitle = startGameRequest.getGameTitle();
        ServerGameState game = (ServerGameState)games.get(gameTitle);
        // TO DO: Verify game and teams are full.
        game.setGamePhase(GameState.PLAYING);
        
        // Put all the players in the WAITING_FOR_INPUT state
        Iterator players = game.getPlayers().iterator();
        while (players.hasNext()) {
            Player player = (Player)players.next();
            player.setState(Player.WAITING_FOR_INPUT);
        }
        
        StartGameResponse startGameResponse = new StartGameResponse(StartGameResponse.SUCCESSFUL);
        return startGameResponse;
    }
    
    /**
     * 
     * @param playHandRequest 
     * @return 
     */
    protected RPSMessage processRequestMessage(PlayHandRequest playHandRequest) {
        
        log("processing PlayHandRequest");
        
        String gameTitle = playHandRequest.getGameTitle();
        String playerId = playHandRequest.getPlayerId();
        int playerChoice = playHandRequest.getChoice();
        
        ServerGameState game = (ServerGameState)games.get(gameTitle);
        game.playHand(playerId, playerChoice);
        
        // If the hand is done (ie. all players have submitted a choice)
        // move all players into the REVIEWING_RESULTS state.
        if (game.handDone()) {
            Iterator players = game.getPlayers().iterator();
            while (players.hasNext()) {
                Player player = (Player)players.next();
                player.setState(Player.REVIEWING_RESULTS);
            }
            
            game.calculateScores();
        }
        
        GameStateResponse gameStateResponse = new GameStateResponse(game);
        return gameStateResponse;
    }
    
    /**
     * 
     * @param handResultsRequest 
     * @return 
     */
    protected RPSMessage processRequestMessage(HandResultsRequest handResultsRequest) {
        
        log("processing HandResultsRequest");
        
        String gameTitle = handResultsRequest.getGameTitle();
        int handNumber = handResultsRequest.getHandNumber();
        
        ServerGameState game = (ServerGameState)games.get(gameTitle);
        boolean handDone = game.handDone(handNumber);
        TreeMap playerChoices = game.getHand(handNumber);
        
        HandResultsResponse handResultsResponse = new HandResultsResponse(handDone, playerChoices);
        return handResultsResponse;
    }
    
    /**
     * 
     * @param endReviewRequest 
     * @return 
     */
    protected RPSMessage processRequestMessage(EndReviewRequest endReviewRequest) {
        
        log("processing EndReviewRequest");
        
        String gameTitle = endReviewRequest.getGameTitle();
        String playerId = endReviewRequest.getPlayerId();
        
        ServerGameState game = (ServerGameState)games.get(gameTitle);
        game.getPlayer(playerId).setState(Player.WAITING_TO_START);
        
        // If all players are done reviewing the results of the last hand,
        // check if the game is over. If so, set the game phase to FINISHED
        // and post the results to the ranking service. Otherwise, move each
        // player to the WAITING_FOR_INPUT state to begin the next hand.
        if (game.reviewPeriodDone()) {
            if (game.isDone()) {
                game.setGamePhase(GameState.FINISHED);
            }
            else {
                Iterator players = game.getPlayers().iterator();
                while (players.hasNext()) {
                    Player player = (Player)players.next();
                    player.setState(Player.WAITING_FOR_INPUT);
                }
                game.setCurrentHandNumber(game.getCurrentHandNumber()+1);
            }
        }
        
        GameStateResponse gameStateResponse = new GameStateResponse(game);
        return gameStateResponse;
    }
    
    /**
     * 
     * @param leaderBoardRequest 
     * @return 
     */
    protected RPSMessage processRequestMessage(LeaderBoardRequest leaderBoardRequest) {
        
        log("processing LeaderBoardRequest");
        
        // TO DO:
        // Use the GameState.Player class rather than rps.common.RPSPlayerRating
        // since they are effectively the same thing. Add compareTo() logic from
        // RPSPlayerRating to Player.
        // TO DO:
        // When are Player's rating and ratingDeviation updated from the  ranking
        // service? Everytime the server sends gamestate to the client? Can the
        // JRS API be changed to make this problem easier/more obvious to solve?
        // RankingService.addListener() would notify this server when the period
        // ends, so could update all GameStates then.
        // OR - maybe its not neccessary since GameState.Players only exist for
        // the duration of the game (and their ratings should not change during
        // the game)
        
        TreeSet rpsPlayerRatings = new TreeSet();
        Iterator playerIds = rankingService.getPlayers().iterator();
        while (playerIds.hasNext()) {
            String playerId = (String)playerIds.next();
            PlayerRating jrsPlayerRating = rankingService.getPlayerRating(playerId);
            RPSPlayerRating rpsPlayerRating =
                new RPSPlayerRating(playerId, 
                                    jrsPlayerRating.getRating(), 
                                    jrsPlayerRating.getRatingDeviation(),
                                    jrsPlayerRating.getRatingVolatility());
            rpsPlayerRatings.add(rpsPlayerRating);
        }
        
        LeaderBoardResponse leaderBoardResponse = new LeaderBoardResponse(rpsPlayerRatings);
        return leaderBoardResponse;
    }
    
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
    {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
    {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "The Rock, Paper, Scissors game server.";
    }
    // </editor-fold>
}
