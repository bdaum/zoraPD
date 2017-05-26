package com.bdaum.zoom.video.youtube.internal.wizard;

import java.util.ArrayList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.scohen.juploadr.uploadapi.AuthException;
import org.scohen.juploadr.uploadapi.CommunicationException;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.net.communities.ui.AbstractCommunityExportWizard;
import com.bdaum.zoom.operations.internal.AbstractMediaSupport;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.video.youtube.internal.Activator;
import com.bdaum.zoom.video.youtube.internal.YouTubeUploadClient;

@SuppressWarnings("restriction")
public class YouTubeExportWizard extends AbstractCommunityExportWizard {

	private ExportToYouTubePage mainPage;

	public YouTubeExportWizard() {
		super();
		setHelpAvailable(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		api = new YouTubeUploadClient();
		setDialogSettings(Ui.getUi().getDialogSettings(settingsId));
		IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		AssetSelection selectedAssets;
		if (activeWorkbenchWindow != null)
			selectedAssets = Ui.getUi()
					.getNavigationHistory(activeWorkbenchWindow)
					.getSelectedAssets();
		else
			selectedAssets = (selection instanceof AssetSelection) ? ((AssetSelection) selection)
					: AssetSelection.EMPTY;
		assets = new ArrayList<Asset>(selectedAssets.size());
		for (Asset asset : selectedAssets) {
			IMediaSupport mediaSupport = CoreActivator.getDefault()
					.getMediaSupport(asset.getFormat());
			if (mediaSupport != null
					&& mediaSupport.testProperty(AbstractMediaSupport.VIDEO))
				assets.add(asset);
		}
		int size = assets.size();
		setWindowTitle(size == 0 ? Messages.YouTubeExportWizard_no_video_selected
				: size == 1 ? Messages.YouTubeExportWizard_export_one_video
						: NLS.bind(
								Messages.YouTubeExportWizard_export_n_videos,
								size));
	}

	@Override
	public void addPages() {
		ImageDescriptor imageDescriptor = AbstractUIPlugin
				.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
						"/icons/banner/youTube.jpg"); //$NON-NLS-1$
		mainPage = new ExportToYouTubePage(
				configElement,
				assets,
				communityId,
				NLS.bind(Messages.YouTubeExportWizard_export_to, communityName),
				imageDescriptor);
		addPage(mainPage);
	}

	@Override
	protected boolean doFinish() throws CommunicationException, AuthException {
		return mainPage.finish();
	}

}
