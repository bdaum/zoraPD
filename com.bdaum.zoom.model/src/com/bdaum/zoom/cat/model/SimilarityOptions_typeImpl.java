package com.bdaum.zoom.cat.model;

import java.util.*;
import java.lang.String;
import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset similarityOptions
 */

/* !! This class is not intended to be modified manually !! */

@SuppressWarnings({ "unused" })
public class SimilarityOptions_typeImpl extends AomObject implements
		SimilarityOptions_type {

	static final long serialVersionUID = -2410413984L;

	/* ----- Constructors ----- */

	public SimilarityOptions_typeImpl() {
		super();
	}

	/**
	 * Constructor
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
	public SimilarityOptions_typeImpl(int method, int maxResults,
			float minScore, int lastTool, int pencilRadius, int airbrushRadius,
			int airbrushIntensity, String assetId, int keywordWeight) {
		super();
		this.method = method;
		this.maxResults = maxResults;
		this.minScore = minScore;
		this.lastTool = lastTool;
		this.pencilRadius = pencilRadius;
		this.airbrushRadius = airbrushRadius;
		this.airbrushIntensity = airbrushIntensity;
		this.assetId = assetId;
		this.keywordWeight = keywordWeight;

	}

	/* ----- Fields ----- */

	/* *** Property method *** */

	private int method;

	/**
	 * Set value of property method
	 *
	 * @param _value - new field value
	 */
	public void setMethod(int _value) {
		method = _value;
	}

	/**
	 * Get value of property method
	 *
	 * @return - value of field method
	 */
	public int getMethod() {
		return method;
	}

	/* *** Property maxResults *** */

	private int maxResults;

	/**
	 * Set value of property maxResults
	 *
	 * @param _value - new field value
	 */
	public void setMaxResults(int _value) {
		maxResults = _value;
	}

	/**
	 * Get value of property maxResults
	 *
	 * @return - value of field maxResults
	 */
	public int getMaxResults() {
		return maxResults;
	}

	/* *** Property minScore *** */

	private float minScore;

	/**
	 * Set value of property minScore
	 *
	 * @param _value - new field value
	 */
	public void setMinScore(float _value) {
		minScore = _value;
	}

	/**
	 * Get value of property minScore
	 *
	 * @return - value of field minScore
	 */
	public float getMinScore() {
		return minScore;
	}

	/* *** Property lastTool *** */

	private int lastTool;

	/**
	 * Set value of property lastTool
	 *
	 * @param _value - new field value
	 */
	public void setLastTool(int _value) {
		lastTool = _value;
	}

	/**
	 * Get value of property lastTool
	 *
	 * @return - value of field lastTool
	 */
	public int getLastTool() {
		return lastTool;
	}

	/* *** Property pencilRadius *** */

	private int pencilRadius;

	/**
	 * Set value of property pencilRadius
	 *
	 * @param _value - new field value
	 */
	public void setPencilRadius(int _value) {
		pencilRadius = _value;
	}

	/**
	 * Get value of property pencilRadius
	 *
	 * @return - value of field pencilRadius
	 */
	public int getPencilRadius() {
		return pencilRadius;
	}

	/* *** Property airbrushRadius *** */

	private int airbrushRadius;

	/**
	 * Set value of property airbrushRadius
	 *
	 * @param _value - new field value
	 */
	public void setAirbrushRadius(int _value) {
		airbrushRadius = _value;
	}

	/**
	 * Get value of property airbrushRadius
	 *
	 * @return - value of field airbrushRadius
	 */
	public int getAirbrushRadius() {
		return airbrushRadius;
	}

	/* *** Property airbrushIntensity *** */

	private int airbrushIntensity;

	/**
	 * Set value of property airbrushIntensity
	 *
	 * @param _value - new field value
	 */
	public void setAirbrushIntensity(int _value) {
		airbrushIntensity = _value;
	}

	/**
	 * Get value of property airbrushIntensity
	 *
	 * @return - value of field airbrushIntensity
	 */
	public int getAirbrushIntensity() {
		return airbrushIntensity;
	}

	/* *** Property pngImage *** */

	private byte[] pngImage = new byte[0];

	/**
	 * Set value of property pngImage
	 *
	 * @param _value - new element value
	 */
	public void setPngImage(byte[] _value) {
		pngImage = _value;
	}

	/**
	 * Set single element of array pngImage
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setPngImage(byte _element, int _i) {
		pngImage[_i] = _element;
	}

	/**
	 * Get value of property pngImage
	 *
	 * @return - value of field pngImage
	 */
	public byte[] getPngImage() {
		return pngImage;
	}

	/**
	 * Get single element of array pngImage
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array pngImage
	 */
	public byte getPngImage(int _i) {
		return pngImage[_i];
	}

	/* *** Property assetId *** */

	private String assetId;

	/**
	 * Set value of property assetId
	 *
	 * @param _value - new field value
	 */
	public void setAssetId(String _value) {
		assetId = _value;
	}

	/**
	 * Get value of property assetId
	 *
	 * @return - value of field assetId
	 */
	public String getAssetId() {
		return assetId;
	}

	/* *** Property keywords *** */

	private String[] keywords = new String[0];

	/**
	 * Set value of property keywords
	 *
	 * @param _value - new element value
	 */
	public void setKeywords(String[] _value) {
		keywords = _value;
	}

	/**
	 * Set single element of array keywords
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setKeywords(String _element, int _i) {
		keywords[_i] = _element;
	}

	/**
	 * Get value of property keywords
	 *
	 * @return - value of field keywords
	 */
	public String[] getKeywords() {
		return keywords;
	}

	/**
	 * Get single element of array keywords
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array keywords
	 */
	public String getKeywords(int _i) {
		return keywords[_i];
	}

	/* *** Property keywordWeight *** */

	private int keywordWeight;

	/**
	 * Set value of property keywordWeight
	 *
	 * @param _value - new field value
	 */
	public void setKeywordWeight(int _value) {
		keywordWeight = _value;
	}

	/**
	 * Get value of property keywordWeight
	 *
	 * @return - value of field keywordWeight
	 */
	public int getKeywordWeight() {
		return keywordWeight;
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

		if (!(o instanceof SimilarityOptions_type) || !super.equals(o))
			return false;
		SimilarityOptions_type other = (SimilarityOptions_type) o;
		return getMethod() == other.getMethod()

				&& getMaxResults() == other.getMaxResults()

				&& getMinScore() == other.getMinScore()

				&& getLastTool() == other.getLastTool()

				&& getPencilRadius() == other.getPencilRadius()

				&& getAirbrushRadius() == other.getAirbrushRadius()

				&& getAirbrushIntensity() == other.getAirbrushIntensity()

				&& ((getPngImage() == null && other.getPngImage() == null) || (getPngImage() != null && getPngImage()
						.equals(other.getPngImage())))

				&& ((getAssetId() == null && other.getAssetId() == null) || (getAssetId() != null && getAssetId()
						.equals(other.getAssetId())))

				&& ((getKeywords() == null && other.getKeywords() == null) || (getKeywords() != null && getKeywords()
						.equals(other.getKeywords())))

				&& getKeywordWeight() == other.getKeywordWeight()

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

		int hashCode = 1247616418 + getMethod();

		hashCode = 31 * hashCode + getMaxResults();

		hashCode = 31 * hashCode + computeDoubleHash(getMinScore());

		hashCode = 31 * hashCode + getLastTool();

		hashCode = 31 * hashCode + getPencilRadius();

		hashCode = 31 * hashCode + getAirbrushRadius();

		hashCode = 31 * hashCode + getAirbrushIntensity();

		hashCode = 31 * hashCode
				+ ((getPngImage() == null) ? 0 : getPngImage().hashCode());

		hashCode = 31 * hashCode
				+ ((getAssetId() == null) ? 0 : getAssetId().hashCode());

		hashCode = 31 * hashCode
				+ ((getKeywords() == null) ? 0 : getKeywords().hashCode());

		hashCode = 31 * hashCode + getKeywordWeight();

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

	}

}
