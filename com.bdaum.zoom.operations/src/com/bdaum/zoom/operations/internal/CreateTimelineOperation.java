package com.bdaum.zoom.operations.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class CreateTimelineOperation extends DbOperation {

	private static final long ONEDAY = 24 * 60 * 60 * 1000L;
	private final String timeline;

	public CreateTimelineOperation(String timeline) {
		super(Messages.getString("CreateTimeLineOperation.update_timeline")); //$NON-NLS-1$
		this.timeline = timeline;
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CAT;
	}

	public int getUndoProfile() {
		return 0;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		List<AssetImpl> set = timeline == null
				|| Meta_type.timeline_no.equals(timeline) ? new ArrayList<AssetImpl>()
				: dbManager.obtainAssets();
		init(aMonitor, set.size() + 1);
		List<SmartCollectionImpl> sms = dbManager.obtainObjects(
				SmartCollectionImpl.class, false, "system", true, QueryField.EQUALS, //$NON-NLS-1$
				"album", false, QueryField.EQUALS); //$NON-NLS-1$
		List<IdentifiableObject> toBeDeleted = new ArrayList<IdentifiableObject>(
				sms.size());
		for (SmartCollectionImpl sm : sms)
			if (sm.getStringId().startsWith(IDbManager.DATETIMEKEY))
				toBeDeleted.add(sm);
		GroupImpl group = dbManager.obtainById(GroupImpl.class,
				Constants.GROUP_ID_TIMELINE);
		if (group != null)
			toBeDeleted.add(group);
		if (!toBeDeleted.isEmpty())
			dbManager.safeTransaction(toBeDeleted, null);
		boolean[] days = new boolean[50000];
		for (AssetImpl asset : set) {
			Date dateCreated = asset.getDateCreated();
			if (dateCreated == null)
				dateCreated = asset.getDateTimeOriginal();
			if (dateCreated == null) {
				aMonitor.worked(1);
				continue;
			}
			int day = (int) (dateCreated.getTime() / ONEDAY);
			if (day >= 0 && day < days.length) {
				if (days[day]) {
					aMonitor.worked(1);
					continue;
				}
				days[day] = true;
			}
			dbManager.createTimeLine(asset, timeline);
			if (updateMonitor(1))
				break;
		}
		fireStructureModified();
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

}
