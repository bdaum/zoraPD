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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.hover;

import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.UiActivator;

public class WatchedFolderLabelProvider extends ZColumnLabelProvider {

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof WatchedFolder && UiActivator.getDefault().getShowHover())
			return UiActivator.getDefault().getHoverManager().getHoverText("com.bdaum.zoom.ui.hover.watchedFolder", //$NON-NLS-1$
					element, null);
		return ""; //$NON-NLS-1$
	}

}
