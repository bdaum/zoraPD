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

package com.bdaum.zoom.ui.internal.views;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.swt.widgets.Event;

import com.bdaum.zoom.ui.internal.UiActivator;

public class ZColumnViewerToolTipSupport extends ColumnViewerToolTipSupport {

	public ZColumnViewerToolTipSupport(ColumnViewer viewer, int style, boolean manualActivation) {
		super(viewer, style, manualActivation);
	}
	
	@Override
	protected boolean shouldCreateToolTip(Event event) {
		return UiActivator.getDefault().getShowHover() && super.shouldCreateToolTip(event);
	}

}
