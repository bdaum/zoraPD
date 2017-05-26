package com.bdaum.aoModeling.runtime;

import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 * @author Berthold Daum
 *
 * (c) 2002 Berthold Daum
 * 
 * This class organises message lookup.
 */
public class ModelMessages {

	private final static String RESOURCE_BUNDLE= "com.bdaum.aoModeling.runtime.ModelMessages";//$NON-NLS-1$
	
	private static ResourceBundle fgResourceBundle = null;
	
	private static boolean notRead = true;

	/**
	 * Returns the resource bundle.
	 * @return ResourceBundle - the resource bundle.
	 */
	public static ResourceBundle getResourceBundle() {
		if (notRead) {
			notRead = false;
			try {
				fgResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
			}
			catch (Exception e) {
			}
		}
		return fgResourceBundle;
	}
	
	/**
	 * Translates a key into a message.
	 * @param key - message key
	 * @return String - resulting message
	 */
	public static String getString(String key) {
		try {
			return getResourceBundle().getString(key);
		} catch (Exception e) {
			return "!" + key + "!";
		}
	}
	
	/**
	 * Translate a key into a message and inserts a parameter
	 * @param key - message key
	 * @param param - parameter to be inserted
	 * @return String - resulting message
	 */
	public static String getString(String key, Object param) {
		String cmsg = getString(key);
		try {
			return MessageFormat.format(cmsg, new Object[] {param});
		} catch (Exception e) {return "!"+key+"!";}
	}
	
	/**
	 * Translate a key into a message and inserts parameters
	 * @param key - message key
	 * @param param1 - parameter1 to be inserted
	 * @param param2 - parameter2 to be inserted
	 * @return String - resulting message
	 */
	public static String getString(String key, Object param1, Object param2) {
		String cmsg = getString(key);
		try {
			return MessageFormat.format(cmsg,  new Object[] {param1, param2});
		} catch (Exception e) {return "!"+key+"!";}
	}

	/**
	 * Translate a key into a message and inserts parameters
	 * @param key - message key
	 * @param param1 - parameter1 to be inserted
	 * @param param2 - parameter2 to be inserted
	 * @param param3 - parameter3 to be inserted
	 * @return String - resulting message
	 */
	public static String getString(String key, Object param1, Object param2, Object param3) {
		String cmsg = getString(key);
		try {
			return MessageFormat.format(cmsg,  new Object[] {param1, param2, param3});
		} catch (Exception e) {return "!"+key+"!";}
	}

	/**
	 * Translate a key into a message and inserts parameters
	 * @param key - message key
	 * @param param1 - parameter1 to be inserted
	 * @param param2 - parameter2 to be inserted
	 * @param param3 - parameter3 to be inserted
	 * @param param4 - parameter4 to be inserted
	 * @return String - resulting message
	 */
	public static String getString(String key, Object param1, Object param2, Object param3, Object param4) {
		String cmsg = getString(key);
		try {
			return MessageFormat.format(cmsg,  new Object[] {param1, param2, param3, param4});
		} catch (Exception e) {return "!"+key+"!";}
	}

}


