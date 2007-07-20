/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.content.impl;

/**
 * database methods.
 */
public interface ContentServiceSql
{
	/**
	 * returns the sql statement which retrieves the body from the specified table (content_resource_body_binary).
	 */
	String getBodySql(String table);

	/**
	 * returns the sql statement which retrieves the collection id from the specified table.
	 */
	String getCollectionIdSql(String table);

	/**
	 * returns the sql statement which deletes content from the specified table (content_resource_body_binary).
	 */
	String getDeleteContentSql(String table);

	/**
	 * returns the sql statement which inserts content into the specified table (content_resource_body_binary).
	 */
	String getInsertContentSql(String table);

	/**
	 * returns the sql statement which retrieves the number of content resources from the content_resource table.
	 */
	String getNumContentResources1Sql();

	/**
	 * returns the sql statement which retrieves the number of content resources from the content_collection table.
	 */
	String getNumContentResources2Sql();

	/**
	 * returns the sql statement which retrieves the number of content resources from the content_resource table.
	 */
	String getNumContentResources3Sql();

	/**
	 * returns the sql statement which retrieves the number of content resources from the content_collection table.
	 */
	String getNumContentResources4Sql();

	/**
	 * returns the sql statement which retrieves resource id from the content_resource table.
	 */
	String getResourceId1Sql();

	/**
	 * returns the sql statement which retrieves the resource id from the content_resource_body_binary table.
	 */
	String getResourceId2Sql();

	/**
	 * returns the sql statement which retrieves the resource id from the specified table.
	 */
	String getResourceId3Sql(String table);

	/**
	 * returns the sql statement which retrieves the resource id and xml fields from the content_resource table.
	 */
	String getResourceIdXmlSql();

	/**
	 * returns the sql statement which retrieves resource uuid from the content_resource table.
	 */
	String getResourceUuidSql();

	/**
	 * returns the sql statement which updates the resource uuid in the content_resource table for a given resource uuid.
	 */
	String getUpdateContentResource1Sql();

	/**
	 * returns the sql statement which updates the resource uuid in the content_resource table for a given resource id.
	 */
	String getUpdateContentResource2Sql();

	/**
	 * returns the sql statement which updates the file path and xml fields in the content_resource table for a given resource id.
	 */
	String getUpdateContentResource3Sql();
}
