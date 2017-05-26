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

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;

import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;

public interface IGalleryGenerator {

	/**
	 * @param gallery - the web gallery
	 * @param monitor - a progress monitor 
	 * @param adaptable - adaptable delivering at least a Shell instance
	 * @param status - MultiStatus object to which other status objects may be added
	 * @throws IOException
	 */
	void generate(WebGalleryImpl gallery, IProgressMonitor monitor,
			IAdaptable adaptable, MultiStatus status) throws IOException;

	/**
	 * @return - true if web engine requires pre-generated thumbnails
	 */
	boolean needsThumbnails();
	
	/**
	 * @return - target folder for generated files
	 */
	File getTargetFolder();

}
