package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 * 
 * (c) 2002 Berthold Daum
 * 
 * This interface defines features that are common to all aspects.
 *
 */
public interface IAspect {
	

	/**
	 * Retrieve "static" property of current aspect instance.
	 * @return boolean - true if instance is not be cloned.
	 */
	public boolean isStatic();

	/**
	 * Creates a clone of the current instance.
	 * If instance type is specified as static, the clone is
	 * identical with the instance. 
	 * 
	 * @return Object - a clone of the current instance. If the instance
	 * was set to static, the original is returned instead.
	 */
	public Object clone();

	/**
	 * Is called for each operation call for which this operation
	 * aspect is registered
	 * @param point - The operation identification as generated in the asset.
	 * @param extension - An extension object specified during configuration
	 * @param receiver - The asset instance for which this operation is called.
	 * @throws ConstraintException - If a constraint is violated
	 */
	public void check(int point, Object extension, IAspectHost receiver)
		throws ConstraintException;

	/**
	 * Is called for each operation call for which this operation
	 * aspect is registered
	 * @param point - The operation identification as generated in the asset.
	 * @param mode - The current mode of the asset.
	 * @param extension - An extension object specified during configuration
	 * @param receiver - The asset instance for which this operation is called.
	 * @param sender - The instance that issued the operation call.
	 * @return boolean - true if the operation was executed successfully, false otherwise
	 */
	public boolean run(
		int point,
		int mode,
		Object extension,
		AomObject receiver,
		Object sender);

	/**
	 * Sets the extension field of the aspects or sub-aspects
	 * @param extension - The extension object
	 */
	public void setExtension(Object extension);
	
	/**
	 * Method getExtension.
	 * @return Object - the extension object set for this aspect or null
	 */
	public Object getExtension();

}
