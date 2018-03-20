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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.core.Messages;
import com.bdaum.zoom.core.db.IDbManager;

public abstract class AbstractDuplicatesProvider  {

	protected boolean ignoreDerivates;
	protected List<List<Asset>> duplicateList;
	protected int hw;
	protected final IDbManager dbManager;
	protected int minSize = 2;

	public AbstractDuplicatesProvider(IDbManager dbManager) {
		this.dbManager = dbManager;
	}

	public void setIgnoreDerivates(boolean ignoreDerivates) {
		this.ignoreDerivates = ignoreDerivates;
	}

	public void findDuplicates(IProgressMonitor monitor) {
		monitor.beginTask(
				Messages.AbstractDuplicatesProvider_searching_duplicates,
				IProgressMonitor.UNKNOWN);
		hw = 0;
	}

	public Asset getAsset(int groupIndex, int index) {
		if (duplicateList != null && groupIndex < duplicateList.size()) {
			List<Asset> parent = duplicateList.get(groupIndex);
			return index < parent.size() ? parent.get(index) : null;
		}
		return null;
	}

	public int getAssetCount(int groupIndex) {
		if (duplicateList == null)
			return 0;
		if (ignoreDerivates) {
			while (hw < groupIndex) {
				List<Asset> parent = duplicateList.get(hw);
				int n = parent.size();
				while (true) {
					Iterator<Asset> it = parent.iterator();
					while (it.hasNext()) {
						Asset asset = it.next();
						List<DerivedByImpl> set = dbManager.obtainStruct(
								DerivedByImpl.class, null, false, "derivative", //$NON-NLS-1$
								asset.getStringId(), false);
						if (!set.isEmpty()) {
							if (--n < minSize)
								break;
							it.remove();
						}
					}
					if (n >= minSize )
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

	public int getDuplicateSetCount() {
		return duplicateList == null ? 0 : duplicateList.size();
	}
	
	public abstract String getLabel();
}