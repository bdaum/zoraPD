/**
 * 
 */
package com.bdaum.aoModeling.runtime;

/**
 * @author Berthold Daum
 * 
 * (c) 2002-2007 Berthold Daum
 * 
 * This class implements an exception type thrown by security aspects.
 * 
 */
public class AomSecurityException extends SecurityException {
	
	private static final long serialVersionUID = 6320191725895865930L;

	private String point;
	
	private String asset;

	/**
	 * 
	 */
	public AomSecurityException() {
	}

	/**
	 * @param s - Error message
	 * @param point - operation point id
	 * @param asset - asset name
	 */
	public AomSecurityException(String s, String point, String asset) {
		super(s);
		this.point = point;
		this.asset = asset;
	}

	public String getPoint() {
		return point;
	}

	public String getAsset() {
		return asset;
	}

}
