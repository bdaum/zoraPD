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
package com.bdaum.zoom.operations.internal.dup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.lire.ILireService;

public class CombinedDuplicatesProvider extends AbstractDuplicatesProvider {

	private final int method;

	public CombinedDuplicatesProvider(IDbManager dbManager, int method) {
		super(dbManager);
		this.method = method;
	}

	@Override
	public void findDuplicates(IProgressMonitor monitor) {
		super.findDuplicates(monitor);
		File indexPath = dbManager.getIndexPath();
		if (indexPath != null) {
			ILireService lire = Core.getCore().getDbFactory().getLireService(true);
			List<List<String>> duplicateIds = lire.findDuplicates(indexPath,
					method, monitor);
			if (duplicateIds == null)
				return;
			int l = duplicateIds.size();
			duplicateList = new ArrayList<List<Asset>>(l);
			for (List<String> duplicate : duplicateIds) {
				Map<ExposureData, List<Asset>> map = new HashMap<ExposureData, List<Asset>>(
						duplicate.size() * 3 / 2);
				for (String id : duplicate) {
					AssetImpl asset = dbManager.obtainAsset(id);
					if (asset != null) {
						Date dateCreated = asset.getDateCreated();
						if (dateCreated == null)
							dateCreated = asset.getDateTimeOriginal();
						ExposureData exposureData = new ExposureData(
								dateCreated, asset.getExposureTime(),
								asset.getModel(), asset.getMake(),
								asset.getFNumber(), asset.getFlashFired(),
								asset.getFocalLength(),
								asset.getIsoSpeedRatings());
						List<Asset> dup = map.get(exposureData);
						if (dup == null) {
							dup = new LinkedList<Asset>();
							map.put(exposureData, dup);
						}
						dup.add(asset);
					}
				}
				if (monitor.isCanceled())
					return;
				for (List<Asset> dup : map.values()) {
					if (dup.size() > 1)
						duplicateList.add(dup);
				}
				if (monitor.isCanceled())
					return;
			}
			Collections.sort(duplicateList, new Comparator<List<Asset>>() {

				public int compare(List<Asset> o1, List<Asset> o2) {
					Date d1 = o1.get(0).getDateTimeOriginal();
					Date d2 = o2.get(0).getDateTimeOriginal();
					if (d1 == d2)
						return 0;
					if (d1 == null)
						return -1;
					if (d2 == null)
						return 1;
					return d1.compareTo(d2);
				}
			});
		}
		monitor.done();
	}

	@Override
	public String getLabel() {
		return Messages.CombinedDuplicatesProvider_duplicates_combined;
	}

}
