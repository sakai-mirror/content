
package org.sakaiproject.content.impl.jcr.migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.jcr.api.JCRService;
import org.sakaiproject.content.migration.api.CHStoJCRMigrator;
import org.sakaiproject.content.migration.api.ContentToJCRCopier;
import org.sakaiproject.content.migration.api.MigrationStatusReporter;

public class CHStoJCRMigratorImpl implements CHStoJCRMigrator {

   private static final Log log = LogFactory.getLog(CHStoJCRMigratorImpl.class);

   // Injected Services
   private SqlService sqlService;
   private boolean autoDDL = false;
   private JCRService jcrService;
   private ContentToJCRCopier contentToJCRCopier;
   private MigrationStatusReporter migrationStatusReporter;
   // End Injected Services

   protected final String CURRENT_USER_MARKER = "originalTestUser";
   protected final String ADMIN_USER = "admin";

   private static final String ORIGINAL_MIGRATION_EVENT = "ORIGINAL_MIGRATION";

   /*
    * Property to allow you to stop and start the system from migrating things in the background.
    */
   private boolean isCurrentlyMigrating = false;
   private int batchSize = 20;
   private int delayBetweenBatchesMilliSeconds = 1000;
   Timer timer = new Timer(false);

   public void init() {
      log.info("init()");
      // creating the needed tables
      if (autoDDL) {
         sqlService.ddl(getClass().getClassLoader(), "setup-migration-dbtables");
      }
   }

   public void destroy() {
      log.info("destroy()");
   }

/*   private void markContentItemFinished(String collectionId) {
      sqlService.dbWrite(MigrationSqlQueries.finish_content_item, collectionId);
   }*/

   public boolean isCurrentlyMigrating() {
      return isCurrentlyMigrating;
   }

   private void addOriginalItemsToQueue() {
      try {
         Connection conn = sqlService.borrowConnection();
         sqlService.dbInsert(conn, MigrationSqlQueries.add_original_collections_to_migrate, null,
               "id");
         sqlService.dbInsert(conn, MigrationSqlQueries.add_original_resources_to_migrate, null,
               "id");
         sqlService.returnConnection(conn);
      } catch (SQLException e) {
         log.error("Problems adding the original content migration items to the queue.", e);
      }
   }

   public void startMigrating() {
      this.isCurrentlyMigrating = true;
      if (!migrationStatusReporter.hasMigrationStarted()) {
         addOriginalItemsToQueue();
      }
      scheduleBatch();
   }

   public void stopMigrating() {
      this.isCurrentlyMigrating = false;
   }

   private void migrateOneItem(ThingToMigrate item) {
      // ContentResources in the Original CHS always end with '/'
      if (item.contentId.endsWith("/")) {
         // This is a ContentCollection
         if (item.eventType.equals(ORIGINAL_MIGRATION_EVENT)) {
            contentToJCRCopier.copyCollectionFromCHStoJCR(item.contentId);
         } else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_ADD)) {
            contentToJCRCopier.copyCollectionFromCHStoJCR(item.contentId);
         } else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_REMOVE)) {
            contentToJCRCopier.deleteItem(item.contentId);
         } else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_WRITE)) {
            contentToJCRCopier.copyCollectionFromCHStoJCR(item.contentId);
         }
      } else {
         // This is a ContentResource
         if (item.eventType.equals(ORIGINAL_MIGRATION_EVENT)) {
            contentToJCRCopier.copyResourceFromCHStoJCR(item.contentId);
         } else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_ADD)) {
            contentToJCRCopier.copyResourceFromCHStoJCR(item.contentId);
         } else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_REMOVE)) {
            contentToJCRCopier.deleteItem(item.contentId);
         } else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_WRITE)) {
            contentToJCRCopier.copyResourceFromCHStoJCR(item.contentId);
         }
      }
   }

   @SuppressWarnings("unchecked")
   private void migrateSomeItems(int numberToMigrate) {
      List<ThingToMigrate> thingsToMigrate = sqlService.dbRead(
            MigrationSqlQueries.select_unfinished_items, 
            new Object[] { numberToMigrate },
            new MigrationTableSqlReader()
            );

      for (ThingToMigrate thing : thingsToMigrate) {
         migrateOneItem(thing);
      }
   }

   private void scheduleBatch() {
      TimerTask batchTask = new TimerTask() {

         public void run() {
            // If there is stuff left, migrate it.
            if (!migrationStatusReporter.hasMigrationFinished()) {
               migrateSomeItems(batchSize);
            } else {
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

   public int getBatchSize() {
      return batchSize;
   }

   public void setBatchSize(int batchSize) {
      this.batchSize = batchSize;
   }

   public int getDelayBetweenBatchesMilliSeconds() {
      return delayBetweenBatchesMilliSeconds;
   }

   public void setDelayBetweenBatchesMilliSeconds(int delayBetweenBatchesMilliSeconds) {
      this.delayBetweenBatchesMilliSeconds = delayBetweenBatchesMilliSeconds;
   }

   /* 
    * 
    * Various Injections Below
    * 
    */
   public void setSqlService(SqlService sqlService) {
      this.sqlService = sqlService;
   }

   public void setJcrService(JCRService jcrService) {
      this.jcrService = jcrService;
   }

   public void setMigrationStatusReporter(MigrationStatusReporter migrationStatusReporter) {
      this.migrationStatusReporter = migrationStatusReporter;
   }

   public void setContentToJCRCopier(ContentToJCRCopier contentToJCRCopier) {
      this.contentToJCRCopier = contentToJCRCopier;
   }

   
   public void setAutoDDL(boolean autoDDL) {
      this.autoDDL = autoDDL;
   }

}