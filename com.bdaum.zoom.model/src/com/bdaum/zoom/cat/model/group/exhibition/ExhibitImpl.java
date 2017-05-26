package com.bdaum.zoom.cat.model.group.exhibition;

import java.util.*;
import com.bdaum.zoom.cat.model.Rgb_type;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.Exhibit_typeImpl;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset exhibit
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class ExhibitImpl extends Exhibit_typeImpl implements Exhibit {

	static final long serialVersionUID = 2595267763L;

	/* ----- Constructors ----- */

	public ExhibitImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param title - Property
	 * @param description - Property
	 * @param credits - Property
	 * @param date - Property
	 * @param x - Property
	 * @param y - Property
	 * @param width - Property
	 * @param height - Property
	 * @param matWidth - Property
	 * @param matColor - Property
	 * @param frameWidth - Property
	 * @param frameColor - Property
	 * @param sold - Property
	 * @param hideLabel - Property
	 * @param labelAlignment - Property
	 * @param labelDistance - Property
	 * @param labelIndent - Property
	 * @param asset - Arc
	 */
	public ExhibitImpl(String title, String description, String credits,
			String date, int x, int y, int width, int height, Integer matWidth,
			Rgb_type matColor, Integer frameWidth, Rgb_type frameColor,
			boolean sold, Boolean hideLabel, Integer labelAlignment,
			Integer labelDistance, Integer labelIndent, String asset) {
		super(title, description, credits, date, x, y, width, height, matWidth,
				matColor, frameWidth, frameColor, sold, hideLabel,
				labelAlignment, labelDistance, labelIndent);
		this.asset = asset;

	}

	/* ----- Initialisation ----- */

	private static List<Instrumentation> _instrumentation = new ArrayList<Instrumentation>();

	public static void attachInstrumentation(int point, Aspect aspect,
			Object extension) {
		attachInstrumentation(_instrumentation, point, aspect, extension);
	}

	public static void attachInstrumentation(int point, Aspect aspect) {
		attachInstrumentation(_instrumentation, point, aspect);
	}

	public static void attachInstrumentation(Properties properties,
			Aspect aspect) {
		attachInstrumentation(_instrumentation, ExhibitImpl.class, properties,
				aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc wall_exhibit_parent *** */

	private String wall_exhibit_parent;

	/**
	 * Set value of property wall_exhibit_parent
	 *
	 * @param _value - new field value
	 */
	public void setWall_exhibit_parent(String _value) {
		wall_exhibit_parent = _value;
	}

	/**
	 * Get value of property wall_exhibit_parent
	 *
	 * @return - value of field wall_exhibit_parent
	 */
	public String getWall_exhibit_parent() {
		return wall_exhibit_parent;
	}

	/* *** Arc asset *** */

	private String asset = AomConstants.INIT_String;

	/**
	 * Set value of property asset
	 *
	 * @param _value - new field value
	 */
	public void setAsset(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "asset"));
		asset = _value;
	}

	/**
	 * Get value of property asset
	 *
	 * @return - value of field asset
	 */
	public String getAsset() {
		return asset;
	}

	/* ----- Equality and Identity ----- */

	/**
	 * Compares the specified object with this object for equality.
	 *
	 * @param o the object to be compared with this object.
	 * @return true if the specified object is equal to this object.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof Exhibit) || !super.equals(o))
			return false;
		Exhibit other = (Exhibit) o;
		return ((getAsset() == null && other.getAsset() == null) || (getAsset() != null && getAsset()
				.equals(other.getAsset())))

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

		return super.hashCode() * 31
				+ ((getAsset() == null) ? 0 : getAsset().hashCode());
	}

	/**
	 * Compares the specified object with this object for primary key equality.
	 *
	 * @param o the object to be compared with this object
	 * @return true if the specified object is key-identical to this object
	 * @see com.bdaum.aoModeling.runtime.IAsset#isKeyIdentical
	 */
	public boolean isKeyIdentical(Object o) {
		return this == o;
	}

	/**
	 * Returns the hash code for the primary key of this object.
	 * @return the primary key hash code value
	 * @see com.bdaum.aoModeling.runtime.IAsset#keyHashCode
	 */
	public int keyHashCode() {
		return hashCode();
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

		if (asset == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "asset"));

		super.validateCompleteness();
	}

	/**
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException {
		validateCompleteness();
	}

	@Override
	public String toString() {
		return "Exhibit: " + getTitle();
	}

}
