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
import com.bdaum.zoom.core.db.IDbListener;

public interface CatalogListener {

	/*** Closing modi ***/
	int NORMAL = IDbListener.NORMAL;
	int SHUTDOWN = IDbListener.SHUTDOWN;
	int EMERGENCY = IDbListener.EMERGENCY;
	int TASKBAR = IDbListener.TASKBAR;
	int TUNE = IDbListener.TUNE;

	/**
	 * Invoked when a catalog was opened
	 * @param newDb - true if a new catalog is opened
	 */
	void catalogOpened(boolean newDb);

	/**
	 * Invoked when a catalog was closed
	 */
	void catalogClosed(int mode);

//	/**
//	 * Invoked when the catalog content was modified (assets added or removed)
//	 */
//	void catalogModified();

//	/**
//	 * Invoked when assets in the catalog have changed
//	 * @param assets - assets that have changed, null for unspecified
//	 * @param node - asset node that has changed - null for all and/or thumbnail
//	 */
//	void assetsModified(Collection<? extends Asset> assets, QueryField node);

	/**
	 * Invoked when assets in the catalog have changed
	 * @param changes - assets that have been modified, added or removed. Null for unspecified
	 * @param node - asset node that has changed - null for all and/or thumbnail
	 */
	void assetsModified(BagChange<Asset> changes, QueryField node);

	/**
	 * Invoked when auto collection creation rules must be applied to assets
	 * @param assets - assets that have changed, null for unspecified
	 * @param node - asset node that has changed - null for all and/or thumbnail
	 */
	void applyRules(Collection<? extends Asset> assets, QueryField node);

	/**
	 * Invoked when the catalog structure was changed (collections added or removed)
	 */
	void structureModified();

	/**
	 * Invoked when relations between assets have been added, removed, or modified
	 */
	void hierarchyModified();

	/**
	 * Invoked when bookmarks were added or removed
	 */
	void bookmarksModified();

	/**
	 * Invoked to change the selection in the catalog
	 * @param selection - selected object
	 * @param forceUpdate - true, if the selection should cause the update of other views
	 */
	void setCatalogSelection(ISelection selection, boolean forceUpdate);

}
