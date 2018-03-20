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
 * (c) 2014 Berthold Daum  
 */
package com.bdaum.zoom.core.internal.lire;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Point;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollection;

public interface ILireService {

	String VERSION = "version"; //$NON-NLS-1$

	/**
	 * @return array of all supported similarity algorithms
	 */
	Algorithm[] getSupportedSimilarityAlgorithms();

	/**
	 * Configure content based image retrieval
	 * @param algo - collection of active algorithms
	 */
	void configureCBIR(Collection<String> algo);

	/**
	 * @param algoId - numeric algorithm ID
	 * @return - algorithm with this ID or null
	 */
	Algorithm getAlgorithmById(int algoId);

	/**
	 * @return - indexing job
	 */
	Job createIndexingJob();

	/**
	 * @param assetIds - assets to index
	 * @return - indexing job
	 */
	Job createIndexingJob(String[] assetIds);

	/**
	 * Restore index from backup
	 * @param indexBackup - index backup location
	 * @param lastBackup - date of last backup
	 * @return - indexing job
	 */
	Job createIndexingJob(File indexBackup, Date lastBackup);

	/**
	 * Create job for resuming indexing
	 * @param assets - assets to index
	 * @param reimport - true for reimports
	 * @param totalWork - total work
	 * @param worked - already done
	 * @param system - true for system jobs
	 * @return - indexing job
	 */
	Job createIndexingJob(Collection<Asset> assets, boolean reimport, int totalWork, int worked, boolean system);

	/**
	 * Postpone indexing of unfinished images
	 * @return - set of asset IDs
	 */
	Set<String> postponeIndexing();

	/**
	 * Find duplicates
	 * @param indexPath - location of index 
	 * @param algoId - algorithm ID
	 * @param monitor - progress monitor
	 * @return - list of duplicate lists
	 */
	List<List<String>> findDuplicates(File indexPath, int algoId, IProgressMonitor monitor);

	/**
	 * Updates a query
	 * @param query - the query to update
	 * @param value - new value
	 * @param adaptable - adaptable, must provide Shell instance
	 * @param field - new field name
	 * @return - updated query
	 */
	SmartCollection updateQuery(SmartCollection query, Object value, IAdaptable adaptable, String field);

	/**
	 * @param value - value
	 * @param adaptable - adaptable, must provide Shell instance
	 * @param field - field name
	 */
	void performQuery(Object value, IAdaptable adaptable, String field);

	/**
	 * Show configure search dialog
	 * @param adaptable - adaptable, must provide Shell instance
	 * @param displayLocation - location where to show the dialog
	 * @return - true if successful
	 */
	boolean ShowConfigureSearch(IAdaptable adaptable, Point displayLocation);

}
