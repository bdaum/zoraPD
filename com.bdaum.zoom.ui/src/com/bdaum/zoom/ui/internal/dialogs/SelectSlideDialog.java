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
 * (c) 2011 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;

public class SelectSlideDialog extends AbstractSelectExhibitDialog {

	public SelectSlideDialog(Shell parentShell,
			List<? extends IdentifiableObject> slides) {
		super(parentShell, slides);
	}

	public SlideImpl getResult() {
		return (SlideImpl) selection;
	}

	@Override
	public String getText(Object element) {
		SlideImpl slide = (SlideImpl) element;
		if (slide.getAsset() != null)
			return "  " + slide.getCaption(); //$NON-NLS-1$
		return "--- " + slide.getCaption() + " ---"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public Font getFont(Object element) {
		return getAssetId(element) == null ? JFaceResources.getBannerFont()
				: null;
	}

	@Override
	protected String getAssetId(Object element) {
		return ((SlideImpl) element).getAsset();
	}

}
