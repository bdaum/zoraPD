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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.views;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import com.bdaum.zoom.ui.internal.hover.IGalleryHover;

public interface IHoverSubject {

	public abstract Control getControl();
	
	public abstract Control[] getControls();

	public abstract String getTooltip(int mx, int my);

	public abstract Object findObject(Event e);

	/**
	 * Find all regions under the mouse cursor
	 * @param event - mouse event
	 * @return - Array of region IDs
	 */
	public abstract ImageRegion[] findAllRegions(Event event);

	public abstract IGalleryHover getGalleryHover(Event event);

}