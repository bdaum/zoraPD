package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset wall
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Wall_type extends AomValueChangedNotifier, IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property location
	 *
	 * @param _value - new element value
	 */
	public void setLocation(String _value);

	/**
	 * Get value of property location
	 *
	 * @return - value of field location
	 */
	public String getLocation();

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

	public static final String gX__unit = "mm";

	public static final String[] gXALLATTRIBUTES = new String[] { gX__unit };

	/**
	 * Set value of property gX
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setGX(int _value);

	/**
	 * Get value of property gX
	 *
	 * @return - value of field gX(unit=mm)
	 */
	public int getGX();

	public static final String gY__unit = "mm";

	public static final String[] gYALLATTRIBUTES = new String[] { gY__unit };

	/**
	 * Set value of property gY
	 *
	 * @param _value - new element value(unit=mm)
	 */
	public void setGY(int _value);

	/**
	 * Get value of property gY
	 *
	 * @return - value of field gY(unit=mm)
	 */
	public int getGY();

	public static final String gAngle__unit = "degree";

	public static final String[] gAngleALLATTRIBUTES = new String[] { gAngle__unit };

	/**
	 * Set value of property gAngle
	 *
	 * @param _value - new element value(unit=degree)
	 */
	public void setGAngle(double _value);

	/**
	 * Get value of property gAngle
	 *
	 * @return - value of field gAngle(unit=degree)
	 */
	public double getGAngle();

	/**
	 * Set value of property color
	 *
	 * @param _value - new element value
	 */
	public void setColor(Rgb_type _value);

	/**
	 * Get value of property color
	 *
	 * @return - value of field color
	 */
	public Rgb_type getColor();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
