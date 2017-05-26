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

package com.bdaum.zoom.batch.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * Utility class for grabbing process outputs.
 */
public class StreamCapture extends Thread {
	private final InputStream inputStream;
	private final StringBuffer data = new StringBuffer();
	private final String statusPrefix;
	private int severity;
	private String kind;
	private String charsetName;
	private boolean abort = false;

	public StreamCapture(InputStream inputStream, String charsetName, String label, String kind, int severity) {
		super(label + "(" + kind + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		this.inputStream = inputStream;
		this.statusPrefix = label + ": "; //$NON-NLS-1$
		this.severity = severity;
		this.kind = kind;
		this.charsetName = charsetName;
	}

	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader((charsetName == null) ? new InputStreamReader(inputStream)
				: new InputStreamReader(inputStream, charsetName))) {
			String line;
			while (!abort && (line = reader.readLine()) != null) {
				data.append(line).append('\n');
				if (severity != IStatus.OK) {
					final String message = statusPrefix + line;
					BatchActivator.getDefault().getLog().log(new Status(severity, BatchActivator.PLUGIN_ID, message));
				}
			}
		} catch (UnsupportedEncodingException e) {
			BatchActivator.getDefault().logError(NLS.bind(Messages.StreamCapture_unsupported_code_page, charsetName), e);
		} catch (final IOException e) {
			BatchActivator.getDefault().logError(NLS.bind(Messages.BatchActivator_error_when_reading, kind), e);
		}
	}

	public String getData() {
		return data.toString();
	}

	public void abort() {
		abort = true;
	}
}