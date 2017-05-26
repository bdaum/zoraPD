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
package com.bdaum.zoom.operations.internal;

import java.util.List;
import java.util.Set;

import org.eclipse.swt.graphics.Rectangle;

public class CatResult {

	private String primary;
	private String[] supplemental;
	private Set<String> keywords;
	private String proposal;
	private String[] supProposals;
	private int privacy;
	private String description;
	private Rectangle imageBounds;
	private List<Rectangle> newFaces;

	public CatResult(String primary, String[] supplemental, String proposal, String[] supProposals,
			Set<String> proposedKeywords, int privacy, String description, Rectangle imageBounds, List<Rectangle> newFaces) {
		this.primary = primary;
		this.supplemental = supplemental;
		this.proposal = proposal;
		this.supProposals = supProposals;
		this.keywords = proposedKeywords;
		this.privacy = privacy;
		this.description = description;
		this.imageBounds = imageBounds;
		this.newFaces = newFaces;
	}
	
	

	public Rectangle getImageBounds() {
		return imageBounds;
	}

	public List<Rectangle> getNewFaces() {
		return newFaces;
	}
	
	public void setPrivacy(int privacy) {
		this.privacy = privacy;
	}

	public String getPrimary() {
		return primary;
	}

	public String[] getSupplemental() {
		return supplemental;
	}

	public Set<String> getKeywords() {
		return keywords;
	}

	public String getProposal() {
		return proposal;
	}

	public String[] getSupProposals() {
		return supProposals;
	}

	public int getPrivacy() {
		return privacy;
	}

	public String getDescription() {
		return description;
	}

}
