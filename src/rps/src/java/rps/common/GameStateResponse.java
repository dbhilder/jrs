/*
 * GameStateResponse.java
 *
 */

package rps.common;

import java.io.*;

/**
 *
 * @author dhilder
 */
public class GameStateResponse extends RPSMessage {
    
    private GameState gameState;
    
    public GameStateResponse() {
        this(null);
    }
    
    public GameStateResponse(GameState gameInfo) {
        this.gameState = gameInfo;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        gameState.writeObject(os);
    }
    
    public void readMessage(InputStream is) throws IOException {
        this.gameState = GameState.createObject(is);
    }
    
    public GameState getGameInfo() {
        return gameState;
    }
}
