package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset migrationPolicy
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface MigrationPolicy_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property name
	 *
	 * @param _value - new element value
	 */
	public void setName(String _value);

	/**
	 * Get value of property name
	 *
	 * @return - value of field name
	 */
	public String getName();

	public static final String fileSeparatorPolicy_nOCHANGE = "NOCHANGE";
	public static final String fileSeparatorPolicy_tOSLASH = "TOSLASH";
	public static final String fileSeparatorPolicy_tOBACKSLASH = "TOBACKSLASH";

	public static final String[] fileSeparatorPolicyALLVALUES = new String[] {
			fileSeparatorPolicy_nOCHANGE, fileSeparatorPolicy_tOSLASH,
			fileSeparatorPolicy_tOBACKSLASH };

	/**
	 * Set value of property fileSeparatorPolicy
	 *
	 * @param _value - new element value
	 */
	public void setFileSeparatorPolicy(String _value);

	/**
	 * Get value of property fileSeparatorPolicy
	 *
	 * @return - value of field fileSeparatorPolicy
	 */
	public String getFileSeparatorPolicy();

	/**
	 * Set value of property targetCatalog
	 *
	 * @param _value - new element value
	 */
	public void setTargetCatalog(String _value);

	/**
	 * Get value of property targetCatalog
	 *
	 * @return - value of field targetCatalog
	 */
	public String getTargetCatalog();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
