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
 * (c) 2009-2017 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.preferences;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.LabelConfigGroup;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;

public class AppearancePreferencePage extends AbstractPreferencePage implements ISelectionChangedListener {

	private static final String PAGEID = "com.bdaum.zoom.ui.preferences.AppearancePreferencePage"; //$NON-NLS-1$
	private static final String[] ratingOptions = new String[] { PreferenceConstants.SHOWRATING_NO,
			PreferenceConstants.SHOWRATING_SIZE, PreferenceConstants.SHOWRATING_COUNT };
	private static final String[] ratingLabels = new String[] { Messages.getString("AppearancePreferencePage.no"), //$NON-NLS-1$
			Messages.getString("AppearancePreferencePage.by_size"), //$NON-NLS-1$
			Messages.getString("AppearancePreferencePage.by_count") //$NON-NLS-1$
	};
	private static final String CSS = ".css"; //$NON-NLS-1$
	private ComboViewer colorViewer;
	private CheckboxButton rotateButton;
	private CheckboxButton voiceNoteButton;
	private CheckboxButton expandButton;
	private CheckboxButton doneButton;
	private CheckboxButton locationButton;
	private Spinner regionField;
	private String theme;
	private Label comment;
	private RadioButtonGroup showRatingGroup;
	private LabelConfigGroup labelConfigGroup;
	private ComboViewer distViewer;
	private ComboViewer dimViewer;

	public AppearancePreferencePage() {
		setDescription(Messages.getString("AppearancePreferencePage.appearance_descr")); //$NON-NLS-1$
	}

	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.APPEARANCE_PREFERENCE_PAGE);
		createTabFolder(composite, "Appearance"); //$NON-NLS-1$
		UiUtilities.createTabItem(tabFolder, Messages.getString("AppearancePreferencePage.color_scheme"), //$NON-NLS-1$
				Messages.getString("AppearancePreferencePage.scheme_tooltip")) //$NON-NLS-1$
				.setControl(createColorSchemeGroup(tabFolder));
		UiUtilities.createTabItem(tabFolder, Messages.getString("AppearancePreferencePage.thumbnails"), //$NON-NLS-1$
				Messages.getString("AppearancePreferencePage.thumbnail_tooltip")) //$NON-NLS-1$
				.setControl(createThumbnailsGroup(tabFolder));
		initTabFolder(0);
		createExtensions(tabFolder, PAGEID);
		fillValues();
	}

	private Control createColorSchemeGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		CGroup schemeGroup = UiUtilities.createGroup(composite, 2,
				Messages.getString("AppearancePreferencePage.bg_color")); //$NON-NLS-1$
		List<String> dropins = new ArrayList<String>();
		String path = Platform.getInstallLocation().getURL().getPath();
		File installFolder = new File(path);
		scanCSSDropins(dropins, new File(installFolder, CssActivator.DROPINFOLDER));
		scanCSSDropins(dropins, new File(installFolder.getParentFile(), CssActivator.DROPINFOLDER));
		String[] colorOptions = new String[dropins.size() + 5];
		String[] colorLabels = new String[dropins.size() + 5];
		if (Constants.OSX)
			colorLabels[0] = "OS X"; //$NON-NLS-1$
		else if (Constants.WIN32)
			colorLabels[0] = "Windows"; //$NON-NLS-1$
		else if (Platform.WS_GTK.equals(Platform.getWS()))
			colorLabels[0] = "GTK"; //$NON-NLS-1$
		else
			colorLabels[0] = Messages.getString("AppearancePreferencePage.platform"); //$NON-NLS-1$
		colorOptions[0] = PreferenceConstants.BACKGROUNDCOLOR_PLATFORM;
		colorLabels[1] = Messages.getString("AppearancePreferencePage.black"); //$NON-NLS-1$
		colorOptions[1] = PreferenceConstants.BACKGROUNDCOLOR_BLACK;
		colorLabels[2] = Messages.getString("AppearancePreferencePage.dark_grey"); //$NON-NLS-1$
		colorOptions[2] = PreferenceConstants.BACKGROUNDCOLOR_DARKGREY;
		colorLabels[3] = Messages.getString("AppearancePreferencePage.grey"); //$NON-NLS-1$
		colorOptions[3] = PreferenceConstants.BACKGROUNDCOLOR_GREY;
		colorLabels[4] = Messages.getString("AppearancePreferencePage.white"); //$NON-NLS-1$
		colorOptions[4] = PreferenceConstants.BACKGROUNDCOLOR_WHITE;
		int j = 5;
		for (String name : dropins)
			colorOptions[j] = colorLabels[j++] = name;
		colorViewer = createComboViewer(schemeGroup, null, colorOptions, colorLabels, false);
		comment = new Label(schemeGroup, SWT.NONE);
		comment.setText(Messages.getString("AppearancePreferencePage.switching_to_platform_colors")); //$NON-NLS-1$
		GridData layoutData = new GridData();
		layoutData.horizontalIndent = 15;
		comment.setLayoutData(layoutData);
		comment.setVisible(false);
		colorViewer.addSelectionChangedListener(this);
		CGroup unitGroup = UiUtilities.createGroup(composite, 2, Messages.getString("AppearancePreferencePage.units")); //$NON-NLS-1$
		new Label(unitGroup, SWT.NONE).setText(Messages.getString("AppearancePreferencePage.distances")); //$NON-NLS-1$
		distViewer = new ComboViewer(unitGroup, SWT.NONE);
		distViewer.setContentProvider(ArrayContentProvider.getInstance());
		distViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					if (((String) element).startsWith("m")) //$NON-NLS-1$
						return Messages.getString("AppearancePreferencePage.miles"); //$NON-NLS-1$
					if (((String) element).startsWith("n")) //$NON-NLS-1$
						return Messages.getString("AppearancePreferencePage.nautical"); //$NON-NLS-1$
				}
				return Messages.getString("AppearancePreferencePage.kilometers"); //$NON-NLS-1$
			}
		});
		distViewer.setInput(new String[] { "k", "m", "n" }); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		new Label(unitGroup, SWT.NONE).setText(Messages.getString("AppearancePreferencePage.physical_dims")); //$NON-NLS-1$
		dimViewer = new ComboViewer(unitGroup, SWT.NONE);
		dimViewer.setContentProvider(ArrayContentProvider.getInstance());
		dimViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					if (((String) element).startsWith("i")) //$NON-NLS-1$
						return Messages.getString("AppearancePreferencePage.inches"); //$NON-NLS-1$
				}
				return Messages.getString("AppearancePreferencePage.centimeters"); //$NON-NLS-1$
			}
		});
		dimViewer.setInput(new String[] { "c", "i" }); //$NON-NLS-1$//$NON-NLS-2$
		return composite;
	}

	private Control createThumbnailsGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		labelConfigGroup = new LabelConfigGroup(composite, false, true, false);
		CGroup group = UiUtilities.createGroup(composite, 2, Messages.getString("AppearancePreferencePage.decoration")); // $NO //$NON-NLS-1$
		new Label(group, SWT.NONE).setText(Messages.getString("AppearancePreferencePage.max_face_regions")); //$NON-NLS-1$
		regionField = new Spinner(group, SWT.BORDER);
		regionField.setMaximum(99);
		Label lab = new Label(group, SWT.NONE);
		lab.setText(Messages.getString("AppearancePreferencePage.color_code_options")); //$NON-NLS-1$
		lab.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2, 1));
		locationButton = WidgetFactory.createCheckButton(group,
				Messages.getString("AppearancePreferencePage.show_location"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2, 1));
		expandButton = WidgetFactory.createCheckButton(group,
				Messages.getString("AppearancePreferencePage.show_expand_collapse"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2, 1));
		doneButton = WidgetFactory.createCheckButton(group,
				Messages.getString("AppearancePreferencePage.show_status_mark"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2, 1));
		showRatingGroup = new RadioButtonGroup(group, Messages.getString("AppearancePreferencePage.show_rating"), //$NON-NLS-1$
				SWT.VERTICAL, ratingLabels);
		rotateButton = WidgetFactory.createCheckButton(group,
				Messages.getString("AppearancePreferencePage.show_rotate_buttons"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2, 1));
		voiceNoteButton = WidgetFactory.createCheckButton(group,
				Messages.getString("AppearancePreferencePage.show_voicenote"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2, 1));
		return composite;
	}

	private static void scanCSSDropins(List<String> dropins, File file) {
		if (file.isDirectory()) {
			File[] members = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(CSS);
				}
			});
			if (members != null)
				for (File f : members) {
					String name = f.getName();
					name = name.substring(0, name.length() - CSS.length());
					if (!name.equals(PreferenceConstants.BACKGROUNDCOLOR_PLATFORM)
							&& !name.equals(PreferenceConstants.BACKGROUNDCOLOR_BLACK)
							&& !name.equals(PreferenceConstants.BACKGROUNDCOLOR_DARKGREY)
							&& !name.equals(PreferenceConstants.BACKGROUNDCOLOR_GREY)
							&& !name.equals(PreferenceConstants.BACKGROUNDCOLOR_WHITE)) {
						if (!dropins.contains(name))
							dropins.add(name);
					}
				}
		}
	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		theme = preferenceStore.getString(PreferenceConstants.BACKGROUNDCOLOR);
		colorViewer.setSelection(new StructuredSelection(theme));
		distViewer.setSelection(new StructuredSelection(preferenceStore.getString(PreferenceConstants.DISTANCEUNIT)));
		dimViewer.setSelection(new StructuredSelection(preferenceStore.getString(PreferenceConstants.DIMUNIT)));
		locationButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.SHOWLOCATION));
		doneButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.SHOWDONEMARK));
		regionField.setSelection(preferenceStore.getInt(PreferenceConstants.MAXREGIONS));
		String rating = preferenceStore.getString(PreferenceConstants.SHOWRATING);
		int r = 1;
		for (int i = 0; i < ratingOptions.length; i++)
			if (ratingOptions[i].equals(rating)) {
				r = i;
				break;
			}
		showRatingGroup.setSelection(r);
		rotateButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.SHOWROTATEBUTTONS));
		voiceNoteButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.SHOWVOICENOTE));
		expandButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.SHOWEXPANDCOLLAPSE));
		labelConfigGroup.setSelection(preferenceStore.getInt(PreferenceConstants.SHOWLABEL),
				preferenceStore.getString(PreferenceConstants.THUMBNAILTEMPLATE),
				preferenceStore.getInt(PreferenceConstants.LABELFONTSIZE),
				preferenceStore.getInt(PreferenceConstants.LABELALIGNMENT), false);
		updateButtons();
	}

	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.DISTANCEUNIT,
				preferenceStore.getDefaultInt(PreferenceConstants.DISTANCEUNIT));
		preferenceStore.setValue(PreferenceConstants.DIMUNIT,
				preferenceStore.getDefaultInt(PreferenceConstants.DIMUNIT));
		preferenceStore.setValue(PreferenceConstants.SHOWLABEL,
				preferenceStore.getDefaultInt(PreferenceConstants.SHOWLABEL));
		preferenceStore.setValue(PreferenceConstants.THUMBNAILTEMPLATE,
				preferenceStore.getDefaultString(PreferenceConstants.THUMBNAILTEMPLATE));
		preferenceStore.setValue(PreferenceConstants.LABELFONTSIZE,
				preferenceStore.getDefaultInt(PreferenceConstants.LABELFONTSIZE));
		preferenceStore.setValue(PreferenceConstants.LABELFONTSIZE,
				preferenceStore.getDefaultInt(PreferenceConstants.LABELALIGNMENT));
		preferenceStore.setValue(PreferenceConstants.BACKGROUNDCOLOR,
				preferenceStore.getDefaultString(PreferenceConstants.BACKGROUNDCOLOR));
		preferenceStore.setValue(PreferenceConstants.SHOWLOCATION,
				preferenceStore.getDefaultBoolean(PreferenceConstants.SHOWLOCATION));
		preferenceStore.setValue(PreferenceConstants.SHOWDONEMARK,
				preferenceStore.getDefaultBoolean(PreferenceConstants.SHOWDONEMARK));
		preferenceStore.setValue(PreferenceConstants.MAXREGIONS,
				preferenceStore.getDefaultBoolean(PreferenceConstants.MAXREGIONS));
		preferenceStore.setValue(PreferenceConstants.SHOWRATING,
				preferenceStore.getDefaultInt(PreferenceConstants.SHOWRATING));
		preferenceStore.setValue(PreferenceConstants.SHOWROTATEBUTTONS,
				preferenceStore.getDefaultBoolean(PreferenceConstants.SHOWROTATEBUTTONS));
		preferenceStore.setValue(PreferenceConstants.SHOWVOICENOTE,
				preferenceStore.getDefaultBoolean(PreferenceConstants.SHOWVOICENOTE));
		preferenceStore.setValue(PreferenceConstants.SHOWEXPANDCOLLAPSE,
				preferenceStore.getDefaultBoolean(PreferenceConstants.SHOWEXPANDCOLLAPSE));
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		int showLabel = labelConfigGroup.getSelection();
		if (showLabel >= 0) {
			preferenceStore.setValue(PreferenceConstants.SHOWLABEL, showLabel);
			preferenceStore.setValue(PreferenceConstants.THUMBNAILTEMPLATE, labelConfigGroup.getTemplate());
			preferenceStore.setValue(PreferenceConstants.LABELFONTSIZE, labelConfigGroup.getFontSize());
			preferenceStore.setValue(PreferenceConstants.LABELALIGNMENT, labelConfigGroup.getAlignment());
		}
		IStructuredSelection selection = colorViewer.getStructuredSelection();
		if (!selection.isEmpty())
			preferenceStore.setValue(PreferenceConstants.BACKGROUNDCOLOR, (String) selection.getFirstElement());
		selection = distViewer.getStructuredSelection();
		if (!selection.isEmpty())
			preferenceStore.setValue(PreferenceConstants.DISTANCEUNIT, (String) selection.getFirstElement());
		selection = dimViewer.getStructuredSelection();
		if (!selection.isEmpty())
			preferenceStore.setValue(PreferenceConstants.DIMUNIT, (String) selection.getFirstElement());
		preferenceStore.setValue(PreferenceConstants.SHOWLOCATION, locationButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.MAXREGIONS, regionField.getSelection());
		preferenceStore.setValue(PreferenceConstants.SHOWDONEMARK, doneButton.getSelection());
		int sel = showRatingGroup.getSelection();
		if (sel >= 0)
			preferenceStore.setValue(PreferenceConstants.SHOWRATING, ratingOptions[sel]);
		preferenceStore.setValue(PreferenceConstants.SHOWROTATEBUTTONS, rotateButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.SHOWVOICENOTE, voiceNoteButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.SHOWEXPANDCOLLAPSE, expandButton.getSelection());
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		Object newTheme = colorViewer.getStructuredSelection().getFirstElement();
		comment.setVisible(PreferenceConstants.BACKGROUNDCOLOR_PLATFORM.equals(newTheme) && !newTheme.equals(theme));
	}

}
