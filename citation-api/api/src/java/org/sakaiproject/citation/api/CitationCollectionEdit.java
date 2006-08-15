/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/citation/trunk/citation-api/api/src/java/org/sakaiproject/citation/api/CitationCollectionEdit.java $
 * $Id: CitationCollectionEdit.java 1527 2006-08-09 18:13:59Z jimeng@umich.edu $
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

//import org.sakaiproject.service.citation.api.CitationList;
import java.util.Comparator;
import java.util.Map;

import org.sakaiproject.entity.api.Edit;

/**
 * 
 *
 */
public interface CitationCollectionEdit extends CitationCollection, Edit
{
	public void sort(Comparator c);
	
	/**
	 * Move an item from one place in the list to another.
	 * @param from The index of the element to be moved.
	 * @param to The index the element should have after the move.
	 * @return true if the move succeeded.
	 */
	public boolean move(int from, int to);
	
	/**
	 * Move an item from a specified index in the list to the beginning of the list (index of 0).
	 * @param index
	 * @return true if the move succeeded.
	 */
	public boolean moveToFront(int index);
	
	/**
	 * Move an item from a specified index in the list to the end of the list (index of list.size() - 1).
	 * @param index
	 * @return true if the move succeeded.
	 */
	public boolean moveToBack(int index);
	
	/**
	 * Inserts the specified Citation at the specified position in this list.
	 * @param index
	 * @param element
	 * @return true if the list changed as a result of this operation.
	 */
	public boolean add(int index, Citation element);
	
	/**
	 * Appends the specified Citation at the end of this list.
	 * @param element
	 * @return true if the list changed as a result of this operation.
	 */
	public boolean add(Citation element);
	
	/**
	 * Appends all of the Citations in the specified CitationCollection to the end of this CitationCollection, in the order that they are 
	 * returned by the specified CitationCollection's iterator. This operation fails if this list and the other list are the same
	 * Object.
	 * @param other The list containing the Citations to be appended to this list.
	 * @return true if the list changed as a result of this operation.
	 */
	public boolean addAll(CitationCollection other); 
	
	/**
	 * Inserts all of the Citations in the specified CitationCollection into this CitationCollection, in the order that they are returned by 
	 * the specified CitationCollection's iterator, beginning at the location indicated by the index parameter. Shifts any subsequent 
	 * elements to the right (increases their indices by the number indicating the size of the other list). This operation fails 
	 * if this list and the other list are the same Object.
	 * @param index The offset from the beginning of this list at which the first Citation from the other list should be inserted. 
	 * @param other The list containing the Citations to be inserted into this list.
	 * @return true if the list changed as a result of this operation.
	 */
	public boolean addAll(int index, CitationCollection other); 
	
	/**
	 * Removes all of the elements from this list.
	 */
	public void clear();
	
	/**
	 * Removes the element at the specified position in this list. Shifts any subsequent elements to the left (subtracts one from their 
	 * indices). Returns the element that was removed from the list (null if index out of bounds).
	 * @param index
	 * @return the element that was removed from the list (null if index out of bounds)
	 */
	public Citation remove(int index);
	
	/**
	 * Removes the first occurrence in this list of the specified Citation. Shifts any subsequent elements to the left (subtracts one from 
	 * their indices). Returns true if the list contained the specified citation.
	 * @param item The element to be removed from the list, if present.
	 * @return true if the list changed as a result of this call.
	 */
	public boolean remove(Citation item);
	
	/**
	 * Removes the first occurrence in this list of a Citation with name-value pairs in its properties matching the name-value pairs in 
	 * the properties parameter. Shifts any subsequent elements to the left (subtracts one from their indices). Returns the element that 
	 * was removed from the list (null if no element in the list matched the properties).
	 * @param item The element to be removed from the list, if present.
	 * @return the element that was removed from the list (null if no element in the list matched the properties).
	 */
	public Citation remove(Map properties);
	

}	// interface Citation

