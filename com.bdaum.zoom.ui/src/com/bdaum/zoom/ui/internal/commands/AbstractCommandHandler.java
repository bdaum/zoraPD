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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class AbstractCommandHandler extends AbstractHandler implements IAdaptable {
	

	private Shell shell;

	private IWorkbenchWindow activeWorkbenchWindow;

	protected void init(ExecutionEvent event) {
		if (event != null)
			init(HandlerUtil.getActiveWorkbenchWindow(event));
	}

	public void init(IWorkbenchWindow w) {
		if (w != null)
			activeWorkbenchWindow = w;
		if (activeWorkbenchWindow != null)
			shell = activeWorkbenchWindow.getShell();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter))
			return shell;
		return null;
	}

	public Shell getShell() {
		return shell;
	}

	public IWorkbenchWindow getActiveWorkbenchWindow() {
		return activeWorkbenchWindow;
	}

	@Override
	public Object execute(ExecutionEvent event)  throws ExecutionException {
		init(event);
		run();
		return null;
	}

	public void run() {
		// do nothing here
	}

	
}