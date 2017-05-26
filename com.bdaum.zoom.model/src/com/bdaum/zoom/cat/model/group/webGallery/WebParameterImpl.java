package com.bdaum.zoom.cat.model.group.webGallery;

import java.util.*;
import com.bdaum.zoom.cat.model.WebParameter_typeImpl;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset webParameter
 *
 * <b>id</b> is composed of namespace.localID
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class WebParameterImpl extends WebParameter_typeImpl implements
		WebParameter {

	static final long serialVersionUID = -291978822L;

	/* ----- Constructors ----- */

	public WebParameterImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param id - Property
	 * @param value - Property
	 * @param encodeHtml - Property
	 * @param linkTo - Property
	 */
	public WebParameterImpl(String id, Object value, boolean encodeHtml,
			String linkTo) {
		super(id, value, encodeHtml, linkTo);

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
		attachInstrumentation(_instrumentation, WebParameterImpl.class,
				properties, aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc webGallery_parameter_parent *** */

	private WebGallery webGallery_parameter_parent;

	/**
	 * Set value of property webGallery_parameter_parent
	 *
	 * @param _value - new field value
	 */
	public void setWebGallery_parameter_parent(WebGallery _value) {
		webGallery_parameter_parent = _value;
	}

	/**
	 * Get value of property webGallery_parameter_parent
	 *
	 * @return - value of field webGallery_parameter_parent
	 */
	public WebGallery getWebGallery_parameter_parent() {
		return webGallery_parameter_parent;
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
		if (!(o instanceof WebParameter))
			return false;
		WebParameter other = (WebParameter) o;
		return ((getId() == null && other.getId() == null) || (getId() != null && getId()
				.equals(other.getId())));
	}

	/**
	 * Returns the hash code for the primary key of this object.
	 * @return the primary key hash code value
	 * @see com.bdaum.aoModeling.runtime.IAsset#keyHashCode
	 */
	public int keyHashCode() {
		return ((getId() == null) ? 0 : getId().hashCode());
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
		return getId() + '=' + getValue();
	}

}
