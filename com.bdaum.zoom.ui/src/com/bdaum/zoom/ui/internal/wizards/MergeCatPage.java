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

package com.bdaum.zoom.ui.internal.wizards;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.db.IValidator;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.program.IRawConverter;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class MergeCatPage extends ColoredWizardPage implements IAdaptable {

	protected IDbManager externalDb = CoreActivator.NULLDBMANAGER;
	private final String filename;
	protected String exception;

	public MergeCatPage(String filename) {
		super("main", Messages.MergeCatPage_merge_catalogs, null); //$NON-NLS-1$
		this.filename = filename;
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		new Label(composite, SWT.NONE);
		createHeaderGroup(composite);
		createOptionsGroup(composite);
		setControl(composite);
		setHelp(HelpContextIds.MERGECAT);
		setTitle(Messages.MergeCatPage_title);
		setMessage(Messages.MergeCatPage_merge_the_selected_cat);
		if (filename != null)
			fileEditor.setText(filename);
		super.createControl(parent);
	}

	@Override
	protected void validatePage() {
		String fn = fileEditor.getText();
		if (fn.isEmpty()) {
			setErrorMessage(Messages.MergeCatPage_please_select_cat);
			setPageComplete(false);
			return;
		}
		File file = new File(fn);
		if (!file.exists()) {
			setErrorMessage(Messages.MergeCatPage_specified_cat_does_not_exist);
			setPageComplete(false);
			return;
		}
		IDbManager dbManager = Core.getCore().getDbManager();
		if (dbManager == null) {
			setErrorMessage(Messages.MergeCatPage_current_cat_is_not_open);
			setPageComplete(false);
			return;
		}
		if (file.equals(dbManager.getFile())) {
			setErrorMessage(Messages.MergeCatPage_you_cannot_merge_cat_into_itself);
			setPageComplete(false);
			return;
		}
		try {
			externalDb.close(CatalogListener.NORMAL);
			externalDb = CoreActivator.NULLDBMANAGER;
			IDbFactory dbFactory = Core.getCore().getDbFactory();
			final IDbErrorHandler errorHandler = dbFactory.getErrorHandler();
			try {
				exception = null;
				dbFactory.setErrorHandler(new IDbErrorHandler() {

					public void signalEOJ(String sound) {
						errorHandler.signalEOJ(sound);
					}

					public void showWarning(String title, String message, IAdaptable adaptable) {
						errorHandler.showWarning(title, message, adaptable);
					}

					public int showMessageDialog(String dialogTitle, Image dialogTitleImage, String dialogMessage,
							int dialogImageType, String[] dialogButtonLabels, int defaultIndex, IAdaptable adaptable) {
						return errorHandler.showMessageDialog(dialogTitle, dialogTitleImage, dialogMessage,
								dialogImageType, dialogButtonLabels, defaultIndex, adaptable);
					}

					public void showInformation(String title, String message, IAdaptable adaptable) {
						errorHandler.showInformation(title, message, adaptable);
					}

					public void showInformation(String title, String message, IAdaptable adaptable,
							IValidator validator) {
						errorHandler.showInformation(title, message, adaptable, validator);
					}

					public void showError(String title, String message, IAdaptable adaptable) {
						errorHandler.showError(title, message, adaptable);
					}

					public boolean question(String title, String message, IAdaptable adaptable) {
						return errorHandler.question(title, message, adaptable);
					}

					public void promptForReconnect(String title, String message, IInputValidator validator,
							IAdaptable adaptable) {
						errorHandler.promptForReconnect(title, message, validator, adaptable);
					}

					public void fatalError(String title, String message, IAdaptable adaptable) {
						exception = message;
					}

					public void invalidFile(String title, String message, URI uri, IProgressMonitor monitor,
							IAdaptable adaptable) {
						errorHandler.invalidFile(title, message, uri, monitor, adaptable);
					}

					public void connectionLostWarning(String title, String message, IAdaptable adaptable) {
						exception = message;
					}

					public void alarmOnPrompt(String sound) {
						errorHandler.alarmOnPrompt(sound);
					}

					public ImportConfiguration showConflictDialog(String title, String message, Asset asset,
							ImportConfiguration currentConfig, boolean multi, IAdaptable adaptable) {
						return errorHandler.showConflictDialog(title, message, asset, currentConfig, multi, adaptable);
					}

					public File showDngDialog(File dngLocation, IAdaptable adaptable) {
						return errorHandler.showDngDialog(dngLocation, adaptable);
					}

					public IRawConverter showRawDialog(IAdaptable adaptable) {
						return errorHandler.showRawDialog(adaptable);
					}

				});
				externalDb = dbFactory.createDbManager(fn, false, true, false);
			} finally {
				dbFactory.setErrorHandler(errorHandler);
			}
			if (exception != null)
				throw new IOException(exception);
		} catch (Exception e1) {
			setErrorMessage(Messages.MergeCatPage_cat_cannot_be_opened + e1);
			setPageComplete(false);
			return;
		}
		if (externalDb.getMeta(true).getVersion() < dbManager.getMeta(true).getVersion()) {
			setErrorMessage(NLS.bind(Messages.MergeCatPage_outdated_version, fn, Constants.APPLICATION_NAME));
			setPageComplete(false);
			return;
		}
		setErrorMessage(null);
		setPageComplete(true);
	}

	private void createHeaderGroup(Composite comp) {
		final Composite header = new Composite(comp, SWT.NONE);
		header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		header.setLayout(new GridLayout());
		UiActivator activator = UiActivator.getDefault();
		fileEditor = new FileEditor(header, SWT.OPEN | SWT.READ_ONLY, Messages.MergeCatPage_file_name, true,
				activator.getCatFileExtensions(), activator.getSupportedCatFileNames(), null,
				'*' + Constants.CATALOGEXTENSION, true, getWizard().getDialogSettings());
		fileEditor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
	}

	private SelectionListener selectionListener = new SelectionAdapter() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			getWizard().getContainer().updateButtons();
		}
	};
	private FileEditor fileEditor;
	private RadioButtonGroup policyButtonGroup;

	private void createOptionsGroup(Composite comp) {
		final CGroup header = new CGroup(comp, SWT.NONE);
		header.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		header.setLayout(new GridLayout());
		header.setText(Messages.MergeCatPage_duplicates);
		policyButtonGroup = new RadioButtonGroup(header, null, SWT.NONE, Messages.MergeCatPage_skip,
				Messages.MergeCatPage_replace, Messages.MergeCatPage_merge);
		policyButtonGroup.setSelection(2);
		policyButtonGroup.addSelectionListener(selectionListener);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter))
			return getContainer().getShell();
		return null;
	}

	public int getDuplicatePolicy() {
		int selection = policyButtonGroup.getSelection();
		if (selection == 0)
			return Constants.SKIP;
		if (selection == 1)
			return Constants.REPLACE;
		return Constants.MERGE;
	}

	public IDbManager getExternalDb() {
		return externalDb;
	}

	public Shell getShell(IAdaptable adaptable) {
		return null;
	}

}
