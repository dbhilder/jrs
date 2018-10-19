/*
 * RPSMessage.java
 *
 */

package rps.common;

import java.io.*;

/**
 *
 * @author dhilder
 */
public abstract class RPSMessage {
    
    /** Creates a new instance of RPSMessage */
    public RPSMessage() {
    }
    
    public static RPSMessage createMessage(InputStream is) 
        throws Exception 
    {
        RPSMessage msg = null;
        DataInputStream dis = null;
        dis = new DataInputStream(is);
        String className = dis.readUTF();
        Class msgClass = Class.forName(className);
        msg = (RPSMessage)msgClass.newInstance();
        msg.readMessage(dis);
        return msg;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        DataOutputStream dos = null;
        dos = new DataOutputStream(os);
        dos.writeUTF(getClass().getName());
    }
    
    public abstract void readMessage(InputStream is) throws IOException;
}
