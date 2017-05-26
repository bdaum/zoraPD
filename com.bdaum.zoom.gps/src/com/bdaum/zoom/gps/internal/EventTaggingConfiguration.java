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
