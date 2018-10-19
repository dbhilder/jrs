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
public class LoginResponse extends RPSMessage {
    
    private boolean authenticated;
    
    public LoginResponse() {
        this(false);
    }
    
    public LoginResponse(boolean authenticated) {
        this.authenticated = authenticated;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeBoolean(authenticated);
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.authenticated = dis.readBoolean();
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
}
