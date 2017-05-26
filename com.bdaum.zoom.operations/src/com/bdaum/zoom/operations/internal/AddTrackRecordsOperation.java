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

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.TrackRecord;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class AddTrackRecordsOperation extends DbOperation {

	private final List<TrackRecord> track;

	public AddTrackRecordsOperation(List<TrackRecord> track) {
		super(Messages.getString("AddTrackRecordsOperation.add_track_records")); //$NON-NLS-1$
		this.track = track;
	}


	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		init(aMonitor, track.size());
		for (TrackRecord record : track) {
			String assetId = record.getAsset_track_parent();
			AssetImpl asset = dbManager.obtainAsset(assetId);
			String[] atrack = asset.getTrack();
			String[] newtrack;
			if (atrack == null) {
				newtrack = new String[] { record.toString() };
			} else {
				newtrack = new String[atrack.length + 1];
				System.arraycopy(atrack, 0, newtrack, 0, atrack.length);
				newtrack[atrack.length] = record.toString();
			}
			asset.setTrack(newtrack);
			storeSafely(null, 1, record, asset);
		}
		return close(info);
	}


	@Override
	public boolean canRedo() {
		return false;
	}


	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		return null;
	}


	@Override
	public boolean canUndo() {
		return false;
	}


	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		return null;
	}


	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT | IProfiledOperation.TRACK;
	}


	public int getUndoProfile() {
		return 0;
	}


	@Override
	public boolean isSilent() {
		return true;
	}

}
