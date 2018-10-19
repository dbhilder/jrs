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
public class StartGatheringResponse extends RPSMessage {
    
    private GameState gameInfo;
    
    public StartGatheringResponse() {
        this(null);
    }
    
    public StartGatheringResponse(GameState gameInfo) {
        this.gameInfo = gameInfo;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        gameInfo.writeObject(os);
    }
    
    public void readMessage(InputStream is) throws IOException {
        this.gameInfo = GameState.createObject(is);
    }
    
    public GameState getGameInfo() {
        return gameInfo;
    }
}
