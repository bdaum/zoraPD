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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.scohen.juploadr.app.ImageAttributes;

/**
 * @author steve
 * 
 */
public class EventOutputStream extends OutputStream {
    private OutputStream source;
    private List<UploadStatusMonitor> listeners;
    private int bytesWritten;
    private String fileName;
    private ImageAttributes image;

    public EventOutputStream(OutputStream src, ImageAttributes image, @SuppressWarnings("unused") int delay) {
        this.source = src;
        this.image = image;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    
	@Override
	public void write(byte[] buffer, int offset, int rename) throws IOException {
        bytesWritten += rename;
        fireEvents();
        source.write(buffer, offset, rename);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(byte[])
     */
    
	@Override
	public void write(byte[] arg0) throws IOException {
        if (arg0 != null) {
            bytesWritten += arg0.length;
        }
        fireEvents();
        source.write(arg0);

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#write(int)
     */
    
	@Override
	public void write(int arg0) throws IOException {
        bytesWritten++;
        fireEvents();
        source.write(arg0);

    }

    public void addUploadStatusMonitor(UploadStatusMonitor toAdd) {
        if (listeners == null) {
            listeners = new LinkedList<UploadStatusMonitor>();
        }
        listeners.add(toAdd);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#close()
     */
    
	@Override
	public void close() throws IOException {
        source.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    
	@Override
	public boolean equals(Object arg0) {
        return source.equals(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.OutputStream#flush()
     */
    
	@Override
	public void flush() throws IOException {
        source.flush();
        fireEvents();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    
	@Override
	public int hashCode() {
        return source.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    
	@Override
	public String toString() {
        return source.toString();
    }

    private void fireEvents() {
        if (true) {
            // if (now > (lastSent + delay)) {
            UploadEvent event = new UploadEvent();
            event.setSource(image);
            event.setBytesWritten(bytesWritten);
            for (Iterator<UploadStatusMonitor> iter = listeners.iterator(); iter.hasNext();) {
                UploadStatusMonitor element = iter.next();
                element.uploadStatusChanged(event);
            }

        }
    }

    /**
     * @return Returns the fileName.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            The fileName to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}