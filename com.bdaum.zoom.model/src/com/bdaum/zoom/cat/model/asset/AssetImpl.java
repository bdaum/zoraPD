package com.bdaum.zoom.cat.model.asset;

import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.locationShown.LocationShown;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContact;
import com.bdaum.zoom.cat.model.derivedBy.DerivedBy;
import com.bdaum.zoom.cat.model.group.slideShow.Slide;
import com.bdaum.aoModeling.runtime.*;
import com.bdaum.zoom.cat.model.composedTo.ComposedTo;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShown;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibit;
import java.util.*;
import com.bdaum.zoom.cat.model.Asset_typeImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreated;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset asset
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class AssetImpl extends Asset_typeImpl implements Asset {

	static final long serialVersionUID = -2508902500L;

	/* ----- Constructors ----- */

	public AssetImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param name - Property
	 * @param uri - Property
	 * @param volume - Property
	 * @param fileState - Property
	 * @param fileSize - Property
	 * @param comments - Property
	 * @param format - Property
	 * @param mimeType - Property
	 * @param safety - Property
	 * @param contentType - Property
	 * @param height - Property
	 * @param width - Property
	 * @param ori - Property
	 * @param previewSize - Property
	 * @param isProgressive - Property
	 * @param colorType - Property
	 * @param xmpModifiedAt - Property
	 * @param voiceFileURI - Property
	 * @param voiceVolume - Property
	 * @param lastModification - Property
	 * @param rotation - Property
	 * @param importDate - Property
	 * @param importedBy - Property
	 * @param status - Property
	 * @param rating - Property
	 * @param ratedBy - Property
	 * @param colorCode - Property
	 * @param userfield1 - Property
	 * @param userfield2 - Property
	 * @param imageLength - Property
	 * @param imageWidth - Property
	 * @param compression - Property
	 * @param photometricInterpretation - Property
	 * @param orientation - Property
	 * @param samplesPerPixel - Property
	 * @param xResolution - Property
	 * @param yResolution - Property
	 * @param dateTime - Property
	 * @param imageDescription - Property
	 * @param make - Property
	 * @param model - Property
	 * @param software - Property
	 * @param originalFileName - Property
	 * @param originalImageId - Property
	 * @param copyright - Property
	 * @param colorSpace - Property
	 * @param profileDescription - Property
	 * @param dateTimeOriginal - Property
	 * @param exposureTime - Property
	 * @param fNumber - Property
	 * @param exposureProgram - Property
	 * @param spectralSensitivity - Property
	 * @param scalarSpeedRatings - Property
	 * @param shutterSpeed - Property
	 * @param aperture - Property
	 * @param brightnessValue - Property
	 * @param exposureBias - Property
	 * @param maxLensAperture - Property
	 * @param subjectDistance - Property
	 * @param meteringMode - Property
	 * @param lightSource - Property
	 * @param flashFired - Property
	 * @param returnLightDetected - Property
	 * @param flashAuto - Property
	 * @param flashFunction - Property
	 * @param redEyeReduction - Property
	 * @param flashExposureComp - Property
	 * @param focalLength - Property
	 * @param flashEnergy - Property
	 * @param focalPlaneXResolution - Property
	 * @param focalPlaneYResolution - Property
	 * @param exposureIndex - Property
	 * @param sensingMethod - Property
	 * @param fileSource - Property
	 * @param exposureMode - Property
	 * @param whiteBalance - Property
	 * @param digitalZoomRatio - Property
	 * @param focalLengthIn35MmFilm - Property
	 * @param focalLengthFactor - Property
	 * @param circleOfConfusion - Property
	 * @param hyperfocalDistance - Property
	 * @param lv - Property
	 * @param sceneCaptureType - Property
	 * @param gainControl - Property
	 * @param contrast - Property
	 * @param saturation - Property
	 * @param sharpness - Property
	 * @param vibrance - Property
	 * @param subjectDistanceRange - Property
	 * @param lens - Property
	 * @param lensSerial - Property
	 * @param serial - Property
	 * @param gPSLatitude - Property
	 * @param gPSLongitude - Property
	 * @param gPSAltitude - Property
	 * @param gPSTime - Property
	 * @param gPSDOP - Property
	 * @param gPSSpeed - Property
	 * @param gPSTrackRef - Property
	 * @param gPSTrack - Property
	 * @param gPSImgDirectionRef - Property
	 * @param gPSImgDirection - Property
	 * @param gPSMapDatum - Property
	 * @param gPSDestLatitude - Property
	 * @param gPSDestLongitude - Property
	 * @param gPSDestBearingRef - Property
	 * @param gPSDestBearing - Property
	 * @param gPSDestDistance - Property
	 * @param gPSAreaInformation - Property
	 * @param gPSDateStamp - Property
	 * @param gPSDifferential - Property
	 * @param writerEditor - Property
	 * @param headline - Property
	 * @param intellectualGenre - Property
	 * @param title - Property
	 * @param specialInstructions - Property
	 * @param jobId - Property
	 * @param category - Property
	 * @param urgency - Property
	 * @param authorsPosition - Property
	 * @param credit - Property
	 * @param usageTerms - Property
	 * @param source - Property
	 * @param dateCreated - Property
	 * @param modelInformation - Property
	 * @param modelAge - Property
	 * @param event - Property
	 * @param lastEdited - Property
	 * @param maxAvailHeight - Property
	 * @param maxAvailWidth - Property
	 * @param emulsion - Property
	 * @param analogType - Property
	 * @param analogFormat - Property
	 * @param analogProcessing - Property
	 * @param lastEditor - Property
	 * @param score - Property
	 * @param sales - Property
	 * @param price - Property
	 * @param earnings - Property
	 * @param noPersons - Property
	 * @param dateRegionsValid - Property
	 * @param lastPicasaIniEntry - Property
	 * @param noLensInfo - Property
	 */
	public AssetImpl(String name, String uri, String volume, int fileState,
			long fileSize, String comments, String format, String mimeType,
			int safety, int contentType, int height, int width, String ori,
			int previewSize, boolean isProgressive, String colorType,
			Date xmpModifiedAt, String voiceFileURI, String voiceVolume,
			Date lastModification, int rotation, Date importDate,
			String importedBy, int status, int rating, String ratedBy,
			int colorCode, String userfield1, String userfield2,
			int imageLength, int imageWidth, int compression,
			int photometricInterpretation, int orientation,
			int samplesPerPixel, double xResolution, double yResolution,
			Date dateTime, String imageDescription, String make, String model,
			String software, String originalFileName, String originalImageId,
			String copyright, int colorSpace, String profileDescription,
			Date dateTimeOriginal, double exposureTime, double fNumber,
			int exposureProgram, String spectralSensitivity,
			int scalarSpeedRatings, double shutterSpeed, double aperture,
			double brightnessValue, double exposureBias,
			double maxLensAperture, double subjectDistance, int meteringMode,
			int lightSource, boolean flashFired, int returnLightDetected,
			int flashAuto, boolean flashFunction, boolean redEyeReduction,
			double flashExposureComp, double focalLength, double flashEnergy,
			double focalPlaneXResolution, double focalPlaneYResolution,
			double exposureIndex, int sensingMethod, int fileSource,
			int exposureMode, String whiteBalance, double digitalZoomRatio,
			int focalLengthIn35MmFilm, double focalLengthFactor,
			double circleOfConfusion, double hyperfocalDistance, double lv,
			int sceneCaptureType, int gainControl, int contrast,
			int saturation, int sharpness, int vibrance,
			int subjectDistanceRange, String lens, String lensSerial,
			String serial, double gPSLatitude, double gPSLongitude,
			double gPSAltitude, Date gPSTime, double gPSDOP, double gPSSpeed,
			String gPSTrackRef, double gPSTrack, String gPSImgDirectionRef,
			double gPSImgDirection, String gPSMapDatum, double gPSDestLatitude,
			double gPSDestLongitude, String gPSDestBearingRef,
			double gPSDestBearing, double gPSDestDistance,
			String gPSAreaInformation, Date gPSDateStamp, int gPSDifferential,
			String writerEditor, String headline, String intellectualGenre,
			String title, String specialInstructions, String jobId,
			String category, int urgency, String authorsPosition,
			String credit, String usageTerms, String source, Date dateCreated,
			String modelInformation, int modelAge, String event,
			Date lastEdited, int maxAvailHeight, int maxAvailWidth,
			String emulsion, int analogType, int analogFormat,
			String analogProcessing, String lastEditor, float score, int sales,
			double price, double earnings, int noPersons,
			Date dateRegionsValid, String lastPicasaIniEntry, boolean noLensInfo) {
		super(name, uri, volume, fileState, fileSize, comments, format,
				mimeType, safety, contentType, height, width, ori, previewSize,
				isProgressive, colorType, xmpModifiedAt, voiceFileURI,
				voiceVolume, lastModification, rotation, importDate,
				importedBy, status, rating, ratedBy, colorCode, userfield1,
				userfield2, imageLength, imageWidth, compression,
				photometricInterpretation, orientation, samplesPerPixel,
				xResolution, yResolution, dateTime, imageDescription, make,
				model, software, originalFileName, originalImageId, copyright,
				colorSpace, profileDescription, dateTimeOriginal, exposureTime,
				fNumber, exposureProgram, spectralSensitivity,
				scalarSpeedRatings, shutterSpeed, aperture, brightnessValue,
				exposureBias, maxLensAperture, subjectDistance, meteringMode,
				lightSource, flashFired, returnLightDetected, flashAuto,
				flashFunction, redEyeReduction, flashExposureComp, focalLength,
				flashEnergy, focalPlaneXResolution, focalPlaneYResolution,
				exposureIndex, sensingMethod, fileSource, exposureMode,
				whiteBalance, digitalZoomRatio, focalLengthIn35MmFilm,
				focalLengthFactor, circleOfConfusion, hyperfocalDistance, lv,
				sceneCaptureType, gainControl, contrast, saturation, sharpness,
				vibrance, subjectDistanceRange, lens, lensSerial, serial,
				gPSLatitude, gPSLongitude, gPSAltitude, gPSTime, gPSDOP,
				gPSSpeed, gPSTrackRef, gPSTrack, gPSImgDirectionRef,
				gPSImgDirection, gPSMapDatum, gPSDestLatitude,
				gPSDestLongitude, gPSDestBearingRef, gPSDestBearing,
				gPSDestDistance, gPSAreaInformation, gPSDateStamp,
				gPSDifferential, writerEditor, headline, intellectualGenre,
				title, specialInstructions, jobId, category, urgency,
				authorsPosition, credit, usageTerms, source, dateCreated,
				modelInformation, modelAge, event, lastEdited, maxAvailHeight,
				maxAvailWidth, emulsion, analogType, analogFormat,
				analogProcessing, lastEditor, score, sales, price, earnings,
				noPersons, dateRegionsValid, lastPicasaIniEntry, noLensInfo);

	}

	/* ----- Initialisation ----- */

	private static List<Instrumentation> _instrumentation = new ArrayList<Instrumentation>();

	public static void attachInstrumentation(int point, Aspect aspect,
			Object extension) {
		attachInstrumentation(_instrumentation, point, aspect, extension);
	}

	public static void attachInstrumentation(int point, Aspect aspect) {
		attachInstrumentation(_instrumentation, point, aspect);
	}

	public static void attachInstrumentation(Properties properties,
			Aspect aspect) {
		attachInstrumentation(_instrumentation, AssetImpl.class, properties,
				aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc smartCollection_parent *** */

	private String smartCollection_parent;

	/**
	 * Set value of property smartCollection_parent
	 *
	 * @param _value - new field value
	 */
	public void setSmartCollection_parent(String _value) {
		smartCollection_parent = _value;
	}

	/**
	 * Get value of property smartCollection_parent
	 *
	 * @return - value of field smartCollection_parent
	 */
	public String getSmartCollection_parent() {
		return smartCollection_parent;
	}

	/* *** Incoming Arc slide_asset_parent *** */

	private String slide_asset_parent;

	/**
	 * Set value of property slide_asset_parent
	 *
	 * @param _value - new field value
	 */
	public void setSlide_asset_parent(String _value) {
		slide_asset_parent = _value;
	}

	/**
	 * Get value of property slide_asset_parent
	 *
	 * @return - value of field slide_asset_parent
	 */
	public String getSlide_asset_parent() {
		return slide_asset_parent;
	}

	/* *** Incoming Arc exhibit_asset_parent *** */

	private String exhibit_asset_parent;

	/**
	 * Set value of property exhibit_asset_parent
	 *
	 * @param _value - new field value
	 */
	public void setExhibit_asset_parent(String _value) {
		exhibit_asset_parent = _value;
	}

	/**
	 * Get value of property exhibit_asset_parent
	 *
	 * @return - value of field exhibit_asset_parent
	 */
	public String getExhibit_asset_parent() {
		return exhibit_asset_parent;
	}

	/* *** Incoming Arc webExhibit_asset_parent *** */

	private String webExhibit_asset_parent;

	/**
	 * Set value of property webExhibit_asset_parent
	 *
	 * @param _value - new field value
	 */
	public void setWebExhibit_asset_parent(String _value) {
		webExhibit_asset_parent = _value;
	}

	/**
	 * Get value of property webExhibit_asset_parent
	 *
	 * @return - value of field webExhibit_asset_parent
	 */
	public String getWebExhibit_asset_parent() {
		return webExhibit_asset_parent;
	}

	/* *** Incoming Arc derivedBy_derivative_parent *** */

	private String derivedBy_derivative_parent;

	/**
	 * Set value of property derivedBy_derivative_parent
	 *
	 * @param _value - new field value
	 */
	public void setDerivedBy_derivative_parent(String _value) {
		derivedBy_derivative_parent = _value;
	}

	/**
	 * Get value of property derivedBy_derivative_parent
	 *
	 * @return - value of field derivedBy_derivative_parent
	 */
	public String getDerivedBy_derivative_parent() {
		return derivedBy_derivative_parent;
	}

	/* *** Incoming Arc derivedBy_original_parent *** */

	private String derivedBy_original_parent;

	/**
	 * Set value of property derivedBy_original_parent
	 *
	 * @param _value - new field value
	 */
	public void setDerivedBy_original_parent(String _value) {
		derivedBy_original_parent = _value;
	}

	/**
	 * Get value of property derivedBy_original_parent
	 *
	 * @return - value of field derivedBy_original_parent
	 */
	public String getDerivedBy_original_parent() {
		return derivedBy_original_parent;
	}

	/* *** Incoming Arc composedTo_component_parent *** */

	private String composedTo_component_parent;

	/**
	 * Set value of property composedTo_component_parent
	 *
	 * @param _value - new field value
	 */
	public void setComposedTo_component_parent(String _value) {
		composedTo_component_parent = _value;
	}

	/**
	 * Get value of property composedTo_component_parent
	 *
	 * @return - value of field composedTo_component_parent
	 */
	public String getComposedTo_component_parent() {
		return composedTo_component_parent;
	}

	/* *** Incoming Arc composedTo_composite_parent *** */

	private String composedTo_composite_parent;

	/**
	 * Set value of property composedTo_composite_parent
	 *
	 * @param _value - new field value
	 */
	public void setComposedTo_composite_parent(String _value) {
		composedTo_composite_parent = _value;
	}

	/**
	 * Get value of property composedTo_composite_parent
	 *
	 * @return - value of field composedTo_composite_parent
	 */
	public String getComposedTo_composite_parent() {
		return composedTo_composite_parent;
	}

	/* *** Incoming Arc artworkOrObjectShown_parent *** */

	private AomList<String> artworkOrObjectShown_parent = new FastArrayList<String>(
			"artworkOrObjectShown_parent",
			PackageInterface.Asset_artworkOrObjectShown_parent, 0,
			Integer.MAX_VALUE, null, null);

	/**
	 * Set value of property artworkOrObjectShown_parent
	 *
	 * @param _value - new element value
	 */
	public void setArtworkOrObjectShown_parent(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL,
					"artworkOrObjectShown_parent"));
		artworkOrObjectShown_parent = _value;
	}

	/**
	 * Set value of property artworkOrObjectShown_parent
	 *
	 * @param _value - new element value
	 */
	public void setArtworkOrObjectShown_parent(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL,
					"artworkOrObjectShown_parent"));
		artworkOrObjectShown_parent = new FastArrayList<String>(_value,
				"artworkOrObjectShown_parent",
				PackageInterface.Asset_artworkOrObjectShown_parent, 0,
				Integer.MAX_VALUE, null, null);
	}

	/**
	 * Set single element of list artworkOrObjectShown_parent
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setArtworkOrObjectShown_parent(String _element, int _i) {
		artworkOrObjectShown_parent.set(_i, _element);
	}

	/**
	 * Add an element to list artworkOrObjectShown_parent
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addArtworkOrObjectShown_parent(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL,
					"ArtworkOrObjectShown_parent._element"));

		return artworkOrObjectShown_parent.add(_element);
	}

	/**
	 * Remove an element from list artworkOrObjectShown_parent
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeArtworkOrObjectShown_parent(String _element) {
		return artworkOrObjectShown_parent.remove(_element);
	}

	/**
	 * Make artworkOrObjectShown_parent empty 
	 */
	public void clearArtworkOrObjectShown_parent() {
		artworkOrObjectShown_parent.clear();
	}

	/**
	 * Get value of property artworkOrObjectShown_parent
	 *
	 * @return - value of field artworkOrObjectShown_parent
	 */
	public AomList<String> getArtworkOrObjectShown_parent() {
		return artworkOrObjectShown_parent;
	}

	/**
	 * Get single element of list artworkOrObjectShown_parent
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list artworkOrObjectShown_parent
	 */
	public String getArtworkOrObjectShown_parent(int _i) {
		return artworkOrObjectShown_parent.get(_i);
	}

	/* *** Incoming Arc creatorsContact_parent *** */

	private String creatorsContact_parent;

	/**
	 * Set value of property creatorsContact_parent
	 *
	 * @param _value - new field value
	 */
	public void setCreatorsContact_parent(String _value) {
		creatorsContact_parent = _value;
	}

	/**
	 * Get value of property creatorsContact_parent
	 *
	 * @return - value of field creatorsContact_parent
	 */
	public String getCreatorsContact_parent() {
		return creatorsContact_parent;
	}

	/* *** Incoming Arc locationCreated_parent *** */

	private String locationCreated_parent;

	/**
	 * Set value of property locationCreated_parent
	 *
	 * @param _value - new field value
	 */
	public void setLocationCreated_parent(String _value) {
		locationCreated_parent = _value;
	}

	/**
	 * Get value of property locationCreated_parent
	 *
	 * @return - value of field locationCreated_parent
	 */
	public String getLocationCreated_parent() {
		return locationCreated_parent;
	}

	/* *** Incoming Arc locationShown_parent *** */

	private AomList<String> locationShown_parent = new FastArrayList<String>(
			"locationShown_parent",
			PackageInterface.Asset_locationShown_parent, 0, Integer.MAX_VALUE,
			null, null);

	/**
	 * Set value of property locationShown_parent
	 *
	 * @param _value - new element value
	 */
	public void setLocationShown_parent(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "locationShown_parent"));
		locationShown_parent = _value;
	}

	/**
	 * Set value of property locationShown_parent
	 *
	 * @param _value - new element value
	 */
	public void setLocationShown_parent(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "locationShown_parent"));
		locationShown_parent = new FastArrayList<String>(_value,
				"locationShown_parent",
				PackageInterface.Asset_locationShown_parent, 0,
				Integer.MAX_VALUE, null, null);
	}

	/**
	 * Set single element of list locationShown_parent
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setLocationShown_parent(String _element, int _i) {
		locationShown_parent.set(_i, _element);
	}

	/**
	 * Add an element to list locationShown_parent
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addLocationShown_parent(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL,
					"LocationShown_parent._element"));

		return locationShown_parent.add(_element);
	}

	/**
	 * Remove an element from list locationShown_parent
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeLocationShown_parent(String _element) {
		return locationShown_parent.remove(_element);
	}

	/**
	 * Make locationShown_parent empty 
	 */
	public void clearLocationShown_parent() {
		locationShown_parent.clear();
	}

	/**
	 * Get value of property locationShown_parent
	 *
	 * @return - value of field locationShown_parent
	 */
	public AomList<String> getLocationShown_parent() {
		return locationShown_parent;
	}

	/**
	 * Get single element of list locationShown_parent
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list locationShown_parent
	 */
	public String getLocationShown_parent(int _i) {
		return locationShown_parent.get(_i);
	}

	/* *** Arc track *** */

	private String[] track = new String[0];

	/**
	 * Set value of property track
	 *
	 * @param _value - new element value
	 */
	public void setTrack(String[] _value) {
		track = _value;
	}

	/**
	 * Set single element of array track
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setTrack(String _element, int _i) {
		track[_i] = _element;
	}

	/**
	 * Get value of property track
	 *
	 * @return - value of field track
	 */
	public String[] getTrack() {
		return track;
	}

	/**
	 * Get single element of array track
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array track
	 */
	public String getTrack(int _i) {
		return track[_i];
	}

	/* *** Arc person *** */

	private String[] person = new String[0];

	/**
	 * Set value of property person
	 *
	 * @param _value - new element value
	 */
	public void setPerson(String[] _value) {
		person = _value;
	}

	/**
	 * Set single element of array person
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setPerson(String _element, int _i) {
		person[_i] = _element;
	}

	/**
	 * Get value of property person
	 *
	 * @return - value of field person
	 */
	public String[] getPerson() {
		return person;
	}

	/**
	 * Get single element of array person
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array person
	 */
	public String getPerson(int _i) {
		return person[_i];
	}

	/* *** Arc mediaExtension *** */

	private MediaExtension[] mediaExtension = new MediaExtension[0];

	/**
	 * Set value of property mediaExtension
	 *
	 * @param _value - new element value
	 */
	public void setMediaExtension(MediaExtension[] _value) {
		mediaExtension = _value;
		if (_value != null)
			for (int _i = 0; _i < _value.length; _i++)
				if (_value[_i] != null)
					_value[_i].setAsset_parent(this);

	}

	/**
	 * Set single element of array mediaExtension
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setMediaExtension(MediaExtension _element, int _i) {
		if (_element != null)
			_element.setAsset_parent(this);
		mediaExtension[_i] = _element;
	}

	/**
	 * Get value of property mediaExtension
	 *
	 * @return - value of field mediaExtension
	 */
	public MediaExtension[] getMediaExtension() {
		return mediaExtension;
	}

	/**
	 * Get single element of array mediaExtension
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array mediaExtension
	 */
	public MediaExtension getMediaExtension(int _i) {
		return mediaExtension[_i];
	}

	/* ----- Equality and Identity ----- */

	/**
	 * Compares the specified object with this object for equality.
	 *
	 * @param o the object to be compared with this object.
	 * @return true if the specified object is equal to this object.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {

		return (o instanceof AssetImpl)
				&& getStringId().equals(((AssetImpl) o).getStringId());
	}

	/**
	 * Returns the hash code for this object.
	 * @return the hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 * @see java.lang.Object#equals(Object)
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {

		return getStringId().hashCode();
	}

	/**
	 * Compares the specified object with this object for primary key equality.
	 *
	 * @param o the object to be compared with this object
	 * @return true if the specified object is key-identical to this object
	 * @see com.bdaum.aoModeling.runtime.IAsset#isKeyIdentical
	 */
	public boolean isKeyIdentical(Object o) {
		return this == o;
	}

	/**
	 * Returns the hash code for the primary key of this object.
	 * @return the primary key hash code value
	 * @see com.bdaum.aoModeling.runtime.IAsset#keyHashCode
	 */
	public int keyHashCode() {
		return hashCode();
	}

	/**
	 * Creates a clone of this object.

	 *   Not supported in this class
	 * @throws CloneNotSupportedException;
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException {

		super.validateCompleteness();
	}

	/**
	 * Performs constraint validation
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 * @see com.bdaum.aoModeling.runtime.IAsset#validate
	 */
	public void validate() throws ConstraintException {
		validateCompleteness();
	}

	@Override
	public String toString() {
		return getStringId();
	}

}
