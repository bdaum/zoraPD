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

package com.bdaum.zoom.ui.internal.preferences;

import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.preferences.AbstractFieldEditorPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;


public class KeywordPreferencePage extends AbstractFieldEditorPreferencePage {


	@Override
	protected void createFieldEditors() {
		setHelp(HelpContextIds.KEYWORD_PREFERENCE_PAGE);
		addField(new ExplanationFieldEditor("", Messages.getString("KeywordPreferencePage.keyword_descr"), getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$
		addField(new PatternListEditor(PreferenceConstants.KEYWORDFILTER,
				Messages.getString("KeywordPreferencePage._KeywordFilter"), Messages.getString("KeywordPreferencePage.keyword_filter"), Messages.getString("KeywordPreferencePage.keyword_pattern"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"*", true, "\n", getFieldEditorParent(), 60)); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
