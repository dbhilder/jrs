/*
 * LeaderBoardRequest.java
 *
 */

package rps.common;

import java.io.*;

/**
 *
 * @author dhilder
 */
public class LeaderBoardRequest extends RPSMessage {
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
    }
    
    public void readMessage(InputStream is) throws IOException {
    }
}
