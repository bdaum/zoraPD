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
import java.util.List;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.image.ImageStore;

/**
 * Frontend for core services
 * This interface should not be implemented.
 *
 */
public interface ICore {

	/**
	 * Writes an error message to the system log
	 *
	 * @param message
	 *            - explanation
	 * @param e
	 *            - cause or null
	 */
	void logError(String message, Throwable e);

	/**
	 * Writes an warning message to the system log
	 *
	 * @param message
	 *            - explanation
	 * @param e
	 *            - cause or null
	 */
	void logWarning(String message, Throwable e);

	/**
	 * Writes an info message to the system log
	 *
	 * @param message
	 *            - explanation
	 */
	void logInfo(String message);

	/**
	 * Opens a new catalog
	 *
	 * @param fileName
	 *            - name of the catalog file
	 * @param newDb
	 *            - true if a new catalog is to be created
	 * @return - instance of database manager
	 */
	IDbManager openDatabase(String fileName);
	
	/**
	 * Opens a new catalog
	 *
	 * @param fileName
	 *            - name of the catalog file
	 * @param newDb
	 *            - true if a new catalog is to be created
	 * @param meta
	 *            - existing catalog properties
	 * @return - instance of database manager
	 */
	IDbManager openDatabase(final String fileName, final boolean newDb, final Meta meta);

	/**
	 * Returns the database manager of the currently open catalog or null
	 *
	 * @return database manager
	 */
	IDbManager getDbManager();

	/**
	 * Returns the image cache
	 *
	 * @return image cache
	 */
	ImageStore getImageCache();

	/**
	 * Returns the default asset provider
	 *
	 * @return default asset provider
	 */
	IAssetProvider getAssetProvider();

	/**
	 * Adds a catalog listener
	 *
	 * @param listener
	 *            catalog listener
	 */
	void addCatalogListener(CatalogListener listener);

	/**
	 * Removes a catalog listener
	 *
	 * @param listener
	 *            catalog listener
	 */
	void removeCatalogListener(CatalogListener listener);

	/**
	 * Notifies about asset modifications
	 *
	 * @param changes
	 *            - modified, added or removed assets or null for unspecified changes
	 * @param node
	 *            - affected node or null for multi-node or thumbnail changes
	 */
	void fireAssetsModified(BagChange<Asset> changes, QueryField node);

	/**
	 * Notifies that collection creation rules are to be applied
	 * @param assets  - modified assets or null for all assets
	 * @param node - affected metadata field or null for all rules
	 */
	void fireApplyRules(Collection<? extends Asset> assets, QueryField node);

//	/**
//	 * Notifies about catalog modifications (assets added or removed)
//	 */
//	void fireCatalogModified();

	/**
	 * Notifies about catalog structure modifications (e.g. collections added or
	 * removed)
	 */
	void fireStructureModified();


	/**
	 * Notifies about catalog selections
	 * @param sel - selection
	 * @param forceUpdate - true if selection should inform other views
	 */
	void fireCatalogSelection(IStructuredSelection sel, boolean forceUpdate);


	/**
	 * Notifies about modifications in relationships between assets
	 */
	void fireHierarchyModified();

	/**
	 * Performs an operation
	 *
	 * @param op
	 *            - the the operation to perform
	 * @param monitor
	 *            - progress monitor
	 * @param info
	 *            - adaptable. Must at least answer Shell.class
	 * @return - status of the operation execution
	 */
	IStatus performOperation(IUndoableOperation op, IProgressMonitor monitor,
			IAdaptable info);

	/**
	 * Returns an instance of the volume manager
	 *
	 * @return - volume manager
	 */
	IVolumeManager getVolumeManager();

	/**
	 * Returns true if application is running in networked mode
	 *
	 * @return true if application is running in networked mode
	 */
	boolean isNetworked();

	/**
	 * Returns the peer service if the application is running in networked mode
	 *
	 * @return peer service or null
	 */
	IPeerService getPeerService();

	/**
	 * Returns an error handler for core errors
	 *
	 * @return - error handler instance or null
	 */
	public IDbErrorHandler getErrorHandler();

	/**
	 * Returns an instance of the database factory
	 *
	 * @return - instance of the database factory
	 */
	public abstract IDbFactory getDbFactory();

	/**
	 * Tests a list of assets contains raw images
	 *
	 * @param assets
	 *            - assets to be checked
	 * @param includeDng
	 * @return - true if a raw image was found
	 */
	boolean containsRawImage(List<Asset> assets, boolean includeDng);


}