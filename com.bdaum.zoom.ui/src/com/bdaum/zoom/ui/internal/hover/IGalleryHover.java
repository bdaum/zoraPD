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

package com.bdaum.zoom.ui.internal.hover;

import org.eclipse.swt.events.MouseEvent;

import com.bdaum.zoom.ui.internal.views.IHoverSubject;

/**
 * Provides a hover popup which appears on top of the model viewer with relevant
 * display information. If the hover does not provide information no hover popup
 * is shown.
 * <p>
 * Clients may implement this interface.
 * 
 */
public interface IGalleryHover {

	/**
	 * Returns the text which should be presented if a hover popup is shown for
	 * the specified hover location.
	 * 
	 * @param subject
	 *            the hover subject on which the hover popup should be shown
	 * @param event
	 *            the mouse event causing the hover
	 * @return the hover popup display information
	 */
	IHoverInfo getHoverInfo(IHoverSubject subject, MouseEvent event);
}
