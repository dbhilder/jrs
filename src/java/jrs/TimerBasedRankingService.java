/*
 * TimerBasedRankingService.java
 *
 */

package jrs;

import java.util.*;

/** A subclass of <code>RankingService</code> that updates players' ratings
  * a regular intervals.
  * <p>
  * Additional methods are provided for this type of ranking service to allow
  * for starting, stopping, pausing and resuming the updates.
  *
  * @author Derek Hilder
  */
public class TimerBasedRankingService extends RankingService {
        
    /** The thread that periodically requests rating period updates. */
    private Thread updateTimer;
    
    /** The number of seconds between periodic updates. If 0, rating 
      * period updates will be triggered after a certain number of games.
      */
    private long updatePeriod;
    
    /** Flag used to indicate that the periodic update timer is running. */
    private boolean updating;
    
    /** Flag used to pause and resume the periodic updating. */
    private boolean paused;
    
    
    /** Creates an instance of the Ranking Service that updates player ratings
      * at regular intervals. Updating will not begin util the <code>startUpdating</code>
      * method is called.
      *
      * @param updatePeriod 
      *     The number of seconds between rating periods.
      */
    public TimerBasedRankingService(long updatePeriod) {
        
        super();
        
        this.updatePeriod = updatePeriod;

        // Start a timer thread to do periodic updates of player ratings
        updateTimer = new Thread() {
            public void run() {
                updating = true;
                while (updating) {

                    if (!paused) {
                        // Notify listeners that a new period is beginning
                        Iterator listenersIter = listeners.iterator();
                        while (listenersIter.hasNext()) {
                            RankingServiceListener listener = (RankingServiceListener)listenersIter.next();
                            listener.beginRatingPeriod(TimerBasedRankingService.this, periodCount);
                        }
                    }

                    try {
                        Thread.sleep(TimerBasedRankingService.this.updatePeriod * 1000);
                    }
                    catch (InterruptedException ie) {
                    }

                    if (!paused) {

                        playerRatings = computePlayerRatings(playerRatings, currentPeriodGameResults);
                        clearResults();

                        // Notify listeners that the period has ended.
                        Iterator listenersIter = listeners.iterator();
                        while (listenersIter.hasNext()) {
                            RankingServiceListener listener = (RankingServiceListener)listenersIter.next();
                            listener.endRatingPeriod(TimerBasedRankingService.this, periodCount);
                        }

                        periodCount++;
                    }
                }
            }
        };
    }
    
    /** Request that the ranking service end the current rating period. 
      * This will cause the ranking service to compute new player ratings based
      * on the results posted during the period, then a new rating period will
      * begin.
      */
    public void endPeriod() {
        updateTimer.interrupt();
    }
    
    /** Start performing periodic updates of the player ratings. This method
      * should be called after instatiating the RankingService with an
      * updatePeriod greater than 0.
      */
    public void startUpdating() {
        updateTimer.start();
    }
    
    /** Stop performing periodic updates of the player ratings. Once updating
      * has been stopped, it cannot be started again.
      */
    public void stopUpdating() {
        updating = false;
        try {
            updateTimer.join();
        }
        catch (InterruptedException ie) {
        }
    }
    
    /** Suspend periodic updating of the player ratings. This only applies to
      * RankingService instances whose updatePeriod is greater than 0.
      */
    public void pauseUpdating() {
        paused = true;
    }
    
    /** Resume periodic updating of the player ratings. This only applies to
      * RankingService instances whose updatePeriod is greater than 0.
      */
    public void resumeUpdating() {
        paused = false;
    }
}
