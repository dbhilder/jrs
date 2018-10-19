/*
 * LoginResponse.java
 *
 */

package rps.common;

import java.io.*;

/**
 *
 * @author dhilder
 */
public class RegistrationResponse extends RPSMessage {
    
    public static final int UNDEFINED = -1;
    public static final int SUCCESSFUL = 0;
    public static final int USERID_IN_USE = 1;
    
    private int resultCode;
    
    public RegistrationResponse() {
        this(UNDEFINED);
    }
    
    public RegistrationResponse(int resultCode) {
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
