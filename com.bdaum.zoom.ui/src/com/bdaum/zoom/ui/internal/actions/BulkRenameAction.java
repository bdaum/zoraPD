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
 * (c) 2015 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.actions;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.BulkRenameOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.dialogs.BulkRenameDialog;

@SuppressWarnings("restriction")
public class BulkRenameAction extends Action {

	private IAdaptable adaptable;

	public BulkRenameAction(String label, String tooltip,
			ImageDescriptor image, IAdaptable adaptable) {
		super(label, image);
		this.adaptable = adaptable;
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		List<Asset> selectedAssets = adaptable
				.getAdapter(AssetSelection.class).getAssets();
		if (!selectedAssets.isEmpty()) {
			BulkRenameDialog dialog = new BulkRenameDialog(
					adaptable.getAdapter(Shell.class),
					selectedAssets.get(0));
			if (dialog.open() == Window.OK)
				OperationJob.executeOperation(new BulkRenameOperation(
						selectedAssets, dialog.getResult(), dialog.getCue(), dialog.getStart(),
						dialog.getField()), adaptable);
		}

	}

}
