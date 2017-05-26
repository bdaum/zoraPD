package com.bdaum.zoom.core.db;


public interface IDbConfigurator {

	/**
	 * Extend the current database configuration
	 * @param handler - the instance who handles the configuration
	 */
	void configure(IConfigurationHandler handler);

}
