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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.webserver.internals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.db.ITypeFilter;
import com.bdaum.zoom.webserver.PreferenceConstants;

import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;

final class IndexContextHandler extends AbstractContextHandler {

	public int serve(Request req, Response resp) throws IOException {
		String path = req.getPath();
		String content = null;
		String ctype = "text/html"; //$NON-NLS-1$
		Map<String, String> substitutions = new HashMap<String, String>();
		substitutions.put("{$appName}", Constants.APPLICATION_NAME); //$NON-NLS-1$
		substitutions.put("{$orgLink}", System.getProperty("com.bdaum.zoom.homePage")); //$NON-NLS-1$ //$NON-NLS-2$
		String startPage = preferenceStore.getString(PreferenceConstants.STARTPAGE);
		substitutions.put("{$imagesTitle}", toHtml(preferenceStore.getString(PreferenceConstants.IMAGESTITLE), false)); //$NON-NLS-1$
		substitutions.put("{$exhibitionsTitle}", //$NON-NLS-1$
				toHtml(preferenceStore.getString(PreferenceConstants.EXHIBITIONSTITLE), false));
		substitutions.put("{$webgalleriesTitle}", //$NON-NLS-1$
				toHtml(preferenceStore.getString(PreferenceConstants.GALLERIESTITLE), false));
		substitutions.put("{$slideshowsTitle}", //$NON-NLS-1$
				toHtml(preferenceStore.getString(PreferenceConstants.SLIDESHOWSTITLE), false));
		substitutions.put("{$$images}", preferenceStore.getBoolean(PreferenceConstants.IMAGES) ? null : ""); //$NON-NLS-1$ //$NON-NLS-2$
		substitutions.put("{$$exhibitions}", //$NON-NLS-1$
				preferenceStore.getBoolean(PreferenceConstants.EXHIBITIONS) ? null : ""); //$NON-NLS-1$
		substitutions.put("{$$galleries}", preferenceStore.getBoolean(PreferenceConstants.GALLERIES) ? null : ""); //$NON-NLS-1$ //$NON-NLS-2$
		substitutions.put("{$$slideshows}", preferenceStore.getBoolean(PreferenceConstants.SLIDESHOWS) ? null : ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (path.endsWith('/' + startPage)) {
			substitutions.put("{$imagesPath}", preferenceStore.getString(PreferenceConstants.IMAGEPATH)); //$NON-NLS-1$
			substitutions.put("{$exhibitionsPath}", preferenceStore.getString(PreferenceConstants.EXHIBITIONPATH)); //$NON-NLS-1$
			substitutions.put("{$webgalleriesPath}", preferenceStore.getString(PreferenceConstants.GALLERYPATH)); //$NON-NLS-1$
			substitutions.put("{$slideshowsPath}", preferenceStore.getString(PreferenceConstants.SLIDESHOWSPATH)); //$NON-NLS-1$
			content = WebserverActivator.getDefault().compilePage(PreferenceConstants.INDEX, substitutions);
		} else if (path.endsWith('/' + PreferenceConstants.HELP)) {
			substitutions.put("{$$addons}", preferenceStore.getBoolean(PreferenceConstants.ALLOWUPLOADS) ? null : ""); //$NON-NLS-1$ //$NON-NLS-2$
			substitutions.put("{$$infoAction}", //$NON-NLS-1$
					preferenceStore.getBoolean(PreferenceConstants.METADATA) ? null : ""); //$NON-NLS-1$
			substitutions.put("{$$fullAction}", //$NON-NLS-1$
					preferenceStore.getBoolean(PreferenceConstants.FULLSIZE) ? null : ""); //$NON-NLS-1$
			substitutions.put("{$$downloadAction}", //$NON-NLS-1$
					preferenceStore.getBoolean(PreferenceConstants.DOWNLOAD) ? null : ""); //$NON-NLS-1$
			substitutions.put("{$$geoAction}", //$NON-NLS-1$
					preferenceStore.getBoolean(PreferenceConstants.GEO) ? null : ""); //$NON-NLS-1$
			int formats = preferenceStore.getInt(PreferenceConstants.FORMATS);
			if (formats == ITypeFilter.ALLIMAGEFORMATS || formats == ITypeFilter.ALLFORMATS)
				substitutions.put("{$$formats}", "");  //$NON-NLS-1$//$NON-NLS-2$
			else {
				substitutions.put("{$$formats}", null);  //$NON-NLS-1$
				substitutions.put("{$formatlist}", createFormatList(formats));  //$NON-NLS-1$
			}
			content = WebserverActivator.getDefault().compilePage(PreferenceConstants.HELP, substitutions);
		} else if (path.endsWith("/zoraPD.css")) { //$NON-NLS-1$
			content = preferenceStore.getString(PreferenceConstants.CSS);
			ctype = "text/css"; //$NON-NLS-1$
		}
		return sendDynamicContent(resp, content, ctype);
	}

	private static String createFormatList(int formats) {
		StringBuilder sb = new StringBuilder();
		addType(formats, sb, ITypeFilter.DNG, "DNG"); //$NON-NLS-1$
		addType(formats, sb, ITypeFilter.JPEG, "JPEG"); //$NON-NLS-1$
		addType(formats, sb, ITypeFilter.RAW, "RAW"); //$NON-NLS-1$
		addType(formats, sb, ITypeFilter.TIFF, "TIFF"); //$NON-NLS-1$
		return sb.toString();
	}

	private static void addType(int formats, StringBuilder sb, int type, String label) {
		if ((formats & type) != 0) {
			if (sb.length() > 0)
				sb.append(", "); //$NON-NLS-1$
			sb.append(label);
		}
	}

}