/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 * It is an adaptation of the equally named file from the jUploadr project (http://sourceforge.net/projects/juploadr/)
 * (c) 2004 Steve Cohen and others
 *
 * jUploadr is licensed under the GNU Library or Lesser General Public License (LGPL).
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
 * Modifications (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package org.scohen.juploadr.app;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringSigner {
    private static MessageDigest md5;
    private static final char[] hexChars = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static String md5(String toSign) {
    	if (md5 == null) {
    		try {
                md5 = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
            } catch (NoSuchAlgorithmException e) {
                // this can't really happen
            }
    	}
        return convertToHexa(md5.digest(convertToBytes(toSign)));
    }

	private static byte[] convertToBytes(String toSign) {
		byte[] stringBytes;
        try {
            stringBytes = toSign.getBytes("UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            stringBytes = toSign.getBytes();
        }
		return stringBytes;
	}

	private static String convertToHexa(byte[] hashed) {
		StringBuffer rv = new StringBuffer();
        for (int i = 0; i < hashed.length; i++) {
            rv.append(hexChars[0x00000F & hashed[i] >> 4]);
            rv.append(hexChars[0x00000F & hashed[i]]);
        }
        return rv.toString();
	}

}
