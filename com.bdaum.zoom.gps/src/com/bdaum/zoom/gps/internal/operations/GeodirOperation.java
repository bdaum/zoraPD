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

package com.bdaum.zoom.gps.internal.operations;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.ui.gps.Trackpoint;

public class GeodirOperation extends DbOperation {

	private String assetId;
	private Trackpoint trackpoint;
	private double oldDir;
	private String oldRef;

	public GeodirOperation(Trackpoint trackpoint, String assetId) {
		super(Messages.getString("GeodirOperation.set_image_direction")); //$NON-NLS-1$
		this.assetId = assetId;
		this.trackpoint = trackpoint;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		init(aMonitor, 1);
		aMonitor.subTask(Messages.getString(Messages.getString("GeodirOperation.set_img_dir"))); //$NON-NLS-1$
		if (isPeerOwned(assetId))
			return close(info);
		Asset asset = dbManager.obtainAsset(assetId);
		oldDir = asset.getGPSImgDirection();
		oldRef = asset.getGPSImgDirectionRef();
		asset.setGPSImgDirection(Core.bearing(asset.getGPSLatitude(), asset.getGPSLongitude(), trackpoint.getLatitude(),
				trackpoint.getLongitude()));
		asset.setGPSImgDirectionRef("T"); //$NON-NLS-1$
		storeSafely(null, 1, asset);
		List<Asset> assets = Collections.singletonList(asset);
		fireApplyRules(assets, QueryField.EXIF_GPS);
		fireAssetsModified(new BagChange<>(null, assets, null, null), QueryField.EXIF_GPS);
		return close(info);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return execute(aMonitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		initUndo(aMonitor, 1);
		if (isPeerOwned(assetId))
			return close(info);
		Asset asset = dbManager.obtainAsset(assetId);
		asset.setGPSImgDirection(oldDir);
		asset.setGPSImgDirectionRef(oldRef);
		dbManager.safeTransaction(null, asset);
		return close(info);
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public int getPriority() {
		return Job.SHORT;
	}

}
