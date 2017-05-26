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
 * (c) 2013 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.cat.foreign.bibble.internal;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.foreign.internal.AbstractForeignCatHandler;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.ImportState;

@SuppressWarnings("restriction")
public class BibbleCatHandler extends AbstractForeignCatHandler {
	private static final String QUERY = "select * from MasterFile INNER JOIN Version ON MasterFile.id=Version.masterID"; //$NON-NLS-1$
	private static final String[] DETECTORS = new String[] { "com.bdaum.zoom.recipes.bibble" }; //$NON-NLS-1$

	public void execute(IProgressMonitor monitor, String folderName,
			Connection connection, ImportState importState) {
		initState(importState, DETECTORS);
		try {
			Statement statement = initStatement(monitor, connection, QUERY,
					importState);
			ResultSet rs = performQuery(monitor, importState, statement, QUERY,
					folderName);
			Statement pathStatement = connection.createStatement();
			while (rs.next()) {
				monitor.subTask(NLS.bind(Messages.BibbleCatHandler_n_of_m,
						importState.importNo, importState.nFiles));
				try {
					importState.nextPicture(importFile(monitor, rs,
							importState, pathStatement));
					if (monitor.isCanceled())
						break;
				} catch (OutOfMemoryError e) {
					handleOutOfMemoryError(folderName, e);
				}
			}
		} catch (SQLException e) {
			importState.operation.addError(NLS.bind(
					Messages.BibbleCatHandler_wrong_cat_format, folderName), e);
		}
	}

	private int importFile(IProgressMonitor monitor, ResultSet rs,
			ImportState importState, Statement pathStatement)
			throws SQLException, OutOfMemoryError {
		String filename = rs.getString("filename"); //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		int pathId = rs.getInt("pathID"); //$NON-NLS-1$
		while (pathId != 0) {
			ResultSet rsp = pathStatement.executeQuery(NLS.bind(
					"select * from Paths where id={0}", pathId)); //$NON-NLS-1$
			if (rsp.next()) {
				String node = rsp.getString("nodeName"); //$NON-NLS-1$
				pathId = rsp.getInt("parentID"); //$NON-NLS-1$
				sb.insert(0, node).insert(node.length(), '/');
			} else
				break;
		}
		sb.append(filename);
		String path = sb.toString();
		File file = new File(path);
		if (file.exists()) {
			URI uri = file.toURI();
			String extension = Core.getFileExtension(uri.toString());
			IMediaSupport mediaSupport = getMediaSupport(extension
					.toUpperCase());
			try {
				// read the result set
				importState.overlayMap.put(QueryField.RATING.getExifToolKey(),
						String.valueOf(rs.getInt("rating"))); //$NON-NLS-1$
				int label = rs.getInt("label"); //$NON-NLS-1$
				int code = -1;
				switch (label) {
				case 1:
					code = Constants.COLOR_RED;
					break;
				case 2:
					code = Constants.COLOR_YELLOW;
					break;
				case 34:
					code = Constants.COLOR_GREEN;
					break;
				case 4:
					code = Constants.COLOR_BLUE;
					break;
				case 5:
					code = Constants.COLOR_VIOLET;
					break;
				}
				if (code >= 0)
					importState.overlayMap.put(
							QueryField.COLORCODE.getExifToolKey(),
							String.valueOf(code));
				return mediaSupport.importFile(file, extension, importState,
						monitor, null);
			} catch (Exception e) {
				importState.operation.addError(
						NLS.bind(Messages.BibbleCatHandler_internal_error,
								file.getPath()), e, uri, monitor);
			}

		}
		return 0;
	}

	public String getName() {
		return Messages.BibbleCatHandler_name;
	}

}
