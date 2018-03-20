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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class AllNoneGroup extends Composite {

	public static final String NONE = "none"; //$NON-NLS-1$
	public static final String ALL = "all"; //$NON-NLS-1$

	/**
	 * Generalized all/none group
	 *
	 * @param parent
	 *            - parent composite
	 * @param selectionListener
	 *            - required selection listener. event.widget.getData() is
	 *            either ALL or NONE
	 */
	public AllNoneGroup(Composite parent,
			final SelectionListener selectionListener) {
		super(parent, SWT.NONE);
		setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		setLayout(layout);
		Button selectAllButton = WidgetFactory.createPushButton(this,
				Messages.AllNoneGroup_selectAll, SWT.BEGINNING);
		selectAllButton.setData(ALL);
		selectAllButton.addSelectionListener(selectionListener);
		Button selectNoneButton = WidgetFactory.createPushButton(this,
				Messages.AllNoneGroup_selectNone, SWT.BEGINNING);
		selectNoneButton.setData(NONE);
		selectNoneButton.addSelectionListener(selectionListener);
	}

}
