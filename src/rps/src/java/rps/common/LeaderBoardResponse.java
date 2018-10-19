/*
 * LeaderBoardResponse.java
 *
 */

package rps.common;

import java.io.*;
import java.util.*;

/**
 *
 * @author dhilder
 */
public class LeaderBoardResponse extends RPSMessage {
    
    private TreeSet playerRatings;
    
    public LeaderBoardResponse() {
        this(new TreeSet());
    }
    
    public LeaderBoardResponse(TreeSet playerRatings) {
        this.playerRatings = playerRatings;
    }
    
    public void writeMessage(OutputStream os) throws IOException {
        super.writeMessage(os);
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeInt(playerRatings.size());
        Iterator playerRatingsIter = playerRatings.iterator();
        while (playerRatingsIter.hasNext()) {
            RPSPlayerRating playerRating = (RPSPlayerRating)playerRatingsIter.next();
            playerRating.writeObject(os);
        }
    }
    
    public void readMessage(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        int numPlayerRatings = dis.readInt();
        for (int i=0; i<numPlayerRatings; i++) {
            RPSPlayerRating playerRating = RPSPlayerRating.createObject(is);
            playerRatings.add(playerRating);
        }
    }
    
    public Set getPlayerRatings() {
        return playerRatings;
    }
}
