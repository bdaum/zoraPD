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

import com.bdaum.aoModeling.runtime.AomObject;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class UnlinkOperation extends DbOperation {

	private int type;
	private String parent;
	private String child;
	private AomObject deleted;
	private String modified;
	private String removed;

	public UnlinkOperation(int type, String parent, String child) {
		super(Messages.getString("UnlinkOperation.Unlink")); //$NON-NLS-1$
		this.type = type;
		this.parent = parent;
		this.child = child;
	}

	@Override
	@SuppressWarnings("fallthrough")
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		init(aMonitor, 1);
		if (!isPeerOwned(parent) && !isPeerOwned(child)) {
			switch (type) {
			case Constants.DERIVATIVES:
				String s = child;
				child = parent;
				parent = s;
			case Constants.ORIGINALS:
				List<DerivedByImpl> derived = dbManager
						.obtainObjects(
								DerivedByImpl.class,
								false,
								"derivative", parent, //$NON-NLS-1$
								QueryField.EQUALS,
								"original", child, QueryField.EQUALS); //$NON-NLS-1$
				if (!derived.isEmpty()) {
					deleted = derived.get(0);
					storeSafely(new Object[] { deleted }, 1, new Object[0]);
				}
				break;
			case Constants.COMPOSITES:
				s = child;
				child = parent;
				parent = s;
				/* FALL-THROUGH */
			case Constants.COMPONENTS:
				List<ComposedToImpl> composed = dbManager.obtainObjects(
						ComposedToImpl.class,
						false,
						"composite", parent, //$NON-NLS-1$
						QueryField.EQUALS,
						"component", child, QueryField.CONTAINS); //$NON-NLS-1$
				if (!composed.isEmpty()) {
					ComposedToImpl rel = composed.get(0);
					if (rel.getComponent().size() > 1) {
						rel.removeComponent(child);
						modified = rel.getStringId();
						removed = child;
						storeSafely(null, 1, rel);
					} else {
						deleted = rel;
						storeSafely(new Object[] { deleted }, 1, new Object[0]);
					}
				}
				break;
			}
			fireHierarchyModified();
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
		return modified != null || deleted != null;
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		initUndo(aMonitor, 1);
		if (!isPeerOwned(parent) && !isPeerOwned(child)) {
			if (modified != null) {
				ComposedToImpl rel = dbManager.obtainById(ComposedToImpl.class,
						modified);
				if (rel != null) {
					rel.addComponent(removed);
					storeSafely(null, 1, rel);
				}
			} else if (deleted != null) {
				storeSafely(null, 1, deleted);
			}
			fireHierarchyModified();
		}
		return close(info);
	}

	public int getExecuteProfile() {
		return IProfiledOperation.HIERARCHY;
	}

	public int getUndoProfile() {
		return IProfiledOperation.HIERARCHY;
	}

	@Override
	public int getPriority() {
		return Job.SHORT;
	}

}
