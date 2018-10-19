/*
 * SimulationReport.java
 *
 */

package jrs.analyzer;

import java.text.*;
import java.util.*;
import javax.swing.tree.*;
import javax.swing.tree.DefaultMutableTreeNode;
import jrs.*;

/** This class serves as a model for the simulation report window's JTree.
  * An example of the data hierarchy in this report appears below.
  *
  * <pre>
  * Report
  *    Player1
  *       Period1 (r,RD,v | r',RD',v')
  *          Games
  *             Game1
  *                Player1 (r',RD') score=10 (-)
  *                Player2 (r',RD') score=10 (0.5)
  *                Player3 (r',RD') score=3  (1)
  *                Player4 (r',RD') score=1  (1)
  *             Game2
  *                Team1 score=5
  *                   Player4 (r',RD') score=3 (0)
  *                   Player3 (r',RD') score=2 (0)
  *                Team2 score=3
  *                   Player1 (r',RD') score=2 (-)
  *                   Player2 (r',RD') score=1 (-)
  *          Matches
  *             Player2 (r',RD')
  * </pre>
  *
  * @author Derek Hilder
  */
public class SimulationReport extends DefaultTreeModel {
    
    private static NumberFormat rf;
    private static NumberFormat rdf;
    private static NumberFormat vf;

    static {
        rf = NumberFormat.getNumberInstance();
        rf.setMaximumFractionDigits(0);
        rf.setGroupingUsed(false);

        rdf = NumberFormat.getNumberInstance();
        rdf.setMaximumFractionDigits(2);
        rdf.setGroupingUsed(false);

        vf = NumberFormat.getNumberInstance();
        vf.setMaximumFractionDigits(6);
    }
        
    private long executionTime = 0;
    
    private RankingService rankingService;
    
    public RankingService getRankingService() {
        return rankingService;
    }
    
    /** Creates a new instance of SimulationReport */
    public SimulationReport(RankingService rankingService) {
        super(new DefaultMutableTreeNode("Report"));
        this.rankingService = rankingService;
    }
     
    /**
     * 
     * @param playerId 
     */
    public void addPlayer(Object playerId) {
        PlayerNodeData playerNodeData = new PlayerNodeData(playerId);
        ((DefaultMutableTreeNode)getRoot()).add(new DefaultMutableTreeNode(playerNodeData));
        reload();
    }
    
    /**
     * 
     * @param playerId 
     * @param periodId 
     * @param prePeriodRating 
     * @param postPeriodRating 
     */
    public void addPeriod(Object playerId, 
                          Object periodId, 
                          PlayerRating prePeriodRating, 
                          PlayerRating postPeriodRating) 
    {
        DefaultMutableTreeNode playerNode = getPlayerNode(playerId);
        PeriodNodeData periodNodeData = new PeriodNodeData(periodId, prePeriodRating, postPeriodRating);
        playerNode.add(new DefaultMutableTreeNode(periodNodeData));
        reload();
    }
    
    /**
     * 
     * @param playerId 
     * @param periodId 
     * @param postPeriodRating 
     */
    public void setPostPeriodRating(Object playerId, 
                                    Object periodId, 
                                    PlayerRating postPeriodRating) 
    {
        DefaultMutableTreeNode periodNode = getPeriodNode(playerId, periodId);
        ((PeriodNodeData)periodNode.getUserObject()).postPeriodRating = postPeriodRating;
        reload();
    }
    
    /**
     * 
     * @param playerId 
     * @param periodId 
     * @param gameId 
     * @param gameResults 
     */
    public void addGame(Object playerId, Object periodId, Object gameId, GameResults gameResults) {
        
        DefaultMutableTreeNode gamesNode = getGamesNode(playerId, periodId);
        GameNodeData gameNodeData = new GameNodeData(gameId);
        DefaultMutableTreeNode gameNode = new DefaultMutableTreeNode(gameNodeData);
        gamesNode.add(gameNode);
        
        if (gameResults.isTeamGame()) {

            // Build a tree of nodes that represents a team game. For example, the
            // nodes for a game under Player1's node may be:
            //
            // Game1
            //    Team1 score=5
            //       Player4 (r',RD') score=3 (0)
            //       Player3 (r',RD') score=2 (0)
            //    Team2 score=3
            //       Player1 (r',RD') score=2 (-)
            //       Player2 (r',RD') score=1 (-)
            
            // Get the team the player plays on, and that team's score
            Object playersTeamId = null;
            double playersTeamScore = 0;
            Iterator teamIds = gameResults.getTeams().iterator();
            while (teamIds.hasNext()) {
                Object teamId = teamIds.next();
                if (gameResults.getTeamMembers(teamId).contains(playerId)) {
                    playersTeamId = teamId;
                    playersTeamScore = gameResults.getTeamResults(playersTeamId);
                    break;
                }
            }
            
            // Add the player's team node, and under that, add the player game 
            // results nodes for each member of the player's team.
            // These have the format "PlayerN (r'=?, RD'=?) score=? (-)"
            
            TeamNodeData playersTeamNodeData = new TeamNodeData(playersTeamId, playersTeamScore);
            DefaultMutableTreeNode playersTeamNode = new DefaultMutableTreeNode(playersTeamNodeData);
            gameNode.add(playersTeamNode);
            
            Iterator memberIds = gameResults.getTeamMembers(playersTeamId).iterator();
            while (memberIds.hasNext()) {
                Object memberId = memberIds.next();
                double memberScore = gameResults.getPlayerResults(memberId);
                PlayerGameResultsNodeData playerGameResultsNodeData =
                    new PlayerGameResultsNodeData(memberId, 
                                                  rankingService.getPlayerRating(memberId), 
                                                  memberScore, 
                                                  -1);
                playersTeamNode.add(new DefaultMutableTreeNode(playerGameResultsNodeData));
            }            
            
            double drawThreshhold = Double.parseDouble(System.getProperty("rs.drawThreshhold", "0"));
            
            // Add a team node for each opposing team. Under each, add the player
            // game results nodes for each member of that team.
            // These have the format "PlayerN (r'=?, RD'=?) score=? (0|0.5|1)"
            
            Iterator opponentTeamIds = gameResults.getTeams().iterator();
            while (opponentTeamIds.hasNext()) {
                Object opponentTeamId = opponentTeamIds.next();
                if (!opponentTeamId.equals(playersTeamId)) {
                    
                    double opponentTeamScore = gameResults.getTeamResults(opponentTeamId);
                    TeamNodeData opponentTeamNodeData = new TeamNodeData(opponentTeamId, opponentTeamScore);
                    DefaultMutableTreeNode opponentTeamNode = new DefaultMutableTreeNode(opponentTeamNodeData);
                    gameNode.add(opponentTeamNode);
                    Iterator opponentMemberIds = gameResults.getTeamMembers(opponentTeamId).iterator();
                    while (opponentMemberIds.hasNext()) {
                        Object opponentMemberId = opponentMemberIds.next();
                        double opponentMemberScore = gameResults.getPlayerResults(opponentMemberId);
                        double glicko2Score = -1;
                        if (Math.abs(playersTeamScore - opponentTeamScore) <= drawThreshhold) {
                            glicko2Score = 0.5;
                        }
                        else if (playersTeamScore > opponentTeamScore) {
                            glicko2Score = 1;
                        }
                        else if (playersTeamScore < opponentTeamScore) {
                            glicko2Score = 0;
                        }
                        PlayerGameResultsNodeData opponentGameResultsNodeData =
                            new PlayerGameResultsNodeData(opponentMemberId, 
                                                          rankingService.getPlayerRating(opponentMemberId), 
                                                          opponentMemberScore, 
                                                          glicko2Score);
                        opponentTeamNode.add(new DefaultMutableTreeNode(opponentGameResultsNodeData));
                    }
                }
            }
        }
        else {
            
            // Build a subtree of nodes representing a multiplayer game.
            // For example, a game in which Player1 tied Player2 and beat
            // Player3 and Player4 would be generated as follows:
            //
            // Game1
            //    Player1 (r'=1450, RD'=60.10) score=10 (-)
            //    Player2 (r'=1100, RD'=55.97) score=10 (0.5)
            //    Player3 (r'=1500, RD'=68.76) score=3  (1)
            //    Player4 (r'=1678, RD'=98.75) score=1  (1)
            
            // Add a result for the player whose games are being listed.
            double playerScore = gameResults.getPlayerResults(playerId);
            PlayerGameResultsNodeData playerGameResultsNodeData =
                new PlayerGameResultsNodeData(playerId, rankingService.getPlayerRating(playerId), playerScore, -1);
            gameNode.add(new DefaultMutableTreeNode(playerGameResultsNodeData));

            // Add a result for each opponent of the player whose games are being
            // listed.            
            double drawThreshhold = Double.parseDouble(System.getProperty("rs.drawThreshhold", "0"));
            Iterator opponentIds = gameResults.getPlayers().iterator();
            while (opponentIds.hasNext()) {
                Object opponentId = opponentIds.next();
                if (!opponentId.equals(playerId)) {
                    double opponentScore = gameResults.getPlayerResults(opponentId);
                    double glicko2Score = -1;
                    if (Math.abs(playerScore - opponentScore) <= drawThreshhold) {
                        glicko2Score = 0.5;
                    }
                    else if (playerScore > opponentScore) {
                        glicko2Score = 1;
                    }
                    else if (playerScore < opponentScore) {
                        glicko2Score = 0;
                    }
                    PlayerGameResultsNodeData opponentGameResultsNodeData =
                        new PlayerGameResultsNodeData(opponentId, rankingService.getPlayerRating(opponentId), 
                                                      opponentScore, glicko2Score);
                    gameNode.add(new DefaultMutableTreeNode(opponentGameResultsNodeData));
                }
            }
        }
        
        reload();
    }
    
    /**
     * 
     * @param playerId 
     * @param periodId 
     * @param matches 
     */
    public void addMatch(Object playerId, Object periodId, Set matches) {
        DefaultMutableTreeNode matchesNode = getMatchesNode(playerId, periodId);
        Iterator matchesIter = matches.iterator();
        while (matchesIter.hasNext()) {
            Match match = (Match)matchesIter.next();
            DefaultMutableTreeNode matchNode = 
                new DefaultMutableTreeNode("Player " + match.getPlayerId() + " (" +
                                           "r'=" + rf.format(match.getRating()) + ", " +
                                           "RD'=" + rdf.format(match.getRatingDeviation()) + ") " +
                                           "P(draw)=" + vf.format(match.getProbabilityOfDraw()));
            matchesNode.add(matchNode);
        }
        reload();
    }
    
    /**
     * 
     * @param playerId 
     * @return 
     */
    private DefaultMutableTreeNode getPlayerNode(Object playerId) {
        // Not a terribly efficient search. Could cache the player nodes in a
        // HashMap indexed on player id.
        Enumeration playerNodes = ((DefaultMutableTreeNode)getRoot()).children();
        while (playerNodes.hasMoreElements()) {
            DefaultMutableTreeNode playerNode = (DefaultMutableTreeNode)playerNodes.nextElement();
            if (((PlayerNodeData)playerNode.getUserObject()).playerId.equals(playerId)) {
                return playerNode;
            }
        }
        return null;
    }
    
    /**
     * 
     * @param playerId 
     * @param periodId 
     * @return 
     */
    private DefaultMutableTreeNode getPeriodNode(Object playerId, Object periodId) {
        // Not a terribly efficient search. Could cache the period nodes in a
        // HashMap<Object pid, HashMap<Object periodId, Node periodNode>>
        DefaultMutableTreeNode playerNode = getPlayerNode(playerId);
        Enumeration periodNodes = playerNode.children();
        while (periodNodes.hasMoreElements()) {
            DefaultMutableTreeNode periodNode = (DefaultMutableTreeNode)periodNodes.nextElement();
            if (((PeriodNodeData)periodNode.getUserObject()).periodId.equals(periodId)) {
                return periodNode;
            }
        }
        return null;
    }

    /**
     * 
     * @param playerId 
     * @param periodId 
     * @return 
     */
    private DefaultMutableTreeNode getGamesNode(Object playerId, Object periodId) {
        
        DefaultMutableTreeNode periodNode = getPeriodNode(playerId, periodId);
        DefaultMutableTreeNode gamesNode = null;
        
        // Find the 'Games' node
        Enumeration periodNodeChildren = periodNode.children();
        while (periodNodeChildren.hasMoreElements()) {
            DefaultMutableTreeNode periodNodeChild = 
                (DefaultMutableTreeNode)periodNodeChildren.nextElement();
            if (periodNodeChild.getUserObject().equals("Games")) {
                gamesNode = periodNodeChild;
                break;
            }
        }
        
        // If not found, create it.
        if (gamesNode == null) {
            gamesNode = new DefaultMutableTreeNode("Games");
            periodNode.add(gamesNode);
        }
        
        return gamesNode;
    }
    
    /**
     * 
     * @param playerId 
     * @param periodId 
     * @return 
     */
    private DefaultMutableTreeNode getMatchesNode(Object playerId, Object periodId) {
        
        DefaultMutableTreeNode periodNode = getPeriodNode(playerId, periodId);
        DefaultMutableTreeNode matchesNode = null;
        
        // Find the 'Matches' node
        Enumeration periodNodeChildren = periodNode.children();
        while (periodNodeChildren.hasMoreElements()) {
            DefaultMutableTreeNode periodNodeChild = 
                (DefaultMutableTreeNode)periodNodeChildren.nextElement();
            if (periodNodeChild.getUserObject().equals("Matches")) {
                matchesNode = periodNodeChild;
                break;
            }
        }
        
        // If not found, create it
        if (matchesNode == null) {
            matchesNode = new DefaultMutableTreeNode("Matches");
            periodNode.add(matchesNode);
        }
        
        return matchesNode;
    }
    
    // -------------------------------------------------------------------------
    // Inner class: PlayerNodeData
    
    /**
      */
    private static class PlayerNodeData {
        
        Object playerId;
        
        /**
         * 
         * @param playerId 
         */
        public PlayerNodeData(Object playerId) {
            this.playerId = playerId;
        }
        
        /**
          */
        public String toString() {
            return "Player " + playerId;
        }
    }
    
    // -------------------------------------------------------------------------
    // Inner class: PeriodNodeData
    
    /**
      */
    private static class PeriodNodeData {
        
        Object periodId;
        PlayerRating prePeriodRating;
        PlayerRating postPeriodRating;
        
        /**
         * 
         * @param periodId 
         * @param prePeriodRating 
         * @param postPeriodRating 
         */
        public PeriodNodeData(Object periodId, PlayerRating prePeriodRating, PlayerRating postPeriodRating) {
            this.periodId = periodId;
            this.prePeriodRating = prePeriodRating;
            this.postPeriodRating = postPeriodRating;
        }
        
        /**
          */
        public String toString() {
            
            String r = "n/a";
            String rd = "n/a";
            String v = "n/a";
            if (postPeriodRating != null) {
                r = rf.format(postPeriodRating.getRating());
                rd = rdf.format(postPeriodRating.getRatingDeviation());
                v = vf.format(postPeriodRating.getRatingVolatility());
            }
            
            StringBuffer sb = new StringBuffer();
            sb.append("Period ").append(periodId).append(" (")
              .append("r=").append(rf.format(prePeriodRating.getRating())).append(", ")
              .append("RD=").append(rdf.format(prePeriodRating.getRatingDeviation())).append(", ")
              .append("v=").append(vf.format(prePeriodRating.getRatingVolatility())).append(" | ")
              .append("r'=").append(r).append(", ")
              .append("RD'=").append(rd).append(", ")
              .append("v'=").append(v).append(")");
        
            return sb.toString();
        }
    }
    
    // -------------------------------------------------------------------------
    // Inner class: GameNodeData
    
    /**
      */
    private static class GameNodeData {
        
        Object gameId;
        
        public GameNodeData(Object gameId) {
            this.gameId = gameId;
        }
        
        public String toString() {
            return "Game " + gameId;
        }
    }
    
    // -------------------------------------------------------------------------
    // Inner class: TeamNodeData
    
    /**
      */
    private static class TeamNodeData {
        
        Object teamId;
        double teamScore;
        
        public TeamNodeData(Object teamId, double teamScore) {
            this.teamId = teamId;
            this.teamScore = teamScore;
        }
        
        public String toString() {
            return "Team " + teamId + " score=" + teamScore;
        }
    }
    
    // -------------------------------------------------------------------------
    // Inner class: PlayerGameResultsNodeData
    
    /**
      */
    private static class PlayerGameResultsNodeData {
        
        Object playerId;
        PlayerRating prePeriodRating;
        double gameScore;
        double glicko2Score;
        
        /**
         * 
         * @param playerId 
         * @param gameScore 
         * @param glicko2Score 
         */
        public PlayerGameResultsNodeData(Object playerId, 
                                         PlayerRating prePeriodRating, 
                                         double gameScore, 
                                         double glicko2Score) 
        {
            this.playerId = playerId;
            this.prePeriodRating = prePeriodRating;
            this.gameScore = gameScore;
            this.glicko2Score = glicko2Score;
        }
        
        /**
          */
        public String toString() {
            return "Player " + playerId + " (" +
                "r=" + rf.format(prePeriodRating.getRating()) + ", " +
                "RD=" + rdf.format(prePeriodRating.getRatingDeviation()) + ") " +
                "score=" + gameScore + " " +
                "(" + (glicko2Score < 0 ? "-" : String.valueOf(glicko2Score)) + ")";
        }
    }
}
