package com.bdaum.aoModeling.runtime;

public interface IAspectHost {

	/**
	 * Set the current operation mode to new value
	 * 
	 * @param mode
	 *            - The new operation mode
	 */
	public abstract void specifyOperationMode(int mode);

	/**
	 * Get the current operation mode
	 * 
	 * @return int - Operation mode
	 */
	public abstract int retrieveOperationMode();

	/**
	 * Returns all aspects registered.
	 * 
	 * @return Aspect[] aspects registered for each instrumentation point.
	 *         Individual entries may be null.
	 */
	public abstract Aspect[] retrieveInstrumentation();

}