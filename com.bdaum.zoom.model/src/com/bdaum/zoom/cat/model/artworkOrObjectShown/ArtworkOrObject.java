package com.bdaum.zoom.cat.model.artworkOrObjectShown;

import com.bdaum.zoom.cat.model.ArtworkOrObject_type;

import java.util.*;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset artworkOrObject
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface ArtworkOrObject extends ArtworkOrObject_type, IAsset {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property artworkOrObjectShown_parent
	 *
	 * @param _value - new element value
	 */
	public void setArtworkOrObjectShown_parent(Collection<String> _value);

	/**
	 * Set single element of list artworkOrObjectShown_parent
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setArtworkOrObjectShown_parent(String _value, int _i);

	/**
	 * Add an element to list artworkOrObjectShown_parent
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addArtworkOrObjectShown_parent(String _element);

	/**
	 * Remove an element from list artworkOrObjectShown_parent
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeArtworkOrObjectShown_parent(String _element);

	/**
	 * Make artworkOrObjectShown_parent empty 
	 */
	public void clearArtworkOrObjectShown_parent();

	/**
	 * Get value of property artworkOrObjectShown_parent
	 *
	 * @return - value of field artworkOrObjectShown_parent
	 */
	public AomList<String> getArtworkOrObjectShown_parent();

	/**
	 * Get single element of list artworkOrObjectShown_parent
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list artworkOrObjectShown_parent
	 */
	public String getArtworkOrObjectShown_parent(int _i);

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
