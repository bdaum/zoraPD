package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset exhibit
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Exhibit_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

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
	 * Set value of property credits
	 *
	 * @param _value - new element value
	 */
	public void setCredits(String _value);

	/**
	 * Get value of property credits
	 *
	 * @return - value of field credits
	 */
	public String getCredits();

	/**
	 * Set value of property date
	 *
	 * @param _value - new element value
	 */
	public void setDate(String _value);

	/**
	 * Get value of property date
	 *
	 * @return - value of field date
	 */
	public String getDate();

	public static final String x__unit = "mm";

	public static final String[] xALLATTRIBUTES = new String[] { x__unit };

	/**
	 * Set value of property x
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setX(int _value);

	/**
	 * Get value of property x
	 *
	 * @return - value of field x(unit=mm)
	 */
	public int getX();

	public static final String y__unit = "mm";

	public static final String[] yALLATTRIBUTES = new String[] { y__unit };

	/**
	 * Set value of property y
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setY(int _value);

	/**
	 * Get value of property y
	 *
	 * @return - value of field y(unit=mm)
	 */
	public int getY();

	public static final String width__unit = "mm";

	public static final String[] widthALLATTRIBUTES = new String[] { width__unit };

	/**
	 * Set value of property width
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setWidth(int _value);

	/**
	 * Get value of property width
	 *
	 * @return - value of field width(unit=mm)
	 */
	public int getWidth();

	public static final String height__unit = "mm";

	public static final String[] heightALLATTRIBUTES = new String[] { height__unit };

	/**
	 * Set value of property height
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setHeight(int _value);

	/**
	 * Get value of property height
	 *
	 * @return - value of field height(unit=mm)
	 */
	public int getHeight();

	public static final String matWidth__unit = "mm";

	public static final String[] matWidthALLATTRIBUTES = new String[] { matWidth__unit };

	/**
	 * Set value of property matWidth
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setMatWidth(Integer _value);

	/**
	 * Get value of property matWidth
	 *
	 * @return - value of field matWidth(unit=mm)
	 */
	public Integer getMatWidth();

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
	public void setFrameWidth(Integer _value);

	/**
	 * Get value of property frameWidth
	 *
	 * @return - value of field frameWidth(unit=mm)
	 */
	public Integer getFrameWidth();

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
	 * Set value of property sold
	 *
	 * @param _value - new element value
	 */
	public void setSold(boolean _value);

	/**
	 * Get value of property sold
	 *
	 * @return - value of field sold
	 */
	public boolean getSold();

	/**
	 * Set value of property hideLabel
	 *
	 * @param _value - new element value
	 */
	public void setHideLabel(Boolean _value);

	/**
	 * Get value of property hideLabel
	 *
	 * @return - value of field hideLabel
	 */
	public Boolean getHideLabel();

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

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
