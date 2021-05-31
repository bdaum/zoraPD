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

package com.bdaum.zoom.ui.internal.views;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IFormatter;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;

public class DateTimeCellEditor extends DialogCellEditor {

	private static class DateTimeEditorDialog extends ZTitleAreaDialog {

		private static final int DELETE_ID = 9999;
		private Object value;
		private QueryField qfield;
		private DateTime dateField;
		private DateTime timeField;

		public DateTimeEditorDialog(Shell parentShell, Object value, QueryField qfield) {
			super(parentShell);
			this.value = value;
			this.qfield = qfield;
		}

		@Override
		public void create() {
			super.create();
			setTitle(qfield.getLabel());
			setMessage(Messages.getString("DateTimeCellEditor.enter_date_and_time")); //$NON-NLS-1$
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite comp = (Composite) super.createDialogArea(parent);
			Composite area = new Composite(comp, SWT.NONE);
			area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			area.setLayout(new GridLayout(2, false));
			new Label(area, SWT.NONE).setText(Messages.getString("DateTimeCellEditor.date")); //$NON-NLS-1$
			dateField = new DateTime(area, SWT.DROP_DOWN | SWT.MEDIUM);
			new Label(area, SWT.NONE).setText(Messages.getString("DateTimeCellEditor.time")); //$NON-NLS-1$
			timeField = new DateTime(area, SWT.TIME);
			GregorianCalendar cal = new GregorianCalendar();
			if (value instanceof Date)
				cal.setTime((Date) value);
			dateField.setDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			timeField.setTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
			return comp;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, DELETE_ID, Messages.getString("DateTimeCellEditor.delete"), false); //$NON-NLS-1$
			super.createButtonsForButtonBar(parent);
		}

		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == DELETE_ID) {
				value = QueryField.EMPTYDATE;
				super.okPressed();
			}
			super.buttonPressed(buttonId);
		}

		@Override
		protected void okPressed() {
			value = new GregorianCalendar(dateField.getYear(), dateField.getMonth(), dateField.getDay(),
					timeField.getHours(), timeField.getMinutes(), timeField.getSeconds()).getTime();
			super.okPressed();
		}

		public Object getResult() {
			return value;
		}

	}

	private final QueryField qfield;
	private Object value;

	public DateTimeCellEditor(Composite parent, QueryField qfield) {
		this.qfield = qfield;
		create(parent);
	}

	@Override
	protected Object doGetValue() {
		return value;
	}

	@Override
	protected void doSetValue(Object v) {
		this.value = v;
		super.doSetValue(v);
	}

	@Override
	protected void updateContents(Object v) {
		IFormatter formatter = qfield.getFormatter();
		super.updateContents(
				v instanceof Date ? (formatter != null ? formatter.format((Date) v) : Format.DFDT.get().format((Date) v)) : v);
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		DateTimeEditorDialog dialog = new DateTimeEditorDialog(cellEditorWindow.getShell(), value, qfield);
		if (dialog.open() == Window.OK)
			return value = dialog.getResult();
		return null;
	}

}
