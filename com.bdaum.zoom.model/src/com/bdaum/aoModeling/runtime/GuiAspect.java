package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 *
 * (c) 2002 Berthold Daum
 * 
 * This is class is the root class for all GUI operation implementations.
 * Concrete GUI aspects are implemented by subclassing this class.
 * 
 * Concrete implementations have the possibility to declare their implementation as
 * static or non-static (default). Static implementations are not cloned for
 * asset instances. To declare an implementation as static, call the method
 * setStatic() in the constructor of the subclass.
 * 
 * When an implementation is non-static and the implementation sets up
 * complex structures within its constructor, it might be necessary to
 * override the clone() method in order to clone these structures, too.
 */

public abstract class GuiAspect extends Aspect {

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IAspect#check(int, Object, AomObject)
	 */
	@Override
	public void check(int point, Object extension, IAspectHost receiver) {
		System.err.println(ModelMessages.getString(ErrorMessages.GUIASPECT_ILLEGALCALL,String.valueOf(point),receiver));
	}

}
