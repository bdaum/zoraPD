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

import java.util.ArrayList;
import java.util.List;

public class CommonUtilities {

	private static int hoverDelay = 200;
	private static int hoverBaseTime = 1000;
	private static int hoverTimePerChar = 25;

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
						if (c == separators[j]) {
							int end = i;
							while (end > offset && chars[end - 1] == ' ')
								--end;
							while (offset < end && chars[offset] == ' ')
								++offset;
							result.add(new String(chars, offset, end - offset));
							token = false;
							break;
						}
				} else
					for (int j = 0; j < seplen; j++)
						if (c == separators[j]) {
							token = true;
							offset = i;
							break;
						}
			}
			if (token) {
				while (l > offset && chars[l - 1] == ' ')
					--l;
				while (offset < l && chars[offset] == ' ')
					++offset;
				result.add(new String(chars, offset, l - offset));
			}
		}
		return result;
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

}
