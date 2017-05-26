package com.bdaum.zoom.cat.model.group.webGallery;

import com.bdaum.zoom.cat.model.group.Group;

import java.util.*;

import com.bdaum.zoom.cat.model.Font_type;

import java.lang.String;

import com.bdaum.zoom.cat.model.Rgb_type;

import com.bdaum.zoom.cat.model.WebGallery_type;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset webGallery
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface WebGallery extends IAsset, WebGallery_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property group_webGallery_parent
	 *
	 * @param _value - new element value
	 */
	public void setGroup_webGallery_parent(String _value);

	/**
	 * Get value of property group_webGallery_parent
	 *
	 * @return - value of field group_webGallery_parent
	 */
	public String getGroup_webGallery_parent();

	/**
	 * Set value of property storyboard
	 *
	 * @param _value - new element value
	 */
	public void setStoryboard(Collection<Storyboard> _value);

	/**
	 * Set single element of list storyboard
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setStoryboard(Storyboard _value, int _i);

	/**
	 * Add an element to list storyboard
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addStoryboard(Storyboard _element);

	/**
	 * Remove an element from list storyboard
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeStoryboard(Storyboard _element);

	/**
	 * Make storyboard empty 
	 */
	public void clearStoryboard();

	/**
	 * Get value of property storyboard
	 *
	 * @return - value of field storyboard
	 */
	public AomList<Storyboard> getStoryboard();

	/**
	 * Get single element of list storyboard
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list storyboard
	 */
	public Storyboard getStoryboard(int _i);

	/**
	 * Set value of property parameter
	 *
	 * @param _value - new element value
	 */
	public void setParameter(Map<String, WebParameter> _value);

	/**
	 * Add an element to map parameter under key _element.getId()
	 *
	 * @param _element - the element to add
	 * @return - the previous element stored under that key
	 */
	public WebParameter putParameter(WebParameter _element);

	/**
	 * Remove an element from map parameter
	 *
	 * @param _key - the key of the element to remove
	 * @return - the previous element stored under that key
	 */
	public WebParameter removeParameter(String _key);

	/**
	 * Make parameter empty 
	 */
	public void clearParameter();

	/**
	 * Get value of property parameter
	 *
	 * @return - value of field parameter
	 */
	public AomMap<String, WebParameter> getParameter();

	/**
	 * Get single element of map parameter
	 *
	 * @param _key - the key of the element
	 * @return - the element belonging to the specified key/>
	 */
	public WebParameter getParameter(String _key);

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
