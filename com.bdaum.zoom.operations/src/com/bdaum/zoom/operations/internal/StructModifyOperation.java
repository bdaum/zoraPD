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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class StructModifyOperation extends DbOperation {

	private final Map<String, Map<QueryField, Object>> structOverlayMap;
	private final Map<String, Map<QueryField, Object>> backupMap;
	private final Stack<DbOperation> undoStack = new Stack<DbOperation>();
	private int work;
	private final Map<String, IIdentifiableObject> newObjects;
	private List<String> assetsToUpdate = new ArrayList<String>();

	public StructModifyOperation(String label,
			Map<String, Map<QueryField, Object>> structOverlayMap,
			Map<String, IIdentifiableObject> newObjects) {
		super(label);
		this.structOverlayMap = structOverlayMap;
		this.newObjects = newObjects;
		this.work = structOverlayMap.size();
		this.backupMap = new HashMap<String, Map<QueryField, Object>>(work);
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		init(aMonitor, 1010000 * work);
		List<IIdentifiableObject> toBeStored = new ArrayList<IIdentifiableObject>();
		for (Map.Entry<String, Map<QueryField, Object>> entry : structOverlayMap
				.entrySet()) {
			String id = entry.getKey();
			IIdentifiableObject obj = newObjects == null ? null : newObjects.get(id);
			if (obj == null)
				obj = dbManager.obtainById(IdentifiableObject.class, id);
			if (obj != null) {
				Map<QueryField, Object> fieldMap = entry.getValue();
				if (fieldMap == null) {
					StructDeleteOperation op = new StructDeleteOperation(
							getLabel(), obj, assetsToUpdate);
					undoStack.push(op);
					op.execute(SubMonitor.convert(monitor, 2), info);
				} else {
					if (obj instanceof Location)
						dbManager.markSystemCollectionsForPurge((Location) obj);
					Map<QueryField, Object> backupFields = new HashMap<QueryField, Object>(
							fieldMap.size());
					backupMap.put(id, backupFields);
					for (Map.Entry<QueryField, Object> fieldEntry : fieldMap
							.entrySet()) {
						QueryField qf = fieldEntry.getKey();
						Object oldValue = qf.obtainPlainFieldValue(obj);
						backupFields.put(qf, oldValue);
						Object value = fieldEntry.getValue();
						qf.setFieldValue(obj, value);
					}
					if (obj instanceof Location)
						dbManager.createLocationFolders((Location) obj,
								dbManager.getMeta(true).getLocationFolders());
					toBeStored.add(obj);
					collectAssetsToUpdate(aMonitor, 1000000, obj, assetsToUpdate, null);
					if (aMonitor.isCanceled()) {
						assetsToUpdate.clear();
						return abort();
					}
				}
			}
		}
		storeSafely(null, 10000*work, toBeStored);
		return close(info, assetsToUpdate.toArray(new String[assetsToUpdate.size()]));
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		assetsToUpdate.clear();
		return execute(aMonitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		initUndo(aMonitor, work);
		while (!undoStack.isEmpty()) {
			DbOperation op = undoStack.pop();
			op.undo(SubMonitor.convert(monitor, 2), info);
		}
		List<IdentifiableObject> toBeStored = new ArrayList<IdentifiableObject>();
		for (Map.Entry<String, Map<QueryField, Object>> entry : backupMap
				.entrySet()) {
			String id = entry.getKey();
			IdentifiableObject obj = dbManager.obtainById(
					IdentifiableObject.class, id);
			if (obj != null) {
				Map<QueryField, Object> fieldMap = entry.getValue();
				if (fieldMap != null) {
					if (obj instanceof Location)
						dbManager.markSystemCollectionsForPurge((Location) obj);
					for (Map.Entry<QueryField, Object> fieldEntry : fieldMap
							.entrySet()) {
						QueryField qf = fieldEntry.getKey();
						Object value = fieldEntry.getValue();
						qf.setFieldValue(obj, value);
					}
					if (obj instanceof Location)
						dbManager.createLocationFolders((Location) obj,
								dbManager.getMeta(true).getLocationFolders());
					toBeStored.add(obj);
				}
			}
			monitor.worked(1);
		}
		Object[] toBeDeleted = newObjects == null ? null : newObjects.values().toArray();
		storeSafely(toBeDeleted, work + (toBeDeleted == null ? 0 : toBeDeleted.length), toBeStored);
		backupMap.clear();
		return close(info, assetsToUpdate.toArray(new String[assetsToUpdate.size()]));
	}

	public int getExecuteProfile() {
		return IProfiledOperation.STRUCT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.STRUCT;
	}

	@Override
	public int getPriority() {
		return Job.SHORT;
	}

}
