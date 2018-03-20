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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ai.internal.services;

import java.awt.image.BufferedImage;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.internal.ai.Prediction;
import com.bdaum.zoom.core.internal.lire.Algorithm;

public interface IAiServiceProvider {
	void setId(String id);

	void setName(String name);

	String getId();

	String getName();

	Prediction predict(byte[] jpeg);
	
	int rate(Asset asset, String opId, int maxRating, String modelId);

	void dispose();

	boolean checkAdultContent();

	boolean generateDescription();

	boolean checkFaces();

	String getTitle();

	void setLatency(int parseInt);

	int getLatency();

	float getMarkAbove();

	boolean getMarkKnownOnly();

	boolean checkCelebrities();
	
	boolean checkSharpness();
	
	boolean checkColor();

	float[] getFeatureVector(BufferedImage image);

	boolean isAccountValid();

	Algorithm getAlgorithm();

	Class<?> getFeature();

	void setFeatureId(int featureId);

	int getFeatureId();

	String[] getRatingModelIds();

	String[] getRatingModelLabels();

}
