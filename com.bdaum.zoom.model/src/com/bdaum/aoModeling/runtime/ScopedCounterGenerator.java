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
public class ScopedCounterGenerator implements IdentifierGenerator {

    private String prefix;
    private long objectCounter = 0;

    /**
     * Constructor
     * @param scope a string identifying the scope
     */
    public ScopedCounterGenerator(String scope) {
        super();
        this.prefix = (scope == null) ? "" : scope;
    }
    /* (non-Javadoc)
     * @see com.bdaum.aoModeling.runtime.IdentifierGenerator#generateIdentifier()
     */
    public Serializable generateIdentifier() {
        return prefix+(objectCounter++);
    }

}
