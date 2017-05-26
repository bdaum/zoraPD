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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;

public class FileNameDuplicatesProvider extends AbstractDuplicatesProvider {

	private final boolean withExtension;

	public FileNameDuplicatesProvider(IDbManager dbManager,
			boolean withExtension) {
		super(dbManager);
		this.withExtension = withExtension;
	}

	@Override
	public void findDuplicates(IProgressMonitor monitor) {
		super.findDuplicates(monitor);
		Map<String, List<Asset>> assetMap = new HashMap<String, List<Asset>>();
		for (AssetImpl asset : dbManager.obtainAssets()) {
			String name = Core.getFileName(asset.getUri(), withExtension);
			List<Asset> duplicate = assetMap.get(name);
			if (duplicate == null) {
				duplicate = new LinkedList<Asset>();
				assetMap.put(name, duplicate);
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
				String n1 = Core.getFileName(o1.get(0).getUri(), withExtension);
				String n2 = Core.getFileName(o2.get(0).getUri(), withExtension);
				return (n1 == n2) ? 0 : (n1 == null) ? -1 : (n2 == null) ? 1
						: n1.compareTo(n2);
			}
		});
		monitor.done();
	}

	@Override
	public String getLabel() {
		return Messages.FileNameDuplicatesProvider_duplicates_file_name;
	}
}
