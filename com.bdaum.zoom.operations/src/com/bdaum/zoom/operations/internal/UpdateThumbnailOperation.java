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
 * (c) 2012 Berthold Daum  
 */

package com.bdaum.zoom.operations.internal;

import java.util.Collections;

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

public class UpdateThumbnailOperation extends DbOperation {

	private Asset asset;
	private final byte[] jpeg;
	private byte[] oldJpeg;

	public UpdateThumbnailOperation(Asset asset, byte[] jpeg) {
		super(Messages.getString("UpdateThumbnailOperation.update_thumbs")); //$NON-NLS-1$
		this.asset = asset;
		this.jpeg = jpeg;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		init(aMonitor, 1);
		if (asset.getFileState() != IVolumeManager.PEER) {
			oldJpeg = asset.getJpegThumbnail();
			asset.setJpegThumbnail(jpeg);
			storeSafely(null, 1, asset);
			fireAssetsModified(new BagChange<>(null, Collections.singleton(asset), null, null), null);
		}
		return close(info);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return execute(aMonitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		initUndo(aMonitor, 1);
		if (asset.getFileState() != IVolumeManager.PEER) {
			asset.setJpegThumbnail(oldJpeg);
			storeSafely(null, 1, asset);
			fireAssetsModified(new BagChange<>(null, Collections.singleton(asset), null, null), null);
		}
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
