/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/citation/trunk/citation-impl/impl/src/java/org/sakaiproject/citation/impl/BaseCitationService.java $
 * $Id: BaseCitationService.java 1552 2006-08-11 06:20:50Z jimeng@umich.edu $
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

package org.sakaiproject.citation.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationEdit;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationCollectionEdit;
import org.sakaiproject.citation.api.CitationService;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.citation.api.Schema.Field;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.javax.Filter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BaseCitationService implements CitationService
{
	// protected static ResourceLoader rb = new ResourceLoader("citation_mgr");
	
	protected static final String UNKNOWN_TYPE = "unknown";

	public static final String SCHEMA_PREFIX = "schema.";
	
	protected Map schemas;

	protected String m_defaultSchema;
	
	/**
	 * 
	 *
	 */
	public void init()
	{
		schemas = new Hashtable();
		
		BasicSchema unknown = new BasicSchema();
		unknown.setIdentifier(UNKNOWN_TYPE);
		unknown.addField("title", String.class.getName(), true, 1, 1);
		unknown.addField("creator", String.class.getName(), false, 0, 1);
		unknown.addField("pubyear", String.class.getName(), false, 0, 1);
		unknown.addField("place", String.class.getName(), false, 0, 1);
		unknown.addField("pubdate", String.class.getName(), false, 0, 1);	
		unknown.addField("publisher", String.class.getName(), false, 0, 1);
		unknown.addField("editor", String.class.getName(), false, 0, 1);
		unknown.addField("issuer", String.class.getName(), false, 0, 1);
		unknown.addField("copyright", String.class.getName(), false, 0, 1);
		unknown.addField("serieseditor", String.class.getName(), false, 0, 1); 	
		unknown.addField("seriestitle", String.class.getName(), false, 0, 1); 
		unknown.addField("edition", String.class.getName(), false, 0, 1);
		unknown.addField("journal", String.class.getName(), false, 0, 1);
		unknown.addField("volume", String.class.getName(), false, 0, 1);
		unknown.addField("issue", String.class.getName(), false, 0, 1);
		unknown.addField("pages", String.class.getName(), false, 0, 1);
		unknown.addField("abstract", String.class.getName(), false, 0, 1); 	
		unknown.addField("notes", String.class.getName(), false, 0, 1);
		unknown.addField("callnum", String.class.getName(), false, 0, 1);	
		unknown.addField("isbn", String.class.getName(), false, 0, 1); 	
		unknown.addField("issn", String.class.getName(), false, 0, 1);
		unknown.addField("language", String.class.getName(), false, 0, 1);
		schemas.put(unknown.getIdentifier(), unknown);

		BasicSchema book = new BasicSchema();
		book.setIdentifier("book");
		book.addRequiredField("title", String.class.getName(), 1, 1);
		book.addRequiredField("creator", String.class.getName(), 0, 1);
		book.addRequiredField("pubyear", String.class.getName(), 0, 1);
		book.addRequiredField("pubdate", String.class.getName(), 0, 1);
		book.addRequiredField("publisher", String.class.getName(), 0, 1);
		book.addRequiredField("editor", String.class.getName(), 0, 1);
		book.addRequiredField("place", String.class.getName(), 0, 1);
		book.addRequiredField("language", String.class.getName(), 0, 1);
		book.addRequiredField("abstract", String.class.getName(), 0, 1);
		book.addRequiredField("notes", String.class.getName(), 0, 1);
		book.addRequiredField("isbn", String.class.getName(), 0, 1);
		book.addRequiredField("callnum", String.class.getName(), 0, 1);
		book.addRequiredField("serieseditor", String.class.getName(), 0, 1);
		book.addRequiredField("seriestitle", String.class.getName(), 0, 1);
		book.addRequiredField("pages", String.class.getName(), 0, 1);
		schemas.put(book.getIdentifier(), book);
		
		BasicSchema report = new BasicSchema();
		report.setIdentifier("report");
		report.addField("title", "String", true, 0, 1);
		report.addField("creator", "String", false, 0, 1);
		report.addField("pubyear", "String", false, 0, 1);
		report.addField("pubdate", "String", false, 0, 1);
		report.addField("publisher", "String", false, 0, 1);
		report.addField("edition", "String", false, 0, 1);
		report.addField("place", "String", false, 0, 1);
		report.addField("copyright", "String", false, 0, 1);
		report.addField("language", "String", false, 0, 1);
		report.addField("abstract", "String", false, 0, 1);
		report.addField("notes", "String", false, 0, 1);
		report.addField("issuer", "String", false, 0, 1);
		report.addField("repnum", "String", false, 0, 1);
		report.addField("pages", "String", false, 0, 1);
		schemas.put(report.getIdentifier(), report);
		
		BasicSchema article = new BasicSchema();
		article.setIdentifier("article");
		article.addField("title", "String", true, 1, 1);
		article.addField("creator", "String", false, 0, 1);
		article.addField("journal", "String", true, 1, 1);
		article.addField("volume", "String", false, 0, 1);
		article.addField("issue", "String", false, 0, 1);
		article.addField("pages", "String", false, 0, 1);
		article.addField("pubyear", "String", false, 0, 1);
		article.addField("pubdate", "String", false, 0, 1);
		article.addField("publisher", "String", false, 0, 1);
		article.addField("edition", "String", false, 0, 1);
		article.addField("place", "String", false, 0, 1);
		article.addField("copyright", "String", false, 0, 1);
		article.addField("language", "String", false, 0, 1);
		article.addField("abstract", "String", false, 0, 1);
		article.addField("notes", "String", false, 0, 1);
		article.addField("issn", "String", false, 0, 1);
		article.addField("callnum", "String", false, 0, 1);
		schemas.put(article.getIdentifier(), article);
		
		m_defaultSchema = article.getIdentifier();
	}
	
	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		
	}


	public class BasicCitationEdit implements CitationEdit
	{
		protected String m_displayName = null;
		protected String m_fullTextUrl = null;
		protected String m_imageUrl = null;
		protected String m_citationUrl = null;
		protected String m_searchSourceUrl = null;
		protected Map m_citationProperties = null;
		protected Reference m_reference = null;
		protected ResourceProperties m_resourceProperties = null;
		protected Schema m_schema;
		
		
		public BasicCitationEdit()
		{
			m_citationProperties = new Hashtable();
			// m_resourceProperties = ;
			
		}

		public void setDisplayName(String name)
		{
			m_displayName = name;
			
		}

		public void setFullTextUrl(String url)
		{
			m_fullTextUrl = url;
			
		}

		public void setImageUrl(String url)
		{
			m_imageUrl = url;
			
		}

		public void setCitationUrl(String url)
		{
			m_citationUrl = url;
			
		}

		public void setSearchSourceUrl(String url)
		{
			m_searchSourceUrl  = url;
			
		}

		public void setCitationProperty(String name, Object value)
		{
			if(m_citationProperties == null)
			{
				m_citationProperties = new Hashtable();
			}
			m_citationProperties.put(name, value);
			
		}

		public String getDisplayName()
		{
			return m_displayName;
			
		}

		public String getFullTextUrl()
		{
			return m_fullTextUrl;
			
		}

		public String getImageUrl()
		{
			return m_imageUrl;
			
		}

		public String getCitationUrl()
		{
			return m_citationUrl ;
			
		}

		public String getSearchSourceUrl()
		{
			return m_searchSourceUrl;
			
		}

		public Map getCitationProperties()
		{
			if(m_citationProperties == null)
			{
				m_citationProperties = new Hashtable();
			}
			
			return m_citationProperties;
			
		}

		public List getCitationPropertyNames()
		{
			if(m_citationProperties == null)
			{
				m_citationProperties = new Hashtable();
			}
			
			return new Vector(m_citationProperties.keySet());
			
		}

		public Object getCitationProperty(String name)
		{
			if(m_citationProperties == null)
			{
				m_citationProperties = new Hashtable();
			}

			return m_citationProperties.get(name);
			
		}

		public String getUrl()
		{
			return m_reference.getUrl();
			
		}

		public String getReference()
		{
			return m_reference.getReference();
		}

		public String getUrl(String rootProperty)
		{
			// TODO Fixme
			return m_reference.getUrl();
		}

		public String getReference(String rootProperty)
		{
			// TODO fixme
			return m_reference.getReference();
		}

		public String getId()
		{
			// TODO Auto-generated method stub
			return m_reference.getId();
		}

		public ResourceProperties getProperties()
		{
			return m_resourceProperties;
			
		}

		public Element toXml(Document doc, Stack stack)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isActiveEdit()
		{
			// TODO Auto-generated method stub
			return false;
		}

		public ResourcePropertiesEdit getPropertiesEdit()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Schema getSchema()
		{
			return m_schema;
		}

	} // BaseCitationService.BasicCitationEdit

	public class BasicCitationListEdit implements CitationCollectionEdit
	{
		public void sort(Comparator c)
		{
			// TODO Auto-generated method stub
			
		}

		public boolean move(int from, int to)
		{
			// TODO Auto-generated method stub
			return false;
		}

		public boolean moveToFront(int index)
		{
			// TODO Auto-generated method stub
			return false;
		}

		public boolean moveToBack(int index)
		{
			// TODO Auto-generated method stub
			return false;
		}

		public boolean add(int index, Citation element)
		{
			// TODO Auto-generated method stub
			return false;
		}

		public boolean add(Citation element)
		{
			// TODO Auto-generated method stub
			return false;
		}

		public boolean addAll(CitationCollection other)
		{
			// TODO Auto-generated method stub
			return false;
		}

		public boolean addAll(int index, CitationCollection other)
		{
			// TODO Auto-generated method stub
			return false;
		}

		public void clear()
		{
			// TODO Auto-generated method stub
			
		}

		public Citation remove(int index)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public boolean remove(Citation item)
		{
			// TODO Auto-generated method stub
			return false;
		}

		public Citation remove(Map properties)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public CitationCollection getCitations(Comparator c)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public CitationCollection getCitations(Filter f)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public CitationCollection getCitations(Comparator c, Filter f)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public CitationCollection getCitations(Map properties)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Citation getCitation(String id)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Citation getCitation(int index)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Citation getCitation(Map properties)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isEmpty()
		{
			// TODO Auto-generated method stub
			return false;
		}

		public Iterator iterator()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public boolean contains(Citation item)
		{
			// TODO Auto-generated method stub
			return false;
		}

		public boolean containsAll(CitationCollection list)
		{
			// TODO Auto-generated method stub
			return false;
		}

		public int indexOf(Citation item)
		{
			// TODO Auto-generated method stub
			return 0;
		}

		public int lastIndexOf(Citation item)
		{
			// TODO Auto-generated method stub
			return 0;
		}


		public String getUrl()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getReference()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getUrl(String rootProperty)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getReference(String rootProperty)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public String getId()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public ResourceProperties getProperties()
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Element toXml(Document doc, Stack stack)
		{
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isActiveEdit()
		{
			// TODO Auto-generated method stub
			return false;
		}

		public ResourcePropertiesEdit getPropertiesEdit()
		{
			// TODO Auto-generated method stub
			return null;
		}

	} // BaseCitationService.BasicCitationListEdit

	protected class BasicSchema implements Schema
	{
		protected List fields;
		protected Map index;
		protected String defaultNamespace;
		protected Map namespaces;
		protected String identifier;

		/**
         * @param schema
         */
        public BasicSchema(Schema other)
        {
	        this.identifier = other.getIdentifier();
	        this.defaultNamespace = other.getNamespaceUri();
	        namespaces = new Hashtable();
	        List nsAbbrevs = other.getNamespaceAbbreviations();
	        if(nsAbbrevs != null)
	        {
		        Iterator nsIt = nsAbbrevs.iterator();
		        while(nsIt.hasNext())
		        {
		        	String nsAbbrev = (String) nsIt.next();
		        	String ns = other.getNamespaceUri(nsAbbrev);
		        	namespaces.put(nsAbbrev, ns);
		        }
	        }
	        this.fields = new Vector();
	        this.index = new Hashtable();
	        List fields = other.getFields();
	        Iterator fieldIt = fields.iterator();
	        while(fieldIt.hasNext())
	        {
	        	Field field = (Field) fieldIt.next();
	        	this.fields.add(new BasicField(field));
				index.put(field.getIdentifier(), field);
	        }
        }

		/**
         * 
         */
        public BasicSchema()
        {
	        this.fields = new Vector();
	        this.index = new Hashtable();
       }

		public List getFields()
		{
			if(fields == null)
			{
				fields = new Vector();
			}			
			return fields;
		}

		/**
         * @param identifier
         */
        public void setIdentifier(String identifier)
        {
	        this.identifier = identifier;
        }

		public List getRequiredFields()
		{
			if(fields == null)
			{
				fields = new Vector();
			}
			List required = new Vector();
			Iterator it = fields.iterator();
			while(it.hasNext())
			{
				Field field = (Field) it.next();
				if(field.isRequired())
				{
					required.add(field);
				}
			}
			
			return required;
		}

		public Field getField(String name)
		{
			if(index == null)
			{
				index = new Hashtable();
			}
			return (Field) index.get(name);
		}

		public Field getField(int index)
		{
			if(fields == null)
			{
				fields = new Vector();
			}
			return (Field) fields.get(index);
		}
		
		public BasicField addRequiredField(String identifier, String valueType, int minCardinality, int maxCardinality)
		{
			return addField(identifier, valueType, true, minCardinality, maxCardinality);
		}
		
		public BasicField addOptionalField(String identifier, String valueType, int minCardinality, int maxCardinality)
		{
			return addField(identifier, valueType, false, minCardinality, maxCardinality);
		}
		
		public BasicField addField(String identifier, String valueType, boolean required, int minCardinality, int maxCardinality)
		{
			if(fields == null)
			{
				fields = new Vector();
			}
			if(index == null)
			{
				index = new Hashtable();
			}
			BasicField field = new BasicField(identifier, valueType, required, minCardinality, maxCardinality);
			fields.add(field);
			index.put(identifier, field);
			return field;
		}
		
		protected class BasicField implements Field
		{
			protected String identifier;
			protected String namespace;
			protected String label;
			protected String description;
			protected boolean required;
			protected String valueType;
			protected int minCardinality;
			protected int maxCardinality;
			protected Object defaultValue;
			
			public BasicField(String identifier, String valueType, boolean required, int minCardinality, int maxCardinality)
			{
				this.identifier = identifier;
				this.valueType = valueType;
				this.required = required;
				this.minCardinality = minCardinality;
				this.maxCardinality = maxCardinality;
				this.namespace = "";
				this.label = "";
				this.description = "";
			}
			
			/**
             * @param field
             */
            public BasicField(Field other)
            {
				this.identifier = other.getIdentifier();
				this.valueType = other.getValueType();
				this.required = other.isRequired();
				this.minCardinality = other.getMinCardinality();
				this.maxCardinality = other.getMaxCardinality();
				this.namespace = other.getNamespaceAbbreviation();
				this.description = other.getDescription();	            
            }

			public boolean isRequired()
			{
				return required;
			}
			
			public String getValueType()
			{
				return valueType;
			}
			
			public int getMinCardinality()
			{
				return minCardinality;
			}
			
			public int getMaxCardinality()
			{
				return maxCardinality;
			}

			/**
			 * @param maxCardinality The maxCardinality to set.
			 */
			public void setMaxCardinality(int maxCardinality)
			{
				this.maxCardinality = maxCardinality;
			}

			/**
			 * @param minCardinality The minCardinality to set.
			 */
			public void setMinCardinality(int minCardinality)
			{
				this.minCardinality = minCardinality;
			}

			/**
			 * @param required The required to set.
			 */
			public void setRequired(boolean required)
			{
				this.required = required;
			}

			/**
			 * @param valueType The valueType to set.
			 */
			public void setValueType(String valueType)
			{
				this.valueType = valueType;
			}

			public Object getDefaultValue()
			{
				return defaultValue;
			}
			
			public void setDefaultValue(Object value)
			{
				this.defaultValue = value;
			}

			public String getNamespaceAbbreviation()
			{
				return this.namespace;
			}

			public void setNamespaceAbbreviation(String namespace)
			{
				this.namespace = namespace;
			}

			public String getIdentifier()
			{
				return identifier;
			}

			public String getLabel()
			{
				return this.label;
			}

			public String getDescription()
			{
				return this.description;
			}
			
		}

		public String getNamespaceUri()
		{
			return defaultNamespace;
		}

		public String getNamespaceUri(String abbrev)
		{
			if(namespaces == null)
			{
				namespaces = new Hashtable();
			}
			return (String) namespaces.get(abbrev);
		}

		public List getNamespaceAbbreviations()
		{
			if(namespaces == null)
			{
				namespaces = new Hashtable();
			}
			Collection keys = namespaces.keySet();
			List rv = new Vector();
			if(keys != null)
			{
				rv.addAll(keys);
			}
			return rv;
		}

		/* (non-Javadoc)
         * @see org.sakaiproject.citation.api.Schema#getIdentifier()
         */
        public String getIdentifier()
        {
	        return this.identifier;
        }

	}

	public CitationEdit newCitation(String mediatype)
	{
		CitationEdit edit = new BasicCitationEdit();
		Schema schema = (Schema) schemas.get(mediatype);
		if(schema == null)
		{
			schema = (Schema) schemas.get(UNKNOWN_TYPE);
		}
		List fields = schema.getFields();
		Iterator it = fields.iterator();
		while(it.hasNext())
		{
			Field field = (Field) it.next();
			if(field.isRequired())
			{
				Object value = field.getDefaultValue();
				if(value == null)
				{
					if(field.getValueType().equals(String.class.getName()))
					{
						value = "";
					}
				}
				edit.setCitationProperty(field.getIdentifier(), value);
			}
		}
		return edit;
	}

	public Schema getSchema(String name)
	{
		if(schemas == null)
		{
			schemas = new Hashtable();
		}
		return (Schema) schemas.get(name);
	}

	/**
	 * Access a list of all schemas that have been defined (other than the "unknown" type).
	 * @return A list of Strings representing the identifiers for known schemas.
	 */
	public List listSchemas()
	{
		if(schemas == null)
		{
			schemas = new Hashtable();
		}
		List names = new Vector();
		Set keys = schemas.keySet();
		if(keys != null)
		{
			names.addAll(keys);
		}
		return names;
	}

	/* (non-Javadoc)
     * @see org.sakaiproject.citation.api.CitationService#getSchemas()
     */
    public List getSchemas()
    {
		if(schemas == null)
		{
			schemas = new Hashtable();
		}
		List list = new Vector();
		Iterator it = schemas.keySet().iterator();
		while(it.hasNext())
		{
			String key = (String) it.next();
			Schema schema = (Schema) schemas.get(key);
			{
				list.add(new BasicSchema(schema));
			}
		}
		return list;
   }

	public boolean willArchiveMerge()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean parseEntityReference(String reference, Reference ref)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public String getEntityDescription(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getEntity(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityUrl(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public HttpAccess getHttpAccess()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
     * @see org.sakaiproject.citation.api.CitationService#getDefaultSchema()
     */
    public Schema getDefaultSchema()
    {
    	Schema rv = null;
    	if(m_defaultSchema != null)
    	{
    		rv = (Schema) schemas.get(m_defaultSchema);
    	}
	    return rv;
    }



} // BaseCitationService

