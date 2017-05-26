/*
 * Created on 06.12.2004
 * 
 * (c) 2004, Berthold Daum
 *
 */
package com.bdaum.aoModeling.runtime;

import java.io.Serializable;

/**
 * @author bdaum
 *
 */
public interface IdentifierGenerator {
    
    public Serializable generateIdentifier();

}
