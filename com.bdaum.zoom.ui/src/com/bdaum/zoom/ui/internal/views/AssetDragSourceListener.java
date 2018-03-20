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
 * (c) 2014 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.views;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;

public final class AssetDragSourceListener implements DragSourceListener {

	private final IDragHost host;
	private final TextTransfer textTransfer;
	private final LocalSelectionTransfer selectionTransfer;
	private final FileTransfer fileTransfer;
	private Set<String> volumes = new HashSet<String>();
	private List<String> errands = new ArrayList<String>();
	private AssetSelection assetSelection;
	private ImageRegion region;

	public AssetDragSourceListener(IDragHost host, int ops) {
		this.host = host;
		DragSource source = new DragSource(host.getControl(), ops);
		fileTransfer = FileTransfer.getInstance();
		textTransfer = TextTransfer.getInstance();
		selectionTransfer = LocalSelectionTransfer.getTransfer();
		source.setTransfer(new Transfer[] { fileTransfer, textTransfer, selectionTransfer });
		source.addDragListener(this);
	}

	public void dragStart(DragSourceEvent event) {
		assetSelection = host.getAssetSelection();
		if (assetSelection == null || assetSelection.isEmpty())
			event.doit = false;
		else if (!host.cursorOverImage(event.x, event.y))
			event.doit = false;
		else {
			IVolumeManager volumeManager = Core.getCore().getVolumeManager();
			for (Asset asset : assetSelection)
				if (volumeManager.isRemote(asset)) {
					event.doit = false;
					break;
				}
		}
		if (event.doit) {
			if (assetSelection.size() == 1)
				region = host.findBestFaceRegion(event.x, event.y, true);
			UiActivator.getDefault().dragStart(host);
		}
	}

	public void dragSetData(DragSourceEvent event) {
		if (fileTransfer.isSupportedType(event.dataType)) {
			ICore core = Core.getCore();
			IVolumeManager volumeManager = core.getVolumeManager();
			List<String> files = new ArrayList<String>(assetSelection.size());
			for (Asset asset : assetSelection) {
				URI uri = volumeManager.findFile(asset);
				if (uri != null && Constants.FILESCHEME.equals(uri.getScheme())) {
					String absolutePath = (new File(uri)).getAbsolutePath();
					if (volumeManager.findExistingFile(asset, true) != null)
						files.add(absolutePath);
					else {
						String volume = asset.getVolume();
						if (volume != null && !volume.isEmpty())
							volumes.add(volume);
						errands.add(absolutePath);
					}
				}
			}
			event.data = files.toArray(new String[files.size()]);
		} else if (textTransfer.isSupportedType(event.dataType)) {
			StringBuilder sb = new StringBuilder();
			for (Asset asset : assetSelection) {
				if (sb.length() > 0)
					sb.append('\n');
				sb.append(asset.getStringId());
			}
			event.data = sb.toString();
		} else if (selectionTransfer.isSupportedType(event.dataType))
			selectionTransfer.setSelection(region != null ? new StructuredSelection(region) : assetSelection);
	}

	public void dragFinished(DragSourceEvent event) {
		UiActivator.getDefault().dragFinished();
		if (!errands.isEmpty())
			AcousticMessageDialog.openWarning(host.getAdapter(Shell.class),
					Messages.getString("AbstractGalleryView.dragging_files"), //$NON-NLS-1$
					errands.size() == 1 ? NLS.bind(Messages.getString("AbstractGalleryView.file_n_offline"), //$NON-NLS-1$
							errands.get(0), volumes.toArray()[0])
							: NLS.bind(Messages.getString("AbstractGalleryView.n_files_offline"), //$NON-NLS-1$
									errands.size(), Core.toStringList(volumes.toArray(), ", "))); //$NON-NLS-1$
		volumes.clear();
		errands.clear();
		assetSelection = null;
		region = null;
	}
}