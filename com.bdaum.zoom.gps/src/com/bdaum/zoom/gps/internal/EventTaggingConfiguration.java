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
package com.bdaum.zoom.gps.internal;

import com.bdaum.zoom.fileMonitor.internal.filefilter.FilterChain;

public class EventTaggingConfiguration {

	public final int timeshift;
	public final boolean web;
	public final boolean cat;
	public final FilterChain keywordFilter;
	public final boolean keywords;

	public EventTaggingConfiguration(int timeshift, boolean web, boolean cat,
			FilterChain keywordFilter, boolean keywords) {
				this.timeshift = timeshift;
				this.web = web;
				this.cat = cat;
				this.keywordFilter = keywordFilter;
				this.keywords = keywords;
	}

}
