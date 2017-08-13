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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

@SuppressWarnings("restriction")
public class VoiceNoteOperation extends DbOperation {

	private boolean undo;
	private File[] oldFiles;
	private String[] oldVolumes;
	private String[] oldUris;
	private List<Asset> assets;
	private String sourceUri;
	private String targetUri;
	private String noteText;

	public VoiceNoteOperation(List<Asset> assets, String sourceUri, String targetUri, String noteText) {
		super(Messages.getString("VoiceNoteOperation.add_remove_voicenote")); //$NON-NLS-1$
		this.assets = assets;
		this.sourceUri = sourceUri;
		this.targetUri = targetUri;
		this.noteText = noteText;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		savePreviousState();
		return noteText != null ? addTextNote(aMonitor, info)
				: targetUri == null ? deleteVoiceNote(aMonitor, info) : addVoiceFile(aMonitor, info);
	}

	private IStatus addTextNote(IProgressMonitor aMonitor, IAdaptable info) {
		init(aMonitor, assets.size());
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				asset.setVoiceFileURI(noteText);
				asset.setVoiceVolume(null);
				if (storeSafely(null, 1, asset))
					fireAssetsModified(new BagChange<>(null, Collections.singleton(asset), null, null), null);
			}
		}
		return close(info);
	}

	private IStatus addVoiceFile(IProgressMonitor aMonitor, IAdaptable info) {
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		init(aMonitor, assets.size());
		String volumeLabel = null;
		if (targetUri.startsWith("file:/")) { //$NON-NLS-1$
			try {
				volumeLabel = volumeManager.getVolumeForFile(new File(new URI(targetUri)));
			} catch (URISyntaxException e) {
				// should never happen
			}
		}
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				if (!targetUri.equals(sourceUri)) {
					String uri = targetUri;
					try {
						if (".".equals(uri)) //$NON-NLS-1$
							uri = Core.getVoicefileURI(new File(new URI(asset.getUri()))).toString();
						BatchUtilities.moveFile(new File(new URI(sourceUri)), new File(new URI(uri)), null);
					} catch (URISyntaxException e) {
						addError(Messages.getString("AddVoiceNoteOperation.bad_uri_transfer"), e); //$NON-NLS-1$
						continue;
					} catch (IOException e) {
						addError(Messages.getString("AddVoiceNoteOperation.io_error_transfer"), e); //$NON-NLS-1$
						continue;
					} catch (DiskFullException e) {
						addError(Messages.getString("AddVoiceNoteOperation.disk_full_transfer"), e); //$NON-NLS-1$
						return close(info);
					}
				}
				asset.setVoiceFileURI(targetUri);
				asset.setVoiceVolume(volumeLabel);
			}
			if (storeSafely(null, 1, asset))
				fireAssetsModified(new BagChange<>(null, Collections.singleton(asset), null, null), null);
		}
		return close(info);
	}

	public IStatus deleteVoiceNote(IProgressMonitor aMonitor, final IAdaptable info) {
		final Set<String> volumes = new HashSet<String>();
		final List<String> errands = new ArrayList<String>();
		init(aMonitor, assets.size() + 2);
		boolean deleted = false;
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				if (".".equals(asset.getVoiceFileURI())) { //$NON-NLS-1$
					URI uri = volumeManager.findFile(asset);
					if (uri != null) {
						if (volumeManager.findExistingFile(asset, true) != null) {
							deleted |= deleteVoiceNote(asset);
						} else {
							String volume = asset.getVolume();
							if (volume != null && !volume.isEmpty())
								volumes.add(volume);
							errands.add(uri.toString());
						}
					}
				} else
					deleted |= deleteVoiceNote(asset);
				if (aMonitor.isCanceled())
					return abort();
			}
		}
		if (deleted)
			fireAssetsModified(new BagChange<>(null, assets, null, null), null);
		if (!errands.isEmpty()) {
			final IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
			if (errorHandler != null) {
				String msg;
				if (errands.size() == 1) {
					msg = NLS.bind(Messages.getString("DeleteVoicenoteOperation.File_offline"), //$NON-NLS-1$
							errands.get(0), volumes.toArray()[0]);
				} else {
					StringBuffer sb = new StringBuffer();
					for (String volume : volumes) {
						if (sb.length() > 0)
							sb.append(", "); //$NON-NLS-1$
						sb.append(volume);
					}
					msg = NLS.bind(Messages.getString("DeleteVoicenoteOperation.Files_offline"), //$NON-NLS-1$
							errands.size(), sb.toString());

				}
				errorHandler.showInformation(Messages.getString("DeleteVoicenoteOperation.Unable_to_delete"), //$NON-NLS-1$
						msg, info);
			}
		}
		return close(info);
	}

	private boolean deleteVoiceNote(Asset asset) {
		String voiceFileURI = asset.getVoiceFileURI();
		if (voiceFileURI != null && !voiceFileURI.isEmpty()) {
			if (".".equals(voiceFileURI)) { //$NON-NLS-1$
				URI uri = Core.getCore().getVolumeManager().findVoiceFile(asset);
				if (uri != null && Constants.FILESCHEME.equals(uri.getScheme()))
					new File(uri).delete();
			}
			asset.setVoiceFileURI(null);
			storeSafely(null, 1, asset);
			return true;
		}
		return false;
	}

	protected void savePreviousState() {
		int size = assets.size();
		oldFiles = new File[size];
		oldVolumes = new String[size];
		oldUris = new String[size];
		int i = 0;
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		for (Asset asset : assets) {
			oldVolumes[i] = asset.getVoiceVolume();
			oldUris[i] = asset.getVoiceFileURI();
			if (".".equals(oldUris[i])) { //$NON-NLS-1$
				URI voiceFileUri = volumeManager.findVoiceFile(asset);
				if (voiceFileUri != null) {
					try {
						File outputFile = ImageActivator.getDefault().createTempFile("Voice", ".wav"); //$NON-NLS-1$ //$NON-NLS-2$
						BatchUtilities.moveFile(new File(voiceFileUri), outputFile, null);
						oldFiles[i] = outputFile;
					} catch (IOException e) {
						addError(Messages.getString("VoiceNoteOperation.io_error_creating_backup"), //$NON-NLS-1$
								e);
						return;
					} catch (DiskFullException e) {
						addError(Messages.getString("VoiceNoteOperation.disk_full_creating_backup"), //$NON-NLS-1$
								e);
						return;
					}
				}
			}
			++i;
		}
		undo = true;
	}

	@Override
	public boolean canRedo() {
		return undo;
	}

	@Override
	public boolean canUndo() {
		return undo;
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		initUndo(aMonitor, assets.size());
		if (undo) {
			int i = 0;
			for (Asset asset : assets) {
				if (oldFiles[i] != null) {
					try {
						String uri = Core.getVoicefileURI(new File(new URI(asset.getUri()).toString())).toString();
						BatchUtilities.moveFile(oldFiles[i], new File(new URI(uri)), null);
					} catch (URISyntaxException e) {
						addError(Messages.getString("VoiceNoteOperation.bad_uri_undo"), e); //$NON-NLS-1$
						return close(info);
					} catch (IOException e) {
						addError(Messages.getString("VoiceNoteOperation.io_error_undo"), e); //$NON-NLS-1$
						return close(info);
					} catch (DiskFullException e) {
						addError(Messages.getString("VoiceNoteOperation.disk_full_undo"), e); //$NON-NLS-1$
						return close(info);
					}
				}
				asset.setVoiceFileURI(oldUris[i]);
				asset.setVoiceVolume(oldVolumes[i]);
				if (storeSafely(null, 1, asset))
					fireAssetsModified(new BagChange<>(null, Collections.singleton(asset), null, null), null);
				++i;
			}
		}
		return close(info);
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public int getPriority() {
		return assets.size() > 3 ? Job.LONG : Job.SHORT;
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return execute(aMonitor, info);
	}

	@Override
	public void dispose() {
		for (File file : oldFiles)
			if (file != null)
				file.delete();
	}

}