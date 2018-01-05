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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.cat.model.meta.CategoryImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;

public class CategoryGroup {

	private Object result;
	private CheckboxTreeViewer treeViewer;
	private Button addButton;
	private Button refineButton;
	private Map<String, Category> categories;
	private final int style;

	@SuppressWarnings("unused")
	public CategoryGroup(final Composite parent, Object category, int style) {
		this.style = style;
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		Meta meta = Core.getCore().getDbManager().getMeta(true);
		categories = new HashMap<String, Category>(meta.getCategory());
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(comp, SWT.NONE);
		new Label(comp, SWT.NONE);
		treeViewer = new CheckboxTreeViewer(comp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
		expandCollapseGroup.setViewer(treeViewer);
		treeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer.setContentProvider(new CategoryContentProvider());
		treeViewer.setComparator(new CategoryComparator());
		treeViewer.setLabelProvider(new CategoryLabelProvider());
		UiUtilities.installDoubleClickExpansion(treeViewer);
		final Composite buttonGroup = new Composite(comp, SWT.NONE);
		buttonGroup.setLayout(new GridLayout());
		buttonGroup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		addButton = new Button(buttonGroup, SWT.NONE);
		addButton.setText(Messages.CategoryGroup_add);
		addButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				EditCategoryDialog inputDialog = new EditCategoryDialog(parent.getShell(), null, categories, null, null,
						false);
				if (inputDialog.open() == Window.OK) {
					String label = inputDialog.getLabel();
					CategoryImpl category = new CategoryImpl(label);
					category.setSynonyms(inputDialog.getSynonyms());
					categories.put(label, category);
					treeViewer.setInput(categories);
				}
			}
		});

		refineButton = new Button(buttonGroup, SWT.NONE);
		refineButton.setText(Messages.CategoryGroup_refine);
		refineButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Category firstElement = (Category) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
				EditCategoryDialog inputDialog = new EditCategoryDialog(parent.getShell(), null, categories, null,
						firstElement, false);
				if (inputDialog.open() == Window.OK) {
					String label = inputDialog.getLabel();
					Category subCategory = new CategoryImpl(label);
					subCategory.setSynonyms(inputDialog.getSynonyms());
					firstElement.putSubCategory(subCategory);
					treeViewer.add(firstElement, subCategory);
					treeViewer.expandToLevel(firstElement, 2);
				}
			}
		});
		treeViewer.setInput(categories);
		if (style == SWT.MULTI) {
			String[] cats = (String[]) category;
			if (cats != null)
				for (String token : cats)
					setCheckMarks(categories, token);
		} else {
			if (category != null)
				setCheckMarks(categories, (String) category);
			treeViewer.addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent event) {
					if (event.getChecked()) {
						Object element = event.getElement();
						Object[] checkedElements = treeViewer.getCheckedElements();
						for (Object object : checkedElements) {
							if (object != element)
								treeViewer.setChecked(object, false);
						}
					}
				}
			});
		}
	}

	private void setCheckMarks(Map<String, Category> catMap, String category) {
		Category cat = null;
		StringTokenizer st = new StringTokenizer(category, "."); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			cat = catMap.get(token);
			if (cat != null)
				catMap = cat.getSubCategory();
			else
				break;
		}
		if (cat != null)
			treeViewer.setChecked(cat, true);
	}

	public void commit() {
		Object[] checkedElements = treeViewer.getCheckedElements();
		if (style == SWT.SINGLE) {
			if (checkedElements.length == 0)
				result = ""; //$NON-NLS-1$
			else
				result = ((Category) checkedElements[0]).getLabel();
		} else {
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0; i < checkedElements.length; i++) {
				String label = ((Category) checkedElements[i]).getLabel();
				list.add(label);
				addSubcategories(list, label + "/", ((Category) checkedElements[i])); //$NON-NLS-1$
			}
			result = list.toArray(new String[list.size()]);
		}
	}

	public static void addSubcategories(List<String> list, String prefix, Category category) {
		Map<String, Category> subCategories = category.getSubCategory();
		if (subCategories != null)
			for (Category subCategory : subCategories.values())
				if (subCategory != null) {
					String label = prefix + subCategory.getLabel();
					list.add(label);
					addSubcategories(list, label + "/", subCategory); //$NON-NLS-1$
				}
	}

	public Object getResult() {
		return result;
	}

}
