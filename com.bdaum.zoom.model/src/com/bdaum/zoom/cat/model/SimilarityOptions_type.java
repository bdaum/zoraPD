package com.bdaum.zoom.cat.model;

import java.util.*;

import java.lang.String;

import com.bdaum.aoModeling.runtime.*;

/**
 * Generated with KLEEN Java Generator V.1.3
 * Implements asset similarityOptions
 */

/* !! This interface is not intended to modified manually !! */

@SuppressWarnings({ "unused" })
public interface SimilarityOptions_type extends AomValueChangedNotifier,
		IIdentifiableObject {

	/* ----- Fields ----- */

	/**
	 * Set value of property method
	 *
	 * @param _value - new element value
	 */
	public void setMethod(int _value);

	/**
	 * Get value of property method
	 *
	 * @return - value of field method
	 */
	public int getMethod();

	/**
	 * Set value of property maxResults
	 *
	 * @param _value - new element value
	 */
	public void setMaxResults(int _value);

	/**
	 * Get value of property maxResults
	 *
	 * @return - value of field maxResults
	 */
	public int getMaxResults();

	/**
	 * Set value of property minScore
	 *
	 * @param _value - new element value
	 */
	public void setMinScore(float _value);

	/**
	 * Get value of property minScore
	 *
	 * @return - value of field minScore
	 */
	public float getMinScore();

	/**
	 * Set value of property lastTool
	 *
	 * @param _value - new element value
	 */
	public void setLastTool(int _value);

	/**
	 * Get value of property lastTool
	 *
	 * @return - value of field lastTool
	 */
	public int getLastTool();

	/**
	 * Set value of property pencilRadius
	 *
	 * @param _value - new element value
	 */
	public void setPencilRadius(int _value);

	/**
	 * Get value of property pencilRadius
	 *
	 * @return - value of field pencilRadius
	 */
	public int getPencilRadius();

	/**
	 * Set value of property airbrushRadius
	 *
	 * @param _value - new element value
	 */
	public void setAirbrushRadius(int _value);

	/**
	 * Get value of property airbrushRadius
	 *
	 * @return - value of field airbrushRadius
	 */
	public int getAirbrushRadius();

	/**
	 * Set value of property airbrushIntensity
	 *
	 * @param _value - new element value
	 */
	public void setAirbrushIntensity(int _value);

	/**
	 * Get value of property airbrushIntensity
	 *
	 * @return - value of field airbrushIntensity
	 */
	public int getAirbrushIntensity();

	/**
	 * Set value of property pngImage
	 *
	 * @param _value - new element value
	 */
	public void setPngImage(byte[] _value);

	/**
	 * Set single element of array pngImage
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setPngImage(byte _element, int _i);

	/**
	 * Get value of property pngImage
	 *
	 * @return - value of field pngImage
	 */
	public byte[] getPngImage();

	/**
	 * Get single element of array pngImage
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array pngImage
	 */
	public byte getPngImage(int _i);

	/**
	 * Set value of property assetId
	 *
	 * @param _value - new element value
	 */
	public void setAssetId(String _value);

	/**
	 * Get value of property assetId
	 *
	 * @return - value of field assetId
	 */
	public String getAssetId();

	/**
	 * Set value of property keywords
	 *
	 * @param _value - new element value
	 */
	public void setKeywords(String[] _value);

	/**
	 * Set single element of array keywords
	 *
	 * @param _element - new element value
	 * @param _i - index of array element
	 */
	public void setKeywords(String _element, int _i);

	/**
	 * Get value of property keywords
	 *
	 * @return - value of field keywords
	 */
	public String[] getKeywords();

	/**
	 * Get single element of array keywords
	 *
	 * @param _i - index of array element
	 * @return - _i-th element of array keywords
	 */
	public String getKeywords(int _i);

	/**
	 * Set value of property keywordWeight
	 *
	 * @param _value - new element value
	 */
	public void setKeywordWeight(int _value);

	/**
	 * Get value of property keywordWeight
	 *
	 * @return - value of field keywordWeight
	 */
	public int getKeywordWeight();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
