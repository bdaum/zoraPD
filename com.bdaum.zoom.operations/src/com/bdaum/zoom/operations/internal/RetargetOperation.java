package com.bdaum.zoom.operations.internal;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class RetargetOperation extends DbOperation {

	private final File newFile;
	private final int level;
	private String[] assetIds;
	private final AssetImpl asset;
	private String oldUri;
	private String oldVolume;
	private String oldName;
	private String oldFolderUri;
	private String oldFolderUriSlash;
	private String newFolderUri;
	private final boolean voiceNote;
	private String[] vAssetIds;
	private String[] oldVoiceUris;

	public RetargetOperation(AssetImpl asset, File newFile, int level, boolean voiceNote) {
		super(Messages.getString("RetargetOperation.retarget")); //$NON-NLS-1$
		this.asset = asset;
		this.newFile = newFile;
		this.level = level;
		this.voiceNote = voiceNote;
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT | IProfiledOperation.STRUCT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT | IProfiledOperation.STRUCT;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		boolean changed = false;
		boolean prune = false;
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		String volume = volumeManager.getVolumeForFile(newFile);
		oldVolume = asset.getVolume();
		int folderLevel = Math.max(1, level);
		oldFolderUri = asset.getUri();
		int p = oldFolderUri.length();
		newFolderUri = newFile.toURI().toString();
		for (int i = folderLevel; i > 0 && p >= 0; i--)
			p = oldFolderUri.lastIndexOf('/', p - 1);
		if (p >= 0) {
			newFolderUri = newFolderUri.substring(0, newFolderUri.length() - oldFolderUri.length() + p);
			oldFolderUri = oldFolderUri.substring(0, p);
		}
		if (level == 0) {
			init(aMonitor, 2);
			if (asset.getFileState() != IVolumeManager.PEER) {
				dbManager.markSystemCollectionsForPurge(asset);
				oldUri = asset.getUri();
				oldName = asset.getName();
				asset.setUri(newFile.toURI().toString());
				asset.setName(newFile.getName());
				asset.setVolume(volume);
				if (dbManager.safeTransaction(null, asset)) {
					changed = dbManager.createFolderHierarchy(asset);
					fireAssetsModified(null, null);
				}
			}
			aMonitor.worked(1);
		} else {
			oldFolderUriSlash = oldFolderUri.substring(0, p + 1);
			List<AssetImpl> assets = dbManager.obtainObjects(AssetImpl.class, "uri", oldFolderUriSlash, //$NON-NLS-1$
					QueryField.STARTSWITH);
			int size = assets.size();
			int vsize = 0;
			List<AssetImpl> vAssets = null;
			if (voiceNote) {
				vAssets = dbManager.obtainObjects(AssetImpl.class, "voiceFileURI", oldFolderUriSlash, //$NON-NLS-1$
						QueryField.STARTSWITH);
				vAssetIds = new String[vsize = vAssets.size()];
				oldVoiceUris = new String[vsize];
			}
			init(aMonitor, size + vsize + 1);
			assetIds = new String[size];
			int i = 0;
			int n = 0;
			int interval = 25;
			for (AssetImpl a : assets) {
				if (volumeManager.findExistingFile(a, false) == null) {
					String newAssetUri = newFolderUri + a.getUri().substring(oldFolderUri.length());
					try {
						File nFile = new File(new URI(newAssetUri));
						if (nFile.exists()) {
							dbManager.markSystemCollectionsForPurge(a);
							a.setUri(newAssetUri);
							a.setVolume(volume);
							if (dbManager.safeTransaction(null, a)) {
								assetIds[i] = a.getStringId();
								changed |= dbManager.createFolderHierarchy(a);
							}
						}
					} catch (URISyntaxException e) {
						addError(NLS.bind(Messages.getString("RetargetOperation.bad_uri_execute"), //$NON-NLS-1$
								newAssetUri), e);
					}
				}
				if (n >= interval) {
					interval += 5;
					fireAssetsModified(null, null);
					if (changed) {
						fireStructureModified();
						prune = true;
						changed = false;
					}
					n = 0;
				}
				aMonitor.worked(1);
				++n;
				++i;
			}
			if (n > 0)
				fireAssetsModified(null, null);
			if (vAssets != null) {
				i = 0;
				for (AssetImpl a : vAssets) {
					if (AssetEnsemble.hasVoiceNote(a) && !AssetEnsemble.hasCloseVoiceNote(a)) {
						String voiceUri = AssetEnsemble.extractVoiceNote(a);
						try {
							String newVoiceUri = newFolderUri + voiceUri.substring(oldFolderUri.length());
							File nVoiceFile = new File(new URI(newVoiceUri));
							if (nVoiceFile.exists()) {
								oldVoiceUris[i] = a.getVoiceFileURI();
								AssetEnsemble.insertVoiceNote(a, volume, newVoiceUri);
								if (dbManager.safeTransaction(null, a))
									vAssetIds[i] = a.getStringId();
							}
						} catch (URISyntaxException e) {
							addError(NLS.bind(Messages.getString("RetargetOperation.bad_uri_execute"), //$NON-NLS-1$
									voiceUri), e);
						}
					}
					aMonitor.worked(1);
					++i;
				}
			}
		}
		if (prune || changed) {
			dbManager.pruneEmptySystemCollections(new NullProgressMonitor(), false);
			fireStructureModified();
		}
		return close(info);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return execute(aMonitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		boolean changed = false;
		if (level == 0) {
			initUndo(aMonitor, 1);
			if (asset.getFileState() != IVolumeManager.PEER) {
				dbManager.markSystemCollectionsForPurge(asset);
				asset.setUri(oldUri);
				asset.setName(oldName);
				asset.setVolume(oldVolume);
				if (dbManager.safeTransaction(null, asset))
					changed = dbManager.createFolderHierarchy(asset);
			}
			aMonitor.worked(1);
		} else {
			initUndo(aMonitor, assetIds.length + vAssetIds.length);
			Set<AssetImpl> assets = new HashSet<AssetImpl>(assetIds.length);
			for (int i = 0; i < assetIds.length; i++) {
				String assetId = assetIds[i];
				if (assetId != null) {
					AssetImpl a = dbManager.obtainAsset(assetId);
					if (a != null) {
						dbManager.markSystemCollectionsForPurge(a);
						String uri = a.getUri();
						if (uri.startsWith(newFolderUri)) {
							a.setVolume(oldVolume);
							a.setUri(oldFolderUri + uri.substring(newFolderUri.length()));
							if (dbManager.safeTransaction(null, a)) {
								assets.add(a);
								changed |= dbManager.createFolderHierarchy(a);
							}
						}
					}
					aMonitor.worked(1);
				}
			}
			if (voiceNote)
				for (int i = 0; i < vAssetIds.length; i++) {
					String assetId = vAssetIds[i];
					if (assetId != null) {
						AssetImpl a = dbManager.obtainAsset(assetId);
						if (a != null) {
							String uri = AssetEnsemble.extractVoiceNote(a);
							if (uri != null && uri.startsWith(newFolderUri)) {
								a.setVoiceVolume(oldVolume);
								a.setVoiceFileURI(oldVoiceUris[i]);
								dbManager.safeTransaction(null, a);
							}
						}
						aMonitor.worked(1);
					}
				}
		}
		fireAssetsModified(null, null);
		if (changed)
			fireStructureModified();
		return close(info);
	}

}
