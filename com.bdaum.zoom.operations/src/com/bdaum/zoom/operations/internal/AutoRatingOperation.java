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
 * (c) 2018 Berthold Daum  
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
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ai.IAiService;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class AutoRatingOperation extends DbOperation {

	private List<Asset> assets;
	private int size;
	private int[] oldRates;
	private String[] oldRatedBy;
	private int[] newRates;
	private String providerName;
	private String providerId;
	private String modelId;
	private boolean overwrite;
	private int maxRating;

	public AutoRatingOperation(List<Asset> assets, String providerId, String modelId, boolean overwrite,
			int maxRating) {
		super(Messages.getString("AutoRatingOperation.rating")); //$NON-NLS-1$
		this.assets = assets;
		this.providerId = providerId;
		this.modelId = modelId;
		this.overwrite = overwrite;
		this.maxRating = maxRating;
		size = assets.size();
		oldRates = new int[size];
		newRates = new int[size];
		oldRatedBy = new String[size];
	}

	public AutoRatingOperation(ImportOperation op, String providerId, String modelId, boolean overwrite,
			int maxRating) {
		this(op.obtainImportedAssets(), providerId, modelId, overwrite, maxRating);
	}

	@Override
	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		init(aMonitor, size);
		IAiService aiService = CoreActivator.getDefault().getAiService();
		if (aiService != null) {
			String[] ids = aiService.getRatingProviderIds();
			String[] names = aiService.getRatingProviderNames();
			for (int i = 0; i < ids.length; i++) {
				if (ids[i].equals(providerId)) {
					providerName = names[i];
					break;
				}
			}
			int i = 0;
			for (Asset asset : assets) {
				oldRates[i] = asset.getRating();
				oldRatedBy[i] = asset.getRatedBy();
				if (asset.getRating() < 0 || overwrite) {
					int rating = aiService.rate(asset, opId, maxRating, modelId, providerId);
					if (rating >= 0) {
						asset.setRating(rating);
						asset.setRatedBy(providerName);
						if (!storeSafely(null, 0, asset))
							break;
					}
				}
				++i;
				aMonitor.worked(1);
			}
			fireApplyRules(assets, QueryField.RATEDBY);
			fireAssetsModified(new BagChange<>(null, assets, null, null), QueryField.RATING);
		}
		return close(info);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		init(aMonitor, size);
		List<Object> toBeStored = new ArrayList<Object>(assets.size());
		int i = 0;
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				asset.setRating(newRates[i]);
				asset.setRatedBy(providerName);
				toBeStored.add(asset);
			}
			++i;
		}
		if (storeSafely(null, 1, toBeStored))
			fireAssetsModified(new BagChange<>(null, assets, null, null), QueryField.RATING);
		return close(info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		initUndo(aMonitor, size);
		List<Object> toBeStored = new ArrayList<Object>(assets.size());
		int i = 0;
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				asset.setRating(oldRates[i]);
				asset.setRatedBy(oldRatedBy[i]);
				toBeStored.add(asset);
			}
			++i;
		}
		if (storeSafely(null, 1, toBeStored))
			fireAssetsModified(new BagChange<>(null, assets, null, null), QueryField.RATING);
		return close(info);
	}

	@Override
	public int getPriority() {
		return size > 3 ? Job.LONG : Job.SHORT;
	}

}
