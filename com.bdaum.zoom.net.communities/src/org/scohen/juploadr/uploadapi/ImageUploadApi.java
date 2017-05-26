/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 * It is an adaptation of the equally named file from the jUploadr project (http://sourceforge.net/projects/juploadr/)
 * (c) 2004 Steve Cohen and others
 *
 * jUploadr is licensed under the GNU Library or Lesser General Public License (LGPL).
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
 * Modifications (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

/**
 * @author wItspirit
 * 28-mei-2005
 * FlickrApi.java
 * @author bdaum
 * 2010
 * generalized for other communities
 */

package org.scohen.juploadr.uploadapi;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.swt.graphics.Image;
import org.scohen.juploadr.app.ImageAttributes;
import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.app.StringSigner;
import org.scohen.juploadr.app.tags.TagParser;
import org.scohen.juploadr.event.UploadCompleteEvent;
import org.scohen.juploadr.event.UploadStatusMonitor;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.net.communities.CommunitiesActivator;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.communities.CommunityApi;
import com.bdaum.zoom.net.communities.Messages;
import com.bdaum.zoom.net.communities.jobs.ExportToCommunityJob;

public abstract class ImageUploadApi implements CommunityApi {
	protected Image icon;
	protected boolean supportsTagging;
	protected boolean supportsPhotosets;
	protected boolean supportsCategories;
	protected boolean supportsSubcategories;
	protected boolean singlePhotosets;
	protected boolean setCategories;
	protected boolean setSubcategories;
	protected boolean supportsGeocoding;
	private IErrorHandler handler;
	private String siteName;
	private MultiStatus status;

	/* (nicht-Javadoc)
	 * @see org.scohen.juploadr.uploadapi.CommunityApi#getSiteName()
	 */
	public String getSiteName() {
		return siteName;
	}

	/* (nicht-Javadoc)
	 * @see org.scohen.juploadr.uploadapi.CommunityApi#initialize(org.scohen.juploadr.uploadapi.Session)
	 */
	public void initialize(Session session) {
		IConfigurationElement conf = (IConfigurationElement) session
				.getAccount().getConfiguration().getParent();
		siteName = conf.getAttribute("name"); //$NON-NLS-1$
		supportsTagging = Boolean.parseBoolean(conf
				.getAttribute("supportsTagging")); //$NON-NLS-1$
		String attribute = conf.getAttribute("supportsPhotosets"); //$NON-NLS-1$
		supportsPhotosets = attribute != null && !"false".equals(attribute); //$NON-NLS-1$
		singlePhotosets = "single".equals(attribute); //$NON-NLS-1$
		attribute = conf.getAttribute("supportsCategories"); //$NON-NLS-1$
		supportsCategories = attribute != null && !attribute.equals("false"); //$NON-NLS-1$
		setCategories = "set".equals(attribute); //$NON-NLS-1$
		attribute = conf.getAttribute("supportsSubCategories"); //$NON-NLS-1$
		supportsSubcategories = attribute != null && !attribute.equals("false"); //$NON-NLS-1$
		setSubcategories = "set".equals(attribute); //$NON-NLS-1$
		supportsGeocoding = Boolean.parseBoolean(conf
				.getAttribute("supportsGeocoding")); //$NON-NLS-1$
	}

	/* (nicht-Javadoc)
	 * @see org.scohen.juploadr.uploadapi.CommunityApi#authenticate(org.scohen.juploadr.uploadapi.Session)
	 */
	public abstract boolean authenticate(Session session)
			throws ProtocolException, CommunicationException;

	/**
	 * @param account
	 *            the account where the image is stored
	 * @param photoId
	 *            the id of the photo
	 * @return
	 * @throws CommunicationException
	 * @throws InfoFailedException
	 * @throws ProtocolException
	 */
	public abstract ImageInfo getImageInfo(Session session, String photoId)
			throws CommunicationException, InfoFailedException,
			ProtocolException;

	/**
	 * Uploads the image and data in <code>imageAttributes</code> for Flickr
	 * user <code>userInfo</code> and notifying <code>monitor</code> of upload
	 * bar.
	 *
	 * @param imageAttributes
	 *            the image to be uploaded
	 * @param account
	 *            the account to which the image is uploaded
	 * @param monitor
	 *            The monitor that should be notified of upload bar
	 * @throws UploadFailedException
	 *             When a problem occurs during the uploading
	 */
	public abstract void upload(ImageAttributes imageAttributes,
			Session session, UploadStatusMonitor monitor)
			throws UploadFailedException, ProtocolException,
			CommunicationException;

	/**
	 * Replaces the image and data in <code>imageAttributes</code> for Flickr
	 * user <code>userInfo</code> and notifying <code>monitor</code> of upload
	 * bar.
	 *
	 * @param imageAttributes
	 *            the image to be uploaded
	 * @param account
	 *            the account to which the image is uploaded
	 * @param monitor
	 *            The monitor that should be notified of upload bar
	 * @throws UploadFailedException
	 *             When a problem occurs during the uploading
	 */
	public abstract void replace(ImageAttributes imageAttributes,
			Session session, UploadStatusMonitor monitor)
			throws UploadFailedException, ProtocolException,
			CommunicationException;

	/**
	 * Invoked after upload of an image
	 * @param e - Upload completion notification
	 * @param atts - uploaded image
	 */
	public abstract void postProcessUpload(UploadCompleteEvent e,
			ImageAttributes atts);

	/**
	 * Factory method to create a new PhotoSet instance
	 * @param name - name of photoset
	 * @param description - description of photoset
	 * @param keywords - initial keywords
	 * @param primaryPhoto - primary photo or null
	 * @return
	 */
	public abstract PhotoSet newPhotoSet(String name, String description,
			String[] keywords, ImageAttributes primaryPhoto);

	/**
	 * Event handler that is fired after all photos have finished uploading
	 *
	 * @param collection
	 *            All the photos that have been uploaded: consists of
	 *            ImageAttributes objects
	 */
	public abstract void postProcessAllUploads(
			Collection<? extends ImageAttributes> collection, Session session,
			ExportToCommunityJob job);

	/* (nicht-Javadoc)
	 * @see org.scohen.juploadr.uploadapi.CommunityApi#isAccountAuthenticated(com.bdaum.zoom.net.communities.CommunityAccount)
	 */
	public boolean isAccountAuthenticated(CommunityAccount account) {
		return account.isAuthenticated();
	}

	/**
	 * Factory method to create a new instance of this API
	 * @param configElement - community-configuration element
	 * @return - new instance
	 * @throws CoreException
	 */
	public static ImageUploadApi newInstance(IConfigurationElement configElement)
			throws CoreException {
		return (ImageUploadApi) configElement.createExecutableExtension("api"); //$NON-NLS-1$
	}

	/**
	 * Factory method for obtaining a tag parser instance
	 * @return tag parser
	 */
	public abstract TagParser getTagParser();

	/**
	 * Logout from session
	 * @param sessionId - session ID
	 * @throws ProtocolException
	 * @throws CommunicationException
	 */
	public void logout(String sessionId) throws ProtocolException,
			CommunicationException {
		// do nothing, subclasses may override
	}

	/* (nicht-Javadoc)
	 * @see org.scohen.juploadr.uploadapi.CommunityApi#decodePassword(java.lang.String)
	 */
	public String decodePassword(String passwordHash) {
		return null;
	}

	/* (nicht-Javadoc)
	 * @see org.scohen.juploadr.uploadapi.CommunityApi#encodePassword(java.lang.String)
	 */
	public String encodePassword(String password) {
		return StringSigner.md5(password);
	}

	/**
	 * Is called before upload occurs. Subclasses should override
	 * @param assets - images to be uploaded
	 * @param session - session object
	 * @param exportToCommunityJob - upload job
	 */
	public void preProcessAllUploads(Collection<? extends Asset> assets,
			Session session, ExportToCommunityJob exportToCommunityJob) {
		// do nothing, subclasses may override
	}

	/**
	 * @return true if community supports tagging
	 */
	public boolean isSupportsTagging() {
		return supportsTagging;
	}

	/**
	 * @return true if community supports photosets
	 */
	public boolean isSupportsPhotosets() {
		return supportsPhotosets;
	}

	/**
	 * @return true if community supports categories
	 */
	public boolean isSupportsCategories() {
		return supportsCategories;
	}

	/**
	 * @return true if community supports subcategories
	 */
	public boolean isSupportsSubcategories() {
		return supportsSubcategories;
	}

	/**
	 * @return true, if an image can only belong to a single photoset
	 */
	public boolean isSinglePhotosets() {
		return singlePhotosets;
	}

	/**
	 * @return true if only photosets may have categories
	 */
	public boolean isSetCategories() {
		return setCategories;
	}

	public boolean isSetSubcategories() {
		return setSubcategories;
	}

	/**
	 * @return  true if community supports geocoding
	 */
	public boolean isSupportsGeocoding() {
		return supportsGeocoding;
	}

	/* (nicht-Javadoc)
	 * @see org.scohen.juploadr.uploadapi.CommunityApi#handleCommunicationError(java.lang.Exception)
	 */
	public void handleCommunicationError(Exception e) {
		if (e instanceof ProtocolException)
			CommunitiesActivator.getDefault().logError(
					Messages.CommunityAccount_internal_protocol_error, e);
		else if (handler != null)
			handler.handleError(this, e);
		else
			CommunitiesActivator.getDefault()
					.logError(Messages.ImageUploadApi_communication_error, e);
	}

	/* (nicht-Javadoc)
	 * @see org.scohen.juploadr.uploadapi.CommunityApi#setErrorHandler(org.scohen.juploadr.uploadapi.IErrorHandler)
	 */
	public void setErrorHandler(IErrorHandler handler) {
		this.handler = handler;
	}

	public void setStatus(MultiStatus status) {
		this.status = status;
	}

	/**
	 * @return status
	 */
	public MultiStatus getStatus() {
		return status;
	}

	public IErrorHandler getErrorHandler() {
		return handler;
	}

}
