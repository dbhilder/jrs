/*
 * GameState.java
 *
 */

package rps.common;

import java.io.*;
import java.text.*;
import java.util.*;
import jrs.*;


/** This class represents the state of game. This includes all phases of the
  * game, from setup, gathering, playing and completion. In addition to information
  * about the game, such as name and duration, this class also maintains a list
  * of the players involved, and their teams, if it is a team game.
  *
  * @author dhilder
  */
public class GameState implements Serializable, Game {
    
    protected static NumberFormat rf;
    protected static NumberFormat rdf;
    protected static NumberFormat vf;

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
    
    public static final int UNKNOWN = -1;
    
    // gameType
    public static final int HANDS = 0;
    public static final int TIMED = 1;
    public static final int POINTS = 2;
    
    // gamePhase
    public static final int GATHERING = 0;
    public static final int PLAYING = 1;
    public static final int FINISHED = 2;
    
    protected String title;
    protected int maxPlayers;
    protected int numTeams;
    protected int gameType;
    protected int endGameLimit;
    protected int gamePhase;
    
    // Map<Object pid, PlayerInfo player>
    protected HashMap players;
    
    // Map<String name, Team team>
    protected HashMap teams;
    
    // TreeMap<Integer handNum, TreeMap<Object pid, Integer choice>>
    protected TreeMap hands;
    protected int currentHandNumber;
    
    // TreeMap<String playerId, Integer score>
    protected TreeMap playerScores;
    
    // TreeMap<String teamName,  Integer score>
    protected TreeMap teamScores;

    
    public GameState() {
        this(null, 0, null, UNKNOWN, 0);
    }
    
    /**
     * Creates a new instance of GameState
     */
    public GameState(String title, int maxPlayers, String[] teamNames, int type, int endGameLimit) {
        this.title = title;
        this.maxPlayers = maxPlayers;
        this.gameType = type;
        this.endGameLimit = endGameLimit;
        players = new HashMap();
        teams = new HashMap();
        if (teamNames != null) {
            numTeams = teamNames.length;
            for (int i=0; i<teamNames.length; i++) {
                int numPlayersPerTeam = (int)(maxPlayers / numTeams);
                teams.put(teamNames[i], new Team(teamNames[i], numPlayersPerTeam, this));
            }
        }
        hands = new TreeMap();
        playerScores = new TreeMap();
        teamScores = new TreeMap();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof GameState) {
            return this.title.equals(((GameState) obj).title);
        }
        else {
            return false;
        }
    }
    
    public String toString() {
        
        String gameTypeString = "<unknown units>";
        switch (gameType) {
            case HANDS:     gameTypeString = "hands";   break;
            case TIMED:     gameTypeString = "minutes"; break;
            case POINTS:    gameTypeString = "points";  break;
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append("<html>")
          .append("<b>").append(title).append("</b>")
          .append(" (ave. rating: ").append(rf.format(getAveRating()))/*.append(" +/- ").append("350")*/.append(")")
          .append("<br><font size=-2>max. players: ").append(maxPlayers)
          .append(", games ends after ").append(endGameLimit).append(" ").append(gameTypeString).append("</font><br>");
        sb.append("<i>Players:</i><table>");
        sb.append("<tr>");
        Iterator playersIter = getPlayers().iterator();
        while (playersIter.hasNext()) {
            Player player = (Player)playersIter.next();
            sb.append("<td>").append(player.getPlayerId())
              .append(" (").append(rf.format(player.getRating()))
              .append("+/-").append(rdf.format(player.getRatingDeviation()))
              .append(")</td>");
        }
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("</html>");
        return sb.toString();
    }

    public String getTitle() {
        return title;
    }
    
    public int getGameType() {
        return gameType;
    }
    
    public int getEndGameLimit() {
        return endGameLimit;
    }

    public int getGamePhase() {
        return gamePhase;
    }
    
    public boolean isTeamGame() {
        return ((numTeams > 0) && (maxPlayers > numTeams));
    }
    
    public boolean isFull() {
        return (getNumPlayers() >= maxPlayers);
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public int getNumPlayers() {
        return players.size();
    }
    
    public int getNumTeams() {
        return teams.size();
    }
    
    public Double getAveRating() {
        // TO DO: compute this by averaging player ratings
        return new Double(1500);
    }
    
    public Player getPlayer(Object playerId) {
        return (Player)players.get(playerId);
    }
    
    /** Get the players participating in the game.
      *
      * @return
      *     A Collection of Player objects.
      */
    public Collection getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }
    
    public Team getTeam(String teamName) {
        return (Team)teams.get(teamName);
    }
    
    // Collection<Team>
    public Collection getTeams() {
        return Collections.unmodifiableCollection(teams.values());
    }

    public Collection getTeamNames() {
        HashSet teamNames = new HashSet();
        Iterator teamsIter = teams.values().iterator();
        while (teamsIter.hasNext()) {
            Team team = (Team)teamsIter.next();
            teamNames.add(team.getName());
        }
        return teamNames;
    }
    
    public boolean handDone() {
        return handDone(currentHandNumber);
    }
    
    public boolean handDone(int handNumber) {
        boolean handDone = false;
        TreeMap hand = getHand(handNumber);
        if ((hand != null) && (hand.size() == players.size())) {
            handDone = true;
        }
        return handDone;
    }
    
    public TreeMap getHand() {
        return getHand(currentHandNumber);
    }
    
    public TreeMap getHand(int handNumber) {
        return (TreeMap)hands.get(new Integer(handNumber));
    }
    
    public int getCurrentHandNumber() {
        return currentHandNumber;
    }
    
    public TreeMap getPlayerScores() {
        return playerScores;
    }
    
    public TreeMap getTeamScores() {
        return teamScores;
    }
    
    /** Check if all players in the game have finished reviewing the results
      * from the last hand played. This occurs when no player is in the
      * <code>REVIEWING_RESULTS</code> state.
      * 
      * @return 
      *     <code>true</code> if all players have finished reviewing the results.
      */
    public boolean reviewPeriodDone() {
        boolean reviewPeriodDone = true;
        Iterator playersIter = getPlayers().iterator();
        while (playersIter.hasNext()) {
            Player player = (Player)playersIter.next();
            if (player.getState() == Player.REVIEWING_RESULTS) {
                reviewPeriodDone = false;
            }
        }
        return reviewPeriodDone;
    }
    
    public void writeObject(OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF(title);
        dos.writeInt(maxPlayers);
        dos.writeInt(gameType);
        dos.writeInt(endGameLimit);
        dos.writeInt(gamePhase);
        dos.writeInt(players.size());
        Iterator playersIter = players.values().iterator();
        while (playersIter.hasNext()) {
            Player player = (Player)playersIter.next();
            player.writeObject(os);
        }
        dos.writeInt(teams.size());
        Iterator teamsIter = teams.values().iterator();
        while (teamsIter.hasNext()) {
            Team team = (Team)teamsIter.next();
            team.writeObject(os);
        }
        dos.writeInt(hands.size());
        Iterator handsIter = hands.keySet().iterator();
        while (handsIter.hasNext()) {
            Integer handNumber = (Integer)handsIter.next();
            dos.writeInt(handNumber.intValue());
            TreeMap hand = (TreeMap)hands.get(handNumber);
            dos.writeInt(hand.keySet().size());
            Iterator playerIds = hand.keySet().iterator();
            while (playerIds.hasNext()) {
                String playerId = (String)playerIds.next();
                int playerChoice = ((Integer)hand.get(playerId)).intValue();
                dos.writeUTF(playerId);
                dos.writeInt(playerChoice);
            }
        }
        dos.writeInt(currentHandNumber);
        dos.writeInt(playerScores.size());
        Iterator playerIds = playerScores.keySet().iterator();
        while (playerIds.hasNext()) {
            String playerId = (String)playerIds.next();
            int playerScore = ((Integer)playerScores.get(playerId)).intValue();
            dos.writeUTF(playerId);
            dos.writeInt(playerScore);
        }
        dos.writeInt(teamScores.size());
        Iterator teamNames = teamScores.keySet().iterator();
        while (teamNames.hasNext()) {
            String teamName = (String)teamNames.next();
            int teamScore = ((Integer)teamScores.get(teamName)).intValue();
            dos.writeUTF(teamName);
            dos.writeInt(teamScore);
        }
    }
    
    public static GameState createObject(InputStream is) throws IOException {
        GameState game = new GameState();
        game.readObject(is);
        return game;
    }
    
    public void readObject(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        title = dis.readUTF();
        maxPlayers = dis.readInt();
        gameType = dis.readInt();
        endGameLimit = dis.readInt();
        gamePhase = dis.readInt();
        int numPlayers = dis.readInt();
        for (int i=0; i<numPlayers; i++) {
            Player player = Player.createObject(is);
            players.put(player.getPlayerId(), player);
        }
        numTeams = dis.readInt();
        for (int i=0; i<numTeams; i++) {
            Team team = Team.createObject(is, this);
            teams.put(team.getName(), team);
        }
        int numHands = dis.readInt();
        for (int i=0; i<numHands; i++) {
            int handNumber = dis.readInt();
            TreeMap hand = new TreeMap();
            int numPlayerChoices = dis.readInt();
            for (int j=0; j<numPlayerChoices; j++) {
                String playerId = dis.readUTF();
                Integer playerChoice = new Integer(dis.readInt());
                hand.put(playerId, playerChoice);
            }
            hands.put(new Integer(handNumber), hand);
        }
        currentHandNumber = dis.readInt();
        int numPlayerScores = dis.readInt();
        for (int i=0; i<numPlayerScores; i++) {
            String playerId = dis.readUTF();
            Integer playerScore = new Integer(dis.readInt());
            playerScores.put(playerId, playerScore);
        }
        int numTeamScores = dis.readInt();
        for (int i=0; i<numTeamScores; i++) {
            String teamName = dis.readUTF();
            Integer teamScore = new Integer(dis.readInt());
            teamScores.put(teamName, teamScore);
        }
    }
    
    //--------------------------------------------------------------------------
    // jrs.Game implementation

    public Object getId() {
        return title;
    }

    public Set getParticipantIds() {
        HashSet participantIds = new HashSet();
        Iterator players = getPlayers().iterator();
        while (players.hasNext()) {
            Player player = (Player)players.next();
            participantIds.add(player.getPlayerId());
        }
        return participantIds;
    }

    //==========================================================================
    // Team class
    
    public static class Team {

        private GameState game;
        private String name;
        private int maxPlayers;
        private ArrayList playerIds;

        // TO DO: Team Color?

        public Team(GameState gameInfo) {
            this(null, 0, gameInfo);
        }

        /**
         * Creates a new instance of Team
         */
        public Team(String name, int maxPlayers, GameState game) {
            this.name = name;
            this.maxPlayers = maxPlayers;
            this.game = game;
            playerIds = new ArrayList();
        }

        /**
         * Getter for property name.
         * @return Value of property name.
         */
        public String getName() {
            return this.name;
        }

        /**
         * Getter for property maxPlayers.
         * @return Value of property maxPlayers.
         */
        public int getMaxPlayers() {
            return this.maxPlayers;
        }

        public int getNumPlayers() {
            return playerIds.size();
        }

        public boolean isFull() {
            return (playerIds.size() >= maxPlayers);
        }

        public boolean isMember(Object playerId) {
            return playerIds.contains(playerId);
        }

        public Collection getPlayers() {
            Collection players = new HashSet();
            Iterator pids = playerIds.iterator();
            while (pids.hasNext()) {
                players.add(game.getPlayer(pids.next()));
            }
            return players;
        }
        
        public Collection getPlayerIds() {
            return playerIds;
        }

        public void addPlayer(String playerId) {
            playerIds.add(playerId);
        }

        public void writeObject(OutputStream os) throws IOException {
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(name);
            dos.writeInt(maxPlayers);
            dos.writeInt(playerIds.size());
            Iterator playerIdsIter = playerIds.iterator();
            while (playerIdsIter.hasNext()) {
                dos.writeUTF((String)playerIdsIter.next());
            }
        }

        public static Team createObject(InputStream is, GameState gameInfo) throws IOException {
            Team teamInfo = new Team(gameInfo);
            teamInfo.readObject(is);
            return teamInfo;
        }

        public void readObject(InputStream is) throws IOException {
            DataInputStream dis = new DataInputStream(is);
            name = dis.readUTF();
            maxPlayers = dis.readInt();
            int numPlayers = dis.readInt();
            for (int i=0; i<numPlayers; i++) {
                playerIds.add(dis.readUTF());
            }
        }
    }

    //==========================================================================
    // Player class
    
    public static class Player {

        public static final int NOT_A_BOT = 0;
        public static final int SMART_BOT = 1;
        public static final int REGULAR_BOT = 2;
        public static final int DUMB_BOT = 3;

        // player states
        public static final int UNKNOWN = -1;
        public static final int WAITING_TO_START = 0;
        public static final int WAITING_FOR_INPUT = 1;
        public static final int WAITING_FOR_RESULTS = 2;
        public static final int REVIEWING_RESULTS = 3;
        
        private Object playerId;
        private double rating;
        private double ratingDeviation;
        private int botType;
        private int state;


        public Player() {
            this(null, 0, 0, NOT_A_BOT);
        }

        /** Creates a new instance of PlayerInfo */
        public Player(Object playerId, double rating, double ratingDeviation) {
            this(playerId, rating, ratingDeviation, NOT_A_BOT);
        }

        public Player(Object playerId, double rating, double ratingDeviation, int botType) {
            this.playerId = playerId;
            this.rating = rating;
            this.ratingDeviation = ratingDeviation;
            this.botType = botType;
            this.state = UNKNOWN;
        }

        /**
         * Getter for property playerId.
         * @return Value of property playerId.
         */
        public Object getPlayerId() {
            return this.playerId;
        }

        /**
         * Setter for property rating.
         * @param rating New value of property rating.
         */
        public void setRating(double rating) {
            this.rating = rating;
        }

        /**
         * Getter for property rating.
         * @return Value of property rating.
         */
        public double getRating() {
            return this.rating;
        }

        /**
         * Setter for property ratingDeviation.
         * @param ratingDeviation New value of property ratingDeviation.
         */
        public void setRatingDeviation(double ratingDeviation) {
            this.ratingDeviation = ratingDeviation;
        }

        /**
         * Getter for property ratingDeviation.
         * @return Value of property ratingDeviation.
         */
        public double getRatingDeviation() {
            return this.ratingDeviation;
        }
        
        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }
        
        public void writeObject(OutputStream os) throws IOException {
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF((String)playerId);
            dos.writeDouble(rating);
            dos.writeDouble(ratingDeviation);
            dos.writeInt(botType);
            dos.writeInt(state);
        }

        public static Player createObject(InputStream is) throws IOException {
            Player player = new Player();
            player.readObject(is);
            return player;
        }

        public void readObject(InputStream is) throws IOException {
            DataInputStream dis = new DataInputStream(is);
            playerId = dis.readUTF();
            rating = dis.readDouble();
            ratingDeviation = dis.readDouble();
            botType = dis.readInt();
            state = dis.readInt();
        }
    }
}
