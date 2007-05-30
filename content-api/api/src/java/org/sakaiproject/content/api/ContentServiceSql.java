/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/chat/chat-api/api/src/java/org/sakaiproject/chat/api/ChatServiceSql.java $
 * $Id: ChatServiceSql.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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
package org.sakaiproject.content.api;




/**
 * database methods.
 */
public interface ContentServiceSql {

   /**
    * returns the sql statement which retrieves the body from the specified table (content_resource_body_binary).
    */
   public String getBodySql(String table);

   /**
    * returns the sql statement which retrieves the collection id from the specified table.
    */
   public String getCollectionIdSql(String table);

   /**
    * returns the sql statement which deletes content from the specified table (content_resource_body_binary).
    */
   public String getDeleteContentSql(String table);

   /**
    * returns the sql statement which inserts content into the specified table (content_resource_body_binary).
    */
   public String getInsertContentSql(String table);

   /**
    * returns the sql statement which retrieves the number of content resources from the content_resource table.
    */
   public String getNumContentResources1Sql();

   /**
    * returns the sql statement which retrieves the number of content resources from the content_collection table.
    */
   public String getNumContentResources2Sql();

   /**
    * returns the sql statement which retrieves the number of content resources from the content_resource table.
    */
   public String getNumContentResources3Sql();

   /**
    * returns the sql statement which retrieves the number of content resources from the content_collection table.
    */
   public String getNumContentResources4Sql();

   /**
    * returns the sql statement which retrieves resource id from the content_resource table.
    */
   public String getResourceId1Sql();

   /**
    * returns the sql statement which retrieves the resource id from the content_resource_body_binary table.
    */
   public String getResourceId2Sql();

   /**
    * returns the sql statement which retrieves the resource id from the specified table.
    */
   public String getResourceId3Sql(String table);

   /**
    * returns the sql statement which retrieves the resource id and xml fields from the content_resource table.
    */
   public String getResourceIdXmlSql();

   /**
    * returns the sql statement which retrieves resource uuid from the content_resource table.
    */
   public String getResourceUuidSql();

   /**
    * returns the sql statement which updates the resource uuid in the content_resource table for a given resource uuid.
    */
   public String getUpdateContentResource1Sql();

   /**
    * returns the sql statement which updates the resource uuid in the content_resource table for a given resource id.
    */
   public String getUpdateContentResource2Sql();

   /**
    * returns the sql statement which updates the file path and xml fields in the content_resource table for a given resource id.
    */
   public String getUpdateContentResource3Sql();
}
