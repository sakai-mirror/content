/**
 * ListPerformanceTesting.java - content-test - 2007 Nov 10, 2007 7:19:07 PM - azeckoski
 */

package org.sakaiproject.content.test.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

/**
 * Testing the performance of various types of lists
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ListPerformanceTesting extends TestCase {

   private Map<String, Date> checkpointMap = new ConcurrentHashMap<String, Date>();
   /**
    * Number of total thread simulation iterations to run
    */
   protected final int totalIterations = 10000000;
   protected final int threads = 50;
   protected final int prefillSize = 100;


   protected DecimalFormat df = new DecimalFormat("#,##0.00");
   protected Random rGen = new Random();

   public void testArrayList() {
      List<int[]> list = new ArrayList<int[]>();
      log.info("testArrayList: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runTestThread(1, 1, list, totalIterations);
   }

/*   public void testSyncedArrayList() {
      List<int[]> list = Collections.synchronizedList(new ArrayList<int[]>());
      log.info("testSyncedArrayList: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runTestThread(1, 1, list, totalIterations);
   }*/

   public void testVector() {
      List<int[]> list = new Vector<int[]>();
      log.info("testVector: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runTestThread(1, 1, list, totalIterations);
   }

   public void testConcurrentList() {
      List<int[]> list = new ConcurrentList<int[]>();
      log.info("testConcurrentList: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runTestThread(1, 1, list, totalIterations);
   }

   public void testArrayListInserts() {
      List<int[]> list = new ArrayList<int[]>();
      log.info("testArrayListInserts: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runInsertTestThread(1, 1, list, totalIterations);
   }

/*   public void testSyncedArrayListInserts() {
      List<int[]> list = Collections.synchronizedList(new ArrayList<int[]>());
      log.info("testSyncedArrayListInserts: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runTestThread(1, 1, list, totalIterations);
   }*/

   public void testVectorInserts() {
      List<int[]> list = new Vector<int[]>();
      log.info("testVectorInserts: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runInsertTestThread(1, 1, list, totalIterations);
   }

   public void testConcurrentListInserts() {
      List<int[]> list = new ConcurrentList<int[]>();
      log.info("testConcurrentListInserts: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runInsertTestThread(1, 1, list, totalIterations);
   }

/*   public void testCopyOnWriteArrayList() {
      List<int[]> list = new CopyOnWriteArrayList<int[]>();
      log.info(list.getClass().getName());
      prefillList(list, prefillSize);
      runTestThread(1, 1, list, totalIterations);
   }*/

/*   public void testArrayListMultiInserts() {
      List<int[]> list = new ArrayList<int[]>();
      log.info("testArrayListMultiInserts: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runMultiThreadTest(threads, list, totalIterations);
   }*/

/*   public void testSyncedArrayListMultiInserts() {
      List<int[]> list = Collections.synchronizedList(new ArrayList<int[]>());
      log.info("testSyncedArrayListMultiInserts: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runMultiThreadTest(threads, list, totalIterations);
   }*/

   public void testVectorMultiInserts() {
      List<int[]> list = new Vector<int[]>();
      log.info("testVectorMultiInserts: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runMultiThreadInsertTest(threads, list, totalIterations);
   }

   public void testConcurrentListMultiInserts() {
      List<int[]> list = new Vector<int[]>();
      log.info("testConcurrentListMultiInserts: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runMultiThreadInsertTest(threads, list, totalIterations);
   }
   
/*   public void testArrayListMulti() {
      List<int[]> list = new ArrayList<int[]>();
      log.info("testArrayListMulti: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runMultiThreadTest(threads, list, totalIterations);
   }
*/
/*   public void testSyncedArrayListMulti() {
      List<int[]> list = Collections.synchronizedList(new ArrayList<int[]>());
      log.info("testSyncedArrayListMulti: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runMultiThreadTest(threads, list, totalIterations);
   }*/

   public void testVectorMulti() {
      List<int[]> list = new Vector<int[]>();
      log.info("testVectorMulti: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runMultiThreadTest(threads, list, totalIterations);
   }

   public void testConcurrentListMulti() {
      List<int[]> list = new Vector<int[]>();
      log.info("testConcurrentListMulti: " + list.getClass().getName());
      prefillList(list, prefillSize);
      runMultiThreadTest(threads, list, totalIterations);
   }

/*   public void testCopyOnWriteArrayListMulti() {
      List<int[]> list = new CopyOnWriteArrayList<int[]>();
      log.info(list.getClass().getName());
      prefillList(list, prefillSize);
      runMultiThreadTest(threads, list, totalIterations);
   }*/

   private void runMultiThreadTest(final int threads, final List<int[]> list, int iterations) {
      final int threadIterations = iterations / threads;

      log.info("Starting threaded test with "+threads+" threads...");
      long start = System.currentTimeMillis();
      for (int t = 0; t < threads; t++) {
         final int threadnum = t+1;
         Thread thread = new Thread( new Runnable() {
            public void run() {
               runTestThread(threadnum, threads, list, threadIterations);
            }
         }, threadnum+"");
         thread.start();
      }
      startThreadMonitor();
      long total = System.currentTimeMillis() - start;
      log.info(threads + " threads completed "+iterations+" iterations in "
            +total+" ms ("+calcUSecsPerOp(iterations, total)+" microsecs per iteration)," +
            		" final size of list: " + list.size());
   }

   private void runTestThread(int threadnum, int threads, final List<int[]> list, int iterations) {
      long readCount = 0;
      int insertCount = 0;
      int deleteCount = 0;
      //String keyPrefix = "threadResource-" + threadnum + "-";
      checkpointMap.put(Thread.currentThread().getName(), new Date());
      long start = System.currentTimeMillis();
      try {
         for (int i = 0; i < iterations; i++) {
            if ( i % 100 == 0 ) {
               int num = insertCount++;
               list.add(new int[] {num});
            }
            // do 10 reads
            int startIndex = rGen.nextInt(list.size()-10);
            for (int j = 0; j < 10; j++) {
               readCount++;
               int[] item = list.get(startIndex + j);
               item[0]++;
            }
            if ( i % 1000 == 0 ) {
               int rIndex = rGen.nextInt(list.size());
               list.remove(rIndex);
               deleteCount++;
            }
            if (i > 0 && i % (iterations/10) == 0) {
               checkpointMap.put(Thread.currentThread().getName(), new Date());
               //log.info("thread: " + threadnum + " " + (i*100/iterations) + "% complete");
            }
         }
      } catch (Exception e) {
         log.warn("Thread "+threadnum+": failed: " + e);
      } finally {
         long total = System.currentTimeMillis() - start;
         checkpointMap.remove(Thread.currentThread().getName());
         if (threadnum == 1) {
            log.info("Thread "+threadnum+": completed "+iterations+" iterations with "+insertCount+" inserts " +
                  "and "+deleteCount+" removes and "+readCount+" reads " +
                  "in "+total+" ms ("+calcUSecsPerOp(iterations, total)+" microsecs per iteration)," +
                     " final size of list: " + list.size());
         }
      }
   }

   private void runMultiThreadInsertTest(final int threads, final List<int[]> list, int iterations) {
      final int threadIterations = iterations / threads;

      log.debug("Starting threaded test with "+threads+" threads...");
      long start = System.currentTimeMillis();
      for (int t = 0; t < threads; t++) {
         final int threadnum = t+1;
         Thread thread = new Thread( new Runnable() {
            public void run() {
               runInsertTestThread(threadnum, threads, list, threadIterations);
            }
         }, threadnum+"");
         thread.start();
      }
      startThreadMonitor();
      long total = System.currentTimeMillis() - start;
      log.info(threads + " threads completed "+iterations+" inserts in "
            +total+" ms ("+calcUSecsPerOp(iterations, total)+" microsecs per iteration)");
   }

   private void runInsertTestThread(int threadnum, int threads, final List<int[]> list, int iterations) {
      int clearCount = 0;
      //String keyPrefix = "threadResource-" + threadnum + "-";
      checkpointMap.put(Thread.currentThread().getName(), new Date());
      long start = System.currentTimeMillis();
      try {
         for (int i = 0; i < iterations; i++) {
            list.add(new int[] {i});
            if ( list.size() > 10000 ) {
               list.clear();
               clearCount++;
            }
            if (i > 0 && i % (iterations/10) == 0) {
               checkpointMap.put(Thread.currentThread().getName(), new Date());
               //log.info("thread: " + threadnum + " " + (i*100/iterations) + "% complete");
            }
         }
      } catch (Exception e) {
         log.warn("Thread "+threadnum+": failed: " + e);
      } finally {
         long total = System.currentTimeMillis() - start;
         checkpointMap.remove(Thread.currentThread().getName());
         if (threadnum == 1) {
            log.info("Thread "+threadnum+": completed "+iterations+" inserts with "+clearCount+ " clears " +
                  "in "+total+" ms ("+calcUSecsPerOp(iterations, total)+" microsecs per iteration)");
         }
      }
   }

   private void prefillList(final List<int[]> list, int count) {
      // prefill the list with 100 items
      for (int i = 0; i < count; i++) {
         list.add(new int[] {i});
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

   /**
    * Emulate the logger to try to reduce the impact of outside forces on the test results 
    * 
    * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
    */
   public static class log {
      public static void info(String value) {
         System.out.println("INFO:" + new Date() + "::" + value);
      }

      public static void warn(String value) {
         System.out.println("WARNING:" + new Date() + "::" + value);
      }

      public static void debug(String value) {
         // disabled for now
         //System.out.println("DEBUG:" + new Date() + "::" + value);
      }
   }

}
