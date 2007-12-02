package org.sakaiproject.content.multiplex;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;

public class ThirdPartyManagerRegistrar {
    private EntityManager entityManager;
    private ContentHostingService contentHostingService;
    
    public void init() {
        entityManager.registerEntityProducer(contentHostingService, 
                ContentHostingService.REFERENCE_ROOT);
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }
}
