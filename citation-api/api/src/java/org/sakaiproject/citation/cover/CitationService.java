/**********************************************************************************
 * $URL:  $
 * $Id:   $
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

package org.sakaiproject.citation.cover;


import org.sakaiproject.component.cover.ComponentManager;

/**
 * 
 */
public class CitationService 
{
	private static org.sakaiproject.citation.api.CitationService m_instance;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.citation.api.CitationService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.citation.api.CitationService) ComponentManager
						.get(org.sakaiproject.citation.api.CitationService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.citation.api.CitationService) ComponentManager
					.get(org.sakaiproject.citation.api.CitationService.class);
		}
	}


	public static org.sakaiproject.citation.api.Schema getSchema(java.lang.String name)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getSchema(name);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.citation.api.CitationService#getSchemas()
	 */
	public static java.util.List getSchemas()
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getSchemas();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.citation.api.CitationService#listSchemas()
	 */
	public static java.util.List listSchemas()
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.listSchemas();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.citation.api.CitationService#getDefaultSchema()
	 */
	public static org.sakaiproject.citation.api.Schema getDefaultSchema()
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getDefaultSchema();		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.citation.api.CitationService#newCitation(java.lang.String)
	 */
	public static org.sakaiproject.citation.api.CitationEdit newCitation(java.lang.String mediatype)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.newCitation(mediatype);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#archive(java.lang.String, org.w3c.dom.Document, java.util.Stack, java.lang.String, java.util.List)
	 */
	public static java.lang.String archive(java.lang.String siteId, org.w3c.dom.Document doc, java.util.Stack stack,
	        java.lang.String archivePath, java.util.List attachments)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.archive(siteId, doc, stack, archivePath, attachments);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntity(org.sakaiproject.entity.api.Reference)
	 */
	public static org.sakaiproject.entity.api.Entity getEntity(org.sakaiproject.entity.api.Reference ref)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getEntity(ref);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityAuthzGroups(org.sakaiproject.entity.api.Reference, java.lang.String)
	 */
	public static java.util.Collection getEntityAuthzGroups(org.sakaiproject.entity.api.Reference ref, java.lang.String userId)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getEntityAuthzGroups(ref, userId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityDescription(org.sakaiproject.entity.api.Reference)
	 */
	public static java.lang.String getEntityDescription(org.sakaiproject.entity.api.Reference ref)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getEntityDescription(ref);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityResourceProperties(org.sakaiproject.entity.api.Reference)
	 */
	public static org.sakaiproject.entity.api.ResourceProperties getEntityResourceProperties(org.sakaiproject.entity.api.Reference ref)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getEntityResourceProperties(ref);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getEntityUrl(org.sakaiproject.entity.api.Reference)
	 */
	public static java.lang.String getEntityUrl(org.sakaiproject.entity.api.Reference ref)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getEntityUrl(ref);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getHttpAccess()
	 */
	public static org.sakaiproject.entity.api.HttpAccess getHttpAccess()
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getHttpAccess();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#getLabel()
	 */
	public static java.lang.String getLabel()
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.getLabel();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#merge(java.lang.String, org.w3c.dom.Element, java.lang.String, java.lang.String, java.util.Map, java.util.Map, java.util.Set)
	 */
	public static java.lang.String merge(java.lang.String siteId, org.w3c.dom.Element root, java.lang.String archivePath,
	        java.lang.String fromSiteId, java.util.Map attachmentNames, java.util.Map userIdTrans,
	        java.util.Set userListAllowImport)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return null;
		}
		return instance.merge(siteId, root, archivePath, fromSiteId, attachmentNames, userIdTrans, userListAllowImport);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#parseEntityReference(java.lang.String, org.sakaiproject.entity.api.Reference)
	 */
	public static boolean parseEntityReference(java.lang.String reference, org.sakaiproject.entity.api.Reference ref)
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return false;
		}
		return instance.parseEntityReference(reference, ref);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.EntityProducer#willArchiveMerge()
	 */
	public static boolean willArchiveMerge()
	{
		org.sakaiproject.citation.api.CitationService instance = getInstance();
		if(instance == null)
		{
			return false;
		}
		return instance.willArchiveMerge();
	}
	

}
