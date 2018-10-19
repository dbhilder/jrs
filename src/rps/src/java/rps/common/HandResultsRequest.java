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
public class HandResultsRequest extends RPSMessage {
    
    private String gameTitle;
    int handNumber;
    
    public HandResultsRequest() {
        this(null, 0);
    }
    
    public HandResultsRequest(String gameTitle, int handNumber) {
        this.gameTitle = gameTitle;
        this.handNumber = handNumber;
    }
    
    public String getGameTitle() {
        return gameTitle;
    }
    
    public int getHandNumber() {
        return handNumber;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(gameTitle);
        dos.writeInt(handNumber);
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.gameTitle = dis.readUTF();
        this.handNumber = dis.readInt();
    }
}
