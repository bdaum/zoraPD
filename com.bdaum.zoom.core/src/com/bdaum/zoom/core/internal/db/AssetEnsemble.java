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

import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.osgi.util.NLS;

import com.adobe.xmp.XMPException;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.Asset_type;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.MediaExtension;
import com.bdaum.zoom.cat.model.asset.Region;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.ExifParser;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.operations.AnalogProperties;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;

public class AssetEnsemble {

	public static class MWGRegion extends Rectangle2D.Double {

		private static final long serialVersionUID = -1372110824228313972L;

		private String type;
		private String name;
		private long personLiveCID;
		private String personEmailDigest;
		private String picasaIniEntry;
		private String description;

		private String value;

		public void setArea(Map<String, Object> areaMap) {
			double h = 0, w = 0, x = 0, y = 0, d = -1;
			for (Map.Entry<String, Object> entry : areaMap.entrySet()) {
				String key = entry.getKey();
				String value = (String) entry.getValue();
				if ("H".equals(key)) //$NON-NLS-1$
					h = QueryField.parseDouble(value);
				else if ("W".equals(key)) //$NON-NLS-1$
					w = QueryField.parseDouble(value);
				else if ("X".equals(key)) //$NON-NLS-1$
					x = QueryField.parseDouble(value);
				else if ("Y".equals(key)) //$NON-NLS-1$
					y = QueryField.parseDouble(value);
				else if ("D".equals(key)) //$NON-NLS-1$
					d = QueryField.parseDouble(value);
			}
			if (d < 0)
				setRect(x - w / 2, y - h / 2, w, h);
			else
				setRect(x, y, d, java.lang.Double.NaN);
		}

		public void setArea(String area) {
			StringTokenizer st = new StringTokenizer(area, ", "); //$NON-NLS-1$
			try {
				double x = java.lang.Double.parseDouble(st.nextToken());
				double y = java.lang.Double.parseDouble(st.nextToken());
				double w = java.lang.Double.parseDouble(st.nextToken());
				double h = java.lang.Double.parseDouble(st.nextToken());
				setRect(x, y, w, h);
			} catch (NoSuchElementException e) {
				throw new NumberFormatException(e.getMessage());
			}
		}

		public void setRect64(String rect64) {
			int l = rect64.length();
			int leadingZeros = l > 12 ? Math.max(0, 16 - l) : Math.max(0, 12 - l);
			int x1 = Utilities.parseHex(rect64, 0 - leadingZeros, 4 - leadingZeros);
			int y1 = Utilities.parseHex(rect64, 4 - leadingZeros, 8 - leadingZeros);
			int x2 = Utilities.parseHex(rect64, 8 - leadingZeros, 12 - leadingZeros);
			if (l > 12) {
				int y2 = Utilities.parseHex(rect64, 12 - leadingZeros, 16 - leadingZeros);
				setRect(x1 / 65535d, y1 / 65535d, (x2 - x1) / 65535d, (y2 - y1) / 65535d);
			} else
				setRect(x1 / 65535d, y1 / 65535d, x2 / 65535d, java.lang.Double.NaN);
		}

		public boolean intersects(MWGRegion region) {
			if (java.lang.Double.isNaN(height)) {
				if (!java.lang.Double.isNaN(region.getHeight()))
					return false;
				double dr = Math.abs(width - region.getWidth());
				double dx = x - region.getX();
				double dy = y - region.getY();
				return (dx * dx + dy * dy <= dr * dr);
			}
			if (java.lang.Double.isNaN(region.getHeight()))
				return false;
			return super.intersects(region);
		}

		public static void union(MWGRegion src1, MWGRegion src2, MWGRegion dest) {
			if (java.lang.Double.isNaN(src1.getHeight())) {
				double w1 = src1.getWidth();
				double w2 = src2.getWidth();
				if (w1 < w2)
					dest.setRect(src2.getX(), src2.getY(), w2, java.lang.Double.NaN);
				else
					dest.setRect(src1.getX(), src1.getY(), w1, java.lang.Double.NaN);
			} else
				Rectangle2D.union(src1, src2, dest);
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setValue(String value) {
			this.value = value;
		}

		/**
		 * @return value
		 */
		public String getValue() {
			return value;
		}

		public void setType(String type) {
			this.type = type;
		}

		/**
		 * @return type
		 */
		public String getType() {
			return type;
		}

		/**
		 * @return name
		 */
		public String getName() {
			return name;
		}

		public void setPersonLiveCID(long personLiveCID) {
			this.personLiveCID = personLiveCID;
		}

		public void setPersonEmailDigest(String personEmailDigest) {
			this.personEmailDigest = personEmailDigest;
		}

		/**
		 * @return liveCID
		 */
		public long getPersonLiveCID() {
			return personLiveCID;
		}

		/**
		 * @return email
		 */
		public String getPersonEmailDigest() {
			return personEmailDigest;
		}

		/**
		 * @return picasaIniEntry
		 */
		public String getPicasaIniEntry() {
			return picasaIniEntry;
		}

		/**
		 * @param picasaIniEntry
		 *            das zu setzende Objekt picasaIniEntry
		 */
		public void setPicasaIniEntry(String picasaIniEntry) {
			this.picasaIniEntry = picasaIniEntry;
		}

		/**
		 * @return description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * @param description
		 *            das zu setzende Objekt description
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		public String getRect64() {
			return Utilities.toRect64(getX(), getY(), getWidth(), getHeight());
		}

	}

	public static final int OLD = 0;
	public static final int MWG = 1;
	public static final int MP = 2;
	public static final int CAMERA = 3;
	public static final int PICASA = 4;

	static final String[] EMPTYSTRINGARRAY = new String[0];
	static final int[] EMPTYINTARRAY = new int[0];
	static final String EMPTYSTRING = ""; //$NON-NLS-1$
	static final byte[] EMPTYBYTEARRAY = new byte[0];
	private static final Date DATE1900 = new GregorianCalendar(1900, 0, 1).getTime();

	public Date xmpTimestamp;
	private Asset asset;
	private ArtworkOrObjectImpl[] artworkShown;
	private LocationImpl[] locationsShown;
	private ContactImpl creatorContact;
	private boolean resetIptc;
	private LocationImpl locationCreated;
	private ImportConfiguration configuration;
	private final IDbManager dbManager;
	private MWGRegion[][] regions = new MWGRegion[5][];
	private final ImportState importState;

	public static List<AssetEnsemble> getAllAssets(IDbManager dbManager, URI uri, ImportState importState) {
		List<AssetImpl> assets = dbManager.obtainAssetsForFile(uri);
		List<AssetEnsemble> ensembles = new ArrayList<AssetEnsemble>(assets.size());
		for (AssetImpl asset : assets)
			ensembles.add(new AssetEnsemble(dbManager, asset, importState));
		return ensembles;
	}

	public static void deleteAll(List<AssetEnsemble> ensembles, List<Asset> deletedAssets,
			Collection<Object> toBeDeleted, Collection<Object> toBeStored) {
		if (ensembles != null) {
			for (AssetEnsemble ensemble : ensembles) {
				ensemble.delete(toBeDeleted, toBeStored);
				deletedAssets.add(ensemble.getAsset());
			}
			ensembles.clear();
		}
	}

	public AssetEnsemble(IDbManager dbManager, Asset asset, ImportState importState) {
		this.dbManager = dbManager;
		this.asset = asset;
		this.importState = importState;
		this.configuration = importState.getConfiguration();
	}

	public AssetEnsemble(IDbManager dbManager, ImportState importState, String id) {
		this.dbManager = dbManager;
		this.importState = importState;
		asset = new AssetImpl();
		if (id != null)
			asset.setStringId(id);
		asset.setAlbum(EMPTYSTRINGARRAY);
		this.configuration = importState.getConfiguration();
	}

	public void resetEnsemble(int status) {
		if (configuration == null || configuration.isResetStatus)
			resetStatus(status);
		if (configuration == null || configuration.isResetExif)
			resetExif();
		if (configuration == null || configuration.isResetGps)
			resetGps();
		if (configuration == null || configuration.isResetFaceData)
			resetFaceData();
		else
			transferFaceDate();
		if (configuration == null || configuration.isResetIptc)
			resetIptc();
	}

	private void transferFaceDate() {
		String[] regionIds = asset.getPerson();
		if (regionIds != null && regionIds.length > 0) {
			List<RegionImpl> oldRegions = dbManager.obtainObjects(RegionImpl.class, "asset_person_parent", //$NON-NLS-1$
					asset.getStringId(), QueryField.EQUALS);
			int i = 0;
			for (RegionImpl region : oldRegions) {
				String regionId = region.getStringId();
				for (String id : regionIds)
					if (regionId.equals(id)) {
						try {
							transferRegionData(dbManager, region, getRegion(OLD, ++i));
						} catch (NumberFormatException e) {
							importState.operation
									.addError(NLS.bind(Messages.AssetEnsemble_bad_face_data, asset.getName()), e);
						}
						break;
					}
			}
		}
	}

	public static void transferRegionData(IDbManager dbManager, RegionImpl region, MWGRegion mwgRegion)
			throws NumberFormatException {
		String type = region.getType();
		mwgRegion.setRect64(region.getStringId());
		if (region.getAlbum() != null) {
			if (Region.type_barCode.equals(type))
				mwgRegion.setValue(region.getAlbum());
			else {
				SmartCollectionImpl album = dbManager.obtainById(SmartCollectionImpl.class, region.getAlbum());
				if (album != null)
					mwgRegion.setName(album.getName());
			}
		}
		mwgRegion.setPersonEmailDigest(region.getPersonEmailDigest());
		mwgRegion.setType(Region.type_face);
		mwgRegion.setPersonLiveCID(region.getPersonLiveCID());
		mwgRegion.setDescription(region.getDescription());
		mwgRegion.setType(type);
	}

	public void transferTo(AssetEnsemble target) {
		target.artworkShown = artworkShown;
		target.creatorContact = creatorContact;
		target.locationCreated = locationCreated;
		target.locationsShown = locationsShown;
		target.regions = regions;
		transferAssetData(asset, target.getAsset(), configuration);
	}

	/**
	 * Copies everything except asset relationships
	 *
	 * @param sourceAsset
	 * @param targetAsset
	 */
	public static void transferAssetData(Asset sourceAsset, Asset targetAsset, ImportConfiguration configuration) {

		targetAsset.setName(sourceAsset.getName());

		targetAsset.setUri(sourceAsset.getUri());

		targetAsset.setVolume(sourceAsset.getVolume());

		targetAsset.setFileState(sourceAsset.getFileState());

		targetAsset.setFileSize(sourceAsset.getFileSize());

		targetAsset.setImportedBy(sourceAsset.getImportedBy());

		targetAsset.setImportDate(sourceAsset.getImportDate());

		targetAsset.setLastModification(sourceAsset.getLastModification());

		targetAsset.setComments(sourceAsset.getComments());

		targetAsset.setFormat(sourceAsset.getFormat());

		targetAsset.setMimeType(sourceAsset.getMimeType());

		targetAsset.setAlbum(sourceAsset.getAlbum());

		targetAsset.setHeight(sourceAsset.getHeight());

		targetAsset.setWidth(sourceAsset.getWidth());

		targetAsset.setOri(sourceAsset.getOri());

		targetAsset.setIsProgressive(sourceAsset.getIsProgressive());

		targetAsset.setColorType(sourceAsset.getColorType());

		targetAsset.setXmpModifiedAt(sourceAsset.getXmpModifiedAt());

		targetAsset.setVoiceFileURI(sourceAsset.getVoiceFileURI());

		targetAsset.setVoiceVolume(sourceAsset.getVoiceVolume());

		targetAsset.setRotation(sourceAsset.getRotation());

		if (configuration == null || configuration.isResetStatus) {

			targetAsset.setStatus(sourceAsset.getStatus());

			targetAsset.setRating(sourceAsset.getRating());

			targetAsset.setRatedBy(sourceAsset.getRatedBy());

			targetAsset.setColorCode(sourceAsset.getColorCode());

			targetAsset.setSafety(sourceAsset.getSafety());

			targetAsset.setContentType(sourceAsset.getContentType());

			targetAsset.setUserfield1(sourceAsset.getUserfield1());

			targetAsset.setUserfield2(sourceAsset.getUserfield2());

			targetAsset.setPrice(sourceAsset.getPrice());

			targetAsset.setEarnings(sourceAsset.getEarnings());

			targetAsset.setSales(sourceAsset.getSales());

			targetAsset.setScore(sourceAsset.getScore());

			targetAsset.setLastEditor(sourceAsset.getLastEditor());
		}

		targetAsset.setJpegThumbnail(sourceAsset.getJpegThumbnail());

		if (configuration == null || configuration.isResetExif) {

			targetAsset.setImageWidth(sourceAsset.getImageWidth());

			targetAsset.setImageLength(sourceAsset.getImageLength());

			targetAsset.setBitsPerSample(sourceAsset.getBitsPerSample());

			targetAsset.setCompression(sourceAsset.getCompression());

			targetAsset.setPhotometricInterpretation(sourceAsset.getPhotometricInterpretation());

			targetAsset.setOrientation(sourceAsset.getOrientation());

			targetAsset.setSamplesPerPixel(sourceAsset.getSamplesPerPixel());

			targetAsset.setXResolution(sourceAsset.getXResolution());

			targetAsset.setYResolution(sourceAsset.getYResolution());

			targetAsset.setDateTime(sourceAsset.getDateTime());

			targetAsset.setImageDescription(sourceAsset.getImageDescription());

			targetAsset.setMake(sourceAsset.getMake());

			targetAsset.setModel(sourceAsset.getModel());

			targetAsset.setSoftware(sourceAsset.getSoftware());

			targetAsset.setOriginalFileName(sourceAsset.getOriginalFileName());

			targetAsset.setOriginalImageId(sourceAsset.getOriginalImageId());

			targetAsset.setArtist(sourceAsset.getArtist());

			targetAsset.setCopyright(sourceAsset.getCopyright());

			targetAsset.setColorSpace(sourceAsset.getColorSpace());

			targetAsset.setProfileDescription(sourceAsset.getProfileDescription());

			targetAsset.setDateTimeOriginal(sourceAsset.getDateTimeOriginal());

			targetAsset.setExposureTime(sourceAsset.getExposureTime());

			targetAsset.setFNumber(sourceAsset.getFNumber());

			targetAsset.setExposureProgram(sourceAsset.getExposureProgram());

			targetAsset.setSpectralSensitivity(sourceAsset.getSpectralSensitivity());

			targetAsset.setIsoSpeedRatings(sourceAsset.getIsoSpeedRatings());

			targetAsset.setScalarSpeedRatings(sourceAsset.getScalarSpeedRatings());

			targetAsset.setShutterSpeed(sourceAsset.getShutterSpeed());

			targetAsset.setAperture(sourceAsset.getAperture());

			targetAsset.setBrightnessValue(sourceAsset.getBrightnessValue());

			targetAsset.setExposureBias(sourceAsset.getExposureBias());

			targetAsset.setMaxLensAperture(sourceAsset.getMaxLensAperture());

			targetAsset.setSubjectDistance(sourceAsset.getSubjectDistance());

			targetAsset.setMeteringMode(sourceAsset.getMeteringMode());

			targetAsset.setLightSource(sourceAsset.getLightSource());

			targetAsset.setFlashFired(sourceAsset.getFlashFired());

			targetAsset.setReturnLightDetected(sourceAsset.getReturnLightDetected());

			targetAsset.setFlashAuto(sourceAsset.getFlashAuto());

			targetAsset.setFlashFunction(sourceAsset.getFlashFunction());

			targetAsset.setRedEyeReduction(sourceAsset.getRedEyeReduction());

			targetAsset.setFocalLength(sourceAsset.getFocalLength());

			targetAsset.setSubjectArea(sourceAsset.getSubjectArea());

			targetAsset.setFlashEnergy(sourceAsset.getFlashEnergy());

			targetAsset.setFocalPlaneXResolution(sourceAsset.getFocalPlaneXResolution());

			targetAsset.setFocalPlaneYResolution(sourceAsset.getFocalPlaneYResolution());

			targetAsset.setSubjectLocation(sourceAsset.getSubjectLocation());
			targetAsset.setExposureIndex(sourceAsset.getExposureIndex());

			targetAsset.setSensingMethod(sourceAsset.getSensingMethod());

			targetAsset.setFileSource(sourceAsset.getFileSource());

			targetAsset.setExposureMode(sourceAsset.getExposureMode());

			targetAsset.setWhiteBalance(sourceAsset.getWhiteBalance());

			targetAsset.setDigitalZoomRatio(sourceAsset.getDigitalZoomRatio());

			targetAsset.setFocalLengthIn35MmFilm(sourceAsset.getFocalLengthIn35MmFilm());

			targetAsset.setFocalLengthFactor(sourceAsset.getFocalLengthFactor());

			targetAsset.setSceneCaptureType(sourceAsset.getSceneCaptureType());

			targetAsset.setGainControl(sourceAsset.getGainControl());

			targetAsset.setContrast(sourceAsset.getContrast());

			targetAsset.setSaturation(sourceAsset.getSaturation());

			targetAsset.setSharpness(sourceAsset.getSharpness());

			targetAsset.setVibrance(sourceAsset.getVibrance());

			targetAsset.setSubjectDistanceRange(sourceAsset.getSubjectDistanceRange());

			targetAsset.setLens(sourceAsset.getLens());

			targetAsset.setLensSerial(sourceAsset.getLensSerial());

			targetAsset.setSerial(sourceAsset.getSerial());
			targetAsset.setDof(sourceAsset.getDof());
			targetAsset.setFov(sourceAsset.getFov());
			targetAsset.setCircleOfConfusion(sourceAsset.getCircleOfConfusion());
			targetAsset.setLv(sourceAsset.getLv());
			targetAsset.setHyperfocalDistance(sourceAsset.getHyperfocalDistance());
			targetAsset.setFlashExposureComp(sourceAsset.getFlashExposureComp());
			targetAsset.setPreviewSize(sourceAsset.getPreviewSize());
			targetAsset.setMakerNotes(sourceAsset.getMakerNotes());
			targetAsset.setAnalogFormat(sourceAsset.getAnalogFormat());
			targetAsset.setAnalogType(sourceAsset.getAnalogType());
			targetAsset.setEmulsion(sourceAsset.getEmulsion());
			IMediaSupport[] mediaSupport = CoreActivator.getDefault().getMediaSupport();
			for (IMediaSupport support : mediaSupport)
				support.transferExtension(sourceAsset, targetAsset);
		}

		if (configuration == null || configuration.isResetGps) {

			targetAsset.setGPSLatitude(sourceAsset.getGPSLatitude());

			targetAsset.setGPSLongitude(sourceAsset.getGPSLongitude());

			targetAsset.setGPSAltitude(sourceAsset.getGPSAltitude());

			targetAsset.setGPSTime(sourceAsset.getGPSTime());

			targetAsset.setGPSDOP(sourceAsset.getGPSDOP());

			targetAsset.setGPSSpeed(sourceAsset.getGPSSpeed());

			targetAsset.setGPSTrackRef(sourceAsset.getGPSTrackRef());

			targetAsset.setGPSTrack(sourceAsset.getGPSTrack());

			targetAsset.setGPSImgDirectionRef(sourceAsset.getGPSImgDirectionRef());

			targetAsset.setGPSImgDirection(sourceAsset.getGPSImgDirection());

			targetAsset.setGPSMapDatum(sourceAsset.getGPSMapDatum());

			targetAsset.setGPSDestLatitude(sourceAsset.getGPSDestLatitude());

			targetAsset.setGPSDestLongitude(sourceAsset.getGPSDestLongitude());

			targetAsset.setGPSDestBearingRef(sourceAsset.getGPSDestBearingRef());
			targetAsset.setGPSDestBearing(sourceAsset.getGPSDestBearing());

			targetAsset.setGPSDestDistance(sourceAsset.getGPSDestDistance());

			targetAsset.setGPSAreaInformation(sourceAsset.getGPSAreaInformation());

			targetAsset.setGPSDateStamp(sourceAsset.getGPSDateStamp());

			targetAsset.setGPSDifferential(sourceAsset.getGPSDifferential());

		}

		if (configuration == null || configuration.isResetFaceData) {
			targetAsset.setNoPersons(sourceAsset.getNoPersons());

			targetAsset.setPerson(sourceAsset.getPerson());

			targetAsset.setDateRegionsValid(sourceAsset.getDateRegionsValid());
		}

		if (configuration == null || configuration.isResetIptc) {

			targetAsset.setWriterEditor(sourceAsset.getWriterEditor());

			targetAsset.setHeadline(sourceAsset.getHeadline());

			targetAsset.setIntellectualGenre(sourceAsset.getIntellectualGenre());
			targetAsset.setTitle(sourceAsset.getTitle());

			targetAsset.setSpecialInstructions(sourceAsset.getSpecialInstructions());

			targetAsset.setJobId(sourceAsset.getJobId());

			targetAsset.setKeyword(sourceAsset.getKeyword());

			targetAsset.setCategory(sourceAsset.getCategory());

			targetAsset.setSupplementalCats(sourceAsset.getSupplementalCats());

			targetAsset.setUrgency(sourceAsset.getUrgency());

			targetAsset.setAuthorsPosition(sourceAsset.getAuthorsPosition());

			targetAsset.setCredit(sourceAsset.getCredit());

			targetAsset.setUsageTerms(sourceAsset.getUsageTerms());

			targetAsset.setOwner(sourceAsset.getOwner());

			targetAsset.setSource(sourceAsset.getSource());

			targetAsset.setDateCreated(sourceAsset.getDateCreated());

			targetAsset.setSceneCode(sourceAsset.getSceneCode());

			targetAsset.setSubjectCode(sourceAsset.getSubjectCode());

			targetAsset.setModelInformation(sourceAsset.getModelInformation());

			targetAsset.setModelAge(sourceAsset.getModelAge());

			targetAsset.setCodeOfOrg(sourceAsset.getCodeOfOrg());

			targetAsset.setNameOfOrg(sourceAsset.getNameOfOrg());

			targetAsset.setPersonShown(sourceAsset.getPersonShown());

			targetAsset.setEvent(sourceAsset.getEvent());

			targetAsset.setLastEdited(sourceAsset.getLastEdited());

			targetAsset.setMaxAvailHeight(sourceAsset.getMaxAvailHeight());

			targetAsset.setMaxAvailWidth(sourceAsset.getMaxAvailWidth());
		}
	}

	public void delete(Collection<Object> toBeDeleted, Collection<Object> toBeStored) {
		deleteStructures(toBeDeleted, toBeStored);
		deleteRelations(toBeDeleted, toBeStored);
		toBeDeleted.add(asset);
	}

	private void deleteRelations(Collection<Object> toBeDeleted, Collection<Object> toBeStored) {
		String assetId = asset.getStringId();
		List<RegionImpl> regions = dbManager.obtainObjects(RegionImpl.class, "asset_person_parent", assetId, //$NON-NLS-1$
				QueryField.EQUALS);
		toBeDeleted.addAll(regions);
		List<ComposedToImpl> composites = dbManager.obtainStruct(ComposedToImpl.class, null, false, "composite", //$NON-NLS-1$
				assetId, false);
		toBeDeleted.addAll(composites);
		List<ComposedToImpl> components = dbManager.obtainStruct(ComposedToImpl.class, null, false, "component", //$NON-NLS-1$
				assetId, true);
		for (IdentifiableObject composedTo : components)
			if (((ComposedToImpl) composedTo).getComponent().size() <= 1)
				toBeDeleted.add(composedTo);
			else
				((ComposedToImpl) composedTo).removeComponent(assetId);
		List<DerivedByImpl> derivatives = dbManager.obtainStruct(DerivedByImpl.class, null, false, "derivative", //$NON-NLS-1$
				assetId, false);
		toBeDeleted.addAll(derivatives);
		for (DerivedByImpl rel : derivatives)
			if (rel.getOriginal().indexOf(':') < 0)
				toBeStored.add(new DerivedByImpl(rel.getRecipe(), rel.getParameterFile(), rel.getTool(), rel.getDate(),
						asset.getUri(), rel.getOriginal()));
		List<DerivedByImpl> originals = dbManager.obtainStruct(DerivedByImpl.class, null, false, "original", assetId, //$NON-NLS-1$
				false);
		toBeDeleted.addAll(derivatives);
		for (DerivedByImpl rel : originals)
			if (rel.getDerivative().indexOf(':') < 0)
				toBeStored.add(new DerivedByImpl(rel.getRecipe(), rel.getParameterFile(), rel.getTool(), rel.getDate(),
						rel.getDerivative(), asset.getUri()));
	}

	private void deleteStructures(Collection<Object> toBeDeleted, Collection<Object> toBeStored) {
		String assetId = asset.getStringId();
		List<ArtworkOrObjectShownImpl> artworks = new ArrayList<ArtworkOrObjectShownImpl>(
				dbManager.obtainStructForAsset(ArtworkOrObjectShownImpl.class, assetId, false));
		toBeDeleted.addAll(artworks);
		List<LocationShownImpl> locsshown = new ArrayList<LocationShownImpl>(
				dbManager.obtainStructForAsset(LocationShownImpl.class, assetId, false));
		toBeDeleted.addAll(locsshown);
		List<LocationCreatedImpl> locscreated = new ArrayList<LocationCreatedImpl>(
				dbManager.obtainStructForAsset(LocationCreatedImpl.class, assetId, true));
		for (LocationCreatedImpl loc : locscreated) {
			loc.removeAsset(assetId);
			if (loc.getAsset().isEmpty())
				toBeDeleted.add(loc);
			else
				toBeStored.add(loc);
		}
		List<CreatorsContactImpl> contacts = new ArrayList<CreatorsContactImpl>(
				dbManager.obtainStructForAsset(CreatorsContactImpl.class, assetId, true));
		for (CreatorsContactImpl contact : contacts) {
			contact.removeAsset(assetId);
			if (contact.getAsset().isEmpty())
				toBeDeleted.add(contact);
			else
				toBeStored.add(contact);
		}
	}

	public void store(Collection<Object> toBeDeleted, Collection<Object> toBeStored) {
		String assetId = asset.getStringId();
		if (resetIptc)
			deleteStructures(toBeDeleted, toBeStored);
		if (creatorContact != null) {
			Iterator<ContactImpl> it = dbManager.queryByExample(creatorContact).iterator();
			if (it.hasNext())
				creatorContact = it.next();
			else
				toBeStored.add(creatorContact);
			String id = creatorContact.getStringId();
			List<CreatorsContactImpl> contacts = dbManager.obtainStruct(CreatorsContactImpl.class, null, false,
					"contact", id, //$NON-NLS-1$
					false);
			if (contacts.isEmpty()) {
				CreatorsContactImpl rel = new CreatorsContactImpl(id);
				rel.addAsset(assetId);
				toBeStored.add(rel);
			} else {
				CreatorsContactImpl rel = contacts.get(0);
				if (!rel.getAsset().contains(assetId)) {
					rel.addAsset(assetId);
					toBeStored.add(rel);
				}
			}
		}
		if (locationCreated != null) {
			locationCreated = queryLocation(locationCreated, toBeStored);
			Iterator<LocationCreatedImpl> it = dbManager
					.obtainStruct(LocationCreatedImpl.class, null, false, "location", //$NON-NLS-1$
							locationCreated.getStringId(), false)
					.iterator();
			if (it.hasNext()) {
				LocationCreatedImpl rel = it.next();
				if (!rel.getAsset().contains(assetId)) {
					rel.addAsset(assetId);
					toBeStored.add(rel);
				}
			} else {
				LocationCreatedImpl rel = new LocationCreatedImpl(locationCreated.getStringId());
				rel.addAsset(assetId);
				toBeStored.add(rel);
			}
		}
		if (locationsShown != null) {
			for (LocationImpl loc : locationsShown) {
				if (loc != null) {
					loc = queryLocation(loc, toBeStored);
					String id = loc.getStringId();
					List<LocationShownImpl> set = dbManager.obtainStruct(LocationShownImpl.class, assetId, false,
							"location", id, false); //$NON-NLS-1$
					if (set.isEmpty())
						toBeStored.add(new LocationShownImpl(id, assetId));
				}
			}
		}
		if (artworkShown != null) {
			for (ArtworkOrObjectImpl art : artworkShown) {
				if (art != null) {
					List<ArtworkOrObjectImpl> queryByExample = dbManager.queryByExample(art);
					if (!queryByExample.isEmpty())
						art = queryByExample.get(0);
					else
						toBeStored.add(art);
					String id = art.getStringId();
					List<ArtworkOrObjectShownImpl> set = dbManager.obtainStruct(ArtworkOrObjectShownImpl.class, assetId,
							false, "artworkOrObject", id, false); //$NON-NLS-1$
					if (set.isEmpty())
						toBeStored.add(new ArtworkOrObjectShownImpl(id, assetId));
				}
			}
		}
		Meta meta = dbManager.getMeta(true);
		Map<String, SmartCollectionImpl> oldPersonAlbums = new HashMap<String, SmartCollectionImpl>();
		List<RegionImpl> oldRegions = dbManager.obtainObjects(RegionImpl.class, "asset_person_parent", assetId, //$NON-NLS-1$
				QueryField.EQUALS);
		for (RegionImpl region : oldRegions) {
			String albumId = region.getAlbum();
			if (albumId != null) {
				SmartCollectionImpl album = dbManager.obtainById(SmartCollectionImpl.class, albumId);
				if (album != null) {
					oldPersonAlbums.put(album.getName(), album);
					if (region.getKeywordAdded())
						asset.setKeyword(Utilities.removeFromStringArray(album.getName(), asset.getKeyword()));
				}
			}
			toBeDeleted.add(region);
		}
		Map<String, SmartCollectionImpl> newPersonAlbums = new HashMap<String, SmartCollectionImpl>();
		List<String> regionIds = new ArrayList<String>();
		int n = 0;
		for (int i = 0; i < regions.length; i++) {
			if (regions[i] != null) {
				for (int j = 0; j < regions[i].length; j++) {
					MWGRegion mwgRegion = regions[i][j];
					if (mwgRegion != null) {
						if (mwgRegion.getPicasaIniEntry() != null)
							asset.setLastPicasaIniEntry(mwgRegion.getPicasaIniEntry());
						SmartCollectionImpl album = null;
						String name = mwgRegion.getName();
						String type = mwgRegion.getType();
						if (name != null && (Region.type_face.equals(type) || Region.type_pet.equals(type))) {
							if (!Region.type_pet.equals(type))
								++n;
							album = newPersonAlbums.get(name);
							if (album == null) {
								album = oldPersonAlbums.remove(name);
								if (album != null)
									newPersonAlbums.put(name, album);
							}
							if (album == null)
								for (SmartCollectionImpl sm : dbManager.<SmartCollectionImpl>obtainObjects(
										SmartCollectionImpl.class, false, "name", name, QueryField.EQUALS, "system", //$NON-NLS-1$ //$NON-NLS-2$
										true, QueryField.EQUALS, "album", true, QueryField.EQUALS)) { //$NON-NLS-1$
									album = sm;
									break;
								}
							if (album == null) {
								GroupImpl personAlbumsGroup = dbManager.obtainById(GroupImpl.class,
										Constants.GROUP_ID_PERSONS);
								if (personAlbumsGroup == null) {
									personAlbumsGroup = new GroupImpl(Messages.AssetEnsemble_persons, true);
									personAlbumsGroup.setStringId(Constants.GROUP_ID_PERSONS);
								}
								album = new SmartCollectionImpl(name, true, true, false, false, "", 0, null, 0, null, //$NON-NLS-1$
										null);
								album.setGroup_rootCollection_parent(Constants.GROUP_ID_PERSONS);
								album.addCriterion(new CriterionImpl(Constants.OID, null, album.getStringId(),
										QueryField.XREF, false));
								album.addSortCriterion(
										new SortCriterionImpl(QueryField.IPTC_DATECREATED.getKey(), null, true));
								personAlbumsGroup.addRootCollection(album.getStringId());
								toBeStored.add(personAlbumsGroup);
							}
						}
						Region region = new RegionImpl(meta.getPersonsToKeywords(), mwgRegion.getPersonEmailDigest(),
								mwgRegion.getPersonLiveCID(), mwgRegion.getDescription(), type,
								Region.type_barCode.equals(type) ? mwgRegion.getValue()
										: album == null ? null : album.getStringId());
						String id = mwgRegion.getRect64();
						region.setStringId(id);
						region.setAsset_person_parent(asset.getStringId());
						regionIds.add(id);
						toBeStored.add(region);
						if (album != null) {
							album.addAsset(asset.getStringId());
							toBeStored.add(album);
							asset.setAlbum(Utilities.addToStringArray(name, asset.getAlbum(), false));
							if (meta.getPersonsToKeywords()) {
								String[] keywords = asset.getKeyword();
								String[] newKeywords = Utilities.addToStringArray(name, keywords, false);
								if (keywords != newKeywords) {
									region.setKeywordAdded(true);
									asset.setKeyword(newKeywords);
									meta.getKeywords().addAll(Arrays.asList(newKeywords));
									toBeStored.add(meta);
								}
							}
							String[] personsShown = asset.getPersonShown();
							String[] newPersonsShown = Utilities.addToStringArray(name, personsShown, false);
							asset.setPersonShown(newPersonsShown);
						}
					}
				}
			}
		}
		for (Map.Entry<String, SmartCollectionImpl> entry : oldPersonAlbums.entrySet()) {
			asset.setAlbum(Utilities.removeFromStringArray(entry.getKey(), asset.getAlbum()));
			SmartCollectionImpl album = entry.getValue();
			album.removeAsset(asset.getStringId());
			dbManager.addDirtyCollection(album.getStringId());
		}
		asset.setNoPersons(Math.max(asset.getNoPersons(), n));
		asset.setPerson(regionIds.isEmpty() ? null : regionIds.toArray(new String[regionIds.size()]));
		toBeStored.add(asset);
		MediaExtension[] mediaExtension = asset.getMediaExtension();
		if (mediaExtension != null)
			for (MediaExtension ext : mediaExtension)
				toBeStored.add(ext);
	}

	private LocationImpl queryLocation(LocationImpl location, Collection<Object> toBeStored) {
		Iterator<LocationImpl> it = dbManager.findLocation(location).iterator();
		if (it.hasNext())
			return it.next();
		Utilities.completeLocation(dbManager, location);
		toBeStored.add(location);
		return location;
	}

	public Asset getAsset() {
		return asset;
	}

	public void resetImageData(URI uri, String volumeLabel, Date now, Date lastModified, long fileSize,
			String originalFileName, int fileSource) {
		asset.setVolume(volumeLabel);
		if (originalFileName != null)
			asset.setOriginalFileName(originalFileName);
		asset.setImportDate(now);
		asset.setImportedBy(System.getProperty("user.name")); //$NON-NLS-1$
		asset.setLastModification(lastModified);
		asset.setFileSize(fileSize);
		asset.setUri(uri.toString());
		asset.setName(Core.getFileName(uri, false));
		if (fileSource >= 0)
			asset.setFileSource(fileSource);
		asset.setHeight(-1);
		asset.setWidth(-1);
		asset.setPreviewSize(-1);
		asset.setOri(Asset_type.ori_u);
		asset.setColorType(Asset_type.colorType_uNKNOWN);
		asset.setArtist(EMPTYSTRINGARRAY);
		asset.setJpegThumbnail(EMPTYBYTEARRAY);
	}

	public void resetStatus(int status) {
		asset.setStatus(status);
		asset.setColorCode(Constants.COLOR_UNDEFINED);
		asset.setRating(Constants.RATING_UNDEFINED);
		asset.setRatedBy(null);
		asset.setUserfield1(EMPTYSTRING);
		asset.setUserfield2(EMPTYSTRING);
		asset.setSafety(QueryField.SAFETY_SAFE);
		asset.setContentType(QueryField.CONTENTTYPE_PHOTO);
		asset.setPrice(0d);
		asset.setEarnings(0d);
		asset.setSales(0);
	}

	public void resetExif() {
		asset.setColorSpace(-1);
		asset.setXResolution(Double.NaN);
		asset.setYResolution(Double.NaN);
		asset.setImageLength(-1);
		asset.setImageWidth(-1);
		asset.setExposureTime(Double.NaN);
		asset.setFNumber(Double.NaN);
		asset.setShutterSpeed(Double.NaN);
		asset.setAperture(Double.NaN);
		asset.setBrightnessValue(Double.NaN);
		asset.setExposureBias(Double.NaN);
		asset.setMaxLensAperture(Double.NaN);
		asset.setSubjectDistance(Double.NaN);
		asset.setFocalLength(Double.NaN);
		asset.setFlashEnergy(Double.NaN);
		asset.setFocalPlaneXResolution(Double.NaN);
		asset.setFocalPlaneYResolution(Double.NaN);
		asset.setExposureIndex(Double.NaN);
		asset.setDigitalZoomRatio(Double.NaN);
		asset.setFocalLengthFactor(Double.NaN);
		asset.setFlashExposureComp(Double.NaN);
		asset.setFocalLengthIn35MmFilm(-1);
		asset.setIsoSpeedRatings(EMPTYINTARRAY);
		asset.setScalarSpeedRatings(-1);
		asset.setDof(new double[] { Double.NaN, Double.NaN });
		asset.setFov(new double[] { Double.NaN });
		asset.setHyperfocalDistance(Double.NaN);
		asset.setLv(Double.NaN);
		asset.setCircleOfConfusion(Double.NaN);
		asset.setMakerNotes(EMPTYSTRINGARRAY);
		for (IMediaSupport support : CoreActivator.getDefault().getMediaSupport())
			support.resetExtension(asset);
	}

	public void resetGps() {
		asset.setGPSAltitude(Double.NaN);
		asset.setGPSLongitude(Double.NaN);
		asset.setGPSLatitude(Double.NaN);
		asset.setGPSDestDistance(Double.NaN);
		asset.setGPSDestLatitude(Double.NaN);
		asset.setGPSDestLongitude(Double.NaN);
		asset.setGPSAltitude(Double.NaN);
		asset.setGPSSpeed(Double.NaN);
		asset.setGPSDestBearing(Double.NaN);
		asset.setGPSDOP(Double.NaN);
		asset.setGPSImgDirection(Double.NaN);
		asset.setGPSTrack(Double.NaN);
	}

	private void resetFaceData() {
		asset.setNoPersons(0);
		asset.setPerson(null);
		asset.setPersonShown(null);
		asset.setDateRegionsValid(null);
		asset.setLastPicasaIniEntry(null);
	}

	public void resetIptc() {
		asset.setUsageTerms(EMPTYSTRING);
		asset.setWriterEditor(EMPTYSTRING);
		asset.setHeadline(EMPTYSTRING);
		asset.setTitle(EMPTYSTRING);
		asset.setIntellectualGenre(EMPTYSTRING);
		asset.setSpecialInstructions(EMPTYSTRING);
		asset.setKeyword(EMPTYSTRINGARRAY);
		asset.setCategory(EMPTYSTRING);
		asset.setSupplementalCats(EMPTYSTRINGARRAY);
		asset.setJobId(EMPTYSTRING);
		asset.setModelInformation(EMPTYSTRING);
		asset.setModelAge(-1);
		asset.setCodeOfOrg(EMPTYSTRINGARRAY);
		asset.setNameOfOrg(EMPTYSTRINGARRAY);
		asset.setPersonShown(EMPTYSTRINGARRAY);
		asset.setEvent(EMPTYSTRING);
		asset.setMaxAvailHeight(-1);
		asset.setMaxAvailWidth(-1);
		asset.setAuthorsPosition(EMPTYSTRING);
		asset.setCredit(EMPTYSTRING);
		asset.setSource(EMPTYSTRING);
		asset.setOwner(EMPTYSTRINGARRAY);
		asset.setSceneCode(EMPTYSTRINGARRAY);
		asset.setSubjectCode(EMPTYSTRINGARRAY);
		resetIptc = true;
	}

	public LocationImpl getLocationCreated() {
		if (locationCreated == null)
			locationCreated = new LocationImpl();
		return locationCreated;
	}

	public ContactImpl getCreatorContact() {
		if (creatorContact == null) {
			creatorContact = new ContactImpl();
			creatorContact.setAddress(EMPTYSTRINGARRAY);
			creatorContact.setEmail(EMPTYSTRINGARRAY);
			creatorContact.setPhone(EMPTYSTRINGARRAY);
			creatorContact.setWebUrl(EMPTYSTRINGARRAY);
		}
		return creatorContact;
	}

	/**
	 * @param index
	 *            STARTS AT 1 !!!
	 * @return
	 */
	public LocationImpl getLocationShown(int index) {
		if (index > 0) {
			if (locationsShown == null)
				locationsShown = new LocationImpl[index];
			else if (locationsShown.length < index) {
				LocationImpl[] copy = new LocationImpl[index];
				System.arraycopy(locationsShown, 0, copy, 0, Math.min(locationsShown.length, index));
				locationsShown = copy;
			}
			return locationsShown[index - 1] = new LocationImpl();
		}
		return null;
	}

	/**
	 * @param index
	 *            STARTS AT 1 !!!
	 * @return
	 */
	public ArtworkOrObjectImpl getArtworkOrObject(int index) {
		if (index > 0) {
			if (artworkShown == null)
				artworkShown = new ArtworkOrObjectImpl[index];
			else if (artworkShown.length < index) {
				ArtworkOrObjectImpl[] copy = new ArtworkOrObjectImpl[index];
				System.arraycopy(artworkShown, 0, copy, 0, Math.min(artworkShown.length, index));
				artworkShown = copy;
			}
			int i = index - 1;
			artworkShown[i] = new ArtworkOrObjectImpl();
			artworkShown[i].setCreator(EMPTYSTRINGARRAY);
			return artworkShown[i];
		}
		return null;
	}

	public MWGRegion getRegion(int type, int index) {
		if (index > 0) {
			if (regions[type] == null)
				regions[type] = new MWGRegion[index];
			else if (regions[type].length < index) {
				MWGRegion[] copy = new MWGRegion[index];
				System.arraycopy(regions[type], 0, copy, 0, Math.min(regions[type].length, index));
				regions[type] = copy;
			}
			MWGRegion[] regs = regions[type];
			int i = index - 1;
			if (regs[i] == null)
				regs[i] = new MWGRegion();
			return regs[i];
		}
		return null;
	}

	public void cleanUp(Date now) {
		setOrientation(asset);
		int focalLengthIn35MmFilm = asset.getFocalLengthIn35MmFilm();
		double focalLength = asset.getFocalLength();
		if (!Double.isNaN(focalLength) && focalLengthIn35MmFilm > 0)
			asset.setFocalLengthFactor(focalLengthIn35MmFilm / focalLength);
		if (Double.isNaN(asset.getGPSLatitude()) || Double.isNaN(asset.getGPSLatitude()))
			// inconsistent data - remove all
			resetGps();
		// Replace invalid image creation date
		Date dateCreated = asset.getDateTimeOriginal();
		if (dateCreated == null || DATE1900.compareTo(dateCreated) > 0 || now.compareTo(dateCreated) < 0) {
			Date dateTime = asset.getDateTime();
			asset.setDateTimeOriginal((dateTime == null) ? now : dateTime);
		}
		if (asset.getDateCreated() == null)
			asset.setDateCreated(asset.getDateTimeOriginal());
		// fold regions
		for (int i = 0; i < regions.length; i++)
			if (regions[i] != null)
				for (int j = 0; j < regions[i].length; j++) {
					MWGRegion region = regions[i][j];
					if (region != null) {
						if (region.type == null || region.type.isEmpty())
							region.type = Region.type_face;
						if (region.name != null && region.name.isEmpty())
							region.name = null;
					}
				}
		boolean goon = true;
		while (goon) {
			goon = false;
			for (int i = 0; i < regions.length; i++)
				if (regions[i] != null)
					for (int j = 0; j < regions[i].length; j++) {
						MWGRegion region1 = regions[i][j];
						if (region1 == null)
							continue;
						int s = j + 1;
						for (int n = i; n < regions.length; n++) {
							if (regions[n] != null)
								for (int m = s; m < regions[n].length; m++) {
									MWGRegion region2 = regions[n][m];
									if (region2 == null || !region1.type.equals(region2.type))
										continue;
									if (region1.name != null && region2.name != null
											&& !region1.name.equals(region2.name))
										continue;
									if (region1.intersects(region2)) {
										MWGRegion.union(region1, region2, region1);
										if (region1.name == null)
											region1.name = region2.name;
										regions[n][m] = null;
										goon = true;
									}
								}
							s = 0;
						}
					}
			asset.setDateRegionsValid(now);
		}
	}

	public static void setOrientation(Asset asset) {
		boolean flip = asset.getRotation() % 90 != 0;
		if (asset.getHeight() >= 0 && asset.getWidth() >= 0) {
			if (asset.getHeight() * 6 >= asset.getWidth() * 7)
				asset.setOri(flip ? Asset_type.ori_l : Asset_type.ori_p);
			else if (asset.getWidth() * 6 >= asset.getHeight() * 7)
				asset.setOri(flip ? Asset_type.ori_p : Asset_type.ori_l);
			else
				asset.setOri(Asset_type.ori_s);
		}
	}

	public void setAnalogProperties(AnalogProperties props) {
		asset.setAnalogFormat(props.format);
		asset.setEmulsion(props.emulsion);
		asset.setAnalogProcessing(props.processingNotes);
		asset.setAnalogType(props.type);
		asset.setScalarSpeedRatings(props.scalarSpeedRatings);
		if (props.make != null)
			asset.setMake(props.make);
		if (props.model != null)
			asset.setModel(props.model);
		if (props.serial != null)
			asset.setMake(props.serial);
		if (props.lens != null)
			asset.setLens(props.lens);
		if (props.lensSerial != null)
			asset.setLensSerial(props.lensSerial);
		asset.setFocalLength(props.focalLength);
		asset.setFocalLengthFactor(props.focalLengthFactor);
		asset.setFocalLengthIn35MmFilm(props.focalLengthIn35MmFilm);
		if (props.creationDate != null) {
			asset.setDateTime(props.creationDate);
			asset.setDateTimeOriginal(props.creationDate);
		}
		asset.setLightSource(props.lightSource);
		asset.setLv(props.lv);
		asset.setExposureTime(props.exposureTime);
		asset.setFNumber(props.fNumber);
		if (props.artist != null) {
			List<String> list = Core.fromStringList(props.artist, ",;"); //$NON-NLS-1$
			asset.setArtist(list.toArray(new String[list.size()]));
		}
		if (props.copyright != null)
			asset.setCopyright(props.copyright);
	}

	@SuppressWarnings("unchecked")
	public void setProperty(QueryField qfield, String rawValue)
			throws SecurityException, IllegalArgumentException, XMPException {
		ExifParser parser = new ExifParser(rawValue);
		Object exifValue = parser.parse();
		if (exifValue instanceof String) {
			String v = (String) exifValue;
			// Legacy Location created
			if (qfield == QueryField.IPTC_CITY)
				getLocationCreated().setCity(v);
			else if (qfield == QueryField.IPTC_SUBLOCATION)
				getLocationCreated().setSublocation(v);
			else if (qfield == QueryField.IPTC_COUNTRY)
				getLocationCreated().setCountryName(v);
			else if (qfield == QueryField.IPTC_COUNTRYCODE)
				getLocationCreated().setCountryISOCode(v);
			else if (qfield == QueryField.IPTC_STATE)
				getLocationCreated().setProvinceOrState(v);
			else if (qfield == QueryField.IPTC_WORLDREGION)
				getLocationCreated().setWorldRegion(v);
			// Legacy Artwork
			else if (qfield == QueryField.IPTC_ARTWORK)
				getArtworkOrObject(1).setTitle(v);
			// Legacy Contact
			else if (qfield == QueryField.IPTC_CONTACT) {
				ContactImpl cContact = getCreatorContact();
				String[] address = cContact.getAddress();
				for (String adr : address)
					if (adr.equals(v))
						return;
				cContact.setAddress(Utilities.addToStringArray(v, address, false));
			} else if (qfield == QueryField.COLORCODE) {
				if (v.isEmpty())
					return;
				try {
					asset.setColorCode(Integer.parseInt(v));
				} catch (NumberFormatException e) {
					int i = 0;
					lp: for (String[] names : QueryField.XMPCOLORCODES) {
						for (String name : names)
							if (name.equalsIgnoreCase(v)) {
								asset.setColorCode(i);
								break lp;
							}
						++i;
					}
				}
				return;
			}
			// Others
			else {
				int type = qfield.getType();
				Object arg = null;
				if (qfield.getCard() == 1) {
					switch (type) {
					case QueryField.T_BOOLEAN:
						arg = ("1".equals(v)) ? Boolean.TRUE //$NON-NLS-1$
								: Boolean.FALSE;
						break;
					case QueryField.T_POSITIVEINTEGER:
						if (v.isEmpty())
							return;
						arg = qfield.parseInt(v, -1);
						break;
					case QueryField.T_INTEGER:
						if (v.isEmpty())
							return;
						arg = qfield.parseInt(v, 0);
						break;
					case QueryField.T_POSITIVELONG:
						if (v.isEmpty())
							return;
						arg = QueryField.parseLong(v, -1L);
						break;
					case QueryField.T_LONG:
						if (v.isEmpty())
							return;
						arg = QueryField.parseLong(v, 0L);
						break;
					case QueryField.T_POSITIVEFLOAT:
					case QueryField.T_FLOAT:
					case QueryField.T_CURRENCY:
					case QueryField.T_FLOATB:
						if (v.isEmpty())
							return;
						arg = QueryField.parseDouble(v);
						break;
					case QueryField.T_DATE:
						if (v.isEmpty())
							return;
						arg = QueryField.parseDate(v);
						break;
					default:
						arg = v;
						break;
					}
				} else {
					switch (type) {
					case QueryField.T_POSITIVEINTEGER: {
						List<String> list = Core.fromStringList(v, " "); //$NON-NLS-1$
						int[] array = new int[list.size()];
						for (int i = 0; i < array.length; i++)
							array[i] = qfield.parseInt(list.get(i), -1);
						arg = array;
						break;
					}
					case QueryField.T_INTEGER: {
						List<String> list = Core.fromStringList(v, " "); //$NON-NLS-1$
						int[] array = new int[list.size()];
						for (int i = 0; i < array.length; i++)
							array[i] = qfield.parseInt(list.get(i), 0);
						arg = array;
						break;
					}
					case QueryField.T_POSITIVELONG: {
						List<String> list = Core.fromStringList(v, " "); //$NON-NLS-1$
						long[] array = new long[list.size()];
						for (int i = 0; i < array.length; i++)
							array[i] = QueryField.parseLong(list.get(i), -1L);
						arg = array;
						break;
					}
					case QueryField.T_LONG: {
						List<String> list = Core.fromStringList(v, " "); //$NON-NLS-1$
						long[] array = new long[list.size()];
						for (int i = 0; i < array.length; i++)
							array[i] = QueryField.parseLong(list.get(i), 0L);
						arg = array;
						break;
					}
					case QueryField.T_CURRENCY:
					case QueryField.T_POSITIVEFLOAT:
					case QueryField.T_FLOAT: {
						List<String> list = Core.fromStringList(v, " "); //$NON-NLS-1$
						double[] array = new double[list.size()];
						for (int i = 0; i < array.length; i++)
							array[i] = QueryField.parseDouble(list.get(i));
						arg = array;
						break;
					}
					default: {
						List<String> list = Core.fromStringList(v, ","); //$NON-NLS-1$
						String[] array = list.toArray(new String[list.size()]);
						for (int i = 0; i < array.length; i++)
							array[i] = array[i].trim();
						arg = array;
						break;
					}
					}
				}
				qfield.setFieldValue(asset, arg);
			}
		} else {
			if (qfield.isStruct()) {
				if (qfield == QueryField.IPTC_CONTACT) {
					Set<Entry<String, Object>> entrySet = ((Map<String, Object>) exifValue).entrySet();
					for (Entry<String, Object> entry : entrySet) {
						QueryField qfield2 = QueryField.findExifProperty(entry.getKey());
						if (qfield2 != null)
							qfield2.setFieldValue(getCreatorContact(), adaptValue(qfield2, entry.getValue()));
					}
				} else if (qfield == QueryField.IPTC_LOCATIONCREATED) {
					Set<Entry<String, Object>> entrySet = ((Map<String, Object>) exifValue).entrySet();
					for (Entry<String, Object> entry : entrySet) {
						QueryField qfield2 = QueryField.findExifProperty(entry.getKey());
						if (qfield2 != null)
							qfield2.setFieldValue(getLocationCreated(), adaptValue(qfield2, entry.getValue()));
					}
				} else if (qfield == QueryField.IPTC_ARTWORK) {
					int i = 0;
					for (Object subStruct : (List<Object>) exifValue) {
						ArtworkOrObjectImpl artworkOrObject = getArtworkOrObject(i + 1);
						Set<Entry<String, Object>> entrySet = ((Map<String, Object>) subStruct).entrySet();
						for (Entry<String, Object> entry : entrySet) {
							QueryField qfield2 = QueryField.findExifProperty(entry.getKey());
							if (qfield2 != null)
								qfield2.setFieldValue(artworkOrObject, adaptValue(qfield2, entry.getValue()));
						}
						++i;
					}
				} else if (qfield == QueryField.IPTC_LOCATIONSHOWN) {
					int i = 0;
					for (Object subStruct : (List<Object>) exifValue) {
						LocationImpl locationShown = getLocationShown(i + 1);
						Set<Entry<String, Object>> entrySet = ((Map<String, Object>) subStruct).entrySet();
						for (Entry<String, Object> entry : entrySet) {
							QueryField qfield2 = QueryField.findExifProperty(entry.getKey());
							if (qfield2 != null)
								qfield2.setFieldValue(locationShown, adaptValue(qfield2, entry.getValue()));
						}
						++i;
					}
				} else if (qfield == QueryField.REGION_MP) {
					Set<Entry<String, Object>> entrySet = ((Map<String, Object>) exifValue).entrySet();
					for (Entry<String, Object> entry : entrySet) {
						QueryField qfield2 = QueryField.findExifProperty(entry.getKey());
						if (qfield2 == QueryField.REGION_MP_LIST) {
							List<Object> regionList = (List<Object>) entry.getValue();
							int i = 0;
							for (Object o : regionList) {
								MWGRegion reg = getRegion(MP, i + 1);
								Set<Entry<String, Object>> entrySet2 = ((Map<String, Object>) o).entrySet();
								for (Entry<String, Object> e2 : entrySet2) {
									QueryField qfield3 = QueryField.findExifProperty(e2.getKey());
									try {
										if (qfield3 == QueryField.REGION_RECTANGLE)
											reg.setArea((String) e2.getValue());
										else if (qfield3 == QueryField.REGION_PERSONEMAILDIGEST)
											reg.setPersonEmailDigest((String) e2.getValue());
										else if (qfield3 == QueryField.REGION_PERSONLIVECIDE)
											reg.setPersonLiveCID((Long) e2.getValue());
										else if (qfield3 == QueryField.REGION_ALBUM)
											reg.setName((String) e2.getValue());
									} catch (NumberFormatException e) {
										importState.operation.addError(
												NLS.bind(Messages.AssetEnsemble_bad_face_data, asset.getName()), e);
									}
								}
								++i;
							}
						}
					}
				} else if (qfield == QueryField.MWG_REGION_INFO) {
					Set<Entry<String, Object>> entrySet = ((Map<String, Object>) exifValue).entrySet();
					for (Entry<String, Object> entry : entrySet) {
						QueryField qfield2 = QueryField.findExifProperty(entry.getKey());
						if (qfield2 == QueryField.MWG_REGION_LIST) {
							List<Object> regionList = (List<Object>) entry.getValue();
							int i = 0;
							for (Object o : regionList) {
								MWGRegion reg = getRegion(MWG, i + 1);
								Set<Entry<String, Object>> entrySet2 = ((Map<String, Object>) o).entrySet();
								for (Entry<String, Object> e2 : entrySet2) {
									QueryField qfield3 = QueryField.findRegionProperty(e2.getKey());
									try {
										if (qfield3 == QueryField.MWG_REGION_RECTANGLE)
											reg.setArea((Map<String, Object>) e2.getValue());
										else if (qfield3 == QueryField.MWG_REGION_ALBUM)
											reg.setName((String) e2.getValue());
										else if (qfield3 == QueryField.MWG_REGION_VALUE)
											reg.setValue((String) e2.getValue());
										else if (qfield3 == QueryField.MWG_REGION_KIND)
											reg.setType((String) e2.getValue());
										else if (qfield3 == QueryField.MWG_REGION_DESCRIPTION)
											reg.setDescription((String) e2.getValue());
									} catch (NumberFormatException e) {
										importState.operation.addError(
												NLS.bind(Messages.AssetEnsemble_bad_face_data, asset.getName()), e);
									}
								}
								++i;
							}
						}
					}
				}
			} else {
				if (qfield.getCard() != 1) {
					List<Object> struct = exifValue instanceof List<?> ? (List<Object>) exifValue
							: Arrays.asList(exifValue);
					Object arg = null;
					switch (qfield.getType()) {
					case QueryField.T_POSITIVEINTEGER: {
						int[] array = new int[struct.size()];
						int i = 0;
						for (Object element : struct)
							array[i++] = qfield.parseInt((String) element, -1);
						arg = array;
						break;
					}
					case QueryField.T_INTEGER: {
						int[] array = new int[struct.size()];
						int i = 0;
						for (Object element : struct)
							array[i++] = qfield.parseInt((String) element, 0);
						arg = array;
						break;
					}
					case QueryField.T_POSITIVELONG: {
						long[] array = new long[struct.size()];
						int i = 0;
						for (Object element : struct)
							array[i++] = QueryField.parseLong((String) element, -1L);
						arg = array;
						break;
					}
					case QueryField.T_LONG: {
						long[] array = new long[struct.size()];
						int i = 0;
						for (Object element : struct)
							array[i++] = QueryField.parseLong((String) element, 0L);
						arg = array;
						break;
					}
					case QueryField.T_CURRENCY:
					case QueryField.T_POSITIVEFLOAT:
					case QueryField.T_FLOAT: {
						double[] array = new double[struct.size()];
						int i = 0;
						for (Object element : struct)
							array[i++] = QueryField.parseDouble((String) element);
						arg = array;
						break;
					}
					default: {
						String[] array = new String[struct.size()];
						int i = 0;
						for (Object element : struct)
							array[i++] = (String) element;
						arg = array;
					}
					}
					qfield.setFieldValue(asset, arg);
				}
			}
		}
	}

	private static Object adaptValue(QueryField qfield, Object value) {
		if (qfield.getCard() == 1 || value instanceof String[])
			return value;
		return (value instanceof String) ? new String[] { (String) value } : EMPTYSTRINGARRAY;
	}

	public void removeFromTrash(List<Object> trashed) {
		if (dbManager.hasTrash() && asset != null) {
			URI uri = CoreActivator.getDefault().getVolumeManager().findFile(asset);
			if (uri != null)
				trashed.addAll(dbManager.obtainTrashForFile(uri));
		}
	}

}