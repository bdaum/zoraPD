package com.bdaum.zoom.cat.model.pageLayout;

import com.bdaum.zoom.cat.model.PageLayout_typeImpl;
import java.util.*;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset PageLayout
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class PageLayoutImpl extends PageLayout_typeImpl implements PageLayout {

	static final long serialVersionUID = 1349830714L;

	/* ----- Constructors ----- */

	public PageLayoutImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param type - Property
	 * @param title - Property
	 * @param subtitle - Property
	 * @param footer - Property
	 * @param size - Property
	 * @param columns - Property
	 * @param leftMargin - Property
	 * @param rightMargin - Property
	 * @param horizontalGap - Property
	 * @param topMargin - Property
	 * @param bottomMargin - Property
	 * @param verticalGap - Property
	 * @param caption1 - Property
	 * @param caption2 - Property
	 * @param alt - Property
	 * @param keyLine - Property
	 * @param landscape - Property
	 * @param facingPages - Property
	 * @param format - Property
	 * @param applySharpening - Property
	 * @param radius - Property
	 * @param amount - Property
	 * @param threshold - Property
	 * @param jpegQuality - Property
	 */
	public PageLayoutImpl(String name, int type, String title, String subtitle,
			String footer, int size, int columns, int leftMargin,
			int rightMargin, int horizontalGap, int topMargin,
			int bottomMargin, int verticalGap, String caption1,
			String caption2, String alt, int keyLine, boolean landscape,
			boolean facingPages, String format, boolean applySharpening,
			float radius, float amount, int threshold, int jpegQuality) {
		super(name, type, title, subtitle, footer, size, columns, leftMargin,
				rightMargin, horizontalGap, topMargin, bottomMargin,
				verticalGap, caption1, caption2, alt, keyLine, landscape,
				facingPages, format, applySharpening, radius, amount,
				threshold, jpegQuality);

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
		attachInstrumentation(_instrumentation, PageLayoutImpl.class,
				properties, aspect);
	}

	/* ----- Equality and Identity ----- */

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

}
