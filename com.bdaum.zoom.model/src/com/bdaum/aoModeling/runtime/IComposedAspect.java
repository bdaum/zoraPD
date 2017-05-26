package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 * 
 * Copyright (c) 2003
 * 
 * This interface defines features that are common to all aspects consisting of sub-aspects.
 *
 */
public interface IComposedAspect extends IAspect {

	/**
	 * Returns the sub-aspects.
	 * @return Aspect[] child aspects of this composed aspect
	 */
	public Aspect[] getChildren();

	/**
	 * Accepts the given visitor.
	 * The visitor's <code>visit</code> method is called with this
	 * composed aspect if applicable. If the visitor returns <code>true</code>,
	 * the aspect's children are also visited.
	 *
	 * @param visitor the visitor
	 * @see IComposedAspectVisitor#visit
	 */
	public void accept(IComposedAspectVisitor visitor);

}
