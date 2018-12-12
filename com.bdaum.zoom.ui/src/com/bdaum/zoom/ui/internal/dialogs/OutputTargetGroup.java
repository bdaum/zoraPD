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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;

public class OutputTargetGroup {
	public static final String SUBFOLDER = "subfolder"; //$NON-NLS-1$
	public static final String SETTINGS = "settings"; //$NON-NLS-1$
	public static final String FTP = "ftp"; //$NON-NLS-1$
	public static final String FILE = "file"; //$NON-NLS-1$
	private static final String SELECTED_ACCOUNT = "selectedAccount"; //$NON-NLS-1$
	private static final String LOCAL_FOLDER = "localFolder"; //$NON-NLS-1$
	private static final String OUTPUT_TARGET = "outputTarget"; //$NON-NLS-1$
	private static final String LOCAL_FOLDERS = "localFolders"; //$NON-NLS-1$
	private static final String SUBFOLDERS = "subFolders"; //$NON-NLS-1$
	private static final String[] EMPTYITEMS = new String[0];
	private static final String OPTIONS = "options"; //$NON-NLS-1$
	private Combo folderField;
	private Button fileButton;
	private Button ftpButton;
	private ComboViewer ftpViewer;
	private List<FtpAccount> ftpAccounts;
	private Button editButton;
	private Button browseButton;
	private CGroup group;
	private ComboViewer subfolderViewer;
	private String subfolderoption;
	private CheckboxButton settingsOption;

	/**
	 * @param parent
	 *            - parent composite
	 * @param gridData
	 *            - layout data
	 * @param listener
	 *            - is informed about all changes except changes in the subfolder
	 *            option
	 * @param subfolders
	 *            - true if the subfolder option shall be shown
	 * @param ftp
	 *            - true if FTP targets are supported
	 * @param showSettingsOption
	 *            - true if target specific options can be selected
	 */
	@SuppressWarnings("unused")
	public OutputTargetGroup(final Composite parent, GridData gridData, final Listener listener,
			boolean subfolders, boolean ftp, boolean showSettingsOption) {
		ftpAccounts = FtpAccount.getAllAccounts();
		ftpAccounts.add(0, new FtpAccount());
		group = new CGroup(parent, SWT.NONE);
		group.setText(Messages.OutputTargetGroup_output_target);
		group.setLayoutData(gridData);
		group.setLayout(new GridLayout(ftp ? 4 : 3, false));
		if (ftp) {
			fileButton = new Button(group, SWT.RADIO);
			fileButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					updateFields();
					notifyListener(listener, event, FILE);
					browseButton.setEnabled(true);
				}
			});
		}
		final Label folderLabel = new Label(group, SWT.NONE);
		folderLabel.setText(Messages.OutputTargetGroup_local_folder);
		folderField = new Combo(group, SWT.BORDER | SWT.READ_ONLY);
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		data.widthHint = 300;
		folderField.setLayoutData(data);
		if (listener != null)
			folderField.addListener(SWT.Modify, listener);
		browseButton = new Button(group, SWT.PUSH);
		browseButton.setText(Messages.WebGalleryEditDialog_browse);
		browseButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell(), SWT.OPEN);
				dialog.setText(Messages.OutputTargetGroup_output_folder);
				dialog.setMessage(Messages.OutputTargetGroup_select_output_folder);
				String path = folderField.getText();
				if (!path.isEmpty())
					dialog.setFilterPath(path);
				String dir = dialog.open();
				if (dir != null) {
					folderField.setItems(UiUtilities.addToHistoryList(folderField.getItems(), dir));
					folderField.setText(dir);
					notifyListener(listener, event, FILE);
				}
			}
		});
		if (ftp) {
			ftpButton = new Button(group, SWT.RADIO);
			ftpButton.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					updateFields();
					notifyListener(listener, event, FTP);
					browseButton.setEnabled(false);
				}
			});
			final Label ftpLabel = new Label(group, SWT.NONE);
			ftpLabel.setText(Messages.OutputTargetGroup_ftp_directory);
			ftpViewer = new ComboViewer(group, SWT.BORDER | SWT.READ_ONLY);
			ftpViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			ftpViewer.setContentProvider(ArrayContentProvider.getInstance());
			ftpViewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof FtpAccount) {
						String name = ((FtpAccount) element).getName();
						return name == null ? Messages.OutputTargetGroup_create_new_account : name;
					}
					return super.getText(element);
				}
			});
			editButton = new Button(group, SWT.PUSH);
			editButton.setText(Messages.OutputTargetGroup_edit);
			editButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Object el = ftpViewer.getStructuredSelection().getFirstElement();
					if (el instanceof FtpAccount) {
						FtpAccount account = (FtpAccount) el;
						boolean createNew = account.getName() == null;
						EditFtpDialog dialog = new EditFtpDialog(editButton.getShell(), account, false, null);
						if (dialog.open() == Window.OK) {
							FtpAccount result = dialog.getResult();
							if (createNew) {
								ftpAccounts.add(0, new FtpAccount());
								ftpViewer.setInput(ftpAccounts);
								ftpViewer.setSelection(new StructuredSelection(result));
							} else
								ftpViewer.update(result, null);
							FtpAccount.saveAccounts(ftpAccounts);
						}
					}
				}
			});
			ftpViewer.setInput(ftpAccounts);
			if (ftpAccounts.size() == 1)
				ftpViewer.setSelection(new StructuredSelection(ftpAccounts.get(0)));
			ftpViewer.getCombo().addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					updateFields();
					notifyListener(listener, event, FTP);
				}
			});
		}
		if (subfolders) {
			Meta meta = Core.getCore().getDbManager().getMeta(true);
			String timelinemode = meta.getTimeline();
			final String[] subfolderoptions = Meta.timeline_no.equals(timelinemode)
					? new String[] { Constants.BY_NONE, Constants.BY_RATING, Constants.BY_CATEGORY, Constants.BY_STATE,
							Constants.BY_JOB, Constants.BY_EVENT, Constants.BY_DATE, Constants.BY_TIME }
					: new String[] { Constants.BY_NONE, Constants.BY_TIMELINE, Constants.BY_NUM_TIMELINE,
							Constants.BY_RATING, Constants.BY_CATEGORY, Constants.BY_STATE, Constants.BY_JOB,
							Constants.BY_EVENT, Constants.BY_DATE, Constants.BY_TIME };
			final String[] OPTIONLABELS = Meta.timeline_no.equals(timelinemode)
					? new String[] { Messages.OutputTargetGroup_none, Messages.OutputTargetGroup_rating,
							Messages.OutputTargetGroup_category, Messages.OutputTargetGroup_state,
							Messages.OutputTargetGroup_job_id, Messages.OutputTargetGroup_event,
							Messages.OutputTargetGroup_export_date, Messages.OutputTargetGroup_export_time }
					: new String[] { Messages.OutputTargetGroup_none, Messages.OutputTargetGroup_timeline,
							Messages.OutputTargetGroup_timeline_num, Messages.OutputTargetGroup_rating,
							Messages.OutputTargetGroup_category, Messages.OutputTargetGroup_state,
							Messages.OutputTargetGroup_job_id, Messages.OutputTargetGroup_event,
							Messages.OutputTargetGroup_export_date, Messages.OutputTargetGroup_export_time };
			new Label(group, SWT.NONE);
			new Label(group, SWT.NONE).setText(Messages.OutputTargetGroup_group_by);
			subfolderViewer = new ComboViewer(group);
			subfolderViewer.setContentProvider(ArrayContentProvider.getInstance());
			subfolderViewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					for (int i = 0; i < subfolderoptions.length; i++)
						if (subfolderoptions[i].equals(element))
							return OPTIONLABELS[i];
					return super.getText(element);
				}
			});
			subfolderViewer.getCombo().addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					subfolderoption = ((String) subfolderViewer.getStructuredSelection().getFirstElement());
					notifyListener(listener, event, SUBFOLDER);
				}
			});
			subfolderViewer.setInput(subfolderoptions);
		}
		if (showSettingsOption) {
			settingsOption = WidgetFactory.createCheckButton(group, Messages.OutputTargetGroup_target_specific, null,
					Messages.OutputTargetGroup_target_specific_tooltip);
			settingsOption.addListener(new Listener() {
				@Override
				public void handleEvent(Event event) {
					notifyListener(listener, event, SETTINGS);
				}
			});
		}
	}

	public IDialogSettings getTargetSection(IDialogSettings settings, boolean save) {
		if (getSettingsOption()) {
			String key;
			if (getTarget() == Constants.FTP) {
				FtpAccount ftpDir = getFtpDir();
				key = ftpDir == null ? null : ftpDir.getUrl();
			} else
				key = getLocalFolder();
			if (key != null) {
				IDialogSettings section = settings.getSection(key);
				if (section == null && save)
					section = settings.addNewSection(key);
				return section;
			}
		}
		return null;
	}

	protected void updateFields() {
		boolean ftp = ftpButton != null && ftpButton.getSelection();
		folderField.setEnabled(!ftp);
		browseButton.setEnabled(!ftp);
		if (ftpViewer != null) {
			ftpViewer.getControl().setEnabled(ftp);
			editButton.setEnabled(ftp && !ftpViewer.getSelection().isEmpty());
		}
		if (ftp)
			ftpViewer.getControl().setFocus();
		else
			folderField.setFocus();
	}

	public void setLocalFolder(String outputFolder) {
		if (outputFolder != null) {
			folderField.setItems(UiUtilities.addToHistoryList(folderField.getItems(), outputFolder));
			folderField.setText(outputFolder);
		}
	}

	public void setFtpDir(String ftpDir) {
		if (ftpDir != null && ftpViewer != null)
			for (FtpAccount acc : ftpAccounts)
				if (ftpDir.equals(acc.getName())) {
					ftpViewer.setSelection(new StructuredSelection(acc));
					break;
				}
	}

	public void setTarget(int target) {
		if (target == Constants.FTP) {
			if (ftpButton != null)
				ftpButton.setSelection(true);
			browseButton.setEnabled(false);
		} else {
			if (fileButton != null)
				fileButton.setSelection(true);
			browseButton.setEnabled(true);
		}
		updateFields();
	}

	public String validate() {
		if (ftpButton != null && ftpButton.getSelection()) {
			FtpAccount acc = (FtpAccount) ftpViewer.getStructuredSelection().getFirstElement();
			if (acc == null || acc.getName() == null)
				return Messages.OutputTargetGroup_select_ftp_dir;
		} else {
			String folder = folderField.getText();
			if (folder.isEmpty())
				return Messages.OutputTargetGroup_select_target_folder;
			if (!new File(folder).exists())
				return Messages.OutputTargetGroup_selected_folder_does_not_exist;
		}
		return null;
	}

	public String getLocalFolder() {
		return folderField.getText();
	}

	public int getTarget() {
		return (ftpButton != null && ftpButton.getSelection()) ? Constants.FTP : Constants.FILE;
	}

	public FtpAccount getFtpDir() {
		return ftpViewer == null ? null
				: (FtpAccount) ftpViewer.getStructuredSelection().getFirstElement();
	}

	public boolean getSettingsOption() {
		return settingsOption == null ? false : settingsOption.getSelection();
	}

	public void initValues(IDialogSettings settings) {
		try {
			setTarget(settings.getInt(OUTPUT_TARGET));
		} catch (NumberFormatException e) {
			setTarget(Constants.FILE);
		}
		String[] localFolders = settings.getArray(LOCAL_FOLDERS);
		if (localFolders != null) {
			List<String> validItems = new ArrayList<String>(localFolders.length);
			for (String folder : localFolders)
				if (new File(folder).exists())
					validItems.add(folder);
			folderField.setItems(validItems.toArray(new String[validItems.size()]));
			String s = settings.get(LOCAL_FOLDER);
			if (s != null)
				for (int i = 0; i < localFolders.length; i++)
					if (s.equals(localFolders[i])) {
						folderField.select(i);
						folderField.setText(s);
						break;
					}
		} else
			folderField.setItems(EMPTYITEMS);
		String ftp = settings.get(SELECTED_ACCOUNT);
		if (ftp != null && ftpViewer != null)
			for (FtpAccount acc : ftpAccounts)
				if (ftp.equals(acc.getName())) {
					ftpViewer.setSelection(new StructuredSelection(acc));
					break;
				}
		if (settingsOption != null)
			settingsOption.setSelection(settings.getBoolean(OPTIONS));
		updateSubfolderOption(settings);
		updateFields();
	}

	public void updateSubfolderOption(IDialogSettings settings) {
		if (subfolderViewer != null) {
			String option = settings.get(SUBFOLDERS);
			subfolderViewer.setSelection(new StructuredSelection(subfolderoption = (option != null ? option : Constants.BY_NONE)));
		}
	}

	public void saveValues(IDialogSettings settings) {
		settings.put(OUTPUT_TARGET, getTarget());
		String text = folderField.getText();
		settings.put(LOCAL_FOLDERS, folderField.getItems());
		settings.put(LOCAL_FOLDER, text);
		Object firstElement = ftpViewer.getStructuredSelection().getFirstElement();
		if (firstElement instanceof FtpAccount)
			settings.put(SELECTED_ACCOUNT, ((FtpAccount) firstElement).getName());
		if (subfolderoption != null)
			settings.put(SUBFOLDERS, subfolderoption);
		if (settingsOption != null)
			settings.put(OPTIONS, settingsOption.getSelection());
	}

	private static void notifyListener(final Listener listener, Event event, Object data) {
		if (listener != null) {
			event.data = data;
			listener.handleEvent(event);
		}
	}

	/**
	 * @return subfolderoption
	 */
	public String getSubfolderoption() {
		return subfolderoption;
	}

}
