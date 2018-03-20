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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.operations;

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.logging.InvalidFileFormatException;
import com.bdaum.zoom.program.DiskFullException;

public abstract class AbstractImportOperation extends DbOperation {

	protected ImportState importState;

	public AbstractImportOperation(String label) {
		super(label);
	}

	/**
	 * Adds an error status to the operations multi status
	 *
	 * @param message
	 *            - error message
	 * @param t
	 *            - cause
	 * @param uri
	 *            - file causing the error
	 * @param monitor
	 *            - progress monitor for control flow
	 */
	public void addError(String message, Throwable t, URI uri,
			IProgressMonitor monitor) {
		if (t instanceof DiskFullException) {
			addError(Messages.getString("AbstractImportOperation.disk_full"), t); //$NON-NLS-1$
			monitor.setCanceled(true);
			return;
		}
		if (t instanceof OutOfMemoryError) {
			addError(NLS.bind(Messages
					.getString("AbstractImportOperation.not_enough_memory"), //$NON-NLS-1$
					uri), t);
			return;
		}
		if (t instanceof InvalidFileFormatException) {
			Core.getCore()
					.getErrorHandler()
					.invalidFile(
							Messages.getString("AbstractImportOperation.invalid_file_format"), t.getMessage(), uri, monitor, //$NON-NLS-1$
							importState.info);
			return;
		}
		addError(message, t);
	}

}
