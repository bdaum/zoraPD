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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.FieldDescriptor;
import com.bdaum.zoom.ui.internal.TemplateProcessor;
import com.bdaum.zoom.ui.internal.dialogs.AddVariablesDialog;
import com.bdaum.zoom.ui.internal.dialogs.TemplateFieldSelectionDialog;

public class TextWithVariableGroup implements Listener {

	class SelectTemplateDialog extends ZDialog implements Listener {

		private String title;
		private String[] templates;
		private List list;
		private String var = null;
		private Label example;

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
			list.addListener(SWT.Selection, this);
			list.addListener(SWT.DefaultSelection, this);
			list.setLayoutData(new GridData(600, 200));
			example = new Label(area, SWT.NONE);
			example.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			list.setItems(templates);
			return area;
		}

		protected void showExample() {
			example.setText(var != null && !var.isEmpty() ? Messages.TextWithVariableGroup_example + ": " //$NON-NLS-1$
					+ templateProcessor.processTemplate(var, asset, collection, -1, -1) : ""); //$NON-NLS-1$
		}

		protected void validate() {
			getButton(OK).setEnabled(var != null);
		}

		@Override
		public void handleEvent(Event e) {
			if (e.type == SWT.DefaultSelection) {
				if (var != null)
					okPressed();
			} else {
				int i = list.getSelectionIndex();
				var = i < 0 ? null : templates[i];
				showExample();
				validate();
			}
		}

	}

	private Text textField;
	private Button addVariableButton;
	private Button addMetadataButon;
	private Button selectTemplateButton;
	private Asset asset;
	private String collection;
	private ListenerList<Listener> listeners = new ListenerList<Listener>();
	private String[] variables;
	private TemplateProcessor templateProcessor;

	public TextWithVariableGroup(Composite composite, String lab, int width, final String[] variables, boolean metadata,
			final String[] templates, Asset asset, String collection) {
		this.variables = variables;
		this.asset = asset;
		this.collection = collection;
		templateProcessor = new TemplateProcessor(variables);
		if (lab != null)
			new Label(composite, SWT.NONE).setText(lab);
		textField = new Text(composite, SWT.BORDER);
		final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = width;
		textField.setLayoutData(data);
		textField.addListener(SWT.Modify, this);
		if (templates != null && templates.length > 0) {
			if (templates.length == 1)
				textField.setText(templates[0]);
			else {
				selectTemplateButton = new Button(composite, SWT.PUSH);
				selectTemplateButton.setText(Messages.TextWithVariableGroup_select_template);
				selectTemplateButton.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event e) {
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
		addVariableButton.addListener(SWT.Selection, this);
		if (metadata) {
			addMetadataButon = new Button(composite, SWT.PUSH);
			addMetadataButon.setText(Messages.TextWithVariableGroup_add_metadata);
			addMetadataButon.addListener(SWT.Selection, this);
		}
		setTooltip();
	}

	public String getText() {
		return textField.getText();
	}

	public void setText(String text) {
		textField.setText(text);
	}

	public void setContext(String collection, Asset asset) {
		this.collection = collection;
		this.asset = asset;
	}

	public void setEnabled(boolean enabled) {
		textField.setEnabled(enabled);
		addVariableButton.setEnabled(enabled);
		if (addMetadataButon != null)
			addMetadataButon.setEnabled(enabled);
		if (selectTemplateButton != null)
			selectTemplateButton.setEnabled(enabled);
	}

	protected void setTooltip() {
		String template = textField.getText();
		textField.setToolTipText(
				!template.isEmpty() ? templateProcessor.processTemplate(template, asset, collection, -1, -1) : null);
	}

	public void setToolTipText(String tooltip) {
		textField.setToolTipText(tooltip);
	}

	public void addListener(int type, Listener listener) {
		if (type == SWT.Modify)
			listeners.add(listener);
	}

	public void removeListener(int type, Listener listener) {
		listeners.remove(listener);
	}

	private void fireEvent(Event event) {
		event.type = SWT.Modify;
		event.data = this;
		for (Listener listener : listeners)
			listener.handleEvent(event);
	}

	@Override
	public void handleEvent(Event event) {
		if (event.widget == textField) {
			setTooltip();
			fireEvent(event);
		} else if (event.widget == addVariableButton) {
			AddVariablesDialog dialog = new AddVariablesDialog(textField.getShell(),
					Messages.TextWithVariableGroup_add_variable, variables, null);
			dialog.create();
			dialog.getShell().setLocation(textField.toDisplay(20, 10));
			if (dialog.open() == Window.OK) {
				textField.insert(dialog.getResult());
				fireEvent(event);
			}
		} else if (event.widget == addMetadataButon) {
			TemplateFieldSelectionDialog dialog = new TemplateFieldSelectionDialog(textField.getShell());
			dialog.create();
			dialog.getShell().setLocation(textField.toDisplay(20, 10));
			if (dialog.open() != TemplateFieldSelectionDialog.OK)
				return;
			FieldDescriptor fd = dialog.getResult();
			String qname = fd.subfield == null ? fd.qfield.getId() : fd.qfield.getId() + '&' + fd.subfield.getId();
			textField.insert(Constants.TV_META + qname + '}');
			fireEvent(event);
		}
	}

}
