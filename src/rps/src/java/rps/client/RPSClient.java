/*
 * RPSClient.java
 *
 */

package rps.client;

import rps.common.*;

/**
 *
 * @author dhilder
 */
public interface RPSClient {
    
    public void addListener(RPSClientListener listener);
    public void removeListener(RPSClientListener listener);
    
    public String getUserId();
    public void setUserId(String userId);
    
    public GameState getCurrentGame();
    public void setCurrentGame(GameState game);
    public String getCurrentGameTitle();
    
    public String popPanel();
    public void pushPanel(String panelName);
    
    public RPSMessage sendMessage(RPSMessage msg) throws Exception;
    
}
