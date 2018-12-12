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
 * (c) 2015 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.RenameGroup;

public class BulkRenameDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.bulkRenameProperties"; //$NON-NLS-1$ ;
	private RenameGroup renameGroup;
	private IDialogSettings settings;
	private final Asset asset;
	private String content;
	private String cue;
	private QueryField field;
	private int start = 1;

	public BulkRenameDialog(Shell parentShell, Asset asset) {
		super(parentShell, HelpContextIds.RENAME_DIALOG);
		this.asset = asset;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.BulkRenameDialog_rename);
		setMessage(Messages.BulkRenameDialog_rename_msg);
		fillValues();
		validate();
	}

	private void fillValues() {
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		renameGroup.fillValues(settings, null, null);
		renameGroup.update();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout());
		renameGroup = new RenameGroup(comp, SWT.NONE, asset, true,
				new RenamingTemplate[] {
						new RenamingTemplate(Messages.BulkRenameDialog_date_filename, "_" + Constants.TV_YYYY //$NON-NLS-1$
								+ Constants.TV_MM + Constants.TV_DD + "-" //$NON-NLS-1$
								+ Constants.TV_FILENAME, true),
						new RenamingTemplate(Messages.BulkRenameDialog_user_year_seq,
								Constants.TV_USER + Constants.TV_YYYY + "-" + Constants.TV_IMAGE_NO5, true), //$NON-NLS-1$
						new RenamingTemplate(Messages.BulkRenameDialog_cue_year_seq,
								Constants.TV_CUE + "_" + Constants.TV_YYYY + "-" + Constants.TV_IMAGE_NO5, true), //$NON-NLS-1$ //$NON-NLS-2$
						new RenamingTemplate(Messages.BulkRenameDialog_filename_seq,
								Constants.TV_FILENAME + "-" + Constants.TV_IMAGE_NO5, true), //$NON-NLS-1$
						new RenamingTemplate(Messages.BulkRenameDialog_orig_filename, Constants.TV_FILENAME, true) },
				Constants.TV_RENAME);
		renameGroup.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				validate();
			}
		});
		return area;
	}

	protected void validate() {
		String errorMsg = renameGroup.validate();
		getButton(OK).setEnabled(errorMsg == null);
		setErrorMessage(errorMsg);
	}

	@Override
	protected void okPressed() {
		renameGroup.saveSettings(settings);
		RenamingTemplate selectedTemplate = renameGroup.getSelectedTemplate();
		if (selectedTemplate != null) {
			content = selectedTemplate.getContent();
			cue = renameGroup.getCue();
			start = renameGroup.getStart();
			field = renameGroup.getField();
		}
		super.okPressed();
	}

	public String getResult() {
		return content;
	}

	/**
	 * @return cue
	 */
	public String getCue() {
		return cue;
	}

	public QueryField getField() {
		return field;
	}

	public int getStart() {
		return start;
	}

}
