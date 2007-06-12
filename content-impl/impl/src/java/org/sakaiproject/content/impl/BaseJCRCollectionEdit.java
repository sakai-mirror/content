/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.jcr.Node;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.impl.BaseContentService.BaseCollectionEdit;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author ieb
 *
 */
public class BaseJCRCollectionEdit extends BaseCollectionEdit
{

	private Node n;
	/**
	 * @param baseContentService
	 * @param id
	 */
	public BaseJCRCollectionEdit(BaseContentService baseContentService, String id)
	{
		baseContentService.super(id);
	}

	
	/**
	 * @param baseContentService
	 * @param el
	 */
	public BaseJCRCollectionEdit(BaseContentService baseContentService, Element el)
	{
		baseContentService.super(el);
	}
	/**
	 * @param baseContentService
	 * @param other
	 */
	public BaseJCRCollectionEdit(BaseContentService baseContentService, ContentCollection other)
	{
		baseContentService.super(other);
	}

	
}
