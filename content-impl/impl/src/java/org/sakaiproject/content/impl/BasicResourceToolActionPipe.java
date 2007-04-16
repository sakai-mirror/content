/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation.
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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;

public class BasicResourceToolActionPipe 
	implements ResourceToolActionPipe 
{
	protected byte[] content;
	protected ContentEntity contentEntity;
	protected InputStream contentInputStream;
	protected String contentType;
	protected String initializationId;
	protected Map propertyValues = new Hashtable();
	protected Map revisedPropertyValues = new Hashtable();
	protected byte[] revisedContent;
	protected InputStream revisedContentStream;
	protected String revisedContentType;
	protected String helperId;
	protected ResourceToolAction action;
	protected boolean actionCompleted;
	protected String errorMessage;
	protected boolean actionCanceled;
	protected boolean errorEncountered;
	protected String fileName;
	protected Object revisedListItem;
	
	/**
	 * @return the helperId
	 */
	public String getHelperId()
	{
		return this.helperId;
	}

	/**
	 * @param helperId the helperId to set
	 */
	public void setHelperId(String helperId)
	{
		this.helperId = helperId;
	}

	public BasicResourceToolActionPipe(String interactionId, ResourceToolAction action)
	{
		this.initializationId = interactionId;
		this.action = action;
	}

	public byte[] getContent() 
	{
		return this.content;
	}

	public ContentEntity getContentEntity() 
	{
		return this.contentEntity;
	}

	public InputStream getContentStream() 
	{
		return this.contentInputStream;
	}

	public String getMimeType() 
	{
		return this.contentType;
	}

	public String getInitializationId() 
	{
		return this.initializationId;
	}

	public Object getPropertyValue(String name) 
	{
		return (String) this.propertyValues.get(name);
	}

	public byte[] getRevisedContent() 
	{
		return this.revisedContent;
	}

	public InputStream getRevisedContentStream() 
	{
		return this.revisedContentStream;
	}

	public String getRevisedMimeType() 
	{
		return this.revisedContentType;
	}

	public Map getRevisedResourceProperties() 
	{
		return this.revisedPropertyValues;
	}

	public void setContent(byte[] content) 
	{
		this.content = content;
	}

	public void setContentEntity(ContentEntity entity) 
	{
		this.contentEntity = entity;
	}

	public void setContentStream(InputStream ostream) 
	{
		this.contentInputStream = ostream;
	}

	public void setMimeType(String type) 
	{
		this.contentType = type;
	}

	public void setInitializationId(String id) 
	{
		this.initializationId = id;
	}

	public void setResourceProperty(String name, String value) 
	{
		if(value == null)
		{
			this.propertyValues.remove(name);
		}
		else
		{
			this.propertyValues.put(name, value);
		}
	}

	public void setRevisedContent(byte[] content) 
	{
		this.revisedContent = content;
	}

	public void setRevisedContentStream(InputStream istream) 
	{
		this.revisedContentStream = istream;
	}

	public void setRevisedMimeType(String type) 
	{
		this.revisedContentType = type;
	}

	public void setRevisedResourceProperty(String name, String value) 
	{
		if(value == null)
		{
			this.revisedPropertyValues.remove(name);
		}
		else
		{
			this.revisedPropertyValues.put(name, value);
		}
	}

	public boolean isActionCanceled() 
	{
		return this.actionCanceled;
	}

	public boolean isErrorEncountered() 
	{
		return this.errorEncountered;
	}

	public void setActionCanceled(boolean actionCanceled) 
	{
		this.actionCanceled = actionCanceled;
	}

	public void setErrorEncountered(boolean errorEncountered) 
	{
		this.errorEncountered = errorEncountered;
	}

	public void setResourceProperty(String key, List list) 
	{
		this.propertyValues.put(key, list);
	}

	public ResourceToolAction getAction() 
	{
		return this.action;
	}

	public void setRevisedResourceProperty(String name, List list) 
	{
		this.revisedPropertyValues.put(name, list);
	}

	public boolean isActionCompleted() 
	{
		return this.actionCompleted;
	}

	public void setActionCompleted(boolean actionCompleted) 
	{
		this.actionCompleted = actionCompleted;
	}

	public String getErrorMessage() 
	{
		return this.errorMessage;
	}

	public void setErrorMessage(String msg) 
	{
		this.errorMessage = msg;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceToolActionPipe#getContentstring()
	 */
	public String getContentstring()
	{
		String rv = null;
		byte[] content = getContent();
		if(content != null)
		{
			rv = new String( content );
//			try
//			{
//				rv = new String( content, "UTF-8" );
//			}
//			catch(UnsupportedEncodingException e)
//			{
//				rv = new String( content );
//			}
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceToolActionPipe#getFileName()
	 */
	public String getFileName()
	{
		return this.fileName;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceToolActionPipe#getFileUploadSize()
	 */
	public int getFileUploadSize()
	{
		int rv = 0;
		if(this.revisedContent != null)
		{
			rv = this.revisedContent.length;
		}
		return rv;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.api.ResourceToolActionPipe#setFileName(java.lang.String)
	 */
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public void setRevisedListItem(Object item) 
	{
		this.revisedListItem = item;
	}

	public Object getRevisedListItem() 
	{
		return revisedListItem;
	}

}
