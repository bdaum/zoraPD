package com.bdaum.zoom.video.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.IMediaUiExtension;
import com.bdaum.zoom.ui.IZoomActionConstants;
import com.bdaum.zoom.ui.internal.views.ImageView;

@SuppressWarnings("restriction")
public class UiExtension implements IMediaUiExtension {

	private final class UpdateThumbnailAction extends Action {
		private IAdaptable info;
		private Asset asset;

		private UpdateThumbnailAction(String text) {
			super(text);
			setToolTipText(Messages.UiExtension_update_thumb_tooltext);
		}

		@Override
		public void run() {
			new SelectFrameDialog(info.getAdapter(Shell.class), asset).open();
		}

		public void setAsset(Asset asset) {
			this.asset = asset;
		}

		public Asset getAsset() {
			return asset;
		}

		public void setAdapter(IAdaptable info) {
			this.info = info;
		}
	}

	private String mediaSupportId;
	private UpdateThumbnailAction updateThumbnailAction = new UpdateThumbnailAction(Messages.UiExtension_update_thumb);

	@Override
	public void setMediaSupportId(String mediaSupportId) {
		this.mediaSupportId = mediaSupportId;
	}

	@Override
	public void addMediaContributions(IMenuManager manager, String anchor, IAdaptable info) {
		if (anchor == IZoomActionConstants.MB_EDIT) {
			if (updateThumbnailAction.getAsset() != null) {
				updateThumbnailAction.setAdapter(info);
				manager.add(updateThumbnailAction);
			}
		}
	}

	@Override
	public void registerMediaContributions(ImageView imageView) {
		imageView.registerCommand(updateThumbnailAction, "com.bdaum.zoom.video.updateThumbCommand"); //$NON-NLS-1$
	}

	@Override
	public void updateMediaContributions(ImageView imageView, int count, int localCount,
			AssetSelection assetSelection) {
		if (count == 1 && !Core.getCore().getDbManager().isReadOnly()) {
			Asset asset = assetSelection.get(0);
			IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(asset.getFormat());
			if (mediaSupport != null && mediaSupport.getId().equals(mediaSupportId)) {
				updateThumbnailAction.setAsset(asset);
				updateThumbnailAction.setEnabled(asset.getFileState() != IVolumeManager.PEER);
				return;
			}
		}
		updateThumbnailAction.setAsset(null);
		updateThumbnailAction.setEnabled(false);
	}

}
