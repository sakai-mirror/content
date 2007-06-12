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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.impl.BaseContentService.BaseCollectionEdit;
import org.sakaiproject.content.impl.BaseContentService.BaseResourceEdit;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author ieb
 */
public class JCRStorageUser implements LiteStorageUser
{

	private static final Log log = LogFactory.getLog(JCRStorageUser.class);

	private static final Object NT_FILE = null;

	private static final Object NT_FOLDER = null;

	private static final String REPOSITORY_PREFIX = "/sakai";

	private static final String DATE_FORMAT = null;

	private static final String E_GROUP_LIST = null;

	private static final String E_ACCESS_MODE = null;

	private static final String E_RELEASE_DATE = null;

	private static final String E_RETRACT_DATE = null;

	private static final String E_HIDDEN = null;

	private static final String E_CONTENT_LENGTH = null;

	private static final String E_CONTENT_TYPE = null;

	private static final String E_RESOURCE_TYPE = null;

	private static final String E_FILE_PATH = null;

	private BaseContentService baseContentService;

	private Map<String, String> jcrTypes;

	private Map<String, String> entityTypes;

	private Map<String, String> jcrToEntity;

	private Map<String, String> entityToJcr;

	public JCRStorageUser(BaseContentService bcs, Map<String, String> jcrTypes,
			Map<String, String> jcrToEntity, Map<String, String> entityToJcr)
	{
		this.baseContentService = bcs;
		this.jcrTypes = jcrTypes;
		this.jcrToEntity = jcrToEntity;
		this.entityToJcr = entityToJcr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#commit(org.sakaiproject.entity.api.Edit,
	 *      javax.jcr.Node)
	 */
	public void commit(Edit edit, Object o)
	{
		if (o instanceof Node)
		{
			Node n = (Node) o;
			copy(edit, n);
			try
			{
				n.save();
			}
			catch (RepositoryException e)
			{
				log.error("Failed to save node to JCR ", e);
			}
		}
	}

	/**
	 * @param edit
	 * @param n
	 */
	private void copy(Edit edit, Object o)
	{
		if ((o instanceof Node)
				&& ((edit instanceof BaseCollectionEdit) || (edit instanceof BaseResourceEdit)))
		{
			Node n = (Node) o;
			ResourceProperties rp = edit.getProperties();
			for (Iterator i = rp.getPropertyNames(); i.hasNext();)
			{
				String name = (String) i.next();
				Object v = rp.get(name);
				String jname = convertEntityName2JCRName(name);
				if (v instanceof String)
				{
					setJCRProperty(jname, (String) v, n);
				}
				else if (v instanceof List)
				{
					setJCRProperty(jname, (List) v, n);
				}
				else
				{
					setJCRProperty(jname, String.valueOf(v), n);
				}
			}
			if (edit instanceof BaseCollectionEdit)
			{
				BaseCollectionEdit bedit = (BaseCollectionEdit) edit;
				setJCRProperty(convertEntityName2JCRName(E_GROUP_LIST), new ArrayList(
						bedit.m_groups), n);
				if (bedit.m_access == null)
				{
					setJCRProperty(convertEntityName2JCRName(E_ACCESS_MODE),
							AccessMode.INHERITED.toString(), n);
				}
				else
				{
					setJCRProperty(convertEntityName2JCRName(E_ACCESS_MODE),
							bedit.m_access.toString(), n);
				}
				if (bedit.m_releaseDate != null)
				{
					setJCRProperty(convertEntityName2JCRName(E_RELEASE_DATE), new Date(
							bedit.m_releaseDate.getTime()), n);
				}
				if (bedit.m_retractDate != null)
				{
					setJCRProperty(convertEntityName2JCRName(E_RETRACT_DATE), new Date(
							bedit.m_retractDate.getTime()), n);
				}
				setJCRProperty(convertEntityName2JCRName(E_HIDDEN), bedit.m_hidden, n);

			}
			else if (edit instanceof BaseResourceEdit)
			{
				BaseResourceEdit bedit = (BaseResourceEdit) edit;
				setJCRProperty(convertEntityName2JCRName(E_CONTENT_LENGTH), bedit
						.getContentLength(), n);
				setJCRProperty(convertEntityName2JCRName(E_CONTENT_TYPE), bedit
						.getContentType(), n);
				setJCRProperty(convertEntityName2JCRName(E_RESOURCE_TYPE), bedit
						.getResourceType(), n);;
				setJCRProperty(convertEntityName2JCRName(E_FILE_PATH), bedit.m_filePath,
						n);;
				if (bedit.m_body != null)
				{

				}
				setJCRProperty(convertEntityName2JCRName(E_GROUP_LIST), new ArrayList(
						bedit.m_groups), n);
				if (bedit.m_access == null)
				{
					setJCRProperty(convertEntityName2JCRName(E_ACCESS_MODE),
							AccessMode.INHERITED.toString(), n);
				}
				else
				{
					setJCRProperty(convertEntityName2JCRName(E_ACCESS_MODE),
							bedit.m_access.toString(), n);
				}
				if (bedit.m_hidden)
				{	
					clearJCRProperty(convertEntityName2JCRName(E_RELEASE_DATE),n);
					clearJCRProperty(convertEntityName2JCRName(E_RETRACT_DATE),n);
				}
				else
				{
					if (bedit.m_releaseDate != null)
					{
						setJCRProperty(convertEntityName2JCRName(E_RELEASE_DATE),
								new Date(bedit.m_releaseDate.getTime()), n);
					}
					if (bedit.m_retractDate != null)
					{
						setJCRProperty(convertEntityName2JCRName(E_RETRACT_DATE),
								new Date(bedit.m_retractDate.getTime()), n);
					}
				}
				setJCRProperty(convertEntityName2JCRName(E_HIDDEN), bedit.m_hidden, n);
			}

		}
	}

	/**
	 * @param string
	 * @param n
	 * @throws RepositoryException 
	 * @throws  
	 */
	private void clearJCRProperty(String jname, Node n) 
	{
		
		try {
			Property p = n.getProperty(jname);
			if ( p != null ) {
				p.setValue((Value)null);
			}
		} catch ( RepositoryException re ) {
			log.error("Failed to clear property ");
		}
	}

	/**
	 * @param jname
	 * @param list
	 * @param n
	 */
	private void setJCRProperty(String jname, List list, Node n)
	{
		try
		{
			String stype = jcrTypes.get(jname);
			int type = PropertyType.STRING;
			if (stype != null)
			{
				type = PropertyType.valueFromName(stype);
			}
			Session s = n.getSession();
			ValueFactory vf = s.getValueFactory();
			switch (type)
			{
				case PropertyType.BINARY:
					throw new UnsupportedOperationException(
							"Cant set a binary list at the moment");
				case PropertyType.BOOLEAN:
				{
					Value[] sv = new Value[list.size()];
					for (int i = 0; i < sv.length; i++)
					{
						Object ov = list.get(i);
						if (ov instanceof Boolean)
						{
							sv[i] = vf.createValue(((Boolean) ov).booleanValue());
						}
						else
						{
							sv[i] = vf.createValue(new Boolean(String
									.valueOf(list.get(i))));
						}
					}

					try
					{
						n.setProperty(jname, sv);
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + list);
					}
					break;
				}
				case PropertyType.DATE:
				{
					Value[] sv = new Value[list.size()];
					for (int i = 0; i < sv.length; i++)
					{
						Object ov = list.get(i);
						if (ov instanceof Calendar)
						{
							sv[i] = vf.createValue((Calendar) ov);
						}
						else if (ov instanceof Date)
						{
							GregorianCalendar gc = new GregorianCalendar();
							gc.setTime((Date) ov);
							sv[i] = vf.createValue(gc);
						}
						else
						{
							GregorianCalendar gc = new GregorianCalendar();
							SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
							try
							{
								gc.setTime(sdf.parse(String.valueOf(ov)));
							}
							catch (ParseException e)
							{
								log.error("Failed to parse Date Value ", e);
							}

							sv[i] = vf.createValue(gc);
						}
					}
					try
					{
						n.setProperty(jname, sv);
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + list);
					}
					break;
				}
				case PropertyType.DOUBLE:
				{
					Value[] sv = new Value[list.size()];
					for (int i = 0; i < sv.length; i++)
					{
						Object ov = list.get(i);
						if (ov instanceof Double)
						{
							sv[i] = vf.createValue(((Double) ov).doubleValue());
						}
						else
						{
							sv[i] = vf.createValue(Double.parseDouble(String.valueOf(list
									.get(i))));
						}
					}
					try
					{
						n.setProperty(jname, sv);
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + list);
					}
					break;
				}
				case PropertyType.LONG:
				{
					Value[] sv = new Value[list.size()];
					for (int i = 0; i < sv.length; i++)
					{
						sv[i] = vf.createValue(Long
								.parseLong(String.valueOf(list.get(i))));
					}
					try
					{
						n.setProperty(jname, sv);
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + list);
					}
					break;
				}
				case PropertyType.UNDEFINED:
				case PropertyType.STRING:
				case PropertyType.REFERENCE:
				case PropertyType.NAME:
				case PropertyType.PATH:
				{
					Value[] sv = new Value[list.size()];
					for (int i = 0; i < sv.length; i++)
					{
						sv[i] = vf.createValue(String.valueOf(list.get(i)));
					}
					try
					{
						n.setProperty(jname, sv);
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + list);
					}
					break;
				}
			}
		}
		catch (RepositoryException e)
		{
			log.error("Failed to set propert " + jname + " to " + list, e);
		}

		// TODO Auto-generated method stub

	}

	/**
	 * @param jname
	 * @param string
	 * @param n
	 */
	private void setJCRProperty(String jname, Object string, Node n)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * @param n
	 * @param e
	 * @throws RepositoryException
	 */
	private void copy(Node n, Entity e) throws RepositoryException
	{

		// setup for properties
		if ( e instanceof BaseCollectionEdit ) {
			BaseCollectionEdit bce = (BaseCollectionEdit) e;
			bce.m_resourceType = ResourceType.TYPE_FOLDER;
		
			String refStr = getReference(bce.m_id);
			Reference ref = m_entityManager.newReference(refStr);
			String context = ref.getContext();
			
			// Site site = null;
			// try
			// {
			// site = m_siteService.getSite(ref.getContext());
			// }
			// catch (IdUnusedException e)
			// {
			//
			// }
			setProperties(n,bce);

			// the children (properties)
			NodeList children = el.getChildNodes();
		final int length = children.getLength();
		for (int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element) child;

			// look for properties
			if (element.getTagName().equals("properties"))
			{
				// re-create properties
				m_properties = new BaseResourcePropertiesEdit(element);
				
				
				
				if(m_prioritySortEnabled)
				{
					// setPriority();
				}
			}
			
			// look for groups
			else if(element.getTagName().equals(GROUP_LIST))
			{
				String groupRef = element.getAttribute(GROUP_NAME);
				if(groupRef != null)
				{
					m_groups.add(groupRef);
				} 
			}
			else if(element.getTagName().equals("rightsAssignment"))
			{
				
			}
		}

		// extract access
		AccessMode access = AccessMode.INHERITED;
		String access_mode = el.getAttribute(ACCESS_MODE);
		if(access_mode != null && !access_mode.trim().equals(""))
		{
			access = AccessMode.fromString(access_mode);
		}
		
		m_access = access;
		if(m_access == null || AccessMode.SITE == m_access)
		{
			m_access = AccessMode.INHERITED;
		}
		
		// extract release date
		// m_releaseDate = TimeService.newTime(0);
		String date0 = el.getAttribute(RELEASE_DATE);
		if(date0 != null && !date0.trim().equals(""))
		{
			m_releaseDate = TimeService.newTimeGmt(date0);
			if(m_releaseDate.getTime() <= START_OF_TIME)
			{
				m_releaseDate = null;
			}
		}
		
		// extract retract date
		// m_retractDate = TimeService.newTimeGmt(9999,12, 31, 23, 59, 59, 999);
		String date1 = el.getAttribute(RETRACT_DATE);
		if(date1 != null && !date1.trim().equals(""))
		{
			m_retractDate = TimeService.newTimeGmt(date1);
			if(m_retractDate.getTime() >= END_OF_TIME)
			{
				m_retractDate = null;
			}
		}
		
		String hidden = el.getAttribute(HIDDEN);
		m_hidden = hidden != null && ! hidden.trim().equals("") && ! Boolean.FALSE.toString().equalsIgnoreCase(hidden);
	}

	/**
	 * @param n
	 * @param bce
	 */
	private void setProperties(Node n, BaseCollectionEdit bce)
	{

	}

	/**
	 * @param name
	 * @return
	 */
	private String convertJCRName2EntityName(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param name
	 * @return
	 */
	private String convertEntityName2JCRName(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#convertId2Storage(java.lang.String)
	 */
	public String convertId2Storage(String id)
	{
		return REPOSITORY_PREFIX + id;
	}

	/**
	 * @param path
	 * @return
	 */
	private String convertStorageToId(String path)
	{
		if (path.startsWith(REPOSITORY_PREFIX))
		{
			path.substring(REPOSITORY_PREFIX.length());
		}
		log.error("Trying to convert a path to Id that is not a storage path " + path);
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#newResource(java.lang.Object)
	 */
	public Entity newResource(Object source)
	{
		// create a new resource of the required type and
		// copy the source (which is a JCR Node) into the resource
		if (source instanceof Node)
		{
			Node n = (Node) source;
			try
			{
				NodeType nt = n.getPrimaryNodeType();
				if (NT_FILE.equals(nt))
				{
					Entity e = newResource(null, convertStorageToId(n.getPath()), null);
					copy(n, e);
					return e;
				}
				else if (NT_FOLDER.equals(nt))
				{
					Entity e = newContainer(convertStorageToId(n.getPath()));
					copy(n, e);
					return e;
				}
			}
			catch (RepositoryException e1)
			{
				log.error("Failed to create new resource", e1);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#newResourceEdit(org.sakaiproject.entity.api.Entity)
	 */
	public Edit newResourceEdit(Object source)
	{
		// create a new resource of the required type and
		// copy the source (which is a JCR Node) into the resource
		if (source instanceof Node)
		{
			Node n = (Node) source;
			try
			{
				NodeType nt = n.getPrimaryNodeType();
				if (NT_FILE.equals(nt))
				{
					Edit e = newResourceEdit(null, convertStorageToId(n.getPath()), null);
					copy(n, e);
					return e;
				}
				else if (NT_FOLDER.equals(nt))
				{
					Edit e = newContainerEdit(convertStorageToId(n.getPath()));
					copy(n, e);
					return e;
				}
			}
			catch (RepositoryException e1)
			{
				log.error("Failed to create new resource", e1);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#getDate(org.sakaiproject.entity.api.Entity)
	 */
	public Time getDate(Entity r)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#getOwnerId(org.sakaiproject.entity.api.Entity)
	 */
	public String getOwnerId(Entity r)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#isDraft(org.sakaiproject.entity.api.Entity)
	 */
	public boolean isDraft(Entity r)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newContainer(java.lang.String)
	 */
	public Entity newContainer(String id)
	{
		return new BaseJCRCollectionEdit(baseContentService,id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newContainer(org.w3c.dom.Element)
	 */
	public Entity newContainer(Element element)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newContainer(org.sakaiproject.entity.api.Entity)
	 */
	public Entity newContainer(Entity other)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newContainerEdit(java.lang.String)
	 */
	public Edit newContainerEdit(String id)
	{
		return new BaseJCRCollectionEdit(baseContentService,id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newContainerEdit(org.w3c.dom.Element)
	 */
	public Edit newContainerEdit(Element element)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newContainerEdit(org.sakaiproject.entity.api.Entity)
	 */
	public Edit newContainerEdit(Entity other)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newResource(org.sakaiproject.entity.api.Entity,
	 *      java.lang.String, java.lang.Object[])
	 */
	public Entity newResource(Entity container, String id, Object[] others)
	{
		return new BaseJCRResourceEdit(baseContentService,id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newResource(org.sakaiproject.entity.api.Entity,
	 *      org.w3c.dom.Element)
	 */
	public Entity newResource(Entity container, Element element)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newResource(org.sakaiproject.entity.api.Entity,
	 *      org.sakaiproject.entity.api.Entity)
	 */
	public Entity newResource(Entity container, Entity other)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newResourceEdit(org.sakaiproject.entity.api.Entity,
	 *      java.lang.String, java.lang.Object[])
	 */
	public Edit newResourceEdit(Entity container, String id, Object[] others)
	{
		return new BaseJCRResourceEdit(baseContentService,id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newResourceEdit(org.sakaiproject.entity.api.Entity,
	 *      org.w3c.dom.Element)
	 */
	public Edit newResourceEdit(Entity container, Element element)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#newResourceEdit(org.sakaiproject.entity.api.Entity,
	 *      org.sakaiproject.entity.api.Entity)
	 */
	public Edit newResourceEdit(Entity container, Entity other)
	{
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.util.StorageUser#storageFields(org.sakaiproject.entity.api.Entity)
	 */
	public Object[] storageFields(Entity r)
	{
		throw new UnsupportedOperationException();
	}

}
