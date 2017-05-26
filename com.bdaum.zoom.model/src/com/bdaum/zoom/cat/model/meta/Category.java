package com.bdaum.zoom.cat.model.meta;

import java.util.*;

import com.bdaum.zoom.cat.model.Category_type;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset category
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Category extends Category_type, IAsset {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property meta_parent
	 *
	 * @param _value - new element value
	 */
	public void setMeta_parent(Meta _value);

	/**
	 * Get value of property meta_parent
	 *
	 * @return - value of field meta_parent
	 */
	public Meta getMeta_parent();

	/**
	 * Set value of property category_subCategory_parent
	 *
	 * @param _value - new element value
	 */
	public void setCategory_subCategory_parent(Category _value);

	/**
	 * Get value of property category_subCategory_parent
	 *
	 * @return - value of field category_subCategory_parent
	 */
	public Category getCategory_subCategory_parent();

	/**
	 * Set value of property subCategory
	 *
	 * @param _value - new element value
	 */
	public void setSubCategory(Map<String, Category> _value);

	/**
	 * Add an element to map subCategory under key _element.getLabel()
	 *
	 * @param _element - the element to add
	 * @return - the previous element stored under that key
	 */
	public Category putSubCategory(Category _element);

	/**
	 * Remove an element from map subCategory
	 *
	 * @param _key - the key of the element to remove
	 * @return - the previous element stored under that key
	 */
	public Category removeSubCategory(String _key);

	/**
	 * Make subCategory empty 
	 */
	public void clearSubCategory();

	/**
	 * Get value of property subCategory
	 *
	 * @return - value of field subCategory
	 */
	public AomMap<String, Category> getSubCategory();

	/**
	 * Get single element of map subCategory
	 *
	 * @param _key - the key of the element
	 * @return - the element belonging to the specified key/>
	 */
	public Category getSubCategory(String _key);

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
