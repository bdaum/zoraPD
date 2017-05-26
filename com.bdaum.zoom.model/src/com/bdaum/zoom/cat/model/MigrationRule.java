package com.bdaum.zoom.cat.model;

import java.util.*;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset migrationRule
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface MigrationRule extends IAsset, MigrationRule_type {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property migrationPolicy_rule_parent
	 *
	 * @param _value - new element value
	 */
	public void setMigrationPolicy_rule_parent(MigrationPolicy _value);

	/**
	 * Get value of property migrationPolicy_rule_parent
	 *
	 * @return - value of field migrationPolicy_rule_parent
	 */
	public MigrationPolicy getMigrationPolicy_rule_parent();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

	/**
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException;

}
