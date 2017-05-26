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

package com.bdaum.zoom.program;

import org.eclipse.core.runtime.Platform;

import com.bdaum.zoom.image.ImageConstants;

public interface BatchConstants {
	// Application
	public final static String APPLICATION_NAME = ImageConstants.APPLICATION_NAME;
	public static final String APPNAME = ImageConstants.APPNAME;
	public static final String APP_PREFERENCES = "ZoraPD_Preferences.zpf"; //$NON-NLS-1$
	// Platforms
	public static final boolean WIN32 = Platform.getOS().equals(
			Platform.OS_WIN32);
	public static final boolean OSX = Platform.getOS().equals(
			Platform.OS_MACOSX);
	public static final boolean LINUX = Platform.getOS().equals(
			Platform.OS_LINUX);
	// Catalog
	public static final String CATFILETYPE = "com.bdaum.zoom.cat"; //$NON-NLS-1$
	public static final String CATEXTENSION = ".zdb"; //$NON-NLS-1$
	public static final String CATMIME = "application/x-zdb"; //$NON-NLS-1$
	// Files
	public static final int MAXPATHLENGTH = OSX ? 1023 : LINUX ? 4096 : 255;
	// Jobs
	public static final long MAXTEMPFILEAGE = 30 * 1000L;
	public static final String DAEMONS = "com.bdaum.zoom.demon"; //$NON-NLS-1$;
	public static final String DROPINFOLDER = "dropins"; //$NON-NLS-1$

}
