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
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.views.AbstractPropertiesView.ViewComparator;
import com.bdaum.zoom.ui.internal.views.Messages;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

public class ConfigureColumnsDialog extends ZTitleAreaDialog implements
		SelectionListener {

	List<QueryField> columnFields = new ArrayList<QueryField>();
	private Button addButton;
	private Button removeButton;
	private Button upButton;
	private Button downButton;
	private TableViewer columnsViewer;
	private TreeViewer metaViewer;

	public ConfigureColumnsDialog(Shell parentShell) {
		super(parentShell, HelpContextIds.COLUMNS_DIALOG);
	}

	@Override
	public void create() {
		super.create();
		fillValues();
		updateButtons();
		setMessage(Messages.getString("ConfigureColumnsDialog.add_new_fields")); //$NON-NLS-1$
	}

	private void fillValues() {
		IPreferenceStore preferenceStore = UiActivator.getDefault()
				.getPreferenceStore();
		String s = preferenceStore.getString(PreferenceConstants.TABLECOLUMNS);
		StringTokenizer st = new StringTokenizer(s, "\n"); //$NON-NLS-1$
		while (st.hasMoreTokens())
			columnFields.add(QueryField.findQueryField(st.nextToken()));
		columnsViewer.setInput(columnFields);
	}

	private void updateButtons() {
		boolean fieldSelected = false;
		IStructuredSelection selection = (IStructuredSelection) metaViewer
				.getSelection();
		Iterator<?> iterator = selection.iterator();
		while (iterator.hasNext()) {
			QueryField qfield = (QueryField) iterator.next();
			if (!qfield.hasChildren()) {
				fieldSelected = true;
				break;
			}
		}
		addButton.setEnabled(fieldSelected);
		selection = (IStructuredSelection) columnsViewer.getSelection();
		boolean colSelected = !selection.isEmpty();
		removeButton.setEnabled(colSelected);
		if (colSelected) {
			int min = Integer.MAX_VALUE;
			int max = -1;
			iterator = selection.iterator();
			while (iterator.hasNext()) {
				QueryField qfield = (QueryField) iterator.next();
				int index = columnFields.indexOf(qfield);
				min = Math.min(min, index);
				max = Math.max(max, index);
			}
			upButton.setEnabled(min > 0);
			downButton.setEnabled(max < columnFields.size() - 1);
		} else {
			upButton.setEnabled(false);
			downButton.setEnabled(false);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(3, false));
		metaViewer = createMetaDataGroup(composite);
		createButtonGroup(composite);
		columnsViewer = createColumnTable(composite);
		return area;
	}

	@SuppressWarnings("unused")
	private TreeViewer createMetaDataGroup(Composite composite) {
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		final TreeViewer viewer = new TreeViewer(composite, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		expandCollapseGroup.setViewer(viewer);
		viewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setLabelProvider(new MetadataLabelProvider());
		viewer.setContentProvider(new MetadataContentProvider());
		viewer.setComparator(new ViewComparator());
		viewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer aViewer, Object parentElement,
					Object element) {
				if (element instanceof QueryField) {
					QueryField qfield = (QueryField) element;
					return qfield.hasChildren() || element != QueryField.EXIF_MAKERNOTES
							&& qfield.isUiField()
							&& !columnFields.contains(element);
				}
				return false;
			}
		});
		viewer.setInput(QueryField.ALL);
		viewer.expandToLevel(2);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		return viewer;
	}

	private void createButtonGroup(Composite composite) {
		Composite buttonComp = new Composite(composite, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false));
		buttonComp.setLayout(new GridLayout(1, false));
		addButton = createButton(buttonComp, SWT.RIGHT,
				Messages.getString("ConfigureColumnsDialog.add_new_columns")); //$NON-NLS-1$
		removeButton = createButton(
				buttonComp,
				SWT.LEFT,
				Messages.getString("ConfigureColumnsDialog.remove_selected_columns")); //$NON-NLS-1$
		upButton = createButton(
				buttonComp,
				SWT.UP,
				Messages.getString("ConfigureColumnsDialog.move_selected_columns_forward")); //$NON-NLS-1$
		downButton = createButton(
				buttonComp,
				SWT.DOWN,
				Messages.getString("ConfigureColumnsDialog.move_selected_columns_back")); //$NON-NLS-1$
	}

	private Button createButton(Composite parent, int dir, String tooltip) {
		Button button = new Button(parent, SWT.ARROW | dir);
		button.setToolTipText(tooltip);
		button.addSelectionListener(this);
		return button;
	}

	private TableViewer createColumnTable(Composite composite) {
		final TableViewer viewer = new TableViewer(composite, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		return viewer;
	}

	@Override
	protected void okPressed() {
		StringBuilder sb = new StringBuilder();
		for (QueryField qfield : columnFields)
			sb.append(qfield.getId()).append('\n');
		IPreferenceStore preferenceStore = UiActivator.getDefault()
				.getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.TABLECOLUMNS,
				sb.toString());
		super.okPressed();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.widget == addButton) {
			IStructuredSelection selection = (IStructuredSelection) metaViewer
					.getSelection();
			@SuppressWarnings("rawtypes")
			Iterator iterator = selection.iterator();
			while (iterator.hasNext()) {
				QueryField qfield = (QueryField) iterator.next();
				if (!qfield.hasChildren())
					columnFields.add(qfield);
			}
			columnsViewer.setInput(columnFields);
			columnsViewer.setSelection(selection);
			metaViewer.refresh();
		} else if (e.widget == removeButton) {
			IStructuredSelection selection = (IStructuredSelection) columnsViewer
					.getSelection();
			columnFields.removeAll(selection.toList());
			columnsViewer.setInput(columnFields);
			metaViewer.refresh();
			metaViewer.setSelection(selection);
		} else if (e.widget == upButton) {
			IStructuredSelection selection = (IStructuredSelection) columnsViewer
					.getSelection();
			@SuppressWarnings("rawtypes")
			Iterator iterator = selection.iterator();
			while (iterator.hasNext()) {
				QueryField qfield = (QueryField) iterator.next();
				int index = columnFields.indexOf(qfield);
				if (index > 0) {
					columnFields.set(index, columnFields.get(index - 1));
					columnFields.set(index - 1, qfield);
				}
			}
			columnsViewer.setInput(columnFields);
		} else if (e.widget == downButton) {
			IStructuredSelection selection = (IStructuredSelection) columnsViewer
					.getSelection();
			@SuppressWarnings("rawtypes")
			Iterator iterator = selection.iterator();
			while (iterator.hasNext()) {
				QueryField qfield = (QueryField) iterator.next();
				int index = columnFields.indexOf(qfield);
				if (index < columnFields.size() - 1) {
					columnFields.set(index, columnFields.get(index + 1));
					columnFields.set(index + 1, qfield);
				}
			}
			columnsViewer.setInput(columnFields);
		}
		updateButtons();
	}

}
