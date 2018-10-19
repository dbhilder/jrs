/*
 * PlayHandRequest.java
 *
 */

package rps.common;

import java.io.*;

/**
 *
 * @author dhilder
 */
public class PlayHandRequest extends RPSMessage {
    
    private String gameTitle;
    private String playerId;
    private int choice;
    
    public PlayHandRequest() {
        this(null, null, 0);
    }
    
    public PlayHandRequest(String gameTitle, String playerId, int choice) {
        this.gameTitle = gameTitle;
        this.playerId = playerId;
        this.choice = choice;
    }
    
    public String getGameTitle() {
        return gameTitle;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public int getChoice() {
        return choice;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(gameTitle);
        dos.writeUTF(playerId);
        dos.writeInt(choice);
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.gameTitle = dis.readUTF();
        this.playerId = dis.readUTF();
        this.choice = dis.readInt();
    }
}
