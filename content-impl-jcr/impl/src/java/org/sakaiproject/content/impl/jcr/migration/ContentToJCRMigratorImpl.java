package org.sakaiproject.content.impl.jcr.migration;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.migration.api.ContentToJCRMigrator;
import org.sakaiproject.jcr.api.JCRService;

public class ContentToJCRMigratorImpl implements ContentToJCRMigrator {
    private static Log log = LogFactory.getLog(ContentToJCRMigratorImpl.class);
    
    private JCRService jcrService;
    private ContentHostingService oldCHSService;
    
    
    public static final String jcr_content_prefix = "/sakai/content";
    
    /* 
     * Copies the ContentCollection folder to JCR for the content collection at 
     * absolute path 'abspath'. This does not copy the contents of the folder, just
     * creates the empty nt:folder and copies all the Sakai metadata.
     */
    public boolean copyCollectionFromCHStoJCR(String abspath) { /*
          Session jcrSession = jcrService.getSession();

          // We don't need to convert the root node
          if (abspath == "/") {
              log.info("Root collection, not copying");
              return true;
          }

         ContentCollection   collection = oldCHSService.getCollection(abspath);
            
           // The parent folder in jcr
         String collectionIDwithNoSlash = collection.getId();
         if (collectionIDwithNoSlash.endsWith("/")) {
             collectionIDwithNoSlash = collectionIDwithNoSlash.substring(0, collectionIDwithNoSlash.length()-1);
         }
         
        // String [] collectionParts 
         
         int lastSlashIndex = collectionIDwithNoSlash.lastIndexOf("/");
         String parentFolderPath = jcr_content_prefix + collection.getId().substring(0,lastSlashIndex);
         */
         /*
         
         // The Parent Folder JCR Node
         Node parentFolderNode = (Node) jcrSession.getItem(parentFolderPath);
            
            collectionNode = parentFolderNode.addNode(
                   collection.id.rstrip('/').split('/').pop(), "nt:folder")
            collectionNode.addMixin("sakaijcr:properties-mix")
            collectionNode.addMixin("mix:lockable")
            self.jcrStorageUser.copy(collection,collectionNode)
            jcrSession.save() */
        return false;
    }

    public boolean copyResourceFromCHStoJCR(String abspath) { /*
        def copyResourceFromCHStoJCR(self, abspath):
            """This will copy the resource from the Legacy ContentHosting implementation 
            to the JCR Repository"""
            jcrSession = self.jcrService.getSession()

            resource = self.oldCHSService.getResource(abspath)

            # See what the mimeType property is
            resourceMimeType = resource.getProperties().getProperty(
                                           ResourceProperties.PROP_CONTENT_TYPE)
            
            # The last modified time as milliseconds from the epoch
            resourceLastMod = resource.getProperties().getTimeProperty(
                                           ResourceProperties.PROP_MODIFIED_DATE).time

            # The last modified time as a GregorianCalendar instance 
            lastModCalendar = GregorianCalendar.getInstance()
            lastModCalendar.setTimeInMillis(resourceLastMod)

            parent = resource.containingCollection
            resourceNodePath = JcrContentHostingMigration.jcr_content_prefix + resource.id

            parentNodePath = JcrContentHostingMigration.jcr_content_prefix + parent.id
            parentNode = jcrSession.getItem(parentNodePath.rstrip("/"))
            resourceNode = parentNode.addNode(resource.id.split('/').pop(), "nt:file")
            resourceNode.addMixin("sakaijcr:properties-mix")
            resourceNode.addMixin("mix:lockable")
            contentNode = resourceNode.addNode("jcr:content","nt:resource")
            contentNode.setProperty("jcr:data",resource.streamContent())
            contentNode.setProperty("jcr:mimeType",resourceMimeType)
            contentNode.setProperty("jcr:lastModified",lastModCalendar)
            self.jcrStorageUser.copy(resource,resourceNode)
            jcrSession.save() */
        return false;
    }

}
