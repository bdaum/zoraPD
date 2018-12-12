package com.bdaum.zoom.video.youtube.internal.job;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.scohen.juploadr.uploadapi.IErrorHandler;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.net.communities.CommunitiesActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.video.youtube.internal.Activator;
import com.bdaum.zoom.video.youtube.internal.YouTubeUploadClient;

@SuppressWarnings("restriction")
public class ExportToYouTubeJob extends CustomJob implements IErrorHandler {

	private final Collection<Asset> assets;
	private MultiStatus status;
	private Object communityName;
	private final Session session;
	private final String category;
	private Object firstVideoId;
	private final boolean includeKeywords;
	private final boolean includeGeo;

	public ExportToYouTubeJob(IConfigurationElement configElement,
			Collection<Asset> assets, Session session, String category,
			boolean includeKeywords, boolean includeGeo, IAdaptable adaptable) {
		super(Messages.ExportToYouTubeJob_upload);
		this.session = session;
		this.category = category;
		this.includeKeywords = includeKeywords;
		this.includeGeo = includeGeo;
		communityName = configElement.getAttribute("name"); //$NON-NLS-1$
		this.assets = assets;
		session.getApi().setErrorHandler(this);
	}

	@Override
	protected IStatus runJob(IProgressMonitor monitor) {
		status = new MultiStatus(CommunitiesActivator.PLUGIN_ID, 0, NLS.bind(
				Messages.ExportToYouTubeJob_export_report, communityName), null);
		int size = assets.size();
		monitor.beginTask(
				NLS.bind(Messages.ExportToYouTubeJob_exporting_video, communityName),
				size * 100);

		for (Asset asset : assets)
			exportAsset(asset, monitor);
		String url = NLS.bind(
						"http://www.youtube.com/my_videos_edit?ns=1&feature=vm&video_id={0}", //$NON-NLS-1$
						firstVideoId);
		try {
			IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport()
					.getExternalBrowser();
			browser.openURL(new URL(url));
		} catch (PartInitException e) {
			// do nothing
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// should never happen
		}
		monitor.done();
		return status;
	}

	private void exportAsset(Asset asset, IProgressMonitor monitor) {
		IVolumeManager vm = Core.getCore().getVolumeManager();
		URI uri = vm.findExistingFile(asset, true);
		if (uri != null) {
			try {
				Date dateCreated = asset.getDateCreated();
				if (dateCreated == null)
					dateCreated = asset.getDateTimeOriginal();
				StringBuilder location = new StringBuilder();
				if (includeGeo
						&& (Double.isNaN(asset.getGPSLatitude()) || Double
								.isNaN(asset.getGPSLongitude()))) {
					IDbManager dbManager = Core.getCore().getDbManager();
					List<LocationCreatedImpl> rel = dbManager.obtainObjects(
							LocationCreatedImpl.class,
							"asset", asset.getStringId(), QueryField.EQUALS); //$NON-NLS-1$
					if (!rel.isEmpty()) {
						String locId = rel.get(0).getLocation();
						LocationImpl loc = dbManager.obtainById(
								LocationImpl.class, locId);
						if (loc != null) {
							if (loc.getCity() != null
									&& loc.getCity().length() > 0)
								location.append(loc.getCity());
							if (loc.getProvinceOrState() != null
									&& loc.getProvinceOrState().length() > 0) {
								if (location.length() > 0)
									location.append(", "); //$NON-NLS-1$
								location.append(loc.getProvinceOrState());
							}
							if (loc.getCountryName() != null
									&& loc.getCountryName().length() > 0) {
								if (location.length() > 0)
									location.append(", "); //$NON-NLS-1$
								location.append(loc.getCountryName());
							}
						}
					}
				}
				YouTubeUploadClient api = (YouTubeUploadClient) session
						.getApi();
				String error = api.uploadVideo(monitor, session, uri,
						asset.getMimeType(),
						UiUtilities.createSlideTitle(asset), dateCreated,
						includeKeywords ? asset.getKeyword() : null, category, asset.getCopyright(),
						includeGeo ? asset.getGPSLatitude() : Double.NaN, asset.getGPSLongitude(),
						location.toString(),
						QueryField.SAFETY_SAFE != asset.getSafety());
				if (error != null)
					addError(NLS.bind(Messages.ExportToYouTubeJob_error_uploading,
							uri, error), null);
				else {
					String videoId = api.getVideoId();
					if (videoId != null && firstVideoId == null)
						firstVideoId = videoId;
				}
			} catch (IOException e) {
				addError(NLS.bind(Messages.ExportToYouTubeJob_io_error_upload, uri),
						e);
//			} catch (ServiceException e) {
//				addError(NLS.bind(Messages.ExportToYouTubeJob_service_error_upload,
//						uri), e);
			} catch (InterruptedException e) {
				addError(NLS.bind(Messages.ExportToYouTubeJob_upload_interrupted, uri), e);
			}
		}

	}

	private void addError(String text, Throwable t) {
		status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, text, t));
	}

	public void handleError(Object source, Exception e) {
		status.add(new Status(IStatus.ERROR, CommunitiesActivator.PLUGIN_ID,
				NLS.bind(Messages.ExportToYouTubeJob_communication_error, session
						.getApi().getSiteName()), e));
	}

}
