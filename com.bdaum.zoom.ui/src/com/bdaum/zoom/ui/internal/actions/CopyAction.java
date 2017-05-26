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

package com.bdaum.zoom.ui.internal.actions;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiUtilities;

public class CopyAction extends AbstractSelectionAction {

	private Clipboard clipboard;

	public CopyAction(IWorkbenchWindow window, Clipboard clipboard) {
		super(window, Messages.CopyAction_copy, Icons.copy.getDescriptor());
		this.clipboard = clipboard;
	}

	@Override
	public void run() {
		if (selection != null) {
			ICore core = Core.getCore();
			IVolumeManager volumeManager = core.getVolumeManager();
			List<String> files = new ArrayList<String>(selection.size());
			List<Asset> errands = new ArrayList<Asset>();
			for (Asset asset : selection.getAssets()) {
				if (asset.getFileState() == IVolumeManager.PEER) {
					errands.add(asset);
					continue;
				}
				URI uri = volumeManager.findFile(asset);
				if (uri != null && Constants.FILESCHEME.equals(uri.getScheme())) {
					if (volumeManager.findExistingFile(asset, true) != null)
						files.add((new File(uri)).getAbsolutePath());
					else
						errands.add(asset);
				}
			}
			String[] str = files.toArray(new String[files.size()]);
			Shell shell = window.getShell();
			try {
				clipboard.setContents(new Object[] { str },
						new Transfer[] { FileTransfer.getInstance() });

			} catch (SWTError ex) {
				if (ex.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
					core.logError(Messages.CopyAction_Error_when_copying, ex);
				else {
					AcousticMessageDialog.openWarning(shell,
							Messages.CopyAction_Problem_copying,
							Messages.CopyAction_Problem_accessing);
				}
			}
			if (!errands.isEmpty())
				UiUtilities.showFilesAreOffline(shell, errands,
						errands.size() == selection.size(),
						Messages.CopyAction_files_offline);
		}
	}

}
