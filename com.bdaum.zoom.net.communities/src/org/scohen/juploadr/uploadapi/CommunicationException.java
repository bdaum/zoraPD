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

/**
 * @author wItspirit
 * 29-mei-2005
 * FlickrCommunicationException.java
 */

package org.scohen.juploadr.uploadapi;

/**
 * Thrown when there is a problem with the communication with Flickr This means
 * that there was no contact with Flickr or the responses could not be properly
 * interpreted.
 */
@SuppressWarnings("serial")
public class CommunicationException extends Exception {

    /**
     * 
     */
    public CommunicationException() {
        super();
    }

    /**
     * @param arg0
     */
    public CommunicationException(String arg0) {
        super(arg0);
    }

    /**
     * @param arg0
     * @param arg1
     */
    public CommunicationException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * @param arg0
     */
    public CommunicationException(Throwable arg0) {
        super(arg0);
    }

}
