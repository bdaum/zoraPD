package com.bdaum.zoom.cat.model.asset;

import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;

import com.bdaum.zoom.cat.model.locationShown.LocationShown;

import com.bdaum.zoom.cat.model.Asset_type;

import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContact;

import com.bdaum.zoom.cat.model.derivedBy.DerivedBy;

import com.bdaum.zoom.cat.model.group.slideShow.Slide;

import com.bdaum.aoModeling.runtime.*;

import com.bdaum.zoom.cat.model.composedTo.ComposedTo;

import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShown;

import com.bdaum.zoom.cat.model.group.exhibition.Exhibit;

import java.util.*;

import com.bdaum.zoom.cat.model.group.SmartCollection;

import com.bdaum.zoom.cat.model.locationCreated.LocationCreated;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset asset
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Asset extends Asset_type, IAsset {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property smartCollection_parent
	 *
	 * @param _value - new element value
	 */
	public void setSmartCollection_parent(String _value);

	/**
	 * Get value of property smartCollection_parent
	 *
	 * @return - value of field smartCollection_parent
	 */
	public String getSmartCollection_parent();

	/**
	 * Set value of property slide_asset_parent
	 *
	 * @param _value - new element value
	 */
	public void setSlide_asset_parent(String _value);

	/**
	 * Get value of property slide_asset_parent
	 *
	 * @return - value of field slide_asset_parent
	 */
	public String getSlide_asset_parent();

	/**
	 * Set value of property exhibit_asset_parent
	 *
	 * @param _value - new element value
	 */
	public void setExhibit_asset_parent(String _value);

	/**
	 * Get value of property exhibit_asset_parent
	 *
	 * @return - value of field exhibit_asset_parent
	 */
	public String getExhibit_asset_parent();

	/**
	 * Set value of property webExhibit_asset_parent
	 *
	 * @param _value - new element value
	 */
	public void setWebExhibit_asset_parent(String _value);

	/**
	 * Get value of property webExhibit_asset_parent
	 *
	 * @return - value of field webExhibit_asset_parent
	 */
	public String getWebExhibit_asset_parent();

	/**
	 * Set value of property derivedBy_derivative_parent
	 *
	 * @param _value - new element value
	 */
	public void setDerivedBy_derivative_parent(String _value);

	/**
	 * Get value of property derivedBy_derivative_parent
	 *
	 * @return - value of field derivedBy_derivative_parent
	 */
	public String getDerivedBy_derivative_parent();

	/**
	 * Set value of property derivedBy_original_parent
	 *
	 * @param _value - new element value
	 */
	public void setDerivedBy_original_parent(String _value);

	/**
	 * Get value of property derivedBy_original_parent
	 *
	 * @return - value of field derivedBy_original_parent
	 */
	public String getDerivedBy_original_parent();

	/**
	 * Set value of property composedTo_component_parent
	 *
	 * @param _value - new element value
	 */
	public void setComposedTo_component_parent(String _value);

	/**
	 * Get value of property composedTo_component_parent
	 *
	 * @return - value of field composedTo_component_parent
	 */
	public String getComposedTo_component_parent();

	/**
	 * Set value of property composedTo_composite_parent
	 *
	 * @param _value - new element value
	 */
	public void setComposedTo_composite_parent(String _value);

	/**
	 * Get value of property composedTo_composite_parent
	 *
	 * @return - value of field composedTo_composite_parent
	 */
	public String getComposedTo_composite_parent();

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

	/**
	 * Set value of property creatorsContact_parent
	 *
	 * @param _value - new element value
	 */
	public void setCreatorsContact_parent(String _value);

	/**
	 * Get value of property creatorsContact_parent
	 *
	 * @return - value of field creatorsContact_parent
	 */
	public String getCreatorsContact_parent();

	/**
	 * Set value of property locationCreated_parent
	 *
	 * @param _value - new element value
	 */
	public void setLocationCreated_parent(String _value);

	/**
	 * Get value of property locationCreated_parent
	 *
	 * @return - value of field locationCreated_parent
	 */
	public String getLocationCreated_parent();

	/**
	 * Set value of property locationShown_parent
	 *
	 * @param _value - new element value
	 */
	public void setLocationShown_parent(Collection<String> _value);

	/**
	 * Set single element of list locationShown_parent
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setLocationShown_parent(String _value, int _i);

	/**
	 * Add an element to list locationShown_parent
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addLocationShown_parent(String _element);

	/**
	 * Remove an element from list locationShown_parent
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeLocationShown_parent(String _element);

	/**
	 * Make locationShown_parent empty 
	 */
	public void clearLocationShown_parent();

	/**
	 * Get value of property locationShown_parent
	 *
	 * @return - value of field locationShown_parent
	 */
	public AomList<String> getLocationShown_parent();

	/**
	 * Get single element of list locationShown_parent
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list locationShown_parent
	 */
	public String getLocationShown_parent(int _i);

	/**
	 * Set value of property track
	 *
	 * @param _value - new element value
	 */
	public void setTrack(String[] _value);

	/**
	 * Set single element of array track
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setTrack(String _element, int _i);

	/**
	 * Get value of property track
	 *
	 * @return - value of field track
	 */
	public String[] getTrack();

	/**
	 * Get single element of array track
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array track
	 */
	public String getTrack(int _i);

	/**
	 * Set value of property person
	 *
	 * @param _value - new element value
	 */
	public void setPerson(String[] _value);

	/**
	 * Set single element of array person
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setPerson(String _element, int _i);

	/**
	 * Get value of property person
	 *
	 * @return - value of field person
	 */
	public String[] getPerson();

	/**
	 * Get single element of array person
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array person
	 */
	public String getPerson(int _i);

	/**
	 * Set value of property mediaExtension
	 *
	 * @param _value - new element value
	 */
	public void setMediaExtension(MediaExtension[] _value);

	/**
	 * Set single element of array mediaExtension
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setMediaExtension(MediaExtension _element, int _i);

	/**
	 * Get value of property mediaExtension
	 *
	 * @return - value of field mediaExtension
	 */
	public MediaExtension[] getMediaExtension();

	/**
	 * Get single element of array mediaExtension
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array mediaExtension
	 */
	public MediaExtension getMediaExtension(int _i);

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
