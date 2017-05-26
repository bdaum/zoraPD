package com.bdaum.zoom.cat.model.group;

import com.bdaum.zoom.cat.model.Group_type;

import java.util.*;

import com.bdaum.zoom.cat.model.group.webGallery.WebGallery;

import com.bdaum.zoom.cat.model.group.slideShow.SlideShow;

import com.bdaum.zoom.cat.model.group.exhibition.Exhibition;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset group
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Group extends Group_type, IAsset {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property group_subgroup_parent
	 *
	 * @param _value - new element value
	 */
	public void setGroup_subgroup_parent(Group _value);

	/**
	 * Get value of property group_subgroup_parent
	 *
	 * @return - value of field group_subgroup_parent
	 */
	public Group getGroup_subgroup_parent();

	/**
	 * Set value of property rootCollection
	 *
	 * @param _value - new element value
	 */
	public void setRootCollection(Collection<String> _value);

	/**
	 * Set single element of list rootCollection
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setRootCollection(String _value, int _i);

	/**
	 * Add an element to list rootCollection
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addRootCollection(String _element);

	/**
	 * Remove an element from list rootCollection
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeRootCollection(String _element);

	/**
	 * Make rootCollection empty 
	 */
	public void clearRootCollection();

	/**
	 * Get value of property rootCollection
	 *
	 * @return - value of field rootCollection
	 */
	public AomList<String> getRootCollection();

	/**
	 * Get single element of list rootCollection
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list rootCollection
	 */
	public String getRootCollection(int _i);

	/**
	 * Set value of property slideshow
	 *
	 * @param _value - new element value
	 */
	public void setSlideshow(Collection<String> _value);

	/**
	 * Set single element of list slideshow
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setSlideshow(String _value, int _i);

	/**
	 * Add an element to list slideshow
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addSlideshow(String _element);

	/**
	 * Remove an element from list slideshow
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeSlideshow(String _element);

	/**
	 * Make slideshow empty 
	 */
	public void clearSlideshow();

	/**
	 * Get value of property slideshow
	 *
	 * @return - value of field slideshow
	 */
	public AomList<String> getSlideshow();

	/**
	 * Get single element of list slideshow
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list slideshow
	 */
	public String getSlideshow(int _i);

	/**
	 * Set value of property exhibition
	 *
	 * @param _value - new element value
	 */
	public void setExhibition(Collection<String> _value);

	/**
	 * Set single element of list exhibition
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setExhibition(String _value, int _i);

	/**
	 * Add an element to list exhibition
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addExhibition(String _element);

	/**
	 * Remove an element from list exhibition
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeExhibition(String _element);

	/**
	 * Make exhibition empty 
	 */
	public void clearExhibition();

	/**
	 * Get value of property exhibition
	 *
	 * @return - value of field exhibition
	 */
	public AomList<String> getExhibition();

	/**
	 * Get single element of list exhibition
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list exhibition
	 */
	public String getExhibition(int _i);

	/**
	 * Set value of property webGallery
	 *
	 * @param _value - new element value
	 */
	public void setWebGallery(Collection<String> _value);

	/**
	 * Set single element of list webGallery
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setWebGallery(String _value, int _i);

	/**
	 * Add an element to list webGallery
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addWebGallery(String _element);

	/**
	 * Remove an element from list webGallery
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeWebGallery(String _element);

	/**
	 * Make webGallery empty 
	 */
	public void clearWebGallery();

	/**
	 * Get value of property webGallery
	 *
	 * @return - value of field webGallery
	 */
	public AomList<String> getWebGallery();

	/**
	 * Get single element of list webGallery
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list webGallery
	 */
	public String getWebGallery(int _i);

	/**
	 * Set value of property subgroup
	 *
	 * @param _value - new element value
	 */
	public void setSubgroup(Collection<Group> _value);

	/**
	 * Set single element of list subgroup
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setSubgroup(Group _value, int _i);

	/**
	 * Add an element to list subgroup
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addSubgroup(Group _element);

	/**
	 * Remove an element from list subgroup
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeSubgroup(Group _element);

	/**
	 * Make subgroup empty 
	 */
	public void clearSubgroup();

	/**
	 * Get value of property subgroup
	 *
	 * @return - value of field subgroup
	 */
	public AomList<Group> getSubgroup();

	/**
	 * Get single element of list subgroup
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list subgroup
	 */
	public Group getSubgroup(int _i);

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
