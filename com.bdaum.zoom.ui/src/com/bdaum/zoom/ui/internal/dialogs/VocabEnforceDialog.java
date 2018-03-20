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
 * (c) 2017 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;

public class VocabEnforceDialog extends ZTitleAreaDialog {

	private List<String[]> changes;
	private CheckboxTableViewer viewer;
	private Object[] checkedElements;
	private RadioButtonGroup buttonGroup;
	private int policy;

	public VocabEnforceDialog(Shell parentShell, List<String[]> changes) {
		super(parentShell);
		this.changes = changes;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.VocabEnforceDialog_preview);
		setMessage(Messages.VocabEnforceDialog_unmark_changes);
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		viewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 400;
		layoutData.widthHint = 250;
		viewer.getTable().setLayoutData(layoutData);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String[]) {
					String[] change = (String[]) element;
					if (change[1] == null)
						return NLS.bind(Messages.VocabEnforceDialog_delete, change[0]);
					return NLS.bind(Messages.VocabEnforceDialog_replace, change[0], change[1]);
				}
				return null;
			}
		});
		viewer.setComparator(new ZViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object o1, Object o2) {
				if (o1 instanceof String[] && o2 instanceof String[])
					return UiUtilities.stringComparator.compare(((String[])o1)[0], ((String[])o2)[0]);
				return super.compare(viewer, o1, o2);
			}
		});
		viewer.setInput(changes);
		viewer.setAllChecked(true);
		new AllNoneGroup(composite, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setAllChecked(e.widget.getData() == AllNoneGroup.ALL);
			}
		});
		buttonGroup = new RadioButtonGroup(composite, null, SWT.HORIZONTAL, Messages.VocabEnforceDialog_apply_to_images, Messages.VocabEnforceDialog_apply_to_catalog);
		buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		buttonGroup.setSelection(0);
		return area;
	}
	
	@Override
	protected void okPressed() {
		checkedElements = viewer.getCheckedElements();
		policy = buttonGroup.getSelection() == 0 ? KeywordDeleteDialog.REMOVE : KeywordDeleteDialog.LEAVE;
		super.okPressed();
	}

	public List<String[]> getChanges() {
		List<String[]> result = new ArrayList<>(checkedElements.length);
		for (Object obj : checkedElements)
			if (obj instanceof String[])
				result.add((String[]) obj);
		return result;
	}
	
	public int getPolicy() {
		return policy;
	}


}
