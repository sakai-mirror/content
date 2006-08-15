/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/citation/trunk/citation-api/api/src/java/org/sakaiproject/citation/api/Schema.java $
 * $Id: Schema.java 1552 2006-08-11 06:20:50Z jimeng@umich.edu $
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

public interface Schema
{
	public interface Field
	{
		String getNamespaceAbbreviation();
		String getIdentifier();
		String getDescription();
		String getValueType();
		boolean isRequired();
		int getMinCardinality();
		int getMaxCardinality();
		Object getDefaultValue();
		
	}
	
	public String getNamespaceUri();
	
	public String getNamespaceUri(String abbrev);
	
	String getIdentifier();
	
	public List getNamespaceAbbreviations();
	
	public List getFields();
	
	public List getRequiredFields();
	
	public Field getField(String name);
	
	public Field getField(int index);
}
