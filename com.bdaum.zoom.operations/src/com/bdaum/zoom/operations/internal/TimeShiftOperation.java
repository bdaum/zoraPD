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
package com.bdaum.zoom.operations.internal;

import java.util.Date;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class TimeShiftOperation extends DbOperation {

	private final List<Asset> assets;
	private final long shift;

	public TimeShiftOperation(List<Asset> assets, long shift) {
		super(Messages.getString("TimeShiftOperation_apply_time_shift")); //$NON-NLS-1$
		this.assets = assets;
		this.shift = shift;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		return shift(aMonitor, info, shift);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		return shift(aMonitor, info, shift);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		return shift(aMonitor, info, -shift);
	}

	private IStatus shift(IProgressMonitor aMonitor, IAdaptable info, long d) {
		boolean shifted = false;
		init(aMonitor, assets.size());
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				boolean changed = false;
				Date odate = asset.getDateTimeOriginal();
				if (odate != null) {
					asset.setDateTimeOriginal(new Date(odate.getTime() + d));
					changed = true;
					Date assetDate = asset.getDateTime();
					if (assetDate != null && assetDate.equals(odate))
						asset.setDateTime(new Date(assetDate.getTime() + d));
					Date creationDate = asset.getDateCreated();
					if (creationDate != null && creationDate.equals(odate))
						asset.setDateCreated(new Date(creationDate.getTime()
								+ d));
				}
				if (changed) {
					if (!storeSafely(null, 1, asset))
						break;
					shifted = true;
				}
			}
		}
		if (shifted)
			fireAssetsModified(new BagChange<>(null, assets, null, null), null);
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
		return assets.size() > 3 ? Job.LONG : Job.SHORT;
	}

}
