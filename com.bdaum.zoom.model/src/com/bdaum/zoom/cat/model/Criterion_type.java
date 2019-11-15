package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset criterion
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Criterion_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property field
	 *
	 * @param _value - new element value
	 */
	public void setField(String _value);

	/**
	 * Get value of property field
	 *
	 * @return - value of field field
	 */
	public String getField();

	/**
	 * Set value of property subfield
	 *
	 * @param _value - new element value
	 */
	public void setSubfield(String _value);

	/**
	 * Get value of property subfield
	 *
	 * @return - value of field subfield
	 */
	public String getSubfield();

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
	 * Set value of property to
	 *
	 * @param _value - new element value
	 */
	public void setTo(Object _value);

	/**
	 * Get value of property to
	 *
	 * @return - value of field to
	 */
	public Object getTo();

	/**
	 * Set value of property relation
	 *
	 * @param _value - new element value
	 */
	public void setRelation(int _value);

	/**
	 * Get value of property relation
	 *
	 * @return - value of field relation
	 */
	public int getRelation();

	/**
	 * Set value of property and
	 *
	 * @param _value - new element value
	 */
	public void setAnd(boolean _value);

	/**
	 * Get value of property and
	 *
	 * @return - value of field and
	 */
	public boolean getAnd();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
