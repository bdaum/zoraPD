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
 * (c) 2009-2018 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.FieldDescriptor;
import com.bdaum.zoom.ui.internal.HelpContextIds;

@SuppressWarnings("restriction")
public class TemplateEditDialog extends ZTitleAreaDialog {

	protected static final String INVALIDCAHRS = "<>?\":|\\/*."; //$NON-NLS-1$
	private Text templateField;
	private Text exampleField;
	private RenamingTemplate template;
	private Text nameField;
	private Button addVariableButton;
	private Asset asset;
	private File file;
	private Button addMetadataButton;
	private final QueryField field;
	private String[] variables;
	private ComboViewer varViewer;
	protected String selectedVar;

	public TemplateEditDialog(Shell parentShell, RenamingTemplate template, Object assetOrFile, QueryField field,
			String[] variables) {
		super(parentShell, HelpContextIds.TEMPLATE_DIALOG);
		this.template = template;
		if (assetOrFile instanceof Asset)
			this.asset = (Asset) assetOrFile;
		else if (assetOrFile instanceof File)
			this.file = (File) assetOrFile;
		this.field = field;
		this.variables = variables;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.TemplateEditDialog_edit_template);
		setMessage((template == null || !template.isSystem()) ? Messages.TemplateEditDialog_templates_control
				: Messages.TemplateEditDialog_predefined_editing_template);
		fillValues();
		updateButtons();
	}

	private void fillValues() {
		if (template != null) {
			nameField.setText(template.getLabel());
			templateField.setText(template.getContent());
			if (template.isSystem()) {
				nameField.setEditable(false);
				templateField.setEditable(false);
				addVariableButton.setEnabled(false);
				nameField.setText(template.getLabel() + Messages.TemplateEditDialog_read_only);
			} else
				nameField.setText(template.getLabel());
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		GridLayout layout = (GridLayout) comp.getLayout();
		layout.marginWidth = 10;
		layout.marginHeight = 5;
		layout.verticalSpacing = 10;

		final Composite composite = new Composite(comp, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		composite.setLayout(gridLayout_1);
		new Label(composite, SWT.NONE).setText(Messages.TemplateEditDialog_name);
		nameField = new Text(composite, SWT.BORDER);
		nameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		});

		new Label(composite, SWT.NONE).setText(Messages.TemplateEditDialog_template);
		templateField = new Text(composite, SWT.BORDER);
		templateField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		templateField.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				for (int i = 0; i < e.text.length(); i++)
					if (INVALIDCAHRS.indexOf(e.text.charAt(i)) >= 0) {
						e.doit = false;
						return;
					}
			}
		});
		templateField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtons();
				computeExample();
			}
		});
		new Label(composite, SWT.NONE).setText(Messages.TemplateEditDialog_example);
		exampleField = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
		final GridData gd_exampleField = new GridData(SWT.FILL, SWT.CENTER, true, false);
		exampleField.setLayoutData(gd_exampleField);

		final Composite buttonComp = new Composite(comp, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		buttonComp.setLayout(gridLayout);
		final ISelectionChangedListener varListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				templateField
						.insert(selectedVar = (String) ((IStructuredSelection) event.getSelection()).getFirstElement());
				varViewer.getCombo().setVisible(false);
			}
		};
		addVariableButton = new Button(buttonComp, SWT.PUSH);
		addVariableButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		addVariableButton.setText(Messages.TemplateEditDialog_add_var);
		addVariableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				varViewer.getCombo().setVisible(true);
				if (selectedVar != null) {
					varViewer.removeSelectionChangedListener(varListener);
					varViewer.setSelection(new StructuredSelection(selectedVar));
					varViewer.addSelectionChangedListener(varListener);
				}
			}
		});
		varViewer = new ComboViewer(buttonComp, SWT.NONE);
		Combo vcontrol = varViewer.getCombo();
		vcontrol.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		vcontrol.setVisibleItemCount(10);
		vcontrol.setVisible(false);
		varViewer.setContentProvider(ArrayContentProvider.getInstance());
		varViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					String varName = ((String) element);
					if (varName.startsWith("{") && varName.endsWith("}")) //$NON-NLS-1$ //$NON-NLS-2$
						return element + TemplateMessages
								.getString(TemplateMessages.PREFIX + varName.substring(1, varName.length() - 1));
				}
				return super.getText(element);
			}
		});
		varViewer.addSelectionChangedListener(varListener);
		varViewer.setInput(variables);
		if (asset != null) {
			addMetadataButton = new Button(buttonComp, SWT.PUSH);
			addMetadataButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
			addMetadataButton.setText(Messages.TemplateEditDialog_add_metadata);
			addMetadataButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TemplateFieldSelectionDialog dialog = new TemplateFieldSelectionDialog(getShell());
					if (dialog.open() != TemplateFieldSelectionDialog.OK)
						return;
					FieldDescriptor fd = dialog.getResult();
					templateField.insert(Constants.TV_META
							+ (fd.subfield == null ? fd.qfield.getId() : fd.qfield.getId() + '&' + fd.subfield.getId())
							+ '}');
				}
			});
			new Label(buttonComp, SWT.NONE).setText(Messages.TemplateEditDialog_example_shows_metadata);
		}
		return comp;
	}

	protected void computeExample() {
		int maxLength = BatchConstants.MAXPATHLENGTH;
		String filename = "_1072417.JPG"; //$NON-NLS-1$
		if (asset != null) {
			try {
				file = new File(new URI(asset.getUri()));
			} catch (URISyntaxException e) {
				// use default
			}
		}
		if (file != null) {
			filename = file.getName();
			String ext = ""; //$NON-NLS-1$
			int p = filename.lastIndexOf('.');
			if (p >= 0)
				ext = filename.substring(p);
			maxLength -= (file.getParent().length() + 1 + ext.length());
		}
		Meta meta = Core.getCore().getDbManager().getMeta(true);
		exampleField.setText(Utilities.evaluateTemplate(templateField.getText(), variables, filename,
				new GregorianCalendar(), 1, meta.getLastSequenceNo() + 1, meta.getLastYearSequenceNo() + 1,
				Messages.TemplateEditDialog_exmp, asset, "", maxLength, //$NON-NLS-1$
				QueryField.URI == field, false));
	}

	protected void updateButtons() {
		boolean enabled = !readonly && validate();
		getShell().setModified(enabled);
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	private boolean validate() {
		if (nameField.getText().trim().isEmpty()) {
			setErrorMessage(Messages.TemplateEditDialog_specify_name);
			return false;
		}
		String content = templateField.getText().trim();
		if (content.isEmpty()) {
			setErrorMessage(Messages.TemplateEditDialog_content_empty);
			return false;
		}
		StringBuilder sb = new StringBuilder(content);
		if (sb.indexOf("{") < 0 || sb.indexOf("}") < 0) { //$NON-NLS-1$ //$NON-NLS-2$
			setErrorMessage(Messages.TemplateEditDialog_template_no_var);
			return false;
		}
		boolean numbered = false;
		for (int i = 0; i < variables.length; i++) {
			while (true) {
				int p = sb.indexOf(variables[i]);
				if (p >= 0) {
					if (i < variables.length - 3)
						numbered = true;
					sb.delete(p, p + variables[i].length());
				} else
					break;
			}
		}
		if (!numbered) {
			setErrorMessage(Messages.TemplateEditDialog_template_not_unique);
			return false;
		}
		if (asset != null) {
			while (true) {
				int p = sb.indexOf(Constants.TV_META);
				if (p >= 0) {
					int q = sb.indexOf("}", p + 1); //$NON-NLS-1$
					if (q < 0)
						break;
					String fname = sb.substring(p + Constants.TV_META.length(), q);
					if (QueryField.findQuerySubField(fname) == null)
						break;
					sb.delete(p, q + 1);
				} else
					break;
			}
		}
		if (sb.indexOf("{") >= 0 || sb.indexOf("}") >= 0) { //$NON-NLS-1$ //$NON-NLS-2$
			setErrorMessage(Messages.TemplateEditDialog_unknown_vars);
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	@Override
	protected void okPressed() {
		if (template == null)
			template = new RenamingTemplate(null, null, false);
		template.setLabel(nameField.getText().trim());
		template.setContent(templateField.getText().trim());
		super.okPressed();
	}

	public RenamingTemplate getResult() {
		return template;
	}

}
