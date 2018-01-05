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

package com.bdaum.zoom.ui.views;

import java.io.IOException;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;

public interface IMediaViewer {

	/**
	 * Initializes and configures the viewer
	 *
	 * @param window
	 *            - the parent workbench window
	 * @param bwmode
	 *            - RGB value if images are to be displayed as grayscale images, null otherwise. The RGB value is used as a filter
	 * @param cropmode
	 *            - ZImage.CROPPED, ZImage.CROPMASK, ZImage.ORIGINAL
	 */
	void init(IWorkbenchWindow window, RGB bwmode, int cropmode);

	/**
	 * Creates the GUI of the image viewer
	 */
	void create();

	/**
	 * Creates the GUI if not already created Opens the specified image
	 *
	 * @param asset
	 *            - the image to be displayed
	 * @throws IOException
	 */
	void open(Asset asset) throws IOException;

	/**
	 * Closes the viewer
	 */
	void close();

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
	 * @return - true if the image viewer can handle remote files
	 */
	boolean canHandleRemote();

}