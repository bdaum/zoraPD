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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal;

import java.util.Date;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.ui.IStateListener;

public interface IKiosk {
	
	int PRIMARY = 0;
	int LEFT = 1;
	int RIGHT = 2;
	
	/**
	 * Initializes the kiosk
	 * 
	 * @param parentWindow - the parent window
	 * @param kind - PRIMARY, LEFT, or RIGHT
	 */
	void init(IWorkbenchWindow parentWindow,  int kind);
	

	/**
	 * Set the shell bounds for non primary viewers
	 * @param bounds - bounds to be appied
	 */
	void setBounds(Rectangle bounds);

	
	/**
	 * Creates the GUI of the kiosk
	 */
	void create();

	/**
	 * Return the time the viewer was created
	 * @return the time the viewer was created
	 */
	Date getCreationDate();

	/**
	 * Closes the viewer
	 */
	boolean close();
	
	/**
	 * Indicates if the viewer has been closed
	 */
	boolean isDisposed();

	
	/**
	 * Adds a state listener
	 * @param listener - listener to be added
	 */
	void addStateListener(IStateListener listener);
	
	/**
	 * Remove a state listener
	 * @param listener - listener to be removed
	 */
	void removeStateListener(IStateListener listener);

}