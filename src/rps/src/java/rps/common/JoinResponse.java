/*
 * JoinResponse.java
 *
 */

package rps.common;

import java.io.*;

/**
 *
 * @author dhilder
 */
public class JoinResponse extends RPSMessage {
    
    public static final int UNDEFINED = -1;
    public static final int PLAYER_JOINED = 0;
    public static final int GAME_FULL = 1;
    public static final int TEAM_FULL = 2;
    
    private int resultCode;
    
    public JoinResponse() {
        this(UNDEFINED);
    }
    
    public JoinResponse(int resultCode) {
        this.resultCode = resultCode;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(resultCode);
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.resultCode = dis.readInt();
    }
    
    public int getResultCode() {
        return resultCode;
    }
}
