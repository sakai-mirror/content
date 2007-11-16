package org.sakaiproject.content.impl.jcr.migration;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.jcr.api.JCRService;
import org.sakaiproject.content.migration.api.ContentToJCRCopier;

public class CHStoJCRmigrator {
    private static final Log log = LogFactory.getLog(CHStoJCRmigrator.class);
   
    // Injected Services
    private SqlService sqlService;
    private ContentHostingService legacyContentHostingService;
    private JCRService jcrService;
    private ContentToJCRCopier contentToJCRMigrator;
    // End Injected Services
    
    protected final String CURRENT_USER_MARKER = "originalTestUser";
    protected final String ADMIN_USER = "admin";
    
    /*
     * Property to allow you to stop and start the system from migrating things in the background.
     */
    private boolean isCurrentlyMigrating = false;
    private int batchSize = 20;
    private int delayBetweenBatchesMilliSeconds = 1000;
    Timer timer = new Timer(false);
    
    public void init() {
        
    }
    
    public void destroy() {
        
    }

    public static final String jcr_content_prefix = "/sakai/content";

    public static final String select_unfinished_collections = "SELECT COLLECTION_ID"
        + " FROM MIGRATE_JCR_CONTENT_COLLECTION M" 
        + " WHERE STATUS = 0"
        + " LIMIT 0, ?";

    public static final String update_finished_collection = "UPDATE MIGRATE_JCR_CONTENT_COLLECTION"
        + " SET STATUS = 1"
        + " WHERE COLLECTION_ID = ?";

    public static final String select_unfinished_resources = "SELECT RESOURCE_ID" 
        + " FROM MIGRATE_JCR_CONTENT_RESOURCE M" 
        + " WHERE STATUS = 0"
        + " LIMIT 0, ?";

    public static final String update_finished_resource = "UPDATE MIGRATE_JCR_CONTENT_RESOURCE"
        + " SET STATUS = 1"
        + " WHERE RESOURCE_ID = ?";

    public static final String count_finished_collections = "SELECT COUNT(*) FROM"
        + " MIGRATE_JCR_CONTENT_COLLECTION" 
        + " WHERE STATUS = 1";

    public static final String count_total_number_collections = "SELECT COUNT(*) FROM"
        + " MIGRATE_JCR_CONTENT_COLLECTION";

    public static final String count_finished_resources = "SELECT COUNT(*) FROM" 
        + " MIGRATE_JCR_CONTENT_RESOURCE M"
        + " WHERE STATUS = 1";

    public static final String count_total_number_resources = "SELECT COUNT(*) FROM"
        + " MIGRATE_JCR_CONTENT_RESOURCE M";

    /* Returns a tuple containing the number of migrated collections and the 
     * total number of collections. ex (43, 101)
     */
    public int[] collectionStatus() {
        int numberFinishedCollections = Integer.parseInt((String)sqlService.dbRead(
                      count_finished_collections).get(0));
        
        int numberTotalCollections = Integer.parseInt((String)sqlService.dbRead(
                      count_total_number_collections).get(0));
        return new int[] {numberFinishedCollections, numberTotalCollections};
    }
        
    /* Returns a tuple containing the number of migrated resources and the 
     * total number of resources. ex (43, 101)
     */
    public int[] resourceStatus() {
       
       int numberFinshedResources = Integer.parseInt((String)sqlService.dbRead(
                       count_finished_resources).get(0));
       int numberTotalResources = Integer.parseInt((String)sqlService.dbRead(
                       count_total_number_resources).get(0));
      return new int[] {numberFinshedResources,numberTotalResources}; 

    }
    
    /* Migrate some folders from content hosting to the jackrabbit service.  
     * The default number to migrate is 20, but can be changed with the number
     * parameter
     */
    public void migrateFolders(int number) {
        
        List<String> sqlResults = sqlService.dbRead(select_unfinished_collections, new Object[] {number}, null);
        for (String collection : sqlResults) { 
         // try {
            contentToJCRMigrator.copyCollectionFromCHStoJCR(collection);
            log.debug("Copied folder to JCR: " + collection);
         // }
         // catch (ItemExistsException e) {
         //   log.info("Folder already exists: " + collection, e); 
         // }
          markCollectionFinished(collection);
        }
    }
       
    /* Migrate some files from content hosting to the jackrabbit service.
     * The default number to migrate is 20, but can be changed with the number
     * parameter. 
     */
    public void migrateFiles(int number) {
       
       List<String> sqlResults = sqlService.dbRead(select_unfinished_resources, 
               new Object[] {number}, null);
        for (String resource : sqlResults) {
          //try {
            contentToJCRMigrator.copyResourceFromCHStoJCR(resource);
            log.debug("Copied file to JCR: " + resource);
          //}
          //catch (ItemExistsException e) {
          //  log.info("File already exists: " + resource, e);
         // }
          markResourceFinished(resource);
        }
    }
    
    public void markCollectionFinished(String collectionId) {
        sqlService.dbWrite(update_finished_collection, collectionId);
    }

    public void markResourceFinished(String resourceId) {
       sqlService.dbWrite(update_finished_resource, resourceId);
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public void setLegacyContentHostingService(
            ContentHostingService legacyContentHostingService) {
        this.legacyContentHostingService = legacyContentHostingService;
    }

    public void setJcrService(JCRService jcrService) {
        this.jcrService = jcrService;
    }

    public boolean isCurrentlyMigrating() {
        return isCurrentlyMigrating;
    }

    public void startMigrating() {
        this.isCurrentlyMigrating = true;
        scheduleBatch();
    }
    
    public void stopMigrating() {
        this.isCurrentlyMigrating = false;
    }
    
    private void scheduleBatch() {     
        TimerTask batchTask = new TimerTask() {
            public void run() {
                // If there folders left migrate them
                int[] colStatus = collectionStatus();
                int[] resStatus = resourceStatus();
                if (colStatus[0] < colStatus[1]) {
                    migrateFolders(batchSize);
                }  
                else if (resStatus[0] < resStatus[1]) {
                    migrateFiles(batchSize);
                }
                else {
                    isCurrentlyMigrating = false;
                    return;
                }
                
                if (isCurrentlyMigrating) {
                    scheduleBatch();
                }
            }
        };
        timer.schedule(batchTask, delayBetweenBatchesMilliSeconds);
    }
    
    public void setContentToJCRMigrator(ContentToJCRCopier contentToJCRMigrator) {
        this.contentToJCRMigrator = contentToJCRMigrator;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getDelayBetweenBatchesMilliSeconds() {
        return delayBetweenBatchesMilliSeconds;
    }

    public void setDelayBetweenBatchesMilliSeconds(
            int delayBetweenBatchesMilliSeconds) {
        this.delayBetweenBatchesMilliSeconds = delayBetweenBatchesMilliSeconds;
    }

}