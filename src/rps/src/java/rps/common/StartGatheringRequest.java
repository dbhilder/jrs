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
public class StartGatheringRequest extends RPSMessage {
    
    private String gatheringUserId;
    private String gameTitle;
    private int numPlayers;
    private String[] teamNames;
    private int gameType;
    private int endGameLimit;
    
    public StartGatheringRequest() {
        this(null, null, 0, null, 0, 0);
    }
    
    public StartGatheringRequest(String gatheringUserId, String gameTitle, 
                                 int numPlayers, String[] teamNames, 
                                 int gameType, int endGameLimit) 
    {
        this.gatheringUserId = gatheringUserId;
        this.gameTitle = gameTitle;
        this.numPlayers = numPlayers;
        this.teamNames = teamNames;
        this.gameType = gameType;
        this.endGameLimit = endGameLimit;
    }
    
    public String getGatheringUserId() {
        return gatheringUserId;
    }
    
    public String getGameTitle() {
        return gameTitle;
    }
    
    public int getNumPlayers() {
        return numPlayers;
    }
    
    public int getNumTeams() {
        return (teamNames == null ? 0 : teamNames.length);
    }
    
    public String[] getTeamNames() {
        return teamNames;
    }
    
    public int getGameType() {
        return gameType;
    }
    
    public int getEndGameLimit() {
        return endGameLimit;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(gatheringUserId);
        dos.writeUTF(gameTitle);
        dos.writeInt(numPlayers);
        if (teamNames == null) {
            dos.writeInt(0);
        }
        else {
            dos.writeInt(teamNames.length);
            for (int i=0; i<teamNames.length; i++) {
                dos.writeUTF(teamNames[i]);
            }
        }
        dos.writeInt(gameType);
        dos.writeInt(endGameLimit);
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        this.gatheringUserId = dis.readUTF();
        this.gameTitle = dis.readUTF();
        this.numPlayers = dis.readInt();
        int numTeams = dis.readInt();
        if (numTeams > 0) {
            this.teamNames = new String[numTeams];
            for (int i=0; i<numTeams; i++) {
                this.teamNames[i] = dis.readUTF();
            }
        }
        this.gameType = dis.readInt();
        this.endGameLimit = dis.readInt();
    }
}
