package com.bdaum.zoom.cat.model.group.webGallery;

import java.util.*;

import com.bdaum.zoom.cat.model.WebParameter_type;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset webParameter
 *
 * <b>id</b> is composed of namespace.localID
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface WebParameter extends IAsset, WebParameter_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property webGallery_parameter_parent
	 *
	 * @param _value - new element value
	 */
	public void setWebGallery_parameter_parent(WebGallery _value);

	/**
	 * Get value of property webGallery_parameter_parent
	 *
	 * @return - value of field webGallery_parameter_parent
	 */
	public WebGallery getWebGallery_parameter_parent();

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
