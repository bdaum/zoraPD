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

public class ExplanationFieldEditor extends FieldEditor {

	private Label label;
	private GridData gridData;

	public ExplanationFieldEditor() {
	}

	public ExplanationFieldEditor(String name, String explanation,
			Composite parent) {
		super(name, explanation, parent);
	}

	
	@Override
	protected void adjustForNumColumns(int numColumns) {
		((GridData) label.getLayoutData()).horizontalSpan = numColumns;
	}

	
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		label = new Label(parent, SWT.WRAP);
		label.setText(getLabelText());
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = numColumns;
		label.setLayoutData(gridData);
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

	
	@Override
	public void setLabelText(String text) {
		if (label != null)
			label.setText(text);
		super.setLabelText(text);
	}

	public void setIndent(int indent) {
		if (label != null) {
			gridData.horizontalIndent = indent;
			label.getParent().layout();
		}
	}

	public void setHeight(int h) {
		if (label != null) {
			gridData.heightHint = h;
			label.getParent().layout();
		}
	}

	public void setWidth(int w) {
		if (label != null) {
			gridData.widthHint = w;
			label.getParent().layout();
		}
	}
}
