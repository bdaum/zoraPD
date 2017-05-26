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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class FieldComment extends FieldEditor {

	private Label label;
	private int indent;

	public FieldComment() {
	}

	public FieldComment(Composite parent, String labelText, int indent) {
		this.indent = indent;
		init("name", labelText); //$NON-NLS-1$
		createControl(parent);		
	}

	
	@Override
	protected void adjustForNumColumns(int numColumns) {
		((GridData) label.getLayoutData()).horizontalSpan = numColumns;
	}

	
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		label = new Label(parent, SWT.WRAP);
		label.setText(getLabelText());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = numColumns;
		data.horizontalIndent = indent;
		label.setLayoutData(data);
	}

	
	@Override
	protected void doLoad() {
		// do nothing
	}

	
	@Override
	protected void doLoadDefault() {
		// do nothing
	}

	
	@Override
	protected void doStore() {
		// do nothing
	}

	
	@Override
	public int getNumberOfControls() {
		return 1;
	}

}
