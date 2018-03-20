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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZProgressMonitorDialog;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.dialogs.SpaceDialog;

@SuppressWarnings("restriction")
public class ArchiveCommand extends AbstractCommandHandler {

	int assetCount;
	long imageSize = 0;
	long catSize = 0;
	int remoteImages = 0;
	int externalImages = 0;
	int localImages = 0;
	int voiceFiles = 0;
	int recipes = 0;

	@Override
	public void run() {
		boolean result = AcousticMessageDialog.openQuestion(getShell(), Messages.ArchiveAction_archive,
				Messages.ArchiveAction_archive_message);
		if (result) {
			ZProgressMonitorDialog dialog = new ZProgressMonitorDialog(getShell());
			try {
				dialog.run(false, true, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						imageSize = 0;
						catSize = 0;
						remoteImages = 0;
						externalImages = 0;
						localImages = 0;
						voiceFiles = 0;
						recipes = 0;
						IDbManager dbManager = Core.getCore().getDbManager();
						IVolumeManager volumeManager = Core.getCore().getVolumeManager();
						List<IRecipeDetector> recipeDetectors = CoreActivator.getDefault().getRecipeDetectors();
						List<AssetImpl> assets = dbManager.obtainAssets();
						assetCount = assets.size();
						monitor.beginTask(Messages.ArchiveAction_calculationg_space, assetCount + 2);
						monitor.subTask(Messages.ArchiveAction_calculationg_image_space);
						for (AssetImpl asset : assets) {
							if (volumeManager.isRemote(asset))
								++remoteImages;
							else {
								URI uri = volumeManager.findExistingFile(asset, true);
								if (uri == null)
									++externalImages;
								else {
									++localImages;
									File file = new File(uri);
									imageSize += file.length() + 4096;
									URI voiceUri = volumeManager.findVoiceFile(asset);
									if (voiceUri != null) {
										++voiceFiles;
										imageSize += new File(voiceUri).length() + 4096;
									}
									for (IRecipeDetector recipeDetector : recipeDetectors) {
										File[] metafiles = recipeDetector.getMetafiles(asset.getUri());
										if (metafiles != null)
											for (File metafile : metafiles) {
												++recipes;
												imageSize += metafile.length() + 4096;
											}
									}
								}
							}
							if (monitor.isCanceled())
								return;
							monitor.worked(1);
						}
						monitor.subTask(Messages.ArchiveAction_calculationg_catalog_space);
						catSize = dbManager.getFile().length();
						monitor.worked(1);
						File indexPath = dbManager.getIndexPath();
						if (indexPath != null) {
							catSize += indexPath.length();
							File[] list = indexPath.listFiles();
							if (list != null)
								for (File file : list)
									catSize += file.length();
						}
						monitor.done();
					}
				});
			} catch (InvocationTargetException e) {
				AcousticMessageDialog.openError(getShell(), Messages.ArchiveAction_error_during_space_calc,
						e.getCause().getMessage());
				return;
			} catch (InterruptedException e) {
				AcousticMessageDialog.openInformation(getShell(), Constants.APPLICATION_NAME,
						Messages.ArchiveAction_archiving_aborted);
				return;
			}
			SpaceDialog spaceDialog = new SpaceDialog(getShell(), remoteImages, externalImages, localImages, voiceFiles,
					recipes, imageSize, catSize);
			if (spaceDialog.open() == SpaceDialog.OK) {
				final File output = spaceDialog.getTargetFile();
				final boolean catReadOnly = spaceDialog.isCatReadonly();
				final boolean fileReadOnly = spaceDialog.isFileReadonly();
				ZProgressMonitorDialog runDialog = new ZProgressMonitorDialog(getShell());
				try {
					runDialog.run(true, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException {
							CoreActivator coreActivator = CoreActivator.getDefault();
							IDbFactory dbFactory = coreActivator.getDbFactory();
							IDbErrorHandler errorHandler = dbFactory.getErrorHandler();
							IDbManager dbManager = coreActivator.getDbManager();
							IVolumeManager volumeManager = coreActivator.getVolumeManager();
							monitor.beginTask(Messages.ArchiveAction_archiving, assetCount + 105);
							monitor.subTask(Messages.ArchiveAction_archiving_cat);
							File catFile = dbManager.getFile();
							File indexPath = dbManager.getIndexPath();
							File catBackupFile = new File(output, catFile.getName());
							dbManager.backup(catBackupFile.getAbsolutePath());
							monitor.worked(100);
							if (indexPath != null) {
								monitor.subTask(Messages.ArchiveAction_archiving_index);
								try {
									BatchUtilities.copyFolder(indexPath, new File(output, indexPath.getName()),
											monitor);
								} catch (IOException e1) {
									errorHandler.showError(Messages.ArchiveAction_error_during_archiving,
											e1.getMessage(), ArchiveCommand.this);
									return;

								} catch (DiskFullException e1) {
									errorHandler.showError(Messages.ArchiveAction_error_during_archiving,
											Messages.ArchiveAction_disk_full, ArchiveCommand.this);
									return;
								}
								if (monitor.isCanceled()) {
									errorHandler.showInformation(Messages.ArchiveAction_operation_cancelled,
											Messages.ArchiveAction_incomplete_archive, ArchiveCommand.this);
									return;
								}
							}
							monitor.worked(5);
							final IDbManager newDbManager = dbFactory.createDbManager(catBackupFile.getAbsolutePath(),
									false, false, false);
							try {
								List<IRecipeDetector> recipeDetectors = coreActivator.getRecipeDetectors();
								monitor.subTask(Messages.ArchiveAction_archiving_images);
								List<AssetImpl> assets = newDbManager.obtainAssets();
								int i = 0;
								long start = System.currentTimeMillis();
								for (AssetImpl asset : assets) {
									URI uri = volumeManager.findExistingFile(asset, true);
									if (uri != null) {
										try {
											String name = Core.getFileName(uri, false);
											File targetFile = BatchUtilities.makeUniqueFile(output, name,
													Core.getFileName(uri, true).substring(name.length()));
											BatchUtilities.copyFile(new File(uri), targetFile, null);
											if (fileReadOnly)
												targetFile.setReadOnly();
											asset.setUri(targetFile.toURI().toString());
											String volume = volumeManager.getVolumeForFile(targetFile);
											asset.setVolume(volume);
											if (asset.getVoiceFileURI() == null
													|| !asset.getVoiceFileURI().startsWith("?")) { //$NON-NLS-1$
												URI voiceUri = volumeManager.findVoiceFile(asset);
												if (voiceUri != null) {
													String voiceName = Core.getFileName(uri, false);
													File voiceTargetFile = BatchUtilities.makeUniqueFile(output, name,
															Core.getFileName(voiceUri, true)
																	.substring(voiceName.length()));
													BatchUtilities.copyFile(new File(voiceUri), voiceTargetFile, null);
													if (fileReadOnly)
														voiceTargetFile.setReadOnly();
													asset.setVoiceFileURI(voiceTargetFile.toURI().toString());
													asset.setVoiceVolume(volume);
												} else {
													asset.setVoiceFileURI(null);
													asset.setVoiceVolume(null);
												}
											}
											for (IRecipeDetector recipeDetector : recipeDetectors)
												recipeDetector.archiveRecipes(output, uri.toString(), asset.getUri(),
														fileReadOnly);
											newDbManager.storeAndCommit(asset);
										} catch (IOException e) {
											errorHandler.showError(Messages.ArchiveAction_error_during_archiving,
													e.getMessage(), ArchiveCommand.this);
											newDbManager.close(CatalogListener.EMERGENCY);
											return;
										} catch (DiskFullException e) {
											errorHandler.showError(Messages.ArchiveAction_error_during_archiving,
													Messages.ArchiveAction_disk_full, ArchiveCommand.this);
											newDbManager.close(CatalogListener.EMERGENCY);
											return;
										}
										if (++i % 10 == 0) {
											int remainingImages = localImages - i;
											if (remainingImages > 0) {
												long elapsed = System.currentTimeMillis() - start;
												long estimated = elapsed / i * remainingImages;
												monitor.subTask(NLS.bind(Messages.ArchiveAction_elapsed_time,
														Format.timeFormatter
																.toString((int) ((elapsed + 30000) / 60000)),
														Format.timeFormatter
																.toString((int) ((estimated + 30000) / 60000))));
											}
										}
									}
									if (monitor.isCanceled()) {
										errorHandler.showInformation(Messages.ArchiveAction_operation_cancelled,
												Messages.ArchiveAction_incomplete_archive, ArchiveCommand.this);
										return;
									}
									monitor.worked(1);

								}
								if (catReadOnly) {
									Meta meta = newDbManager.getMeta(false);
									if (meta != null) {
										meta.setReadonly(true);
										newDbManager.storeAndCommit(meta);
									}
								}
							} finally {
								newDbManager.close(CatalogListener.NORMAL);
								monitor.done();
							}
						}
					});
				} catch (InvocationTargetException e) {
					AcousticMessageDialog.openError(getShell(), Messages.ArchiveAction_error_during_archiving,
							e.getCause().getMessage());
				} catch (InterruptedException e) {
					AcousticMessageDialog.openInformation(getShell(), Constants.APPLICATION_NAME,
							Messages.ArchiveAction_incomplete_archive);
				}
			}
		}
	}

}
