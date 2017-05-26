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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.widgets;

import java.io.File;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.ui.internal.Icons;

public class FileEditor extends Composite {

	private Text fileNameField;
	ListenerList<ModifyListener> listeners = new ListenerList<ModifyListener>();
	private String path;
	private Button clearButton;
	private boolean browsed;

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
	 */
	public FileEditor(Composite parent, final int style, final String lab,
			boolean hasLabel, final String[] filterExtensions,
			final String[] filterNames, final String path,
			final String fileName, final boolean overwrite) {
		this(parent, style, lab, hasLabel, filterExtensions, filterNames, path,
				fileName, overwrite, false);
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
	 * @param fileName
	 *            - file name (not used for folders)
	 * @param overwrite
	 *            - check for overwrite (not used for folders)
	 * @param clear
	 *            - has clear button
	 */
	public FileEditor(Composite parent, final int style, final String lab,
			boolean hasLabel, final String[] filterExtensions,
			final String[] filterNames, final String path,
			final String fileName, final boolean overwrite, final boolean clear) {
		super(parent, style);
		this.path = path;
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
		fileNameField = new Text(this, SWT.BORDER | (style & SWT.READ_ONLY));
		final GridData gd_fileName = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		gd_fileName.widthHint = 200;
		fileNameField.setLayoutData(gd_fileName);
		if (fileName != null)
			fileNameField.setText(fileName);
		if ((style & SWT.READ_ONLY) == 0) {
			fileNameField.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					fireModifyEvent(e);
				}
			});
		}
		if (overwrite && path != null && (style & (SWT.OPEN | SWT.SAVE)) != 0) {
			fileNameField.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					// do nothing
				}
				public void focusGained(FocusEvent e) {
					if (!browsed) {
						File file = new File(path);
						if (file.exists())
							openBrowseDialog(style, lab, filterExtensions, filterNames,
									path, fileName, overwrite);
					}
				}
			});
		}
		Button button = new Button(this, SWT.PUSH);
		button.setText(Messages.FileEditor_browse);
		button.setToolTipText(Messages.FileEditor_browse_tooltip);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openBrowseDialog(style, lab, filterExtensions, filterNames,
						path, fileName, overwrite);
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
					if (text.length() > 0)
						fireModifyEvent(createModifyEvent());
				}
			});
			addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					clearButton.setEnabled(getText().length() > 0);
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
		for (Object o : listeners.getListeners())
			((ModifyListener) o).modifyText(e);
	}

	public String getText() {
		return fileNameField.getText();
	}

	public void setText(String string) {
		fileNameField.setText(string);
		if (clearButton != null)
			clearButton.setEnabled(getText().length() > 0);
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

	public void openBrowseDialog(final int style, final String lab,
			final String[] filterExtensions, final String[] filterNames,
			final String path, final String fileName, final boolean overwrite) {
		browsed = true;
		if ((style & (SWT.OPEN | SWT.SAVE)) != 0) {
			FileDialog dialog = new FileDialog(getShell(), style
					& (SWT.OPEN | SWT.SAVE));
			dialog.setText(lab);
			dialog.setFilterExtensions(filterExtensions);
			dialog.setFilterNames(filterNames);
			dialog.setFilterPath(path);
			String fn = fileNameField.getText().trim();
			if (fn.length() > 0) {
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
				fileNameField.setText(file);
				FileEditor.this.path = dialog.getFilterPath();
				ModifyEvent e1 = createModifyEvent();
				for (Object o : listeners.getListeners())
					((ModifyListener) o).modifyText(e1);
			}
		} else {
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			dialog.setText(lab);
			dialog.setFilterPath(path);
			String fn = fileNameField.getText().trim();
			if (fn.length() > 0)
				dialog.setFilterPath(fn);
			else if (path != null)
				dialog.setFilterPath(path);
			String folder = dialog.open();
			if (folder != null) {
				if (!folder.endsWith(File.separator))
					folder += File.separator;
				fileNameField.setText(folder);
				FileEditor.this.path = dialog.getFilterPath();
				ModifyEvent e1 = createModifyEvent();
				for (Object o : listeners.getListeners())
					((ModifyListener) o).modifyText(e1);
			}
		}
	}

	@Override
	public boolean setFocus() {
		return fileNameField.setFocus();
	}

}
