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
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.QueryOptions;
import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.SearchResultGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class FindDuplicatesDialog extends ZTitleAreaDialog implements Listener {

	private static final String SETTINGSID = "com.bdaum.zoom.findDuplicatesDialog"; //$NON-NLS-1$

	private static final String KIND = "kind"; //$NON-NLS-1$

	private static final String IGNOREDERIVED = "ignoreDerived"; //$NON-NLS-1$

	private static final String EXTENSION = "extension"; //$NON-NLS-1$

	private boolean ignoreDerivates;
	private int kind;

	private Button byExposureDataButton;

	private Button bySimilarityButton;

	private IDialogSettings settings;

	private CheckboxButton ignoreDerivedButton;

	private Button combinedButton;

	private SearchResultGroup searchResultGroup;

	private QueryOptions options;

	private Button byFileNameButton, byOriginalButton;

	private CheckboxButton extensionButton;

	private boolean withExtension;

	private Algorithm selectedAlgorithm;

	public FindDuplicatesDialog(Shell parentShell) {
		super(parentShell, HelpContextIds.DUPLICATES_DIALOG);
		options = UiActivator.getDefault().getQueryOptions();
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.FindDuplicatesDialog_find_duplicates);
		setMessage(Messages.FindDuplicatesDialog_please_select_the_method);
		searchResultGroup.updateControls();
		fillValues();
		updateControls();
	}

	private void updateControls() {
		searchResultGroup.setEnabled(bySimilarityButton.getSelection()
				|| combinedButton.getSelection());
		extensionButton.setEnabled(byFileNameButton.getSelection() || byOriginalButton.getSelection());
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout());
		final CGroup methodGroup = new CGroup(comp, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		layoutData.horizontalIndent = 10;
		layoutData.verticalIndent = 5;
		methodGroup.setLayoutData(layoutData);
		methodGroup.setLayout(new GridLayout(2, false));
		methodGroup.setText(Messages.FindDuplicatesDialog_method);
		byFileNameButton = new Button(methodGroup, SWT.RADIO);
		byFileNameButton.addListener(SWT.Selection, this);
		new Label(methodGroup, SWT.NONE)
				.setText(Messages.FindDuplicatesDialog_by_filename);
		byOriginalButton = new Button(methodGroup, SWT.RADIO);
		byOriginalButton.addListener(SWT.Selection, this);
		new Label(methodGroup, SWT.NONE)
				.setText(Messages.FindDuplicatesDialog_by_original);
		new Label(methodGroup, SWT.NONE);
		extensionButton = WidgetFactory.createCheckButton(methodGroup,
				Messages.FindDuplicatesDialog_include_ext, null);

		byExposureDataButton = new Button(methodGroup, SWT.RADIO);
		byExposureDataButton.addListener(SWT.Selection, this);
		new Label(methodGroup, SWT.NONE)
				.setText(Messages.FindDuplicatesDialog_match_by_exposure);
		if (!Core.getCore().getDbManager().getMeta(true).getNoIndex()) {
			bySimilarityButton = new Button(methodGroup, SWT.RADIO);
			bySimilarityButton.addListener(SWT.Selection, this);
			new Label(methodGroup, SWT.NONE)
					.setText(Messages.FindDuplicatesDialog_match_by_similarity);

			combinedButton = new Button(methodGroup, SWT.RADIO);
			combinedButton.addListener(SWT.Selection, this);
			new Label(methodGroup, SWT.NONE)
					.setText(Messages.FindDuplicatesDialog_match_similarity_exposure_data);
			new Label(methodGroup, SWT.NONE);
			searchResultGroup = new SearchResultGroup(methodGroup, SWT.NONE,
					true, false, false, getButton(OK), new GridData(SWT.FILL,
							SWT.BEGINNING, true, false));
		}
		layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1);
		layoutData.horizontalIndent = 10;
		ignoreDerivedButton = WidgetFactory.createCheckButton(comp,
				Messages.FindDuplicatesDialog_ignore_derived, layoutData);
		return area;
	}

	private void fillValues() {
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		try {
			int buttonpressed = settings.getInt(KIND);
			switch (buttonpressed) {
			case Constants.DUPES_BYEXPOSUREDATA:
				byExposureDataButton.setSelection(true);
				break;
			case Constants.DUPES_BYSIMILARITY:
				if (bySimilarityButton != null)
					bySimilarityButton.setSelection(true);
				else
					byExposureDataButton.setSelection(true);
				break;
			case Constants.DUPES_COMBINED:
				if (combinedButton != null)
					combinedButton.setSelection(true);
				else
					byExposureDataButton.setSelection(true);
				break;
			case Constants.DUPES_BYORIGINAL:
				byOriginalButton.setSelection(true);
				break;
			default:
				byFileNameButton.setSelection(true);
				break;
			}
		} catch (NumberFormatException e) {
			if (combinedButton != null)
				combinedButton.setSelection(true);
			else
				byExposureDataButton.setSelection(true);
		}
		ignoreDerivedButton.setSelection(settings.getBoolean(IGNOREDERIVED));
		extensionButton.setSelection(settings.getBoolean(EXTENSION));
		searchResultGroup.fillValues(options.getScore(), options.getMaxHits(),
				options.getMethod(), 0);
	}

	@Override
	protected void okPressed() {
		selectedAlgorithm = searchResultGroup.getSelectedAlgorithm();
		kind = Constants.DUPES_COMBINED;
		if (byExposureDataButton.getSelection())
			kind = Constants.DUPES_BYEXPOSUREDATA;
		else if (bySimilarityButton != null
				&& bySimilarityButton.getSelection())
			kind = Constants.DUPES_BYSIMILARITY;
		else if (byFileNameButton.getSelection())
			kind = Constants.DUPES_BYFILENAME;
		else if (byOriginalButton.getSelection())
			kind = Constants.DUPES_BYORIGINAL;
		settings.put(KIND, kind);
		ignoreDerivates = ignoreDerivedButton.getSelection();
		withExtension = extensionButton.getSelection();
		settings.put(IGNOREDERIVED, ignoreDerivates);
		settings.put(EXTENSION, withExtension);
		super.okPressed();
	}

	public int getKind() {
		return kind;
	}

	public boolean getIgnoreDerivates() {
		return ignoreDerivates;
	}

	public int getMethod() {
		return selectedAlgorithm == null ? -1 : selectedAlgorithm.getId();
	}

	/**
	 * @return withExtension
	 */
	public boolean isWithExtension() {
		return withExtension;
	}

	@Override
	public void handleEvent(Event event) {
		updateControls();
	}

}
