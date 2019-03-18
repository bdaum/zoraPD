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
 * (c) 2009-2019 Berthold Daum  
 */

package com.bdaum.zoom.gps.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

import com.bdaum.zoom.common.internal.FileLocator;

public class Icons {

	public static class Icon {

		private static String iconFolder;

		static {
			try {
				URL url = FileLocator.findFileURL(GpsActivator.getDefault().getBundle(), "icons", true); //$NON-NLS-1$
				iconFolder = url == null ? null : url.toString();
			} catch (IOException e) {
				// leave iconFolder null
			}
		}

		private static final String prefix = GpsActivator.PLUGIN_ID + ".icn_"; //$NON-NLS-1$
		private static int counter = 0;
		private String key;
		private String path;

		public Icon(String path) {
			this.path = path;
		}

		private String getKey() {
			if (key == null) {
				ImageDescriptor imageDescriptor;
				if (iconFolder == null)
					imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
				else
					try {
						imageDescriptor = ImageDescriptor.createFromURL(new URL(iconFolder + path));
					} catch (MalformedURLException e) {
						imageDescriptor = ImageDescriptor.getMissingImageDescriptor();
					}
				JFaceResources.getImageRegistry().put(key = prefix + (++counter), imageDescriptor);
				path = null;
			}
			return key;
		}

		public ImageDescriptor getDescriptor() {
			return JFaceResources.getImageRegistry().getDescriptor(getKey());
		}

		public Image getImage() {
			return JFaceResources.getImage(getKey());
		}
	}

	public static final Icon external = new Icon("external.png"); //$NON-NLS-1$
	public static final Icon refresh = new Icon("refresh.png"); //$NON-NLS-1$
	public static final Icon pin = new Icon("pin.png"); //$NON-NLS-1$
	public static final Icon forward = new Icon("forward_nav.gif"); //$NON-NLS-1$
	public static final Icon backward = new Icon("backward_nav.gif"); //$NON-NLS-1$
	public static final Icon camPin = new Icon("campin.png"); //$NON-NLS-1$
	public static final Icon dirPin = new Icon("dirpin.png"); //$NON-NLS-1$
	public static final Icon shownPin = new Icon("shownPin.png"); //$NON-NLS-1$
	public static final Icon delete = new Icon("delete32.png"); //$NON-NLS-1$
	public static final Icon gpx64 = new Icon("gpx64.png"); //$NON-NLS-1$
	public static final Icon kml64 = new Icon("kml64.png"); //$NON-NLS-1$
}
