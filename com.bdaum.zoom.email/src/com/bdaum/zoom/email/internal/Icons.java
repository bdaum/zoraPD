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
package com.bdaum.zoom.email.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Icons {

	public static class Icon {

		private static final String prefix = Activator.PLUGIN_ID + ".icn_"; //$NON-NLS-1$
		private static int counter = 0;
		private final String key;

		public Icon(String path) {
			JFaceResources.getImageRegistry().put(key = prefix + (++counter),
					AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, path));
		}

		public ImageDescriptor getDescriptor() {
			return JFaceResources.getImageRegistry().getDescriptor(key);
		}

		public Image getImage() {
			return JFaceResources.getImage(key);
		}

	}

	public static final Icon pdfemail64 = new Icon("icons/banner/pdfemail64.png"); //$NON-NLS-1$
	public static final Icon email64 = new Icon("icons/banner/email64.png"); //$NON-NLS-1$
	public static final Icon pdf64 = new Icon("icons/banner/pdf64.png"); //$NON-NLS-1$
}
