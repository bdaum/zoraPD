package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset group
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Group_typeImpl extends AomObject implements Group_type {

	static final long serialVersionUID = -1571391332L;

	/* ----- Constructors ----- */

	public Group_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param system - Property
	 * @param showLabel - Property
	 * @param labelTemplate - Property
	 * @param fontSize - Property
	 * @param alignment - Property
	 * @param annotations - Property
	 */
	public Group_typeImpl(String name, boolean system, int showLabel,
			String labelTemplate, int fontSize, int alignment,
			String annotations) {
		super();
		this.name = name;
		this.system = system;
		this.showLabel = showLabel;
		this.labelTemplate = labelTemplate;
		this.fontSize = fontSize;
		this.alignment = alignment;
		this.annotations = annotations;

	}

	/* ----- Fields ----- */

	/* *** Property name *** */

	private String name = AomConstants.INIT_String;

	/**
	 * Set value of property name
	 *
	 * @param _value - new field value
	 */
	public void setName(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "name"));
		name = _value;
	}

	/**
	 * Get value of property name
	 *
	 * @return - value of field name
	 */
	public String getName() {
		return name;
	}

	/* *** Property system *** */

	private boolean system;

	/**
	 * Set value of property system
	 *
	 * @param _value - new field value
	 */
	public void setSystem(boolean _value) {
		system = _value;
	}

	/**
	 * Get value of property system
	 *
	 * @return - value of field system
	 */
	public boolean getSystem() {
		return system;
	}

	/* *** Property showLabel *** */

	private int showLabel;

	/**
	 * Set value of property showLabel
	 *
	 * @param _value - new field value
	 */
	public void setShowLabel(int _value) {
		showLabel = _value;
	}

	/**
	 * Get value of property showLabel
	 *
	 * @return - value of field showLabel
	 */
	public int getShowLabel() {
		return showLabel;
	}

	/* *** Property labelTemplate *** */

	private String labelTemplate;

	/**
	 * Set value of property labelTemplate
	 *
	 * @param _value - new field value
	 */
	public void setLabelTemplate(String _value) {
		labelTemplate = _value;
	}

	/**
	 * Get value of property labelTemplate
	 *
	 * @return - value of field labelTemplate
	 */
	public String getLabelTemplate() {
		return labelTemplate;
	}

	/* *** Property fontSize *** */

	private int fontSize;

	/**
	 * Set value of property fontSize
	 *
	 * @param _value - new field value
	 */
	public void setFontSize(int _value) {
		fontSize = _value;
	}

	/**
	 * Get value of property fontSize
	 *
	 * @return - value of field fontSize
	 */
	public int getFontSize() {
		return fontSize;
	}

	/* *** Property alignment *** */

	private int alignment;

	/**
	 * Set value of property alignment
	 *
	 * @param _value - new field value
	 */
	public void setAlignment(int _value) {
		alignment = _value;
	}

	/**
	 * Get value of property alignment
	 *
	 * @return - value of field alignment
	 */
	public int getAlignment() {
		return alignment;
	}

	/* *** Property annotations *** */

	private String annotations;

	/**
	 * Set value of property annotations
	 *
	 * @param _value - new field value
	 */
	public void setAnnotations(String _value) {
		annotations = _value;
	}

	/**
	 * Get value of property annotations
	 *
	 * @return - value of field annotations
	 */
	public String getAnnotations() {
		return annotations;
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

		if (!(o instanceof Group_type) || !super.equals(o))
			return false;
		Group_type other = (Group_type) o;
		return ((getName() == null && other.getName() == null) || (getName() != null && getName()
				.equals(other.getName())))

				&& getSystem() == other.getSystem()

				&& getShowLabel() == other.getShowLabel()

				&& ((getLabelTemplate() == null && other.getLabelTemplate() == null) || (getLabelTemplate() != null && getLabelTemplate()
						.equals(other.getLabelTemplate())))

				&& getFontSize() == other.getFontSize()

				&& getAlignment() == other.getAlignment()

				&& ((getAnnotations() == null && other.getAnnotations() == null) || (getAnnotations() != null && getAnnotations()
						.equals(other.getAnnotations())))

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

		int hashCode = 1487514854 + ((getName() == null) ? 0 : getName()
				.hashCode());

		hashCode = 31 * hashCode + (getSystem() ? 1231 : 1237);

		hashCode = 31 * hashCode + getShowLabel();

		hashCode = 31
				* hashCode
				+ ((getLabelTemplate() == null) ? 0 : getLabelTemplate()
						.hashCode());

		hashCode = 31 * hashCode + getFontSize();

		hashCode = 31 * hashCode + getAlignment();

		hashCode = 31
				* hashCode
				+ ((getAnnotations() == null) ? 0 : getAnnotations().hashCode());

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

		if (name == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "name"));

	}

}
