package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset PageLayout
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface PageLayout_type extends AomValueChangedNotifier,
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
	 * Set value of property type
	 *
	 * @param _value - new element value
	 */
	public void setType(int _value);

	/**
	 * Get value of property type
	 *
	 * @return - value of field type
	 */
	public int getType();

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
	 * Set value of property subtitle
	 *
	 * @param _value - new element value
	 */
	public void setSubtitle(String _value);

	/**
	 * Get value of property subtitle
	 *
	 * @return - value of field subtitle
	 */
	public String getSubtitle();

	/**
	 * Set value of property footer
	 *
	 * @param _value - new element value
	 */
	public void setFooter(String _value);

	/**
	 * Get value of property footer
	 *
	 * @return - value of field footer
	 */
	public String getFooter();

	/**
	 * Set value of property size
	 *
	 * @param _value - new element value
	 */
	public void setSize(int _value);

	/**
	 * Get value of property size
	 *
	 * @return - value of field size
	 */
	public int getSize();

	/**
	 * Set value of property columns
	 *
	 * @param _value - new element value
	 */
	public void setColumns(int _value);

	/**
	 * Get value of property columns
	 *
	 * @return - value of field columns
	 */
	public int getColumns();

	public static final String leftMargin__unit = "mm";

	public static final String[] leftMarginALLATTRIBUTES = new String[] { leftMargin__unit };

	/**
	 * Set value of property leftMargin
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setLeftMargin(int _value);

	/**
	 * Get value of property leftMargin
	 *
	 * @return - value of field leftMargin(unit=mm)
	 */
	public int getLeftMargin();

	public static final String rightMargin__unit = "mm";

	public static final String[] rightMarginALLATTRIBUTES = new String[] { rightMargin__unit };

	/**
	 * Set value of property rightMargin
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setRightMargin(int _value);

	/**
	 * Get value of property rightMargin
	 *
	 * @return - value of field rightMargin(unit=mm)
	 */
	public int getRightMargin();

	public static final String horizontalGap__unit = "mm";

	public static final String[] horizontalGapALLATTRIBUTES = new String[] { horizontalGap__unit };

	/**
	 * Set value of property horizontalGap
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setHorizontalGap(int _value);

	/**
	 * Get value of property horizontalGap
	 *
	 * @return - value of field horizontalGap(unit=mm)
	 */
	public int getHorizontalGap();

	public static final String topMargin__unit = "mm";

	public static final String[] topMarginALLATTRIBUTES = new String[] { topMargin__unit };

	/**
	 * Set value of property topMargin
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setTopMargin(int _value);

	/**
	 * Get value of property topMargin
	 *
	 * @return - value of field topMargin(unit=mm)
	 */
	public int getTopMargin();

	public static final String bottomMargin__unit = "mm";

	public static final String[] bottomMarginALLATTRIBUTES = new String[] { bottomMargin__unit };

	/**
	 * Set value of property bottomMargin
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setBottomMargin(int _value);

	/**
	 * Get value of property bottomMargin
	 *
	 * @return - value of field bottomMargin(unit=mm)
	 */
	public int getBottomMargin();

	public static final String verticalGap__unit = "mm";

	public static final String[] verticalGapALLATTRIBUTES = new String[] { verticalGap__unit };

	/**
	 * Set value of property verticalGap
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setVerticalGap(int _value);

	/**
	 * Get value of property verticalGap
	 *
	 * @return - value of field verticalGap(unit=mm)
	 */
	public int getVerticalGap();

	/**
	 * Set value of property caption1
	 *
	 * @param _value - new element value
	 */
	public void setCaption1(String _value);

	/**
	 * Get value of property caption1
	 *
	 * @return - value of field caption1
	 */
	public String getCaption1();

	/**
	 * Set value of property caption2
	 *
	 * @param _value - new element value
	 */
	public void setCaption2(String _value);

	/**
	 * Get value of property caption2
	 *
	 * @return - value of field caption2
	 */
	public String getCaption2();

	/**
	 * Set value of property alt
	 *
	 * @param _value - new element value
	 */
	public void setAlt(String _value);

	/**
	 * Get value of property alt
	 *
	 * @return - value of field alt
	 */
	public String getAlt();

	/**
	 * Set value of property keyLine
	 *
	 * @param _value - new element value
	 */
	public void setKeyLine(int _value);

	/**
	 * Get value of property keyLine
	 *
	 * @return - value of field keyLine
	 */
	public int getKeyLine();

	/**
	 * Set value of property landscape
	 *
	 * @param _value - new element value
	 */
	public void setLandscape(boolean _value);

	/**
	 * Get value of property landscape
	 *
	 * @return - value of field landscape
	 */
	public boolean getLandscape();

	/**
	 * Set value of property facingPages
	 *
	 * @param _value - new element value
	 */
	public void setFacingPages(boolean _value);

	/**
	 * Get value of property facingPages
	 *
	 * @return - value of field facingPages
	 */
	public boolean getFacingPages();

	public static final String format_a0 = "A0";
	public static final String format_a1 = "A1";
	public static final String format_a2 = "A2";
	public static final String format_a3 = "A3";
	public static final String format_a4 = "A4";
	public static final String format_a5 = "A5";
	public static final String format_a6 = "A6";
	public static final String format_b0 = "B0";
	public static final String format_b1 = "B1";
	public static final String format_b2 = "B2";
	public static final String format_b3 = "B3";
	public static final String format_b4 = "B4";
	public static final String format_b5 = "B5";
	public static final String format_b6 = "B6";
	public static final String format_b7 = "B7";
	public static final String format_letter = "Letter";
	public static final String format_halfletter = "Halfletter";
	public static final String format_legal = "Legal";
	public static final String format_11X17 = "11x17";

	public static final String[] formatALLVALUES = new String[] { format_a0,
			format_a1, format_a2, format_a3, format_a4, format_a5, format_a6,
			format_b0, format_b1, format_b2, format_b3, format_b4, format_b5,
			format_b6, format_b7, format_letter, format_halfletter,
			format_legal, format_11X17 };

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

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
