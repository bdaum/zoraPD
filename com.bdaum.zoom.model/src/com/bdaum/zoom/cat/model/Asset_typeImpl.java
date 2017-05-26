package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset asset
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Asset_typeImpl extends AomObject implements Asset_type {

	static final long serialVersionUID = -414715061L;

	/* ----- Constructors ----- */

	public Asset_typeImpl() {
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
	 */
	public Asset_typeImpl(String name, String uri, String volume,
			int fileState, long fileSize, String comments, String format,
			String mimeType, int safety, int contentType, int height,
			int width, String ori, int previewSize, boolean isProgressive,
			String colorType, Date xmpModifiedAt, String voiceFileURI,
			String voiceVolume, Date lastModification, int rotation,
			Date importDate, String importedBy, int status, int rating,
			String ratedBy, int colorCode, String userfield1,
			String userfield2, int imageLength, int imageWidth,
			int compression, int photometricInterpretation, int orientation,
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
			Date dateRegionsValid, String lastPicasaIniEntry) {
		super();
		this.name = name;
		this.uri = uri;
		this.volume = volume;
		this.fileState = fileState;
		this.fileSize = fileSize;
		this.comments = comments;
		this.format = format;
		this.mimeType = mimeType;
		this.safety = safety;
		this.contentType = contentType;
		this.height = height;
		this.width = width;
		this.ori = ori;
		this.previewSize = previewSize;
		this.isProgressive = isProgressive;
		this.colorType = colorType;
		this.xmpModifiedAt = xmpModifiedAt;
		this.voiceFileURI = voiceFileURI;
		this.voiceVolume = voiceVolume;
		this.lastModification = lastModification;
		this.rotation = rotation;
		this.importDate = importDate;
		this.importedBy = importedBy;
		this.status = status;
		this.rating = rating;
		this.ratedBy = ratedBy;
		this.colorCode = colorCode;
		this.userfield1 = userfield1;
		this.userfield2 = userfield2;
		this.imageLength = imageLength;
		this.imageWidth = imageWidth;
		this.compression = compression;
		this.photometricInterpretation = photometricInterpretation;
		this.orientation = orientation;
		this.samplesPerPixel = samplesPerPixel;
		this.xResolution = xResolution;
		this.yResolution = yResolution;
		this.dateTime = dateTime;
		this.imageDescription = imageDescription;
		this.make = make;
		this.model = model;
		this.software = software;
		this.originalFileName = originalFileName;
		this.originalImageId = originalImageId;
		this.copyright = copyright;
		this.colorSpace = colorSpace;
		this.profileDescription = profileDescription;
		this.dateTimeOriginal = dateTimeOriginal;
		this.exposureTime = exposureTime;
		this.fNumber = fNumber;
		this.exposureProgram = exposureProgram;
		this.spectralSensitivity = spectralSensitivity;
		this.scalarSpeedRatings = scalarSpeedRatings;
		this.shutterSpeed = shutterSpeed;
		this.aperture = aperture;
		this.brightnessValue = brightnessValue;
		this.exposureBias = exposureBias;
		this.maxLensAperture = maxLensAperture;
		this.subjectDistance = subjectDistance;
		this.meteringMode = meteringMode;
		this.lightSource = lightSource;
		this.flashFired = flashFired;
		this.returnLightDetected = returnLightDetected;
		this.flashAuto = flashAuto;
		this.flashFunction = flashFunction;
		this.redEyeReduction = redEyeReduction;
		this.flashExposureComp = flashExposureComp;
		this.focalLength = focalLength;
		this.flashEnergy = flashEnergy;
		this.focalPlaneXResolution = focalPlaneXResolution;
		this.focalPlaneYResolution = focalPlaneYResolution;
		this.exposureIndex = exposureIndex;
		this.sensingMethod = sensingMethod;
		this.fileSource = fileSource;
		this.exposureMode = exposureMode;
		this.whiteBalance = whiteBalance;
		this.digitalZoomRatio = digitalZoomRatio;
		this.focalLengthIn35MmFilm = focalLengthIn35MmFilm;
		this.focalLengthFactor = focalLengthFactor;
		this.circleOfConfusion = circleOfConfusion;
		this.hyperfocalDistance = hyperfocalDistance;
		this.lv = lv;
		this.sceneCaptureType = sceneCaptureType;
		this.gainControl = gainControl;
		this.contrast = contrast;
		this.saturation = saturation;
		this.sharpness = sharpness;
		this.vibrance = vibrance;
		this.subjectDistanceRange = subjectDistanceRange;
		this.lens = lens;
		this.lensSerial = lensSerial;
		this.serial = serial;
		this.gPSLatitude = gPSLatitude;
		this.gPSLongitude = gPSLongitude;
		this.gPSAltitude = gPSAltitude;
		this.gPSTime = gPSTime;
		this.gPSDOP = gPSDOP;
		this.gPSSpeed = gPSSpeed;
		this.gPSTrackRef = gPSTrackRef;
		this.gPSTrack = gPSTrack;
		this.gPSImgDirectionRef = gPSImgDirectionRef;
		this.gPSImgDirection = gPSImgDirection;
		this.gPSMapDatum = gPSMapDatum;
		this.gPSDestLatitude = gPSDestLatitude;
		this.gPSDestLongitude = gPSDestLongitude;
		this.gPSDestBearingRef = gPSDestBearingRef;
		this.gPSDestBearing = gPSDestBearing;
		this.gPSDestDistance = gPSDestDistance;
		this.gPSAreaInformation = gPSAreaInformation;
		this.gPSDateStamp = gPSDateStamp;
		this.gPSDifferential = gPSDifferential;
		this.writerEditor = writerEditor;
		this.headline = headline;
		this.intellectualGenre = intellectualGenre;
		this.title = title;
		this.specialInstructions = specialInstructions;
		this.jobId = jobId;
		this.category = category;
		this.urgency = urgency;
		this.authorsPosition = authorsPosition;
		this.credit = credit;
		this.usageTerms = usageTerms;
		this.source = source;
		this.dateCreated = dateCreated;
		this.modelInformation = modelInformation;
		this.modelAge = modelAge;
		this.event = event;
		this.lastEdited = lastEdited;
		this.maxAvailHeight = maxAvailHeight;
		this.maxAvailWidth = maxAvailWidth;
		this.emulsion = emulsion;
		this.analogType = analogType;
		this.analogFormat = analogFormat;
		this.analogProcessing = analogProcessing;
		this.lastEditor = lastEditor;
		this.score = score;
		this.sales = sales;
		this.price = price;
		this.earnings = earnings;
		this.noPersons = noPersons;
		this.dateRegionsValid = dateRegionsValid;
		this.lastPicasaIniEntry = lastPicasaIniEntry;

	}

	/* ----- Fields ----- */

	/* *** Property name *** */

	private String name = AomConstants.INIT_String;

	/**
	 * Set value of property name
	 *
	 * @param _value - new field value
	 */
	public void setName(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "name"));
		name = _value;
	}

	/**
	 * Get value of property name
	 *
	 * @return - value of field name
	 */
	public String getName() {
		return name;
	}

	/* *** Property uri *** */

	private String uri = AomConstants.INIT_String;

	/**
	 * Set value of property uri
	 *
	 * @param _value - new field value
	 */
	public void setUri(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "uri"));
		uri = _value;
	}

	/**
	 * Get value of property uri
	 *
	 * @return - value of field uri
	 */
	public String getUri() {
		return uri;
	}

	/* *** Property volume *** */

	private String volume;

	/**
	 * Set value of property volume
	 *
	 * @param _value - new field value
	 */
	public void setVolume(String _value) {
		volume = _value;
	}

	/**
	 * Get value of property volume
	 *
	 * @return - value of field volume
	 */
	public String getVolume() {
		return volume;
	}

	/* *** Property fileState *** */

	private transient int fileState;

	/**
	 * Set value of property fileState
	 *
	 * @param _value - new field value
	 */
	public void setFileState(int _value) {
		fileState = _value;
	}

	/**
	 * Get value of property fileState
	 *
	 * @return - value of field fileState
	 */
	public int getFileState() {
		return fileState;
	}

	/* *** Property fileSize *** */

	private long fileSize;

	/**
	 * Set value of property fileSize
	 *
	 * @param _value - new field value
	 */
	public void setFileSize(long _value) {
		fileSize = _value;
	}

	/**
	 * Get value of property fileSize
	 *
	 * @return - value of field fileSize
	 */
	public long getFileSize() {
		return fileSize;
	}

	/* *** Property album *** */

	private String[] album = new String[0];

	/**
	 * Set value of property album
	 *
	 * @param _value - new element value
	 */
	public void setAlbum(String[] _value) {
		album = _value;
	}

	/**
	 * Set single element of array album
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setAlbum(String _element, int _i) {
		album[_i] = _element;
	}

	/**
	 * Get value of property album
	 *
	 * @return - value of field album
	 */
	public String[] getAlbum() {
		return album;
	}

	/**
	 * Get single element of array album
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array album
	 */
	public String getAlbum(int _i) {
		return album[_i];
	}

	/* *** Property comments *** */

	private String comments;

	/**
	 * Set value of property comments
	 *
	 * @param _value - new field value
	 */
	public void setComments(String _value) {
		comments = _value;
	}

	/**
	 * Get value of property comments
	 *
	 * @return - value of field comments
	 */
	public String getComments() {
		return comments;
	}

	/* *** Property format *** */

	private String format;

	/**
	 * Set value of property format
	 *
	 * @param _value - new field value
	 */
	public void setFormat(String _value) {
		format = _value;
	}

	/**
	 * Get value of property format
	 *
	 * @return - value of field format
	 */
	public String getFormat() {
		return format;
	}

	/* *** Property mimeType *** */

	private String mimeType;

	/**
	 * Set value of property mimeType
	 *
	 * @param _value - new field value
	 */
	public void setMimeType(String _value) {
		mimeType = _value;
	}

	/**
	 * Get value of property mimeType
	 *
	 * @return - value of field mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/* *** Property safety *** */

	private int safety;

	/**
	 * Set value of property safety
	 *
	 * @param _value - new field value
	 */
	public void setSafety(int _value) {
		safety = _value;
	}

	/**
	 * Get value of property safety
	 *
	 * @return - value of field safety
	 */
	public int getSafety() {
		return safety;
	}

	/* *** Property contentType *** */

	private int contentType;

	/**
	 * Set value of property contentType
	 *
	 * @param _value - new field value
	 */
	public void setContentType(int _value) {
		contentType = _value;
	}

	/**
	 * Get value of property contentType
	 *
	 * @return - value of field contentType
	 */
	public int getContentType() {
		return contentType;
	}

	/* *** Property height *** */

	private int height;

	/**
	 * Set value of property height
	 *
	 * @param _value - new field value
	 */
	public void setHeight(int _value) {
		height = _value;
	}

	/**
	 * Get value of property height
	 *
	 * @return - value of field height
	 */
	public int getHeight() {
		return height;
	}

	/* *** Property width *** */

	private int width;

	/**
	 * Set value of property width
	 *
	 * @param _value - new field value
	 */
	public void setWidth(int _value) {
		width = _value;
	}

	/**
	 * Get value of property width
	 *
	 * @return - value of field width
	 */
	public int getWidth() {
		return width;
	}

	/* *** Property ori *** */

	private String ori = AomConstants.INIT_String;

	/**
	 * Set value of property ori
	 *
	 * @param _value - new field value
	 */
	public void setOri(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "ori"));
		String _valueIntern = _value.intern();
		if (!(_valueIntern == ori_u || _valueIntern == ori_p
				|| _valueIntern == ori_s || _valueIntern == ori_l))
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_VIOLATES_ENUMERATION,
					String.valueOf(_value)));

		ori = _value;
	}

	/**
	 * Get value of property ori
	 *
	 * @return - value of field ori
	 */
	public String getOri() {
		return ori;
	}

	/* *** Property previewSize *** */

	private int previewSize;

	/**
	 * Set value of property previewSize
	 *
	 * @param _value - new field value
	 */
	public void setPreviewSize(int _value) {
		previewSize = _value;
	}

	/**
	 * Get value of property previewSize
	 *
	 * @return - value of field previewSize
	 */
	public int getPreviewSize() {
		return previewSize;
	}

	/* *** Property isProgressive *** */

	private boolean isProgressive;

	/**
	 * Set value of property isProgressive
	 *
	 * @param _value - new field value
	 */
	public void setIsProgressive(boolean _value) {
		isProgressive = _value;
	}

	/**
	 * Get value of property isProgressive
	 *
	 * @return - value of field isProgressive
	 */
	public boolean getIsProgressive() {
		return isProgressive;
	}

	/* *** Property colorType *** */

	private String colorType = AomConstants.INIT_String;

	/**
	 * Set value of property colorType
	 *
	 * @param _value - new field value
	 */
	public void setColorType(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "colorType"));
		String _valueIntern = _value.intern();
		if (!(_valueIntern == colorType_bW
				|| _valueIntern == colorType_gREYSCALE
				|| _valueIntern == colorType_rGB
				|| _valueIntern == colorType_cMYK
				|| _valueIntern == colorType_oTHER || _valueIntern == colorType_uNKNOWN))
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_VIOLATES_ENUMERATION,
					String.valueOf(_value)));

		colorType = _value;
	}

	/**
	 * Get value of property colorType
	 *
	 * @return - value of field colorType
	 */
	public String getColorType() {
		return colorType;
	}

	/* *** Property xmpModifiedAt *** */

	private Date xmpModifiedAt;

	/**
	 * Set value of property xmpModifiedAt
	 *
	 * @param _value - new field value
	 */
	public void setXmpModifiedAt(Date _value) {
		xmpModifiedAt = _value;
	}

	/**
	 * Get value of property xmpModifiedAt
	 *
	 * @return - value of field xmpModifiedAt
	 */
	public Date getXmpModifiedAt() {
		return xmpModifiedAt;
	}

	/* *** Property voiceFileURI *** */

	private String voiceFileURI;

	/**
	 * Set value of property voiceFileURI
	 *
	 * @param _value - new field value
	 */
	public void setVoiceFileURI(String _value) {
		voiceFileURI = _value;
	}

	/**
	 * Get value of property voiceFileURI
	 *
	 * @return - value of field voiceFileURI
	 */
	public String getVoiceFileURI() {
		return voiceFileURI;
	}

	/* *** Property voiceVolume *** */

	private String voiceVolume;

	/**
	 * Set value of property voiceVolume
	 *
	 * @param _value - new field value
	 */
	public void setVoiceVolume(String _value) {
		voiceVolume = _value;
	}

	/**
	 * Get value of property voiceVolume
	 *
	 * @return - value of field voiceVolume
	 */
	public String getVoiceVolume() {
		return voiceVolume;
	}

	/* *** Property lastModification *** */

	private Date lastModification;

	/**
	 * Set value of property lastModification
	 *
	 * @param _value - new field value
	 */
	public void setLastModification(Date _value) {
		lastModification = _value;
	}

	/**
	 * Get value of property lastModification
	 *
	 * @return - value of field lastModification
	 */
	public Date getLastModification() {
		return lastModification;
	}

	/* *** Property rotation *** */

	private int rotation;

	/**
	 * Set value of property rotation
	 *
	 * @param _value - new field value
	 */
	public void setRotation(int _value) {
		rotation = _value;
	}

	/**
	 * Get value of property rotation
	 *
	 * @return - value of field rotation
	 */
	public int getRotation() {
		return rotation;
	}

	/* *** Property importDate *** */

	private Date importDate;

	/**
	 * Set value of property importDate
	 *
	 * @param _value - new field value
	 */
	public void setImportDate(Date _value) {
		importDate = _value;
	}

	/**
	 * Get value of property importDate
	 *
	 * @return - value of field importDate
	 */
	public Date getImportDate() {
		return importDate;
	}

	/* *** Property importedBy *** */

	private String importedBy;

	/**
	 * Set value of property importedBy
	 *
	 * @param _value - new field value
	 */
	public void setImportedBy(String _value) {
		importedBy = _value;
	}

	/**
	 * Get value of property importedBy
	 *
	 * @return - value of field importedBy
	 */
	public String getImportedBy() {
		return importedBy;
	}

	/* *** Property status *** */

	private int status;

	/**
	 * Set value of property status
	 *
	 * @param _value - new field value
	 */
	public void setStatus(int _value) {
		status = _value;
	}

	/**
	 * Get value of property status
	 *
	 * @return - value of field status
	 */
	public int getStatus() {
		return status;
	}

	/* *** Property rating *** */

	private int rating;

	/**
	 * Set value of property rating
	 *
	 * @param _value - new field value
	 */
	public void setRating(int _value) {
		rating = _value;
	}

	/**
	 * Get value of property rating
	 *
	 * @return - value of field rating
	 */
	public int getRating() {
		return rating;
	}

	/* *** Property ratedBy *** */

	private String ratedBy;

	/**
	 * Set value of property ratedBy
	 *
	 * @param _value - new field value
	 */
	public void setRatedBy(String _value) {
		ratedBy = _value;
	}

	/**
	 * Get value of property ratedBy
	 *
	 * @return - value of field ratedBy
	 */
	public String getRatedBy() {
		return ratedBy;
	}

	/* *** Property colorCode *** */

	private int colorCode;

	/**
	 * Set value of property colorCode
	 *
	 * @param _value - new field value
	 */
	public void setColorCode(int _value) {
		colorCode = _value;
	}

	/**
	 * Get value of property colorCode
	 *
	 * @return - value of field colorCode
	 */
	public int getColorCode() {
		return colorCode;
	}

	/* *** Property userfield1 *** */

	private String userfield1;

	/**
	 * Set value of property userfield1
	 *
	 * @param _value - new field value
	 */
	public void setUserfield1(String _value) {
		userfield1 = _value;
	}

	/**
	 * Get value of property userfield1
	 *
	 * @return - value of field userfield1
	 */
	public String getUserfield1() {
		return userfield1;
	}

	/* *** Property userfield2 *** */

	private String userfield2;

	/**
	 * Set value of property userfield2
	 *
	 * @param _value - new field value
	 */
	public void setUserfield2(String _value) {
		userfield2 = _value;
	}

	/**
	 * Get value of property userfield2
	 *
	 * @return - value of field userfield2
	 */
	public String getUserfield2() {
		return userfield2;
	}

	/* *** Property jpegThumbnail *** */

	private byte[] jpegThumbnail = new byte[0];

	/**
	 * Set value of property jpegThumbnail
	 *
	 * @param _value - new element value
	 */
	public void setJpegThumbnail(byte[] _value) {
		jpegThumbnail = _value;
	}

	/**
	 * Set single element of array jpegThumbnail
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setJpegThumbnail(byte _element, int _i) {
		jpegThumbnail[_i] = _element;
	}

	/**
	 * Get value of property jpegThumbnail
	 *
	 * @return - value of field jpegThumbnail
	 */
	public byte[] getJpegThumbnail() {
		return jpegThumbnail;
	}

	/**
	 * Get single element of array jpegThumbnail
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array jpegThumbnail
	 */
	public byte getJpegThumbnail(int _i) {
		return jpegThumbnail[_i];
	}

	/* *** Property bitsPerSample *** */

	private int[] bitsPerSample = new int[4];

	/**
	 * Set value of property bitsPerSample
	 *
	 * @param _value - new element value
	 */
	public void setBitsPerSample(int[] _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "bitsPerSample"));
		if (_value.length > 4)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_MAXOCCURS, "4", "bitsPerSample"));
		if (_value.length < 1)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_MINOCCURS, "1", "bitsPerSample"));
		bitsPerSample = _value;
	}

	/**
	 * Set single element of array bitsPerSample
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setBitsPerSample(int _element, int _i) {
		bitsPerSample[_i] = _element;
	}

	/**
	 * Get value of property bitsPerSample
	 *
	 * @return - value of field bitsPerSample
	 */
	public int[] getBitsPerSample() {
		return bitsPerSample;
	}

	/**
	 * Get single element of array bitsPerSample
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array bitsPerSample
	 */
	public int getBitsPerSample(int _i) {
		return bitsPerSample[_i];
	}

	/* *** Property imageLength *** */

	private int imageLength;

	/**
	 * Set value of property imageLength
	 *
	 * @param _value - new field value
	 */
	public void setImageLength(int _value) {
		imageLength = _value;
	}

	/**
	 * Get value of property imageLength
	 *
	 * @return - value of field imageLength
	 */
	public int getImageLength() {
		return imageLength;
	}

	/* *** Property imageWidth *** */

	private int imageWidth;

	/**
	 * Set value of property imageWidth
	 *
	 * @param _value - new field value
	 */
	public void setImageWidth(int _value) {
		imageWidth = _value;
	}

	/**
	 * Get value of property imageWidth
	 *
	 * @return - value of field imageWidth
	 */
	public int getImageWidth() {
		return imageWidth;
	}

	/* *** Property compression *** */

	private int compression;

	/**
	 * Set value of property compression
	 *
	 * @param _value - new field value
	 */
	public void setCompression(int _value) {
		compression = _value;
	}

	/**
	 * Get value of property compression
	 *
	 * @return - value of field compression
	 */
	public int getCompression() {
		return compression;
	}

	/* *** Property photometricInterpretation *** */

	private int photometricInterpretation;

	/**
	 * Set value of property photometricInterpretation
	 *
	 * @param _value - new field value
	 */
	public void setPhotometricInterpretation(int _value) {
		photometricInterpretation = _value;
	}

	/**
	 * Get value of property photometricInterpretation
	 *
	 * @return - value of field photometricInterpretation
	 */
	public int getPhotometricInterpretation() {
		return photometricInterpretation;
	}

	/* *** Property orientation *** */

	private int orientation;

	/**
	 * Set value of property orientation
	 *
	 * @param _value - new field value
	 */
	public void setOrientation(int _value) {
		orientation = _value;
	}

	/**
	 * Get value of property orientation
	 *
	 * @return - value of field orientation
	 */
	public int getOrientation() {
		return orientation;
	}

	/* *** Property samplesPerPixel *** */

	private int samplesPerPixel;

	/**
	 * Set value of property samplesPerPixel
	 *
	 * @param _value - new field value
	 */
	public void setSamplesPerPixel(int _value) {
		samplesPerPixel = _value;
	}

	/**
	 * Get value of property samplesPerPixel
	 *
	 * @return - value of field samplesPerPixel
	 */
	public int getSamplesPerPixel() {
		return samplesPerPixel;
	}

	/* *** Property xResolution(unit=ppi) *** */

	public static final String xResolution__unit = "ppi";

	private double xResolution;

	/**
	 * Set value of property xResolution
	 *
	 * @param _value - new field value(unit=ppi)
	 */
	public void setXResolution(double _value) {
		xResolution = _value;
	}

	/**
	 * Get value of property xResolution
	 *
	 * @return - value of field xResolution(unit=ppi)
	 */
	public double getXResolution() {
		return xResolution;
	}

	/* *** Property yResolution(unitr=ppi) *** */

	public static final String yResolution__unitr = "ppi";

	private double yResolution;

	/**
	 * Set value of property yResolution
	 *
	 * @param _value - new field value(unitr=ppi)
	 */
	public void setYResolution(double _value) {
		yResolution = _value;
	}

	/**
	 * Get value of property yResolution
	 *
	 * @return - value of field yResolution(unitr=ppi)
	 */
	public double getYResolution() {
		return yResolution;
	}

	/* *** Property dateTime *** */

	private Date dateTime;

	/**
	 * Set value of property dateTime
	 *
	 * @param _value - new field value
	 */
	public void setDateTime(Date _value) {
		dateTime = _value;
	}

	/**
	 * Get value of property dateTime
	 *
	 * @return - value of field dateTime
	 */
	public Date getDateTime() {
		return dateTime;
	}

	/* *** Property imageDescription *** */

	private String imageDescription;

	/**
	 * Set value of property imageDescription
	 *
	 * @param _value - new field value
	 */
	public void setImageDescription(String _value) {
		imageDescription = _value;
	}

	/**
	 * Get value of property imageDescription
	 *
	 * @return - value of field imageDescription
	 */
	public String getImageDescription() {
		return imageDescription;
	}

	/* *** Property make *** */

	private String make;

	/**
	 * Set value of property make
	 *
	 * @param _value - new field value
	 */
	public void setMake(String _value) {
		make = _value;
	}

	/**
	 * Get value of property make
	 *
	 * @return - value of field make
	 */
	public String getMake() {
		return make;
	}

	/* *** Property model *** */

	private String model;

	/**
	 * Set value of property model
	 *
	 * @param _value - new field value
	 */
	public void setModel(String _value) {
		model = _value;
	}

	/**
	 * Get value of property model
	 *
	 * @return - value of field model
	 */
	public String getModel() {
		return model;
	}

	/* *** Property software *** */

	private String software;

	/**
	 * Set value of property software
	 *
	 * @param _value - new field value
	 */
	public void setSoftware(String _value) {
		software = _value;
	}

	/**
	 * Get value of property software
	 *
	 * @return - value of field software
	 */
	public String getSoftware() {
		return software;
	}

	/* *** Property originalFileName *** */

	private String originalFileName;

	/**
	 * Set value of property originalFileName
	 *
	 * @param _value - new field value
	 */
	public void setOriginalFileName(String _value) {
		originalFileName = _value;
	}

	/**
	 * Get value of property originalFileName
	 *
	 * @return - value of field originalFileName
	 */
	public String getOriginalFileName() {
		return originalFileName;
	}

	/* *** Property originalImageId *** */

	private String originalImageId;

	/**
	 * Set value of property originalImageId
	 *
	 * @param _value - new field value
	 */
	public void setOriginalImageId(String _value) {
		originalImageId = _value;
	}

	/**
	 * Get value of property originalImageId
	 *
	 * @return - value of field originalImageId
	 */
	public String getOriginalImageId() {
		return originalImageId;
	}

	/* *** Property artist *** */

	private String[] artist = new String[0];

	/**
	 * Set value of property artist
	 *
	 * @param _value - new element value
	 */
	public void setArtist(String[] _value) {
		artist = _value;
	}

	/**
	 * Set single element of array artist
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setArtist(String _element, int _i) {
		artist[_i] = _element;
	}

	/**
	 * Get value of property artist
	 *
	 * @return - value of field artist
	 */
	public String[] getArtist() {
		return artist;
	}

	/**
	 * Get single element of array artist
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array artist
	 */
	public String getArtist(int _i) {
		return artist[_i];
	}

	/* *** Property copyright *** */

	private String copyright;

	/**
	 * Set value of property copyright
	 *
	 * @param _value - new field value
	 */
	public void setCopyright(String _value) {
		copyright = _value;
	}

	/**
	 * Get value of property copyright
	 *
	 * @return - value of field copyright
	 */
	public String getCopyright() {
		return copyright;
	}

	/* *** Property colorSpace *** */

	private int colorSpace;

	/**
	 * Set value of property colorSpace
	 *
	 * @param _value - new field value
	 */
	public void setColorSpace(int _value) {
		colorSpace = _value;
	}

	/**
	 * Get value of property colorSpace
	 *
	 * @return - value of field colorSpace
	 */
	public int getColorSpace() {
		return colorSpace;
	}

	/* *** Property profileDescription *** */

	private String profileDescription;

	/**
	 * Set value of property profileDescription
	 *
	 * @param _value - new field value
	 */
	public void setProfileDescription(String _value) {
		profileDescription = _value;
	}

	/**
	 * Get value of property profileDescription
	 *
	 * @return - value of field profileDescription
	 */
	public String getProfileDescription() {
		return profileDescription;
	}

	/* *** Property dateTimeOriginal *** */

	private Date dateTimeOriginal;

	/**
	 * Set value of property dateTimeOriginal
	 *
	 * @param _value - new field value
	 */
	public void setDateTimeOriginal(Date _value) {
		dateTimeOriginal = _value;
	}

	/**
	 * Get value of property dateTimeOriginal
	 *
	 * @return - value of field dateTimeOriginal
	 */
	public Date getDateTimeOriginal() {
		return dateTimeOriginal;
	}

	/* *** Property exposureTime *** */

	private double exposureTime;

	/**
	 * Set value of property exposureTime
	 *
	 * @param _value - new field value
	 */
	public void setExposureTime(double _value) {
		exposureTime = _value;
	}

	/**
	 * Get value of property exposureTime
	 *
	 * @return - value of field exposureTime
	 */
	public double getExposureTime() {
		return exposureTime;
	}

	/* *** Property fNumber *** */

	private double fNumber;

	/**
	 * Set value of property fNumber
	 *
	 * @param _value - new field value
	 */
	public void setFNumber(double _value) {
		fNumber = _value;
	}

	/**
	 * Get value of property fNumber
	 *
	 * @return - value of field fNumber
	 */
	public double getFNumber() {
		return fNumber;
	}

	/* *** Property exposureProgram *** */

	private int exposureProgram;

	/**
	 * Set value of property exposureProgram
	 *
	 * @param _value - new field value
	 */
	public void setExposureProgram(int _value) {
		exposureProgram = _value;
	}

	/**
	 * Get value of property exposureProgram
	 *
	 * @return - value of field exposureProgram
	 */
	public int getExposureProgram() {
		return exposureProgram;
	}

	/* *** Property spectralSensitivity *** */

	private String spectralSensitivity;

	/**
	 * Set value of property spectralSensitivity
	 *
	 * @param _value - new field value
	 */
	public void setSpectralSensitivity(String _value) {
		spectralSensitivity = _value;
	}

	/**
	 * Get value of property spectralSensitivity
	 *
	 * @return - value of field spectralSensitivity
	 */
	public String getSpectralSensitivity() {
		return spectralSensitivity;
	}

	/* *** Property isoSpeedRatings *** */

	private int[] isoSpeedRatings = new int[0];

	/**
	 * Set value of property isoSpeedRatings
	 *
	 * @param _value - new element value
	 */
	public void setIsoSpeedRatings(int[] _value) {
		isoSpeedRatings = _value;
	}

	/**
	 * Set single element of array isoSpeedRatings
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setIsoSpeedRatings(int _element, int _i) {
		isoSpeedRatings[_i] = _element;
	}

	/**
	 * Get value of property isoSpeedRatings
	 *
	 * @return - value of field isoSpeedRatings
	 */
	public int[] getIsoSpeedRatings() {
		return isoSpeedRatings;
	}

	/**
	 * Get single element of array isoSpeedRatings
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array isoSpeedRatings
	 */
	public int getIsoSpeedRatings(int _i) {
		return isoSpeedRatings[_i];
	}

	/* *** Property scalarSpeedRatings *** */

	private int scalarSpeedRatings;

	/**
	 * Set value of property scalarSpeedRatings
	 *
	 * @param _value - new field value
	 */
	public void setScalarSpeedRatings(int _value) {
		scalarSpeedRatings = _value;
	}

	/**
	 * Get value of property scalarSpeedRatings
	 *
	 * @return - value of field scalarSpeedRatings
	 */
	public int getScalarSpeedRatings() {
		return scalarSpeedRatings;
	}

	/* *** Property shutterSpeed *** */

	private double shutterSpeed;

	/**
	 * Set value of property shutterSpeed
	 *
	 * @param _value - new field value
	 */
	public void setShutterSpeed(double _value) {
		shutterSpeed = _value;
	}

	/**
	 * Get value of property shutterSpeed
	 *
	 * @return - value of field shutterSpeed
	 */
	public double getShutterSpeed() {
		return shutterSpeed;
	}

	/* *** Property aperture *** */

	private double aperture;

	/**
	 * Set value of property aperture
	 *
	 * @param _value - new field value
	 */
	public void setAperture(double _value) {
		aperture = _value;
	}

	/**
	 * Get value of property aperture
	 *
	 * @return - value of field aperture
	 */
	public double getAperture() {
		return aperture;
	}

	/* *** Property brightnessValue *** */

	private double brightnessValue;

	/**
	 * Set value of property brightnessValue
	 *
	 * @param _value - new field value
	 */
	public void setBrightnessValue(double _value) {
		brightnessValue = _value;
	}

	/**
	 * Get value of property brightnessValue
	 *
	 * @return - value of field brightnessValue
	 */
	public double getBrightnessValue() {
		return brightnessValue;
	}

	/* *** Property exposureBias *** */

	private double exposureBias;

	/**
	 * Set value of property exposureBias
	 *
	 * @param _value - new field value
	 */
	public void setExposureBias(double _value) {
		exposureBias = _value;
	}

	/**
	 * Get value of property exposureBias
	 *
	 * @return - value of field exposureBias
	 */
	public double getExposureBias() {
		return exposureBias;
	}

	/* *** Property maxLensAperture *** */

	private double maxLensAperture;

	/**
	 * Set value of property maxLensAperture
	 *
	 * @param _value - new field value
	 */
	public void setMaxLensAperture(double _value) {
		maxLensAperture = _value;
	}

	/**
	 * Get value of property maxLensAperture
	 *
	 * @return - value of field maxLensAperture
	 */
	public double getMaxLensAperture() {
		return maxLensAperture;
	}

	/* *** Property subjectDistance *** */

	private double subjectDistance;

	/**
	 * Set value of property subjectDistance
	 *
	 * @param _value - new field value
	 */
	public void setSubjectDistance(double _value) {
		subjectDistance = _value;
	}

	/**
	 * Get value of property subjectDistance
	 *
	 * @return - value of field subjectDistance
	 */
	public double getSubjectDistance() {
		return subjectDistance;
	}

	/* *** Property meteringMode *** */

	private int meteringMode;

	/**
	 * Set value of property meteringMode
	 *
	 * @param _value - new field value
	 */
	public void setMeteringMode(int _value) {
		meteringMode = _value;
	}

	/**
	 * Get value of property meteringMode
	 *
	 * @return - value of field meteringMode
	 */
	public int getMeteringMode() {
		return meteringMode;
	}

	/* *** Property lightSource *** */

	private int lightSource;

	/**
	 * Set value of property lightSource
	 *
	 * @param _value - new field value
	 */
	public void setLightSource(int _value) {
		lightSource = _value;
	}

	/**
	 * Get value of property lightSource
	 *
	 * @return - value of field lightSource
	 */
	public int getLightSource() {
		return lightSource;
	}

	/* *** Property flashFired *** */

	private boolean flashFired;

	/**
	 * Set value of property flashFired
	 *
	 * @param _value - new field value
	 */
	public void setFlashFired(boolean _value) {
		flashFired = _value;
	}

	/**
	 * Get value of property flashFired
	 *
	 * @return - value of field flashFired
	 */
	public boolean getFlashFired() {
		return flashFired;
	}

	/* *** Property returnLightDetected *** */

	private int returnLightDetected;

	/**
	 * Set value of property returnLightDetected
	 *
	 * @param _value - new field value
	 */
	public void setReturnLightDetected(int _value) {
		returnLightDetected = _value;
	}

	/**
	 * Get value of property returnLightDetected
	 *
	 * @return - value of field returnLightDetected
	 */
	public int getReturnLightDetected() {
		return returnLightDetected;
	}

	/* *** Property flashAuto *** */

	private int flashAuto;

	/**
	 * Set value of property flashAuto
	 *
	 * @param _value - new field value
	 */
	public void setFlashAuto(int _value) {
		flashAuto = _value;
	}

	/**
	 * Get value of property flashAuto
	 *
	 * @return - value of field flashAuto
	 */
	public int getFlashAuto() {
		return flashAuto;
	}

	/* *** Property flashFunction *** */

	private boolean flashFunction;

	/**
	 * Set value of property flashFunction
	 *
	 * @param _value - new field value
	 */
	public void setFlashFunction(boolean _value) {
		flashFunction = _value;
	}

	/**
	 * Get value of property flashFunction
	 *
	 * @return - value of field flashFunction
	 */
	public boolean getFlashFunction() {
		return flashFunction;
	}

	/* *** Property redEyeReduction *** */

	private boolean redEyeReduction;

	/**
	 * Set value of property redEyeReduction
	 *
	 * @param _value - new field value
	 */
	public void setRedEyeReduction(boolean _value) {
		redEyeReduction = _value;
	}

	/**
	 * Get value of property redEyeReduction
	 *
	 * @return - value of field redEyeReduction
	 */
	public boolean getRedEyeReduction() {
		return redEyeReduction;
	}

	/* *** Property flashExposureComp *** */

	private double flashExposureComp;

	/**
	 * Set value of property flashExposureComp
	 *
	 * @param _value - new field value
	 */
	public void setFlashExposureComp(double _value) {
		flashExposureComp = _value;
	}

	/**
	 * Get value of property flashExposureComp
	 *
	 * @return - value of field flashExposureComp
	 */
	public double getFlashExposureComp() {
		return flashExposureComp;
	}

	/* *** Property focalLength *** */

	private double focalLength;

	/**
	 * Set value of property focalLength
	 *
	 * @param _value - new field value
	 */
	public void setFocalLength(double _value) {
		focalLength = _value;
	}

	/**
	 * Get value of property focalLength
	 *
	 * @return - value of field focalLength
	 */
	public double getFocalLength() {
		return focalLength;
	}

	/* *** Property subjectArea *** */

	private int[] subjectArea = new int[4];

	/**
	 * Set value of property subjectArea
	 *
	 * @param _value - new element value
	 */
	public void setSubjectArea(int[] _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "subjectArea"));
		if (_value.length > 4)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_MAXOCCURS, "4", "subjectArea"));
		if (_value.length < 2)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_MINOCCURS, "2", "subjectArea"));
		subjectArea = _value;
	}

	/**
	 * Set single element of array subjectArea
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSubjectArea(int _element, int _i) {
		subjectArea[_i] = _element;
	}

	/**
	 * Get value of property subjectArea
	 *
	 * @return - value of field subjectArea
	 */
	public int[] getSubjectArea() {
		return subjectArea;
	}

	/**
	 * Get single element of array subjectArea
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array subjectArea
	 */
	public int getSubjectArea(int _i) {
		return subjectArea[_i];
	}

	/* *** Property flashEnergy *** */

	private double flashEnergy;

	/**
	 * Set value of property flashEnergy
	 *
	 * @param _value - new field value
	 */
	public void setFlashEnergy(double _value) {
		flashEnergy = _value;
	}

	/**
	 * Get value of property flashEnergy
	 *
	 * @return - value of field flashEnergy
	 */
	public double getFlashEnergy() {
		return flashEnergy;
	}

	/* *** Property focalPlaneXResolution(unit=ppi) *** */

	public static final String focalPlaneXResolution__unit = "ppi";

	private double focalPlaneXResolution;

	/**
	 * Set value of property focalPlaneXResolution
	 *
	 * @param _value - new field value(unit=ppi)
	 */
	public void setFocalPlaneXResolution(double _value) {
		focalPlaneXResolution = _value;
	}

	/**
	 * Get value of property focalPlaneXResolution
	 *
	 * @return - value of field focalPlaneXResolution(unit=ppi)
	 */
	public double getFocalPlaneXResolution() {
		return focalPlaneXResolution;
	}

	/* *** Property focalPlaneYResolution(unit=ppi) *** */

	public static final String focalPlaneYResolution__unit = "ppi";

	private double focalPlaneYResolution;

	/**
	 * Set value of property focalPlaneYResolution
	 *
	 * @param _value - new field value(unit=ppi)
	 */
	public void setFocalPlaneYResolution(double _value) {
		focalPlaneYResolution = _value;
	}

	/**
	 * Get value of property focalPlaneYResolution
	 *
	 * @return - value of field focalPlaneYResolution(unit=ppi)
	 */
	public double getFocalPlaneYResolution() {
		return focalPlaneYResolution;
	}

	/* *** Property subjectLocation *** */

	private int[] subjectLocation = new int[2];

	/**
	 * Set value of property subjectLocation
	 *
	 * @param _value - new element value
	 */
	public void setSubjectLocation(int[] _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "subjectLocation"));
		if (_value.length > 2)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_MAXOCCURS, "2", "subjectLocation"));
		if (_value.length < 2)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_MINOCCURS, "2", "subjectLocation"));
		subjectLocation = _value;
	}

	/**
	 * Set single element of array subjectLocation
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSubjectLocation(int _element, int _i) {
		subjectLocation[_i] = _element;
	}

	/**
	 * Get value of property subjectLocation
	 *
	 * @return - value of field subjectLocation
	 */
	public int[] getSubjectLocation() {
		return subjectLocation;
	}

	/**
	 * Get single element of array subjectLocation
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array subjectLocation
	 */
	public int getSubjectLocation(int _i) {
		return subjectLocation[_i];
	}

	/* *** Property exposureIndex *** */

	private double exposureIndex;

	/**
	 * Set value of property exposureIndex
	 *
	 * @param _value - new field value
	 */
	public void setExposureIndex(double _value) {
		exposureIndex = _value;
	}

	/**
	 * Get value of property exposureIndex
	 *
	 * @return - value of field exposureIndex
	 */
	public double getExposureIndex() {
		return exposureIndex;
	}

	/* *** Property sensingMethod *** */

	private int sensingMethod;

	/**
	 * Set value of property sensingMethod
	 *
	 * @param _value - new field value
	 */
	public void setSensingMethod(int _value) {
		sensingMethod = _value;
	}

	/**
	 * Get value of property sensingMethod
	 *
	 * @return - value of field sensingMethod
	 */
	public int getSensingMethod() {
		return sensingMethod;
	}

	/* *** Property fileSource *** */

	private int fileSource;

	/**
	 * Set value of property fileSource
	 *
	 * @param _value - new field value
	 */
	public void setFileSource(int _value) {
		fileSource = _value;
	}

	/**
	 * Get value of property fileSource
	 *
	 * @return - value of field fileSource
	 */
	public int getFileSource() {
		return fileSource;
	}

	/* *** Property exposureMode *** */

	private int exposureMode;

	/**
	 * Set value of property exposureMode
	 *
	 * @param _value - new field value
	 */
	public void setExposureMode(int _value) {
		exposureMode = _value;
	}

	/**
	 * Get value of property exposureMode
	 *
	 * @return - value of field exposureMode
	 */
	public int getExposureMode() {
		return exposureMode;
	}

	/* *** Property whiteBalance *** */

	private String whiteBalance;

	/**
	 * Set value of property whiteBalance
	 *
	 * @param _value - new field value
	 */
	public void setWhiteBalance(String _value) {
		whiteBalance = _value;
	}

	/**
	 * Get value of property whiteBalance
	 *
	 * @return - value of field whiteBalance
	 */
	public String getWhiteBalance() {
		return whiteBalance;
	}

	/* *** Property digitalZoomRatio *** */

	private double digitalZoomRatio;

	/**
	 * Set value of property digitalZoomRatio
	 *
	 * @param _value - new field value
	 */
	public void setDigitalZoomRatio(double _value) {
		digitalZoomRatio = _value;
	}

	/**
	 * Get value of property digitalZoomRatio
	 *
	 * @return - value of field digitalZoomRatio
	 */
	public double getDigitalZoomRatio() {
		return digitalZoomRatio;
	}

	/* *** Property focalLengthIn35MmFilm *** */

	private int focalLengthIn35MmFilm;

	/**
	 * Set value of property focalLengthIn35MmFilm
	 *
	 * @param _value - new field value
	 */
	public void setFocalLengthIn35MmFilm(int _value) {
		focalLengthIn35MmFilm = _value;
	}

	/**
	 * Get value of property focalLengthIn35MmFilm
	 *
	 * @return - value of field focalLengthIn35MmFilm
	 */
	public int getFocalLengthIn35MmFilm() {
		return focalLengthIn35MmFilm;
	}

	/* *** Property focalLengthFactor *** */

	private double focalLengthFactor;

	/**
	 * Set value of property focalLengthFactor
	 *
	 * @param _value - new field value
	 */
	public void setFocalLengthFactor(double _value) {
		focalLengthFactor = _value;
	}

	/**
	 * Get value of property focalLengthFactor
	 *
	 * @return - value of field focalLengthFactor
	 */
	public double getFocalLengthFactor() {
		return focalLengthFactor;
	}

	/* *** Property circleOfConfusion *** */

	private double circleOfConfusion;

	/**
	 * Set value of property circleOfConfusion
	 *
	 * @param _value - new field value
	 */
	public void setCircleOfConfusion(double _value) {
		circleOfConfusion = _value;
	}

	/**
	 * Get value of property circleOfConfusion
	 *
	 * @return - value of field circleOfConfusion
	 */
	public double getCircleOfConfusion() {
		return circleOfConfusion;
	}

	/* *** Property hyperfocalDistance *** */

	private double hyperfocalDistance;

	/**
	 * Set value of property hyperfocalDistance
	 *
	 * @param _value - new field value
	 */
	public void setHyperfocalDistance(double _value) {
		hyperfocalDistance = _value;
	}

	/**
	 * Get value of property hyperfocalDistance
	 *
	 * @return - value of field hyperfocalDistance
	 */
	public double getHyperfocalDistance() {
		return hyperfocalDistance;
	}

	/* *** Property dof *** */

	private double[] dof = new double[2];

	/**
	 * Set value of property dof
	 *
	 * @param _value - new element value
	 */
	public void setDof(double[] _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "dof"));
		if (_value.length > 2)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_MAXOCCURS, "2", "dof"));
		if (_value.length < 2)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_MINOCCURS, "2", "dof"));
		dof = _value;
	}

	/**
	 * Set single element of array dof
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setDof(double _element, int _i) {
		dof[_i] = _element;
	}

	/**
	 * Get value of property dof
	 *
	 * @return - value of field dof
	 */
	public double[] getDof() {
		return dof;
	}

	/**
	 * Get single element of array dof
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array dof
	 */
	public double getDof(int _i) {
		return dof[_i];
	}

	/* *** Property lv *** */

	private double lv;

	/**
	 * Set value of property lv
	 *
	 * @param _value - new field value
	 */
	public void setLv(double _value) {
		lv = _value;
	}

	/**
	 * Get value of property lv
	 *
	 * @return - value of field lv
	 */
	public double getLv() {
		return lv;
	}

	/* *** Property fov *** */

	private double[] fov = new double[2];

	/**
	 * Set value of property fov
	 *
	 * @param _value - new element value
	 */
	public void setFov(double[] _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "fov"));
		if (_value.length > 2)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_MAXOCCURS, "2", "fov"));
		if (_value.length < 1)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_MINOCCURS, "1", "fov"));
		fov = _value;
	}

	/**
	 * Set single element of array fov
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setFov(double _element, int _i) {
		fov[_i] = _element;
	}

	/**
	 * Get value of property fov
	 *
	 * @return - value of field fov
	 */
	public double[] getFov() {
		return fov;
	}

	/**
	 * Get single element of array fov
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array fov
	 */
	public double getFov(int _i) {
		return fov[_i];
	}

	/* *** Property sceneCaptureType *** */

	private int sceneCaptureType;

	/**
	 * Set value of property sceneCaptureType
	 *
	 * @param _value - new field value
	 */
	public void setSceneCaptureType(int _value) {
		sceneCaptureType = _value;
	}

	/**
	 * Get value of property sceneCaptureType
	 *
	 * @return - value of field sceneCaptureType
	 */
	public int getSceneCaptureType() {
		return sceneCaptureType;
	}

	/* *** Property gainControl *** */

	private int gainControl;

	/**
	 * Set value of property gainControl
	 *
	 * @param _value - new field value
	 */
	public void setGainControl(int _value) {
		gainControl = _value;
	}

	/**
	 * Get value of property gainControl
	 *
	 * @return - value of field gainControl
	 */
	public int getGainControl() {
		return gainControl;
	}

	/* *** Property contrast *** */

	private int contrast;

	/**
	 * Set value of property contrast
	 *
	 * @param _value - new field value
	 */
	public void setContrast(int _value) {
		contrast = _value;
	}

	/**
	 * Get value of property contrast
	 *
	 * @return - value of field contrast
	 */
	public int getContrast() {
		return contrast;
	}

	/* *** Property saturation *** */

	private int saturation;

	/**
	 * Set value of property saturation
	 *
	 * @param _value - new field value
	 */
	public void setSaturation(int _value) {
		saturation = _value;
	}

	/**
	 * Get value of property saturation
	 *
	 * @return - value of field saturation
	 */
	public int getSaturation() {
		return saturation;
	}

	/* *** Property sharpness *** */

	private int sharpness;

	/**
	 * Set value of property sharpness
	 *
	 * @param _value - new field value
	 */
	public void setSharpness(int _value) {
		sharpness = _value;
	}

	/**
	 * Get value of property sharpness
	 *
	 * @return - value of field sharpness
	 */
	public int getSharpness() {
		return sharpness;
	}

	/* *** Property vibrance *** */

	private int vibrance;

	/**
	 * Set value of property vibrance
	 *
	 * @param _value - new field value
	 */
	public void setVibrance(int _value) {
		vibrance = _value;
	}

	/**
	 * Get value of property vibrance
	 *
	 * @return - value of field vibrance
	 */
	public int getVibrance() {
		return vibrance;
	}

	/* *** Property subjectDistanceRange *** */

	private int subjectDistanceRange;

	/**
	 * Set value of property subjectDistanceRange
	 *
	 * @param _value - new field value
	 */
	public void setSubjectDistanceRange(int _value) {
		subjectDistanceRange = _value;
	}

	/**
	 * Get value of property subjectDistanceRange
	 *
	 * @return - value of field subjectDistanceRange
	 */
	public int getSubjectDistanceRange() {
		return subjectDistanceRange;
	}

	/* *** Property lens *** */

	private String lens = AomConstants.INIT_String;

	/**
	 * Set value of property lens
	 *
	 * @param _value - new field value
	 */
	public void setLens(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lens"));
		lens = _value;
	}

	/**
	 * Get value of property lens
	 *
	 * @return - value of field lens
	 */
	public String getLens() {
		return lens;
	}

	/* *** Property lensSerial *** */

	private String lensSerial = AomConstants.INIT_String;

	/**
	 * Set value of property lensSerial
	 *
	 * @param _value - new field value
	 */
	public void setLensSerial(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lensSerial"));
		lensSerial = _value;
	}

	/**
	 * Get value of property lensSerial
	 *
	 * @return - value of field lensSerial
	 */
	public String getLensSerial() {
		return lensSerial;
	}

	/* *** Property serial *** */

	private String serial = AomConstants.INIT_String;

	/**
	 * Set value of property serial
	 *
	 * @param _value - new field value
	 */
	public void setSerial(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "serial"));
		serial = _value;
	}

	/**
	 * Get value of property serial
	 *
	 * @return - value of field serial
	 */
	public String getSerial() {
		return serial;
	}

	/* *** Property gPSLatitude *** */

	private double gPSLatitude;

	/**
	 * Set value of property gPSLatitude
	 *
	 * @param _value - new field value
	 */
	public void setGPSLatitude(double _value) {
		gPSLatitude = _value;
	}

	/**
	 * Get value of property gPSLatitude
	 *
	 * @return - value of field gPSLatitude
	 */
	public double getGPSLatitude() {
		return gPSLatitude;
	}

	/* *** Property gPSLongitude *** */

	private double gPSLongitude;

	/**
	 * Set value of property gPSLongitude
	 *
	 * @param _value - new field value
	 */
	public void setGPSLongitude(double _value) {
		gPSLongitude = _value;
	}

	/**
	 * Get value of property gPSLongitude
	 *
	 * @return - value of field gPSLongitude
	 */
	public double getGPSLongitude() {
		return gPSLongitude;
	}

	/* *** Property gPSAltitude *** */

	private double gPSAltitude;

	/**
	 * Set value of property gPSAltitude
	 *
	 * @param _value - new field value
	 */
	public void setGPSAltitude(double _value) {
		gPSAltitude = _value;
	}

	/**
	 * Get value of property gPSAltitude
	 *
	 * @return - value of field gPSAltitude
	 */
	public double getGPSAltitude() {
		return gPSAltitude;
	}

	/* *** Property gPSTime *** */

	private Date gPSTime;

	/**
	 * Set value of property gPSTime
	 *
	 * @param _value - new field value
	 */
	public void setGPSTime(Date _value) {
		gPSTime = _value;
	}

	/**
	 * Get value of property gPSTime
	 *
	 * @return - value of field gPSTime
	 */
	public Date getGPSTime() {
		return gPSTime;
	}

	/* *** Property gPSDOP *** */

	private double gPSDOP;

	/**
	 * Set value of property gPSDOP
	 *
	 * @param _value - new field value
	 */
	public void setGPSDOP(double _value) {
		gPSDOP = _value;
	}

	/**
	 * Get value of property gPSDOP
	 *
	 * @return - value of field gPSDOP
	 */
	public double getGPSDOP() {
		return gPSDOP;
	}

	/* *** Property gPSSpeed(unit=kmh) *** */

	public static final String gPSSpeed__unit = "kmh";

	private double gPSSpeed;

	/**
	 * Set value of property gPSSpeed
	 *
	 * @param _value - new field value(unit=kmh)
	 */
	public void setGPSSpeed(double _value) {
		gPSSpeed = _value;
	}

	/**
	 * Get value of property gPSSpeed
	 *
	 * @return - value of field gPSSpeed(unit=kmh)
	 */
	public double getGPSSpeed() {
		return gPSSpeed;
	}

	/* *** Property gPSTrackRef *** */

	private String gPSTrackRef;

	/**
	 * Set value of property gPSTrackRef
	 *
	 * @param _value - new field value
	 */
	public void setGPSTrackRef(String _value) {
		gPSTrackRef = _value;
	}

	/**
	 * Get value of property gPSTrackRef
	 *
	 * @return - value of field gPSTrackRef
	 */
	public String getGPSTrackRef() {
		return gPSTrackRef;
	}

	/* *** Property gPSTrack *** */

	private double gPSTrack;

	/**
	 * Set value of property gPSTrack
	 *
	 * @param _value - new field value
	 */
	public void setGPSTrack(double _value) {
		gPSTrack = _value;
	}

	/**
	 * Get value of property gPSTrack
	 *
	 * @return - value of field gPSTrack
	 */
	public double getGPSTrack() {
		return gPSTrack;
	}

	/* *** Property gPSImgDirectionRef *** */

	private String gPSImgDirectionRef;

	/**
	 * Set value of property gPSImgDirectionRef
	 *
	 * @param _value - new field value
	 */
	public void setGPSImgDirectionRef(String _value) {
		gPSImgDirectionRef = _value;
	}

	/**
	 * Get value of property gPSImgDirectionRef
	 *
	 * @return - value of field gPSImgDirectionRef
	 */
	public String getGPSImgDirectionRef() {
		return gPSImgDirectionRef;
	}

	/* *** Property gPSImgDirection *** */

	private double gPSImgDirection;

	/**
	 * Set value of property gPSImgDirection
	 *
	 * @param _value - new field value
	 */
	public void setGPSImgDirection(double _value) {
		gPSImgDirection = _value;
	}

	/**
	 * Get value of property gPSImgDirection
	 *
	 * @return - value of field gPSImgDirection
	 */
	public double getGPSImgDirection() {
		return gPSImgDirection;
	}

	/* *** Property gPSMapDatum *** */

	private String gPSMapDatum;

	/**
	 * Set value of property gPSMapDatum
	 *
	 * @param _value - new field value
	 */
	public void setGPSMapDatum(String _value) {
		gPSMapDatum = _value;
	}

	/**
	 * Get value of property gPSMapDatum
	 *
	 * @return - value of field gPSMapDatum
	 */
	public String getGPSMapDatum() {
		return gPSMapDatum;
	}

	/* *** Property gPSDestLatitude *** */

	private double gPSDestLatitude;

	/**
	 * Set value of property gPSDestLatitude
	 *
	 * @param _value - new field value
	 */
	public void setGPSDestLatitude(double _value) {
		gPSDestLatitude = _value;
	}

	/**
	 * Get value of property gPSDestLatitude
	 *
	 * @return - value of field gPSDestLatitude
	 */
	public double getGPSDestLatitude() {
		return gPSDestLatitude;
	}

	/* *** Property gPSDestLongitude *** */

	private double gPSDestLongitude;

	/**
	 * Set value of property gPSDestLongitude
	 *
	 * @param _value - new field value
	 */
	public void setGPSDestLongitude(double _value) {
		gPSDestLongitude = _value;
	}

	/**
	 * Get value of property gPSDestLongitude
	 *
	 * @return - value of field gPSDestLongitude
	 */
	public double getGPSDestLongitude() {
		return gPSDestLongitude;
	}

	/* *** Property gPSDestBearingRef *** */

	private String gPSDestBearingRef;

	/**
	 * Set value of property gPSDestBearingRef
	 *
	 * @param _value - new field value
	 */
	public void setGPSDestBearingRef(String _value) {
		gPSDestBearingRef = _value;
	}

	/**
	 * Get value of property gPSDestBearingRef
	 *
	 * @return - value of field gPSDestBearingRef
	 */
	public String getGPSDestBearingRef() {
		return gPSDestBearingRef;
	}

	/* *** Property gPSDestBearing *** */

	private double gPSDestBearing;

	/**
	 * Set value of property gPSDestBearing
	 *
	 * @param _value - new field value
	 */
	public void setGPSDestBearing(double _value) {
		gPSDestBearing = _value;
	}

	/**
	 * Get value of property gPSDestBearing
	 *
	 * @return - value of field gPSDestBearing
	 */
	public double getGPSDestBearing() {
		return gPSDestBearing;
	}

	/* *** Property gPSDestDistance(unit=kmh) *** */

	public static final String gPSDestDistance__unit = "kmh";

	private double gPSDestDistance;

	/**
	 * Set value of property gPSDestDistance
	 *
	 * @param _value - new field value(unit=kmh)
	 */
	public void setGPSDestDistance(double _value) {
		gPSDestDistance = _value;
	}

	/**
	 * Get value of property gPSDestDistance
	 *
	 * @return - value of field gPSDestDistance(unit=kmh)
	 */
	public double getGPSDestDistance() {
		return gPSDestDistance;
	}

	/* *** Property gPSAreaInformation *** */

	private String gPSAreaInformation;

	/**
	 * Set value of property gPSAreaInformation
	 *
	 * @param _value - new field value
	 */
	public void setGPSAreaInformation(String _value) {
		gPSAreaInformation = _value;
	}

	/**
	 * Get value of property gPSAreaInformation
	 *
	 * @return - value of field gPSAreaInformation
	 */
	public String getGPSAreaInformation() {
		return gPSAreaInformation;
	}

	/* *** Property gPSDateStamp *** */

	private Date gPSDateStamp;

	/**
	 * Set value of property gPSDateStamp
	 *
	 * @param _value - new field value
	 */
	public void setGPSDateStamp(Date _value) {
		gPSDateStamp = _value;
	}

	/**
	 * Get value of property gPSDateStamp
	 *
	 * @return - value of field gPSDateStamp
	 */
	public Date getGPSDateStamp() {
		return gPSDateStamp;
	}

	/* *** Property gPSDifferential *** */

	private int gPSDifferential;

	/**
	 * Set value of property gPSDifferential
	 *
	 * @param _value - new field value
	 */
	public void setGPSDifferential(int _value) {
		gPSDifferential = _value;
	}

	/**
	 * Get value of property gPSDifferential
	 *
	 * @return - value of field gPSDifferential
	 */
	public int getGPSDifferential() {
		return gPSDifferential;
	}

	/* *** Property writerEditor *** */

	private String writerEditor;

	/**
	 * Set value of property writerEditor
	 *
	 * @param _value - new field value
	 */
	public void setWriterEditor(String _value) {
		writerEditor = _value;
	}

	/**
	 * Get value of property writerEditor
	 *
	 * @return - value of field writerEditor
	 */
	public String getWriterEditor() {
		return writerEditor;
	}

	/* *** Property headline *** */

	private String headline;

	/**
	 * Set value of property headline
	 *
	 * @param _value - new field value
	 */
	public void setHeadline(String _value) {
		headline = _value;
	}

	/**
	 * Get value of property headline
	 *
	 * @return - value of field headline
	 */
	public String getHeadline() {
		return headline;
	}

	/* *** Property intellectualGenre *** */

	private String intellectualGenre;

	/**
	 * Set value of property intellectualGenre
	 *
	 * @param _value - new field value
	 */
	public void setIntellectualGenre(String _value) {
		intellectualGenre = _value;
	}

	/**
	 * Get value of property intellectualGenre
	 *
	 * @return - value of field intellectualGenre
	 */
	public String getIntellectualGenre() {
		return intellectualGenre;
	}

	/* *** Property title *** */

	private String title;

	/**
	 * Set value of property title
	 *
	 * @param _value - new field value
	 */
	public void setTitle(String _value) {
		title = _value;
	}

	/**
	 * Get value of property title
	 *
	 * @return - value of field title
	 */
	public String getTitle() {
		return title;
	}

	/* *** Property specialInstructions *** */

	private String specialInstructions;

	/**
	 * Set value of property specialInstructions
	 *
	 * @param _value - new field value
	 */
	public void setSpecialInstructions(String _value) {
		specialInstructions = _value;
	}

	/**
	 * Get value of property specialInstructions
	 *
	 * @return - value of field specialInstructions
	 */
	public String getSpecialInstructions() {
		return specialInstructions;
	}

	/* *** Property jobId *** */

	private String jobId;

	/**
	 * Set value of property jobId
	 *
	 * @param _value - new field value
	 */
	public void setJobId(String _value) {
		jobId = _value;
	}

	/**
	 * Get value of property jobId
	 *
	 * @return - value of field jobId
	 */
	public String getJobId() {
		return jobId;
	}

	/* *** Property keyword *** */

	private String[] keyword = new String[0];

	/**
	 * Set value of property keyword
	 *
	 * @param _value - new element value
	 */
	public void setKeyword(String[] _value) {
		keyword = _value;
	}

	/**
	 * Set single element of array keyword
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setKeyword(String _element, int _i) {
		keyword[_i] = _element;
	}

	/**
	 * Get value of property keyword
	 *
	 * @return - value of field keyword
	 */
	public String[] getKeyword() {
		return keyword;
	}

	/**
	 * Get single element of array keyword
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array keyword
	 */
	public String getKeyword(int _i) {
		return keyword[_i];
	}

	/* *** Property category *** */

	private String category;

	/**
	 * Set value of property category
	 *
	 * @param _value - new field value
	 */
	public void setCategory(String _value) {
		category = _value;
	}

	/**
	 * Get value of property category
	 *
	 * @return - value of field category
	 */
	public String getCategory() {
		return category;
	}

	/* *** Property supplementalCats *** */

	private String[] supplementalCats = new String[0];

	/**
	 * Set value of property supplementalCats
	 *
	 * @param _value - new element value
	 */
	public void setSupplementalCats(String[] _value) {
		supplementalCats = _value;
	}

	/**
	 * Set single element of array supplementalCats
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSupplementalCats(String _element, int _i) {
		supplementalCats[_i] = _element;
	}

	/**
	 * Get value of property supplementalCats
	 *
	 * @return - value of field supplementalCats
	 */
	public String[] getSupplementalCats() {
		return supplementalCats;
	}

	/**
	 * Get single element of array supplementalCats
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array supplementalCats
	 */
	public String getSupplementalCats(int _i) {
		return supplementalCats[_i];
	}

	/* *** Property urgency *** */

	private int urgency;

	/**
	 * Set value of property urgency
	 *
	 * @param _value - new field value
	 */
	public void setUrgency(int _value) {
		urgency = _value;
	}

	/**
	 * Get value of property urgency
	 *
	 * @return - value of field urgency
	 */
	public int getUrgency() {
		return urgency;
	}

	/* *** Property authorsPosition *** */

	private String authorsPosition;

	/**
	 * Set value of property authorsPosition
	 *
	 * @param _value - new field value
	 */
	public void setAuthorsPosition(String _value) {
		authorsPosition = _value;
	}

	/**
	 * Get value of property authorsPosition
	 *
	 * @return - value of field authorsPosition
	 */
	public String getAuthorsPosition() {
		return authorsPosition;
	}

	/* *** Property credit *** */

	private String credit;

	/**
	 * Set value of property credit
	 *
	 * @param _value - new field value
	 */
	public void setCredit(String _value) {
		credit = _value;
	}

	/**
	 * Get value of property credit
	 *
	 * @return - value of field credit
	 */
	public String getCredit() {
		return credit;
	}

	/* *** Property usageTerms *** */

	private String usageTerms;

	/**
	 * Set value of property usageTerms
	 *
	 * @param _value - new field value
	 */
	public void setUsageTerms(String _value) {
		usageTerms = _value;
	}

	/**
	 * Get value of property usageTerms
	 *
	 * @return - value of field usageTerms
	 */
	public String getUsageTerms() {
		return usageTerms;
	}

	/* *** Property owner *** */

	private String[] owner = new String[0];

	/**
	 * Set value of property owner
	 *
	 * @param _value - new element value
	 */
	public void setOwner(String[] _value) {
		owner = _value;
	}

	/**
	 * Set single element of array owner
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setOwner(String _element, int _i) {
		owner[_i] = _element;
	}

	/**
	 * Get value of property owner
	 *
	 * @return - value of field owner
	 */
	public String[] getOwner() {
		return owner;
	}

	/**
	 * Get single element of array owner
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array owner
	 */
	public String getOwner(int _i) {
		return owner[_i];
	}

	/* *** Property source *** */

	private String source;

	/**
	 * Set value of property source
	 *
	 * @param _value - new field value
	 */
	public void setSource(String _value) {
		source = _value;
	}

	/**
	 * Get value of property source
	 *
	 * @return - value of field source
	 */
	public String getSource() {
		return source;
	}

	/* *** Property dateCreated *** */

	private Date dateCreated;

	/**
	 * Set value of property dateCreated
	 *
	 * @param _value - new field value
	 */
	public void setDateCreated(Date _value) {
		dateCreated = _value;
	}

	/**
	 * Get value of property dateCreated
	 *
	 * @return - value of field dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/* *** Property sceneCode *** */

	private String[] sceneCode = new String[0];

	/**
	 * Set value of property sceneCode
	 *
	 * @param _value - new element value
	 */
	public void setSceneCode(String[] _value) {
		sceneCode = _value;
	}

	/**
	 * Set single element of array sceneCode
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSceneCode(String _element, int _i) {
		sceneCode[_i] = _element;
	}

	/**
	 * Get value of property sceneCode
	 *
	 * @return - value of field sceneCode
	 */
	public String[] getSceneCode() {
		return sceneCode;
	}

	/**
	 * Get single element of array sceneCode
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array sceneCode
	 */
	public String getSceneCode(int _i) {
		return sceneCode[_i];
	}

	/* *** Property subjectCode *** */

	private String[] subjectCode = new String[0];

	/**
	 * Set value of property subjectCode
	 *
	 * @param _value - new element value
	 */
	public void setSubjectCode(String[] _value) {
		subjectCode = _value;
	}

	/**
	 * Set single element of array subjectCode
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSubjectCode(String _element, int _i) {
		subjectCode[_i] = _element;
	}

	/**
	 * Get value of property subjectCode
	 *
	 * @return - value of field subjectCode
	 */
	public String[] getSubjectCode() {
		return subjectCode;
	}

	/**
	 * Get single element of array subjectCode
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array subjectCode
	 */
	public String getSubjectCode(int _i) {
		return subjectCode[_i];
	}

	/* *** Property modelInformation *** */

	private String modelInformation;

	/**
	 * Set value of property modelInformation
	 *
	 * @param _value - new field value
	 */
	public void setModelInformation(String _value) {
		modelInformation = _value;
	}

	/**
	 * Get value of property modelInformation
	 *
	 * @return - value of field modelInformation
	 */
	public String getModelInformation() {
		return modelInformation;
	}

	/* *** Property modelAge *** */

	private int modelAge;

	/**
	 * Set value of property modelAge
	 *
	 * @param _value - new field value
	 */
	public void setModelAge(int _value) {
		modelAge = _value;
	}

	/**
	 * Get value of property modelAge
	 *
	 * @return - value of field modelAge
	 */
	public int getModelAge() {
		return modelAge;
	}

	/* *** Property codeOfOrg *** */

	private String[] codeOfOrg = new String[0];

	/**
	 * Set value of property codeOfOrg
	 *
	 * @param _value - new element value
	 */
	public void setCodeOfOrg(String[] _value) {
		codeOfOrg = _value;
	}

	/**
	 * Set single element of array codeOfOrg
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setCodeOfOrg(String _element, int _i) {
		codeOfOrg[_i] = _element;
	}

	/**
	 * Get value of property codeOfOrg
	 *
	 * @return - value of field codeOfOrg
	 */
	public String[] getCodeOfOrg() {
		return codeOfOrg;
	}

	/**
	 * Get single element of array codeOfOrg
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array codeOfOrg
	 */
	public String getCodeOfOrg(int _i) {
		return codeOfOrg[_i];
	}

	/* *** Property nameOfOrg *** */

	private String[] nameOfOrg = new String[0];

	/**
	 * Set value of property nameOfOrg
	 *
	 * @param _value - new element value
	 */
	public void setNameOfOrg(String[] _value) {
		nameOfOrg = _value;
	}

	/**
	 * Set single element of array nameOfOrg
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setNameOfOrg(String _element, int _i) {
		nameOfOrg[_i] = _element;
	}

	/**
	 * Get value of property nameOfOrg
	 *
	 * @return - value of field nameOfOrg
	 */
	public String[] getNameOfOrg() {
		return nameOfOrg;
	}

	/**
	 * Get single element of array nameOfOrg
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array nameOfOrg
	 */
	public String getNameOfOrg(int _i) {
		return nameOfOrg[_i];
	}

	/* *** Property personShown *** */

	private String[] personShown = new String[0];

	/**
	 * Set value of property personShown
	 *
	 * @param _value - new element value
	 */
	public void setPersonShown(String[] _value) {
		personShown = _value;
	}

	/**
	 * Set single element of array personShown
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setPersonShown(String _element, int _i) {
		personShown[_i] = _element;
	}

	/**
	 * Get value of property personShown
	 *
	 * @return - value of field personShown
	 */
	public String[] getPersonShown() {
		return personShown;
	}

	/**
	 * Get single element of array personShown
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array personShown
	 */
	public String getPersonShown(int _i) {
		return personShown[_i];
	}

	/* *** Property event *** */

	private String event;

	/**
	 * Set value of property event
	 *
	 * @param _value - new field value
	 */
	public void setEvent(String _value) {
		event = _value;
	}

	/**
	 * Get value of property event
	 *
	 * @return - value of field event
	 */
	public String getEvent() {
		return event;
	}

	/* *** Property lastEdited *** */

	private Date lastEdited;

	/**
	 * Set value of property lastEdited
	 *
	 * @param _value - new field value
	 */
	public void setLastEdited(Date _value) {
		lastEdited = _value;
	}

	/**
	 * Get value of property lastEdited
	 *
	 * @return - value of field lastEdited
	 */
	public Date getLastEdited() {
		return lastEdited;
	}

	/* *** Property maxAvailHeight *** */

	private int maxAvailHeight;

	/**
	 * Set value of property maxAvailHeight
	 *
	 * @param _value - new field value
	 */
	public void setMaxAvailHeight(int _value) {
		maxAvailHeight = _value;
	}

	/**
	 * Get value of property maxAvailHeight
	 *
	 * @return - value of field maxAvailHeight
	 */
	public int getMaxAvailHeight() {
		return maxAvailHeight;
	}

	/* *** Property maxAvailWidth *** */

	private int maxAvailWidth;

	/**
	 * Set value of property maxAvailWidth
	 *
	 * @param _value - new field value
	 */
	public void setMaxAvailWidth(int _value) {
		maxAvailWidth = _value;
	}

	/**
	 * Get value of property maxAvailWidth
	 *
	 * @return - value of field maxAvailWidth
	 */
	public int getMaxAvailWidth() {
		return maxAvailWidth;
	}

	/* *** Property emulsion *** */

	private String emulsion;

	/**
	 * Set value of property emulsion
	 *
	 * @param _value - new field value
	 */
	public void setEmulsion(String _value) {
		emulsion = _value;
	}

	/**
	 * Get value of property emulsion
	 *
	 * @return - value of field emulsion
	 */
	public String getEmulsion() {
		return emulsion;
	}

	/* *** Property analogType *** */

	private int analogType;

	/**
	 * Set value of property analogType
	 *
	 * @param _value - new field value
	 */
	public void setAnalogType(int _value) {
		analogType = _value;
	}

	/**
	 * Get value of property analogType
	 *
	 * @return - value of field analogType
	 */
	public int getAnalogType() {
		return analogType;
	}

	/* *** Property analogFormat *** */

	private int analogFormat;

	/**
	 * Set value of property analogFormat
	 *
	 * @param _value - new field value
	 */
	public void setAnalogFormat(int _value) {
		analogFormat = _value;
	}

	/**
	 * Get value of property analogFormat
	 *
	 * @return - value of field analogFormat
	 */
	public int getAnalogFormat() {
		return analogFormat;
	}

	/* *** Property analogProcessing *** */

	private String analogProcessing;

	/**
	 * Set value of property analogProcessing
	 *
	 * @param _value - new field value
	 */
	public void setAnalogProcessing(String _value) {
		analogProcessing = _value;
	}

	/**
	 * Get value of property analogProcessing
	 *
	 * @return - value of field analogProcessing
	 */
	public String getAnalogProcessing() {
		return analogProcessing;
	}

	/* *** Property lastEditor *** */

	private String lastEditor;

	/**
	 * Set value of property lastEditor
	 *
	 * @param _value - new field value
	 */
	public void setLastEditor(String _value) {
		lastEditor = _value;
	}

	/**
	 * Get value of property lastEditor
	 *
	 * @return - value of field lastEditor
	 */
	public String getLastEditor() {
		return lastEditor;
	}

	/* *** Property score *** */

	private transient float score;

	/**
	 * Set value of property score
	 *
	 * @param _value - new field value
	 */
	public void setScore(float _value) {
		score = _value;
	}

	/**
	 * Get value of property score
	 *
	 * @return - value of field score
	 */
	public float getScore() {
		return score;
	}

	/* *** Property sales *** */

	private int sales;

	/**
	 * Set value of property sales
	 *
	 * @param _value - new field value
	 */
	public void setSales(int _value) {
		sales = _value;
	}

	/**
	 * Get value of property sales
	 *
	 * @return - value of field sales
	 */
	public int getSales() {
		return sales;
	}

	/* *** Property price *** */

	private double price;

	/**
	 * Set value of property price
	 *
	 * @param _value - new field value
	 */
	public void setPrice(double _value) {
		price = _value;
	}

	/**
	 * Get value of property price
	 *
	 * @return - value of field price
	 */
	public double getPrice() {
		return price;
	}

	/* *** Property earnings *** */

	private double earnings;

	/**
	 * Set value of property earnings
	 *
	 * @param _value - new field value
	 */
	public void setEarnings(double _value) {
		earnings = _value;
	}

	/**
	 * Get value of property earnings
	 *
	 * @return - value of field earnings
	 */
	public double getEarnings() {
		return earnings;
	}

	/* *** Property makerNotes *** */

	private String[] makerNotes = new String[0];

	/**
	 * Set value of property makerNotes
	 *
	 * @param _value - new element value
	 */
	public void setMakerNotes(String[] _value) {
		makerNotes = _value;
	}

	/**
	 * Set single element of array makerNotes
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setMakerNotes(String _element, int _i) {
		makerNotes[_i] = _element;
	}

	/**
	 * Get value of property makerNotes
	 *
	 * @return - value of field makerNotes
	 */
	public String[] getMakerNotes() {
		return makerNotes;
	}

	/**
	 * Get single element of array makerNotes
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array makerNotes
	 */
	public String getMakerNotes(int _i) {
		return makerNotes[_i];
	}

	/* *** Property noPersons *** */

	private int noPersons;

	/**
	 * Set value of property noPersons
	 *
	 * @param _value - new field value
	 */
	public void setNoPersons(int _value) {
		noPersons = _value;
	}

	/**
	 * Get value of property noPersons
	 *
	 * @return - value of field noPersons
	 */
	public int getNoPersons() {
		return noPersons;
	}

	/* *** Property dateRegionsValid *** */

	private Date dateRegionsValid;

	/**
	 * Set value of property dateRegionsValid
	 *
	 * @param _value - new field value
	 */
	public void setDateRegionsValid(Date _value) {
		dateRegionsValid = _value;
	}

	/**
	 * Get value of property dateRegionsValid
	 *
	 * @return - value of field dateRegionsValid
	 */
	public Date getDateRegionsValid() {
		return dateRegionsValid;
	}

	/* *** Property lastPicasaIniEntry *** */

	private String lastPicasaIniEntry;

	/**
	 * Set value of property lastPicasaIniEntry
	 *
	 * @param _value - new field value
	 */
	public void setLastPicasaIniEntry(String _value) {
		lastPicasaIniEntry = _value;
	}

	/**
	 * Get value of property lastPicasaIniEntry
	 *
	 * @return - value of field lastPicasaIniEntry
	 */
	public String getLastPicasaIniEntry() {
		return lastPicasaIniEntry;
	}

	/* ----- Equality ----- */

	/**
	 * Compares the specified object with this object for equality.
	 *
	 * @param o the object to be compared with this object.
	 * @return true if the specified object is equal to this object.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {

		return (o instanceof Asset_typeImpl)
				&& getStringId().equals(((Asset_typeImpl) o).getStringId());
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

		if (name == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "name"));

		if (uri == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "uri"));

		if (ori == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "ori"));

		if (colorType == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "colorType"));

		if (bitsPerSample == null || bitsPerSample.length == 0)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "bitsPerSample"));

		if (subjectArea == null || subjectArea.length == 0)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "subjectArea"));

		if (subjectLocation == null || subjectLocation.length == 0)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "subjectLocation"));

		if (dof == null || dof.length == 0)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "dof"));

		if (fov == null || fov.length == 0)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "fov"));

		if (lens == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lens"));

		if (lensSerial == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lensSerial"));

		if (serial == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "serial"));

	}

}
