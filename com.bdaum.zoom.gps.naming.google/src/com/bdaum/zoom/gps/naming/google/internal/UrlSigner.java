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
 * (c) 2009-2011 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.gps.naming.google.internal;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.bdaum.zoom.net.core.internal.Base64;

public class UrlSigner {

	public static String computeSignature(String urlString, String keyString)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException {

		// Convert the string to a URL so we can parse it
		URL url = new URL(urlString);
		keyString = keyString.replace('-', '+');
		keyString = keyString.replace('_', '/');
		byte[] key = Base64.decode(keyString);
		return computeSignature(url.getPath(), url.getQuery(), key);
	}

	public static String computeSignature(String path, String query, byte[] key)
			throws NoSuchAlgorithmException, InvalidKeyException {

		// Retrieve the proper URL components to sign
		String resource = path + '?' + query;

		// Get an HMAC-SHA1 signing key from the raw key bytes
		SecretKeySpec sha1Key = new SecretKeySpec(key, "HmacSHA1"); //$NON-NLS-1$

		// Get an HMAC-SHA1 Mac instance and initialize it with the HMAC-SHA1
		// key
		Mac mac = Mac.getInstance("HmacSHA1"); //$NON-NLS-1$
		mac.init(sha1Key);

		// compute the binary signature for the request
		byte[] sigBytes = mac.doFinal(resource.getBytes());

		// base 64 encode the binary signature
		String signature = Base64.encodeBytes(sigBytes);

		// convert the signature to 'web safe' base 64
		signature = signature.replace('+', '-');
		signature = signature.replace('/', '_');

		return signature;
	}
}
