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
 * (c) 2014 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.CompoundOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.operations.internal.AddAlbumOperation;
import com.bdaum.zoom.operations.internal.ImportOperation;
import com.bdaum.zoom.operations.internal.RetargetOperation;
import com.bdaum.zoom.operations.internal.VoiceNoteOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.IDropinHandler;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.ImportModeDialog;
import com.bdaum.zoom.ui.internal.wizards.ImportFromDeviceWizard;

@SuppressWarnings("restriction")
public final class ImageDropTargetListener extends EffectDropTargetListener {
	private final IDropHost host;
	private final FileTransfer fileTransfer;
	private final boolean images;
	private final boolean gps;
	private final boolean sound;
	private boolean isFileTransfer;
	private int chosenOperation = DND.DROP_NONE;

	public ImageDropTargetListener(IDropHost host, boolean images, boolean gps, boolean sound, int ops) {
		super(host.getControl());
		this.host = host;
		this.images = images;
		this.gps = gps;
		this.sound = sound;
		final DropTarget target = new DropTarget(host.getControl(), ops);
		fileTransfer = FileTransfer.getInstance();
		target.setTransfer(new Transfer[] { fileTransfer });
		target.addDropListener(this);
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		chosenOperation = event.detail;
		isFileTransfer = false;
		IDragHost dragHost = UiActivator.getDefault().getDragHost();
		if ((dragHost == null || !(dragHost instanceof AbstractGalleryView)) && !host.isDragging()
				&& !Core.getCore().getDbManager().isReadOnly())
			for (int i = 0; i < event.dataTypes.length; i++)
				if (fileTransfer.isSupportedType(event.dataTypes[i])) {
					event.currentDataType = event.dataTypes[i];
					isFileTransfer = true;
					break;
				}
		checkOperation(event);
		super.dragEnter(event);
	}

	public void dragOperationChanged(DropTargetEvent event) {
		chosenOperation = event.detail;
		checkOperation(event);
	}

	@Override
	public void dragOver(DropTargetEvent event) {
		checkOperation(event);
		super.dragOver(event);
	}

	private void checkOperation(DropTargetEvent event) {
		event.detail = DND.DROP_NONE;
		if (isFileTransfer) {
			event.detail = DND.DROP_COPY;
			if ((chosenOperation & DND.DROP_LINK) != 0) {
				Point coord = control.toControl(event.x, event.y);
				Object obj = host.findObject(coord.x, coord.y);
				if (obj == null)
					event.detail = DND.DROP_LINK;
				else if (obj instanceof Asset)
					if (((Asset) obj).getFileState() == IVolumeManager.OFFLINE)
						event.detail = DND.DROP_LINK;
			}
		}
	}

	public void drop(DropTargetEvent event) {
		String[] filenames = (String[]) event.data;
		if (filenames != null && filenames.length > 0) {
			File soundFile = null;
			List<File> imageFiles = null;
			List<File> folders = null;
			List<File> gpsFiles = null;
			if (images) {
				imageFiles = new ArrayList<File>(filenames.length);
				Utilities.collectImages(filenames, imageFiles);
				folders = new ArrayList<File>(filenames.length);
				Utilities.collectFolders(filenames, folders);
			}
			if (gps) {
				gpsFiles = new ArrayList<File>();
				Utilities.collectFilteredFiles(filenames, gpsFiles,
						UiActivator.getDefault().createGpsFileFormatFilter());
			}
			if (sound) {
				for (int j = 0; j < filenames.length; j++) {
					File file = new File(filenames[j]);
					if (Constants.SOUNDFILEFILTER.accept(file)) {
						soundFile = file;
						break;
					}
				}
			}
			if (imageFiles != null && !imageFiles.isEmpty()) {
				if ((event.detail & DND.DROP_LINK) != 0)
					linkImage(event.x, event.y, imageFiles);
				else
					importImages(folders, imageFiles);
			}
			if (gpsFiles != null && !gpsFiles.isEmpty())
				importGpx(gpsFiles);
			if (soundFile != null) {
				importSound(event.x, event.y, soundFile);
			}
		}
	}

	private void linkImage(int x, int y, List<File> imageFiles) {
		if (imageFiles != null && imageFiles.size() == 1) {
			Point coord = control.toControl(x, y);
			Object obj = host.findObject(coord.x, coord.y);
			if (obj instanceof AssetImpl)
				OperationJob.executeOperation(new RetargetOperation((AssetImpl) obj, imageFiles.get(0), 0, false),
						host);
		}
	}

	@SuppressWarnings("unchecked")
	private void importSound(int x, int y, File sound) {
		Point coord = control.toControl(x, y);
		Object obj = host.findObject(coord.x, coord.y);
		List<Asset> assets = null;
		if (obj instanceof Asset)
			assets = Collections.singletonList((Asset) obj);
		else if (obj instanceof List<?>)
			assets = (List<Asset>) obj;
		if (assets != null) {
			String uri = sound.toURI().toString();
			OperationJob.executeOperation(new VoiceNoteOperation(assets, uri, uri,null), host);
		}
	}

	private void importGpx(List<File> gpx) {
		AssetSelection assetSelection = host.getAssetSelection();
		String[] assetIds = null;
		if (assetSelection != null && !assetSelection.isEmpty())
			assetIds = assetSelection.getAssetIds();
		else {
			IAssetProvider assetProvider = host.getAssetProvider();
			if (assetProvider != null)
				assetIds = assetProvider.getAssetIds();
		}
		if (assetIds != null && assetIds.length > 0) {
			IDropinHandler handler = UiActivator.getDefault().getDropinHandler("gps"); //$NON-NLS-1$
			if (handler != null)
				handler.handleDropin(gpx.toArray(new File[gpx.size()]), assetIds, host);
		}
	}

	private void importImages(List<File> folders, List<File> images) {
		boolean device = false;
		boolean foreignFolders = !folders.isEmpty();
		for (File folder : folders) {
			if (folder.isFile()) {
				foreignFolders = false;
				break;
			}
			File parent = folder;
			while (parent != null) {
				String name = parent.getName();
				if ("DCIM".equals(name)) { //$NON-NLS-1$
					device = true;
					break;
				}
				parent = parent.getParentFile();
			}
			ICore core = Core.getCore();
			File catRootFile = core.getVolumeManager().getRootFile(core.getDbManager().getFile());
			if (catRootFile != null && catRootFile.equals(core.getVolumeManager().getRootFile(folder))) {
				foreignFolders = false;
				break;
			}
		}
		Shell shell = host.getAdapter(Shell.class);
		ImportModeDialog dialog = new ImportModeDialog(shell, foreignFolders || device);
		if (dialog.open() != ImportModeDialog.OK)
			return;
		if (dialog.isNewStructure()) {
			ImportFromDeviceWizard wizard = new ImportFromDeviceWizard(images.toArray(new File[images.size()]),
					folders.toArray(new File[folders.size()]), device && foreignFolders, false);
			WizardDialog wizardDialog = new WizardDialog(shell, wizard);
			wizard.init(null, null);
			wizardDialog.open();
			return;
		}
		IProfiledOperation op = new ImportOperation(new FileInput(images, false),
				UiActivator.getDefault().createImportConfiguration(host), null,
				folders.toArray(new File[folders.size()]));
		SmartCollectionImpl selectedCollection = host.getSelectedCollection();
		if (selectedCollection != null && selectedCollection.getAlbum() && !selectedCollection.getSystem()) {
			CompoundOperation compoundOperation = new CompoundOperation(op.getLabel());
			compoundOperation.addOperation(op);
			compoundOperation.addOperation(new AddAlbumOperation(selectedCollection, (ImportOperation) op));
			op = compoundOperation;
		}
		OperationJob.executeOperation(op, host);
	}

}