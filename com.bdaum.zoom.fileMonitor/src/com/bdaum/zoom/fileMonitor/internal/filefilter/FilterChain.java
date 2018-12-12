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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.fileMonitor.internal.filefilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

public class FilterChain {

	private static final ArrayList<String> EMPTYLIST = new ArrayList<String>(0);
	private WildCardFilter[] filters;
	private int baseLength = -1;

	public FilterChain(String exclusions, String prefixes, String sep, boolean includeDirs) {
		List<String> elements = fromStringList(exclusions, sep);
		init(elements.toArray(new String[elements.size()]), prefixes, includeDirs);
	}

	public static List<String> fromStringList(String stringlist, String seps) {
		ArrayList<String> result = new ArrayList<String>();
		if (stringlist != null) {
			StringTokenizer st = new StringTokenizer(stringlist, seps);
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				if (!token.isEmpty())
					result.add(token);
			}
		}
		return result;
	}

	private void init(String[] exclusions, String prefixes, boolean includeDirs) {
		int l = exclusions == null ? 0 : exclusions.length;
		filters = new WildCardFilter[l];
		if (exclusions != null)
			for (int i = 0; i < l; i++)
				filters[i] = new WildCardFilter(exclusions[i], prefixes, includeDirs);
	}

	public boolean accept(String keyword) {
		for (int i = 0; i < filters.length; i++)
			if (filters[i].accept(keyword))
				return !filters[i].isRejecting();
		return true;
	}

	public boolean accept(File file, boolean isDir) {
		for (int i = 0; i < filters.length; i++) {
			WildCardFilter filter = filters[i];
			if (baseLength >= 0 && filter.isPath()) {
				String path = file.getAbsolutePath();
				if (path.length() < baseLength)
					return !filter.isRejecting();
				path = path.substring(baseLength);
				if (File.separatorChar == '\\')
					path = path.replace('\\', '/');
				if (filter.accept(path, isDir))
					return !filter.isRejecting();
			} else if (filter.accept(file.getName(), isDir))
				return !filter.isRejecting();
		}
		return true;
	}

	public Collection<String> filter(String[] keywords) {
		if (keywords == null)
			return EMPTYLIST;
		List<String> result = new ArrayList<String>(keywords.length);
		for (String keyword : keywords)
			if (keyword != null && accept(keyword.toString()))
				result.add(keyword);
		return result;
	}

	public Collection<String> filter(Collection<String> keywords) {
		if (keywords == null)
			return EMPTYLIST;
		List<String> result = new ArrayList<String>(keywords.size());
		for (String keyword : keywords)
			if (keyword != null && accept(keyword.toString()))
				result.add(keyword);
		return result;
	}

	public void setBaseLength(int baseLength) {
		this.baseLength = baseLength;
	}

}