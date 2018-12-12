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

package com.bdaum.zoom.operations.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileWatchManager;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.operations.internal.xmp.XMPUtilities;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

@SuppressWarnings("restriction")
public class ExportMetadataOperation extends DbOperation {

	private List<Asset> assets;
	private boolean overwriteAll;
	private boolean ignoreAll;
	private final Set<QueryField> xmpFilter;
	private FileWatchManager fileWatcher = CoreActivator.getDefault().getFileWatchManager();
	private final boolean jpeg;
	private boolean silent;
	private File firstExport;
	private final boolean show;

	public ExportMetadataOperation(List<Asset> assets, Set<QueryField> xmpFilter, boolean jpeg, boolean silent,
			boolean show) {
		super(Messages.getString("ExportMetadataOperation.Export_metadata")); //$NON-NLS-1$
		this.assets = assets;
		this.xmpFilter = xmpFilter;
		this.jpeg = jpeg;
		this.silent = silent;
		this.show = show;
	}

	@Override
	public boolean isSilent() {
		return silent;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, final IAdaptable info) throws ExecutionException {
		try {
			final List<String> errands = new ArrayList<String>();
			final Set<String> volumes = new HashSet<String>();
			if (assets.isEmpty()) {
				List<AssetImpl> set = dbManager.obtainStruct(AssetImpl.class, null, false, null, null, false);
				init(aMonitor, set.size());
				for (AssetImpl asset : set) {
					export(asset, aMonitor, set.size() > 1, info, errands, volumes);
					if (aMonitor.isCanceled())
						return abort();
				}
			} else {
				init(aMonitor, assets.size());
				for (Asset asset : assets) {
					if (asset.getFileState() != IVolumeManager.PEER) {
						export(asset, aMonitor, assets.size() > 1, info, errands, volumes);
						if (aMonitor.isCanceled())
							return abort();
					}
				}
			}
			if (!errands.isEmpty()) {
				final IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
				if (errorHandler != null) {
					String msg;
					if (errands.size() == 1)
						msg = NLS.bind(Messages.getString("ExportMetadataOperation.File_offline"), //$NON-NLS-1$
								errands.get(0), volumes.toArray()[0]);
					else {
						StringBuffer sb = new StringBuffer();
						for (String volume : volumes) {
							if (sb.length() > 0)
								sb.append(", "); //$NON-NLS-1$
							sb.append(volume);
						}
						msg = NLS.bind(Messages.getString("ExportMetadataOperation.Files_offline"), //$NON-NLS-1$
								errands.size(), sb.toString());

					}
					errorHandler.showInformation(Messages.getString("ExportMetadataOperation.Unable_to_export_XMP"), //$NON-NLS-1$
							msg, info);
				}
			}
			return close(info);
		} finally {
			fileWatcher.stopIgnoring(opId);
			if (show && firstExport != null)
				BatchUtilities.showInFolder(firstExport);
		}
	}

	@SuppressWarnings("fallthrough")
	private void export(Asset asset, IProgressMonitor aMonitor, boolean multiple, IAdaptable info, List<String> errands,
			Set<String> volumes) {
		URI uri = Core.getCore().getVolumeManager().findFile(asset);
		if (uri != null) {
			boolean conflict = false;
			boolean saveOriginal = false;
			File xmpFile = null;
			File[] sidecars = Core.getSidecarFiles(uri, false);
			if (sidecars.length > 0) {
				xmpFile = sidecars[sidecars.length - 1];
				if (xmpFile.exists()) {
					long lastModified = xmpFile.lastModified();
					Date xmpModifiedAt = asset.getXmpModifiedAt();
					conflict = xmpModifiedAt != null && lastModified > xmpModifiedAt.getTime();
					saveOriginal = xmpModifiedAt == null || xmpModifiedAt.getTime() < asset.getImportDate().getTime();
					if (conflict) {
						if (ignoreAll)
							return;
						if (!overwriteAll) {
							int ret = 4;
							IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
							if (errorHandler != null)
								ret = errorHandler
										.showMessageDialog(Messages.getString("ExportMetadataOperation.XMP_conflict"), //$NON-NLS-1$
												null,
												NLS.bind(Messages.getString("ExportMetadataOperation.XMP_out_of_sync"), //$NON-NLS-1$
														xmpFile),
												MessageDialog.QUESTION,
												(multiple)
														? new String[] {
																Messages.getString("ExportMetadataOperation.Overwrite"), //$NON-NLS-1$
																Messages.getString(
																		"ExportMetadataOperation.Overwrite_all"), //$NON-NLS-1$
																IDialogConstants.SKIP_LABEL,
																Messages.getString("ExportMetadataOperation.Skip_all"), //$NON-NLS-1$
																Messages.getString("ExportMetadataOperation.Cancel") } //$NON-NLS-1$
														: new String[] {
																Messages.getString("ExportMetadataOperation.Overwrite"), //$NON-NLS-1$
																IDialogConstants.CANCEL_LABEL },
												0, info);
							if (multiple) {
								switch (ret) {
								case 1:
									overwriteAll = true;
									/* FALL-THROUGH */
								case 0:
									break;
								case 3:
									ignoreAll = true;
									/* FALL-THROUGH */
								case 2:
									return;
								default:
									aMonitor.setCanceled(true);
									return;
								}
							} else {
								switch (ret) {
								case 0:
									break;
								default:
									aMonitor.setCanceled(true);
									return;
								}
							}
						}
					}
				}
			}
			for (int i = 0; i < sidecars.length; i++) {
				xmpFile = sidecars[i];
				if (xmpFile.exists() || i == sidecars.length - 1) {
					File newFile = new File(xmpFile.getAbsoluteFile() + ".new"); //$NON-NLS-1$
					fileWatcher.ignore(newFile, opId);
					fileWatcher.ignore(xmpFile, opId);
					newFile.delete();
					try {
						XMPUtilities.configureXMPFactory();
						XMPMeta xmpMeta;
						if (xmpFile.exists())
							try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(xmpFile))) {
								xmpMeta = XMPMetaFactory.parse(in);
							} catch (XMPException e) {
								addError(NLS.bind(Messages.getString("ExportMetadataOperation.invalid_xmp"), xmpFile), e); //$NON-NLS-1$
								xmpMeta = XMPMetaFactory.create();
							}
						else
							xmpMeta = XMPMetaFactory.create();
						try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(newFile))) {
							XMPUtilities.writeProperties(xmpMeta, asset, xmpFilter, false);
							XMPMetaFactory.serialize(xmpMeta, out);
							if (jpeg && ImageConstants.isJpeg(Core.getFileExtension(uri.toString()))) {
								File imageFile = new File(uri);
								try (BufferedInputStream in1 = new BufferedInputStream(
										new FileInputStream(imageFile))) {
									byte[] bytes = new byte[(int) imageFile.length()];
									if (in1.read(bytes) > 0)
										try {
											byte[] oldXmp = XMPUtilities.getXmpFromJPEG(bytes);
											if (oldXmp != null)
												try (BufferedInputStream in = new BufferedInputStream(
														new ByteArrayInputStream(oldXmp))) {
													xmpMeta = XMPMetaFactory.parse(in);
												} catch (XMPException e) {
													addError(NLS.bind(Messages.getString("ExportMetadataOperation.invalid_inline_xmp"), imageFile), e); //$NON-NLS-1$
													xmpMeta = XMPMetaFactory.create();
												}
											else
												xmpMeta = XMPMetaFactory.create();
											ByteArrayOutputStream mout = new ByteArrayOutputStream();
											XMPUtilities.writeProperties(xmpMeta, asset, xmpFilter, false);
											XMPMetaFactory.serialize(xmpMeta, mout);
											bytes = XMPUtilities.insertXmpIntoJPEG(bytes, mout.toByteArray());
										} catch (XMPException e) {
											addError(NLS.bind(
													Messages.getString("ExportMetadataOperation.unable_to_export"), //$NON-NLS-1$
													imageFile), e);
										}
									fileWatcher.ignore(imageFile, opId);
									try (BufferedOutputStream out1 = new BufferedOutputStream(
											new FileOutputStream(imageFile))) {
										out1.write(bytes);
									}
								}
							}
						}
					} catch (IOException e) {
						addError(Messages.getString("ExportMetadataOperation.IO_error_creating_XMP"), e); //$NON-NLS-1$
					} catch (XMPException e) {
						addError(Messages.getString("ExportMetadataOperation.XMP_parsing_error"), //$NON-NLS-1$
								e);
					}
					if (newFile.exists()) {
						try {
							if (saveOriginal) {
								File backupFile = new File(xmpFile.getAbsolutePath() + ".original"); //$NON-NLS-1$
								if (!backupFile.exists()) {
									fileWatcher.ignore(backupFile, opId);
									BatchUtilities.moveFile(xmpFile, backupFile, aMonitor);
								}
							}
							BatchUtilities.moveFile(newFile, xmpFile, aMonitor);
							asset.setXmpModifiedAt(new Date(xmpFile.lastModified()));
							storeSafely(null, 1, asset);
						} catch (IOException e) {
							Core.getCore().logError(
									Messages.getString("ExportMetadataOperation.IO_error_exporting_metadata"), e); //$NON-NLS-1$
						} catch (DiskFullException e) {
							Core.getCore().logError(
									Messages.getString("ExportMetadataOperation.IO_error_exporting_metadata"), e); //$NON-NLS-1$
						}
						if (firstExport == null)
							firstExport = xmpFile;
					}
				}
			}
		} else {
			String volume = asset.getVolume();
			if (volume != null && !volume.isEmpty())
				volumes.add(volume);
			errands.add(asset.getUri());
		}
	}

	@Override
	public boolean canRedo() {
		return false;
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return null;
	}

	@Override
	public boolean canUndo() {
		return false;
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return null;
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT | IProfiledOperation.XMP;
	}

	public int getUndoProfile() {
		return 0;
	}

	@Override
	public int getPriority() {
		return assets.size() > 3 ? Job.LONG : Job.SHORT;
	}

}
