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
 * (c) 2014 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.wizards;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.IUi;
import com.bdaum.zoom.ui.Ui;

public abstract class AbstractAssetSelectionWizard extends ZWizard {

	protected List<Asset> assets;
	protected List<Object> setAssets(IWorkbench workbench, IStructuredSelection selection, boolean prune) {
		IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		AssetSelection assetSelection;
		IUi ui = Ui.getUi();
		if (activeWorkbenchWindow != null) {
			assetSelection = ui
					.getNavigationHistory(activeWorkbenchWindow)
					.getSelectedAssets();
		} else
			assetSelection = (selection instanceof AssetSelection) ? ((AssetSelection) selection)
					: AssetSelection.EMPTY;
		assets = assetSelection.getAssets();
		if (assets.isEmpty()) {
			List<Object> presentationItems = ui.getPresentationItems();
			assets = ui.getAssetsFromPresentationItems(presentationItems, prune);
			return presentationItems;
		}
		return null;
	}

}