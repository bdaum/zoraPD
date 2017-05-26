package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 * 
 *         (c) 2002 Berthold Daum
 * 
 *         This aspect executes a series of sub-aspects conditionally.
 */
public class CondOperationAspect extends ComposedOperationAspect {

	/**
	 * Constructor.
	 * 
	 * @see com.bdaum.aoModeling.runtime.ComposedOperationAspect#ComposedOperationAspect(OperationAspect[])
	 */

	public CondOperationAspect(OperationAspect[] operations) {
		super(operations);
	}

	/**
	 * Runs the child aspects. If a child aspect fails, the remaining aspects
	 * are not executed and "false" is returned. If all child aspects succeed,
	 * "true" is returned.
	 * 
	 * @see com.bdaum.aoModeling.runtime.IAspect#run(int, int, Object,
	 *      AomObject, Object)
	 */
	@Override
	public boolean run(int point, int mode, Object extension,
			AomObject receiver, Object sender) {
		for (int i = 0; i < operations.length; i++) {
			OperationAspect operation = operations[i];
			if (!operation.run(point, mode, operation._extension, receiver,
					sender))
				return false;
		}
		return true;
	}

}