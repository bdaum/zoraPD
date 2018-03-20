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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.peer.internal.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ScoredString;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.peer.AssetSearchResult;
import com.bdaum.zoom.core.internal.peer.FileInfo;
import com.bdaum.zoom.core.internal.peer.IPeerProvider;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.peer.internal.PeerActivator;
import com.bdaum.zoom.peer.internal.model.PeerDefinition;
import com.bdaum.zoom.peer.internal.model.SharedCatalog;
import com.bdaum.zoom.ui.internal.UiUtilities;

@SuppressWarnings("restriction")
public class PeerProvider implements IPeerProvider, Serializable {

	private static final ScoredString[] EMPTYSCORES = new ScoredString[0];
	private static final long serialVersionUID = 4247313397050959299L;
	private static final String FT = "ft"; //$NON-NLS-1$
	private static int ftCounter;
	private final String host;
	private Map<String, InputStream> openFiles = new HashMap<String, InputStream>(3);
	private String location;

	/**
	 * Constructor
	 *
	 * @param host
	 *            - host of this provider peer
	 * @param port
	 *            - port of this provider peer
	 */
	public PeerProvider(String host, int port) {
		this.host = host;
		this.location = host + ":" + port; //$NON-NLS-1$
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerProvider#isLocalRequest(java.lang
	 * .String)
	 */
	public synchronized boolean isLocalRequest(String sender) {
		return location.equals(sender);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerProvider#select(com.bdaum.zoom
	 * .cat.model.group.SmartCollection, com.bdaum.zoom.core.IAssetFilter,
	 * java.lang.String)
	 */
	public synchronized Collection<AssetSearchResult> select(SmartCollection scoll, IAssetFilter[] assetFilters,
			String receiver) {
		List<AssetSearchResult> result = new ArrayList<AssetSearchResult>(100);
		if (PeerActivator.getDefault().addIncomingCall(receiver, IPeerService.SEARCH)) {
			boolean local = isLocalRequest(receiver);
			File currentCatFile = Core.getCore().getDbManager().getFile();
			List<SharedCatalog> sharedCatalogs = PeerActivator.getDefault().getSharedCatalogs();
			for (SharedCatalog cat : sharedCatalogs) {
				if (local) {
					if (currentCatFile != null && currentCatFile.equals(cat.getPath()))
						continue;
				} else if (cat.getPrivacy() == QueryField.SAFETY_LOCAL)
					continue;
				int p = receiver.lastIndexOf(':');
				if (p > 0)
					receiver = receiver.substring(0, p);
				for (PeerDefinition res : cat.getRestrictions())
					if (!local && res.getHost().equals(receiver)) {
						if ((res.getRights() & IPeerService.SEARCH) == 0)
							return result;
						break;
					}
				int privacy = cat.getPrivacy();
				scoll = Utilities.localizeSmartCollection(scoll);
				if (!local && privacy != QueryField.SAFETY_RESTRICTED)
					scoll.addCriterion(
							new CriterionImpl(QueryField.SAFETY.getKey(), null, privacy, QueryField.NOTGREATER, true));
				IDbManager dbManager = cat.getDbManager();
				if (dbManager.getFile() != null) {
					ICollectionProcessor processor = dbManager.createCollectionProcessor(scoll, assetFilters, null);
					List<Asset> select = processor.select(false);
					String location = getLocation();
					for (Asset asset : select)
						result.add(new AssetSearchResult(asset, cat.getPath(), location));
				}
			}
		}
		return result;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerProvider#getFileInfo(java.lang
	 * .String, java.lang.String)
	 */
	public synchronized FileInfo getFileInfo(String uri, String volume) {
		File file = Core.getCore().getVolumeManager().findExistingFile(uri, volume);
		return (file != null) ? new FileInfo(file.length(), file) : null;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerProvider#ftOpen(com.bdaum.zoom
	 * .core.internal.peer.FileInfo)
	 */
	public synchronized String ftOpen(FileInfo info) {
		try {
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(info.getFile()));
			String ticket = FT + (++ftCounter);
			openFiles.put(ticket, in);
			return ticket;
		} catch (FileNotFoundException e) {
			// fall through
		}
		return null;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerProvider#ftRead(java.lang.String,
	 * int)
	 */
	public synchronized byte[] ftRead(String ticket, int size) {
		InputStream in = openFiles.get(ticket);
		if (in != null) {
			byte[] buf = new byte[size];
			try {
				int read = in.read(buf);
				return (read != size) ? null : buf;
			} catch (IOException e) {
				// fall through
			}
		}
		return null;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerProvider#ftClose(java.lang.String)
	 */
	@SuppressWarnings("resource")
	public synchronized void ftClose(String ticket) {
		InputStream in = openFiles.remove(ticket);
		if (in != null)
			try {
				in.close();
			} catch (IOException e) {
				// do nothing
			}
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerProvider#checkCredentials(int,
	 * int, java.io.File, java.lang.String)
	 */
	public synchronized boolean checkCredentials(int operation, int safety, File catFile, String receiver) {
		if (PeerActivator.getDefault().addIncomingCall(receiver, operation)) {
			boolean local = isLocalRequest(receiver);
			File currentCatFile = Core.getCore().getDbManager().getFile();
			if (local && currentCatFile != null && currentCatFile.equals(catFile))
				return true;
			for (SharedCatalog cat : PeerActivator.getDefault().getSharedCatalogs()) {
				if (cat.getPath().equals(catFile)) {
					if (!local && safety > cat.getPrivacy())
						return false;
					int p = receiver.lastIndexOf(':');
					if (p > 0)
						receiver = receiver.substring(0, p);
					List<PeerDefinition> restrictions = cat.getRestrictions();
					for (PeerDefinition res : restrictions)
						if (res.getHost().equals(receiver))
							return (res.getRights() & operation) == operation;
					return operation == IPeerService.SEARCH || operation == IPeerService.VIEW;
				}
			}
		}
		return false;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerProvider#obtainStructForAsset(
	 * java.io.File, java.lang.Class, java.lang.String, boolean)
	 */
	public synchronized <T extends IIdentifiableObject> List<T> obtainStructForAsset(File catFile, Class<T> clazz,
			String assetId, boolean multipleAsset) {
		for (SharedCatalog cat : PeerActivator.getDefault().getSharedCatalogs())
			if (cat.getPath().equals(catFile)) {
				IDbManager dbManager = cat.getDbManager();
				if (dbManager.getFile() != null)
					return new ArrayList<T>(dbManager.obtainStruct(clazz, assetId, multipleAsset, null, null, false));
			}
		return new ArrayList<T>(0);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerProvider#obtainObjects(java.io
	 * .File, java.lang.Class, java.lang.String, java.lang.String[])
	 */
	public synchronized <T extends IIdentifiableObject> List<T> obtainObjects(File catFile, Class<T> clazz,
			String field, String[] ids) {
		for (SharedCatalog cat : PeerActivator.getDefault().getSharedCatalogs())
			if (cat.getPath().equals(catFile)) {
				IDbManager dbManager = cat.getDbManager();
				if (dbManager.getFile() != null)
					return new ArrayList<T>(dbManager.obtainObjects(clazz, field, ids));
			}
		return new ArrayList<T>(0);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerProvider#obtainById(java.io.File,
	 * java.lang.Class, java.lang.String)
	 */
	public <T extends IIdentifiableObject> T obtainById(File catFile, Class<T> clazz, String id) {
		for (SharedCatalog cat : PeerActivator.getDefault().getSharedCatalogs())
			if (cat.getPath().equals(catFile)) {
				IDbManager dbManager = cat.getDbManager();
				if (dbManager.getFile() != null)
					return dbManager.obtainById(clazz, id);
			}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerProvider#obtainAsset(java.io.File,
	 * java.lang.String)
	 */
	public synchronized Asset obtainAsset(File catFile, String assetId, String receiver) throws SecurityException {
		for (SharedCatalog cat : PeerActivator.getDefault().getSharedCatalogs())
			if (cat.getPath().equals(catFile)) {
				IDbManager dbManager = cat.getDbManager();
				if (dbManager.getFile() != null) {
					AssetImpl asset = dbManager.obtainAsset(assetId);
					if (!checkCredentials(IPeerService.SEARCH, asset.getSafety(), catFile, receiver))
						throw new SecurityException();
					return asset;
				}
			}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.peer.IPeerProvider#getLocation()
	 */
	public String getLocation() {
		return location;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.peer.IPeerProvider#getHost()
	 */
	public String getHost() {
		return host;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.peer.IPeerProvider#
	 * supportsSimilarityAlgorithm (java.lang.String)
	 */
	public synchronized boolean supportsSimilarityAlgorithm(String algoId) {
		return CoreActivator.getDefault().getCbirAlgorithms().contains(algoId);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerProvider#getValueProposals(java
	 * .lang.String, java.lang.String)
	 */
	public Collection<? extends String> getValueProposals(String field, String subfield, String receiver) {
		if (field == null) {
			int occ = Integer.parseInt(subfield);
			ScoredString[] tagCloud = getTagCloud(occ, receiver);
			List<String> result = new ArrayList<String>(tagCloud.length * 2);
			for (ScoredString scoredString : tagCloud) {
				result.add(scoredString.getString());
				result.add(String.valueOf(scoredString.getScore()));
			}
			return result;
		}
		Set<String> result = new HashSet<String>(30);
		if (PeerActivator.getDefault().addIncomingCall(receiver, IPeerService.SEARCH)) {
			boolean local = isLocalRequest(receiver);
			File currentCatFile = Core.getCore().getDbManager().getFile();
			List<SharedCatalog> sharedCatalogs = PeerActivator.getDefault().getSharedCatalogs();
			catloop: for (SharedCatalog cat : sharedCatalogs) {
				if (local) {
					if (currentCatFile != null && currentCatFile.equals(cat.getPath()))
						continue;
				} else if (cat.getPrivacy() == QueryField.SAFETY_LOCAL)
					continue;
				int p = receiver.lastIndexOf(':');
				if (p > 0)
					receiver = receiver.substring(0, p);
				for (PeerDefinition res : cat.getRestrictions())
					if (!local && res.getHost().equals(receiver)) {
						if ((res.getRights() & IPeerService.SEARCH) == 0)
							continue catloop;
						break;
					}
				IDbManager dbManager = cat.getDbManager();
				if (dbManager.getFile() != null) {
					Set<String> valueProposals = UiUtilities.getValueProposals(dbManager, field, subfield);
					if (valueProposals != null)
						result.addAll(valueProposals);
				}
			}
		}
		return result;
	}

	private ScoredString[] getTagCloud(int occurrences, String receiver) {
		Map<String, List<ScoredString>> scoreMap = null;
		if (PeerActivator.getDefault().addIncomingCall(receiver, IPeerService.SEARCH)) {
			boolean local = isLocalRequest(receiver);
			File currentCatFile = Core.getCore().getDbManager().getFile();
			List<SharedCatalog> sharedCatalogs = PeerActivator.getDefault().getSharedCatalogs();
			catloop: for (SharedCatalog cat : sharedCatalogs) {
				if (local) {
					if (currentCatFile != null && currentCatFile.equals(cat.getPath()))
						continue;
				} else if (cat.getPrivacy() == QueryField.SAFETY_LOCAL)
					continue;
				int p = receiver.lastIndexOf(':');
				if (p > 0)
					receiver = receiver.substring(0, p);
				for (PeerDefinition res : cat.getRestrictions())
					if (!local && res.getHost().equals(receiver)) {
						if ((res.getRights() & IPeerService.SEARCH) == 0)
							continue catloop;
						break;
					}
				IDbManager dbManager = cat.getDbManager();
				File indexPath = dbManager.getIndexPath();
				if (indexPath != null) {
					try {
						List<ScoredString> tags = Core.getCore().getDbFactory().getLuceneService().listTags(indexPath,
								occurrences);
						if (tags != null) {
							if (sharedCatalogs.size() == 1)
								return tags.toArray(new ScoredString[tags.size()]);
							if (scoreMap == null)
								scoreMap = new HashMap<String, List<ScoredString>>(occurrences * 3 / 2);
							for (ScoredString scoredString : tags) {
								String key = scoredString.getString();
								List<ScoredString> list = scoreMap.get(key);
								if (list == null) {
									list = new ArrayList<ScoredString>(sharedCatalogs.size());
									scoreMap.put(key, list);
								}
								list.add(scoredString);
							}
						}
					} catch (IOException e) {
						// ignore this catalog
					}
				}
			}
		}
		if (scoreMap == null)
			return EMPTYSCORES;
		List<ScoredString> cumulated = new ArrayList<ScoredString>(scoreMap.size());
		for (Map.Entry<String, List<ScoredString>> entry : scoreMap.entrySet()) {
			int score = 0;
			for (ScoredString scoredString : entry.getValue())
				score += scoredString.getScore();
			cumulated.add(new ScoredString(entry.getKey(), score));
		}
		ScoredString[] scoredStrings = cumulated.toArray(new ScoredString[cumulated.size()]);
		Arrays.sort(scoredStrings);
		if (scoredStrings.length > occurrences) {
			ScoredString[] trunc = new ScoredString[occurrences];
			System.arraycopy(scoredStrings, 0, trunc, 0, occurrences);
			scoredStrings = trunc;
		}
		return scoredStrings;
	}

}
