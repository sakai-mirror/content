package org.sakaiproject.content.migration.api;

/*
 * This interface is for migrating content from the Legacy
 * CHS to the JCR Implementation of ContentHosting.
 * 
 * Need to add some methods to find out the status of the migration.
 * 
 */
public interface CHStoJCRMigrator
{

	/*
	 * Start the Migration
	 */
	public void startMigrating();

	/*
	 * Stop/Pause the Migration. Will finish the current batch of files/folders
	 */
	public void stopMigrating();

	/*
	 * Find out if we're currently copying files in the background. @return Are
	 * we currently migrating?
	 */
	public boolean isCurrentlyMigrating();
	
	/*
	 * Returns number finished and total number to convert. example: There are
	 * 32 out of 3000 files remaining. [ 32 , 3000 ]
	 */
	public int[] filesRemaining();

	public boolean hasMigrationStarted();

	public boolean hasMigrationFinished();

}
