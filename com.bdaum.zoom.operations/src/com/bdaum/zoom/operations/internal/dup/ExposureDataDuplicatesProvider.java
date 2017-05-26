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
package com.bdaum.zoom.operations.internal.dup;

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
import com.bdaum.zoom.core.db.IDbManager;

public class ExposureDataDuplicatesProvider extends AbstractDuplicatesProvider {

	public ExposureDataDuplicatesProvider(IDbManager dbManager) {
		super(dbManager);
	}


	@Override
	public void findDuplicates(IProgressMonitor monitor) {
		super.findDuplicates(monitor);
		Map<ExposureData, List<Asset>> assetMap = new HashMap<ExposureData, List<Asset>>();
		for (AssetImpl asset : dbManager.obtainAssets()) {
			Date dateCreated = asset.getDateCreated();
			if (dateCreated == null)
				dateCreated = asset.getDateTimeOriginal();
			if (dateCreated == null || dateCreated.getTime() == 0)
				continue;
			double exposureTime = asset.getExposureTime();
			double fNumber = asset.getFNumber();
			double focalLength = asset.getFocalLength();
			int[] isoSpeedRatings = asset.getIsoSpeedRatings();
			if (Double.isNaN(exposureTime) || Double.isNaN(fNumber)
					|| Double.isNaN(focalLength) || isoSpeedRatings == null
					|| isoSpeedRatings.length == 0)
				continue;
			ExposureData exposureData = new ExposureData(dateCreated,
					exposureTime, asset.getModel(), asset.getMake(), fNumber,
					asset.getFlashFired(), focalLength, isoSpeedRatings);
			List<Asset> duplicate = assetMap.get(exposureData);
			if (duplicate == null) {
				duplicate = new LinkedList<Asset>();
				assetMap.put(exposureData, duplicate);
			}
			duplicate.add(asset);
			if (monitor.isCanceled())
				return;
		}
		duplicateList = new ArrayList<List<Asset>>(assetMap.size());
		for (List<Asset> dup : assetMap.values())
			if (dup.size() > 1)
				duplicateList.add(dup);
		if (monitor.isCanceled())
			return;
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

		monitor.done();
	}


	@Override
	public String getLabel() {
		return Messages.ExposureDataDuplicatesProvider_duplicates_exif;
	}
}
