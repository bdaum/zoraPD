/*******************************************************************************
 * Copyright (c) 2009-2010 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.image.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.image.IFileHandler;

public class EipFileHandler implements IFileHandler {

	public File getImageFile(File file) {
		final int BUFFER = 65536;
		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				String entryName = entry.getName();
				if (entryName.startsWith("0.")) { //$NON-NLS-1$
					int p = entryName.lastIndexOf('.');
					String extension;
					if (p >= 0)
						extension = entryName.substring(p);
					else
						extension = ""; //$NON-NLS-1$
					File out = ImageActivator.getDefault().createTempFile("Eip", extension); //$NON-NLS-1$
					int count;
					byte data[] = new byte[BUFFER];
					// write the files to the disk
					try (BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(out), BUFFER)) {
						while ((count = zis.read(data, 0, BUFFER)) != -1)
							dest.write(data, 0, count);
						dest.flush();
						return out;
					}
				}
			}
			return null;
		} catch (Exception e) {
			ImageActivator.getDefault().logError(NLS.bind(Messages.EipFileHandler_error_when_unpacking_image, file), e);
			return null;
		}
	}

	public File getSidecar(File file) {
		final int BUFFER = 2048;
		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				String entryName = entry.getName();
				if (entryName.toUpperCase().endsWith(".COS")) { //$NON-NLS-1$
					File out = ImageActivator.getDefault().createTempFile("Eip", ".cos"); //$NON-NLS-1$ //$NON-NLS-2$
					int count;
					byte data[] = new byte[BUFFER];
					// write the files to the disk
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					while ((count = zis.read(data, 0, BUFFER)) != -1)
						bos.write(data, 0, count);
					byte[] byteArray = bos.toByteArray();
					int len = byteArray.length;
					while (byteArray[--len] == 0) {
						// search for first char
					}
					try (BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(out), BUFFER)) {
						dest.write(byteArray, 0, len + 1);
						return out;
					}
				}
			}
			return null;
		} catch (Exception e) {
			ImageActivator.getDefault().logError(NLS.bind(Messages.EipFileHandler_error_when_unpacking_sidecar, file),
					e);
			return null;
		}
	}

}
