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
 * (c) 2011 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.codes.CodeParser;
import com.bdaum.zoom.ui.internal.codes.Topic;
import com.bdaum.zoom.ui.internal.dialogs.CodesDialog;

public class CodeGroup extends Composite implements SelectionListener {

	private Text textfield;
	private CodeParser parser;
	private Button button;
	private boolean withLayout;
	private QueryField qfield;

	public CodeGroup(final Composite parent, int style, QueryField qfield) {
		super(parent, style);
		setQueryField(qfield);
		withLayout = parent.getLayout() != null;
		if (withLayout) {
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = layout.marginWidth = 0;
			setLayout(layout);
		}
		textfield = new Text(this, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		textfield.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		button = new Button(this, SWT.PUSH);
		button.setText("..."); //$NON-NLS-1$
		button.setToolTipText(Messages.CodeGroup_select_code);
		button.addSelectionListener(this);
	}

	@Override
	public void setBounds(Rectangle rect) {
		if (!withLayout) {
			textfield.setBounds(0, 0, rect.width - rect.height, rect.height);
			button.setBounds(rect.width - rect.height, 0, rect.height, rect.height);
		}
		super.setBounds(rect);
	}

	public void setText(String text) {
		textfield.setText(text == null ? "" : text); //$NON-NLS-1$
		Topic topic = (text != null && !text.isEmpty() && parser != null) ? parser.findTopic(text) : null;
		textfield.setToolTipText(topic == null ? "" : topic.getName()); //$NON-NLS-1$
	}

	public String getText() {
		return textfield.getText();
	}

	/**
	 * @return the parser
	 */
	public CodeParser getParser() {
		return parser;
	}

	/**
	 * @param parser
	 *            the parser to set
	 */
	public void setQueryField(QueryField qfield) {
		this.qfield = qfield;
		this.parser = qfield == null ? null : UiActivator.getDefault().getCodeParser((Integer) qfield.getEnumeration());
	}

	public Text getTextControl() {
		return textfield;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		CodesDialog dialog = new CodesDialog(getShell(), qfield, textfield.getText(), null);
		if (dialog.open() == Dialog.OK)
			setText(dialog.getResult());
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}

	public void removeListener(Listener listener) {
		textfield.removeListener(SWT.Modify, listener);
	}

	public void addListener(Listener listener) {
		textfield.addListener(SWT.Modify, listener);
	}

}
