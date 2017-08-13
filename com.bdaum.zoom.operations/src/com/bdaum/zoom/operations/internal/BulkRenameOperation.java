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
 * (c) 2015 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.operations.internal;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
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
import com.bdaum.zoom.core.Ticketbox;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileWatchManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.program.BatchConstants;

@SuppressWarnings("restriction")
public class BulkRenameOperation extends AbstractRenamingOperation {

	protected FileWatchManager fileWatchManager = CoreActivator.getDefault()
			.getFileWatchManager();
	private final GregorianCalendar cal = new GregorianCalendar();
	private final List<Asset> assets;
	private final String template;
	private String[] uris;
	private final String cue;
	private final QueryField field;
	private int start;

	public BulkRenameOperation(List<Asset> assets, String template, String cue,
			int start, QueryField field) {
		super(Messages.getString("BulkRenameOperation.renaming")); //$NON-NLS-1$
		this.assets = assets;
		this.template = template;
		this.cue = cue;
		this.start = start;
		this.field = field;
		uris = new String[assets.size()];
	}

	@Override
	public IStatus execute(final IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		init(monitor, 2 * assets.size());
		for (IRelationDetector detector : info
				.getAdapter(IRelationDetector[].class))
			detector.reset();
		final Set<String> volumes = new HashSet<String>();
		final List<String> errands = new ArrayList<String>();
		final List<Asset> modified = new ArrayList<Asset>(assets.size());
		int i = 0;
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		for (final Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				if (field != QueryField.URI) {
					URI uri = volumeManager.findFile(asset);
					if (uri != null) {
						final File file = new File(uri);
						String filename = file.getName();
						cal.setTimeInMillis(file.lastModified());
						asset.setTitle(Utilities.computeFileName(template,
								filename, cal.getTime(), -1, i + start, -1,
								cue, asset, field.getMaxlength(), false, false));
						dbManager.storeAndCommit(asset);
						modified.add(asset);
					}
				} else
					try {
						URI uri = volumeManager.findExistingFile(asset, true);
						if (uri != null) {
							final File file = new File(uri);
							File folder = file.getParentFile();
							String filename = file.getName();
							int p = filename.lastIndexOf('.');
							String ext = p >= 0 ? filename.substring(p) : ""; //$NON-NLS-1$
							int maxLength = BatchConstants.MAXPATHLENGTH
									- folder.getAbsolutePath().length() - 1
									- ext.length();
							cal.setTimeInMillis(file.lastModified());
							String newFilename = Utilities.computeFileName(
									template, filename, cal.getTime(), -1,
									i + 1, -1, cue, asset, maxLength, true,
									false);
							final File dest = new File(folder, newFilename
									+ ext);
							if (file.equals(dest)) {
								monitor.worked(2);
								++i;
								continue;
							}
							if (dest.exists()) {
								addError(
										NLS.bind(
												Messages.getString("BulkRenameOperation.file_already_exists"), //$NON-NLS-1$
												file, dest), null);
								monitor.worked(2);
								++i;
								continue;
							}
							uris[i] = asset.getUri();
							if (!storeSafely(() -> renameAsset(asset, file, dest, monitor,
									fileWatchManager), 1)) {
								monitor.worked(1);
								++i;
								continue;
							}
							modified.add(asset);
							IRelationDetector[] detectors = info.getAdapter(IRelationDetector[].class);
							for (IRelationDetector detector : detectors)
								if (detector.renameAsset(asset, file, dest,
										info, opId))
									break;
						} else {
							uri = volumeManager.findFile(asset);
							if (uri != null) {
								String volume = asset.getVolume();
								if (volume != null && !volume.isEmpty())
									volumes.add(volume);
								errands.add(uri.toString());
								if (monitor.isCanceled())
									return abort();
							}
						}
					} finally {
						fileWatchManager.stopIgnoring(opId);
					}
			}
			++i;
		}
		fireAssetsModified(new BagChange<>(null, modified, null, null), QueryField.URI);
		if (!errands.isEmpty()) {
			final IDbErrorHandler errorHandler = Core.getCore()
					.getErrorHandler();
			if (errorHandler != null)
				errorHandler.showInformation(Messages
						.getString("BulkRenameOperation.unable_to_rename"), //$NON-NLS-1$
						Ticketbox.computeErrorMessage(errands, volumes), info);
		}
		return close(info);
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		init(monitor, 2 * assets.size());
		int i = 0;
		for (final Asset asset : assets) {
			try {
				if (asset.getFileState() != IVolumeManager.PEER
						&& uris[i] != null) {
					IVolumeManager volumeManager = Core.getCore()
							.getVolumeManager();
					URI uri = volumeManager.findExistingFile(asset, true);
					if (uri == null) {
						addError(
								NLS.bind(
										Messages.getString("BulkRenameOperation.file_does_not_exisst"), //$NON-NLS-1$
										uri), null);
						return close(info);
					}
					File dest = volumeManager.findExistingFile(uris[i],
							asset.getVolume());
					if (dest.exists()) {
						addError(NLS.bind(Messages
								.getString("BulkRenameOperation.undo_failed"), //$NON-NLS-1$
								dest), null);
						return close(info);
					}
					File file = new File(uri);
					renameAsset(asset, file, dest, monitor, fileWatchManager);
					fireAssetsModified(new BagChange<>(null, Collections.singleton(asset), null, null), QueryField.URI);
				} else
					monitor.worked(2);
			} finally {
				fileWatchManager.stopIgnoring(opId);
			}
			++i;
		}
		return close(info);
	}

}
