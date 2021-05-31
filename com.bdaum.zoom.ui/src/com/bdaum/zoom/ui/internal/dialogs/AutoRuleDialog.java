package com.bdaum.zoom.ui.internal.dialogs;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.operations.AutoRule;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.widgets.GroupComboCatFilter;
import com.bdaum.zoom.ui.internal.widgets.GroupComboLabelProvider;

@SuppressWarnings("restriction")
public class AutoRuleDialog extends ZTitleAreaDialog
		implements Listener, ICheckStateListener, ISelectionChangedListener {

	private static NumberFormat af = NumberFormat.getNumberInstance();

	private AutoRule rule;
	private ComboViewer groupCombo;
	private ComboViewer fieldCombo;
	private List<QueryField> fields = new ArrayList<QueryField>(50);
	private Label explanation;
	private Text intervalField;
	private Label resultingIntervals;
	private final List<AutoRule> autoRules;
	private Composite stack;
	private Composite intervalGroup;
	private Composite enumGroup;
	private CheckboxTableViewer enumViewer;
	private QueryField qfield;

	private Composite selectGroup;

	private Text valueField;

	private StackLayout stackLayout;

	private Composite comp;

	private Text nameField;

	public AutoRuleDialog(Shell parentShell, AutoRule rule, List<AutoRule> autoRules) {
		super(parentShell, HelpContextIds.CLUSTER_DIALOG);
		this.rule = rule;
		this.autoRules = autoRules;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.AutoRuleDialog_rule_for_auto_creation);
		setMessage(Messages.AutoRuleDialog_select_a_metatdata_field);
		fillValues();
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(4, false));
		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.AutoRuleDialog_name);
		nameField = new Text(comp, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		nameField.addListener(SWT.Modify, this);
		new Label(comp, SWT.NONE).setText(Messages.AutoRuleDialog_group);
		List<Object> categories = QueryField.getCategoriesAndSubgroups();
		groupCombo = new ComboViewer(comp, SWT.READ_ONLY | SWT.BORDER);
		Combo comboControl = groupCombo.getCombo();
		comboControl.setVisibleItemCount(categories.size());
		groupCombo.setContentProvider(ArrayContentProvider.getInstance());
		groupCombo.setFilters(new ViewerFilter[] { new GroupComboCatFilter() });
		groupCombo.setLabelProvider(new GroupComboLabelProvider());
		groupCombo.setInput(categories);
		groupCombo.getControl().setLayoutData(new GridData(100, SWT.DEFAULT));
		groupCombo.addSelectionChangedListener(this);
		new Label(comp, SWT.NONE).setText(Messages.AutoRuleDialog_field);
		fieldCombo = new ComboViewer(comp, SWT.READ_ONLY | SWT.BORDER);
		comboControl = fieldCombo.getCombo();
		comboControl.setVisibleItemCount(10);
		comboControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fieldCombo.setContentProvider(ArrayContentProvider.getInstance());
		fieldCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof QueryField)
					return ((QueryField) element).getLabel();
				return super.getText(element);
			}
		});
		if (autoRules != null)
			fieldCombo.setFilters(new ViewerFilter[] { new ViewerFilter() {
				@Override
				public boolean select(Viewer viewer, Object parentElement, Object element) {
					if (element instanceof QueryField) {
						for (AutoRule rule : autoRules)
							if (rule.getQfield() == element)
								return false;
						return true;
					}
					return false;
				}
			} });
		fieldCombo.setComparator(ZViewerComparator.INSTANCE);
		fieldCombo.addSelectionChangedListener(this);
		new Label(comp, SWT.NONE).setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 4, 1));
		explanation = new Label(comp, SWT.WRAP);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 4, 1);
		layoutData.heightHint = 46;
		layoutData.widthHint = 500;
		explanation.setLayoutData(layoutData);

		stack = new Composite(comp, SWT.NONE);
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);
		intervalGroup = new Composite(stack, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		intervalGroup.setLayout(layout);

		new Label(intervalGroup, SWT.NONE).setText(Messages.AutoRuleDialog_interval);
		intervalField = new Text(intervalGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		intervalField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		intervalField.addListener(SWT.Modify, this);
		new Label(intervalGroup, SWT.NONE).setText(Messages.AutoRuleDialog_result);
		resultingIntervals = new Label(intervalGroup, SWT.WRAP);
		layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		layoutData.widthHint = 500;
		resultingIntervals.setLayoutData(layoutData);

		enumGroup = new Composite(stack, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		enumGroup.setLayout(layout);
		label = new Label(enumGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
		label.setText(Messages.AutoRuleDialog_values);
		enumViewer = CheckboxTableViewer.newCheckList(enumGroup, SWT.BORDER | SWT.V_SCROLL);
		layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
		layoutData.heightHint = 200;
		enumViewer.getControl().setLayoutData(layoutData);
		enumViewer.setContentProvider(ArrayContentProvider.getInstance());
		enumViewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (qfield != null)
					return qfield.formatScalarValue(element);
				return null;
			}
		});
		enumViewer.addCheckStateListener(this);
		new AllNoneGroup(enumGroup, this);
		selectGroup = new Composite(stack, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		selectGroup.setLayout(layout);
		new Label(selectGroup, SWT.NONE).setText(Messages.AutoRuleDialog_values);
		valueField = new Text(selectGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		valueField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		valueField.addListener(SWT.Modify, this);
		return area;
	}

	protected Object compileInterval(String text, QueryField qfield) {
		List<String> list = Core.fromStringList(text, ";"); //$NON-NLS-1$
		double[] intervals = new double[list.size()];
		int i = 0;
		for (String s : list)
			try {
				af.setMaximumFractionDigits(7);
				af.setMinimumFractionDigits(0);
				double ld = af.parse(s).doubleValue();
				if (i > 0)
					if (ld <= intervals[i - 1])
						return NLS.bind(Messages.AutoRuleDialog_value_not_greater, s);
				intervals[i++] = ld;
			} catch (ParseException e1) {
				return NLS.bind(Messages.AutoRuleDialog_not_a_valid_number, s);
			}
		if (intervals.length == 0)
			return qfield.getAutoPolicy() == QueryField.AUTO_LOG ? Messages.AutoRuleDialog_specify_base
					: Messages.AutoRuleDialog_specify_interval;
		if (intervals.length == 1) {
			if (intervals[0] <= 0)
				return Messages.AutoRuleDialog_not_negative;
			if (intervals[0] <= 1 && qfield.getAutoPolicy() == QueryField.AUTO_LOG)
				return Messages.AutoRuleDialog_not_less_one;
		}
		return intervals;
	}

	private void fillExplanation(QueryField qfield) {
		if (qfield == null)
			explanation.setText(""); //$NON-NLS-1$
		else
			switch (qfield.getAutoPolicy()) {
			case QueryField.AUTO_DISCRETE:
				if (qfield.getEnumeration() == null && qfield.getType() != QueryField.T_BOOLEAN)
					explanation.setText(Messages.AutoRuleDialog_discrete_values);
				else
					explanation.setText(Messages.AutoRuleDialog_enumerated_values);
				break;
			case QueryField.AUTO_LINEAR:
				explanation.setText(Messages.AutoRuleDialog_linear_distribution);
				break;
			case QueryField.AUTO_LOG:
				explanation.setText(Messages.AutoRuleDialog_exponential_distribution);
				break;
			case QueryField.AUTO_CONTAINS:
				explanation.setText(Messages.AutoRuleDialog_arbitrary_text);
				break;
			case QueryField.AUTO_SELECT:
				explanation.setText(Messages.AutoRuleDialog_multiple_value);
				break;
			}
	}

	protected void fillFieldCombo() {
		fields.clear();
		Object g = groupCombo.getStructuredSelection().getFirstElement();
		for (String id : QueryField.getQueryFieldKeys()) {
			QueryField mainField = QueryField.findQueryField(id);
			if (mainField.belongsTo(g) && mainField.hasLabel() && mainField.getAutoPolicy() != 0)
				fields.add(mainField);
		}
		fieldCombo.setInput(fields);
	}

	private void fillValues() {
		if (rule == null)
			groupCombo.setSelection(new StructuredSelection(QueryField.CATEGORY_ALL));
		else {
			nameField.setText(rule.getName());
			QueryField qfield = rule.getQfield();
			groupCombo.setSelection(new StructuredSelection(qfield.getCategoryOrSubgroup()));
			fieldCombo.setSelection(new StructuredSelection(qfield));
			updateStack();
			switch (qfield.getAutoPolicy()) {
			case QueryField.AUTO_CONTAINS:
			case QueryField.AUTO_SELECT:
				valueField.setText(rule.getValueSpec());
				break;
			case QueryField.AUTO_DISCRETE:
				if (qfield.getEnumeration() != null) {
					enumViewer.setCheckedElements(rule.getEnumeration());
					break;
				} else if (qfield.getType() == QueryField.T_BOOLEAN) {
					enumViewer.setCheckedElements(rule.getEnumeration());
					break;
				}
				//$FALL-THROUGH$
			default:
				intervalField.setText(rule.getIntervalSpec());
				break;
			}
		}
		validate();
	}

	protected void updateStack() {
		if (qfield != null) {
			switch (qfield.getAutoPolicy()) {
			case QueryField.AUTO_DISCRETE:
				Object enumeration = qfield.getEnumeration();
				if (enumeration != null) {
					stackLayout.topControl = enumGroup;
					stack.setVisible(true);
					List<Object> input = new ArrayList<Object>();
					if (enumeration instanceof String[])
						input.addAll(Arrays.asList((String[]) enumeration));
					else if (enumeration instanceof int[])
						for (int v : ((int[]) enumeration))
							input.add(v);
					enumViewer.setInput(input);
					break;
				} else if (qfield.getType() == QueryField.T_BOOLEAN) {
					stackLayout.topControl = enumGroup;
					stack.setVisible(true);
					List<Object> input = new ArrayList<Object>();
					input.add(Boolean.FALSE);
					input.add(Boolean.TRUE);
					enumViewer.setInput(input);
					break;
				}
				//$FALL-THROUGH$
			case QueryField.AUTO_LINEAR:
			case QueryField.AUTO_LOG:
				stackLayout.topControl = intervalGroup;
				stack.setVisible(true);
				break;
			case QueryField.AUTO_CONTAINS:
			case QueryField.AUTO_SELECT:
				stackLayout.topControl = selectGroup;
				stack.setVisible(true);
				break;
			default:
				stack.setVisible(false);
				break;
			}
			comp.layout(true, true);
		}
	}

	private void validate() {
		String errorMsg = null;
		String name = nameField.getText();
		if (name.isEmpty())
			errorMsg = Messages.AutoRuleDialog_name_empty;
		else if (name.indexOf("::") >= 0) //$NON-NLS-1$
			errorMsg = Messages.AutoRuleDialog_name_colons;
		if (errorMsg == null) {
			if (qfield == null)
				errorMsg = Messages.AutoRuleDialog_select_metadata;
			else {
				int autoPolicy = qfield.getAutoPolicy();
				switch (autoPolicy) {
				case QueryField.AUTO_CONTAINS:
					if (valueField.getText().isEmpty())
						errorMsg = Messages.AutoRuleDialog_specify_at_least_one;
					break;
				case QueryField.AUTO_SELECT:
					if (valueField.getText().trim().isEmpty())
						errorMsg = Messages.AutoRuleDialog_specify_at_least_one;
					break;
				case QueryField.AUTO_DISCRETE:
					if ((qfield.getEnumeration() != null || qfield.getType() == QueryField.T_BOOLEAN)
							&& enumViewer.getCheckedElements().length == 0)
						errorMsg = Messages.AutoRuleDialog_at_least_one_value;
					break;
				default:
					Object result = compileInterval(intervalField.getText(), qfield);
					if (result instanceof String)
						errorMsg = (String) result;
					break;
				}
			}
		}
		getButton(OK).setEnabled(errorMsg == null);
		setErrorMessage(errorMsg);
	}

	@Override
	protected void okPressed() {
		switch (qfield.getAutoPolicy()) {
		case QueryField.AUTO_CONTAINS:
		case QueryField.AUTO_SELECT:
			if (rule == null)
				rule = new AutoRule();
			rule.setValues(Core.fromStringList(valueField.getText(), ";")); //$NON-NLS-1$
			break;
		case QueryField.AUTO_DISCRETE:
			if (rule == null)
				rule = new AutoRule();
			rule.setQfield(qfield);
			if (qfield.getEnumeration() != null) {
				rule.setEnumeration(enumViewer.getCheckedElements());
				break;
			} else if (qfield.getType() == QueryField.T_BOOLEAN) {
				rule.setEnumeration(enumViewer.getCheckedElements());
				break;
			}
			//$FALL-THROUGH$
		default:
			Object result = compileInterval(intervalField.getText(), qfield);
			if (result instanceof double[]) {
				if (rule == null)
					rule = new AutoRule();
				rule.setQfield(qfield);
				rule.setIntervals((double[]) result);
			}
			break;
		}
		if (rule != null)
			rule.setName(nameField.getText());
		super.okPressed();
	}

	/**
	 * @return rule
	 */
	public AutoRule getRule() {
		return rule;
	}

	private void setResultingInterval(Object spec, QueryField queryField) {
		if (spec instanceof String) {
			resultingIntervals.setText((String) spec);
			resultingIntervals.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		} else {
			resultingIntervals.setForeground(resultingIntervals.getParent().getForeground());
			StringBuilder sb = new StringBuilder();
			double[] intervals = (double[]) spec;
			if (intervals.length > 1) {
				for (double d : intervals) {
					if (sb.length() > 0)
						sb.append(" - "); //$NON-NLS-1$
					sb.append(nice(d));
				}
			} else {
				double interval = intervals[0];
				int start = -5;
				if (queryField.getAutoPolicy() == QueryField.AUTO_LINEAR) {
					double d = 0;
					int type = queryField.getType();
					if (type == QueryField.T_POSITIVEFLOAT || type == QueryField.T_POSITIVELONG
							|| type == QueryField.T_POSITIVEINTEGER)
						start = 0;
					else {
						sb.append("… - "); //$NON-NLS-1$
						d = -start * interval;
					}
					for (int i = start; i < 10 + start; i++) {
						if (i > start)
							sb.append(" - "); //$NON-NLS-1$
						sb.append(nice(d));
						d += interval;
					}
				} else {
					sb.append("… - "); //$NON-NLS-1$
					for (int i = start; i < 10 + start; i++) {
						if (i > start)
							sb.append(" - "); //$NON-NLS-1$
						sb.append(nice(Math.pow(interval, i)));
					}
				}
				sb.append(" - …"); //$NON-NLS-1$
			}
			resultingIntervals.setText(sb.toString());
		}
	}

	private static Object nice(double d) {
		af.setMinimumFractionDigits(0);
		af.setMaximumFractionDigits(d >= 10d ? 2 : d < 0.1d ? 5 : 3);
		return af.format(d);
	}

	@Override
	public void handleEvent(Event e) {
		if (e.type == SWT.Modify) {
			if (e.widget == intervalField) {
				QueryField qfield = (QueryField) fieldCombo.getStructuredSelection().getFirstElement();
				if (qfield != null)
					setResultingInterval(compileInterval(intervalField.getText(), qfield), qfield);
			}
			validate();
		} else
			enumViewer.setAllChecked(e.widget.getData() == AllNoneGroup.ALL);

	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		validate();
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (event.getSource() == groupCombo) {
			fillFieldCombo();
			fieldCombo.setSelection(new StructuredSelection(fields.get(0)));
		} else {
			fillExplanation(qfield = (QueryField) ((IStructuredSelection) event.getSelection()).getFirstElement());
			intervalField.setText(""); //$NON-NLS-1$
			updateStack();
			validate();
		}

	}

}
