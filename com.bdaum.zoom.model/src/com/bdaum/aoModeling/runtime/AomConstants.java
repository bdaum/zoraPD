/**
 * @author Berthold Daum
 *
 * (c) 2004 Berthold Daum
 * 
 * This class defines basic constants for the execution of generated AOM classes
 * 
 */
package com.bdaum.aoModeling.runtime;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

public class AomConstants {
	public static final Character INIT_Character = new Character((char) 0);
	public static final Byte INIT_Byte = new Byte((byte) 0);
	public static final Short INIT_Short = new Short((short) 0);
	public static final Integer INIT_Integer = new Integer(0);
	public static final Long INIT_Long = new Long(0L);
	public static final Float INIT_Float = new Float(0f);
	public static final Double INIT_Double = new Double(0d);
	public static final BigDecimal INIT_BigDecimal = new BigDecimal(0d);
	public static URI INIT_URI;
	static {
		try {
			INIT_URI = new URI("http://localhost/");
		} catch (URISyntaxException e) {
			INIT_URI = null;
		}
	}
	public static final String INIT_String = "";
}
