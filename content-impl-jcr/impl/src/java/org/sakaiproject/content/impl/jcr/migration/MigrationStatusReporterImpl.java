package org.sakaiproject.content.impl.jcr.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.migration.api.MigrationStatusReporter;
import org.sakaiproject.db.api.SqlService;

public class MigrationStatusReporterImpl implements MigrationStatusReporter {
    private static Log log = LogFactory.getLog(MigrationStatusReporterImpl.class);

    private SqlService sqlService;
    
    public int[] filesRemaining() {
        int numberTotalItems = Integer.parseInt((String)sqlService.dbRead(
                MigrationSqlQueries.count_total_content_items_in_queue).get(0));
        
        int numberFinishedItems = Integer.parseInt((String)sqlService.dbRead(
                MigrationSqlQueries.count_finished_content_items_in_queue).get(0)); 
        
        return new int[] { numberFinishedItems, numberTotalItems };           
    }
    
    /* 
     * If we've copied the original Collections and Resources over, then we deem
     * that the migration has truly started.
     */
    public boolean hasMigrationStarted() {
        int totalInQueue = Integer.parseInt((String) sqlService.dbRead(MigrationSqlQueries.count_total_content_items_in_queue).get(0));
    
        if (totalInQueue > 0) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public boolean hasMigrationFinished() {
        int[] remaining = filesRemaining();
        
        if (remaining[0] == remaining[1]) {
            return true;
        }
        else {
            return false;
        }
    }
    

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }
    
    public void init() {
        log.info("init()");
    }
    
    public void destroy() {
        log.info("destroy()");
    }

}
