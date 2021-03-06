/*
 * RPSApplet.java
 *
 */

package rps.client;

import rps.common.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import javax.swing.*;

/**
 *
 * @author  dhilder
 */
public class RPSApplet extends JApplet implements RPSClient {

    public static NumberFormat rf;
    public static NumberFormat rdf;
    public static NumberFormat vf;

    static {
        rf = NumberFormat.getNumberInstance();
        rf.setMaximumFractionDigits(0);
        rf.setGroupingUsed(false);

        rdf = NumberFormat.getNumberInstance();
        rdf.setMaximumFractionDigits(2);
        rdf.setGroupingUsed(false);

        vf = NumberFormat.getNumberInstance();
        vf.setMaximumFractionDigits(6);
    }
    
    private HashSet listeners;
    private String userId;
    private GameState currentGame;
    private Stack panelStack;
    

    /** Initializes the applet RPSApplet */
    public void init() {
        listeners = new HashSet();
        panelStack = new Stack();
        try {
            java.awt.EventQueue.invokeAndWait(
                new Runnable() {
                    public void run() {
                        initComponents();
                        getContentPane().add(new LoginPanel(RPSApplet.this), "LoginPanel");
                        getContentPane().add(new LeaderBoardPanel(RPSApplet.this), "LeaderBoardPanel");
                        getContentPane().add(new RegistrationPanel(RPSApplet.this), "RegistrationPanel");
                        getContentPane().add(new JoinPanel(RPSApplet.this), "JoinPanel");
                        getContentPane().add(new GameSetupPanel(RPSApplet.this), "GameSetupPanel");
                        getContentPane().add(new GatherPanel(RPSApplet.this), "GatherPanel");
                        getContentPane().add(new GamePanel(RPSApplet.this), "GamePanel");
                        ((CardLayout)getContentPane().getLayout()).show(getContentPane(), "LoginPanel");
                    }
                }
            );
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void destroy() {
        fireAppletDestroyed();
    }
    
    private void fireAppletDestroyed() {
        Iterator listenersIter = listeners.iterator();
        while (listenersIter.hasNext()) {
            RPSClientListener listener = (RPSClientListener)listenersIter.next();
            listener.appletDestroyed();
        }
    }
    
    
    
    /** This method is called from within the init() method to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        getContentPane().setLayout(new java.awt.CardLayout());

    }// </editor-fold>//GEN-END:initComponents

    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
   
    public String getCurrentGameTitle() {
        return currentGame.getTitle();
    }
    
    public GameState getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(GameState game) {
        this.currentGame = game;
    }
    
    public RPSMessage sendMessage(RPSMessage msg) throws Exception {

        OutputStream os = null;
        InputStream is = null;
        RPSMessage responseMsg = null;
        
        try {
            URL docBase = getDocumentBase();
            URL servletUrl = new URL(docBase.getProtocol(), docBase.getHost(), docBase.getPort(), "/rps/server");
            URLConnection conn = servletUrl.openConnection();
            
            // Post the request
            conn.setDoOutput(true);
            os = conn.getOutputStream();
            msg.writeMessage(os);
            
            // Receive the response
            is = conn.getInputStream();
            responseMsg = RPSMessage.createMessage(is);
        }
        finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
        
        return responseMsg;
    }
    
    public void addListener(RPSClientListener listener) {
        listeners.add(listener);
    }

    public void removeListener(RPSClientListener listener) {
        listeners.remove(listener);
    }

    public String popPanel() {
        String panelName = "LoginPanel";
        if (!panelStack.empty()) {
            panelName = (String)panelStack.pop();
        }
        return panelName;
    }

    public void pushPanel(String panelName) {
        panelStack.push(panelName);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
