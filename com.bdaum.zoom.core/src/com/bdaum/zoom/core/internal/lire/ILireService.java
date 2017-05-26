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
 * (c) 2014 Berthold Daum  (berthold.daum@bdaum.de)
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
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;

public interface ILireService {

	String VERSION = "version"; //$NON-NLS-1$

	Algorithm[] getSupportedSimilarityAlgorithms();

	void configureCBIR(Collection<String> algo);

	Algorithm getAlgorithmById(int method);

	Job createIndexingJob();

	Job createIndexingJob(String[] assetIds);

	Job createIndexingJob(File indexBackup, Date lastBackup);

	Job createIndexingJob(Collection<Asset> assets, boolean reimport, int totalWork, int worked, boolean system);

	Set<String> postponeIndexing();

	List<List<String>> findDuplicates(File indexPath, int method, IProgressMonitor monitor);

	SmartCollectionImpl updateQuery(SmartCollectionImpl current, Object value, IAdaptable adaptable, String kind);

	void performQuery(Object value, IAdaptable adaptable, String kind);

	boolean configureSearch(IAdaptable adaptable, Point displayLocation);

}
