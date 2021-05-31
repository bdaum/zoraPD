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
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbErrorHandler;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.widgets.CLink;

public class DNGConverterDialog extends ZTitleAreaDialog implements Listener {

	private static final int WITHOUTDNG = 9999;
	private static final String SETTINGSID = "com.bdaum.zoom.dngConverterDialog"; //$NON-NLS-1$
	private File dngLocation;
	private Image dngImage;
	private FileEditor fileEditor;
	private IDialogSettings dialogSettings;

	public DNGConverterDialog(Shell parentShell, File dngLocation) {
		super(parentShell);
		this.dngLocation = dngLocation;
		dialogSettings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
	}

	@Override
	public int open() {
		IDbErrorHandler errorHandler = Core.getCore().getErrorHandler();
		if (errorHandler != null)
			errorHandler.alarmOnPrompt("question"); //$NON-NLS-1$
		return super.open();
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.DNGConverterDialog_no_converter);
		setTitleImage(dngImage = UiActivator.getImageDescriptor("icons/banner/dng.png").createImage()); //$NON-NLS-1$
		setMessage(Messages.DNGConverterDialog_make_sure);
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		composite.setLayout(new GridLayout(1, false));
		fileEditor = new FileEditor(composite, SWT.OPEN, Messages.DNGConverterDialog_location, true,
				Constants.EXEEXTENSION, Constants.EXEFILTERNAMES, null,
				dngLocation == null ? "" : dngLocation.getAbsolutePath(), //$NON-NLS-1$
				false, false, dialogSettings);
		fileEditor.addListener(SWT.Modify, this);
		CLink link = new CLink(composite, SWT.NONE);
		link.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		link.setText(Messages.DNGConverterDialog_download);
		link.addListener(SWT.Selection, this);
		return area;
	}
	
	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Modify:
			updateButtons();
			return;
		case SWT.Selection:
			String vlcDownload = System.getProperty(Messages.DNGConverterDialog_dngkey);
			try {
				PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(vlcDownload));
			} catch (PartInitException e1) {
				// do nothing
			} catch (MalformedURLException e1) {
				// should never happen
			}
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, WITHOUTDNG, Messages.DNGConverterDialog_import_without_dng, false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == WITHOUTDNG) {
			dngLocation = new File(""); //$NON-NLS-1$
			okPressed();
		}
		super.buttonPressed(buttonId);
	}

	@Override
	protected void okPressed() {
		fileEditor.saveValues();
		super.okPressed();
	}

	protected void updateButtons() {
		boolean enabled = false;
		String s = fileEditor.getText();
		if (!s.isEmpty())
			dngLocation = new File(s);
		enabled = dngLocation.exists();
		setErrorMessage(enabled ? null : Messages.DNGConverterDialog_for_converting);
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	@Override
	protected void cancelPressed() {
		dngLocation = null;
		super.cancelPressed();
	}

	public File getResult() {
		if (dngLocation != null && !dngLocation.getName().isEmpty() && !dngLocation.exists())
			return null;
		return dngLocation;
	}

	@Override
	public boolean close() {
		if (dngImage != null)
			dngImage.dispose();
		return super.close();
	}

}
