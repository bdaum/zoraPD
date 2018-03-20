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

package com.bdaum.zoom.operations.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class RateOperation extends DbOperation {

	private int rate;
	private List<Asset> assets;
	private int[] oldRates;
	private String[] oldRatedBy;
	private int size;

	public RateOperation(List<Asset> assets, int rate) {
		super(Messages.getString("RateOperation.Rating")); //$NON-NLS-1$
		this.assets = assets;
		this.rate = rate;
		size = assets.size();
		oldRates = new int[size];
		oldRatedBy = new String[size];
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		for (int i = 0; i < size; i++)
			oldRates[i] = assets.get(i).getRating();
		return rate(aMonitor, info);
	}

	private IStatus rate(IProgressMonitor aMonitor, IAdaptable info) {
		init(aMonitor, size);
		String ratedBy = System.getProperty("user.name"); //$NON-NLS-1$
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				asset.setRating(rate);
				asset.setRatedBy(ratedBy);
				if (!storeSafely(null, 1, asset))
					break;
			}
		}
		fireApplyRules(assets, QueryField.RATEDBY);
		fireAssetsModified(new BagChange<>(null, assets, null, null), QueryField.RATING);
		return close(info);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		return rate(aMonitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		initUndo(aMonitor, size);
		List<Object> toBeStored = new ArrayList<Object>(assets.size());
		int i = 0;
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				asset.setRating(oldRates[i]);
				asset.setRatedBy(oldRatedBy[i]);
				toBeStored.add(asset);
			}
			++i;
		}
		if (storeSafely(null, 1, toBeStored))
			fireAssetsModified(new BagChange<>(null, assets, null, null), QueryField.RATING);
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
		return size > 3 ? Job.LONG : Job.SHORT;
	}

}
