package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 * 
 *         (c) 2002 Berthold Daum
 * 
 *         This aspect executes a series of sub-aspects unconditionally.
 */
public class SeqConstraintAspect extends ComposedConstraintAspect {

	/*
	 * (non-Javadoc)
	 * 
	 * @seecom.bdaum.aoModeling.runtime.ComposedConstraintAspect#
	 * ComposedConstraintAspect(ConstraintAspect[])
	 */

	public SeqConstraintAspect(ConstraintAspect[] operations) {
		super(operations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bdaum.aoModeling.runtime.IAspect#check(int, Object, AomObject)
	 */
	@Override
	public void check(int point, Object extension, IAspectHost receiver)
			throws ConstraintException {
		for (int i = 0; i < operations.length; i++) {
			ConstraintAspect operation = operations[i];
			operation.check(point, operation._extension, receiver);
		}
	}

}
