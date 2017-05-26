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
 * (c) 2013 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.peer.internal.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetFilter;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.internal.ScoredString;
import com.bdaum.zoom.core.internal.peer.AssetOrigin;
import com.bdaum.zoom.core.internal.peer.AssetSearchResult;
import com.bdaum.zoom.core.internal.peer.ConnectionLostException;
import com.bdaum.zoom.core.internal.peer.FileInfo;
import com.bdaum.zoom.core.internal.peer.IPeerProvider;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.peer.internal.PeerActivator;
import com.bdaum.zoom.program.DiskFullException;

@SuppressWarnings("restriction")
public class PeerService implements IPeerService, Serializable {

	public class TransferJob extends Job {

		private final String uri;
		private final String volume;
		private final AssetOrigin assetOrigin;
		private final File outFile;

		public TransferJob(String uri, String volume, AssetOrigin assetOrigin, File outFile) {
			super(Messages.PeerService_file_transfer);
			setPriority(Job.INTERACTIVE);
			this.uri = uri;
			this.volume = volume;
			this.assetOrigin = assetOrigin;
			this.outFile = outFile;
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == this || Constants.FILETRANSFER == family || Constants.CRITICAL == family;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				FileInfo info = getPeerProvider(assetOrigin.getLocation()).getFileInfo(uri, volume);
				return transferRemoteFile(monitor, assetOrigin, info, outFile) ? Status.OK_STATUS
						: Status.CANCEL_STATUS;
			} catch (ConnectionLostException e) {
				return new Status(IStatus.ERROR, PeerActivator.PLUGIN_ID, e.getLocalizedMessage());
			} catch (IOException e) {
				return new Status(IStatus.ERROR, PeerActivator.PLUGIN_ID, Messages.PeerService_io_error_reading, e);
			} catch (DiskFullException e) {
				return new Status(IStatus.ERROR, PeerActivator.PLUGIN_ID, Messages.PeerService_disk_full, e);
			}
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Object getAdapter(Class adapter) {
			if (File.class.equals(adapter))
				return outFile;
			return super.getAdapter(adapter);
		}

	}

	private static final long serialVersionUID = -2433887216711012960L;
	private static final String PEER_SELECT = "peerSelect"; //$NON-NLS-1$
	private int counter = 0;
	private Map<String, Object> responseMap = new HashMap<String, Object>();
	public Map<String, AssetOrigin> assetOriginMap = new HashMap<String, AssetOrigin>(557);
	private Map<String, Thread[]> jobMap = new HashMap<String, Thread[]>(7);

	public class PeerSelectTask extends Thread {

		private final IPeerProvider provider;
		private final SmartCollection scoll;
		private final IAssetFilter[] assetFilters;
		private final List<Asset> assets;

		public PeerSelectTask(IPeerProvider provider, String ticket, SmartCollection scoll, IAssetFilter[] assetFilters,
				List<Asset> assets) {
			super(NLS.bind(Messages.PeerService_fetch, ticket));
			this.provider = provider;
			this.scoll = scoll;
			this.assetFilters = assetFilters;
			this.assets = assets;
		}

		@Override
		public void run() {
			Collection<AssetSearchResult> result = provider.select(scoll, assetFilters,
					PeerActivator.getDefault().getLocation());
			for (AssetSearchResult assetSearchResult : result) {
				Asset asset = assetSearchResult.getAsset();
				asset.setFileState(IVolumeManager.PEER);
				assets.add(asset);
				assetSearchResult.setAsset(null);
				assetOriginMap.put(asset.getStringId(),
						new AssetOrigin(assetSearchResult.getCatalog(), assetSearchResult.getLocation()));
			}
		}
	}

	public class ProposalSelectTask extends Thread {

		private final IPeerProvider provider;
		private final Set<String> proposals;
		private final String field;
		private final String subfield;

		public ProposalSelectTask(IPeerProvider provider, String ticket, String field, String subfield,
				Set<String> proposals) {
			super(NLS.bind(Messages.PeerService_fetch, ticket));
			this.provider = provider;
			this.field = field;
			this.subfield = subfield;
			this.proposals = proposals;
		}

		@Override
		public void run() {
			Collection<? extends String> valueProposals = provider.getValueProposals(field, subfield,
					PeerActivator.getDefault().getLocation());
			proposals.addAll(valueProposals);
		}
	}

	public class TagCloudTask extends Thread {

		private final IPeerProvider provider;
		private final Map<String, List<ScoredString>> tags;
		private final int occurences;

		public TagCloudTask(IPeerProvider provider, String ticket, int occurences,
				Map<String, List<ScoredString>> tags) {
			super(NLS.bind(Messages.PeerService_fetch, ticket));
			this.provider = provider;
			this.occurences = occurences;
			this.tags = tags;
		}

		@Override
		public void run() {
			Collection<? extends String> tagCloud = provider.getValueProposals(null, String.valueOf(occurences),
					PeerActivator.getDefault().getLocation());
			Iterator<? extends String> iterator = tagCloud.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				int score = Integer.parseInt(iterator.next());
				List<ScoredString> list = tags.get(key);
				if (list == null) {
					list = new ArrayList<ScoredString>();
					tags.put(key, list);
				}
				list.add(new ScoredString(key, score));
			}
		}
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerService#select(com.bdaum.zoom.
	 * cat.model.group.SmartCollection, com.bdaum.zoom.core.IAssetFilter)
	 */
	public String select(SmartCollection scoll, IAssetFilter[] assetFilters) {
		String ticket = PEER_SELECT + (counter++);
		List<Asset> assets = Collections.synchronizedList(new ArrayList<Asset>(100));
		responseMap.put(ticket, assets);
		IPeerProvider[] peerProviders = PeerActivator.getDefault().getPeerProviders();
		if (peerProviders.length == 0)
			return null;
		Thread[] jobs = new PeerSelectTask[peerProviders.length];
		for (int i = 0; i < peerProviders.length; i++) {
			jobs[i] = new PeerSelectTask(peerProviders[i], ticket, scoll, assetFilters, assets);
			jobs[i].start();
		}
		jobMap.put(ticket, jobs);
		return ticket;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerService#getSelect(java.lang.String
	 * )
	 */
	@SuppressWarnings("unchecked")
	public Collection<? extends Asset> getSelect(String ticket) {
		Thread[] jobs = jobMap.remove(ticket);
		if (jobs != null) {
			for (Thread job : jobs) {
				try {
					job.join();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
		return (Collection<? extends Asset>) responseMap.get(ticket);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerService#hasPeerPeerProviders()
	 */
	public boolean hasPeerPeerProviders() {
		return PeerActivator.getDefault().hasPeerProviders();
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.peer.IPeerService#getOnlinePeerCount()
	 */
	public int getOnlinePeerCount() {
		return PeerActivator.getDefault().getOnlinePeerCount();
	}

	/**
	 * Called when service is activated.
	 */
	protected void activate() {
		PeerActivator.getDefault().logInfo(Messages.PeerService_peer_service_activated);
	}

	/**
	 * Called when service is deactivated.
	 */
	protected void deactivate() {
		PeerActivator.getDefault().deactivate();
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerService#isOwnedByPeer(java.lang
	 * .String)
	 */
	public boolean isOwnedByPeer(String assetId) {
		return assetOriginMap.containsKey(assetId);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerService#getAssetOrigin(java.lang
	 * .String)
	 */
	public AssetOrigin getAssetOrigin(String assetId) {
		return assetOriginMap.get(assetId);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerService#scheduleTransferJob(com
	 * .bdaum.zoom.cat.model.asset.Asset,
	 * com.bdaum.zoom.core.internal.peer.AssetOrigin)
	 */
	public Job scheduleTransferJob(Asset asset, AssetOrigin assetOrigin) {
		String uri = asset.getUri();
		try {
			File tempFile = Core.createTempFile("peerfile", Core.getFileExtension(uri)); //$NON-NLS-1$
			tempFile.deleteOnExit();
			TransferJob job = new TransferJob(uri, asset.getVolume(), assetOrigin, tempFile);
			job.schedule();
			return job;
		} catch (IOException e) {
			PeerActivator.getDefault().logError(Messages.PeerService_io_error_creating, e);
		}
		return null;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.peer.IPeerService#checkCredentials(int,
	 * int, com.bdaum.zoom.core.internal.peer.AssetOrigin)
	 */
	public boolean checkCredentials(int operation, int safety, AssetOrigin assetOrigin) throws ConnectionLostException {
		if (assetOrigin == null)
			return operation != COPY;
		return getPeerProvider(assetOrigin.getLocation()).checkCredentials(operation, safety, assetOrigin.getCatFile(),
				PeerActivator.getDefault().getLocation());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerService#obtainAsset(java.lang.
	 * String, java.io.File, java.lang.String)
	 */
	public Asset obtainAsset(String peer, File catFile, String assetId)
			throws SecurityException, ConnectionLostException {
		Asset asset = getPeerProvider(peer).obtainAsset(catFile, assetId, PeerActivator.getDefault().getLocation());
		if (asset != null) {
			asset.setFileState(IVolumeManager.PEER);
			assetOriginMap.put(asset.getStringId(), new AssetOrigin(catFile, peer));
		}
		return asset;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerService#getPeerProvider(java.lang
	 * .String)
	 */
	public IPeerProvider getPeerProvider(String location) throws ConnectionLostException {
		return PeerActivator.getDefault().getPeerProvider(location);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.peer.IPeerService#transferRemoteFile(
	 * IProgressMonitor, IPeerProvider, String, String, File)
	 */
	public boolean transferRemoteFile(IProgressMonitor monitor, AssetOrigin assetOrigin, FileInfo info, File outFile)
			throws IOException, DiskFullException, ConnectionLostException {
		if (info == null)
			return false;
		long size = info.getSize();
		if (size <= 0)
			return false;
		monitor.beginTask(NLS.bind(Messages.PeerService_transfer_file, info.getFile().getName()),
				(int) ((size + 4095) / 4096));
		String location = assetOrigin.getLocation();
		IPeerProvider peerProvider = getPeerProvider(location);
		String ticket = peerProvider.ftOpen(info);
		try {
			if (ticket == null)
				return false;
			try {
				try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile))) {
					while (size > 0 && !monitor.isCanceled()) {
						peerProvider = getPeerProvider(location);
						byte[] bytes = peerProvider.ftRead(ticket, (int) Math.min(4096, size));
						if (bytes == null)
							return false;
						out.write(bytes);
						size -= bytes.length;
						if (monitor.isCanceled()) {
							try {
								out.close();
								outFile.delete();
							} catch (IOException e) {
								// do nothing
							}
							return false;
						}
						monitor.worked(1);
					}
				} finally {
					long outsize = outFile.length();
					if (outsize < size)
						throw new DiskFullException(NLS.bind("File {0}: {1}", //$NON-NLS-1$
								outFile, outsize + " < " + size)); //$NON-NLS-1$
				}
			} finally {
				peerProvider = getPeerProvider(location);
				peerProvider.ftClose(ticket);
			}
			monitor.done();
			return true;
		} catch (FileNotFoundException e1) {
			// should never happen
			return false;
		}
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.core.internal.peer.IPeerService#getHost()
	 */
	public String getHost() {
		return PeerActivator.getDefault().getHostOrHostname();
	}

	public String[] findWeakSimilarityPeers(String algoId) {
		IPeerProvider[] peerProviders = PeerActivator.getDefault().getPeerProviders();
		List<String> peers = new ArrayList<String>(peerProviders.length);
		for (IPeerProvider provider : peerProviders)
			if (!provider.supportsSimilarityAlgorithm(algoId))
				peers.add(provider.getLocation());
		return peers.isEmpty() ? null : peers.toArray(new String[peers.size()]);
	}

	public boolean isLocal(String peer) {
		return peer == null || PeerActivator.getDefault().getLocation().equals(peer);
	}

	public FileInfo getFileInfo(AssetOrigin assetOrigin, String uri, String volume) throws ConnectionLostException {
		return getPeerProvider(assetOrigin.getLocation()).getFileInfo(uri, volume);
	}

	public <T extends IIdentifiableObject> List<T> obtainStructForAsset(AssetOrigin assetOrigin, Class<T> clazz,
			String assetId, boolean multiAsset) throws ConnectionLostException {
		return getPeerProvider(assetOrigin.getLocation()).obtainStructForAsset(assetOrigin.getCatFile(), clazz, assetId,
				multiAsset);
	}

	public <T extends IIdentifiableObject> T obtainById(AssetOrigin assetOrigin, Class<T> clazz, String id)
			throws ConnectionLostException {
		return getPeerProvider(assetOrigin.getLocation()).obtainById(assetOrigin.getCatFile(), clazz, id);
	}

	public <T extends IIdentifiableObject> List<T> obtainObjects(AssetOrigin assetOrigin, Class<T> clazz, String field,
			String[] values) throws ConnectionLostException {
		return getPeerProvider(assetOrigin.getLocation()).obtainObjects(assetOrigin.getCatFile(), clazz, field, values);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerService#select(com.bdaum.zoom.
	 * cat.model.group.SmartCollection, com.bdaum.zoom.core.IAssetFilter)
	 */
	public String askForValueProposals(String field, String subfield) {
		String ticket = PEER_SELECT + (counter++);
		Set<String> proposals = Collections.synchronizedSet(new HashSet<String>(100));
		responseMap.put(ticket, proposals);
		IPeerProvider[] peerProviders = PeerActivator.getDefault().getPeerProviders();
		Thread[] jobs = new ProposalSelectTask[peerProviders.length];
		for (int i = 0; i < peerProviders.length; i++) {
			jobs[i] = new ProposalSelectTask(peerProviders[i], ticket, field, subfield, proposals);
			jobs[i].start();
		}
		jobMap.put(ticket, jobs);
		return ticket;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerService#getSelect(java.lang.String
	 * )
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getProposals(String ticket) {
		Thread[] jobs = jobMap.remove(ticket);
		if (jobs != null)
			for (Thread job : jobs)
				try {
					job.join();
				} catch (InterruptedException e) {
					// do nothing
				}
		return (Set<String>) responseMap.get(ticket);
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.core.internal.peer.IPeerService#discardProposals(java.
	 * lang.String)
	 */
	public void discardTask(String ticket) {
		jobMap.remove(ticket);
	}

	public void checkListeningPort(IAdaptable adaptable) {
		PeerActivator.getDefault().checkListeningPort(adaptable);
	}

	public String askForTagCloud(int occurences) {
		String ticket = PEER_SELECT + (counter++);
		Map<String, List<ScoredString>> tags = Collections
				.synchronizedMap(new HashMap<String, List<ScoredString>>(300));
		responseMap.put(ticket, tags);
		IPeerProvider[] peerProviders = PeerActivator.getDefault().getPeerProviders();
		Thread[] jobs = new TagCloudTask[peerProviders.length];
		for (int i = 0; i < peerProviders.length; i++) {
			jobs[i] = new TagCloudTask(peerProviders[i], ticket, occurences, tags);
			jobs[i].start();
		}
		jobMap.put(ticket, jobs);
		return ticket;
	}

	@SuppressWarnings("unchecked")
	public Map<String, List<ScoredString>> getTagCloud(String ticket) {
		Thread[] jobs = jobMap.remove(ticket);
		if (jobs != null)
			for (Thread job : jobs)
				try {
					job.join();
				} catch (InterruptedException e) {
					// do nothing
				}
		return (Map<String, List<ScoredString>>) responseMap.get(ticket);

	}

}
