package com.bdaum.zoom.ui.internal.job;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.program.IRawConverter;

@SuppressWarnings("restriction")
public class UpdateRawImagesJob extends SynchronizeCatJob {

	private final boolean all;

	public UpdateRawImagesJob(boolean all) {
		super(Messages.UpdateRawImagesJob_updating_raw_images);
		this.all = all;
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.CRITICAL == family || super.belongsTo(family);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		List<File> outdatedFiles = new ArrayList<File>();
		IRawConverter currentRawConverter = BatchActivator.getDefault()
				.getCurrentRawConverter(false);
		ICore core = Core.getCore();
		IVolumeManager volumeManager = core.getVolumeManager();
		List<AssetImpl> assets = core.getDbManager().obtainAssets();
		monitor.beginTask(Messages.UpdateRawImagesJob_updating_raw_images,
				assets.size());
		for (AssetImpl asset : assets) {
			URI uri = volumeManager.findExistingFile(asset, true);
			if (uri != null) {
				String uriString = uri.toString();
				if (all
						&& ImageConstants.isRaw(uriString, true)
						|| currentRawConverter != null
						&& currentRawConverter.getRecipe(uri.toString(), false,
								null, null, null) != null)
					outdatedFiles.add(new File(uri));
			}
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			monitor.worked(1);
		}
		monitor.done();
		if (!outdatedFiles.isEmpty())
			new ChangeProcessor(null, outdatedFiles, null, null,
					System.currentTimeMillis(), null, Constants.CRITICAL, this)
					.schedule(250);
		return Status.OK_STATUS;
	}

}
