package com.bdaum.zoom.ui.internal.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.operations.AutoRule;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.AutoRuleOperation;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.dialogs.AutoRuleDialog;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class AutoPreferencePage extends AbstractPreferencePage {

	private CTabItem tabItem0;
	private TableViewer ruleViewer;
	private List<AutoRule> autoRules = new ArrayList<AutoRule>();

	private Button editAutoButton;

	private Button removeAutoButton;
	private Button applyButton;

	public AutoPreferencePage() {
		setDescription(Messages.getString("AutoPreferencePage.control_which")); //$NON-NLS-1$
	}

	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.IMPORT_PREFERENCE_PAGE);
		createTabFolder(composite, "Auto"); //$NON-NLS-1$
		tabItem0 = createTabItem(
				tabFolder,
				Messages.getString("AutoPreferencePage.automatic_collection_creation")); //$NON-NLS-1$
		tabItem0.setControl(createCollectionGroup(tabFolder));
		initTabFolder(0);
		createExtensions(tabFolder,
				"com.bdaum.zoom.ui.preferences.AutoPreferencePage"); //$NON-NLS-1$
		fillValues();
	}

	private Control createCollectionGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		CGroup autoGroup = createGroup(composite, 2,
				Messages.getString("ImportPreferencePage.automatic_creation")); //$NON-NLS-1$
		ruleViewer = new TableViewer(autoGroup, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.MULTI | SWT.V_SCROLL);
		TableViewerColumn col0 = new TableViewerColumn(ruleViewer, SWT.NONE);
		col0.getColumn().setText(
				Messages.getString("ImportPreferencePage.group")); //$NON-NLS-1$
		col0.getColumn().setWidth(180);
		col0.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AutoRule)
					return ((AutoRule) element).getQfield().getCategory()
							.toString();
				return element.toString();
			}
		});
		TableViewerColumn col1 = new TableViewerColumn(ruleViewer, SWT.NONE);
		col1.getColumn().setText(
				Messages.getString("ImportPreferencePage.field")); //$NON-NLS-1$
		col1.getColumn().setWidth(220);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AutoRule)
					return ((AutoRule) element).getQfield().getLabel();
				return element.toString();
			}
		});
		TableViewerColumn col2 = new TableViewerColumn(ruleViewer, SWT.NONE);
		col2.getColumn().setText(
				Messages.getString("ImportPreferencePage.type")); //$NON-NLS-1$
		col2.getColumn().setWidth(160);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AutoRule) {
					AutoRule rule = (AutoRule) element;
					if (rule.hasCustomIntervals())
						return Messages
								.getString("ImportPreferencePage.custom"); //$NON-NLS-1$
					switch (rule.getQfield().getAutoPolicy()) {
					case QueryField.AUTO_DISCRETE:
						if (rule.getEnumeration() != null)
							return Messages
									.getString("AutoPreferencePage.enumeration"); //$NON-NLS-1$
						return Messages
								.getString("ImportPreferencePage.discrete"); //$NON-NLS-1$
					case QueryField.AUTO_LINEAR:
						return Messages
								.getString("ImportPreferencePage.linear"); //$NON-NLS-1$
					case QueryField.AUTO_LOG:
						return Messages
								.getString("ImportPreferencePage.logarithmic"); //$NON-NLS-1$
					case QueryField.AUTO_CONTAINS:
						return Messages
								.getString("AutoPreferencePage.arbitrary"); //$NON-NLS-1$
					case QueryField.AUTO_SELECT:
						return Messages
								.getString("AutoPreferencePage.multiple"); //$NON-NLS-1$
					}
				}
				return element.toString();
			}
		});
		TableViewerColumn col3 = new TableViewerColumn(ruleViewer, SWT.NONE);
		col3.getColumn().setText(
				Messages.getString("ImportPreferencePage.parameters")); //$NON-NLS-1$
		col3.getColumn().setWidth(220);
		col3.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AutoRule) {
					AutoRule rule = (AutoRule) element;
					QueryField qfield = rule.getQfield();
					switch (qfield.getAutoPolicy()) {
					case QueryField.AUTO_DISCRETE:
						return rule.getEnumerationSpec();
					case QueryField.AUTO_CONTAINS:
					case QueryField.AUTO_SELECT:
						return rule.getValueSpec();
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
				updateAutoRuleButtons();
			}
		});
		ruleViewer.setInput(autoRules);
		Composite autoButtonBar = new Composite(autoGroup, SWT.NONE);
		autoButtonBar.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING,
				false, false));
		autoButtonBar.setLayout(new GridLayout(1, false));
		Button addAutoButton = new Button(autoButtonBar, SWT.PUSH);
		addAutoButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		addAutoButton.setText(Messages.getString("ImportPreferencePage.add")); //$NON-NLS-1$
		addAutoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AutoRuleDialog dialog = new AutoRuleDialog(getShell(), null,
						autoRules);
				if (dialog.open() == AutoRuleDialog.OK) {
					AutoRule rule = dialog.getRule();
					autoRules.add(rule);
					ruleViewer.add(rule);
					ruleViewer.setSelection(new StructuredSelection(rule));
					updateAutoRuleButtons();
				}
			}
		});
		editAutoButton = new Button(autoButtonBar, SWT.PUSH);
		editAutoButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		editAutoButton.setText(Messages.getString("ImportPreferencePage.edit")); //$NON-NLS-1$
		editAutoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AutoRule rule = (AutoRule) ((IStructuredSelection) ruleViewer
						.getSelection()).getFirstElement();
				List<AutoRule> otherRules = new ArrayList<AutoRule>(autoRules);
				otherRules.remove(rule);
				AutoRuleDialog dialog = new AutoRuleDialog(getShell(), rule,
						otherRules);
				if (dialog.open() == AutoRuleDialog.OK)
					ruleViewer.update(rule, null);
				updateAutoRuleButtons();
			}
		});
		removeAutoButton = new Button(autoButtonBar, SWT.PUSH);
		removeAutoButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		removeAutoButton.setText(Messages
				.getString("ImportPreferencePage.remove")); //$NON-NLS-1$
		removeAutoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				@SuppressWarnings("unchecked")
				Iterator<AutoRule> it = ((IStructuredSelection) ruleViewer
						.getSelection()).iterator();
				while (it.hasNext()) {
					AutoRule rule = it.next();
					int index = autoRules.indexOf(rule);
					autoRules.remove(rule);
					if (index >= autoRules.size())
						--index;
					ruleViewer.remove(rule);
					if (index >= 0)
						ruleViewer.setSelection(new StructuredSelection(
								autoRules.get(index)));
				}
				updateAutoRuleButtons();
			}
		});
		Label sep = new Label(autoButtonBar, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		applyButton = new Button(autoButtonBar, SWT.PUSH);
		applyButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		applyButton.setText(Messages.getString("AutoPreferencePage.apply_now")); //$NON-NLS-1$
		applyButton.setToolTipText(Messages
				.getString("AutoPreferencePage.apply_selected_rules")); //$NON-NLS-1$
		applyButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection sel = (IStructuredSelection) ruleViewer
						.getSelection();
				if (!sel.isEmpty())
					OperationJob.executeSlaveOperation(new AutoRuleOperation(
							new ArrayList<AutoRule>(sel.toList()), null, null),
							AutoPreferencePage.this);
			}
		});
		updateAutoRuleButtons();
		return composite;
	}

	protected void updateAutoRuleButtons() {
		IStructuredSelection selection = (IStructuredSelection) ruleViewer
				.getSelection();
		editAutoButton.setEnabled(selection.size() == 1);
		removeAutoButton.setEnabled(!selection.isEmpty());
		applyButton.setEnabled(!selection.isEmpty());
	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		autoRules = AutoRule.constructRules(preferenceStore
				.getString(PreferenceConstants.AUTORULES));
		ruleViewer.setInput(autoRules);
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		StringBuilder sb = new StringBuilder();
		for (AutoRule rule : autoRules) {
			if (sb.length() > 0)
				sb.append("\n"); //$NON-NLS-1$
			sb.append(rule.toString());
		}
		preferenceStore.setValue(PreferenceConstants.AUTORULES, sb.toString());
	}

	@Override
	public void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore
				.setValue(PreferenceConstants.AUTORULES, preferenceStore
						.getDefaultString(PreferenceConstants.AUTORULES));

	}

}
