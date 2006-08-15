/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/citation/trunk/citation-api/api/src/java/org/sakaiproject/citation/api/Citation.java $
 * $Id: Citation.java 1527 2006-08-09 18:13:59Z jimeng@umich.edu $
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
import java.util.Map;

import org.sakaiproject.entity.api.Entity;


/**
 * @author jimeng
 *
 */
public interface Citation extends Entity
{
	/**
	 * Access the brief "title" of the resource, which can be used to display the item in a list of items.
	 * @return The display name.  
	 */
	public String getDisplayName();

	/**
	 * Access a url from which the full-text of the item may be retrieved.
	 * @return The URL from which the full text version of the document can be retrieved. May be null if no full-text version is known to be available.
	 */
	public String getFullTextUrl();

	/**
	 * Access a URL from which can be retrieved an image that be a thumbnail or preview of the document.
	 * @return The URL for the preview or thumbnail image.  May be null if no image is known to be available.
	 */
	public String getImageUrl();

	/**
	 * Access a URL from which can be retrieved bibliographic information about the document, possibly including a URL	 for access to a full-text version of the resource.
	 * @return The URL from which a bibliographice record may be retrieved. May be null if no online bibliograophic record is known to be available.
	 */
	public String getCitationUrl();

	/**
	 * Access a URL for the Search Source that was used to retrieve this citation.
	 * @return The URL for the Search Source that was used to retrieve this citation.  May be null if TwinPeaks was not used to retrieve the resource (if, for example, the citation was constructed by the user).
	 */
	public String getSearchSourceUrl();

	/**
	 * Access a mapping of name-value pairs for various bibliographic information about the resource.
	 * Ideally, the names will be expressed as URIs for standard tags representing nuggets of bibliographic metadata.
	 * Values will be strings in formats specified by the standards defining the property names.  For example if the 
	 * name is a URI referencing a standard format for a "publication_date" and the standard indicates that the value
	 * should be expressed as in xs:date format, the value should be expressed as a string representation of xs:date.
	 * @return The mapping of name-value pairs.  The mapping may be empty, but it should never be null.
	 */
	public Map getCitationProperties();

	/**
	 * Access a list of names of citation properties defined for this resource.
	 * @return The list of property names.  The list may be empty, but it should never be null.
	 */
	public List getCitationPropertyNames();

	/**
	 * Access a string representation of the value of a named property.
	 * @param name The name of the property for which a value is to be returned.
	 * @return A string representation of the value of the named property.  May be null if the property is not defined.
	 */
	public Object getCitationProperty(String name);
	
	/**
	 * Access the schema for the Citation
	 * @return th
	 */
	public Schema getSchema();
	
} // interface Citation

