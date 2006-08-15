/**********************************************************************************
 * $URL:  $
 * $Id:  $
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

/**
 * CitationHelper describes the contract between the CitationHelper and its clients. 
 */
public interface CitationHelper
{
	/** The identifier by which the CitationHelper can be located (call ToolManager.getTool(HELPER_ID) */
	public static final String CITATION_ID = "sakai.citation.tool";
	
	public static final String SPECIAL_HELPER_ID = "sakai.special.helper.id";
	
	public static final String HELPER_CLASS_NAME = "org.sakaiproject.citation.tool.CitationHelperAction";
	
	public static final String HELPER_INITIAL_ACTION = "doList";
	
	public static final String HELPER_DISPATCHER = "buildHelperContext";
	
	
	/** The name of the state attribute indicating which mode the helper is in */
	public static final String STATE_HELPER_MODE = "citation.helper.mode";

	public static final String CITATION_FRAME_ID = "Citations";

	/** The name of the state attribute for the build-context */
	public static final String BUILD_CONTEXT = "sakai.special.helper.build_path";

	
}
