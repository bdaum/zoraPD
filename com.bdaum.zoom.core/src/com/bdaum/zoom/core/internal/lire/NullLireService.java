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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Point;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollection;

public class NullLireService implements ILireService {

	private static final Algorithm[] NULLALGORITHMS = new Algorithm[0];

	public Algorithm[] getSupportedSimilarityAlgorithms() {
		return NULLALGORITHMS;
	}

	public void configureCBIR(Collection<String> algo) {
		// do nothing
	}

	public Algorithm getAlgorithmById(int method) {
		return null;
	}

	public Job createIndexingJob() {
		return null;
	}

	public Job createIndexingJob(String[] assetIds) {
		return null;
	}

	public Job createIndexingJob(File indexBackup, Date lastBackup) {
		return null;
	}

	public Job createIndexingJob(Collection<Asset> assets, boolean reimport,
			int totalWork, int worked, boolean system) {
		return null;
	}

	public Set<String> postponeIndexing() {
		return new HashSet<String>(0);
	}

	public List<List<String>> findDuplicates(File indexPath, int method,
			IProgressMonitor monitor) {
		return new ArrayList<List<String>>();
	}

	@Override
	public SmartCollection updateQuery(SmartCollection query, Object value, IAdaptable adaptable, String field) {
		return query;
	}

	@Override
	public void performQuery(Object value, IAdaptable adaptable, String field) {
		// do nothing
	}

	@Override
	public boolean showConfigureSearch(IAdaptable adaptable, Point displayLocation) {
		return false;
	}


}
