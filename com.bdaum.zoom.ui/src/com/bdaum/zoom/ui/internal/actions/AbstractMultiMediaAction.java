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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.ui.AssetSelection;

@SuppressWarnings("restriction")
public abstract class AbstractMultiMediaAction extends AbstractSelectionAction  {

	private final int flags;

	public AbstractMultiMediaAction(IWorkbenchWindow window, String label, ImageDescriptor image, int flags) {
		super(window, label, image);
		this.flags = flags;
	}


	@Override
	public void assetsChanged(IWorkbenchPart part, AssetSelection selectedAssets) {
		selection = selectedAssets;
		CoreActivator activator = CoreActivator.getDefault();
		for (Asset asset : selection) {
			IMediaSupport mediaSupport = activator.getMediaSupport(asset.getFormat());
			if (mediaSupport != null) {
				if (mediaSupport.testProperty(flags)) {
					setEnabled(true);
					return;
				}
			} else if ((flags & QueryField.PHOTO) != 0) {
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);
	}

}