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
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.core.trash.StructBackup;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class StructDeleteOperation extends DbOperation {

	private IIdentifiableObject element;
	private List<String> assetsToUpdate;

	public StructDeleteOperation(String label, IIdentifiableObject element,
			List<String> assetsToUpdate) {
		super(label);
		this.element = element;
		this.assetsToUpdate = assetsToUpdate;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		final StructBackup backup = new StructBackup(opId, element);
		int work = 100000000;
		init(aMonitor, work);
		collectAssetsToUpdate(aMonitor, work, element, assetsToUpdate, backup);
		if (aMonitor.isCanceled()) {
			assetsToUpdate.clear();
			return abort();
		}
		storeSafely(new Runnable() {
			public void run() {
				backup.performDelete(dbManager);
			}
		}, 1);
		return close(info);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		return execute(aMonitor, info);
	}

	@Override
	public IStatus undo(final IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		List<StructBackup> set = dbManager.getTrash(StructBackup.class, opId);
		initUndo(aMonitor, set.size());
		for (StructBackup backup : set) {
			final StructBackup b = backup;
			if (!storeSafely(new Runnable() {
				public void run() {
					b.restore(dbManager);
				}
			}, 1))
				break;
		}
		return close(info);
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
