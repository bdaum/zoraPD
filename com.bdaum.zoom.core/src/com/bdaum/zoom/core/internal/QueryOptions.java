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
 * (c) 2009-2013 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.core.internal;

import com.bdaum.zoom.core.Constants;

public class QueryOptions {
	private int score = Constants.TEXTSEARCHOPTIONS_DEFAULT_MIN_SCORE;
	private int maxHits = Constants.TEXTSEARCHOPTIONS_DEFAULT_MAXCOUNT;
	private boolean networked = false;
	private int method = -1;
	private int keywordWeight = Constants.TEXTSEARCHOPTIONS_DEFAULT_WEIGHT;

	/**
	 * @return method
	 */
	public int getMethod() {
		if (method < 0)
			method = CoreActivator.getDefault().getDefaultCbirAlgorithm()
					.getId();
		return method;
	}

	/**
	 * @param method
	 *            das zu setzende Objekt method
	 */
	public void setMethod(int method) {
		this.method = method;
	}

	/**
	 * @return score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * @param score das zu setzende Objekt score
	 */
	public void setScore(int score) {
		this.score = score;
	}

	/**
	 * @return maxHits
	 */
	public int getMaxHits() {
		return maxHits;
	}

	/**
	 * @param maxHits das zu setzende Objekt maxHits
	 */
	public void setMaxHits(int maxHits) {
		this.maxHits = maxHits;
	}

	/**
	 * @return networked
	 */
	public boolean isNetworked() {
		return networked;
	}

	/**
	 * @param networked das zu setzende Objekt networked
	 */
	public void setNetworked(boolean networked) {
		this.networked = networked;
	}

	/**
	 * @return keywordWeight
	 */
	public int getKeywordWeight() {
		return keywordWeight;
	}

	/**
	 * @param keywordWeight das zu setzende Objekt keywordWeight
	 */
	public void setKeywordWeight(int keywordWeight) {
		this.keywordWeight = keywordWeight;
	}

}
