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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.operations.internal.ImageMediaSupport;

@SuppressWarnings({ "restriction" })
public abstract class AbstractForeignCatHandler implements IForeignCatHandler {

	private final GregorianCalendar cal = new GregorianCalendar();
	protected Date previousImport;
	protected ImportState importState;
	private ImageMediaSupport imageMediaSupport;

	protected ResultSet performQuery(IProgressMonitor monitor,
			final ImportState importState, Statement statement, String query,
			final String fileName) throws SQLException {
		ResultSet rs1 = statement.executeQuery(query);
		monitor.worked(1);
		final IDbManager dbManager = importState.operation.getDbManager();
		cal.setTime(importState.meta.getLastImport());
		int year = cal.get(Calendar.YEAR);
		importState.meta.setLastImport(importState.importDate);
		cal.setTime(importState.importDate);
		if (year != cal.get(Calendar.YEAR))
			importState.meta.setLastYearSequenceNo(0);
		if (importState.operation.storeSafely(() -> {
			previousImport = dbManager.createLastImportCollection(
					importState.importDate,
					false,
					NLS.bind(
							Messages.AbstractForeignCatHandler_import_from_foreign,
							fileName));
			dbManager.store(importState.meta);
		}, 1))
			((ImportForeignCatOperation) importState.operation)
					.fireStructureModified();
		monitor.worked(1);
		return rs1;
	}

	protected Statement initStatement(IProgressMonitor monitor,
			Connection connection, String query, final ImportState importState)
			throws SQLException {
		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30); // set timeout to 30 sec.
		int n = 0;
		ResultSet rs = statement.executeQuery(query);
		while (rs.next())
			++n;
		importState.nFiles = n;
		((ImportForeignCatOperation) importState.operation).init(monitor,
				IMediaSupport.IMPORTWORKSTEPS * n + 3);
		return statement;
	}

	protected void initState(final ImportState importState, String[] detectors) {
		this.importState = importState;
		importState.recipeDetectorIds = detectors;
		importState.importedAssets = new HashSet<Asset>();
	}

	protected void handleOutOfMemoryError(String fileName, OutOfMemoryError e) {
		importState.operation.addError(
				NLS.bind(Messages.AbstractForeignCatHandler_not_enough_memory,
						fileName), e);
	}

	/**
	 * @return previousImport
	 */
	public Date getPreviousImport() {
		return previousImport;
	}

	protected IMediaSupport getMediaSupport(String format) {
		IMediaSupport mediaSupport = CoreActivator.getDefault()
				.getMediaSupport(format);
		if (mediaSupport != null)
			return mediaSupport;
		if (imageMediaSupport == null)
			imageMediaSupport = new ImageMediaSupport();
		return imageMediaSupport;
	}

}