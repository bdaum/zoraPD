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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.views.HierarchyView;

public class ShowAssetAction extends Action {

	private IAdaptable adaptable;
	private int type;

	public ShowAssetAction(int type, String text, ImageDescriptor image, String tooltip, IAdaptable adaptable) {
		super(text, image);
		this.adaptable = adaptable;
		this.type = type;
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		IWorkbenchPage activePage = adaptable.getAdapter(IWorkbenchPage.class);
		Asset localAsset = adaptable.getAdapter(AssetSelection.class).getFirstLocalAsset();
		if (localAsset != null)
			try {
				IViewPart view = activePage.showView(HierarchyView.IDS[type]);
				if (view instanceof HierarchyView)
					((HierarchyView) view).setInput(localAsset);
			} catch (PartInitException e) {
				// do nothing
			}
	}
}
