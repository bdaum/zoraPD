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

package com.bdaum.zoom.db.internal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.ListenerList;

import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.IPostProcessor2;
import com.bdaum.zoom.core.db.IColorCodeFilter;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.db.IDbListener;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.db.IRatingFilter;
import com.bdaum.zoom.core.db.ITypeFilter;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.lire.ILireService;
import com.bdaum.zoom.core.internal.lucene.ILuceneService;
import com.bdaum.zoom.core.internal.peer.IPeerService;

@SuppressWarnings("restriction")
public class DbFactory implements IDbFactory, IDbListener {


	private static Map<String, Float> toleranceMap = new HashMap<String, Float>(
			50);

	private IDbErrorHandler errorHandler;
	private int maxImports;
	private IPeerService peerService;
	private boolean peerServiceInitialized;
	private ListenerList<IDbListener> dbListeners = new ListenerList<IDbListener>();
	private IPostProcessor2[] autoColoringPostProcessors;

	private ILireService lireService;

	private ILuceneService luceneService;


	public void activate() {
		DbActivator.getDefault().logInfo(Messages.DbFactory_db_service_started);
	}

	public void deactivate() {
		// do nothing
	}

	public DbFactory() {
		addDbListener(this);
	}

	public void addDbListener(IDbListener listener) {
		dbListeners.add(listener);
	}

	public void removeDbListener(IDbListener listener) {
		dbListeners.remove(listener);
	}

	public Object[] getListeners() {
		return dbListeners.getListeners();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbFactory#createDbManager(java.lang.String,
	 * boolean, boolean)
	 */
	public IDbManager createDbManager(String fileName, boolean newDb,
			boolean readOnly, boolean primary) {
		if (!newDb)
			for (Object listener : dbListeners.getListeners()) {
				IDbManager foreignDb = ((IDbListener) listener)
						.databaseAboutToOpen(fileName, primary);
				if (foreignDb != null) {
					foreignDb.setReadOnly(foreignDb.isReadOnly() && readOnly);
					return foreignDb;
				}
			}
		IDbManager db = new DbManager(this, fileName, newDb, readOnly);
		for (Object listener : dbListeners.getListeners())
			((IDbListener) listener).databaseOpened(db, primary);
		return db;
	}

	public IDbManager databaseAboutToOpen(String filename, boolean primary) {
		return null;
	}

	public void databaseOpened(IDbManager manager, boolean primary) {
		// do nothing
	}

	public boolean databaseAboutToClose(IDbManager manager) {
		return true;
	}

	public void databaseClosed(IDbManager manager, int mode) {
		File indexPath = manager.getIndexPath();
		if (indexPath != null && luceneService != null)
			luceneService.invalidateAllReaders(indexPath);
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbFactory#setTolerances(java.lang.String)
	 */
	public void setTolerances(String prefs) {
		toleranceMap.clear();
		StringTokenizer st = new StringTokenizer(prefs, "\n"); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String key = st.nextToken();
			int p = key.lastIndexOf("="); //$NON-NLS-1$
			if (p > 0) {
				try {
					toleranceMap.put(key.substring(0, p),
							Float.parseFloat(key.substring(p + 1)));
				} catch (NumberFormatException e) {
					// ignore
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbFactory#getTolerance(java.lang.String)
	 */
	public float getTolerance(String field) {
		Float f = toleranceMap.get(field);
		return f == null ? 0f : f;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.db.IDbFactory#setErrorHandler(com.bdaum.zoom.core
	 * .db.IDbErrorHandler)
	 */
	public void setErrorHandler(IDbErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbFactory#getErrorHandler()
	 */
	public IDbErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbFactory#createRatingFilter(int)
	 */
	public IRatingFilter createRatingFilter(int rating) {
		return new RatingFilter(rating);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbFactory#createColorCodeFilter(int)
	 */
	public IColorCodeFilter createColorCodeFilter(int colorCode) {
		return new ColorCodeFilter(colorCode);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbFactory#createTypeAndRatingFilter(int,
	 * int)
	 */
	public ITypeFilter createTypeFilter(int formats) {
		return new TypeFilter(formats);
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbFactory#setMaxImports(int)
	 */
	public void setMaxImports(int maxImports) {
		this.maxImports = maxImports;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbFactory#getMaxImports()
	 */
	public int getMaxImports() {
		return maxImports;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbFactory#getPeerService()
	 */
	public IPeerService getPeerService() {
		if (!peerServiceInitialized) {
			peerServiceInitialized = true;
			peerService = DbActivator.getDefault().getPeerService();
		}
		return peerService;
	}

	@Override
	public int getLireServiceVersion() {
		return DbActivator.getDefault().getLireServiceVersion();
	}
	
	/* (nicht-Javadoc)
	 * @see com.bdaum.zoom.core.db.IDbFactory#getLireService()
	 */
	public ILireService getLireService(boolean activate) {
		if (lireService == null && activate) {
			lireService = DbActivator.getDefault().getLireService();
			lireService.configureCBIR(CoreActivator.getDefault().getCbirAlgorithms());
		}
		return lireService;
	}

	/* (nicht-Javadoc)
	 * @see com.bdaum.zoom.core.db.IDbFactory#getLireService()
	 */
	public ILuceneService getLuceneService() {
		if (luceneService == null) {
			luceneService = DbActivator.getDefault().getLuceneService();
			luceneService.configureTextIndex(CoreActivator.getDefault().getIndexedTextFields());
		}
		return luceneService;
	}


	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.db.IDbFactory#setAutoColoringProcessors(com.bdaum
	 * .zoom.core.IPostProcessor2[])
	 */
	public void setAutoColoringProcessors(
			IPostProcessor2[] autoColoringPostProcessors) {
		this.autoColoringPostProcessors = autoColoringPostProcessors;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.db.IDbFactory#getAutoColoringProcessors()
	 */
	public IPostProcessor2[] getAutoColoringProcessors() {
		return autoColoringPostProcessors;
	}

	public IPostProcessor2 createQueryPostProcessor(SmartCollection sm) {
		QueryPostProcessor processor = new QueryPostProcessor(this);
		processor.setSmartCollection_parent(sm);
		return processor;
	}


}
