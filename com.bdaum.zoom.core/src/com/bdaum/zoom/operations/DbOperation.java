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

package com.bdaum.zoom.operations;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;

import com.bdaum.aoModeling.runtime.AomList;
import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.operations.UpdateTextIndexOperation;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.core.trash.StructBackup;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.program.BatchUtilities;

public abstract class DbOperation extends AbstractOperation implements IProfiledOperation {

	protected String opId = java.util.UUID.randomUUID().toString() + '/';
	protected Date date = new Date();
	protected OperationStatus status;
	protected DocumentBuilder builder;
	protected Object iw;
	protected File indexPath;
	protected IDbManager dbManager;
	protected IProgressMonitor monitor;
	protected IPeerService peerService;
	private OperationJob operationJob;

	/**
	 * Constructor
	 *
	 * @param label
	 *            - Label appearing in Undo messages
	 */
	public DbOperation(String label) {
		super(label);
		ICore core = Core.getCore();
		dbManager = core.getDbManager();
		peerService = core.getPeerService();
	}

	/**
	 * Initializes the operation Subclasses should call this method at the
	 * beginning of the execute() and redo() methods
	 *
	 * @param aMonitor
	 *            - progress monitor
	 * @param work
	 *            - amount of work to initialize the progress monitor with
	 */
	protected void init(IProgressMonitor aMonitor, int work) {
		status = new OperationStatus(CoreActivator.PLUGIN_ID, 0, getLabel(), null);
		this.monitor = aMonitor;
		aMonitor.beginTask(getLabel(), work);
	}

	/**
	 * Initializes the undo Subclasses should call this method at the beginning
	 * of the undo() method
	 *
	 * @param aMonitor
	 *            - progress monitor
	 * @param work
	 *            - amount of work to initialize the progress monitor with
	 */
	protected void initUndo(IProgressMonitor aMonitor, int work) {
		status = new OperationStatus(CoreActivator.PLUGIN_ID, 0,
				NLS.bind(Messages.getString("DbOperation.Undo"), getLabel()), null); //$NON-NLS-1$
		this.monitor = aMonitor;
		aMonitor.beginTask(NLS.bind(Messages.getString("DbOperation.Undo"), getLabel()), work); //$NON-NLS-1$
	}

	/**
	 * Updates the monitor and check for cancellation If the operation is
	 * cancelled the database is rolled back, too
	 *
	 * @param work
	 *            - incremental amount of work done
	 * @return true if cancelled
	 */
	protected boolean updateMonitor(int work) {
		if (monitor != null) {
			if (monitor.isCanceled()) {
				dbManager.rollback();
				status.add(new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID,
						Messages.getString("DbOperation.Operation_cancelled"))); //$NON-NLS-1$
				return true;
			}
			monitor.worked(work);
		}
		return false;
	}

	/**
	 * Stores and deletes objects and commits
	 *
	 * @param toBeDeleted
	 *            - objects to be deleted, maybe null
	 * @param work
	 *            - incremental amount of work done
	 * @param toBeStored
	 *            - objects to be stored, maybe null
	 * @return true in the case of success
	 */
	protected boolean storeSafely(Object[] toBeDeleted, int work, Object... toBeStored) {
		if (dbManager.beginSafeTransaction())
			try {
				if (toBeDeleted != null)
					for (Object del : toBeDeleted)
						dbManager.delete(del);
				if (toBeStored != null)
					for (Object obj : toBeStored)
						dbManager.store(obj);
				if (updateMonitor(work))
					return false;
				dbManager.commit();
				return true;
			} catch (RuntimeException e) {
				dbManager.rollback();
				throw e;
			} finally {
				dbManager.endSafeTransaction();
				BatchUtilities.yield();
			}
		status.add(new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID,
				Messages.getString("DbOperation.operation_timed_out"))); //$NON-NLS-1$
		return false;
	}

	/**
	 * Runs the runnable and commits
	 *
	 * @param runnable
	 *            - a runnable performing database operations
	 * @param work
	 *            - incremental amount of work done
	 * @return true in the case of success
	 */
	public boolean storeSafely(Runnable runnable, int work) {
		if (dbManager.beginSafeTransaction())
			try {
				runnable.run();
				if (updateMonitor(work)) {
					dbManager.rollbackTrash();
					return false;
				}
				dbManager.commit();
				dbManager.commitTrash();
				return true;
			} catch (RuntimeException e) {
				dbManager.rollback();
				throw e;
			} finally {
				dbManager.endSafeTransaction();
			}
		status.add(new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID,
				Messages.getString("DbOperation.operation_timed_out"))); //$NON-NLS-1$
		return false;
	}

	/**
	 * Adds an error status to the operations multi status
	 *
	 * @param message
	 *            - error message
	 * @param t
	 *            - cause
	 */
	public void addError(String message, Throwable t) {
		if (status != null)
			status.add(new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, message, t));
		else
			CoreActivator.getDefault().logError(message, t);
	}

	/**
	 * Adds an warning status to the operations multi status
	 *
	 * @param message
	 *            - warning message
	 * @param t
	 *            - cause
	 */
	public void addWarning(String message, Throwable t) {
		if (status != null)
			status.add(new Status(IStatus.WARNING, CoreActivator.PLUGIN_ID, message, t));
		else
			CoreActivator.getDefault().logWarning(message, t);
	}

	/**
	 * Adds an info status to the operations multi status
	 *
	 * @param message
	 *            - info message
	 */
	public void addInfo(String message) {
//		if (status != null)
//			status.add(new Status(IStatus.INFO, CoreActivator.PLUGIN_ID, message, null));
//		else
			CoreActivator.getDefault().logInfo(message);
	}

	/**
	 * Finalizes the operation. Should be called at the end of execute(),
	 * undo(), and redo()
	 *
	 * @param info
	 *            - adaptable answering at least to Shell.class
	 * @return - operation status
	 */
	protected IStatus close(IAdaptable info) {
		if (!isSilent())
			CoreActivator.getDefault().getLog().log(status);
		if (monitor != null)
			monitor.done();
		return status;
	}

	/**
	 * Finalizes the operation. Should be called at the end of execute(),
	 * undo(), and redo()
	 *
	 * @param info
	 *            - adaptable answering at least to Shell.class
	 * @param assetsToIndex
	 *            - IDs of assets to be indexed for content based search and
	 *            text search, may be null.
	 * @return - operation status
	 */
	protected IStatus close(IAdaptable info, String[] assetsToIndex) {
		if (assetsToIndex != null && !dbManager.getMeta(true).getNoIndex())
			OperationJob.executeSlaveOperation(new UpdateTextIndexOperation(assetsToIndex), info);
		return close(info);
	}

	/**
	 * Finalizes the operation. Should be called at the end of execute(),
	 * undo(), and redo()
	 *
	 * @param info
	 *            - adaptable answering at least to Shell.class
	 * @param assetsToIndex
	 *            - assets to be indexed for content based search and text
	 *            search, may be null.
	 * @return - operation status
	 */
	protected IStatus close(IAdaptable info, Collection<? extends Asset> assetsToIndex) {
		if (assetsToIndex != null && !dbManager.getMeta(true).getNoIndex())
			OperationJob.executeSlaveOperation(new UpdateTextIndexOperation(assetsToIndex), info);
		return close(info);
	}

	/**
	 * Aborts the operation. Should be called when an operation was cancelled
	 * via the monitor
	 *
	 * @return - operation status
	 */
	protected IStatus abort() {
		status.add(Status.CANCEL_STATUS);
		if (!isSilent())
			CoreActivator.getDefault().getLog().log(status);
		if (monitor != null)
			monitor.done();
		return status;
	}

	/**
	 * Notifies about catalog structure modifications (e.g. collections added or
	 * removed)
	 */
	protected void fireStructureModified() {
		Core.getCore().fireStructureModified();
	}

	/**
	 * Notifies about asset modifications
	 *
	 * @param changes
	 *            - modified, deleted or added assets or null for unspecified
	 *            changes
	 * @param node
	 *            - affected node or null for multi-node or thumbnail changes
	 */
	protected void fireAssetsModified(final BagChange<Asset> changes, final QueryField node) {
		Core.getCore().fireAssetsModified(changes, node);
	}

	/**
	 * Notifies about asset modifications
	 *
	 * @param assets
	 *            - modified assets or null for unspecified changes
	 * @param node
	 *            - affected node or null for multi-node or thumbnail changes
	 */
	protected void fireApplyRules(final Collection<? extends Asset> assets, final QueryField node) {
		Core.getCore().fireApplyRules(assets, node);
	}

	/**
	 * Notifies about catalog selections
	 *
	 * @param sel
	 *            - selection
	 * @param forceUpdate
	 *            - true if selection should inform other views
	 */
	protected void fireCatalogSelection(IStructuredSelection sel, boolean forceUpdate) {
		Core.getCore().fireCatalogSelection(sel, forceUpdate);
	}

	/**
	 * Notifies about modifications in relationships between assets
	 */
	protected void fireHierarchyModified() {
		Core.getCore().fireHierarchyModified();
	}

	/**
	 * Opens the Lucene index for modification Make sure to call closeIndex() in
	 * a finally clause
	 */
	protected void openIndexWriter() {
		indexPath = dbManager.getIndexPath();
		if (indexPath != null)
			try {
				iw = Core.getCore().getDbFactory().getLuceneService().openIndexWriter(indexPath);
			} catch (IOException e) {
				addError(Messages.getString("DbOperation.ioerror_creating_lucene_index"), //$NON-NLS-1$
						e);
			}
	}

	/**
	 * Closes the Lucene index
	 */
	protected void closeIndex() {
		if (iw != null) {
			try {
				CoreActivator.getDefault().getDbFactory().getLuceneService().closeIndexWriter(iw, indexPath);
			} catch (IOException e) {
				addError(Messages.getString("DbOperation.ioerror_closing_lucene_index"), e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Removes a document from the Lucene index
	 *
	 * @param assetId
	 *            - ID of the owning asset
	 */
	protected void deleteIndexEntry(String assetId) {
		if (iw != null && builder != null) {
			try {
				Core.getCore().getDbFactory().getLuceneService().deleteIndexEntry(iw, assetId);
			} catch (IOException e) {
				addError(Messages.getString("DbOperation.ioerror_removing_images_from_lucene"), //$NON-NLS-1$
						e);
			}
		}
	}

	/**
	 * Return true if the asset originated from a peer node
	 *
	 * @param assetId
	 *            - asset to check
	 * @return true if the asset originated from a peer node
	 */
	protected boolean isPeerOwned(String assetId) {
		return peerService != null && peerService.isOwnedByPeer(assetId);
	}

	/**
	 * @param asset
	 *            - asset added, deleted, or modified
	 * @param folders
	 *            - true if folder hierarchies are wanted
	 * @param timeline
	 *            - type of timeline wanted (Meta.timeline_xxx). Maybe null
	 * @param locations
	 *            - type of location folders wanted (Meta.locationFolders_xxx).
	 *            Maybe null
	 * @param cleanup
	 *            - true if empty system collections should be purged
	 * @return
	 */
	public boolean updateFolderHierarchies(Asset asset, boolean folders, String timeline, String locations,
			boolean cleanup) {
		boolean changed = false;
		if (asset != null) {
			if (folders)
				changed = dbManager.createFolderHierarchy(asset);
			if (timeline != null)
				changed |= dbManager.createTimeLine(asset, timeline);
			if (locations != null)
				changed |= dbManager.createLocationFolders(asset, locations);
			if (cleanup)
				dbManager.markSystemCollectionsForPurge(asset);
		}
		return changed;
	}

	protected void collectAssetsToUpdate(IProgressMonitor aMonitor, int work, IIdentifiableObject element,
			List<String> assetsToUpdate, StructBackup backup) {
		String id = element.getStringId();
		if (element instanceof LocationImpl) {
			List<LocationCreatedImpl> locs = dbManager.obtainObjects(LocationCreatedImpl.class, "location", id, //$NON-NLS-1$
					QueryField.EQUALS);
			List<LocationShownImpl> locs2 = dbManager.obtainObjects(LocationShownImpl.class, "location", //$NON-NLS-1$
					id, QueryField.EQUALS);
			int cnt = locs.size() + locs2.size();
			if (cnt > 0) {
				int incr = work / cnt;
				for (LocationCreatedImpl obj : locs) {
					AomList<String> assets = obj.getAsset();
					assetsToUpdate.addAll(assets);
					if (backup != null)
						backup.add(obj);
					aMonitor.worked(incr);
					if (aMonitor.isCanceled()) {
						assetsToUpdate.clear();
						return;
					}
				}
				for (LocationShownImpl obj : locs2) {
					String asset = obj.getAsset();
					assetsToUpdate.add(asset);
					if (backup != null)
						backup.add(obj);
					aMonitor.worked(incr);
					if (aMonitor.isCanceled()) {
						assetsToUpdate.clear();
						return;
					}
				}
			} else
				aMonitor.worked(work);
		} else if (element instanceof ContactImpl) {
			List<CreatorsContactImpl> contacts = dbManager.obtainObjects(CreatorsContactImpl.class, "contact", //$NON-NLS-1$
					id, QueryField.EQUALS);
			int cnt = contacts.size();
			if (cnt > 0) {
				int incr = work / cnt;
				for (CreatorsContactImpl obj : contacts) {
					AomList<String> assets = obj.getAsset();
					assetsToUpdate.addAll(assets);
					if (backup != null)
						backup.add(obj);
					aMonitor.worked(incr);
					if (aMonitor.isCanceled()) {
						assetsToUpdate.clear();
						return;
					}
				}
			} else
				aMonitor.worked(work);
		} else if (element instanceof ArtworkOrObjectImpl) {
			List<ArtworkOrObjectShownImpl> objects = dbManager.obtainObjects(ArtworkOrObjectShownImpl.class,
					"artworkOrObject", id, QueryField.EQUALS); //$NON-NLS-1$
			int cnt = objects.size();
			if (cnt > 0) {
				int incr = work / cnt;
				for (ArtworkOrObjectShownImpl obj : objects) {
					String asset = obj.getAsset();
					assetsToUpdate.add(asset);
					if (backup != null)
						backup.add(obj);
					aMonitor.worked(incr);
					if (aMonitor.isCanceled()) {
						assetsToUpdate.clear();
						return;
					}
				}
			} else
				aMonitor.worked(work);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.operations.IProfiledOperation#isSilent()
	 */

	public boolean isSilent() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.operations.IProfiledOperation#getPriority()
	 */

	public int getPriority() {
		return Job.LONG;
	}

	/**
	 * Returns the unique ID of this operation
	 *
	 * @return - operation ID
	 */
	public String getOpId() {
		return opId;
	}

	/**
	 * @return dbManager
	 */
	public IDbManager getDbManager() {
		return dbManager;
	}

	protected void yieldRule() {
		if (operationJob != null)
			operationJob.yieldRule(monitor);
	}

	/**
	 * Sets the job that executes this operation
	 *
	 * @param operationJob
	 *            - the operation job
	 */
	public void setJob(OperationJob operationJob) {
		this.operationJob = operationJob;
	}

}
