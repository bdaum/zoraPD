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
 * (c) 2009-2018 Berthold Daum  
 */

package com.bdaum.zoom.ui.views;

import java.io.IOException;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.internal.IKiosk;

public interface IMediaViewer extends IKiosk {

	/**
	 * Initializes and configures the viewer
	 *
	 * @param window
	 *            - the parent workbench window
	 * @param kind
	 *            - PRIMARY if this is a primary viewer, LEFT or RIGHT if it is a
	 *            subviewer
	 * @param bwmode
	 *            - RGB value if images are to be displayed as grayscale images,
	 *            null otherwise. The RGB value is used as a filter
	 * @param cropmode
	 *            - ZImage.CROPPED, ZImage.CROPMASK, ZImage.ORIGINAL
	 */
	void init(IWorkbenchWindow window, int kind, RGB bwmode, int cropmode);

	/**
	 * Creates the GUI if not already created Opens the specified images
	 *
	 * @param assets
	 *            - the images to be displayed
	 * @throws IOException
	 */
	void open(Asset[] assets) throws IOException;

	/**
	 * Retrieves the viewers name
	 *
	 * @return viewers name
	 */
	String getName();

	/**
	 * Retrieves the viewers ID
	 *
	 * @return ID
	 */
	String getId();

	/**
	 * Sets the viewers ID
	 *
	 * @param id
	 *            - ID
	 */
	void setId(String id);

	/**
	 * Sets the viewers name
	 *
	 * @param name
	 *            - Name
	 */
	void setName(String name);

	/**
	 * Indicates if the image viewer can handle remote files
	 * 
	 * @return - true if the image viewer can handle remote files
	 */
	boolean canHandleRemote();

	/**
	 * Notifies of a key released
	 * @param e - key event
	 */
	void releaseKey(Event e);
	
	/**
	 * True if this viewer is not a valid internal viewer
	 * Default: false
	 * @return
	 */
	boolean isDummy();

}