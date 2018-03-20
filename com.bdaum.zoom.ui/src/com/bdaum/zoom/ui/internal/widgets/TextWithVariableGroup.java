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

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.ui.internal.FieldDescriptor;
import com.bdaum.zoom.ui.internal.dialogs.TemplateFieldSelectionDialog;
import com.bdaum.zoom.ui.internal.dialogs.TemplateMessages;

@SuppressWarnings("restriction")
public class TextWithVariableGroup implements ModifyListener {

	static class SelectTemplateDialog extends ZDialog {

		private String title;
		private String[] templates;
		private List list;
		String var = null;

		public SelectTemplateDialog(Shell parent, String title, String[] templates) {
			super(parent);
			this.title = title;
			this.templates = templates;
		}

		@Override
		public void create() {
			super.create();
			validate();
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(title);
		}

		public String getResult() {
			return var;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite) super.createDialogArea(parent);
			area.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			list = new List(area, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
			list.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int i = list.getSelectionIndex();
					var = i < 0 ? null : templates[i];
					validate();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					if (var != null)
						okPressed();
				}
			});
			list.setLayoutData(new GridData(500, 200));
			list.setItems(templates);
			return area;
		}

		protected void validate() {
			getButton(OK).setEnabled(var != null);
		}

	}

	static class AddVariablesDialog extends ZDialog {
		String var = null;
		private List list;
		private String[] vlist;
		private final String title;

		public AddVariablesDialog(Shell parentShell, String title, String[] vlist) {
			super(parentShell);
			this.title = title;
			this.vlist = vlist;
		}

		@Override
		public void create() {
			super.create();
			validate();
		}

		public String getResult() {
			return var;
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText(title);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite area = (Composite) super.createDialogArea(parent);
			area.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			list = new List(area, SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
			list.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int i = list.getSelectionIndex();
					var = i < 0 ? null : vlist[i];
					validate();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					if (var != null)
						okPressed();
				}
			});
			list.setLayoutData(new GridData(250, 200));
			String[] compList = new String[vlist.length];
			for (int i = 0; i < vlist.length; i++) {
				String varName = vlist[i];
				if (varName.startsWith("{") && varName.endsWith("}")) { //$NON-NLS-1$//$NON-NLS-2$
					varName = varName.substring(1, varName.length() - 1);
					compList[i] = vlist[i] + TemplateMessages.getString(TemplateMessages.PREFIX + varName);
				}
			}
			list.setItems(compList);
			return area;
		}

		protected void validate() {
			getButton(OK).setEnabled(var != null);
		}
	}

	private Text textField;
	private Button addVariableButton;
	private Button addMetadataButon;
	private Button selectTemplateButton;
	private Asset asset;
	private String collection;

	public TextWithVariableGroup(Composite composite, String lab, int width,
			final String[] variables, boolean metadata, final String[] templates, Asset asset, String collection) {
		this.asset = asset;
		this.collection = collection;
		if (lab != null)
			new Label(composite, SWT.NONE).setText(lab);
		textField = new Text(composite, SWT.BORDER);
		final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = width;
		textField.setLayoutData(data);
		textField.addModifyListener(this);
		if (templates != null && templates.length > 0) {
			if (templates.length == 1)
				textField.setText(templates[0]);
			else {
				selectTemplateButton = new Button(composite, SWT.PUSH);
				selectTemplateButton.setText(Messages.TextWithVariableGroup_select_template);
				selectTemplateButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						SelectTemplateDialog dialog = new SelectTemplateDialog(composite.getShell(),
								Messages.TextWithVariableGroup_select_template, templates);
						Point loc = textField.toDisplay(20, 10);
						dialog.create();
						dialog.getShell().setLocation(loc);
						if (dialog.open() == Window.OK)
							textField.setText(dialog.getResult());
					}
				});
			}
		}
		addVariableButton = new Button(composite, SWT.PUSH);
		addVariableButton.setText(Messages.TextWithVariableGroup_add_variable);
		addVariableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AddVariablesDialog dialog = new AddVariablesDialog(composite.getShell(),
						Messages.TextWithVariableGroup_add_variable, variables);
				Point loc = textField.toDisplay(20, 10);
				dialog.create();
				dialog.getShell().setLocation(loc);
				if (dialog.open() == Window.OK)
					textField.insert(dialog.getResult());
			}
		});
		if (metadata) {
			addMetadataButon = new Button(composite, SWT.PUSH);
			addMetadataButon.setText(Messages.TextWithVariableGroup_add_metadata);
			addMetadataButon.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TemplateFieldSelectionDialog dialog = new TemplateFieldSelectionDialog(composite.getShell());
					Point loc = textField.toDisplay(20, 10);
					dialog.create();
					dialog.getShell().setLocation(loc);
					if (dialog.open() != TemplateFieldSelectionDialog.OK)
						return;
					FieldDescriptor fd = dialog.getResult();
					String qname = fd.subfield == null ? fd.qfield.getId()
							: fd.qfield.getId() + '&' + fd.subfield.getId();
					textField.insert(Constants.TV_META + qname + '}');
				}
			});
		}
		setTooltip();
	}

	public String getText() {
		return textField.getText();
	}

	public void setText(String text) {
		textField.setText(text);
	}

	public void setEnabled(boolean enabled) {
		textField.setEnabled(enabled);
		addVariableButton.setEnabled(enabled);
		if (addMetadataButon != null)
			addMetadataButon.setEnabled(enabled);
	}

	@Override
	public void modifyText(ModifyEvent e) {
		setTooltip();
	}

	protected void setTooltip() {
		if (asset != null && collection != null) {
			String text = textField.getText();
			if (!text.isEmpty())
				setToolTipText(Utilities.evaluateTemplate(text, Constants.PI_ALL, null, null, 0, 1, 1, null, asset,
						collection, Integer.MAX_VALUE, false));
		}
	}

	public void setToolTipText(String tooltip) {
		textField.setToolTipText(tooltip);
	}

}
