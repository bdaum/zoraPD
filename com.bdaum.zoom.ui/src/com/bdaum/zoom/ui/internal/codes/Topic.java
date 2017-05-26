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
 * (c) 2011 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.codes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;

public class Topic {

	private String code;
	private String name;
	private String description;
	private List<Topic> subTopics;
	private Topic parent;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Topic && ((Topic) obj).getCode().equals(code);
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the subTopics
	 */
	public List<Topic> getSubTopics() {
		return subTopics;
	}

	public boolean addSubTopic(Topic subTopic) {
		if (subTopics == null)
			subTopics = new ArrayList<Topic>();
		subTopic.parent = this;
		return subTopics.add(subTopic);
	}

	public Topic getParent() {
		return parent;
	}

	public boolean isShown(WildCardFilter filter, boolean deep,
			Set<String> exclusions) {
		if (filter == null && exclusions == null)
			return true;
		if (subTopics != null && deep) {
			for (Topic subTopic : subTopics)
				if (subTopic.isShown(filter, deep, exclusions))
					return true;
		}
		return (exclusions != null && exclusions.contains(code)) ? false
				: filter == null || filter.accept(name);
	}
}
