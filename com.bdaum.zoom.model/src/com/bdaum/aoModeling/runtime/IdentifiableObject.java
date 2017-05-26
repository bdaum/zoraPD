/**
 * @author Berthold Daum
 * 
 * (c) 2002-2004 Berthold Daum
 * 
 * This is the base class for all business objects that need ID and
 * serialization support
 *  
 */
package com.bdaum.aoModeling.runtime;

import java.io.Serializable;

public class IdentifiableObject implements Serializable, Cloneable, IIdentifiableObject {

	/**
     * 
     */
	private static final long serialVersionUID = -6513770575489450459L;

	private static IdentifierGenerator idGenerator;

	private Serializable objectId;
	private String stringId;
	private long longId;
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	public static void setIdentifierGenerator(IdentifierGenerator generator) {
		idGenerator = generator;
	}

	/**
	 * Returns a new id number
	 * 
	 * @return generated id number
	 */
	public static Serializable getNewObjectID() {
		return (idGenerator != null) ? idGenerator.generateIdentifier() : null;
	}

	/**
	 * Constructor
	 */
	public IdentifiableObject() {
		super();
		if (idGenerator != null)
			setSerializableId(idGenerator.generateIdentifier());
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IIdentifiableObject#getSerializableId()
	 */
	public Serializable getSerializableId() {
		return objectId;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IIdentifiableObject#setSerializableId(java.io.Serializable)
	 */
	public void setSerializableId(Serializable id) {
		if (id instanceof String)
			setStringId((String) id);
		else if (id instanceof Long)
			setLongId((Long) id);
		this.objectId = id;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IIdentifiableObject#getLongId()
	 */
	public Long getLongId() {
		return longId;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IIdentifiableObject#setLongId(java.lang.Long)
	 */
	public void setLongId(Long id) {
		this.longId = id;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IIdentifiableObject#getStringId()
	 */
	public String getStringId() {
		return stringId;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IIdentifiableObject#setStringId(java.lang.String)
	 */
	public void setStringId(String id) {
		this.stringId = id;
	}
}