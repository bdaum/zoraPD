package com.bdaum.juploadr.uploadapi.smugrest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
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
import org.scohen.juploadr.util.NonNullList;

import com.bdaum.juploadr.uploadapi.smugrest.authentification.Logout;
import com.bdaum.juploadr.uploadapi.smugrest.authentification.SmugmugAuthEventDirector;
import com.bdaum.juploadr.uploadapi.smugrest.categories.GetCategoryList;
import com.bdaum.juploadr.uploadapi.smugrest.categories.GetSubcategoryList;
import com.bdaum.juploadr.uploadapi.smugrest.info.SmugmugInfo;
import com.bdaum.juploadr.uploadapi.smugrest.photosets.GetPhotosetList;
import com.bdaum.juploadr.uploadapi.smugrest.photosets.SmugmugPhotoSet;
import com.bdaum.juploadr.uploadapi.smugrest.tags.GetListUser;
import com.bdaum.juploadr.uploadapi.smugrest.ui.AlbumPolicyDialog;
import com.bdaum.juploadr.uploadapi.smugrest.upload.SmugmugUpload;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.jobs.ExportToCommunityJob;

public class SmugmugRestApi extends ImageUploadApi {

	public static final String SMUGMUG_SHARED_SECRET = "848fe0f535a87865ef040b182ad83516"; //$NON-NLS-1$
	public static final String SMUGMUG_API_KEY = "X7NtA0N8GBtRklxjqJWgAR7HscDR6Yq6"; //$NON-NLS-1$
	public static final int SELECT = 0;
	public static final int USEDEFAULT = 1;

	private static NonNullList<PhotoSet> photoSets = new NonNullList<PhotoSet>();
	private static Map<Class<? extends PostUploadAction>, List<PostUploadAction>> unfinishedActions = new HashMap<Class<? extends PostUploadAction>, List<PostUploadAction>>();
	private TagParser tagParser;


	@Override
	public boolean authenticate(Session session) throws ProtocolException,
			CommunicationException {
		return new SmugmugAuthEventDirector().execute(session);
	}


	@Override
	public ImageInfo getImageInfo(Session session, String photoId)
			throws CommunicationException, InfoFailedException,
			ProtocolException {
		if (!isAccountAuthenticated(session.getAccount())) {
			try {
				authenticate(session);
			} catch (ProtocolException e) {
				throw new InfoFailedException(e.getMessage(), e);
			}
		}
		if (isAccountAuthenticated(session.getAccount())) {
			SmugmugInfo smugmugInfo = new SmugmugInfo(photoId, session);
			smugmugInfo.execute();
			return smugmugInfo.getImageInfo();
		}
		return null;
	}


	@Override
	public TagParser getTagParser() {
		if (tagParser == null)
			tagParser = new TagParser(' ');
		return tagParser;
	}


	@Override
	public void initialize(final Session session) {
		super.initialize(session);
		Thread getPhotoSets = null;
		if (supportsPhotosets)
			getPhotoSets = new Thread() {

				@Override
				public void run() {
					GetPhotosetList getList = new GetPhotosetList(session);
					executeMethod(getList);

				}
			};
		Thread getTags = null;
		if (supportsTagging)
			getTags = new Thread() {

				@Override
				public void run() {
					GetListUser tags = new GetListUser(session);
					executeMethod(tags);
					session.getAccount().setUserTags(tags.getTags());
				}
			};
		Thread getCategories = null;
		Thread getSubcategories = null;
		if (supportsCategories)
			getCategories = new Thread() {

				@Override
				public void run() {
					GetCategoryList categories = new GetCategoryList(session);
					executeMethod(categories);
					session.getAccount().setCategories(
							categories.getCategories());
				}
			};
		if (supportsSubcategories)
			getSubcategories = new Thread() {

				@Override
				public void run() {
					GetSubcategoryList categories = new GetSubcategoryList(
							session);
					executeMethod(categories);
					session.getAccount().setSubcategories(
							categories.getCategories());
				}
			};
		if (getPhotoSets != null)
			getPhotoSets.start();
		if (getTags != null)
			getTags.start();
		if (getCategories != null)
			getCategories.start();
		if (getSubcategories != null)
			getSubcategories.start();
		try {
			if (getPhotoSets != null)
				getPhotoSets.join();
			if (getTags != null)
				getTags.join();
			if (getCategories != null)
				getCategories.join();
			if (getSubcategories != null)
				getSubcategories.join();
		} catch (InterruptedException e) {
			// ignore
		}
	}


	@Override
	public PhotoSet newPhotoSet(String name, String description,
			String[] keywords, ImageAttributes primaryPhoto) {
		return new SmugmugPhotoSet(name, description);
	}


	@Override
	public void preProcessAllUploads(Collection<? extends Asset> assets,
			final Session session, final ExportToCommunityJob job) {
		CommunityAccount account = session.getAccount();
		if (supportsPhotosets && account.isAlbumsAsSets()) {
			Map<String, SmugmugPhotoSet> albums = new HashMap<String, SmugmugPhotoSet>();
			for (Asset asset : assets) {
				for (String album : asset.getAlbum()) {
					SmugmugPhotoSet photoset = albums.get(album);
					if (photoset == null) {
						photoset = (SmugmugPhotoSet) newPhotoSet(album, "", //$NON-NLS-1$
								null, null);
						albums.put(album, photoset);
					}
					if (asset.getSafety() > QueryField.SAFETY_SAFE
							&& asset.getSafety() < QueryField.SAFETY_RESTRICTED)
						photoset.setPublicAlbum(false);
				}
			}
			List<PhotoSet> sets = account.getPhotosets();
			for (PhotoSet photoSet : sets)
				albums.remove(photoSet.getTitle());
			sets.addAll(albums.values());
			CreateAllPhotosetsTask createTask = new CreateAllPhotosetsTask(job,
					session);
			if (createTask.hasSetsToCreate())
				createTask.schedule();
			// ok, since we've updated the photosets, re-get them to make
			// sure
			// we're in sync
			GetPhotosetList getPhotoSets = new GetPhotosetList(session);
			executeMethod(getPhotoSets);
			while (unfinishedActions.size() > 0
					&& showUnfinishedUploadErrorMessage()) {
				retryUnfinishedActions();
			}
		}
		photoSets.clear();
		GetPhotosetList getList = new GetPhotosetList(session);
		executeMethod(getList);
		photoSets = new NonNullList<PhotoSet>(getList.getPhotoSets());
		account.setPhotosets(photoSets);
		account.save();
		if (singlePhotosets && !photoSets.isEmpty()) {
			if (photoSets.size() > 1) {
				final Shell shell = (Shell) job.getAdapter(Shell.class);
				shell.getDisplay().syncExec(new Runnable() {


					public void run() {
						AlbumPolicyDialog dialog = new AlbumPolicyDialog(shell,
								session);
						if (dialog.open() != Window.OK) {
							job.cancel();
						}
					}
				});
			} else {
				session.setAlbumPolicy(USEDEFAULT);
				session.setDefaultAlbum(photoSets.get(0));
			}
		}
	}


	@Override
	public void postProcessAllUploads(
			Collection<? extends ImageAttributes> collection, Session session,
			ExportToCommunityJob job) {
		// do nothing
	}


	@Override
	public void postProcessUpload(UploadCompleteEvent e, ImageAttributes atts) {
		atts.setPhotoId(e.getResponse().getPhotoId());
	}


	@Override
	public void replace(ImageAttributes imageAttributes, Session session,
			UploadStatusMonitor monitor) throws UploadFailedException,
			ProtocolException, CommunicationException {
		upload(imageAttributes, session, monitor, true);
	}


	@Override
	public void upload(ImageAttributes imageAttributes, Session session,
			UploadStatusMonitor monitor) throws UploadFailedException,
			ProtocolException, CommunicationException {
		upload(imageAttributes, session, monitor, false);
	}

	private void upload(ImageAttributes imageAttributes, Session session,
			UploadStatusMonitor monitor, boolean replace)
			throws CommunicationException, UploadFailedException {
		if (!isAccountAuthenticated(session.getAccount())) {
			try {
				authenticate(session);
			} catch (ProtocolException e) {
				throw new UploadFailedException(e.getMessage(), e);
			}
		}
		if (isAccountAuthenticated(session.getAccount())) {
			SmugmugUpload uploader = new SmugmugUpload(imageAttributes,
					session, replace, monitor);
			try {
				uploader.execute();
				authenticate(session);
			} catch (ProtocolException e) {
				throw new UploadFailedException(e.getMessage(), e);
			}
		}
	}

	public boolean executeMethod(SmugmugMethod method) {
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

	private void retryUnfinishedActions() {
		List<PostUploadAction> actions = new LinkedList<PostUploadAction>();

		for (List<PostUploadAction> next : unfinishedActions.values()) {
			actions.addAll(next);
		}
		unfinishedActions.clear();
		Iterator<PostUploadAction> iter = actions.iterator();
		while (iter.hasNext()) {
			executeMethod((SmugmugMethod) iter.next());
		}
	}

	/**
     *
     */
	private static boolean showUnfinishedUploadErrorMessage() {
		StringBuffer errorMessage = new StringBuffer();
		errorMessage.append(Messages
				.getString("juploadr.smugmug.error.after.upload")); //$NON-NLS-1$
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
							.getString("juploadr.smugmug.failed.to")); //$NON-NLS-1$
					errorMessage.append(method.getErrorText());
					errorMessage.append("\n"); //$NON-NLS-1$
				}
			}

		}
		errorMessage
				.append(Messages.getString("juploadr.smugmug.option.retry")); //$NON-NLS-1$
		return MessageDialog.openQuestion(null, errorMessage.toString(),
				Messages.getString("SmugmugRestApi.actions_failed")); //$NON-NLS-1$
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

	private static List<PostUploadAction> getFailedActions(
			Class<? extends PostUploadAction> clazz) {
		return unfinishedActions.get(clazz);
	}

	public void logout(Session session) throws ProtocolException,
			CommunicationException {
		Logout gus = new Logout(session);
		gus.execute();
	}


	@Override
	public String decodePassword(String encrypted) {
		if (encrypted == null)
			return ""; //$NON-NLS-1$
		char[] charArray = encrypted.toCharArray();
		int o = 7;
		for (int i = 0; i < charArray.length; i++) {
			charArray[i] -= (o % 31);
			o *= 37;
		}
		return new String(charArray);
	}


	@Override
	public String encodePassword(String text) {
		if (text == null)
			return ""; //$NON-NLS-1$
		char[] charArray = text.toCharArray();
		int o = 7;
		for (int i = 0; i < charArray.length; i++) {
			charArray[i] += (o % 31);
			o *= 37;
		}
		return new String(charArray);
	}


	public Session getSession(CommunityAccount account) throws AuthException,
			CommunicationException {
		return new Session(this, account);
	}

}
