package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 * 
 * (c) 2003 Berthold Daum
 * 
 * This interface defines a visitor for composed aspects. 
 * It is used to implement the classic visitor pattern.
 *
 */
public interface IComposedAspectVisitor {

	/** 
	 * Visits the given composed aspect instance.
	 * 
	 * @return <code>true</code> if the composed aspect's children should
	 *		be visited; <code>false</code> if they should be skipped.
	 */
	public boolean visit(IComposedAspect aspect);
}
