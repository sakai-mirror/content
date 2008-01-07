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

public class CHStoJCRMigratorImpl implements CHStoJCRMigrator, MigrationStatusReporter
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

	private SessionManager sessionManager;

	private AuthzGroupService authzGroupService;
	
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
		//Connection conn = null;
		try {
			//dbConnection = dataSource.getConnection();
			finishItemPreparedStatement.clearParameters();
			finishItemPreparedStatement.setString(1, contentId);
			finishItemPreparedStatement.executeUpdate();
		} 
		catch (SQLException e) {
			log.error("Error marking the migration content item finished.", e);
		}
		//finally {
			
			//if (conn != null) {
			//	try {
			//		conn.close();
			//	} catch (SQLException e) {
			//		log.error("Error closing connection", e);
			//	}
			//}
		//}
		//slService.dbWrite(MigrationSqlQueries.finish_content_item, contentId);
	}

	public boolean isCurrentlyMigrating()
	{
		return isCurrentlyMigrating;
	}

	private void addOriginalItemsToQueue()
	{
		if ( !jcrService.isEnabled() ) return;
		
		//Connection conn = null;
		try
		{
			//conn = dataSource.getConnection();
			
			//PreparedStatement addOriginalCollectionsStmt = 
			//	conn.prepareStatement(MigrationSqlQueries.add_original_collections_to_migrate);
			
			addOriginalCollPreparedStatement.executeUpdate();
			//addOriginalCollPreparedStatement.close();
			
			//addOriginalResourcesPreparedStatement. = 
			//	conn.prepareStatement(MigrationSqlQueries.add_original_resources_to_migrate);
			
			addOriginalResourcesPreparedStatement.executeUpdate();
			//addOriginalResourcesPreparedStatement.close();
			//slService.dbInsert(conn,
			//		MigrationSqlQueries.add_original_collections_to_migrate, null, "id");
			//slService.dbInsert(conn,
			//		MigrationSqlQueries.add_original_resources_to_migrate, null, "id");
			//slService.returnConnection(conn);
		}
		catch (SQLException e)
		{
			log.error("Problems adding the original content migration items to the queue.", e);
		}
		//finally {
		//	if (conn != null) {
		//		try {
		//			conn.close();
		//		} catch (SQLException e) {
		//			log.error("Error closing connection.", e);
		//		}
		//	}
		//}
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
		markContentItemFinished(item.contentId);
	}

	@SuppressWarnings("unchecked")
	private void migrateSomeItems(int numberToMigrate)
	{
		List<ThingToMigrate> thingsToMigrate = new ArrayList<ThingToMigrate>();
		
		//Connection conn = null;
		
		try {
			//conn = dataSource.getConnection();
			
			//PreparedStatement thingsStmt = 
			//	conn.prepareStatement(MigrationSqlQueries.select_unfinished_items);
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
		} //finally {
		//	if (conn != null) {
		//		try {
		//			conn.close();
		//		} catch (SQLException e) {
		//			log.error("Error closing connection.", e);
		//		}
		//	}
		//}
		
		//List<ThingToMigrate> thingsToMigrate = slService.dbRead(
		//		MigrationSqlQueries.select_unfinished_items,
		//		new Object[] { numberToMigrate }, new MigrationTableSqlReader());

		// try {
		// javax.jcr.Session session = jcrService.login();
		for (ThingToMigrate thing : thingsToMigrate)
		{
			migrateOneItem(jcrSession, thing);
		}
		// session.logout();
		// jcrService.logout();
		// }
		// catch (Exception e) {
		// log.error("Error Migrating some CHS to JCR items: ", e);
		// }
	}

	private void scheduleBatch()
	{
		TimerTask batchTask = new TimerTask()
		{

			public void run()
			{

				/*
				 * There seems to be a problem running this permission wise,
				 * perhaps because it's in it's own thread and the user
				 * information isn't getting carried over? ERROR: Problems
				 * migrating collection:
				 * /group/usedtools/jcr-2.0/docs/javax/jcr/nodetype/ (2007-11-29
				 * 11:42:06,723
				 * Timer-17_org.sakaiproject.content.impl.jcr.migration.ContentToJCRCopierImpl)
				 * org.sakaiproject.exception.PermissionException user=null
				 * lock=content.revise.any
				 * resource=/content/group/usedtools/jcr-2.0/docs/javax/jcr/nodetype/
				 * at
				 * org.sakaiproject.content.impl.BaseContentService.editCollection(BaseContentService.java:4101)
				 * at
				 * org.sakaiproject.content.impl.jcr.migration.ContentToJCRCopierImpl.copyCollectionFromCHStoJCR(ContentToJCRCopierImpl.java:53)
				 * at
				 * org.sakaiproject.content.impl.jcr.migration.CHStoJCRMigratorImpl.migrateOneItem(CHStoJCRMigratorImpl.java:94)
				 */
				becomeHackUser();
				// If there is stuff left, migrate it.
				while (!hasMigrationFinished() && isCurrentlyMigrating)
				{
					migrateSomeItems(1);
					//try {
					//	Thread.sleep(1000);
					//} catch (InterruptedException e) {
					//	log.error("Could not sleep.", e);
					//}
				}
				//else
				//{
					isCurrentlyMigrating = false;
					return;
				//}

				//if (isCurrentlyMigrating)
				//{
				//	scheduleBatch();
				//}
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
			log
					.info("There was an error previously with the migration, recreating the Migration Timer");
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

	//public void setMigrationStatusReporter(MigrationStatusReporter migrationStatusReporter)
	//{
	//	this.migrationStatusReporter = migrationStatusReporter;
	//}

	public void setContentToJCRCopier(ContentToJCRCopier contentToJCRCopier)
	{
		this.contentToJCRCopier = contentToJCRCopier;
	}

	public void setAutoDDL(boolean autoDDL)
	{
		this.autoDDL = autoDDL;
	}

	private void becomeHackUser()
	{

		User u = null;
		try
		{
			u = userDirectoryService.getUserByEid(HACKUSER);
		}
		catch (UserNotDefinedException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Session s = sessionManager.getCurrentSession();

		s.setUserEid(u.getEid());
		s.setUserId(u.getId());
		s.setActive();
		sessionManager.setCurrentSession(s);
		authzGroupService.refreshUser(u.getId());
	}
	
	public int[] filesRemaining()
	{
		//if ( !enabled ) return new int[] {0,0};
		//int numberTotalItems = Integer.parseInt((String) slService.dbRead(
		//		MigrationSqlQueries.count_total_content_items_in_queue).get(0));
		// = 
		
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
		//int numberFinishedItems = Integer.parseInt((String) slService.dbRead(
		//		MigrationSqlQueries.count_finished_content_items_in_queue).get(0));
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
		//if ( !enabled ) return false;
		//int totalInQueue = Integer.parseInt((String) slService.dbRead(
		//		MigrationSqlQueries.count_total_content_items_in_queue).get(0));

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
		//if ( !enabled ) return false;
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
	 * @return the authzGroupService
	 */
	public AuthzGroupService getAuthzGroupService()
	{
		return authzGroupService;
	}

	/**
	 * @param authzGroupService the authzGroupService to set
	 */
	public void setAuthzGroupService(AuthzGroupService authzGroupService)
	{
		this.authzGroupService = authzGroupService;
	}

	/**
	 * @return the sessionManager
	 */
	public SessionManager getSessionManager()
	{
		return sessionManager;
	}

	/**
	 * @param sessionManager the sessionManager to set
	 */
	public void setSessionManager(SessionManager sessionManager)
	{
		this.sessionManager = sessionManager;
	}

	/**
	 * @return the userDirectoryService
	 */
	public UserDirectoryService getUserDirectoryService()
	{
		return userDirectoryService;
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

}