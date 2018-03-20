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
package com.bdaum.zoom.db.internal.job;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.job.ProfiledSchedulingRule;
import com.bdaum.zoom.operations.IProfiledOperation;

public class CleanupJob extends CustomJob {

	public CleanupJob() {
		super(Messages.getString("CleanupJob.cleanup_system_collections")); //$NON-NLS-1$
		setPriority(Job.DECORATE);
		setRule(new ProfiledSchedulingRule(this.getClass(),
				IProfiledOperation.CAT));
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.DAEMONS == family;
	}

	@Override
	protected IStatus runJob(IProgressMonitor monitor) {
		boolean pruned = false;
		ICore core = Core.getCore();
		LinkedList<SmartCollectionImpl> tobeChecked = new LinkedList<SmartCollectionImpl>();
		final IDbManager dbManager = core.getDbManager();
		for (SmartCollectionImpl coll : dbManager
				.obtainObjects(SmartCollectionImpl.class)) {
			SmartCollection root = getRootCollection(coll);
			String grp = root.getGroup_rootCollection_parent();
			if (Constants.GROUP_ID_IMPORTS.equals(grp) && !root.getSystem()
					|| Constants.GROUP_ID_TIMELINE.equals(grp)
					&& root.getSystem()
					|| Constants.GROUP_ID_FOLDERSTRUCTURE.equals(grp)
					&& root.getSystem())
				tobeChecked.add(coll);
		}
		Collections.sort(tobeChecked, new Comparator<SmartCollectionImpl>() {
			public int compare(SmartCollectionImpl o1, SmartCollectionImpl o2) {
				int l1 = o1.getStringId().length();
				int l2 = o2.getStringId().length();
				return (l1 == l2 ? 0 : l1 < l2 ? 1 : -1);
			}
		});
		monitor.beginTask(
				Messages.getString("CleanupJob.cleaning_up"), tobeChecked.size()); //$NON-NLS-1$
		while (!tobeChecked.isEmpty()) {
			SmartCollectionImpl sm = tobeChecked.poll();
			String id = sm.getStringId();
			boolean removed = dbManager.pruneSystemCollection(sm);
			pruned |= removed;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// ignore
			}
			if (monitor.isCanceled()) {
				monitor.done();
				return Status.OK_STATUS;
			}
			monitor.worked(1);
			if (!removed) {
				Iterator<SmartCollectionImpl> it = tobeChecked.iterator();
				while (it.hasNext()) {
					String next = it.next().getStringId();
					if (id.startsWith(next)) {
						it.remove();
						monitor.worked(1);
					}
				}
			}
		}
		Meta meta = dbManager.getMeta(true);
		meta.setCleaned(true);
		dbManager.safeTransaction(null, meta);
		if (pruned)
			core.fireStructureModified();
		monitor.done();
		return Status.OK_STATUS;
	}

	private static SmartCollection getRootCollection(SmartCollectionImpl coll) {
		SmartCollection parent = coll;
		while (parent.getSmartCollection_subSelection_parent() != null)
			parent = parent.getSmartCollection_subSelection_parent();
		return parent;
	}

}
