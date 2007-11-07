/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/content/branches/SAK-12105/content-test/test/src/main/java/org/sakaiproject/content/test/LoadTestContentHostingService.java $
 * $Id: CollectionSizeTests.java 37919 2007-11-07 12:51:42Z aaronz@vt.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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

package org.sakaiproject.content.test;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.testrunner.utils.SpringTestCase;
import org.sakaiproject.testrunner.utils.annotations.Autowired;
import org.sakaiproject.testrunner.utils.annotations.Resource;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/* 
 * This is a test of counting the recursive children in a 
 * collection using ContentCollection.getCollectionSize
 * 
 * The comments for the method reads:
 * Count the number of (recursive) children for a given id. examples: 
 * With a nested collection structure exactly like this: 
 * 
 * /a /a/b /a/b/1 /a/b/2 
 * 
 * getCollectionSize(/a) returns 3 (due to these three children: /a/b /a/b/1 /a/b/2) 
 * 
 * getCollectionSize(/a/b)
 * returns 2 (due to these two children: /a/b/1 /a/b/2) 
 * 
 * getCollectionSize(/a/b/1) returns 0 (nothing falls below this collection)      
 */
public class CollectionSizeTests extends SpringTestCase {

    protected final String CURRENT_USER_MARKER = "originalTestUser";
    protected final String ADMIN_USER = "admin";
    
    protected final String BASE_CONTENT_FOLDER = "/collection_size_tests/";
    
    protected final String FOLDER_A_PATH = BASE_CONTENT_FOLDER + "a/";
    protected final String FOLDER_B_PATH = BASE_CONTENT_FOLDER + "a/b/";
    protected final String FILE_1_PATH = BASE_CONTENT_FOLDER + "a/b/1.txt";
    protected final String FILE_2_PATH = BASE_CONTENT_FOLDER + "a/b/2.txt";
    
    private ContentHostingService contentHostingService;
    @Resource(name="org.sakaiproject.content.api.ContentHostingService")
    public void setContentHostingService(ContentHostingService contentHostingService) {
       this.contentHostingService = contentHostingService;
    }
    
    private SessionManager sessionManager;
    @Resource(name="org.sakaiproject.tool.api.SessionManager")
    public void setSessionManager(SessionManager sessionManager) {
       this.sessionManager = sessionManager;
    }

    private AuthzGroupService authzGroupService;
    @Autowired
    public void setAuthzGroupService(AuthzGroupService authzGroupService) {
       this.authzGroupService = authzGroupService;
    }
    
    @Override
    protected void setUp() throws Exception {
       super.setUp();

       // create test content in case there is not enough to test with
       // switch to the admin to run this
       Session currentSession = sessionManager.getCurrentSession();
       if (currentSession != null) {
          currentSession.setAttribute(CURRENT_USER_MARKER, currentSession.getUserId());
          currentSession.setUserId(ADMIN_USER);
          currentSession.setActive();
          sessionManager.setCurrentSession(currentSession);
          authzGroupService.refreshUser(ADMIN_USER);
       } else {
          throw new RuntimeException("no CurrentSession, cannot set to admin user");
       }
       
       /* Create the test files and directory if they don't exist yet */
       makeCollection(BASE_CONTENT_FOLDER);
       makeCollection(FOLDER_A_PATH);
       makeCollection(FOLDER_B_PATH);
       makeResource(FILE_1_PATH + "a/b/1");
       makeResource(FILE_2_PATH + "a/b/2");
    }
    
    /*
     * Makes a Folder if it doesn't exist yet, ignores exceptions if it already exists.
     * 
     * @param id The absolute path of the folder to create.
     */
    protected void makeCollection(String id) {
        ContentCollectionEdit cc;
        try {
            cc = contentHostingService.addCollection(id);
            contentHostingService.commitCollection(cc);
        } catch (IdUsedException e) {
            // It's cool if it's already there.
        } catch (IdInvalidException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InconsistentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /*
     * Creates a file if it doesn't exist yet, ignores exceptions if it already exists.
     * 
     * @param id The absolute path of the file to create.
     */
    protected void makeResource(String id) {
        try {
            ContentResourceEdit cr = contentHostingService.addResource(id);
            contentHostingService.commitResource(cr);
        } catch (PermissionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IdUsedException e) {
         // It's cool if it's already there.
        } catch (IdInvalidException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InconsistentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServerOverloadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OverQuotaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void tearDown() throws Exception {
       super.tearDown();

       // switch user back (if set)
       Session currentSession = sessionManager.getCurrentSession();
       String currentUserId = null;
       if (currentSession != null) {
          currentUserId = (String) currentSession.getAttribute(CURRENT_USER_MARKER);
       }
       currentSession.setUserId(currentUserId);
       sessionManager.setCurrentSession(currentSession);
       authzGroupService.refreshUser(currentUserId);
    }
    
    protected int getCollectionSizeWithoutAMillionExceptions(String id) {
        int size = -1;
        try {
            size = contentHostingService.getCollectionSize(id);
        } catch (IdUnusedException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (TypeException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (PermissionException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        return size;
    }
    
    public void testFolderA() {
        int size = getCollectionSizeWithoutAMillionExceptions(FOLDER_A_PATH);
        assertEquals(size, 3);
        
        size = getCollectionSizeWithoutAMillionExceptions(FOLDER_B_PATH);
        assertEquals(size, 2);
        
        size = getCollectionSizeWithoutAMillionExceptions(FILE_1_PATH);
        assertEquals(size, 0);
        
        size = getCollectionSizeWithoutAMillionExceptions(FILE_2_PATH);
        assertEquals(size, 0);
        
    }
    
    
}
