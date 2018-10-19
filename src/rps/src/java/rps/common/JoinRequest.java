/*
 * JoinRequest.java
 *
 */

package rps.common;

import java.io.*;

/**
 *
 * @author dhilder
 */
public class JoinRequest extends RPSMessage {
    
    private String userId;
    private String gameTitle;
    private String teamName;
    
    public JoinRequest() {
        this(null, null, null);
    }
    
    public JoinRequest(String userId, String gameTitle, String teamName) {
        this.userId = userId;
        this.gameTitle = gameTitle;
        this.teamName = teamName;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getGameTitle() {
        return gameTitle;
    }
    
    public String getTeamName() {
        return teamName;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(userId);
        dos.writeUTF(gameTitle);
        dos.writeUTF(teamName == null ? "" : teamName);
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.userId = dis.readUTF();
        this.gameTitle = dis.readUTF();
        this.teamName = dis.readUTF();
        if (teamName.equals("")) {
            teamName = null;
        }
    }
}
