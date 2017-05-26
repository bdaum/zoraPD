package com.bdaum.zoom.ui.internal.widgets;

import java.util.List;

import com.bdaum.zoom.core.ISpellCheckingService.ISpellIncident;

public interface ISpellCheckingTarget {

	public abstract void setSpellingOptions(int maxSuggestions,
			int spellingOptions);

	public abstract void setSpellIncidents(List<ISpellIncident> incidents);

}