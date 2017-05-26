package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 * 
 *         (c) 2002 Berthold Daum
 * 
 *         This aspect executes a series of sub-aspects unconditionally.
 */
public class SeqOperationAspect extends ComposedOperationAspect {

	/**
	 * Constructor.
	 * 
	 * @see com.bdaum.aoModeling.runtime.ComposedOperationAspect#ComposedOperationAspect(OperationAspect[])
	 */
	public SeqOperationAspect(OperationAspect[] operations) {
		super(operations);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bdaum.aoModeling.runtime.IAspect#run(int, int, Object,
	 * AomObject, Object)
	 */
	@Override
	public boolean run(int point, int mode, Object extension,
			AomObject receiver, Object sender) {
		boolean success = true;
		for (int i = 0; i < operations.length; i++) {
			OperationAspect operation = operations[i];
			success &= operation.run(point, mode, operation._extension,
					receiver, sender);
		}
		return success;
	}

}
