package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset asset
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Asset_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property name
	 *
	 * @param _value - new element value
	 */
	public void setName(String _value);

	/**
	 * Get value of property name
	 *
	 * @return - value of field name
	 */
	public String getName();

	/**
	 * Set value of property uri
	 *
	 * @param _value - new element value
	 */
	public void setUri(String _value);

	/**
	 * Get value of property uri
	 *
	 * @return - value of field uri
	 */
	public String getUri();

	/**
	 * Set value of property volume
	 *
	 * @param _value - new element value
	 */
	public void setVolume(String _value);

	/**
	 * Get value of property volume
	 *
	 * @return - value of field volume
	 */
	public String getVolume();

	/**
	 * Set value of property fileState
	 *
	 * @param _value - new element value
	 */
	public void setFileState(int _value);

	/**
	 * Get value of property fileState
	 *
	 * @return - value of field fileState
	 */
	public int getFileState();

	/**
	 * Set value of property fileSize
	 *
	 * @param _value - new element value
	 */
	public void setFileSize(long _value);

	/**
	 * Get value of property fileSize
	 *
	 * @return - value of field fileSize
	 */
	public long getFileSize();

	/**
	 * Set value of property album
	 *
	 * @param _value - new element value
	 */
	public void setAlbum(String[] _value);

	/**
	 * Set single element of array album
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setAlbum(String _element, int _i);

	/**
	 * Get value of property album
	 *
	 * @return - value of field album
	 */
	public String[] getAlbum();

	/**
	 * Get single element of array album
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array album
	 */
	public String getAlbum(int _i);

	/**
	 * Set value of property comments
	 *
	 * @param _value - new element value
	 */
	public void setComments(String _value);

	/**
	 * Get value of property comments
	 *
	 * @return - value of field comments
	 */
	public String getComments();

	/**
	 * Set value of property format
	 *
	 * @param _value - new element value
	 */
	public void setFormat(String _value);

	/**
	 * Get value of property format
	 *
	 * @return - value of field format
	 */
	public String getFormat();

	/**
	 * Set value of property mimeType
	 *
	 * @param _value - new element value
	 */
	public void setMimeType(String _value);

	/**
	 * Get value of property mimeType
	 *
	 * @return - value of field mimeType
	 */
	public String getMimeType();

	/**
	 * Set value of property safety
	 *
	 * @param _value - new element value
	 */
	public void setSafety(int _value);

	/**
	 * Get value of property safety
	 *
	 * @return - value of field safety
	 */
	public int getSafety();

	/**
	 * Set value of property contentType
	 *
	 * @param _value - new element value
	 */
	public void setContentType(int _value);

	/**
	 * Get value of property contentType
	 *
	 * @return - value of field contentType
	 */
	public int getContentType();

	/**
	 * Set value of property height
	 *
	 * @param _value - new element value
	 */
	public void setHeight(int _value);

	/**
	 * Get value of property height
	 *
	 * @return - value of field height
	 */
	public int getHeight();

	/**
	 * Set value of property width
	 *
	 * @param _value - new element value
	 */
	public void setWidth(int _value);

	/**
	 * Get value of property width
	 *
	 * @return - value of field width
	 */
	public int getWidth();

	public static final String ori_u = "u";
	public static final String ori_p = "p";
	public static final String ori_s = "s";
	public static final String ori_l = "l";

	public static final String[] oriALLVALUES = new String[] { ori_u, ori_p,
			ori_s, ori_l };

	/**
	 * Set value of property ori
	 *
	 * @param _value - new element value
	 */
	public void setOri(String _value);

	/**
	 * Get value of property ori
	 *
	 * @return - value of field ori
	 */
	public String getOri();

	/**
	 * Set value of property previewSize
	 *
	 * @param _value - new element value
	 */
	public void setPreviewSize(int _value);

	/**
	 * Get value of property previewSize
	 *
	 * @return - value of field previewSize
	 */
	public int getPreviewSize();

	/**
	 * Set value of property isProgressive
	 *
	 * @param _value - new element value
	 */
	public void setIsProgressive(boolean _value);

	/**
	 * Get value of property isProgressive
	 *
	 * @return - value of field isProgressive
	 */
	public boolean getIsProgressive();

	public static final String colorType_bW = "BW";
	public static final String colorType_gREYSCALE = "GREYSCALE";
	public static final String colorType_rGB = "RGB";
	public static final String colorType_cMYK = "CMYK";
	public static final String colorType_oTHER = "OTHER";
	public static final String colorType_uNKNOWN = "UNKNOWN";

	public static final String[] colorTypeALLVALUES = new String[] {
			colorType_bW, colorType_gREYSCALE, colorType_rGB, colorType_cMYK,
			colorType_oTHER, colorType_uNKNOWN };

	/**
	 * Set value of property colorType
	 *
	 * @param _value - new element value
	 */
	public void setColorType(String _value);

	/**
	 * Get value of property colorType
	 *
	 * @return - value of field colorType
	 */
	public String getColorType();

	/**
	 * Set value of property xmpModifiedAt
	 *
	 * @param _value - new element value
	 */
	public void setXmpModifiedAt(Date _value);

	/**
	 * Get value of property xmpModifiedAt
	 *
	 * @return - value of field xmpModifiedAt
	 */
	public Date getXmpModifiedAt();

	/**
	 * Set value of property voiceFileURI
	 *
	 * @param _value - new element value
	 */
	public void setVoiceFileURI(String _value);

	/**
	 * Get value of property voiceFileURI
	 *
	 * @return - value of field voiceFileURI
	 */
	public String getVoiceFileURI();

	/**
	 * Set value of property voiceVolume
	 *
	 * @param _value - new element value
	 */
	public void setVoiceVolume(String _value);

	/**
	 * Get value of property voiceVolume
	 *
	 * @return - value of field voiceVolume
	 */
	public String getVoiceVolume();

	/**
	 * Set value of property lastModification
	 *
	 * @param _value - new element value
	 */
	public void setLastModification(Date _value);

	/**
	 * Get value of property lastModification
	 *
	 * @return - value of field lastModification
	 */
	public Date getLastModification();

	/**
	 * Set value of property rotation
	 *
	 * @param _value - new element value
	 */
	public void setRotation(int _value);

	/**
	 * Get value of property rotation
	 *
	 * @return - value of field rotation
	 */
	public int getRotation();

	/**
	 * Set value of property importDate
	 *
	 * @param _value - new element value
	 */
	public void setImportDate(Date _value);

	/**
	 * Get value of property importDate
	 *
	 * @return - value of field importDate
	 */
	public Date getImportDate();

	/**
	 * Set value of property importedBy
	 *
	 * @param _value - new element value
	 */
	public void setImportedBy(String _value);

	/**
	 * Get value of property importedBy
	 *
	 * @return - value of field importedBy
	 */
	public String getImportedBy();

	/**
	 * Set value of property status
	 *
	 * @param _value - new element value
	 */
	public void setStatus(int _value);

	/**
	 * Get value of property status
	 *
	 * @return - value of field status
	 */
	public int getStatus();

	/**
	 * Set value of property rating
	 *
	 * @param _value - new element value
	 */
	public void setRating(int _value);

	/**
	 * Get value of property rating
	 *
	 * @return - value of field rating
	 */
	public int getRating();

	/**
	 * Set value of property ratedBy
	 *
	 * @param _value - new element value
	 */
	public void setRatedBy(String _value);

	/**
	 * Get value of property ratedBy
	 *
	 * @return - value of field ratedBy
	 */
	public String getRatedBy();

	/**
	 * Set value of property colorCode
	 *
	 * @param _value - new element value
	 */
	public void setColorCode(int _value);

	/**
	 * Get value of property colorCode
	 *
	 * @return - value of field colorCode
	 */
	public int getColorCode();

	/**
	 * Set value of property userfield1
	 *
	 * @param _value - new element value
	 */
	public void setUserfield1(String _value);

	/**
	 * Get value of property userfield1
	 *
	 * @return - value of field userfield1
	 */
	public String getUserfield1();

	/**
	 * Set value of property userfield2
	 *
	 * @param _value - new element value
	 */
	public void setUserfield2(String _value);

	/**
	 * Get value of property userfield2
	 *
	 * @return - value of field userfield2
	 */
	public String getUserfield2();

	/**
	 * Set value of property jpegThumbnail
	 *
	 * @param _value - new element value
	 */
	public void setJpegThumbnail(byte[] _value);

	/**
	 * Set single element of array jpegThumbnail
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setJpegThumbnail(byte _element, int _i);

	/**
	 * Get value of property jpegThumbnail
	 *
	 * @return - value of field jpegThumbnail
	 */
	public byte[] getJpegThumbnail();

	/**
	 * Get single element of array jpegThumbnail
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array jpegThumbnail
	 */
	public byte getJpegThumbnail(int _i);

	/**
	 * Set value of property bitsPerSample
	 *
	 * @param _value - new element value
	 */
	public void setBitsPerSample(int[] _value);

	/**
	 * Set single element of array bitsPerSample
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setBitsPerSample(int _element, int _i);

	/**
	 * Get value of property bitsPerSample
	 *
	 * @return - value of field bitsPerSample
	 */
	public int[] getBitsPerSample();

	/**
	 * Get single element of array bitsPerSample
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array bitsPerSample
	 */
	public int getBitsPerSample(int _i);

	/**
	 * Set value of property imageLength
	 *
	 * @param _value - new element value
	 */
	public void setImageLength(int _value);

	/**
	 * Get value of property imageLength
	 *
	 * @return - value of field imageLength
	 */
	public int getImageLength();

	/**
	 * Set value of property imageWidth
	 *
	 * @param _value - new element value
	 */
	public void setImageWidth(int _value);

	/**
	 * Get value of property imageWidth
	 *
	 * @return - value of field imageWidth
	 */
	public int getImageWidth();

	/**
	 * Set value of property compression
	 *
	 * @param _value - new element value
	 */
	public void setCompression(int _value);

	/**
	 * Get value of property compression
	 *
	 * @return - value of field compression
	 */
	public int getCompression();

	/**
	 * Set value of property photometricInterpretation
	 *
	 * @param _value - new element value
	 */
	public void setPhotometricInterpretation(int _value);

	/**
	 * Get value of property photometricInterpretation
	 *
	 * @return - value of field photometricInterpretation
	 */
	public int getPhotometricInterpretation();

	/**
	 * Set value of property orientation
	 *
	 * @param _value - new element value
	 */
	public void setOrientation(int _value);

	/**
	 * Get value of property orientation
	 *
	 * @return - value of field orientation
	 */
	public int getOrientation();

	/**
	 * Set value of property samplesPerPixel
	 *
	 * @param _value - new element value
	 */
	public void setSamplesPerPixel(int _value);

	/**
	 * Get value of property samplesPerPixel
	 *
	 * @return - value of field samplesPerPixel
	 */
	public int getSamplesPerPixel();

	public static final String xResolution__unit = "ppi";

	public static final String[] xResolutionALLATTRIBUTES = new String[] { xResolution__unit };

	/**
	 * Set value of property xResolution
	 *
	 * @param _value - new element value(unit=ppi)
	 */
	public void setXResolution(double _value);

	/**
	 * Get value of property xResolution
	 *
	 * @return - value of field xResolution(unit=ppi)
	 */
	public double getXResolution();

	public static final String yResolution__unitr = "ppi";

	public static final String[] yResolutionALLATTRIBUTES = new String[] { yResolution__unitr };

	/**
	 * Set value of property yResolution
	 *
	 * @param _value - new element value(unitr=ppi)
	 */
	public void setYResolution(double _value);

	/**
	 * Get value of property yResolution
	 *
	 * @return - value of field yResolution(unitr=ppi)
	 */
	public double getYResolution();

	/**
	 * Set value of property dateTime
	 *
	 * @param _value - new element value
	 */
	public void setDateTime(Date _value);

	/**
	 * Get value of property dateTime
	 *
	 * @return - value of field dateTime
	 */
	public Date getDateTime();

	/**
	 * Set value of property imageDescription
	 *
	 * @param _value - new element value
	 */
	public void setImageDescription(String _value);

	/**
	 * Get value of property imageDescription
	 *
	 * @return - value of field imageDescription
	 */
	public String getImageDescription();

	/**
	 * Set value of property make
	 *
	 * @param _value - new element value
	 */
	public void setMake(String _value);

	/**
	 * Get value of property make
	 *
	 * @return - value of field make
	 */
	public String getMake();

	/**
	 * Set value of property model
	 *
	 * @param _value - new element value
	 */
	public void setModel(String _value);

	/**
	 * Get value of property model
	 *
	 * @return - value of field model
	 */
	public String getModel();

	/**
	 * Set value of property software
	 *
	 * @param _value - new element value
	 */
	public void setSoftware(String _value);

	/**
	 * Get value of property software
	 *
	 * @return - value of field software
	 */
	public String getSoftware();

	/**
	 * Set value of property originalFileName
	 *
	 * @param _value - new element value
	 */
	public void setOriginalFileName(String _value);

	/**
	 * Get value of property originalFileName
	 *
	 * @return - value of field originalFileName
	 */
	public String getOriginalFileName();

	/**
	 * Set value of property originalImageId
	 *
	 * @param _value - new element value
	 */
	public void setOriginalImageId(String _value);

	/**
	 * Get value of property originalImageId
	 *
	 * @return - value of field originalImageId
	 */
	public String getOriginalImageId();

	/**
	 * Set value of property artist
	 *
	 * @param _value - new element value
	 */
	public void setArtist(String[] _value);

	/**
	 * Set single element of array artist
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setArtist(String _element, int _i);

	/**
	 * Get value of property artist
	 *
	 * @return - value of field artist
	 */
	public String[] getArtist();

	/**
	 * Get single element of array artist
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array artist
	 */
	public String getArtist(int _i);

	/**
	 * Set value of property copyright
	 *
	 * @param _value - new element value
	 */
	public void setCopyright(String _value);

	/**
	 * Get value of property copyright
	 *
	 * @return - value of field copyright
	 */
	public String getCopyright();

	/**
	 * Set value of property colorSpace
	 *
	 * @param _value - new element value
	 */
	public void setColorSpace(int _value);

	/**
	 * Get value of property colorSpace
	 *
	 * @return - value of field colorSpace
	 */
	public int getColorSpace();

	/**
	 * Set value of property profileDescription
	 *
	 * @param _value - new element value
	 */
	public void setProfileDescription(String _value);

	/**
	 * Get value of property profileDescription
	 *
	 * @return - value of field profileDescription
	 */
	public String getProfileDescription();

	/**
	 * Set value of property dateTimeOriginal
	 *
	 * @param _value - new element value
	 */
	public void setDateTimeOriginal(Date _value);

	/**
	 * Get value of property dateTimeOriginal
	 *
	 * @return - value of field dateTimeOriginal
	 */
	public Date getDateTimeOriginal();

	/**
	 * Set value of property exposureTime
	 *
	 * @param _value - new element value
	 */
	public void setExposureTime(double _value);

	/**
	 * Get value of property exposureTime
	 *
	 * @return - value of field exposureTime
	 */
	public double getExposureTime();

	/**
	 * Set value of property fNumber
	 *
	 * @param _value - new element value
	 */
	public void setFNumber(double _value);

	/**
	 * Get value of property fNumber
	 *
	 * @return - value of field fNumber
	 */
	public double getFNumber();

	/**
	 * Set value of property exposureProgram
	 *
	 * @param _value - new element value
	 */
	public void setExposureProgram(int _value);

	/**
	 * Get value of property exposureProgram
	 *
	 * @return - value of field exposureProgram
	 */
	public int getExposureProgram();

	/**
	 * Set value of property spectralSensitivity
	 *
	 * @param _value - new element value
	 */
	public void setSpectralSensitivity(String _value);

	/**
	 * Get value of property spectralSensitivity
	 *
	 * @return - value of field spectralSensitivity
	 */
	public String getSpectralSensitivity();

	/**
	 * Set value of property isoSpeedRatings
	 *
	 * @param _value - new element value
	 */
	public void setIsoSpeedRatings(int[] _value);

	/**
	 * Set single element of array isoSpeedRatings
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setIsoSpeedRatings(int _element, int _i);

	/**
	 * Get value of property isoSpeedRatings
	 *
	 * @return - value of field isoSpeedRatings
	 */
	public int[] getIsoSpeedRatings();

	/**
	 * Get single element of array isoSpeedRatings
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array isoSpeedRatings
	 */
	public int getIsoSpeedRatings(int _i);

	/**
	 * Set value of property scalarSpeedRatings
	 *
	 * @param _value - new element value
	 */
	public void setScalarSpeedRatings(int _value);

	/**
	 * Get value of property scalarSpeedRatings
	 *
	 * @return - value of field scalarSpeedRatings
	 */
	public int getScalarSpeedRatings();

	/**
	 * Set value of property shutterSpeed
	 *
	 * @param _value - new element value
	 */
	public void setShutterSpeed(double _value);

	/**
	 * Get value of property shutterSpeed
	 *
	 * @return - value of field shutterSpeed
	 */
	public double getShutterSpeed();

	/**
	 * Set value of property aperture
	 *
	 * @param _value - new element value
	 */
	public void setAperture(double _value);

	/**
	 * Get value of property aperture
	 *
	 * @return - value of field aperture
	 */
	public double getAperture();

	/**
	 * Set value of property brightnessValue
	 *
	 * @param _value - new element value
	 */
	public void setBrightnessValue(double _value);

	/**
	 * Get value of property brightnessValue
	 *
	 * @return - value of field brightnessValue
	 */
	public double getBrightnessValue();

	/**
	 * Set value of property exposureBias
	 *
	 * @param _value - new element value
	 */
	public void setExposureBias(double _value);

	/**
	 * Get value of property exposureBias
	 *
	 * @return - value of field exposureBias
	 */
	public double getExposureBias();

	/**
	 * Set value of property maxLensAperture
	 *
	 * @param _value - new element value
	 */
	public void setMaxLensAperture(double _value);

	/**
	 * Get value of property maxLensAperture
	 *
	 * @return - value of field maxLensAperture
	 */
	public double getMaxLensAperture();

	/**
	 * Set value of property subjectDistance
	 *
	 * @param _value - new element value
	 */
	public void setSubjectDistance(double _value);

	/**
	 * Get value of property subjectDistance
	 *
	 * @return - value of field subjectDistance
	 */
	public double getSubjectDistance();

	/**
	 * Set value of property meteringMode
	 *
	 * @param _value - new element value
	 */
	public void setMeteringMode(int _value);

	/**
	 * Get value of property meteringMode
	 *
	 * @return - value of field meteringMode
	 */
	public int getMeteringMode();

	/**
	 * Set value of property lightSource
	 *
	 * @param _value - new element value
	 */
	public void setLightSource(int _value);

	/**
	 * Get value of property lightSource
	 *
	 * @return - value of field lightSource
	 */
	public int getLightSource();

	/**
	 * Set value of property flashFired
	 *
	 * @param _value - new element value
	 */
	public void setFlashFired(boolean _value);

	/**
	 * Get value of property flashFired
	 *
	 * @return - value of field flashFired
	 */
	public boolean getFlashFired();

	/**
	 * Set value of property returnLightDetected
	 *
	 * @param _value - new element value
	 */
	public void setReturnLightDetected(int _value);

	/**
	 * Get value of property returnLightDetected
	 *
	 * @return - value of field returnLightDetected
	 */
	public int getReturnLightDetected();

	/**
	 * Set value of property flashAuto
	 *
	 * @param _value - new element value
	 */
	public void setFlashAuto(int _value);

	/**
	 * Get value of property flashAuto
	 *
	 * @return - value of field flashAuto
	 */
	public int getFlashAuto();

	/**
	 * Set value of property flashFunction
	 *
	 * @param _value - new element value
	 */
	public void setFlashFunction(boolean _value);

	/**
	 * Get value of property flashFunction
	 *
	 * @return - value of field flashFunction
	 */
	public boolean getFlashFunction();

	/**
	 * Set value of property redEyeReduction
	 *
	 * @param _value - new element value
	 */
	public void setRedEyeReduction(boolean _value);

	/**
	 * Get value of property redEyeReduction
	 *
	 * @return - value of field redEyeReduction
	 */
	public boolean getRedEyeReduction();

	/**
	 * Set value of property flashExposureComp
	 *
	 * @param _value - new element value
	 */
	public void setFlashExposureComp(double _value);

	/**
	 * Get value of property flashExposureComp
	 *
	 * @return - value of field flashExposureComp
	 */
	public double getFlashExposureComp();

	/**
	 * Set value of property focalLength
	 *
	 * @param _value - new element value
	 */
	public void setFocalLength(double _value);

	/**
	 * Get value of property focalLength
	 *
	 * @return - value of field focalLength
	 */
	public double getFocalLength();

	/**
	 * Set value of property subjectArea
	 *
	 * @param _value - new element value
	 */
	public void setSubjectArea(int[] _value);

	/**
	 * Set single element of array subjectArea
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSubjectArea(int _element, int _i);

	/**
	 * Get value of property subjectArea
	 *
	 * @return - value of field subjectArea
	 */
	public int[] getSubjectArea();

	/**
	 * Get single element of array subjectArea
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array subjectArea
	 */
	public int getSubjectArea(int _i);

	/**
	 * Set value of property flashEnergy
	 *
	 * @param _value - new element value
	 */
	public void setFlashEnergy(double _value);

	/**
	 * Get value of property flashEnergy
	 *
	 * @return - value of field flashEnergy
	 */
	public double getFlashEnergy();

	public static final String focalPlaneXResolution__unit = "ppi";

	public static final String[] focalPlaneXResolutionALLATTRIBUTES = new String[] { focalPlaneXResolution__unit };

	/**
	 * Set value of property focalPlaneXResolution
	 *
	 * @param _value - new element value(unit=ppi)
	 */
	public void setFocalPlaneXResolution(double _value);

	/**
	 * Get value of property focalPlaneXResolution
	 *
	 * @return - value of field focalPlaneXResolution(unit=ppi)
	 */
	public double getFocalPlaneXResolution();

	public static final String focalPlaneYResolution__unit = "ppi";

	public static final String[] focalPlaneYResolutionALLATTRIBUTES = new String[] { focalPlaneYResolution__unit };

	/**
	 * Set value of property focalPlaneYResolution
	 *
	 * @param _value - new element value(unit=ppi)
	 */
	public void setFocalPlaneYResolution(double _value);

	/**
	 * Get value of property focalPlaneYResolution
	 *
	 * @return - value of field focalPlaneYResolution(unit=ppi)
	 */
	public double getFocalPlaneYResolution();

	/**
	 * Set value of property subjectLocation
	 *
	 * @param _value - new element value
	 */
	public void setSubjectLocation(int[] _value);

	/**
	 * Set single element of array subjectLocation
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSubjectLocation(int _element, int _i);

	/**
	 * Get value of property subjectLocation
	 *
	 * @return - value of field subjectLocation
	 */
	public int[] getSubjectLocation();

	/**
	 * Get single element of array subjectLocation
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array subjectLocation
	 */
	public int getSubjectLocation(int _i);

	/**
	 * Set value of property exposureIndex
	 *
	 * @param _value - new element value
	 */
	public void setExposureIndex(double _value);

	/**
	 * Get value of property exposureIndex
	 *
	 * @return - value of field exposureIndex
	 */
	public double getExposureIndex();

	/**
	 * Set value of property sensingMethod
	 *
	 * @param _value - new element value
	 */
	public void setSensingMethod(int _value);

	/**
	 * Get value of property sensingMethod
	 *
	 * @return - value of field sensingMethod
	 */
	public int getSensingMethod();

	/**
	 * Set value of property fileSource
	 *
	 * @param _value - new element value
	 */
	public void setFileSource(int _value);

	/**
	 * Get value of property fileSource
	 *
	 * @return - value of field fileSource
	 */
	public int getFileSource();

	/**
	 * Set value of property exposureMode
	 *
	 * @param _value - new element value
	 */
	public void setExposureMode(int _value);

	/**
	 * Get value of property exposureMode
	 *
	 * @return - value of field exposureMode
	 */
	public int getExposureMode();

	/**
	 * Set value of property whiteBalance
	 *
	 * @param _value - new element value
	 */
	public void setWhiteBalance(String _value);

	/**
	 * Get value of property whiteBalance
	 *
	 * @return - value of field whiteBalance
	 */
	public String getWhiteBalance();

	/**
	 * Set value of property digitalZoomRatio
	 *
	 * @param _value - new element value
	 */
	public void setDigitalZoomRatio(double _value);

	/**
	 * Get value of property digitalZoomRatio
	 *
	 * @return - value of field digitalZoomRatio
	 */
	public double getDigitalZoomRatio();

	/**
	 * Set value of property focalLengthIn35MmFilm
	 *
	 * @param _value - new element value
	 */
	public void setFocalLengthIn35MmFilm(int _value);

	/**
	 * Get value of property focalLengthIn35MmFilm
	 *
	 * @return - value of field focalLengthIn35MmFilm
	 */
	public int getFocalLengthIn35MmFilm();

	/**
	 * Set value of property focalLengthFactor
	 *
	 * @param _value - new element value
	 */
	public void setFocalLengthFactor(double _value);

	/**
	 * Get value of property focalLengthFactor
	 *
	 * @return - value of field focalLengthFactor
	 */
	public double getFocalLengthFactor();

	/**
	 * Set value of property circleOfConfusion
	 *
	 * @param _value - new element value
	 */
	public void setCircleOfConfusion(double _value);

	/**
	 * Get value of property circleOfConfusion
	 *
	 * @return - value of field circleOfConfusion
	 */
	public double getCircleOfConfusion();

	/**
	 * Set value of property hyperfocalDistance
	 *
	 * @param _value - new element value
	 */
	public void setHyperfocalDistance(double _value);

	/**
	 * Get value of property hyperfocalDistance
	 *
	 * @return - value of field hyperfocalDistance
	 */
	public double getHyperfocalDistance();

	/**
	 * Set value of property dof
	 *
	 * @param _value - new element value
	 */
	public void setDof(double[] _value);

	/**
	 * Set single element of array dof
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setDof(double _element, int _i);

	/**
	 * Get value of property dof
	 *
	 * @return - value of field dof
	 */
	public double[] getDof();

	/**
	 * Get single element of array dof
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array dof
	 */
	public double getDof(int _i);

	/**
	 * Set value of property lv
	 *
	 * @param _value - new element value
	 */
	public void setLv(double _value);

	/**
	 * Get value of property lv
	 *
	 * @return - value of field lv
	 */
	public double getLv();

	/**
	 * Set value of property fov
	 *
	 * @param _value - new element value
	 */
	public void setFov(double[] _value);

	/**
	 * Set single element of array fov
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setFov(double _element, int _i);

	/**
	 * Get value of property fov
	 *
	 * @return - value of field fov
	 */
	public double[] getFov();

	/**
	 * Get single element of array fov
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array fov
	 */
	public double getFov(int _i);

	/**
	 * Set value of property sceneCaptureType
	 *
	 * @param _value - new element value
	 */
	public void setSceneCaptureType(int _value);

	/**
	 * Get value of property sceneCaptureType
	 *
	 * @return - value of field sceneCaptureType
	 */
	public int getSceneCaptureType();

	/**
	 * Set value of property gainControl
	 *
	 * @param _value - new element value
	 */
	public void setGainControl(int _value);

	/**
	 * Get value of property gainControl
	 *
	 * @return - value of field gainControl
	 */
	public int getGainControl();

	/**
	 * Set value of property contrast
	 *
	 * @param _value - new element value
	 */
	public void setContrast(int _value);

	/**
	 * Get value of property contrast
	 *
	 * @return - value of field contrast
	 */
	public int getContrast();

	/**
	 * Set value of property saturation
	 *
	 * @param _value - new element value
	 */
	public void setSaturation(int _value);

	/**
	 * Get value of property saturation
	 *
	 * @return - value of field saturation
	 */
	public int getSaturation();

	/**
	 * Set value of property sharpness
	 *
	 * @param _value - new element value
	 */
	public void setSharpness(int _value);

	/**
	 * Get value of property sharpness
	 *
	 * @return - value of field sharpness
	 */
	public int getSharpness();

	/**
	 * Set value of property vibrance
	 *
	 * @param _value - new element value
	 */
	public void setVibrance(int _value);

	/**
	 * Get value of property vibrance
	 *
	 * @return - value of field vibrance
	 */
	public int getVibrance();

	/**
	 * Set value of property subjectDistanceRange
	 *
	 * @param _value - new element value
	 */
	public void setSubjectDistanceRange(int _value);

	/**
	 * Get value of property subjectDistanceRange
	 *
	 * @return - value of field subjectDistanceRange
	 */
	public int getSubjectDistanceRange();

	/**
	 * Set value of property lens
	 *
	 * @param _value - new element value
	 */
	public void setLens(String _value);

	/**
	 * Get value of property lens
	 *
	 * @return - value of field lens
	 */
	public String getLens();

	/**
	 * Set value of property lensSerial
	 *
	 * @param _value - new element value
	 */
	public void setLensSerial(String _value);

	/**
	 * Get value of property lensSerial
	 *
	 * @return - value of field lensSerial
	 */
	public String getLensSerial();

	/**
	 * Set value of property serial
	 *
	 * @param _value - new element value
	 */
	public void setSerial(String _value);

	/**
	 * Get value of property serial
	 *
	 * @return - value of field serial
	 */
	public String getSerial();

	/**
	 * Set value of property gPSLatitude
	 *
	 * @param _value - new element value
	 */
	public void setGPSLatitude(double _value);

	/**
	 * Get value of property gPSLatitude
	 *
	 * @return - value of field gPSLatitude
	 */
	public double getGPSLatitude();

	/**
	 * Set value of property gPSLongitude
	 *
	 * @param _value - new element value
	 */
	public void setGPSLongitude(double _value);

	/**
	 * Get value of property gPSLongitude
	 *
	 * @return - value of field gPSLongitude
	 */
	public double getGPSLongitude();

	/**
	 * Set value of property gPSAltitude
	 *
	 * @param _value - new element value
	 */
	public void setGPSAltitude(double _value);

	/**
	 * Get value of property gPSAltitude
	 *
	 * @return - value of field gPSAltitude
	 */
	public double getGPSAltitude();

	/**
	 * Set value of property gPSTime
	 *
	 * @param _value - new element value
	 */
	public void setGPSTime(Date _value);

	/**
	 * Get value of property gPSTime
	 *
	 * @return - value of field gPSTime
	 */
	public Date getGPSTime();

	/**
	 * Set value of property gPSDOP
	 *
	 * @param _value - new element value
	 */
	public void setGPSDOP(double _value);

	/**
	 * Get value of property gPSDOP
	 *
	 * @return - value of field gPSDOP
	 */
	public double getGPSDOP();

	public static final String gPSSpeed__unit = "kmh";

	public static final String[] gPSSpeedALLATTRIBUTES = new String[] { gPSSpeed__unit };

	/**
	 * Set value of property gPSSpeed
	 *
	 * @param _value - new element value(unit=kmh)
	 */
	public void setGPSSpeed(double _value);

	/**
	 * Get value of property gPSSpeed
	 *
	 * @return - value of field gPSSpeed(unit=kmh)
	 */
	public double getGPSSpeed();

	/**
	 * Set value of property gPSTrackRef
	 *
	 * @param _value - new element value
	 */
	public void setGPSTrackRef(String _value);

	/**
	 * Get value of property gPSTrackRef
	 *
	 * @return - value of field gPSTrackRef
	 */
	public String getGPSTrackRef();

	/**
	 * Set value of property gPSTrack
	 *
	 * @param _value - new element value
	 */
	public void setGPSTrack(double _value);

	/**
	 * Get value of property gPSTrack
	 *
	 * @return - value of field gPSTrack
	 */
	public double getGPSTrack();

	/**
	 * Set value of property gPSImgDirectionRef
	 *
	 * @param _value - new element value
	 */
	public void setGPSImgDirectionRef(String _value);

	/**
	 * Get value of property gPSImgDirectionRef
	 *
	 * @return - value of field gPSImgDirectionRef
	 */
	public String getGPSImgDirectionRef();

	/**
	 * Set value of property gPSImgDirection
	 *
	 * @param _value - new element value
	 */
	public void setGPSImgDirection(double _value);

	/**
	 * Get value of property gPSImgDirection
	 *
	 * @return - value of field gPSImgDirection
	 */
	public double getGPSImgDirection();

	/**
	 * Set value of property gPSMapDatum
	 *
	 * @param _value - new element value
	 */
	public void setGPSMapDatum(String _value);

	/**
	 * Get value of property gPSMapDatum
	 *
	 * @return - value of field gPSMapDatum
	 */
	public String getGPSMapDatum();

	/**
	 * Set value of property gPSDestLatitude
	 *
	 * @param _value - new element value
	 */
	public void setGPSDestLatitude(double _value);

	/**
	 * Get value of property gPSDestLatitude
	 *
	 * @return - value of field gPSDestLatitude
	 */
	public double getGPSDestLatitude();

	/**
	 * Set value of property gPSDestLongitude
	 *
	 * @param _value - new element value
	 */
	public void setGPSDestLongitude(double _value);

	/**
	 * Get value of property gPSDestLongitude
	 *
	 * @return - value of field gPSDestLongitude
	 */
	public double getGPSDestLongitude();

	/**
	 * Set value of property gPSDestBearingRef
	 *
	 * @param _value - new element value
	 */
	public void setGPSDestBearingRef(String _value);

	/**
	 * Get value of property gPSDestBearingRef
	 *
	 * @return - value of field gPSDestBearingRef
	 */
	public String getGPSDestBearingRef();

	/**
	 * Set value of property gPSDestBearing
	 *
	 * @param _value - new element value
	 */
	public void setGPSDestBearing(double _value);

	/**
	 * Get value of property gPSDestBearing
	 *
	 * @return - value of field gPSDestBearing
	 */
	public double getGPSDestBearing();

	public static final String gPSDestDistance__unit = "km";

	public static final String[] gPSDestDistanceALLATTRIBUTES = new String[] { gPSDestDistance__unit };

	/**
	 * Set value of property gPSDestDistance
	 *
	 * @param _value - new element value(unit=km)
	 */
	public void setGPSDestDistance(double _value);

	/**
	 * Get value of property gPSDestDistance
	 *
	 * @return - value of field gPSDestDistance(unit=km)
	 */
	public double getGPSDestDistance();

	/**
	 * Set value of property gPSAreaInformation
	 *
	 * @param _value - new element value
	 */
	public void setGPSAreaInformation(String _value);

	/**
	 * Get value of property gPSAreaInformation
	 *
	 * @return - value of field gPSAreaInformation
	 */
	public String getGPSAreaInformation();

	/**
	 * Set value of property gPSDateStamp
	 *
	 * @param _value - new element value
	 */
	public void setGPSDateStamp(Date _value);

	/**
	 * Get value of property gPSDateStamp
	 *
	 * @return - value of field gPSDateStamp
	 */
	public Date getGPSDateStamp();

	/**
	 * Set value of property gPSDifferential
	 *
	 * @param _value - new element value
	 */
	public void setGPSDifferential(int _value);

	/**
	 * Get value of property gPSDifferential
	 *
	 * @return - value of field gPSDifferential
	 */
	public int getGPSDifferential();

	/**
	 * Set value of property writerEditor
	 *
	 * @param _value - new element value
	 */
	public void setWriterEditor(String _value);

	/**
	 * Get value of property writerEditor
	 *
	 * @return - value of field writerEditor
	 */
	public String getWriterEditor();

	/**
	 * Set value of property headline
	 *
	 * @param _value - new element value
	 */
	public void setHeadline(String _value);

	/**
	 * Get value of property headline
	 *
	 * @return - value of field headline
	 */
	public String getHeadline();

	/**
	 * Set value of property intellectualGenre
	 *
	 * @param _value - new element value
	 */
	public void setIntellectualGenre(String _value);

	/**
	 * Get value of property intellectualGenre
	 *
	 * @return - value of field intellectualGenre
	 */
	public String getIntellectualGenre();

	/**
	 * Set value of property title
	 *
	 * @param _value - new element value
	 */
	public void setTitle(String _value);

	/**
	 * Get value of property title
	 *
	 * @return - value of field title
	 */
	public String getTitle();

	/**
	 * Set value of property specialInstructions
	 *
	 * @param _value - new element value
	 */
	public void setSpecialInstructions(String _value);

	/**
	 * Get value of property specialInstructions
	 *
	 * @return - value of field specialInstructions
	 */
	public String getSpecialInstructions();

	/**
	 * Set value of property jobId
	 *
	 * @param _value - new element value
	 */
	public void setJobId(String _value);

	/**
	 * Get value of property jobId
	 *
	 * @return - value of field jobId
	 */
	public String getJobId();

	/**
	 * Set value of property keyword
	 *
	 * @param _value - new element value
	 */
	public void setKeyword(String[] _value);

	/**
	 * Set single element of array keyword
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setKeyword(String _element, int _i);

	/**
	 * Get value of property keyword
	 *
	 * @return - value of field keyword
	 */
	public String[] getKeyword();

	/**
	 * Get single element of array keyword
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array keyword
	 */
	public String getKeyword(int _i);

	/**
	 * Set value of property category
	 *
	 * @param _value - new element value
	 */
	public void setCategory(String _value);

	/**
	 * Get value of property category
	 *
	 * @return - value of field category
	 */
	public String getCategory();

	/**
	 * Set value of property supplementalCats
	 *
	 * @param _value - new element value
	 */
	public void setSupplementalCats(String[] _value);

	/**
	 * Set single element of array supplementalCats
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSupplementalCats(String _element, int _i);

	/**
	 * Get value of property supplementalCats
	 *
	 * @return - value of field supplementalCats
	 */
	public String[] getSupplementalCats();

	/**
	 * Get single element of array supplementalCats
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array supplementalCats
	 */
	public String getSupplementalCats(int _i);

	/**
	 * Set value of property urgency
	 *
	 * @param _value - new element value
	 */
	public void setUrgency(int _value);

	/**
	 * Get value of property urgency
	 *
	 * @return - value of field urgency
	 */
	public int getUrgency();

	/**
	 * Set value of property authorsPosition
	 *
	 * @param _value - new element value
	 */
	public void setAuthorsPosition(String _value);

	/**
	 * Get value of property authorsPosition
	 *
	 * @return - value of field authorsPosition
	 */
	public String getAuthorsPosition();

	/**
	 * Set value of property credit
	 *
	 * @param _value - new element value
	 */
	public void setCredit(String _value);

	/**
	 * Get value of property credit
	 *
	 * @return - value of field credit
	 */
	public String getCredit();

	/**
	 * Set value of property usageTerms
	 *
	 * @param _value - new element value
	 */
	public void setUsageTerms(String _value);

	/**
	 * Get value of property usageTerms
	 *
	 * @return - value of field usageTerms
	 */
	public String getUsageTerms();

	/**
	 * Set value of property owner
	 *
	 * @param _value - new element value
	 */
	public void setOwner(String[] _value);

	/**
	 * Set single element of array owner
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setOwner(String _element, int _i);

	/**
	 * Get value of property owner
	 *
	 * @return - value of field owner
	 */
	public String[] getOwner();

	/**
	 * Get single element of array owner
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array owner
	 */
	public String getOwner(int _i);

	/**
	 * Set value of property source
	 *
	 * @param _value - new element value
	 */
	public void setSource(String _value);

	/**
	 * Get value of property source
	 *
	 * @return - value of field source
	 */
	public String getSource();

	/**
	 * Set value of property dateCreated
	 *
	 * @param _value - new element value
	 */
	public void setDateCreated(Date _value);

	/**
	 * Get value of property dateCreated
	 *
	 * @return - value of field dateCreated
	 */
	public Date getDateCreated();

	/**
	 * Set value of property sceneCode
	 *
	 * @param _value - new element value
	 */
	public void setSceneCode(String[] _value);

	/**
	 * Set single element of array sceneCode
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSceneCode(String _element, int _i);

	/**
	 * Get value of property sceneCode
	 *
	 * @return - value of field sceneCode
	 */
	public String[] getSceneCode();

	/**
	 * Get single element of array sceneCode
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array sceneCode
	 */
	public String getSceneCode(int _i);

	/**
	 * Set value of property subjectCode
	 *
	 * @param _value - new element value
	 */
	public void setSubjectCode(String[] _value);

	/**
	 * Set single element of array subjectCode
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setSubjectCode(String _element, int _i);

	/**
	 * Get value of property subjectCode
	 *
	 * @return - value of field subjectCode
	 */
	public String[] getSubjectCode();

	/**
	 * Get single element of array subjectCode
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array subjectCode
	 */
	public String getSubjectCode(int _i);

	/**
	 * Set value of property modelInformation
	 *
	 * @param _value - new element value
	 */
	public void setModelInformation(String _value);

	/**
	 * Get value of property modelInformation
	 *
	 * @return - value of field modelInformation
	 */
	public String getModelInformation();

	/**
	 * Set value of property modelAge
	 *
	 * @param _value - new element value
	 */
	public void setModelAge(int _value);

	/**
	 * Get value of property modelAge
	 *
	 * @return - value of field modelAge
	 */
	public int getModelAge();

	/**
	 * Set value of property codeOfOrg
	 *
	 * @param _value - new element value
	 */
	public void setCodeOfOrg(String[] _value);

	/**
	 * Set single element of array codeOfOrg
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setCodeOfOrg(String _element, int _i);

	/**
	 * Get value of property codeOfOrg
	 *
	 * @return - value of field codeOfOrg
	 */
	public String[] getCodeOfOrg();

	/**
	 * Get single element of array codeOfOrg
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array codeOfOrg
	 */
	public String getCodeOfOrg(int _i);

	/**
	 * Set value of property nameOfOrg
	 *
	 * @param _value - new element value
	 */
	public void setNameOfOrg(String[] _value);

	/**
	 * Set single element of array nameOfOrg
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setNameOfOrg(String _element, int _i);

	/**
	 * Get value of property nameOfOrg
	 *
	 * @return - value of field nameOfOrg
	 */
	public String[] getNameOfOrg();

	/**
	 * Get single element of array nameOfOrg
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array nameOfOrg
	 */
	public String getNameOfOrg(int _i);

	/**
	 * Set value of property personShown
	 *
	 * @param _value - new element value
	 */
	public void setPersonShown(String[] _value);

	/**
	 * Set single element of array personShown
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setPersonShown(String _element, int _i);

	/**
	 * Get value of property personShown
	 *
	 * @return - value of field personShown
	 */
	public String[] getPersonShown();

	/**
	 * Get single element of array personShown
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array personShown
	 */
	public String getPersonShown(int _i);

	/**
	 * Set value of property event
	 *
	 * @param _value - new element value
	 */
	public void setEvent(String _value);

	/**
	 * Get value of property event
	 *
	 * @return - value of field event
	 */
	public String getEvent();

	/**
	 * Set value of property lastEdited
	 *
	 * @param _value - new element value
	 */
	public void setLastEdited(Date _value);

	/**
	 * Get value of property lastEdited
	 *
	 * @return - value of field lastEdited
	 */
	public Date getLastEdited();

	/**
	 * Set value of property maxAvailHeight
	 *
	 * @param _value - new element value
	 */
	public void setMaxAvailHeight(int _value);

	/**
	 * Get value of property maxAvailHeight
	 *
	 * @return - value of field maxAvailHeight
	 */
	public int getMaxAvailHeight();

	/**
	 * Set value of property maxAvailWidth
	 *
	 * @param _value - new element value
	 */
	public void setMaxAvailWidth(int _value);

	/**
	 * Get value of property maxAvailWidth
	 *
	 * @return - value of field maxAvailWidth
	 */
	public int getMaxAvailWidth();

	/**
	 * Set value of property emulsion
	 *
	 * @param _value - new element value
	 */
	public void setEmulsion(String _value);

	/**
	 * Get value of property emulsion
	 *
	 * @return - value of field emulsion
	 */
	public String getEmulsion();

	/**
	 * Set value of property analogType
	 *
	 * @param _value - new element value
	 */
	public void setAnalogType(int _value);

	/**
	 * Get value of property analogType
	 *
	 * @return - value of field analogType
	 */
	public int getAnalogType();

	/**
	 * Set value of property analogFormat
	 *
	 * @param _value - new element value
	 */
	public void setAnalogFormat(int _value);

	/**
	 * Get value of property analogFormat
	 *
	 * @return - value of field analogFormat
	 */
	public int getAnalogFormat();

	/**
	 * Set value of property analogProcessing
	 *
	 * @param _value - new element value
	 */
	public void setAnalogProcessing(String _value);

	/**
	 * Get value of property analogProcessing
	 *
	 * @return - value of field analogProcessing
	 */
	public String getAnalogProcessing();

	/**
	 * Set value of property lastEditor
	 *
	 * @param _value - new element value
	 */
	public void setLastEditor(String _value);

	/**
	 * Get value of property lastEditor
	 *
	 * @return - value of field lastEditor
	 */
	public String getLastEditor();

	/**
	 * Set value of property score
	 *
	 * @param _value - new element value
	 */
	public void setScore(float _value);

	/**
	 * Get value of property score
	 *
	 * @return - value of field score
	 */
	public float getScore();

	/**
	 * Set value of property sales
	 *
	 * @param _value - new element value
	 */
	public void setSales(int _value);

	/**
	 * Get value of property sales
	 *
	 * @return - value of field sales
	 */
	public int getSales();

	/**
	 * Set value of property price
	 *
	 * @param _value - new element value
	 */
	public void setPrice(double _value);

	/**
	 * Get value of property price
	 *
	 * @return - value of field price
	 */
	public double getPrice();

	/**
	 * Set value of property earnings
	 *
	 * @param _value - new element value
	 */
	public void setEarnings(double _value);

	/**
	 * Get value of property earnings
	 *
	 * @return - value of field earnings
	 */
	public double getEarnings();

	/**
	 * Set value of property makerNotes
	 *
	 * @param _value - new element value
	 */
	public void setMakerNotes(String[] _value);

	/**
	 * Set single element of array makerNotes
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setMakerNotes(String _element, int _i);

	/**
	 * Get value of property makerNotes
	 *
	 * @return - value of field makerNotes
	 */
	public String[] getMakerNotes();

	/**
	 * Get single element of array makerNotes
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array makerNotes
	 */
	public String getMakerNotes(int _i);

	/**
	 * Set value of property noPersons
	 *
	 * @param _value - new element value
	 */
	public void setNoPersons(int _value);

	/**
	 * Get value of property noPersons
	 *
	 * @return - value of field noPersons
	 */
	public int getNoPersons();

	/**
	 * Set value of property dateRegionsValid
	 *
	 * @param _value - new element value
	 */
	public void setDateRegionsValid(Date _value);

	/**
	 * Get value of property dateRegionsValid
	 *
	 * @return - value of field dateRegionsValid
	 */
	public Date getDateRegionsValid();

	/**
	 * Set value of property lastPicasaIniEntry
	 *
	 * @param _value - new element value
	 */
	public void setLastPicasaIniEntry(String _value);

	/**
	 * Get value of property lastPicasaIniEntry
	 *
	 * @return - value of field lastPicasaIniEntry
	 */
	public String getLastPicasaIniEntry();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
