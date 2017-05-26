package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset exhibition
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Exhibition_typeImpl extends AomObject implements Exhibition_type {

	static final long serialVersionUID = -3525603572L;

	/* ----- Constructors ----- */

	public Exhibition_typeImpl() {
		super();
	}

	/**
	 * Constructor
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
	public Exhibition_typeImpl(String name, String description, String info,
			int defaultViewingHeight, int variance, int gridSize,
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
		super();
		this.name = name;
		this.description = description;
		this.info = info;
		this.defaultViewingHeight = defaultViewingHeight;
		this.variance = variance;
		this.gridSize = gridSize;
		this.showGrid = showGrid;
		this.snapToGrid = snapToGrid;
		this.defaultDescription = defaultDescription;
		this.labelFontFamily = labelFontFamily;
		this.labelFontSize = labelFontSize;
		this.labelSequence = labelSequence;
		this.hideLabel = hideLabel;
		this.labelAlignment = labelAlignment;
		this.labelDistance = labelDistance;
		this.labelIndent = labelIndent;
		this.startX = startX;
		this.startY = startY;
		this.matWidth = matWidth;
		this.matColor = matColor;
		this.frameWidth = frameWidth;
		this.frameColor = frameColor;
		this.groundColor = groundColor;
		this.horizonColor = horizonColor;
		this.ceilingColor = ceilingColor;
		this.audio = audio;
		this.outputFolder = outputFolder;
		this.ftpDir = ftpDir;
		this.isFtp = isFtp;
		this.pageName = pageName;
		this.applySharpening = applySharpening;
		this.radius = radius;
		this.amount = amount;
		this.threshold = threshold;
		this.addWatermark = addWatermark;
		this.contactName = contactName;
		this.email = email;
		this.webUrl = webUrl;
		this.copyright = copyright;
		this.logo = logo;
		this.infoPlatePosition = infoPlatePosition;
		this.hideCredits = hideCredits;
		this.jpegQuality = jpegQuality;
		this.scalingMethod = scalingMethod;
		this.lastAccessDate = lastAccessDate;
		this.perspective = perspective;

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

	/* *** Property description *** */

	private String description = AomConstants.INIT_String;

	/**
	 * Set value of property description
	 *
	 * @param _value - new field value
	 */
	public void setDescription(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "description"));
		description = _value;
	}

	/**
	 * Get value of property description
	 *
	 * @return - value of field description
	 */
	public String getDescription() {
		return description;
	}

	/* *** Property info *** */

	private String info;

	/**
	 * Set value of property info
	 *
	 * @param _value - new field value
	 */
	public void setInfo(String _value) {
		info = _value;
	}

	/**
	 * Get value of property info
	 *
	 * @return - value of field info
	 */
	public String getInfo() {
		return info;
	}

	/* *** Property defaultViewingHeight(unit=mm) *** */

	public static final String defaultViewingHeight__unit = "mm";

	private int defaultViewingHeight;

	/**
	 * Set value of property defaultViewingHeight
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setDefaultViewingHeight(int _value) {
		defaultViewingHeight = _value;
	}

	/**
	 * Get value of property defaultViewingHeight
	 *
	 * @return - value of field defaultViewingHeight(unit=mm)
	 */
	public int getDefaultViewingHeight() {
		return defaultViewingHeight;
	}

	/* *** Property variance(unit=mm) *** */

	public static final String variance__unit = "mm";

	private int variance;

	/**
	 * Set value of property variance
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setVariance(int _value) {
		variance = _value;
	}

	/**
	 * Get value of property variance
	 *
	 * @return - value of field variance(unit=mm)
	 */
	public int getVariance() {
		return variance;
	}

	/* *** Property gridSize(unit=mm) *** */

	public static final String gridSize__unit = "mm";

	private int gridSize;

	/**
	 * Set value of property gridSize
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setGridSize(int _value) {
		gridSize = _value;
	}

	/**
	 * Get value of property gridSize
	 *
	 * @return - value of field gridSize(unit=mm)
	 */
	public int getGridSize() {
		return gridSize;
	}

	/* *** Property showGrid *** */

	private boolean showGrid;

	/**
	 * Set value of property showGrid
	 *
	 * @param _value - new field value
	 */
	public void setShowGrid(boolean _value) {
		showGrid = _value;
	}

	/**
	 * Get value of property showGrid
	 *
	 * @return - value of field showGrid
	 */
	public boolean getShowGrid() {
		return showGrid;
	}

	/* *** Property snapToGrid *** */

	private boolean snapToGrid;

	/**
	 * Set value of property snapToGrid
	 *
	 * @param _value - new field value
	 */
	public void setSnapToGrid(boolean _value) {
		snapToGrid = _value;
	}

	/**
	 * Get value of property snapToGrid
	 *
	 * @return - value of field snapToGrid
	 */
	public boolean getSnapToGrid() {
		return snapToGrid;
	}

	/* *** Property defaultDescription *** */

	private String defaultDescription;

	/**
	 * Set value of property defaultDescription
	 *
	 * @param _value - new field value
	 */
	public void setDefaultDescription(String _value) {
		defaultDescription = _value;
	}

	/**
	 * Get value of property defaultDescription
	 *
	 * @return - value of field defaultDescription
	 */
	public String getDefaultDescription() {
		return defaultDescription;
	}

	/* *** Property labelFontFamily *** */

	private String labelFontFamily = AomConstants.INIT_String;

	/**
	 * Set value of property labelFontFamily
	 *
	 * @param _value - new field value
	 */
	public void setLabelFontFamily(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "labelFontFamily"));
		labelFontFamily = _value;
	}

	/**
	 * Get value of property labelFontFamily
	 *
	 * @return - value of field labelFontFamily
	 */
	public String getLabelFontFamily() {
		return labelFontFamily;
	}

	/* *** Property labelFontSize *** */

	private int labelFontSize;

	/**
	 * Set value of property labelFontSize
	 *
	 * @param _value - new field value
	 */
	public void setLabelFontSize(int _value) {
		labelFontSize = _value;
	}

	/**
	 * Get value of property labelFontSize
	 *
	 * @return - value of field labelFontSize
	 */
	public int getLabelFontSize() {
		return labelFontSize;
	}

	/* *** Property labelSequence *** */

	private int labelSequence;

	/**
	 * Set value of property labelSequence
	 *
	 * @param _value - new field value
	 */
	public void setLabelSequence(int _value) {
		labelSequence = _value;
	}

	/**
	 * Get value of property labelSequence
	 *
	 * @return - value of field labelSequence
	 */
	public int getLabelSequence() {
		return labelSequence;
	}

	/* *** Property hideLabel *** */

	private boolean hideLabel;

	/**
	 * Set value of property hideLabel
	 *
	 * @param _value - new field value
	 */
	public void setHideLabel(boolean _value) {
		hideLabel = _value;
	}

	/**
	 * Get value of property hideLabel
	 *
	 * @return - value of field hideLabel
	 */
	public boolean getHideLabel() {
		return hideLabel;
	}

	/* *** Property labelAlignment *** */

	private Integer labelAlignment;

	/**
	 * Set value of property labelAlignment
	 *
	 * @param _value - new field value
	 */
	public void setLabelAlignment(Integer _value) {
		labelAlignment = _value;
	}

	/**
	 * Get value of property labelAlignment
	 *
	 * @return - value of field labelAlignment
	 */
	public Integer getLabelAlignment() {
		return labelAlignment;
	}

	/* *** Property labelDistance(unit=mm) *** */

	public static final String labelDistance__unit = "mm";

	private Integer labelDistance;

	/**
	 * Set value of property labelDistance
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setLabelDistance(Integer _value) {
		labelDistance = _value;
	}

	/**
	 * Get value of property labelDistance
	 *
	 * @return - value of field labelDistance(unit=mm)
	 */
	public Integer getLabelDistance() {
		return labelDistance;
	}

	/* *** Property labelIndent(unit=mm) *** */

	public static final String labelIndent__unit = "mm";

	private Integer labelIndent;

	/**
	 * Set value of property labelIndent
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setLabelIndent(Integer _value) {
		labelIndent = _value;
	}

	/**
	 * Get value of property labelIndent
	 *
	 * @return - value of field labelIndent(unit=mm)
	 */
	public Integer getLabelIndent() {
		return labelIndent;
	}

	/* *** Property startX(unit=mm) *** */

	public static final String startX__unit = "mm";

	private int startX;

	/**
	 * Set value of property startX
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setStartX(int _value) {
		startX = _value;
	}

	/**
	 * Get value of property startX
	 *
	 * @return - value of field startX(unit=mm)
	 */
	public int getStartX() {
		return startX;
	}

	/* *** Property startY(unit=mm) *** */

	public static final String startY__unit = "mm";

	private int startY;

	/**
	 * Set value of property startY
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setStartY(int _value) {
		startY = _value;
	}

	/**
	 * Get value of property startY
	 *
	 * @return - value of field startY(unit=mm)
	 */
	public int getStartY() {
		return startY;
	}

	/* *** Property matWidth(unit=mm) *** */

	public static final String matWidth__unit = "mm";

	private int matWidth;

	/**
	 * Set value of property matWidth
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setMatWidth(int _value) {
		matWidth = _value;
	}

	/**
	 * Get value of property matWidth
	 *
	 * @return - value of field matWidth(unit=mm)
	 */
	public int getMatWidth() {
		return matWidth;
	}

	/* *** Property matColor *** */

	private Rgb_type matColor;

	/**
	 * Set value of property matColor
	 *
	 * @param _value - new field value
	 */
	public void setMatColor(Rgb_type _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "matColor"));
		matColor = _value;
	}

	/**
	 * Get value of property matColor
	 *
	 * @return - value of field matColor
	 */
	public Rgb_type getMatColor() {
		return matColor;
	}

	/* *** Property frameWidth(unit=mm) *** */

	public static final String frameWidth__unit = "mm";

	private int frameWidth;

	/**
	 * Set value of property frameWidth
	 *
	 * @param _value - new field value(unit=mm)
	 */
	public void setFrameWidth(int _value) {
		frameWidth = _value;
	}

	/**
	 * Get value of property frameWidth
	 *
	 * @return - value of field frameWidth(unit=mm)
	 */
	public int getFrameWidth() {
		return frameWidth;
	}

	/* *** Property frameColor *** */

	private Rgb_type frameColor;

	/**
	 * Set value of property frameColor
	 *
	 * @param _value - new field value
	 */
	public void setFrameColor(Rgb_type _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "frameColor"));
		frameColor = _value;
	}

	/**
	 * Get value of property frameColor
	 *
	 * @return - value of field frameColor
	 */
	public Rgb_type getFrameColor() {
		return frameColor;
	}

	/* *** Property groundColor *** */

	private Rgb_type groundColor;

	/**
	 * Set value of property groundColor
	 *
	 * @param _value - new field value
	 */
	public void setGroundColor(Rgb_type _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "groundColor"));
		groundColor = _value;
	}

	/**
	 * Get value of property groundColor
	 *
	 * @return - value of field groundColor
	 */
	public Rgb_type getGroundColor() {
		return groundColor;
	}

	/* *** Property horizonColor *** */

	private Rgb_type horizonColor;

	/**
	 * Set value of property horizonColor
	 *
	 * @param _value - new field value
	 */
	public void setHorizonColor(Rgb_type _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "horizonColor"));
		horizonColor = _value;
	}

	/**
	 * Get value of property horizonColor
	 *
	 * @return - value of field horizonColor
	 */
	public Rgb_type getHorizonColor() {
		return horizonColor;
	}

	/* *** Property ceilingColor *** */

	private Rgb_type ceilingColor;

	/**
	 * Set value of property ceilingColor
	 *
	 * @param _value - new field value
	 */
	public void setCeilingColor(Rgb_type _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "ceilingColor"));
		ceilingColor = _value;
	}

	/**
	 * Get value of property ceilingColor
	 *
	 * @return - value of field ceilingColor
	 */
	public Rgb_type getCeilingColor() {
		return ceilingColor;
	}

	/* *** Property audio *** */

	private String audio = AomConstants.INIT_String;

	/**
	 * Set value of property audio
	 *
	 * @param _value - new field value
	 */
	public void setAudio(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "audio"));
		audio = _value;
	}

	/**
	 * Get value of property audio
	 *
	 * @return - value of field audio
	 */
	public String getAudio() {
		return audio;
	}

	/* *** Property outputFolder *** */

	private String outputFolder;

	/**
	 * Set value of property outputFolder
	 *
	 * @param _value - new field value
	 */
	public void setOutputFolder(String _value) {
		outputFolder = _value;
	}

	/**
	 * Get value of property outputFolder
	 *
	 * @return - value of field outputFolder
	 */
	public String getOutputFolder() {
		return outputFolder;
	}

	/* *** Property ftpDir *** */

	private String ftpDir;

	/**
	 * Set value of property ftpDir
	 *
	 * @param _value - new field value
	 */
	public void setFtpDir(String _value) {
		ftpDir = _value;
	}

	/**
	 * Get value of property ftpDir
	 *
	 * @return - value of field ftpDir
	 */
	public String getFtpDir() {
		return ftpDir;
	}

	/* *** Property isFtp *** */

	private boolean isFtp;

	/**
	 * Set value of property isFtp
	 *
	 * @param _value - new field value
	 */
	public void setIsFtp(boolean _value) {
		isFtp = _value;
	}

	/**
	 * Get value of property isFtp
	 *
	 * @return - value of field isFtp
	 */
	public boolean getIsFtp() {
		return isFtp;
	}

	/* *** Property pageName *** */

	private String pageName;

	/**
	 * Set value of property pageName
	 *
	 * @param _value - new field value
	 */
	public void setPageName(String _value) {
		pageName = _value;
	}

	/**
	 * Get value of property pageName
	 *
	 * @return - value of field pageName
	 */
	public String getPageName() {
		return pageName;
	}

	/* *** Property applySharpening *** */

	private Boolean applySharpening;

	/**
	 * Set value of property applySharpening
	 *
	 * @param _value - new field value
	 */
	public void setApplySharpening(Boolean _value) {
		applySharpening = _value;
	}

	/**
	 * Get value of property applySharpening
	 *
	 * @return - value of field applySharpening
	 */
	public Boolean getApplySharpening() {
		return applySharpening;
	}

	/* *** Property radius *** */

	private float radius;

	/**
	 * Set value of property radius
	 *
	 * @param _value - new field value
	 */
	public void setRadius(float _value) {
		radius = _value;
	}

	/**
	 * Get value of property radius
	 *
	 * @return - value of field radius
	 */
	public float getRadius() {
		return radius;
	}

	/* *** Property amount *** */

	private float amount;

	/**
	 * Set value of property amount
	 *
	 * @param _value - new field value
	 */
	public void setAmount(float _value) {
		amount = _value;
	}

	/**
	 * Get value of property amount
	 *
	 * @return - value of field amount
	 */
	public float getAmount() {
		return amount;
	}

	/* *** Property threshold *** */

	private int threshold;

	/**
	 * Set value of property threshold
	 *
	 * @param _value - new field value
	 */
	public void setThreshold(int _value) {
		threshold = _value;
	}

	/**
	 * Get value of property threshold
	 *
	 * @return - value of field threshold
	 */
	public int getThreshold() {
		return threshold;
	}

	/* *** Property addWatermark *** */

	private boolean addWatermark;

	/**
	 * Set value of property addWatermark
	 *
	 * @param _value - new field value
	 */
	public void setAddWatermark(boolean _value) {
		addWatermark = _value;
	}

	/**
	 * Get value of property addWatermark
	 *
	 * @return - value of field addWatermark
	 */
	public boolean getAddWatermark() {
		return addWatermark;
	}

	/* *** Property contactName *** */

	private String contactName;

	/**
	 * Set value of property contactName
	 *
	 * @param _value - new field value
	 */
	public void setContactName(String _value) {
		contactName = _value;
	}

	/**
	 * Get value of property contactName
	 *
	 * @return - value of field contactName
	 */
	public String getContactName() {
		return contactName;
	}

	/* *** Property email *** */

	private String email;

	/**
	 * Set value of property email
	 *
	 * @param _value - new field value
	 */
	public void setEmail(String _value) {
		email = _value;
	}

	/**
	 * Get value of property email
	 *
	 * @return - value of field email
	 */
	public String getEmail() {
		return email;
	}

	/* *** Property webUrl *** */

	private String webUrl;

	/**
	 * Set value of property webUrl
	 *
	 * @param _value - new field value
	 */
	public void setWebUrl(String _value) {
		webUrl = _value;
	}

	/**
	 * Get value of property webUrl
	 *
	 * @return - value of field webUrl
	 */
	public String getWebUrl() {
		return webUrl;
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

	/* *** Property logo *** */

	private String logo;

	/**
	 * Set value of property logo
	 *
	 * @param _value - new field value
	 */
	public void setLogo(String _value) {
		logo = _value;
	}

	/**
	 * Get value of property logo
	 *
	 * @return - value of field logo
	 */
	public String getLogo() {
		return logo;
	}

	/* *** Property keyword *** */

	private AomList<String> keyword = new FastArrayList<String>("keyword",
			PackageInterface.Exhibition_keyword, 0, Integer.MAX_VALUE, null,
			null);

	/**
	 * Set value of property keyword
	 *
	 * @param _value - new element value
	 */
	public void setKeyword(AomList<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "keyword"));
		keyword = _value;
	}

	/**
	 * Set value of property keyword
	 *
	 * @param _value - new element value
	 */
	public void setKeyword(Collection<String> _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "keyword"));
		keyword = new FastArrayList<String>(_value, "keyword",
				PackageInterface.Exhibition_keyword, 0, Integer.MAX_VALUE,
				null, null);
	}

	/**
	 * Set single element of list keyword
	 *
	 * @param _element - new element value
	 * @param _i - index of list element
	 */
	public void setKeyword(String _element, int _i) {
		keyword.set(_i, _element);
	}

	/**
	 * Add an element to list keyword
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addKeyword(String _element) {
		if (_element == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "Keyword._element"));

		return keyword.add(_element);
	}

	/**
	 * Remove an element from list keyword
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeKeyword(String _element) {
		return keyword.remove(_element);
	}

	/**
	 * Make keyword empty 
	 */
	public void clearKeyword() {
		keyword.clear();
	}

	/**
	 * Get value of property keyword
	 *
	 * @return - value of field keyword
	 */
	public AomList<String> getKeyword() {
		return keyword;
	}

	/**
	 * Get single element of list keyword
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list keyword
	 */
	public String getKeyword(int _i) {
		return keyword.get(_i);
	}

	/* *** Property infoPlatePosition *** */

	private int infoPlatePosition;

	/**
	 * Set value of property infoPlatePosition
	 *
	 * @param _value - new field value
	 */
	public void setInfoPlatePosition(int _value) {
		infoPlatePosition = _value;
	}

	/**
	 * Get value of property infoPlatePosition
	 *
	 * @return - value of field infoPlatePosition
	 */
	public int getInfoPlatePosition() {
		return infoPlatePosition;
	}

	/* *** Property hideCredits *** */

	private boolean hideCredits;

	/**
	 * Set value of property hideCredits
	 *
	 * @param _value - new field value
	 */
	public void setHideCredits(boolean _value) {
		hideCredits = _value;
	}

	/**
	 * Get value of property hideCredits
	 *
	 * @return - value of field hideCredits
	 */
	public boolean getHideCredits() {
		return hideCredits;
	}

	/* *** Property jpegQuality *** */

	private int jpegQuality;

	/**
	 * Set value of property jpegQuality
	 *
	 * @param _value - new field value
	 */
	public void setJpegQuality(int _value) {
		jpegQuality = _value;
	}

	/**
	 * Get value of property jpegQuality
	 *
	 * @return - value of field jpegQuality
	 */
	public int getJpegQuality() {
		return jpegQuality;
	}

	/* *** Property scalingMethod *** */

	private int scalingMethod;

	/**
	 * Set value of property scalingMethod
	 *
	 * @param _value - new field value
	 */
	public void setScalingMethod(int _value) {
		scalingMethod = _value;
	}

	/**
	 * Get value of property scalingMethod
	 *
	 * @return - value of field scalingMethod
	 */
	public int getScalingMethod() {
		return scalingMethod;
	}

	/* *** Property lastAccessDate *** */

	private Date lastAccessDate = new Date();

	/**
	 * Set value of property lastAccessDate
	 *
	 * @param _value - new field value
	 */
	public void setLastAccessDate(Date _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastAccessDate"));
		lastAccessDate = _value;
	}

	/**
	 * Get value of property lastAccessDate
	 *
	 * @return - value of field lastAccessDate
	 */
	public Date getLastAccessDate() {
		return lastAccessDate;
	}

	/* *** Property perspective *** */

	private String perspective;

	/**
	 * Set value of property perspective
	 *
	 * @param _value - new field value
	 */
	public void setPerspective(String _value) {
		perspective = _value;
	}

	/**
	 * Get value of property perspective
	 *
	 * @return - value of field perspective
	 */
	public String getPerspective() {
		return perspective;
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

		if (!(o instanceof Exhibition_type) || !super.equals(o))
			return false;
		Exhibition_type other = (Exhibition_type) o;
		return ((getName() == null && other.getName() == null) || (getName() != null && getName()
				.equals(other.getName())))

				&& ((getDescription() == null && other.getDescription() == null) || (getDescription() != null && getDescription()
						.equals(other.getDescription())))

				&& ((getInfo() == null && other.getInfo() == null) || (getInfo() != null && getInfo()
						.equals(other.getInfo())))

				&& getDefaultViewingHeight() == other.getDefaultViewingHeight()

				&& getVariance() == other.getVariance()

				&& getGridSize() == other.getGridSize()

				&& getShowGrid() == other.getShowGrid()

				&& getSnapToGrid() == other.getSnapToGrid()

				&& ((getDefaultDescription() == null && other
						.getDefaultDescription() == null) || (getDefaultDescription() != null && getDefaultDescription()
						.equals(other.getDefaultDescription())))

				&& ((getLabelFontFamily() == null && other.getLabelFontFamily() == null) || (getLabelFontFamily() != null && getLabelFontFamily()
						.equals(other.getLabelFontFamily())))

				&& getLabelFontSize() == other.getLabelFontSize()

				&& getLabelSequence() == other.getLabelSequence()

				&& getHideLabel() == other.getHideLabel()

				&& ((getLabelAlignment() == null && other.getLabelAlignment() == null) || (getLabelAlignment() != null && getLabelAlignment()
						.equals(other.getLabelAlignment())))

				&& ((getLabelDistance() == null && other.getLabelDistance() == null) || (getLabelDistance() != null && getLabelDistance()
						.equals(other.getLabelDistance())))

				&& ((getLabelIndent() == null && other.getLabelIndent() == null) || (getLabelIndent() != null && getLabelIndent()
						.equals(other.getLabelIndent())))

				&& getStartX() == other.getStartX()

				&& getStartY() == other.getStartY()

				&& getMatWidth() == other.getMatWidth()

				&& ((getMatColor() == null && other.getMatColor() == null) || (getMatColor() != null && getMatColor()
						.equals(other.getMatColor())))

				&& getFrameWidth() == other.getFrameWidth()

				&& ((getFrameColor() == null && other.getFrameColor() == null) || (getFrameColor() != null && getFrameColor()
						.equals(other.getFrameColor())))

				&& ((getGroundColor() == null && other.getGroundColor() == null) || (getGroundColor() != null && getGroundColor()
						.equals(other.getGroundColor())))

				&& ((getHorizonColor() == null && other.getHorizonColor() == null) || (getHorizonColor() != null && getHorizonColor()
						.equals(other.getHorizonColor())))

				&& ((getCeilingColor() == null && other.getCeilingColor() == null) || (getCeilingColor() != null && getCeilingColor()
						.equals(other.getCeilingColor())))

				&& ((getAudio() == null && other.getAudio() == null) || (getAudio() != null && getAudio()
						.equals(other.getAudio())))

				&& ((getOutputFolder() == null && other.getOutputFolder() == null) || (getOutputFolder() != null && getOutputFolder()
						.equals(other.getOutputFolder())))

				&& ((getFtpDir() == null && other.getFtpDir() == null) || (getFtpDir() != null && getFtpDir()
						.equals(other.getFtpDir())))

				&& getIsFtp() == other.getIsFtp()

				&& ((getPageName() == null && other.getPageName() == null) || (getPageName() != null && getPageName()
						.equals(other.getPageName())))

				&& ((getApplySharpening() == null && other.getApplySharpening() == null) || (getApplySharpening() != null && getApplySharpening()
						.equals(other.getApplySharpening())))

				&& getRadius() == other.getRadius()

				&& getAmount() == other.getAmount()

				&& getThreshold() == other.getThreshold()

				&& getAddWatermark() == other.getAddWatermark()

				&& ((getContactName() == null && other.getContactName() == null) || (getContactName() != null && getContactName()
						.equals(other.getContactName())))

				&& ((getEmail() == null && other.getEmail() == null) || (getEmail() != null && getEmail()
						.equals(other.getEmail())))

				&& ((getWebUrl() == null && other.getWebUrl() == null) || (getWebUrl() != null && getWebUrl()
						.equals(other.getWebUrl())))

				&& ((getCopyright() == null && other.getCopyright() == null) || (getCopyright() != null && getCopyright()
						.equals(other.getCopyright())))

				&& ((getLogo() == null && other.getLogo() == null) || (getLogo() != null && getLogo()
						.equals(other.getLogo())))

				&& ((getKeyword() == null && other.getKeyword() == null) || (getKeyword() != null && getKeyword()
						.equals(other.getKeyword())))

				&& getInfoPlatePosition() == other.getInfoPlatePosition()

				&& getHideCredits() == other.getHideCredits()

				&& getJpegQuality() == other.getJpegQuality()

				&& getScalingMethod() == other.getScalingMethod()

				&& ((getLastAccessDate() == null && other.getLastAccessDate() == null) || (getLastAccessDate() != null && getLastAccessDate()
						.equals(other.getLastAccessDate())))

				&& ((getPerspective() == null && other.getPerspective() == null) || (getPerspective() != null && getPerspective()
						.equals(other.getPerspective())))

		;
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

		int hashCode = 1036477558 + ((getName() == null) ? 0 : getName()
				.hashCode());

		hashCode = 31
				* hashCode
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());

		hashCode = 31 * hashCode
				+ ((getInfo() == null) ? 0 : getInfo().hashCode());

		hashCode = 31 * hashCode + getDefaultViewingHeight();

		hashCode = 31 * hashCode + getVariance();

		hashCode = 31 * hashCode + getGridSize();

		hashCode = 31 * hashCode + (getShowGrid() ? 1231 : 1237);

		hashCode = 31 * hashCode + (getSnapToGrid() ? 1231 : 1237);

		hashCode = 31
				* hashCode
				+ ((getDefaultDescription() == null) ? 0
						: getDefaultDescription().hashCode());

		hashCode = 31
				* hashCode
				+ ((getLabelFontFamily() == null) ? 0 : getLabelFontFamily()
						.hashCode());

		hashCode = 31 * hashCode + getLabelFontSize();

		hashCode = 31 * hashCode + getLabelSequence();

		hashCode = 31 * hashCode + (getHideLabel() ? 1231 : 1237);

		hashCode = 31
				* hashCode
				+ ((getLabelAlignment() == null) ? 0 : getLabelAlignment()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getLabelDistance() == null) ? 0 : getLabelDistance()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getLabelIndent() == null) ? 0 : getLabelIndent().hashCode());

		hashCode = 31 * hashCode + getStartX();

		hashCode = 31 * hashCode + getStartY();

		hashCode = 31 * hashCode + getMatWidth();

		hashCode = 31 * hashCode
				+ ((getMatColor() == null) ? 0 : getMatColor().hashCode());

		hashCode = 31 * hashCode + getFrameWidth();

		hashCode = 31 * hashCode
				+ ((getFrameColor() == null) ? 0 : getFrameColor().hashCode());

		hashCode = 31
				* hashCode
				+ ((getGroundColor() == null) ? 0 : getGroundColor().hashCode());

		hashCode = 31
				* hashCode
				+ ((getHorizonColor() == null) ? 0 : getHorizonColor()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getCeilingColor() == null) ? 0 : getCeilingColor()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getAudio() == null) ? 0 : getAudio().hashCode());

		hashCode = 31
				* hashCode
				+ ((getOutputFolder() == null) ? 0 : getOutputFolder()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getFtpDir() == null) ? 0 : getFtpDir().hashCode());

		hashCode = 31 * hashCode + (getIsFtp() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getPageName() == null) ? 0 : getPageName().hashCode());

		hashCode = 31
				* hashCode
				+ ((getApplySharpening() == null) ? 0 : getApplySharpening()
						.hashCode());

		hashCode = 31 * hashCode + computeDoubleHash(getRadius());

		hashCode = 31 * hashCode + computeDoubleHash(getAmount());

		hashCode = 31 * hashCode + getThreshold();

		hashCode = 31 * hashCode + (getAddWatermark() ? 1231 : 1237);

		hashCode = 31
				* hashCode
				+ ((getContactName() == null) ? 0 : getContactName().hashCode());

		hashCode = 31 * hashCode
				+ ((getEmail() == null) ? 0 : getEmail().hashCode());

		hashCode = 31 * hashCode
				+ ((getWebUrl() == null) ? 0 : getWebUrl().hashCode());

		hashCode = 31 * hashCode
				+ ((getCopyright() == null) ? 0 : getCopyright().hashCode());

		hashCode = 31 * hashCode
				+ ((getLogo() == null) ? 0 : getLogo().hashCode());

		hashCode = 31 * hashCode
				+ ((getKeyword() == null) ? 0 : getKeyword().hashCode());

		hashCode = 31 * hashCode + getInfoPlatePosition();

		hashCode = 31 * hashCode + (getHideCredits() ? 1231 : 1237);

		hashCode = 31 * hashCode + getJpegQuality();

		hashCode = 31 * hashCode + getScalingMethod();

		hashCode = 31
				* hashCode
				+ ((getLastAccessDate() == null) ? 0 : getLastAccessDate()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getPerspective() == null) ? 0 : getPerspective().hashCode());

		return hashCode;
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

		if (description == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "description"));

		if (labelFontFamily == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "labelFontFamily"));

		if (matColor == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "matColor"));
		matColor.validateCompleteness();

		if (frameColor == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "frameColor"));
		frameColor.validateCompleteness();

		if (groundColor == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "groundColor"));
		groundColor.validateCompleteness();

		if (horizonColor == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "horizonColor"));
		horizonColor.validateCompleteness();

		if (ceilingColor == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "ceilingColor"));
		ceilingColor.validateCompleteness();

		if (audio == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "audio"));

		if (lastAccessDate == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastAccessDate"));

	}

}
