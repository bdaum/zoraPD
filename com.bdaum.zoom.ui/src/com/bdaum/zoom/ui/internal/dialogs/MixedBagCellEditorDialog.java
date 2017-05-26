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
 * (c) 2012 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.UiUtilities;

public class MixedBagCellEditorDialog extends AbstractListCellEditorDialog {

	private Text viewer;
	private boolean multiple;

	public MixedBagCellEditorDialog(Shell parentShell, Object value,
			QueryField qfield, boolean multiple) {
		super(parentShell, value, qfield);
		this.multiple = multiple;
	}

	@Override
	public void create() {
		super.create();
		setMessage(Messages.ListCellEditorDialog_enter_each_item_on_separate_line
				+ (multiple ? "\n" + Messages.MixedBagCellEditorDialog_only_common_items : "")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		viewer = new Text(parent, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 50;
		viewer.setLayoutData(layoutData);
		viewer.setText(UiUtilities.csv(value, qfield.getType(), "\n")); //$NON-NLS-1$
		return comp;
	}

	@Override
	protected void okPressed() {
		List<String> items = Core.fromStringList(viewer.getText(), "\n"); //$NON-NLS-1$
		List<String> oldItems = Arrays.asList((String[]) value);
		Set<String> added = new HashSet<String>(items);
		added.removeAll(oldItems);
		Set<String> removed = new HashSet<String>(oldItems);
		removed.removeAll(items);
		value = new BagChange<String>(added, null,
				removed, items.toArray(new String[items.size()]));
		super.okPressed();
	}

}