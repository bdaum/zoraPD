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
import java.util.Date;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.event.ImageUploadResponse;
import org.scohen.juploadr.event.UploadCompleteEvent;
import org.scohen.juploadr.event.UploadEvent;
import org.scohen.juploadr.event.UploadStatusMonitor;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ImageUploadApi;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;
import org.scohen.juploadr.uploadapi.UploadFailedException;

import com.bdaum.zoom.cat.model.TrackRecord_type;
import com.bdaum.zoom.cat.model.asset.TrackRecord;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.net.communities.CommunitiesActivator;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.operations.jobs.AbstractExportJob;

public class UploadJob extends CustomJob {
	/**
	 *
	 */
	private final ExportToCommunityJob exportToCommunityJob;

	final class UploadRule implements ISchedulingRule {

		public boolean isConflicting(ISchedulingRule rule) {
			return rule instanceof PostProcessingTask.PostProcessingRule
					|| rule instanceof UploadJob.UploadRule;
		}

		public ExportToCommunityJob getParentJob() {
			return UploadJob.this.exportToCommunityJob;
		}


		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
	}

	public class DelegatedUploadMonitor implements UploadStatusMonitor {

		private final IProgressMonitor progressMonitor;

		public DelegatedUploadMonitor(IProgressMonitor monitor) {
			progressMonitor = monitor;
		}


		public void allUploadsComplete() {
			progressMonitor.done();
		}


		public void uploadCancelled() {
			progressMonitor.setCanceled(true);
		}


		public void uploadFailed(UploadEvent e) {
			addError(status, Messages.UploadJob_upload_failed, e
					.getErrorMessage());
		}


		public void uploadFinished(UploadCompleteEvent e) {
			ImageUploadResponse response = e.getResponse();
			String photoId = response.getPhotoId();
			imageAttributes.setPhotoId(photoId);
			String key = response.getKey();
			if (key != null)
				imageAttributes.setSecret(key);
			String url = response.getUrl();
			if (url != null)
				imageAttributes.setUrl(url);
			imageAttributes.setPhotoId(photoId);
			progressMonitor.done();
		}


		public void uploadStarted(UploadEvent e) {
			progressMonitor.beginTask(Messages.UploadJob_file_upload,
					IProgressMonitor.UNKNOWN);
		}


		public void uploadStatusChanged(UploadEvent e) {
			// ignore
		}

	}

	protected MultiStatus status;
	private final ImageAttributes imageAttributes;
	private final boolean replace;
	private final boolean deleteAfterTransfer;

	public UploadJob(ExportToCommunityJob exportToCommunityJob,
			ImageAttributes imageAttributes, boolean replace,
			boolean deleteAfterTransfer) {
		super(NLS.bind(Messages.UploadJob_export_to, imageAttributes.getAsset()
				.getName(), exportToCommunityJob.getCommunityName()));
		this.exportToCommunityJob = exportToCommunityJob;
		this.imageAttributes = imageAttributes;
		this.replace = replace;
		this.deleteAfterTransfer = deleteAfterTransfer;
		setRule(new UploadRule());
		setPriority(Job.LONG);
	}

	public void addError(MultiStatus status2, String msg, String errorMsg) {
		status2.add(new Status(IStatus.ERROR, CommunitiesActivator.PLUGIN_ID,
				msg + ": " + errorMsg)); //$NON-NLS-1$
	}


	@Override
	public boolean belongsTo(Object family) {
		return Constants.UPLOADJOBFAMILY.equals(family);
	}


	@Override
	protected IStatus runJob(IProgressMonitor monitor) {
		Session session = exportToCommunityJob.getSession();
		status = new MultiStatus(CommunitiesActivator.PLUGIN_ID, 0, NLS.bind(
				Messages.UploadJob_export_report, imageAttributes.getAsset()
						.getName(), this.exportToCommunityJob
						.getCommunityName()), null);
		try {
			ImageUploadApi api = exportToCommunityJob.getImageUploadApi();
			api.setStatus(status);
			if (replace)
				api.replace(imageAttributes, session,
						new DelegatedUploadMonitor(monitor));
			else
				api.upload(imageAttributes, session,
						new DelegatedUploadMonitor(monitor));
			CommunityAccount account = session.getAccount();
			if (account.isTrackExport()) {
				IConfigurationElement configuration = account
						.getConfiguration();
				String communityName = ((IConfigurationElement) configuration
						.getParent()).getAttribute("name"); //$NON-NLS-1$
				TrackRecord record = new TrackRecordImpl(
						TrackRecord_type.type_community, account
								.getCommunityId(), communityName, account
								.getName(), imageAttributes.getPhotoId(),
						new Date(), replace, account.getVisit());
				record.setAsset_track_parent(imageAttributes.getAsset()
						.getStringId());
				exportToCommunityJob.addTrackRecord(record);
			}
			exportToCommunityJob.incrementCounter();
			if (deleteAfterTransfer) {
				File file = new File(imageAttributes.getImagePath());
				file.delete();
			}
		} catch (UploadFailedException e) {
			AbstractExportJob.addErrorStatus(status,
					Messages.UploadJob_upload_failed, e);
		} catch (ProtocolException e) {
			AbstractExportJob.addErrorStatus(status,
					Messages.UploadJob_internal_error, e);
		} catch (CommunicationException e) {
			AbstractExportJob.addErrorStatus(status,
					Messages.UploadJob_communication_error, e);
		}
		return status;
	}
}