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
public class HandResultsResponse extends RPSMessage {
    
    private boolean handDone;
    private TreeMap playerChoices;
    
    public HandResultsResponse() {
        this(false, new TreeMap());
    }
    
    public HandResultsResponse(boolean handDone, TreeMap playerChoices) {
        this.handDone = handDone;
        this.playerChoices = playerChoices == null ? new TreeMap() : playerChoices;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeBoolean(handDone);
        dos.writeInt(playerChoices.size());
        Iterator playerIds = playerChoices.keySet().iterator();
        while (playerIds.hasNext()) {
            String playerId = (String)playerIds.next();
            int playerChoice = ((Integer)playerChoices.get(playerId)).intValue();
            dos.writeUTF(playerId);
            dos.writeInt(playerChoice);
        }
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.handDone = dis.readBoolean();
        int numPlayerChoices = dis.readInt();
        for (int i=0; i<numPlayerChoices; i++) {
            String playerId = dis.readUTF();
            Integer playerChoice = new Integer(dis.readInt());
            playerChoices.put(playerId, playerChoice);
        }
    }
    
    public boolean handDone() {
        return handDone;
    }
    
    public TreeMap getPlayerChoices() {
        return playerChoices;
    }
}
