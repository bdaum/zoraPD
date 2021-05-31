/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2012 Berthold Daum  
 */
package com.bdaum.zoom.common;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CommonUtilities {

	public static final String key = "Bcp18793Var93012"; //$NON-NLS-1$
	public static final String prefix = "\n!\n"; //$NON-NLS-1$
	private static int hoverDelay = 200;
	private static int hoverBaseTime = 1000;
	private static int hoverTimePerChar = 25;
	private static Cipher cipher;
	private static SecretKeySpec aesKey;

	private CommonUtilities() {
		// inhibit instantiation
	}

	/**
	 * Encodes the blanks of a URL
	 *
	 * @param s
	 *            - URL or URL part/
	 * @return the encoded string
	 */
	public static String encodeBlanks(String s) {
		return s == null ? null : s.replaceAll("[\\+]|\\s", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Converts a separator separated string list into a list of strings
	 *
	 * @param stringlist
	 *            - input string
	 * @param seps
	 *            - valid separators
	 * @return - resulting list
	 */

	public static List<String> fromStringList(String stringlist, String seps) {
		ArrayList<String> result = new ArrayList<String>();
		if (stringlist != null) {
			char[] separators = seps.toCharArray();
			int seplen = separators.length;
			char[] chars = stringlist.toCharArray();
			boolean token = false;
			int offset = 0;
			int l = chars.length;
			for (int i = 0; i < l; i++) {
				char c = chars[i];
				if (token) {
					for (int j = 0; j < seplen; j++)
						if (c != separators[j]) {
							token = false;
							offset = i;
							break;
						}
				}
				if (!token)
					for (int j = 0; j < seplen; j++)
						if (c == separators[j]) {
							addToResult(chars, offset, i, separators, result);
							token = true;
							break;
						}
			}
			if (!token)
				addToResult(chars, offset, l, separators, result);
		}
		return result;
	}

	private static void addToResult(char[] chars, int offset, int end, char[] separators, ArrayList<String> result) {
		while (end > offset && chars[end - 1] == ' ')
			--end;
		while (offset < end && chars[offset] == ' ')
			++offset;
		if (offset < end)
			result.add(new String(chars, offset, end - offset));
	}

	public static int computeHoverTime(int nchars) {
		return hoverBaseTime + nchars * hoverTimePerChar;
	}

	public static void setHoverTiming(int dt, int bt, int ct) {
		hoverDelay = dt;
		hoverBaseTime = bt;
		hoverTimePerChar = ct;
	}

	public static long getHoverDelay() {
		return hoverDelay;
	}

	public static String decode(String string) {
		if (string != null && !string.isEmpty()) {
			if (!string.startsWith(prefix))
				return string;
			try {
				getCipher().init(Cipher.DECRYPT_MODE, getAesKey());
				return new String(cipher.doFinal(string.substring(prefix.length()).getBytes()));
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException e) {
				// should never happen
			}
		}
		return ""; //$NON-NLS-1$
	}

	private static Key getAesKey() {
		if (aesKey == null)
			aesKey = new SecretKeySpec(key.getBytes(), "AES"); //$NON-NLS-1$
		return aesKey;
	}

	private static Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
		if (cipher == null)
			cipher = Cipher.getInstance("AES"); //$NON-NLS-1$
		return cipher;
	}

	public static String encode(String text) {
		if (text != null && !text.isEmpty())
			try {
				// encrypt the text
				getCipher().init(Cipher.ENCRYPT_MODE, getAesKey());
				return prefix + new String(cipher.doFinal(text.getBytes()));
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException e) {
				// should never happen
			}
		return ""; //$NON-NLS-1$
	}

}
