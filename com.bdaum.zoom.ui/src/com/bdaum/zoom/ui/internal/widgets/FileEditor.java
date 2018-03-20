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

package com.bdaum.zoom.ui.internal.widgets;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiUtilities;

public class FileEditor extends Composite {

	private static final String FILEHISTORY = "fileHistory"; //$NON-NLS-1$
	private static final String LASTFILENAME = "lastFilename"; //$NON-NLS-1$
	private Combo fileNameField;
	private ListenerList<ModifyListener> listeners = new ListenerList<ModifyListener>();
	private String path;
	private Button clearButton;
	private IDialogSettings settings;
	private String historyKey;
	private String lastfileKey;
	private String fileName;
	private int lastSelectionIndex = 0;

	/**
	 * Constructor
	 *
	 * @param parent
	 *            - parent container
	 * @param style
	 *            - style bits, neither SWT.OPEN nor SWT.SAVE set: FolderDialog
	 * @param lab
	 *            - label
	 * @param hasLabel
	 *            - true if the input field has a label
	 * @param filterExtensions
	 *            - filter extensions (not used for folders)
	 * @param filterNames
	 *            - filter names (not used for folders)
	 * @param path
	 *            - path
	 * @param fileName
	 *            - file name (not used for folders)
	 * @param overwrite
	 *            - check for overwrite (not used for folders)
	 * @param settings
	 *            - dialogSettings for storing and retrieving the history
	 */
	public FileEditor(Composite parent, final int style, final String lab, boolean hasLabel,
			final String[] filterExtensions, final String[] filterNames, final String path, final String fileName,
			final boolean overwrite, IDialogSettings settings) {
		this(parent, style, lab, hasLabel, filterExtensions, filterNames, path, fileName, overwrite, false, settings);
	}

	/**
	 * Constructor
	 *
	 * @param parent
	 *            - parent container
	 * @param style
	 *            - style bits, neither SWT.OPEN nor SWT.SAVE set: FolderDialog
	 * @param lab
	 *            - label
	 * @param hasLabel
	 *            - true if the input field has a label
	 * @param filterExtensions
	 *            - filter extensions (not used for folders)
	 * @param filterNames
	 *            - filter names (not used for folders)
	 * @param path
	 *            - path
	 * @param filenameProposal
	 *            - file name (not used for folders)
	 * @param overwrite
	 *            - check for overwrite (not used for folders)
	 * @param clear
	 *            - has clear button
	 * @param settings
	 *            - dialogSettings for storing and retrieving the history
	 */
	public FileEditor(Composite parent, final int style, final String lab, boolean hasLabel,
			final String[] filterExtensions, final String[] filterNames, final String path,
			final String filenameProposal, final boolean overwrite, final boolean clear, IDialogSettings settings) {
		super(parent, style);
		this.path = path;
		this.fileName = filenameProposal;
		this.settings = settings;
		int hashCode = Arrays.hashCode(filterExtensions);
		historyKey = FILEHISTORY + hashCode;
		lastfileKey = LASTFILENAME + hashCode;
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		int cols = 2;
		if (hasLabel)
			++cols;
		if (clear)
			++cols;
		GridLayout layout = new GridLayout(cols, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		if (hasLabel)
			new Label(this, SWT.NONE).setText(lab);
		fileNameField = new Combo(this, SWT.BORDER | (style & SWT.READ_ONLY));
		final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = 200;
		fileNameField.setLayoutData(data);
		String[] items = settings == null ? null : settings.getArray(historyKey);
		if (items == null)
			items = new String[0];
		fileNameField.setVisibleItemCount(8);
		String lastFile = settings == null ? null : settings.get(lastfileKey);
		if (lastFile != null)
			this.path = new File(this.fileName = lastFile).getParent();
		if (this.fileName != null) {
			fileNameField.setItems(UiUtilities.addToHistoryList(items, this.fileName));
			fileNameField.setText(this.fileName);
		}
		if (overwrite && lastFile != null && !lastFile.isEmpty() && new File(lastFile).exists()) {
			fileNameField.setItems(UiUtilities.addToHistoryList(fileNameField.getItems(), filenameProposal));
			fileNameField.setText(this.fileName = filenameProposal);
			this.path = path;
		}
		fileNameField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selectionIndex = fileNameField.getSelectionIndex();
				if (!overwrite || !new File(fileNameField.getItem(selectionIndex)).exists() || MessageDialog
						.openQuestion(getShell(), Messages.FileEditor_file_exists, Messages.FileEditor_overwrite))
					lastSelectionIndex = selectionIndex;
				else
					fileNameField.select(lastSelectionIndex);
				fireModifyEvent(createModifyEvent());
			}
		});
		if ((style & SWT.READ_ONLY) == 0)
			fileNameField.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					fireModifyEvent(e);
				}
			});
		Button button = new Button(this, SWT.PUSH);
		button.setText(Messages.FileEditor_browse);
		button.setToolTipText(Messages.FileEditor_browse_tooltip);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openBrowseDialog(style, lab, filterExtensions, filterNames, FileEditor.this.path,
						FileEditor.this.fileName, overwrite);
			}
		});
		if (clear) {
			clearButton = new Button(this, SWT.PUSH);
			clearButton.setImage(Icons.delete.getImage());
			clearButton.setToolTipText(Messages.FileEditor_clear_tooltip);
			clearButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String text = getText();
					setText(""); //$NON-NLS-1$
					if (!text.isEmpty())
						fireModifyEvent(createModifyEvent());
				}
			});
			addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					clearButton.setEnabled(!getText().isEmpty());
				}
			});
		}
	}

	public void addModifyListener(ModifyListener listener) {
		listeners.add(listener);
	}

	public void removeModifyListener(ModifyListener listener) {
		listeners.remove(listener);
	}

	public void fireModifyEvent(ModifyEvent e) {
		for (ModifyListener o : listeners)
			o.modifyText(e);
	}

	public String getText() {
		return fileNameField.getText();
	}

	public void setText(String string) {
		int index = fileNameField.indexOf(string);
		if (index < 0) {
			String[] items = fileNameField.getItems();
			String[] newItems = new String[items.length + 1];
			newItems[0] = string;
			System.arraycopy(items, 0, newItems, 1, items.length);
			fileNameField.setItems(newItems);
		}
		fileNameField.setText(string);
		if (clearButton != null)
			clearButton.setEnabled(!getText().isEmpty());
	}

	public String getFilterPath() {
		return path;
	}

	private ModifyEvent createModifyEvent() {
		Event ev = new Event();
		ev.widget = this;
		ev.display = getDisplay();
		return new ModifyEvent(ev);
	}

	public void openBrowseDialog(final int style, final String lab, final String[] filterExtensions,
			final String[] filterNames, final String path, final String fileName, final boolean overwrite) {
		if ((style & (SWT.OPEN | SWT.SAVE)) != 0) {
			FileDialog dialog = new FileDialog(getShell(), style & (SWT.OPEN | SWT.SAVE));
			dialog.setText(lab);
			dialog.setFilterExtensions(filterExtensions);
			dialog.setFilterNames(filterNames);
			dialog.setFilterPath(path);
			String fn = fileNameField.getText().trim();
			if (!fn.isEmpty()) {
				int q = fn.lastIndexOf('\\');
				int p = Math.max(q, fn.lastIndexOf('/'));
				if (p > 0) {
					dialog.setFilterPath(fn.substring(0, p));
					dialog.setFileName(fn.substring(p + 1));
				} else
					dialog.setFileName(fn);
			} else if (fileName != null)
				dialog.setFileName(fileName);
			dialog.setFilterIndex(0);
			dialog.setOverwrite(overwrite);
			String file = dialog.open();
			if (file != null) {
				fileNameField.setItems(UiUtilities.addToHistoryList(fileNameField.getItems(), file));
				fileNameField.setText(file);
				this.path = dialog.getFilterPath();
				fireModifyEvent(createModifyEvent());
			}
		} else {
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			dialog.setText(lab);
			dialog.setFilterPath(path);
			String fn = fileNameField.getText().trim();
			if (!fn.isEmpty())
				dialog.setFilterPath(fn);
			else if (path != null)
				dialog.setFilterPath(path);
			String folder = dialog.open();
			if (folder != null) {
				if (!folder.endsWith(File.separator))
					folder += File.separator;
				fileNameField.setItems(UiUtilities.addToHistoryList(fileNameField.getItems(), folder));
				fileNameField.setText(folder);
				this.path = dialog.getFilterPath();
				fireModifyEvent(createModifyEvent());
			}
		}
	}

	@Override
	public boolean setFocus() {
		return fileNameField.setFocus();
	}

	public void saveValues() {
		if (settings != null) {
			String text = getText();
			if (!text.isEmpty()) {
				settings.put(lastfileKey, text);
				settings.put(historyKey, UiUtilities.getComboHistory(fileNameField, '*'));
			}
		}
	}

}
