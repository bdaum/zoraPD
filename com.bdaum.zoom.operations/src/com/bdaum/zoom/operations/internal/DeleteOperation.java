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
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.Ghost_typeImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.WallImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.core.trash.Trash;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;

@SuppressWarnings("restriction")
public class DeleteOperation extends DbOperation {

	private List<Asset> assets;
	private boolean includingFiles;
	private ImportConfiguration configuration;
	private List<Ghost_typeImpl> ghosts = new ArrayList<Ghost_typeImpl>();
	protected Asset asset;
	private final List<SlideImpl> slides;
	private final List<ExhibitImpl> exhibits;
	private final List<WebExhibitImpl> webexhibits;

	public DeleteOperation(List<Asset> assets, boolean includingFiles, List<SlideImpl> slides,
			List<ExhibitImpl> exhibits, List<WebExhibitImpl> webexhibits, ImportConfiguration configuration) {
		super(Messages.getString("DeleteOperation.Delete_images")); //$NON-NLS-1$
		this.slides = slides;
		this.exhibits = exhibits;
		this.webexhibits = webexhibits;
		this.configuration = configuration;
		this.assets = assets;
		this.includingFiles = includingFiles;
		this.date = new Date();
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, final IAdaptable info) throws ExecutionException {
		Set<String> volumes = null;
		String errand = null;
		int errs = 0;
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		int size = assets.size();
		init(aMonitor, size + 1);
		if ((webexhibits != null && !webexhibits.isEmpty()) || (slides != null && !slides.isEmpty())
				|| (exhibits != null && !exhibits.isEmpty())) {
			final Map<String, SlideShowImpl> showMap = new HashMap<String, SlideShowImpl>();
			final Map<String, WallImpl> wallMap = new HashMap<String, WallImpl>();
			final Map<String, StoryboardImpl> storyMap = new HashMap<String, StoryboardImpl>();
			if (slides != null) {
				for (SlideImpl slide : slides) {
					String slideshowId = slide.getSlideShow_entry_parent();
					SlideShowImpl show = showMap.get(slideshowId);
					if (show == null) {
						show = dbManager.obtainById(SlideShowImpl.class, slideshowId);
						if (show != null)
							showMap.put(slideshowId, show);
					}
					if (show != null)
						show.removeEntry(slide.getStringId());
				}
			}
			if (exhibits != null) {
				for (ExhibitImpl exhibit : exhibits) {
					String wallId = exhibit.getWall_exhibit_parent();
					WallImpl wall = wallMap.get(wallId);
					if (wall == null) {
						wall = dbManager.obtainById(WallImpl.class, wallId);
						if (wall != null)
							wallMap.put(wallId, wall);
					}
					if (wall != null)
						wall.removeExhibit(exhibit.getStringId());
				}
			}
			if (webexhibits != null) {
				for (WebExhibitImpl webexhibit : webexhibits) {
					String storyBoardId = webexhibit.getStoryboard_exhibit_parent();
					StoryboardImpl story = storyMap.get(storyBoardId);
					if (story == null) {
						story = dbManager.obtainById(StoryboardImpl.class, storyBoardId);
						if (story != null)
							storyMap.put(storyBoardId, story);
					}
					if (story != null)
						story.removeExhibit(webexhibit.getStringId());
				}
			}
			dbManager.safeTransaction(() -> {
				if (slides != null)
					for (SlideImpl slide : slides)
						dbManager.delete(slide);
				if (exhibits != null)
					for (ExhibitImpl exhibit : exhibits)
						dbManager.delete(exhibit);
				if (webexhibits != null)
					for (WebExhibitImpl webexhibit : webexhibits)
						dbManager.delete(webexhibit);
				for (SlideShowImpl show : showMap.values())
					dbManager.store(show);
				for (WallImpl wall : wallMap.values())
					dbManager.store(wall);
				for (StoryboardImpl story : storyMap.values())
					dbManager.store(story);
			});
		}
		openIndexWriter();
		try {
			for (Asset a : assets) {
				if (a.getFileState() != IVolumeManager.PEER) {
					boolean deleted = false;
					if (includingFiles) {
						URI uri = volumeManager.findFile(a);
						if (uri != null) {
							URI fileURI = volumeManager.findExistingFile(a, true);
							boolean canWrite = fileURI != null ? new File(fileURI).canWrite() : false;
							if (canWrite)
								deleted = deleteAsset(a, true, false);
							else {
								String volume = a.getVolume();
								if (volume != null && !volume.isEmpty()) {
									if (volumes == null)
										volumes = new HashSet<String>();
									volumes.add(volume);
								}
								if (errs++ == 0)
									errand = a.getUri();
							}
						}
					} else {
						deleteAsset(a, false, volumeManager.findExistingFile(a, true) != null);
						deleted = true;
					}
					if (aMonitor.isCanceled())
						return abort();
					if (deleted)
						dbManager.markSystemCollectionsForPurge(a);
					Thread.yield();
				}
			}
		} finally {
			closeIndex();
			fireAssetsModified(new BagChange<>(null, null, assets, null), QueryField.ALL);
			final IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
			if (errorHandler != null) {
				final int ferrs = errs;
				final Set<String> fvolumes = volumes;
				final String ferrand = errand;
				Shell shell = info.getAdapter(Shell.class);
				if (shell != null && !shell.isDisposed()) {
					Display display = shell.getDisplay();
					display.asyncExec(() -> {
						if (!shell.isDisposed()) {
							if (ferrs > 0) {
								String msg;
								if (ferrs == 1) {
									if (fvolumes == null)
										msg = NLS.bind(Messages.getString("DeleteOperation.file_deleted_no_volume"), //$NON-NLS-1$
												ferrand);
									msg = NLS.bind(Messages.getString("DeleteOperation.File_offline"), //$NON-NLS-1$
											ferrand, fvolumes.toArray()[0]);
								} else {
									if (fvolumes == null)
										msg = NLS.bind(Messages.getString("DeleteOperation.files_deleted_no_volume"), //$NON-NLS-1$
												ferrs);
									msg = NLS.bind(Messages.getString("DeleteOperation.Files_offline"), //$NON-NLS-1$
											ferrs, Core.toStringList(fvolumes.toArray(), ", ")); //$NON-NLS-1$

								}
								errorHandler.showInformation(Messages.getString("DeleteOperation.Unable_to_delete"), //$NON-NLS-1$
										msg, info);
							}
						}
					});
				}
			}
			assets = null;
		}
		return close(info);
	}

	private boolean deleteAsset(final Asset anAsset, final boolean files, final boolean needsGhost) {
		return storeSafely(() -> doDeleteAsset(anAsset, files, needsGhost), 1);
	}

	private void doDeleteAsset(Asset anAsset, boolean files, boolean needsGhost) {
		String assetId = anAsset.getStringId();
		Trash trashItem = new Trash(opId, anAsset, files, date);
		List<IdentifiableObject> tobeStored = new ArrayList<IdentifiableObject>();
		dbManager.delete(anAsset);
		if (needsGhost && !files) {
			Ghost_typeImpl ghost = new Ghost_typeImpl(anAsset.getName(), anAsset.getUri(), anAsset.getVolume());
			dbManager.store(ghost);
			ghosts.add(ghost);
		}
		// Albums
		String[] albums = anAsset.getAlbum();
		if (albums != null)
			for (String name : albums) {
				List<SmartCollectionImpl> allAlbums = dbManager.obtainObjects(SmartCollectionImpl.class, false, "album", //$NON-NLS-1$
						true, QueryField.EQUALS, "name", name, QueryField.EQUALS); //$NON-NLS-1$
				for (SmartCollectionImpl album : allAlbums)
					if (album.removeAsset(assetId)) {
						tobeStored.add(album);
						if (album.getSystem())
							dbManager.addDirtyCollection(album.getStringId());
					}
			}
		// Relations
		List<ComposedToImpl> composites = dbManager.obtainStruct(ComposedToImpl.class, null, false, "composite", //$NON-NLS-1$
				assetId, false);
		trashItem.addObjects(composites);
		List<ComposedToImpl> components = dbManager.obtainStruct(ComposedToImpl.class, null, false, "component", //$NON-NLS-1$
				assetId, true);
		for (IdentifiableObject composedTo : components)
			if (((ComposedToImpl) composedTo).getComponent().size() <= 1)
				trashItem.addObject(composedTo);
			else {
				trashItem.addComposition(composedTo.getStringId());
				((ComposedToImpl) composedTo).removeComponent(assetId);
				tobeStored.add(composedTo);
			}
		List<DerivedByImpl> derivatives = dbManager.obtainStruct(DerivedByImpl.class, null, false, "derivative", //$NON-NLS-1$
				assetId, false);
		trashItem.addObjects(derivatives);
		for (DerivedByImpl rel : derivatives)
			if (rel.getOriginal().indexOf(':') < 0)
				tobeStored.add(new DerivedByImpl(rel.getRecipe(), rel.getParameterFile(), rel.getTool(), rel.getDate(),
						anAsset.getUri(), rel.getOriginal()));
		List<DerivedByImpl> originals = dbManager.obtainStruct(DerivedByImpl.class, null, false, "original", assetId, //$NON-NLS-1$
				false);
		trashItem.addObjects(originals);
		for (DerivedByImpl rel : originals)
			if (rel.getDerivative().indexOf(':') < 0)
				tobeStored.add(new DerivedByImpl(rel.getRecipe(), rel.getParameterFile(), rel.getTool(), rel.getDate(),
						rel.getDerivative(), anAsset.getUri()));
		// Structures
		List<LocationCreatedImpl> locs = dbManager.obtainStruct(LocationCreatedImpl.class, assetId, true, null, null,
				false);
		for (IdentifiableObject loc : locs)
			if (((LocationCreatedImpl) loc).getAsset().size() <= 1)
				trashItem.addObject(loc);
			else {
				trashItem.addLocationCreated(loc.getStringId());
				((LocationCreatedImpl) loc).removeAsset(assetId);
				tobeStored.add(loc);
			}
		List<LocationShownImpl> locsShown = dbManager.obtainStruct(LocationShownImpl.class, assetId, false, null, null,
				false);
		trashItem.addObjects(locsShown);
		List<CreatorsContactImpl> contacts = dbManager.obtainStruct(CreatorsContactImpl.class, assetId, false, null,
				null, false);
		for (IdentifiableObject contact : contacts)
			if (((CreatorsContactImpl) contact).getAsset().size() <= 1)
				trashItem.addObject(contact);
			else {
				trashItem.addCreatorsContact(contact.getStringId());
				((CreatorsContactImpl) contact).removeAsset(assetId);
				tobeStored.add(contact);
			}
		List<ArtworkOrObjectShownImpl> artworks = dbManager.obtainStruct(ArtworkOrObjectShownImpl.class, assetId, false,
				null, null, false);
		trashItem.addObjects(artworks);
		// Faces
		List<RegionImpl> regions = dbManager.obtainObjects(RegionImpl.class, "asset_person_parent", assetId, //$NON-NLS-1$
				QueryField.EQUALS);
		for (IdentifiableObject obj : regions)
			dbManager.delete(obj);
		// Tracks
		List<TrackRecordImpl> trackRecords = dbManager.obtainObjects(TrackRecordImpl.class, "asset_track_parent", //$NON-NLS-1$
				assetId, QueryField.EQUALS);
		trashItem.addObjects(trackRecords);
		dbManager.storeTrash(trashItem);
		List<IdentifiableObject> objects = trashItem.getObjects();
		if (objects != null)
			for (IdentifiableObject obj : objects)
				dbManager.delete(obj);
		for (IdentifiableObject obj : tobeStored)
			dbManager.store(obj);
		deleteIndexEntry(assetId);
	}

	@Override
	public boolean canRedo() {
		return false;
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return null;
	}

	@Override
	public boolean canUndo() {
		return !dbManager.getTrash(Trash.class, opId).isEmpty();
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		List<Trash> set = dbManager.getTrash(Trash.class, opId);
		initUndo(aMonitor, set.size() + ghosts.size());
		boolean changed = false;
		openIndexWriter();
		try {
			for (Trash trash : set) {
				final Trash t = trash;
				if (!storeSafely(() -> asset = t.restore(dbManager, iw, status), 1))
					break;
				changed |= updateFolderHierarchies(asset, true, configuration.timeline, configuration.locations, false);
				aMonitor.worked(1);
			}
			storeSafely(ghosts.toArray(), 1);
		} finally {
			closeIndex();
			if (changed)
				fireStructureModified();
			fireAssetsModified(new BagChange<>(assets, null, null, null), null);
		}
		return close(info);
	}

	public int getExecuteProfile() {
		int profile = IProfiledOperation.CONTENT | IProfiledOperation.INDEX;
		if (includingFiles)
			profile |= IProfiledOperation.FILE;
		return profile;
	}

	public int getUndoProfile() {
		int profile = IProfiledOperation.CONTENT | IProfiledOperation.INDEX;
		if (includingFiles)
			profile |= IProfiledOperation.FILE;
		return profile;
	}

	@Override
	public int getPriority() {
		return assets.size() > 1 ? Job.LONG : Job.SHORT;
	}

}
