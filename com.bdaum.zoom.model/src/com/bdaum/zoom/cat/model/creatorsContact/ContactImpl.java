package com.bdaum.zoom.cat.model.creatorsContact;

import java.util.*;
import com.bdaum.aoModeling.runtime.*;
import com.bdaum.zoom.cat.model.Contact_typeImpl;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset Contact
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class ContactImpl extends Contact_typeImpl implements Contact {

	static final long serialVersionUID = 2307565049L;

	/* ----- Constructors ----- */

	public ContactImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param city - Property
	 * @param country - Property
	 * @param postalCode - Property
	 * @param state - Property
	 */
	public ContactImpl(String city, String country, String postalCode,
			String state) {
		super(city, country, postalCode, state);

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
		attachInstrumentation(_instrumentation, ContactImpl.class, properties,
				aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc creatorsContact_parent *** */

	private String creatorsContact_parent;

	/**
	 * Set value of property creatorsContact_parent
	 *
	 * @param _value - new field value
	 */
	public void setCreatorsContact_parent(String _value) {
		creatorsContact_parent = _value;
	}

	/**
	 * Get value of property creatorsContact_parent
	 *
	 * @return - value of field creatorsContact_parent
	 */
	public String getCreatorsContact_parent() {
		return creatorsContact_parent;
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
