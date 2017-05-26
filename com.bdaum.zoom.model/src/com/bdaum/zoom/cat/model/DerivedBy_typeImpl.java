package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset derivedBy
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class DerivedBy_typeImpl extends AomObject implements DerivedBy_type {

	static final long serialVersionUID = -310217881L;

	/* ----- Constructors ----- */

	public DerivedBy_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param recipe - Property
	 * @param parameterFile - Property
	 * @param tool - Property
	 * @param date - Property
	 */
	public DerivedBy_typeImpl(String recipe, String parameterFile, String tool,
			Date date) {
		super();
		this.recipe = recipe;
		this.parameterFile = parameterFile;
		this.tool = tool;
		this.date = date;

	}

	/* ----- Fields ----- */

	/* *** Property recipe *** */

	private String recipe;

	/**
	 * Set value of property recipe
	 *
	 * @param _value - new field value
	 */
	public void setRecipe(String _value) {
		recipe = _value;
	}

	/**
	 * Get value of property recipe
	 *
	 * @return - value of field recipe
	 */
	public String getRecipe() {
		return recipe;
	}

	/* *** Property parameterFile *** */

	private String parameterFile;

	/**
	 * Set value of property parameterFile
	 *
	 * @param _value - new field value
	 */
	public void setParameterFile(String _value) {
		parameterFile = _value;
	}

	/**
	 * Get value of property parameterFile
	 *
	 * @return - value of field parameterFile
	 */
	public String getParameterFile() {
		return parameterFile;
	}

	/* *** Property archivedRecipe *** */

	private byte[] archivedRecipe = new byte[0];

	/**
	 * Set value of property archivedRecipe
	 *
	 * @param _value - new element value
	 */
	public void setArchivedRecipe(byte[] _value) {
		archivedRecipe = _value;
	}

	/**
	 * Set single element of array archivedRecipe
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setArchivedRecipe(byte _element, int _i) {
		archivedRecipe[_i] = _element;
	}

	/**
	 * Get value of property archivedRecipe
	 *
	 * @return - value of field archivedRecipe
	 */
	public byte[] getArchivedRecipe() {
		return archivedRecipe;
	}

	/**
	 * Get single element of array archivedRecipe
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array archivedRecipe
	 */
	public byte getArchivedRecipe(int _i) {
		return archivedRecipe[_i];
	}

	/* *** Property tool *** */

	private String tool;

	/**
	 * Set value of property tool
	 *
	 * @param _value - new field value
	 */
	public void setTool(String _value) {
		tool = _value;
	}

	/**
	 * Get value of property tool
	 *
	 * @return - value of field tool
	 */
	public String getTool() {
		return tool;
	}

	/* *** Property date *** */

	private Date date = new Date();

	/**
	 * Set value of property date
	 *
	 * @param _value - new field value
	 */
	public void setDate(Date _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "date"));
		date = _value;
	}

	/**
	 * Get value of property date
	 *
	 * @return - value of field date
	 */
	public Date getDate() {
		return date;
	}

	/* ----- Equality ----- */

	/**
	 * Creates a clone of this object.

	 *   Not supported in this class
	 * @throws CloneNotSupportedException;
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException {

		if (date == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "date"));

	}

}
