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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.jna.ext;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class KernelFunctions {
	interface Kernel32 extends Library {

		public static int FORMAT_MESSAGE_FROM_SYSTEM = 4096;
		public static int FORMAT_MESSAGE_IGNORE_INSERTS = 512;

		public int GetLongPathNameA(String longname, byte[] out, int len);

		public int GetShortPathNameA(String shortName, byte[] out, int len);

		public int GetLastError();

		public int FormatMessageW(int dwFlags, Pointer lpSource,
				int dwMessageId, int dwLanguageId, char[] lpBuffer, int nSize,
				Pointer Arguments);
	}

	private static String getLastError(Kernel32 lib) {
		int dwMessageId = lib.GetLastError();
		char[] lpBuffer = new char[1024];
		int lenW = lib.FormatMessageW(Kernel32.FORMAT_MESSAGE_FROM_SYSTEM
				| Kernel32.FORMAT_MESSAGE_IGNORE_INSERTS, null, dwMessageId, 0,
				lpBuffer, lpBuffer.length, null);
		return new String(lpBuffer, 0, lenW);
	}

	public static File getLongPathName(File shortPath) throws IOException {
		String cs = Charset.defaultCharset().name();
		Kernel32 lib = (Kernel32) Native
				.loadLibrary("kernel32", Kernel32.class); //$NON-NLS-1$
		byte[] out = new byte[256];
		String shortPathName = shortPath.getPath();
		int l = lib.GetLongPathNameA(shortPathName, out, out.length);
		if (l > 0)
			try {
				String longPathName = new String(out, 0, l, cs);
				return shortPathName.equals(longPathName) ? shortPath
						: new File(longPathName);
			} catch (UnsupportedEncodingException e) {
				// should never happen
			}
		else
			throw new IOException(getLastError(lib));
		return shortPath;
	}

	public static File getShortPathName(File longPath) throws IOException {
		Kernel32 lib = (Kernel32) Native
				.loadLibrary("kernel32", Kernel32.class); //$NON-NLS-1$
		byte[] out = new byte[256];
		String longPathName = longPath.getPath();
		int l = lib.GetShortPathNameA(longPathName, out, out.length);
		if (l > 0)
			try {
				String shortPathName = new String(out, 0, l, "ISO-8859-1"); //$NON-NLS-1$
				return longPathName.equals(longPathName) ? longPath : new File(
						shortPathName);
			} catch (UnsupportedEncodingException e) {
				// should never happen
			}
		else
			throw new IOException(getLastError(lib));
		return longPath;
	}

}
