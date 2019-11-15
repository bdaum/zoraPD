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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.properties.XMPPropertyInfo;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.mtp.IRootManager;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.operations.internal.xmp.XMPField;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

@SuppressWarnings("restriction")
public class PasteMetadataOperation extends DbOperation {

	private final Collection<XMPField> selectedFields;
	private final int mode;
	private final List<Asset> assets;
	private final Map<XMPField, Object>[] undoMaps;
	private boolean fullTextSearch = false;
	private File voiceFile;
	private String[] oldNotes;
	private String[] oldVolumes;
	private List<File> filesToDelete = new ArrayList<>();
	private Map<File, File> renamedFiles = new HashMap<>();
	private String svg;
	private String noteText;
	private String voiceUri;

	@SuppressWarnings("unchecked")
	public PasteMetadataOperation(List<Asset> selectedAssets, Collection<XMPField> selectedFields, int mode,
			String note, File voiceFile) {
		super(Messages.getString("PasteMetadataOperation.paste_metadata")); //$NON-NLS-1$
		this.assets = selectedAssets;
		this.selectedFields = selectedFields;
		this.mode = mode;
		this.voiceFile = voiceFile;
		undoMaps = new Map[selectedAssets.size()];
		oldNotes = new String[selectedAssets.size()];
		oldVolumes = new String[selectedAssets.size()];
		if (note != null) {
			int p = note.indexOf('\f');
			if (p >= 0) {
				int q = note.indexOf('\f', p + 1);
				if (q >= 0) {
					svg = note.substring(q + 1);
					noteText = note.substring(p + 1, q);
				} else
					noteText = note.substring(p + 1);
				voiceUri = note.substring(0, p);
			}
		}
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		init(aMonitor, assets.size());
		int i = 0;
		for (Asset asset : assets) {
			if (asset.getFileState() != IVolumeManager.PEER) {
				boolean modified = false;
				Set<Object> toBeStored = new HashSet<Object>();
				Set<Object> toBeDeleted = new HashSet<Object>();
				HashMap<XMPField, Object> undoMap = new HashMap<XMPField, Object>(selectedFields.size() * 4 / 3);
				undoMaps[i] = undoMap;
				// Annotations
				String oldNote = asset.getVoiceFileURI();
				oldNotes[i] = oldNote;
				oldVolumes[i++] = asset.getVoiceVolume();
				String oldvoiceUri = null;
				String oldnoteText = null;
				String oldsvg = null;
				if (oldNote != null) {
					int p = oldNote.indexOf('\f');
					if (p >= 0) {
						int q = oldNote.indexOf('\f', p + 1);
						if (q >= 0) {
							oldsvg = oldNote.substring(q + 1);
							oldnoteText = oldNote.substring(p + 1, q);
						} else
							oldnoteText = oldNote.substring(p + 1);
						oldvoiceUri = oldNote.substring(0, p);
					} else if (oldNote.startsWith("?")) //$NON-NLS-1$
						oldnoteText = oldNote.substring(1);
					else
						oldvoiceUri = oldNote;
				}
				if (voiceUri != null && !voiceUri.isEmpty()
						&& (mode != Constants.SKIP || oldvoiceUri == null || oldvoiceUri.isEmpty())) {
					IRootManager vm = Core.getCore().getVolumeManager();
					if (".".equals(voiceUri)) //$NON-NLS-1$
						try {
							URI uri = Core.getVoicefileURI(new File(new URI(asset.getUri())));
							File target = new File(uri);
							if (target.exists()) {
								File tempFile = Core.createTempFile("Voice", "wav"); //$NON-NLS-1$//$NON-NLS-2$
								renamedFiles.put(target, tempFile);
								BatchUtilities.moveFile(target, tempFile, null);
							}
							BatchUtilities.copyFile(voiceFile, target, null);
							filesToDelete.add(target);
							oldvoiceUri = voiceUri;
							asset.setVoiceVolume(asset.getVolume());
						} catch (URISyntaxException e) {
							addError(Messages.getString("PasteMetadataOperation.bad_uri_pasting"), e); //$NON-NLS-1$
						} catch (IOException e) {
							addError(Messages.getString("PasteMetadataOperation.io_error_pasting"), e); //$NON-NLS-1$
						} catch (DiskFullException e) {
							addError(Messages.getString("PasteMetadataOperation.disk_full_pasting"), e); //$NON-NLS-1$
							return close(info);
						}
					else {
						oldvoiceUri = voiceUri;
						asset.setVoiceVolume(vm.getVolumeForFile(voiceFile));
					}
				}
				if (noteText != null && !noteText.isEmpty()
						&& (mode != Constants.SKIP || oldnoteText == null || oldnoteText.isEmpty()))
					oldnoteText = noteText;
				if (svg != null && !svg.isEmpty() && (mode != Constants.SKIP || oldsvg == null || oldsvg.isEmpty()))
					oldsvg = svg;
				StringBuilder sb = new StringBuilder();
				if (oldvoiceUri != null)
					sb.append(oldvoiceUri);
				sb.append('\f');
				if (oldnoteText != null)
					sb.append(oldnoteText);
				sb.append('\f');
				if (oldsvg != null)
					sb.append(oldsvg);
				asset.setVoiceFileURI(sb.length() == 2 ? null : sb.toString());
				toBeStored.add(asset);
				modified = true;
				// Metadata
				AssetEnsemble ensemble = new AssetEnsemble(dbManager, asset, null);
				for (XMPField field : selectedFields) {
					QueryField qfield = field.getQfield();
					XMPPropertyInfo prop = field.getProp();
					try {
						Object oldValue = field.getIndex1() <= 1 ? field.fetchStoredValue(ensemble)
								: undoMap.get(field);
						if (mode == Constants.SKIP && !qfield.isNeutralValue(oldValue))
							continue;
						undoMap.put(field, oldValue);
						if (field.getIndex1() <= 1)
							deleteRelations(qfield, asset, toBeDeleted, toBeStored);
						if (!createRelation(qfield, asset, prop.getValue().toString(), toBeStored))
							field.assignValue(ensemble, null);
						fullTextSearch |= qfield.isFullTextSearch();
						modified = true;
					} catch (XMPException e) {
						addWarning(NLS.bind(Messages.getString("PasteMetadataOperation.io_error_while_accessing_xmp"), //$NON-NLS-1$
								prop.getPath()), e);
					} catch (Exception e) {
						addError(NLS.bind(
								Messages.getString("PasteMetadataOperation.internal_error_while_accessing_clipboard"), //$NON-NLS-1$
								qfield.getKey()), e);
					}
				}
				if (modified) {
					ensemble.store(toBeDeleted, toBeStored);
					storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
				}
			}
		}

		fireApplyRules(assets, null);
		fireAssetsModified(new BagChange<>(null, assets, null, null), null);
		return close(info, fullTextSearch ? assets : null);
	}

	private boolean createRelation(QueryField qfield, Asset asset, String id, Collection<Object> toBeStored) {
		String assetId = asset.getStringId();
		if (qfield == QueryField.LOCATIONSHOWN_ID) {
			List<LocationShownImpl> rels = dbManager.obtainStruct(LocationShownImpl.class, assetId, false, "location", //$NON-NLS-1$
					id, false);
			if (rels.isEmpty())
				toBeStored.add(new LocationShownImpl(id, assetId));
			return true;
		}
		if (qfield == QueryField.LOCATIONCREATED_ID) {
			List<LocationCreatedImpl> set = dbManager.obtainObjects(LocationCreatedImpl.class, "location", id, //$NON-NLS-1$
					QueryField.EQUALS);
			if (set.isEmpty()) {
				LocationCreatedImpl rel = new LocationCreatedImpl(id);
				rel.addAsset(assetId);
				asset.setLocationCreated_parent(rel.getStringId());
				toBeStored.add(rel);
			} else {
				LocationCreatedImpl rel = set.get(0);
				if (!rel.getAsset().contains(assetId)) {
					rel.addAsset(assetId);
					asset.setLocationCreated_parent(rel.getStringId());
					toBeStored.add(rel);
				}
			}
			return true;
		}
		if (qfield == QueryField.CONTACT_ID) {
			List<CreatorsContactImpl> set = dbManager.obtainObjects(CreatorsContactImpl.class, "contact", id, //$NON-NLS-1$
					QueryField.EQUALS);
			if (set.isEmpty()) {
				CreatorsContactImpl rel = new CreatorsContactImpl(id);
				rel.addAsset(assetId);
				asset.setCreatorsContact_parent(rel.getStringId());
				toBeStored.add(rel);
			} else {
				CreatorsContactImpl rel = set.get(0);
				if (!rel.getAsset().contains(assetId)) {
					rel.addAsset(assetId);
					asset.setCreatorsContact_parent(rel.getStringId());
					toBeStored.add(rel);
				}
			}
			return true;
		}
		if (qfield == QueryField.ARTWORK_ID) {
			List<ArtworkOrObjectShownImpl> rels = dbManager.obtainStruct(ArtworkOrObjectShownImpl.class, assetId, false,
					"artworkOrObject", id, false); //$NON-NLS-1$
			if (rels.isEmpty())
				toBeStored.add(new ArtworkOrObjectShownImpl(id, assetId));
			return true;
		}
		return false;
	}

	private void deleteRelations(QueryField qfield, Asset asset, Collection<Object> toBeDeleted,
			Collection<Object> toBeStored) {
		String assetId = asset.getStringId();
		if (qfield == QueryField.LOCATIONSHOWN_ID)
			for (LocationShownImpl obj : new ArrayList<LocationShownImpl>(
					dbManager.obtainStructForAsset(LocationShownImpl.class, assetId, false)))
				toBeDeleted.add(obj);
		else if (qfield == QueryField.LOCATIONCREATED_ID) {
			LocationCreatedImpl rel = dbManager.obtainById(LocationCreatedImpl.class,
					asset.getLocationCreated_parent());
			if (rel != null) {
				rel.removeAsset(assetId);
				if (rel.getAsset().isEmpty())
					toBeDeleted.add(rel);
				else
					toBeStored.add(rel);
				asset.setLocationCreated_parent(null);
			}
		} else if (qfield == QueryField.CONTACT_ID) {
			CreatorsContactImpl rel = dbManager.obtainById(CreatorsContactImpl.class,
					asset.getCreatorsContact_parent());
			if (rel != null) {
				rel.removeAsset(assetId);
				if (rel.getAsset().isEmpty())
					toBeDeleted.add(rel);
				else
					toBeStored.add(rel);
				asset.setCreatorsContact_parent(null);
			}
		} else if (qfield == QueryField.ARTWORK_ID)
			for (ArtworkOrObjectShownImpl obj : new ArrayList<ArtworkOrObjectShownImpl>(
					dbManager.obtainStructForAsset(ArtworkOrObjectShownImpl.class, assetId, false)))
				toBeDeleted.add(obj);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		filesToDelete.clear();
		renamedFiles.clear();
		return execute(aMonitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		initUndo(aMonitor, assets.size());
		int i = 0;
		for (Asset asset : assets)
			if (asset.getFileState() != IVolumeManager.PEER) {
				Set<Object> toBeStored = new HashSet<Object>();
				Set<Object> toBeDeleted = new HashSet<Object>();
				asset.setVoiceFileURI(oldNotes[i]);
				asset.setVoiceVolume(oldVolumes[i]);
				toBeStored.add(asset);
				Map<XMPField, Object> undoMap = undoMaps[i++];
				if (undoMap != null && !undoMap.isEmpty()) {
					toBeDeleted = new HashSet<Object>();
					AssetEnsemble ensemble = new AssetEnsemble(dbManager, asset, null);
					for (Map.Entry<XMPField, Object> entry : undoMap.entrySet()) {
						Object oldValue = entry.getValue();
						XMPField field = entry.getKey();
						QueryField qfield = field.getQfield();
						deleteRelations(qfield, asset, toBeDeleted, toBeStored);
						boolean done = false;
						if (oldValue instanceof String)
							done = createRelation(qfield, asset, (String) oldValue, toBeStored);
						else if (oldValue instanceof String[])
							for (String id : (String[]) oldValue)
								done |= createRelation(qfield, asset, id, toBeStored);
						if (!done)
							try {
								field.assignValue(ensemble, oldValue);
							} catch (Exception e) {
								addError(NLS.bind(
										Messages.getString("PasteMetadataOperation.internal_error_when_restoring"), //$NON-NLS-1$
										qfield.getKey()), e);
							}
					}
					ensemble.store(toBeDeleted, toBeStored);
				}
				storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
			}
		for (File file : filesToDelete)
			file.delete();
		for (Map.Entry<File, File> entry : renamedFiles.entrySet())
			try {
				BatchUtilities.moveFile(entry.getValue(), entry.getKey(), null);
			} catch (IOException e) {
				addError(Messages.getString("PasteMetadataOperation.io_error_restoring"), e); //$NON-NLS-1$
			} catch (DiskFullException e) {
				addError(Messages.getString("PasteMetadataOperation.disk_full_restoring"), e); //$NON-NLS-1$
				return close(info);
			}
		return close(info, fullTextSearch ? assets : null);
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

	@Override
	public void dispose() {
		for (File file : renamedFiles.values())
			file.delete();
		super.dispose();
	}

}
