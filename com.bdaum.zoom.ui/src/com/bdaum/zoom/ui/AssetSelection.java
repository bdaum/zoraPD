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
 * (c) 2009-2014 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;

/**
 * A selection of asset instances
 *
 */
@SuppressWarnings("restriction")
public class AssetSelection extends StructuredSelection implements
		Iterable<Asset> {

	private static final Object[] DUMMY = new Object[] { AssetSelection.class };

	/**
	 * Empty selection
	 */
	public static final AssetSelection EMPTY = new AssetSelection(0);

	private static final String[] EMPTYSTRING = new String[0];
	private List<Asset> assets;
	private HashSet<Asset> assetSet;
	private IAssetProvider assetProvider;
	private boolean picked = false;
	private int mediaFlags = 0;

	/**
	 * Creates a new asset selection obtaining its assets from the supplied
	 * assetProvider
	 *
	 * @param assetProvider
	 *            - Asset provider
	 */
	public AssetSelection(IAssetProvider assetProvider) {
		super(DUMMY);
		this.assetProvider = assetProvider;
	}

	/**
	 * Creates a new asset selection
	 *
	 * @param assets
	 *            - selected assets
	 */
	public AssetSelection(List<Asset> assets) {
		super(DUMMY);
		this.assets = assets;
		picked = true;
	}

	/**
	 * Creates an empty asset selection
	 *
	 * @param initialSize
	 *            - expected size
	 */
	public AssetSelection(int initialSize) {
		super(DUMMY);
		this.assets = new ArrayList<Asset>(initialSize);
		picked = true;
	}

	/**
	 * Creates an asset selection with a single asset
	 *
	 * @param asset
	 *            - selected asset
	 */
	public AssetSelection(Asset asset) {
		super(DUMMY);
		this.assets = new ArrayList<Asset>(1);
		this.assets.add(asset);
		picked = true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jface.viewers.StructuredSelection#equals(java.lang.Object)
	 */

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof AssetSelection) {
			AssetSelection other = (AssetSelection) obj;
			if (!picked)
				return assetProvider == other.assetProvider;
			if (!other.picked)
				return false;
			List<Asset> otherAssets = other.assets;
			if (assets.size() != otherAssets.size())
				return false;
			return assets.equals(otherAssets);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */

	@Override
	public int hashCode() {
		return picked ? assets.hashCode() : assetProvider.hashCode();
	}

	/**
	 * Tests if the selection contains at least one of the supplied candidates
	 *
	 * @param candidates
	 *            - supplied candidates
	 * @return - true if the selection contains at least one of the supplied
	 *         candidates
	 */
	public boolean containsAssets(Collection<? extends Asset> candidates) {
		if (candidates == null || candidates.isEmpty())
			return false;
		if (!picked)
			return true;
		if (assets.isEmpty())
			return false;
		if (assets.size() == 1)
			return candidates.contains(assets.get(0));
		if (assetSet != null || candidates.size() > 1) {
			HashSet<Asset> set = getAssetSet();
			for (Asset cand : candidates)
				if (set.contains(cand))
					return true;
			return false;
		}
		for (Asset a : assets)
			if (candidates.contains(a))
				return true;
		return false;
	}

	/**
	 * Return selected assets as a Set
	 *
	 * @return - selected assets
	 */
	private HashSet<Asset> getAssetSet() {
		if (assetSet == null)
			assetSet = new HashSet<Asset>(getAssets());
		return assetSet;
	}

	/**
	 * Return selected assets as a List
	 *
	 * @return - selected assets
	 */
	public List<Asset> getAssets() {
		if (assets == null)
			// Copy to new list for resolving DB proxies
			assets = new ArrayList<Asset>(assetProvider.getAssets());
		return assets;
	}

	/**
	 * Returns a list of assets not owned by a peer or null
	 *
	 * @return list of assets not owned by a peer or null
	 */
	public List<Asset> getLocalAssets() {
		if (Core.getCore().isNetworked()) {
			if (!picked)
				return assetProvider.getLocalAssets();
			List<Asset> localAssets = new ArrayList<Asset>(assets.size());
			for (Asset a : assets)
				if (a.getFileState() != IVolumeManager.PEER)
					localAssets.add(a);
			return localAssets;
		}
		return getAssets();
	}

	/**
	 * Return IDs of selected assets
	 *
	 * @return IDs of selected assets
	 */
	public String[] getAssetIds() {
		getAssets();
		int i = 0;
		String[] ids = new String[size()];
		for (Asset asset : assets)
			ids[i++] = asset.getStringId();
		return ids;
	}

	/**
	 * Return IDs of selected assets not owned by a peer
	 *
	 * @return IDs of selected assets not owned by a peer
	 */
	public String[] getLocalAssetIds() {
		List<Asset> localAssets = getLocalAssets();
		if (localAssets == null)
			return EMPTYSTRING;
		int i = 0;
		String[] ids = new String[size()];
		for (Asset asset : localAssets)
			ids[i++] = asset.getStringId();
		return ids;
	}

	/**
	 * Test if the supplied asset is selected
	 *
	 * @param asset
	 *            to be tested
	 * @return true if the supplied asset is selected
	 */
	public boolean isSelected(Asset asset) {
		if (asset == null || !picked)
			return true;
		if (assets.isEmpty())
			return false;
		if (assets.size() == 1)
			return assets.get(0).equals(asset);
		return getAssetSet().contains(asset);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.StructuredSelection#isEmpty()
	 */

	@Override
	public boolean isEmpty() {
		if (assets != null)
			return assets.isEmpty();
		return assetProvider.isEmpty();
//		return size() == 0;
	}

	/**
	 * Returns the first asset not owned by a peer
	 *
	 * @return - the first asset not owned by a peer
	 */
	public Asset getFirstLocalAsset() {
		if (assets == null)
			return assetProvider.getFirstLocalAsset();
		for (Asset a : assets)
			if (a.getFileState() != IVolumeManager.PEER)
				return a;
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.StructuredSelection#size()
	 */

	@Override
	public int size() {
		return getAssets().size();
	}

	/**
	 * Adds another asset to the selection
	 *
	 * @param asset
	 *            - additional selected asset
	 */
	public void add(Asset asset) {
		if (assets != null)
			assets.add(asset);
	}

	/**
	 * Returns the i-th asset in the selection
	 *
	 * @param i
	 *            - index
	 * @return i-th asset in the selection
	 */
	public Asset get(int i) {
		return assets == null ? assetProvider.getAsset(i) : assets.get(i);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.StructuredSelection#toArray()
	 *
	 * @deprecated
	 */

	@Override
	public Asset[] toArray() {
		getAssets();
		return assets.toArray(new Asset[assets.size()]);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.StructuredSelection#getFirstElement()
	 */

	@Override
	public Asset getFirstElement() {
		getAssets();
		return (assets.isEmpty()) ? null : assets.get(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.StructuredSelection#iterator()
	 */

	@Override
	public Iterator<Asset> iterator() {
		return getAssets().iterator();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.StructuredSelection#toList()
	 */

	@Override
	public List<Asset> toList() {
		return getAssets();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.viewers.StructuredSelection#toString()
	 */

	@Override
	public String toString() {
		return picked ? String.valueOf(assets) : "all"; //$NON-NLS-1$
	}

	/**
	 * Returns if the selection list individual assets or is a "Select all"
	 * selection
	 *
	 * @return - true if the selection list individual assets
	 */
	public boolean isPicked() {
		return picked;
	}

	/**
	 * Returns the combination of IMediaSupport flags for all assets in the
	 * selection
	 *
	 * @return media flags
	 */
	public int getMediaFlags() {
		if (mediaFlags == 0) {
			CoreActivator activator = CoreActivator.getDefault();
			for (Asset asset : getAssets()) {
				IMediaSupport mediaSupport = activator.getMediaSupport(asset
						.getFormat());
				if (mediaSupport != null)
					mediaFlags |= mediaSupport.getPropertyFlags();
			}
			if (mediaFlags == 0)
				mediaFlags = QueryField.PHOTO;
		}
		return mediaFlags;
	}

}
