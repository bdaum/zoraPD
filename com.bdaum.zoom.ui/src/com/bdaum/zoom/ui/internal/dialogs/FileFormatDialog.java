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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.db.ITypeFilter;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.internal.widgets.ZDialog;

@SuppressWarnings("restriction")
public class FileFormatDialog extends ZDialog implements Listener {

	public static final int ABORT = -1;
	private int formats;
	private CheckboxButton rawField;
	private CheckboxButton othersField;
	private CheckboxButton mediaField;
	private CheckboxButton tifField;
	private CheckboxButton jpgField;
	private CheckboxButton dngField;
	private CheckboxButton allField;

	public FileFormatDialog(Shell parentShell, int formats) {
		super(parentShell);
		setShellStyle(SWT.NO_TRIM);
		this.formats = formats;
	}

	@Override
	public void create() {
		super.create();
		updateButtons();
		Shell shell = getShell();
		shell.layout();
		shell.pack();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		area.setLayout(new FillLayout());
		Composite comp = new Composite(area, SWT.BORDER);
		comp.setLayout(new GridLayout(2, false));
		allField = WidgetFactory.createCheckButton(comp, Messages.FileFormatDialog_all, null);
		allField.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				boolean sel = allField.getSelection();
				rawField.setSelection(sel);
				dngField.setSelection(sel);
				jpgField.setSelection(sel);
				tifField.setSelection(sel);
				othersField.setSelection(sel);
				mediaField.setSelection(sel);
				getButton(IDialogConstants.OK_ID).setEnabled(sel);
			}
		});
		rawField = WidgetFactory.createCheckButton(comp, "RAW", null); //$NON-NLS-1$
		rawField.addListener(this);
		dngField = WidgetFactory.createCheckButton(comp, "DNG", null); //$NON-NLS-1$
		dngField.addListener(this);
		jpgField = WidgetFactory.createCheckButton(comp, "JPEG", null); //$NON-NLS-1$
		jpgField.addListener(this);
		tifField = WidgetFactory.createCheckButton(comp, "TIFF", null); //$NON-NLS-1$
		tifField.addListener(this);
		othersField = WidgetFactory.createCheckButton(comp, Messages.FileFormatDialog_Other, null);
		othersField.addListener(this);
		rawField.setSelection((formats & ITypeFilter.RAW) != 0);
		dngField.setSelection((formats & ITypeFilter.DNG) != 0);
		jpgField.setSelection((formats & ITypeFilter.JPEG) != 0);
		tifField.setSelection((formats & ITypeFilter.TIFF) != 0);
		othersField.setSelection((formats & ITypeFilter.OTHER) != 0);
		if (!CoreActivator.getDefault().getMediaSupportMap().isEmpty()) {
			mediaField = WidgetFactory.createCheckButton(comp, Messages.FileFormatDialog_other_media, null);
			mediaField.addListener(this);
			mediaField.setSelection((formats & ITypeFilter.MEDIA) != 0);
		}
		return area;
	}

	@Override
	protected void okPressed() {
		formats = 0;
		if (rawField.getSelection())
			formats |= ITypeFilter.RAW;
		if (dngField.getSelection())
			formats |= ITypeFilter.DNG;
		if (jpgField.getSelection())
			formats |= ITypeFilter.JPEG;
		if (tifField.getSelection())
			formats |= ITypeFilter.TIFF;
		if (othersField.getSelection())
			formats |= ITypeFilter.OTHER;
		if (mediaField != null && mediaField.getSelection())
			formats |= ITypeFilter.MEDIA;
		super.okPressed();
	}

	@Override
	public int open() {
		int open = super.open();
		if (open == CANCEL)
			return ABORT;
		return formats;
	}

	public void handleEvent(Event e) {
		updateButtons();
	}

	private void updateButtons() {
		allField.setSelection(
				rawField.getSelection() && dngField.getSelection() && jpgField.getSelection() && tifField.getSelection()
						&& othersField.getSelection() && (mediaField == null || mediaField.getSelection()));
		getButton(IDialogConstants.OK_ID).setEnabled(
				rawField.getSelection() || dngField.getSelection() || jpgField.getSelection() || tifField.getSelection()
						|| othersField.getSelection() || (mediaField != null && mediaField.getSelection()));

	}

}
