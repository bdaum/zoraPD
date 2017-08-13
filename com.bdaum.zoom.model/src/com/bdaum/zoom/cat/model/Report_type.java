package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset report
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface Report_type extends AomValueChangedNotifier,
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
	 * Set value of property mode
	 *
	 * @param _value - new element value
	 */
	public void setMode(int _value);

	/**
	 * Get value of property mode
	 *
	 * @return - value of field mode
	 */
	public int getMode();

	/**
	 * Set value of property sortField
	 *
	 * @param _value - new element value
	 */
	public void setSortField(int _value);

	/**
	 * Get value of property sortField
	 *
	 * @return - value of field sortField
	 */
	public int getSortField();

	/**
	 * Set value of property descending
	 *
	 * @param _value - new element value
	 */
	public void setDescending(boolean _value);

	/**
	 * Get value of property descending
	 *
	 * @return - value of field descending
	 */
	public boolean getDescending();

	/**
	 * Set value of property field
	 *
	 * @param _value - new element value
	 */
	public void setField(String _value);

	/**
	 * Get value of property field
	 *
	 * @return - value of field field
	 */
	public String getField();

	/**
	 * Set value of property timeLower
	 *
	 * @param _value - new element value
	 */
	public void setTimeLower(long _value);

	/**
	 * Get value of property timeLower
	 *
	 * @return - value of field timeLower
	 */
	public long getTimeLower();

	/**
	 * Set value of property timeUpper
	 *
	 * @param _value - new element value
	 */
	public void setTimeUpper(long _value);

	/**
	 * Get value of property timeUpper
	 *
	 * @return - value of field timeUpper
	 */
	public long getTimeUpper();

	/**
	 * Set value of property valueLower
	 *
	 * @param _value - new element value
	 */
	public void setValueLower(long _value);

	/**
	 * Get value of property valueLower
	 *
	 * @return - value of field valueLower
	 */
	public long getValueLower();

	/**
	 * Set value of property valueUpper
	 *
	 * @param _value - new element value
	 */
	public void setValueUpper(long _value);

	/**
	 * Get value of property valueUpper
	 *
	 * @return - value of field valueUpper
	 */
	public long getValueUpper();

	/**
	 * Set value of property dayInterval
	 *
	 * @param _value - new element value
	 */
	public void setDayInterval(int _value);

	/**
	 * Get value of property dayInterval
	 *
	 * @return - value of field dayInterval
	 */
	public int getDayInterval();

	/**
	 * Set value of property timeInterval
	 *
	 * @param _value - new element value
	 */
	public void setTimeInterval(int _value);

	/**
	 * Get value of property timeInterval
	 *
	 * @return - value of field timeInterval
	 */
	public int getTimeInterval();

	/**
	 * Set value of property valueInterval
	 *
	 * @param _value - new element value
	 */
	public void setValueInterval(int _value);

	/**
	 * Get value of property valueInterval
	 *
	 * @return - value of field valueInterval
	 */
	public int getValueInterval();

	/**
	 * Set value of property filter
	 *
	 * @param _value - new element value
	 */
	public void setFilter(String[] _value);

	/**
	 * Set single element of array filter
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setFilter(String _element, int _i);

	/**
	 * Get value of property filter
	 *
	 * @return - value of field filter
	 */
	public String[] getFilter();

	/**
	 * Get single element of array filter
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array filter
	 */
	public String getFilter(int _i);

	/**
	 * Set value of property threshold
	 *
	 * @param _value - new element value
	 */
	public void setThreshold(float _value);

	/**
	 * Get value of property threshold
	 *
	 * @return - value of field threshold
	 */
	public float getThreshold();

	/**
	 * Set value of property properties
	 *
	 * @param _value - new element value
	 */
	public void setProperties(Object _value);

	/**
	 * Get value of property properties
	 *
	 * @return - value of field properties
	 */
	public Object getProperties();

	/**
	 * Set value of property skipOrphans
	 *
	 * @param _value - new element value
	 */
	public void setSkipOrphans(boolean _value);

	/**
	 * Get value of property skipOrphans
	 *
	 * @return - value of field skipOrphans
	 */
	public boolean getSkipOrphans();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
