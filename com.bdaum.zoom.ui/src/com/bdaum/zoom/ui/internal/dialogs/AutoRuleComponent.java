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
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.operations.AutoRule;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.AutoRuleOperation;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class AutoRuleComponent {

	private List<AutoRule> autoRules = new ArrayList<AutoRule>();
	private TableViewer ruleViewer;
	private Button editAutoButton;
	private Button applyButton;
	protected IAdaptable info;
	private Composite composite;
	private Button removeAutoButton;
	private Button addAutoButton;

	public AutoRuleComponent(Composite parent, IAdaptable info) {
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
		CGroup autoGroup = UiUtilities.createGroup(composite, 2,Messages.AutoRuleComponent_title);
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
							return Format.booleanFormatter.toString(Boolean.parseBoolean(rule.getBooleanSpec()));
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
		layoutData.heightHint = 300;
		ruleViewer.getTable().setLayoutData(layoutData);
		ruleViewer.setContentProvider(ArrayContentProvider.getInstance());
		ruleViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		ruleViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				editRule();
			}
		});
		ruleViewer.setInput(autoRules);
		Composite autoButtonBar = new Composite(autoGroup, SWT.NONE);
		autoButtonBar.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		autoButtonBar.setLayout(new GridLayout(1, false));
		addAutoButton = new Button(autoButtonBar, SWT.PUSH);
		addAutoButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		addAutoButton.setText(Messages.AutoRuleComponent_add);
		addAutoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AutoRuleDialog dialog = new AutoRuleDialog(parent.getShell(), null, autoRules);
				if (dialog.open() == AutoRuleDialog.OK) {
					AutoRule rule = dialog.getRule();
					autoRules.add(rule);
					ruleViewer.add(rule);
					ruleViewer.setSelection(new StructuredSelection(rule));
					updateButtons();
				}
			}
		});
		editAutoButton = new Button(autoButtonBar, SWT.PUSH);
		editAutoButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		editAutoButton.setText(Messages.AutoRuleComponent_edit); 
		editAutoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editRule();
			}
		});
		removeAutoButton = new Button(autoButtonBar, SWT.PUSH);
		removeAutoButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		removeAutoButton.setText(Messages.AutoRuleComponent_remove);
		removeAutoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				@SuppressWarnings("unchecked")
				Iterator<AutoRule> it = ((IStructuredSelection) ruleViewer.getSelection()).iterator();
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
				updateButtons();
			}
		});
		Label sep = new Label(autoButtonBar, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		applyButton = new Button(autoButtonBar, SWT.PUSH);
		applyButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		applyButton.setText(Messages.AutoRuleComponent_apply); 
		applyButton.setToolTipText(Messages.AutoRuleComponent_apply_tooltip); 
		applyButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) ruleViewer.getSelection();
				if (!sel.isEmpty())
					OperationJob.executeSlaveOperation(
							new AutoRuleOperation(new ArrayList<AutoRule>(sel.toList()), null, null), info);
			}
		});
		updateButtons();
	}

	protected void updateButtons() {
		if (ruleViewer.getControl().isEnabled()) {
			IStructuredSelection selection = (IStructuredSelection) ruleViewer.getSelection();
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
		AutoRule rule = (AutoRule) ((IStructuredSelection) ruleViewer.getSelection()).getFirstElement();
		List<AutoRule> otherRules = new ArrayList<AutoRule>(autoRules);
		otherRules.remove(rule);
		AutoRuleDialog dialog = new AutoRuleDialog(composite.getShell(), rule, otherRules);
		if (dialog.open() == AutoRuleDialog.OK)
			ruleViewer.update(rule, null);
		updateButtons();
	}

	public void fillValues(String serialized) {
		autoRules = AutoRule.constructRules(serialized);
		ruleViewer.setInput(autoRules);
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
		updateButtons();
	}

}
