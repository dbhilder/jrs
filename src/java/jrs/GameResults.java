/*
 * GameResults.java
 *
 */

package jrs;

import java.util.*;

/** A class used to represent the results of a multiplayer or team based game.
  * <p>
  * The game server should create an instances of this class at the end of each
  * game, then pass the instance to the RankingService.postResults() method.
  *
  * @author Derek Hilder
  */
public class GameResults {
    
    /** A list of the players' results, indexed by player id.
      * <p>
      * <code>Map&lt;Object pid, Double score&gt;</code> 
      */
    private HashMap playerResults;
    
    /** A list of the teams' results, indexed by team id.
      * <p>
      * <code>Map&lt;Object tid, Double score&gt;</code> 
      */
    private HashMap teamResults;
    
    /** A list of the members of each team.
      * <p>
      * <code>Map&lt;Object tid, List&lt;Object pid&gt;&gt;</code>
      */
    private HashMap teamMembers;
    
    /** Create an empty GameResults object. Use the <code>addPlayerResults</code>
      * methods to add each player's results.
      */
    public GameResults() {
        playerResults = new HashMap();
        teamResults = new HashMap();
        teamMembers = new HashMap();
    }
    
    /** Add the results for a player in a multiplayer game.
      *
      * @param playerId 
      *     The id of the player.
      * @param score 
      *     The player's individual score at the end of the game.
      */
    public void addPlayerResults(Object playerId, double score) {
        // In a multiplayer game, assign each player to their own team.
        addPlayerResults(playerId, playerId, score);
    }
    
    /** Add the results for a player in a team game.
      *
      * @param teamId 
      *     The id of the team the player was playing on.
      * @param playerId 
      *     The id of the player.
      * @param score 
      *     The player's individual score at the end of the game.
      * <p>
      * <i><b>Note:</b> individual scores in team games are currently not
      * recognized. Rather, the team result will be used. However, by
      * default, the team result is set to be the sum of the individual
      * scores of each of team's members. This can be override with
      * the <code>setTeamResult</code> method. </i>
      */
    public void addPlayerResults(Object teamId, Object playerId, double score) {
        
        // Add the player's results
        playerResults.put(playerId, new Double(score));
        
        // Add the player to the specified team
        if (teamMembers.get(teamId) == null) {
            teamMembers.put(teamId, new ArrayList());
        }
        ((List)teamMembers.get(teamId)).add(playerId);
        
        // Set the team's result to be the sum of its members' scores
        setTeamResults(teamId, getTeamResults(teamId) + score);
    }
    
    /** Determine if these game results are from a team game.
      *
      * @return 
      *     <code>true</code> if these game results are from a team game.
      */
    public boolean isTeamGame() {
        // If there are more players than teams, assume it was a team game.
        return playerResults.size() > teamMembers.size();
    }
    
    /** Get the teams that participated in the game.
      * 
      * @return 
      *     A Set of Objects representing the team ids.
      */
    public Set getTeams() {
        return teamMembers.keySet();
    }
    
    /** Get the game results, or score, for the specified team.
      * 
      * @param teamId
      *     The team id.
      * @return 
      *     The team's results for the game. 
      */
    public double getTeamResults(Object teamId) {
        Double teamResult = (Double)teamResults.get(teamId);
        if (teamResult == null) {
            return 0;
        }
        else {
            return teamResult.doubleValue();
        }
    }
    
    /** Set the results, or score, for a team. By default, the team's
      * results are set to be the sum of each of its player's individual
      * results. Use this method to override this behavior, but don't 
      * do so until after all the player's results have been added.
      *
      * @param teamId 
      *     The team's id.
      * @param score 
      *     The team's result, or score.
      */
    public void setTeamResults(Object teamId, double score) {
        teamResults.put(teamId, new Double(score));
    }
    
    /** Get the members of a team.
      * 
      * @param teamId 
      *     The team's id.
      * @return 
      *     A List of Objects representing the ids of the players on the team.
      */
    public List getTeamMembers(Object teamId) {
        return (List)teamMembers.get(teamId);
    }
    
    /** Get a list of all the players that played in the game.
      * 
      * @return 
      *     A Set of Objects representing the ids of the players.
      */
    public Set getPlayers() {
        return playerResults.keySet();
    }
    
    /** Get the inidividual results, or score, of a player.
      * 
      * @param playerId 
      *     The player's id.
      * @return 
      *     The player's results, or score.
      */
    public double getPlayerResults(Object playerId) {
        return ((Double)playerResults.get(playerId)).doubleValue();
    }
}
