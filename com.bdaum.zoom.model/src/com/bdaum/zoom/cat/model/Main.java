package com.bdaum.zoom.cat.model;

import com.bdaum.aoModeling.runtime.*;
import com.bdaum.zoom.cat.model.Bookmark;
import com.bdaum.zoom.cat.model.BookmarkImpl;
import com.bdaum.zoom.cat.model.Font;
import com.bdaum.zoom.cat.model.FontImpl;
import com.bdaum.zoom.cat.model.MigrationPolicy;
import com.bdaum.zoom.cat.model.MigrationPolicyImpl;
import com.bdaum.zoom.cat.model.MigrationRule;
import com.bdaum.zoom.cat.model.MigrationRuleImpl;
import com.bdaum.zoom.cat.model.Rgb;
import com.bdaum.zoom.cat.model.RgbImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObject;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectImpl;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShown;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObjectShownImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.MediaExtension;
import com.bdaum.zoom.cat.model.asset.MediaExtensionImpl;
import com.bdaum.zoom.cat.model.asset.Region;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.asset.TrackRecord;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.cat.model.composedTo.ComposedTo;
import com.bdaum.zoom.cat.model.composedTo.ComposedToImpl;
import com.bdaum.zoom.cat.model.creatorsContact.Contact;
import com.bdaum.zoom.cat.model.creatorsContact.ContactImpl;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContact;
import com.bdaum.zoom.cat.model.creatorsContact.CreatorsContactImpl;
import com.bdaum.zoom.cat.model.derivedBy.DerivedBy;
import com.bdaum.zoom.cat.model.derivedBy.DerivedByImpl;
import com.bdaum.zoom.cat.model.ghost.Ghost;
import com.bdaum.zoom.cat.model.ghost.GhostImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.PostProcessor;
import com.bdaum.zoom.cat.model.group.PostProcessorImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibit;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibition;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.Wall;
import com.bdaum.zoom.cat.model.group.exhibition.WallImpl;
import com.bdaum.zoom.cat.model.group.slideShow.Slide;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShow;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.Storyboard;
import com.bdaum.zoom.cat.model.group.webGallery.StoryboardImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibit;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebGallery;
import com.bdaum.zoom.cat.model.group.webGallery.WebGalleryImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameter;
import com.bdaum.zoom.cat.model.group.webGallery.WebParameterImpl;
import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreated;
import com.bdaum.zoom.cat.model.locationCreated.LocationCreatedImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShown;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.cat.model.meta.CategoryImpl;
import com.bdaum.zoom.cat.model.meta.LastDeviceImport;
import com.bdaum.zoom.cat.model.meta.LastDeviceImportImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.MetaImpl;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.cat.model.pageLayout.PageLayout;
import com.bdaum.zoom.cat.model.pageLayout.PageLayoutImpl;
import com.bdaum.zoom.cat.model.report.Report;
import com.bdaum.zoom.cat.model.report.ReportImpl;
import com.bdaum.zoom.cat.model.similarityOptions.SimilarityOptions;
import com.bdaum.zoom.cat.model.similarityOptions.SimilarityOptionsImpl;
import com.bdaum.zoom.cat.model.textSearchOptions.TextSearchOptions;
import com.bdaum.zoom.cat.model.textSearchOptions.TextSearchOptionsImpl;
import java.io.Serializable;
import java.util.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 */

/* !! This class may be modified manually. Generated comments must not be modified or deleted !! */

public class Main extends IdentifiableObject implements IModel,
		ValueChangedListener {
	/*--com.bdaum.aoModeling.generator.java/instrumentation constraint */
	// Start of KLEEN maintained section --->
	/**
	 * Create and register main Constraint Aspects
	 */
	private static boolean mainConstraintInstrumentationSet = false;

	public static void attachMainConstraintInstrumentation() {

		if (mainConstraintInstrumentationSet)
			return;
		mainConstraintInstrumentationSet = true;
		/*--com.bdaum.aoModeling.generator.java/aspect */
	}

	// <--- End of KLEEN maintained section
	/*--com.bdaum.aoModeling.generator.java/instrumentation*/

	/*--com.bdaum.aoModeling.generator.java/instrumentation security */
	// Start of KLEEN maintained section --->
	/**
	 * Create and register main Security Aspects
	 */
	private static boolean mainSecurityInstrumentationSet = false;

	public static void attachMainSecurityInstrumentation() {

		if (mainSecurityInstrumentationSet)
			return;
		mainSecurityInstrumentationSet = true;
		/*--com.bdaum.aoModeling.generator.java/aspect */
	}

	// <--- End of KLEEN maintained section
	/*--com.bdaum.aoModeling.generator.java/instrumentation*/

	/*--com.bdaum.aoModeling.generator.java/instrumentation operation */
	// Start of KLEEN maintained section --->
	/**
	 * Create and register main Operation Aspects
	 */
	private static boolean mainOperationInstrumentationSet = false;

	public static void attachMainOperationInstrumentation() {

		if (mainOperationInstrumentationSet)
			return;
		mainOperationInstrumentationSet = true;
		/*--com.bdaum.aoModeling.generator.java/aspect */
	}

	// <--- End of KLEEN maintained section
	/*--com.bdaum.aoModeling.generator.java/instrumentation*/

	/*--com.bdaum.aoModeling.generator.java/instrumentation logging */
	// Start of KLEEN maintained section --->
	/**
	 * Create and register main Logging Aspects
	 */
	private static boolean mainLoggingInstrumentationSet = false;

	public static void attachMainLoggingInstrumentation() {

		if (mainLoggingInstrumentationSet)
			return;
		mainLoggingInstrumentationSet = true;
		/*--com.bdaum.aoModeling.generator.java/aspect */
	}

	// <--- End of KLEEN maintained section
	/*--com.bdaum.aoModeling.generator.java/instrumentation*/

	/*--com.bdaum.aoModeling.generator.java/instrumentation gui */
	// Start of KLEEN maintained section --->
	/**
	 * Create and register main Gui Aspects
	 */
	private static boolean mainGuiInstrumentationSet = false;

	public static void attachMainGuiInstrumentation() {

		if (mainGuiInstrumentationSet)
			return;
		mainGuiInstrumentationSet = true;
		/*--com.bdaum.aoModeling.generator.java/aspect */
	}

	// <--- End of KLEEN maintained section
	/*--com.bdaum.aoModeling.generator.java/instrumentation*/

	/*--com.bdaum.aoModeling.generator.java/instrumentation custom */
	// Start of KLEEN maintained section --->
	/**
	 * Create and register main Custom Aspects
	 */
	private static boolean mainCustomInstrumentationSet = false;

	public static void attachMainCustomInstrumentation() {

		if (mainCustomInstrumentationSet)
			return;
		mainCustomInstrumentationSet = true;
		/*--com.bdaum.aoModeling.generator.java/aspect */
	}

	// <--- End of KLEEN maintained section
	/*--com.bdaum.aoModeling.generator.java/instrumentation*/

	static final long serialVersionUID = 131725394954712L;

	private static Main instance;

	private List<ValueChangedListener> valueChangedListeners = new ArrayList<ValueChangedListener>(
			3);

	private String[] parms;

	private boolean globalEventing = false;

	/*
	 * Returns the single instance of the model.
	 * Creates this instance if necessary.
	 */
	public static Main getInstance() {
		if (instance == null) {
			attachInstrumentation();
			instance = new Main();
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IModel#addValueChangedListener(ValueChangedListener)
	 */
	public void addValueChangedListener(ValueChangedListener listener) {
		if (!valueChangedListeners.contains(listener))
			valueChangedListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IModel#removeValueChangedListener(ValueChangedListener)
	 */
	public void removeValueChangedListener(ValueChangedListener listener) {
		valueChangedListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.ValueChangedListener#valueChanged(ValueChangedEvent)
	 */
	public void valueChanged(ValueChangedEvent event) {
		Iterator it = valueChangedListeners.iterator();
		while (it.hasNext())
			((ValueChangedListener) it.next()).valueChanged(event);
	}

	/*--com.bdaum.aoModeling.generator.java/instrumentation factory */
	// Start of KLEEN maintained section --->

	/**
	 * Factory method for creating an instance of asset ArtworkOrObject
	 *
	 * @param copyrightNotice - Property
	 * @param dateCreated - Property
	 * @param source - Property
	 * @param sourceInventoryNumber - Property
	 * @param title - Property
	 */
	public ArtworkOrObject createArtworkOrObjectInstance(
			String copyrightNotice, Date dateCreated, String source,
			String sourceInventoryNumber, String title) {
		return new ArtworkOrObjectImpl(copyrightNotice, dateCreated, source,
				sourceInventoryNumber, title);
	}

	/**
	 * Factory method for creating an instance of asset ArtworkOrObjectShown
	 *
	 * @param artworkOrObject - Arc
	 * @param asset - Arc
	 */
	public ArtworkOrObjectShown createArtworkOrObjectShownInstance(
			String artworkOrObject, String asset) {
		return new ArtworkOrObjectShownImpl(artworkOrObject, asset);
	}

	/**
	 * Factory method for creating an instance of asset Asset
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
	public Asset createAssetInstance(String name, String uri, String volume,
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
		return new AssetImpl(name, uri, volume, fileState, fileSize, comments,
				format, mimeType, safety, contentType, height, width, ori,
				previewSize, isProgressive, colorType, xmpModifiedAt,
				voiceFileURI, voiceVolume, lastModification, rotation,
				importDate, importedBy, status, rating, ratedBy, colorCode,
				userfield1, userfield2, imageLength, imageWidth, compression,
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
				noPersons, dateRegionsValid, lastPicasaIniEntry);
	}

	/**
	 * Factory method for creating an instance of asset Bookmark
	 *
	 * @param label - Property
	 * @param assetId - Property
	 * @param collectionId - Property
	 * @param createdAt - Property
	 * @param peer - Property
	 * @param catFile - Property
	 */
	public Bookmark createBookmarkInstance(String label, String assetId,
			String collectionId, Date createdAt, String peer, String catFile) {
		return new BookmarkImpl(label, assetId, collectionId, createdAt, peer,
				catFile);
	}

	/**
	 * Factory method for creating an instance of asset Category
	 *
	 * @param label - Property
	 */
	public Category createCategoryInstance(String label) {
		return new CategoryImpl(label);
	}

	/**
	 * Factory method for creating an instance of asset ComposedTo
	 *
	 * @param type - Property
	 * @param recipe - Property
	 * @param parameterFile - Property
	 * @param tool - Property
	 * @param date - Property
	 * @param composite - Arc
	 */
	public ComposedTo createComposedToInstance(String type, String recipe,
			String parameterFile, String tool, Date date, String composite) {
		return new ComposedToImpl(type, recipe, parameterFile, tool, date,
				composite);
	}

	/**
	 * Factory method for creating an instance of asset Contact
	 *
	 * @param city - Property
	 * @param country - Property
	 * @param postalCode - Property
	 * @param state - Property
	 */
	public Contact createContactInstance(String city, String country,
			String postalCode, String state) {
		return new ContactImpl(city, country, postalCode, state);
	}

	/**
	 * Factory method for creating an instance of asset CreatorsContact
	 *
	 * @param contact - Arc
	 */
	public CreatorsContact createCreatorsContactInstance(String contact) {
		return new CreatorsContactImpl(contact);
	}

	/**
	 * Factory method for creating an instance of asset Criterion
	 *
	 * @param field - Property
	 * @param subfield - Property
	 * @param value - Property
	 * @param relation - Property
	 * @param and - Property
	 */
	public Criterion createCriterionInstance(String field, String subfield,
			Object value, int relation, boolean and) {
		return new CriterionImpl(field, subfield, value, relation, and);
	}

	/**
	 * Factory method for creating an instance of asset DerivedBy
	 *
	 * @param recipe - Property
	 * @param parameterFile - Property
	 * @param tool - Property
	 * @param date - Property
	 * @param derivative - Arc
	 * @param original - Arc
	 */
	public DerivedBy createDerivedByInstance(String recipe,
			String parameterFile, String tool, Date date, String derivative,
			String original) {
		return new DerivedByImpl(recipe, parameterFile, tool, date, derivative,
				original);
	}

	/**
	 * Factory method for creating an instance of asset Exhibit
	 *
	 * @param title - Property
	 * @param description - Property
	 * @param credits - Property
	 * @param date - Property
	 * @param x - Property
	 * @param y - Property
	 * @param width - Property
	 * @param height - Property
	 * @param matWidth - Property
	 * @param matColor - Property
	 * @param frameWidth - Property
	 * @param frameColor - Property
	 * @param sold - Property
	 * @param hideLabel - Property
	 * @param labelAlignment - Property
	 * @param labelDistance - Property
	 * @param labelIndent - Property
	 * @param asset - Arc
	 */
	public Exhibit createExhibitInstance(String title, String description,
			String credits, String date, int x, int y, int width, int height,
			Integer matWidth, Rgb_type matColor, Integer frameWidth,
			Rgb_type frameColor, boolean sold, Boolean hideLabel,
			Integer labelAlignment, Integer labelDistance, Integer labelIndent,
			String asset) {
		return new ExhibitImpl(title, description, credits, date, x, y, width,
				height, matWidth, matColor, frameWidth, frameColor, sold,
				hideLabel, labelAlignment, labelDistance, labelIndent, asset);
	}

	/**
	 * Factory method for creating an instance of asset Exhibition
	 *
	 * @param name - Property
	 * @param description - Property
	 * @param info - Property
	 * @param defaultViewingHeight - Property
	 * @param variance - Property
	 * @param gridSize - Property
	 * @param showGrid - Property
	 * @param snapToGrid - Property
	 * @param defaultDescription - Property
	 * @param labelFontFamily - Property
	 * @param labelFontSize - Property
	 * @param labelSequence - Property
	 * @param hideLabel - Property
	 * @param labelAlignment - Property
	 * @param labelDistance - Property
	 * @param labelIndent - Property
	 * @param startX - Property
	 * @param startY - Property
	 * @param matWidth - Property
	 * @param matColor - Property
	 * @param frameWidth - Property
	 * @param frameColor - Property
	 * @param groundColor - Property
	 * @param horizonColor - Property
	 * @param ceilingColor - Property
	 * @param audio - Property
	 * @param outputFolder - Property
	 * @param ftpDir - Property
	 * @param isFtp - Property
	 * @param pageName - Property
	 * @param applySharpening - Property
	 * @param radius - Property
	 * @param amount - Property
	 * @param threshold - Property
	 * @param addWatermark - Property
	 * @param contactName - Property
	 * @param email - Property
	 * @param webUrl - Property
	 * @param copyright - Property
	 * @param logo - Property
	 * @param infoPlatePosition - Property
	 * @param hideCredits - Property
	 * @param jpegQuality - Property
	 * @param scalingMethod - Property
	 * @param lastAccessDate - Property
	 * @param perspective - Property
	 */
	public Exhibition createExhibitionInstance(String name, String description,
			String info, int defaultViewingHeight, int variance, int gridSize,
			boolean showGrid, boolean snapToGrid, String defaultDescription,
			String labelFontFamily, int labelFontSize, int labelSequence,
			boolean hideLabel, Integer labelAlignment, Integer labelDistance,
			Integer labelIndent, int startX, int startY, int matWidth,
			Rgb_type matColor, int frameWidth, Rgb_type frameColor,
			Rgb_type groundColor, Rgb_type horizonColor, Rgb_type ceilingColor,
			String audio, String outputFolder, String ftpDir, boolean isFtp,
			String pageName, Boolean applySharpening, float radius,
			float amount, int threshold, boolean addWatermark,
			String contactName, String email, String webUrl, String copyright,
			String logo, int infoPlatePosition, boolean hideCredits,
			int jpegQuality, int scalingMethod, Date lastAccessDate,
			String perspective) {
		return new ExhibitionImpl(name, description, info,
				defaultViewingHeight, variance, gridSize, showGrid, snapToGrid,
				defaultDescription, labelFontFamily, labelFontSize,
				labelSequence, hideLabel, labelAlignment, labelDistance,
				labelIndent, startX, startY, matWidth, matColor, frameWidth,
				frameColor, groundColor, horizonColor, ceilingColor, audio,
				outputFolder, ftpDir, isFtp, pageName, applySharpening, radius,
				amount, threshold, addWatermark, contactName, email, webUrl,
				copyright, logo, infoPlatePosition, hideCredits, jpegQuality,
				scalingMethod, lastAccessDate, perspective);
	}

	/**
	 * Factory method for creating an instance of asset Font
	 *
	 * @param size - Property
	 * @param style - Property
	 * @param weight - Property
	 * @param variant - Property
	 * @param color - Property
	 */
	public Font createFontInstance(int size, int style, int weight,
			int variant, Rgb_type color) {
		return new FontImpl(size, style, weight, variant, color);
	}

	/**
	 * Factory method for creating an instance of asset Ghost
	 *
	 * @param name - Property
	 * @param uri - Property
	 * @param volume - Property
	 */
	public Ghost createGhostInstance(String name, String uri, String volume) {
		return new GhostImpl(name, uri, volume);
	}

	/**
	 * Factory method for creating an instance of asset Group
	 *
	 * @param name - Property
	 * @param system - Property
	 */
	public Group createGroupInstance(String name, boolean system) {
		return new GroupImpl(name, system);
	}

	/**
	 * Factory method for creating an instance of asset LastDeviceImport
	 *
	 * @param volume - Property
	 * @param timestamp - Property
	 * @param description - Property
	 * @param owner - Property
	 */
	public LastDeviceImport createLastDeviceImportInstance(String volume,
			long timestamp, String description, String owner) {
		return new LastDeviceImportImpl(volume, timestamp, description, owner);
	}

	/**
	 * Factory method for creating an instance of asset Location
	 *
	 * @param city - Property
	 * @param details - Property
	 * @param provinceOrState - Property
	 * @param countryName - Property
	 * @param countryISOCode - Property
	 * @param sublocation - Property
	 * @param worldRegion - Property
	 * @param worldRegionCode - Property
	 * @param longitude - Property
	 * @param latitude - Property
	 * @param altitude - Property
	 */
	public Location createLocationInstance(String city, String details,
			String provinceOrState, String countryName, String countryISOCode,
			String sublocation, String worldRegion, String worldRegionCode,
			Double longitude, Double latitude, Double altitude) {
		return new LocationImpl(city, details, provinceOrState, countryName,
				countryISOCode, sublocation, worldRegion, worldRegionCode,
				longitude, latitude, altitude);
	}

	/**
	 * Factory method for creating an instance of asset LocationCreated
	 *
	 * @param location - Arc
	 */
	public LocationCreated createLocationCreatedInstance(String location) {
		return new LocationCreatedImpl(location);
	}

	/**
	 * Factory method for creating an instance of asset LocationShown
	 *
	 * @param location - Arc
	 * @param asset - Arc
	 */
	public LocationShown createLocationShownInstance(String location,
			String asset) {
		return new LocationShownImpl(location, asset);
	}

	/**
	 * Factory method for creating an instance of asset MediaExtension
	 *
	 */
	public MediaExtension createMediaExtensionInstance() {
		return new MediaExtensionImpl();
	}

	/**
	 * Factory method for creating an instance of asset Meta
	 *
	 * @param version - Property
	 * @param relevantLireVersion - Property
	 * @param creationDate - Property
	 * @param owner - Property
	 * @param themeID - Property
	 * @param description - Property
	 * @param userFieldLabel1 - Property
	 * @param userFieldLabel2 - Property
	 * @param timeline - Property
	 * @param locationFolders - Property
	 * @param lastImport - Property
	 * @param lastSequenceNo - Property
	 * @param lastYearSequenceNo - Property
	 * @param lastBackup - Property
	 * @param lastBackupFolder - Property
	 * @param backupLocation - Property
	 * @param lastSessionEnd - Property
	 * @param thumbnailResolution - Property
	 * @param thumbnailFromPreview - Property
	 * @param lastSelection - Property
	 * @param lastCollection - Property
	 * @param pauseFolderWatch - Property
	 * @param folderWatchLatency - Property
	 * @param cleaned - Property
	 * @param readonly - Property
	 * @param autoWatch - Property
	 * @param sharpen - Property
	 * @param locale - Property
	 * @param platform - Property
	 * @param lastPicasaScan - Property
	 * @param picasaScannerVersion - Property
	 * @param cumulateImports - Property
	 * @param webpCompression - Property
	 * @param jpegQuality - Property
	 * @param noIndex - Property
	 * @param personsToKeywords - Property
	 */
	public Meta createMetaInstance(int version, int relevantLireVersion,
			Date creationDate, String owner, String themeID,
			String description, String userFieldLabel1, String userFieldLabel2,
			String timeline, String locationFolders, Date lastImport,
			int lastSequenceNo, int lastYearSequenceNo, Date lastBackup,
			String lastBackupFolder, String backupLocation,
			Date lastSessionEnd, String thumbnailResolution,
			boolean thumbnailFromPreview, String lastSelection,
			String lastCollection, boolean pauseFolderWatch,
			int folderWatchLatency, boolean cleaned, boolean readonly,
			boolean autoWatch, int sharpen, String locale, String platform,
			Date lastPicasaScan, int picasaScannerVersion,
			boolean cumulateImports, boolean webpCompression, int jpegQuality,
			boolean noIndex, Boolean personsToKeywords) {
		return new MetaImpl(version, relevantLireVersion, creationDate, owner,
				themeID, description, userFieldLabel1, userFieldLabel2,
				timeline, locationFolders, lastImport, lastSequenceNo,
				lastYearSequenceNo, lastBackup, lastBackupFolder,
				backupLocation, lastSessionEnd, thumbnailResolution,
				thumbnailFromPreview, lastSelection, lastCollection,
				pauseFolderWatch, folderWatchLatency, cleaned, readonly,
				autoWatch, sharpen, locale, platform, lastPicasaScan,
				picasaScannerVersion, cumulateImports, webpCompression,
				jpegQuality, noIndex, personsToKeywords);
	}

	/**
	 * Factory method for creating an instance of asset MigrationPolicy
	 *
	 * @param name - Property
	 * @param fileSeparatorPolicy - Property
	 * @param targetCatalog - Property
	 */
	public MigrationPolicy createMigrationPolicyInstance(String name,
			String fileSeparatorPolicy, String targetCatalog) {
		return new MigrationPolicyImpl(name, fileSeparatorPolicy, targetCatalog);
	}

	/**
	 * Factory method for creating an instance of asset MigrationRule
	 *
	 * @param sourcePattern - Property
	 * @param targetPattern - Property
	 * @param targetVolume - Property
	 */
	public MigrationRule createMigrationRuleInstance(String sourcePattern,
			String targetPattern, String targetVolume) {
		return new MigrationRuleImpl(sourcePattern, targetPattern, targetVolume);
	}

	/**
	 * Factory method for creating an instance of asset PageLayout
	 *
	 * @param name - Property
	 * @param type - Property
	 * @param title - Property
	 * @param subtitle - Property
	 * @param footer - Property
	 * @param size - Property
	 * @param columns - Property
	 * @param leftMargin - Property
	 * @param rightMargin - Property
	 * @param horizontalGap - Property
	 * @param topMargin - Property
	 * @param bottomMargin - Property
	 * @param verticalGap - Property
	 * @param caption1 - Property
	 * @param caption2 - Property
	 * @param alt - Property
	 * @param keyLine - Property
	 * @param landscape - Property
	 * @param facingPages - Property
	 * @param format - Property
	 * @param applySharpening - Property
	 * @param radius - Property
	 * @param amount - Property
	 * @param threshold - Property
	 * @param jpegQuality - Property
	 */
	public PageLayout createPageLayoutInstance(String name, int type,
			String title, String subtitle, String footer, int size,
			int columns, int leftMargin, int rightMargin, int horizontalGap,
			int topMargin, int bottomMargin, int verticalGap, String caption1,
			String caption2, String alt, int keyLine, boolean landscape,
			boolean facingPages, String format, boolean applySharpening,
			float radius, float amount, int threshold, int jpegQuality) {
		return new PageLayoutImpl(name, type, title, subtitle, footer, size,
				columns, leftMargin, rightMargin, horizontalGap, topMargin,
				bottomMargin, verticalGap, caption1, caption2, alt, keyLine,
				landscape, facingPages, format, applySharpening, radius,
				amount, threshold, jpegQuality);
	}

	/**
	 * Factory method for creating an instance of asset PostProcessor
	 *
	 */
	public PostProcessor createPostProcessorInstance() {
		return new PostProcessorImpl();
	}

	/**
	 * Factory method for creating an instance of asset Region
	 *
	 * @param keywordAdded - Property
	 * @param personEmailDigest - Property
	 * @param personLiveCID - Property
	 * @param description - Property
	 * @param type - Property
	 * @param album - Arc
	 */
	public Region createRegionInstance(boolean keywordAdded,
			String personEmailDigest, Long personLiveCID, String description,
			String type, String album) {
		return new RegionImpl(keywordAdded, personEmailDigest, personLiveCID,
				description, type, album);
	}

	/**
	 * Factory method for creating an instance of asset Report
	 *
	 * @param name - Property
	 * @param description - Property
	 * @param source - Property
	 * @param mode - Property
	 * @param sortField - Property
	 * @param descending - Property
	 * @param field - Property
	 * @param timeLower - Property
	 * @param timeUpper - Property
	 * @param valueLower - Property
	 * @param valueUpper - Property
	 * @param dayInterval - Property
	 * @param timeInterval - Property
	 * @param valueInterval - Property
	 * @param threshold - Property
	 * @param properties - Property
	 */
	public Report createReportInstance(String name, String description,
			String source, int mode, int sortField, boolean descending,
			String field, long timeLower, long timeUpper, long valueLower,
			long valueUpper, int dayInterval, int timeInterval,
			int valueInterval, float threshold, Object properties) {
		return new ReportImpl(name, description, source, mode, sortField,
				descending, field, timeLower, timeUpper, valueLower,
				valueUpper, dayInterval, timeInterval, valueInterval,
				threshold, properties);
	}

	/**
	 * Factory method for creating an instance of asset Rgb
	 *
	 * @param r - Property
	 * @param g - Property
	 * @param b - Property
	 */
	public Rgb createRgbInstance(int r, int g, int b) {
		return new RgbImpl(r, g, b);
	}

	/**
	 * Factory method for creating an instance of asset SimilarityOptions
	 *
	 * @param method - Property
	 * @param maxResults - Property
	 * @param minScore - Property
	 * @param lastTool - Property
	 * @param pencilRadius - Property
	 * @param airbrushRadius - Property
	 * @param airbrushIntensity - Property
	 * @param assetId - Property
	 * @param keywordWeight - Property
	 */
	public SimilarityOptions createSimilarityOptionsInstance(int method,
			int maxResults, float minScore, int lastTool, int pencilRadius,
			int airbrushRadius, int airbrushIntensity, String assetId,
			int keywordWeight) {
		return new SimilarityOptionsImpl(method, maxResults, minScore,
				lastTool, pencilRadius, airbrushRadius, airbrushIntensity,
				assetId, keywordWeight);
	}

	/**
	 * Factory method for creating an instance of asset Slide
	 *
	 * @param caption - Property
	 * @param sequenceNo - Property
	 * @param description - Property
	 * @param layout - Property
	 * @param delay - Property
	 * @param fadeIn - Property
	 * @param duration - Property
	 * @param fadeOut - Property
	 * @param effect - Property
	 * @param noVoice - Property
	 * @param asset - Arc
	 */
	public Slide createSlideInstance(String caption, int sequenceNo,
			String description, int layout, int delay, int fadeIn,
			int duration, int fadeOut, int effect, boolean noVoice, String asset) {
		return new SlideImpl(caption, sequenceNo, description, layout, delay,
				fadeIn, duration, fadeOut, effect, noVoice, asset);
	}

	/**
	 * Factory method for creating an instance of asset SlideShow
	 *
	 * @param name - Property
	 * @param description - Property
	 * @param fromPreview - Property
	 * @param duration - Property
	 * @param effect - Property
	 * @param fading - Property
	 * @param titleDisplay - Property
	 * @param titleContent - Property
	 * @param adhoc - Property
	 * @param skipDublettes - Property
	 * @param voiceNotes - Property
	 * @param lastAccessDate - Property
	 * @param perspective - Property
	 */
	public SlideShow createSlideShowInstance(String name, String description,
			boolean fromPreview, int duration, int effect, int fading,
			int titleDisplay, int titleContent, boolean adhoc,
			boolean skipDublettes, boolean voiceNotes, Date lastAccessDate,
			String perspective) {
		return new SlideShowImpl(name, description, fromPreview, duration,
				effect, fading, titleDisplay, titleContent, adhoc,
				skipDublettes, voiceNotes, lastAccessDate, perspective);
	}

	/**
	 * Factory method for creating an instance of asset SmartCollection
	 *
	 * @param name - Property
	 * @param system - Property
	 * @param album - Property
	 * @param adhoc - Property
	 * @param network - Property
	 * @param description - Property
	 * @param colorCode - Property
	 * @param lastAccessDate - Property
	 * @param generation - Property
	 * @param perspective - Property
	 * @param postProcessor - Arc
	 */
	public SmartCollection createSmartCollectionInstance(String name,
			boolean system, boolean album, boolean adhoc, boolean network,
			String description, int colorCode, Date lastAccessDate,
			int generation, String perspective, PostProcessor postProcessor) {
		return new SmartCollectionImpl(name, system, album, adhoc, network,
				description, colorCode, lastAccessDate, generation,
				perspective, postProcessor);
	}

	/**
	 * Factory method for creating an instance of asset SortCriterion
	 *
	 * @param field - Property
	 * @param subfield - Property
	 * @param descending - Property
	 */
	public SortCriterion createSortCriterionInstance(String field,
			String subfield, boolean descending) {
		return new SortCriterionImpl(field, subfield, descending);
	}

	/**
	 * Factory method for creating an instance of asset Storyboard
	 *
	 * @param title - Property
	 * @param sequenceNo - Property
	 * @param htmlDescription - Property
	 * @param description - Property
	 * @param imageSize - Property
	 * @param enlargeSmall - Property
	 * @param showCaptions - Property
	 * @param showDescriptions - Property
	 * @param showExif - Property
	 */
	public Storyboard createStoryboardInstance(String title, int sequenceNo,
			boolean htmlDescription, String description, int imageSize,
			boolean enlargeSmall, boolean showCaptions,
			boolean showDescriptions, boolean showExif) {
		return new StoryboardImpl(title, sequenceNo, htmlDescription,
				description, imageSize, enlargeSmall, showCaptions,
				showDescriptions, showExif);
	}

	/**
	 * Factory method for creating an instance of asset TextSearchOptions
	 *
	 * @param queryString - Property
	 * @param maxResults - Property
	 * @param minScore - Property
	 */
	public TextSearchOptions createTextSearchOptionsInstance(
			String queryString, int maxResults, float minScore) {
		return new TextSearchOptionsImpl(queryString, maxResults, minScore);
	}

	/**
	 * Factory method for creating an instance of asset TrackRecord
	 *
	 * @param type - Property
	 * @param serviceId - Property
	 * @param serviceName - Property
	 * @param target - Property
	 * @param derivative - Property
	 * @param exportDate - Property
	 * @param replaced - Property
	 * @param visit - Property
	 */
	public TrackRecord createTrackRecordInstance(String type, String serviceId,
			String serviceName, String target, String derivative,
			Date exportDate, boolean replaced, String visit) {
		return new TrackRecordImpl(type, serviceId, serviceName, target,
				derivative, exportDate, replaced, visit);
	}

	/**
	 * Factory method for creating an instance of asset Wall
	 *
	 * @param location - Property
	 * @param x - Property
	 * @param y - Property
	 * @param width - Property
	 * @param height - Property
	 * @param gX - Property
	 * @param gY - Property
	 * @param gAngle - Property
	 * @param color - Property
	 */
	public Wall createWallInstance(String location, int x, int y, int width,
			int height, int gX, int gY, double gAngle, Rgb_type color) {
		return new WallImpl(location, x, y, width, height, gX, gY, gAngle,
				color);
	}

	/**
	 * Factory method for creating an instance of asset WatchedFolder
	 *
	 * @param uri - Property
	 * @param volume - Property
	 * @param lastObservation - Property
	 * @param recursive - Property
	 * @param filters - Property
	 * @param transfer - Property
	 * @param artist - Property
	 * @param skipDuplicates - Property
	 * @param skipPolicy - Property
	 * @param targetDir - Property
	 * @param subfolderPolicy - Property
	 * @param selectedTemplate - Property
	 * @param cue - Property
	 * @param fileSource - Property
	 */
	public WatchedFolder createWatchedFolderInstance(String uri, String volume,
			long lastObservation, boolean recursive, String filters,
			boolean transfer, String artist, boolean skipDuplicates,
			int skipPolicy, String targetDir, int subfolderPolicy,
			String selectedTemplate, String cue, int fileSource) {
		return new WatchedFolderImpl(uri, volume, lastObservation, recursive,
				filters, transfer, artist, skipDuplicates, skipPolicy,
				targetDir, subfolderPolicy, selectedTemplate, cue, fileSource);
	}

	/**
	 * Factory method for creating an instance of asset WebExhibit
	 *
	 * @param caption - Property
	 * @param sequenceNo - Property
	 * @param description - Property
	 * @param htmlDescription - Property
	 * @param altText - Property
	 * @param downloadable - Property
	 * @param includeMetadata - Property
	 * @param asset - Arc
	 */
	public WebExhibit createWebExhibitInstance(String caption, int sequenceNo,
			String description, boolean htmlDescription, String altText,
			boolean downloadable, boolean includeMetadata, String asset) {
		return new WebExhibitImpl(caption, sequenceNo, description,
				htmlDescription, altText, downloadable, includeMetadata, asset);
	}

	/**
	 * Factory method for creating an instance of asset WebGallery
	 *
	 * @param template - Property
	 * @param name - Property
	 * @param logo - Property
	 * @param htmlDescription - Property
	 * @param description - Property
	 * @param hideHeader - Property
	 * @param opacity - Property
	 * @param padding - Property
	 * @param thumbSize - Property
	 * @param downloadText - Property
	 * @param hideDownload - Property
	 * @param copyright - Property
	 * @param addWatermark - Property
	 * @param showMeta - Property
	 * @param contactName - Property
	 * @param email - Property
	 * @param webUrl - Property
	 * @param hideFooter - Property
	 * @param bgImage - Property
	 * @param bgRepeat - Property
	 * @param bgColor - Property
	 * @param shadeColor - Property
	 * @param borderColor - Property
	 * @param linkColor - Property
	 * @param titleFont - Property
	 * @param sectionFont - Property
	 * @param captionFont - Property
	 * @param descriptionFont - Property
	 * @param footerFont - Property
	 * @param controlsFont - Property
	 * @param selectedEngine - Property
	 * @param outputFolder - Property
	 * @param ftpDir - Property
	 * @param isFtp - Property
	 * @param pageName - Property
	 * @param poweredByText - Property
	 * @param applySharpening - Property
	 * @param radius - Property
	 * @param amount - Property
	 * @param threshold - Property
	 * @param headHtml - Property
	 * @param topHtml - Property
	 * @param footerHtml - Property
	 * @param jpegQuality - Property
	 * @param scalingMethod - Property
	 * @param lastAccessDate - Property
	 * @param perspective - Property
	 */
	public WebGallery createWebGalleryInstance(boolean template, String name,
			String logo, boolean htmlDescription, String description,
			boolean hideHeader, int opacity, int padding, int thumbSize,
			String downloadText, boolean hideDownload, String copyright,
			boolean addWatermark, boolean showMeta, String contactName,
			String email, String webUrl, boolean hideFooter, String bgImage,
			boolean bgRepeat, Rgb_type bgColor, Rgb_type shadeColor,
			Rgb_type borderColor, Rgb_type linkColor, Font_type titleFont,
			Font_type sectionFont, Font_type captionFont,
			Font_type descriptionFont, Font_type footerFont,
			Font_type controlsFont, String selectedEngine, String outputFolder,
			String ftpDir, boolean isFtp, String pageName,
			String poweredByText, boolean applySharpening, float radius,
			float amount, int threshold, String headHtml, String topHtml,
			String footerHtml, int jpegQuality, int scalingMethod,
			Date lastAccessDate, String perspective) {
		return new WebGalleryImpl(template, name, logo, htmlDescription,
				description, hideHeader, opacity, padding, thumbSize,
				downloadText, hideDownload, copyright, addWatermark, showMeta,
				contactName, email, webUrl, hideFooter, bgImage, bgRepeat,
				bgColor, shadeColor, borderColor, linkColor, titleFont,
				sectionFont, captionFont, descriptionFont, footerFont,
				controlsFont, selectedEngine, outputFolder, ftpDir, isFtp,
				pageName, poweredByText, applySharpening, radius, amount,
				threshold, headHtml, topHtml, footerHtml, jpegQuality,
				scalingMethod, lastAccessDate, perspective);
	}

	/**
	 * Factory method for creating an instance of asset WebParameter
	 *
	 * @param id - Property
	 * @param value - Property
	 * @param encodeHtml - Property
	 * @param linkTo - Property
	 */
	public WebParameter createWebParameterInstance(String id, Object value,
			boolean encodeHtml, String linkTo) {
		return new WebParameterImpl(id, value, encodeHtml, linkTo);
	}

	// <--- End of KLEEN maintained section 
	/*--com.bdaum.aoModeling.generator.java/instrumentation*/

	/*
	 * Connects all aspect classes with data model classes
	 */
	public static void attachInstrumentation() {
		attachMainSecurityInstrumentation();
		attachCustomInstrumentation1();
		attachMainLoggingInstrumentation();
		attachMainOperationInstrumentation();
		attachMainCustomInstrumentation();
		attachMainConstraintInstrumentation();
		attachMainGuiInstrumentation();
		attachCustomInstrumentation2();
	}

	/**
	 * Connects custom aspect classes with data model classes
	 */
	private static boolean customInstrumentation1Set = false;

	private static void attachCustomInstrumentation1() {
		if (customInstrumentation1Set)
			return;
		customInstrumentation1Set = true;
		//TODO Add custom aspects here (before default aspects)
	}

	/**
	 * Connects custom aspect classes with data model classes
	 */
	private static boolean customInstrumentation2Set = false;

	private static void attachCustomInstrumentation2() {
		if (customInstrumentation2Set)
			return;
		customInstrumentation2Set = true;
		//TODO Add custom aspects here (after default aspects)
	}

	/**
	 * Main method for creating and running the model
	 */
	public static void main(String[] args) {
		Main model = getInstance();
		model.setSerializableId(getNewObjectID());
		model.setParms(args);
		model.run();
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IModel#setParms(String[])
	 */
	public void setParms(String[] args) {
		parms = args;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IModel#getParms()
	 */
	public String[] getParms() {
		return parms;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IModel#setGlobalEventing(boolean)
	 */
	public void setGlobalEventing(boolean globalEventing) {
		this.globalEventing = globalEventing;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IModel#getGlobalEventing()
	 */
	public boolean getGlobalEventing() {
		return globalEventing;
	}

	/* (non-Javadoc)
	 * @see com.bdaum.aoModeling.runtime.IModel#run()
	 */
	public void run() {
		//TODO Complete Main.run()
	}

}
