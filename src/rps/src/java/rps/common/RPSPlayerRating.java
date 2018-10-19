/*
 * RPSPlayerRating.java
 *
 */

package rps.common;

import jrs.*;
import java.io.*;

/** An extension of <code>PlayerRating</code> that supports reading and writing
  * to a data stream.
  *
  * @author dhilder
  */
public class RPSPlayerRating extends PlayerRating /*implements Comparable*/ {
    
    /** 
      */
    public RPSPlayerRating() {
        this(null, 0, 0, 0);
    }
    
    /**
     * 
     * @param playerId 
     * @param rating 
     * @param ratingDeviation 
     * @param ratingVolatility 
     */
    public RPSPlayerRating(String playerId, double rating, double ratingDeviation, double ratingVolatility) {
        super(playerId, rating, ratingDeviation, ratingVolatility);
    }
    
    /**
     * 
     * @param os 
     * @throws java.io.IOException 
     */
    public void writeObject(OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        dos.writeUTF((String)playerId);
        dos.writeDouble(rating);
        dos.writeDouble(ratingDeviation);
    }
    
    /**
     * 
     * @param is 
     * @throws java.io.IOException 
     * @return 
     */
    public static RPSPlayerRating createObject(InputStream is) throws IOException {
        RPSPlayerRating playerRating = new RPSPlayerRating();
        playerRating.readObject(is);
        return playerRating;
    }
    
    /**
     * 
     * @param is 
     * @throws java.io.IOException 
     */
    public void readObject(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        playerId = dis.readUTF();
        rating = dis.readDouble();
        ratingDeviation = dis.readDouble();
    }
    
}
