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

package com.bdaum.zoom.ui.internal.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.PatternListEditor;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;

public class KeywordPreferencePage extends AbstractPreferencePage {

	private PatternListEditor patternListEditor;

	@Override
	protected void createPageContents(Composite composite) {
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText( Messages.getString("KeywordPreferencePage.keyword_descr")); //$NON-NLS-1$
		CGroup group = UiUtilities.createGroup(composite, 1, Messages.getString("KeywordPreferencePage._KeywordFilter")); //$NON-NLS-1$
		patternListEditor = new PatternListEditor(group, SWT.BORDER,
				Messages.getString("KeywordPreferencePage.keyword_filter"), //$NON-NLS-1$
				Messages.getString("KeywordPreferencePage.keyword_pattern"), "*", true, "\n"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		fillValues();
		updateButtons();
	}

	@Override
	protected void doFillValues() {
		patternListEditor.setInput(getPreferenceStore().getString(PreferenceConstants.KEYWORDFILTER));
	}
	
	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.KEYWORDFILTER,
				preferenceStore
						.getDefaultString(PreferenceConstants.KEYWORDFILTER));
	}
	
	@Override
	protected void doPerformOk() {
		getPreferenceStore().setValue(PreferenceConstants.KEYWORDFILTER, patternListEditor.getResult());
	}

}
