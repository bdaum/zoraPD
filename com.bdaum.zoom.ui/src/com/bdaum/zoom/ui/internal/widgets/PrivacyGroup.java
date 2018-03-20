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

package com.bdaum.zoom.ui.internal.widgets;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.QueryField;

public class PrivacyGroup {

	private static final String RATING = "rating"; //$NON-NLS-1$
	private boolean enabled = true;
	private boolean hasPublic;
	private boolean hasModerate;
	private boolean hasPrivate;
	private RadioButtonGroup privacyButtonGroup;

	public PrivacyGroup(Composite parent, String lab, List<Asset> assets) {
		if (assets == null) {
			hasPublic = true;
			hasModerate = true;
			hasPrivate = true;
		} else
			for (Asset asset : assets) {
				switch (asset.getSafety()) {
				case QueryField.SAFETY_SAFE:
					hasPublic = true;
					break;
				case QueryField.SAFETY_MODERATE:
					hasModerate = true;
					break;
				case QueryField.SAFETY_RESTRICTED:
					hasPrivate = true;
					break;
				}
			}
		privacyButtonGroup = new RadioButtonGroup(parent, lab, SWT.HORIZONTAL, Messages.PrivacyGroup_public,
				Messages.PrivacyGroup_publicMedium, Messages.PrivacyGroup_all);
		Layout layout = parent.getLayout();
		if (layout instanceof GridLayout)
			privacyButtonGroup.setLayoutData(
					new GridData(SWT.FILL, SWT.BEGINNING, true, false, ((GridLayout) layout).numColumns, 1));
		updateButtons();
	}

	public void setSelection(int rating) {
		switch (rating) {
		case QueryField.SAFETY_SAFE:
			privacyButtonGroup.setSelection(0);
			break;
		case QueryField.SAFETY_MODERATE:
			privacyButtonGroup.setSelection(1);
			break;
		case QueryField.SAFETY_RESTRICTED:
			privacyButtonGroup.setSelection(2);
			break;
		}
		updateButtons();
	}

	private void updateButtons() {
		if (!hasPublic & privacyButtonGroup.getSelection() == 0)
			privacyButtonGroup.setSelection(1);
		if (!hasModerate & privacyButtonGroup.getSelection() == 1)
			privacyButtonGroup.setSelection(2);
		if (!hasPrivate & privacyButtonGroup.getSelection() == 2)
			privacyButtonGroup.setSelection(0);
		privacyButtonGroup.setEnabled(0, hasPublic && enabled);
		privacyButtonGroup.setEnabled(1, hasModerate && enabled);
		privacyButtonGroup.setEnabled(2, hasPrivate && enabled);
	}

	public int getSelection() {
		switch (privacyButtonGroup.getSelection()) {
		case 0:
			return QueryField.SAFETY_SAFE;
		case 1:
			return QueryField.SAFETY_MODERATE;
		default:
			return QueryField.SAFETY_RESTRICTED;
		}
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		updateButtons();
	}

	public void saveSettings(IDialogSettings settings) {
		settings.put(RATING, getSelection());
	}

	public void fillValues(IDialogSettings settings) {
		if (settings != null)
			try {
				setSelection(settings.getInt(RATING));
				return;
			} catch (NumberFormatException e) {
				// do nothing
			}
		setSelection(QueryField.SAFETY_MODERATE);
	}
}
