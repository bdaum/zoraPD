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
package com.bdaum.zoom.video.internal.views;

import java.io.IOException;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.internal.views.AbstractMediaViewer;

@SuppressWarnings("restriction")
public class VideoViewer extends AbstractMediaViewer {


	public void init(IWorkbenchWindow window, int kind, RGB bw, int cropmode) {
		//dummy
	}

	public void create() {
		//dummy
	}

	public void open(Asset[] assets) throws IOException {
		//dummy
	}

	public boolean close() {
		return false;
	}
	public void releaseKey(KeyEvent e) {
		//dummy
	}

	public boolean canHandleRemote() {
		return true;
	}

	@Override
	public boolean isDisposed() {
		return false;
	}

	@Override
	public boolean isDummy() {
		return true;
	}
	
}
