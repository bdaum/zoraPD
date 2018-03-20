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

import java.net.URI;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class SynchronizeOperation extends AbstractOperation implements
		IProfiledOperation {

	private DbOperation importOp;
	private DbOperation deleteOp;

	public SynchronizeOperation(FileInput outdatedFiles,
			ImportConfiguration configuration, List<URI> uris,
			List<Asset> assets) {
		super(Messages.getString("SynchronizeOperation.Synchronize")); //$NON-NLS-1$
		if (outdatedFiles != null && outdatedFiles.size() > 0)
			importOp = new ImportOperation(
					Messages.getString("SynchronizeOperation.Synchronizing"), outdatedFiles, //$NON-NLS-1$
					configuration);
		if (uris != null && !uris.isEmpty())
			importOp = new ImportOperation(
					Messages
							.getString("SynchronizeOperation.refreshing_remote"), uris.toArray(new URI[uris.size()]), //$NON-NLS-1$
					configuration);
		if (assets != null && !assets.isEmpty())
			deleteOp = new DeleteOperation(assets, false, null, null, null, configuration);
	}


	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (importOp != null)
			importOp.execute(monitor, info);
		if (deleteOp != null && !monitor.isCanceled())
			deleteOp.execute(monitor, info);
		return Status.OK_STATUS;
	}


	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (importOp != null)
			importOp.redo(monitor, info);
		if (deleteOp != null && !monitor.isCanceled())
			deleteOp.redo(monitor, info);
		return Status.OK_STATUS;
	}


	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException {
		if (deleteOp != null)
			deleteOp.undo(monitor, info);
		if (importOp != null && !monitor.isCanceled())
			importOp.undo(monitor, info);
		return Status.OK_STATUS;
	}


	public int getExecuteProfile() {
		int profile = 0;
		if (importOp != null)
			profile |= importOp.getExecuteProfile();
		if (deleteOp != null)
			profile |= deleteOp.getExecuteProfile();
		return profile;
	}


	public int getUndoProfile() {
		int profile = 0;
		if (importOp != null)
			profile |= importOp.getUndoProfile();
		if (deleteOp != null)
			profile |= deleteOp.getUndoProfile();
		return profile;
	}


	public int getPriority() {
		return Job.LONG;
	}


	public boolean isSilent() {
		return false;
	}

}
