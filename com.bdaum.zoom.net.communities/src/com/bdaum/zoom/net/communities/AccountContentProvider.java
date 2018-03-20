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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

final class AccountContentProvider implements
		ITreeContentProvider {

	private final Map<String, List<CommunityAccount>> accounts;

	public AccountContentProvider(
			Map<String, List<CommunityAccount>> accounts) {
		this.accounts = accounts;
	}

	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}

	
	public void dispose() {
		// do nothing
	}

	
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IExtensionPoint) {
			List<IConfigurationElement> allElements = new ArrayList<IConfigurationElement>();
			IExtension[] extensions = ((IExtensionPoint) inputElement)
					.getExtensions();
			for (IExtension ext : extensions) {
				allElements.addAll(Arrays.asList(ext
						.getConfigurationElements()));
			}
			return allElements.toArray();
		}
		return CommunitiesPreferencePage.EMPTY;
	}

	
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	
	public Object getParent(Object element) {
		if (element instanceof CommunityAccount) {
			return ((CommunityAccount) element).getConfiguration()
					.getParent();
		}
		return null;
	}

	
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IConfigurationElement) {
			IConfigurationElement configElement = (IConfigurationElement) parentElement;
			IConfigurationElement accountConfig = configElement
					.getChildren("account")[0]; //$NON-NLS-1$
			String id = configElement.getAttribute("id"); //$NON-NLS-1$
			List<CommunityAccount> acc = accounts.get(id);
			if (acc == null) {
				acc = CommunityAccount.loadAllAccounts(id, accountConfig);
				accounts.put(id, acc);
			}
			return acc.toArray();
		}
		return CommunitiesPreferencePage.EMPTY;
	}
}