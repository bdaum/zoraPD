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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.aoModeling.runtime.AomMap;
import com.bdaum.zoom.cat.model.meta.Category;
import com.bdaum.zoom.cat.model.meta.CategoryImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;

public class SupplementalCategoryGroup {

	private static class SupplementalCategoryContentProvider implements ITreeContentProvider {

		private static final Object[] EMPTY = new Object[0];

		public void dispose() {
			// do nothing
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}

		public Object[] getElements(Object inputElement) {
			return inputElement instanceof Collection<?> ? ((Collection<?>) inputElement).toArray() : new Object[0];
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Category) {
				Map<String, Category> subCategories = ((Category) parentElement).getSubCategory();
				if (subCategories != null) {
					List<Category> children = new ArrayList<>(subCategories.size());
					for (Category cat : subCategories.values())
						if (cat != null)
							children.add(cat);
					return children.toArray();
				}
			}
			return EMPTY;
		}

		public Object getParent(Object element) {
			return element instanceof Category ? ((Category) element).getCategory_subCategory_parent() : null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof Category) {
				Map<String, Category> subCategories = ((Category) element).getSubCategory();
				return subCategories != null && !subCategories.isEmpty();
			}
			return false;
		}
	}

	private BagChange<String> result;
	private TreeViewer treeViewer;
	private Button addButton;
	private Button refineButton;
	private List<Category> supCategories = new ArrayList<Category>();
	private Button deleteButton;
	private final String[] supplemental;

	@SuppressWarnings("unused")
	public SupplementalCategoryGroup(final Composite parent, String[] supplemental) {
		final Map<String, Category> categories = Core.getCore().getDbManager().getMeta(true).getCategory();
		this.supplemental = supplemental;
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		treeViewer = new TreeViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
		expandCollapseGroup.setViewer(treeViewer);
		treeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		treeViewer.setContentProvider(new SupplementalCategoryContentProvider());
		treeViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Category)
					return ((Category) element).getLabel();
				return element.toString();
			}
		});
		UiUtilities.installDoubleClickExpansion(treeViewer);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		final Composite buttonGroup = new Composite(composite, SWT.NONE);
		buttonGroup.setLayout(new GridLayout());
		buttonGroup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		addButton = new Button(buttonGroup, SWT.NONE);
		addButton.setText(Messages.SupplementalCategoryGroup_add);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EditCategoryDialog inputDialog = new EditCategoryDialog(parent.getShell(), null, categories,
						supCategories, null, false);
				if (inputDialog.open() == Window.OK) {
					String label = inputDialog.getLabel();
					Category category = new CategoryImpl(label);
					category.setSynonyms(inputDialog.getSynonyms());
					supCategories.add(category);
					treeViewer.setInput(supCategories);
					updateButtons();
				}
			}
		});
		refineButton = new Button(buttonGroup, SWT.NONE);
		refineButton.setText(Messages.SupplementalCategoryGroup_refine);
		refineButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Category parentNode = (Category) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
				EditCategoryDialog inputDialog = new EditCategoryDialog(parent.getShell(), null, categories,
						supCategories, parentNode, false);
				if (inputDialog.open() == Window.OK) {
					String label = inputDialog.getLabel();
					Category subCategory = new CategoryImpl(label);
					subCategory.setSynonyms(inputDialog.getSynonyms());
					subCategory.setCategory_subCategory_parent(parentNode);
					treeViewer.add(parentNode, subCategory);
					treeViewer.expandToLevel(parentNode, 2);
					updateButtons();
				}
			}
		});
		deleteButton = new Button(buttonGroup, SWT.NONE);
		deleteButton.setText(Messages.SupplementalCategoryGroup_delete);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Category node = (Category) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();
				if (node.getCategory_subCategory_parent() == null)
					supCategories.remove(node);
				else
					node.getCategory_subCategory_parent().removeSubCategory(node.getLabel());
				treeViewer.remove(node);
				updateButtons();
			}
		});
		for (String supp : supplemental) {
			Category root = expandCategory(supp);
			if (root != null)
				supCategories.add(root);
		}
		treeViewer.setInput(supCategories);
		updateButtons();
	}

	protected void updateButtons() {
		boolean enabled = !treeViewer.getSelection().isEmpty();
		deleteButton.setEnabled(enabled);
		refineButton.setEnabled(enabled);
	}

	private static Category expandCategory(String supp) {
		StringTokenizer st = new StringTokenizer(supp, "/"); //$NON-NLS-1$
		Category parent = null;
		Category root = null;
		lp: while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (parent != null) {
				AomMap<String, Category> subCategories = parent.getSubCategory();
				if (subCategories != null)
					for (Category child : subCategories.values())
						if (child != null && token.equals(child.getLabel())) {
							parent = child;
							continue lp;
						}
				Category newCat = new CategoryImpl(token);
				newCat.setCategory_subCategory_parent(parent);
				parent = newCat;
			} else
				root = parent = new CategoryImpl(token);
		}
		return root;
	}

	public void commit() {
		List<String> cats = new ArrayList<String>();
		for (Category node : supCategories)
			flatten(node, cats, ""); //$NON-NLS-1$
		List<String> oldCats = Arrays.asList(supplemental);
		Set<String> added = new HashSet<String>(cats);
		added.removeAll(oldCats);
		Set<String> removed = new HashSet<String>(oldCats);
		removed.removeAll(cats);
		result = new BagChange<String>(added, null, removed, cats.toArray(new String[cats.size()]));
	}

	private void flatten(Category node, List<String> cats, String prefix) {
		AomMap<String, Category> subCategories = node.getSubCategory();
		if (subCategories != null) {
			if (subCategories.isEmpty())
				cats.add(prefix + node.getLabel());
			else
				for (Category child : subCategories.values())
					if (child != null)
						flatten(child, cats, prefix + node.getLabel() + '/');
		}
	}

	public BagChange<String> getResult() {
		return result;
	}

}
