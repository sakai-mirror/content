/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/citation/trunk/citation-api/api/src/java/org/sakaiproject/citation/api/CitationCollection.java $
 * $Id: CitationCollection.java 1527 2006-08-09 18:13:59Z jimeng@umich.edu $
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

package org.sakaiproject.citation.api;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.javax.Filter;


/**
 * 
 *
 */
public interface CitationCollection extends Entity
{
	/**
	 * Access an ordered list of all Citations belonging to the CitationCollection.
	 * @return The ordered list of Citation objects. May be empty but will not be null.
	 */
	// public List getCitations();
	
	/**
	 * Access a sorted list of all Citations belonging to the CitationCollection, where the sort order is
	 * determined by the Comparator (@see http://java.sun.com/j2se/1.4.2/docs/api/java/util/Comparator.html).  
	 * @param c The comparator that determines the relative ordering of any two Citations. 
	 * @return The sorted list of Citations.  May be empty but will not be null.
	 */
	public CitationCollection getCitations(Comparator c);
	
	/**
	 * Access an ordered list of a subset of the Citations belonging to the CitationCollection, where membership in the subset is
	 * determined by the filter (@see Filter).  
	 * @param f The filter that determines membership in the subset.
	 * @return The filtered list of Citations.  May be empty but will not be null.
	 */
	public CitationCollection getCitations(Filter f);
	
	/**
	 * Access a sorted list of a subset of the Citations belonging to the CitationCollection, where the sort order is
	 * determined by the Comparator (@see http://java.sun.com/j2se/1.4.2/docs/api/java/util/Comparator.html) and 
	 * membership in the subset is determined by the filter (@see Filter). 
	 * @param c The comparator that determines the relative ordering of any two Citations.
	 * @param f The filter that determines membership in the subset.
	 * @return The sorted, filtered list of Citations.  May be empty but will not be null. 
	 */
	public CitationCollection getCitations(Comparator c, Filter f);
	
	/**
	 * Access all Citations in the list with name-value pairs in their properties matching the name-value pairs in 
	 * the properties parameter. Returns a list of Citations satisfying the criteria, which may be empty if no Citations
	 * in the list satisfy the criteria.  
	 * @param properties A mapping of name-value pairs indicating names of properties as Strings and values of properties as Strings.
	 * @return the list of elements that match, which may be empty but not null.
	 */
	public CitationCollection getCitations(Map properties);
		
	/**
	 * Access a particular Citation from the list.
	 * @param id
	 * @return The Citation.
	 */
	public Citation getCitation(String id);
	
	/**
	 * Access a particular Citation from the list.
	 * @param index The index the the Citation in the unsorted, unfiltered list. 
	 * @return The Citation.
	 */
	public Citation getCitation(int index);
	
	/**
	 * Access the first occurrence in this list of a Citation with name-value pairs in its properties matching the name-value pairs in 
	 * the properties parameter. Returns the element, or null if no element in the list matched the properties.
	 * @param properties A mapping of name-value pairs indicating names of properties as Strings and values of properties as Strings.
	 * @return the first element that matches, or null if no element in the list matches the properties.
	 */
	public Citation getCitation(Map properties);
		
	/**
	 * Returns true if this collection contains no elements.
	 * @return true if this collection contains no elements.
	 */
	public boolean isEmpty();
	
	/**
	 * Returns an iterator over the Citation objects in this CitationCollection.
	 * @return an Iterator over the Citation objects in this CitationCollection
	 */
	public Iterator iterator();
	
	public boolean contains(Citation item); 
	
	public boolean containsAll(CitationCollection list);
	
	/**
	 * Returns the index in this list of the first occurrence of the specified Citation, or -1 if this list does not contain this Citation.
	 * @param item The element to search for.
	 * @return the index in this list of the first occurrence of the specified Citation, or -1 if this list does not contain this Citation.
	 */
	public int indexOf(Citation item);
	
	/**
	 * Returns the index in this list of the last occurrence of the specified Citation, or -1 if this list does not contain this Citation.
	 * @param item The element to search for.
	 * @return the index in this list of the last occurrence of the specified Citation, or -1 if this list does not contain this Citation.
	 */
	public int lastIndexOf(Citation item);
	
	

}	// interface Citation

