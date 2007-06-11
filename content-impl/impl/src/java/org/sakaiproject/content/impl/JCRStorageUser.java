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

import javax.jcr.Node;

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.time.api.Time;
import org.w3c.dom.Element;

/**
 * @author ieb
 *
 */
public class JCRStorageUser implements LiteStorageUser
{

	
	private BaseContentService baseContentService;

	public JCRStorageUser(BaseContentService bcs) {
		this.baseContentService = bcs;
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.LiteStorageUser#commit(org.sakaiproject.entity.api.Edit, javax.jcr.Node)
	 */
	public void commit(Edit edit, Object n)
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.LiteStorageUser#convertId2Storage(java.lang.String)
	 */
	public String convertId2Storage(String id)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.LiteStorageUser#newResource(java.lang.Object)
	 */
	public Entity newResource(Object source)
	{
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.LiteStorageUser#newResourceEdit(org.sakaiproject.entity.api.Entity)
	 */
	public Edit newResourceEdit(Object source)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#getDate(org.sakaiproject.entity.api.Entity)
	 */
	public Time getDate(Entity r)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#getOwnerId(org.sakaiproject.entity.api.Entity)
	 */
	public String getOwnerId(Entity r)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#isDraft(org.sakaiproject.entity.api.Entity)
	 */
	public boolean isDraft(Entity r)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#newContainer(java.lang.String)
	 */
	public Entity newContainer(String id)
	{
		return baseContentService.new BaseCollectionEdit(id);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#newContainer(org.w3c.dom.Element)
	 */
	public Entity newContainer(Element element)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#newContainer(org.sakaiproject.entity.api.Entity)
	 */
	public Entity newContainer(Entity other)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#newContainerEdit(java.lang.String)
	 */
	public Edit newContainerEdit(String id)
	{
		return baseContentService.new BaseCollectionEdit(id);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#newContainerEdit(org.w3c.dom.Element)
	 */
	public Edit newContainerEdit(Element element)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#newContainerEdit(org.sakaiproject.entity.api.Entity)
	 */
	public Edit newContainerEdit(Entity other)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#newResource(org.sakaiproject.entity.api.Entity, java.lang.String, java.lang.Object[])
	 */
	public Entity newResource(Entity container, String id, Object[] others)
	{
		return baseContentService.new BaseResourceEdit(id);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#newResource(org.sakaiproject.entity.api.Entity, org.w3c.dom.Element)
	 */
	public Entity newResource(Entity container, Element element)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#newResource(org.sakaiproject.entity.api.Entity, org.sakaiproject.entity.api.Entity)
	 */
	public Entity newResource(Entity container, Entity other)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#newResourceEdit(org.sakaiproject.entity.api.Entity, java.lang.String, java.lang.Object[])
	 */
	public Edit newResourceEdit(Entity container, String id, Object[] others)
	{
		return baseContentService.new BaseResourceEdit(id);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#newResourceEdit(org.sakaiproject.entity.api.Entity, org.w3c.dom.Element)
	 */
	public Edit newResourceEdit(Entity container, Element element)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#newResourceEdit(org.sakaiproject.entity.api.Entity, org.sakaiproject.entity.api.Entity)
	 */
	public Edit newResourceEdit(Entity container, Entity other)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.util.StorageUser#storageFields(org.sakaiproject.entity.api.Entity)
	 */
	public Object[] storageFields(Entity r)
	{
		throw new UnsupportedOperationException();
	}

}
