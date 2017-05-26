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

package com.bdaum.zoom.ui.internal.widgets;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.PathEditor;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;

/**
 * A field editor to edit directory paths.
 */
public class VolumePathEditor extends PathEditor {

	private static final String VOLEND = "}"; //$NON-NLS-1$
	private static final String VOLSEP = " -> {"; //$NON-NLS-1$

	/**
	 * Creates a new path field editor
	 */
	protected VolumePathEditor() {
	}

	/**
	 * Creates a path field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param dirChooserLabelText
	 *            the label text displayed for the directory chooser
	 * @param parent
	 *            the parent of the field editor's control
	 */
	public VolumePathEditor(String name, String labelText,
			String dirChooserLabelText, Composite parent) {
		super(name, labelText, dirChooserLabelText, parent);
	}

	/*
	 * (non-Javadoc) Method declared on ListEditor. Creates a single string from
	 * the given array by separating each string with the appropriate
	 * OS-specific path separator.
	 */

	@Override
	protected String createList(String[] items) {
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < items.length; i++) {
			String path = items[i];
			String volume = null;
			int p = path.lastIndexOf(VOLSEP);
			if (p > 0 && path.endsWith(VOLEND)) {
				volume = path.substring(p + VOLSEP.length(), path.length() - 1);
				path = path.substring(0, p);
			}
			if (volume == null) {
				File file = new File(items[i]);
				volume = volumeManager.rootToVolume(volumeManager
						.getRootFile(file));
			}
			sb.append(path);
			sb.append(File.pathSeparator);
			if (volume != null)
				sb.append(volume);
			else
				sb.append("*"); //$NON-NLS-1$
			sb.append(File.pathSeparator);
		}
		return sb.toString();
	}

	@Override
	protected String getNewInputObject() {
		String dir = super.getNewInputObject();
		File file = new File(dir);
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		String volume = volumeManager.rootToVolume(volumeManager
				.getRootFile(file));
		if (volume != null)
			dir += VOLSEP + volume + VOLEND;
		return dir;
	}

	/*
	 * (non-Javadoc) Method declared on ListEditor.
	 */

	@Override
	protected String[] parseString(String stringList) {
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator
				+ "\n\r");//$NON-NLS-1$
		ArrayList<String> v = new ArrayList<String>();
		String dir = null;
		int i = 0;
		while (st.hasMoreTokens()) {
			if ((i++ % 2) == 0)
				dir = st.nextToken();
			else {
				String volume = st.nextToken();
				while (volume.startsWith("{")) //$NON-NLS-1$
					volume = volume.substring(1);
				File root = volumeManager.volumeToRoot(volume);
				if (root != null) {
					File file = new File(dir);
					File oldRoot = volumeManager.getRootFile(file);
					File newFile = new File(root, file.getAbsolutePath()
							.substring(oldRoot.getAbsolutePath().length()));
					dir = newFile.getAbsolutePath();
				}
				if (!"*".equals(volume)) //$NON-NLS-1$
					dir += VOLSEP + volume + VOLEND;
				v.add(dir);
			}
		}
		return v.toArray(new String[v.size()]);
	}
}
