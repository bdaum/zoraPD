package com.bdaum.zoom.cat.model;

import java.util.Date;

import com.bdaum.aoModeling.runtime.AomConstants;
import com.bdaum.aoModeling.runtime.AomObject;
import com.bdaum.aoModeling.runtime.ConstraintException;
import com.bdaum.aoModeling.runtime.ErrorMessages;
import com.bdaum.aoModeling.runtime.ModelMessages;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset webGallery
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class WebGallery_typeImpl extends AomObject implements WebGallery_type {

	static final long serialVersionUID = -3584223139L;

	/* ----- Constructors ----- */

	public WebGallery_typeImpl() {
		super();
	}

	/**
	 * Constructor
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
	 * @param safety - Property
	 */
	public WebGallery_typeImpl(boolean template, String name, String logo,
			boolean htmlDescription, String description, boolean hideHeader,
			int opacity, int padding, int thumbSize, String downloadText,
			boolean hideDownload, String copyright, boolean addWatermark,
			boolean showMeta, String contactName, String email, String webUrl,
			boolean hideFooter, String bgImage, boolean bgRepeat,
			Rgb_type bgColor, Rgb_type shadeColor, Rgb_type borderColor,
			Rgb_type linkColor, Font_type titleFont, Font_type sectionFont,
			Font_type captionFont, Font_type descriptionFont,
			Font_type footerFont, Font_type controlsFont,
			String selectedEngine, String outputFolder, String ftpDir,
			boolean isFtp, String pageName, String poweredByText, int imageFormat,
			boolean applySharpening, float radius, float amount, int threshold,
			String headHtml, String topHtml, String footerHtml,
			int jpegQuality, int scalingMethod, Date lastAccessDate,
			String perspective, int safety) {
		super();
		this.template = template;
		this.name = name;
		this.logo = logo;
		this.htmlDescription = htmlDescription;
		this.description = description;
		this.hideHeader = hideHeader;
		this.opacity = opacity;
		this.padding = padding;
		this.thumbSize = thumbSize;
		this.downloadText = downloadText;
		this.hideDownload = hideDownload;
		this.copyright = copyright;
		this.addWatermark = addWatermark;
		this.showMeta = showMeta;
		this.contactName = contactName;
		this.email = email;
		this.webUrl = webUrl;
		this.hideFooter = hideFooter;
		this.bgImage = bgImage;
		this.bgRepeat = bgRepeat;
		this.bgColor = bgColor;
		this.shadeColor = shadeColor;
		this.borderColor = borderColor;
		this.linkColor = linkColor;
		this.titleFont = titleFont;
		this.sectionFont = sectionFont;
		this.captionFont = captionFont;
		this.descriptionFont = descriptionFont;
		this.footerFont = footerFont;
		this.controlsFont = controlsFont;
		this.selectedEngine = selectedEngine;
		this.outputFolder = outputFolder;
		this.ftpDir = ftpDir;
		this.isFtp = isFtp;
		this.pageName = pageName;
		this.poweredByText = poweredByText;
		this.imageFormat = imageFormat;
		this.applySharpening = applySharpening;
		this.radius = radius;
		this.amount = amount;
		this.threshold = threshold;
		this.headHtml = headHtml;
		this.topHtml = topHtml;
		this.footerHtml = footerHtml;
		this.jpegQuality = jpegQuality;
		this.scalingMethod = scalingMethod;
		this.lastAccessDate = lastAccessDate;
		this.perspective = perspective;
		this.safety = safety;

	}

	/* ----- Fields ----- */

	/* *** Property template *** */

	private boolean template;

	/**
	 * Set value of property template
	 *
	 * @param _value - new field value
	 */
	public void setTemplate(boolean _value) {
		template = _value;
	}

	/**
	 * Get value of property template
	 *
	 * @return - value of field template
	 */
	public boolean getTemplate() {
		return template;
	}

	/* *** Property preview *** */

	private byte[] preview = new byte[0];

	/**
	 * Set value of property preview
	 *
	 * @param _value - new element value
	 */
	public void setPreview(byte[] _value) {
		preview = _value;
	}

	/**
	 * Set single element of array preview
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setPreview(byte _element, int _i) {
		preview[_i] = _element;
	}

	/**
	 * Get value of property preview
	 *
	 * @return - value of field preview
	 */
	public byte[] getPreview() {
		return preview;
	}

	/**
	 * Get single element of array preview
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array preview
	 */
	public byte getPreview(int _i) {
		return preview[_i];
	}

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

	/* *** Property htmlDescription *** */

	private boolean htmlDescription;

	/**
	 * Set value of property htmlDescription
	 *
	 * @param _value - new field value
	 */
	public void setHtmlDescription(boolean _value) {
		htmlDescription = _value;
	}

	/**
	 * Get value of property htmlDescription
	 *
	 * @return - value of field htmlDescription
	 */
	public boolean getHtmlDescription() {
		return htmlDescription;
	}

	/* *** Property description *** */

	private String description;

	/**
	 * Set value of property description
	 *
	 * @param _value - new field value
	 */
	public void setDescription(String _value) {
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

	/* *** Property hideHeader *** */

	private boolean hideHeader;

	/**
	 * Set value of property hideHeader
	 *
	 * @param _value - new field value
	 */
	public void setHideHeader(boolean _value) {
		hideHeader = _value;
	}

	/**
	 * Get value of property hideHeader
	 *
	 * @return - value of field hideHeader
	 */
	public boolean getHideHeader() {
		return hideHeader;
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

	/* *** Property opacity *** */

	private int opacity;

	/**
	 * Set value of property opacity
	 *
	 * @param _value - new field value
	 */
	public void setOpacity(int _value) {
		opacity = _value;
	}

	/**
	 * Get value of property opacity
	 *
	 * @return - value of field opacity
	 */
	public int getOpacity() {
		return opacity;
	}

	/* *** Property padding *** */

	private int padding;

	/**
	 * Set value of property padding
	 *
	 * @param _value - new field value
	 */
	public void setPadding(int _value) {
		padding = _value;
	}

	/**
	 * Get value of property padding
	 *
	 * @return - value of field padding
	 */
	public int getPadding() {
		return padding;
	}

	/* *** Property margins *** */

	private int[] margins = new int[0];

	/**
	 * Set value of property margins
	 *
	 * @param _value - new element value
	 */
	public void setMargins(int[] _value) {
		margins = _value;
	}

	/**
	 * Set single element of array margins
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setMargins(int _element, int _i) {
		margins[_i] = _element;
	}

	/**
	 * Get value of property margins
	 *
	 * @return - value of field margins
	 */
	public int[] getMargins() {
		return margins;
	}

	/**
	 * Get single element of array margins
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array margins
	 */
	public int getMargins(int _i) {
		return margins[_i];
	}

	/* *** Property thumbSize *** */

	private int thumbSize;

	/**
	 * Set value of property thumbSize
	 *
	 * @param _value - new field value
	 */
	public void setThumbSize(int _value) {
		thumbSize = _value;
	}

	/**
	 * Get value of property thumbSize
	 *
	 * @return - value of field thumbSize
	 */
	public int getThumbSize() {
		return thumbSize;
	}

	/* *** Property downloadText *** */

	private String downloadText;

	/**
	 * Set value of property downloadText
	 *
	 * @param _value - new field value
	 */
	public void setDownloadText(String _value) {
		downloadText = _value;
	}

	/**
	 * Get value of property downloadText
	 *
	 * @return - value of field downloadText
	 */
	public String getDownloadText() {
		return downloadText;
	}

	/* *** Property hideDownload *** */

	private boolean hideDownload;

	/**
	 * Set value of property hideDownload
	 *
	 * @param _value - new field value
	 */
	public void setHideDownload(boolean _value) {
		hideDownload = _value;
	}

	/**
	 * Get value of property hideDownload
	 *
	 * @return - value of field hideDownload
	 */
	public boolean getHideDownload() {
		return hideDownload;
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

	/* *** Property xmpFilter *** */

	private String[] xmpFilter = new String[0];

	/**
	 * Set value of property xmpFilter
	 *
	 * @param _value - new element value
	 */
	public void setXmpFilter(String[] _value) {
		xmpFilter = _value;
	}

	/**
	 * Set single element of array xmpFilter
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setXmpFilter(String _element, int _i) {
		xmpFilter[_i] = _element;
	}

	/**
	 * Get value of property xmpFilter
	 *
	 * @return - value of field xmpFilter
	 */
	public String[] getXmpFilter() {
		return xmpFilter;
	}

	/**
	 * Get single element of array xmpFilter
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array xmpFilter
	 */
	public String getXmpFilter(int _i) {
		return xmpFilter[_i];
	}

	/* *** Property showMeta *** */

	private boolean showMeta;

	/**
	 * Set value of property showMeta
	 *
	 * @param _value - new field value
	 */
	public void setShowMeta(boolean _value) {
		showMeta = _value;
	}

	/**
	 * Get value of property showMeta
	 *
	 * @return - value of field showMeta
	 */
	public boolean getShowMeta() {
		return showMeta;
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

	/* *** Property hideFooter *** */

	private boolean hideFooter;

	/**
	 * Set value of property hideFooter
	 *
	 * @param _value - new field value
	 */
	public void setHideFooter(boolean _value) {
		hideFooter = _value;
	}

	/**
	 * Get value of property hideFooter
	 *
	 * @return - value of field hideFooter
	 */
	public boolean getHideFooter() {
		return hideFooter;
	}

	/* *** Property bgImage *** */

	private String bgImage;

	/**
	 * Set value of property bgImage
	 *
	 * @param _value - new field value
	 */
	public void setBgImage(String _value) {
		bgImage = _value;
	}

	/**
	 * Get value of property bgImage
	 *
	 * @return - value of field bgImage
	 */
	public String getBgImage() {
		return bgImage;
	}

	/* *** Property bgRepeat *** */

	private boolean bgRepeat;

	/**
	 * Set value of property bgRepeat
	 *
	 * @param _value - new field value
	 */
	public void setBgRepeat(boolean _value) {
		bgRepeat = _value;
	}

	/**
	 * Get value of property bgRepeat
	 *
	 * @return - value of field bgRepeat
	 */
	public boolean getBgRepeat() {
		return bgRepeat;
	}

	/* *** Property bgColor *** */

	private Rgb_type bgColor;

	/**
	 * Set value of property bgColor
	 *
	 * @param _value - new field value
	 */
	public void setBgColor(Rgb_type _value) {
		bgColor = _value;
	}

	/**
	 * Get value of property bgColor
	 *
	 * @return - value of field bgColor
	 */
	public Rgb_type getBgColor() {
		return bgColor;
	}

	/* *** Property shadeColor *** */

	private Rgb_type shadeColor;

	/**
	 * Set value of property shadeColor
	 *
	 * @param _value - new field value
	 */
	public void setShadeColor(Rgb_type _value) {
		shadeColor = _value;
	}

	/**
	 * Get value of property shadeColor
	 *
	 * @return - value of field shadeColor
	 */
	public Rgb_type getShadeColor() {
		return shadeColor;
	}

	/* *** Property borderColor *** */

	private Rgb_type borderColor;

	/**
	 * Set value of property borderColor
	 *
	 * @param _value - new field value
	 */
	public void setBorderColor(Rgb_type _value) {
		borderColor = _value;
	}

	/**
	 * Get value of property borderColor
	 *
	 * @return - value of field borderColor
	 */
	public Rgb_type getBorderColor() {
		return borderColor;
	}

	/* *** Property linkColor *** */

	private Rgb_type linkColor;

	/**
	 * Set value of property linkColor
	 *
	 * @param _value - new field value
	 */
	public void setLinkColor(Rgb_type _value) {
		linkColor = _value;
	}

	/**
	 * Get value of property linkColor
	 *
	 * @return - value of field linkColor
	 */
	public Rgb_type getLinkColor() {
		return linkColor;
	}

	/* *** Property titleFont *** */

	private Font_type titleFont;

	/**
	 * Set value of property titleFont
	 *
	 * @param _value - new field value
	 */
	public void setTitleFont(Font_type _value) {
		titleFont = _value;
	}

	/**
	 * Get value of property titleFont
	 *
	 * @return - value of field titleFont
	 */
	public Font_type getTitleFont() {
		return titleFont;
	}

	/* *** Property sectionFont *** */

	private Font_type sectionFont;

	/**
	 * Set value of property sectionFont
	 *
	 * @param _value - new field value
	 */
	public void setSectionFont(Font_type _value) {
		sectionFont = _value;
	}

	/**
	 * Get value of property sectionFont
	 *
	 * @return - value of field sectionFont
	 */
	public Font_type getSectionFont() {
		return sectionFont;
	}

	/* *** Property captionFont *** */

	private Font_type captionFont;

	/**
	 * Set value of property captionFont
	 *
	 * @param _value - new field value
	 */
	public void setCaptionFont(Font_type _value) {
		captionFont = _value;
	}

	/**
	 * Get value of property captionFont
	 *
	 * @return - value of field captionFont
	 */
	public Font_type getCaptionFont() {
		return captionFont;
	}

	/* *** Property descriptionFont *** */

	private Font_type descriptionFont;

	/**
	 * Set value of property descriptionFont
	 *
	 * @param _value - new field value
	 */
	public void setDescriptionFont(Font_type _value) {
		descriptionFont = _value;
	}

	/**
	 * Get value of property descriptionFont
	 *
	 * @return - value of field descriptionFont
	 */
	public Font_type getDescriptionFont() {
		return descriptionFont;
	}

	/* *** Property footerFont *** */

	private Font_type footerFont;

	/**
	 * Set value of property footerFont
	 *
	 * @param _value - new field value
	 */
	public void setFooterFont(Font_type _value) {
		footerFont = _value;
	}

	/**
	 * Get value of property footerFont
	 *
	 * @return - value of field footerFont
	 */
	public Font_type getFooterFont() {
		return footerFont;
	}

	/* *** Property controlsFont *** */

	private Font_type controlsFont;

	/**
	 * Set value of property controlsFont
	 *
	 * @param _value - new field value
	 */
	public void setControlsFont(Font_type _value) {
		controlsFont = _value;
	}

	/**
	 * Get value of property controlsFont
	 *
	 * @return - value of field controlsFont
	 */
	public Font_type getControlsFont() {
		return controlsFont;
	}

	/* *** Property selectedEngine *** */

	private String selectedEngine;

	/**
	 * Set value of property selectedEngine
	 *
	 * @param _value - new field value
	 */
	public void setSelectedEngine(String _value) {
		selectedEngine = _value;
	}

	/**
	 * Get value of property selectedEngine
	 *
	 * @return - value of field selectedEngine
	 */
	public String getSelectedEngine() {
		return selectedEngine;
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

	/* *** Property poweredByText *** */

	private String poweredByText;

	/**
	 * Set value of property poweredByText
	 *
	 * @param _value - new field value
	 */
	public void setPoweredByText(String _value) {
		poweredByText = _value;
	}

	/**
	 * Get value of property poweredByText
	 *
	 * @return - value of field poweredByText
	 */
	public String getPoweredByText() {
		return poweredByText;
	}
	
	/* *** Property imageFormat *** */

	private int imageFormat;

	/**
	 * Set value of property imageFormat
	 *
	 * @param _value - new field value
	 */
	public void setImageFormat(int _value) {
		imageFormat = _value;
	}

	/**
	 * Get value of property imageFormat
	 *
	 * @return - value of field imageFormat
	 */
	public int getImageFormat() {
		return imageFormat;
	}


	/* *** Property applySharpening *** */

	private boolean applySharpening;

	/**
	 * Set value of property applySharpening
	 *
	 * @param _value - new field value
	 */
	public void setApplySharpening(boolean _value) {
		applySharpening = _value;
	}

	/**
	 * Get value of property applySharpening
	 *
	 * @return - value of field applySharpening
	 */
	public boolean getApplySharpening() {
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

	/* *** Property headHtml *** */

	private String headHtml;

	/**
	 * Set value of property headHtml
	 *
	 * @param _value - new field value
	 */
	public void setHeadHtml(String _value) {
		headHtml = _value;
	}

	/**
	 * Get value of property headHtml
	 *
	 * @return - value of field headHtml
	 */
	public String getHeadHtml() {
		return headHtml;
	}

	/* *** Property topHtml *** */

	private String topHtml;

	/**
	 * Set value of property topHtml
	 *
	 * @param _value - new field value
	 */
	public void setTopHtml(String _value) {
		topHtml = _value;
	}

	/**
	 * Get value of property topHtml
	 *
	 * @return - value of field topHtml
	 */
	public String getTopHtml() {
		return topHtml;
	}

	/* *** Property footerHtml *** */

	private String footerHtml;

	/**
	 * Set value of property footerHtml
	 *
	 * @param _value - new field value
	 */
	public void setFooterHtml(String _value) {
		footerHtml = _value;
	}

	/**
	 * Get value of property footerHtml
	 *
	 * @return - value of field footerHtml
	 */
	public String getFooterHtml() {
		return footerHtml;
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

		if (!(o instanceof WebGallery_type) || !super.equals(o))
			return false;
		WebGallery_type other = (WebGallery_type) o;
		return getTemplate() == other.getTemplate()

				&& ((getPreview() == null && other.getPreview() == null) || (getPreview() != null && getPreview()
						.equals(other.getPreview())))

				&& ((getName() == null && other.getName() == null) || (getName() != null && getName()
						.equals(other.getName())))

				&& ((getLogo() == null && other.getLogo() == null) || (getLogo() != null && getLogo()
						.equals(other.getLogo())))

				&& getHtmlDescription() == other.getHtmlDescription()

				&& ((getDescription() == null && other.getDescription() == null) || (getDescription() != null && getDescription()
						.equals(other.getDescription())))

				&& getHideHeader() == other.getHideHeader()

				&& ((getKeyword() == null && other.getKeyword() == null) || (getKeyword() != null && getKeyword()
						.equals(other.getKeyword())))

				&& getOpacity() == other.getOpacity()

				&& getPadding() == other.getPadding()

				&& ((getMargins() == null && other.getMargins() == null) || (getMargins() != null && getMargins()
						.equals(other.getMargins())))

				&& getThumbSize() == other.getThumbSize()

				&& ((getDownloadText() == null && other.getDownloadText() == null) || (getDownloadText() != null && getDownloadText()
						.equals(other.getDownloadText())))

				&& getHideDownload() == other.getHideDownload()

				&& ((getCopyright() == null && other.getCopyright() == null) || (getCopyright() != null && getCopyright()
						.equals(other.getCopyright())))

				&& getAddWatermark() == other.getAddWatermark()

				&& ((getXmpFilter() == null && other.getXmpFilter() == null) || (getXmpFilter() != null && getXmpFilter()
						.equals(other.getXmpFilter())))

				&& getShowMeta() == other.getShowMeta()

				&& ((getContactName() == null && other.getContactName() == null) || (getContactName() != null && getContactName()
						.equals(other.getContactName())))

				&& ((getEmail() == null && other.getEmail() == null) || (getEmail() != null && getEmail()
						.equals(other.getEmail())))

				&& ((getWebUrl() == null && other.getWebUrl() == null) || (getWebUrl() != null && getWebUrl()
						.equals(other.getWebUrl())))

				&& getHideFooter() == other.getHideFooter()

				&& ((getBgImage() == null && other.getBgImage() == null) || (getBgImage() != null && getBgImage()
						.equals(other.getBgImage())))

				&& getBgRepeat() == other.getBgRepeat()

				&& ((getBgColor() == null && other.getBgColor() == null) || (getBgColor() != null && getBgColor()
						.equals(other.getBgColor())))

				&& ((getShadeColor() == null && other.getShadeColor() == null) || (getShadeColor() != null && getShadeColor()
						.equals(other.getShadeColor())))

				&& ((getBorderColor() == null && other.getBorderColor() == null) || (getBorderColor() != null && getBorderColor()
						.equals(other.getBorderColor())))

				&& ((getLinkColor() == null && other.getLinkColor() == null) || (getLinkColor() != null && getLinkColor()
						.equals(other.getLinkColor())))

				&& ((getTitleFont() == null && other.getTitleFont() == null) || (getTitleFont() != null && getTitleFont()
						.equals(other.getTitleFont())))

				&& ((getSectionFont() == null && other.getSectionFont() == null) || (getSectionFont() != null && getSectionFont()
						.equals(other.getSectionFont())))

				&& ((getCaptionFont() == null && other.getCaptionFont() == null) || (getCaptionFont() != null && getCaptionFont()
						.equals(other.getCaptionFont())))

				&& ((getDescriptionFont() == null && other.getDescriptionFont() == null) || (getDescriptionFont() != null && getDescriptionFont()
						.equals(other.getDescriptionFont())))

				&& ((getFooterFont() == null && other.getFooterFont() == null) || (getFooterFont() != null && getFooterFont()
						.equals(other.getFooterFont())))

				&& ((getControlsFont() == null && other.getControlsFont() == null) || (getControlsFont() != null && getControlsFont()
						.equals(other.getControlsFont())))

				&& ((getSelectedEngine() == null && other.getSelectedEngine() == null) || (getSelectedEngine() != null && getSelectedEngine()
						.equals(other.getSelectedEngine())))

				&& ((getOutputFolder() == null && other.getOutputFolder() == null) || (getOutputFolder() != null && getOutputFolder()
						.equals(other.getOutputFolder())))

				&& ((getFtpDir() == null && other.getFtpDir() == null) || (getFtpDir() != null && getFtpDir()
						.equals(other.getFtpDir())))

				&& getIsFtp() == other.getIsFtp()

				&& ((getPageName() == null && other.getPageName() == null) || (getPageName() != null && getPageName()
						.equals(other.getPageName())))

				&& ((getPoweredByText() == null && other.getPoweredByText() == null) || (getPoweredByText() != null && getPoweredByText()
						.equals(other.getPoweredByText())))

				&& getApplySharpening() == other.getApplySharpening()

				&& getRadius() == other.getRadius()

				&& getAmount() == other.getAmount()

				&& getThreshold() == other.getThreshold()

				&& ((getHeadHtml() == null && other.getHeadHtml() == null) || (getHeadHtml() != null && getHeadHtml()
						.equals(other.getHeadHtml())))

				&& ((getTopHtml() == null && other.getTopHtml() == null) || (getTopHtml() != null && getTopHtml()
						.equals(other.getTopHtml())))

				&& ((getFooterHtml() == null && other.getFooterHtml() == null) || (getFooterHtml() != null && getFooterHtml()
						.equals(other.getFooterHtml())))

				&& getJpegQuality() == other.getJpegQuality()

				&& getScalingMethod() == other.getScalingMethod()

				&& ((getLastAccessDate() == null && other.getLastAccessDate() == null) || (getLastAccessDate() != null && getLastAccessDate()
						.equals(other.getLastAccessDate())))

				&& ((getPerspective() == null && other.getPerspective() == null) || (getPerspective() != null && getPerspective()
						.equals(other.getPerspective())))

				&& getSafety() == other.getSafety()

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

		int hashCode = -780729019 + (getTemplate() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getPreview() == null) ? 0 : getPreview().hashCode());

		hashCode = 31 * hashCode
				+ ((getName() == null) ? 0 : getName().hashCode());

		hashCode = 31 * hashCode
				+ ((getLogo() == null) ? 0 : getLogo().hashCode());

		hashCode = 31 * hashCode + (getHtmlDescription() ? 1231 : 1237);

		hashCode = 31
				* hashCode
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());

		hashCode = 31 * hashCode + (getHideHeader() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getKeyword() == null) ? 0 : getKeyword().hashCode());

		hashCode = 31 * hashCode + getOpacity();

		hashCode = 31 * hashCode + getPadding();

		hashCode = 31 * hashCode
				+ ((getMargins() == null) ? 0 : getMargins().hashCode());

		hashCode = 31 * hashCode + getThumbSize();

		hashCode = 31
				* hashCode
				+ ((getDownloadText() == null) ? 0 : getDownloadText()
						.hashCode());

		hashCode = 31 * hashCode + (getHideDownload() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getCopyright() == null) ? 0 : getCopyright().hashCode());

		hashCode = 31 * hashCode + (getAddWatermark() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getXmpFilter() == null) ? 0 : getXmpFilter().hashCode());

		hashCode = 31 * hashCode + (getShowMeta() ? 1231 : 1237);

		hashCode = 31
				* hashCode
				+ ((getContactName() == null) ? 0 : getContactName().hashCode());

		hashCode = 31 * hashCode
				+ ((getEmail() == null) ? 0 : getEmail().hashCode());

		hashCode = 31 * hashCode
				+ ((getWebUrl() == null) ? 0 : getWebUrl().hashCode());

		hashCode = 31 * hashCode + (getHideFooter() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getBgImage() == null) ? 0 : getBgImage().hashCode());

		hashCode = 31 * hashCode + (getBgRepeat() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getBgColor() == null) ? 0 : getBgColor().hashCode());

		hashCode = 31 * hashCode
				+ ((getShadeColor() == null) ? 0 : getShadeColor().hashCode());

		hashCode = 31
				* hashCode
				+ ((getBorderColor() == null) ? 0 : getBorderColor().hashCode());

		hashCode = 31 * hashCode
				+ ((getLinkColor() == null) ? 0 : getLinkColor().hashCode());

		hashCode = 31 * hashCode
				+ ((getTitleFont() == null) ? 0 : getTitleFont().hashCode());

		hashCode = 31
				* hashCode
				+ ((getSectionFont() == null) ? 0 : getSectionFont().hashCode());

		hashCode = 31
				* hashCode
				+ ((getCaptionFont() == null) ? 0 : getCaptionFont().hashCode());

		hashCode = 31
				* hashCode
				+ ((getDescriptionFont() == null) ? 0 : getDescriptionFont()
						.hashCode());

		hashCode = 31 * hashCode
				+ ((getFooterFont() == null) ? 0 : getFooterFont().hashCode());

		hashCode = 31
				* hashCode
				+ ((getControlsFont() == null) ? 0 : getControlsFont()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getSelectedEngine() == null) ? 0 : getSelectedEngine()
						.hashCode());

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
				+ ((getPoweredByText() == null) ? 0 : getPoweredByText()
						.hashCode());

		hashCode = 31 * hashCode + (getApplySharpening() ? 1231 : 1237);

		hashCode = 31 * hashCode + computeDoubleHash(getRadius());

		hashCode = 31 * hashCode + computeDoubleHash(getAmount());

		hashCode = 31 * hashCode + getThreshold();

		hashCode = 31 * hashCode
				+ ((getHeadHtml() == null) ? 0 : getHeadHtml().hashCode());

		hashCode = 31 * hashCode
				+ ((getTopHtml() == null) ? 0 : getTopHtml().hashCode());

		hashCode = 31 * hashCode
				+ ((getFooterHtml() == null) ? 0 : getFooterHtml().hashCode());

		hashCode = 31 * hashCode + getJpegQuality();

		hashCode = 31 * hashCode + getScalingMethod();

		hashCode = 31
				* hashCode
				+ ((getLastAccessDate() == null) ? 0 : getLastAccessDate()
						.hashCode());

		hashCode = 31
				* hashCode
				+ ((getPerspective() == null) ? 0 : getPerspective().hashCode());

		hashCode = 31 * hashCode + getSafety();

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

		if (lastAccessDate == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "lastAccessDate"));

	}

}
