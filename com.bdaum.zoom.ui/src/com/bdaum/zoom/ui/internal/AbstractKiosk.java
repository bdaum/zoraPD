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

import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.IStateListener;

public abstract class AbstractKiosk implements IKiosk, DisposeListener, IAdaptable {

	protected Date creationDate;
	protected IWorkbenchWindow parentWindow;
	protected Shell shell;
	protected Rectangle mbounds;
	protected Display display;
	protected Cursor transparentCursor;
	protected int kind;
	private ListenerList<IStateListener> stateListeners = new ListenerList<>();

	@Override
	public void init(IWorkbenchWindow parentWindow, int kind) {
		this.parentWindow = parentWindow;
		this.kind = kind;
		shell = parentWindow.getShell();
		if (kind == PRIMARY)
			shell.addDisposeListener(this);
		this.display = shell.getDisplay();
	}

	public void setBounds(Rectangle bounds) {
		mbounds = bounds;
	}

	@Override
	public void create() {
		creationDate = new Date();
		if (mbounds == null)
			UiActivator.getDefault().registerKiosk(this,
					mbounds = UiActivator.getDefault().getSecondaryMonitorBounds(shell));
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public boolean close() {
		if (transparentCursor != null) {
			transparentCursor.dispose();
			transparentCursor = null;
		}
		if (kind == PRIMARY)
			UiActivator.getDefault().registerKiosk(null, mbounds);
		return true;
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		close();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		return (Shell.class.equals(adapter)) ? parentWindow.getShell() : null;
	}

	protected Shell createKioskShell(String label) {
		Shell shell = new Shell(display, SWT.NO_TRIM);
		shell.setText(NLS.bind("{0} - {1}", Constants.APPNAME, label)); //$NON-NLS-1$
		shell.setImage(Icons.zoraShell.getImage());
		shell.setFullScreen(true);
		shell.setLayout(new FillLayout());
		shell.setBounds(mbounds);
		return shell;
	}

	protected void createTransparentCursor() {
		ImageData cursorData = new ImageData(16, 16, 1, new PaletteData(new RGB[] {
				display.getSystemColor(SWT.COLOR_WHITE).getRGB(), display.getSystemColor(SWT.COLOR_BLACK).getRGB() }));
		cursorData.transparentPixel = 0;
		transparentCursor = new Cursor(display, cursorData, 0, 0);
	}

	protected void sleepTick(long tick) {
		try {
			Thread.sleep(tick);
		} catch (InterruptedException e) {
			// do nothing
		}
	}

	public void addStateListener(IStateListener listener) {
		stateListeners.add(listener);
	}

	public void removeStateListener(IStateListener listener) {
		stateListeners.remove(listener);
	}

	protected void fireStateEvent(int state) {
		for (IStateListener listener : stateListeners)
			listener.stateChanged(this, state);
	}

}
