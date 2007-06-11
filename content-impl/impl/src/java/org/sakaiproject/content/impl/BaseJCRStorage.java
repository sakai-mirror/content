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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.jcr.api.JCRService;

/**
 * <p>
 * BaseDbSingleStorage is a class that stores Resources (of some type) in a
 * database, <br />
 * provides locked access, and generally implements a services "storage" class.
 * The <br />
 * service's storage class can extend this to provide covers to turn Resource
 * and <br />
 * Edit into something more type specific to the service.
 * </p>
 * <p>
 * Note: the methods here are all "id" based, with the following assumptions:
 * <br /> - just the Resource Id field is enough to distinguish one Resource
 * from another <br /> - a resource's reference is based on no more than the
 * resource id <br /> - a resource's id cannot change.
 * </p>
 * <p>
 * In order to handle Unicode characters properly, the SQL statements executed
 * by this class <br />
 * should not embed Unicode characters into the SQL statement text; rather,
 * Unicode values <br />
 * should be inserted as fields in a PreparedStatement. Databases handle Unicode
 * better in fields.
 * </p>
 */
public class BaseJCRStorage
{
	private static final String NT_FOLDER = null;

	private static final String NT_ROOT = null;

	private static final String NT_FILE = null;

	private static final String JCR_CONTENT = null;

	private static final String NT_RESOURCE = null;

	private static final String MIX_REFERENCEABLE = null;

	private static final String JCR_LASTMODIFIED = null;

	private static final String JCR_MIMETYPE = null;

	private static final String JCR_DATA = null;

	private static final String JCR_ENCODING = null;

	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BaseJCRStorage.class);

	/** The xml tag name for the element holding each actual resource entry. */
	protected String m_resourceEntryTagName = null;

	/** The StorageUser to callback for new Resource and Edit objects. */
	protected LiteStorageUser m_user = null;

	/**
	 * Locks, keyed by reference, holding Connections (or, if locks are done
	 * locally, holding an Edit).
	 */
	protected Hashtable m_locks = null;

	/** If set, we treat reasource ids as case insensitive. */
	protected boolean m_caseInsensitive = false;

	private JCRService jcrService;

	private String nodeType;

	private Map<String, String> queryTerms;

	/**
	 * @param jcrService
	 * @param collectionUser
	 * @param string
	 */
	public BaseJCRStorage(JCRService jcrService, LiteStorageUser storageUser,
			String nodeType)
	{
		this.jcrService = jcrService;
		this.m_user = storageUser;
		this.nodeType = nodeType;
		
		queryTerms = new HashMap<String, String>();
		queryTerms.put("WHERE:IN_COLLECTION", "/{0}/{1}/*/element(*,nodeType)" );
		queryTerms.put("WHERELIKE:IN_COLLECTION", "/{0}/{1}/*/element(*,nodeType)");
	}

	/**
	 * Open and be ready to read / write.
	 */
	public void open()
	{
	}

	/**
	 * Close.
	 */
	public void close()
	{
	}

	/**
	 * Read one Resource from xml
	 * 
	 * @param xml
	 *        An string containing the xml which describes the resource.
	 * @return The Resource object created from the xml.
	 */
	protected Entity readResource(Node n)
	{

		try
		{

			return m_user.newResource(n);
		}
		catch (Exception e)
		{
			M_log.debug("readResource(): ", e);
			return null;
		}
	}

	/**
	 * Check if a Resource by this id exists.
	 * 
	 * @param id
	 *        The id.
	 * @return true if a Resource by this id exists, false if not.
	 */
	public boolean checkResource(String id)
	{
		Node n = getNodeById(id);
		if (n != null)
		{
			try
			{
				NodeType nt = n.getPrimaryNodeType();
				if (nodeType.equals(nt.getName()))
				{
					return true;
				}
			}
			catch (RepositoryException e)
			{
				M_log.error("Failed ", e);
			}
		}
		return false;
	}

	/**
	 * @param id
	 * @return
	 */
	private Node getNodeById(String id)
	{
		try
		{
			Session session = jcrService.getSession();
			Item i = session.getItem(m_user.convertId2Storage(id));
			if (i != null && i.isNode())
			{
				return (Node) i;
			}
		}
		catch (RepositoryException re)
		{
			M_log.debug("Node Not Found " + id, re);
		}
		return null;
	}

	/**
	 * Get the Resource with this id, or null if not found.
	 * 
	 * @param id
	 *        The id.
	 * @return The Resource with this id, or null if not found.
	 */
	public Entity getResource(String id)
	{

		return readResource(getNodeById(id));

	}

	public boolean isEmpty()
	{
		Node n = getNodeById("/");
		try
		{
			return n.hasNodes();
		}
		catch (RepositoryException e)
		{
			return true;
		}
	}

	public List getAllResources()
	{

		throw new UnsupportedOperationException("Not Available");
	}

	public List getAllResources(int first, int last)
	{
		throw new UnsupportedOperationException("Not Available");
	}

	public int countAllResources()
	{
		throw new UnsupportedOperationException("Not Available");
	}

	public int countSelectedResourcesWhere(String sqlWhere)
	{
		throw new UnsupportedOperationException("Not Available");
	}

	/**
	 * Get all Resources where the given field matches the given value.
	 * 
	 * @param field
	 *        The db field name for the selection.
	 * @param value
	 *        The value to select.
	 * @return The list of all Resources that meet the criteria.
	 */
	public List getAllResourcesWhere(String field, String value)
	{
		return getNodeList("WHERE:"+field,new Object[] {value});
	}

	public List getAllResourcesWhereLike(String field, String value)
	{
		return getNodeList("WHERELIKE:"+field,new Object[] {value});
	}

	/**
	 * @param string
	 * @param value
	 * @return
	 */
	private List getNodeList(String key, Object[] value)
	{
		try
		{
			String queryFormat = queryTerms.get(key);
			if ( queryFormat == null ) {
				throw new UnsupportedOperationException("No List Option for "+key);
			}
			String qs = MessageFormat.format(queryFormat, value);
			Session session = jcrService.getSession();
			Workspace w = session.getWorkspace();
			QueryManager qm = w.getQueryManager();
			
			Query q = qm.createQuery(qs, Query.XPATH);
			QueryResult qr = q.execute();
			NodeIterator ni = qr.getNodes();
			List<Entity> al = new ArrayList<Entity>();
			for (; ni.hasNext();)
			{
				Node n = ni.nextNode();
				al.add(m_user.newResource(n));
			}
			// because we need size, we have to do this, It would be MUCH
			// better if we could return the Iterator
			return al;
		}
		catch (RepositoryException e)
		{
			M_log.error("Failed to get List ",e);
			return new ArrayList();
		}
	}

	/**
	 * Get selected Resources, filtered by a test on the id field
	 * 
	 * @param filter
	 *        A filter to select what gets returned.
	 * @return The list of selected Resources.
	 */
	public List getSelectedResources(final Filter filter)
	{
		throw new UnsupportedOperationException("Not Available");
	}

	/**
	 * Get selected Resources, using a supplied where clause
	 * 
	 * @param sqlWhere
	 *        The SQL where clause.
	 * @return The list of selected Resources.
	 */
	public List getSelectedResourcesWhere(String sqlWhere)
	{
		throw new UnsupportedOperationException("Not Available");
	}

	/**
	 * Add a new Resource with this id.
	 * 
	 * @param id
	 *        The id.
	 * @param others
	 *        Other fields for the newResource call
	 * @return The locked Resource object with this id, or null if the id is in
	 *         use.
	 */
	public Edit putResource(String id, Object[] others)
	{
		try
		{
			Node n;
			n = createNode(id, this.nodeType);
			if (n == null)
			{
				return null;
			}
			return editResource(id);
		}
		catch (TypeException e)
		{
			M_log.error("Incorrect Node Type", e);
		}
		return null;
	}

	/**
	 * store the record in content_resource_delete table along with
	 * resource_uuid and date
	 */
	public Edit putDeleteResource(String id, String uuid, String userId, Object[] others)
	{
		throw new UnsupportedOperationException(
				"No Possible since the deleted UUID cannot be shared ");
	}

	/** update XML attribute on properties and remove locks */
	public void commitDeleteResource(Edit edit, String uuid)
	{
		throw new UnsupportedOperationException(
				"No Possible since the deleted UUID cannot be shared ");
	}

	/**
	 * Get a lock on the Resource with this id, or null if a lock cannot be
	 * gotten.
	 * 
	 * @param id
	 *        The user id.
	 * @return The locked Resource with this id, or null if this records cannot
	 *         be locked.
	 */
	public Edit editResource(String id)
	{
		Node n = getNodeById(id);
		try
		{
			if (n.lock(true, true) == null)
			{
				return null;
			}
		}
		catch (UnsupportedRepositoryOperationException e)
		{
			M_log.warn("Operation Not Supported ", e);
		}
		catch (RepositoryException e)
		{
			M_log.warn("Lock Failed ", e);
			return null;
		}
		return m_user.newResourceEdit(n);
	}

	/**
	 * Commit the changes and release the lock.
	 * 
	 * @param user
	 *        The Edit to commit.
	 */
	public void commitResource(Edit edit)
	{
		Node n = getNodeById(edit.getId());
		m_user.commit(edit, n);
		try
		{
			n.save();
			n.unlock();
		}
		catch (RepositoryException e)
		{
			M_log.error("Failed to save resource ", e);
		}
	}

	/**
	 * Cancel the changes and release the lock.
	 * 
	 * @param user
	 *        The Edit to cancel.
	 */
	public void cancelResource(Edit edit)
	{
		Node n = getNodeById(edit.getId());
		try
		{
			n.refresh(false);
		}
		catch (RepositoryException e)
		{
			M_log.warn("Failed to cancel Edit ", e);
		}
		try
		{
			n.unlock();
		}
		catch (RepositoryException e)
		{
			M_log.warn("Failed to un Lock ", e);
		}
	}

	/**
	 * Remove this (locked) Resource.
	 * 
	 * @param user
	 *        The Edit to remove.
	 */
	public void removeResource(Edit edit)
	{
		Node n = getNodeById(edit.getId());
		try
		{
			n.remove();
		}
		catch (RepositoryException e)
		{
			M_log.warn("Failed to cancel Edit ", e);
		}
		try
		{
			n.unlock();
		}
		catch (RepositoryException e)
		{
			M_log.warn("Failed to un Lock ", e);
		}
	}

	/**
	 * Create a new node. Nodes are of the form
	 * nt:folder/nt:folder/nt:folder/nt:file nt:folders have properties nt:files
	 * have properties nt:files have a nt:resource subnode
	 * 
	 * @param id
	 * @param collection
	 * @return
	 * @throws TypeException
	 */
	private Node createNode(String id, String type) throws TypeException
	{
		Node node = null;
		try
		{
			M_log.info("Creating Node " + id);
			String absPath = m_user.convertId2Storage(id);
			Session s = jcrService.getSession();
			Node n = getNodeFromSession(s, absPath);
			String vpath = getParentPath(absPath);
			while (n == null && "/".equals(vpath))
			{
				n = getNodeFromSession(s, vpath);
				if (n == null)
				{
					vpath = getParentPath(vpath);
				}
			}
			if (n == null)
			{
				n = s.getRootNode();
			}
			String relPath = absPath.substring(vpath.length());
			Node rootNode = s.getRootNode();
			if (relPath.startsWith("/"))
			{
				relPath = relPath.substring(1);
			}

			String[] pathElements = relPath.split("/");
			Node currentNode = n;
			for (int i = 0; i < pathElements.length; i++)
			{
				try
				{
					currentNode = currentNode.getNode(pathElements[i]);
					if (!currentNode.isNodeType(NT_FOLDER)
							&& !currentNode.isNodeType(NT_ROOT))
					{
						throw new TypeException(
								"Cant create collection or a folder inside a node that is not a folder "
										+ currentNode.getPath());
					}

				}
				catch (PathNotFoundException pnfe)
				{
					if (i < pathElements.length - 1 || NT_FOLDER.equals(type))
					{
						currentNode = currentNode.addNode(pathElements[i], NT_FOLDER);
						populateFolder(currentNode);
					}
					else
					{
						currentNode = currentNode.addNode(pathElements[i], NT_FILE);
						populateFile(currentNode);

					}
				}
			}
			node = currentNode;
		}
		catch (RepositoryException rex)
		{
			M_log.warn("Unspecified Repository Failiure ", rex);
			M_log.error("Unspecified Repository Failiure " + rex.getMessage());
		}
		return node;

	}

	/**
	 * @param absPath
	 * @return
	 */
	private String getParentPath(String absPath)
	{
		int pre = absPath.lastIndexOf("/");
		if ( pre > 0 ) {
			return absPath.substring(0,pre-1);
		}
		return "/";
	}

	/**
	 * @param s
	 * @param id
	 * @return
	 * @throws TypeException
	 * @throws RepositoryException
	 */
	private Node getNodeFromSession(Session s, String id) throws RepositoryException,
			TypeException
	{
		Item i = s.getItem(id);
		Node n = null;
		if (i != null)
		{
			if (i.isNode())
			{
				n = (Node) i;
			}
			else
			{
				throw new TypeException("Path does not point to a node");
			}
		}
		return n;
	}

	private void populateFile(Node node) throws RepositoryException
	{
		// JCR Types
		node.addMixin(MIX_REFERENCEABLE);
		// node.setProperty("jcr:created", new GregorianCalendar());
		Node resource = node.addNode(JCR_CONTENT, NT_RESOURCE);
		resource.setProperty(JCR_LASTMODIFIED, new GregorianCalendar());
		resource.setProperty(JCR_MIMETYPE, "application/octet-stream");
		resource.setProperty(JCR_DATA, "");
		resource.setProperty(JCR_ENCODING, "UTF-8");
	}

	private void populateFolder(Node node)
	{
		// JCR Types
		// TODO: perhpase
		M_log.debug("Doing populate Folder");

	}

}
