package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.FieldDescriptor;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.widgets.GroupComboCatFilter;
import com.bdaum.zoom.ui.internal.widgets.GroupComboLabelProvider;

public class TemplateFieldSelectionDialog extends ZTitleAreaDialog {

	private ComboViewer groupCombo;
	private ComboViewer fieldCombo;
	private List<FieldDescriptor> fields = new ArrayList<FieldDescriptor>(50);
	private FieldDescriptor result;

	public TemplateFieldSelectionDialog(Shell parentShell) {
		super(parentShell, HelpContextIds.TEMPLATE_DIALOG);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.TemplateFieldSelectionDialog_field_selection);
		setMessage(Messages.TemplateFieldSelectionDialog_field_selection_msg);
		fillValues();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(4, false));
		new Label(composite, SWT.NONE)
				.setText(Messages.TemplateFieldSelectionDialog_group);
		List<Object> categories = QueryField.getCategoriesAndSubgroups();
		groupCombo = new ComboViewer(composite, SWT.READ_ONLY | SWT.BORDER);
		Combo comboControl = groupCombo.getCombo();
		comboControl.setVisibleItemCount(categories.size());
		groupCombo.setContentProvider(ArrayContentProvider.getInstance());
		groupCombo.setFilters(new ViewerFilter[] { new GroupComboCatFilter() });
		groupCombo.setLabelProvider(new GroupComboLabelProvider());
		groupCombo.setInput(categories);
		groupCombo.getControl().setLayoutData(new GridData(100, SWT.DEFAULT));
		groupCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				fillFieldCombo();
				fieldCombo.setSelection(new StructuredSelection(fields.get(0)));
			}
		});
		new Label(composite, SWT.NONE)
				.setText(Messages.TemplateFieldSelectionDialog_field);
		fieldCombo = new ComboViewer(composite, SWT.READ_ONLY | SWT.BORDER);
		comboControl = fieldCombo.getCombo();
		comboControl.setVisibleItemCount(10);
		comboControl.setLayoutData(new GridData(300, SWT.DEFAULT));
		fieldCombo.setContentProvider(ArrayContentProvider.getInstance());
		fieldCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof FieldDescriptor) {
					FieldDescriptor fd = (FieldDescriptor) element;
					return fd.subfield == null ? fd.qfield.getLabel()
							: fd.qfield.getLabel() + ':'
									+ fd.subfield.getLabel();
				}
				return super.getText(element);
			}
		});
		fieldCombo.setComparator(ZViewerComparator.INSTANCE);
		fieldCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validate();
			}
		});
		return area;
	}

	private void fillValues() {
		groupCombo
				.setSelection(new StructuredSelection(QueryField.CATEGORY_ALL));
		validate();
	}

	private void validate() {
		FieldDescriptor fd = (FieldDescriptor) ((IStructuredSelection) fieldCombo
				.getSelection()).getFirstElement();
		String errorMsg = null;
		if (fd == null)
			errorMsg = Messages.TemplateFieldSelectionDialog_select_field;
		getButton(OK).setEnabled(errorMsg == null);
		setErrorMessage(errorMsg);
	}

	protected void fillFieldCombo() {
		fields.clear();
		Object g = ((IStructuredSelection) groupCombo.getSelection())
				.getFirstElement();
		for (String id : QueryField.getQueryFieldKeys()) {
			QueryField mainField = QueryField.findQueryField(id);
			if (mainField.belongsTo(g) && mainField.isUiField()) {
				fields.add(new FieldDescriptor(mainField, null));
				if (mainField.isStruct()) {
					QueryField[] children = null;
					QueryField parent = QueryField.getStructParent(mainField
							.getType());
					if (parent != null)
						children = parent.getChildren();
					if (children != null)
						for (QueryField detailField : children)
							if (detailField.isUiField())
								fields.add(new FieldDescriptor(mainField,
										detailField));
				}
			}
		}
		fieldCombo.setInput(fields);
	}

	@Override
	protected void okPressed() {
		result = (FieldDescriptor) ((IStructuredSelection) fieldCombo
				.getSelection()).getFirstElement();
		super.okPressed();
	}

	/**
	 * @return result
	 */
	public FieldDescriptor getResult() {
		return result;
	}

}
