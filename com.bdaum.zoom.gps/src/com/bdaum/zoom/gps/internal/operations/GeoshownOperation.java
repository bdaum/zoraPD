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
 * (c) 2017 Berthold Daum  
 */

package com.bdaum.zoom.gps.internal.operations;

import java.io.EOFException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.operations.Backup;
import com.bdaum.zoom.gps.internal.GpsConfiguration;
import com.bdaum.zoom.gps.internal.GpsUtilities;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.gps.Trackpoint;
import com.bdaum.zoom.ui.gps.Waypoint;

@SuppressWarnings("restriction")
public class GeoshownOperation extends GeotagOperation {

	private String[] ids;
	private Trackpoint trackpoint;
	private String[] locIds;
	private boolean modify;
	private String uuid;
	private LocationShownImpl[] locs;

	/**
	 * @param trackpoint
	 * @param modify
	 *            - true if existing shown locations are updated
	 * @param ids
	 *            - LocationsShown ids when modified, assetIds otherwise
	 * @param uuid
	 * @param gpsConfiguration
	 */
	public GeoshownOperation(Trackpoint trackpoint, boolean modify, String[] ids, String uuid,
			GpsConfiguration gpsConfiguration) {
		super(Messages.getString("GeoshownOperation.set_loc_shownn"), gpsConfiguration); //$NON-NLS-1$
		this.modify = modify;
		this.ids = ids;
		this.trackpoint = trackpoint;
		this.uuid = uuid;
		removeTag = Double.isNaN(trackpoint.getLatitude()) || Double.isNaN(trackpoint.getLongitude());
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		int size = ids.length;
		init(aMonitor, size + 1);
		aMonitor.subTask(Messages.getString(
				removeTag ? Messages.getString("GeoshownOperation.delete_shown") : "GeoshownOperation.set_loc_shownn")); //$NON-NLS-1$ //$NON-NLS-2$
		Meta meta = dbManager.getMeta(true);
		Set<Object> toBeStored = new HashSet<>();
		Set<Object> toBeDeleted = new HashSet<>();
		Set<Asset> assetList = new HashSet<>();
		backups = new Backup[size];
		if (modify) {
			Location newLocation = null;
			String newLocId = null;
			if (!removeTag) {
				locIds = new String[size];
				List<LocationImpl> locs = dbManager.obtainObjects(LocationImpl.class, false, "longitude", //$NON-NLS-1$
						trackpoint.getLongitude(), QueryField.EQUALS, "latitude", trackpoint.getLatitude(), //$NON-NLS-1$
						QueryField.EQUALS);

				if (locs.isEmpty()) {
					newLocation = new LocationImpl();
					newLocation.setLatitude(trackpoint.getLatitude());
					newLocation.setLongitude(trackpoint.getLongitude());
					geoname(null, newLocation, meta, toBeStored, info);
					toBeStored.add(newLocation);
				} else
					newLocation = locs.get(0);
				newLocId = newLocation.getStringId();
			} else
				locs = new LocationShownImpl[size];
			int i = 0;
			for (String relId : ids) {
				LocationShownImpl rel = dbManager.obtainById(LocationShownImpl.class, relId);
				if (rel != null) {
					if (isPeerOwned(rel.getAsset()))
						continue;
					if (removeTag) {
						toBeDeleted.add(rel);
						locs[i] = rel;
					} else {
						AssetImpl asset = dbManager.obtainAsset(rel.getAsset());
						if (asset != null) {
							try {
								backups[i] = new Backup(opId, asset, QueryField.EXIF_GPSIMAGEDIR,
										QueryField.EXIF_GPSIMAGEDIRREF, QueryField.IPTC_KEYWORDS);
							} catch (Exception e) {
								addError(Messages.getString("GeotagOperation.error_creating_backup"), e); //$NON-NLS-1$
							}
							locIds[i] = rel.getLocation();
							rel.setLocation(newLocId);
							toBeStored.add(rel);
							double gpsLatitude = asset.getGPSLatitude();
							double gpsLongitude = asset.getGPSLongitude();
							if (!Double.isNaN(gpsLatitude) && !Double.isNaN(gpsLongitude)) {
								asset.setGPSImgDirection(Core.bearing(gpsLatitude, gpsLongitude,
										trackpoint.getLatitude(), trackpoint.getLongitude()));
								asset.setGPSImgDirectionRef("T"); //$NON-NLS-1$
							}
							List<String> previousKeywords = new ArrayList<String>();
							Location previousLoc = dbManager.obtainById(LocationImpl.class, locIds[i]);
							if (previousLoc != null)
								Utilities.extractKeywords(previousLoc, previousKeywords);
							updateKeywords(asset, newLocation, meta, previousKeywords);
							assetList.add(asset);
							toBeStored.add(asset);
						}
					}
				}
				++i;
			}
		} else {
			int i = 0;
			for (String assetId : ids) {
				if (!isPeerOwned(assetId)) {
					Asset asset = dbManager.obtainAsset(assetId);
					if (asset != null) {
						try {
							backups[i] = new Backup(opId, asset, QueryField.EXIF_GPSIMAGEDIR,
									QueryField.EXIF_GPSIMAGEDIRREF, QueryField.IPTC_KEYWORDS);
						} catch (Exception e) {
							addError(Messages.getString("GeotagOperation.error_creating_backup"), e); //$NON-NLS-1$
						}
						tag(i, meta, asset, toBeStored, info);
						if (aMonitor.isCanceled())
							return close(info);
						double gpsLatitude = asset.getGPSLatitude();
						double gpsLongitude = asset.getGPSLongitude();
						if (!Double.isNaN(gpsLatitude) && !Double.isNaN(gpsLongitude)) {
							asset.setGPSImgDirection(Core.bearing(gpsLatitude, gpsLongitude, trackpoint.getLatitude(),
									trackpoint.getLongitude()));
							asset.setGPSImgDirectionRef("T"); //$NON-NLS-1$
							toBeStored.add(asset);
						}
						assetList.add(asset);
					}
				}
				monitor.worked(1);
				++i;
			}
		}
		storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
		if (!assetList.isEmpty()) {
			fireApplyRules(assetList, QueryField.EXIF_GPS);
			fireAssetsModified(new BagChange<>(null, assetList, null, null), null);
		}
		for (Backup backup : backups)
			if (backup != null)
				dbManager.storeTrash(backup);
		dbManager.commitTrash();
		backups = null;
		return close(info, assetList);
	}

	private void tag(int i, Meta meta, Asset asset, Collection<Object> toBeStored, IAdaptable info) {
		double lat = trackpoint.getLatitude();
		double lon = trackpoint.getLongitude();
		List<LocationImpl> locs = dbManager.obtainObjects(LocationImpl.class, false, "longitude", lon, //$NON-NLS-1$
				QueryField.EQUALS, "latitude", lat, //$NON-NLS-1$
				QueryField.EQUALS);
		LocationShownImpl rel;
		if (locs.isEmpty()) {
			LocationImpl loc = new LocationImpl();
			loc.setLatitude(lat);
			loc.setLongitude(lon);
			toBeStored.add(loc);
			rel = new LocationShownImpl(loc.getStringId(), asset.getStringId());
			if (uuid != null)
				rel.setStringId(uuid);
			geoname(asset, loc, meta, toBeStored, info);
		} else {
			for (LocationImpl location : locs)
				if (!dbManager.obtainObjects(LocationShownImpl.class, false, "asset", //$NON-NLS-1$
						asset.getStringId(), QueryField.EQUALS, "location", location.getStringId(), //$NON-NLS-1$
						QueryField.EQUALS).isEmpty())
					return;
			rel = new LocationShownImpl(locs.get(0).getStringId(), asset.getStringId());
			if (uuid != null)
				rel.setStringId(uuid);
		}
		if (backups[i] != null)
			backups[i].addObject(rel.getStringId());
		toBeStored.add(rel);
	}

	private void geoname(Asset asset, Location loc, Meta meta, Collection<Object> toBeStored, IAdaptable info) {
		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();
		try {
			Waypoint wp = getPlaceInfo(meta, 0, latitude, longitude, null, info);
			if (wp != null)
				GpsUtilities.transferPlacedata(wp, loc);
			if (asset != null && updateKeywords(asset, loc, meta, Collections.emptyList())) {
				toBeStored.add(asset);
				toBeStored.add(meta);
			}
		} catch (UnknownHostException e) {
			addError(Messages.getString("GeotagOperation.webservice_not_reached"), //$NON-NLS-1$
					e);
		} catch (EOFException e) {
			addError(Messages.getString("GeoshownOperation.geonaming_aborted"), null); //$NON-NLS-1$
		}
	}

	protected void handleResume(Meta meta, int code, int i, IAdaptable info) {
		AcousticMessageDialog.openWarning(info.getAdapter(Shell.class),
				Messages.getString("GeoshownOperation.geonaming_interrupted"), //$NON-NLS-1$
				getCause(code) + ". " + Messages.getString("GeoshownOperation.try_later")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return execute(aMonitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		backups = getBackupsFromTrash();
		int size = backups.length;
		initUndo(aMonitor, (modify ? 2 * size : size) + 1);
		List<Object> toBeStored = new ArrayList<Object>(size);
		List<Object> toBeDeleted = new ArrayList<Object>(size);
		if (modify) {
			if (locs != null)
				for (LocationShownImpl loc : locs) {
					if (loc != null)
						toBeStored.add(loc);
					aMonitor.worked(1);
				}
			else
				for (int i = ids.length - 1; i >= 0; i--) {
					if (ids[i] != null) {
						LocationShownImpl rel = dbManager.obtainById(LocationShownImpl.class, ids[i]);
						if (rel != null) {
							rel.setLocation(locIds[i]);
							toBeStored.add(rel);
						}
					}
					aMonitor.worked(1);
				}
		}
		List<Asset> assetList = new ArrayList<>(size);
		for (int i = backups.length - 1; i >= 0; i--) {
			if (backups[i] != null)
				try {
					backups[i].restore(toBeStored, toBeDeleted);
					AssetImpl asset = backups[i].getAsset();
					if (asset != null)
						assetList.add(asset);
				} catch (Exception e) {
					addError(Messages.getString("GeotagOperation.error_assigning_former_value"), //$NON-NLS-1$
							e);
				}
			aMonitor.worked(1);
		}
		backups = null;
		locs = null;
		storeSafely(toBeDeleted.toArray(), 1, toBeStored.toArray());
		fireAssetsModified(null, null);
		return close(info, assetList);
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public int getPriority() {
		return Job.SHORT;
	}

}
