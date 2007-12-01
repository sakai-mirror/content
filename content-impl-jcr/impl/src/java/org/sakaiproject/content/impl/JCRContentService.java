/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * <p>
 * DbContentService is an extension of the BaseContentService with a database implementation.
 * </p>
 * <p>
 * The sql scripts in src/sql/chef_content.sql must be run on the database.
 * </p>
 */
public class JCRContentService extends DbContentService {

   /** Our logger. */
   private static final Log log = LogFactory.getLog(JCRContentService.class);

   private JCRStorage storage;
   public void setStorage(JCRStorage storage) {
      this.storage = storage;
   }

   private SessionManager sessionManager;
   public void setSessionManager(SessionManager sessionManager) {
      this.sessionManager = sessionManager;
   }

   private UserDirectoryService userDirectoryService;
   public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
      this.userDirectoryService = userDirectoryService;
   }


   @Override
   public void init() {
      m_autoDdl = false;
      m_convertToFile = false;
      m_sqlService = null;
      // log in as admin
      Session session = null;
      try {
         session = sessionManager.startSession();
         User u = userDirectoryService.getUser("admin");
         session.setUserId(u.getId());
         sessionManager.setCurrentSession(session);
         log.info("Session Has been set to admin " + sessionManager.getCurrentSessionUserId());
         super.init();
      } catch (UserNotDefinedException e) {
         log.error("Admin User is not defined.... wierd!", e);
      } finally {
         try {
            // There are plenty of other things in the startup thread that need setting
            // so we will keep the session arround
            // sessionManager.setCurrentSession(null);
            // session.invalidate();
         } catch (Exception ex) {
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.content.impl.DbContentService#newStorage()
    */
   @Override
   protected JCRStorage newStorage() {
      log.info("Retruning Storage as " + storage);
      return storage;
   }

   /**
    * The following overrides are in place to prevent DbContentService from using the database,
    * These methods appear to have leaked out of the storage layer and should not really be here
    */
   @Override
   protected int countQuery(String sql, String param) throws IdUnusedException {
      log.error("Should not be using this countQuery with JCR");
      throw new UnsupportedOperationException("Should not be using this countQuery with JCR");
   }

   @Override
   protected void setUuidInternal(String id, String uuid) {
      log.error("JCR uuids are immutable and cannot be set");
      throw new UnsupportedOperationException("JCR uuids are immutable and cannot be set");
      // storage.setResourceUuid(id,uuid);
   }

   @Override
   protected String moveCollection(ContentCollectionEdit thisCollection, String new_folder_id)
         throws PermissionException, IdUnusedException, TypeException, InUseException,
         OverQuotaException, IdUsedException, ServerOverloadException {
      return storage.moveCollection(thisCollection, new_folder_id);
   }

   @Override
   protected String moveResource(ContentResourceEdit thisResource, String new_id)
         throws PermissionException, IdUnusedException, TypeException, InUseException,
         OverQuotaException, IdUsedException, ServerOverloadException {
      return storage.moveResource(thisResource, new_id);
   }

   @Override
   protected String findUuid(String id) {
      log.error("Should not be using this findUuid with JCR, getUuid dipuplicates the effort ");
      throw new UnsupportedOperationException("Should not be using this findUuid with JCR");
   }

   @Override
   public String getUuid(String id) {
      return storage.getUuid(id);
   }

   @Override
   public void setUuid(String id, String uuid) throws IdInvalidException {
      log.error("JCR uuids are immutable and cannot be set");
      throw new UnsupportedOperationException("JCR uuids are immutable and cannot be set");
   }

   /**
    * This method is crazy, why have resolve, find and get UUID that all do the same thing ? Got to
    *      override it just in case :( I am almost certain the the horrible catch throwable is not
    *      necessary, but its there to make it behave the same as the DbContentService version
    *      
    * @see org.sakaiproject.content.impl.DbContentService#resolveUuid(java.lang.String)
    */
   @Override
   public String resolveUuid(String uuid) {
      try {
         return getUuid(uuid);
      } catch (Throwable t) {
         log.error("resolve UUID failed  for " + uuid);
      }
      return null;
   }

   @Override
   protected void convertToFile() {
      log
            .error("JCR manages where data is stored, it is not possible with the service to convert a JCR from DB storage to File Storage, please contact your JCR supplier for a utility");
      throw new UnsupportedOperationException(
            "JCR manages where data is stored, it is not possible with the service to convert a JCR from DB storage to File Storage, please contact your JCR supplier for a utility");
   }

   @Override
   protected ContentCollection findCollection(String id) throws TypeException {
      // log.info("+++++++++ Start to find Collection "+id);
      ContentCollection cc = storage.getCollection(id);
      // log.info("========= End to find Collection "+id+" "+cc);
      return cc;
   }

   @Override
   protected ContentResource findResource(String id) throws TypeException {
      return storage.getResource(id);
   }

   @Override
   public int getCollectionSize(String id) throws IdUnusedException, TypeException,
         PermissionException {
      // Exception e = new Exception("Traceback:");
      // log.info("Get Collection Size called ", e);

      // return 0; // TODO : something sensible
      return countCollectionMembers(id, 0);
   }

   /**
    * This may not be an efficient way to get this. Need to look into an JCR SQL or XPath query to
    * do this maybe. However! It will make a great generalized unit test for ContentHosting after I
    * can remove it from here.
    * 
    * Recursively counts members. See the getCollectionSize javadoc on ContentHostingService API.
    * 
    * @param id The Collection ID. ex. /group/mysite/ @param currentCount This method is recursive,
    * call it starting with zero here @return The number of members (folders and files) recursively
    */
   @SuppressWarnings("unchecked")
   private int countCollectionMembers(String id, int currentCount) {
      ContentCollection cc = storage.getCollection(id);
      int newCount = 0;
      List<ContentEntity> members = cc.getMemberResources();
      for (ContentEntity ce : members) {
         if (ce instanceof ContentCollection) {
            newCount += countCollectionMembers(ce.getId(), newCount);
            newCount++;
         } else {
            newCount++;
         }
      }
      return newCount;
   }

   @Override
   public void addResourceToDeleteTable(ContentResourceEdit edit, String uuid, String userId)
         throws PermissionException {
      if (log.isDebugEnabled()) log.debug("Delete table not implemented in JCR, managed by persistance manager...");
   }

}
