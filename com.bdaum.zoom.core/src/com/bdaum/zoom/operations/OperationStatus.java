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
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.operations;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.osgi.util.NLS;

public class OperationStatus extends MultiStatus {

	public OperationStatus(String pluginId, int code, String operationName, Throwable exception) {
		super(pluginId, code, operationName, exception);
	}

	@Override
	public String getMessage() {
		String operationName = super.getMessage();
		if (isOK())
			return NLS.bind(Messages.getString("OperationStatus.successful"), operationName); //$NON-NLS-1$
		if (getSeverity() == CANCEL)
			return NLS.bind(Messages.getString("OperationStatus.canceled"), operationName); //$NON-NLS-1$
		StringBuilder sb = new StringBuilder();
		sb.append(NLS.bind(Messages.getString("OperationStatus.with_errors"), operationName)); //$NON-NLS-1$
		StringBuilder eb = new StringBuilder();
		collect(this, eb, ERROR);
		if (eb.length() > 0)
			sb.append(Messages.getString("OperationStatus.errors")).append(eb); //$NON-NLS-1$
		collect(this, eb, WARNING);
		if (eb.length() > 0)
			sb.append(Messages.getString("OperationStatus.warnings")).append(eb); //$NON-NLS-1$
		return sb.toString();
	}

	private void collect(OperationStatus operationStatus, StringBuilder eb, int severity) {
		for (IStatus status : getChildren())
			if (status.getSeverity() == severity)
				eb.append("\n\t\t").append(status.getMessage()); //$NON-NLS-1$
	}

}
