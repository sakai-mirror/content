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
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.impl.BaseContentService.BaseCollectionEdit;
import org.sakaiproject.content.impl.BaseContentService.BaseResourceEdit;
import org.sakaiproject.content.impl.jcr.DAVConstants;
import org.sakaiproject.content.impl.jcr.SakaiConstants;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.jcr.api.JcrConstants;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * @author ieb
 */
public class JCRStorageUser implements LiteStorageUser
{
	private static final Log log = LogFactory.getLog(JCRStorageUser.class);

	private static final String IGNORE_PROPERTY = "ignore";

	private static final String REPOSITORY_PREFIX = "/content";

	private BaseContentService baseContentService;

	private Map<String, String> jcrTypes;

	private Map<String, String> jcrToEntity;

	private Map<String, String> entityToJcr;

	private String repoPrefix;

	private String jcrWorkspace = "/sakai";

	private List<String> createNodes = new ArrayList<String>();

	private ConcurrentHashMap<String, PropertyDefinition> ntCache = new ConcurrentHashMap<String, PropertyDefinition>();

	public JCRStorageUser()
	{
	}

	public void init()
	{
		repoPrefix = jcrWorkspace + REPOSITORY_PREFIX;
	}

	public void destroy()
	{

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
				setJCRProperty(
						convertEntityName2JCRName(SakaiConstants.SAKAI_GROUP_LIST),
						new ArrayList(bedit.m_groups), n);
				if (bedit.m_access == null)
				{
					setJCRProperty(
							convertEntityName2JCRName(SakaiConstants.SAKAI_ACCESS_MODE),
							AccessMode.INHERITED.toString(), n);
				}
				else
				{
					setJCRProperty(
							convertEntityName2JCRName(SakaiConstants.SAKAI_ACCESS_MODE),
							bedit.m_access.toString(), n);
				}
				if (bedit.m_releaseDate != null)
				{
					setJCRProperty(
							convertEntityName2JCRName(SakaiConstants.SAKAI_RELEASE_DATE),
							new Date(bedit.m_releaseDate.getTime()), n);
				}
				if (bedit.m_retractDate != null)
				{
					setJCRProperty(
							convertEntityName2JCRName(SakaiConstants.SAKAI_RETRACT_DATE),
							new Date(bedit.m_retractDate.getTime()), n);
				}
				setJCRProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_HIDDEN),
						bedit.m_hidden, n);

			}
			else if (edit instanceof BaseResourceEdit)
			{
				BaseResourceEdit bedit = (BaseResourceEdit) edit;
				setJCRProperty(
						convertEntityName2JCRName(SakaiConstants.DAV_CONTENT_LENGTH),
						bedit.getContentLength(), n);
				setJCRProperty(
						convertEntityName2JCRName(SakaiConstants.DAV_CONTENT_TYPE), bedit
								.getContentType(), n);
				setJCRProperty(
						convertEntityName2JCRName(SakaiConstants.SAKAI_RESOURCE_TYPE),
						bedit.getResourceType(), n);;
				setJCRProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_FILE_PATH),
						bedit.m_filePath, n);;
				if (bedit.m_body != null)
				{

				}
				setJCRProperty(
						convertEntityName2JCRName(SakaiConstants.SAKAI_GROUP_LIST),
						new ArrayList(bedit.m_groups), n);
				if (bedit.m_access == null)
				{
					setJCRProperty(
							convertEntityName2JCRName(SakaiConstants.SAKAI_ACCESS_MODE),
							AccessMode.INHERITED.toString(), n);
				}
				else
				{
					setJCRProperty(
							convertEntityName2JCRName(SakaiConstants.SAKAI_ACCESS_MODE),
							bedit.m_access.toString(), n);
				}
				if (bedit.m_hidden)
				{
					clearJCRProperty(
							convertEntityName2JCRName(SakaiConstants.SAKAI_RELEASE_DATE),
							n);
					clearJCRProperty(
							convertEntityName2JCRName(SakaiConstants.SAKAI_RETRACT_DATE),
							n);
				}
				else
				{
					if (bedit.m_releaseDate != null)
					{
						setJCRProperty(
								convertEntityName2JCRName(SakaiConstants.SAKAI_RELEASE_DATE),
								new Date(bedit.m_releaseDate.getTime()), n);
					}
					if (bedit.m_retractDate != null)
					{
						setJCRProperty(
								convertEntityName2JCRName(SakaiConstants.SAKAI_RETRACT_DATE),
								new Date(bedit.m_retractDate.getTime()), n);
					}
				}
				setJCRProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_HIDDEN),
						bedit.m_hidden, n);
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

		try
		{
			if (n.hasProperty(jname))
			{
				Property p = n.getProperty(jname);
				if (p != null)
				{
					p.setValue((Value) null);
				}
			}
		}
		catch (RepositoryException re)
		{
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
			if (IGNORE_PROPERTY.equals(stype))
			{
				return;
			}
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
						log.error("Failed to set " + jname + " to " + list + " cause:"
								+ e.getMessage());
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
							SimpleDateFormat sdf = new SimpleDateFormat(
									SakaiConstants.SAKAI_DATE_FORMAT);
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
						log.error("Failed to set " + jname + " to " + list + " cause:"
								+ e.getMessage());
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
						log.error("Failed to set " + jname + " to " + list + " cause:"
								+ e.getMessage());
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
						log.error("Failed to set " + jname + " to " + list + " cause:"
								+ e.getMessage());
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
						log.error("Failed to set " + jname + " to " + list + " cause:"
								+ e.getMessage());
					}
					break;
				}
			}
		}
		catch (RepositoryException e)
		{
			log.error("Failed to set propert " + jname + " to " + list, e);
		}
	}

	/**
	 * @param jname
	 * @param string
	 * @param n
	 */
	private void setJCRProperty(String jname, Object ov, Node n)
	{
		try
		{
			if (isProtected(n, jname))
			{
				log.info(jname+" is protected ignoring ");
				return;
			}
			String stype = jcrTypes.get(jname);
			if (IGNORE_PROPERTY.equals(stype))
			{
				return;
			}
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
					try
					{
						if (ov instanceof Boolean)
						{
							n.setProperty(jname, ((Boolean) ov).booleanValue());
						}
						else
						{
							n.setProperty(jname, new Boolean(String.valueOf(ov)));
						}
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + ov + " cause:"
								+ e.getMessage());
					}
					break;
				}
				case PropertyType.DATE:
				{
					try
					{
						if (ov instanceof Calendar)
						{
							n.setProperty(jname, (Calendar) ov);
						}
						else if (ov instanceof Date)
						{
							GregorianCalendar gc = new GregorianCalendar();
							gc.setTime((Date) ov);
							n.setProperty(jname, gc);
						}
						else
						{
							GregorianCalendar gc = new GregorianCalendar();
							SimpleDateFormat sdf = new SimpleDateFormat(
									SakaiConstants.SAKAI_DATE_FORMAT);
							try
							{
								gc.setTime(sdf.parse(String.valueOf(ov)));
							}
							catch (ParseException e)
							{
								log.error("Failed to parse Date Value ", e);
							}

							n.setProperty(jname, gc);
						}
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + ov + " cause:"
								+ e.getMessage());
					}
					break;
				}
				case PropertyType.DOUBLE:
				{
					try
					{
						if (ov instanceof Double)
						{
							n.setProperty(jname, ((Double) ov).doubleValue());
						}
						else
						{
							n.setProperty(jname, Double.parseDouble(String.valueOf(ov)));
						}
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + ov + " cause:"
								+ e.getMessage());
					}
					break;
				}
				case PropertyType.LONG:
				{
					try
					{
						if (ov instanceof Long)
						{
							n.setProperty(jname, ((Long) ov).longValue());
						}
						else if (ov instanceof Integer)
						{
							n.setProperty(jname, ((Integer) ov).longValue());
						}
						else
						{
							n.setProperty(jname, Long.parseLong(String.valueOf(ov)));
						}
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + ov + " cause:"
								+ e.getMessage());
					}
					break;
				}
				case PropertyType.UNDEFINED:
				case PropertyType.STRING:
				case PropertyType.REFERENCE:
				case PropertyType.NAME:
				case PropertyType.PATH:
				{
					try
					{
						n.setProperty(jname, String.valueOf(ov));
					}
					catch (RepositoryException e)
					{
						log.error("Failed to set " + jname + " to " + ov + " cause: "
								+ e.getMessage());
					}
					break;
				}
			}
		}
		catch (RepositoryException e)
		{
			log.error("Failed to set propert " + jname + " to " + ov, e);
		}

	}

	/**
	 * @param n
	 * @param jname
	 * @return
	 * @throws RepositoryException 
	 */
	private boolean isProtected(Node n, String jname) throws RepositoryException
	{
		PropertyDefinition pd  = getDefinition(n, jname);
		if ( pd == null ) {
			return false;
		}
		return pd.isProtected();
	}

	/**
	 * @param n
	 * @param jname
	 * @throws RepositoryException 
	 */
	private PropertyDefinition getDefinition(Node n, String jname) throws RepositoryException
	{
		NodeType pnt = n.getPrimaryNodeType();
		String name = pnt.getName() + ":" + jname;
		PropertyDefinition opd = ntCache.get(name);
		if (opd == null)
		{
			{

				for (PropertyDefinition pd : pnt.getPropertyDefinitions())
				{
					ntCache.put(pnt.getName()+":"+pd.getName(), pd);
				}
			}
			for (NodeType nt : n.getMixinNodeTypes())
			{
				for (PropertyDefinition pd : nt.getPropertyDefinitions())
				{
					ntCache.put(pnt.getName()+":"+pd.getName(), pd);
				}
			}
			opd = ntCache.get(name);
		}
		return opd;
	}

	/**
	 * @param n
	 * @param e
	 * @throws RepositoryException
	 */
	private void copy(Node n, Entity e) throws RepositoryException
	{

		// copy from the node to the entity,
		// there may be some items in the Node properties that we do not want to
		// copy
		ResourceProperties rp = e.getProperties();
		for (PropertyIterator pi = n.getProperties(); pi.hasNext();)
		{
			Property p = pi.nextProperty();
			setEntityProperty(p, rp);
		}

		if (e instanceof BaseJCRCollectionEdit)
		{
			BaseJCRCollectionEdit bce = (BaseJCRCollectionEdit) e;
			bce.setNode(n);
			bce.m_groups = new ArrayList<String>();
			if (n.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_GROUP_LIST)))
			{
				Property p = n
						.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_GROUP_LIST));
				if (p != null)
				{
					Value[] v = p.getValues();
					if (v != null)
					{
						for (int i = 0; i < v.length; i++)
						{
							bce.m_groups.add(v[i].toString());
						}
					}
				}
			}
			bce.m_access = AccessMode.INHERITED;
			if (n
					.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_ACCESS_MODE)))
			{
				Property p = n
						.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_ACCESS_MODE));
				if (p != null)
				{
					bce.m_access = AccessMode.fromString(p.toString());
				}
			}
			bce.m_hidden = false;
			if (n.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_HIDDEN)))
			{
				Property p = n
						.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_HIDDEN));
				if (p != null)
				{
					bce.m_hidden = p.getBoolean();
				}
			}
			if (bce.m_hidden)
			{
				bce.m_releaseDate = null;
				bce.m_retractDate = null;
			}
			else
			{
				if (n
						.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RELEASE_DATE)))
				{
					Property p = n
							.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RELEASE_DATE));
					if (p != null)
					{
						bce.m_releaseDate = TimeService.newTime(p.getDate()
								.getTimeInMillis());
					}
				}
				if (n
						.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RETRACT_DATE)))
				{
					Property p = n
							.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RETRACT_DATE));
					if (p != null)
					{
						bce.m_retractDate = TimeService.newTime(p.getDate()
								.getTimeInMillis());
					}
				}
			}
		}
		else if (e instanceof BaseJCRResourceEdit)
		{
			BaseJCRResourceEdit bre = (BaseJCRResourceEdit) e;
			bre.setNode(n);
			if (n.hasProperty(DAVConstants.DAV_GETCONTENTTYPE))
			{
				Property p = n.getProperty(DAVConstants.DAV_GETCONTENTTYPE);
				if (p != null)
				{
					bre.m_contentType = p.getString();
				}
			}
			if (n.hasProperty(DAVConstants.DAV_GETCONTENTTYPE))
			{
				Property p = n.getProperty(DAVConstants.DAV_GETCONTENTTYPE);
				if (p != null)
				{
					bre.m_contentLength = (int) p.getLong();
				}
			}
			if (n
					.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RESOURCE_TYPE)))
			{
				Property p = n
						.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RESOURCE_TYPE));
				if (p != null)
				{
					bre.setResourceType(p.toString());
				}
			}
			if (n.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_FILE_PATH)))
			{
				Property p = n
						.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_FILE_PATH));
				if (p != null)
				{
					bre.m_filePath = StringUtil.trimToNull(p.toString());
				}

			}
			bre.m_groups = new ArrayList<String>();
			if (n.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_GROUP_LIST)))
			{
				Property p = n
						.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_GROUP_LIST));
				if (p != null)
				{
					Value[] v = p.getValues();
					if (v != null)
					{
						for (int i = 0; i < v.length; i++)
						{
							bre.m_groups.add(v[i].toString());
						}
					}
				}
			}
			bre.m_access = AccessMode.INHERITED;
			if (n
					.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_ACCESS_MODE)))
			{
				Property p = n
						.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_ACCESS_MODE));
				if (p != null)
				{
					bre.m_access = AccessMode.fromString(p.toString());
				}
			}
			bre.m_hidden = false;
			if (n.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_HIDDEN)))
			{
				Property p = n
						.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_HIDDEN));
				if (p != null)
				{
					bre.m_hidden = p.getBoolean();
				}
			}
			if (bre.m_hidden)
			{
				bre.m_releaseDate = null;
				bre.m_retractDate = null;
			}
			else
			{
				if (n
						.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RELEASE_DATE)))
				{
					Property p = n
							.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RELEASE_DATE));
					if (p != null)
					{
						bre.m_releaseDate = TimeService.newTime(p.getDate()
								.getTimeInMillis());
					}
				}
				if (n
						.hasProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RETRACT_DATE)))
				{
					Property p = n
							.getProperty(convertEntityName2JCRName(SakaiConstants.SAKAI_RETRACT_DATE));
					if (p != null)
					{
						bre.m_retractDate = TimeService.newTime(p.getDate()
								.getTimeInMillis());
					}
				}
			}

		}

	}

	/**
	 * @param p
	 * @param rp
	 * @throws RepositoryException
	 * @throws
	 */
	private void setEntityProperty(Property p, ResourceProperties rp)
			throws RepositoryException
	{
		if (IGNORE_PROPERTY.equals(jcrTypes.get(p.getName())))
		{
			return;
		}
		log.info("Converting " + p.getName());

		PropertyDefinition pd = p.getDefinition();
		if (pd.isMultiple())
		{
			String ename = convertJCRName2EntityName(p.getName());
			for (Value v : p.getValues())
			{
				rp.addPropertyToList(ename, v.toString());
			}
		}
		else
		{
			rp.addProperty(convertJCRName2EntityName(p.getName()), p.getString());
		}
	}

	/**
	 * @param name
	 * @return
	 */
	private String convertJCRName2EntityName(String name)
	{
		String entityName = jcrToEntity.get(name);
		if (entityName == null)
		{
			return name;
		}
		return entityName;
	}

	/**
	 * @param name
	 * @return
	 */
	private String convertEntityName2JCRName(String name)
	{
		String jcrName = entityToJcr.get(name);
		if (jcrName == null)
		{
			return name;
		}
		return jcrName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#convertId2Storage(java.lang.String)
	 */
	public String convertId2Storage(String id)
	{
		String jcrPath = repoPrefix + id;
		if (jcrPath.endsWith("/"))
		{
			jcrPath = jcrPath.substring(0, jcrPath.length() - 1);
		}
		log.info(" Id2JCR [" + id + "] >> [" + jcrPath + "]");
		return jcrPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#convertStorage2Id(java.lang.String)
	 */
	public String convertStorage2Id(String path)
	{
		String id = path;
		if (path.startsWith(repoPrefix))
		{
			id = path.substring(repoPrefix.length());
		}
		else
		{
			log
					.error("Trying to convert a path to Id that is not a storage path "
							+ path);
		}
		log.info(" JCR2Id [" + path + "] >> [" + id + "]");
		return id;
	}

	/**
	 * @param path
	 * @return
	 */
	private String xconvertStorage2Ref(String path)
	{
		String id = convertStorage2Id(path);
		return baseContentService.getReference(id);
	}

	/**
	 * @param ref
	 * @return
	 */
	private String convertRef2Id(String ref)
	{
		String baseRef = baseContentService.getReference("/");
		if (baseRef.endsWith("/"))
		{
			baseRef = baseRef.substring(0, baseRef.length() - 1);
		}
		log.info("Base Reference is " + baseRef);
		String id = ref;
		if (ref.startsWith(baseRef))
		{
			id = ref.substring(baseRef.length());
			log.error("Ref2Id ref[" + ref + "] >> id[" + id + "]");
		}
		else
		{
			log.error("Reference does not appear to be a CHS reference [" + ref
					+ "] should start with [" + baseRef + "]");
		}

		return id;
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
				if (JcrConstants.NT_FILE.equals(nt.getName()))
				{
					Entity e = newResource(null, convertStorage2Id(n.getPath()), null);
					copy(n, e);
					return e;
				}
				else if (JcrConstants.NT_FOLDER.equals(nt.getName()))
				{
					Entity e = newContainerById(convertStorage2Id(n.getPath()));
					copy(n, e);
					return e;
				}
				else
				{
					log.error("Unable to determine node type " + nt.getName());
					return null;
				}
			}
			catch (RepositoryException e1)
			{
				log.error("Failed to create new resource", e1);
			}
		}
		log.error("Cant Create Resource from source " + source);
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
				log.info("Building resource from " + nt.getName());
				if (JcrConstants.NT_FILE.equals(nt.getName()))
				{
					Edit e = newResourceEdit(null, convertStorage2Id(n.getPath()), null);
					copy(n, e);
					return e;
				}
				else if (JcrConstants.NT_FOLDER.equals(nt.getName()))
				{
					Edit e = newContainerEditById(convertStorage2Id(n.getPath()));
					copy(n, e);
					return e;
				}
				else
				{
					log.error("Cant create Resource Edit from a " + nt.getName());
					return null;
				}
			}
			catch (RepositoryException e1)
			{
				log.error("Failed to create new resource", e1);
			}
		}
		log.error("Unable to create JCR based resource from a source object " + source);
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
	public Entity newContainer(String ref)
	{
		String id = convertRef2Id(ref);
		return new BaseJCRCollectionEdit(baseContentService, id);
	}

	public Entity newContainerById(String id)
	{
		return new BaseJCRCollectionEdit(baseContentService, id);
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
	public Edit newContainerEdit(String ref)
	{
		String id = convertRef2Id(ref);
		return new BaseJCRCollectionEdit(baseContentService, id);
	}

	public Edit newContainerEditById(String id)
	{
		return new BaseJCRCollectionEdit(baseContentService, id);
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
		return new BaseJCRResourceEdit(baseContentService, id);
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
		return new BaseJCRResourceEdit(baseContentService, id);
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

	/**
	 * @return the baseContentService
	 */
	public BaseContentService getBaseContentService()
	{
		return baseContentService;
	}

	/**
	 * @param baseContentService
	 *        the baseContentService to set
	 */
	public void setBaseContentService(BaseContentService baseContentService)
	{
		this.baseContentService = baseContentService;
	}

	/**
	 * @return the entityToJcr
	 */
	public Map<String, String> getEntityToJcr()
	{
		return entityToJcr;
	}

	/**
	 * @param entityToJcr
	 *        the entityToJcr to set
	 */
	public void setEntityToJcr(Map<String, String> entityToJcr)
	{
		this.entityToJcr = entityToJcr;
	}

	/**
	 * @return the jcrToEntity
	 */
	public Map<String, String> getJcrToEntity()
	{
		return jcrToEntity;
	}

	/**
	 * @param jcrToEntity
	 *        the jcrToEntity to set
	 */
	public void setJcrToEntity(Map<String, String> jcrToEntity)
	{
		this.jcrToEntity = jcrToEntity;
	}

	/**
	 * @return the jcrTypes
	 */
	public Map<String, String> getJcrTypes()
	{
		return jcrTypes;
	}

	/**
	 * @param jcrTypes
	 *        the jcrTypes to set
	 */
	public void setJcrTypes(Map<String, String> jcrTypes)
	{
		this.jcrTypes = jcrTypes;
	}

	/**
	 * @return the jcrWorkspace
	 */
	public String getJcrWorkspace()
	{
		return jcrWorkspace;
	}

	/**
	 * @param jcrWorkspace
	 *        the jcrWorkspace to set
	 */
	public void setJcrWorkspace(String jcrWorkspace)
	{
		this.jcrWorkspace = jcrWorkspace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.LiteStorageUser#startupNodes()
	 */
	public Iterator<String> startupNodes()
	{
		return createNodes.iterator();
	}

	/**
	 * @return the createNodes
	 */
	public List<String> getCreateNodes()
	{
		return createNodes;
	}

	/**
	 * @param createNodes
	 *        the createNodes to set
	 */
	public void setCreateNodes(List<String> createNodes)
	{
		this.createNodes = createNodes;
	}

}
