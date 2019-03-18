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
 * (c) 2012 Berthold Daum  
 */
package com.bdaum.zoom.video.model;

import com.bdaum.aoModeling.runtime.ConstraintException;
import com.bdaum.aoModeling.runtime.IAsset;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.MediaExtension;

/**
 * Implements asset video
 */


public interface Video extends IAsset, MediaExtension {

	/*----- Operation points -----*/

	public static final int OP_$init = 0;

	public static final int OP_$dispose = 1;

	/* ----- Fields ----- */

	/**
	 * Set value of property zc_asset_media_parent
	 *
	 * @param _value - new element value
	 */
	public void setZc_asset_media_parent(Asset _value);

	/**
	 * Get value of property zc_asset_media_parent
	 *
	 * @return - value of field zc_asset_media_parent
	 */
	public Asset getZc_asset_media_parent();

	/* ----- Fields ----- */

	/**
	 * Set value of property audioBitrate
	 *
	 * @param _value - new element value
	 */
	public void setAudioBitrate(int _value);

	/**
	 * Get value of property audioBitrate
	 *
	 * @return - value of field audioBitrate
	 */
	public int getAudioBitrate();

	/**
	 * Set value of property audioBitsPerSample
	 *
	 * @param _value - new element value
	 */
	public void setAudioBitsPerSample(int _value);

	/**
	 * Get value of property audioBitsPerSample
	 *
	 * @return - value of field audioBitsPerSample
	 */
	public int getAudioBitsPerSample();

	/**
	 * Set value of property audioChannels
	 *
	 * @param _value - new element value
	 */
	public void setAudioChannels(int _value);

	/**
	 * Get value of property audioChannels
	 *
	 * @return - value of field audioChannels
	 */
	public int getAudioChannels();

	/**
	 * Set value of property audioSampleRate
	 *
	 * @param _value - new element value
	 */
	public void setAudioSampleRate(int _value);

	/**
	 * Get value of property audioSampleRate
	 *
	 * @return - value of field audioSampleRate
	 */
	public int getAudioSampleRate();

	/**
	 * Set value of property surroundMode
	 *
	 * @param _value - new element value
	 */
	public void setSurroundMode(int _value);

	/**
	 * Get value of property surroundMode
	 *
	 * @return - value of field surroundMode
	 */
	public int getSurroundMode();

	/**
	 * Set value of property audioStreamType
	 *
	 * @param _value - new element value
	 */
	public void setAudioStreamType(int _value);

	/**
	 * Get value of property audioStreamType
	 *
	 * @return - value of field audioStreamType
	 */
	public int getAudioStreamType();

	public static final String duration__unit = "ms"; //$NON-NLS-1$

	public static final String[] durationALLATTRIBUTES = new String[] { duration__unit };

	/**
	 * Set value of property duration
	 *
	 * @param _value - new element value(unit=ms)
	 */
	public void setDuration(double _value);

	/**
	 * Get value of property duration
	 *
	 * @return - value of field duration(unit=ms)
	 */
	public double getDuration();

	/**
	 * Set value of property bitDepth
	 *
	 * @param _value - new element value
	 */
	public void setBitDepth(int _value);

	/**
	 * Get value of property bitDepth
	 *
	 * @return - value of field bitDepth
	 */
	public int getBitDepth();

	/**
	 * Set value of property videoFrameRate
	 *
	 * @param _value - new element value
	 */
	public void setVideoFrameRate(double _value);

	/**
	 * Get value of property videoFrameRate
	 *
	 * @return - value of field videoFrameRate
	 */
	public double getVideoFrameRate();

	/**
	 * Set value of property videoStreamType
	 *
	 * @param _value - new element value
	 */
	public void setVideoStreamType(int _value);

	/**
	 * Get value of property videoStreamType
	 *
	 * @return - value of field videoStreamType
	 */
	public int getVideoStreamType();
	
	/**
	 * Set value of property avgBitrate
	 *
	 * @param _value - new element value
	 */
	
	public void setAvgBitrate(int _value);
	/**
	 * Get value of property avgBitrate();
	 *
	 * @return - value of field avgBitrate
	 */
	public int getAvgBitrate();

	/* ----- Validation ----- */

	/**
	 * Tests if all non-null properties and arcs have been supplied with values
	 * @throws com.bdaum.aoModeling.runtime.ConstraintException
	 */
	public void validateCompleteness() throws ConstraintException;

}
