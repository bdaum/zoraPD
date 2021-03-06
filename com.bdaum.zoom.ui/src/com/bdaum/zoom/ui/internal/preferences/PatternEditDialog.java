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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.preferences;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.ui.internal.widgets.PatternListEditor;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.ZDialog;

public class PatternEditDialog extends ZDialog implements Listener {

	private String title, prompt;

	private Text text;

	private String result;

	private String forbiddenChars;

	private boolean rule;

	private Label errorMsg;

	private RadioButtonGroup policyButtonGroup;

	public PatternEditDialog(Shell shell, String title, String label, String dflt, String forbiddenChars,
			boolean rule) {
		super(shell);
		this.title = title;
		this.prompt = label;
		this.forbiddenChars = forbiddenChars;
		this.rule = rule;
		result = dflt;
	}

	public String getResult() {
		return result;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite comp = (Composite) super.createDialogArea(parent);
		parent.getShell().setText(title);
		Composite body = new Composite(comp, SWT.NONE);
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		body.setLayout(new GridLayout());
		if (rule) {
			policyButtonGroup = new RadioButtonGroup(body, null, SWT.NONE,
					Messages.getString("PatternEditDialog.Reject"), Messages //$NON-NLS-1$
							.getString("PatternEditDialog.Accept")); //$NON-NLS-1$
			policyButtonGroup.setToolTipText(0, Messages.getString("PatternEditDialog.Pattern_rejects")); //$NON-NLS-1$
			policyButtonGroup.setToolTipText(1, Messages.getString("PatternEditDialog.Pattern_accepts")); //$NON-NLS-1$
			policyButtonGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			policyButtonGroup.addListener(SWT.Selection, this);
			policyButtonGroup.setSelection(result.startsWith(">") ? 1 : 0); //$NON-NLS-1$
		}

		Label label = new Label(body, SWT.NONE);
		label.setText(prompt);
		text = new Text(body, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		errorMsg = new Label(body, SWT.NONE);
		errorMsg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		errorMsg.setData(CSSProperties.ID, CSSProperties.ERRORS);
		errorMsg.setForeground(errorMsg.getDisplay().getSystemColor(SWT.COLOR_RED));
		if (forbiddenChars != null)
			text.addListener(SWT.Verify, this);
		text.setText(result);

		text.addListener(SWT.Modify, this);
		text.setFocus();
		text.selectAll();
		return comp;
	}

	private void setAcceptOrReject(boolean reject) {
		if (reject && result.endsWith(PatternListEditor.ACCEPTS))
			result = result.substring(1, result.length() - PatternListEditor.ACCEPTS.length())
					+ PatternListEditor.REJECTS;
		else if (!reject && result.endsWith(PatternListEditor.REJECTS))
			result = result.substring(1, result.length() - PatternListEditor.REJECTS.length())
					+ PatternListEditor.ACCEPTS;
	}

	@Override
	public void handleEvent(Event e) {
		if (e.type == SWT.Verify ) {
			if (forbiddenChars.indexOf(e.character) >= 0) {
				e.doit = false;
				errorMsg.setText(NLS.bind(Messages.getString("PatternEditDialog.bad_chars"), //$NON-NLS-1$
						forbiddenChars));
			} else {
				e.doit = true;
				errorMsg.setText(""); //$NON-NLS-1$
			}
		} else if (e.type == SWT.Modify )
			result = text.getText();
		else
			setAcceptOrReject(policyButtonGroup.getSelection() == 0);
		
	}

}
