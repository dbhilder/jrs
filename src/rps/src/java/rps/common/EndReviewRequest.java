/*
 * EndReviewRequest.java
 *
 */

package rps.common;

import java.io.*;

/**
 *
 * @author dhilder
 */
public class EndReviewRequest extends RPSMessage {
    
    private String gameTitle;
    private String playerId;
    
    public EndReviewRequest() {
        this(null, null);
    }
    
    public EndReviewRequest(String gameTitle, String playerId) {
        this.gameTitle = gameTitle;
        this.playerId = playerId;
    }
    
    public String getGameTitle() {
        return gameTitle;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(gameTitle);
        dos.writeUTF(playerId);
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.gameTitle = dis.readUTF();
        this.playerId = dis.readUTF();
    }
}
