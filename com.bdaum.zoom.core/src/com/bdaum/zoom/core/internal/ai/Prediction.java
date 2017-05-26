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
 * (c) 2016 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.core.internal.ai;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;

public class Prediction {

	private static NumberFormat nf = NumberFormat.getNumberInstance();
	static {
		nf.setMaximumFractionDigits(1);
	}

	public static class Token {

		public String label;
		public float score;
		private int match;
		private String category;

		public Token(String label, float score) {
			this.label = label;
			this.score = score;
		}

		public String getLabel() {
			return label;
		}

		public float getScore() {
			return score;
		}

		@Override
		public String toString() {
			return NLS.bind("{0} ({1}%)", label, nf.format(score*100)); //$NON-NLS-1$
		}

		public void setMatch(int match) {
			this.match = match;
		}

		public int getMatch() {
			return match;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public String getCategory() {
			return category;
		}

		public void setLabel(String label) {
			this.label = label;
		}

	}

	private Token[] concepts;
	private IStatus status;
	private String serviceName;
	private float adultScore = -1f;
	private String description;
	private Token[] keywords;
	private List<Rectangle> faces;
	private float racyScore = -1f;

	public Prediction(String serviceName, Token[] concepts, Token[] keywords, IStatus status) {
		this.serviceName = serviceName;
		setConcepts(concepts);
		setKeywords(keywords);
		this.status = status;
	}

	public String getServiceName() {
		return serviceName;
	}

	public Token[] getConcepts() {
		return concepts;
	}

	public void setConcepts(Token[] tokens) {
		this.concepts = fold(tokens);
	}
	
	public Token[] getKeywords() {
		return keywords;
	}

	public void setKeywords(Token[] tokens) {
		this.keywords = fold(tokens);
	}

	private static Token[] fold(Token[] tokens) {
		if (tokens == null)
			return null;
		Map<String, Token> map = new HashMap<>(tokens.length * 3 / 2);
		for (Token token : tokens) { 
			String label = token.label;
			Token alias = map.get(label);
			if (alias == null || token.score > alias.score)
				map.put(label, token);
		}
		Collection<Token> values = map.values();
		return values.toArray(new Token[values.size()]);
	}

	public IStatus getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setSafeForWork(float adultScore, float racyScore) {
		this.adultScore = adultScore;
		this.racyScore = racyScore;
	}

	public float getAdultScore() {
		return adultScore;
	}

	public float getRacyScore() {
		return racyScore;
	}

	public String getDescription() {
		return description;
	}

	public void setFaces(List<Rectangle> faces) {
		this.faces = faces;
	}

	public List<Rectangle> getFaces() {
		return faces;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
