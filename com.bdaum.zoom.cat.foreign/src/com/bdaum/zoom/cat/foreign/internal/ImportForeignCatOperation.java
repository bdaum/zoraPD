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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.cat.foreign.internal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.operations.AbstractImportOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class ImportForeignCatOperation extends AbstractImportOperation {

	private IForeignCatHandler handler;
	private final String fileName;
	private Date previousImport;

	public ImportForeignCatOperation(String fileName, IForeignCatHandler handler,
			ImportConfiguration importConfiguration) {
		super(NLS.bind(Messages.ImportForeignCatOperation_importing, handler.getName()));
		this.fileName = fileName;
		this.handler = handler;
		importState = new ImportState(importConfiguration, null, null, this, Constants.FILESOURCE_UNKNOWN);
		importConfiguration.rawOptions = Constants.RAWIMPORT_ONLYRAW;
	}

	@Override
	public void init(IProgressMonitor aMonitor, int work) {
		super.init(aMonitor, work);
	}

	@Override
	public void fireStructureModified() {
		super.fireStructureModified();
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT | IProfiledOperation.SYNCHRONIZE;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT | IProfiledOperation.INDEX;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		importState.info = info;
		try {
			// load the sqlite-JDBC driver using the current class loader
			try {
				Class.forName("org.sqlite.JDBC"); //$NON-NLS-1$
			} catch (ClassNotFoundException e1) {
				CatActivator.getDefault().logError(Messages.ImportForeignCatOperation_no_driver, e1);
			}
			String path = fileName.replaceAll("\\\\", "/"); //$NON-NLS-1$//$NON-NLS-2$
			// create a database connection
			try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + path)) { //$NON-NLS-1$
				handler.execute(monitor, fileName, connection, importState);
				previousImport = handler.getPreviousImport();
			} catch (SQLException e) {
				CatActivator.getDefault().logError(NLS.bind(Messages.ImportForeignCatOperation_no_connection, fileName),
						e);
			}
		} finally {
			if (storeSafely(null, 1, importState.meta))
				fireAssetsModified(null, null);
			for (Asset a : importState.allDeletedAssets)
				dbManager.markSystemCollectionsForPurge(a);
			importState.importFinished();
		}
		return close(info);
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
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		importState.info = info;
		List<Asset> assets = importState.obtainImportedAssets();
		initUndo(monitor, assets.size() + importState.allDeletedAssets.size() + 3);
		openIndexWriter();
		final List<Object> toBeStored = new ArrayList<Object>();
		final Set<Object> toBeDeleted = new HashSet<Object>();
		try {
			toBeStored.addAll(importState.allDeletedGhosts);
			monitor.worked(1);
			int i = 0;
			for (Asset asset : assets) {
				new AssetEnsemble(dbManager, asset, importState).delete(toBeDeleted, toBeStored);
				storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
				if (!importState.allDeletedAssets.contains(asset))
					deleteIndexEntry(asset.getStringId());
				dbManager.markSystemCollectionsForPurge(asset);
				if (i == 0)
					fireAssetsModified(null, null);
				if (i++ == 16)
					i = 0;
			}
			storeSafely(null, importState.allDeletedAssets.size(), importState.allDeletedAssets.toArray());
			if (!importState.allDeletedAssets.isEmpty() && !dbManager.getMeta(true).getNoIndex()) {
				Job job = Core.getCore().getDbFactory().getLireService(true).createIndexingJob(importState.allDeletedAssets,
						true, -1, 0, CoreActivator.getDefault().isNoProgress());
				if (job != null)
					job.schedule();
			}
		} finally {
			closeIndex();
			CoreActivator.getDefault().getFileWatchManager().stopIgnoring(opId);
			Utilities.popLastImport(toBeStored, toBeDeleted, previousImport, true);
			storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
			fireAssetsModified(null, null);
			fireStructureModified();
		}
		return close(info);
	}

}
