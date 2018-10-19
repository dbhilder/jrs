/*
 * GameStateRequest.java
 *
 */

package rps.common;

import java.io.*;

/**
 *
 * @author dhilder
 */
public class GameStateRequest extends RPSMessage {
    
    private String gameTitle;
    
    public GameStateRequest() {
        this(null);
    }
    
    public GameStateRequest(String gameTitle) {
        this.gameTitle = gameTitle;
    }
    
    public String getGameTitle() {
        return gameTitle;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(gameTitle);
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.gameTitle = dis.readUTF();
    }
}
