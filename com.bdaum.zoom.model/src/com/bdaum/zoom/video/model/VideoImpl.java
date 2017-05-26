/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2012 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.video.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.bdaum.aoModeling.runtime.Aspect;
import com.bdaum.aoModeling.runtime.ConstraintException;
import com.bdaum.aoModeling.runtime.Instrumentation;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.MediaExtensionImpl;

public class VideoImpl extends MediaExtensionImpl implements Video {

	static final long serialVersionUID = 2630966L;

	/* ----- Constructors ----- */

	public VideoImpl() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param audioBitrate
	 *            - Property
	 * @param audioBitsPerSample
	 *            - Property
	 * @param audioChannels
	 *            - Property
	 * @param audioSampleRate
	 *            - Property
	 * @param surroundMode
	 *            - Property
	 * @param audioStreamType
	 *            - Property
	 * @param duration
	 *            - Property
	 * @param bitDepth
	 *            - Property
	 * @param videoFrameRate
	 *            - Property
	 * @param videoStreamType
	 *            - Property
	 */
	public VideoImpl(int audioBitrate, int audioBitsPerSample,
			int audioChannels, int audioSampleRate, int surroundMode,
			int audioStreamType, double duration, int bitDepth,
			double videoFrameRate, int videoStreamType) {
		super();
		this.audioBitrate = audioBitrate;
		this.audioBitsPerSample = audioBitsPerSample;
		this.audioChannels = audioChannels;
		this.audioSampleRate = audioSampleRate;
		this.surroundMode = surroundMode;
		this.audioStreamType = audioStreamType;
		this.duration = duration;
		this.bitDepth = bitDepth;
		this.videoFrameRate = videoFrameRate;
		this.videoStreamType = videoStreamType;

	}

	/* ----- Initialisation ----- */

	private static List<Instrumentation> _instrumentation = new ArrayList<Instrumentation>();

	public static void attachInstrumentation(int point, Aspect aspect,
			Object extension) {
		attachInstrumentation(_instrumentation, point, aspect, extension);
	}

	public static void attachInstrumentation(int point, Aspect aspect) {
		attachInstrumentation(_instrumentation, point, aspect);
	}

	public static void attachInstrumentation(Properties properties,
			Aspect aspect) {
		attachInstrumentation(_instrumentation, VideoImpl.class, properties,
				aspect);
	}

	/* ----- Fields ----- */

	/* *** Incoming Arc zc_asset_media_parent *** */

	private Asset zc_asset_media_parent;

	/**
	 * Set value of property zc_asset_media_parent
	 *
	 * @param _value
	 *            - new field value
	 */
	public void setZc_asset_media_parent(Asset _value) {
		zc_asset_media_parent = _value;
	}

	/**
	 * Get value of property zc_asset_media_parent
	 *
	 * @return - value of field zc_asset_media_parent
	 */
	public Asset getZc_asset_media_parent() {
		return zc_asset_media_parent;
	}

	/* ----- Equality and Identity ----- */

	/**
	 * Compares the specified object with this object for primary key equality.
	 *
	 * @param o
	 *            the object to be compared with this object
	 * @return true if the specified object is key-identical to this object
	 * @see com.bdaum.aoModeling.runtime.IAsset#isKeyIdentical
	 */
	@Override
	public boolean isKeyIdentical(Object o) {
		return this == o;
	}

	/**
	 * Returns the hash code for the primary key of this object.
	 *
	 * @return the primary key hash code value
	 * @see com.bdaum.aoModeling.runtime.IAsset#keyHashCode
	 */
	@Override
	public int keyHashCode() {
		return hashCode();
	}

	/* ----- Fields ----- */

	/* *** Property audioBitrate *** */

	private int audioBitrate;

	/**
	 * Set value of property audioBitrate
	 *
	 * @param _value
	 *            - new field value
	 */
	public void setAudioBitrate(int _value) {
		audioBitrate = _value;
	}

	/**
	 * Get value of property audioBitrate
	 *
	 * @return - value of field audioBitrate
	 */
	public int getAudioBitrate() {
		return audioBitrate;
	}

	/* *** Property audioBitsPerSample *** */

	private int audioBitsPerSample;

	/**
	 * Set value of property audioBitsPerSample
	 *
	 * @param _value
	 *            - new field value
	 */
	public void setAudioBitsPerSample(int _value) {
		audioBitsPerSample = _value;
	}

	/**
	 * Get value of property audioBitsPerSample
	 *
	 * @return - value of field audioBitsPerSample
	 */
	public int getAudioBitsPerSample() {
		return audioBitsPerSample;
	}

	/* *** Property audioChannels *** */

	private int audioChannels;

	/**
	 * Set value of property audioChannels
	 *
	 * @param _value
	 *            - new field value
	 */
	public void setAudioChannels(int _value) {
		audioChannels = _value;
	}

	/**
	 * Get value of property audioChannels
	 *
	 * @return - value of field audioChannels
	 */
	public int getAudioChannels() {
		return audioChannels;
	}

	/* *** Property audioSampleRate *** */

	private int audioSampleRate;

	/**
	 * Set value of property audioSampleRate
	 *
	 * @param _value
	 *            - new field value
	 */
	public void setAudioSampleRate(int _value) {
		audioSampleRate = _value;
	}

	/**
	 * Get value of property audioSampleRate
	 *
	 * @return - value of field audioSampleRate
	 */
	public int getAudioSampleRate() {
		return audioSampleRate;
	}

	/* *** Property surroundMode *** */

	private int surroundMode;

	/**
	 * Set value of property surroundMode
	 *
	 * @param _value
	 *            - new field value
	 */
	public void setSurroundMode(int _value) {
		surroundMode = _value;
	}

	/**
	 * Get value of property surroundMode
	 *
	 * @return - value of field surroundMode
	 */
	public int getSurroundMode() {
		return surroundMode;
	}

	/* *** Property audioStreamType *** */

	private int audioStreamType;

	/**
	 * Set value of property audioStreamType
	 *
	 * @param _value
	 *            - new field value
	 */
	public void setAudioStreamType(int _value) {
		audioStreamType = _value;
	}

	/**
	 * Get value of property audioStreamType
	 *
	 * @return - value of field audioStreamType
	 */
	public int getAudioStreamType() {
		return audioStreamType;
	}

	/* *** Property duration(unit=ms) *** */

	public static final String duration__unit = "ms"; //$NON-NLS-1$

	private double duration;

	/**
	 * Set value of property duration
	 *
	 * @param _value
	 *            - new field value(unit=ms)
	 */
	public void setDuration(double _value) {
		duration = _value;
	}

	/**
	 * Get value of property duration
	 *
	 * @return - value of field duration(unit=ms)
	 */
	public double getDuration() {
		return duration;
	}

	/* *** Property bitDepth *** */

	private int bitDepth;

	/**
	 * Set value of property bitDepth
	 *
	 * @param _value
	 *            - new field value
	 */
	public void setBitDepth(int _value) {
		bitDepth = _value;
	}

	/**
	 * Get value of property bitDepth
	 *
	 * @return - value of field bitDepth
	 */
	public int getBitDepth() {
		return bitDepth;
	}

	/* *** Property videoFrameRate *** */

	private double videoFrameRate;

	/**
	 * Set value of property videoFrameRate
	 *
	 * @param _value
	 *            - new field value
	 */
	public void setVideoFrameRate(double _value) {
		videoFrameRate = _value;
	}

	/**
	 * Get value of property videoFrameRate
	 *
	 * @return - value of field videoFrameRate
	 */
	public double getVideoFrameRate() {
		return videoFrameRate;
	}

	/* *** Property videoStreamType *** */

	private int videoStreamType;

	/**
	 * Set value of property videoStreamType
	 *
	 * @param _value
	 *            - new field value
	 */
	public void setVideoStreamType(int _value) {
		videoStreamType = _value;
	}

	/**
	 * Get value of property videoStreamType
	 *
	 * @return - value of field videoStreamType
	 */
	public int getVideoStreamType() {
		return videoStreamType;
	}

	/* ----- Equality ----- */

	/**
	 * Compares the specified object with this object for equality.
	 *
	 * @param o
	 *            the object to be compared with this object.
	 * @return true if the specified object is equal to this object.
	 * @see java.lang.Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof Video) || !super.equals(o))
			return false;
		Video other = (Video) o;
		return getAudioBitrate() == other.getAudioBitrate()

		&& getAudioBitsPerSample() == other.getAudioBitsPerSample()

		&& getAudioChannels() == other.getAudioChannels()

		&& getAudioSampleRate() == other.getAudioSampleRate()

		&& getSurroundMode() == other.getSurroundMode()

		&& getAudioStreamType() == other.getAudioStreamType()

		&& getDuration() == other.getDuration()

		&& getBitDepth() == other.getBitDepth()

		&& getVideoFrameRate() == other.getVideoFrameRate()

		&& getVideoStreamType() == other.getVideoStreamType()

		;
	}

	/**
	 * Returns the hash code for this object.
	 *
	 * @return the hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 * @see java.lang.Object#equals(Object)
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {

		int hashCode = 1545109474 + getAudioBitrate();

		hashCode = 31 * hashCode + getAudioBitsPerSample();

		hashCode = 31 * hashCode + getAudioChannels();

		hashCode = 31 * hashCode + getAudioSampleRate();

		hashCode = 31 * hashCode + getSurroundMode();

		hashCode = 31 * hashCode + getAudioStreamType();

		long bits = Double.doubleToLongBits(getDuration());
		hashCode = 31 * hashCode + (int) (bits ^ (bits >>> 32));

		hashCode = 31 * hashCode + getBitDepth();

		hashCode = 31 * hashCode + (new Double(getVideoFrameRate()).hashCode());

		hashCode = 31 * hashCode + getVideoStreamType();

		return hashCode;
	}

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 *
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	@Override
	public void validateCompleteness() throws ConstraintException {

	}
}
