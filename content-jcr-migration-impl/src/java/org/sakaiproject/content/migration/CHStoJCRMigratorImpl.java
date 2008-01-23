package org.sakaiproject.content.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.migration.api.CHStoJCRMigrator;
import org.sakaiproject.content.migration.api.ContentToJCRCopier;
import org.sakaiproject.content.migration.api.MigrationStatusReporter;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.jcr.api.JCRService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CHStoJCRMigratorImpl extends SakaiRequestEmulator 
	implements CHStoJCRMigrator, MigrationStatusReporter, ApplicationContextAware
{

	private static final Log log = LogFactory.getLog(CHStoJCRMigratorImpl.class);

	// Injected Services
	private DataSource dataSource;
	
	private SqlService sqlService;

	private boolean autoDDL = false;

	private JCRService jcrService;

	private ContentToJCRCopier contentToJCRCopier;

	//private MigrationStatusReporter migrationStatusReporter;

	// End Injected Services

	protected final String CURRENT_USER_MARKER = "originalTestUser";

	protected final String ADMIN_USER = "admin";

	private static final String ORIGINAL_MIGRATION_EVENT = "ORIGINAL_MIGRATION";

	/*
	 * Property to allow you to stop and start the system from migrating things
	 * in the background.
	 */
	private boolean isCurrentlyMigrating = false;

	private int batchSize = 20;

	private int delayBetweenBatchesMilliSeconds = 1000;

	/* Our things to do the work. */
	private Timer timer = new Timer(false);

	private javax.jcr.Session jcrSession;
	

	private String HACKUSER = "admin";

	private UserDirectoryService userDirectoryService;
	
	private Connection dbConnection;

	// Prepared Statements
	private PreparedStatement finishItemPreparedStatement;
	private PreparedStatement addOriginalCollPreparedStatement;
	private PreparedStatement addOriginalResourcesPreparedStatement; 
	private PreparedStatement nextThingsToMigratePreparedStatement;
	private PreparedStatement countTotalItemsInQueuePreparedStatement;
	private PreparedStatement countFinishedItemsInQueuePreparedStatement;

	public void init()
	{
		if ( !jcrService.isEnabled() ) return;
		
		try {
			dbConnection = dataSource.getConnection();
			finishItemPreparedStatement = 
				dbConnection.prepareStatement(MigrationSqlQueries.finish_content_item);
			addOriginalCollPreparedStatement = 
				dbConnection.prepareStatement(MigrationSqlQueries.add_original_collections_to_migrate);
			addOriginalResourcesPreparedStatement = 
				dbConnection.prepareStatement(MigrationSqlQueries.add_original_resources_to_migrate);
			nextThingsToMigratePreparedStatement = 
				dbConnection.prepareStatement(MigrationSqlQueries.select_unfinished_items);
			countTotalItemsInQueuePreparedStatement = 
				dbConnection.prepareStatement(MigrationSqlQueries.count_total_content_items_in_queue);
			countFinishedItemsInQueuePreparedStatement =
				dbConnection.prepareStatement(MigrationSqlQueries.count_finished_content_items_in_queue);
		} catch (SQLException e1) {
			log.error("Unable to set up Db connection.", e1);
		}
		
		log.info("init()");
		try
		{
			jcrSession = jcrService.login();
		}
		catch (LoginException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (RepositoryException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// creating the needed tables
		if (autoDDL)
		{
			sqlService.ddl(getClass().getClassLoader(), "setup-migration-dbtables");
		}
	}

	public void destroy()
	{
		if ( !jcrService.isEnabled() ) return;
		jcrSession.logout();
		if (dbConnection != null) {
			try {
				dbConnection.close();
			} catch (SQLException e) {
				log.error("Unable to close db connection.", e);
			}
		}
		log.info("destroy()");
	}

	private void markContentItemFinished(String contentId)
	{
		if ( !jcrService.isEnabled() ) return;
		try {
			finishItemPreparedStatement.clearParameters();
			finishItemPreparedStatement.setString(1, contentId);
			finishItemPreparedStatement.executeUpdate();
		} 
		catch (SQLException e) {
			log.error("Error marking the migration content item finished.", e);
		}
	}

	public boolean isCurrentlyMigrating()
	{
		return isCurrentlyMigrating;
	}

	private void addOriginalItemsToQueue()
	{
		if ( !jcrService.isEnabled() ) return;

		try
		{
			addOriginalCollPreparedStatement.executeUpdate();
			addOriginalResourcesPreparedStatement.executeUpdate();
		}
		catch (SQLException e)
		{
			log.error("Problems adding the original content migration items to the queue.", e);
		}
	}

	public void startMigrating()
	{
		if ( !jcrService.isEnabled() ) return;
		this.isCurrentlyMigrating = true;
		if (!hasMigrationStarted())
		{
			addOriginalItemsToQueue();
		}
		scheduleBatch();
	}

	public void stopMigrating()
	{
		if ( !jcrService.isEnabled() ) return;
		this.isCurrentlyMigrating = false;
	}

	private void migrateOneItem(javax.jcr.Session session, ThingToMigrate item)
	{
		setTestUser(SUPER_USER);
		startEmulatedRequest(SUPER_USER);
		// ContentResources in the Original CHS always end with '/'
		if (item.contentId.endsWith("/"))
		{
			// This is a ContentCollection
			if (item.eventType.equals(ORIGINAL_MIGRATION_EVENT))
			{
				contentToJCRCopier.copyCollectionFromCHStoJCR(session, item.contentId);
			}
			else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_ADD))
			{
				contentToJCRCopier.copyCollectionFromCHStoJCR(session, item.contentId);
			}
			else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_REMOVE))
			{
				contentToJCRCopier.deleteItem(session, item.contentId);
			}
			else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_WRITE))
			{
				contentToJCRCopier.copyCollectionFromCHStoJCR(session, item.contentId);
			}
		}
		else
		{
			// This is a ContentResource
			if (item.eventType.equals(ORIGINAL_MIGRATION_EVENT))
			{
				contentToJCRCopier.copyResourceFromCHStoJCR(session, item.contentId);
			}
			else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_ADD))
			{
				contentToJCRCopier.copyResourceFromCHStoJCR(session, item.contentId);
			}
			else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_REMOVE))
			{
				contentToJCRCopier.deleteItem(session, item.contentId);
			}
			else if (item.eventType.equals(ContentHostingService.EVENT_RESOURCE_WRITE))
			{
				contentToJCRCopier.copyResourceFromCHStoJCR(session, item.contentId);
			}
		}
		//try {
		//	Thread.sleep(1000);
		//} catch (InterruptedException e) {
		//	log.error("Problems while sleeping during CHS->JCR Migration.", e);
		//}
		markContentItemFinished(item.contentId);
		endEmulatedRequest();
	}

	@SuppressWarnings("unchecked")
	private void migrateSomeItems(int numberToMigrate)
	{
		List<ThingToMigrate> thingsToMigrate = new ArrayList<ThingToMigrate>();

		try {
			nextThingsToMigratePreparedStatement.clearParameters();
			nextThingsToMigratePreparedStatement.setInt(1, numberToMigrate);
			ResultSet result = nextThingsToMigratePreparedStatement.executeQuery();
			try {
				while (result.next()) {
					ThingToMigrate thing = new ThingToMigrate();
					thing.contentId = result.getString("CONTENT_ID");
					thing.status = result.getInt("STATUS");
					// TODO TODO TODO The time added
					thing.eventType = result.getString("EVENT_TYPE");
					thingsToMigrate.add(thing);
				}
			} finally {
				result.close();
			}
		} catch (SQLException e) {
			log.error("SQL Error Migrating JCR Items.", e);
		}

		for (ThingToMigrate thing : thingsToMigrate)
		{
			final ThingToMigrate thing2 = thing;
			//Thread thread = new Thread( new Runnable() {
			//	public void run() {
			//		migrateOneItem(jcrSession, thing2);
			//	}
			//});
			//thread.start();
			CopierRunnable aCopier = (CopierRunnable) appContext.getBean("CopierRunnable");
			aCopier.setJcrSession(jcrSession);
			aCopier.setThing(thing);
			Thread thread = new Thread(aCopier);
			thread.start();
			if (thread.isAlive()) {
				System.out.println("SWG Our migrate thing thread is still alive");
				try {
					Thread.sleep(1000);
				} catch (java.lang.InterruptedException e) {
					log.info("Unable to sleep while checking if the migrate thread is still alive", e);
				}
			}
			markContentItemFinished(thing.contentId);
		}
	}

	private void scheduleBatch()
	{
		TimerTask batchTask = new TimerTask()
		{
			public void run()
			{
				// If there is stuff left, migrate it.
				//startEmulatedRequest(SUPER_USER);
				//while (!hasMigrationFinished() && isCurrentlyMigrating)
				//{
					
					migrateSomeItems(100000);
					//endEmulatedRequest();
					//try {
					//	Thread.sleep(20*1000);
					//} catch (InterruptedException e) {
					//	isCurrentlyMigrating = false;
					//	break;
					//}
				//}
				isCurrentlyMigrating = false;
				//endEmulatedRequest();
				return;
			}
		};
		if (timer == null) timer = new Timer(false);
		try
		{
			timer.schedule(batchTask, delayBetweenBatchesMilliSeconds);
		}
		catch (IllegalStateException ise)
		{
			// If there was a problem before, the timer will have been
			// cancelled.
			log.info("There was an error previously with the migration, recreating the Migration Timer");
			timer.cancel();
			timer = new Timer(false);
			timer.schedule(batchTask, delayBetweenBatchesMilliSeconds);
		}
	}

	public int getBatchSize()
	{
		return batchSize;
	}

	public void setBatchSize(int batchSize)
	{
		this.batchSize = batchSize;
	}

	public int getDelayBetweenBatchesMilliSeconds()
	{
		return delayBetweenBatchesMilliSeconds;
	}

	public void setDelayBetweenBatchesMilliSeconds(int delayBetweenBatchesMilliSeconds)
	{
		this.delayBetweenBatchesMilliSeconds = delayBetweenBatchesMilliSeconds;
	}

	/*
	 * Various Injections Below
	 */
	public void setSqlService(SqlService sqlService)
	{
		this.sqlService = sqlService;
	}

	public void setJcrService(JCRService jcrService)
	{
		this.jcrService = jcrService;
	}

	public void setContentToJCRCopier(ContentToJCRCopier contentToJCRCopier)
	{
		this.contentToJCRCopier = contentToJCRCopier;
	}

	public void setAutoDDL(boolean autoDDL)
	{
		this.autoDDL = autoDDL;
	}

	public int[] filesRemaining()
	{	
		int numberTotalItems = 0, numberFinishedItems = 0;
		
		ResultSet numberTotalItemsRS = null;
		try {
			numberTotalItemsRS = countTotalItemsInQueuePreparedStatement.executeQuery();
			numberTotalItemsRS.next();
			numberTotalItems = numberTotalItemsRS.getInt(1);
		} catch (SQLException e) {
			log.error("Unable to count items in migration queue.", e);
		}
		finally {
			if (numberTotalItemsRS != null) {
				try {
					numberTotalItemsRS.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		ResultSet numberFinishedRS = null;
		try {
			numberFinishedRS = countFinishedItemsInQueuePreparedStatement.executeQuery();
			numberFinishedRS.next();
			numberFinishedItems = numberFinishedRS.getInt(1);
		} catch (SQLException e) {
			log.error("Unable to count items in migration queue.", e);
		} finally {
			if (numberFinishedRS != null) {
				try {
					numberFinishedRS.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return new int[] { numberFinishedItems, numberTotalItems };
	}

	/*
	 * If we've copied the original Collections and Resources over, then we deem
	 * that the migration has truly started.
	 */
	public boolean hasMigrationStarted()
	{
		int totalInQueue = filesRemaining()[1];
		
		if (totalInQueue > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean hasMigrationFinished()
	{
		int[] remaining = filesRemaining();

		if (remaining[0] == remaining[1])
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * @param userDirectoryService the userDirectoryService to set
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private ApplicationContext appContext;
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.appContext = ctx;
	}

}