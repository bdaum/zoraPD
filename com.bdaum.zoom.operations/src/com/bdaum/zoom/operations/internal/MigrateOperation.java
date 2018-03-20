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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.operations.internal;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;

import com.bdaum.aoModeling.runtime.AomList;
import com.bdaum.zoom.cat.model.BookmarkImpl;
import com.bdaum.zoom.cat.model.Ghost_typeImpl;
import com.bdaum.zoom.cat.model.MigrationPolicy;
import com.bdaum.zoom.cat.model.MigrationPolicy_type;
import com.bdaum.zoom.cat.model.MigrationRule;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.MediaExtension;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.PostProcessor;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.WallImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameterImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.LastDeviceImport;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.cat.model.pageLayout.PageLayoutImpl;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IPostProcessor;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.VolumeManager;

@SuppressWarnings("restriction")
public class MigrateOperation extends AbstractCloneCatOperation {

	private IDbManager newDbManager;
	private Pattern[] patterns;
	private IVolumeManager volumeManager;
	private Set<String> visited = new HashSet<String>();
	private MigrationRule[] rules;
	private int fileSeparatorPolicy;
	private String finalMessage;
	private int severity;
	private boolean winTarget;
	private String currentVolume;

	public MigrateOperation(IDbManager newDbManager, MigrationPolicy policy) {
		super(Messages.getString("MigrateOperation.migrate_cat")); //$NON-NLS-1$
		this.newDbManager = newDbManager;
		volumeManager = Core.getCore().getVolumeManager();
		rules = policy.getRule();
		patterns = new Pattern[rules.length];
		int flags = Constants.WIN32 ? Pattern.CASE_INSENSITIVE : 0;
		for (int i = 0; i < rules.length; i++)
			patterns[i] = Pattern.compile(rules[i].getSourcePattern(), flags);
		String fsp = policy.getFileSeparatorPolicy();
		fileSeparatorPolicy = MigrationPolicy_type.fileSeparatorPolicy_tOSLASH.equals(fsp) ? 1
				: MigrationPolicy_type.fileSeparatorPolicy_tOBACKSLASH.equals(fsp) ? 2 : 0;
		switch (fileSeparatorPolicy) {
		case 1:
			winTarget = false;
			break;
		case 2:
			winTarget = true;
			break;
		default:
			winTarget = Constants.WIN32;
		}
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		List<URI> failedWatchedFolders = new ArrayList<URI>();
		List<URI> watchedFoldersWithFilters = new ArrayList<URI>();
		init(monitor, 1337000);
		monitor.subTask(Messages.getString("MigrateOperation.migrating")); //$NON-NLS-1$
		try {
			final Meta oldMeta = dbManager.getMeta(true);
			Meta newMeta = newDbManager.getMeta(true);
			newMeta.setPlatform("$migrated$"); //$NON-NLS-1$
			List<Object> toBeStored = cloneMeta(newDbManager, oldMeta, newMeta, oldMeta.getDescription());
			newMeta.setKeywords(oldMeta.getKeywords());
			Utilities.ensureCatConsistency(oldMeta.getCategory());
			newMeta.setCategory(oldMeta.getCategory());
			newMeta.setCleaned(false);
			newMeta.setCreationDate(oldMeta.getCreationDate());
			newMeta.setRelevantLireVersion(oldMeta.getRelevantLireVersion());
			newMeta.setBackupLocation(migrate(oldMeta.getBackupLocation()));
			if (oldMeta.getColorLabels() != null)
				newMeta.setColorLabels(oldMeta.getColorLabels());
			newMeta.setPersonsToKeywords(oldMeta.getPersonsToKeywords());
			newMeta.setThumbnailResolution(oldMeta.getThumbnailResolution());
			newMeta.setSharpen(oldMeta.getSharpen());
			newMeta.setWebpCompression(oldMeta.getWebpCompression());
			newMeta.setJpegQuality(oldMeta.getJpegQuality());
			newMeta.setThumbnailFromPreview(oldMeta.getThumbnailFromPreview());
			newMeta.setTimeline(oldMeta.getTimeline());
			newMeta.setLocationFolders(oldMeta.getLocationFolders());
			newMeta.setPauseFolderWatch(oldMeta.getPauseFolderWatch());
			newMeta.setReadonly(false);
			newMeta.setAutoWatch(oldMeta.getAutoWatch());
			newMeta.setFolderWatchLatency(oldMeta.getFolderWatchLatency());
			newMeta.setLocale(oldMeta.getLocale());
			newMeta.setCumulateImports(oldMeta.getCumulateImports());
			newMeta.setCbirAlgorithms(CoreActivator.getDefault().getCbirAlgorithms());
			newMeta.setIndexedTextFields(CoreActivator.getDefault().getIndexedTextFields(oldMeta));
			Map<String, LastDeviceImport> lastDeviceImports = oldMeta.getLastDeviceImport();
			if (lastDeviceImports != null)
				toBeStored.addAll(lastDeviceImports.values());
			List<String> watchedFolders = oldMeta.getWatchedFolder();
			if (watchedFolders != null)
				for (String id : watchedFolders) {
					WatchedFolder watchedFolder = dbManager.obtainById(WatchedFolder.class, id);
					String oldUri = watchedFolder.getUri();
					String oldVolume = watchedFolder.getVolume();
					String oldTarget = watchedFolder.getTargetDir();
					String source = migrateUri(oldUri, oldVolume);
					if (source.isEmpty()) {
						failedWatchedFolders.add(volumeManager.findFile(oldUri, oldVolume));
						continue;
					}
					if (watchedFolder.getTransfer()) {
						String target = migrate(oldTarget);
						if (target.isEmpty()) {
							failedWatchedFolders.add(volumeManager.findFile(oldUri, oldVolume));
							continue;
						}
						watchedFolder.setTargetDir(target);
					}
					watchedFolder.setUri(toUri(source));
					watchedFolder.setVolume(extractVolume(source));
					String oldFilters = watchedFolder.getFilters();
					if (oldFilters != null && !oldFilters.isEmpty())
						watchedFoldersWithFilters.add(volumeManager.findFile(oldUri, oldVolume));
					newDbManager.storeAndCommit(watchedFolder);
					newMeta.addWatchedFolder(id);
					watchedFolder.setUri(oldUri);
					watchedFolder.setVolume(oldVolume);
					watchedFolder.setTargetDir(oldTarget);
					if (monitor.isCanceled())
						return abort();
				}
			if (monitor.isCanceled())
				return abort();
			toBeStored.add(newMeta);
			newDbManager.safeTransaction(null, toBeStored);
			if (monitor.isCanceled())
				return abort();
			monitor.worked(1000);
			monitor.subTask(Messages.getString("MigrateOperation.migrate_groups")); //$NON-NLS-1$
			if (copyObjects(GroupImpl.class, 20000))
				return abort();
			GroupImpl folderGroup = newDbManager.obtainById(GroupImpl.class, Constants.GROUP_ID_FOLDERSTRUCTURE);
			List<Group> oldSubgroup = new ArrayList<Group>(folderGroup.getSubgroup());
			folderGroup.getSubgroup().clear();
			newDbManager.storeAndCommit(folderGroup);
			folderGroup.setSubgroup(oldSubgroup);
			if (copyObjects(ContactImpl.class, 1000))
				return abort();
			if (copyObjects(CreatorsContactImpl.class, 1000))
				return abort();
			if (copyObjects(LocationImpl.class, 1000))
				return abort();
			if (copyObjects(LocationShownImpl.class, 1000))
				return abort();
			if (copyObjects(LocationCreatedImpl.class, 1000))
				return abort();
			if (copyObjects(ArtworkOrObjectImpl.class, 1000))
				return abort();
			if (copyObjects(ArtworkOrObjectShownImpl.class, 1000))
				return abort();
			if (copyObjects(TrackRecordImpl.class, 1000))
				return abort();
			if (copyObjects(RegionImpl.class, 1000))
				return abort();
			if (copyObjects(ComposedToImpl.class, 1000))
				return abort();
			if (copyObjects(DerivedByImpl.class, 1000))
				return abort();
			int i = 0;
			List<Asset> assets = dbManager.obtainObjects(Asset.class);
			int na = assets.size();
			int work = 1000000;
			int incr = na == 0 ? work : work / na;
			for (Asset asset : assets) {
				monitor.subTask(NLS.bind(Messages.getString("MigrateOperation.migrating_image"), ++i, na)); //$NON-NLS-1$
				String oldUri = asset.getUri();
				String oldVolume = asset.getVolume();
				boolean remote = volumeManager.isRemote(asset);
				String migratedPath = migrateUri(oldUri, oldVolume);
				if (remote || !migratedPath.isEmpty()) {
					toBeStored.clear();
					toBeStored.add(asset);
					String assetId = asset.getStringId();
					String oldVoiceFileURI = asset.getVoiceFileURI();
					String oldVoiceVolume = asset.getVoiceVolume();
					String migratedUri = null;
					String migratedVolume = null;
					if (!remote) {
						migratedUri = toUri(migratedPath);
						asset.setUri(migratedUri);
						migratedVolume = extractVolume(migratedPath);
						asset.setVolume(migratedVolume);
						if (oldVoiceFileURI == null || !oldVoiceFileURI.startsWith("?")) { //$NON-NLS-1$
							String migratedVoicePath = migrateUri(oldVoiceFileURI, oldVoiceVolume);
							asset.setVoiceFileURI(migratedVoicePath.isEmpty() ? null : toUri(migratedVoicePath));
							asset.setVoiceVolume(extractVolume(migratedVoicePath));
						}
					}
					visited.add(assetId);
					List<Ghost_typeImpl> ghosts = dbManager.obtainObjects(Ghost_typeImpl.class,
							QueryField.NAME.getKey(), asset.getName(), QueryField.EQUALS);
					for (Ghost_typeImpl ghost : ghosts)
						if (ghost.getUri() != null && ghost.getUri().equals(oldUri) && migratedUri != null) {
							ghost.setUri(migratedUri);
							ghost.setVolume(migratedVolume);
							toBeStored.add(ghost);
						}
					MediaExtension[] mediaExtensions = asset.getMediaExtension();
					if (mediaExtensions != null)
						for (MediaExtension mediaExt : mediaExtensions)
							toBeStored.add(mediaExt);
					newDbManager.safeTransaction(null, toBeStored);
					newDbManager.createFolderHierarchy(asset, winTarget);
					asset.setUri(oldUri);
					asset.setVolume(oldVolume);
					asset.setVoiceFileURI(oldVoiceFileURI);
					asset.setVoiceVolume(oldVoiceVolume);
					for (Object obj : toBeStored)
						if (obj instanceof Ghost_typeImpl) {
							((Ghost_typeImpl) obj).setUri(oldUri);
							((Ghost_typeImpl) obj).setVolume(oldVolume);
						}
				}
				if (monitor.isCanceled())
					return abort();
				monitor.worked(incr);
				work -= incr;
			}
			monitor.worked(work);
			List<SmartCollectionImpl> collections = dbManager.obtainObjects(SmartCollectionImpl.class);
			work = 100000;
			na = collections.size();
			incr = na == 0 ? work : work / na;
			String uriKey = QueryField.URI.getKey() + '=';
			String volumeKey = QueryField.VOLUME.getKey() + '=';
			for (SmartCollectionImpl coll : collections) {
				if (coll.getSmartCollection_subSelection_parent() == null) {
					String id = coll.getStringId();
					if (!id.startsWith(uriKey) && !id.startsWith(volumeKey)) {
						toBeStored.clear();
						monitor.subTask(
								NLS.bind(Messages.getString("MigrateOperation.migrating_collections"), coll.getName())); //$NON-NLS-1$
						transferSmartCollection(toBeStored, coll, null);
						newDbManager.safeTransaction(null, toBeStored);
						if (monitor.isCanceled())
							return abort();
					}
				}
				monitor.worked(incr);
				work -= incr;
			}
			monitor.worked(work);
			monitor.subTask(Messages.getString("MigrateOperation.migrating_presentations")); //$NON-NLS-1$
			if (copyObjects(SlideShowImpl.class, 5000))
				return abort();
			if (copyObjects(SlideImpl.class, 50000))
				return abort();
			if (copyObjects(ExhibitImpl.class, 50000))
				return abort();
			if (copyObjects(WallImpl.class, 5000))
				return abort();
			List<ExhibitionImpl> exhibitions = dbManager.obtainObjects(ExhibitionImpl.class);
			work = 5000;
			na = exhibitions.size();
			incr = na == 0 ? work : work / na;
			for (ExhibitionImpl exhibition : exhibitions) {
				String oldFolder = exhibition.getOutputFolder();
				if (oldFolder != null) {
					exhibition.setOutputFolder(migrate(oldFolder));
					newDbManager.storeAndCommit(exhibition);
					exhibition.setOutputFolder(oldFolder);
				} else
					newDbManager.storeAndCommit(exhibition);
				if (monitor.isCanceled())
					return abort();
				monitor.worked(incr);
				work -= incr;
			}
			monitor.worked(work);
			if (copyObjects(WebExhibitImpl.class, 50000))
				return abort();
			if (copyObjects(WebParameterImpl.class, 5000))
				return abort();
			if (copyObjects(StoryboardImpl.class, 5000))
				return abort();
			List<WebGalleryImpl> galleries = dbManager.obtainObjects(WebGalleryImpl.class);
			work = 5000;
			na = galleries.size();
			incr = na == 0 ? work : work / na;
			for (WebGalleryImpl exhibition : galleries) {
				String oldFolder = exhibition.getOutputFolder();
				if (oldFolder != null) {
					exhibition.setOutputFolder(migrate(oldFolder));
					newDbManager.storeAndCommit(exhibition);
					exhibition.setOutputFolder(oldFolder);
				} else
					newDbManager.storeAndCommit(exhibition);
				if (monitor.isCanceled())
					return abort();
				monitor.worked(incr);
				work -= incr;
			}
			monitor.subTask(Messages.getString("MigrateOperation.migrating_other_entries")); //$NON-NLS-1$
			monitor.worked(work);
			if (copyObjects(PageLayoutImpl.class, 5000))
				return abort();
			List<BookmarkImpl> bookmarks = dbManager.obtainObjects(BookmarkImpl.class);
			work = 20000;
			na = galleries.size();
			incr = na == 0 ? work : work / na;
			for (BookmarkImpl bookmark : bookmarks) {
				String catFile = bookmark.getCatFile();
				if (catFile == null || new File(catFile).equals(dbManager.getFile())) {
					bookmark.setCatFile(null);
					newDbManager.storeAndCommit(bookmark);
				}
				if (monitor.isCanceled())
					return abort();
				monitor.worked(incr);
				work -= incr;
			}
			monitor.worked(work);
			newDbManager.safeTransaction(null, newMeta);
			if (!failedWatchedFolders.isEmpty() || watchedFoldersWithFilters.isEmpty() || oldMeta.getReadonly()) {
				StringBuilder sb = new StringBuilder();
				sb.append(Messages.getString("MigrateOperation.migration_completed")); //$NON-NLS-1$
				if (!failedWatchedFolders.isEmpty()) {
					sb.append(Messages.getString("MigrateOperation.failed_watched_folders")); //$NON-NLS-1$
					for (URI uri : failedWatchedFolders)
						sb.append("\n\t").append(new File(uri).getPath()); //$NON-NLS-1$
				}
				if (!watchedFoldersWithFilters.isEmpty()) {
					sb.append(Messages.getString("MigrateOperation.watched_folders_with_filters")); //$NON-NLS-1$
					for (URI uri : watchedFoldersWithFilters)
						sb.append("\n\t").append(new File(uri).getPath()); //$NON-NLS-1$
				}
				if (oldMeta.getReadonly())
					sb.append(Messages.getString("MigrateOperation.read_only")); //$NON-NLS-1$
				finalMessage = sb.toString();
				severity = MessageDialog.WARNING;
			} else {
				finalMessage = Messages.getString("MigrateOperation.migration_completed"); //$NON-NLS-1$
				severity = MessageDialog.INFORMATION;
			}
			return close(info);
		} finally {
			newDbManager.close(CatalogListener.NORMAL);
		}
	}

	@SuppressWarnings("unchecked")
	private boolean copyObjects(@SuppressWarnings("rawtypes") Class clazz, int work) {
		@SuppressWarnings("rawtypes")
		List objects = dbManager.obtainObjects(clazz);
		int na = objects.size();
		int incr = na == 0 ? work : work / na;
		for (Object obj : objects) {
			newDbManager.store(obj);
			if (monitor.isCanceled())
				return true;
			monitor.worked(incr);
			work -= incr;
		}
		newDbManager.commit();
		monitor.worked(work);
		return false;
	}

	private void transferSmartCollection(List<Object> toBeStored, SmartCollection coll, SmartCollection newParent) {
		PostProcessor postProcessor = coll.getPostProcessor();
		SmartCollectionImpl newColl = new SmartCollectionImpl(coll.getName(), coll.getSystem(), coll.getAlbum(),
				coll.getAdhoc(), coll.getNetwork(), coll.getDescription(), coll.getColorCode(),
				coll.getLastAccessDate(), coll.getGeneration(), coll.getPerspective(), coll.getShowLabel(),
				coll.getLabelTemplate(), coll.getFontSize(), postProcessor);
		String id = coll.getStringId();
		newColl.setStringId(id);
		toBeStored.add(newColl);
		if (newParent != null) {
			newParent.addSubSelection(newColl);
			newColl.setSmartCollection_subSelection_parent(newParent);
		} else
			newColl.setGroup_rootCollection_parent(coll.getGroup_rootCollection_parent());
		if (postProcessor instanceof IPostProcessor) {
			PostProcessor newProcessor = ((IPostProcessor) postProcessor).clone();
			newProcessor.setStringId(postProcessor.getStringId());
			newProcessor.setSmartCollection_parent(newColl);
			newColl.setPostProcessor(newProcessor);
			toBeStored.add(newProcessor);
		}
		for (Criterion crit : coll.getCriterion()) {
			Object value = crit.getValue();
			Criterion newCrit = new CriterionImpl(crit.getField(), crit.getSubfield(), value, crit.getRelation(),
					crit.getAnd());
			newCrit.setStringId(crit.getStringId());
			newColl.addCriterion(newCrit);
			newCrit.setSmartCollection_parent(newColl);
			toBeStored.add(newCrit);
			// Utilities.addCustomType(newCrit.getValue(), toBeStored);
			if (!coll.getSystem()) {
				if (QueryField.URI.getKey().equals(crit.getField())) {
					String uri = (String) value;
					String path;
					if (uri.startsWith(VolumeManager.FILE)) {
						try {
							path = new File(new URI(uri)).getPath();
						} catch (URISyntaxException e) {
							path = uri.substring(VolumeManager.FILE.length()).replace('/', File.separatorChar);
						}
						String migrated = migrate(path);
						if (migrated.isEmpty()) {
							if (coll.getSmartCollection_subSelection_parent() == null) {
								GroupImpl group = newDbManager.obtainById(GroupImpl.class,
										coll.getGroup_rootCollection_parent());
								group.removeRootCollection(id);
								newDbManager.storeAndCommit(group);
							}
							return;
						}
						String migratedUri = toUri(migrated);
						newCrit.setValue(migratedUri);
					}
				}
			}
		}
		for (SortCriterion crit : coll.getSortCriterion()) {
			SortCriterion newCrit = new SortCriterionImpl(crit.getField(), crit.getSubfield(), crit.getDescending());
			newCrit.setStringId(crit.getStringId());
			newColl.addSortCriterion(newCrit);
			newCrit.setSmartCollection_parent(newColl);
			toBeStored.add(newCrit);
		}
		for (SmartCollection subColl : coll.getSubSelection())
			transferSmartCollection(toBeStored, subColl, newColl);
		AomList<String> assets = coll.getAsset();
		if (assets != null) {
			List<String> newAssets = new ArrayList<String>(assets.size());
			for (String asset : assets)
				if (newDbManager.obtainById(AssetImpl.class, asset) != null)
					newAssets.add(asset);
			newColl.setAsset(newAssets);
		}
	}

	private static String toUri(String source) {
		try {
			String sp = source.replace('\\', '/');
			if (!sp.startsWith("/")) //$NON-NLS-1$
				sp = "/" + sp; //$NON-NLS-1$
			if (sp.startsWith("//")) //$NON-NLS-1$
				sp = "//" + sp; //$NON-NLS-1$
			return new URI(Constants.FILESCHEME, null, sp, null).toString();
		} catch (URISyntaxException e) {
			// should not happen
			return source;
		}
	}

	private String extractVolume(String migratedPath) {
		if (migratedPath == null)
			return null;
		if (winTarget) {
			if (currentVolume != null && !currentVolume.isEmpty())
				return currentVolume;
		} else if (migratedPath.startsWith("/")) //$NON-NLS-1$
			return ((VolumeManager) volumeManager).extractLinuxPath(migratedPath);
		return null;
	}

	private String migrateUri(String sourceUri, String volume) {
		if (sourceUri == null || !sourceUri.startsWith(VolumeManager.FILE))
			return ""; //$NON-NLS-1$
		try {
			String path = new File(new URI(sourceUri)).getPath();
			if (sourceUri.endsWith("/") && !path.endsWith(File.separator)) //$NON-NLS-1$
				path += File.separator;
			if (Constants.WIN32 && volume != null) {
				int i = path.indexOf(':');
				if (i == 1)
					return migrate(volume + path.substring(1));
			}
			return migrate(path);
		} catch (URISyntaxException e) {
			return ""; //$NON-NLS-1$
		}
	}

	private String migrate(String s) {
		currentVolume = null;
		for (int i = 0; i < rules.length; i++) {
			Pattern pattern = patterns[i];
			MigrationRule rule = rules[i];
			Matcher matcher = pattern.matcher(s);
			if (matcher.matches()) {
				currentVolume = rule.getTargetVolume();
				String targetPattern = rule.getTargetPattern();
				if (targetPattern == null || targetPattern.isEmpty())
					break;
				StringBuffer sb = new StringBuffer();
				matcher.appendReplacement(sb, targetPattern);
				String result = sb.toString();
				switch (fileSeparatorPolicy) {
				case 1:
					return result.replace('\\', '/');
				case 2:
					return result.replace('/', '\\');
				default:
					return result;
				}
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @return finalMessage
	 */
	public String getFinalMessage() {
		return finalMessage;
	}

	/**
	 * @return severity
	 */
	public int getSeverity() {
		return severity;
	}

	protected void handleResume(Meta meta, int code, int i, IAdaptable info) {
		// do nothing
	}

}
