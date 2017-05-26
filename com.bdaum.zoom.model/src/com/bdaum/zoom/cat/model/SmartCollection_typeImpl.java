package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset smartCollection
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class SmartCollection_typeImpl extends AomObject implements
		SmartCollection_type {

	static final long serialVersionUID = -3609186604L;

	/* ----- Constructors ----- */

	public SmartCollection_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param system - Property
	 * @param album - Property
	 * @param adhoc - Property
	 * @param network - Property
	 * @param description - Property
	 * @param colorCode - Property
	 * @param lastAccessDate - Property
	 * @param generation - Property
	 * @param perspective - Property
	 */
	public SmartCollection_typeImpl(String name, boolean system, boolean album,
			boolean adhoc, boolean network, String description, int colorCode,
			Date lastAccessDate, int generation, String perspective) {
		super();
		this.name = name;
		this.system = system;
		this.album = album;
		this.adhoc = adhoc;
		this.network = network;
		this.description = description;
		this.colorCode = colorCode;
		this.lastAccessDate = lastAccessDate;
		this.generation = generation;
		this.perspective = perspective;

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

	/* *** Property album *** */

	private boolean album;

	/**
	 * Set value of property album
	 *
	 * @param _value - new field value
	 */
	public void setAlbum(boolean _value) {
		album = _value;
	}

	/**
	 * Get value of property album
	 *
	 * @return - value of field album
	 */
	public boolean getAlbum() {
		return album;
	}

	/* *** Property adhoc *** */

	private boolean adhoc;

	/**
	 * Set value of property adhoc
	 *
	 * @param _value - new field value
	 */
	public void setAdhoc(boolean _value) {
		adhoc = _value;
	}

	/**
	 * Get value of property adhoc
	 *
	 * @return - value of field adhoc
	 */
	public boolean getAdhoc() {
		return adhoc;
	}

	/* *** Property network *** */

	private boolean network;

	/**
	 * Set value of property network
	 *
	 * @param _value - new field value
	 */
	public void setNetwork(boolean _value) {
		network = _value;
	}

	/**
	 * Get value of property network
	 *
	 * @return - value of field network
	 */
	public boolean getNetwork() {
		return network;
	}

	/* *** Property description *** */

	private String description;

	/**
	 * Set value of property description
	 *
	 * @param _value - new field value
	 */
	public void setDescription(String _value) {
		description = _value;
	}

	/**
	 * Get value of property description
	 *
	 * @return - value of field description
	 */
	public String getDescription() {
		return description;
	}

	/* *** Property colorCode *** */

	private int colorCode;

	/**
	 * Set value of property colorCode
	 *
	 * @param _value - new field value
	 */
	public void setColorCode(int _value) {
		colorCode = _value;
	}

	/**
	 * Get value of property colorCode
	 *
	 * @return - value of field colorCode
	 */
	public int getColorCode() {
		return colorCode;
	}

	/* *** Property lastAccessDate *** */

	private Date lastAccessDate = new Date();

	/**
	 * Set value of property lastAccessDate
	 *
	 * @param _value - new field value
	 */
	public void setLastAccessDate(Date _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastAccessDate"));
		lastAccessDate = _value;
	}

	/**
	 * Get value of property lastAccessDate
	 *
	 * @return - value of field lastAccessDate
	 */
	public Date getLastAccessDate() {
		return lastAccessDate;
	}

	/* *** Property generation *** */

	private int generation;

	/**
	 * Set value of property generation
	 *
	 * @param _value - new field value
	 */
	public void setGeneration(int _value) {
		generation = _value;
	}

	/**
	 * Get value of property generation
	 *
	 * @return - value of field generation
	 */
	public int getGeneration() {
		return generation;
	}

	/* *** Property perspective *** */

	private String perspective;

	/**
	 * Set value of property perspective
	 *
	 * @param _value - new field value
	 */
	public void setPerspective(String _value) {
		perspective = _value;
	}

	/**
	 * Get value of property perspective
	 *
	 * @return - value of field perspective
	 */
	public String getPerspective() {
		return perspective;
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

		if (!(o instanceof SmartCollection_type) || !super.equals(o))
			return false;
		SmartCollection_type other = (SmartCollection_type) o;
		return ((getName() == null && other.getName() == null) || (getName() != null && getName()
				.equals(other.getName())))

				&& getSystem() == other.getSystem()

				&& getAlbum() == other.getAlbum()

				&& getAdhoc() == other.getAdhoc()

				&& getNetwork() == other.getNetwork()

				&& ((getDescription() == null && other.getDescription() == null) || (getDescription() != null && getDescription()
						.equals(other.getDescription())))

				&& getColorCode() == other.getColorCode()

				&& ((getLastAccessDate() == null && other.getLastAccessDate() == null) || (getLastAccessDate() != null && getLastAccessDate()
						.equals(other.getLastAccessDate())))

				&& getGeneration() == other.getGeneration()

				&& ((getPerspective() == null && other.getPerspective() == null) || (getPerspective() != null && getPerspective()
						.equals(other.getPerspective())))

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

		int hashCode = -1554596434
				+ ((getName() == null) ? 0 : getName().hashCode());

		hashCode = 31 * hashCode + (getSystem() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getAlbum() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getAdhoc() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getNetwork() ? 1231 : 1237);

		hashCode = 31
				* hashCode
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());

		hashCode = 31 * hashCode + getColorCode();

		hashCode = 31
				* hashCode
				+ ((getLastAccessDate() == null) ? 0 : getLastAccessDate()
						.hashCode());

		hashCode = 31 * hashCode + getGeneration();

		hashCode = 31
				* hashCode
				+ ((getPerspective() == null) ? 0 : getPerspective().hashCode());

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

		if (lastAccessDate == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastAccessDate"));

	}

}
