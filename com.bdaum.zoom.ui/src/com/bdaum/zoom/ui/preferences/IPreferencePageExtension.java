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
 * (c) 2014 Berthold Daum  
 */
package com.bdaum.zoom.ui.preferences;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IPreferencePageExtension {


	/**
	 * Creates contents within the specified parent composite and returns the top control of these contents
	 * @param parent - parent composite
	 * @param parentPage - parent page
	 * @return - top composite
	 */
	Control createPageContents(Composite parent,
			AbstractPreferencePage parentPage);

	/**
	 * Returns the label of the extension, typically a tab item label or a group label
	 * @return label
	 */
	String getLabel();
	
	/**
	 * Returns the tooltip of the extension or null
	 * @return label
	 */
	String getTooltip();


	/**
	 * Fills the created contents with values
	 */
	void fillValues();

	/**
	 * Retrieves the values from the created contents and writes it to the preference store
	 */
	void performOk();


	/**
	 * Dispose resources
	 */
	void performCancel();

	/**
	 * Overwrites the non-default values with the default values of the contributing preferences
	 */
	void performDefaults();

	/**
	 * Validates the created contents
	 * @return - errormessage or null
	 */
	String validate();

	/**
	 * Updates any buttons in the created contents
	 */
	void updateButtons();

	/**
	 * Set the enablement state of the whhole page part
	 * @param enabled
	 */
	void setEnabled(boolean enabled);
}
