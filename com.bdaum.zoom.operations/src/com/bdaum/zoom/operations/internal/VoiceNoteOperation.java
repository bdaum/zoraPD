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
 * (c) 2015 Berthold Daum  
 */
package com.bdaum.zoom.operations.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

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
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

public class VoiceNoteOperation extends DbOperation {

	private String oldVolume;
	private String oldUri;
	private Asset asset;
	private String sourceUri;
	private String targetUri;
	private String noteText;
	private String svg;
	private File renameSource;
	private File renameTarget;
	private File fileToDelete;

	public VoiceNoteOperation(Asset asset, String sourceUri, String targetUri, String noteText, String svg) {
		super(Messages.getString("VoiceNoteOperation.add_remove_voicenote")); //$NON-NLS-1$
		this.asset = asset;
		this.sourceUri = sourceUri;
		this.targetUri = targetUri;
		this.noteText = noteText;
		this.svg = svg;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		init(aMonitor, 1);
		oldVolume = asset.getVoiceVolume();
		oldUri = asset.getVoiceFileURI();
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		StringBuilder sb = new StringBuilder();
		String volumeLabel = null;
		if (targetUri != null) {
			if (targetUri.startsWith("file:/")) { //$NON-NLS-1$
				try {
					volumeLabel = volumeManager.getVolumeForFile(new File(new URI(targetUri)));
				} catch (URISyntaxException e) {
					// should never happen
				}
			}
			if (!targetUri.equals(sourceUri)) {
				String uri = targetUri;
				try {
					if (".".equals(uri)) { //$NON-NLS-1$
						uri = Core.getVoicefileURI(new File(new URI(asset.getUri()))).toString();
						volumeLabel = asset.getVolume();
					}
					File source = new File(new URI(sourceUri));
					File target = new File(new URI(uri));
					if (!source.equals(target)) {
						if (target.exists()) {
							File tempFile = Core.createTempFile("Voice", "wav"); //$NON-NLS-1$//$NON-NLS-2$
							BatchUtilities.moveFile(renameSource = target, renameTarget = tempFile, null);
						}
						BatchUtilities.moveFile(source, target, null);
					}
					if (".".equals(uri)) //$NON-NLS-1$
						fileToDelete = target;
				} catch (URISyntaxException e) {
					addError(Messages.getString("AddVoiceNoteOperation.bad_uri_transfer"), e); //$NON-NLS-1$
					return close(info);
				} catch (IOException e) {
					addError(Messages.getString("AddVoiceNoteOperation.io_error_transfer"), e); //$NON-NLS-1$
					return close(info);
				} catch (DiskFullException e) {
					addError(Messages.getString("AddVoiceNoteOperation.disk_full_transfer"), e); //$NON-NLS-1$
					return close(info);
				}
			}
			sb.append(targetUri);
		} else {
			String voiceFileURI = asset.getVoiceFileURI();
			if (voiceFileURI != null && (voiceFileURI.startsWith("\f.") || ".".equals(voiceFileURI))) { //$NON-NLS-1$ //$NON-NLS-2$
				URI uri = volumeManager.findVoiceFile(asset);
				if (uri != null && Constants.FILESCHEME.equals(uri.getScheme())) {
					String voiceVolume = asset.getVoiceVolume();
					File file = volumeManager.findExistingFile(uri.toString(), voiceVolume);
					if (file != null) {
						try {
							File tempFile = Core.createTempFile("Voice", "wav"); //$NON-NLS-1$//$NON-NLS-2$
							BatchUtilities.moveFile(renameSource = file, renameTarget = tempFile, null);
						} catch (IOException e) {
							addError(Messages.getString("VoiceNoteOperation.io_error_deleting"), e);  //$NON-NLS-1$
							return close(info);
						} catch (DiskFullException e) {
							addError(Messages.getString("VoiceNoteOperation.disk_full_deleting"), e);  //$NON-NLS-1$
							return close(info);
						}
					} else {
						final IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
						if (errorHandler != null) {
							String onVolume = voiceVolume == null || voiceVolume.isEmpty() ? "" //$NON-NLS-1$
									: NLS.bind(Messages.getString("VoiceNoteOperation.on_volume"), voiceVolume); //$NON-NLS-1$
							String msg = NLS.bind(Messages.getString("VoiceNoteOperation.already_deleted"), uri, onVolume); //$NON-NLS-1$
							errorHandler.showInformation(Messages.getString("VoiceNoteOperation.unable_to_delete"), msg, info); //$NON-NLS-1$
						}
					}
				}
			}
		}
		sb.append('\f');
		if (noteText != null)
			sb.append(noteText);
		sb.append('\f');
		if (svg != null)
			sb.append(svg);
		asset.setVoiceFileURI(sb.toString());
		asset.setVoiceVolume(volumeLabel);
		if (storeSafely(null, 1, asset))
			fireAssetsModified(new BagChange<>(null, Collections.singleton(asset), null, null), null);
		return close(info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		initUndo(aMonitor, 1);
		if (renameSource != null) {
			try {
				BatchUtilities.moveFile(renameTarget, renameSource, null);
			} catch (IOException e) {
				addError(Messages.getString("VoiceNoteOperation.io_error_undo"), e); //$NON-NLS-1$
				return close(info);
			} catch (DiskFullException e) {
				addError(Messages.getString("VoiceNoteOperation.disk_full_undo"), e); //$NON-NLS-1$
				return close(info);
			}
		}
		asset.setVoiceFileURI(oldUri);
		asset.setVoiceVolume(oldVolume);
		if (fileToDelete != null) {
			fileToDelete.delete();
			fileToDelete = null;
		}
		if (storeSafely(null, 1, asset))
			fireAssetsModified(new BagChange<>(null, Collections.singleton(asset), null, null), null);
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
		return Job.SHORT;
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return execute(aMonitor, info);
	}

	@Override
	public void dispose() {
		if (renameTarget != null)
			renameTarget.delete();
	}

}