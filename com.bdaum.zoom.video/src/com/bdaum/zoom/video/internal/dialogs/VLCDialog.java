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
package com.bdaum.zoom.video.internal.dialogs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.osgi.util.NLS;
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
import com.bdaum.zoom.video.internal.VideoActivator;

@SuppressWarnings("restriction")
public class VLCDialog extends ZTitleAreaDialog {

	private static final String SETTINGSID = "com.bdaum.zoom.vlcDialog"; //$NON-NLS-1$
	private File vlcLocation;
	private Image vlcImage;
	private FileEditor fileEditor;
	private String errorMessage;

	public VLCDialog(Shell parentShell, File vlcLocation, String errorMessage) {
		super(parentShell);
		this.vlcLocation = vlcLocation;
		this.errorMessage = errorMessage;
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
		getShell().setText(Constants.APPLICATION_NAME);
		setTitle(Messages.VLCDialog_no_vlc);
		setTitleImage(vlcImage = VideoActivator.getImageDescriptor("icons/banner/largeVLC.png").createImage()); //$NON-NLS-1$
		setMessage(NLS.bind(Messages.VLCDialog_assign_correct_version, Constants.APPNAME));
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		composite.setLayout(new GridLayout(1, false));
		fileEditor = new FileEditor(composite, SWT.OPEN, Messages.VLCDialog_executable, true, Constants.EXEEXTENSION,
				Constants.EXEFILTERNAMES, null, vlcLocation == null ? "" //$NON-NLS-1$
						: vlcLocation.getAbsolutePath(),
				false, false, getDialogSettings(UiActivator.getDefault(), SETTINGSID));
		fileEditor.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				updateButtons();
			}
		});
		CLink link = new CLink(composite, SWT.NONE);
		link.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		link.setText(Messages.VLCDialog_download);
		link.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				String vlcDownload = System.getProperty(Messages.VLCDialog_vlckey);
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(vlcDownload));
				} catch (PartInitException e1) {
					// do nothing
				} catch (MalformedURLException e1) {
					// should never happen
				}
			}
		});
		return area;
	}

	protected void updateButtons() {
		String s = fileEditor.getText();
		if (errorMessage == null) {
			if (s.isEmpty())
				errorMessage = Messages.VLCDialog_not_specified;
			else {
				vlcLocation = new File(s);
				if (!vlcLocation.exists())
					errorMessage = Messages.VLCDialog_does_not_exist;
				else if (Constants.WIN32 && Platform.getOSArch().indexOf("64") >= 0 //$NON-NLS-1$
						&& s.indexOf("(x86)") >= 0) //$NON-NLS-1$
					errorMessage = NLS.bind(Messages.VLCDialog_not_suitable, Constants.APPNAME);

			}
		}
		getShell().setModified(errorMessage == null);
		getButton(IDialogConstants.OK_ID).setEnabled(errorMessage == null);
		setErrorMessage(errorMessage);
		errorMessage = null;
	}

	public File getResult() {
		return (vlcLocation == null || !vlcLocation.exists()) ? null : vlcLocation;
	}

	@Override
	protected void cancelPressed() {
		vlcLocation = null;
		super.cancelPressed();
	}

	@Override
	public boolean close() {
		if (vlcImage != null)
			vlcImage.dispose();
		return super.close();
	}

}
