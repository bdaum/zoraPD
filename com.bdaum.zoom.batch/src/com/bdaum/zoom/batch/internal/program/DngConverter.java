/*******************************************************************************
 * Copyright (c) 2009 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.batch.internal.program;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bdaum.zoom.batch.internal.Options;
import com.bdaum.zoom.image.IFileHandler;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.program.IConverter;

public class DngConverter implements IConverter {

	private String location;
	private File file;

	public void setConverterLocation(String location) {
		this.location = location;
	}

	public File setInput(File in, Options options) {
		String name = in.getAbsolutePath();
		IFileHandler fileHandler = ImageConstants.findFileHandler(in);
		if (fileHandler != null) {
			file = fileHandler.getImageFile(in);
			if (file == null)
				return null;
		} else
			file = in;
		name = file.getAbsolutePath();
		int p = name.lastIndexOf('.');
		if (p >= 0)
			name = name.substring(0, p);
		File f = new File(name + ".dng"); //$NON-NLS-1$
		String folder = (String) options.get("outputFolder"); //$NON-NLS-1$
		return (folder != null) ? new File(folder, f.getName()) : f;
	}

	public String[] getParms(Options options) {
		List<String> parms = new ArrayList<String>();
		parms.add(location);
		if (getOption(options, "uncompressed")) //$NON-NLS-1$
			parms.add("-u"); //$NON-NLS-1$
		else
			parms.add("-c"); //$NON-NLS-1$
		if (getOption(options, "highres")) //$NON-NLS-1$
			parms.add("-p2"); //$NON-NLS-1$
		else
			parms.add("-p1"); //$NON-NLS-1$
		if (getOption(options, "linear")) //$NON-NLS-1$
			parms.add("-l"); //$NON-NLS-1$
		if (getOption(options, "embedded")) //$NON-NLS-1$
			parms.add("-e"); //$NON-NLS-1$
		String folder = (String) options.get("outputFolder"); //$NON-NLS-1$
		if (folder != null) {
			parms.add("-d"); //$NON-NLS-1$
			parms.add(folder);
		}
		parms.add(file.getAbsolutePath());
		return parms.toArray(new String[parms.size()]);
	}

	private static boolean getOption(Options options, String option) {
		if (options == null)
			return false;
		return options.getBoolean(option);
	}

	public File getInputDirectory() {
		return file == null ? null : file.getParentFile();
	}

}
