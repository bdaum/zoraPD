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

package com.bdaum.zoom.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.ui.internal.UiActivator;

/**
 * This class provides access to UI instances.
 */
public class Ui {

	private static Runnable runnable;
	private static boolean active;

	static {
		runnable = new Runnable() {
			public void run() {
				Display display = PlatformUI.getWorkbench().getDisplay();
				if (display == null || display.isDisposed())
					active = false;
				else {
					Shell activeShell = display.getActiveShell();
					active = activeShell == null ? false : activeShell.getMinimized() && activeShell.isVisible();
				}
			}
		};
	}

	/**
	 * @return singleton instance of the core UI class
	 */
	public static IUi getUi() {
		return UiActivator.getDefault();
	}

	public static boolean isWorkbenchActive() {
		try {
			Display display = PlatformUI.getWorkbench().getDisplay();
			if (display == null || display.isDisposed())
				return false;
			display.syncExec(runnable);
			return active;
		} catch (Exception e) {
			return false;
		}
	}

}
