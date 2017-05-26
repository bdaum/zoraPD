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
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.UiActivator;

/**
 * Base class for resizable Dialogs with persistent window bounds.
 */
public abstract class ZResizableDialog extends ZTrayDialog {

	// dialog store id constants
	private final static String DIALOG_BOUNDS_KEY = "ResizableDialogBounds."; //$NON-NLS-1$
	private static final String X = "x"; //$NON-NLS-1$
	private static final String Y = "y"; //$NON-NLS-1$
	private static final String WIDTH = "width"; //$NON-NLS-1$
	private static final String HEIGHT = "height"; //$NON-NLS-1$
	private static final String SETTINGSID = "com.bdaum.zoom.ui.resizableDialog"; //$NON-NLS-1$

	Rectangle fNewBounds;
	private IDialogSettings fSettings;
	private int initialWidth = 700;
	private int initialHeight = 500;

	public ZResizableDialog(Shell parent) {
		this(parent, null);
	}

	public ZResizableDialog(Shell parent, String helpId) {
		super(parent, helpId);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fSettings =  UiActivator.getDefault().getDialogSettings(SETTINGSID);
	}
	

	@Override
	public void create() {
		super.create();
		if (helpId != null)
			setHelpId(helpId);
	}

	/**
	 * Sets the context help ID of the dialog
	 *
	 * @param id
	 *            - help id
	 */
	public void setHelpId(String id) {
		setHelpAvailable(id != null);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), id);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Constants.APPLICATION_NAME);
	}

	@Override
	protected Point getInitialSize() {

		int width = 0;
		int height = 0;

		final Shell s = getShell();
		if (s != null) {
			s.addControlListener(new ControlListener() {

				public void controlMoved(ControlEvent arg0) {
					fNewBounds = s.getBounds();
				}

				public void controlResized(ControlEvent arg0) {
					fNewBounds = s.getBounds();
				}
			});
		}

		IDialogSettings bounds = fSettings.getSection(getBoundsKey());
		if (bounds == null) {
			Shell shell = getParentShell();
			if (shell != null) {
				Point parentSize = shell.getSize();
				width = parentSize.x - 100;
				height = parentSize.y - 100;
			}
			if (width < initialWidth)
				width = initialWidth;
			if (height < initialHeight)
				height = initialHeight;
		} else {
			Point pnt = getDefaultSize();
			if (pnt != null)
				return pnt;
			try {
				width = bounds.getInt(WIDTH);
			} catch (NumberFormatException e) {
				width = initialWidth;
			}
			try {
				height = bounds.getInt(HEIGHT);
			} catch (NumberFormatException e) {
				height = initialHeight;
			}
		}

		return new Point(width, height);
	}

	protected Point getDefaultSize() {
		return null;
	}

	/**
	 * @return the bounds key
	 */
	private String getBoundsKey() {
		return DIALOG_BOUNDS_KEY+getId();
	}

	/**
	 * @return the id
	 */
	protected abstract String getId();


	@Override
	protected Point getInitialLocation(Point initialSize) {
		Point loc = super.getInitialLocation(initialSize);

		IDialogSettings bounds = fSettings.getSection(getBoundsKey());
		if (bounds != null) {
			try {
				loc.x = bounds.getInt(X);
			} catch (NumberFormatException e) {
				// do nothing
			}
			try {
				loc.y = bounds.getInt(Y);
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		return loc;
	}


	@Override
	public boolean close() {
		boolean closed = super.close();
		if (closed && fNewBounds != null)
			saveBounds(fNewBounds);
		return closed;
	}

	private void saveBounds(Rectangle bounds) {
		IDialogSettings dialogBounds = fSettings.getSection(getBoundsKey());
		if (dialogBounds == null) {
			dialogBounds = new DialogSettings(getBoundsKey());
			fSettings.addSection(dialogBounds);
		}
		dialogBounds.put(X, bounds.x);
		dialogBounds.put(Y, bounds.y);
		dialogBounds.put(WIDTH, bounds.width);
		dialogBounds.put(HEIGHT, bounds.height);
	}
}
