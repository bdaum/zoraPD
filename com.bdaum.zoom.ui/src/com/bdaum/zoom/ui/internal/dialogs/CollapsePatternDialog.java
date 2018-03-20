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
 * (c) 2009-2014 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IRelationDetector;
import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class CollapsePatternDialog extends ZTitleAreaDialog {

	private static final String HISTORY = "history"; //$NON-NLS-1$
	private static final String DEFAULTPATTERN = "/*.*"; //$NON-NLS-1$
	private static final String DEFAULTPATTERN2 = "/*_v*.*;/*.*"; //$NON-NLS-1$
	private static final String SETTINGSID = "com.bdaum.zoom.collapseDialog"; //$NON-NLS-1$
	private static final int RESET = 9999;
	private static final String NOSTACK = "nostack"; //$NON-NLS-1$
	private String pattern;
	private Combo patternField;
	private List<String> histList = new ArrayList<String>();
	private CheckboxButton stackButton;
	private boolean stack;
	private IDialogSettings dialogSettings;

	public CollapsePatternDialog(Shell parentShell, String helpId,
			String pattern) {
		super(parentShell, helpId);
		this.pattern = pattern;
		dialogSettings = getDialogSettings(
				UiActivator.getDefault(), SETTINGSID);
		histList = Core.fromStringList(dialogSettings.get(HISTORY), "\n"); //$NON-NLS-1$
		stack = !dialogSettings.getBoolean(NOSTACK);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.CollapsePatternDialog_collapse_pattern);
		setMessage(Messages.CollapsePatternDialog_define_filename);
		patternField.setVisibleItemCount(8);
		Collection<IRelationDetector> relationDetectors = UiActivator
				.getDefault().getRelationDetectors();
		for (IRelationDetector detector : relationDetectors) {
			String pattern = detector.getCollapsePattern();
			if (pattern != null && !histList.contains(pattern))
				histList.add(pattern);
		}
		if (!histList.contains(DEFAULTPATTERN2))
			histList.add(DEFAULTPATTERN2);
		if (!histList.contains(DEFAULTPATTERN))
			histList.add(DEFAULTPATTERN);
		patternField.setItems(histList.toArray(new String[histList.size()]));
		patternField.setText(pattern == null ? DEFAULTPATTERN : pattern);
		stackButton.setSelection(stack);
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE)
				.setText(Messages.CollapsePatternDialog_pattern);
		patternField = new Combo(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		patternField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		patternField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		});
		patternField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}
		});
		stackButton = WidgetFactory.createCheckButton(composite,
				Messages.CollapsePatternDialog_collapse_named_stacks,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		return area;
	}

	private boolean validate() {
		String errorMessage = null;
		String pattern = patternField.getText();
		if (pattern.isEmpty() && !stackButton.getSelection())
			errorMessage = Messages.CollapsePatternDialog_specify_pattern;
		else if (!pattern.isEmpty()) {
			errorMessage = Messages.CollapsePatternDialog_no_relevant_parts;
			int pos = 0;
			while (true) {
				int p = pattern.indexOf('/', pos);
				if (p < 0 || p >= pattern.length() - 1)
					break;
				char c = pattern.charAt(p + 1);
				if (c == '?' || c == '*') {
					errorMessage = null;
					break;
				}
				pos = p + 1;
			}
			if (errorMessage == null)
				errorMessage = WildCardFilter.validate(pattern);
		}
		setErrorMessage(errorMessage);
		return errorMessage == null;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, RESET, Messages.CollapsePatternDialog_reset, false);
		super.createButtonsForButtonBar(parent);
	}

	private void updateButtons() {
		getButton(OK).setEnabled(validate());
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == RESET) {
			patternField.setText(DEFAULTPATTERN);
			updateButtons();
			return;
		}
		super.buttonPressed(buttonId);
	}

	@Override
	protected void okPressed() {
		pattern = patternField.getText();
		histList.remove(pattern);
		histList.add(0, pattern);
		if (histList.size() > 4)
			histList = histList.subList(0, 4);
		dialogSettings.put(HISTORY, Core.toStringList(histList, '\n'));
		stack = stackButton.getSelection();
		dialogSettings.put(NOSTACK, !stack);
		if (stack)
			pattern = pattern.isEmpty() ?  UiConstants.STACKPATTERN : UiConstants.STACKPATTERN + ';' + pattern;
		super.okPressed();
	}

	public String getPattern() {
		return pattern;
	}

}
