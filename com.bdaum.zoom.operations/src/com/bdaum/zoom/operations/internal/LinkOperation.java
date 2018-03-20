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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class LinkOperation extends DbOperation {

	private int type;
	private String parent;
	private String[] ids;
	private List<String> added = new ArrayList<String>();
	private List<String> modified = new ArrayList<String>();
	private List<String> inserted = new ArrayList<String>();

	public LinkOperation(int type, String parent, String[] ids) {
		super(Messages.getString("LinkOperation.Link")); //$NON-NLS-1$
		this.type = type;
		this.parent = parent;
		this.ids = ids;
	}

	@Override
	@SuppressWarnings("fallthrough")
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		ComposedToImpl comp = null;
		init(aMonitor, ids.length);
		if (!isPeerOwned(parent)) {
			for (String id : ids) {
				if (!isPeerOwned(id)) {
					String original = id;
					String derivative = parent;
					switch (type) {
					case Constants.DERIVATIVES:
						original = parent;
						derivative = id;
					case Constants.ORIGINALS:
						AssetImpl asset = dbManager.obtainAsset(derivative);
						if (asset != null) {
							DerivedByImpl rel = new DerivedByImpl(
									"", null, asset //$NON-NLS-1$
											.getSoftware(), new Date(),
									derivative, original);
							if (storeSafely(null, 1, rel))
								added.add(rel.getStringId());
						}
						break;
					case Constants.COMPOSITES:
						original = parent;
						derivative = id;
						comp = null;
						/* FALL-THROUGH */
					case Constants.COMPONENTS:
						if (comp == null) {
							Iterator<ComposedToImpl> it = dbManager.obtainObjects(
									ComposedToImpl.class,
									"composite", derivative, //$NON-NLS-1$
									QueryField.EQUALS).iterator();
							if (it.hasNext()) {
								comp = it.next();
								modified.add(comp.getStringId());
							} else {
								comp = new ComposedToImpl(
										"", null, "", "", new Date(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
										derivative);
								added.add(comp.getStringId());
							}
						}
						comp.addComponent(original);
						inserted.add(original);
						storeSafely(null, 1, comp);
						break;
					}
				}
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
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		initUndo(aMonitor, modified.size() + added.size());
		List<Object> toBeStored = new ArrayList<Object>();
		List<Object> toBeDeleted = new ArrayList<Object>();
		for (String id : modified) {
			ComposedToImpl obj = dbManager.obtainById(ComposedToImpl.class, id);
			if (obj != null) {
				obj.getComponent().removeAll(inserted);
				toBeStored.add(obj);
			}
		}
		for (String id : added) {
			IdentifiableObject obj = dbManager.obtainById(
					IdentifiableObject.class, id);
			if (obj != null)
				toBeDeleted.add(obj);
		}
		if (storeSafely(toBeDeleted.toArray(), modified.size() + added.size(),
				toBeStored.toArray()))
			fireHierarchyModified();
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
