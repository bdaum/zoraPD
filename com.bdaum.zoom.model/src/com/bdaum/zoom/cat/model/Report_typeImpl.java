package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset report
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class Report_typeImpl extends AomObject implements Report_type {

	static final long serialVersionUID = 403554631L;

	/* ----- Constructors ----- */

	public Report_typeImpl() {
		super();
	}

	/**
	 * Constructor
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
	 * @param skipOrphans - Property
	 */
	public Report_typeImpl(String name, String description, String source,
			int mode, int sortField, boolean descending, String field,
			long timeLower, long timeUpper, long valueLower, long valueUpper,
			int dayInterval, int timeInterval, int valueInterval,
			float threshold, Object properties, boolean skipOrphans) {
		super();
		this.name = name;
		this.description = description;
		this.source = source;
		this.mode = mode;
		this.sortField = sortField;
		this.descending = descending;
		this.field = field;
		this.timeLower = timeLower;
		this.timeUpper = timeUpper;
		this.valueLower = valueLower;
		this.valueUpper = valueUpper;
		this.dayInterval = dayInterval;
		this.timeInterval = timeInterval;
		this.valueInterval = valueInterval;
		this.threshold = threshold;
		this.properties = properties;
		this.skipOrphans = skipOrphans;

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

	/* *** Property source *** */

	private String source;

	/**
	 * Set value of property source
	 *
	 * @param _value - new field value
	 */
	public void setSource(String _value) {
		source = _value;
	}

	/**
	 * Get value of property source
	 *
	 * @return - value of field source
	 */
	public String getSource() {
		return source;
	}

	/* *** Property mode *** */

	private int mode;

	/**
	 * Set value of property mode
	 *
	 * @param _value - new field value
	 */
	public void setMode(int _value) {
		mode = _value;
	}

	/**
	 * Get value of property mode
	 *
	 * @return - value of field mode
	 */
	public int getMode() {
		return mode;
	}

	/* *** Property sortField *** */

	private int sortField;

	/**
	 * Set value of property sortField
	 *
	 * @param _value - new field value
	 */
	public void setSortField(int _value) {
		sortField = _value;
	}

	/**
	 * Get value of property sortField
	 *
	 * @return - value of field sortField
	 */
	public int getSortField() {
		return sortField;
	}

	/* *** Property descending *** */

	private boolean descending;

	/**
	 * Set value of property descending
	 *
	 * @param _value - new field value
	 */
	public void setDescending(boolean _value) {
		descending = _value;
	}

	/**
	 * Get value of property descending
	 *
	 * @return - value of field descending
	 */
	public boolean getDescending() {
		return descending;
	}

	/* *** Property field *** */

	private String field = AomConstants.INIT_String;

	/**
	 * Set value of property field
	 *
	 * @param _value - new field value
	 */
	public void setField(String _value) {
		if (_value == null)
			throw new IllegalArgumentException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "field"));
		field = _value;
	}

	/**
	 * Get value of property field
	 *
	 * @return - value of field field
	 */
	public String getField() {
		return field;
	}

	/* *** Property timeLower *** */

	private long timeLower;

	/**
	 * Set value of property timeLower
	 *
	 * @param _value - new field value
	 */
	public void setTimeLower(long _value) {
		timeLower = _value;
	}

	/**
	 * Get value of property timeLower
	 *
	 * @return - value of field timeLower
	 */
	public long getTimeLower() {
		return timeLower;
	}

	/* *** Property timeUpper *** */

	private long timeUpper;

	/**
	 * Set value of property timeUpper
	 *
	 * @param _value - new field value
	 */
	public void setTimeUpper(long _value) {
		timeUpper = _value;
	}

	/**
	 * Get value of property timeUpper
	 *
	 * @return - value of field timeUpper
	 */
	public long getTimeUpper() {
		return timeUpper;
	}

	/* *** Property valueLower *** */

	private long valueLower;

	/**
	 * Set value of property valueLower
	 *
	 * @param _value - new field value
	 */
	public void setValueLower(long _value) {
		valueLower = _value;
	}

	/**
	 * Get value of property valueLower
	 *
	 * @return - value of field valueLower
	 */
	public long getValueLower() {
		return valueLower;
	}

	/* *** Property valueUpper *** */

	private long valueUpper;

	/**
	 * Set value of property valueUpper
	 *
	 * @param _value - new field value
	 */
	public void setValueUpper(long _value) {
		valueUpper = _value;
	}

	/**
	 * Get value of property valueUpper
	 *
	 * @return - value of field valueUpper
	 */
	public long getValueUpper() {
		return valueUpper;
	}

	/* *** Property dayInterval *** */

	private int dayInterval;

	/**
	 * Set value of property dayInterval
	 *
	 * @param _value - new field value
	 */
	public void setDayInterval(int _value) {
		dayInterval = _value;
	}

	/**
	 * Get value of property dayInterval
	 *
	 * @return - value of field dayInterval
	 */
	public int getDayInterval() {
		return dayInterval;
	}

	/* *** Property timeInterval *** */

	private int timeInterval;

	/**
	 * Set value of property timeInterval
	 *
	 * @param _value - new field value
	 */
	public void setTimeInterval(int _value) {
		timeInterval = _value;
	}

	/**
	 * Get value of property timeInterval
	 *
	 * @return - value of field timeInterval
	 */
	public int getTimeInterval() {
		return timeInterval;
	}

	/* *** Property valueInterval *** */

	private int valueInterval;

	/**
	 * Set value of property valueInterval
	 *
	 * @param _value - new field value
	 */
	public void setValueInterval(int _value) {
		valueInterval = _value;
	}

	/**
	 * Get value of property valueInterval
	 *
	 * @return - value of field valueInterval
	 */
	public int getValueInterval() {
		return valueInterval;
	}

	/* *** Property filter *** */

	private String[] filter = new String[0];

	/**
	 * Set value of property filter
	 *
	 * @param _value - new element value
	 */
	public void setFilter(String[] _value) {
		filter = _value;
	}

	/**
	 * Set single element of array filter
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setFilter(String _element, int _i) {
		filter[_i] = _element;
	}

	/**
	 * Get value of property filter
	 *
	 * @return - value of field filter
	 */
	public String[] getFilter() {
		return filter;
	}

	/**
	 * Get single element of array filter
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array filter
	 */
	public String getFilter(int _i) {
		return filter[_i];
	}

	/* *** Property threshold *** */

	private float threshold;

	/**
	 * Set value of property threshold
	 *
	 * @param _value - new field value
	 */
	public void setThreshold(float _value) {
		threshold = _value;
	}

	/**
	 * Get value of property threshold
	 *
	 * @return - value of field threshold
	 */
	public float getThreshold() {
		return threshold;
	}

	/* *** Property properties *** */

	private Object properties;

	/**
	 * Set value of property properties
	 *
	 * @param _value - new field value
	 */
	public void setProperties(Object _value) {
		properties = _value;
	}

	/**
	 * Get value of property properties
	 *
	 * @return - value of field properties
	 */
	public Object getProperties() {
		return properties;
	}

	/* *** Property skipOrphans *** */

	private boolean skipOrphans;

	/**
	 * Set value of property skipOrphans
	 *
	 * @param _value - new field value
	 */
	public void setSkipOrphans(boolean _value) {
		skipOrphans = _value;
	}

	/**
	 * Get value of property skipOrphans
	 *
	 * @return - value of field skipOrphans
	 */
	public boolean getSkipOrphans() {
		return skipOrphans;
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

		if (!(o instanceof Report_type) || !super.equals(o))
			return false;
		Report_type other = (Report_type) o;
		return ((getName() == null && other.getName() == null) || (getName() != null && getName()
				.equals(other.getName())))

				&& ((getDescription() == null && other.getDescription() == null) || (getDescription() != null && getDescription()
						.equals(other.getDescription())))

				&& ((getSource() == null && other.getSource() == null) || (getSource() != null && getSource()
						.equals(other.getSource())))

				&& getMode() == other.getMode()

				&& getSortField() == other.getSortField()

				&& getDescending() == other.getDescending()

				&& ((getField() == null && other.getField() == null) || (getField() != null && getField()
						.equals(other.getField())))

				&& getTimeLower() == other.getTimeLower()

				&& getTimeUpper() == other.getTimeUpper()

				&& getValueLower() == other.getValueLower()

				&& getValueUpper() == other.getValueUpper()

				&& getDayInterval() == other.getDayInterval()

				&& getTimeInterval() == other.getTimeInterval()

				&& getValueInterval() == other.getValueInterval()

				&& ((getFilter() == null && other.getFilter() == null) || (getFilter() != null && getFilter()
						.equals(other.getFilter())))

				&& getThreshold() == other.getThreshold()

				&& ((getProperties() == null && other.getProperties() == null) || (getProperties() != null && getProperties()
						.equals(other.getProperties())))

				&& getSkipOrphans() == other.getSkipOrphans()

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

		int hashCode = -1713669733
				+ ((getName() == null) ? 0 : getName().hashCode());

		hashCode = 31
				* hashCode
				+ ((getDescription() == null) ? 0 : getDescription().hashCode());

		hashCode = 31 * hashCode
				+ ((getSource() == null) ? 0 : getSource().hashCode());

		hashCode = 31 * hashCode + getMode();

		hashCode = 31 * hashCode + getSortField();

		hashCode = 31 * hashCode + (getDescending() ? 1231 : 1237);

		hashCode = 31 * hashCode
				+ ((getField() == null) ? 0 : getField().hashCode());

		hashCode = 31 * hashCode
				+ (int) (getTimeLower() ^ (getTimeLower() >>> 32));

		hashCode = 31 * hashCode
				+ (int) (getTimeUpper() ^ (getTimeUpper() >>> 32));

		hashCode = 31 * hashCode
				+ (int) (getValueLower() ^ (getValueLower() >>> 32));

		hashCode = 31 * hashCode
				+ (int) (getValueUpper() ^ (getValueUpper() >>> 32));

		hashCode = 31 * hashCode + getDayInterval();

		hashCode = 31 * hashCode + getTimeInterval();

		hashCode = 31 * hashCode + getValueInterval();

		hashCode = 31 * hashCode
				+ ((getFilter() == null) ? 0 : getFilter().hashCode());

		hashCode = 31 * hashCode + computeDoubleHash(getThreshold());

		hashCode = 31 * hashCode
				+ ((getProperties() == null) ? 0 : getProperties().hashCode());

		hashCode = 31 * hashCode + (getSkipOrphans() ? 1231 : 1237);

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

		if (field == null)
			throw new ConstraintException(ModelMessages.getString(
					ErrorMessages.ARGUMENT_NOT_NULL, "field"));

	}

}
