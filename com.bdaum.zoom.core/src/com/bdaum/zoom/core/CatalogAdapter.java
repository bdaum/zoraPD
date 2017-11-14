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

package com.bdaum.zoom.core;

import java.util.Collection;

import org.eclipse.jface.viewers.ISelection;

import com.bdaum.zoom.cat.model.asset.Asset;

public class CatalogAdapter implements CatalogListener {

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.CatalogListener#assetsModified(java.util.List,
	 * com.bdaum.zoom.core.QueryField)
	 */

	public void assetsModified(BagChange<Asset> changes, QueryField node) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.CatalogListener#catalogOpened()
	 */

	public void catalogOpened(boolean newDb) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.CatalogListener#catalogClosed()
	 */

	public void catalogClosed(int mode) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.CatalogListener#structureModified()
	 */

	public void structureModified() {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.CatalogListener#hierarchyModified()
	 */

	public void hierarchyModified() {
		// do nothing
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see com.bdaum.zoom.core.CatalogListener#catalogModified()
	// */
	//
	// public void catalogModified() {
	// // do nothing
	// }

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.CatalogListener#bookmarksModified()
	 */

	public void bookmarksModified() {
		// do nothing
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see
	 * com.bdaum.zoom.core.CatalogListener#setCatalogSelection(org.eclipse.jface
	 * .viewers.ISelection, boolean)
	 */
	public void setCatalogSelection(ISelection selection, boolean forceUpdate) {
		// do nothing
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.core.CatalogListener#applyRules(java.util.Collection,
	 * com.bdaum.zoom.core.QueryField)
	 */
	public void applyRules(Collection<? extends Asset> assets, QueryField node) {
		// do nothing
	}

}
