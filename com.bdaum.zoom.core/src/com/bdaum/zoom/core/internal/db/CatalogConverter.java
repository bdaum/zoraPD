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
 * (c) 2009-2015 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.core.internal.db;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Range;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.image.ImageConstants;

public class CatalogConverter {

	private static final String ADOBE_DNG_CONVERTER = "Adobe DNG Converter"; //$NON-NLS-1$

	public static void convert(IDbManager db, IProgressMonitor monitor) {
		// Fix up wrong file types (format) in cat version 0
		if (db.isReadOnly())
			return;
		db.repairCatalog();
		Meta meta = db.getMeta(false);
		int version = meta.getVersion();
		if (version == 0) {
			monitor.subTask(Messages.CatalogConverter_image_ID);
			List<AssetImpl> set = db.obtainAssets();
			int c = 0;
			for (Asset asset : set) {
				boolean update = false;
				String stringId = asset.getStringId();
				if (stringId.indexOf('-') < 0) {
					asset.setOriginalImageId(stringId);
					update = true;
				}
				String format = asset.getFormat();
				if (format == null || format.isEmpty()) {
					String uri = asset.getUri();
					String ext = Core.getFileExtension(uri);
					String f = ImageConstants.getRawFormatDescription(ext);
					if (f != null) {
						asset.setFormat(f);
						asset.setMimeType("image/x-raw"); //$NON-NLS-1$
						asset.setDigitalZoomRatio(Double.NaN);
						update = true;
					}
				}
				if (update) {
					db.store(asset);
					++c;
					if (c % 100 == 0)
						db.commit();
				}
			}
		}
		if (version <= 2) {
			monitor.subTask(Messages.CatalogConverter_full_text);
			// Recreate Lucene index for full text search
			db.resetLuceneIndex();
			// Fix up SmartCollections
			for (SmartCollectionImpl coll : db.obtainObjects(SmartCollectionImpl.class)) {
				Iterator<SmartCollection> it = coll.getSubSelection().iterator();
				boolean changed = false;
				while (it.hasNext()) {
					SmartCollection child = it.next();
					if (child == null) {
						it.remove();
						changed = true;
					}
				}
				if (changed)
					db.storeAndCommit(coll);
			}
			// Fix up duplicate derivations
			monitor.subTask(Messages.CatalogConverter_duplicated_relationships);
			List<DerivedByImpl> derived = db.obtainObjects(DerivedByImpl.class);
			Set<String> dupSet = new HashSet<String>(derived.size() * 5 / 4);
			DerivedByImpl[] rels = derived.toArray(new DerivedByImpl[derived.size()]);
			for (DerivedByImpl rel : rels) {
				String idid = rel.getOriginal() + rel.getDerivative();
				if (dupSet.contains(idid)) {
					db.delete(rel);
					db.commit();
				} else {
					AssetImpl original = db.obtainAsset(rel.getOriginal());
					if (original == null) {
						db.delete(rel);
						db.commit();
						continue;
					}
					if (ImageConstants.isRaw(original.getUri(), false)) {
						AssetImpl derivate = db.obtainAsset(rel.getDerivative());
						if (derivate == null) {
							db.delete(rel);
							db.commit();
							continue;
						}
						if (ImageConstants.isDng(derivate.getUri())) {
							if (!ADOBE_DNG_CONVERTER.equals(rel.getTool())) {
								rel.setTool(ADOBE_DNG_CONVERTER);
								db.storeAndCommit(rel);
							}
							dupSet.add(idid);
						}
					}
				}
			}
			// Clean-up composites
			List<ComposedToImpl> composites = db.obtainObjects(ComposedToImpl.class);
			ComposedToImpl[] comps = composites.toArray(new ComposedToImpl[composites.size()]);
			for (ComposedToImpl rel : comps) {
				AssetImpl composite = db.obtainAsset(rel.getComposite());
				if (composite == null) {
					db.delete(rel);
					db.commit();
					continue;
				}
				for (String compId : rel.getComponent())
					if (db.obtainAsset(compId) == null) {
						db.delete(rel);
						db.commit();
						break;
					}
			}
		}
		if (version <= 3) {
			monitor.subTask(Messages.CatalogConverter_fast_album_access);
			List<SmartCollectionImpl> set = db.obtainObjects(SmartCollectionImpl.class, "album", true, //$NON-NLS-1$
					QueryField.EQUALS);
			for (SmartCollectionImpl sm : set) {
				if (sm.getCriterion().size() == 1) {
					List<AssetImpl> assets = db.obtainObjects(AssetImpl.class, "album", sm.getName(), //$NON-NLS-1$
							QueryField.EQUALS);
					List<String> ids = new ArrayList<String>(assets.size());
					for (AssetImpl asset : assets)
						ids.add(asset.getStringId());
					sm.setAsset(ids);
					Criterion crit = sm.getCriterion(0);
					crit.setRelation(QueryField.XREF);
					db.store(crit);
					Object value = crit.getValue();
					if (value instanceof Range)
						db.store(value);
					db.store(sm);
				}
			}
			db.commit();
		}
		if (version <= 4) {
			monitor.subTask(Messages.CatalogConverter_overlapping_timeline);
			List<SmartCollectionImpl> set = db.obtainObjects(SmartCollectionImpl.class, false, "system", true, //$NON-NLS-1$
					QueryField.EQUALS, Constants.OID, IDbManager.DATETIMEKEY, QueryField.STARTSWITH);
			boolean added = false;
			for (SmartCollectionImpl sm : set) {
				if (sm.getCriterion().size() == 1) {
					String id = sm.getStringId();
					int p = id.indexOf('-');
					if (p > 0 && p == id.lastIndexOf('-')) {
						Criterion criterion = sm.getCriterion(0);
						if (criterion != null) {
							Object value = criterion.getValue();
							if (value instanceof Range) {
								Range range = (Range) value;
								Date d1 = (Date) range.getFrom();
								Date d2 = (Date) range.getTo();
								GregorianCalendar from = new GregorianCalendar();
								from.setTime(d1);
								GregorianCalendar to = new GregorianCalendar(from.get(Calendar.YEAR),
										from.get(Calendar.MONTH), from.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59,
										59);
								Date d3 = to.getTime();
								if (!d3.equals(d2)) {
									range.setTo(d3);
									db.store(range);
									added = db.addDirtyCollection(id);
								}
							}
						}
					}
				}
			}
			if (added) {
				meta.setCleaned(false);
				db.store(meta);
			}
			db.commit();
		}
		if (version <= 6) {
			// Update timeline collections to recognize IPTC DateCreated
			monitor.subTask(Messages.CatalogConverter_iptc_dateCreated);
			List<Object> toBeDeleted = new ArrayList<Object>();
			List<Object> toBeStored = new ArrayList<Object>();
			for (SmartCollectionImpl sm : db.<SmartCollectionImpl>obtainObjects(SmartCollectionImpl.class, false,
					"system", true, //$NON-NLS-1$
					QueryField.EQUALS, Constants.OID, IDbManager.DATETIMEKEY, QueryField.STARTSWITH)) {
				if (!sm.getCriterion().isEmpty()) {
					int i = 0;
					Iterator<Criterion> it = sm.getCriterion().iterator();
					while (it.hasNext()) {
						Criterion criterion = it.next();
						if (i == 0) {
							if (criterion.getField().equals(QueryField.EXIF_DATETIMEORIGINAL.getKey())) {
								criterion.setField(QueryField.IPTC_DATECREATED.getKey());
								criterion.setAnd(false);
								toBeStored.add(criterion);
							}
						} else {
							toBeDeleted.add(criterion);
							it.remove();
						}
						++i;
					}
					boolean iptc = false;
					boolean descending = true;
					boolean exif = false;
					Iterator<SortCriterion> iterator = sm.getSortCriterion().iterator();
					while (iterator.hasNext()) {
						SortCriterion sortCriterion = iterator.next();
						if (sortCriterion.getField().equals(QueryField.EXIF_DATETIMEORIGINAL.getKey())) {
							exif = true;
							toBeDeleted.add(sortCriterion);
							descending = sortCriterion.getDescending();
							iterator.remove();
						} else if (sortCriterion.getField().equals(QueryField.IPTC_DATECREATED.getKey()))
							iptc = true;
					}
					if (!iptc && exif) {
						SortCriterionImpl sortCrit = new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null,
								descending);
						toBeStored.add(sortCrit);
						sm.addSortCriterion(sortCrit);
					}
					toBeStored.add(sm);
					db.store(sm);
				}
			}
			for (Object object : toBeDeleted)
				db.delete(object);
			for (Object object : toBeStored)
				db.store(object);
			db.commit();
			int i = 0;
			for (AssetImpl asset : db.obtainAssets()) {
				if (asset.getDateCreated() == null) {
					asset.setDateCreated(asset.getDateTimeOriginal());
					db.store(asset);
					if (++i >= 500) {
						db.commit();
						i = 0;
					}
				}
			}
			db.commit();
		}
		if (version <= 8) {
			monitor.subTask(Messages.CatalogConverter_world_region);
			int i = 0;
			List<LocationImpl> set = db.obtainObjects(LocationImpl.class);
			for (LocationImpl loc : set.toArray(new LocationImpl[set.size()])) {
				if (Utilities.completeLocation(db, loc)) {
					db.store(loc);
					if (++i >= 500) {
						db.commit();
						i = 0;
					}
				}
			}
			db.commit();
		}
		if (version <= 9) {
			monitor.subTask(Messages.CatalogConverter_consolidate_album_count);
			List<SmartCollection> albums = db.obtainObjects(SmartCollection.class, "album", true, QueryField.EQUALS); //$NON-NLS-1$
			for (SmartCollection album : albums) {
				List<String> assetIds = album.getAsset();
				if (assetIds != null && !assetIds.isEmpty()) {
					List<String> newIds = new ArrayList<String>(assetIds.size());
					for (String id : assetIds)
						if (db.obtainAsset(id) != null)
							newIds.add(id);
					if (newIds.size() < assetIds.size()) {
						album.setAsset(newIds);
						db.storeAndCommit(album);
					}
				}
			}
		}
		if (version <= 10) {
			monitor.subTask(Messages.CatalogConverter_fix_watched_folder_ids);
			List<Object> tobeDeleted = new ArrayList<Object>();
			List<Object> tobeStored = new ArrayList<Object>();
			List<String> watchedFolders = meta.getWatchedFolder();
			List<String> newWatchedFolders = new ArrayList<String>();
			IVolumeManager volumeManager = Core.getCore().getVolumeManager();
			for (WatchedFolderImpl wf : db.obtainObjects(WatchedFolderImpl.class)) {
				String oldId = wf.getStringId();
				String uri = wf.getUri();
				try {
					File file = new File(new URI(uri));
					String volume = wf.getVolume();
					if (volume == null)
						wf.setVolume(volume = volumeManager.getVolumeForFile(file));
					String id = Utilities.computeWatchedFolderId(file, volume);
					wf.setStringId(id);
					if (watchedFolders != null && watchedFolders.contains(oldId)) {
						newWatchedFolders.add(id);
						tobeStored.add(wf);
					} else
						tobeDeleted.add(wf);
				} catch (URISyntaxException e) {
					tobeDeleted.add(wf);
				}
			}
			meta.setWatchedFolder(newWatchedFolders);
			tobeStored.add(meta);
			db.safeTransaction(tobeDeleted, tobeStored);
		}
		meta.setVersion(db.getVersion());
		db.storeAndCommit(meta);
	}
}
