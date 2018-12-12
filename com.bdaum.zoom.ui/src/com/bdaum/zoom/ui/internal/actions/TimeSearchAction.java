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

package com.bdaum.zoom.ui.internal.actions;

import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.dialogs.TimeSearchDialog;

public class TimeSearchAction extends Action {

	private IAdaptable adaptable;

	public TimeSearchAction(String label, String tooltip, ImageDescriptor image, IAdaptable adaptable) {
		super(label, image);
		this.adaptable = adaptable;
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		Date date1 = new Date(Long.MAX_VALUE);
		Date date2 = new Date(0);
		List<Asset> selectedAssets = adaptable.getAdapter(AssetSelection.class).getAssets();
		if (!selectedAssets.isEmpty())
			for (Asset asset : selectedAssets) {
				Date date = asset.getDateTimeOriginal();
				if (date == null)
					date = asset.getDateTime();
				if (date != null) {
					if (date1.compareTo(date) > 0)
						date1 = date;
					if (date2.compareTo(date) < 0)
						date2 = date;
				}
			}
		if (date1.compareTo(date2) > 0) {
			date1 = new Date();
			date2 = new Date();
		}
		TimeSearchDialog dialog = new TimeSearchDialog(adaptable.getAdapter(Shell.class), date1, date2);
		if (dialog.open() == Window.OK)
			Ui.getUi().getNavigationHistory(adaptable.getAdapter(IWorkbenchWindow.class))
					.postSelection(new StructuredSelection(dialog.getResult()));
	}
}
