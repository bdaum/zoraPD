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

package com.bdaum.zoom.operations.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

public class ManageKeywordsOperation extends DbOperation {

	private List<String> newMetaKeywords = new ArrayList<String>();
	private List<AssetImpl> assets;
	private String[][] oldKeywords;
	private final BagChange<String> bagChange;

	@SuppressWarnings("unchecked")
	public ManageKeywordsOperation(BagChange<String> bagChange, List<? extends Asset> selectedAssets) {
		super(Messages.getString("ManageKeywordsOperation.update_keywords")); //$NON-NLS-1$
		this.bagChange = bagChange;
		this.assets = (List<AssetImpl>) selectedAssets;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		if (assets == null) {
			String kw = bagChange.getDisplay()[0];
			assets = dbManager.obtainObjects(AssetImpl.class, QueryField.IPTC_KEYWORDS.getKey(), kw, QueryField.EQUALS);
		}
		if (oldKeywords == null)
			oldKeywords = new String[assets.size()][];
		init(aMonitor, assets.size() + 1);
		saveOldKeywords();
		aMonitor.worked(1);
		List<Asset> result = setKeywords();
		fireAssetsModified(new BagChange<>(null, result, null, null), QueryField.IPTC_KEYWORDS);
		return close(info, assets);
	}

	private List<Asset> setKeywords() {
		List<Asset> result = new ArrayList<>(assets.size());
		newMetaKeywords.clear();
		Meta meta = dbManager.getMeta(true);
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				Set<Object> kwset = new HashSet<Object>(Arrays.asList(asset.getKeyword()));
				bagChange.update(kwset);
				String[] newKeywords = new String[kwset.size()];
				int i = 0;
				for (Object kw : kwset)
					newKeywords[i++] = String.valueOf(kw);
				Arrays.sort(newKeywords);
				asset.setKeyword(newKeywords);
				Set<String> added = bagChange.getAdded();
				result.add(asset);
				if (added != null && !added.isEmpty() && meta.getKeywords().addAll(added))
					storeSafely(null, 1, asset, meta);
				else
					storeSafely(null, 1, asset);
			}
		}
		return result;
	}

	private void saveOldKeywords() {
		int i = 0;
		for (Asset asset : assets)
			oldKeywords[i++] = asset.getKeyword();
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		List<Asset> result = setKeywords();
		fireAssetsModified(new BagChange<>(null, result, null, null), QueryField.IPTC_KEYWORDS);
		return close(info, assets);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		initUndo(aMonitor, assets.size());
		List<Object> toBeStored = new ArrayList<>(assets.size() + 1);
		List<Asset> result = new ArrayList<>(assets.size());
		int i = 0;
		for (Asset asset : assets)
			if (asset.getFileState() != IVolumeManager.PEER) {
				asset.setKeyword(oldKeywords[i]);
				toBeStored.add(asset);
				result.add(asset);
			}
		Meta meta = dbManager.getMeta(true);
		meta.getKeywords().removeAll(newMetaKeywords);
		toBeStored.add(meta);
		if (storeSafely(null, assets.size(), toBeStored))
			fireAssetsModified(new BagChange<>(null, result, null, null), QueryField.IPTC_KEYWORDS);
		return close(info, assets);
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public int getPriority() {
		return assets.size() > 3 ? Job.LONG : Job.SHORT;
	}

}
