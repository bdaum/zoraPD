package com.bdaum.zoom.cat.model.group;

import com.bdaum.zoom.cat.model.SmartCollection_type;

import java.util.*;

import com.bdaum.zoom.cat.model.asset.Region;

import com.bdaum.zoom.cat.model.asset.Asset;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset smartCollection
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface SmartCollection extends IAsset, SmartCollection_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property region_album_parent
	 *
	 * @param _value - new element value
	 */
	public void setRegion_album_parent(String _value);

	/**
	 * Get value of property region_album_parent
	 *
	 * @return - value of field region_album_parent
	 */
	public String getRegion_album_parent();

	/**
	 * Set value of property smartCollection_subSelection_parent
	 *
	 * @param _value - new element value
	 */
	public void setSmartCollection_subSelection_parent(SmartCollection _value);

	/**
	 * Get value of property smartCollection_subSelection_parent
	 *
	 * @return - value of field smartCollection_subSelection_parent
	 */
	public SmartCollection getSmartCollection_subSelection_parent();

	/**
	 * Set value of property group_rootCollection_parent
	 *
	 * @param _value - new element value
	 */
	public void setGroup_rootCollection_parent(String _value);

	/**
	 * Get value of property group_rootCollection_parent
	 *
	 * @return - value of field group_rootCollection_parent
	 */
	public String getGroup_rootCollection_parent();

	/**
	 * Set value of property subSelection
	 *
	 * @param _value - new element value
	 */
	public void setSubSelection(Collection<SmartCollection> _value);

	/**
	 * Set single element of list subSelection
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setSubSelection(SmartCollection _value, int _i);

	/**
	 * Add an element to list subSelection
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addSubSelection(SmartCollection _element);

	/**
	 * Remove an element from list subSelection
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeSubSelection(SmartCollection _element);

	/**
	 * Make subSelection empty 
	 */
	public void clearSubSelection();

	/**
	 * Get value of property subSelection
	 *
	 * @return - value of field subSelection
	 */
	public AomList<SmartCollection> getSubSelection();

	/**
	 * Get single element of list subSelection
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list subSelection
	 */
	public SmartCollection getSubSelection(int _i);

	/**
	 * Set value of property criterion
	 *
	 * @param _value - new element value
	 */
	public void setCriterion(Collection<Criterion> _value);

	/**
	 * Set single element of list criterion
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setCriterion(Criterion _value, int _i);

	/**
	 * Add an element to list criterion
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addCriterion(Criterion _element);

	/**
	 * Remove an element from list criterion
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeCriterion(Criterion _element);

	/**
	 * Make criterion empty 
	 */
	public void clearCriterion();

	/**
	 * Get value of property criterion
	 *
	 * @return - value of field criterion
	 */
	public AomList<Criterion> getCriterion();

	/**
	 * Get single element of list criterion
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list criterion
	 */
	public Criterion getCriterion(int _i);

	/**
	 * Set value of property sortCriterion
	 *
	 * @param _value - new element value
	 */
	public void setSortCriterion(Collection<SortCriterion> _value);

	/**
	 * Set single element of list sortCriterion
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setSortCriterion(SortCriterion _value, int _i);

	/**
	 * Add an element to list sortCriterion
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addSortCriterion(SortCriterion _element);

	/**
	 * Remove an element from list sortCriterion
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeSortCriterion(SortCriterion _element);

	/**
	 * Make sortCriterion empty 
	 */
	public void clearSortCriterion();

	/**
	 * Get value of property sortCriterion
	 *
	 * @return - value of field sortCriterion
	 */
	public AomList<SortCriterion> getSortCriterion();

	/**
	 * Get single element of list sortCriterion
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list sortCriterion
	 */
	public SortCriterion getSortCriterion(int _i);

	/**
	 * Set value of property postProcessor
	 *
	 * @param _value - new element value
	 */
	public void setPostProcessor(PostProcessor _value);

	/**
	 * Get value of property postProcessor
	 *
	 * @return - value of field postProcessor
	 */
	public PostProcessor getPostProcessor();

	/**
	 * Set value of property asset
	 *
	 * @param _value - new element value
	 */
	public void setAsset(Collection<String> _value);

	/**
	 * Set single element of list asset
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setAsset(String _value, int _i);

	/**
	 * Add an element to list asset
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addAsset(String _element);

	/**
	 * Remove an element from list asset
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeAsset(String _element);

	/**
	 * Make asset empty 
	 */
	public void clearAsset();

	/**
	 * Get value of property asset
	 *
	 * @return - value of field asset
	 */
	public AomList<String> getAsset();

	/**
	 * Get single element of list asset
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list asset
	 */
	public String getAsset(int _i);

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
