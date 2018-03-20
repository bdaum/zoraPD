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
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.text.NumberFormat;

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

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.NumericControl;

public class LabelLayoutGroup extends Composite implements ModifyListener {

	private static NumberFormat nf = NumberFormat.getNumberInstance();
	static {
		nf.setMaximumFractionDigits(1);
	}
	private CheckboxButton hideButton;
	private CCombo alignField;
	private NumericControl distField, indentField;
	private String[] words = new String[] { Messages.LabelLayoutGroup_left,
			Messages.LabelLayoutGroup_center, Messages.LabelLayoutGroup_right,
			Messages.LabelLayoutGroup_top, Messages.LabelLayoutGroup_middle,
			Messages.LabelLayoutGroup_bottom };
	private ListenerList<ModifyListener> listeners = new ListenerList<ModifyListener>();
	private final boolean dontHide;
	private char unit = Core.getCore().getDbFactory().getDimUnit();

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
				.setText(Messages.LabelLayoutGroup_distance + captionUnitcmin());
		distField = new NumericControl(composite, SWT.NONE);
		distField.setDigits(1);
		distField.setMaximum(unit == 'i' ? 1000 : 2500);
		distField.addModifyListener(this);
		new Label(composite, SWT.NONE)
				.setText(Messages.LabelLayoutGroup_indent + captionUnitcmin());
		indentField = new NumericControl(composite, SWT.NONE);
		indentField.setDigits(1);
		indentField.setMaximum(unit == 'i' ? 400 : 1000);
		indentField.setMinimum(unit == 'i' ? -400 : -1000);
		indentField.addModifyListener(this);
	}

	public void fillValues(boolean hide, int alignment, int distance, int indent) {
		hideButton.setSelection(hide);
		alignField.select(alignment);
		distField.setSelection(fromMm(distance));
		indentField.setSelection(fromMm(indent));
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
		return toMm(distField.getSelection());
	}

	/**
	 * @return indent
	 */
	public int getIndent() {
		return toMm(indentField.getSelection());
	}

	public void modifyText(ModifyEvent e) {
		for (ModifyListener listener : listeners)
			listener.modifyText(e);
	}

	public void addModifyListener(ModifyListener listener) {
		listeners.add(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		listeners.remove(listener);
	}

	private String captionUnitcmin() {
		return unit == 'i' ? " (in)" : " (cm)"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private int fromMm(double x) {
		return (int) ((unit == 'i' ? (x / 2.54d ) : x) + + 0.5d);
	}

	private int toMm(int x) {
		return unit == 'i' ? (int) (x * 2.54d  + 0.5d) : x;
	}

}
