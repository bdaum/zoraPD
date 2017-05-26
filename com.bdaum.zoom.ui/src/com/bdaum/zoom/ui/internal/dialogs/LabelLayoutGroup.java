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
 * (c) 2014 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class LabelLayoutGroup extends Composite implements ModifyListener {

	private static NumberFormat nf = NumberFormat.getNumberInstance();
	static {
		nf.setMaximumFractionDigits(1);
	}
	private CheckboxButton hideButton;
	private CCombo alignField;
	private Text distField;
	private Text indentField;
	private String[] words = new String[] { Messages.LabelLayoutGroup_left,
			Messages.LabelLayoutGroup_center, Messages.LabelLayoutGroup_right,
			Messages.LabelLayoutGroup_top, Messages.LabelLayoutGroup_middle,
			Messages.LabelLayoutGroup_bottom };
	private ListenerList<ModifyListener> listeners = new ListenerList<ModifyListener>();
	private final boolean dontHide;

	public LabelLayoutGroup(Composite parent, int style, boolean dontHide) {
		super(parent, style);
		this.dontHide = dontHide;
		setLayout(new GridLayout());
		hideButton = WidgetFactory.createCheckButton(this,
				Messages.LabelLayoutGroup_hide, null);
		if (!dontHide)
			hideButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateFields();
				}
			});
		Composite composite = new Composite(this, SWT.NONE);
		composite
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		composite.setLayout(new GridLayout(6, false));
		new Label(composite, SWT.NONE)
				.setText(Messages.LabelLayoutGroup_position_alignment);
		alignField = new CCombo(composite, SWT.DROP_DOWN | SWT.READ_ONLY
				| SWT.BORDER);
		alignField
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		String[] items = new String[36];
		Object[] insertions = new Object[4];
		for (int i = 0; i < items.length; i++) {
			int group = i % 9 / 3;
			int item = i % 3;
			switch (i / 9) {
			case 0:
				insertions[0] = words[2];
				insertions[2] = words[0];
				insertions[1] = words[group + 3];
				insertions[3] = words[item + 3];
				break;
			case 1:
				insertions[0] = words[5];
				insertions[2] = words[3];
				insertions[1] = words[2-group];
				insertions[3] = words[item];
				break;
			case 2:
				insertions[0] = words[0];
				insertions[2] = words[2];
				insertions[1] = words[5 - group];
				insertions[3] = words[item + 3];
				break;
			default:
				insertions[0] = words[3];
				insertions[2] = words[5];
				insertions[1] = words[group];
				insertions[3] = words[item];
				break;
			}
			items[i] = NLS.bind(Messages.LabelLayoutGroup_image_label,
					insertions);
		}
		alignField.setItems(items);
		alignField.setVisibleItemCount(9);
		new Label(composite, SWT.NONE)
				.setText(Messages.LabelLayoutGroup_distance);
		distField = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		distField.setLayoutData(new GridData(30, SWT.DEFAULT));
		distField.addModifyListener(this);
		new Label(composite, SWT.NONE)
				.setText(Messages.LabelLayoutGroup_indent);
		indentField = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		indentField.setLayoutData(new GridData(30, SWT.DEFAULT));
		indentField.addModifyListener(this);

	}

	public void fillValues(boolean hide, int alignment, int distance, int indent) {
		hideButton.setSelection(hide);
		alignField.select(alignment);
		distField.setText(nf.format(distance * 0.1d + 0.05d*Math.signum(distance)));
		indentField.setText(nf.format(indent * 0.1d + 0.05d*Math.signum(indent)));
		if (!dontHide)
			updateFields();
	}

	private void updateFields() {
		boolean enabled = !hideButton.getSelection();
		alignField.setEnabled(enabled);
		distField.setEnabled(enabled);
		indentField.setEnabled(enabled);
	}

	/**
	 * @return hide
	 */
	public boolean isHide() {
		return hideButton.getSelection();
	}

	/**
	 * @return align
	 */
	public int getAlign() {
		return alignField.getSelectionIndex();
	}

	/**
	 * @return dist
	 */
	public int getDist() {
		try {
			return (int) (10 * nf.parse(distField.getText()).doubleValue() + 0.5);
		} catch (ParseException e) {
			// should never happen
			return 50;
		}
	}

	/**
	 * @return indent
	 */
	public int getIndent() {
		try {
			return (int) (10 * nf.parse(indentField.getText()).doubleValue() + 0.5);
		} catch (ParseException e) {
			// should never happen
			return 50;
		}
	}

	public void modifyText(ModifyEvent e) {
		for (Object listener : listeners.getListeners())
			((ModifyListener) listener).modifyText(e);
	}

	public void addModifyListener(ModifyListener listener) {
		listeners.add(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		listeners.remove(listener);
	}

	public String validate() {
		try {
			Number n = nf.parse(distField.getText());
			if (n.doubleValue() < 0)
				return Messages.LabelLayoutGroup_distance_negative;

		} catch (Exception e) {
			return Messages.LabelLayoutGroup_bad_distance;
		}
		try {
			nf.parse(indentField.getText());
		} catch (Exception e) {
			return Messages.LabelLayoutGroup_bad_indent;
		}
		return null;
	}

}
