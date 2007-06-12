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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
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
	protected static final String JCR_RESOURCE = null;

	protected static final String JCR_COLLECTION = null;

	/** Our logger. */
	private static final Log log = LogFactory.getLog(JCRContentService.class);

	private JCRService jcrService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.DbContentService#init()
	 */
	@Override
	public void init()
	{
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
		return new JCRStorage(new JCRStorageUser(this), new JCRStorageUser(this),
				(m_bodyPath != null), contentHostingHandlerResolver);
	}

	protected class JCRStorage implements Storage
	{
		/** A storage for collections. */
		protected BaseJCRStorage m_collectionStore = null;

		/** A storage for resources. */
		protected BaseJCRStorage m_resourceStore = null;

		/** htripath- Storage for resources delete */
		protected BaseJCRStorage m_resourceDeleteStore = null;

		protected BaseContentHostingHandlerResolver resolver = null;

		private ThreadLocal stackMarker = new ThreadLocal();

		/**
		 * Construct.
		 * 
		 * @param collectionUser
		 *        The StorageUser class to call back for creation of collection
		 *        objects.
		 * @param resourceUser
		 *        The StorageUser class to call back for creation of resource
		 *        objects.
		 */
		public JCRStorage(LiteStorageUser collectionUser, LiteStorageUser resourceUser,
				boolean bodyInFile, BaseContentHostingHandlerResolver resolver)
		{
			this.resolver = resolver;
			this.resolver.setResourceUser(resourceUser);
			this.resolver.setCollectionUser(collectionUser);

			// build the collection store - a single level store
			m_collectionStore = new BaseJCRStorage(jcrService, collectionUser,
					"nt:collection");

			// build the resources store - a single level store
			m_resourceStore = new BaseJCRStorage(jcrService, collectionUser, "nt:file");

			// htripath-build the resource for store of deleted
			// record-single
			// level store
			m_resourceDeleteStore = new BaseJCRStorage(jcrService, collectionUser,
					"nt:file");

		} // DbStorage

		/**
		 * Open and be ready to read / write.
		 */
		public void open()
		{
			m_collectionStore.open();
			m_resourceStore.open();
			m_resourceDeleteStore.open();
		} // open

		/**
		 * Close.
		 */
		public void close()
		{
			m_collectionStore.close();
			m_resourceStore.close();
			m_resourceDeleteStore.close();
		} // close

		private class StackRef
		{
			protected int count = 0;
		}

		/**
		 * increase the stack counter and return true if this is the top of the
		 * stack
		 * 
		 * @return
		 */
		private boolean in()
		{
			StackRef r = (StackRef) stackMarker.get();
			if (r == null)
			{
				r = new StackRef();
				stackMarker.set(r);
			}
			r.count++;
			return r.count <= 1;// johnf@caret -- used to permit no
			// self-recurses; now permits 0 or 2
			// (r.count == 1);
		}

		/**
		 * decrement the stack counter on the thread
		 */
		private void out()
		{
			StackRef r = (StackRef) stackMarker.get();
			if (r == null)
			{
				r = new StackRef();
				stackMarker.set(r);
			}
			r.count--;
			if (r.count < 0)
			{
				r.count = 0;
			}
		}

		/** Collections * */

		public boolean checkCollection(String id)
		{
			if (id == null || id.trim().length() == 0)
			{
				return false;
			}
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return resolver.checkCollection(this, id);
				}
				else
				{
					return m_collectionStore.checkResource(id);
				}
			}
			finally
			{
				out();
			}
		}

		public ContentCollection getCollection(String id)
		{
			if (id == null || id.trim().length() == 0)
			{
				return null;
			}
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return resolver.getCollection(this, id);
				}
				else
				{
					return (ContentCollection) m_collectionStore.getResource(id);
				}
			}
			finally
			{
				out();
			}

		}

		/**
		 * Get a list of all getCollections within a collection.
		 */
		public List getCollections(ContentCollection collection)
		{
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return resolver.getCollections(this, collection);
				}
				else
				{
					// limit to those whose reference path (based on id)
					// matches
					// the
					// collection id
					final String target = collection.getId();

					/*
					 * // read all the records, then filter them to accept only
					 * those in this collection // Note: this is not desirable,
					 * as the read is linear to the database site -ggolden List
					 * rv = m_collectionStore.getSelectedResources( new Filter() {
					 * public boolean accept(Object o) { // o is a String, the
					 * collection id return StringUtil.referencePath((String)
					 * o).equals(target); } } );
					 */

					// read the records with a where clause to let the
					// database
					// select
					// those in this collection
					return m_collectionStore
							.getAllResourcesWhere("IN_COLLECTION", target);
				}
			}
			finally
			{
				out();
			}

		} // getCollections

		public ContentCollectionEdit putCollection(String id)
		{
			if (id == null || id.trim().length() == 0)
			{
				return null;
			}
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return (ContentCollectionEdit) resolver.putCollection(this, id);
				}
				else
				{
					return (ContentCollectionEdit) m_collectionStore
							.putResource(id, null);
				}
			}
			finally
			{
				out();
			}
		}

		public ContentCollectionEdit editCollection(String id)
		{
			if (id == null || id.trim().length() == 0)
			{
				return null;
			}
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return (ContentCollectionEdit) resolver.editCollection(this, id);
				}
				else
				{
					return (ContentCollectionEdit) m_collectionStore.editResource(id);
				}
			}
			finally
			{
				out();
			}
		}

		// protected String
		// externalResourceDeleteFileName(ContentResource resource)
		// {
		// return m_bodyPath + "/delete/" + ((BaseResourceEdit)
		// resource).m_filePath;
		// }

		// htripath -end

		public void cancelResource(ContentResourceEdit edit)
		{
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					resolver.cancelResource(this, edit);
				}
				else
				{
					// clear the memory image of the body
					byte[] body = ((BaseResourceEdit) edit).m_body;
					((BaseResourceEdit) edit).m_body = null;
					m_resourceStore.cancelResource(edit);

				}
			}
			finally
			{
				out();
			}
		}

		public void commitCollection(ContentCollectionEdit edit)
		{
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					resolver.commitCollection(this, edit);
				}
				else
				{
					m_collectionStore.commitResource(edit);
				}
			}
			finally
			{
				out();
			}
		}

		public void cancelCollection(ContentCollectionEdit edit)
		{
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					resolver.cancelCollection(this, edit);
				}
				else
				{
					m_collectionStore.cancelResource(edit);
				}
			}
			finally
			{
				out();
			}

		}

		public void removeCollection(ContentCollectionEdit edit)
		{
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					resolver.removeCollection(this, edit);
				}
				else
				{
					m_collectionStore.removeResource(edit);
				}
			}
			finally
			{
				out();
			}
		}

		/** Resources * */

		public boolean checkResource(String id)
		{
			if (id == null || id.trim().length() == 0)
			{
				return false;
			}
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return resolver.checkResource(this, id);
				}
				else
				{
					return m_resourceStore.checkResource(id);
				}
			}
			finally
			{
				out();
			}
		}

		public ContentResource getResource(String id)
		{
			if (id == null || id.trim().length() == 0)
			{
				return null;
			}
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return (ContentResource) resolver.getResource(this, id);
				}
				else
				{
					return (ContentResource) m_resourceStore.getResource(id);
				}
			}
			finally
			{
				out();
			}
		}

		public List getResources(ContentCollection collection)
		{
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return resolver.getResources(this, collection);
				}
				else
				{
					// limit to those whose reference path (based on id)
					// matches
					// the
					// collection id
					final String target = collection.getId();

					/*
					 * // read all the records, then filter them to accept only
					 * those in this collection // Note: this is not desirable,
					 * as the read is linear to the database site -ggolden List
					 * rv = m_resourceStore.getSelectedResources( new Filter() {
					 * public boolean accept(Object o) { // o is a String, the
					 * resource id return StringUtil.referencePath((String)
					 * o).equals(target); } } );
					 */

					// read the records with a where clause to let the
					// database
					// select
					// those in this collection
					return m_resourceStore.getAllResourcesWhere("IN_COLLECTION", target);
				}
			}
			finally
			{
				out();
			}

		} // getResources

		public List getFlatResources(String collectionId)
		{
			List rv = null;
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					rv = resolver.getFlatResources(this, collectionId);
				}
				else
				{
					rv = m_resourceStore.getAllResourcesWhereLike("IN_COLLECTION",
							collectionId + "%");
				}
				return rv;
			}
			finally
			{
				out();
			}
		}

		public ContentResourceEdit putResource(String id)
		{
			if (id == null || id.trim().length() == 0)
			{
				return null;
			}
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return (ContentResourceEdit) resolver.putResource(this, id);
				}
				else
				{
					return (ContentResourceEdit) m_resourceStore.putResource(id, null);
				}
			}
			finally
			{
				out();
			}
		}

		public ContentResourceEdit editResource(String id)
		{
			if (id == null || id.trim().length() == 0)
			{
				return null;
			}
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return (ContentResourceEdit) resolver.editResource(this, id);
				}
				else
				{
					return (ContentResourceEdit) m_resourceStore.editResource(id);
				}
			}
			finally
			{
				out();
			}
		}

		public void commitResource(ContentResourceEdit edit)
				throws ServerOverloadException
		{
			// keep the body out of the XML

			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					resolver.commitResource(this, edit);
				}
				else
				{
					BaseResourceEdit redit = (BaseResourceEdit) edit;
					if (redit.m_body == null)
					{
						if (redit.m_contentStream == null)
						{
							// no body and no stream -- may result from
							// edit in which body is not accessed or
							// modified
							log
									.info("ContentResource committed with no change to contents (i.e. no body and no stream for content): "
											+ edit.getReference());
						}
						else
						{
							// if we have been configured to use an
							// external file system
							if (m_bodyPath != null)
							{
								boolean ok = putResourceBodyFilesystem(edit,
										redit.m_contentStream);
								if (!ok)
								{
									cancelResource(edit);
									throw new ServerOverloadException(
											"failed to write file");
								}
							}

							// otherwise use the database
							else
							{
								putResourceBodyDb(edit, redit.m_contentStream);
							}
						}
					}
					else
					{
						byte[] body = ((BaseResourceEdit) edit).m_body;
						((BaseResourceEdit) edit).m_body = null;

						// update the resource body
						if (body != null)
						{
							// if we have been configured to use an
							// external file
							// system
							if (m_bodyPath != null)
							{
								boolean ok = putResourceBodyFilesystem(edit, body);
								if (!ok)
								{
									cancelResource(edit);
									throw new ServerOverloadException(
											"failed to write file");
								}
							}

							// otherwise use the database
							else
							{
								putResourceBodyDb(edit, body);
							}
						}
					}
					m_resourceStore.commitResource(edit);
				}

			}
			finally
			{
				out();
			}
		}

		// htripath - start
		/**
		 * Add resource to content_resouce_delete table for user deleted
		 * resources
		 */
		public ContentResourceEdit putDeleteResource(String id, String uuid, String userId)
		{
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return (ContentResourceEdit) resolver.putDeleteResource(this, id,
							uuid, userId);
				}
				else
				{
					return (ContentResourceEdit) m_resourceDeleteStore.putDeleteResource(
							id, uuid, userId, null);
				}
			}
			finally
			{
				out();
			}
		}

		/**
		 * update xml and store the body of file TODO storing of body content is
		 * not used now.
		 */
		public void commitDeleteResource(ContentResourceEdit edit, String uuid)
		{
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					resolver.commitDeleteResource(this, edit, uuid);
				}
				else
				{
					byte[] body = ((BaseResourceEdit) edit).m_body;
					((BaseResourceEdit) edit).m_body = null;

					// update properties in xml and delete locks
					m_resourceDeleteStore.commitDeleteResource(edit, uuid);
				}
			}
			finally
			{
				out();
			}

		}

		public void removeResource(ContentResourceEdit edit)
		{
			// delete the body
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					resolver.removeResource(this, edit);
				}
				else
				{

					// if we have been configured to use an external
					// file system
					if (m_bodyPath != null)
					{
						delResourceBodyFilesystem(edit);
					}

					// otherwise use the database
					else
					{
						delResourceBodyDb(edit);
					}

					// clear the memory image of the body
					byte[] body = ((BaseResourceEdit) edit).m_body;
					((BaseResourceEdit) edit).m_body = null;

					m_resourceStore.removeResource(edit);

				}
			}
			finally
			{
				out();
			}

		}

		/**
		 * Read the resource's body.
		 * 
		 * @param resource
		 *        The resource whose body is desired.
		 * @return The resources's body content as a byte array.
		 * @exception ServerOverloadException
		 *            if the server is configured to save the resource body in
		 *            the filesystem and an error occurs while accessing the
		 *            server's filesystem.
		 */
		public byte[] getResourceBody(ContentResource resource)
				throws ServerOverloadException
		{
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return resolver.getResourceBody(this, resource);
				}
				else
				{
					if (((BaseResourceEdit) resource).m_contentLength <= 0)
					{
						log.warn("getResourceBody(): non-positive content length: "
								+ ((BaseResourceEdit) resource).m_contentLength
								+ "  id: " + resource.getId());
						return null;
					}

					// if we have been configured to use an external
					// file system
					if (m_bodyPath != null)
					{
						return getResourceBodyFilesystem(resource);
					}

					// otherwise use the database
					else
					{
						return getResourceBodyDb(resource);
					}
				}
			}
			finally
			{
				out();
			}

		}

		/**
		 * Read the resource's body from the database.
		 * 
		 * @param resource
		 *        The resource whose body is desired.
		 * @return The resources's body content as a byte array.
		 */
		protected byte[] getResourceBodyDb(ContentResource resource)
		{
			// get the resource from the db
			String sql = "select BODY from " + m_resourceBodyTableName
					+ " where ( RESOURCE_ID = ? )";

			Object[] fields = new Object[1];
			fields[0] = resource.getId();

			// create the body to read into
			byte[] body = new byte[((BaseResourceEdit) resource).m_contentLength];
			m_sqlService.dbReadBinary(sql, fields, body);

			return body;

		}

		/**
		 * Read the resource's body from the external file system.
		 * 
		 * @param resource
		 *        The resource whose body is desired.
		 * @return The resources's body content as a byte array.
		 * @exception ServerOverloadException
		 *            if server is configured to store resource body in
		 *            filesystem and error occurs trying to read from
		 *            filesystem.
		 */
		protected byte[] getResourceBodyFilesystem(ContentResource resource)
				throws ServerOverloadException
		{
			// form the file name
			File file = new File(externalResourceFileName(resource));

			// read the new
			try
			{
				byte[] body = new byte[((BaseResourceEdit) resource).m_contentLength];
				FileInputStream in = new FileInputStream(file);

				in.read(body);
				in.close();

				return body;
			}
			catch (Throwable t)
			{
				// If there is not supposed to be data in the file -
				// simply return zero length byte array
				if (((BaseResourceEdit) resource).m_contentLength == 0)
				{
					return new byte[0];
				}

				// If we have a non-zero body length and reading failed,
				// it is an error worth of note
				log.warn(": failed to read resource: " + resource.getId() + " len: "
						+ ((BaseResourceEdit) resource).m_contentLength + " : " + t);
				throw new ServerOverloadException("failed to read resource");
				// return null;
			}

		}

		// the body is already in the resource for this version of
		// storage
		public InputStream streamResourceBody(ContentResource resource)
				throws ServerOverloadException
		{
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return resolver.streamResourceBody(this, resource);
				}
				else
				{
					if (((BaseResourceEdit) resource).m_contentLength <= 0)
					{
						log.warn("streamResourceBody(): non-positive content length: "
								+ ((BaseResourceEdit) resource).m_contentLength
								+ "  id: " + resource.getId());
						return null;
					}

					// if we have been configured to use an external
					// file system
					if (m_bodyPath != null)
					{
						return streamResourceBodyFilesystem(resource);
					}

					// otherwise use the database
					else
					{
						return streamResourceBodyDb(resource);
					}
				}
			}
			finally
			{
				out();
			}
		}

		/**
		 * Return an input stream.
		 * 
		 * @param resource -
		 *        the resource for the stream It is a non-fatal error for the
		 *        file not to be readible as long as the resource's expected
		 *        length is zero. A zero length body is indicated by returning
		 *        null. We check for the body length *after* we try to read the
		 *        file. If the file is readible, we simply read it and return it
		 *        as the body.
		 */

		protected InputStream streamResourceBodyFilesystem(ContentResource resource)
				throws ServerOverloadException
		{
			// form the file name
			File file = new File(externalResourceFileName(resource));

			// read the new
			try
			{
				FileInputStream in = new FileInputStream(file);
				return in;
			}
			catch (Throwable t)
			{
				// If there is not supposed to be data in the file - simply
				// return null
				if (((BaseResourceEdit) resource).m_contentLength == 0)
				{
					return null;
				}

				// If we have a non-zero body length and reading failed, it is
				// an error worth of note
				log.warn(": failed to read resource: " + resource.getId() + " len: "
						+ ((BaseResourceEdit) resource).m_contentLength + " : " + t);
				throw new ServerOverloadException("failed to read resource body");
				// return null;
			}
		}

		/**
		 * When resources are stored, zero length bodys are not placed in the
		 * table hence this routine will return a null when the particular
		 * resource body is not found
		 */
		protected InputStream streamResourceBodyDb(ContentResource resource)
				throws ServerOverloadException
		{
			// get the resource from the db
			String sql = "select BODY from " + m_resourceBodyTableName
					+ " where ( RESOURCE_ID = ? )";

			Object[] fields = new Object[1];
			fields[0] = resource.getId();

			// get the stream, set expectations that this could be big
			InputStream in = m_sqlService.dbReadBinary(sql, fields, true);

			return in;
		}

		/**
		 * Write the resource body to the database table.
		 * 
		 * @param resource
		 *        The resource whose body is being written.
		 * @param body
		 *        The body bytes to write. If there is no body or the body is
		 *        zero bytes, no entry is inserted into the table.
		 */
		protected void putResourceBodyDb(ContentResourceEdit resource, byte[] body)
		{

			if ((body == null) || (body.length == 0)) return;

			// delete the old
			String statement = "delete from " + m_resourceBodyTableName
					+ " where resource_id = ? ";

			Object[] fields = new Object[1];
			fields[0] = resource.getId();

			m_sqlService.dbWrite(statement, fields);

			// add the new
			statement = "insert into " + m_resourceBodyTableName + " (RESOURCE_ID, BODY)"
					+ " values (? , ? )";

			m_sqlService.dbWriteBinary(statement, fields, body, 0, body.length);

			/*
			 * %%% BLOB code // read the record's blob and update statement =
			 * "select body from " + m_resourceTableName + " where ( resource_id = '" +
			 * Validator.escapeSql(resource.getId()) + "' ) for update";
			 * Sql.dbReadBlobAndUpdate(statement,
			 * ((BaseResource)resource).m_body);
			 */
		}

		/**
		 * @param edit
		 * @param stream
		 */
		protected void putResourceBodyDb(ContentResourceEdit edit, InputStream stream)
		{
			// Do not create the files for resources with zero length bodies
			if ((stream == null)) return;

			ByteArrayOutputStream bstream = new ByteArrayOutputStream();

			int byteCount = 0;

			// chunk
			byte[] chunk = new byte[STREAM_BUFFER_SIZE];
			int lenRead;
			try
			{
				while ((lenRead = stream.read(chunk)) != -1)
				{
					bstream.write(chunk, 0, lenRead);
					byteCount += lenRead;
				}

				edit.setContentLength(byteCount);
				ResourcePropertiesEdit props = edit.getPropertiesEdit();
				props.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, Long
						.toString(byteCount));
				if (edit.getContentType() != null)
				{
					props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, edit
							.getContentType());
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				log.warn("IOException ", e);
			}
			finally
			{
				if (stream != null)
				{
					try
					{
						stream.close();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						log.warn("IOException ", e);
					}
				}
			}

			if (bstream != null && bstream.size() > 0)
			{
				putResourceBodyDb(edit, bstream.toByteArray());
			}
		}

		/**
		 * @param edit
		 * @param stream
		 * @return
		 */
		private boolean putResourceBodyFilesystem(ContentResourceEdit resource,
				InputStream stream)
		{
			// Do not create the files for resources with zero length bodies
			if ((stream == null)) return true;

			// form the file name
			File file = new File(externalResourceFileName(resource));

			// delete the old
			if (file.exists())
			{
				file.delete();
			}

			FileOutputStream out = null;

			// add the new
			try
			{
				// make sure all directories are there
				File container = file.getParentFile();
				if (container != null)
				{
					container.mkdirs();
				}

				// write the file
				out = new FileOutputStream(file);

				int byteCount = 0;
				// chunk
				byte[] chunk = new byte[STREAM_BUFFER_SIZE];
				int lenRead;
				while ((lenRead = stream.read(chunk)) != -1)
				{
					out.write(chunk, 0, lenRead);
					byteCount += lenRead;
				}

				resource.setContentLength(byteCount);
				ResourcePropertiesEdit props = resource.getPropertiesEdit();
				props.addProperty(ResourceProperties.PROP_CONTENT_LENGTH, Long
						.toString(byteCount));
				if (resource.getContentType() != null)
				{
					props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, resource
							.getContentType());
				}
			}
			// catch (Throwable t)
			// {
			// M_log.warn(": failed to write resource: " + resource.getId() + "
			// : " + t);
			// return false;
			// }
			catch (IOException e)
			{
				log.warn("IOException", e);
				return false;
			}
			finally
			{
				if (stream != null)
				{
					try
					{
						stream.close();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						log.warn("IOException ", e);
					}
				}

				if (out != null)
				{
					try
					{
						out.close();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						log.warn("IOException ", e);
					}
				}
			}

			return true;
		}

		/**
		 * Write the resource body to the external file system. The file name is
		 * the m_bodyPath with the resource id appended.
		 * 
		 * @param resource
		 *        The resource whose body is being written.
		 * @param body
		 *        The body bytes to write. If there is no body or the body is
		 *        zero bytes, no entry is inserted into the filesystem.
		 */
		protected boolean putResourceBodyFilesystem(ContentResourceEdit resource,
				byte[] body)
		{
			// Do not create the files for resources with zero length bodies
			if ((body == null) || (body.length == 0)) return true;

			// form the file name
			File file = new File(externalResourceFileName(resource));

			// delete the old
			if (file.exists())
			{
				file.delete();
			}

			// add the new
			try
			{
				// make sure all directories are there
				File container = file.getParentFile();
				if (container != null)
				{
					container.mkdirs();
				}

				// write the file
				FileOutputStream out = new FileOutputStream(file);
				out.write(body);
				out.close();
			}
			catch (Throwable t)
			{
				log.warn(": failed to write resource: " + resource.getId() + " : " + t);
				return false;
			}

			return true;
		}

		/**
		 * Delete the resource body from the database table.
		 * 
		 * @param resource
		 *        The resource whose body is being deleted.
		 */
		protected void delResourceBodyDb(ContentResourceEdit resource)
		{
			// delete the record
			String statement = "delete from " + m_resourceBodyTableName
					+ " where resource_id = ?";

			Object[] fields = new Object[1];
			fields[0] = resource.getId();

			m_sqlService.dbWrite(statement, fields);
		}

		/**
		 * Delete the resource body from the external file system. The file name
		 * is the m_bodyPath with the resource id appended.
		 * 
		 * @param resource
		 *        The resource whose body is being written.
		 */
		protected void delResourceBodyFilesystem(ContentResourceEdit resource)
		{
			// form the file name
			File file = new File(externalResourceFileName(resource));

			// delete
			if (file.exists())
			{
				file.delete();
			}
		}

		public int getMemberCount(String collectionId)
		{
			if (collectionId == null || collectionId.trim().length() == 0)
			{
				return 0;
			}
			boolean goin = in();
			try
			{
				if (resolver != null && goin)
				{
					return resolver.getMemberCount(this, collectionId);
				}
				else
				{

					int fileCount = 0;
					try
					{
						fileCount = countQuery(
								"select count(IN_COLLECTION) from CONTENT_RESOURCE where IN_COLLECTION = ?",
								collectionId);
					}
					catch (IdUnusedException e)
					{
						// ignore -- means this is not a collection or the
						// collection contains no files, so zero is right answer
					}
					int folderCount = 0;
					try
					{
						folderCount = countQuery(
								"select count(IN_COLLECTION) from CONTENT_COLLECTION where IN_COLLECTION = ?",
								collectionId);
					}
					catch (IdUnusedException e)
					{
						// ignore -- means this is not a collection or the
						// collection contains no folders, so zero is right
						// answer
					};
					return fileCount + folderCount;
				}
			}
			finally
			{
				out();
			}
		}

		public Collection<String> getMemberCollectionIds(String collectionId)
		{
			List list = null;
			try
			{
				String sql = "select COLLECTION_ID from " + m_collectionTableName
						+ " where IN_COLLECTION = ?";
				Object[] fields = new Object[1];
				fields[0] = collectionId;

				list = m_sqlService.dbRead(sql, fields, null);
			}
			catch (Throwable t)
			{
				log.warn("getMemberCollectionIds: failed: " + t);
			}
			return (Collection<String>) list;
		}

		public Collection<String> getMemberResourceIds(String collectionId)
		{
			List list = null;
			try
			{
				String sql = "select RESOURCE_ID from " + m_resourceTableName
						+ " where IN_COLLECTION = ?";
				Object[] fields = new Object[1];
				fields[0] = collectionId;

				list = m_sqlService.dbRead(sql, fields, null);
			}
			catch (Throwable t)
			{
				log.warn("getMemberResourceIds: failed: " + t);
			}
			return (Collection<String>) list;
		}

	}

	/**
	 * @return the jcrService
	 */
	public JCRService getJcrService()
	{
		return jcrService;
	}

	/**
	 * @param jcrService the jcrService to set
	 */
	public void setJcrService(JCRService jcrService)
	{
		this.jcrService = jcrService;
	}
}
