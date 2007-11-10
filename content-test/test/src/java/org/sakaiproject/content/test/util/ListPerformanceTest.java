/**
 * ListPerformanceTest.java - content-test - 2007 Nov 10, 2007 7:19:07 PM - azeckoski
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
import java.util.concurrent.CopyOnWriteArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Testing the performance of various types of lists
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ListPerformanceTest extends TestCase {

   private static Log log = LogFactory.getLog(ListPerformanceTest.class);

   private Map<String, Date> checkpointMap = new ConcurrentHashMap<String, Date>();
   /**
    * Number of total thread simulation iterations to run
    */
   protected final int totalIterations = 1000000;
   protected final int threads = 50;
   protected final int prefillSize = 100;


   protected DecimalFormat df = new DecimalFormat("#,##0.00");
   protected Random rGen = new Random();

   public void testArrayList() {
      List<String> list = new ArrayList<String>();
      prefillList(list, prefillSize);
      runTestThread(1, 1, list, totalIterations);
   }

   public void testVector() {
      List<String> list = new Vector<String>();
      prefillList(list, prefillSize);
      runTestThread(1, 1, list, totalIterations);
   }

   public void testConcurrentList() {
      List<String> list = new ConcurrentList<String>();
      prefillList(list, prefillSize);
      runTestThread(1, 1, list, totalIterations);
   }

/*   public void testCopyOnWriteArrayList() {
      List<String> list = new CopyOnWriteArrayList<String>();
      prefillList(list, prefillSize);
      runTestThread(1, 1, list, totalIterations);
   }*/

   public void testVectorMulti() {
      List<String> list = new Vector<String>();
      prefillList(list, prefillSize);
      runMultiThreadTest(threads, list, totalIterations);
   }

   public void testConcurrentListMulti() {
      List<String> list = new Vector<String>();
      prefillList(list, prefillSize);
      runMultiThreadTest(threads, list, totalIterations);
   }

/*   public void testCopyOnWriteArrayListMulti() {
      List<String> list = new CopyOnWriteArrayList<String>();
      prefillList(list, prefillSize);
      runMultiThreadTest(threads, list, totalIterations);
   }
*/
   private void runMultiThreadTest(final int threads, final List<String> list, int iterations) {
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

   private void runTestThread(int threadnum, int threads, final List<String> list, int iterations) {
      long readCount = 0;
      int insertCount = 0;
      int deleteCount = 0;
      String keyPrefix = "threadResource-" + threadnum + "-";
      checkpointMap.put(Thread.currentThread().getName(), new Date());
      long start = System.currentTimeMillis();
      for (int i = 0; i < iterations; i++) {
         if ( i % 10 == 0 ) {
            int num = insertCount++;
            list.add(keyPrefix + num);
         }
         // do 10 reads
         int startIndex = rGen.nextInt(list.size()-10);
         for (int j = 0; j < 10; j++) {
            readCount++;
            list.get(startIndex + j);
         }
         if ( i % 100 == 0 ) {
            int rIndex = rGen.nextInt(list.size());
            list.remove(rIndex);
            deleteCount++;
         }
         if (i > 0 && i % (iterations/5) == 0) {
            checkpointMap.put(Thread.currentThread().getName(), new Date());
            //log.info("thread: " + threadnum + " " + (i*100/iterations) + "% complete");
         }
      }
      long total = System.currentTimeMillis() - start;
      checkpointMap.remove(Thread.currentThread().getName());
      if (threadnum == 1) {
         log.info("Thread "+threadnum+": completed "+iterations+" iterations with "+insertCount+" inserts " +
               "and "+deleteCount+" removes and "+readCount+" reads " +
               "in "+total+" ms ("+calcUSecsPerOp(iterations, total)+" microsecs per iteration)," +
                  " final size of list: " + list.size());
      }
   }

   private void prefillList(final List<String> list, int count) {
      // prefill the list with 100 items
      for (int i = 0; i < count; i++) {
         list.add("prefill-item-" + i);
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
