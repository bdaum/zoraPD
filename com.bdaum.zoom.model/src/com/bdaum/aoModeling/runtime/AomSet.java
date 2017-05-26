package com.bdaum.aoModeling.runtime;

import java.util.Set;

/**
 * @author Berthold Daum
 *
 * (c) 2002 Berthold Daum
 * 
 * This interface defines a wrapping around java.util.Set. It includes
 * event managing and element validation.
 * 
 * @see java.util.Set
 * @see com.bdaum.aoModeling.runtime.AomValueChangedNotifier
 */
public interface AomSet<T> extends Set<T>, AomCollection<T> {
	
	
	/**
	 * Replaces the whole set with the argument set.
	 * No change notification is performed. Note that the argument will continue to live
	 * within this AomSet instance. Changes will affect the argument list.
	 * <b>This method is not part of the public API. Its purpose is to support
	 * persistency packages.</b>
	 * @param replacement - the set to replace the current set
	 */
	public void replaceSilently(Set<T> replacement);

	
}
