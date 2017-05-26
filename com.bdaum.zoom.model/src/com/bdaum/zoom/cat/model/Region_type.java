package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset region
 *
 * StringID is the hexadecimal representation of 4 16-bit parts (x,y,width,height). Each part is called to the maximum unsigned binary 16-bit number.
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Region_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property keywordAdded
	 *
	 * @param _value - new element value
	 */
	public void setKeywordAdded(boolean _value);

	/**
	 * Get value of property keywordAdded
	 *
	 * @return - value of field keywordAdded
	 */
	public boolean getKeywordAdded();

	/**
	 * Set value of property personEmailDigest
	 *
	 * @param _value - new element value
	 */
	public void setPersonEmailDigest(String _value);

	/**
	 * Get value of property personEmailDigest
	 *
	 * @return - value of field personEmailDigest
	 */
	public String getPersonEmailDigest();

	/**
	 * Set value of property personLiveCID
	 *
	 * @param _value - new element value
	 */
	public void setPersonLiveCID(Long _value);

	/**
	 * Get value of property personLiveCID
	 *
	 * @return - value of field personLiveCID
	 */
	public Long getPersonLiveCID();

	/**
	 * Set value of property description
	 *
	 * @param _value - new element value
	 */
	public void setDescription(String _value);

	/**
	 * Get value of property description
	 *
	 * @return - value of field description
	 */
	public String getDescription();

	public static final String type_face = "Face";
	public static final String type_pet = "Pet";
	public static final String type_focus = "Focus";
	public static final String type_barCode = "BarCode";

	public static final String[] typeALLVALUES = new String[] { type_face,
			type_pet, type_focus, type_barCode };

	/**
	 * Set value of property type
	 *
	 * @param _value - new element value
	 */
	public void setType(String _value);

	/**
	 * Get value of property type
	 *
	 * @return - value of field type
	 */
	public String getType();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
