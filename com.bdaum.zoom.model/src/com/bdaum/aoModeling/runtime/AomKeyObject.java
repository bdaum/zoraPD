/*
 * Created on 19.08.2003
 * 
 * (c) 2003, Berthold Daum
 *
 */
package com.bdaum.aoModeling.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;

/**
 * @author Berthold Daum
 *
 * This is the base class for all key objects.
 */

public class AomKeyObject extends AomObject {
	
	/**
     * 
     */
    private static final long serialVersionUID = -777111207850755260L;

    /**
	 * Serializes the current object to a string.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			objOut.writeObject(this);
		} catch (IOException e) {
		    // should never happen
		}
		return new String(out.toByteArray());
	}
	
	/**
	 * Unmarshalls an object from a string
	 * @param s - the serialized instance
	 * @return - the deserialized object
	 */
	protected Object _fromString(String s) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new PrintStream(out).print(s);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		try {
			ObjectInputStream objIn = new ObjectInputStream(in);
			return objIn.readObject();
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

}
