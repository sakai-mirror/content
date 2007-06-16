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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;

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

	private JCRStorage storage;

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
	protected JCRStorage newStorage()
	{
		log.error("Retruning Storage as "+storage);
		return storage; 
	}



	/**
	 * @return the storage
	 */
	public JCRStorage getStorage()
	{
		return storage;
	}

	/**
	 * @param storage the storage to set
	 */
	public void setStorage(JCRStorage storage)
	{
		this.storage = storage;
	}
	
	
	/** The following overrides are in place to prevent DbContentService from using the database,
	 * These methods appear to have leaked out of the storage layer and should not really be here */
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.DbContentService#countQuery(java.lang.String, java.lang.String)
	 */
	@Override
	int countQuery(String sql, String param) throws IdUnusedException
	{
		log.error("Should not be using this countQuery with JCR");
		throw new UnsupportedOperationException("Should not be using this countQuery with JCR");
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.DbContentService#setUuidInternal(java.lang.String, java.lang.String)
	 */
	@Override
	protected void setUuidInternal(String id, String uuid)
	{
		log.error("JCR uuids are immutable and cannot be set");
		throw new UnsupportedOperationException("JCR uuids are immutable and cannot be set");
		//storage.setResourceUuid(id,uuid);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.BaseContentService#moveCollection(org.sakaiproject.content.api.ContentCollectionEdit, java.lang.String)
	 */
	@Override
	protected String moveCollection(ContentCollectionEdit thisCollection, String new_folder_id) throws PermissionException, IdUnusedException, TypeException, InUseException, OverQuotaException, IdUsedException, ServerOverloadException
	{
		return storage.moveCollection(thisCollection, new_folder_id);
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.content.impl.BaseContentService#moveResource(org.sakaiproject.content.api.ContentResourceEdit, java.lang.String)
	 */
	@Override
	protected String moveResource(ContentResourceEdit thisResource, String new_id) throws PermissionException, IdUnusedException, TypeException, InUseException, OverQuotaException, IdUsedException, ServerOverloadException
	{
		return storage.moveResource(thisResource, new_id);
	}
    /* (non-Javadoc)
     * @see org.sakaiproject.content.impl.DbContentService#findUuid(java.lang.String)
     */
    @Override
    protected String findUuid(String id)
    {
		log.error("Should not be using this findUuid with JCR, getUuid dipuplicates the effort ");
		throw new UnsupportedOperationException("Should not be using this findUuid with JCR");
    }
    /* (non-Javadoc)
     * @see org.sakaiproject.content.impl.DbContentService#getUuid(java.lang.String)
     */
    @Override
    public String getUuid(String id)
    {
    	return storage.getUuid(id);
    }
    /* (non-Javadoc)
     * @see org.sakaiproject.content.impl.DbContentService#setUuid(java.lang.String, java.lang.String)
     */
    @Override
    public void setUuid(String id, String uuid) throws IdInvalidException
    {
		log.error("JCR uuids are immutable and cannot be set");
		throw new UnsupportedOperationException("JCR uuids are immutable and cannot be set");
    }
	
    /* (non-Javadoc)
     * @see org.sakaiproject.content.impl.DbContentService#resolveUuid(java.lang.String)
     * This method is crazy, why have resolve, find and get UUID that all do the same thing ?
     * Got to override it just in case :(
     * I am almost certain the the horrible catch throwable is not necessary, but its there to make it
     * behave the same as the DbContentService version
     */
    @Override
    public String resolveUuid(String uuid)
    {
    	try {
    		return getUuid(uuid);
    	} catch ( Throwable t ) {
    		log.error("resolve UUID failed  for "+uuid);
    	}
    	return null;
    }
    
    /* (non-Javadoc)
     * @see org.sakaiproject.content.impl.DbContentService#convertToFile()
     */
    @Override
    protected void convertToFile()
    {
		log.error("JCR manages where data is stored, it is not possible with the service to convert a JCR from DB storage to File Storage, please contact your JCR supplier for a utility");
		throw new UnsupportedOperationException("JCR manages where data is stored, it is not possible with the service to convert a JCR from DB storage to File Storage, please contact your JCR supplier for a utility");
    }
}
