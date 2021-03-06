/*
 * LoginRequest.java
 *
 */

package rps.common;

import java.io.*;

/**
 *
 * @author dhilder
 */
public class LoginRequest extends RPSMessage {
    
    private String userId;
    private String password;
    
    public LoginRequest() {
        this(null, null);
    }
    
    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(userId);
        dos.writeUTF(password);
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.userId = dis.readUTF();
        this.password = dis.readUTF();
    }
}
