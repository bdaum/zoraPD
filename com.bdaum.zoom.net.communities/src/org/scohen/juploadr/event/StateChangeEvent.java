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
 * Modifications (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package org.scohen.juploadr.event;

import java.util.EventObject;

/**
 * @author steve
 * 
 */
@SuppressWarnings("serial")
public class StateChangeEvent extends EventObject {
    private int bytesRead;

    /**
     * @param arg0
     */
    public StateChangeEvent(Object src) {
        super(src);
    }

    /**
     * @return Returns the bytesRead.
     */
    public int getBytesRead() {
        return bytesRead;
    }

    /**
     * @param bytesRead
     *            The bytesRead to set.
     */
    public void setBytesRead(int bytesRead) {
        this.bytesRead = bytesRead;
    }
}
