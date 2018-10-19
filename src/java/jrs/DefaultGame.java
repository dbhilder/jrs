/*
 * DefaultGame.java
 *
 */

package jrs;

import java.util.*;

/** A default implementation of the <code>Game</code> interface. This class
  * can be used to obtain matches from the ranking service when the game's state
  * object cannot implement the <code>Game</code> interface.
  *
  * @see RankingService#orderByBestMatch(Object,Map)
  *
  * @author Derek Hilder
  */
public class DefaultGame implements Game {
    
    /** The game's ID */
    private Object id;
    
    /** A Set of Objects representing the IDs of the players participating
      * in the game.
      */
    private HashSet participantIds;
    
    /** Create an instance of a DefaultGame with no participants.
      * 
      * @param id 
      *     The unique ID to assign to the game.
      */
    public DefaultGame(Object id) {
        this(id, new HashSet());
    }
    
    /** Create an instance of a DefaultGame with the specified list of
      * partipants.
      * 
      * @param id 
      *     The unique ID to assign to the game.
      * @param participantIds 
      *     The IDs of the players participating in the game.
      */
    public DefaultGame(Object id, Set participantIds) {
        this.id = id;
        this.participantIds = new HashSet(participantIds);
    }

    /** Get the game's ID.
      * 
      * @return 
      *     An Object representing the game's unique ID.
      */
    public Object getId() {
        return id;
    }

    /** Get the IDs of the players participating in the game.
      * 
      * @return 
      *     A Set of Objects representing the IDs of the players participating
      *     in the game.
      */
    public Set getParticipantIds() {
        return participantIds;
    }
}
