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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.core.db;


public interface IDbListener {

	/*** Closing modi ***/
	int NORMAL = 0;
	int SHUTDOWN = 1;
	int EMERGENCY = 2;
	int TASKBAR = 3;
	int TUNE = 4;

	/**
	 * Signals that a database will be opened
	 * @param filename - database file name
	 * @param primary - true if database is opened as primary database
	 * @return - database manager instance if database is already open or null
	 */
	IDbManager databaseAboutToOpen(String filename, boolean primary);

	/**
	 * Signals that a database will be opened
	 * @param manager - database manager
	 * @param primary  - true if database is opened as primary database
	 */
	void databaseOpened(IDbManager manager, boolean primary);

	/**
	 * Signals that a database will be opened
	 * @param manager - database manager
	 * @return true if closing is okay, false if database should not be closed
	 */
	boolean databaseAboutToClose(IDbManager manager);

	/**
	 * Signals that a database had been closed
	 * @param manager - database manager
	 * @param mode - closing mode
	 */
	void databaseClosed(IDbManager manager, int mode);

}
