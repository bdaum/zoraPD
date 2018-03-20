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
package com.bdaum.zoom.net.communities;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.bdaum.zoom.css.ZColumnLabelProvider;

final class AccountLabelProvider extends ZColumnLabelProvider {

	protected Map<String, Image> imageMap = new HashMap<String, Image>();
	private final Viewer viewer;

	public AccountLabelProvider(Viewer viewer) {
		this.viewer = viewer;
	}

	
	@Override
	public String getText(Object element) {
		if (element instanceof CommunityAccount)
			return ((CommunityAccount) element).getName();
		if (element instanceof IConfigurationElement)
			return ((IConfigurationElement) element).getAttribute("name"); //$NON-NLS-1$
		return element.toString();
	}

	
	@Override
	public Image getImage(Object element) {
		if (element instanceof IConfigurationElement) {
			String iconpath = ((IConfigurationElement) element)
					.getAttribute("icon"); //$NON-NLS-1$
			if (iconpath != null) {
				Image image = imageMap.get(iconpath);
				if (image == null) {
					String namespaceIdentifier = ((IConfigurationElement) element)
							.getNamespaceIdentifier();
					ImageDescriptor imageDescriptor = AbstractUIPlugin
							.imageDescriptorFromPlugin(namespaceIdentifier,
									iconpath);
					image = imageDescriptor.createImage(viewer.getControl()
							.getDisplay());
					if (image != null)
						imageMap.put(iconpath, image);
				}
				return image;
			}
		}
		return null;
	}

	
	@Override
	public void dispose() {
		for (Image image : imageMap.values()) {
			image.dispose();
		}
		super.dispose();
	}
}