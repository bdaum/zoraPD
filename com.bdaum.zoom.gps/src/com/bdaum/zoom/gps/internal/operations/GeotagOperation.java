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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.gps.internal.operations;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpException;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.xml.sax.SAXException;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.operations.Backup;
import com.bdaum.zoom.gps.geonames.WebServiceException;
import com.bdaum.zoom.gps.internal.GpsConfiguration;
import com.bdaum.zoom.gps.internal.GpsUtilities;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.DbOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.gps.RasterCoordinate;
import com.bdaum.zoom.ui.gps.Trackpoint;
import com.bdaum.zoom.ui.gps.Waypoint;

@SuppressWarnings("restriction")
public class GeotagOperation extends DbOperation {

	private static final String[] EMPTYSTRINGS = new String[0];
	private static final long ONEMINUTE = 60000L;
	private static final long DELAY = 64 * ONEMINUTE;
	private static final long ONEHOUR = ONEMINUTE * 60;
	private static long startTime = 0L;
	private static int alreadyDone = 0;

	private String[] assetIds;
	protected Backup[] backups;
	private final Date refDate = new Date(0);
	private String[] assetsTobeNamed;
	private boolean resumeLater;
	private List<String> postPonedAssets;
	protected GpsConfiguration gpsConfiguration;
	private Trackpoint[] trackpoints = new Trackpoint[0];
	private int tagged;
	private int named;
	private int notnamed;
	private Map<RasterCoordinate, Waypoint> placeMap = new HashMap<RasterCoordinate, Waypoint>();
	private List<String> processed = new ArrayList<String>();
	private Map<RasterCoordinate, Double> elevationMap = new HashMap<RasterCoordinate, Double>();
	protected boolean removeTag;

	public GeotagOperation(GpsConfiguration gpsConfiguration) {
		super(Messages.getString("GeotagOperation.Geonaming")); //$NON-NLS-1$
		this.assetIds = EMPTYSTRINGS;
		this.gpsConfiguration = gpsConfiguration;
	}

	public GeotagOperation(Trackpoint[] trackpoints, String[] assetIds, GpsConfiguration gpsConfiguration) {
		super(Messages.getString("GeotagOperation.GeoTagging")); //$NON-NLS-1$
		this.assetIds = assetIds;
		this.gpsConfiguration = gpsConfiguration;
		this.trackpoints = trackpoints;
		removeTag = trackpoints.length == 1
				&& (Double.isNaN(trackpoints[0].getLatitude()) || Double.isNaN(trackpoints[0].getLongitude()));
	}

	protected GeotagOperation(String label, GpsConfiguration gpsConfiguration2) {
		super(label);
		gpsConfiguration = gpsConfiguration2;
	}

	@Override
	public IStatus execute(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return execute(aMonitor, info, false);
	}

	private IStatus execute(IProgressMonitor aMonitor, IAdaptable info, boolean redo) {
		if (gpsConfiguration == null)
			return Status.CANCEL_STATUS;
		resumeLater = false;
		tagged = 0;
		int work = 0;
		int f = 1;
		if (trackpoints.length > 0)
			work = f = 2;
		int l = assetIds.length;
		work += f * l;
		backups = new Backup[l];
		init(aMonitor, work);
		Meta meta = dbManager.getMeta(true);
		Set<String> postponed = meta.getPostponedNaming();
		processed.clear();
		if (trackpoints.length > 0) {
			aMonitor.subTask(removeTag ? Messages.getString("GeotagOperation.removing_tags") //$NON-NLS-1$
					: Messages.getString("GeotagOperation.Tagging")); //$NON-NLS-1$
			List<Asset> assets = new ArrayList<Asset>(assetIds.length);
			int i = 0;
			for (String assetId : assetIds) {
				if (!isPeerOwned(assetId)) {
					AssetImpl asset = dbManager.obtainAsset(assetId);
					if (asset != null) {
						if (removeTag) {
							try {
								backups[i] = new Backup(opId, asset, QueryField.EXIF_GPSLONGITUDE,
										QueryField.EXIF_GPSLATITUDE, QueryField.EXIF_GPSIMAGEDIR);
							} catch (Exception e) {
								addError(Messages.getString("GeotagOperation.error_creating_backup"), e); //$NON-NLS-1$
							}
							asset.setGPSDestLatitude(Double.NaN);
							asset.setGPSLongitude(Double.NaN);
							asset.setGPSImgDirection(Double.NaN);
							List<LocationCreatedImpl> rels = dbManager.obtainStructForAsset(LocationCreatedImpl.class,
									assetId, true);
							if (backups[i] != null)
								backups[i].addAllDeleted(rels);
							storeSafely(rels.toArray(), 1, asset);
							++tagged;
						} else if (asset.getDateTimeOriginal() != null) {
							if (aMonitor.isCanceled())
								return close(info);
							assets.add(asset);
							if (tag(asset, meta, i))
								++tagged;
							String gpsImgDirectionRef = asset.getGPSImgDirectionRef();
							if (Double.isNaN(asset.getGPSImgDirection()) || gpsImgDirectionRef == null
									|| gpsImgDirectionRef.isEmpty()) {
								for (LocationShownImpl locationShown : dbManager
										.obtainStructForAsset(LocationShownImpl.class, assetId, false)) {
									LocationImpl loc = dbManager.obtainById(LocationImpl.class,
											locationShown.getLocation());
									if (loc != null) {
										asset.setGPSImgDirection(Core.bearing(asset.getGPSLatitude(),
												asset.getGPSLongitude(), loc.getLatitude(), loc.getLongitude()));
										asset.setGPSImgDirectionRef("T"); //$NON-NLS-1$
										break;
									}
								}
							}
						}
					}
				}
				++i;
			}
			addInfo(removeTag ? Messages.getString("GeotagOperation.0") //$NON-NLS-1$
					: NLS.bind(Messages.getString("GeotagOperation.n_images_tagged"), tagged, l - tagged)); //$NON-NLS-1$
			fireApplyRules(assets, QueryField.EXIF_GPS);
			fireAssetsModified(new BagChange<>(null, assets, null, null), QueryField.EXIF_GPS);
		}
		if (!removeTag && !aMonitor.isCanceled()) {
			aMonitor.subTask(Messages.getString("GeotagOperation.Geonaming_assets")); //$NON-NLS-1$
			int resumed = 0;
			if (postponed == null || postponed.isEmpty())
				assetsTobeNamed = assetIds;
			else {
				resumed = postponed.size();
				assetsTobeNamed = new String[resumed + assetIds.length];
				String[] postponedAssets = postponed.toArray(new String[resumed]);
				System.arraycopy(postponedAssets, 0, assetsTobeNamed, 0, resumed);
				System.arraycopy(assetIds, 0, assetsTobeNamed, resumed, assetIds.length);
				postponed.clear();
			}
			try {
				int i = 0;
				List<Asset> assets = new ArrayList<Asset>(assetsTobeNamed.length);
				for (String assetId : assetsTobeNamed)
					if (!isPeerOwned(assetId)) {
						AssetImpl asset = dbManager.obtainAsset(assetId);
						if (asset != null) {
							if (aMonitor.isCanceled())
								return close(info);
							geoname(meta, resumed, i, asset, aMonitor, info);
							assets.add(asset);
							if (resumeLater)
								break;
							++i;
						}
					}
				dbManager.storeAndCommit(meta);
				addInfo(NLS.bind(Messages.getString("GeotagOperation.n_images_decorated"), //$NON-NLS-1$
						named, notnamed));
				if (!redo)
					fireApplyRules(assets, null);
				fireAssetsModified(redo ? null : new BagChange<>(null, assets, null, null), null);
			} catch (UnknownHostException e) {
				addError(Messages.getString("GeotagOperation.webservice_not_reached"), //$NON-NLS-1$
						e);
			}
			for (Backup backup : backups)
				if (backup != null)
					dbManager.storeTrash(backup);
			dbManager.commitTrash();
			backups = null;
		}
		return close(info, processed.isEmpty() ? (String[]) null : processed.toArray(new String[processed.size()]));
	}

	private void geoname(Meta meta, int resumed, final int i, AssetImpl asset, IProgressMonitor aMonitor,
			IAdaptable info) throws UnknownHostException {
		if (geoname(asset, meta, resumed, i, aMonitor, info))
			++named;
		else
			++notnamed;
	}

	private boolean geoname(final Asset asset, final Meta meta, final int resumed, final int i,
			IProgressMonitor aMonitor, IAdaptable info) throws UnknownHostException {
		double latitude = asset.getGPSLatitude();
		double longitude = asset.getGPSLongitude();
		if (!Double.isNaN(latitude) && !Double.isNaN(longitude)) {
			LocationImpl location = null;
			List<String> oldKeywords = new ArrayList<String>();
			LocationCreatedImpl rel = null;
			String assetId = asset.getStringId();
			for (LocationCreatedImpl locationCreated : dbManager.obtainStructForAsset(LocationCreatedImpl.class,
					assetId, true)) {
				rel = locationCreated;
				LocationImpl obj = dbManager.obtainById(LocationImpl.class, rel.getLocation());
				if (obj != null)
					Utilities.extractKeywords(location = obj, oldKeywords);
			}
			List<String> keepKeywords = new ArrayList<String>();
			for (LocationShownImpl locationShown : dbManager.obtainStructForAsset(LocationShownImpl.class, assetId,
					false)) {
				LocationImpl obj = dbManager.obtainById(LocationImpl.class, locationShown.getLocation());
				if (obj != null)
					Utilities.extractKeywords(obj, keepKeywords);
			}
			oldKeywords.removeAll(keepKeywords);
			yieldWebservice(aMonitor);
			final LocationImpl loc = location;
			final List<String> ok = oldKeywords;
			final LocationCreatedImpl created = rel;
			if (location == null || GpsUtilities.isEmpty(location) || gpsConfiguration.overwrite) {
				Waypoint wp = getPlaceInfo(meta, i, latitude, longitude, aMonitor, info);
				if (wp != null) {
					if (i >= resumed)
						createBackup(asset, i - resumed);
					final LocationImpl newLocation = new LocationImpl();
					GpsUtilities.transferPlacedata(wp, newLocation);
					return storeSafely(() -> assignGeoNames(asset, meta, i >= resumed ? backups[i - resumed] : null,
							loc, ok, created, newLocation), 1);
				}
			}
		}
		aMonitor.worked(1);
		return false;
	}

	private void yieldWebservice(IProgressMonitor aMonitor) {
		long interval = gpsConfiguration.interval;
		int hourly = (int) (ONEHOUR / interval);
		long time = System.currentTimeMillis();
		if (time - startTime >= ONEHOUR) {
			startTime = time / ONEHOUR * ONEHOUR;
			alreadyDone = 0;
		}
		if (alreadyDone > hourly / 10) {
			long requiredTime = alreadyDone * interval;
			long passedTime = time - startTime;
			long delay = requiredTime - passedTime;
			while (delay > 0 && !aMonitor.isCanceled()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// do noting
				}
				delay -= 100;
			}
		}
		++alreadyDone;
	}

	private void createBackup(final Asset asset, final int i) {
		if (backups[i] == null)
			try {
				backups[i] = new Backup(opId, asset, QueryField.EXIF_GPSLONGITUDE, QueryField.EXIF_GPSLATITUDE,
						QueryField.EXIF_GPSALTITUDE, QueryField.EXIF_GPSSPEED, QueryField.EXIF_GPSIMAGEDIR,
						QueryField.EXIF_GPSIMAGEDIRREF, QueryField.IPTC_KEYWORDS);
			} catch (Exception e) {
				addError(Messages.getString("GeotagOperation.error_creating_backup"), e); //$NON-NLS-1$
			}
	}

	private void assignGeoNames(Asset asset, Meta meta, Backup backup, LocationImpl location, List<String> oldKeywords,
			LocationCreatedImpl rel, LocationImpl newLocation) {
		if (location == null || GpsUtilities.isEmpty(location) || !location.equals(newLocation)) {
			location = newLocation;
			Iterator<LocationImpl> it = dbManager.findLocation(location).iterator();
			if (it.hasNext())
				location = it.next();
			else {
				dbManager.store(location);
				if (backup != null)
					backup.addObject(location.getStringId());
			}
			if (rel != null) {
				rel.removeAsset(asset.getStringId());
				if (backup != null)
					backup.addModification(QueryField.IPTC_LOCATIONCREATED.getKey(), rel.getStringId(), true);
				if (rel.getAsset().isEmpty()) {
					dbManager.delete(rel);
					if (backup != null)
						backup.addDeleted(rel);
				} else
					dbManager.store(rel);
			}
			rel = new LocationCreatedImpl(location.getStringId());
			rel.addAsset(asset.getStringId());
			dbManager.store(rel);
			if (backup != null)
				backup.addObject(rel.getStringId());
		}
		if (backup != null)
			dbManager.storeTrash(backup);
		if (updateKeywords(asset, newLocation, meta, oldKeywords)) {
			dbManager.store(asset);
			dbManager.store(meta);
		}
		processed.add(asset.getStringId());
	}

	protected boolean updateKeywords(Asset asset, Location newLocation, Meta meta, List<String> oldKeywords) {
		if (gpsConfiguration.includeNames) {
			Set<String> keywords = new HashSet<String>(Arrays.asList(asset.getKeyword()));
			List<String> newKeywords = new ArrayList<String>(3);
			Utilities.extractKeywords(newLocation, newKeywords);
			addKeywordsToCat(meta, newKeywords);
			if (oldKeywords != null)
				keywords.removeAll(oldKeywords);
			keywords.addAll(newKeywords);
			String[] kws = keywords.toArray(new String[keywords.size()]);
			Arrays.sort(kws, Utilities.KEYWORDCOMPARATOR);
			asset.setKeyword(kws);
			return true;
		}
		return false;
	}

	protected void handleResume(Meta meta, int code, int i, IAdaptable info) {
		resumeLater = true;
		Shell shell = info.getAdapter(Shell.class);
		String msg = getCause(code);
		if (AcousticMessageDialog.openQuestion(shell, Messages.getString("GeotagOperation.geonaming_interrupted"), //$NON-NLS-1$
				msg + ". " + NLS.bind(Messages.getString("GeotagOperation.resume"), Constants.APPNAME))) { //$NON-NLS-1$ //$NON-NLS-2$
			if (code == 19)
				OperationJob.executeOperation(new GeotagOperation(gpsConfiguration), info, false, DELAY);
			postPonedAssets = new ArrayList<String>(assetsTobeNamed.length - i);
			Set<String> postponedNaming = meta.getPostponedNaming();
			if (postponedNaming == null)
				postponedNaming = new HashSet<String>((assetsTobeNamed.length - i) * 3 / 2);
			for (int j = i; j < assetsTobeNamed.length; j++) {
				postponedNaming.add(assetsTobeNamed[j]);
				postPonedAssets.add(assetsTobeNamed[j]);
			}
			meta.setPostponed(postponedNaming);
		}
	}

	protected String getCause(int code) {
		switch (code) {
		case 18:
			return Messages.getString("GeotagOperation.daily_limit_reached"); //$NON-NLS-1$
		case 19:
			return Messages.getString("GeotagOperation.hourly_limit_reached"); //$NON-NLS-1$
		default:
			return Messages.getString("GeotagOperation.weekly_limit_reached"); //$NON-NLS-1$
		}
	}

	private boolean tag(AssetImpl asset, Meta meta, int i) {
		if (!gpsConfiguration.overwrite
				&& (!Double.isNaN(asset.getGPSLongitude()) || !Double.isNaN(asset.getGPSLatitude()))) {
			monitor.worked(1);
			return false;
		}
		createBackup(asset, i);
		long refTime = asset.getDateTimeOriginal().getTime() + gpsConfiguration.timeshift * ONEMINUTE;
		refDate.setTime(refTime);
		int index = Arrays.binarySearch(trackpoints, refDate);
		Trackpoint lower = null;
		Trackpoint upper = null;
		long lowerdiff = 0;
		long upperdiff = 0;
		if (index >= 0)
			upper = lower = trackpoints[index];
		else {
			index = -index - 1;
			lower = trackpoints[index <= 0 ? 0 : index - 1];
			upper = trackpoints[index > trackpoints.length - 1 ? trackpoints.length - 1 : index];
			lowerdiff = lower.getTime() < 0 ? 0 : Math.abs(lower.getTime() - refTime);
			upperdiff = upper.getTime() < 0 ? 0 : Math.abs(upper.getTime() - refTime);
			if (lower.getMinTime() >= 0 && upper.getMinTime() >= 0) {
				if (refTime > upper.getMaxTime() || refTime < lower.getMinTime()) {
					monitor.worked(1);
					return false;
				}
				if (refTime > upper.getMaxTime())
					upper = lower;
				else if (refTime < lower.getMinTime())
					lower = upper;
			} else {
				long tolerance = gpsConfiguration.tolerance * ONEMINUTE;
				if (upperdiff > tolerance) {
					if (lowerdiff > tolerance) {
						monitor.worked(1);
						return false;
					}
					upper = lower;
				} else if (lowerdiff > tolerance)
					lower = upper;
			}
		}
		if (lower == upper) {
			if (!Double.isNaN(lower.getLongitude()))
				asset.setGPSLongitude(lower.getLongitude());
			if (!Double.isNaN(lower.getLatitude()))
				asset.setGPSLatitude(lower.getLatitude());
			if (!Double.isNaN(lower.getAltitude()))
				asset.setGPSAltitude(lower.getAltitude());
			if (!Double.isNaN(lower.getSpeed()))
				asset.setGPSSpeed(lower.getSpeed());
		} else {
			long diff = lowerdiff + upperdiff;
			if (diff == 0) {
				monitor.worked(1);
				return false;
			}
			double fac1 = (double) upperdiff / diff;
			double fac2 = (double) lowerdiff / diff;
			if (!Double.isNaN(lower.getLongitude()) && !Double.isNaN(upper.getLongitude())) {
				double lowerLongitude = lower.getLongitude();
				double upperLongitude = upper.getLongitude();
				if (Math.abs(lowerLongitude - upperLongitude) > 180) {
					if (lowerLongitude > upperLongitude)
						lowerLongitude -= 360;
					else
						upperLongitude -= 360;
				}
				double longitude = lowerLongitude * fac1 + upperLongitude * fac2;
				if (longitude < 0)
					longitude += 360;
				asset.setGPSLongitude(longitude);
			}
			if (!Double.isNaN(lower.getLatitude()) && !Double.isNaN(upper.getLatitude()))
				asset.setGPSLatitude((lower.getLatitude() * fac1 + upper.getLatitude() * fac2));
			if (!Double.isNaN(lower.getAltitude()) && !Double.isNaN(upper.getAltitude()))
				asset.setGPSAltitude((lower.getAltitude() * fac1 + upper.getAltitude() * fac2));
			if (!Double.isNaN(lower.getSpeed()) && !Double.isNaN(upper.getSpeed()))
				asset.setGPSSpeed((lower.getSpeed() * fac1 + upper.getSpeed() * fac2));
		}
		if (gpsConfiguration.updateAltitude && !Double.isNaN(asset.getGPSLatitude())
				&& !Double.isNaN(asset.getGPSLongitude())) {
			yieldWebservice(monitor);
			double elevation = getElevation(asset.getGPSLatitude(), asset.getGPSLongitude());
			if (!Double.isNaN(elevation))
				asset.setGPSAltitude(elevation);
		}
		if (gpsConfiguration.includeCoordinates)
			return addCoordinateKeywords(asset, asset.getGPSLatitude(), asset.getGPSLongitude(), meta);
		return storeSafely(null, 1, asset);
	}

	@Override
	public IStatus redo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		return execute(aMonitor, info, true);
	}

	@Override
	public IStatus undo(IProgressMonitor aMonitor, IAdaptable info) throws ExecutionException {
		Backup[] backups = getBackupsFromTrash();
		int size = backups.length;
		initUndo(aMonitor, size);
		if (postPonedAssets != null) {
			Meta meta = dbManager.getMeta(false);
			if (meta != null) {
				Set<String> postponedNaming = meta.getPostponedNaming();
				if (postponedNaming != null)
					postponedNaming.removeAll(postPonedAssets);
			}
		}
		List<Object> toBeStored = new ArrayList<Object>(size);
		List<Object> toBeDeleted = new ArrayList<Object>(size);
		for (Backup backup : backups) {
			try {
				backup.restore(toBeStored, toBeDeleted);
			} catch (Exception e) {
				addError(Messages.getString("GeotagOperation.error_assigning_former_value"), //$NON-NLS-1$
						e);
			}
			dbManager.safeTransaction(toBeDeleted, toBeStored);
			toBeDeleted.clear();
			toBeStored.clear();
		}
		fireAssetsModified(null, null);
		return close(info);
	}


	protected boolean addCoordinateKeywords(Asset asset, double lat, double lon, Meta meta) {
		String[] keywords = asset.getKeyword();
		List<String> oldKeywords = new ArrayList<String>(keywords.length);
		for (String kw : keywords)
			if (!kw.startsWith("geo:lat=") && !kw.startsWith("geo:lon=") //$NON-NLS-1$ //$NON-NLS-2$
					&& !kw.equals("geotagged")) //$NON-NLS-1$
				oldKeywords.add(kw);
		List<String> newKeywords = new ArrayList<String>(3);
		newKeywords.add("geo:lat=" + lat); //$NON-NLS-1$
		newKeywords.add("geo:lon=" + lon); //$NON-NLS-1$
		newKeywords.add("geotagged"); //$NON-NLS-1$
		addKeywordsToCat(meta, newKeywords);
		newKeywords.addAll(oldKeywords);
		String[] kws = newKeywords.toArray(new String[newKeywords.size()]);
		Arrays.sort(kws, Utilities.KEYWORDCOMPARATOR);
		asset.setKeyword(kws);
		return storeSafely(null, 1, meta, asset);
	}

	protected void addKeywordsToCat(Meta meta, List<String> newKeywords) {
		meta.getKeywords()
				.addAll(gpsConfiguration.keywordFilter.filter(newKeywords.toArray(new String[newKeywords.size()])));
	}

	protected double getElevation(double lat, double lon) {
		RasterCoordinate coord = new RasterCoordinate(lat, lon, 3);
		Double elevation = elevationMap.get(coord);
		if (elevation != null)
			return elevation.doubleValue();
		try {
			double v = GpsUtilities.fetchElevation(lat, lon);
			elevationMap.put(coord, v);
			return v;
		} catch (MalformedURLException e) {
			// should never happen
		} catch (IOException e) {
			addError(Messages.getString("GeotagOperation.IO_Error_retrieving_elevation"), //$NON-NLS-1$
					e);
		}
		return Double.NaN;
	}

	protected Waypoint getPlaceInfo(Meta meta, int i, double lat, double lon, IProgressMonitor aMonitor,
			IAdaptable info) throws UnknownHostException {
		RasterCoordinate coord = new RasterCoordinate(lat, lon, 2);
		Waypoint place = coord.findClosestMatch(placeMap, 0.06d, 'K');
		if (place != null)
			return place;
		try {
			place = GpsUtilities.fetchPlaceInfo(lat, lon);
			if (place != null && !Double.isNaN(place.getLat()) && !Double.isNaN(place.getLon())) {
				double elevation = getElevation(place.getLat(), place.getLon());
				if (!Double.isNaN(elevation))
					place.setElevation(elevation);
			}
			if (place != null) {
				placeMap.put(coord, place);
				return place;
			}
		} catch (MalformedURLException e) {
			// should never happen
		} catch (SocketTimeoutException e) {
			addError(Messages.getString("GeotagOperation.connection_timed_out"), //$NON-NLS-1$
					null);
		} catch (HttpException e) {
			String message = e.getMessage();
			if (message.indexOf("(503)") >= 0) { //$NON-NLS-1$
				addError(Messages.getString("GeotagOperation.geonaming_aborted"), e); //$NON-NLS-1$
				if (aMonitor != null)
					aMonitor.setCanceled(true);
			} else
				addError(Messages.getString("GeotagOperation.http_exception"), //$NON-NLS-1$
						null);
		} catch (IOException e) {
			if (e instanceof UnknownHostException)
				throw (UnknownHostException) e;
			addError(Messages.getString("GeotagOperation.IO_Error_parsing_response"), //$NON-NLS-1$
					e);
		} catch (NumberFormatException e) {
			addError(Messages.getString("GeotagOperation.Number_format_parsing_response"), //$NON-NLS-1$
					e);
		} catch (WebServiceException e) {
			addWarning(Messages.getString("GeotagOperation.Geoname_signalled_exception"), e); //$NON-NLS-1$
			Throwable e2 = e.getCause();
			if (e2 instanceof WebServiceException && e2 != e) {
				try {
					int code = Integer.parseInt(e2.getMessage());
					if (code >= 18 && code <= 20) {
						handleResume(meta, code, i, info);
						return null;
					}
				} catch (NumberFormatException e1) {
					// do nothing
				}
				addError(Messages.getString("GeotagOperation.geonaming_aborted"), e2); //$NON-NLS-1$
				if (aMonitor != null)
					aMonitor.setCanceled(true);
			}
		} catch (SAXException e) {
			addError(Messages.getString("GeotagOperation.XML_problem_parsing_response"), e); //$NON-NLS-1$
		} catch (ParserConfigurationException e) {
			addError(Messages.getString("GeotagOperation.internal_error_configuring_sax"), e); //$NON-NLS-1$
		}
		return null;
	}

	public int getExecuteProfile() {
		return IProfiledOperation.CONTENT;
	}

	public int getUndoProfile() {
		return IProfiledOperation.CONTENT;
	}

	@Override
	public int getPriority() {
		return assetIds.length > 3 ? Job.LONG : Job.SHORT;
	}

}
