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
 * (c) 2009-2015 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.operations.internal;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IRelationDetector;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbErrorHandler;

@SuppressWarnings("restriction")
public class RenameAssetOperation extends AbstractRenamingOperation {

	protected Asset asset;
	private final String newName;
	private String oldUri;
	private final boolean first;

	public RenameAssetOperation(Asset asset, String newName, boolean first) {
		super(Messages.getString("RenameAssetOperation.rename_image")); //$NON-NLS-1$
		this.asset = asset;
		this.first = first;
		if (newName.lastIndexOf('.') < 0) {
			String uri = asset.getUri();
			int p = uri.lastIndexOf('/');
			int q = uri.lastIndexOf('.');
			if (q > p)
				newName += uri.substring(q);
		}
		this.newName = newName;
	}

	@Override
	public IStatus execute(final IProgressMonitor aMonitor,
			final IAdaptable info) throws ExecutionException {
		init(aMonitor, 2);
		if (first)
			for (IRelationDetector detector : info
					.getAdapter(IRelationDetector[].class))
				detector.reset();
		if (asset.getFileState() != IVolumeManager.PEER) {
			oldUri = null;
			final Set<String> volumes = new HashSet<String>();
			final List<String> errands = new ArrayList<String>();
			IVolumeManager volumeManager = Core.getCore().getVolumeManager();
			try {
				URI uri = volumeManager.findExistingFile(asset, true);
				if (uri != null) {
					final File file = new File(uri);
					File folder = file.getParentFile();
					final File dest = new File(folder, newName);
					if (file.equals(dest))
						return close(info);
					if (dest.exists()) {
						addError(
								NLS.bind(
										Messages.getString("RenameAssetOperation.file_exists"), dest), //$NON-NLS-1$
								null);
						return close(info);
					}
					oldUri = asset.getUri();
					if (!storeSafely(() -> renameAsset(asset, file, dest, aMonitor,
							fileWatchManager), 1))
						return close(info);
					for (IRelationDetector detector : info
							.getAdapter(IRelationDetector[].class))
						if (detector.renameAsset(asset, file, dest, info, opId))
							break;
				} else {
					uri = volumeManager.findFile(asset);
					if (uri != null) {
						String volume = asset.getVolume();
						if (volume != null && !volume.isEmpty())
							volumes.add(volume);
						errands.add(uri.toString());
						if (aMonitor.isCanceled())
							return abort();
					}
				}
			} finally {
				fileWatchManager.stopIgnoring(opId);
			}
			fireAssetsModified(new BagChange<>(null, Collections.singleton(asset), null, null), QueryField.URI);
			if (!errands.isEmpty()) {
				final IDbErrorHandler errorHandler = Core.getCore()
						.getErrorHandler();
				if (errorHandler != null)
					errorHandler
							.showInformation(
									Messages.getString("RenameAssetOperation.unable_to_rename"), NLS.bind( //$NON-NLS-1$
													Messages.getString("RenameAssetOperation.file_does_not_exist"), //$NON-NLS-1$
													errands.get(0), volumes
															.toArray()[0]),
									info);
			}
		}
		return close(info);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		return execute(aMonitor, info);
	}

	@Override
	public IStatus undo(final IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		try {
			initUndo(aMonitor, 2);
			if (asset.getFileState() != IVolumeManager.PEER && oldUri != null) {
				IVolumeManager volumeManager = Core.getCore()
						.getVolumeManager();
				URI uri = volumeManager.findExistingFile(asset, true);
				if (uri == null) {
					addError(
							NLS.bind(
									Messages.getString("RenameAssetOperation.undo_file_does_not_exist"), //$NON-NLS-1$
									uri), null);
					return close(info);
				}
				File dest = volumeManager.findExistingFile(oldUri,
						asset.getVolume());
				if (dest.exists()) {
					addError(
							NLS.bind(
									Messages.getString("RenameAssetOperation.undo_file_already_exists"), //$NON-NLS-1$
									dest), null);
					return close(info);
				}
				File file = new File(uri);
				renameAsset(asset, file, dest, aMonitor, fileWatchManager);
				fireAssetsModified(new BagChange<>(null, Collections.singleton(asset), null, null), QueryField.URI);
			}
			return close(info);
		} finally {
			fileWatchManager.stopIgnoring(opId);
		}
	}

}
