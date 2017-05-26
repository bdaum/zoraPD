package com.bdaum.zoom.cat.model.group.slideShow;

import java.util.*;
import com.bdaum.zoom.cat.model.Slide_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset slide
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class SlideImpl extends Slide_typeImpl implements Slide {

	static final long serialVersionUID = 1573316648L;

	/* ----- Constructors ----- */

	public SlideImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param caption - Property
	 * @param sequenceNo - Property
	 * @param description - Property
	 * @param layout - Property
	 * @param delay - Property
	 * @param fadeIn - Property
	 * @param duration - Property
	 * @param fadeOut - Property
	 * @param effect - Property
	 * @param noVoice - Property
	 * @param asset - Arc
	 */
	public SlideImpl(String caption, int sequenceNo, String description,
			int layout, int delay, int fadeIn, int duration, int fadeOut,
			int effect, boolean noVoice, String asset) {
		super(caption, sequenceNo, description, layout, delay, fadeIn,
				duration, fadeOut, effect, noVoice);
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
		attachInstrumentation(_instrumentation, SlideImpl.class, properties,
				aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc slideShow_entry_parent *** */

	private String slideShow_entry_parent;

	/**
	 * Set value of property slideShow_entry_parent
	 *
	 * @param _value - new field value
	 */
	public void setSlideShow_entry_parent(String _value) {
		slideShow_entry_parent = _value;
	}

	/**
	 * Get value of property slideShow_entry_parent
	 *
	 * @return - value of field slideShow_entry_parent
	 */
	public String getSlideShow_entry_parent() {
		return slideShow_entry_parent;
	}

	/* *** Arc asset *** */

	private String asset;

	/**
	 * Set value of property asset
	 *
	 * @param _value - new field value
	 */
	public void setAsset(String _value) {
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

		return (o instanceof SlideImpl)
				&& getStringId().equals(((SlideImpl) o).getStringId());
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

		return getStringId().hashCode();
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
		return getStringId();
	}

}
