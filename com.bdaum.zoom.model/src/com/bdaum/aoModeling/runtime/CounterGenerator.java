/*
 * Created on 06.12.2004
 * 
 * (c) 2003, Berthold Daum
 *
 */
package com.bdaum.aoModeling.runtime;

import java.io.Serializable;

/**
 * @author bdaum
 *
 */
public class CounterGenerator implements IdentifierGenerator {
    
    private long objectCounter = 0;

    /* (non-Javadoc)
     * @see com.bdaum.aoModeling.runtime.IdentifierGenerator#generateIdentifier()
     */
    public Serializable generateIdentifier() {
        return new Long(objectCounter++);
    }

}
