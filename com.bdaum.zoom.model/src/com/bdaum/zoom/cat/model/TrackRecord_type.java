package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset trackRecord
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface TrackRecord_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	public static final String type_ftp = "ftp";
	public static final String type_community = "community";
	public static final String type_email = "email";

	public static final String[] typeALLVALUES = new String[] { type_ftp,
			type_community, type_email };

	/**
	 * Set value of property type
	 *
	 * @param _value - new element value
	 */
	public void setType(String _value);

	/**
	 * Get value of property type
	 *
	 * @return - value of field type
	 */
	public String getType();

	/**
	 * Set value of property serviceId
	 *
	 * @param _value - new element value
	 */
	public void setServiceId(String _value);

	/**
	 * Get value of property serviceId
	 *
	 * @return - value of field serviceId
	 */
	public String getServiceId();

	/**
	 * Set value of property serviceName
	 *
	 * @param _value - new element value
	 */
	public void setServiceName(String _value);

	/**
	 * Get value of property serviceName
	 *
	 * @return - value of field serviceName
	 */
	public String getServiceName();

	/**
	 * Set value of property target
	 *
	 * @param _value - new element value
	 */
	public void setTarget(String _value);

	/**
	 * Get value of property target
	 *
	 * @return - value of field target
	 */
	public String getTarget();

	/**
	 * Set value of property derivative
	 *
	 * @param _value - new element value
	 */
	public void setDerivative(String _value);

	/**
	 * Get value of property derivative
	 *
	 * @return - value of field derivative
	 */
	public String getDerivative();

	/**
	 * Set value of property exportDate
	 *
	 * @param _value - new element value
	 */
	public void setExportDate(Date _value);

	/**
	 * Get value of property exportDate
	 *
	 * @return - value of field exportDate
	 */
	public Date getExportDate();

	/**
	 * Set value of property replaced
	 *
	 * @param _value - new element value
	 */
	public void setReplaced(boolean _value);

	/**
	 * Get value of property replaced
	 *
	 * @return - value of field replaced
	 */
	public boolean getReplaced();

	/**
	 * Set value of property visit
	 *
	 * @param _value - new element value
	 */
	public void setVisit(String _value);

	/**
	 * Get value of property visit
	 *
	 * @return - value of field visit
	 */
	public String getVisit();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
