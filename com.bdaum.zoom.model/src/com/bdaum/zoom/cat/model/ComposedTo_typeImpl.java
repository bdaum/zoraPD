package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset composedTo
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class ComposedTo_typeImpl extends AomObject implements ComposedTo_type {

	static final long serialVersionUID = -360218770L;

	/* ----- Constructors ----- */

	public ComposedTo_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param type - Property
	 * @param recipe - Property
	 * @param parameterFile - Property
	 * @param tool - Property
	 * @param date - Property
	 */
	public ComposedTo_typeImpl(String type, String recipe,
			String parameterFile, String tool, Date date) {
		super();
		this.type = type;
		this.recipe = recipe;
		this.parameterFile = parameterFile;
		this.tool = tool;
		this.date = date;

	}

	/* ----- Fields ----- */

	/* *** Property type *** */

	private String type = AomConstants.INIT_String;

	/**
	 * Set value of property type
	 *
	 * @param _value - new field value
	 */
	public void setType(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "type"));
		type = _value;
	}

	/**
	 * Get value of property type
	 *
	 * @return - value of field type
	 */
	public String getType() {
		return type;
	}

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
	 * Compares the specified object with this object for equality.
	 *
	 * @param o the object to be compared with this object.
	 * @return true if the specified object is equal to this object.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof ComposedTo_type) || !super.equals(o))
			return false;
		ComposedTo_type other = (ComposedTo_type) o;
		return ((getType() == null && other.getType() == null) || (getType() != null && getType()
				.equals(other.getType())))

				&& ((getRecipe() == null && other.getRecipe() == null) || (getRecipe() != null && getRecipe()
						.equals(other.getRecipe())))

				&& ((getParameterFile() == null && other.getParameterFile() == null) || (getParameterFile() != null && getParameterFile()
						.equals(other.getParameterFile())))

				&& ((getTool() == null && other.getTool() == null) || (getTool() != null && getTool()
						.equals(other.getTool())))

				&& ((getDate() == null && other.getDate() == null) || (getDate() != null && getDate()
						.equals(other.getDate())))

		;
	}

	/**
	 * Returns the hash code for this object.
	 * @return the hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 * @see java.lang.Object#equals(Object)
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {

		int hashCode = 379158612 + ((getType() == null) ? 0 : getType()
				.hashCode());

		hashCode = 31 * hashCode
				+ ((getRecipe() == null) ? 0 : getRecipe().hashCode());

		hashCode = 31
				* hashCode
				+ ((getParameterFile() == null) ? 0 : getParameterFile()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getTool() == null) ? 0 : getTool().hashCode());

		hashCode = 31 * hashCode
				+ ((getDate() == null) ? 0 : getDate().hashCode());

		return hashCode;
	}

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

		if (type == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "type"));

		if (date == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "date"));

	}

}
