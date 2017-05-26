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

public class ColorCodeOperation extends DbOperation {

	private int code;
	private List<Asset> assets;
	private int[] oldCodes;

	public ColorCodeOperation(List<Asset> assets, int code) {
		super(Messages.getString("ColorCodeOperation.Color_code")); //$NON-NLS-1$
		this.assets = assets;
		this.code = code;
		oldCodes = new int[assets.size()];
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		for (int i = 0; i < assets.size(); i++)
			oldCodes[i] = assets.get(i).getColorCode();
		setCode(aMonitor);
		fireApplyRules(assets, QueryField.COLORCODE);
		return close(info);
	}

	private void setCode(IProgressMonitor aMonitor) {
		init(aMonitor, assets.size());
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				asset.setColorCode(code);
				storeSafely(null, 1, asset);
			}
		}
		fireAssetsModified(new BagChange<>(null, assets, null, null), QueryField.COLORCODE);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		setCode(aMonitor);
		return close(info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		int size = assets.size();
		initUndo(aMonitor, size);
		List<Object> toBeStored = new ArrayList<Object>(assets.size());
		int i = 0;
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				asset.setColorCode(oldCodes[i]);
				toBeStored.add(asset);
			}
			++i;
		}
		if (storeSafely(null, size, toBeStored))
			fireAssetsModified(new BagChange<>(null, assets, null, null), QueryField.COLORCODE);
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
