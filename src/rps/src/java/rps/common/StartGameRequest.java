/*
 * StartGameRequest.java
 *
 */

package rps.common;

import java.io.*;

/**
 *
 * @author dhilder
 */
public class StartGameRequest extends RPSMessage {
    
    private String gameTitle;
    
    public StartGameRequest() {
        this(null);
    }
    
    public StartGameRequest(String gameTitle) {
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
