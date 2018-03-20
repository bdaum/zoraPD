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
import java.util.List;

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

public class ModifyKeywordOperation extends DbOperation {

	private String[] assetIds;
	private boolean[] kwDeleted;
	private final String oldKeyword;
	private final String newKeyword;

	public ModifyKeywordOperation(String oldKeyword, String newKeyword) {
		super(Messages.getString("ModifyKeywordOperation.modify_keyword")); //$NON-NLS-1$
		this.oldKeyword = oldKeyword;
		this.newKeyword = newKeyword;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info)
			throws ExecutionException {
		List<AssetImpl> assets = dbManager.obtainObjects(AssetImpl.class,
				QueryField.IPTC_KEYWORDS.getKey(), oldKeyword,
				QueryField.EQUALS);
		int size = assets.size();
		assetIds = new String[size];
		kwDeleted = new boolean[size];
		init(aMonitor, size * 2);

		List<Asset> toBeStored = new ArrayList<Asset>(assets.size());
		int i = 0;
		for (AssetImpl asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				assetIds[i] = asset.getStringId();
				String[] keywords = asset.getKeyword();
				boolean present = false;
				boolean found = false;
				for (int j = 0; j < keywords.length; j++)
					if (newKeyword.equals(keywords[j])) {
						present = true;
						break;
					}
				for (int j = 0; j < keywords.length; j++) {
					if (oldKeyword.equals(keywords[j])) {
						found = true;
						if (present) {
							String[] newKeywords = new String[keywords.length - 1];
							System.arraycopy(keywords, 0, newKeywords, 0, j);
							System.arraycopy(keywords, j + 1, newKeywords, j,
									newKeywords.length - j);
							keywords = newKeywords;
							kwDeleted[i] = true;
						} else {
							keywords[j] = newKeyword;
							kwDeleted[i] = false;
						}
						asset.setKeyword(keywords);
						toBeStored.add(asset);
						break;
					}
				}
				if (!found)
					assetIds[i] = null;
			}
			aMonitor.worked(1);
			++i;
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
		initUndo(aMonitor, assetIds.length * 2);
		List<Asset> toBeStored = new ArrayList<Asset>(assetIds.length);
		for (int i = 0; i < assetIds.length; i++) {
			if (assetIds[i] != null) {
				AssetImpl asset = dbManager.obtainAsset(assetIds[i]);
				String[] keywords = asset.getKeyword();
				if (kwDeleted[i]) {
					String[] newKeywords = new String[keywords.length + 1];
					System.arraycopy(keywords, 0, newKeywords, 0,
							keywords.length);
					newKeywords[keywords.length] = oldKeyword;
					keywords = newKeywords;
				} else
					for (int j = 0; j < keywords.length; j++)
						if (newKeyword.equals(keywords[j])) {
							keywords[j] = oldKeyword;
							break;
						}
				asset.setKeyword(keywords);
				toBeStored.add(asset);
			}
			aMonitor.worked(1);
		}
		if (storeSafely(null, assetIds.length, toBeStored.toArray()))
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
