package org.sakaiproject.content.migration.api;

public interface MigrationStatusReporter {
  
    /*
     * Returns number finished and total number to convert.
     * example:
     * [ 32 , 3000 ]
     */
    public int[] filesRemaining();
  
}
