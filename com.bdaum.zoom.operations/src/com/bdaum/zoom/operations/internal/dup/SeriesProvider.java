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
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Messages;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbManager;

public class SeriesProvider extends AbstractDuplicatesProvider {

	private final SmartCollection coll;
	private final long interval;
	private final boolean separateFormats;
	private int type;

	public SeriesProvider(IDbManager dbManager, SmartCollection coll,
			int interval, int minLength, boolean separateFormats, int type) {
		super(dbManager);
		this.coll = coll;
		this.separateFormats = separateFormats;
		this.type = type;
		this.interval = interval * 1000;
		this.minSize = minLength;
	}

	public static boolean equalDistance(Asset asset, Asset previous) {
		double previousSubjectDistance = previous.getSubjectDistance();
		double subjectDistance = asset.getSubjectDistance();
		if (Double.isNaN(previousSubjectDistance)
				|| Double.isNaN(subjectDistance)) {
			double[] previousDof = previous.getDof();
			double[] dof = asset.getDof();
			if (previousDof == null || previousDof.length < 2 || dof == null
					|| dof.length < 2)
				return false;
			return Math.abs(previousDof[0] - dof[0]) < previousDof[0] * 0.1
					&& Math.abs(previousDof[1] - dof[1]) < previousDof[1] * 0.1;
		}
		return Math.abs(previousSubjectDistance - subjectDistance) < previousSubjectDistance * 0.1;
	}

	@Override
	public void findDuplicates(IProgressMonitor monitor) {
		monitor.beginTask(Messages.SeriesProvider_searching_series,
				IProgressMonitor.UNKNOWN);
		hw = 0;
		duplicateList = new ArrayList<List<Asset>>(100);
		ICollectionProcessor processor = dbManager
				.createCollectionProcessor(coll);
		List<Asset> set = processor.select(true);
		List<Asset> series = new ArrayList<Asset>(5);
		long lastTime = -86400000L;
		String lastFormat = null;
		for (Asset asset : set) {
			double exposureTime = asset.getExposureTime();
			double fNumber = asset.getFNumber();
			double focalLength = asset.getFocalLength();
			int[] isoSpeedRatings = asset.getIsoSpeedRatings();
			if (Double.isNaN(exposureTime) || Double.isNaN(fNumber)
					|| Double.isNaN(focalLength) || isoSpeedRatings == null
					|| isoSpeedRatings.length == 0)
				continue;
			Date dateCreated = asset.getDateCreated();
			if (dateCreated == null)
				dateCreated = asset.getDateTimeOriginal();
			long time = dateCreated.getTime();
			String format = asset.getFormat();
			if (time > lastTime + interval
					|| (separateFormats && !(lastFormat == format || (lastFormat != null && lastFormat
							.equals(format))))
					|| !checkSeriesType(type, series, asset)) {
				if (series.size() >= minSize) {
					duplicateList.add(series);
					series = new ArrayList<Asset>(5);
				} else
					series.clear();
			}
			series.add(asset);
			lastTime = time;
			lastFormat = format;
			if (monitor.isCanceled())
				return;
		}
		if (series.size() >= minSize)
			duplicateList.add(series);
		monitor.done();
	}

	private boolean checkSeriesType(int type, List<Asset> series, Asset asset) {
		if (series.isEmpty())
			return true;
		switch (type) {
		case Constants.SERIES_ALL:
		case Constants.SERIES_RAPID:
			return true;
		case Constants.SERIES_EXP_BRACKET:
			if (Double.isNaN(asset.getLv()))
				return false;
			Asset previous = series.get(0);
			if (!equalDistance(asset, previous)
					|| previous.getFocalLength() != asset.getFocalLength())
				return false;
			double lv = asset.getLv();
			for (Asset a : series)
				if (a.getLv() == lv)
					return false;
			return true;
		case Constants.SERIES_FOCUS_BRACKET:
			previous = series.get(0);
			if (previous.getFocalLength() != asset.getFocalLength()
					|| previous.getLv() != asset.getLv())
				return false;
			for (Asset a : series)
				if (equalDistance(asset, a))
					return false;
			return true;
		case Constants.SERIES_ZOOM_BRACKET:
			if (Double.isNaN(asset.getFocalLength()))
				return false;
			previous = series.get(0);
			if (!equalDistance(asset, previous)
					|| previous.getLv() != asset.getLv())
				return false;
			double zoom = asset.getFocalLength();
			for (Asset a : series)
				if (a.getFocalLength() == zoom)
					return false;
			return true;
		default:
			return !checkSeriesType(Constants.SERIES_EXP_BRACKET, series, asset)
					&& !checkSeriesType(Constants.SERIES_FOCUS_BRACKET, series,
							asset)
					&& !checkSeriesType(Constants.SERIES_ZOOM_BRACKET, series,
							asset)
					&& !checkSeriesType(Constants.SERIES_RAPID, series, asset);
		}
	}

	@Override
	public String getLabel() {
		return Messages.SeriesProvider_series;
	}
}
