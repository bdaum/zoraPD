package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 *
 * (c) 2002 Berthold Daum
 * 
 * An instance of this class describes a single instrumentation for an asset.
 */

public class Instrumentation {

	protected Aspect aspect;
	protected Object extension;
	protected int point;

	/**
	 * Constructor.
	 * @param aspect - Operation or constraint
	 * @param point - the index of the operation or constraint
	 * @param extension - a user defined extension
	 */
	public Instrumentation(Aspect aspect, int point, Object extension) {
		this.aspect = aspect;
		this.point = point;
		this.extension = extension;
	}
}

