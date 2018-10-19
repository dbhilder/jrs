/*
 * GamePanel.java
 *
 */

package rps.client;

import javax.swing.text.PlainDocument;
import rps.common.*;
import rps.common.GameState.*;
import java.awt.image.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import rps.common.GameState.Player;

/**
 *
 * @author dhilder
 */
public class GamePanel extends JPanel implements Runnable, RPSClientListener {

    private static final int PANEL_WIDTH = 450;
    private static final int PANEL_HEIGHT = 500;
    
    private static final int DEFAULT_FPS = 30;

    // Number of frames with a delay of 0 ms before the animation thread yields
    // to other running threads.
    private static final int NO_DELAYS_PER_YIELD = 16;
    
    // no. of frames that can be skipped in any one animation loop
    // i.e the games state is updated but not rendered
    private static final int MAX_FRAME_SKIPS = 5;
    
    // Number of seconds between network updates
    private static final int NETWORK_UPDATE_PERIOD = 1;
    
    // The number of seconds to wait while player's review the results of the
    // hand.
    private static final int REVIEW_PERIOD = 5;
    
    private RPSClient rpsClient;

    private Thread animatorThread;
    
    private NetworkThread networkThread;
    
    private BlockingQueue requestQueue;
    private BlockingQueue responseQueue;
    
    private static final int UNDEFINED = -1;
    private static final int ROCK = 0;
    private static final int PAPER = 1;
    private static final int SCISSORS = 2;
    private int choice = UNDEFINED;

    
    /** Create the panel that the game will be presented in.
      *
      * @param rpsClient 
      *     The instance of RPSClient that owns this panel.
      */
    public GamePanel(RPSClient rpsClient) {
        
        this.rpsClient = rpsClient;
        rpsClient.addListener(this);
        
        setDoubleBuffered(false);
        setBackground(Color.white);
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        
        // Get the focus so this panel receives key events, then register
        // ourselves as a listener to those key events.
        setFocusable(true);
        requestFocus();
        addKeyListener(
            new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    processKey(e);  
                }
            }
        );
        
        addMouseListener(
            new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    processMouseClicked(e);
                }
            }
        );
        
        // Create the queues for sending and receiving messages to and from the server.
        requestQueue = new BlockingQueue();
        responseQueue = new BlockingQueue();
    }
    
    /**
     * 
     * @param visible 
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        System.out.println(rpsClient.getUserId() + ": GamePanel.setVisible(" + visible + ")");
        if (visible) {
            if (!running) {
                startGame();
            }
            else if (isPaused) {
                resumeGame();
            }
        }
        else {
            if (running && !isPaused) {
                pauseGame();
            }
        }
    }
    
    /** Start the game. This starts the animation thread, and a thread to 
      * communicate with the server. 
      */
    private void startGame() {
        
        buttonImages = new BufferedImage[3];
//        buttonImages[0] = loadImage("/images/rock.png");
//        buttonImages[1] = loadImage("/images/paper.png");
//        buttonImages[2] = loadImage("/images/scissors.png");
        buttonImages[0] = loadImage("rock.png");
        buttonImages[1] = loadImage("paper.png");
        buttonImages[2] = loadImage("scissors.png");
        
        // Initialize the game state variables used by the animation thread.
        gameTitle = rpsClient.getCurrentGameTitle();
        gameState = rpsClient.getCurrentGame();
        
        if (animatorThread == null) {
            animatorThread = new Thread(this, "Animator thread for " + rpsClient.getUserId());
            animatorThread.start();
        }
        
        if (networkThread == null) {
            networkThread = new NetworkThread(this);
            networkThread.start();
        }
    }
    
    public void resumeGame() {
        isPaused = false;
    }
    
    
    public void pauseGame() { 
        isPaused = true;   
    }
    
    
    /** Stop the game. This will stop the network and animation threads.
      */
    public void stopGame() {
        
        if ((networkThread != null) && networkThread.isAlive()) {
            System.out.println("Stopping the network thread...");
            networkThread.running = false;
            try {
                networkThread.join();
                System.out.println("Network thread stopped.");
                networkThread = null;
            } 
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        
        if ((animatorThread != null) && animatorThread.isAlive()) {
            System.out.println("Stopping the animator thread...");
            running = false;
            try {
                animatorThread.join();
                System.out.println("Animator thread stopped.");
                animatorThread = null;
            } 
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
    
    /** Clean up the panel's resources before the applet is destroyed.
      */
    public void appletDestroyed() {
        stopGame();
    }
    
    /** Handle key presses
      * 
      * @param keyEvent
      */
    private void processKey(KeyEvent keyEvent) {
        
        int keyCode = keyEvent.getKeyCode();
        
        // termination keys
        if ((keyCode == KeyEvent.VK_ESCAPE) || 
            (keyCode == KeyEvent.VK_Q) ||
            (keyCode == KeyEvent.VK_END) ||
            ((keyCode == KeyEvent.VK_C) && keyEvent.isControlDown()))
        {
            stopGame();
            ((CardLayout)getParent().getLayout()).show(getParent(), "JoinPanel");
        }
        
        // game-play keys
        if (!isPaused && !gameOver) {
            
            if (currentState == Player.WAITING_FOR_INPUT) {
                switch (keyCode) {
                    case KeyEvent.VK_R:
                        choice = ROCK;
                        break;
                    case KeyEvent.VK_P:
                        choice = PAPER;
                        break;
                    case KeyEvent.VK_S:
                        choice = SCISSORS;
                        break;
                }
            }
        }
    }
    
    /** Respond to mouse click events by setting the player's choice of 
      * rock, paper, or scissors, depending on which button was clicked.
      * 
      * @param mouseEvent 
      */
    private void processMouseClicked(MouseEvent mouseEvent) {
        
        int[] x = {PANEL_WIDTH*1/4, PANEL_WIDTH/2, PANEL_WIDTH*3/4};
        int y = 100;
        int w = 40;
        int h = 40;
        
        Rectangle rectR = new Rectangle(x[0]-(w/2), y, w, h);
        Rectangle rectP = new Rectangle(x[1]-(w/2), y, w, h);
        Rectangle rectS = new Rectangle(x[2]-(w/2), y, w, h);
        
        if (rectR.contains(mouseEvent.getPoint())) {
            choice = ROCK;
        }
        else if (rectP.contains(mouseEvent.getPoint())) {
            choice = PAPER;
        }
        else if (rectS.contains(mouseEvent.getPoint())) {
            choice = SCISSORS;
        }
    }
    
   /** Load an image from the specified file.
     * 
     * @param filePath 
     * @return 
     */
    private static BufferedImage loadImage(String filePath) {
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        
        try {
            BufferedImage image = ImageIO.read(GamePanel.class.getResource(filePath));
            BufferedImage copy = gc.createCompatibleImage(image.getWidth(), image.getHeight(), 
                                                          image.getColorModel().getTransparency());
            Graphics2D g2d = copy.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            return copy;
        } 
        catch (IOException e) {
            System.err.println("Load Image error for " + filePath + ":\n" + e);
            e.printStackTrace();
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // The remaining methods are invoked from the animator thread, so anything
    // they access outside of this class, or above this comment, should be
    // synchronized (unless they're primatives)
    
    private boolean running = false;
    private boolean isPaused = false;
    private boolean gameStarted = false;
    private boolean gameOver = false;

    // Period between drawing, in milliseconds (This probably should be
    // in nanoseconds, but JDK 1.4 does not provide easy access to a timer
    // with this resolution)
    // TO DO: Should this be configurable?
    private long period = (long)(1000.0/DEFAULT_FPS);

    // The time of the next network update (ie. when the next GameStateRequest
    // will be sent to the server)
    private long nextNetworkUpdate = 0;
    
    // The time at which the player will stop reviewing the results of the hand
    private long endReviewTime = Long.MAX_VALUE;
    
    // off-screen rendering
    private Image backBufferImage = null;
    private Graphics bbg;
    
    BufferedImage[] buttonImages;
    
    private String gameTitle;
    
    // The latest game state received from the server.
    private GameState gameState;
    
    // Map<Integer choice, Set<Object pid>>
    private TreeMap handResults;
    
    // The phase the player is according to the last game state update from the
    // server.
    private int currentState = Player.UNKNOWN;
    
    // The phase the player was in prior to the last game state update from
    // the server.
    private int lastState = Player.UNKNOWN;

    
    /** Update the game state and render it to the screen. Takes care of
      * adjustments to keep frame rate smooth.
      */
    public void run() {
        
        long beforeTime = System.currentTimeMillis();
        long afterTime;
        long timeDiff;
        long sleepTime;
        long oversleepTime = 0L;
        int noDelays = 0;
        long excess = 0L;
        
        running = true;
        while (running) {
            
            try {
                gameUpdate(beforeTime);
                gameRender();
                paintScreen();

                afterTime = System.currentTimeMillis();
                timeDiff = afterTime - beforeTime;
                sleepTime = (period - timeDiff) - oversleepTime;

                if (sleepTime > 0) {   
                    // some time left in this cycle
                    try {
                        Thread.sleep(sleepTime);
                    } 
                    catch (InterruptedException ex) {
                    }
                    oversleepTime = (System.currentTimeMillis() - afterTime) - sleepTime;
                } 
                else {
                    // sleepTime <= 0; the frame took longer than the period

                    excess -= sleepTime;
                    oversleepTime = 0L;

                    if (++noDelays >= NO_DELAYS_PER_YIELD) {
                        Thread.yield();
                        noDelays = 0;
                    }
                }

                beforeTime = System.currentTimeMillis();

                // If frame animation is taking too long, update the game state
                // without rendering it, to get the updates/sec nearer to
                // the required FPS.
                int skips = 0;
                while ((excess > period) && (skips < MAX_FRAME_SKIPS)) {
                    excess -= period;
                    gameUpdate(beforeTime);
                    skips++;
                }
            }
            catch (Throwable t) {
                // TO DO: Exit game loop? Present a dialog giving the user the option?
                // If we catch specific exeptions, may be able to make that decision in code.
                t.printStackTrace();
            }
        }
    } 
    
    /** Update the game state.
      */
    private void gameUpdate(long timeOfUpdate) {
        
        if (!isPaused && !gameOver) {
            
            // Periodically, send a GameStateRequest to the server
            if (timeOfUpdate >= nextNetworkUpdate) {
                GameStateRequest gameStateRequest = new GameStateRequest(gameTitle);
                requestQueue.enqueue(gameStateRequest);
                nextNetworkUpdate = timeOfUpdate + (NETWORK_UPDATE_PERIOD*1000);
            }
            
            // Get any responses received from the server
            Object response = null;
            while ((response = responseQueue.dequeue(false)) != null) {
                if (response instanceof GameStateResponse) {
                    gameState = ((GameStateResponse)response).getGameInfo();
                }
            }
            
            lastState = currentState;
            if (gameState.getPlayer(rpsClient.getUserId()) != null) {
                currentState = gameState.getPlayer(rpsClient.getUserId()).getState();
            }
            
            // DEBUG
            if (currentState != lastState) {
                System.out.println("Player " + rpsClient.getUserId() + ": current = " +
                    stateToString(currentState) + ", last = " + stateToString(lastState));
            }
            
            // Check for input
            
            if (currentState == Player.WAITING_FOR_INPUT) {
                
                // If we're just entering the WAITING_FOR_INPUT phase, clear
                // the choice.
                if (lastState != Player.WAITING_FOR_INPUT) {
                    choice = UNDEFINED;
                }
                else {
                    // If the player has chosen one of R,P,S, send the choice to the server.
                    // This will move the player into the WAITING_FOR_RESULTS phase.
                    if (choice != UNDEFINED) {
                        String gameTitle = gameState.getTitle();
                        String playerId = rpsClient.getUserId();
                        PlayHandRequest playHandRequest = new PlayHandRequest(gameTitle, playerId, choice);
                            requestQueue.enqueue(playHandRequest);
                    }
                }
            }
            
            // If we've just entered the review results state, create a list
            // of the players that chose each of R, P, or S. This will be
            // used by gameRender().
            if ((currentState == Player.REVIEWING_RESULTS) &&
                (lastState != Player.REVIEWING_RESULTS))
            {
                handResults = new TreeMap();
                Iterator playerIds = gameState.getHand().keySet().iterator();
                while (playerIds.hasNext()) {
                    String playerId = (String)playerIds.next();
                    Integer playerChoice = (Integer)gameState.getHand().get(playerId);
                    if (!handResults.containsKey(playerChoice)) {
                        handResults.put(playerChoice, new HashSet());
                    }
                    Set players = (Set)handResults.get(playerChoice);
                    players.add(playerId);
                }
                
                endReviewTime = timeOfUpdate + (REVIEW_PERIOD*1000);
            }
            
            // Check if the review period is over. If so, send a message to the server
            // requesting that the player be moved into the WAITING_TO_START state.
            // When all players are in the WAITING_TO_START state, the server will
            // move them into the WAITING_FOR_INPUT state and a new hand will begin.
            if ((gameState.getGamePhase() != GameState.FINISHED) && 
                (timeOfUpdate >= endReviewTime)) 
            {
                EndReviewRequest endReviewRequest = new EndReviewRequest(gameTitle, rpsClient.getUserId());
                requestQueue.enqueue(endReviewRequest);
                endReviewTime = Long.MAX_VALUE;
            }
        }
    }
    
    /** Draw the game objects to a backbuffer image.
      */
    private void gameRender() {
        
        // Create the backbuffer image, if necessary
        if (backBufferImage == null){
            backBufferImage = createImage(PANEL_WIDTH, PANEL_HEIGHT);
            if (backBufferImage == null) {
                System.err.println("backBufferImage is null");
                return;
            } 
            else {
                bbg = backBufferImage.getGraphics();
            }
        }

        Graphics2D bbg2d = (Graphics2D)bbg;

        Font defaultFont = bbg2d.getFont();

        // draw background
        bbg.setColor(Color.white);
        bbg.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        
        // Game title
        String gameTitle = rpsClient.getCurrentGame().getTitle();
        bbg2d.setColor(Color.black);
        Font titleFont = new Font(defaultFont.getName(), Font.BOLD, 16);
        bbg2d.setFont(titleFont);
        FontMetrics fm = bbg2d.getFontMetrics();
        int titleWidth = fm.stringWidth(gameTitle);
        int titleHeight = fm.getHeight();
        bbg2d.drawString(gameTitle, (PANEL_WIDTH/2)-(titleWidth/2), 10+titleHeight);
        
        // Game status (ie. gathering, current hand count, timer?)
        String gameStatus = null;
        switch (gameState.getGamePhase()) {
            case GameState.GATHERING:
                gameStatus = "Gathering";
                break;
            case GameState.PLAYING:
                gameStatus = "Hand: " + (gameState.getCurrentHandNumber()+1);
                break;
            case GameState.FINISHED:
                gameStatus = "Game over";
        }
        bbg2d.setColor(Color.black);
        Font statusFont = new Font(defaultFont.getName(), Font.ITALIC, 10);
        bbg2d.setFont(statusFont);
        fm = bbg2d.getFontMetrics();
        int statusWidth = fm.stringWidth(gameStatus);
        int statusHeight = fm.getHeight();
        bbg2d.drawString(gameStatus, (PANEL_WIDTH/2)-(statusWidth/2), 10+titleHeight+5+statusHeight);
        
        // Line describing the conditions under which the game will end.
        String endGameType = null;
        switch (gameState.getGameType()) {
            case GameState.HANDS:   endGameType = "hands";      break;
            case GameState.TIMED:   endGameType = "minutes";    break;
            case GameState.POINTS:  endGameType = "points";     break;
        }
        String endGameString = "Game ends after " + gameState.getEndGameLimit() + " " + endGameType + ".";
        int endGameStringWidth = fm.stringWidth(endGameString);
        int endGameStringHeight = fm.getHeight();
        bbg2d.drawString(endGameString, (PANEL_WIDTH/2)-(endGameStringWidth/2), 10+titleHeight+5+statusHeight+5+endGameStringHeight);
        
        int[] columns = {PANEL_WIDTH*1/4, PANEL_WIDTH/2, PANEL_WIDTH*3/4};
        
        // R,P,S buttons
        // TO DO: Determine if enabled or not (if not, draw different image, or is there
        // a way to 'disable' via code?)
        // Also, should mouseDown/Up be tracked so we can draw it in '3D'?
        int w = 40;
        int h = 40;
        for (int i=0; i<3; i++) {
            bbg.drawImage(buttonImages[i], columns[i]-(w/2), 100, w, h, null);
        }

        if (gameState.getGamePhase() == GameState.FINISHED) {
            
            // Game over message
            bbg2d.setColor(Color.red);
            Font gameOverFont = new Font(defaultFont.getName(), Font.PLAIN, 18);
            bbg2d.setFont(gameOverFont);
            FontMetrics gameOverFontMetrics = bbg2d.getFontMetrics();
            String gameOverString = "Game Over!";
            int gameOverStringWidth = gameOverFontMetrics.stringWidth(gameOverString);
            int gameOverStringHeight = gameOverFontMetrics.getHeight();
            bbg2d.drawString(gameOverString, PANEL_WIDTH/2 - gameOverStringWidth/2, 
                PANEL_HEIGHT/2 - gameOverStringHeight/2);
            
            // Message instructing the user to hit 'esc' or 'q' to
            // return to the Join screen.
            bbg2d.setColor(Color.black);
            Font instructionsFont = new Font(defaultFont.getName(), Font.ITALIC, 10);
            bbg2d.setFont(instructionsFont);
            FontMetrics instructionsFontMetrics = bbg2d.getFontMetrics();
            String instructionsString = "Press the 'esc' key to return to the games list";
            int instructionsStringWidth = instructionsFontMetrics.stringWidth(instructionsString);
            int instructionsStringHeight = instructionsFontMetrics.getHeight();
            bbg2d.drawString(instructionsString, PANEL_WIDTH/2 - instructionsStringWidth/2, 
                (PANEL_HEIGHT/2 + gameOverStringHeight/2) + 10 + instructionsStringHeight);
        }
        else {
            
            // Player choices
            if (currentState == Player.REVIEWING_RESULTS) {
                
                bbg2d.setColor(Color.black);
                Font resultsFont = new Font(defaultFont.getName(), Font.PLAIN, 12);
                bbg2d.setFont(resultsFont);
                FontMetrics resultsFontMetrics = bbg2d.getFontMetrics();

                int playerIdHeight = resultsFontMetrics.getHeight();
                for (int i=0; i<3; i++) {
                    if (handResults.containsKey(new Integer(i))) {
                        String[] playerIds = (String[])((Set)handResults.get(new Integer(i))).toArray(new String[0]);
                        for (int j=0; j<playerIds.length; j++) {
                            int playerIdWidth = resultsFontMetrics.stringWidth(playerIds[j]);
                            int x = columns[i] - (playerIdWidth/2);
                            int y = 200 + ((playerIdHeight+10)*j);
                            bbg2d.drawString(playerIds[j], x, y);
                        }
                    }
                }
            }
        }
        
        // Scores
        if (gameState.getGamePhase() == GameState.PLAYING) {
            
            bbg2d.setColor(Color.black);
            Font scoreFont = new Font(defaultFont.getName(), Font.PLAIN, 12);
            bbg2d.setFont(scoreFont);
            FontMetrics scoreFontMetrics = bbg2d.getFontMetrics();
            int scoreFontHeight = scoreFontMetrics.getHeight();
            
            bbg2d.drawString("Scoring:", 20, PANEL_HEIGHT-100);
            
            int[] playerIdColumns = {25, 100, 175};
            int[] playerScoreColumns = {75, 150, 225};
            
            if (gameState.isTeamGame()) {
                
                int teamRow = PANEL_HEIGHT-100+scoreFontHeight;
                TreeMap playerScores = gameState.getPlayerScores();
                TreeMap teamScores = gameState.getTeamScores();
                String[] teamNames = (String[])teamScores.keySet().toArray(new String[0]);
                for (int j=0; j<teamNames.length; j++) {
                
                    String teamName = teamNames[j];
                    String teamScore = String.valueOf(teamScores.get(teamName));
                    bbg2d.drawString(teamName, playerIdColumns[0], teamRow);
                    bbg2d.drawString(teamScore, playerScoreColumns[0], teamRow);
                    
                    int playerRow = teamRow /*+ 5 + scoreFontHeight*/;
                    String[] playerIds = (String[])gameState.getTeam(teamName).getPlayerIds().toArray(new String[0]);
                    for (int i=0; i<playerIds.length; i++) {
                        String playerId = playerIds[i];
                        String playerScore = String.valueOf(playerScores.get(playerId));
                        if (i%3 == 0) {
                            playerRow += scoreFontHeight;
                        }
                        bbg2d.drawString(playerId, playerIdColumns[i%3], playerRow);
                        bbg2d.drawString(playerScore, playerScoreColumns[i%3], playerRow);
                    }
                    
                    teamRow = playerRow + 5 + scoreFontHeight;
                }
            }
            else {
                int scoreRow = PANEL_HEIGHT-100+scoreFontHeight;
                TreeMap scores = gameState.getPlayerScores();
                String[] playerIds = (String[])scores.keySet().toArray(new String[0]);
                for (int i=0; i<playerIds.length; i++) {
                    String playerId = playerIds[i];
                    String playerScore = String.valueOf(scores.get(playerId));
                    if (i%3 == 0) {
                        scoreRow += scoreFontHeight;
                    }
                    bbg2d.drawString(playerId, playerIdColumns[i%3], scoreRow);
                    bbg2d.drawString(playerScore, playerScoreColumns[i%3], scoreRow);
                }
            }
        }
        
    } 
    
    /** Paint the back buffer image to the graphics context of the screen.
      */
    private void paintScreen() {
        Graphics g;
        try {
            
            // TO DO: Is this thread safe? Isn't the graphics context used by
            // the swing event loop thread? Could use SwingUtilities.invokeAndWait()
            // to get this and set it to a syncronized instance var, but that sounds
            // like it would cause stutter...
            g = this.getGraphics();
            
            if (g != null) {
                if (backBufferImage != null) {
                    g.drawImage(backBufferImage, 0, 0, null);
                }
                g.dispose();
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // For debugging
    private static String stateToString(int state) {
        String stateString = "UNDEFINED";
        switch (state) {
            case Player.UNKNOWN: stateString = "UNKNOWN"; break;
            case Player.WAITING_TO_START: stateString = "WAITING_TO_START"; break;
            case Player.WAITING_FOR_INPUT: stateString = "WAITING_FOR_INPUT"; break;
            case Player.WAITING_FOR_RESULTS: stateString = "WAITING_FOR_RESULTS"; break;
            case Player.REVIEWING_RESULTS: stateString = "REVIEWING_RESULTS"; break;
        }
        return stateString;
    }
    
    // ================================================================================
    // BlockingQueue class
    
    private static class BlockingQueue {
        
        private LinkedList queue;
        
        public BlockingQueue() {
            queue = new LinkedList();
        }
        
        public synchronized void enqueue(Object o) {
            queue.addLast(o);
            notify();
        }
        
        /**
          * 
          * @param wait 
          *     If <code>true</code>, this method will block until an object is
          *     available in the queue. Otherwise, it will return immediately, 
          *     either with the next object in the queue, or <code>null</code>
          *     if the queue is empty.
          * @return 
          */
        public synchronized Object dequeue(boolean wait) {
            
            Object o = null;
            
            if (wait) {
                while (queue.isEmpty()) {
                    try {
                        wait(3*NETWORK_UPDATE_PERIOD*1000);
                    }
                    catch (InterruptedException ie) {
                    }
                }
                try {
                    o = queue.removeLast();
                }
                catch (NoSuchElementException nsee) {
                    System.err.println("Queue is empty.\nException: " + nsee);
                }
            }
            else {
                if (!queue.isEmpty()) {
                    try {
                        o = queue.removeLast();
                    }
                    catch (NoSuchElementException nsee) {
                        System.err.println("Queue is empty.\nException: " + nsee);
                    }
                }
            }
            
            return o;
        }
        
        public synchronized boolean isEmpty() {
            return queue.isEmpty();
        }
    }
    
    // ================================================================================
    // NetworkThread class
    
    private static class NetworkThread extends Thread {
        
        private GamePanel owner;
        boolean running;
        
        public NetworkThread(GamePanel owner) {
            super("Network thread for " + owner.rpsClient.getUserId());
            this.owner = owner;
        }
        
        public void run() {
            running = true;
            while (running) {
                try {
                    // This call will block the thread until a request is 
                    // available to go out.
                    RPSMessage request = (RPSMessage)owner.requestQueue.dequeue(true);
                    RPSMessage response = owner.rpsClient.sendMessage(request);
                    owner.responseQueue.enqueue(response);
                }
                catch (Exception e) {
                    // TO DO: This is thrown by sendMessage(). What do we do?
                    // Put up a dialog? Should we stop polling?
                    e.printStackTrace();
                }
            }
        }
    }    
}
