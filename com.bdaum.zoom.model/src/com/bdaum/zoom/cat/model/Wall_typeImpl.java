package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset wall
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Wall_typeImpl extends AomObject implements Wall_type {

	static final long serialVersionUID = -2732931727L;

	/* ----- Constructors ----- */

	public Wall_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param location - Property
	 * @param x - Property
	 * @param y - Property
	 * @param width - Property
	 * @param height - Property
	 * @param gX - Property
	 * @param gY - Property
	 * @param gAngle - Property
	 * @param color - Property
	 */
	public Wall_typeImpl(String location, int x, int y, int width, int height,
			int gX, int gY, double gAngle, Rgb_type color) {
		super();
		this.location = location;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gX = gX;
		this.gY = gY;
		this.gAngle = gAngle;
		this.color = color;

	}

	/* ----- Fields ----- */

	/* *** Property location *** */

	private String location = AomConstants.INIT_String;

	/**
	 * Set value of property location
	 *
	 * @param _value - new field value
	 */
	public void setLocation(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "location"));
		location = _value;
	}

	/**
	 * Get value of property location
	 *
	 * @return - value of field location
	 */
	public String getLocation() {
		return location;
	}

	/* *** Property x(unit=mm) *** */

	public static final String x__unit = "mm";

	private int x;

	/**
	 * Set value of property x
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setX(int _value) {
		x = _value;
	}

	/**
	 * Get value of property x
	 *
	 * @return - value of field x(unit=mm)
	 */
	public int getX() {
		return x;
	}

	/* *** Property y(unit=mm) *** */

	public static final String y__unit = "mm";

	private int y;

	/**
	 * Set value of property y
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setY(int _value) {
		y = _value;
	}

	/**
	 * Get value of property y
	 *
	 * @return - value of field y(unit=mm)
	 */
	public int getY() {
		return y;
	}

	/* *** Property width(unit=mm) *** */

	public static final String width__unit = "mm";

	private int width;

	/**
	 * Set value of property width
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setWidth(int _value) {
		width = _value;
	}

	/**
	 * Get value of property width
	 *
	 * @return - value of field width(unit=mm)
	 */
	public int getWidth() {
		return width;
	}

	/* *** Property height(unit=mm) *** */

	public static final String height__unit = "mm";

	private int height;

	/**
	 * Set value of property height
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setHeight(int _value) {
		height = _value;
	}

	/**
	 * Get value of property height
	 *
	 * @return - value of field height(unit=mm)
	 */
	public int getHeight() {
		return height;
	}

	/* *** Property gX(unit=mm) *** */

	public static final String gX__unit = "mm";

	private int gX;

	/**
	 * Set value of property gX
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setGX(int _value) {
		gX = _value;
	}

	/**
	 * Get value of property gX
	 *
	 * @return - value of field gX(unit=mm)
	 */
	public int getGX() {
		return gX;
	}

	/* *** Property gY(unit=mm) *** */

	public static final String gY__unit = "mm";

	private int gY;

	/**
	 * Set value of property gY
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setGY(int _value) {
		gY = _value;
	}

	/**
	 * Get value of property gY
	 *
	 * @return - value of field gY(unit=mm)
	 */
	public int getGY() {
		return gY;
	}

	/* *** Property gAngle(unit=degree) *** */

	public static final String gAngle__unit = "degree";

	private double gAngle;

	/**
	 * Set value of property gAngle
	 *
	 * @param _value - new field value(unit=degree)
	 */
	public void setGAngle(double _value) {
		gAngle = _value;
	}

	/**
	 * Get value of property gAngle
	 *
	 * @return - value of field gAngle(unit=degree)
	 */
	public double getGAngle() {
		return gAngle;
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

		if (!(o instanceof Wall_type) || !super.equals(o))
			return false;
		Wall_type other = (Wall_type) o;
		return ((getLocation() == null && other.getLocation() == null) || (getLocation() != null && getLocation()
				.equals(other.getLocation())))

				&& getX() == other.getX()

				&& getY() == other.getY()

				&& getWidth() == other.getWidth()

				&& getHeight() == other.getHeight()

				&& getGX() == other.getGX()

				&& getGY() == other.getGY()

				&& getGAngle() == other.getGAngle()

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

		int hashCode = -160499023
				+ ((getLocation() == null) ? 0 : getLocation().hashCode());

		hashCode = 31 * hashCode + getX();

		hashCode = 31 * hashCode + getY();

		hashCode = 31 * hashCode + getWidth();

		hashCode = 31 * hashCode + getHeight();

		hashCode = 31 * hashCode + getGX();

		hashCode = 31 * hashCode + getGY();

		hashCode = 31 * hashCode + (new Double(getGAngle()).hashCode());

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

		if (location == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "location"));

	}

}
