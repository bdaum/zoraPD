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
package com.bdaum.juploadr.uploadapi.locrrest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.app.tags.TagParser;
import org.scohen.juploadr.event.UploadCompleteEvent;
import org.scohen.juploadr.event.UploadStatusMonitor;
import org.scohen.juploadr.uploadapi.AuthException;
import org.scohen.juploadr.uploadapi.CommunicationException;
import org.scohen.juploadr.uploadapi.ImageInfo;
import org.scohen.juploadr.uploadapi.ImageUploadApi;
import org.scohen.juploadr.uploadapi.InfoFailedException;
import org.scohen.juploadr.uploadapi.PostUploadAction;
import org.scohen.juploadr.uploadapi.ProtocolException;
import org.scohen.juploadr.uploadapi.Session;
import org.scohen.juploadr.uploadapi.UploadFailedException;

import com.bdaum.juploadr.uploadapi.locrrest.albums.GetAlbums;
import com.bdaum.juploadr.uploadapi.locrrest.albums.GetListUser;
import com.bdaum.juploadr.uploadapi.locrrest.albums.LocrPhotoSet;
import com.bdaum.juploadr.uploadapi.locrrest.authentification.LocrAuthEventDirector;
import com.bdaum.juploadr.uploadapi.locrrest.info.LocrInfo;
import com.bdaum.juploadr.uploadapi.locrrest.upload.LocrUpload;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.InvalidAuthTokenException;
import com.bdaum.zoom.net.communities.jobs.ExportToCommunityJob;

public class RestLocrApi extends ImageUploadApi {

	public static final String ID = "locr.com"; //$NON-NLS-1$
	private static Map<Class<? extends PostUploadAction>, List<PostUploadAction>> unfinishedActions = new HashMap<Class<? extends PostUploadAction>, List<PostUploadAction>>();
	private TagParser tagParser;

	@Override
	public boolean authenticate(Session session) throws ProtocolException,
			CommunicationException {
		LocrAuthEventDirector eventDirector = new LocrAuthEventDirector();
		return eventDirector.execute(session);
	}

	@Override
	public ImageInfo getImageInfo(Session session, String photoId)
			throws CommunicationException, ProtocolException {
		if (!isAccountAuthenticated(session.getAccount())) {
			try {
				authenticate(session);
			} catch (ProtocolException e) {
				throw new InfoFailedException(e.getMessage(), e);
			}
		}
		if (isAccountAuthenticated(session.getAccount())) {
			LocrInfo locrInfo = new LocrInfo(photoId, session);
			locrInfo.execute();
			return locrInfo.getImageInfo();
		}
		return null;
	}

	@Override
	public void initialize(final Session session) {
		super.initialize(session);
		Thread getPhotoSets = null;
		if (supportsPhotosets)
			getPhotoSets = new Thread() {

				@Override
				public void run() {
					// get their photosets
					GetAlbums psets = new GetAlbums(session);
					try {
						psets.execute();
					} catch (InvalidAuthTokenException ite) {
						reauthAccount(session);
					} catch (ProtocolException e) {
						handleCommunicationError(e);
					} catch (CommunicationException e) {
						handleCommunicationError(e);
					}
				}
			};
		Thread getTags = null;
		if (supportsTagging)
			getTags = new Thread() {

				@Override
				public void run() {
					GetListUser list = new GetListUser(session);
					try {
						list.execute();
						session.getAccount().setUserTags(list.getTags());
						// account.save();
					} catch (InvalidAuthTokenException ite) {
						reauthAccount(session);
					} catch (ProtocolException e) {
						handleCommunicationError(e);
					} catch (CommunicationException e) {
						handleCommunicationError(e);
					}

				}
			};
		if (getPhotoSets != null)
			getPhotoSets.start();
		if (getTags != null)
			getTags.start();
		try {
			if (getPhotoSets != null)
				getPhotoSets.join();
			if (getTags != null)
				getTags.join();
		} catch (InterruptedException e) {
			// ignore
		}
	}

	@Override
	public PhotoSet newPhotoSet(String name, String description,
			String[] keywords, ImageAttributes primaryPhoto) {
		return new LocrPhotoSet(name, description, keywords, primaryPhoto);
	}

	@Override
	public void postProcessAllUploads(
			Collection<? extends ImageAttributes> images, Session session,
			ExportToCommunityJob job) {
		if (supportsPhotosets) {
			CreateAllAlbumsTask createTask = new CreateAllAlbumsTask(job,
					session);
			if (createTask.hasSetsToCreate()) {
				createTask.schedule();
			}

			AddToAlbumTask addTask = new AddToAlbumTask(job, session);
			if (addTask.hasSetsToAdd()) {
				addTask.schedule();
			}
		}

		while (unfinishedActions.size() > 0
				&& showUnfinishedUploadErrorMessage()) {
			retryUnfinishedActions();
		}

		// ok, since we've updated the photosets, re-get them to make sure we're
		// in sync
		if (supportsPhotosets) {
			SynchronizePhotosetsTask syncTask = new SynchronizePhotosetsTask(
					job, session);
			syncTask.schedule();

			AddToAlbumTask addTask = new AddToAlbumTask(job, session);
			if (addTask.hasSetsToAdd()) {
				addTask.schedule();
			}
		}
	}

	@Override
	public void postProcessUpload(UploadCompleteEvent e, ImageAttributes atts) {
		atts.setPhotoId(e.getResponse().getPhotoId());
	}

	@Override
	public void replace(ImageAttributes imageAttributes, Session session,
			UploadStatusMonitor monitor) throws UploadFailedException,
			ProtocolException, CommunicationException {
		throw new UnsupportedOperationException("replace"); //$NON-NLS-1$
	}

	/**
	 * @see org.scohen.juploadr.uploadapi.ImageUploadApi#upload(org.scohen.juploadr.uploadapi.UserInfo,
	 *      org.scohen.juploadr.app.ImageAttributes,
	 *      org.scohen.juploadr.event.UploadStatusMonitor)
	 */
	@Override
	public void upload(ImageAttributes imageAttributes, Session session,
			UploadStatusMonitor monitor) throws UploadFailedException,
			ProtocolException, CommunicationException {
		if (!isAccountAuthenticated(session.getAccount())) {
			try {
				authenticate(session);
			} catch (ProtocolException e) {
				throw new UploadFailedException(e.getMessage(), e);
			}
		}
		if (isAccountAuthenticated(session.getAccount())) {
			LocrUpload flickrUpload = new LocrUpload(imageAttributes, session,
					monitor);
			flickrUpload.execute();
			authenticate(session);
		}
	}

	void reauthAccount(final Session session) {
		if (!session.getAccount().isNullAccount()) {
			session.getAccount().setAuthenticated(false);
			Display current = Display.getCurrent();
			if (current != null)
				current.syncExec(new Runnable() {

					public void run() {
						try {
							authenticate(session);
						} catch (ProtocolException e) {
							// oh boy, this is terrible.
							e.printStackTrace();
						} catch (CommunicationException e) {
							MessageDialog.openError(
									null,
									Messages.getString("RestLocrApi.communication_error"), //$NON-NLS-1$
									org.scohen.juploadr.Messages
											.getString("juploadr.ui.dialog.error.communication") + e.getMessage()); //$NON-NLS-1$
						}
					}
				});
		}
	}

	public boolean executeMethod(LocrMethod method) {
		try {
			method.execute();
			return true;
		} catch (ProtocolException e) {
			handleCommunicationError(e);
		} catch (CommunicationException e) {
			handleCommunicationError(e);
		}
		if (method instanceof PostUploadAction) {
			addFailedAction((PostUploadAction) method);
		}
		return false;
	}

	private static void addFailedAction(PostUploadAction failed) {
		List<PostUploadAction> failedActions = unfinishedActions.get(failed
				.getClass());
		if (failedActions == null) {
			failedActions = new LinkedList<PostUploadAction>();
		}
		if (!failedActions.contains(failed)) {
			failedActions.add(failed);
		}
		unfinishedActions.put(failed.getClass(), failedActions);
	}

	private static boolean showUnfinishedUploadErrorMessage() {
		StringBuffer errorMessage = new StringBuffer();
		errorMessage.append(Messages
				.getString("juploadr.locrrest.error.after.upload")); //$NON-NLS-1$
		Iterator<Class<? extends PostUploadAction>> failureClasses = unfinishedActions
				.keySet().iterator();
		while (failureClasses.hasNext()) {
			List<PostUploadAction> actions = getFailedActions(failureClasses
					.next());
			if (actions != null) {
				Iterator<PostUploadAction> actionIter = actions.iterator();
				while (actionIter.hasNext()) {
					PostUploadAction method = actionIter.next();
					errorMessage.append(Messages
							.getString("juploadr.locrrest.failed.to")); //$NON-NLS-1$
					errorMessage.append(method.getErrorText());
					errorMessage.append("\n"); //$NON-NLS-1$
				}
			}

		}
		errorMessage.append(Messages
				.getString("juploadr.locrrest.option.retry")); //$NON-NLS-1$
		return MessageDialog.openQuestion(null, errorMessage.toString(),
				Messages.getString("RestLocrApi.actions_failed")); //$NON-NLS-1$
	}

	private static List<PostUploadAction> getFailedActions(
			Class<? extends PostUploadAction> clazz) {
		return unfinishedActions.get(clazz);
	}

	private void retryUnfinishedActions() {
		List<PostUploadAction> actions = new LinkedList<PostUploadAction>();
		for (List<PostUploadAction> next : unfinishedActions.values()) {
			actions.addAll(next);
		}
		unfinishedActions.clear();
		Iterator<PostUploadAction> iter2 = actions.iterator();
		while (iter2.hasNext()) {
			LocrMethod method = (LocrMethod) iter2.next();
			executeMethod(method);
		}
	}

	@Override
	public TagParser getTagParser() {
		if (tagParser == null)
			tagParser = new TagParser(',');
		return tagParser;
	}

	public Session getSession(CommunityAccount account) throws AuthException,
			CommunicationException {
		return new Session(this, account);
	}

}
