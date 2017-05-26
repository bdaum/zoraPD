package com.bdaum.zoom.cat.model.meta;

import java.util.*;

import java.lang.String;

import com.bdaum.zoom.cat.model.Meta_type;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset meta
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Meta extends IAsset, Meta_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property category
	 *
	 * @param _value - new element value
	 */
	public void setCategory(Map<String, Category> _value);

	/**
	 * Add an element to map category under key _element.getLabel()
	 *
	 * @param _element - the element to add
	 * @return - the previous element stored under that key
	 */
	public Category putCategory(Category _element);

	/**
	 * Remove an element from map category
	 *
	 * @param _key - the key of the element to remove
	 * @return - the previous element stored under that key
	 */
	public Category removeCategory(String _key);

	/**
	 * Make category empty 
	 */
	public void clearCategory();

	/**
	 * Get value of property category
	 *
	 * @return - value of field category
	 */
	public AomMap<String, Category> getCategory();

	/**
	 * Get single element of map category
	 *
	 * @param _key - the key of the element
	 * @return - the element belonging to the specified key/>
	 */
	public Category getCategory(String _key);

	/**
	 * Set value of property watchedFolder
	 *
	 * @param _value - new element value
	 */
	public void setWatchedFolder(Collection<String> _value);

	/**
	 * Set single element of list watchedFolder
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setWatchedFolder(String _value, int _i);

	/**
	 * Add an element to list watchedFolder
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addWatchedFolder(String _element);

	/**
	 * Remove an element from list watchedFolder
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeWatchedFolder(String _element);

	/**
	 * Make watchedFolder empty 
	 */
	public void clearWatchedFolder();

	/**
	 * Get value of property watchedFolder
	 *
	 * @return - value of field watchedFolder
	 */
	public AomList<String> getWatchedFolder();

	/**
	 * Get single element of list watchedFolder
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list watchedFolder
	 */
	public String getWatchedFolder(int _i);

	/**
	 * Set value of property lastDeviceImport
	 *
	 * @param _value - new element value
	 */
	public void setLastDeviceImport(Map<String, LastDeviceImport> _value);

	/**
	 * Add an element to map lastDeviceImport under key _element.getVolume()
	 *
	 * @param _element - the element to add
	 * @return - the previous element stored under that key
	 */
	public LastDeviceImport putLastDeviceImport(LastDeviceImport _element);

	/**
	 * Remove an element from map lastDeviceImport
	 *
	 * @param _key - the key of the element to remove
	 * @return - the previous element stored under that key
	 */
	public LastDeviceImport removeLastDeviceImport(String _key);

	/**
	 * Make lastDeviceImport empty 
	 */
	public void clearLastDeviceImport();

	/**
	 * Get value of property lastDeviceImport
	 *
	 * @return - value of field lastDeviceImport
	 */
	public AomMap<String, LastDeviceImport> getLastDeviceImport();

	/**
	 * Get single element of map lastDeviceImport
	 *
	 * @param _key - the key of the element
	 * @return - the element belonging to the specified key/>
	 */
	public LastDeviceImport getLastDeviceImport(String _key);

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

	/**
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException;

}
