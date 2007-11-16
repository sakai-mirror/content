package org.sakaiproject.content.impl.jcr.migration;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.impl.JCRStorageUser;
import org.sakaiproject.content.migration.api.ContentToJCRCopier;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.jcr.api.JCRService;

public class ContentToJCRCopierImpl implements ContentToJCRCopier {
    private static Log log = LogFactory.getLog(ContentToJCRCopierImpl.class);

    private JCRService jcrService;
    private JCRStorageUser jcrStorageUser;
    private ContentHostingService oldCHSService;


    public static final String jcr_content_prefix = "/sakai/content";

    public String rstripSlash(String theString) {
        if (theString.endsWith("/")) {
            return theString.substring(0, theString.length()-1);
        }
        else {
            return theString;
        }
    }

    /* 
     * Copies the ContentCollection folder to JCR for the content collection at 
     * absolute path 'abspath'. This does not copy the contents of the folder, just
     * creates the empty nt:folder and copies all the Sakai metadata.
     */
    public boolean copyCollectionFromCHStoJCR(String abspath) {
        try {
            Session jcrSession = jcrService.getSession();

            // We don't need to convert the root node
            if (abspath == "/") {
                log.info("Root collection, not copying");
                return true;
            }

            ContentCollectionEdit   collection = oldCHSService.editCollection(abspath);

            // The parent folder in jcr
            String collectionIDwithNoSlash = collection.getId();
            ///if (collectionIDwithNoSlash.endsWith("/")) {
            //    collectionIDwithNoSlash = collectionIDwithNoSlash.substring(0, collectionIDwithNoSlash.length()-1);
            //}
            collectionIDwithNoSlash = rstripSlash(collectionIDwithNoSlash);

            int lastSlashIndex = collectionIDwithNoSlash.lastIndexOf("/");
            String parentFolderPath = jcr_content_prefix + collection.getId().substring(0,lastSlashIndex);

            String collectionName = collectionIDwithNoSlash.substring(lastSlashIndex+1,collectionIDwithNoSlash.length());

            // The Parent Folder JCR Node
            Node parentFolderNode = (Node) jcrSession.getItem(parentFolderPath);

            Node collectionNode = parentFolderNode.addNode(collectionName, "nt:folder");
            collectionNode.addMixin("sakaijcr:properties-mix");
            collectionNode.addMixin("mix:lockable");

            jcrStorageUser.copy(collection,((Object) collectionNode));
            jcrSession.save();
            oldCHSService.cancelCollection(collection);
            return true;
        }
        catch (Exception e) {
            log.error("Problems migrating collection: " + abspath, e);
        }

        return false;
    }

    /*
     * This will copy the resource from the Legacy ContentHosting implementation 
     * to the JCR Repository
     */
    public boolean copyResourceFromCHStoJCR(String abspath) {
        try {    
            Session jcrSession = jcrService.getSession();

            ContentResourceEdit resource = oldCHSService.editResource(abspath);

            // See what the mimeType property is
            String resourceMimeType = resource.getProperties().getProperty(
                    ResourceProperties.PROP_CONTENT_TYPE);

            // The last modified time as milliseconds from the epoch
            long resourceLastMod = resource.getProperties().getTimeProperty(
                    ResourceProperties.PROP_MODIFIED_DATE).getTime();

            // The last modified time as a GregorianCalendar instance 
            Calendar  lastModCalendar = GregorianCalendar.getInstance();
            lastModCalendar.setTimeInMillis(resourceLastMod);

            ContentCollection  parent = resource.getContainingCollection();
            String  resourceNodePath = jcr_content_prefix + resource.getId();

            String  parentNodePath = jcr_content_prefix + parent.getId();
            Node  parentNode = (Node) jcrSession.getItem(rstripSlash(parentNodePath));

            int lastSlashIndex = resource.getId().lastIndexOf("/");
            String resourceName = resource.getId().substring(lastSlashIndex+1,resource.getId().length());

            Node resourceNode = parentNode.addNode(resourceName, "nt:file");
            resourceNode.addMixin("sakaijcr:properties-mix");
            resourceNode.addMixin("mix:lockable");
            Node contentNode = resourceNode.addNode("jcr:content","nt:resource");
            contentNode.setProperty("jcr:data",resource.streamContent());
            contentNode.setProperty("jcr:mimeType",resourceMimeType);
            contentNode.setProperty("jcr:lastModified",lastModCalendar);
            jcrStorageUser.copy(resource,resourceNode);
            jcrSession.save();
            oldCHSService.cancelResource(resource);
            return true;
        }
        catch (Exception e) {
            log.debug("Failed to migrate resource: " + abspath, e);
        }

        return false;
    }

    public void setJcrService(JCRService jcrService) {
        this.jcrService = jcrService;
    }

    public void setJcrStorageUser(JCRStorageUser jcrStorageUser) {
        this.jcrStorageUser = jcrStorageUser;
    }

    public void setOldCHSService(ContentHostingService oldCHSService) {
        this.oldCHSService = oldCHSService;
    }

}
