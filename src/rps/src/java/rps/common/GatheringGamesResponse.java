/*
 * LoginResponse.java
 *
 */

package rps.common;

import java.io.*;
import java.util.*;

/**
 *
 * @author dhilder
 */
public class GatheringGamesResponse extends RPSMessage {
    
    private LinkedHashSet gatheringGames;
    
    public GatheringGamesResponse() {
        this(new LinkedHashSet());
    }
    
    public GatheringGamesResponse(LinkedHashSet gatheringGames) {
        this.gatheringGames = gatheringGames;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(gatheringGames.size());
        Iterator gatheringGamesIter = gatheringGames.iterator();
        while (gatheringGamesIter.hasNext()) {
            GameState gameInfo = (GameState)gatheringGamesIter.next();
            gameInfo.writeObject(os);
        }
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        int numGatheringGames = dis.readInt();
        for (int i=0; i<numGatheringGames; i++) {
            GameState gatheringGame = GameState.createObject(is);
            gatheringGames.add(gatheringGame);
        }
    }
    
    public LinkedHashSet getGatheringGames() {
        return gatheringGames;
    }
}
