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


package org.sakaiproject.content.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.entity.api.Reference;

/**
 * An InteractionAction defines a kind of ResourceToolAction which involves 
 * user interaction to complete the action. The Resources tool will invoke 
 * a helper to render an html page (or possibly a series of pages), process 
 * the response(s) and turn control back to the Resources tool when done.  
 *
 */
public interface InteractionAction extends ResourceToolAction
{
	/**
	 * Access the unique identifier for the tool that will handle this action. 
	 * This is the identifier by which the helper is registered with the 
	 * ToolManager.
	 * @return
	 */
	public String getHelperId();
	
	/**
	 * Access a list of properties that should be provided to the helper if they are defined. 
	 * Returning null or empty list indicates no properties are needed by the helper.
	 * @return a List of Strings if property values are required. 
	 */
	public List getRequiredPropertyKeys();
	
	/**
	 * ResourcesAction calls this method before starting the helper. This is intended to give
	 * the registrant a chance to do any preparation needed before the helper starts with respect
	 * to this action and the reference specified in the parameter. The method returns a String
	 * (possibly null) which will be provided as the "initializationId" parameter to other
	 * methods and in 
	 * @param reference
	 * @return TODO
	 */
	public String initializeAction(Reference reference);
	
	/**
	 * @param reference
	 * @param initializationId TODO
	 */
	public void finalizeAction(Reference reference, String initializationId);
	
	/**
	 * @param reference
	 * @param initializationId TODO
	 */
	public void cancelAction(Reference reference, String initializationId);
	
}
