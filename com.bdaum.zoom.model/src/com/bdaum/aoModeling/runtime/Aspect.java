package com.bdaum.aoModeling.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Berthold Daum
 * 
 * (c) 2002-2008 Berthold Daum
 * 
 * This is class is the root class for all aspect implementations. Concrete
 * aspects are implemented by subclassing this class.
 * 
 * Concrete aspects have the possibility to declare their implementation as
 * static (default) or non-static by overriding the method isStatic(). Static
 * implementations are not cloned for aspect instances.
 * 
 * When an implementation is non-static and the implementation sets up complex
 * structures within its constructor, it might be necessary to override the
 * clone() method in order to clone these structures, too.
 */

public abstract class Aspect implements Cloneable, IAspect {

	// holds an aspect extension
	protected Object _extension;

	private Map<String, Object> _valueMap;

	/**
	 * @see com.bdaum.aoModeling.runtime.IAspect#isStatic() Implementors may
	 *      override.
	 */
	public boolean isStatic() {
		return true;
	}

	/**
	 * @see java.lang.Cloneable Implementors may override.
	 */
	@Override
	public Object clone() {
		if (isStatic())
			return this;
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// should never happen
			return null;
		}
	}

	/**
	 * @see com.bdaum.aoModeling.runtime.IAspect#check(int, Object, IAspectHost)
	 */
	public abstract void check(int point, Object extension, IAspectHost receiver)
			throws ConstraintException;

	/**
	 * @see com.bdaum.aoModeling.runtime.IAspect#run(int, int, Object,
	 *      AomObject, Object)
	 */
	public abstract boolean run(int point, int mode, Object extension,
			AomObject receiver, Object sender);

	/**
	 * @see com.bdaum.aoModeling.runtime.IAspect#setExtension(Object)
	 */
	public void setExtension(Object extension) {
		_extension = extension;
	}

	/**
	 * @see com.bdaum.aoModeling.runtime.IAspect#getExtension()
	 */
	public Object getExtension() {
		return _extension;
	}

	/**
	 * Obtains a custom named value associated with this aspect 
	 * @param key - the value name
	 * @return - the associated value or null
	 */
	public Object getData(String key) {
		return _valueMap == null ? null : _valueMap.get(key);
	}

	/**
	 * Sets custom data associated with this aspect
	 * @param key - the value name
	 * @param value - the value
	 * @return - previously associated value or null
	 */
	public Object setData(String key, Object value) {
		if (_valueMap == null)
			_valueMap = new HashMap<String, Object>();
		return _valueMap.put(key, value);
	}

}
