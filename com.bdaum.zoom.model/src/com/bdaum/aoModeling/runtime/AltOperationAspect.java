package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 *
 * (c) 2002 Berthold Daum
 */
public class AltOperationAspect extends ComposedOperationAspect {
	
	/**
	 * Method SeqOperationAspect. 
	 * This aspect executes a series of sub-aspects conditionally. 
	 * @see com.bdaum.aoModeling.runtime.ComposedOperationAspect#ComposedOperationAspect(OperationAspect[])
	 */
	
	public AltOperationAspect(OperationAspect[] operations) {
		super(operations);
	}

	/**
	 * Runs the child aspects. If a child aspect succeeds, the remaining aspects are not executed
	 * and "true" is returned. If no child aspects succeeds, "false" is returned.
	 * 
	 * @see com.bdaum.aoModeling.runtime.IAspect#run(int, int, Object, AomObject, Object)
	 */
	@Override
	public boolean run(
		int point,
		int mode, Object extension, 
		AomObject receiver,
		Object sender) {
			for (int i = 0; i < operations.length; i++) {
				OperationAspect operation = operations[i];
				if (operation.run(point,mode, operation._extension, receiver,sender)) return true;
			}
		return false;
	}

}
