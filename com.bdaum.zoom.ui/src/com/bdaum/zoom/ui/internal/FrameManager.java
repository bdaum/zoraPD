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

import org.eclipse.core.runtime.ListenerList;

import com.bdaum.zoom.ui.IFrameListener;
import com.bdaum.zoom.ui.IFrameManager;
import com.bdaum.zoom.ui.IFrameProvider;

public class FrameManager implements IFrameManager, IFrameListener {

	private ListenerList<IFrameListener> listeners = new ListenerList<>();

	@Override
	public void addFrameListener(IFrameListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeFrameListener(IFrameListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void registerFrameProvider(IFrameProvider provider) {
		provider.addFrameListener(this);
	}

	@Override
	public void deregisterFrameProvider(IFrameProvider provider) {
		provider.removeFrameListener(this);
	}

	@Override
	public void frameChanged(IFrameProvider provider, String assetId, double x, double y, double w, double h) {
		for (IFrameListener listener : listeners)
			listener.frameChanged(provider, assetId, x, y, w, h);
	}

}
