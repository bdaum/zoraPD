package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 * 
 * (c) 2005 Berthold Daum
 * 
 * This interface defines features common to all asset classes
 * 
 * 2011-10-31 bd made IAsset extend IIdentifiableObject
 *  
 */
public interface IAsset extends IIdentifiableObject {

    /**
     * Compares the specified object with this object for primary key equality
     * 
     * @param o
     *            the object to be compared with this object.
     * @return true if the specified object is key-identical to this object.
     *         Defaults to object identity if no key is defined.
     */
    public boolean isKeyIdentical(Object o);

    /**
     * Returns the hash code for the primary key of this object. Defaults to
     * object's hash code if no key is defined.
     * 
     * @return the primary key hash code value
     */
    public int keyHashCode();

    /**
     * Performs constraint validation
     * 
     * @throws ConstraintException
     */
    public void validate() throws ConstraintException;

}