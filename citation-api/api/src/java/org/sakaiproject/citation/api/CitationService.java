/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/citation/trunk/citation-api/api/src/java/org/sakaiproject/citation/api/CitationService.java $
 * $Id: CitationService.java 1552 2006-08-11 06:20:50Z jimeng@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.citation.api;

import java.util.List;

import org.sakaiproject.entity.api.EntityProducer;

import org.sakaiproject.citation.api.CitationEdit;
import org.sakaiproject.citation.api.Schema;

public interface CitationService extends EntityProducer
{
	/** This string can be used to find the service in the service manager. */
	public static final String SERVICE_NAME = CitationService.class.getName();

	/**
	 * 
	 * @param listId
	 * @return
	 */
	public CitationEdit newCitation(String mediatype);
	
	/**
	 * Access a schema by its name
	 * @param name The name of the schema
	 * @return The schema, or null if no schema has been defined with that name.
	 */
	public Schema getSchema(String name);
	
	/**
	 * Access the default schema
	 * @return The default schema, or null if no schema has been set as the default.
	 */
	public Schema getDefaultSchema();
	
	/**
	 * Access a list of identifiers for all schemas that have been defined
	 * @return A list of Strings representing the names of schemas that have been defined.
	 */
	public List listSchemas();

	/**
	 * Access a list of all schemas that have been defined
	 * @return A list of Schema objects representing the schemas that have been defined.
	 */
	public List getSchemas();

}	// interface CitationService

