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

package com.bdaum.zoom.video.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

public class Icons {

	public static class Icon {

		private static final String prefix = VideoActivator.PLUGIN_ID + ".icn_"; //$NON-NLS-1$
		private static int counter = 0;

		private String key;
		private String path;

		public Icon(String path) {
			this.path = path;
		}

		private String getKey() {
			if (key == null) {
				key = prefix + (++counter);
				JFaceResources.getImageRegistry().put(key,
						VideoActivator.getImageDescriptor(path));
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

	public static final Icon video40 = new Icon("icons/video40.png"); //$NON-NLS-1$
	public static final Icon volume = new Icon("/icons/banner/loudness.png"); //$NON-NLS-1$
	public static final Icon volume_d = new Icon("/icons/banner/loudness_d.png"); //$NON-NLS-1$
	public static final Icon play = new Icon("/icons/play.png"); //$NON-NLS-1$
	public static final Icon stop = new Icon("/icons/stop.png"); //$NON-NLS-1$
	public static final Icon sound = new Icon("/icons/sound.png"); //$NON-NLS-1$
	public static final Icon soundMute = new Icon("/icons/sound_mute.png"); //$NON-NLS-1$
	public static final Icon snapshot = new Icon("/icons/snapshot.png"); //$NON-NLS-1$
}
