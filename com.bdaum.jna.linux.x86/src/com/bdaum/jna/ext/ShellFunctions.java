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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import com.bdaum.jna.internal.JnaActivator;

public class ShellFunctions {

	private static final String USERNAME = "{username}"; //$NON-NLS-1$
	private static final String USERHOME = "{userhome}"; //$NON-NLS-1$

	public static File getPicasaAppData() {
		String userhome = System.getProperty("user.home"); //$NON-NLS-1$
		String username = System.getProperty("user.name"); //$NON-NLS-1$
		URL iniUri = FileLocator.find(JnaActivator.getDefault().getBundle(),
				new Path("/picasaContacts.ini"), null); //$NON-NLS-1$
		try {
			iniUri = FileLocator.toFileURL(iniUri);
		} catch (IOException e) {
			return null;
		}
		try (BufferedReader r = new BufferedReader(new FileReader(new File(iniUri.getPath())))) {
			String line;
			while ((line = r.readLine()) != null) {
				if (line.startsWith("#")) //$NON-NLS-1$
					continue;
				line = line.trim();
				StringBuilder sb = new StringBuilder(line);
				int p = sb.indexOf(USERHOME);
				if (p >= 0)
					sb.replace(p, p + USERHOME.length(), userhome);
				p = sb.indexOf(USERNAME);
				if (p >= 0)
					sb.replace(p, p + USERNAME.length(), username);
				File result = new File(sb.toString());
				if (result.exists())
					return result;
			}
		} catch (Exception e) {
			return null;
		}
		//		String userhome = System.getProperty("user.home"); //$NON-NLS-1$
		//		String username = System.getProperty("user.name"); //$NON-NLS-1$
		//		String path = userhome + "/.wine/drive_c/users/" + username //$NON-NLS-1$
		//				+ "/Local Settings/Application Data/Google/Picasa2"; //$NON-NLS-1$
		// File result = new File(path);
		// if (result.exists())
		// return result;
		//		path = userhome + "/.google/picasa/3.0/drive_c/Documents and Settings/" //$NON-NLS-1$
		//				+ username + "/Local Settings/Application Data/Google/Picasa2"; //$NON-NLS-1$
		// result = new File(path);
		// if (result.exists())
		// return result;
		return null;
	}

}
