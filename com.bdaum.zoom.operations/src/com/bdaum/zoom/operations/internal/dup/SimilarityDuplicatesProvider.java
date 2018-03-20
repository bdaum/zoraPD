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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;

public class SimilarityDuplicatesProvider extends AbstractDuplicatesProvider {

	private final int method;
	protected List<List<String>> duplicateList; // must mask superclass field

	public SimilarityDuplicatesProvider(IDbManager dbManager, int method) {
		super(dbManager);
		this.method = method;
	}

	@Override
	public AssetImpl getAsset(int groupIndex, int index) {
		if (duplicateList != null && groupIndex < duplicateList.size()) {
			List<String> parent = duplicateList.get(groupIndex);
			String docId = index < parent.size() ? parent.get(index) : null;
			if (docId != null)
				return dbManager.obtainAsset(docId);
		}
		return null;
	}

	@Override
	public int getAssetCount(int groupIndex) {
		if (duplicateList == null)
			return 0;
		if (ignoreDerivates) {
			while (groupIndex >= hw) {
				List<String> parent = duplicateList.get(hw);
				int n = parent.size();
				while (true) {
					Iterator<String> it = parent.iterator();
					while (it.hasNext()) {
						String id = it.next();
						List<DerivedByImpl> set = dbManager.obtainStruct(
								DerivedByImpl.class, null, false, "derivative", //$NON-NLS-1$
								id, false);
						if (!set.isEmpty()) {
							if (--n < 2)
								break;
							it.remove();
						}
					}
					if (n >= 2)
						break;
					duplicateList.remove(hw);
					if (groupIndex >= duplicateList.size())
						return -1;
					parent = duplicateList.get(hw);
					n = parent.size();
				}
				++hw;
			}
		}
		return duplicateList.get(groupIndex).size();
	}

	@Override
	public int getDuplicateSetCount() {
		return duplicateList == null ? 0 : duplicateList.size();
	}

	@Override
	public void findDuplicates(IProgressMonitor monitor) {
		super.findDuplicates(monitor);
		File indexPath = dbManager.getIndexPath();
		if (indexPath != null) {
			duplicateList = Core.getCore().getDbFactory().getLireService(true).findDuplicates(indexPath, method, monitor);
			if (duplicateList == null)
				return;
		}
		monitor.done();
	}

	@Override
	public String getLabel() {
		return Messages.SimilarityDuplicatesProvider_duplicates_similar;
	}

}
