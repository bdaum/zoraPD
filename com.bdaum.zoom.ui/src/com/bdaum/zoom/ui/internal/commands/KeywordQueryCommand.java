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

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;

import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.KeywordSearchDialog;

/**
 * @author berth
 *
 */
public class KeywordQueryCommand extends AbstractCommandHandler {

	@Override
	public void run() {
		KeywordSearchDialog dialog = new KeywordSearchDialog(getShell(), null);
		if (dialog.open() == Window.OK)
			UiActivator.getDefault().getNavigationHistory(getActiveWorkbenchWindow())
					.postSelection(new StructuredSelection(dialog.getResult()));
	}

}
