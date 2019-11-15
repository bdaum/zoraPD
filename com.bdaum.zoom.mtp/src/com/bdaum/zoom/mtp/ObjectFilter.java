package com.bdaum.zoom.mtp;

import java.io.FileFilter;

public interface ObjectFilter extends FileFilter {

	/**
	 * Tests whether or not the specified storage object is accepted
	 *
	 * @param object
	 *            The storage object to be tested
	 * @return <code>true</code> if and only if <code>object</code> should be
	 *         accepted
	 */
	boolean accept(StorageObject object);

	/**
	 * Tests whether or not the specified file name is accepted
	 *
	 * @param fileName
	 *            The file name to be tested
	 * @return <code>true</code> if and only if <code>fileName</code> should be
	 *         accepted
	 */
	boolean accept(String fileName);

}
