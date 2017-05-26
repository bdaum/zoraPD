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
package com.bdaum.zoom.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface IFTPService {
	
	String FTPSCHEME = "ftp"; //$NON-NLS-1$
	
	/**
	 * Starts a new FTP session
	 * @return ticket object
	 */
	Object startSession();
	
	/**
	 * Ends an FTP session
	 * @param ticket - ticket object representing the session
	 */
	void endSession(Object ticket);
	
	/**
	 * Retrieves a file resource from an FTP account as an input stream
	 * @param ticket - ticket object representing the session
	 * @param url - URL identifying the file
	 * @return - the input stream
	 * @throws IOException
	 */
	InputStream retrieveFile(Object ticket, URL url) throws IOException;

	/**
	 * Returns an FTP client instance 
	 * @param ticket - ticket object representing the session
	 * @param url - URL identifying the FTP account
	 * @return - FTP client
	 */
	Object getClient(Object ticket, URL url);

}
