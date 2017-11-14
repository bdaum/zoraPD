package com.bdaum.zoom.ui.internal.dialogs;

import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;

@SuppressWarnings("restriction")
public class ListCellEditorDialog extends AbstractListCellEditorDialog {

	private Text viewer;

	public ListCellEditorDialog(Shell parentShell, Object value,
			QueryField qfield) {
		super(parentShell, value, qfield);
	}

	@Override
	public void create() {
		super.create();
		setMessage(Messages.ListCellEditorDialog_enter_each_item_on_separate_line);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		viewer = new Text(parent, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = 50;
		viewer.setLayoutData(layoutData);
		viewer.setText(Utilities.csv(value, qfield.getType(), "\n")); //$NON-NLS-1$
		if (qfield.getType() == QueryField.T_INTEGER
				|| qfield.getType() == QueryField.T_POSITIVEINTEGER)
			viewer.addVerifyListener(new VerifyListener() {
				public void verifyText(VerifyEvent e) {
					String text = viewer.getText();
					String newText = text.substring(0, e.start) + e.text
							+ text.substring(e.end);
					try {
						StringTokenizer st = new StringTokenizer(newText, "\n"); //$NON-NLS-1$
						while (st.hasMoreTokens())
							Integer.parseInt(st.nextToken().trim());
					} catch (NumberFormatException e1) {
						e.doit = false;
					}
				}
			});
		return comp;
	}

	@Override
	protected void okPressed() {
		String text = viewer.getText();
		if (qfield.getType() == QueryField.T_INTEGER
				|| qfield.getType() == QueryField.T_POSITIVEINTEGER) {
			StringTokenizer st = new StringTokenizer(text, "\n"); //$NON-NLS-1$
			int cnt = st.countTokens();
			int[] result = new int[cnt];
			int i = 0;
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				result[i++] = Integer.parseInt(token);
			}
			value = result;
		} else if (qfield.getType() == QueryField.T_STRING) {
			StringTokenizer st = new StringTokenizer(text, "\n"); //$NON-NLS-1$
			int cnt = st.countTokens();
			String[] result = new String[cnt];
			int i = 0;
			while (st.hasMoreTokens())
				result[i++] = st.nextToken().trim();
			value = result;
		}
		super.okPressed();
	}

}