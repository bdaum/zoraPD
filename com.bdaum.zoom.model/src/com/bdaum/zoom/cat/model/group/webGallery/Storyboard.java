package com.bdaum.zoom.cat.model.group.webGallery;

import com.bdaum.zoom.cat.model.Storyboard_type;

import java.util.*;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset storyboard
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Storyboard extends IAsset, Storyboard_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property webGallery_storyboard_parent
	 *
	 * @param _value - new element value
	 */
	public void setWebGallery_storyboard_parent(WebGallery _value);

	/**
	 * Get value of property webGallery_storyboard_parent
	 *
	 * @return - value of field webGallery_storyboard_parent
	 */
	public WebGallery getWebGallery_storyboard_parent();

	/**
	 * Set value of property exhibit
	 *
	 * @param _value - new element value
	 */
	public void setExhibit(Collection<String> _value);

	/**
	 * Set single element of list exhibit
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setExhibit(String _value, int _i);

	/**
	 * Add an element to list exhibit
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addExhibit(String _element);

	/**
	 * Remove an element from list exhibit
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeExhibit(String _element);

	/**
	 * Make exhibit empty 
	 */
	public void clearExhibit();

	/**
	 * Get value of property exhibit
	 *
	 * @return - value of field exhibit
	 */
	public AomList<String> getExhibit();

	/**
	 * Get single element of list exhibit
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list exhibit
	 */
	public String getExhibit(int _i);

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
