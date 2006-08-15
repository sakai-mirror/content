/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/citation/trunk/citation-tool/tool/src/java/org/sakaiproject/citation/tool/CitationHelperAction.java $
 * $Id: CitationHelperAction.java 1597 2006-08-15 19:28:28Z jimeng@umich.edu $
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

package org.sakaiproject.citation.tool;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.citation.api.CitationHelper;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.citation.cover.CitationService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * 
 */
public class CitationHelperAction extends VelocityPortletPaneledAction
{
	public static ResourceLoader rb = new ResourceLoader("citation");

	protected final static Log Log = LogFactory.getLog(CitationHelperAction.class);
	
	protected static final String CREATE_CONTEXT = "buildCreatePanelContext";
	protected static final String EDIT_CONTEXT = "buildEditPanelContext";
	protected static final String LIST_CONTEXT = "buildListPanelContext";
	protected static final String SEARCH_CONTEXT = "buildSearchPanelContext";
	protected static final String VIEW_CONTEXT = "buildViewPanelContext";

	protected static final String ELEMENT_ID_CREATE_FORM = "createForm";
	protected static final String ELEMENT_ID_EDIT_FORM = "editForm";
	protected static final String ELEMENT_ID_LIST_FORM = "listForm";
	protected static final String ELEMENT_ID_SEARCH_FORM = "searchForm";
	protected static final String ELEMENT_ID_VIEW_FORM = "viewForm";
	
	protected static final String MODE_CREATE = "citation.mode.create";
	protected static final String MODE_EDIT = "citation.mode.edit";
	protected static final String MODE_LIST = "citation.mode.list";
	protected static final String MODE_SEARCH = "citation.mode.search";
	protected static final String MODE_VIEW = "citation.mode.view";
	
	protected static final String PARAM_FORM_NAME = "FORM_NAME";

	protected static final String STARTED = "citation.state.started";
	
	protected static final String TEMPLATE_CREATE = "citation/sakai_citation-create";
	protected static final String TEMPLATE_EDIT = "citation/sakai_citation-edit";
	protected static final String TEMPLATE_LIST = "citation/sakai_citation-list";
	protected static final String TEMPLATE_SEARCH = "citation/sakai_citation-search";
	protected static final String TEMPLATE_VIEW = "citation/sakai_citation-view";
	
	/**
	 * build the context in helper mode.
	 * 
	 * @return The name of the template to use.
	 */
	public static String buildCitationsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		initHelper(portlet, context, rundata, state);
		
		// always put appropriate bundle in velocity context
		context.put("tcite", rb);
		
		context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		context.put("citationToolId", CitationHelper.CITATION_ID);
		context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);

		// set me as the helper class
		state.setAttribute(VelocityPortletPaneledAction.STATE_HELPER, CitationHelperAction.class.getName());

		// make sure observers are disabled
		VelocityPortletPaneledAction.disableObservers(state);
		
		String template = "";
		String mode = (String) state.getAttribute(CitationHelper.STATE_HELPER_MODE);
		if(mode == null || mode.equals("") || mode.equalsIgnoreCase(MODE_LIST))
		{
			mode = MODE_LIST;
			state.setAttribute(CitationHelper.STATE_HELPER_MODE, mode);
		}
		if(mode.equalsIgnoreCase(MODE_LIST))
		{
			template = buildListPanelContext(portlet, context, rundata, state);
		}
		else if(mode.equalsIgnoreCase(MODE_CREATE))
		{
			template = buildCreatePanelContext(portlet, context, rundata, state);
		}
		else if(mode.equalsIgnoreCase(MODE_SEARCH))
		{
			template = buildSearchPanelContext(portlet, context, rundata, state);
		}
		else if(mode.equalsIgnoreCase(MODE_EDIT))
		{
			template = buildEditPanelContext(portlet, context, rundata, state);
		}
		else if(mode.equalsIgnoreCase(MODE_VIEW))
		{
			template = buildViewPanelContext(portlet, context, rundata, state);
		}
		else
		{
			template = buildListPanelContext(portlet, context, rundata, state);
		}
		
		return template;
		
	}	// buildCitationsPanelContext
	
	/**
	 * build the context.
	 * 
	 * @return The name of the template to use.
	 */
	public static String buildCreatePanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// always put appropriate bundle in velocity context
		context.put("tcite", rb);
		
		context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		context.put("citationToolId", CitationHelper.CITATION_ID);
		context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);
		
		context.put(PARAM_FORM_NAME, ELEMENT_ID_CREATE_FORM);
		
		List schemas = CitationService.getSchemas();
		context.put("TEMPLATES", schemas);
		
		Schema defaultSchema = CitationService.getDefaultSchema();
		context.put("DEFAULT_TEMPLATE", defaultSchema);

		return TEMPLATE_CREATE;
		
	}	// buildCreatePanelContext
	
	/**
     * @param portlet
     * @param context
     * @param rundata
     * @param state
     * @return
     */
    public static String buildEditPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
    {
		// always put appropriate bundle in velocity context
		context.put("tcite", rb);
		
		context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		context.put("citationToolId", CitationHelper.CITATION_ID);
		context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);
	    
	    return TEMPLATE_EDIT;
    }

	/**
	 * build the context.
	 * 
	 * @return The name of the template to use.
	 */
	public static String buildListPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// always put appropriate bundle in velocity context
		context.put("tcite", rb);
		
		context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		context.put("citationToolId", CitationHelper.CITATION_ID);
		context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);
		
		context.put(PARAM_FORM_NAME, ELEMENT_ID_LIST_FORM);

		return TEMPLATE_LIST;
		
	}	// buildListPanelContext

	/**
	 * build the context.
	 * 
	 * @return The name of the template to use.
	 */
	public static String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		initHelper(portlet, context, rundata, state);
		
		// always put appropriate bundle in velocity context
		context.put("tcite", rb);

		context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		context.put("citationToolId", CitationHelper.CITATION_ID);
		context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);
		
		// set me as the helper class
		state.setAttribute(VelocityPortletPaneledAction.STATE_HELPER, CitationHelperAction.class.getName());

		// make sure observers are disabled
		VelocityPortletPaneledAction.disableObservers(state);
		
		String template = "";
		
		String mode = (String) state.getAttribute(CitationHelper.STATE_HELPER_MODE);
		if(mode == null || mode.equals("") || mode.equalsIgnoreCase(MODE_LIST))
		{
			template = buildListPanelContext(portlet, context, rundata, state);
		}
		else if(mode.equalsIgnoreCase(MODE_CREATE))
		{
			template = buildCreatePanelContext(portlet, context, rundata, state);
		}
		else if(mode.equalsIgnoreCase(MODE_SEARCH))
		{
			template = buildSearchPanelContext(portlet, context, rundata, state);
		}
		else if(mode.equalsIgnoreCase(MODE_EDIT))
		{
			template = buildEditPanelContext(portlet, context, rundata, state);
		}
		else if(mode.equalsIgnoreCase(MODE_VIEW))
		{
			template = buildViewPanelContext(portlet, context, rundata, state);
		}
		else
		{
			template = buildListPanelContext(portlet, context, rundata, state);
		}
		
		return template;
		
	}	// buildMainPanelContext

	/**
     * @param portlet
     * @param context
     * @param rundata
     * @param state
     * @return
     */
    public static String buildSearchPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
    {
		// always put appropriate bundle in velocity context
		context.put("tcite", rb);
		
		context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		context.put("citationToolId", CitationHelper.CITATION_ID);
		context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);
	    
	    return TEMPLATE_SEARCH;
	    
    }	// buildSearchPanelContext
	
	/**
     * @param portlet
     * @param context
     * @param rundata
     * @param state
     * @return
     */
    public static String buildViewPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
    {
		// always put appropriate bundle in velocity context
		context.put("tcite", rb);
		
		context.put("mainFrameId", CitationHelper.CITATION_FRAME_ID);
		context.put("citationToolId", CitationHelper.CITATION_ID);
		context.put("specialHelperFlag", CitationHelper.SPECIAL_HELPER_ID);
	    
	    return TEMPLATE_VIEW;
	    
    }	// buildViewPanelContext
	
	/**
	* 
	*/
	public static void doCreate ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		//state.setAttribute(CitationHelper.SPECIAL_HELPER_ID, CitationHelper.CITATION_ID);
		state.setAttribute (CitationHelper.STATE_HELPER_MODE, MODE_CREATE);

	}	// doCreate

	/**
	* 
	*/
	public static void doList ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute (CitationHelper.STATE_HELPER_MODE, MODE_LIST);

	}	// doList

	/**
	* 
	*/
	public static void doSearch ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute (CitationHelper.STATE_HELPER_MODE, MODE_SEARCH);

	}	// doSearch

	/**
	* 
	*/
	public static void doView ( RunData data)
	{
		// get the state object
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());

		state.setAttribute (CitationHelper.STATE_HELPER_MODE, MODE_VIEW);

	}	// doView

	

	protected static void initHelper(VelocityPortlet portlet, Context context,
	        RunData rundata, SessionState state)
	{
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		// String prefix = (String)
		// toolSession.getAttribute(PermissionsHelper.PREFIX);
		// String targetRef = (String)
		// toolSession.getAttribute(PermissionsHelper.TARGET_REF);
		// String description = (String)
		// toolSession.getAttribute(PermissionsHelper.DESCRIPTION);
		// String rolesRef = (String)
		// toolSession.getAttribute(PermissionsHelper.ROLES_REF);
		// if (rolesRef == null) rolesRef = targetRef;

		toolSession.setAttribute(STARTED, new Boolean(true));

		// // setup for editing the permissions of the site for this tool, using
		// the roles of this site, too
		// state.setAttribute(PermissionsAction.STATE_REALM_ID, targetRef);
		//		
		// // use the roles from this ref's AuthzGroup
		// state.setAttribute(PermissionsAction.STATE_REALM_ROLES_ID, rolesRef);
		//
		// // ... with this description
		// state.setAttribute(PermissionsAction.STATE_DESCRIPTION, description);
		//
		// // ... showing only locks that are prpefixed with this
		// state.setAttribute(PermissionsAction.STATE_PREFIX, prefix);

		// start the helper
		state.setAttribute(CitationHelperAction.STATE_MODE,
				CitationHelperAction.MODE_LIST);
		
	}	// initHelper



	/**
	 * Remove the state variables used internally, on the way out.
	 */
	private void cleanupState(SessionState state)
	{
		state.removeAttribute(CitationHelper.STATE_HELPER_MODE);
		state.removeAttribute(VelocityPortletPaneledAction.STATE_HELPER);

		// re-enable observers
		VelocityPortletPaneledAction.enableObservers(state);
		
	}	// cleanupState

}	// class CitationHelperAction
