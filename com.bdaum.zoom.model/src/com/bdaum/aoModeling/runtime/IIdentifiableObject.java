package com.bdaum.aoModeling.runtime;

import java.io.Serializable;

public interface IIdentifiableObject {

	/**
	 * Retrieves the object id (used by some persistence layers)
	 * 
	 * @return - the object id
	 */
	public abstract Serializable getSerializableId();

	/**
	 * Sets the object id (used by some persistence layers)
	 * 
	 * @param id
	 *            - the object id
	 */
	public abstract void setSerializableId(Serializable id);

	/**
	 * Retrieves the object id as Long object (used by some persistence layers)
	 * 
	 * @return - the object id
	 */
	public abstract Long getLongId();

	/**
	 * Sets a Long object id (used by some persistence layers)
	 * 
	 * @param id
	 *            - the object id
	 */
	public abstract void setLongId(Long id);

	/**
	 * Retrieves the object id as String object (used by some persistence
	 * layers)
	 * 
	 * @return - the object id
	 */
	public abstract String getStringId();

	/**
	 * Sets a String object id (used by some persistence layers)
	 * 
	 * @param id
	 *            - the object id
	 */
	public abstract void setStringId(String id);

}