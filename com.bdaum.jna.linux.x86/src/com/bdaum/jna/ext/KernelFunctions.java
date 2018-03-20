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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.jna.ext;

import java.io.File;
import java.io.IOException;

import com.sun.jna.Library;
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

	@SuppressWarnings("unused")
	public static File getLongPathName(File shortPath) throws IOException {
		return shortPath;
	}

	@SuppressWarnings("unused")
	public static File getShortPathName(File longPath) throws IOException {
		return longPath;
	}

}
