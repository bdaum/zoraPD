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

package com.bdaum.zoom.core.internal.operations;

import java.util.Collection;
import java.util.Date;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class UpdateTextIndexOperation extends DbOperation {

	private Object textObjects;

	public UpdateTextIndexOperation(String[] textObjects) {
		super(Messages.getString("UpdateTextIndexOperation.update_text_index")); //$NON-NLS-1$
		this.textObjects = textObjects;
		this.date = new Date();
	}

	public UpdateTextIndexOperation(Collection<? extends Asset> textObjects) {
		super(Messages.getString("UpdateTextIndexOperation.update_text_index")); //$NON-NLS-1$
		this.textObjects = textObjects;
		this.date = new Date();
	}

	@Override
	public boolean isSilent() {
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		init(aMonitor, IProgressMonitor.UNKNOWN);
		if (textObjects instanceof String[]) {
			Job job = Core.getCore().getDbFactory().getLireService(true)
					.createIndexingJob((String[]) textObjects);
			if (job != null)
				job.schedule(20L);
		} else if (textObjects instanceof Collection<?>) {
			Job job = Core
					.getCore()
					.getDbFactory()
					.getLireService(true)
					.createIndexingJob((Collection<Asset>) textObjects, true,
							-1, 0, true);
			if (job != null)
				job.schedule(20L);
		}
		textObjects = null;
		return close(info, (String[]) null);
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
		// return IProfiledOperation.INDEX;
		return IProfiledOperation.NONE;
	}

	public int getUndoProfile() {
		return IProfiledOperation.NONE;
	}

}
