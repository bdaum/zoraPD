package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset webParameter
 *
 * <b>id</b> is composed of namespace.localID
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface WebParameter_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property id
	 *
	 * @param _value - new element value
	 */
	public void setId(String _value);

	/**
	 * Get value of property id
	 *
	 * @return - value of field id
	 */
	public String getId();

	/**
	 * Set value of property value
	 *
	 * @param _value - new element value
	 */
	public void setValue(Object _value);

	/**
	 * Get value of property value
	 *
	 * @return - value of field value
	 */
	public Object getValue();

	/**
	 * Set value of property encodeHtml
	 *
	 * @param _value - new element value
	 */
	public void setEncodeHtml(boolean _value);

	/**
	 * Get value of property encodeHtml
	 *
	 * @return - value of field encodeHtml
	 */
	public boolean getEncodeHtml();

	/**
	 * Set value of property linkTo
	 *
	 * @param _value - new element value
	 */
	public void setLinkTo(String _value);

	/**
	 * Get value of property linkTo
	 *
	 * @return - value of field linkTo
	 */
	public String getLinkTo();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
