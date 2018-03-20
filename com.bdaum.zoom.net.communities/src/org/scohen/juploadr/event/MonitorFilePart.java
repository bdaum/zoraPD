/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 * It is an adaptation of the equally named file from the jUploadr project (http://sourceforge.net/projects/juploadr/)
 * (c) 2004 Steve Cohen and others
 * 
 * jUploadr is licensed under the GNU Library or Lesser General Public License (LGPL).
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
 * Modifications (c) 2009 Berthold Daum  
 */

package org.scohen.juploadr.event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.scohen.juploadr.app.ImageAttributes;

/**
 * @author steve
 * 
 */
public class MonitorFilePart extends FilePart {
	private List<UploadStatusMonitor> listeners;
	private File uploadFile;
	private ImageAttributes image;

	/**
	 * @param arg0
	 * @param arg1
	 * @throws java.io.FileNotFoundException
	 */
	public MonitorFilePart(String arg0, File arg1, ImageAttributes img)
			throws FileNotFoundException {
		super(arg0, arg1);
		uploadFile = arg1;
		this.image = img;
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @throws java.io.FileNotFoundException
	 */
	public MonitorFilePart(String arg0, File arg1, String arg2, String arg3,
			ImageAttributes img) throws FileNotFoundException {
		super(arg0, arg1, arg2, arg3);
		uploadFile = arg1;
		this.image = img;
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @throws java.io.FileNotFoundException
	 */
	public MonitorFilePart(String arg0, String arg1, File arg2,
			ImageAttributes img) throws FileNotFoundException {
		super(arg0, arg1, arg2);
		this.image = img;
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @throws java.io.FileNotFoundException
	 */
	public MonitorFilePart(String arg0, String arg1, File arg2, String arg3,
			String arg4, ImageAttributes img) throws FileNotFoundException {
		super(arg0, arg1, arg2, arg3, arg4);
		uploadFile = arg2;
		this.image = img;
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public MonitorFilePart(String arg0, PartSource arg1, ImageAttributes img) {
		super(arg0, arg1);
		uploadFile = new File(arg1.getFileName());
		this.image = img;
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public MonitorFilePart(String arg0, PartSource arg1, String arg2,
			String arg3, ImageAttributes img) {
		super(arg0, arg1, arg2, arg3);
		uploadFile = new File(arg1.getFileName());
		this.image = img;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.commons.httpclient.methods.multipart.Part#sendData(java.io
	 * .OutputStream)
	 */

	@Override
	protected void sendData(OutputStream out) throws IOException {

		@SuppressWarnings("resource")
		EventOutputStream eventOut = new EventOutputStream(out, image, 1);
		eventOut.setFileName(uploadFile.getAbsolutePath());
		for (int i = 0; i < listeners.size(); i++) {
			UploadStatusMonitor listener = listeners.get(i);
			eventOut.addUploadStatusMonitor(listener);
		}
		super.sendData(eventOut);
	}

	public void addStateChangeListener(UploadStatusMonitor toAdd) {
		if (listeners == null) {
			listeners = new LinkedList<UploadStatusMonitor>();
		}
		listeners.add(toAdd);
	}

	public ImageAttributes getImage() {
		return image;
	}

	public void setImage(ImageAttributes image) {
		this.image = image;
	}
}