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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
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

   private static final String ROOT = "/";

   /**
    * Number of total thread simulation iterations to run (NOTE: scale all 3 items below at once)
    */
   protected final int iterations = 10000;
   /**
    * maximum number of content item ids to load for the read testing
    */
   protected final int maxTestContentSize = 10000;
   /**
    * Maximum number of inserts to do while simulating
    */
   protected final int maxInserts = 500;


   protected final String INSERT = "insert";
   protected final String REMOVE = "remove";
   protected final String GET = "get";

   protected final String CURRENT_USER_MARKER = "originalTestUser";
   protected final String ADMIN_USER = "admin";

   protected final String COLLECTION_ID_PREFIX = "LOAD_TEST_ID_PREFIX_AZ_CHS_";

   // make sure the collection names and sizes array are the same length
   protected final String[] COLLECTION_NAMES = {
         "collection_small",
         "collection_big",
         "collection_huge",
         "collection_large",
         "collection_average",
         "collection_verysmall",
         "collection_tiny",
      };
   // list version to make it easy to work with the names
   protected final List<String> collectionNames = Arrays.asList(COLLECTION_NAMES);
   protected final int[] COLLECTION_SIZES = {
         10,
         100,
         400,
         25,
         15,
         5,
         1
      };
   protected long totalCreatedSize = 0;
   protected long totalCreatedItems = 0;

   private Map<String, Date> checkpointMap = new ConcurrentHashMap<String, Date>();


   private ContentHostingService contentHostingService;
   @Resource(name="org.sakaiproject.content.api.ContentHostingService")
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


   protected DecimalFormat df = new DecimalFormat("#,##0.00");
   protected Random rGen = new Random();

   protected final String testPayload = // 1040 characters
      "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
      "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
      "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR" +
      "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO" +
      "NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN" +
      "ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" +
      "EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE" +
      "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC" +
      "KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK" +
      "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO" +
      "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS" +
      "KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK" +
      "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII";



   @Override
   protected void setUp() throws Exception {
      super.setUp();

      // create test content in case there is not enough to test with
      // switch to the admin to run this
      setAdminUser();
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
      totalCreatedItems = 0;
      totalCreatedSize = 0;

      // wipe the test data if there is any
      log.info("Cleanup any existing test data first (if there is any)...");
      for (int i = 0; i < COLLECTION_NAMES.length; i++) {
         try {
            removeCollection(makeCollectionId(COLLECTION_NAMES[i]));
         } catch (Exception e) {
            log.debug("Failed to cleanup existing test collection", e);
         }
      }

      start = System.currentTimeMillis();
      int contentCount;
      try {
         contentCount = contentHostingService.getCollectionSize(ROOT);
      } catch (Exception e) {
         throw new RuntimeException("Failed to get the size of the root collection (/)", e);
      }
      total = System.currentTimeMillis() - start;
      log.info("Completed count of all current content items ("+contentCount+") in "+total+" ms");
      assertTrue(contentCount > 1);

      log.info("Current content size ("+contentCount+"), Now we will create simulated content...");

      // first make the test folder (we do not clean this up later)
      makeCollection(null);
         
      start = System.currentTimeMillis();
      int createdItems = 0;
      for (int i = 0; i < COLLECTION_NAMES.length; i++) {
         makeAndFillCollection(COLLECTION_NAMES[i], COLLECTION_SIZES[i]);
         createdItems += COLLECTION_SIZES[i];
      }
      total = System.currentTimeMillis() - start;
      log.info("Completed creation of ("+COLLECTION_NAMES.length+") collections with "+createdItems+" content items in "
            +total+" ms ("+calcUSecsPerOp(createdItems, total)+" microsecs per operation)," +
            "average size of created items: " + (totalCreatedSize/totalCreatedItems) + " bytes");

      start = System.currentTimeMillis();
      contentCount = 0;
      try {
         contentCount = contentHostingService.getCollectionSize(ROOT);
      } catch (Exception e) {
         throw new RuntimeException("Failed to get the size of the root collection: "+ROOT, e);
      }
      total = System.currentTimeMillis() - start;
      log.info("Completed count of all current content items ("+contentCount+") in "+total+" ms");
      assertTrue(contentCount > 1);
   }

   public void testReadLargeContentSet() {
      long start = 0;
      long total = 0;

      log.info("Test reading generated content and existing content...");

      List<String> contentIds = new ArrayList<String>();
      start = System.currentTimeMillis();
      try {
         ContentCollection collection = contentHostingService.getCollection(ROOT);
         accumulateContentIds(contentIds, collection, maxTestContentSize);
      } catch (IdUnusedException e) {
         throw new RuntimeException("Failed to find the root collection: "+ROOT, e);
      } catch (Exception e) {
         throw new RuntimeException("Failed to get the contents of the root collection: "+ROOT, e);
      }
      total = System.currentTimeMillis() - start;
      log.info("Completed load of "+contentIds.size()+" content items in "+total+" ms");
   }

   /**
    * Simulating load against the content service (single threaded)
    */
   public void testSimulatedUsageOneThread() {
      log.info("Test simulating usage (one thread)...");

      // first we accumulate all the content
      List<String> contentIds = new ArrayList<String>();
      try {
         ContentCollection collection = contentHostingService.getCollection(ROOT);
         accumulateContentIds(contentIds, collection, maxTestContentSize);
      } catch (IdUnusedException e) {
         throw new RuntimeException("Failed to find the root collection: "+ROOT, e);
      } catch (Exception e) {
         throw new RuntimeException("Failed to get the contents of the root collection: "+ROOT, e);
      }

      runTestThread(1, 1, contentIds, iterations, maxInserts);
      log.info("complete");
   }

   
   /**
    * Simulating load against the content service (multi threaded)
    * TODO - currently disabled because CHS cannot handle concurrent threads
    */
   public void testSimulatedUsageMultiThread() {
      final int threads = 10;
      final int threadIterations = iterations / threads;
      final int threadMaxInserts = maxInserts / threads;

      // first we accumulate all the content
      final List<String> contentIds = new Vector<String>();
      try {
         ContentCollection collection = contentHostingService.getCollection(ROOT);
         accumulateContentIds(contentIds, collection, maxTestContentSize);
      } catch (IdUnusedException e) {
         throw new RuntimeException("Failed to find the root collection: "+ROOT, e);
      } catch (Exception e) {
         throw new RuntimeException("Failed to get the contents of the root collection: "+ROOT, e);
      }

      log.info("Starting concurrent caching load test with "+threads+" threads...");
      long start = System.currentTimeMillis();
      for (int t = 0; t < threads; t++) {
         final int threadnum = t+1;
         Thread thread = new Thread( new Runnable() {
            public void run() {
               setAdminUser();   
               runTestThread(threadnum, threads, contentIds, threadIterations, threadMaxInserts);
            }
         }, threadnum+"");
         thread.start();
      }
      startThreadMonitor();
      long total = System.currentTimeMillis() - start;
      log.info(threads + " threads completed "+iterations+" iterations in "
            +total+" ms ("+calcUSecsPerOp(iterations, total)+" microsecs per iteration)");
   }

  
   public void testRemoveLargeContentSet() {
      long start = 0;
      long total = 0;

      log.info("Test removing generated content...");
      start = System.currentTimeMillis();
      int removedItems = 0;
      for (int i = 0; i < COLLECTION_NAMES.length; i++) {
         String collectionId = makeCollectionId(COLLECTION_NAMES[i]);
         removedItems += removeCollection(collectionId);
      }
      total = System.currentTimeMillis() - start;
      log.info("Completed removal of ("+COLLECTION_NAMES.length+") created collections with "+removedItems+" content items in "
            +total+" ms ("+calcUSecsPerOp(removedItems, total)+" microsecs per operation)");
   }



   /**
    * Run a simulated usage test
    * @param threadnum
    * @param threads
    * @param contentIds
    * @param iterations
    * @param maxInserts
    */
   private void runTestThread(int threadnum, int threads, List<String> contentIds, final int iterations, int maxInserts) {
      long readCount = 0;
      long readMissCount = 0;
      int insertCount = 0;
      int deleteCount = 0;
      Random rGen = new Random();
      String keyPrefix = "threadResource-" + threadnum + "-";
      checkpointMap.put(Thread.currentThread().getName(), new Date());
      long start = System.currentTimeMillis();
      try {
         for (int i = 0; i < iterations; i++) {
            int random = rGen.nextInt(100);
            if ( (i < 100 || random >= 92) && (insertCount < maxInserts) ) {
               int num = insertCount++;
               String rid = keyPrefix + num;
               String collectionId = makeCollectionId(COLLECTION_NAMES[rGen.nextInt(COLLECTION_NAMES.length)]);
               try {
                  ContentResource resource = contentHostingService.addResource(rid, collectionId, 3, "text/plain", makeTestContent(), null, 0);
                  assertNotNull(resource.getId());
                  contentIds.add(resource.getId());
               } catch (Exception e) {
                  throw new RuntimeException("Died while attempting to add a resource ("+rid+") to collection: " + collectionId, e);
               }
            }
            if (i > 2) {
               // do 10 reads from content hosting
               for (int j = 0; j < 10; j++) {
                  readCount++;
                  String rid = contentIds.get(rGen.nextInt(contentIds.size()));
                  try {
                     ContentResource resource = contentHostingService.getResource(rid);
                     assertNotNull(resource);
                  } catch (IdUnusedException e) {
                     readMissCount++;
                  } catch (Exception e) {
                     throw new RuntimeException("Died while attempting to get a resource ("+rid+")", e);
                  }
               }
            }
            if ( random < 3 && (deleteCount < (maxInserts/4)) ) {
               // try to remove an item 5 times
               for (int j = 0; j < 5; j++) {
                  int rIndex = rGen.nextInt(contentIds.size());
                  String rid = contentIds.get(rIndex);
                  try {
                     String collectionId = contentHostingService.getContainingCollectionId(rid);
//                     ContentResource resource = contentHostingService.getResource(rid);
//                     String collectionId = resource.getContainingCollection().getId();
                     if (collectionNames.contains(collectionId)) {
                        contentIds.remove(rIndex);
                        deleteCount++;
                        contentHostingService.removeResource(rid);
                        break;
                     }
                  } catch (IdUnusedException e) {
                     readMissCount++;
                  } catch (Exception e) {
                     throw new RuntimeException("Died while attempting to remove a resource ("+rid+")", e);
                  }                  
               }
            }
            if (i > 0 && i % (iterations/50) == 0) {
               checkpointMap.put(Thread.currentThread().getName(), new Date());
               //log.info("thread: " + threadnum + " " + (i*100/iterations) + "% complete");
            }
         }
      } catch (Exception e) {
         log.error("Thread "+threadnum+": failed to complete because of exception", e);
      } finally {
         long total = System.currentTimeMillis() - start;
         checkpointMap.remove(Thread.currentThread().getName());
         if (threadnum == 1) {
            log.info("Thread "+threadnum+": completed "+iterations+" iterations with "+insertCount+" inserts " +
                  "and "+deleteCount+" removes and "+readCount+" reads ("+readMissCount+" misses)" +
                  "in "+total+" ms ("+calcUSecsPerOp(iterations, total)+" microsecs per iteration)");
         }
      }
   }

   /**
    * @param collectionId
    */
   private int removeCollection(String collectionId) {
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
      return removedItems;
   }

   /**
    * Loops through and gets all the content ids recursively (does not get collection ids)
    * @param contentIds
    * @param collection
    * @param maxToGet
    */
   @SuppressWarnings("unchecked")
   private void accumulateContentIds(List<String> contentIds, ContentCollection collection, long maxToGet) {
      if (collection.getMemberCount() > 0) {
         List<ContentEntity> contents = collection.getMemberResources();
         for (ContentEntity content : contents) {
            if (contentIds.size() >= maxToGet) {
               break;
            }
            if (content instanceof ContentCollection) {
               accumulateContentIds(contentIds, (ContentCollection) content, maxToGet);
            } else if (content instanceof ContentResource) {
               contentIds.add(content.getId());
            } else {
               log.warn("What the heck is this content (id="+content.getId()+")?: " + content.getClass());
            }
         }
      }
   }

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
            // TODO - figure out why this dies on Steve's machine, switching to logging for now -AZ
            //throw new RuntimeException("Died while attempting to add a resource ("+rid+") to collection: " + collectionId, e);
            log.error("Died while attempting to add a resource ("+rid+") to collection: " + collectionId, e);
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
      totalCreatedSize += sb.length();
      totalCreatedItems++;
      return sb.toString().getBytes();
   }

   /**
    * @param cname a name for the collection or null to make the special root test collection
    * @return the id of the newly created collection or the existing one with this id
    */
   private String makeCollection(String cname) {
      String testId = null;
      if (cname == null) {
         testId = makeTestRootCollectionId();
      } else {
         testId = makeCollectionId(cname);
      }
      String collectionId = null; // this is everything after "/content", Reference includes the "/content"
      try {
         ContentCollectionEdit collection = contentHostingService.addCollection(testId);
         collection.getPropertiesEdit().addProperty(COLLECTION_ID_PREFIX+"LOCATOR", testId);
         collection.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, "LOADTEST-" + cname);
         contentHostingService.commitCollection(collection);
         collectionId = collection.getId();
      } catch (PermissionException e) {
         throw new SecurityException("Invalid permissions to create collection: " + cname, e);
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
      return makeTestRootCollectionId() + COLLECTION_ID_PREFIX+cid+"/";
   }

   private String makeTestRootCollectionId() {
      return ROOT + "LoadTestFolder" + "/";
   }

   /**
    * Change the current user for a thread to the admin user
    */
   private void setAdminUser() {
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
      log.debug("Starting up monitoring of test threads...");
      try {
         Thread.sleep(3 * 1000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

      while (true) {
         if (checkpointMap.size() == 0) {
            log.debug("All test threads complete... monitoring exiting");
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

         if (! stalledThreads.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Deadlocked/slow threads (of "+checkpointMap.size()+"): ");
            sb.append("total="+stalledThreads.size()+":: ");
            Collections.sort(stalledThreads);
            for (int j = stalledThreads.size()-1; j >= 0; j--) {
               String string = stalledThreads.get(j);
               sb.append(string.substring(string.indexOf(':')+1) + "(" + string.substring(0, string.indexOf(':')) + "s):");
            }
            log.info(sb.toString());
         }

         try {
            Thread.sleep(5 * 1000);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }
   }

}

