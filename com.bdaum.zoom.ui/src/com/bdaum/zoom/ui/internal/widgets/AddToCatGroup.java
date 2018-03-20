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

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class AddToCatGroup {

	private static final String ADD_TO_CATALOG = "addToCatalog"; //$NON-NLS-1$
	private static final String ADD_TO_WATCHED = "addToWatched"; //$NON-NLS-1$
	private CheckboxButton addButton;
	private boolean addEnabled = true;
	private CheckboxButton watchButton;
	private boolean watchEnabled;

	public AddToCatGroup(Composite parent) {
		addButton = WidgetFactory.createCheckButton(parent,
				Messages.AddToCatGroup_add_exported_to_cat, null);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		watchButton = WidgetFactory.createCheckButton(parent,
				Messages.AddToCatGroup_add_to_watched, null);
		updateButtons();
	}

	public void setSelection(boolean add, boolean watch) {
		addButton.setSelection(add);
		addButton.setSelection(watch);
		updateButtons();
	}

	private void updateButtons() {
		addButton.setEnabled(addEnabled);
		watchButton.setEnabled(watchEnabled && addButton.getSelection());
	}

	public boolean getAddSelection() {
		return addButton.isEnabled() && addButton.getSelection();
	}

	public boolean getWatchSelection() {
		return watchButton.isEnabled() && watchButton.getSelection();
	}


	public void setEnabled(boolean add, boolean watch) {
		this.addEnabled = add;
		this.watchEnabled = watch;
		updateButtons();
	}

	public void fillValues(IDialogSettings settings) {
		addButton.setSelection(settings.getBoolean(ADD_TO_CATALOG));
		watchButton.setSelection(settings.getBoolean(ADD_TO_WATCHED));
	}

	public void saveValues(IDialogSettings settings) {
		settings.put(ADD_TO_CATALOG, addButton.getSelection());
		settings.put(ADD_TO_WATCHED, addButton.getSelection());
	}

}
