package com.bdaum.zoom.cat.model.creatorsContact;

import com.bdaum.zoom.cat.model.Contact_type;

import java.util.*;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset Contact
 */

/* !! This interface is not intended to modified manually !! */

//Modified manually: no backpointers and other overhead

@SuppressWarnings({ "unused" })
public interface Contact extends Contact_type, IAsset {


	/* ----- Validation ----- */

	/**
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException;

}
