package com.bdaum.zoom.cat.model;

import java.util.*;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset rgb
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Rgb_typeImpl extends AomObject implements Rgb_type {

	static final long serialVersionUID = -2552957426L;

	/* ----- Constructors ----- */

	public Rgb_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param r - Property
	 * @param g - Property
	 * @param b - Property
	 */
	public Rgb_typeImpl(int r, int g, int b) {
		super();
		this.r = r;
		this.g = g;
		this.b = b;

	}

	/* ----- Fields ----- */

	/* *** Property r *** */

	private int r;

	/**
	 * Set value of property r
	 *
	 * @param _value - new field value
	 */
	public void setR(int _value) {
		r = _value;
	}

	/**
	 * Get value of property r
	 *
	 * @return - value of field r
	 */
	public int getR() {
		return r;
	}

	/* *** Property g *** */

	private int g;

	/**
	 * Set value of property g
	 *
	 * @param _value - new field value
	 */
	public void setG(int _value) {
		g = _value;
	}

	/**
	 * Get value of property g
	 *
	 * @return - value of field g
	 */
	public int getG() {
		return g;
	}

	/* *** Property b *** */

	private int b;

	/**
	 * Set value of property b
	 *
	 * @param _value - new field value
	 */
	public void setB(int _value) {
		b = _value;
	}

	/**
	 * Get value of property b
	 *
	 * @return - value of field b
	 */
	public int getB() {
		return b;
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

		if (!(o instanceof Rgb_type) || !super.equals(o))
			return false;
		Rgb_type other = (Rgb_type) o;
		return getR() == other.getR()

		&& getG() == other.getG()

		&& getB() == other.getB()

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

		int hashCode = 1123737012 + getR();

		hashCode = 31 * hashCode + getG();

		hashCode = 31 * hashCode + getB();

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
