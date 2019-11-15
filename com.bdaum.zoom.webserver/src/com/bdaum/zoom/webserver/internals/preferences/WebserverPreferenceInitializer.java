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
package com.bdaum.zoom.webserver.internals.preferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ITypeFilter;
import com.bdaum.zoom.webserver.PreferenceConstants;
import com.bdaum.zoom.webserver.internals.WebserverActivator;

public class WebserverPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences defaultNode = DefaultScope.INSTANCE.getNode(WebserverActivator.PLUGIN_ID);
		defaultNode.put(PreferenceConstants.PORT, "8080"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.STARTPAGE, "index.html"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.IMAGEPATH, "images"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.EXHIBITIONPATH, "exhibitions"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.GALLERYPATH, "webgalleries"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.SLIDESHOWSPATH, "slideshows"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.IMAGESTITLE, "Images"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.EXHIBITIONSTITLE, "Exhibitions"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.GALLERIESTITLE, "Web galleries"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.SLIDESHOWSTITLE, "Slideshows"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.AUTOSTART, "true"); //$NON-NLS-1$
		StringBuilder sbh = new StringBuilder();
		for (QueryField qfield : QueryField.getQueryFields())
			if (qfield.hasLabel() && qfield.getChildren().length == 0 && qfield.isHover() && qfield.isEssential())
				sbh.append(qfield.getId()).append('\n');
		defaultNode.put(PreferenceConstants.WEBMETADATA, sbh.toString());
		defaultNode.put(PreferenceConstants.THUMBNAILSPERPAGE, "15"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.PRIVACY, String.valueOf(QueryField.SAFETY_SAFE));
		defaultNode.put(PreferenceConstants.FORMATS, String.valueOf(ITypeFilter.JPEG));
		defaultNode.put(PreferenceConstants.IMAGES, "true"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.EXHIBITIONS, "true"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.GALLERIES, "true"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.SLIDESHOWS, "true"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.METADATA, "true"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.FULLSIZE, "true"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.DOWNLOAD, "true"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.GEO, "true"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.ORPHANS, "true"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.GROUPS,
				String.valueOf(PreferenceConstants.AUTOMATIC | PreferenceConstants.CATEGERIES
						| PreferenceConstants.DIRECTORIES | PreferenceConstants.IMPORTS | PreferenceConstants.LOCATIONS
						| PreferenceConstants.MEDIA | PreferenceConstants.PERSONS | PreferenceConstants.RATINGS
						| PreferenceConstants.TIMELINE | PreferenceConstants.USER));
		for (String type : PreferenceConstants.HTMLTYPES)
			defaultNode.put(type, loadFile(type));
		defaultNode.put(PreferenceConstants.CSS, loadFile("zoraPD.css")); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.ALLOWMETADATAMOD, "true"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.APPLY_SHARPENING, "true"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.RADIUS, "1.5"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.AMOUNT, "0.25"); //$NON-NLS-1$
		defaultNode.put(PreferenceConstants.THRESHOLD, "2"); //$NON-NLS-1$
	}

	private static String loadFile(String name) {
		StringBuilder sb = new StringBuilder(4000);
		URL url = WebserverActivator.getDefault().findTemplate(name);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
			char[] cbuf = new char[1024];
			while (true) {
				int read = reader.read(cbuf);
				if (read < 0)
					break;
				sb.append(cbuf, 0, read);
			}
		} catch (IOException e) {
			// do not read
		}
		return sb.toString();
	}

}
