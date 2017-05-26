package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset trackRecord
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class TrackRecord_typeImpl extends AomObject implements TrackRecord_type {

	static final long serialVersionUID = -3175580097L;

	/* ----- Constructors ----- */

	public TrackRecord_typeImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param type - Property
	 * @param serviceId - Property
	 * @param serviceName - Property
	 * @param target - Property
	 * @param derivative - Property
	 * @param exportDate - Property
	 * @param replaced - Property
	 * @param visit - Property
	 */
	public TrackRecord_typeImpl(String type, String serviceId,
			String serviceName, String target, String derivative,
			Date exportDate, boolean replaced, String visit) {
		super();
		this.type = type;
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.target = target;
		this.derivative = derivative;
		this.exportDate = exportDate;
		this.replaced = replaced;
		this.visit = visit;

	}

	/* ----- Fields ----- */

	/* *** Property type *** */

	private String type = AomConstants.INIT_String;

	/**
	 * Set value of property type
	 *
	 * @param _value - new field value
	 */
	public void setType(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "type"));
		String _valueIntern = _value.intern();
		if (!(_valueIntern == type_ftp || _valueIntern == type_community || _valueIntern == type_email))
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_VIOLATES_ENUMERATION,
					String.valueOf(_value)));

		type = _value;
	}

	/**
	 * Get value of property type
	 *
	 * @return - value of field type
	 */
	public String getType() {
		return type;
	}

	/* *** Property serviceId *** */

	private String serviceId = AomConstants.INIT_String;

	/**
	 * Set value of property serviceId
	 *
	 * @param _value - new field value
	 */
	public void setServiceId(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "serviceId"));
		serviceId = _value;
	}

	/**
	 * Get value of property serviceId
	 *
	 * @return - value of field serviceId
	 */
	public String getServiceId() {
		return serviceId;
	}

	/* *** Property serviceName *** */

	private String serviceName = AomConstants.INIT_String;

	/**
	 * Set value of property serviceName
	 *
	 * @param _value - new field value
	 */
	public void setServiceName(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "serviceName"));
		serviceName = _value;
	}

	/**
	 * Get value of property serviceName
	 *
	 * @return - value of field serviceName
	 */
	public String getServiceName() {
		return serviceName;
	}

	/* *** Property target *** */

	private String target;

	/**
	 * Set value of property target
	 *
	 * @param _value - new field value
	 */
	public void setTarget(String _value) {
		target = _value;
	}

	/**
	 * Get value of property target
	 *
	 * @return - value of field target
	 */
	public String getTarget() {
		return target;
	}

	/* *** Property derivative *** */

	private String derivative;

	/**
	 * Set value of property derivative
	 *
	 * @param _value - new field value
	 */
	public void setDerivative(String _value) {
		derivative = _value;
	}

	/**
	 * Get value of property derivative
	 *
	 * @return - value of field derivative
	 */
	public String getDerivative() {
		return derivative;
	}

	/* *** Property exportDate *** */

	private Date exportDate = new Date();

	/**
	 * Set value of property exportDate
	 *
	 * @param _value - new field value
	 */
	public void setExportDate(Date _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "exportDate"));
		exportDate = _value;
	}

	/**
	 * Get value of property exportDate
	 *
	 * @return - value of field exportDate
	 */
	public Date getExportDate() {
		return exportDate;
	}

	/* *** Property replaced *** */

	private boolean replaced;

	/**
	 * Set value of property replaced
	 *
	 * @param _value - new field value
	 */
	public void setReplaced(boolean _value) {
		replaced = _value;
	}

	/**
	 * Get value of property replaced
	 *
	 * @return - value of field replaced
	 */
	public boolean getReplaced() {
		return replaced;
	}

	/* *** Property visit *** */

	private String visit;

	/**
	 * Set value of property visit
	 *
	 * @param _value - new field value
	 */
	public void setVisit(String _value) {
		visit = _value;
	}

	/**
	 * Get value of property visit
	 *
	 * @return - value of field visit
	 */
	public String getVisit() {
		return visit;
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

		if (!(o instanceof TrackRecord_type) || !super.equals(o))
			return false;
		TrackRecord_type other = (TrackRecord_type) o;
		return ((getType() == null && other.getType() == null) || (getType() != null && getType()
				.equals(other.getType())))

				&& ((getServiceId() == null && other.getServiceId() == null) || (getServiceId() != null && getServiceId()
						.equals(other.getServiceId())))

				&& ((getServiceName() == null && other.getServiceName() == null) || (getServiceName() != null && getServiceName()
						.equals(other.getServiceName())))

				&& ((getTarget() == null && other.getTarget() == null) || (getTarget() != null && getTarget()
						.equals(other.getTarget())))

				&& ((getDerivative() == null && other.getDerivative() == null) || (getDerivative() != null && getDerivative()
						.equals(other.getDerivative())))

				&& ((getExportDate() == null && other.getExportDate() == null) || (getExportDate() != null && getExportDate()
						.equals(other.getExportDate())))

				&& getReplaced() == other.getReplaced()

				&& ((getVisit() == null && other.getVisit() == null) || (getVisit() != null && getVisit()
						.equals(other.getVisit())))

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

		int hashCode = -997696605
				+ ((getType() == null) ? 0 : getType().hashCode());

		hashCode = 31 * hashCode
				+ ((getServiceId() == null) ? 0 : getServiceId().hashCode());

		hashCode = 31
				* hashCode
				+ ((getServiceName() == null) ? 0 : getServiceName().hashCode());

		hashCode = 31 * hashCode
				+ ((getTarget() == null) ? 0 : getTarget().hashCode());

		hashCode = 31 * hashCode
				+ ((getDerivative() == null) ? 0 : getDerivative().hashCode());

		hashCode = 31 * hashCode
				+ ((getExportDate() == null) ? 0 : getExportDate().hashCode());

		hashCode = 31 * hashCode + (getReplaced() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getVisit() == null) ? 0 : getVisit().hashCode());

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

		if (type == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "type"));

		if (serviceId == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "serviceId"));

		if (serviceName == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "serviceName"));

		if (exportDate == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "exportDate"));

	}

}
