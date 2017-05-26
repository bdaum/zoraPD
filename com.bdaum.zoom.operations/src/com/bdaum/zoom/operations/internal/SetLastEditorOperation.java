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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class SetLastEditorOperation extends DbOperation {

	private String lastEditor;
	private final AssetImpl[] assets;

	public SetLastEditorOperation(AssetImpl[] assets, String lastEditor) {
		super(Messages.getString("SetLastEditorOperation.set_last_editor")); //$NON-NLS-1$
		this.assets = assets;
		this.lastEditor = lastEditor;
	}

	@Override
	public boolean isSilent() {
		return true;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		init(aMonitor, assets.length);
		for (AssetImpl asset : assets)
			if (asset.getFileState() != IVolumeManager.PEER)
				asset.setLastEditor(lastEditor);
		storeSafely(null, assets.length, (Object[]) assets);
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
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

}
