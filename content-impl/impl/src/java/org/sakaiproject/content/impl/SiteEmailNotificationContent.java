/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.EmailNotification;
import org.sakaiproject.util.SiteEmailNotification;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.FormattedText;

/**
 * <p>
 * SiteEmailNotificationContent fills the notification message and headers with details from the content change that triggered the notification event.
 * </p>
 * 
 * @author Sakai Software Development Team
 */
public class SiteEmailNotificationContent extends SiteEmailNotification
{
	private static ResourceBundle rb = ResourceBundle.getBundle("siteemacon");
	private final String MULTIPART_BOUNDARY = "======sakai-multi-part-boundary======";
	private final String BOUNDARY_LINE = "\n\n--"+MULTIPART_BOUNDARY+"\n";
	private final String TERMINATION_LINE = "\n\n--"+MULTIPART_BOUNDARY+"--\n\n";

	private final String MIME_ADVISORY = "This message is for MIME-compliant mail readers.";
	
	/** The related site id. */
	protected String m_siteId = null;
	protected Event event = null;

	
	protected String plainTextContent() {
		return generateContentForType(false);
	}
	
	protected String htmlContent() {
		return generateContentForType(true);
	}

	private String generateContentForType(boolean shouldProduceHtml) {
		// get the content & properties
		Reference ref = EntityManager.newReference(this.event.getResource());
		// TODO:  ResourceProperties props = ref.getProperties();

		// get the function
		String function = this.event.getEvent();
		String subject = getSubject(this.event);

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// get a site title
		String title = siteId;
		try
		{
			Site site = SiteService.getSite(siteId);
			title = site.getTitle();
		}
		catch (Exception ignore)
		{
		}
		
		StringBuilder buf = new StringBuilder();
		addMessageText(buf, ref, subject, title, function, shouldProduceHtml);
		return buf.toString();
	}

	/**
	 * Construct.
	 */
	public SiteEmailNotificationContent()
	{
	}

	/**
	 * Construct.
	 */
	public SiteEmailNotificationContent(String siteId)
	{
		super(siteId);
	}

	/**
	 * @inheritDoc
	 */
	protected String getResourceAbility()
	{
		return ContentHostingService.EVENT_RESOURCE_READ;
	}
	
	protected EmailNotification makeEmailNotification() {
		return new SiteEmailNotificationContent();
	}

	/**
	 * @inheritDoc
	 */
	private void addMessageText(StringBuilder buf, Reference ref, String subject, String title, String function, boolean doHtml)
	{
		ResourceProperties props = ref.getProperties();
		String resourceName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		String description = props.getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION);
		String url = ref.getUrl();
		String blankLine = "\n\n";
		String newLine = "\n";

		if ( doHtml ) 
		{
			title = FormattedText.escapeHtmlFormattedTextarea(title);
			subject = FormattedText.escapeHtmlFormattedTextarea(subject);
			resourceName = FormattedText.escapeHtmlFormattedTextarea(resourceName);
			description = FormattedText.escapeHtmlFormattedTextarea(description);
			blankLine = "\n</p><p>\n";
			newLine = "<br/>\n";
		}

		// get the resource copyright alert property
		boolean copyrightAlert = props.getProperty(ResourceProperties.PROP_COPYRIGHT_ALERT) != null ? true : false;

		// Now build up the message text.
		if (doHtml) {
			buf.append("<p>");
		}
		if (ContentHostingService.EVENT_RESOURCE_ADD.equals(function))
		{
			buf.append(rb.getString("anewres"));
		}
		else
		{
			buf.append(rb.getString("anewres2"));
		}
		buf.append(" ");
		buf.append(rb.getString("the"));
		buf.append(" \"");
		buf.append(title);
		buf.append("\" ");
		buf.append(rb.getString("sitat"));
		buf.append(" ");
		if ( doHtml )
		{
			buf.append("<a href=\"");
			buf.append(ServerConfigurationService.getPortalUrl());
			buf.append("\">");
			buf.append(ServerConfigurationService.getString("ui.service", "Sakai"));
			buf.append("</a>");
		}
		else
		{
			buf.append(ServerConfigurationService.getString("ui.service", "Sakai"));
			buf.append(" (");
			buf.append(ServerConfigurationService.getPortalUrl());
			buf.append(")");
		}
		buf.append(blankLine);

		// add location
		String path = constructPath(ref.getReference());
		buf.append(rb.getString("locsit") + " \"" + title + "\" > " + rb.getString("reso") + " " + path + " > ");
		if ( doHtml ) 
		{
			buf.append("<a href=\"");
			buf.append(url);
			buf.append("\">");
			buf.append(resourceName);
			buf.append("</a>");
		}
		else
		{
			buf.append(resourceName);
		}

		if (copyrightAlert)
		{
			buf.append(" (c)");
		}
		buf.append(blankLine);

		// resource description
		if ((description != null) && (description.length() > 0))
		{
			buf.append(rb.getString("descrip") + " " + description);
			buf.append(blankLine);
		}

		// add a reference to the resource for non-HTML
		if ( ! doHtml )
		{
			buf.append("\n" + rb.getString("resour") + " " + resourceName);
			if (copyrightAlert)
			{
				buf.append(" (c)");
			}
			buf.append(" " + url);
			buf.append("\n\n");  // End on a blank line
		}

		// Add the tag
		if (doHtml) {
			buf.append("<hr/>" + newLine + rb.getString("this") + " "
	                                + ServerConfigurationService.getString("ui.service", "Sakai") + " (<a href=\"" + ServerConfigurationService.getPortalUrl()
	                                + "\" >" + ServerConfigurationService.getPortalUrl() + "</a>) " + rb.getString("forthe") + " " + title + " "
	                                + rb.getString("site") + newLine + rb.getString("youcan"));
		} else {
			buf.append(rb.getString("separator") + newLine + rb.getString("this") + " "
                    + ServerConfigurationService.getString("ui.service", "Sakai") + " (" + ServerConfigurationService.getPortalUrl()
                    + ") " + rb.getString("forthe") + " " + title + " " + rb.getString("site") + newLine + rb.getString("youcan"));
		}
		
		if (doHtml) {
			buf.append("</p>");
		}
		
	}

	/**
	 * @inheritDoc
	 */
	protected List<String> getHeaders(Event event)
	{
		List<String> rv = super.getHeaders(event);

		// the Subject
		rv.add("Subject: " + getSubject(event));

		// from
		rv.add(getFrom(event));

		// to
		rv.add(getTo(event));

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	protected String getTag(String newLine, String title)
	{
		// We handle this in the bodies of the messages
		return "";
	}

	/**
	 * @inheritDoc
	 */
	protected String getTag(String newLine, boolean renderHtml)
	{
		// We handle this in the bodies of the messages
		return "";
	}

	/**
	 * Get the message for the email.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return the message for the email.
	 */
	protected String getMessage(Event event)
	{
		// get a site title: use either the configured site, or if not configured, the site (context) of the resource
		Reference ref = EntityManager.newReference(event.getResource());
		String title = (getSite() != null) ? getSite() : ref.getContext();
		try
		{
			Site site = SiteService.getSite(title);
			title = site.getTitle();
		}
		catch (Exception ignore) {}

		StringBuilder message = new StringBuilder();
		message.append(MIME_ADVISORY);
		
		message.append(BOUNDARY_LINE);
		message.append(plainTextHeaders());
		message.append(plainTextContent());
		message.append(getTag(title, false) );
		
		message.append(BOUNDARY_LINE);
		message.append(htmlHeaders());
		message.append(htmlPreamble());
		message.append(htmlContent());
		message.append(getTag(title, true) );
		message.append(htmlEnd());
		
		message.append(TERMINATION_LINE);
		return message.toString();	}

	/**
	 * Form a "Bread Crumb" style path showing the folders in which this referenced resource lives.
	 * 
	 * @param ref
	 *        The reference string to the resource.
	 * @return The path string for this resource.
	 */
	protected String constructPath(String ref)
	{
		StringBuffer buf = new StringBuffer();

		// expect the ref to be /content/group/site/folder/folder2/folderEtc/file.ext
		String[] parts = StringUtil.split(ref, Entity.SEPARATOR);

		// 0 is null, 1 is "content", 2 is "group" or whatever, 3 is the site, the last is the file name
		if (parts.length > 4)
		{
			// grow this collection id as we descend into the collections
			String root = Entity.SEPARATOR + parts[2] + Entity.SEPARATOR + parts[3] + Entity.SEPARATOR;

			// take all the collection parts
			for (int i = 4; i < parts.length - 1; i++)
			{
				buf.append(" > ");
				String collectionId = parts[i];
				root = root + collectionId + Entity.SEPARATOR;
				try
				{
					// get the display name
					ContentCollection collection = ContentHostingService.getCollection(root);
					buf.append(collection.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME));
				}
				catch (Exception any)
				{
					// use the id if there's a problem
					buf.append(collectionId);
				}
			}
		}

		return buf.toString();
	}

	/**
	 * Get the subject for the email.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return the subject for the email.
	 */
	protected String getSubject(Event event)
	{
		Reference ref = EntityManager.newReference(event.getResource());
		Entity r = ref.getEntity();
		ResourceProperties props = ref.getProperties();

		// get the function
		String function = event.getEvent();

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// get a site title
		String title = siteId;
		try
		{
			Site site = SiteService.getSite(siteId);
			title = site.getTitle();
		}
		catch (Exception ignore)
		{
		}

		// use the message's subject
		String resourceName = props.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
		return "[ " + title + " - "
				+ (ContentHostingService.EVENT_RESOURCE_ADD.equals(function) ? rb.getString("new") : rb.getString("chan")) + " "
				+ rb.getString("reso2") + " ] " + resourceName;
	}

	/**
	 * Add to the user list any other users who should be notified about this ref's change.
	 * 
	 * @param users
	 *        The user list, already populated based on site visit and resource ability.
	 * @param ref
	 *        The entity reference.
	 */
	protected void addSpecialRecipients(List users, Reference ref)
	{
		// include any users who have AnnouncementService.SECURE_ALL_GROUPS and getResourceAbility() in the context
		String contextRef = SiteService.siteReference(ref.getContext());

		// get the list of users who have SECURE_ALL_GROUPS
		List allGroupUsers = SecurityService.unlockUsers(ContentHostingService.AUTH_RESOURCE_ALL_GROUPS, contextRef);

		// filter down by the permission
		if (getResourceAbility() != null)
		{
			List allGroupUsers2 = SecurityService.unlockUsers(getResourceAbility(), contextRef);
			allGroupUsers.retainAll(allGroupUsers2);
		}

		// remove any in the list already
		allGroupUsers.removeAll(users);

		// combine
		users.addAll(allGroupUsers);
	}

	protected String plainTextHeaders() {
		return "Content-Type: text/plain\n\n";
	}
	
	protected String htmlHeaders() {
		return "Content-Type: text/html\n\n";
	}
	
	protected String htmlPreamble() {
		StringBuilder buf = new StringBuilder();
		buf.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"\n");
		buf.append("    \"http://www.w3.org/TR/html4/loose.dtd\">\n");
		buf.append("<html>");
		buf.append("  <head><title>");
		buf.append(getSubject(event));
		buf.append("</title></head>");
		buf.append("  <body>");
		return buf.toString();
	}
	
	protected String htmlEnd() {
		return "  </body></html>";
	}

	/**
	 * Do the notification.
	 * 
	 * @param notification
	 *        The notification responding to the event.
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 */
	public void notify(Notification notification, Event event)
	{
		this.event = event;
	
		super.notify(notification, event);
	}

}
