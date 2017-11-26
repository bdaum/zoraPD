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
 * (c) 2017 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import com.bdaum.zoom.ui.internal.preferences.PatternEditDialog;

public class PatternListEditor extends Composite implements SelectionListener, ISelectionChangedListener {

	public static final String ACCEPTS = Messages.PatternListEditor_accept;
	public static final String REJECTS = Messages.PatternListEditor_reject;

	private ListViewer viewer;
	private Button addButton;
	private Button removeButton;
	private Button upButton;
	private Button downButton;
	private boolean rule;
	private String separator;
	private String forbiddenChars;
	private String[] patterns;
	private String title;
	private String itemText;
	private String dflt;

	public PatternListEditor(Composite parent, int style, String title, String itemText, String dflt, boolean rule,
			String separator) {
		super(parent, style & ~SWT.BORDER);
		this.title = title;
		this.itemText = itemText;
		this.dflt = dflt;
		this.rule = rule;
		this.separator = separator;
		this.forbiddenChars = separator;
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setLayout(new GridLayout(2, false));
		viewer = new ListViewer(this, style);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 300;
		viewer.getControl().setLayoutData(layoutData);
		viewer.addSelectionChangedListener(this);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		Composite buttonBar = new Composite(this, SWT.NONE);
		buttonBar.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
		buttonBar.setLayout(new GridLayout());
		addButton = createPushButton(buttonBar, Messages.PatternListEditor_add);
		removeButton = createPushButton(buttonBar, Messages.PatternListEditor_remove);
		upButton = createPushButton(buttonBar, Messages.PatternListEditor_up);
		downButton = createPushButton(buttonBar, Messages.PatternListEditor_down);
	}

	public void setInput(String input) {
		patterns = parseString(input);
		viewer.setInput(patterns);
		updateButtons();
	}

	private Button createPushButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.addSelectionListener(this);
		return button;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		Widget widget = e.widget;
		if (widget == addButton)
			addPressed();
		else if (widget == removeButton)
			removePressed();
		else if (widget == upButton)
			upPressed();
		else if (widget == downButton)
			downPressed();
	}

	private void downPressed() {
		swap(false);
	}

	private void upPressed() {
		swap(true);
	}

	private void removePressed() {
		int index = getSelectionIndex();
		if (index >= 0) {
			String[] newPatterns = new String[patterns.length - 1];
			System.arraycopy(patterns, 0, newPatterns, 0, index);
			System.arraycopy(patterns, index+1, newPatterns, index, patterns.length - index - 1);
			viewer.setInput(patterns = newPatterns);
			updateButtons();
		}
	}

	private void swap(boolean up) {
		int index = getSelectionIndex();
		if (index >= 0) {
			String oldLine =  patterns[index];
			int target = up ? index - 1 : index + 1;
			String line = patterns[target];
			patterns[target] = patterns[index];
			patterns[index] = line;
			viewer.setInput(patterns);
			viewer.setSelection(new StructuredSelection(oldLine));
			updateButtons();
		}
	}

	private void addPressed() {
		PatternEditDialog dialog = new PatternEditDialog(getShell(), title, itemText, dflt, forbiddenChars, rule);
		if (dialog.open() == Window.OK) {
			String input = dialog.getResult();
			int index = getSelectionIndex();
			String[] newPatterns = new String[patterns.length + 1];
			if (index >= 0) {
				System.arraycopy(patterns, 0, newPatterns, 0, index);
				newPatterns[index] = input;
				System.arraycopy(patterns, index, newPatterns, index + 1, patterns.length - index);
			} else {
				System.arraycopy(patterns, 0, newPatterns, 0, patterns.length);
				newPatterns[patterns.length] = input;
			}
			viewer.setInput(patterns = newPatterns);
			viewer.setSelection(new StructuredSelection(input));
			updateButtons();
		}
	}

	private String[] parseString(String stringList) {
		List<String> result = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(stringList, separator);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.startsWith(">")) //$NON-NLS-1$
				token = token.substring(1) + ACCEPTS;
			else
				token += REJECTS;
			result.add(token);
		}
		return result.toArray(new String[result.size()]);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		updateButtons();
	}

	private void updateButtons() {
		int index = -1;
		int size = 0;
		if (patterns != null) {
			index = getSelectionIndex();
			size = patterns.length;
		}
		removeButton.setEnabled(index >= 0);
		upButton.setEnabled(size > 1 && index > 0);
		downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
	}

	private int getSelectionIndex() {
		String line = (String) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		for (int i = 0; i < patterns.length; i++)
			if (patterns[i].equals(line))
				return i;
		return -1;
	}
	
	private String createList(String[] items) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < items.length; i++) {
			if (sb.length() > 0)
				sb.append(separator);
			if (items[i].endsWith(REJECTS))
				sb.append(items[i].substring(0, items[i].length()
						- REJECTS.length()));
			else if (items[i].endsWith(ACCEPTS))
				sb.append('>').append(
						items[i].substring(0, items[i].length()
								- ACCEPTS.length()));
			else
				sb.append(items[i]);
		}
		return sb.toString();
	}
	
	public String getResult() {
		return createList(patterns);
	}

}
