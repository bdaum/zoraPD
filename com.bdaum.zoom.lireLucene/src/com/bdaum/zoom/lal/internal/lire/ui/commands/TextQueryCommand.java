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

package com.bdaum.zoom.lal.internal.lire.ui.commands;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.lal.internal.lire.ui.dialogs.TextSearchDialog;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.commands.AbstractCommandHandler;

/**
 * @author berth
 *
 */
@SuppressWarnings("restriction")
public class TextQueryCommand extends AbstractCommandHandler {

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.ui.internal.commands.AbstractCommandHandler#run()
	 */
	@Override
	public void run() {
		if (!Core.getCore().getDbManager().getMeta(true).getNoIndex()) {
			Shell shell = getShell();
			TextSearchDialog dialog = new TextSearchDialog(shell, null, null);
			if (dialog.open() == Window.OK) {
				SmartCollection collection = dialog.getResult();
				Ui.getUi().getNavigationHistory(getActiveWorkbenchWindow())
						.postSelection(new StructuredSelection(collection));
			}
		}
	}

	@Override
	public boolean isEnabled() {
		return !Core.getCore().getDbManager().getMeta(true).getNoIndex();
	}

}
