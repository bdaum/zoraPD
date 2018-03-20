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
 * (c) 2009-2017 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.QueryField.Category;
import com.bdaum.zoom.ui.internal.FieldDescriptor;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.dialogs.Messages;

public class SortCriterionGroup extends AbstractCriterionGroup {

	private static final FieldDescriptor extra = new FieldDescriptor(Messages.SortCriterionGroup_no_sort);
	private static final String[] relationLabels = new String[] { Messages.SortCriterionGroup_no_sort, Messages.SortCriterionGroup_ascending,
			Messages.SortCriterionGroup_descending };

	public SortCriterionGroup(final Composite parent, int groupNo, CollectionEditGroup collectionEditGroup,
			final SortCriterion crit, boolean enabled) {
		super(parent, collectionEditGroup, groupNo, enabled, true);
		relationKeys = new int[] { 0, 1, 2 };
		initGroup(crit != null ? QueryField.findQueryField(crit.getField()) : null);
		if (groupCombo != null)
			groupCombo.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					fillFieldCombo(crit);
					signalModification();
				}
			});
		createButtons(parent);
		if (fieldCombo != null)
			fieldCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					fillRelationCombo(crit);
					signalModification();
				}
			});
		if (relationCombo != null) {
			relationCombo.setVisibleItemCount(3);
			relationCombo.addSelectionListener(selectionListener);
		}
		// init
		fillFieldCombo(crit);
		signalModification();
	}

	private void fillFieldCombo(SortCriterion crit) {
		fillFieldCombo(extra);
		if (crit != null)
			setFieldValue(new FieldDescriptor(crit));
		fillRelationCombo(crit);
	}

	private void createButtons(final Composite parent) {
		andButton = createButton(parent, Messages.SortCriterionGroup_AND);
		andButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				collectionEditGroup.addSortGroup(parent, SortCriterionGroup.this, null);
				signalModification();
			}
		});
		clearButton = createButton(parent, Icons.delete.getImage());
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (groupNo == 0)
					reset();
				else {
					collectionEditGroup.removeSortGroup(SortCriterionGroup.this);
					signalModification();
				}
			}
		});
	}

	protected void reset() {
		setGroupValue(QueryField.CATEGORY_ALL);
		if (!fieldDescriptors.isEmpty())
			setFieldValue(fieldDescriptors.get(0));
		setRelationValue(relationKeys[0], relationKeys, relationLabels);
	}

	void fillRelationCombo(SortCriterion crit) {
		FieldDescriptor des = getFieldValue();
		if (des != null)
			setRelationValue(crit == null ? 0 : crit.getDescending() ? 2 : 1, relationKeys, relationLabels);
	}

	public SortCriterion getCriterion() {
		FieldDescriptor des = getFieldValue();
		int relation = getRelationValue();
		if (getGroupValue() == null || des == null || relation < 1)
			return null;
		SortCriterion crit = new SortCriterionImpl();
		crit.setField(des.qfield.getKey());
		crit.setSubfield(des.subfield == null ? null : des.subfield.getKey());
		crit.setDescending(relation == 2);
		return crit;
	}

	@Override
	protected boolean acceptField(QueryField mainField) {
		return mainField.getCard() == 1 && mainField.testFlags(QueryField.PHOTO);
	}

	@Override
	protected boolean acceptRootStruct(QueryField mainField) {
		return !mainField.isStruct();
	}

	@Override
	protected ViewerFilter getExtensionFilter() {
		return new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return element instanceof Category && !((Category) element).extension
						|| element instanceof QueryField && !((QueryField) element).getCategory().extension;
			}
		};
	}

}
