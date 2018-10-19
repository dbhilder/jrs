/*
 * RankingServiceTest.java
 */

package jrs;

import junit.framework.*;
import java.util.*;

/**
  *
  * @author Derek Hilder
  */
public class RankingServiceTest extends TestCase {
    
    public RankingServiceTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(RankingServiceTest.class);
        
        return suite;
    }

    /**
     * Test of registerPlayer method, of class jrs.RankingService.
     */
//    public void testRegisterPlayer() {
//        System.out.println("registerPlayer");
//        
//        Object playerId = null;
//        RankingService instance = new RankingService();
//        
//        instance.registerPlayer(playerId);
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }

    /**
     * Test of deregisterPlayer method, of class jrs.RankingService.
     */
//    public void testDeregisterPlayer() {
//        System.out.println("deregisterPlayer");
//        
//        Object playerId = null;
//        RankingService instance = new RankingService();
//        
//        instance.deregisterPlayer(playerId);
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }

    /**
     * Test of postResults method, of class jrs.RankingService.
     */
//    public void testPostResults() {
//        System.out.println("postResults");
//        
//        Object winnersId = null;
//        int winnersScore = 0;
//        Object losersId = null;
//        int losersScore = 0;
//        RankingService instance = new RankingService();
//        
//        instance.postResults(winnersId, winnersScore, losersId, losersScore);
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }

    /** Test the jrs.RankingService.computePlayerRatings() method by supplying
      * the same inputs as the sample calculation in the paper detailing the 
      * Glicko2 algorithm and verifying that that the method returns the expected
      * values for rating, rating deviation and rating volatility.
      */
    public void testComputePlayerRatings() {
        
        Long pid0 = new Long(0);
        Long pid1 = new Long(1);
        Long pid2 = new Long(2);
        Long pid3 = new Long(3);
        
        HashMap playerRatings = new HashMap();
        playerRatings.put(pid0, new PlayerRating(pid0, 1500, 200, 0.06));
        playerRatings.put(pid1, new PlayerRating(pid1, 1400, 30, 0.06));
        playerRatings.put(pid2, new PlayerRating(pid2, 1550, 100, 0.06));
        playerRatings.put(pid3, new PlayerRating(pid3, 1700, 300, 0.06));
        
        System.setProperty("jrs.glicko2SystemConstant", "0.5");
        System.setProperty("jrs.downweightResults", "false");
        
        HashMap gameResults = new HashMap();
        gameResults.put(pid0, new PairWiseGameResultsList());
        gameResults.put(pid1, new PairWiseGameResultsList());
        gameResults.put(pid2, new PairWiseGameResultsList());
        gameResults.put(pid3, new PairWiseGameResultsList());
        
        // pid0 beats pid1
        ((List)gameResults.get(pid0)).add(new PairWiseGameResult(pid1, 1));
        ((List)gameResults.get(pid1)).add(new PairWiseGameResult(pid0, 0));
        
        // pid0 loses to pid2
        ((List)gameResults.get(pid0)).add(new PairWiseGameResult(pid2, 0));
        ((List)gameResults.get(pid2)).add(new PairWiseGameResult(pid0, 1));
        
        // pid0 loses to pid3
        ((List)gameResults.get(pid0)).add(new PairWiseGameResult(pid3, 0));
        ((List)gameResults.get(pid3)).add(new PairWiseGameResult(pid0, 1));
        
        HashMap postPeriodPlayerRatings = new RankingService().computePlayerRatings(playerRatings, gameResults);
        
        PlayerRating postPeriodPlayerRating = (PlayerRating)postPeriodPlayerRatings.get(pid0);
        double postPeriodRating = postPeriodPlayerRating.getRating();
        assertTrue("Post period rating of " + postPeriodRating + " does not match expected value of 1464.06.",
                   Math.abs(1464.06 - postPeriodPlayerRating.getRating()) < .01);
        double postPeriodRatingDeviation = postPeriodPlayerRating.getRatingDeviation();
        assertTrue("Post period rating deviation of " + postPeriodRatingDeviation + " does not match expected value of 151.52.",
                   Math.abs(151.52 - postPeriodPlayerRating.getRatingDeviation()) < .01);
        double postPeriodRatingVolatility = postPeriodPlayerRating.getRatingVolatility();
        assertTrue("Post period rating volatility of " + postPeriodRatingVolatility + " does not match expected value of 0.05999.",
                   Math.abs(0.05999 - postPeriodRatingVolatility) < .0001);
    }

//    /**
//     * Test of getMatches method, of class jrs.RankingService.
//     */
//    public void testGetMatches() {
//        System.out.println("getMatches");
//        
//        Object playerId = null;
//        int numMatches = 0;
//        RankingService instance = new RankingService();
//        
//        Object[] expResult = null;
//        Object[] result = instance.getMatches(playerId, numMatches);
//        assertEquals(expResult, result);
//        
//        // TODO add your test code below by replacing the default call to fail.
//        fail("The test case is empty.");
//    }
    
}
