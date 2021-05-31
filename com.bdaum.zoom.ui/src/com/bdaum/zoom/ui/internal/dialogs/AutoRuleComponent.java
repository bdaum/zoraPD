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
 * (c) 2017-2018 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.operations.AutoRule;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.AutoRuleOperation;
import com.bdaum.zoom.ui.internal.SortColumnManager;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class AutoRuleComponent implements ISelectionChangedListener, IDoubleClickListener, Listener {

	private List<AutoRule> autoRules = new ArrayList<AutoRule>();
	private TableViewer ruleViewer;
	private Button editAutoButton;
	private Button applyButton;
	protected IAdaptable info;
	private Composite composite;
	private Button removeAutoButton;
	private Button addAutoButton;
	protected boolean cntrlDwn;
	private CheckboxTableViewer accelViewer;
	private List<String> accelerated;

	@SuppressWarnings("unused")
	public AutoRuleComponent(Composite parent, int style, IAdaptable info) {
		this.info = info;
		composite = new Composite(parent, SWT.NONE);
		Layout layout = parent.getLayout();
		if (layout instanceof GridLayout) {
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
			layoutData.horizontalSpan = ((GridLayout) layout).numColumns;
			composite.setLayoutData(layoutData);
		}
		composite.setLayout(new GridLayout());
		new Label(composite, SWT.NONE).setText(Messages.AutoRuleComponent_collections_expl);
		CGroup autoGroup = UiUtilities.createGroup(composite, 2, Messages.AutoRuleComponent_title);
		ruleViewer = new TableViewer(autoGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL);
		TableViewerColumn col0 = new TableViewerColumn(ruleViewer, SWT.NONE);
		col0.getColumn().setText(Messages.AutoRuleComponent_name);
		col0.getColumn().setWidth(100);
		col0.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AutoRule)
					return ((AutoRule) element).getName();
				return element.toString();
			}
		});
		TableViewerColumn col1 = new TableViewerColumn(ruleViewer, SWT.NONE);
		col1.getColumn().setText(Messages.AutoRuleComponent_group);
		col1.getColumn().setWidth(180);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AutoRule)
					return ((AutoRule) element).getQfield().getCategory().toString();
				return element.toString();
			}
		});
		TableViewerColumn col2 = new TableViewerColumn(ruleViewer, SWT.NONE);
		col2.getColumn().setText(Messages.AutoRuleComponent_field);
		col2.getColumn().setWidth(220);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AutoRule)
					return ((AutoRule) element).getQfield().getLabel();
				return element.toString();
			}
		});
		TableViewerColumn col3 = new TableViewerColumn(ruleViewer, SWT.NONE);
		col3.getColumn().setText(Messages.AutoRuleComponent_type);
		col3.getColumn().setWidth(160);
		col3.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AutoRule) {
					AutoRule rule = (AutoRule) element;
					if (rule.hasCustomIntervals())
						return Messages.AutoRuleComponent_custom;
					QueryField qfield = rule.getQfield();
					switch (qfield.getAutoPolicy()) {
					case QueryField.AUTO_DISCRETE:
						if (qfield.getEnumeration() != null)
							return Messages.AutoRuleComponent_enum;
						if (qfield.getType() == QueryField.T_BOOLEAN)
							return Messages.AutoRuleComponent_bool;
						return Messages.AutoRuleComponent_discrete;
					case QueryField.AUTO_LINEAR:
						return Messages.AutoRuleComponent_linear;
					case QueryField.AUTO_LOG:
						return Messages.AutoRuleComponent_exp;
					case QueryField.AUTO_CONTAINS:
						return Messages.AutoRuleComponent_arbitrary;
					case QueryField.AUTO_SELECT:
						return Messages.AutoRuleComponent_multiple;
					}
				}
				return element.toString();
			}
		});
		TableViewerColumn col4 = new TableViewerColumn(ruleViewer, SWT.NONE);
		col4.getColumn().setText(Messages.AutoRuleComponent_parms);
		col4.getColumn().setWidth(220);
		col4.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AutoRule) {
					AutoRule rule = (AutoRule) element;
					QueryField qfield = rule.getQfield();
					switch (qfield.getAutoPolicy()) {
					case QueryField.AUTO_CONTAINS:
					case QueryField.AUTO_SELECT:
						return rule.getValueSpec();
					case QueryField.AUTO_DISCRETE:
						if (qfield.getEnumeration() != null)
							return rule.getEnumerationSpec();
						if (qfield.getType() == QueryField.T_BOOLEAN)
							return Format.booleanFormatter.format(Boolean.parseBoolean(rule.getBooleanSpec()));
						//$FALL-THROUGH$
					default:
						return rule.getIntervalSpec();
					}
				}
				return element.toString();
			}
		});

		ruleViewer.getTable().setHeaderVisible(true);
		ruleViewer.getTable().setLinesVisible(true);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = (style & SWT.SHORT) != 0 ? 150 : 300;
		ruleViewer.getTable().setLayoutData(layoutData);
		ruleViewer.setContentProvider(ArrayContentProvider.getInstance());
		new SortColumnManager(ruleViewer, new int[] { SWT.UP, SWT.UP, SWT.UP, SWT.NONE, SWT.NONE }, 0);
		ruleViewer.setComparator(ZViewerComparator.INSTANCE);
		ruleViewer.getControl().addListener(SWT.KeyDown, this);
		ruleViewer.getControl().addListener(SWT.KeyUp, this);
		ruleViewer.addSelectionChangedListener(this);
		ruleViewer.addDoubleClickListener(this);
		ruleViewer.setInput(autoRules);
		Composite autoButtonBar = new Composite(autoGroup, SWT.NONE);
		autoButtonBar.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		autoButtonBar.setLayout(new GridLayout(1, false));
		addAutoButton = new Button(autoButtonBar, SWT.PUSH);
		addAutoButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		addAutoButton.setText(Messages.AutoRuleComponent_add);
		addAutoButton.addListener(SWT.Selection, this);
		editAutoButton = new Button(autoButtonBar, SWT.PUSH);
		editAutoButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		editAutoButton.setText(Messages.AutoRuleComponent_edit);
		editAutoButton.addListener(SWT.Selection, this);
		removeAutoButton = new Button(autoButtonBar, SWT.PUSH);
		removeAutoButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		removeAutoButton.setText(Messages.AutoRuleComponent_remove);
		removeAutoButton.addListener(SWT.Selection, this);
		new Label(autoButtonBar, SWT.SEPARATOR | SWT.HORIZONTAL)
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		applyButton = new Button(autoButtonBar, SWT.PUSH);
		applyButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		applyButton.setText(Messages.AutoRuleComponent_apply);
		applyButton.setToolTipText(Messages.AutoRuleComponent_apply_tooltip);
		applyButton.addListener(SWT.Selection, this);
		CGroup accelGroup = UiUtilities.createGroup(composite, 1, Messages.AutoRuleComponent_accel_candidates);
		new Label(accelGroup, SWT.WRAP).setText(Messages.AutoRuleComponent_accel_msg);
		accelViewer = CheckboxTableViewer.newCheckList(accelGroup, SWT.V_SCROLL | SWT.BORDER);
		accelViewer.getTable().setLayoutData(new GridData(400, (style & SWT.SHORT) != 0 ? 50 : 80));
		accelViewer.setContentProvider(ArrayContentProvider.getInstance());
		accelViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof QueryField)
					return ((QueryField) element).getLabel();
				return element.toString();
			}
		});
		accelViewer.setComparator(ZViewerComparator.INSTANCE);
		updateButtons();
	}

	protected void updateButtons() {
		if (ruleViewer.getControl().isEnabled()) {
			IStructuredSelection selection = ruleViewer.getStructuredSelection();
			editAutoButton.setEnabled(selection.size() == 1);
			removeAutoButton.setEnabled(!selection.isEmpty());
			applyButton.setEnabled(!selection.isEmpty());
			addAutoButton.setEnabled(true);
		} else {
			editAutoButton.setEnabled(false);
			removeAutoButton.setEnabled(false);
			applyButton.setEnabled(false);
			addAutoButton.setEnabled(false);
		}
	}

	protected void editRule() {
		AutoRule rule = (AutoRule) ruleViewer.getStructuredSelection().getFirstElement();
		List<AutoRule> otherRules = new ArrayList<AutoRule>(autoRules);
		otherRules.remove(rule);
		AutoRuleDialog dialog = new AutoRuleDialog(composite.getShell(), rule, otherRules);
		if (dialog.open() == AutoRuleDialog.OK)
			ruleViewer.update(rule, null);
		fillAccelViewer();
		updateButtons();
	}

	public void fillValues(String serialized) {
		accelerated = Core.fromStringList(
				UiActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.METADATATUNING), "\n"); //$NON-NLS-1$
		autoRules = AutoRule.constructRules(serialized);
		ruleViewer.setInput(autoRules);
		fillAccelViewer();
	}

	private void fillAccelViewer() {
		Set<QueryField> candidates = new HashSet<>();
		for (AutoRule autoRule : autoRules) {
			QueryField qfield = autoRule.getQfield();
			if (qfield.isUiField() && !qfield.isStruct() && qfield.isQuery() && !accelerated.contains(qfield.getKey()))
				candidates.add(qfield);
		}
		Object[] checkedElements = accelViewer.getCheckedElements();
		accelViewer.setInput(candidates);
		accelViewer.setCheckedElements(checkedElements);
	}

	public String getResult() {
		StringBuilder sb = new StringBuilder();
		for (AutoRule rule : autoRules) {
			if (sb.length() > 0)
				sb.append("\n"); //$NON-NLS-1$
			sb.append(rule.toString());
		}
		return sb.toString();
	}

	public Control getControl() {
		return composite;
	}

	public void setEnabled(boolean enabled) {
		ruleViewer.getControl().setEnabled(enabled);
		accelViewer.getControl().setEnabled(enabled);
		updateButtons();
	}

	public void accelerate() {
		Object[] checkedElements = accelViewer.getCheckedElements();
		if (checkedElements.length > 0) {
			for (Object element : checkedElements)
				if (element instanceof QueryField)
					accelerated.add(((QueryField) element).getKey());
			UiActivator.getDefault().getPreferenceStore().putValue(PreferenceConstants.METADATATUNING,
					Core.toStringList(accelerated, 'n'));
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		updateButtons();
		if (cntrlDwn) {
			if (editAutoButton.isEnabled())
				editRule();
			cntrlDwn = false;
		}
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		if (!cntrlDwn && editAutoButton.isEnabled())
			editRule();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.KeyDown:
			if (e.keyCode == SWT.CTRL)
				cntrlDwn = true;
			return;
		case SWT.KeyUp:
			if (e.keyCode == SWT.CTRL)
				cntrlDwn = false;
			return;
		case SWT.Selection:
			if (e.widget == addAutoButton) {
				AutoRuleDialog dialog = new AutoRuleDialog(composite.getShell(), null, autoRules);
				if (dialog.open() == AutoRuleDialog.OK) {
					AutoRule rule = dialog.getRule();
					autoRules.add(rule);
					ruleViewer.add(rule);
					ruleViewer.setSelection(new StructuredSelection(rule));
					fillAccelViewer();
					updateButtons();
				}
			} else if (e.widget == editAutoButton) {
				editRule();
			} else if (e.widget == removeAutoButton) {
				Iterator<AutoRule> it = ruleViewer.getStructuredSelection().iterator();
				while (it.hasNext()) {
					AutoRule rule = it.next();
					int index = autoRules.indexOf(rule);
					autoRules.remove(rule);
					if (index >= autoRules.size())
						--index;
					ruleViewer.remove(rule);
					if (index >= 0)
						ruleViewer.setSelection(new StructuredSelection(autoRules.get(index)));
				}
				fillAccelViewer();
				updateButtons();
			} else if (e.widget == applyButton) {
				IStructuredSelection sel = ruleViewer.getStructuredSelection();
				if (!sel.isEmpty())
					OperationJob.executeSlaveOperation(
							new AutoRuleOperation(new ArrayList<AutoRule>(sel.toList()), null, null), info, false);
			}
		}

	}

}
