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
 * (c) 2009-2014 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePagePart;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;

public class ColorCodePage extends AbstractPreferencePagePart {

	private static final String[] colorCodeOptions = new String[] {
			PreferenceConstants.COLORCODE_NO,
			PreferenceConstants.COLORCODE_AUTO,
			PreferenceConstants.COLORCODE_MANUAL };

	private static final String[] colorCodeLabels = new String[] {
			Messages.getString("AppearancePreferencePage.no"), //$NON-NLS-1$
			Messages.getString("AppearancePreferencePage.auto"), //$NON-NLS-1$
			Messages.getString("AppearancePreferencePage.manual") //$NON-NLS-1$
	};

	private ComboViewer colorCodeViewer;

	private CGroup autoGroup;

	private ColorCodeGroup[] colorCodeGroups = new ColorCodeGroup[QueryField.COLORCODELABELS.length - 1];

	private AbstractPreferencePage parentPage;

	@Override
	public String getLabel() {
		return Messages.getString("ColorCodePage.color_codes"); //$NON-NLS-1$
	}

	public Control createPageContents(Composite parent,
			AbstractPreferencePage parentPage) {
		this.parentPage = parentPage;
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		createThumbGroup(composite);
		createAutoColorGroup(composite);
		return composite;
	}

	@Override
	public void fillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		colorCodeViewer.setSelection(new StructuredSelection(preferenceStore
				.getString(PreferenceConstants.SHOWCOLORCODE)));
		updateAutoGroup();
		String[] tokens = new String[colorCodeGroups.length];
		String s = preferenceStore
				.getString(PreferenceConstants.AUTOCOLORCODECRIT);
		if (s != null) {
			int i = 0;
			int off = 0;
			while (i < colorCodeGroups.length) {
				int p = s.indexOf('\n', off);
				if (p < 0)
					break;
				tokens[i++] = s.substring(off, p);
				off = p + 1;
			}
		}
		for (int i = 0; i < colorCodeGroups.length; i++)
			colorCodeGroups[i].fillValues(tokens[i]);
	}

	private static IPreferenceStore getPreferenceStore() {
		return UiActivator.getDefault().getPreferenceStore();
	}

	@Override
	public void performDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.BACKGROUNDCOLOR,
				preferenceStore
						.getDefaultString(PreferenceConstants.BACKGROUNDCOLOR));
		preferenceStore
				.setValue(
						PreferenceConstants.AUTOCOLORCODECRIT,
						preferenceStore
								.getDefaultString(PreferenceConstants.AUTOCOLORCODECRIT));
	}

	@Override
	public void performOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		IStructuredSelection selection = (IStructuredSelection) colorCodeViewer
				.getSelection();
		if (!selection.isEmpty())
			preferenceStore.setValue(PreferenceConstants.SHOWCOLORCODE,
					(String) selection.getFirstElement());
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colorCodeGroups.length; i++)
			sb.append(colorCodeGroups[i].encodeCriterion()).append("\n"); //$NON-NLS-1$
		preferenceStore.setValue(PreferenceConstants.AUTOCOLORCODECRIT,
				sb.toString());
	}

	private void createThumbGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true,
				false));
		composite.setLayout(new GridLayout(2, false));
		colorCodeViewer = createComboViewer(
				composite,
				Messages.getString("AppearancePreferencePage.show_colorcode"), colorCodeOptions, colorCodeLabels, false); //$NON-NLS-1$
		colorCodeViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						updateAutoGroup();
					}
				});
	}

	protected void updateAutoGroup() {
		autoGroup.setVisible(PreferenceConstants.COLORCODE_AUTO
				.equals(((IStructuredSelection) colorCodeViewer.getSelection())
						.getFirstElement()));
	}

	@SuppressWarnings("unused")
	private void createAutoColorGroup(Composite composite) {
		autoGroup = createGroup(composite, 7,
				Messages.getString("AppearancePreferencePage.autocoding")); //$NON-NLS-1$
		((GridLayout) autoGroup.getLayout()).verticalSpacing = 0;
		new Label(autoGroup, SWT.NONE);
		new Label(autoGroup, SWT.NONE).setText("   "); //$NON-NLS-1$
		new Label(autoGroup, SWT.NONE).setText(Messages
				.getString("AppearancePreferencePage.group")); //$NON-NLS-1$
		new Label(autoGroup, SWT.NONE).setText(Messages
				.getString("AppearancePreferencePage.field")); //$NON-NLS-1$
		new Label(autoGroup, SWT.NONE).setText(Messages
				.getString("AppearancePreferencePage.relation")); //$NON-NLS-1$
		new Label(autoGroup, SWT.NONE).setText(Messages
				.getString("AppearancePreferencePage.value")); //$NON-NLS-1$
		new Label(autoGroup, SWT.NONE);
		for (int i = 0; i < colorCodeGroups.length; i++)
			colorCodeGroups[i] = new ColorCodeGroup(autoGroup, i, parentPage);
	}

	@Override
	public void performCancel() {
		// do nothing
	}

}
