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
 * (c) 2009-2015 Berthold Daum  
 */
package com.bdaum.zoom.core.internal.db;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Range;
import com.bdaum.zoom.core.TetheredRange;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.mtp.IRootManager;

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
					if (++c % 100 == 0)
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
					if (!db.exists(AssetImpl.class, compId)) {
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
							Object vto = criterion.getTo();
							if (vto != null) {
								Date d1 = (Date) value;
								Date d2 = (Date) vto;
								GregorianCalendar from = new GregorianCalendar();
								from.setTime(d1);
								GregorianCalendar to = new GregorianCalendar(from.get(Calendar.YEAR),
										from.get(Calendar.MONTH), from.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59,
										59);
								Date d3 = to.getTime();
								if (!d3.equals(d2)) {
									criterion.setTo(d3);
									db.store(criterion);
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
						if (!db.exists(AssetImpl.class, id))
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
			IRootManager volumeManager = Core.getCore().getVolumeManager();
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
		if (version <= 13) {
			monitor.subTask(Messages.CatalogConverter_restore_directory_entries);
			Map<String, Asset> uris = new HashMap<>(257);
			List<AssetImpl> assets = db.obtainAssets();
			for (AssetImpl asset : assets) {
				String uri = asset.getUri();
				int p = uri.lastIndexOf('/');
				if (p > 0)
					uris.put(uri.substring(0, p), asset);
			}
			for (Asset asset : uris.values())
				db.createFolderHierarchy(asset);
			String locationOption = db.getMeta(true).getLocationFolders();
			for (LocationImpl loc : db.obtainObjects(LocationImpl.class)) {
				if (Utilities.completeLocation(db, loc))
					db.storeAndCommit(loc);
				db.createLocationFolders(loc, locationOption);
			}
			monitor.subTask(Messages.CatalogConverter_prune_system_collections);
			List<SmartCollectionImpl> set = db.obtainObjects(SmartCollectionImpl.class, Constants.OID,
					IDbManager.LOCATIONKEY, QueryField.STARTSWITH);
			List<Object> toBeDeleted = new ArrayList<>();
			for (SmartCollectionImpl sm : set)
				if (sm.getName() == null || sm.getName().isEmpty())
					Utilities.deleteCollection(sm, true, toBeDeleted);
			db.safeTransaction(toBeDeleted, null);
			db.pruneEmptySystemCollections(monitor, true);
		}
		if (version <= 14) {
			int c = 0;
			IVolumeManager volumeManager = Core.getCore().getVolumeManager();
			monitor.subTask(Messages.CatalogConverter_fix_raw_file_properties);
			List<AssetImpl> assets = db.obtainAssets();
			for (AssetImpl asset : assets) {
				String uri = asset.getUri();
				boolean isDng = ImageConstants.isDng(uri);
				boolean isRaw = ImageConstants.isRaw(uri, false);
				if (isDng || isRaw) {
					File file = volumeManager.findExistingFile(uri, asset.getVolume());
					if (file != null) {
						asset.setDateTime(new Date(file.lastModified()));
						asset.setFileSize(file.length());
					}
					asset.setMimeType(isDng ? ImageConstants.IMAGE_X_DNG : ImageConstants.IMAGE_X_RAW);
					asset.setDigitalZoomRatio(Double.NaN);
					if (isRaw && Constants.STATE_CONVERTED == asset.getStatus())
						asset.setStatus(Constants.STATE_RAW);
					db.store(asset);
					if (++c % 500 == 0)
						db.commit();
				}
			}
		}
		if (version <= 15) {
			monitor.subTask(Messages.CatalogConverter_fix_range);
			for (CriterionImpl crit : db.obtainObjects(CriterionImpl.class)) {
				Object value = crit.getValue();
				if (value instanceof Range) {
					crit.setValue(((Range) value).getFrom());
					crit.setTo(((Range) value).getTo());
					crit.setAnd(value instanceof TetheredRange);
					db.store(crit);
					db.delete(value);
				} else if (value == null) {
					SmartCollection sm = crit.getSmartCollection_parent();
					if (sm != null) {
						String id = sm.getStringId();
						if (id.startsWith(IDbManager.DATETIMEKEY)) {
							GregorianCalendar cal1 = new GregorianCalendar();
							GregorianCalendar cal2 = new GregorianCalendar();
							String[] split = id.split("=|-"); //$NON-NLS-1$
							switch (split.length) {
							case 2: {
								int year = getInt(split[1]);
								cal1.set(year, 0, 1, 0, 0, 0);
								cal2.set(year, 11, 31, 23, 59, 59);
								break;
							}
							case 3: {
								int year = getInt(split[1]);
								if (split[2].startsWith("W")) { //$NON-NLS-1$
									int week = getInt(split[2].substring(1));
									cal1.set(year, 0, 1, 0, 0, 0);
									cal1.set(GregorianCalendar.WEEK_OF_YEAR, week);
									cal1.set(GregorianCalendar.DAY_OF_WEEK, 0);
									cal2.set(Calendar.YEAR, year);
									cal2.set(Calendar.WEEK_OF_YEAR, week);
									cal2.set(Calendar.DAY_OF_WEEK, 6);
									cal2.set(Calendar.HOUR_OF_DAY, 23);
									cal2.set(Calendar.MINUTE, 59);
									cal2.set(Calendar.SECOND, 59);
								} else {
									int month = getInt(split[2]);
									cal1.set(year, month, 1, 0, 0, 0);
									cal2.set(year, month, cal1.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
								}
								break;
							}
							case 4: {
								int year = getInt(split[1]);
								int day = getInt(split[3]);
								if (split[2].startsWith("W")) { //$NON-NLS-1$
									int week = getInt(split[2].substring(1));
									cal1.set(year, 0, 1, 0, 0, 0);
									cal1.set(GregorianCalendar.WEEK_OF_YEAR, week);
									cal1.set(GregorianCalendar.DAY_OF_WEEK, day);
									cal2.set(Calendar.YEAR, year);
									cal2.set(Calendar.WEEK_OF_YEAR, week);
									cal2.set(Calendar.DAY_OF_WEEK, day);
									cal2.set(Calendar.HOUR_OF_DAY, 23);
									cal2.set(Calendar.MINUTE, 59);
									cal2.set(Calendar.SECOND, 59);
								} else {
									int month = getInt(split[2]);
									cal1.set(year, month, day, 0, 0, 0);
									cal2.set(year, month, day, 23, 59, 59);
								}
								break;
							}
							}
							crit.setValue(cal1.getTime());
							crit.setTo(cal2.getTime());
							db.store(crit);
						}
					} else
						db.delete(crit);
				}
			}
			db.commit();
			int c = 0;
			monitor.subTask(Messages.CatalogConverter_tune_structures);
			List<CreatorsContactImpl> contacts = new ArrayList<CreatorsContactImpl>(
					db.obtainObjects(CreatorsContactImpl.class));
			for (CreatorsContactImpl rel : contacts) {
				List<String> assetIds = rel.getAsset();
				if (assetIds.isEmpty()) {
					deleteContact(db, rel);
					continue;
				}
				for (String assetId : assetIds) {
					AssetImpl asset = db.obtainAsset(assetId);
					if (asset != null) {
						asset.setCreatorsContact_parent(rel.getStringId());
						db.store(asset);
					} else {
						rel.removeAsset(assetId);
						if (rel.getAsset().isEmpty())
							deleteContact(db, rel);
						else
							db.store(rel);
					}
					if (++c % 500 == 0)
						db.commit();
				}
			}
			db.commit();
			c = 0;
			List<LocationCreatedImpl> locs = new ArrayList<LocationCreatedImpl>(
					db.obtainObjects(LocationCreatedImpl.class));
			for (LocationCreatedImpl rel : locs) {
				List<String> assetIds = rel.getAsset();
				if (assetIds.isEmpty()) {
					db.delete(rel);
					continue;
				}
				for (String assetId : assetIds) {
					AssetImpl asset = db.obtainAsset(assetId);
					if (asset != null) {
						asset.setLocationCreated_parent(rel.getStringId());
						db.store(asset);
					} else {
						rel.removeAsset(assetId);
						if (rel.getAsset().isEmpty())
							db.delete(rel);
						else
							db.store(rel);
					}
					if (++c % 500 == 0)
						db.commit();
				}
			}
			db.commit();
		}
		meta.setVersion(db.getVersion());
		db.storeAndCommit(meta);
	}

	private static int getInt(String s) {
		return Integer.parseInt(s);
	}

	private static void deleteContact(IDbManager db, CreatorsContactImpl rel) {
		String contactId = rel.getContact();
		ContactImpl contact = db.obtainById(ContactImpl.class, contactId);
		if (contact != null)
			db.delete(contact);
		db.delete(rel);
	}
}
