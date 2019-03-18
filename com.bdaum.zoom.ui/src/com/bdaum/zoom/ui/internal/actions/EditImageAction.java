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

package com.bdaum.zoom.ui.internal.actions;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.program.Program;

import com.bdaum.zoom.ui.internal.preferences.EditorDescriptor;

public class EditImageAction extends EditWithAction {

	public EditImageAction(String label, String tooltip, ImageDescriptor image, IAdaptable adaptable) {
		super(label, tooltip, image, adaptable);
	}

	protected void selectAndLaunchEditor(List<String> parms) {
		if (lastEditor != null && lastEditor != MIXED) {
			EditorDescriptor editor = null;
			if (lastEditor.startsWith(">")) { //$NON-NLS-1$
				String pname = lastEditor.substring(1);
				Program[] programs = Program.getPrograms();
				for (Program program : programs)
					if (pname.equals(program.getName())) {
						editor = EditorDescriptor.createForProgram(program);
						break;
					}
			} else
				editor = EditorDescriptor.createForProgram(lastEditor);
			if (editor != null)
				launchEditor(editor, parms, assetSelection.getAssets());
		} else
			super.selectAndLaunchEditor(parms);
	}

	protected EditorDescriptor showDialog(final String ext) {
		return defaultEditors.size() == 1 ? (EditorDescriptor) defaultEditors.toArray()[0] : super.showDialog(ext);
	}
}
