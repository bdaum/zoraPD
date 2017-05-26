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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.net.communities.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;

public class AlbumDescriptor {

	private final String name;
	private String description;
	private Set<String> tags;

	public AlbumDescriptor(String name, String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AlbumDescriptor)
			return name.equals(((AlbumDescriptor) obj).getName());
		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String[] getTags() {
		return tags.toArray(new String[tags.size()]);
	}

	public void addTags(String[] keywords) {
		if (tags == null)
			tags = new HashSet<String>();
		FilterChain keywordFilter = QueryField.getKeywordFilter();
		Collection<String> filtered = keywordFilter.filter(keywords);
		for (String kw : filtered)
			if (kw != null)
				tags.add(kw.toString());
	}

}
