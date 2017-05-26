package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 * 
 *         (c) 2002 Berthold Daum
 * 
 *         This is class is the root class for all constraint implementations.
 *         Concrete constraint aspects are implemented by subclassing this
 *         class.
 * 
 *         Concrete constraint have the possibility to declare their
 *         implementation as static or non-static (default). Static
 *         implementations are not cloned for asset instances. To declare an
 *         implementation as static, call the method setStatic() in the
 *         constructor of the subclass.
 * 
 *         When an implementation is non-static and the implementation sets up
 *         complex structures within its constructor, it might be necessary to
 *         override the clone() method in order to clone these structures, too.
 */

public abstract class ConstraintAspect extends Aspect {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bdaum.aoModeling.runtime.IAspect#run(int, int, Object,
	 * AomObject, Object)
	 */
	@Override
	public boolean run(int point, int mode, Object extension,
			AomObject receiver, Object sender) {
		System.err
				.println(ModelMessages.getString(
						ErrorMessages.CONSTRAINTASPECT_ILLEGALCALL, String
								.valueOf(point), String.valueOf(mode),
						receiver, sender));
		return false;
	}

}
