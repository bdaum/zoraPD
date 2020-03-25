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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.FieldDescriptor;
import com.bdaum.zoom.ui.internal.dialogs.Messages;

public abstract class AbstractCriterionGroup implements Listener {

	protected static final int ALLITEMS = 3;

	protected ComboViewer groupCombo;
	protected Combo fieldCombo;
	protected List<FieldDescriptor> fieldDescriptors = new ArrayList<FieldDescriptor>(150);
	protected FieldDescriptor fieldDescriptor;
	protected Button clearButton;
	protected CollectionEditGroup collectionEditGroup;
	protected Button andButton;
	protected Combo relationCombo;
	protected int groupNo;
	protected boolean enabled;
	protected int borderStyle;
	protected boolean and;
	protected int[] relationKeys;
	private List<Control> children = new ArrayList<>(10);
	
	private Label groupLab;
	private Label fieldLab;
	private Label relationLab;
	private Object groupValue;
	private String[] fieldItems;
	private FieldDescriptor fieldValue;
	private int relationValue;

	public AbstractCriterionGroup(Composite parent, CollectionEditGroup collectionEditGroup, int groupNo,
			boolean enabled, boolean and) {
		this.collectionEditGroup = collectionEditGroup;
		this.groupNo = groupNo;
		this.enabled = enabled;
		this.and = and;
		this.borderStyle = enabled ? SWT.BORDER : SWT.NONE;
		Label opLabel = new Label(parent, SWT.NONE);
		opLabel.setAlignment(SWT.RIGHT);
		opLabel.setText((and) ? Messages.CriterionGroup_and : Messages.CriterionGroup_or);
		opLabel.setVisible(groupNo > 0);
		children.add(opLabel);
		List<Object> categories = QueryField.getCategoriesAndSubgroups();
		if (enabled) {
			groupCombo = new ComboViewer(parent, SWT.READ_ONLY | borderStyle);
			Combo comboControl = groupCombo.getCombo();
			setLayoutData(comboControl, 160);
			children.add(comboControl);
			comboControl.setVisibleItemCount(categories.size());
			groupCombo.setContentProvider(ArrayContentProvider.getInstance());
			groupCombo.setFilters(new ViewerFilter[] { new GroupComboCatFilter(), getExtensionFilter() });
			groupCombo.setLabelProvider(new GroupComboLabelProvider());
			groupCombo.setInput(categories);
			comboControl.setEnabled(enabled);
			fieldCombo = new Combo(parent, SWT.READ_ONLY | borderStyle);
			setLayoutData(fieldCombo, 180);
			children.add(fieldCombo);
			fieldCombo.setVisibleItemCount(10);
			relationCombo = new Combo(parent, SWT.READ_ONLY | borderStyle);
			setLayoutData(relationCombo, 112);
			children.add(relationCombo);
			relationCombo.setEnabled(enabled);
			fieldCombo.setEnabled(enabled);
		} else {
			groupLab = new Label(parent, SWT.NONE);
			setLayoutData(groupLab, 160);
			children.add(groupLab);
			fieldLab = new Label(parent, SWT.NONE);
			setLayoutData(fieldLab, 180);
			children.add(fieldLab);
			relationLab = new Label(parent, SWT.NONE);
			setLayoutData(relationLab, 112);
			children.add(relationLab);
		}
	}
	
	@Override
	public void handleEvent(Event e) {
		validate();
		signalModification(e);
	}


	protected void validate() {
		// do nothing
	}

	protected void setLayoutData(Control control, int w) {
		GridData gridData = new GridData(w, SWT.DEFAULT);
		control.setLayoutData(gridData);
	}

	protected abstract ViewerFilter getExtensionFilter();

	public void dispose() {
		for (Control control : children)
			control.dispose();
	}

	protected void signalModification(Event ev) {
		collectionEditGroup.fireModified(ev);
	}

	protected void fillFieldCombo(FieldDescriptor extra) {
		List<FieldDescriptor> fields = new ArrayList<FieldDescriptor>();
		fieldDescriptors.clear();
		Object g = getGroupValue();
		for (String id : QueryField.getQueryFieldKeys()) {
			QueryField mainField = QueryField.findQueryField(id);
			if (mainField.hasLabel() && mainField.isQuery() && acceptField(mainField) && mainField.belongsTo(g)) {
				if (acceptRootStruct(mainField))
					fields.add(new FieldDescriptor(mainField, null));
				if (mainField.isStruct()) {
					QueryField[] qchildren = null;
					QueryField parent = QueryField.getStructParent(mainField.getType());
					if (parent != null)
						qchildren = parent.getChildren();
					if (qchildren != null)
						for (QueryField detailField : qchildren)
							fields.add(new FieldDescriptor(mainField, detailField));
				}
			}
		}
		Collections.sort(fields, new Comparator<FieldDescriptor>() {
			public int compare(FieldDescriptor o1, FieldDescriptor o2) {
				return o1.label.compareTo(o2.label);
			}
		});
		List<String> fieldLabels = new ArrayList<String>();
		if (extra != null) {
			fieldDescriptors.add(extra);
			fieldLabels.add(extra.label);
		}
		for (FieldDescriptor des : fields) {
			fieldDescriptors.add(des);
			fieldLabels.add(des.label);
		}
		fieldItems = fieldLabels.toArray(new String[fieldLabels.size()]);
		if (fieldCombo != null)
			fieldCombo.setItems(fieldItems);
	}

	protected Object getGroupValue() {
		if (enabled)
			return groupCombo.getStructuredSelection().getFirstElement();
		return groupValue;
	}

	protected abstract boolean acceptRootStruct(QueryField mainField);

	protected abstract boolean acceptField(QueryField mainField);

	protected Button createButton(Composite parent, Object label) {
		Button button = new Button(parent, SWT.PUSH);
		if (label instanceof String)
			button.setText((String) label);
		else
			button.setImage((Image) label);
		button.setAlignment(SWT.CENTER);
		button.setVisible(enabled);
		children.add(button);
		return button;
	}

	protected void initGroup(QueryField qfield) {
		setGroupValue(qfield == null ? QueryField.CATEGORY_ALL : qfield.getCategoryOrSubgroup());
	}

	protected void setGroupValue(Object value) {
		groupValue = value;
		if (enabled)
			groupCombo.setSelection(new StructuredSelection(groupValue));
		else
			groupLab.setText(String.valueOf(groupValue));
	}

	protected void addChild(Control control) {
		children.add(control);
	}

	protected void setFieldValue(FieldDescriptor des) {
		if (fieldCombo != null)
			fieldCombo.select(fieldDescriptors.indexOf(des));
		else {
			fieldValue = des;
			fieldLab.setText(des == null ? "" : des.label); //$NON-NLS-1$
		}
	}

	protected FieldDescriptor getFieldValue() {
		if (fieldCombo != null) {
			int i = fieldCombo.getSelectionIndex();
			return i < 0 ? null : fieldDescriptors.get(i);
		}
		return fieldValue;
	}

	protected void moveBelow(AbstractCriterionGroup sender) {
		if (sender != null) {
			Control anchor = sender.getRearAnchor();
			if (anchor != null) {
				for (Control child : children) {
					child.moveBelow(anchor);
					anchor = child;
				}
				anchor.getShell().layout(true, true);
			}
		}
	}

	protected Control getRearAnchor() {
		if (!children.isEmpty())
			return children.get(children.size() - 1);
		return null;
	}

	protected void setRelationValue(int rel, int[] relationKeys, String[] relationLabels) {
		int index = -1;
		if (relationCombo != null)
			relationCombo.setItems(relationLabels);
		if (relationCombo == null || relationCombo.getItemCount() >= 1)
			index = 0;
		for (int k = 0; k < relationLabels.length; k++)
			if (relationKeys[k] == rel) {
				index = k;
				break;
			}
		if (index >= 0) {
			if (relationCombo != null)
				relationCombo.select(index);
			else {
				relationValue = rel;
				relationLab.setText(relationLabels[index]);
			}
		}
	}

	protected int getRelationValue() {
		if (relationCombo != null) {
			int index = relationCombo.getSelectionIndex();
			return index < 0 ? 0 : relationKeys[index];
		}
		return relationValue;
	}

}