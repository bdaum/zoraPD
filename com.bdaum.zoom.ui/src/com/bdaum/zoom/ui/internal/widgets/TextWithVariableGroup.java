package com.bdaum.zoom.ui.internal.widgets;

import java.util.HashMap;
import java.util.Map;

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

public class TextWithVariableGroup {

	static class PrintVariablesDialog extends ZDialog {

		String var = null;
		private List list;
		private String[] vlist;
		private final String title;

		public PrintVariablesDialog(Shell parentShell, String title,
				String[] vlist) {
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
			final GridData gd_list = new GridData(SWT.FILL, SWT.CENTER, true,
					false);
			gd_list.widthHint = 200;
			gd_list.heightHint = 200;
			list.setLayoutData(gd_list);
			String[] compList = new String[vlist.length];
			for (int i = 0; i < vlist.length; i++)
				compList[i] = vlist[i] + " - " + textMap.get(vlist[i]); //$NON-NLS-1$
			list.setItems(compList);
			return area;
		}
	}

	private Text textField;
	private static Map<String, String> textMap = new HashMap<String, String>();

	static {
		textMap.put(Constants.PT_CATALOG,
				Messages.TextWithVariableGroup_cat_name);
		textMap.put(Constants.PT_TODAY,
				Messages.TextWithVariableGroup_current_date);
		textMap.put(Constants.PT_COUNT,
				Messages.TextWithVariableGroup_number_of_images);
		textMap.put(Constants.PT_PAGENO,
				Messages.TextWithVariableGroup_page_number);
		textMap.put(Constants.PT_COLLECTION,
				Messages.TextWithVariableGroup_collection_name);
		textMap.put(Constants.PT_USER,
				Messages.TextWithVariableGroup_current_computer_user);
		textMap.put(Constants.PT_OWNER,
				Messages.TextWithVariableGroup_cat_owner);

		textMap.put(Constants.PI_TITLE, Messages.TextWithVariableGroup_title);
		textMap.put(Constants.PI_NAME, Messages.TextWithVariableGroup_file_name);
		textMap.put(Constants.PI_DESCRIPTION,
				Messages.TextWithVariableGroup_description);
		textMap.put(Constants.PI_FORMAT,
				Messages.TextWithVariableGroup_file_format);
		textMap.put(Constants.PI_CREATIONDATE,
				Messages.TextWithVariableGroup_creation_date);
		textMap.put(Constants.PI_SIZE, Messages.TextWithVariableGroup_size);
		textMap.put(Constants.PI_SEQUENCENO,
				Messages.TextWithVariableGroup_sequence_number);
		textMap.put(Constants.PI_PAGEITEM,
				Messages.TextWithVariableGroup_page_item_no);

	}

	public TextWithVariableGroup(Composite composite, String lab,
			final String title, final String[] variables, boolean metadata) {
		new Label(composite, SWT.NONE).setText(lab);
		textField = new Text(composite, SWT.BORDER);
		final GridData gd_titleField = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		gd_titleField.widthHint = 300;
		textField.setLayoutData(gd_titleField);

		final Button addVariableButton = new Button(composite, SWT.PUSH);
		addVariableButton.setText(Messages.TextWithVariableGroup_add_variable);
		addVariableButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				PrintVariablesDialog dialog = new PrintVariablesDialog(
						textField.getShell(), title, variables);
				Point loc = textField.toDisplay(20, 10);
				dialog.create();
				dialog.getShell().setLocation(loc);
				if (dialog.open() == Window.OK)
					textField.insert(dialog.getResult());
			}
		});
		if (metadata) {
			final Button addMetadataButon = new Button(composite, SWT.PUSH);
			addMetadataButon.setText(Messages.TextWithVariableGroup_add_metadata);
			addMetadataButon.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TemplateFieldSelectionDialog dialog = new TemplateFieldSelectionDialog(
							addMetadataButon.getShell());
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

}
