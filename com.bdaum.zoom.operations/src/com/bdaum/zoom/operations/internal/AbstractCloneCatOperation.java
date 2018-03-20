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
import java.util.Map.Entry;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.bdaum.aoModeling.runtime.AomMap;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.meta.LastDeviceImport;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public abstract class AbstractCloneCatOperation extends DbOperation {

	protected AbstractCloneCatOperation(String label) {
		super(label);
	}


	protected List<Object> cloneMeta(IDbManager newDbManager, final Meta oldMeta, Meta newMeta, String description) {
		newMeta.setDescription(description);
		newMeta.setOwner(oldMeta.getOwner());
		newMeta.setThumbnailFromPreview(oldMeta.getThumbnailFromPreview());
		newMeta.setThumbnailResolution(oldMeta.getThumbnailResolution());
		newMeta.setTimeline(oldMeta.getTimeline());
		newMeta.setUserFieldLabel1(oldMeta.getUserFieldLabel1());
		newMeta.setUserFieldLabel2(oldMeta.getUserFieldLabel2());
		List<Object> toBeStored = new ArrayList<Object>();
		AomMap<String, LastDeviceImport> set = oldMeta.getLastDeviceImport();
		for (Entry<String, LastDeviceImport> entry : set.entrySet()) {
			LastDeviceImport lastDeviceImport = entry.getValue();
			toBeStored.add(lastDeviceImport);
			newMeta.putLastDeviceImport(lastDeviceImport);
		}
		return toBeStored;
	}

	protected SmartCollection getRootCollection(SmartCollectionImpl coll) {
		SmartCollection parent = coll;
		while (parent.getSmartCollection_subSelection_parent() != null)
			parent = parent.getSmartCollection_subSelection_parent();
		return parent;
	}
	
	@Override
	public boolean canRedo() {
		return false;
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return null;
	}

	@Override
	public boolean canUndo() {
		return false;
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return null;
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CAT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CAT;
	}



}