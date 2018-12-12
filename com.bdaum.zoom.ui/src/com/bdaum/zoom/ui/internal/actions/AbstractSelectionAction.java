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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.views.EducatedSelectionListener;

public abstract class AbstractSelectionAction extends Action implements EducatedSelectionListener {

	protected IWorkbenchWindow window;
	protected AssetSelection selection;

	public AbstractSelectionAction(IWorkbenchWindow window, String label, ImageDescriptor image) {
		super(label, image);
		this.window = window;
		Ui.getUi().getNavigationHistory(window).addSelectionListener(this);
	}

	public void assetsChanged(IWorkbenchPart part, AssetSelection selectedAssets) {
		selection = selectedAssets;
		setEnabled(!selectedAssets.isEmpty());
	}

	public void filterChanged() {
		// do nothing
	}

	public void collectionChanged(IWorkbenchPart part, IStructuredSelection sel) {
		// do nothing
	}

	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		// do nothing
	}

	public void sortChanged() {
		// do nothing
	}

	public void cueChanged(Object object) {
		// do nothing
	}

}