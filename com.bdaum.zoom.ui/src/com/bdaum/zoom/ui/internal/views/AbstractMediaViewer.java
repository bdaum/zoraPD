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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.internal.AbstractKiosk;
import com.bdaum.zoom.ui.views.IMediaViewer;

public abstract class AbstractMediaViewer extends AbstractKiosk implements IMediaViewer, Listener {

	protected String name;
	protected String id;
	protected RGB bwmode;
	protected int cropmode;
	protected Asset asset;
	protected boolean keyDown;

	@Override
	public void init(IWorkbenchWindow window, int kind, RGB bwmode, int cropmode) {
		super.init(window, kind);
		this.bwmode = bwmode;
		this.cropmode = cropmode;
		keyDown = false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean canHandleRemote() {
		return false;
	}
	
	@Override
	public void releaseKey(Event e) {
		// do nothing
	}

	@Override
	public boolean isDummy() {
		return false;
	}
	
	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.KeyDown:
			if (keyDown)
				releaseKey(e);
			keyDown = true;
			break;
		case SWT.KeyUp:
			releaseKey(e);
			keyDown = false;
			break;
		}
	}

}
