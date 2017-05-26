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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.email.internal.job;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.TrackRecord_type;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.TrackRecord;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Ticketbox;
import com.bdaum.zoom.email.internal.Activator;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.AddTrackRecordsOperation;
import com.bdaum.zoom.operations.internal.job.SourceAndTarget;
import com.bdaum.zoom.operations.jobs.AbstractExportJob;

@SuppressWarnings("restriction")
public class EmailJob extends AbstractExportJob {

	private String subject;
	private String message;
	private final boolean trackExports;
	private List<TrackRecord> track = new ArrayList<TrackRecord>();
	private final int cropMode;
	private final List<String> to;

	public EmailJob(List<Asset> assets, List<String> to, int mode, int sizing,
			double scale, int maxSize, int cropMode, UnsharpMask umask,
			int jpegQuality, String subject, String message, Set<QueryField> xmpFilter,
			boolean createWatermark, String copyright,
			int privacy, boolean trackExports, IAdaptable adaptable) {
		super(Messages.EmailJob_Preparing_email, assets, mode, sizing, scale, maxSize,
				umask, jpegQuality, xmpFilter, createWatermark, copyright,
				privacy, adaptable);
		this.to = to;
		this.cropMode = cropMode;
		this.subject = subject;
		this.message = message;
		this.trackExports = trackExports;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */

	@Override
	protected IStatus doRun(IProgressMonitor monitor) {
		MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 0,
				Messages.EmailJob_Email_report, null);
		int size = assets.size();
		monitor.beginTask(Messages.EmailJob_Preparing_email_mon, size + 1);
		SubMonitor progress = SubMonitor.convert(monitor, 1000 * (size + 1));
		List<String> attachments = new ArrayList<String>(size);
		List<String> originalNames = new ArrayList<String>(size);
		IVolumeManager vm = Core.getCore().getVolumeManager();
		Ticketbox box = new Ticketbox();
		try {
			for (Asset asset : assets) {
				URI uri = vm.findExistingFile(asset, false);
				if (uri != null) {
					if (mode == Constants.FORMAT_ORIGINAL) {
						File file = new File(uri);
						attachments.add(uri.toString());
						originalNames.add(file.getName());
					} else {
						File file = null;
						try {
							file = box.obtainFile(uri);
						} catch (IOException e) {
							status.add(new Status(IStatus.ERROR,
									Activator.PLUGIN_ID, NLS.bind(
											Messages.EmailJob_download_failed,
											uri), e));
						}
						if (file != null) {
							try {
								File outfile = Activator.getDefault()
										.createTempFile(file.getName(), ".jpg"); //$NON-NLS-1$
								SourceAndTarget sourceAndTarget = downScaleImage(
										status, progress, asset, file, outfile,
										Double.MAX_VALUE, cropMode);
								if (sourceAndTarget != null) {
									attachments.add(sourceAndTarget
											.getOutfile().toURI().toString());
									originalNames.add(file.getName());
									if (trackExports) {
										TrackRecord record = new TrackRecordImpl(
												TrackRecord_type.type_email,
												"email", Messages.EmailJob_Email, //$NON-NLS-1$
												"", sourceAndTarget.getOutfile().getName(), //$NON-NLS-1$
												new Date(), false, null);
										record.setAsset_track_parent(sourceAndTarget
												.getAsset().getStringId());
										track.add(record);
									}
								}
							} catch (IOException e) {
								addErrorStatus(
										status,
										NLS.bind(
												Messages.EmailJob_io_error_creating_output,
												file), e);
							}
							box.cleanup();
						}
					}
				}
				if (monitor.isCanceled()) {
					status.add(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
							NLS.bind(Messages.EmailJob_Email_prep_cancelled,
									uri)));
					return status;
				}
			}
		} finally {
			box.endSession();
		}
		IStatus sendStatus = Activator.getDefault().sendMail(to, null, null,
				subject, message, attachments, originalNames);
		if (!sendStatus.isOK())
			status.add(sendStatus);
		if (!track.isEmpty()) {
			AddTrackRecordsOperation op = new AddTrackRecordsOperation(track);
			OperationJob.executeOperation(op, adaptable);
		}
		monitor.done();
		return status;
	}

}
