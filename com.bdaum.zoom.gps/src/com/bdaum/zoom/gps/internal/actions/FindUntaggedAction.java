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
package com.bdaum.zoom.gps.internal.actions;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.dialogs.FindUntaggedDialog;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.actions.AbstractViewAction;

public class FindUntaggedAction extends AbstractViewAction {

	public FindUntaggedAction() {
		setImageDescriptor(GpsActivator.imageDescriptorFromPlugin(GpsActivator.PLUGIN_ID, "/icons/untagged.gif")); //$NON-NLS-1$
	}

	@Override
	public void run() {
		if (adaptable == null)
			return;
		FindUntaggedDialog dialog = new FindUntaggedDialog(adaptable.getAdapter(Shell.class));
		if (dialog.open() == Window.OK)
			Ui.getUi().getNavigationHistory(adaptable.getAdapter(IWorkbenchWindow.class))
					.postSelection(new StructuredSelection(dialog.getResult()));
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(true);
	}

}
