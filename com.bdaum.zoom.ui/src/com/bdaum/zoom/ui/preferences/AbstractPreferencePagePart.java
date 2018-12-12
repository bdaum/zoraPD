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
 * (c) 2009-2014 Berthold Daum  
 */
package com.bdaum.zoom.ui.preferences;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.widgets.CGroup;

public abstract class AbstractPreferencePagePart implements IPreferencePageExtension {

	protected boolean enabled = true;
	protected Label statusField;

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.preferences.IPreferencePageExtension#getLabel()
	 */
	public String getLabel() {
		return null;
	}

	public String getTooltip() {
		return null;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.preferences.IPreferencePageExtension#fillValues()
	 */
	public void fillValues() {
		// do nothing
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.preferences.IPreferencePageExtension#performOk()
	 */
	public void performOk() {
		// do nothing
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.preferences.IPreferencePageExtension#performDefaults()
	 */
	public void performDefaults() {
		// do nothing
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.preferences.IPreferencePageExtension#validate()
	 */
	public String validate() {
		return null;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.preferences.IPreferencePageExtension#updateButtons()
	 */
	public void updateButtons() {
		// do nothing
	}

	/**
	 * Creates a labeled group
	 *
	 * @param parent
	 *            - parent composite
	 * @param columns
	 *            - number of columns within group
	 * @param label
	 *            - group label
	 * @return - group control
	 */
	protected CGroup createGroup(Composite parent, int columns, String label) {
		return UiUtilities.createGroup(parent, columns, label);
	}

	/**
	 * Creates a tab item
	 *
	 * @param tabFolder
	 *            - tab folder
	 * @param text
	 *            - tab item label
	 * @return - tab item
	 */
	protected ComboViewer createComboViewer(Composite parent, String lab, final String[] options,
			final Object labelling, boolean sort) {
		return AbstractPreferencePage.createComboViewer(parent, lab, options, labelling, sort);
	}

	protected void showStatus(String msg, boolean error) {
		statusField.setText(msg);
		statusField.setData(CSSProperties.ID, error ? CSSProperties.ERRORS : null);
		CssActivator.getDefault().setColors(statusField);
	}

}
