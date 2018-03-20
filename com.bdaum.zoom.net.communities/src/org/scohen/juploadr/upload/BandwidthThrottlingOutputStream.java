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

package org.scohen.juploadr.upload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BandwidthThrottlingOutputStream extends OutputStream {
    private static final int FREQUENCY = 8;
    private static final int WINDOW_LENGTH = 1000 / FREQUENCY;
    private int bytesPerWindow;
    private int bytesWrittenThisWindow = 0;
    private long windowEnd;
    private OutputStream out;
    long writeStartedAt = -1;

    public BandwidthThrottlingOutputStream(final OutputStream out, int maxBytesPerSecond) {
        super();
        this.bytesPerWindow = maxBytesPerSecond / FREQUENCY;
        this.out = out;
    }

    
	@Override
	public void write(int b) throws IOException {
        checkTime();
        if (bytesWrittenThisWindow > bytesPerWindow) {
            synchronized (out) {
                long howLongToWait = windowEnd - System.currentTimeMillis();
                if (howLongToWait > 0) {
                    try {
                        out.wait(howLongToWait);
                    } catch (InterruptedException e) {
                        throw new IOException(Messages.BandwidthThrottlingOutputStream_write_interrupted);
                    }
                }
            }
        }
        out.write(b);
        bytesWrittenThisWindow++;
    }

    
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
        if (len <= bytesPerWindow) {
            out.write(b, off, len);
        } else {
            byte[] buffer = new byte[bytesPerWindow];
            ByteArrayInputStream in = new ByteArrayInputStream(b, off, len);

            while (in.available() > 0) {
                checkTime();
                int numread = in.read(buffer);
                out.write(buffer, 0, numread);
                bytesWrittenThisWindow += numread;

                if (bytesWrittenThisWindow > bytesPerWindow) {
                    synchronized (out) {
                        long howLongToWait = windowEnd - System.currentTimeMillis();
                        if (howLongToWait > 0) {
                            try {
                                out.wait(howLongToWait);
                            } catch (InterruptedException e) {
                                throw new IOException(Messages.BandwidthThrottlingOutputStream_write_interrupted);
                            }
                        }
                    }
                }
            }
        }
    }

    
	@Override
	public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void setMaxBytesPerSecond(int bytesPerSecond) {
        this.bytesPerWindow = bytesPerSecond;
    }

    private void checkTime() {
        if (System.currentTimeMillis() - writeStartedAt >= WINDOW_LENGTH) {
            writeStartedAt = System.currentTimeMillis();
            windowEnd = writeStartedAt + WINDOW_LENGTH;
            bytesWrittenThisWindow = 0;
        }
    }

}
