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
package com.bdaum.zoom.cat.foreign.captureOne.internal;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.foreign.internal.AbstractForeignCatHandler;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.ImportState;

@SuppressWarnings("restriction")
public class C1CatHandler extends AbstractForeignCatHandler {
	private static final String QUERY = "select * from ZIMAGE INNER JOIN ZMETADATA ON ZIMAGE.ZMETADATA=ZMETADATA.Z_PK"; //$NON-NLS-1$
	private static final String[] DETECTORS = new String[] { "com.bdaum.zoom.recipes.c1" }; //$NON-NLS-1$

	public void execute(IProgressMonitor monitor, String fileName,
			Connection connection, final ImportState importState) {

		initState(importState, DETECTORS);
		try {
			Statement statement = initStatement(monitor, connection, QUERY,
					importState);
			ResultSet rs = performQuery(monitor, importState, statement, QUERY,
					fileName);
			while (rs.next()) {
				monitor.subTask(NLS.bind(Messages.C1CatHandler_n_of_m,
						importState.importNo, importState.nFiles));
				try {
					importState
							.nextPicture(importFile(monitor, rs, importState));
					if (monitor.isCanceled())
						break;
				} catch (OutOfMemoryError e) {
					handleOutOfMemoryError(fileName, e);
				}
			}
		} catch (SQLException e) {
			importState.operation.addError(
					NLS.bind(Messages.C1CatHandler_wrong_format, fileName), e);
		}
	}

	private int importFile(IProgressMonitor monitor, ResultSet rs,
			ImportState importState) throws SQLException, OutOfMemoryError {
		String path = rs.getString("ZPATH"); //$NON-NLS-1$
		path = path.replace("\\|", "").replace('\\', '/'); //$NON-NLS-1$ //$NON-NLS-2$
		File file = new File(path);
		if (file.exists()) {
			URI uri = file.toURI();
			String extension = Core.getFileExtension(uri.toString());
			IMediaSupport mediaSupport = getMediaSupport(extension
					.toUpperCase());
			try {
				// read the result set
				String copyright = rs.getString("ZBASIC_COPYRIGHT"); //$NON-NLS-1$
				if (copyright != null && !copyright.isEmpty()) {
					importState.overlayMap.put(
							QueryField.IPTC_OWNER.getExifToolKey(), copyright);
					importState.overlayMap.put(
							QueryField.EXIF_COPYRIGHT.getExifToolKey(),
							copyright);
				}
				try {
					QueryField qfield = QueryField.RATING;
					importState.overlayMap.put(
							qfield.getExifToolKey(),
							String.valueOf(qfield.parseInt(
									rs.getString("ZBASIC_RATING"), -1))); //$NON-NLS-1$
				} catch (Exception e) {
					// do nothing
				}
				String caption = rs.getString("ZBASIC_CAPTION"); //$NON-NLS-1$
				if (caption != null && !caption.isEmpty())
					importState.overlayMap.put(
							QueryField.IPTC_HEADLINE.getExifToolKey(), caption);
				return mediaSupport.importFile(file, extension, importState,
						monitor, null);
			} catch (Exception e) {
				importState.operation.addError(
						NLS.bind(Messages.C1CatHandler_internal_error,
								file.getPath()), e, uri, monitor);
			}
		}
		return 0;
	}

	public String getName() {
		return Messages.C1CatHandler_name;
	}

}
