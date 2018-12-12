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

package com.bdaum.zoom.ui.internal.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.VoiceNoteOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.VoiceNoteDialog;

@SuppressWarnings("restriction")
public class AddVoiceNoteAction extends Action {

	private IAdaptable adaptable;
	private IWorkbenchWindow window;

	public AddVoiceNoteAction(IWorkbenchWindow window, String label, String tooltip, ImageDescriptor image,
			IAdaptable adaptable) {
		super(label, image);
		this.window = window;
		this.adaptable = adaptable;
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		Asset asset = adaptable.getAdapter(AssetSelection.class).getFirstElement();
		if (asset != null) {
			UiActivator.getDefault().stopAudio();
			VoiceNoteDialog dialog = new VoiceNoteDialog(window.getShell(), asset);
			if (dialog.open() == VoiceNoteDialog.OK)
				OperationJob.executeOperation(new VoiceNoteOperation(asset, dialog.getSourceUri(), dialog.getTargetUri(), dialog.getNoteText(), dialog.getSvg()),
							adaptable);
		}
	}
}
