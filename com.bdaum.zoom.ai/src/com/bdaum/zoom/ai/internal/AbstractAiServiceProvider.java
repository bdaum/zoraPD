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
 * (c) 2017 Berthold Daum  
 */
package com.bdaum.zoom.ai.internal;

import java.awt.image.BufferedImage;

import com.bdaum.zoom.ai.internal.services.IAiServiceProvider;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.internal.lire.Algorithm;

public abstract class AbstractAiServiceProvider implements IAiServiceProvider {

	private String id;
	private String name;
	private int latency;
	private int featureId;
	private String[] ratingModelIds;
	private String[] ratingModelLabels;

	@Override
	public int rate(Asset asset, String opId, int maxRating, String modelId) {
		return -1;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setLatency(int latency) {
		this.latency = latency;
	}

	public int getLatency() {
		return latency;
	}

	@Override
	public boolean checkAdultContent() {
		return false;
	}

	public boolean checkCelebrities() {
		return false;
	}

	@Override
	public boolean generateDescription() {
		return false;
	}

	@Override
	public boolean checkFaces() {
		return false;
	}

	@Override
	public boolean checkSharpness() {
		return false;
	}

	@Override
	public boolean checkColor() {
		return false;
	}

	@Override
	public float[] getFeatureVector(BufferedImage image) {
		return null;
	}

	@Override
	public Algorithm getAlgorithm() {
		return null;
	}

	@Override
	public Class<?> getFeature() {
		return null;
	}

	@Override
	public int getFeatureId() {
		return featureId;
	}

	@Override
	public void setFeatureId(int featureId) {
		this.featureId = featureId;
	}

	public void setRatingModelIds(String[] ratingModelIds) {
		this.ratingModelIds = ratingModelIds;
	}

	public void setRatingModelLabels(String[] ratingModelLabels) {
		this.ratingModelLabels = ratingModelLabels;
	}

	@Override
	public String[] getRatingModelIds() {
		return ratingModelIds;
	}

	@Override
	public String[] getRatingModelLabels() {
		return ratingModelLabels;
	}

}