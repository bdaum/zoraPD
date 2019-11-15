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
import java.text.ParseException;
import java.util.Date;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.core.Format;

public class WatermarkGroup {
	public static final String COPYRIGHT = "copyright"; //$NON-NLS-1$
	public static final String WATERMARK = "watermark"; //$NON-NLS-1$

	private CheckboxButton createWatermarkButton;
	private Text copyrightField;
	private Button fileButton;
	private ListenerList<Listener> listeners = new ListenerList<>();

	public WatermarkGroup(Composite parent) {
		int columns = -1;
		Layout layout = parent.getLayout();
		if (layout instanceof GridLayout)
			columns = ((GridLayout) layout).numColumns;
		Composite composite = new Composite(parent, SWT.NONE);
		if (columns >= 0)
			composite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, columns, 1));
		GridLayout gridlayout = new GridLayout(4, false);
		gridlayout.marginHeight = 0;
		gridlayout.marginWidth = 0;
		composite.setLayout(gridlayout);
		createWatermarkButton = WidgetFactory.createCheckButton(composite, Messages.WatermarkGroup_create_watermark,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		createWatermarkButton.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateButtons();
				if (copyrightField.getEnabled())
					copyrightField.setFocus();
				fireEvent(event);
			}
		});
		copyrightField = new Text(composite, SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.widthHint = 150;
		copyrightField.setLayoutData(data);
		createWatermarkButton.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				fireEvent(event);
			}
		});
		fileButton = new Button(composite, SWT.PUSH);
		fileButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		fileButton.setText(Messages.WatermarkGroup_select_file);
		fileButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				FileDialog dialog = new FileDialog(composite.getShell());
				dialog.setFilterExtensions(new String[] { "*.bmp;*.png" }); //$NON-NLS-1$
				dialog.setFilterNames(new String[] { Messages.WatermarkGroup_watermark_files });
				String path = dialog.open();
				if (path != null) {
					copyrightField.setText(path);
					fireEvent(e);
				}
			}
		});
		updateButtons();
	}

	protected void fireEvent(Event event) {
		for (Listener listener : listeners)
			listener.handleEvent(event);
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	private static boolean testYear(String s) {
		try {
			Format.YEAR_FORMAT.get().parse(s.trim());
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	public void fillValues(IDialogSettings settings) {
		if (settings != null) {
			String copyright = settings.get(COPYRIGHT);
			if (copyright == null || copyright.trim().isEmpty()
					|| copyright.trim().length() == 4 && testYear(copyright))
				copyrightField.setText(Format.YEAR_FORMAT.get().format(new Date()) + " "); //$NON-NLS-1$ 
			createWatermarkButton.setSelection(settings.getBoolean(WATERMARK));
			if (copyright != null)
				copyrightField.setText(copyright);
		}
		updateButtons();
	}

	private void updateButtons() {
		boolean enabled = createWatermarkButton.getSelection() && createWatermarkButton.getEnabled();
		copyrightField.setEnabled(enabled);
		fileButton.setEnabled(enabled);
	}

	public boolean getCreateWatermark() {
		return createWatermarkButton.getSelection();
	}

	public String getCopyright() {
		return copyrightField.getText();
	}

	public void setEnabled(boolean enabled) {
		createWatermarkButton.setEnabled(enabled);
		updateButtons();
	}

	public void saveSettings(IDialogSettings settings) {
		settings.put(WATERMARK, getCreateWatermark());
		settings.put(COPYRIGHT, getCopyright());
	}

	public String validate() {
		if (copyrightField.isEnabled()) {
			String text = copyrightField.getText();
			if (text.endsWith(".png") || text.endsWith(".bmp")) { //$NON-NLS-1$ //$NON-NLS-2$
				try {
					if (new File(text).exists())
						return null;
				} catch (Exception e) {
					// no file
				}
				return Messages.WatermarkGroup_does_not_exist;
			}
		}
		return null;
	}
}
