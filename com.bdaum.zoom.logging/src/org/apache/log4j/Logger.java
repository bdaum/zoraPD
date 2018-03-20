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
 * (c) 2015 Berthold Daum  
 */
package org.apache.log4j;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.logging.LoggingActivator;

public class Logger {

	private String name;
	private boolean debugging;
	private LoggingActivator activator;

	public static Logger getLogger(Class<?> clazz) {
		return new Logger(clazz);
	}

	public Logger(Class<?> clazz) {
		name = clazz.getName();
		activator = LoggingActivator.getDefault();
		debugging = activator.isDebugging();
	}

	public void warn(String msg) {
		activator.log(IStatus.WARNING, formatMessage(msg), null);
	}

	private String formatMessage(String msg) {
		return NLS.bind("{0}: {1}", name, msg); //$NON-NLS-1$
	}

	public void error(String msg, Throwable e) {
		activator.log(IStatus.ERROR, formatMessage(msg), e);
	}

	public void trace(String msg) {
		if (debugging)
			System.out.println(formatMessage(msg));
	}

	public void debug(String msg) {
		if (debugging)
			System.out.println(formatMessage(msg));
	}

	public boolean isDebugEnabled() {
		return debugging;
	}

	public boolean isInfoEnabled() {
		return true;
	}

	public void info(String msg) {
		activator.log(IStatus.INFO, formatMessage(msg), null);
	}

}
