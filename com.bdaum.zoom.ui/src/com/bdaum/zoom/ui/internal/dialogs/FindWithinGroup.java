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

package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class FindWithinGroup {

	private static final String FIND_WITHIN = "findWithin"; //$NON-NLS-1$
	private CheckboxButton checkButton;
	private SmartCollectionImpl parentCollection;
	private Composite composite;

	public FindWithinGroup(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		Layout layout = parent.getLayout();
		if (layout instanceof GridLayout)
			layoutData.horizontalSpan = ((GridLayout) layout).numColumns;
		composite.setLayoutData(layoutData);
		composite.setLayout(new GridLayout(2, false));
		checkButton = WidgetFactory.createCheckButton(composite,
				Messages.FindWithinGroup_find_within_current_collection, null);
		checkButton.setEnabled(canFindWithin());
	}
	
	public void addListener(int type, Listener listener) {
		checkButton.addListener(type, listener);
	}
	
	public void removeListener(int type, Listener listener) {
		checkButton.removeListener(type, listener);
	}


	private boolean canFindWithin() {
		IAssetProvider assetProvider = Core.getCore().getAssetProvider();
		if (assetProvider != null) {
			parentCollection = assetProvider.getParentCollection();
			return parentCollection != null;
		}
		return false;
	}

	public SmartCollection getParentCollection() {
		return (checkButton.isEnabled() && checkButton.getSelection()) ? parentCollection
				: null;
	}

	public void setBounds(int x, int y) {
		composite.setBounds(x, y, 350, 30);
		composite.layout();
	}

	public void fillValues(IDialogSettings settings) {
		checkButton.setSelection(settings.getBoolean(FIND_WITHIN));
	}

	public void saveValues(IDialogSettings settings) {
		settings.put(FIND_WITHIN, checkButton.getSelection());
	}

	public Rectangle getBounds() {
		return composite.getBounds();
	}

	public void setSelection(boolean within) {
		checkButton.setSelection(within);
	}

	public boolean getSelection() {
		return checkButton.getSelection();
	}

}
