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

import com.bdaum.zoom.ui.internal.IHelpContexts;

/**
 * Help context ids for the text editor.
 */
@SuppressWarnings("restriction")
public interface HelpContextIds extends IHelpContexts {

	public static final String PREFIX = GpsActivator.PLUGIN_ID + "."; //$NON-NLS-1$

	/* Pages */
	public static final String PREFERENCE_PAGE = PREFIX + "pref" + PAGE_POSTFIX; //$NON-NLS-1$
	public static final String EVENTTAGGING_PREFERENCE_PAGE = PREFIX
			+ "eventTagging" + PAGE_POSTFIX; //$NON-NLS-1$

	/* Dialogs */
	public static final String MAP_DIALOG = PREFIX + "map" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String TRACKPOINTS = PREFIX
			+ "trackpoint" + DIALOG_POSTFIX; //$NON-NLS-1$

	/* Views */
	public static final String MAP_VIEW = PREFIX + "map" + VIEW_POSTFIX; //$NON-NLS-1$

}
