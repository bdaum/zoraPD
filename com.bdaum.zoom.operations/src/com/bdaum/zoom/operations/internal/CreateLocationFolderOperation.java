package com.bdaum.zoom.operations.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class CreateLocationFolderOperation extends DbOperation {

	private final String locationOption;

	public CreateLocationFolderOperation(String locationOption) {
		super(
				Messages.getString("CreateLocationFolderOperation.update_location_collections")); //$NON-NLS-1$
		this.locationOption = locationOption;
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
		List<LocationImpl> set = locationOption == null
				|| Meta_type.locationFolders_no.equals(locationOption) ? new ArrayList<LocationImpl>()
				: dbManager.obtainObjects(LocationImpl.class);
		init(aMonitor, set.size() + 1);
		List<SmartCollectionImpl> sms = dbManager.obtainObjects(
				SmartCollectionImpl.class, false,
				"system", true, QueryField.EQUALS, //$NON-NLS-1$
				"album", false, QueryField.EQUALS); //$NON-NLS-1$
		List<IdentifiableObject> toBeDeleted = new ArrayList<IdentifiableObject>(
				sms.size());
		for (SmartCollectionImpl sm : sms)
			if (sm.getStringId().startsWith(IDbManager.LOCATIONKEY))
				toBeDeleted.add(sm);
		GroupImpl group = dbManager.obtainById(GroupImpl.class,
				Constants.GROUP_ID_LOCATIONS);
		if (group != null)
			toBeDeleted.add(group);
		if (!toBeDeleted.isEmpty())
			dbManager.safeTransaction(toBeDeleted, null);
		for (LocationImpl location : set) {
			dbManager.createLocationFolders(location, locationOption);
			dbManager.markSystemCollectionsForPurge(location);
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
