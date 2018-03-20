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
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.RotateOperation;
import com.bdaum.zoom.ui.AssetSelection;

@SuppressWarnings("restriction")
public class RotateAction extends Action {

	private IAdaptable adaptable;
	private final int degrees;

	public RotateAction(IWorkbenchWindow window, String label, String tooltip, ImageDescriptor image,
			IAdaptable adaptable, int degrees) {
		super(label, image);
		this.adaptable = adaptable;
		this.degrees = degrees;
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		OperationJob.executeOperation(
				new RotateOperation(adaptable.getAdapter(AssetSelection.class).getLocalAssets(), degrees), adaptable);
	}

}
