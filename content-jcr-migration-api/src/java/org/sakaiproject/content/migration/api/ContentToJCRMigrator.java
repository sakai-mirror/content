package org.sakaiproject.content.migration.api;

/*
 * This interface is used to copy a file or folder from the Legacy CHS Implementation
 * to the JCR Implementation.
 * 
 * This interface is only responsible for moving single instances of files and folders
 * over.  I am still not sure of the best algorithm for migrating an entire installation 
 * of data, so that logic is left to other modules, but they can use this to make their 
 * life easy.
 * 
 * Just as an example, you may want to migrate all your data during a down time, or do it
 * in the background and continue to listen for content events to they can be added to
 * the queue for processing.
 */
public interface ContentToJCRMigrator {
    
    /* 
     * Copies the ContentCollection folder to JCR for the content collection at 
     * absolute path 'abspath'. This does not copy the contents of the folder, just
     * creates the empty nt:folder and copies all the Sakai metadata.
     */
    public boolean copyCollectionFromCHStoJCR(String abspath);
        
    /*
     * This will copy the resource from the Legacy ContentHosting implementation 
     *to the JCR Repository
     */
    public boolean copyResourceFromCHStoJCR(String abspath);
            
}
