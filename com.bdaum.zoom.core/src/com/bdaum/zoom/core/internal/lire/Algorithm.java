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
 * (c) 2013 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.core.internal.lire;

public class Algorithm {

	private int id;
	private String name;
	private String label;
	private String description;
	private boolean essential;
	protected String providerId;

	/**
	 * @param id - algorithm ID
	 * @param name - algorithm internal name
	 * @param label - algorithm display name
	 * @param description - algorithm description
	 * @param essential - true if algorithm is always shown
	 */
	public Algorithm(int id, String name, String label, String description, boolean essential) {
		this.id = id;
		this.name = name;
		this.label = label;
		this.description = description;
		this.essential = essential;
	}

	/**
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return label;
	}

	/**
	 * @return essential
	 */
	public boolean isEssential() {
		return essential;
	}

	/**
	 * @return providerId
	 */
	public String getProviderId() {
		return providerId;
	}

	/**
	 * @return true if this is an AI algorithm
	 */
	public boolean isAi() {
		return false;
	}

	/**
	 * @return true if AI service is enabled
	 */
	public boolean isEnabled() {
		return true;
	}

	/**
	 * @return true if AI web service account is valid
	 */
	public boolean isAccountValid() {
		return true;
	}

}
