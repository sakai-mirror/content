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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.impl.BaseContentService.Storage;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.jcr.api.JCRService;

/**
 * <p>
 * DbContentService is an extension of the BaseContentService with a database
 * implementation.
 * </p>
 * <p>
 * The sql scripts in src/sql/chef_content.sql must be run on the database.
 * </p>
 */
public class JCRContentService extends DbContentService
{

	/** Our logger. */
	private static final Log log = LogFactory.getLog(JCRContentService.class);

	private Storage storage;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#init()
	 */
	@Override
	public void init()
	{
		m_autoDdl = false;
		m_convertToFile = false;
		m_sqlService = null;
		super.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#newStorage()
	 */
	@Override
	protected Storage newStorage()
	{
		return storage; 
	}



	/**
	 * @return the storage
	 */
	public Storage getStorage()
	{
		return storage;
	}

	/**
	 * @param storage the storage to set
	 */
	public void setStorage(Storage storage)
	{
		this.storage = storage;
	}
}
