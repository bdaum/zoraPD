package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset exhibition
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Exhibition_type extends AomValueChangedNotifier,
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
	 * Set value of property info
	 *
	 * @param _value - new element value
	 */
	public void setInfo(String _value);

	/**
	 * Get value of property info
	 *
	 * @return - value of field info
	 */
	public String getInfo();

	public static final String defaultViewingHeight__unit = "mm";

	public static final String[] defaultViewingHeightALLATTRIBUTES = new String[] { defaultViewingHeight__unit };

	/**
	 * Set value of property defaultViewingHeight
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setDefaultViewingHeight(int _value);

	/**
	 * Get value of property defaultViewingHeight
	 *
	 * @return - value of field defaultViewingHeight(unit=mm)
	 */
	public int getDefaultViewingHeight();

	public static final String variance__unit = "mm";

	public static final String[] varianceALLATTRIBUTES = new String[] { variance__unit };

	/**
	 * Set value of property variance
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setVariance(int _value);

	/**
	 * Get value of property variance
	 *
	 * @return - value of field variance(unit=mm)
	 */
	public int getVariance();

	public static final String gridSize__unit = "mm";

	public static final String[] gridSizeALLATTRIBUTES = new String[] { gridSize__unit };

	/**
	 * Set value of property gridSize
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setGridSize(int _value);

	/**
	 * Get value of property gridSize
	 *
	 * @return - value of field gridSize(unit=mm)
	 */
	public int getGridSize();

	/**
	 * Set value of property showGrid
	 *
	 * @param _value - new element value
	 */
	public void setShowGrid(boolean _value);

	/**
	 * Get value of property showGrid
	 *
	 * @return - value of field showGrid
	 */
	public boolean getShowGrid();

	/**
	 * Set value of property snapToGrid
	 *
	 * @param _value - new element value
	 */
	public void setSnapToGrid(boolean _value);

	/**
	 * Get value of property snapToGrid
	 *
	 * @return - value of field snapToGrid
	 */
	public boolean getSnapToGrid();

	/**
	 * Set value of property defaultDescription
	 *
	 * @param _value - new element value
	 */
	public void setDefaultDescription(String _value);

	/**
	 * Get value of property defaultDescription
	 *
	 * @return - value of field defaultDescription
	 */
	public String getDefaultDescription();

	/**
	 * Set value of property labelFontFamily
	 *
	 * @param _value - new element value
	 */
	public void setLabelFontFamily(String _value);

	/**
	 * Get value of property labelFontFamily
	 *
	 * @return - value of field labelFontFamily
	 */
	public String getLabelFontFamily();

	/**
	 * Set value of property labelFontSize
	 *
	 * @param _value - new element value
	 */
	public void setLabelFontSize(int _value);

	/**
	 * Get value of property labelFontSize
	 *
	 * @return - value of field labelFontSize
	 */
	public int getLabelFontSize();

	/**
	 * Set value of property labelSequence
	 *
	 * @param _value - new element value
	 */
	public void setLabelSequence(int _value);

	/**
	 * Get value of property labelSequence
	 *
	 * @return - value of field labelSequence
	 */
	public int getLabelSequence();

	/**
	 * Set value of property hideLabel
	 *
	 * @param _value - new element value
	 */
	public void setHideLabel(boolean _value);

	/**
	 * Get value of property hideLabel
	 *
	 * @return - value of field hideLabel
	 */
	public boolean getHideLabel();

	/**
	 * Set value of property labelAlignment
	 *
	 * @param _value - new element value
	 */
	public void setLabelAlignment(Integer _value);

	/**
	 * Get value of property labelAlignment
	 *
	 * @return - value of field labelAlignment
	 */
	public Integer getLabelAlignment();

	public static final String labelDistance__unit = "mm";

	public static final String[] labelDistanceALLATTRIBUTES = new String[] { labelDistance__unit };

	/**
	 * Set value of property labelDistance
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setLabelDistance(Integer _value);

	/**
	 * Get value of property labelDistance
	 *
	 * @return - value of field labelDistance(unit=mm)
	 */
	public Integer getLabelDistance();

	public static final String labelIndent__unit = "mm";

	public static final String[] labelIndentALLATTRIBUTES = new String[] { labelIndent__unit };

	/**
	 * Set value of property labelIndent
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setLabelIndent(Integer _value);

	/**
	 * Get value of property labelIndent
	 *
	 * @return - value of field labelIndent(unit=mm)
	 */
	public Integer getLabelIndent();

	public static final String startX__unit = "mm";

	public static final String[] startXALLATTRIBUTES = new String[] { startX__unit };

	/**
	 * Set value of property startX
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setStartX(int _value);

	/**
	 * Get value of property startX
	 *
	 * @return - value of field startX(unit=mm)
	 */
	public int getStartX();

	public static final String startY__unit = "mm";

	public static final String[] startYALLATTRIBUTES = new String[] { startY__unit };

	/**
	 * Set value of property startY
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setStartY(int _value);

	/**
	 * Get value of property startY
	 *
	 * @return - value of field startY(unit=mm)
	 */
	public int getStartY();

	public static final String matWidth__unit = "mm";

	public static final String[] matWidthALLATTRIBUTES = new String[] { matWidth__unit };

	/**
	 * Set value of property matWidth
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setMatWidth(int _value);

	/**
	 * Get value of property matWidth
	 *
	 * @return - value of field matWidth(unit=mm)
	 */
	public int getMatWidth();

	/**
	 * Set value of property matColor
	 *
	 * @param _value - new element value
	 */
	public void setMatColor(Rgb_type _value);

	/**
	 * Get value of property matColor
	 *
	 * @return - value of field matColor
	 */
	public Rgb_type getMatColor();

	public static final String frameWidth__unit = "mm";

	public static final String[] frameWidthALLATTRIBUTES = new String[] { frameWidth__unit };

	/**
	 * Set value of property frameWidth
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setFrameWidth(int _value);

	/**
	 * Get value of property frameWidth
	 *
	 * @return - value of field frameWidth(unit=mm)
	 */
	public int getFrameWidth();

	/**
	 * Set value of property frameColor
	 *
	 * @param _value - new element value
	 */
	public void setFrameColor(Rgb_type _value);

	/**
	 * Get value of property frameColor
	 *
	 * @return - value of field frameColor
	 */
	public Rgb_type getFrameColor();

	/**
	 * Set value of property groundColor
	 *
	 * @param _value - new element value
	 */
	public void setGroundColor(Rgb_type _value);

	/**
	 * Get value of property groundColor
	 *
	 * @return - value of field groundColor
	 */
	public Rgb_type getGroundColor();

	/**
	 * Set value of property horizonColor
	 *
	 * @param _value - new element value
	 */
	public void setHorizonColor(Rgb_type _value);

	/**
	 * Get value of property horizonColor
	 *
	 * @return - value of field horizonColor
	 */
	public Rgb_type getHorizonColor();

	/**
	 * Set value of property ceilingColor
	 *
	 * @param _value - new element value
	 */
	public void setCeilingColor(Rgb_type _value);

	/**
	 * Get value of property ceilingColor
	 *
	 * @return - value of field ceilingColor
	 */
	public Rgb_type getCeilingColor();

	/**
	 * Set value of property audio
	 *
	 * @param _value - new element value
	 */
	public void setAudio(String _value);

	/**
	 * Get value of property audio
	 *
	 * @return - value of field audio
	 */
	public String getAudio();

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
	 * Set value of property applySharpening
	 *
	 * @param _value - new element value
	 */
	public void setApplySharpening(Boolean _value);

	/**
	 * Get value of property applySharpening
	 *
	 * @return - value of field applySharpening
	 */
	public Boolean getApplySharpening();

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
	 * Set value of property keyword
	 *
	 * @param _value - new element value
	 */
	public void setKeyword(Collection<String> _value);

	/**
	 * Set single element of list keyword
	 *
	 * @param _value - new element value
	 * @param _i - index of list element
	 */
	public void setKeyword(String _value, int _i);

	/**
	 * Add an element to list keyword
	 *
	 * @param _element - new element value
	 * @return - true (as per the general contract of the Collection.add method).
	 */
	public boolean addKeyword(String _element);

	/**
	 * Remove an element from list keyword
	 *
	 * @param _element - the element to remove
	 * @return - true, if the list contained the specified element.
	 */
	public boolean removeKeyword(String _element);

	/**
	 * Make keyword empty 
	 */
	public void clearKeyword();

	/**
	 * Get value of property keyword
	 *
	 * @return - value of field keyword
	 */
	public AomList<String> getKeyword();

	/**
	 * Get single element of list keyword
	 *
	 * @param _i - index of list element
	 * @return - _i-th element of list keyword
	 */
	public String getKeyword(int _i);

	/**
	 * Set value of property infoPlatePosition
	 *
	 * @param _value - new element value
	 */
	public void setInfoPlatePosition(int _value);

	/**
	 * Get value of property infoPlatePosition
	 *
	 * @return - value of field infoPlatePosition
	 */
	public int getInfoPlatePosition();

	/**
	 * Set value of property hideCredits
	 *
	 * @param _value - new element value
	 */
	public void setHideCredits(boolean _value);

	/**
	 * Get value of property hideCredits
	 *
	 * @return - value of field hideCredits
	 */
	public boolean getHideCredits();

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

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
