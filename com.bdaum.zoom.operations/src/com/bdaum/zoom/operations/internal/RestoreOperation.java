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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.core.trash.Trash;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class RestoreOperation extends DbOperation {

	private Object[] selection;
	private ImportConfiguration configuration;
	protected Asset asset;

	public RestoreOperation(Object[] selection, ImportConfiguration configuration) {
		super(Messages.getString("RestoreOperation.Restore")); //$NON-NLS-1$
		this.selection = selection;
		this.configuration = configuration;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		init(aMonitor, selection.length);
		boolean changed = false;
		openIndexWriter();
		Set<Asset> restoredAssets = new HashSet<>(selection.length * 3 / 2);
		int i = 0;
		try {
			for (Object obj : selection) {
				final Trash t = (Trash) obj;
				if (!storeSafely(() -> {
					asset = t.restore(dbManager, iw, status);
					restoredAssets.add(asset);
				}, 1))
					return close(info);
				changed |= updateFolderHierarchies(asset, true, configuration.timeline, configuration.locations, false);
				if (i == 0) {
					fireAssetsModified(new BagChange<>(restoredAssets, null, null, null), null);
					if (changed) {
						fireStructureModified();
						changed = false;
					}
					restoredAssets.clear();
				}
				if (i++ == 8)
					i = 0;
			}
		} finally {
			closeIndex();
			if (i > 0)
				fireAssetsModified(new BagChange<>(restoredAssets, null, null, null), null);
			if (changed)
				fireStructureModified();
		}
		return close(info);
	}

	@Override
	public boolean canUndo() {
		return false;
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
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return null;
	}

	public int getExecuteProfile() {
		return IProfiledOperation.INDEX | IProfiledOperation.FILE;
	}

	public int getUndoProfile() {
		return IProfiledOperation.FILE;
	}

	@Override
	public int getPriority() {
		return Job.SHORT;
	}

}
