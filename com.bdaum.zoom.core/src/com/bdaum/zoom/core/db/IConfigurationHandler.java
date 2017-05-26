package com.bdaum.zoom.core.db;

import org.osgi.framework.Bundle;

public interface IConfigurationHandler {

	/**
	 * Sets whether or not a class is indexed
	 *
	 * @param clazz
	 * @param indexed
	 */
	void setIndexed(Class<?> clazz, boolean indexed);

	/**
	 * Sets whether or not a field is indexed
	 *
	 * @param clazz
	 * @param fieldName
	 * @param indexed
	 */
	void setIndexed(Class<?> clazz, String fieldName, boolean indexed);

	/**
	 * Adds a bundle to the classloading mechanism of the database
	 *
	 * @param bundle
	 */
	void addBundle(Bundle bundle);

	/**
	 * Removes a bundle from the classloading mechanism of the database
	 *
	 * @param bundle
	 */
	void removeBundle(Bundle bundle);

}
