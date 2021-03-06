/*
 * PlayerRating.java
 *
 */

package jrs;

/** A class representing a player's rating, as computed by the ranking service.
  * <p>
  * The ranking service does not compute true ratings, but rather gives an
  * estimate of the player's rating in the form of a confidence interval. The
  * interval is specified by a <i>mean</i> rating, and a rating <i>deviation</i>
  * which expresses the uncertainty in the rating. Additionally, ratings are
  * assigned a volatility, which reflects how consistently the rating has 
  * changed over time.
  *
  * @author Derek Hilder
  */
public class PlayerRating implements Comparable {

    private static final double GLICKO2_SCALE = 173.7178;
    
    protected Object playerId;
    protected double rating;
    protected double ratingDeviation;
    protected double ratingVolatility;
    
    
    /** Create a PlayerRating instance using rating parameters specified in the
      * standard Glicko scale.
      *
      * @param playerId 
      *     The unique identifier for the player to whom the rating applies.
      * @param rating 
      *     The mean rating of the player's skill.
      * @param ratingDeviation
      *     The deviation in the rating.
      * @param ratingVolatility 
      *     The volatility of the rating.
      *
      * @todo
      *     Should the player's id be passed in too?
      */
    public PlayerRating(Object playerId, double rating, double ratingDeviation, double ratingVolatility) {
        this(playerId, rating, ratingDeviation, ratingVolatility, false);
    }
    
    /** Create a PlayerRating instance using the specified rating parameters.
      * 
      * @param playerId 
      *     The unique identifier for the player to whom the rating applies.
      * @param rating 
      *     The mean rating of the player's skill.
      * @param ratingDeviation
      *     The deviation in the rating.
      * @param ratingVolatility 
      *     The volatility of the rating.
      * @param glicko2Scale 
      *     If <code>true</code>, the parameters are assumed to have been specified
      *     in the Glicko2 scale.
      *
      * @todo
      *     Should the player's id be passed in too?
      */
    public PlayerRating(Object playerId, double rating, double ratingDeviation, 
                        double ratingVolatility, boolean glicko2Scale) 
    {
        this.playerId = playerId;
        if (glicko2Scale) {
            setGlicko2Rating(rating);
            setGlicko2RatingDeviation(ratingDeviation);
        }
        else {
            setRating(rating);
            setRatingDeviation(ratingDeviation);
        }
        setRatingVolatility(ratingVolatility);
    }
    
    /** Get the Id of the player being rated.
      * 
      * @return 
      *     An <code>Object</code> that uniquely identifies the player.
      */
    public Object getPlayerId() {
        return playerId;
    }

    /** Get the mean rating of the player's skill.
      * 
      * @return 
      *     The player's rating.
      */
    public double getRating() {
        return rating;
    }

    /** Get the mean rating of the player's skill in the
      * Glicko2 scale.
      * 
      * @return 
      *     The player's rating, in the Glicko2 scale.
      */
    double getGlicko2Rating() {
        // TO DO: cache this result?
        return (rating - 1500) / GLICKO2_SCALE;
    }
    
    /** Set the player's rating.
      * 
      * @param rating 
      *     A standard glicko rating.
      */
    void setRating(double rating) {
        this.rating = rating;
    }
    
    /** Set the player's rating in the Glicko2 scale.
      * 
      * @param glicko2Rating 
      *     A Glicko2 rating.
      */
    void setGlicko2Rating(double glicko2Rating) {
        rating = (GLICKO2_SCALE * glicko2Rating) + 1500;
    }

    /** Get the rating deviation.
      * 
      * @return 
      *     A standard Glicko rating deviation.
      */
    public double getRatingDeviation() {
        return ratingDeviation;
    }
    
    /** Get the rating deviation in the Glicko2 scale.
      * 
      * @return 
      *     A Glicko2 rating deviation.
      */
    double getGlicko2RatingDeviation() {
        // TO DO: cache this result?
        return ratingDeviation / GLICKO2_SCALE;
    }

    /** Set the rating deviation.
      * 
      * @param ratingDeviation 
      *     A standard Glicko rating deviation.
      */
    void setRatingDeviation(double ratingDeviation) {
        this.ratingDeviation = ratingDeviation;
    }
    
    /** Set the rating deviation in the Glicko2 scale.
      * 
      * @param ratingDeviation 
      *     A Glicko2 rating deviation.
      */
    void setGlicko2RatingDeviation(double glicko2RatingDeviation) {
        ratingDeviation = GLICKO2_SCALE * glicko2RatingDeviation;
    }

    /** Get the rating volatility.
      * 
      * @return 
      *     A rating volatility.
      */
    public double getRatingVolatility() {
        return ratingVolatility;
    }

    /** Set the rating volatility.
      * 
      * @param ratingVolatility 
      *     A rating volatility.
      */
    void setRatingVolatility(double ratingVolatility) {
        this.ratingVolatility = ratingVolatility;
    }
    
    /** Determine if the object is equals to this <code>PlayerRating</code> object.
      * 
      * @param o 
      *     The object to compare to.
      * @return 
      *     <code>true</code> if the objects are equal.
      */
    public boolean equals(Object o) {
        PlayerRating other = (PlayerRating)o;
        return (compareTo(o) == 0);
    }
    
    
    /** Compare the object to this <code>PlayerRating</code> object. The comparison
      * first considers the rating, and if both ratings are the same, then it
      * compares the rating deviation. An object with a larger rating deviation
      * is considered to be less than an object with a smaller rating deviation.
      * If both the rating and the rating deviation are equal, then the player Id
      * is compared. If the player Id object does not implement <code>Comparable</code>,
      * then the two objects are considered equal.
      * 
      * @param o 
      *     The object to compare to.
      * @return 
      *     <code>-1</code> if this object is less than the specified object,
      *     <code>0</code> if it is equal to the specified object, and
      *     <code>1</code> if it is greater than the specified object.
      */
    public int compareTo(Object o) {
        PlayerRating other = (PlayerRating)o;
        if (this.rating < other.rating) {
            return 1;
        }
        else if (this.rating > other.rating) {
            return -1;
        }
        else {
            if (this.ratingDeviation < other.ratingDeviation) {
                return -1;
            }
            else if (this.ratingDeviation > other.ratingDeviation) {
                return 1;
            }
            else {
                if (this.playerId instanceof Comparable) {
                    return ((Comparable)this.playerId).compareTo(other.playerId);
                }
                else {
                    return 0;
                }
            }
        }
    }
}
