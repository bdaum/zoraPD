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

package com.bdaum.zoom.operations.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class ReplaceKeywordOperation extends DbOperation {

	private Map<String, String[]> previousKeywords;
	private final String oldKeyword;
	private final String[] newKeywords;

	public ReplaceKeywordOperation(String oldKeyword, String[] newKeywords) {
		super(Messages.getString("ModifyKeywordOperation.modify_keyword")); //$NON-NLS-1$
		this.oldKeyword = oldKeyword;
		this.newKeywords = newKeywords;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		List<AssetImpl> assets = dbManager.obtainObjects(AssetImpl.class,
				QueryField.IPTC_KEYWORDS.getKey(), oldKeyword,
				QueryField.EQUALS);
		int size = assets.size();
		previousKeywords = new HashMap<String, String[]>(size * 3 / 2);
		init(aMonitor, size * 2);

		List<Asset> toBeStored = new ArrayList<Asset>(assets.size());
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				String[] k = asset.getKeyword();
				previousKeywords.put(asset.getStringId(), k);
				Set<String> keywords = new HashSet<String>(Arrays.asList(k));
				keywords.remove(oldKeyword);
				keywords.addAll(Arrays.asList(newKeywords));
				String[] a = keywords.toArray(new String[keywords.size()]);
				Arrays.sort(a);
				asset.setKeyword(a);
				toBeStored.add(asset);
			}
			aMonitor.worked(1);
		}
		if (storeSafely(null, assets.size(), toBeStored.toArray()))
			fireAssetsModified(new BagChange<>(null, toBeStored, null, null), QueryField.IPTC_KEYWORDS);
		return close(info, toBeStored);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		return execute(aMonitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		int size = previousKeywords.size();
		initUndo(aMonitor, size * 2);
		List<Asset> toBeStored = new ArrayList<Asset>(size);
		for (Entry<String, String[]> entry : previousKeywords.entrySet()) {
			AssetImpl asset = dbManager.obtainAsset(entry.getKey());
			if (asset != null) {
				asset.setKeyword(entry.getValue());
				toBeStored.add(asset);
			}
			aMonitor.worked(1);
		}
		if (storeSafely(null, size, toBeStored.toArray()))
			fireAssetsModified(new BagChange<>(null, toBeStored, null, null), QueryField.IPTC_KEYWORDS);
		return close(info, toBeStored);
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public int getPriority() {
		return Job.LONG;
	}

}
