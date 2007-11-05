/**
 * LoadTestContentHostingService.java - content-test - 2007 Nov 05, 2007 5:03:06 PM - azeckoski
 **********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.testrunner.utils.SpringTestCase;
import org.sakaiproject.testrunner.utils.annotations.Autowired;
import org.sakaiproject.testrunner.utils.annotations.Resource;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;


/**
 * This is a test to load up the content hosting service and establish performance benchmarks <br/>
 * <br/>
 * My understanding of the content structure:<br/>
 * Content root (maybe a site) <br/>
 *  -contains- Collection <br/>
 *     -contains- ResourceProperty <br/>
 *     -contains- Resource <br/>
 *        -contains- Property <br/>
 *        -linkedfrom- AttachmentResource <br/>
 * <br/>
 * Final ratios for load testing will be:<br/>
 * read: 1 <br/>
 * create: 1/20 <br/>
 * update: 1/200 <br/>
 * delete: 1/60 <br/>
 * <br/>
 * NOTES about attempting to use CHS:<br/>
 * <ol>
 * <li>Could not figure out how to create a folder for items (Collection), had to ask</li>
 * <li>Could not understand the ids used for Collection</li>
 * <li>Many API methods are missing comments</li>
 * <li>Some methods throw 6+ exceptions which have to handled</li>
 * <li>Some methods throw contradictory exceptions, example: 
 * addCollection(String collectionId, String name) throws IdUnusedException and IdUsedException (among others)</li>
 * </ol>
 * 
 * <p>NOTE: ContentHostingService has 103 methods so this test cannot possibly be comprehensive,
 * this should have been split up a long time ago -AZ</p>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class LoadTestContentHostingService extends SpringTestCase {

   private static Log log = LogFactory.getLog(LoadTestContentHostingService.class);

   private ContentHostingService contentHostingService;
   @Autowired   
   public void setContentHostingService(ContentHostingService contentHostingService) {
      this.contentHostingService = contentHostingService;
   }

   private SessionManager sessionManager;
   @Resource(name="org.sakaiproject.tool.api.SessionManager")
   public void setSessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }

   private AuthzGroupService authzGroupService;
   @Autowired
   public void setAuthzGroupService(AuthzGroupService authzGroupService) {
      this.authzGroupService = authzGroupService;
   }

   /**
    * Number of total thread simulation iterations to run
    */
   protected final int iterations = 1000000;

   protected DecimalFormat df = new DecimalFormat("#,##0.00");
   protected Random rGen = new Random();

   protected final String testPayload = 
      "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
      "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
      "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR" +
      "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO" +
      "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" +
      "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" +
      "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE" +
      "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC" +
      "KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK" +
      "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO" +
      "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS" +
      "KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK" +
      "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII";

   protected final String INSERT = "insert";
   protected final String REMOVE = "remove";
   protected final String GET = "get";

   protected final String CURRENT_USER_MARKER = "originalTestUser";
   protected final String ADMIN_USER = "admin";

   protected final String COLLECTION_ID_PREFIX = "/LOAD_TEST_ID_PREFIX_AZ_CHS_";

   // make sure the collection names and sizes array are the same length
   protected final String[] COLLECTION_NAMES = {
         "collection_small",
         "collection_average",
         "collection_large"
      };
   protected final int[] COLLECTION_SIZES = {
         10,
         100,
         10000
      };

   private Map<String, Date> checkpointMap = new ConcurrentHashMap<String, Date>();


   @Override
   protected void setUp() throws Exception {
      super.setUp();

      // create test content in case there is not enough to test with
      // switch to the admin to run this
      Session currentSession = sessionManager.getCurrentSession();
      if (currentSession != null) {
         currentSession.setAttribute(CURRENT_USER_MARKER, currentSession.getUserId());
         currentSession.setUserId(ADMIN_USER);
         currentSession.setActive();
         sessionManager.setCurrentSession(currentSession);
         authzGroupService.refreshUser(ADMIN_USER);
      } else {
         throw new RuntimeException("no CurrentSession, cannot set to admin user");
      }
   }

   @Override
   protected void tearDown() throws Exception {
      super.tearDown();

      // switch user back (if set)
      Session currentSession = sessionManager.getCurrentSession();
      String currentUserId = null;
      if (currentSession != null) {
         currentUserId = (String) currentSession.getAttribute(CURRENT_USER_MARKER);
      }
      currentSession.setUserId(currentUserId);
      sessionManager.setCurrentSession(currentSession);
      authzGroupService.refreshUser(currentUserId);
   }


   public void testCanGetSakaiBeans() {
      assertNotNull(contentHostingService);
      assertNotNull(sessionManager);

      // also check the test arrays are ok
      assertEquals(COLLECTION_NAMES.length, COLLECTION_SIZES.length);
   }

   /**
    * creates and times a large set of content which will be used during load testing
    */
   public void testCreateLargeContentSet() {
      long start = 0;
      long total = 0;

      start = System.currentTimeMillis();
      int contentCount;
      try {
         contentCount = contentHostingService.getCollectionSize("/");
      } catch (Exception e) {
         throw new RuntimeException("Failed to get the size of the root collection (/)", e);
      }
      total = System.currentTimeMillis() - start;
      log.info("Completed count of all current content items ("+contentCount+") in "+total+" ms");
      assertTrue(contentCount > 1);

      log.info("Current content size ("+contentCount+"), Now we will create simulated content...");
      // insert all the fake content in 3 collections

      start = System.currentTimeMillis();
      int createdItems = 0;
      for (int i = 0; i < COLLECTION_NAMES.length; i++) {
         makeAndFillCollection(COLLECTION_NAMES[i], COLLECTION_SIZES[i]);
         createdItems += COLLECTION_SIZES[i];
      }
      total = System.currentTimeMillis() - start;
      log.info("Completed creation of ("+COLLECTION_NAMES.length+") collections with "+createdItems+" content items in "
            +total+" ms ("+calcUSecsPerOp(createdItems, total)+" microsecs per operation)");

      start = System.currentTimeMillis();
      contentCount = 0;
      try {
         contentCount = contentHostingService.getCollectionSize("/");
      } catch (Exception e) {
         throw new RuntimeException("Failed to get the size of the root collection (/)", e);
      }
      total = System.currentTimeMillis() - start;
      log.info("Completed count of all current content items ("+contentCount+") in "+total+" ms");
      assertTrue(contentCount > 1);
   }

   public void testRemoveLargeContentSet() {
      long start = 0;
      long total = 0;

      log.info("Test removing generated content...");
      start = System.currentTimeMillis();
      int removedItems = 0;
      for (int i = 0; i < COLLECTION_NAMES.length; i++) {
         String collectionId = makeCollectionId(COLLECTION_NAMES[i]);
         removeCollection(collectionId);
         removedItems += COLLECTION_SIZES[i];
      }
      total = System.currentTimeMillis() - start;
      log.info("Completed removal of ("+COLLECTION_NAMES.length+") created collections with "+removedItems+" content items in "
            +total+" ms ("+calcUSecsPerOp(removedItems, total)+" microsecs per operation)");
   }

   /**
    * @param collectionId
    */
   private void removeCollection(String collectionId) {
      long start = 0;
      long total = 0;

      start = System.currentTimeMillis();
      int removedItems = 0;
      try {
         removedItems = contentHostingService.getCollectionSize(collectionId);
         contentHostingService.removeCollection(collectionId);
      } catch (Exception e) {
         throw new RuntimeException("Failure removing collection: " + collectionId, e);
      }
      total = System.currentTimeMillis() - start;
      log.info("Completed removal of collection ("+collectionId+") with "+removedItems+" content items in "
            +total+" ms ("+calcUSecsPerOp(removedItems, total)+" microsecs per item)");
   }

   /**
    * Simulating load against the content service (single threaded)
    */
/*   public void testSimulatedUsageOneThread() {
      runCacheTestThread(1, 1, testCache, iterations, maxCacheSize);
   }*/

   
   /**
    * Simulating load against the content service (multi threaded)
    */
/*   public void testSimulatedUsageMultiThread() {
      final int threads = 30;
      final int threadIterations = iterations / threads;

      log.info("Starting concurrent caching load test with "+threads+" threads...");
      long start = System.currentTimeMillis();
      for (int t = 0; t < threads; t++) {
         final int threadnum = t+1;
         Thread thread = new Thread( new Runnable() {
            public void run() {
               //runCacheTestThread(threadnum, threads, testCache, threadIterations, maxCacheSize);
            }
         }, threadnum+"");
         thread.start();
      }
      startThreadMonitor();
      long total = System.currentTimeMillis() - start;
      log.info(threads + " threads completed "+iterations+" iterations in "
            +total+" ms ("+calcUSecsPerOp(iterations, total)+" microsecs per iteration)");
   }*/


/*   private void runTestThread(int threadnum, int threads, Cache testCache, final int iterations,
         long maxCacheSize) {
      long missCount = 0;
      long hitCount = 0;
      long readCount = 0;
      int insertCount = 0;
      int deleteCount = 0;
      Random rGen = new Random();
      String keyPrefix = "key-" + threadnum + "-";
      checkpointMap.put(Thread.currentThread().getName(), new Date());
      long start = System.currentTimeMillis();
      for (int i = 0; i < iterations; i++) {
         int random = rGen.nextInt(100);
         if ( (i < 100 || random >= 95) && ((insertCount*threads) < maxCacheSize) ) {
            int num = insertCount++;
            testCache.put(keyPrefix + num, "Number=" + num + ": " + testPayload);
         }
         if (i > 2) {
            // do 10 reads from this threads cache
            for (int j = 0; j < 10; j++) {
               readCount++;
               if (testCache.get(keyPrefix + rGen.nextInt(insertCount)) == null) {
                  missCount++;
               } else {
                  hitCount++;
               }
            }
            // do 5 more from a random threads cache
            String otherKeyPrefix = "key-" + (rGen.nextInt(threads)+1) + "-";
            for (int j = 0; j < 5; j++) {
               readCount++;
               if (testCache.get(otherKeyPrefix + rGen.nextInt(insertCount)) == null) {
                  missCount++;
               } else {
                  hitCount++;
               }
            }
         }
         if ( random < 1 && ((deleteCount*threads) < (maxCacheSize/8)) ) {
            testCache.remove(keyPrefix + rGen.nextInt(insertCount));
            deleteCount++;
         }
         if (i > 0 && i % (iterations/5) == 0) {
            checkpointMap.put(Thread.currentThread().getName(), new Date());
            //log.info("thread: " + threadnum + " " + (i*100/iterations) + "% complete");
         }
      }
      long total = System.currentTimeMillis() - start;
      checkpointMap.remove(Thread.currentThread().getName());
      final String hitPercentage = ((hitCount+missCount) > 0) ? ((100l * hitCount) / (hitCount + missCount)) + "%" : "N/A";
      log.info("Thread "+threadnum+": completed "+iterations+" iterations with "+insertCount+" inserts " +
      		"and "+deleteCount+" removes and "+readCount+" reads " +
      		"(hits: " + hitCount + ", misses: " + missCount + ", hit%: "+hitPercentage+") " +
      		"in "+total+" ms ("+calcUSecsPerOp(iterations, total)+" microsecs per iteration)");
   }*/


   /**
    * Make a content collection and fill it to the size specified with fake data
    * @param name
    * @param size
    */
   private void makeAndFillCollection(String name, int collectionSize) {
      // Small collection
      long start = System.currentTimeMillis();
      String collectionId = makeCollection(name);
      for (int i = 0; i < collectionSize; i++) {
         String rid = name + i;
         try {
            ContentResource resource = contentHostingService.addResource(rid, collectionId, 3, "text/plain", makeTestContent(), null, 0);
            assertNotNull(resource);
         } catch (Exception e) {
            throw new RuntimeException("Died while attempting to add a resource ("+rid+") to collection: " + collectionId, e);
         }
      }
      long total = System.currentTimeMillis() - start;
      log.info("Completed creation of collection ("+collectionId+") with "+collectionSize+" content items in "
            +total+" ms ("+calcUSecsPerOp(iterations, total)+" microsecs per operation)");
   }

   /**
    * @return some generated content to drop into a resource
    */
   private byte[] makeTestContent() {
      StringBuilder sb = new StringBuilder();
      sb.append(COLLECTION_ID_PREFIX);
      sb.append(": ");
      for (int i = 0; i < (20 + rGen.nextInt(10000)); i++) {
         sb.append(":>");
         sb.append(i);
         sb.append("<:");
         sb.append(testPayload);
      }
      return sb.toString().getBytes();
   }

   /**
    * @return the id of the newly created collection or the existing one with this id
    */
   private String makeCollection(String cid) {
      String testId = makeCollectionId(cid);
      String collectionId = null; // this is everything after "/content", Reference includes the "/content"
      try {
         ContentCollectionEdit collection = contentHostingService.addCollection(testId);
         collection.getPropertiesEdit().addProperty(COLLECTION_ID_PREFIX+"LOCATOR", testId);
         contentHostingService.commitCollection(collection);
         collectionId = collection.getId();
      } catch (PermissionException e) {
         throw new SecurityException("Invalid permissions to create collection: " + cid, e);
      } catch (IdUsedException e) {
         // this is ok, just get the existing one
         try {
            collectionId = contentHostingService.getCollection(testId).getId();
         } catch (Exception ex) {
            throw new RuntimeException("Died horribly trying to get the existing collection: " + testId, e);
         }
      } catch (IdInvalidException e) {
         throw new IllegalArgumentException("Id is invalid: " + testId, e);
      } catch (InconsistentException e) {
         throw new RuntimeException("Died horribly trying to create the collection: " + testId, e);
      }
      assertNotNull(collectionId);
      return collectionId;
   }

   /**
    * @param cid
    * @return
    */
   private String makeCollectionId(String cid) {
      return COLLECTION_ID_PREFIX+cid+"/";
   }

   /**
    * @param loopCount total number of operations
    * @param totalMilliSecs total number of milliseconds
    * @return the number of microsecs per operation
    */
   private String calcUSecsPerOp(long loopCount, long totalMilliSecs) {
      return df.format(((double)(totalMilliSecs * 1000))/((double)loopCount));
   }

   /**
    * Monitor the other test threads and block this test from completing until all test threads complete
    */
   private void startThreadMonitor() {
      // monitor the other running threads
      Map<String, Date> m = new HashMap<String, Date>();
      log.info("Starting up monitoring of test threads...");
      try {
         Thread.sleep(3 * 1000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      while (true) {
         if (checkpointMap.size() == 0) {
            log.info("All test threads complete... monitoring exiting");
            break;
         }
         int deadlocks = 0;
         List<String> stalledThreads = new ArrayList<String>();
         for (String key : checkpointMap.keySet()) {
            if (m.containsKey(key)) {
               if (m.get(key).equals(checkpointMap.get(key))) {
                  double stallTime = (new Date().getTime() - checkpointMap.get(key).getTime()) / 1000.0d;
                  stalledThreads.add(df.format(stallTime) + ":" + key);
                  deadlocks++;
               }
            }
            m.put(key, checkpointMap.get(key));
         }

         StringBuilder sb = new StringBuilder();
         sb.append("Deadlocked/slow threads (of "+checkpointMap.size()+"): ");
         if (stalledThreads.isEmpty()) {
            sb.append("NONE");
         } else {
            sb.append("total="+stalledThreads.size()+":: ");
            Collections.sort(stalledThreads);
            for (int j = stalledThreads.size()-1; j >= 0; j--) {
               String string = stalledThreads.get(j);
               sb.append(string.substring(string.indexOf(':')+1) + "(" + string.substring(0, string.indexOf(':')) + "s):");
            }
         }
         log.info(sb.toString());

         try {
            Thread.sleep(2 * 1000);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }

}

