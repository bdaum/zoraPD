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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.Meta_type;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class SplitCatDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.splitCatDialog"; //$NON-NLS-1$
	private String timeline;
	private CheckedText descriptionField;
	private ComboViewer timelineViewer;
	private String filename;
	private String description;
	private CheckboxButton deleteButton;
	private boolean delete;
	private final int number;
	private final boolean selected;
	private FileEditor fileEditor;
	private String locationOption;
	private ComboViewer locationViewer;

	public SplitCatDialog(Shell parentShell, String timeline, int number, boolean selected) {
		super(parentShell, HelpContextIds.SPLITCAT);
		this.timeline = timeline;
		this.number = number;
		this.selected = selected;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.SplitCatDialog_split_catalog);
		setMessage(NLS.bind(selected ? Messages.SplitCatDialog_extract_the_current_selection
				: Messages.SplitCatDialog_extract_the_current_collection, number));
		updateButtons();
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		comp.setLayout(gridLayout);
		createHeaderGroup(comp);
		return comp;
	}

	private void updateButtons() {
		boolean enabled = validate();
		getShell().setModified(enabled);
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	private boolean validate() {
		boolean valid = fileEditor.getText() != null;
		setErrorMessage(valid ? null : Messages.SplitCatDialog_enter_name_of_cat);
		return valid;
	}

	private void createHeaderGroup(Composite comp) {
		UiActivator activator = UiActivator.getDefault();
		fileEditor = new FileEditor(comp, SWT.SAVE | SWT.READ_ONLY, Messages.EditMetaDialog_file_name, true,
				activator.getCatFileExtensions(), activator.getSupportedCatFileNames(), null,
				'*' + Constants.CATALOGEXTENSION, true, getDialogSettings(UiActivator.getDefault(), SETTINGSID));
		fileEditor.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateButtons();
			}
		});
		Composite header = new Composite(comp, SWT.NONE);
		header.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		header.setLayout(new GridLayout(2, false));
		new Label(header, SWT.NONE).setText(Messages.EditMetaDialog_description);
		final GridData gd_description = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_description.heightHint = 100;
		gd_description.widthHint = 400;
		descriptionField = new CheckedText(header, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI | SWT.WRAP);
		descriptionField.setLayoutData(gd_description);
		// timeline
		new Label(header, SWT.NONE).setText(Messages.EditMetaDialog_create_timeline);
		timelineViewer = new ComboViewer(header);
		timelineViewer.getControl().setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		timelineViewer.setContentProvider(ArrayContentProvider.getInstance());
		timelineViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (Meta_type.timeline_year.equals(element))
					return Messages.EditMetaDialog_by_year;
				if (Meta_type.timeline_month.equals(element))
					return Messages.EditMetaDialog_by_month;
				if (Meta_type.timeline_day.equals(element))
					return Messages.EditMetaDialog_by_day;
				return Messages.EditMetaDialog_none;
			}
		});
		timelineViewer.setInput(Meta_type.timelineALLVALUES);
		timelineViewer.setSelection(new StructuredSelection(timeline == null ? Meta_type.timeline_no : timeline), true);
		// locations
		new Label(header, SWT.NONE).setText(Messages.EditMetaDialog_create_loc_folders);
		locationViewer = new ComboViewer(header);
		locationViewer.getControl().setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		locationViewer.setContentProvider(ArrayContentProvider.getInstance());
		locationViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (Meta_type.locationFolders_country.equals(element))
					return Messages.EditMetaDialog_byCountry;
				if (Meta_type.locationFolders_state.equals(element))
					return Messages.EditMetaDialog_byState;
				if (Meta_type.locationFolders_city.equals(element))
					return Messages.EditMetaDialog_byCity;
				return Messages.EditMetaDialog_none;
			}
		});
		locationViewer.setInput(Meta_type.locationFoldersALLVALUES);
		locationViewer.setSelection(
				new StructuredSelection(locationOption == null ? Meta_type.locationFolders_no : locationOption), true);
		deleteButton = WidgetFactory.createCheckButton(header, Messages.SplitCatDialog_remove_extracted_entries,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 3, 1));
		deleteButton.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (deleteButton.getSelection() && !AcousticMessageDialog.openQuestion(getShell(),
						Messages.SplitCatDialog_delete_exported, Messages.SplitCatDialog_delete_exported_msg))
					deleteButton.setSelection(false);
			}
		});
	}

	@Override
	protected void okPressed() {
		filename = fileEditor.getText();
		description = descriptionField.getText();
		timeline = (String) timelineViewer.getStructuredSelection().getFirstElement();
		if (timeline == null)
			timeline = Meta_type.timeline_no;
		locationOption = (String) locationViewer.getStructuredSelection().getFirstElement();
		if (locationOption == null)
			locationOption = Meta_type.locationFolders_no;
		delete = deleteButton.getSelection();
		super.okPressed();
	}

	public String getFile() {
		return filename;
	}

	public boolean getDelete() {
		return delete;
	}

	public String getDescription() {
		return description;
	}

	public String getTimeline() {
		return timeline;
	}

	public String getLocationOption() {
		return locationOption;
	}

}
