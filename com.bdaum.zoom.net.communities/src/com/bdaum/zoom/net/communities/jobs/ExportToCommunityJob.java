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
package com.bdaum.zoom.net.communities.jobs;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.IErrorHandler;
import org.scohen.juploadr.uploadapi.ImageUploadApi;
import org.scohen.juploadr.uploadapi.InfoFailedException;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.TrackRecord;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.core.Assetbox;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.net.communities.CommunitiesActivator;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.ui.AlbumDescriptor;
import com.bdaum.zoom.operations.internal.AddTrackRecordsOperation;
import com.bdaum.zoom.operations.jobs.AbstractExportJob;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;

@SuppressWarnings("restriction")
public class ExportToCommunityJob extends AbstractExportJob implements IErrorHandler {

	private static final class ExportRule implements ISchedulingRule {

		public boolean isConflicting(ISchedulingRule rule) {
			return rule instanceof ExportToCommunityJob.ExportRule;
		}

		public boolean contains(ISchedulingRule rule) {
			if (rule == this)
				return true;
			if (rule instanceof UploadJob.UploadRule && ((UploadJob.UploadRule) rule).getParentJob().getRule() == this)
				return true;
			if (rule instanceof PostProcessingTask.PostProcessingRule
					&& ((PostProcessingTask.PostProcessingRule) rule).getParentJob().getRule() == this)
				return true;
			return false;
		}
	}

	private static int exportCounter = 0;
	private Session session;
	private String communityName;
	private List<ImageAttributes> imagesTransferred = new ArrayList<ImageAttributes>();
	private String exportId;
	private List<TrackRecord> track;
	private boolean uploadall;
	protected boolean skipall;
	private boolean skipalloversized;
	private boolean replaceall;
	private final boolean showDescription;
	private int uploadCounter;
	private final AlbumDescriptor[] associatedAlbums;
	private final String[] titles;
	private final String[] descriptions;
	private boolean uploadallunsafe;
	private boolean skipallunsafe;
	private MultiStatus status;
	private final int cropMode;

	public ExportToCommunityJob(IConfigurationElement config, List<Asset> assets, AlbumDescriptor[] associatedAlbums,
			String[] titles, String[] descriptions, int mode, int sizing, double scale, int maxSize, int cropMode,
			UnsharpMask umask, int jpegQuality, Session session, Set<QueryField> xmpFilter, boolean showDescription,
			boolean createWatermark, String copyright, IAdaptable adaptable) {
		super(NLS.bind("Export to {0}", config.getAttribute("name")), assets, //$NON-NLS-1$ //$NON-NLS-2$
				mode, sizing, scale, maxSize, umask, jpegQuality, xmpFilter, createWatermark, copyright,
				QueryField.SAFETY_RESTRICTED, adaptable);
		this.associatedAlbums = associatedAlbums;
		this.titles = titles;
		this.descriptions = descriptions;
		this.cropMode = cropMode;
		this.showDescription = showDescription;
		communityName = config.getAttribute("name"); //$NON-NLS-1$
		track = new ArrayList<TrackRecord>(assets.size());
		this.session = session;
		exportId = String.valueOf(++exportCounter);
		setRule(new ExportRule());
		if (session.getApi().getErrorHandler() == null)
			session.getApi().setErrorHandler(this);
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.OPERATIONJOBFAMILY == family || Constants.CRITICAL == family;
	}

	@Override
	protected IStatus doRun(IProgressMonitor monitor) {
		long startTime = System.currentTimeMillis();

		status = new MultiStatus(CommunitiesActivator.PLUGIN_ID, 0,
				NLS.bind(Messages.ExportToCommunityJob_export_to_report, communityName), null);
		int size = assets.size();
		monitor.beginTask(NLS.bind(Messages.ExportToCommunityJob_exporting_to, communityName), size + 1);
		SubMonitor progress = SubMonitor.convert(monitor, 1200 * (size + 1) + 100);
		File targetFolder;
		try {
			targetFolder = Core.createTempDirectory(communityName + "Transfer"); //$NON-NLS-1$
		} catch (IOException e) {
			status.add(new Status(IStatus.ERROR, CommunitiesActivator.PLUGIN_ID,
					NLS.bind(Messages.ExportToCommunityJob_error_when_exporting, communityName), e));
			return status;
		}
		progress.worked(100);
		try (Assetbox box = new Assetbox(null, status, false)) {
			ImageUploadApi imageUploadApi = getImageUploadApi();
			imageUploadApi.preProcessAllUploads(assets, session, this);
			if (monitor.isCanceled())
				return status;
			for (int i = 0; i < size; i++) {
				Asset asset = assets.get(i);
				AlbumDescriptor album = (associatedAlbums == null) ? null : associatedAlbums[i];
				String title = (titles == null) ? null : titles[i];
				String description = (descriptions == null) ? null : descriptions[i];
				File file = box.obtainFile(asset);
				if (file != null) {
					File outfile = makeUniqueTargetFile(targetFolder, box.getUri(), mode);
					if (mode == Constants.FORMAT_ORIGINAL) {
						ImageAttributes imageAttributes = new ImageAttributes(session, asset, album, file, false);
						imageAttributes.setTitle(title);
						if (showDescription)
							imageAttributes.setDescription(description);
						else
							imageAttributes.setDescription(""); //$NON-NLS-1$
						imagesTransferred.add(imageAttributes);
						upload(imageAttributes, monitor, false);
						if (monitor.isCanceled())
							break;
					} else {
						if (downScaleImage(status, progress, asset, file, outfile, 1d, cropMode) != null) {
							if (monitor.isCanceled())
								break;
							ImageAttributes imageAttributes = new ImageAttributes(session, asset, album, outfile, true);
							imageAttributes.setTitle(title);
							if (showDescription)
								imageAttributes.setDescription(description);
							else
								imageAttributes.setDescription(""); //$NON-NLS-1$
							imagesTransferred.add(imageAttributes);
							upload(imageAttributes, monitor, true);
							if (monitor.isCanceled())
								break;
						}
						progress.worked(200);
					}
				}
				if (monitor.isCanceled()) {
					status.add(new Status(IStatus.WARNING, CommunitiesActivator.PLUGIN_ID,
							NLS.bind(Messages.ExportToCommunityJob_export_was_canceled, communityName)));
					return status;
				}
			}
			imageUploadApi.postProcessAllUploads(imagesTransferred, session, this);
			try {
				getJobManager().join(exportId, monitor);
				session.getAccount().save();
				if (!track.isEmpty())
					OperationJob.executeOperation(new AddTrackRecordsOperation(track), adaptable);
				CommunitiesActivator.getDefault().getLog()
						.log(new Status(IStatus.INFO, CommunitiesActivator.PLUGIN_ID,
								NLS.bind(Messages.ExportToCommunityJob_n_images_transferred,
										new Object[] { track.size(), communityName, session.getAccount().getName() })));
			} catch (OperationCanceledException e) {
				// do nothing
			} catch (InterruptedException e) {
				// do nothing
			}
		} finally {
			try {
				Job.getJobManager().join(Constants.UPLOADJOBFAMILY, monitor);
			} catch (InterruptedException e) {
				Job.getJobManager().cancel(Constants.UPLOADJOBFAMILY);
			}
			session.close();
			targetFolder.delete();
		}
		OperationJob.signalJobEnd(startTime);
		showCommunityWebPage();
		monitor.done();
		return status;
	}

	private void showCommunityWebPage() {
		try {
			if (uploadCounter > 0 && session.getAccount().getVisit() != null
					&& !session.getAccount().getVisit().isEmpty()) {
				Shell shell = adaptable.getAdapter(Shell.class);
				if (shell != null && !shell.isDisposed()) {
					shell.getDisplay().asyncExec(() -> {
						if (!shell.isDisposed())
							session.getAccount().testVisit();
					});
				}
			}
		} catch (Exception e) {
			// do notbhing
		}
	}

	@SuppressWarnings("fallthrough")
	private void upload(ImageAttributes imageAttributes, final IProgressMonitor monitor, boolean deleteAfterTransfer) {
		boolean replace = false;
		final Shell shell = adaptable.getAdapter(Shell.class);
		String name = imageAttributes.getAsset().getName();
		boolean single = assets.size() <= 1;
		String imagePath = imageAttributes.getImagePath();
		File imageFile = new File(imagePath);
		long filesize = imageFile.length();
		if (filesize > session.getAccount().getMaxFilesize()) {
			if (skipalloversized)
				return;
			String[] buttons = single ? new String[] { IDialogConstants.CANCEL_LABEL }
					: new String[] { Messages.ExportToCommunityJob_skip_all, IDialogConstants.SKIP_LABEL,
							IDialogConstants.CANCEL_LABEL };
			final AcousticMessageDialog dialog = new AcousticMessageDialog(shell,
					Messages.ExportToCommunityJob_image_too_large, null,
					NLS.bind(Messages.ExportToCommunityJob_image_is_larger_than, name,
							session.getAccount().getMaxFilesize() / (1024 * 1024)),
					MessageDialog.QUESTION, buttons, 1);
			shell.getDisplay().syncExec(() -> dialog.open());
			int ret = dialog.getReturnCode();
			if (single) {
				monitor.setCanceled(true);
				return;
			}
			switch (ret) {
			case 0:
				skipalloversized = true;
			case 1:
				return;
			case 2:
				monitor.setCanceled(true);
				return;
			}
		} else if (filesize > (session.getAccount().getTrafficLimit() - session.getAccount().getCurrentUploadUsed())) {
			shell.getDisplay().syncExec(new Runnable() {

				public void run() {
					AcousticMessageDialog.openWarning(shell, Messages.ExportToCommunityJob_traffic_limit_exceeded,
							NLS.bind(Messages.ExportToCommunityJob_the_traffic_limit_of,
									new Object[] { session.getAccount().getTrafficLimit() / (1024 * 1024),
											communityName, session.getAccount().getName() }));
				}
			});
			monitor.setCanceled(true);
		} else {
			if (!uploadall) {
				final Date date = getUploadDate(imageAttributes);
				if (date != null) {
					if (skipall)
						return;
					if (replaceall)
						replace = true;
					else {
						String[] buttons = session.getAccount().isCanReplace() ? (single
								? new String[] { Messages.ExportToCommunityJob_replace,
										Messages.ExportToCommunityJob_upload, Messages.ExportToCommunityJob_cancel }
								: new String[] { Messages.ExportToCommunityJob_replace_all,
										Messages.ExportToCommunityJob_replace, Messages.ExportToCommunityJob_upload_all,
										Messages.ExportToCommunityJob_upload, Messages.ExportToCommunityJob_skip_all,
										Messages.ExportToCommunityJob_skp, Messages.ExportToCommunityJob_cancel })
								: (single
										? new String[] { Messages.ExportToCommunityJob_upload,
												Messages.ExportToCommunityJob_cancel }
										: new String[] { Messages.ExportToCommunityJob_upload_all,
												Messages.ExportToCommunityJob_upload,
												Messages.ExportToCommunityJob_skip_all,
												Messages.ExportToCommunityJob_skp,
												Messages.ExportToCommunityJob_cancel });
						SimpleDateFormat df = new SimpleDateFormat(Messages.ExportToCommunityJob_tracik_date_format);
						final AcousticMessageDialog dialog = new AcousticMessageDialog(shell,
								Messages.ExportToCommunityJob_image_already_uploaded, null,
								NLS.bind(Messages.ExportToCommunityJob_image_uploaded_at, new Object[] { name,
										df.format(date), communityName, session.getAccount().getName() }),
								MessageDialog.QUESTION, buttons, 1);
						shell.getDisplay().syncExec(new Runnable() {

							public void run() {
								dialog.open();
							}
						});
						int ret = dialog.getReturnCode();
						if (session.getAccount().isCanReplace()) {
							if (single) {
								switch (ret) {
								case 0:
									replace = true;
									break;
								case 2:
									monitor.setCanceled(true);
									return;
								}
							} else {
								switch (ret) {
								case 0:
									replaceall = true;
									break;
								case 1:
									replace = true;
									break;
								case 2:
									uploadall = true;
									break;
								case 4:
									skipall = true;
								case 5:
									return;
								case 6:
									monitor.setCanceled(true);
									return;
								}
							}
						} else {
							if (single) {
								switch (ret) {
								case 1:
									monitor.setCanceled(true);
									return;
								}
							} else {
								switch (ret) {
								case 0:
									uploadall = true;
									break;
								case 2:
									skipall = true;
								case 3:
									return;
								case 4:
									monitor.setCanceled(true);
									return;
								}
							}
						}
					}
				}
			}
			if (!replace && !uploadallunsafe) {
				PhotoSet photoset = session.matchAlbum(imageAttributes);
				if (photoset != null && photoset.isUnsafe(imageAttributes)) {
					if (skipallunsafe)
						return;
					String[] buttons = (single
							? new String[] { Messages.ExportToCommunityJob_upload,
									Messages.ExportToCommunityJob_cancel }
							: new String[] { Messages.ExportToCommunityJob_upload_all,
									Messages.ExportToCommunityJob_upload, Messages.ExportToCommunityJob_skip_all,
									Messages.ExportToCommunityJob_skp, Messages.ExportToCommunityJob_cancel });
					final AcousticMessageDialog dialog = new AcousticMessageDialog(shell,
							Messages.ExportToCommunityJob_privacy_violated, null,
							NLS.bind(Messages.ExportToCommunityJob_image_classified_as_unsafe, new Object[] { name,
									photoset.getTitle(), communityName, session.getAccount().getName() }),
							MessageDialog.QUESTION, buttons, 1);
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							dialog.open();
						}
					});
					int ret = dialog.getReturnCode();
					if (single) {
						switch (ret) {
						case 1:
							monitor.setCanceled(true);
							return;
						}
					} else {
						switch (ret) {
						case 0:
							uploadallunsafe = true;
							break;
						case 2:
							skipallunsafe = true;
						case 3:
							return;
						case 4:
							monitor.setCanceled(true);
							return;
						}
					}
				}
			}
			new UploadJob(this, imageAttributes, replace, deleteAfterTransfer).schedule();
		}
	}

	private Date getUploadDate(ImageAttributes imageAttributes) {
		IDbManager dbManager = Core.getCore().getDbManager();
		String[] imageTrack = imageAttributes.getAsset().getTrack();
		if (imageTrack != null) {
			ImageUploadApi imageUploadApi = getImageUploadApi();
			for (String id : imageTrack) {
				TrackRecordImpl record = dbManager.obtainById(TrackRecordImpl.class, id);
				if (record != null) {
					try {

						if (imageUploadApi.getImageInfo(session, record.getDerivative()) != null)
							return record.getExportDate();
					} catch (InfoFailedException e) {
						// ignore
					} catch (CommunicationException e) {
						// ignore
					} catch (ProtocolException e) {
						// ignore
					}
				}
			}
		}
		return null;
	}

	public ImageUploadApi getImageUploadApi() {
		ImageUploadApi api = (ImageUploadApi) session.getApi();
		api.setStatus(status);
		return api;
	}

	public Set<QueryField> getFilter() {
		return xmpFilter;
	}

	public String getExportId() {
		return exportId;
	}

	public void addTrackRecord(TrackRecord record) {
		track.add(record);
	}

	public CommunityAccount getAccount() {
		return session.getAccount();
	}

	public String getCommunityName() {
		return communityName;
	}

	public void incrementCounter() {
		++uploadCounter;
	}

	public Session getSession() {
		return session;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		Object obj = adaptable.getAdapter(adapter);
		return obj != null ? obj : super.getAdapter(adapter);
	}

	public void handleError(Object source, Exception e) {
		status.add(new Status(IStatus.ERROR, CommunitiesActivator.PLUGIN_ID,
				NLS.bind(Messages.ExportToCommunityJob_communication_error, session.getApi().getSiteName()), e));
	}

	public MultiStatus getStatus() {
		return status;
	}

}
