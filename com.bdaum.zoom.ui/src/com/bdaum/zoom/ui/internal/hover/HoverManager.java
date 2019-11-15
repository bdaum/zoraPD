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
 * (c) 2019 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.hover;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.ui.internal.TemplateProcessor;
import com.bdaum.zoom.ui.internal.UiActivator;

public class HoverManager {

	private Map<String, IHoverContribution> hoverContributions;
	private TemplateProcessor templateProcessor = new TemplateProcessor(null);

	public String getHoverTitle(String key, Object object, IHoverContext context) {
		IHoverContribution contrib = getHoverContribution(key);
		return contrib != null ? templateProcessor.processTemplate(contrib, object, context, true) : ""; //$NON-NLS-1$
	}

	public String getHoverText(String key, Object object, IHoverContext context) {
		IHoverContribution contrib = getHoverContribution(key);
		return contrib != null ? templateProcessor.processTemplate(contrib, object, context, false) : ""; //$NON-NLS-1$
	}

	public IHoverContribution getHoverContribution(String key) {
		return getHoverContributions().get(key);
	}

	public Map<String, IHoverContribution> getHoverContributions() {
		if (hoverContributions == null) {
			hoverContributions = new HashMap<String, IHoverContribution>();
			for (IExtension extension : Platform.getExtensionRegistry()
					.getExtensionPoint(UiActivator.PLUGIN_ID, "hoverContribution") //$NON-NLS-1$
					.getExtensions())
				for (IConfigurationElement conf : extension.getConfigurationElements()) {
					String id = conf.getAttribute("id"); //$NON-NLS-1$
					try {
						IHoverContribution contrib = (IHoverContribution) conf.createExecutableExtension("class"); //$NON-NLS-1$
						contrib.setId(id);
						hoverContributions.put(id, contrib);
					} catch (CoreException e) {
						UiActivator.getDefault().logError(NLS.bind(Messages.HoverManager_cannot_instatiate, id), e);
					}
				}

		}
		return hoverContributions;
	}

}
