/*
 * GatheringGamesRequest.java
 *
 */

package rps.common;

import java.io.*;

/**
 *
 * @author dhilder
 */
public class GatheringGamesRequest extends RPSMessage {
    
    private String requestorId;
    
    public GatheringGamesRequest() {
        this(null);
    }
    
    public GatheringGamesRequest(String requestorId) {
        this.requestorId = requestorId;
    }
    
    public String getRequestorId() {
        return requestorId;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(requestorId);
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.requestorId = dis.readUTF();
    }
}
