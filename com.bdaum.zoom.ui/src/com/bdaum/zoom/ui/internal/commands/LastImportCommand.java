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

import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.Ui;

public class LastImportCommand extends AbstractCommandHandler {

	@Override
	public void run() {
		SmartCollectionImpl sm = Core.getCore().getDbManager().obtainById(SmartCollectionImpl.class,
				Constants.LAST_IMPORT_ID);
		if (sm != null)
			Ui.getUi().getNavigationHistory(getActiveWorkbenchWindow())
					.postSelection(new StructuredSelection(sm));
	}

}
