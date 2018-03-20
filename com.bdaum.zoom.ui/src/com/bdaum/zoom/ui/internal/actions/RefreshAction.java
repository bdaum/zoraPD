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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.SynchronizeOperation;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.RefreshDialog;
import com.bdaum.zoom.ui.internal.views.CatalogView;

@SuppressWarnings("restriction")
public class RefreshAction extends Action {

	List<Asset> missingFiles = new ArrayList<Asset>(100);
	List<File> outdatedFiles = new ArrayList<File>(200);
	List<File> otherFiles = new ArrayList<File>(200);
	List<URI> uris = new ArrayList<URI>(100);
	private IAdaptable adaptable;
	private Shell shell;

	public RefreshAction(String label, String tooltip, ImageDescriptor image, IAdaptable adaptable) {
		super(label, image);
		this.adaptable = adaptable;
		shell = adaptable.getAdapter(Shell.class);
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		missingFiles.clear();
		outdatedFiles.clear();
		otherFiles.clear();
		uris.clear();
		ICore core = Core.getCore();
		IVolumeManager volumeManager = core.getVolumeManager();
		List<Asset> selectedAssets = getSelectedAssets();
		if (selectedAssets != null)
			for (Asset asset : selectedAssets) {
				URI uri = volumeManager.findFile(asset);
				if (uri != null) {
					if (Constants.FILESCHEME.equals(uri.getScheme())) {
						checkFile(new File(uri), asset);
					} else {
						uris.add(uri);
					}
				}
			}
		if (missingFiles.isEmpty() && outdatedFiles.isEmpty() && otherFiles.isEmpty()) {
			AcousticMessageDialog.openInformation(shell, Messages.RefreshAction_synchronize,
					Messages.RefreshAction_nothing_to_refresh);
			return;
		}
		RefreshDialog dialog = new RefreshDialog(shell, otherFiles.size(), outdatedFiles.size(), missingFiles.size(),
				uris.size());

		if (dialog.open() == Window.OK) {
			List<File> existingFiles = null;
			if (dialog.isReImport()) {
				existingFiles = new ArrayList<File>(outdatedFiles.size() + otherFiles.size());
				existingFiles.addAll(outdatedFiles);
				if (dialog.isRefresh())
					existingFiles.addAll(otherFiles);
			}
			SynchronizeOperation op = new SynchronizeOperation(
					existingFiles == null ? null : new FileInput(existingFiles, false),
					UiActivator.getDefault().createImportConfiguration(adaptable, true, dialog.isResetImage(),
							dialog.isResetStatus(), dialog.isResetExif(), dialog.isResetIptc(), dialog.isResetGps(),
							dialog.isResetFaceData(), true, false),
					dialog.isIncludeRemote() ? uris : null, dialog.isDelete() ? missingFiles : null);
			OperationJob.executeOperation(op, adaptable);
		}
		missingFiles.clear();
		outdatedFiles.clear();
		otherFiles.clear();
	}

	private boolean checkFile(File file, Asset assetImpl) {
		if (file.exists())
			(BatchUtilities.getImageFileModificationTimestamp(file) > assetImpl.getImportDate().getTime()
					? outdatedFiles
					: otherFiles).add(file);
		else
			missingFiles.add(assetImpl);
		return false;
	}

	public List<Asset> getSelectedAssets() {
		AssetSelection selection = adaptable.getAdapter(AssetSelection.class);
		if (selection != null && !selection.isEmpty())
			return selection.getLocalAssets();
		final List<Asset> assets = new ArrayList<Asset>(1000);
		IWorkbenchPage page = adaptable.getAdapter(IWorkbenchPage.class);
		CatalogView catView = (CatalogView) page.findView(CatalogView.ID);
		if (catView != null) {
			final IStructuredSelection sel = (IStructuredSelection) catView.getSelection();
			BusyIndicator.showWhile(shell.getDisplay(), () -> {
				IDbManager dbManager = Core.getCore().getDbManager();
				for (Iterator<?> iterator = sel.iterator();iterator.hasNext();) {
					Object object = iterator.next();
					if (object instanceof SmartCollectionImpl)
						assets.addAll(dbManager
								.createCollectionProcessor(Utilities.localizeSmartCollection((SmartCollection) object))
								.select(false));
				}
			});
		}
		return assets;
	}
}
