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

package com.bdaum.zoom.lal.internal.lire.ui.dialogs;

import com.bdaum.zoom.ui.internal.IHelpContexts;

@SuppressWarnings("restriction")
public interface HelpContextIds extends IHelpContexts {
	//	public static final String PREFIX = UiActivator.PLUGIN_ID + "."; //$NON-NLS-1$
	public static final String PREFIX = "com.bdaum.zoom.lireAndLucene.";//$NON-NLS-1$



	public static final String SEARCHSIMILAR_DIALOG = PREFIX
			+ "searchSimilar" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String TEXT_SEARCH_DIALOG = PREFIX
			+ "textSearch" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String CONFIGURESIMULARITY_DIALOG = PREFIX
			+ "configureSimilarity" + DIALOG_POSTFIX; //$NON-NLS-1$

	public static final String CONFIGURESEARCH_DIALOG = PREFIX
			+ "configureSearch" + DIALOG_POSTFIX; //$NON-NLS-1$


}
