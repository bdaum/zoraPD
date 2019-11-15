package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset webGallery
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface WebGallery_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property template
	 *
	 * @param _value - new element value
	 */
	public void setTemplate(boolean _value);

	/**
	 * Get value of property template
	 *
	 * @return - value of field template
	 */
	public boolean getTemplate();

	/**
	 * Set value of property preview
	 *
	 * @param _value - new element value
	 */
	public void setPreview(byte[] _value);

	/**
	 * Set single element of array preview
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setPreview(byte _element, int _i);

	/**
	 * Get value of property preview
	 *
	 * @return - value of field preview
	 */
	public byte[] getPreview();

	/**
	 * Get single element of array preview
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array preview
	 */
	public byte getPreview(int _i);

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
	 * Set value of property logo
	 *
	 * @param _value - new element value
	 */
	public void setLogo(String _value);

	/**
	 * Get value of property logo
	 *
	 * @return - value of field logo
	 */
	public String getLogo();

	/**
	 * Set value of property htmlDescription
	 *
	 * @param _value - new element value
	 */
	public void setHtmlDescription(boolean _value);

	/**
	 * Get value of property htmlDescription
	 *
	 * @return - value of field htmlDescription
	 */
	public boolean getHtmlDescription();

	/**
	 * Set value of property description
	 *
	 * @param _value - new element value
	 */
	public void setDescription(String _value);

	/**
	 * Get value of property description
	 *
	 * @return - value of field description
	 */
	public String getDescription();

	/**
	 * Set value of property hideHeader
	 *
	 * @param _value - new element value
	 */
	public void setHideHeader(boolean _value);

	/**
	 * Get value of property hideHeader
	 *
	 * @return - value of field hideHeader
	 */
	public boolean getHideHeader();

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
	 * Set value of property opacity
	 *
	 * @param _value - new element value
	 */
	public void setOpacity(int _value);

	/**
	 * Get value of property opacity
	 *
	 * @return - value of field opacity
	 */
	public int getOpacity();

	/**
	 * Set value of property padding
	 *
	 * @param _value - new element value
	 */
	public void setPadding(int _value);

	/**
	 * Get value of property padding
	 *
	 * @return - value of field padding
	 */
	public int getPadding();

	/**
	 * Set value of property margins
	 *
	 * @param _value - new element value
	 */
	public void setMargins(int[] _value);

	/**
	 * Set single element of array margins
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setMargins(int _element, int _i);

	/**
	 * Get value of property margins
	 *
	 * @return - value of field margins
	 */
	public int[] getMargins();

	/**
	 * Get single element of array margins
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array margins
	 */
	public int getMargins(int _i);

	/**
	 * Set value of property thumbSize
	 *
	 * @param _value - new element value
	 */
	public void setThumbSize(int _value);

	/**
	 * Get value of property thumbSize
	 *
	 * @return - value of field thumbSize
	 */
	public int getThumbSize();

	/**
	 * Set value of property downloadText
	 *
	 * @param _value - new element value
	 */
	public void setDownloadText(String _value);

	/**
	 * Get value of property downloadText
	 *
	 * @return - value of field downloadText
	 */
	public String getDownloadText();

	/**
	 * Set value of property hideDownload
	 *
	 * @param _value - new element value
	 */
	public void setHideDownload(boolean _value);

	/**
	 * Get value of property hideDownload
	 *
	 * @return - value of field hideDownload
	 */
	public boolean getHideDownload();

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
	 * Set value of property addWatermark
	 *
	 * @param _value - new element value
	 */
	public void setAddWatermark(boolean _value);

	/**
	 * Get value of property addWatermark
	 *
	 * @return - value of field addWatermark
	 */
	public boolean getAddWatermark();

	/**
	 * Set value of property xmpFilter
	 *
	 * @param _value - new element value
	 */
	public void setXmpFilter(String[] _value);

	/**
	 * Set single element of array xmpFilter
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setXmpFilter(String _element, int _i);

	/**
	 * Get value of property xmpFilter
	 *
	 * @return - value of field xmpFilter
	 */
	public String[] getXmpFilter();

	/**
	 * Get single element of array xmpFilter
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array xmpFilter
	 */
	public String getXmpFilter(int _i);

	/**
	 * Set value of property showMeta
	 *
	 * @param _value - new element value
	 */
	public void setShowMeta(boolean _value);

	/**
	 * Get value of property showMeta
	 *
	 * @return - value of field showMeta
	 */
	public boolean getShowMeta();

	/**
	 * Set value of property contactName
	 *
	 * @param _value - new element value
	 */
	public void setContactName(String _value);

	/**
	 * Get value of property contactName
	 *
	 * @return - value of field contactName
	 */
	public String getContactName();

	/**
	 * Set value of property email
	 *
	 * @param _value - new element value
	 */
	public void setEmail(String _value);

	/**
	 * Get value of property email
	 *
	 * @return - value of field email
	 */
	public String getEmail();

	/**
	 * Set value of property webUrl
	 *
	 * @param _value - new element value
	 */
	public void setWebUrl(String _value);

	/**
	 * Get value of property webUrl
	 *
	 * @return - value of field webUrl
	 */
	public String getWebUrl();

	/**
	 * Set value of property hideFooter
	 *
	 * @param _value - new element value
	 */
	public void setHideFooter(boolean _value);

	/**
	 * Get value of property hideFooter
	 *
	 * @return - value of field hideFooter
	 */
	public boolean getHideFooter();

	/**
	 * Set value of property bgImage
	 *
	 * @param _value - new element value
	 */
	public void setBgImage(String _value);

	/**
	 * Get value of property bgImage
	 *
	 * @return - value of field bgImage
	 */
	public String getBgImage();

	/**
	 * Set value of property bgRepeat
	 *
	 * @param _value - new element value
	 */
	public void setBgRepeat(boolean _value);

	/**
	 * Get value of property bgRepeat
	 *
	 * @return - value of field bgRepeat
	 */
	public boolean getBgRepeat();

	/**
	 * Set value of property bgColor
	 *
	 * @param _value - new element value
	 */
	public void setBgColor(Rgb_type _value);

	/**
	 * Get value of property bgColor
	 *
	 * @return - value of field bgColor
	 */
	public Rgb_type getBgColor();

	/**
	 * Set value of property shadeColor
	 *
	 * @param _value - new element value
	 */
	public void setShadeColor(Rgb_type _value);

	/**
	 * Get value of property shadeColor
	 *
	 * @return - value of field shadeColor
	 */
	public Rgb_type getShadeColor();

	/**
	 * Set value of property borderColor
	 *
	 * @param _value - new element value
	 */
	public void setBorderColor(Rgb_type _value);

	/**
	 * Get value of property borderColor
	 *
	 * @return - value of field borderColor
	 */
	public Rgb_type getBorderColor();

	/**
	 * Set value of property linkColor
	 *
	 * @param _value - new element value
	 */
	public void setLinkColor(Rgb_type _value);

	/**
	 * Get value of property linkColor
	 *
	 * @return - value of field linkColor
	 */
	public Rgb_type getLinkColor();

	/**
	 * Set value of property titleFont
	 *
	 * @param _value - new element value
	 */
	public void setTitleFont(Font_type _value);

	/**
	 * Get value of property titleFont
	 *
	 * @return - value of field titleFont
	 */
	public Font_type getTitleFont();

	/**
	 * Set value of property sectionFont
	 *
	 * @param _value - new element value
	 */
	public void setSectionFont(Font_type _value);

	/**
	 * Get value of property sectionFont
	 *
	 * @return - value of field sectionFont
	 */
	public Font_type getSectionFont();

	/**
	 * Set value of property captionFont
	 *
	 * @param _value - new element value
	 */
	public void setCaptionFont(Font_type _value);

	/**
	 * Get value of property captionFont
	 *
	 * @return - value of field captionFont
	 */
	public Font_type getCaptionFont();

	/**
	 * Set value of property descriptionFont
	 *
	 * @param _value - new element value
	 */
	public void setDescriptionFont(Font_type _value);

	/**
	 * Get value of property descriptionFont
	 *
	 * @return - value of field descriptionFont
	 */
	public Font_type getDescriptionFont();

	/**
	 * Set value of property footerFont
	 *
	 * @param _value - new element value
	 */
	public void setFooterFont(Font_type _value);

	/**
	 * Get value of property footerFont
	 *
	 * @return - value of field footerFont
	 */
	public Font_type getFooterFont();

	/**
	 * Set value of property controlsFont
	 *
	 * @param _value - new element value
	 */
	public void setControlsFont(Font_type _value);

	/**
	 * Get value of property controlsFont
	 *
	 * @return - value of field controlsFont
	 */
	public Font_type getControlsFont();

	/**
	 * Set value of property selectedEngine
	 *
	 * @param _value - new element value
	 */
	public void setSelectedEngine(String _value);

	/**
	 * Get value of property selectedEngine
	 *
	 * @return - value of field selectedEngine
	 */
	public String getSelectedEngine();

	/**
	 * Set value of property outputFolder
	 *
	 * @param _value - new element value
	 */
	public void setOutputFolder(String _value);

	/**
	 * Get value of property outputFolder
	 *
	 * @return - value of field outputFolder
	 */
	public String getOutputFolder();

	/**
	 * Set value of property ftpDir
	 *
	 * @param _value - new element value
	 */
	public void setFtpDir(String _value);

	/**
	 * Get value of property ftpDir
	 *
	 * @return - value of field ftpDir
	 */
	public String getFtpDir();

	/**
	 * Set value of property isFtp
	 *
	 * @param _value - new element value
	 */
	public void setIsFtp(boolean _value);

	/**
	 * Get value of property isFtp
	 *
	 * @return - value of field isFtp
	 */
	public boolean getIsFtp();

	/**
	 * Set value of property pageName
	 *
	 * @param _value - new element value
	 */
	public void setPageName(String _value);

	/**
	 * Get value of property pageName
	 *
	 * @return - value of field pageName
	 */
	public String getPageName();

	/**
	 * Set value of property poweredByText
	 *
	 * @param _value - new element value
	 */
	public void setPoweredByText(String _value);

	/**
	 * Get value of property poweredByText
	 *
	 * @return - value of field poweredByText
	 */
	public String getPoweredByText();

	/**
	 * Set value of property applySharpening
	 *
	 * @param _value - new element value
	 */
	public void setApplySharpening(boolean _value);

	/**
	 * Get value of property applySharpening
	 *
	 * @return - value of field applySharpening
	 */
	public boolean getApplySharpening();

	/**
	 * Set value of property radius
	 *
	 * @param _value - new element value
	 */
	public void setRadius(float _value);

	/**
	 * Get value of property radius
	 *
	 * @return - value of field radius
	 */
	public float getRadius();

	/**
	 * Set value of property amount
	 *
	 * @param _value - new element value
	 */
	public void setAmount(float _value);

	/**
	 * Get value of property amount
	 *
	 * @return - value of field amount
	 */
	public float getAmount();

	/**
	 * Set value of property threshold
	 *
	 * @param _value - new element value
	 */
	public void setThreshold(int _value);

	/**
	 * Get value of property threshold
	 *
	 * @return - value of field threshold
	 */
	public int getThreshold();

	/**
	 * Set value of property headHtml
	 *
	 * @param _value - new element value
	 */
	public void setHeadHtml(String _value);

	/**
	 * Get value of property headHtml
	 *
	 * @return - value of field headHtml
	 */
	public String getHeadHtml();

	/**
	 * Set value of property topHtml
	 *
	 * @param _value - new element value
	 */
	public void setTopHtml(String _value);

	/**
	 * Get value of property topHtml
	 *
	 * @return - value of field topHtml
	 */
	public String getTopHtml();

	/**
	 * Set value of property footerHtml
	 *
	 * @param _value - new element value
	 */
	public void setFooterHtml(String _value);

	/**
	 * Get value of property footerHtml
	 *
	 * @return - value of field footerHtml
	 */
	public String getFooterHtml();

	/**
	 * Set value of property jpegQuality
	 *
	 * @param _value - new element value
	 */
	public void setJpegQuality(int _value);

	/**
	 * Get value of property jpegQuality
	 *
	 * @return - value of field jpegQuality
	 */
	public int getJpegQuality();

	/**
	 * Set value of property scalingMethod
	 *
	 * @param _value - new element value
	 */
	public void setScalingMethod(int _value);

	/**
	 * Get value of property scalingMethod
	 *
	 * @return - value of field scalingMethod
	 */
	public int getScalingMethod();

	/**
	 * Set value of property lastAccessDate
	 *
	 * @param _value - new element value
	 */
	public void setLastAccessDate(Date _value);

	/**
	 * Get value of property lastAccessDate
	 *
	 * @return - value of field lastAccessDate
	 */
	public Date getLastAccessDate();

	/**
	 * Set value of property perspective
	 *
	 * @param _value - new element value
	 */
	public void setPerspective(String _value);

	/**
	 * Get value of property perspective
	 *
	 * @return - value of field perspective
	 */
	public String getPerspective();

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

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
