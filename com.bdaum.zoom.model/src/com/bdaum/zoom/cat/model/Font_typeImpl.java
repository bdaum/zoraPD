package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset font
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Font_typeImpl extends AomObject implements Font_type {

	static final long serialVersionUID = -1103933300L;

	/* ----- Constructors ----- */

	public Font_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param size - Property
	 * @param style - Property
	 * @param weight - Property
	 * @param variant - Property
	 * @param color - Property
	 */
	public Font_typeImpl(int size, int style, int weight, int variant,
			Rgb_type color) {
		super();
		this.size = size;
		this.style = style;
		this.weight = weight;
		this.variant = variant;
		this.color = color;

	}

	/* ----- Fields ----- */

	/* *** Property family *** */

	private String[] family = new String[0];

	/**
	 * Set value of property family
	 *
	 * @param _value - new element value
	 */
	public void setFamily(String[] _value) {
		family = _value;
	}

	/**
	 * Set single element of array family
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setFamily(String _element, int _i) {
		family[_i] = _element;
	}

	/**
	 * Get value of property family
	 *
	 * @return - value of field family
	 */
	public String[] getFamily() {
		return family;
	}

	/**
	 * Get single element of array family
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array family
	 */
	public String getFamily(int _i) {
		return family[_i];
	}

	/* *** Property size *** */

	private int size;

	/**
	 * Set value of property size
	 *
	 * @param _value - new field value
	 */
	public void setSize(int _value) {
		size = _value;
	}

	/**
	 * Get value of property size
	 *
	 * @return - value of field size
	 */
	public int getSize() {
		return size;
	}

	/* *** Property style *** */

	private int style;

	/**
	 * Set value of property style
	 *
	 * @param _value - new field value
	 */
	public void setStyle(int _value) {
		style = _value;
	}

	/**
	 * Get value of property style
	 *
	 * @return - value of field style
	 */
	public int getStyle() {
		return style;
	}

	/* *** Property weight *** */

	private int weight;

	/**
	 * Set value of property weight
	 *
	 * @param _value - new field value
	 */
	public void setWeight(int _value) {
		weight = _value;
	}

	/**
	 * Get value of property weight
	 *
	 * @return - value of field weight
	 */
	public int getWeight() {
		return weight;
	}

	/* *** Property variant *** */

	private int variant;

	/**
	 * Set value of property variant
	 *
	 * @param _value - new field value
	 */
	public void setVariant(int _value) {
		variant = _value;
	}

	/**
	 * Get value of property variant
	 *
	 * @return - value of field variant
	 */
	public int getVariant() {
		return variant;
	}

	/* *** Property color *** */

	private Rgb_type color;

	/**
	 * Set value of property color
	 *
	 * @param _value - new field value
	 */
	public void setColor(Rgb_type _value) {
		color = _value;
	}

	/**
	 * Get value of property color
	 *
	 * @return - value of field color
	 */
	public Rgb_type getColor() {
		return color;
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

		if (!(o instanceof Font_type) || !super.equals(o))
			return false;
		Font_type other = (Font_type) o;
		return ((getFamily() == null && other.getFamily() == null) || (getFamily() != null && getFamily()
				.equals(other.getFamily())))

				&& getSize() == other.getSize()

				&& getStyle() == other.getStyle()

				&& getWeight() == other.getWeight()

				&& getVariant() == other.getVariant()

				&& ((getColor() == null && other.getColor() == null) || (getColor() != null && getColor()
						.equals(other.getColor())))

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

		int hashCode = -1201155338
				+ ((getFamily() == null) ? 0 : getFamily().hashCode());

		hashCode = 31 * hashCode + getSize();

		hashCode = 31 * hashCode + getStyle();

		hashCode = 31 * hashCode + getWeight();

		hashCode = 31 * hashCode + getVariant();

		hashCode = 31 * hashCode
				+ ((getColor() == null) ? 0 : getColor().hashCode());

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

	}

}
