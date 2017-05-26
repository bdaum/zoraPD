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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.internal.peer.ConnectionLostException;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.MoveOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class MoveAction extends AbstractSelectionAction {

	private IAdaptable adaptable;

	public MoveAction(IWorkbenchWindow window, String label, String tooltip, ImageDescriptor image,
			IAdaptable adaptable) {
		super(window, label, image);
		this.adaptable = adaptable;
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		List<Asset> selectedAssets = adaptable.getAdapter(AssetSelection.class).getAssets();
		IPeerService peerService = Core.getCore().getPeerService();
		int size = selectedAssets.size();
		List<Asset> errands = new ArrayList<Asset>(size);
		List<Asset> assets = new ArrayList<Asset>(size);
		for (Asset asset : selectedAssets) {
			if (asset.getFileState() == IVolumeManager.PEER) {
				if (peerService == null) {
					errands.add(asset);
					continue;
				}
				boolean valid = false;
				try {
					valid = peerService.checkCredentials(IPeerService.COPY, asset.getSafety(),
							peerService.getAssetOrigin(asset.getStringId()));
				} catch (ConnectionLostException e) {
					errands.add(asset);
					continue;
				}
				if (!valid) {
					errands.add(asset);
					continue;
				}
			}
			assets.add(asset);
		}
		if (!errands.isEmpty())
			AcousticMessageDialog.openInformation(window.getShell(), Messages.MoveFilesAction_move,
					errands.size() == 1 ? NLS.bind(Messages.MoveFilesAction_item_not_copied, errands.get(0).getName())
							: NLS.bind(Messages.MoveFilesAction_n_items_not_copied, errands.size()));
		if (!assets.isEmpty()) {
			UiActivator activator = UiActivator.getDefault();
			DirectoryDialog dialog = new DirectoryDialog(window.getShell(), SWT.NONE);
			dialog.setText(Messages.MoveFilesAction_move_to_folder);
			dialog.setFilterPath(activator.getInputFolderLocation());
			dialog.setMessage(Messages.MoveFilesAction_select_target_folder);
			String file = dialog.open();
			if (file != null) {
				activator.setInputFolderLocation(dialog.getFilterPath());
				OperationJob.executeOperation(new MoveOperation(assets, new File(file)), adaptable);
			}
		}
	}

}
