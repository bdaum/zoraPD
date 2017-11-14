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
 * (c) 2009-2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
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

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.FieldDescriptor;
import com.bdaum.zoom.ui.internal.dialogs.TemplateFieldSelectionDialog;
import com.bdaum.zoom.ui.internal.dialogs.TemplateMessages;

public class TextWithVariableGroup {

	static class PrintVariablesDialog extends ZDialog {

		String var = null;
		private List list;
		private String[] vlist;
		private final String title;

		public PrintVariablesDialog(Shell parentShell, String title, String[] vlist) {
			super(parentShell);
			this.title = title;
			this.vlist = vlist;
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
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					okPressed();
				}
			});
			final GridData gd_list = new GridData(SWT.FILL, SWT.CENTER, true, false);
			gd_list.widthHint = 200;
			gd_list.heightHint = 200;
			list.setLayoutData(gd_list);
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
	}

	private Text textField;
	private Button addVariableButton;
	private Button addMetadataButon;

	public TextWithVariableGroup(Composite composite, String lab, final String title, final String[] variables,
			boolean metadata) {
		new Label(composite, SWT.NONE).setText(lab);
		textField = new Text(composite, SWT.BORDER);
		final GridData gd_titleField = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_titleField.widthHint = 300;
		textField.setLayoutData(gd_titleField);

		addVariableButton = new Button(composite, SWT.PUSH);
		addVariableButton.setText(Messages.TextWithVariableGroup_add_variable);
		addVariableButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				PrintVariablesDialog dialog = new PrintVariablesDialog(textField.getShell(), title, variables);
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
					TemplateFieldSelectionDialog dialog = new TemplateFieldSelectionDialog(addMetadataButon.getShell());
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

}
