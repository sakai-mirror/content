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
 * methods for accessing content data in an oracle database.
 */
public class ContentServiceSqlOracle extends ContentServiceSqlDefault
{
	/**
	 * returns the sql statement to add the FILE_SIZE column to the CONTENT_RESOURCE table.
	 */
	public String getAddFilesizeColumnSql(String table)
	{
		return "alter table " + table + " add FILE_SIZE NUMBER(18) default NULL";
	}

	/**
	 * returns the sql statement to add the CONTEXT column to the CONTENT_RESOURCE table.
	 */
	public String getAddContextColumnSql(String table)
	{
		return "alter table " + table + " add CONTEXT VARCHAR2(99) default NULL";
	}

	/**
	 * returns the sql statement to add the RESOURCE_TYPE_ID column to the specified table.
	 */
	public String getAddResourceTypeColumnSql(String table)
	{
		return "alter table " + table + " add RESOURCE_TYPE_ID VARCHAR2(255) default null"; 
	}
	
	/**
	 * returns the sql statement to add an index of the CONTENT column to the CONTENT_RESOURCE table.
	 */
	public String getAddContextIndexSql(String table)
	{
		return "create index " + table.trim() + "_CI on " + table + " (CONTEXT)";
	}

	public String getFilesizeColumnExistsSql() 
	{
		return "select column_name from user_tab_columns where table_name = 'CONTENT_RESOURCE' and column_name = 'FILE_SIZE'";
	}

}
