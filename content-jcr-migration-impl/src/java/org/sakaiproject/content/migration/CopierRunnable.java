package org.sakaiproject.content.migration;

import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.migration.api.ContentToJCRCopier;

public class CopierRunnable extends SakaiRequestEmulator implements Runnable {
	private static Log log = LogFactory.getLog(CopierRunnable.class);
	
	private static final String ORIGINAL_MIGRATION_EVENT = "ORIGINAL_MIGRATION";

	private ContentToJCRCopier copier;
	private ThingToMigrate thing;
	private Session jcrSession;
	
	public void init() {
	}
	
	public void destroy() {
	}
	
	public CopierRunnable() {
		
	}
	
	public void run() {
		setTestUser(SUPER_USER);
		startEmulatedRequest(SUPER_USER);
		log.info("About to try and migrate: " + thing.contentId + " , " + thing.eventType);
		migrateOneItem();
		endEmulatedRequest();
	}

	private void migrateOneItem()
	{
		setTestUser(SUPER_USER);
		startEmulatedRequest(SUPER_USER);
		// ContentResources in the Original CHS always end with '/'
		if (thing.contentId.endsWith("/"))
		{
			// This is a ContentCollection
			if (thing.eventType.equals(ORIGINAL_MIGRATION_EVENT))
			{
				copier.copyCollectionFromCHStoJCR(jcrSession, thing.contentId);
			}
			else if (thing.eventType.equals(ContentHostingService.EVENT_RESOURCE_ADD))
			{
				copier.copyCollectionFromCHStoJCR(jcrSession, thing.contentId);
			}
			else if (thing.eventType.equals(ContentHostingService.EVENT_RESOURCE_REMOVE))
			{
				copier.deleteItem(jcrSession, thing.contentId);
			}
			else if (thing.eventType.equals(ContentHostingService.EVENT_RESOURCE_WRITE))
			{
				copier.copyCollectionFromCHStoJCR(jcrSession, thing.contentId);
			}
		}
		else
		{
			// This is a ContentResource
			if (thing.eventType.equals(ORIGINAL_MIGRATION_EVENT))
			{
				copier.copyResourceFromCHStoJCR(jcrSession, thing.contentId);
			}
			else if (thing.eventType.equals(ContentHostingService.EVENT_RESOURCE_ADD))
			{
				copier.copyResourceFromCHStoJCR(jcrSession, thing.contentId);
			}
			else if (thing.eventType.equals(ContentHostingService.EVENT_RESOURCE_REMOVE))
			{
				copier.deleteItem(jcrSession, thing.contentId);
			}
			else if (thing.eventType.equals(ContentHostingService.EVENT_RESOURCE_WRITE))
			{
				copier.copyResourceFromCHStoJCR(jcrSession, thing.contentId);
			}
		}
		//try {
		//	Thread.sleep(1000);
		//} catch (InterruptedException e) {
		//	log.error("Problems while sleeping during CHS->JCR Migration.", e);
		//}
		
		endEmulatedRequest();
	}

	public void setCopier(ContentToJCRCopier copier) {
		this.copier = copier;
	}

	public void setThing(ThingToMigrate thing) {
		this.thing = thing;
	}

	public void setJcrSession(Session jcrSession) {
		this.jcrSession = jcrSession;
	}
}
