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
 * (c) 2009-2918 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.preferences;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.IRelationDetector;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.AutoRuleComponent;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;

public class AutoPreferencePage extends AbstractPreferencePage {

	public static final String ID = "com.bdaum.zoom.ui.preferences.AutoPreferencePage"; //$NON-NLS-1$
	public static final String RULES = "rules"; //$NON-NLS-1$
	private CTabItem tabItem0;
	private AutoRuleComponent ruleComponent;
	private CTabItem tabItem1;
	private CheckboxButton autoButton;
	private Map<String, CheckboxButton> detectorButtons = new HashMap<>(5);
	private CheckboxButton xmpButton;
	private ComboViewer relviewer;

	public AutoPreferencePage() {
		setDescription(Messages.getString("AutoPreferencePage.control_which")); //$NON-NLS-1$
	}

	@Override
	public void applyData(Object data) {
		tabFolder.setSelection(RULES.equals(data) ? tabItem1 : tabItem0);
	}

	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.AUTO_PREFERENCE_PAGE);
		createTabFolder(composite, "Auto"); //$NON-NLS-1$
		tabItem0 = UiUtilities.createTabItem(tabFolder, Messages.getString("AutoPreferencePage.relations"), Messages.getString("AutoPreferencePage.relationships_tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
		Composite comp0 = createRelationsPage(tabFolder);
		tabItem0.setControl(comp0);
		tabItem1 = UiUtilities.createTabItem(tabFolder,
				Messages.getString("AutoPreferencePage.automatic_collection_creation"), Messages.getString("AutoPreferencePage.autorules_tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
		ruleComponent = new AutoRuleComponent(tabFolder, SWT.NONE, this);
		tabItem1.setControl(ruleComponent.getControl());
		initTabFolder(0);
		createExtensions(tabFolder, "com.bdaum.zoom.ui.preferences.AutoPreferencePage"); //$NON-NLS-1$
		fillValues();
	}

	private Composite createRelationsPage(CTabFolder tabFolder) {
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		new Label(composite, SWT.NONE).setText(
				Messages.getString("AutoPreferencePage.relations_expl")); //$NON-NLS-1$
		CGroup group = UiUtilities.createGroup(composite, 2,Messages.getString("AutoPreferencePage.options")); //$NON-NLS-1$
		if (Constants.WIN32 || Constants.OSX) {
			final String[] deriveOptions = new String[] { Constants.DERIVE_NO, Constants.DERIVE_FOLDER,
					Constants.DERIVE_ALL };
			final String[] deriveLabels = new String[] { Messages.getString("ImportPreferencePage.no"), //$NON-NLS-1$
					Messages.getString("ImportPreferencePage.within_folders"), //$NON-NLS-1$
					Messages.getString("ImportPreferencePage.across_folders") }; //$NON-NLS-1$
			relviewer = createComboViewer(group,
					Messages.getString("ImportPreferencePage.automatically_derive_relationships"), //$NON-NLS-1$
					deriveOptions, deriveLabels, false);
		}
		autoButton = WidgetFactory.createCheckButton(group,
				Messages.getString("ImportPreferencePage.automatically_detect_derived"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		Collection<IRelationDetector> relationDetectors = UiActivator.getDefault().getRelationDetectors();
		for (IRelationDetector detector : relationDetectors)
			detectorButtons.put(detector.getId(), WidgetFactory.createCheckButton(group, detector.getDescription(),
					new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1)));
		xmpButton = WidgetFactory.createCheckButton(group,
				Messages.getString("ImportPreferencePage.applyXmp_to_Derivates"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		return composite;
	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		ruleComponent.fillValues(preferenceStore.getString(PreferenceConstants.AUTORULES));
		autoButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.AUTODERIVE));
		xmpButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.APPLYXMPTODERIVATES));
		StringTokenizer st = new StringTokenizer(preferenceStore.getString(PreferenceConstants.RELATIONDETECTORS));
		while (st.hasMoreTokens()) {
			CheckboxButton button = detectorButtons.get(st.nextToken());
			if (button != null)
				button.setSelection(true);
		}
		if (relviewer != null)
			relviewer.setSelection(
					new StructuredSelection(preferenceStore.getString(PreferenceConstants.DERIVERELATIONS)));
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.AUTORULES, ruleComponent.getResult());
		preferenceStore.setValue(PreferenceConstants.AUTODERIVE, autoButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.APPLYXMPTODERIVATES, xmpButton.getSelection());
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, CheckboxButton> entry : detectorButtons.entrySet())
			if (entry.getValue().getSelection()) {
				if (sb.length() > 0)
					sb.append(' ');
				sb.append(entry.getKey());
			}
		preferenceStore.setValue(PreferenceConstants.RELATIONDETECTORS, sb.toString());
		if (relviewer != null) {
			IStructuredSelection selection = relviewer.getStructuredSelection();
			if (!selection.isEmpty())
				preferenceStore.setValue(PreferenceConstants.DERIVERELATIONS, (String) selection.getFirstElement());
		}
		ruleComponent.accelerate();
	}

	@Override
	public void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.AUTORULES,
				preferenceStore.getDefaultString(PreferenceConstants.AUTORULES));
		preferenceStore.setValue(PreferenceConstants.AUTODERIVE,
				preferenceStore.getDefaultBoolean(PreferenceConstants.AUTODERIVE));
		preferenceStore.setValue(PreferenceConstants.APPLYXMPTODERIVATES,
				preferenceStore.getDefaultBoolean(PreferenceConstants.APPLYXMPTODERIVATES));
		preferenceStore.setValue(PreferenceConstants.RELATIONDETECTORS,
				preferenceStore.getDefaultBoolean(PreferenceConstants.RELATIONDETECTORS));
		preferenceStore.setValue(PreferenceConstants.DERIVERELATIONS,
				preferenceStore.getDefaultString(PreferenceConstants.DERIVERELATIONS));
	}

}
