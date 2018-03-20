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

package com.bdaum.zoom.gps.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Icons {

	public static class Icon {

		private static final String prefix = GpsActivator.PLUGIN_ID + ".icn_"; //$NON-NLS-1$
		private static int counter = 0;
		private final String key;

		public Icon(String path) {
			key = prefix + (++counter);
			JFaceResources.getImageRegistry().put(
					key,
					AbstractUIPlugin.imageDescriptorFromPlugin(GpsActivator.PLUGIN_ID,
							path));
		}

		public ImageDescriptor getDescriptor() {
			return JFaceResources.getImageRegistry().getDescriptor(key);
		}

		public Image getImage() {
			return JFaceResources.getImage(key);
		}

	}


	public static final Icon external = new Icon("/icons/external.png"); //$NON-NLS-1$
	public static final Icon refresh = new Icon("/icons/refresh.png"); //$NON-NLS-1$
	public static final Icon pin = new Icon("/icons/pin.png"); //$NON-NLS-1$
	public static final Icon forward = new Icon("/icons/forward_nav.gif"); //$NON-NLS-1$
	public static final Icon backward = new Icon("/icons/backward_nav.gif"); //$NON-NLS-1$
	public static final Icon camPin = new Icon("/icons/campin.png"); //$NON-NLS-1$
	public static final Icon dirPin = new Icon("/icons/dirpin.png"); //$NON-NLS-1$
	public static final Icon shownPin = new Icon("/icons/shownPin.png"); //$NON-NLS-1$
	public static final Icon delete = new Icon("/icons/delete32.png"); //$NON-NLS-1$
}
