/**
 * ConcurrentList.java - content-test - 2007 Nov 9, 2007 3:33:26 PM - azeckoski
 */

package org.sakaiproject.content.test.util;

import java.util.AbstractList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Concurrent list - thread safe
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ConcurrentList<V> extends AbstractList<V> {

   private ConcurrentHashMap<Integer, V> hashMap = new ConcurrentHashMap<Integer, V>();

   @Override
   public boolean add(V o) {
      V existing = hashMap.put(hashMap.size(), o);
      return existing == o;
   }

   @Override
   public V get(int index) {
      if (index >= hashMap.size()) {
         throw new IndexOutOfBoundsException(index + " is not within the size of the list: " + hashMap.size());
      }
      return hashMap.get(index);
   }

   @Override
   public int size() {
      return hashMap.size();
   }

   @Override
   public boolean remove(Object o) {
      boolean result = false;
      for (Map.Entry<Integer, V> entry : hashMap.entrySet()) {
         if (entry.getValue().equals(o)) {
            removeItem(entry.getKey());
            result = true;
            break;
         }
      }
      return result;
   }

   /**
    * @param entry
    */
   private V removeItem(int index) {
      V current = null;
      if ( index == (hashMap.size() - 1) ) {
         current = hashMap.remove(index);
      } else {
         Integer endKey = hashMap.size()-1;
         current = hashMap.put(index, hashMap.get(endKey));
         hashMap.remove(endKey);
      }
      return current;
   }

   @Override
   public V remove(int index) {
      if (!hashMap.containsKey(index)) {
         throw new IndexOutOfBoundsException("List does not contain an item at index: " + index);
      } else {
         return removeItem(index);
      }
   }

   @Override
   public void clear() {
      hashMap.clear();
   }

}
