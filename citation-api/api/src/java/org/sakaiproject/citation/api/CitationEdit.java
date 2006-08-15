/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/citation/trunk/citation-api/api/src/java/org/sakaiproject/citation/api/CitationEdit.java $
 * $Id: CitationEdit.java 1527 2006-08-09 18:13:59Z jimeng@umich.edu $
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

import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.entity.api.Edit;

public interface CitationEdit extends Citation, Edit
{
	public void setDisplayName(String name);
	
	public void setFullTextUrl(String url);
	
	public void setImageUrl(String url);
	
	public void setCitationUrl(String url);
	
	public void setSearchSourceUrl(String url);
	
	public void setCitationProperty(String name, Object value);


}	// interface CitationEdit

