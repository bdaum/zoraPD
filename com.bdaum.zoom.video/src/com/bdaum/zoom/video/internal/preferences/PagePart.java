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
 * (c) 2014 Berthold Daum  
 */
package com.bdaum.zoom.video.internal.preferences;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePagePart;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.CLink;
import com.bdaum.zoom.video.internal.VideoActivator;

@SuppressWarnings("restriction")
public class PagePart extends AbstractPreferencePagePart {

	private static final String SETTINGSID = "com.bdaum.zoom.videoPreferencePagePart"; //$NON-NLS-1$
	private FileEditor fileEditor;

	@SuppressWarnings("unused")
	public Control createPageContents(Composite parent, AbstractPreferencePage parentPage) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout());
		new Label(composite, SWT.WRAP)
				.setText(NLS.bind(Messages.VideoPreferencePage_vlc_description, Constants.APPNAME));
		new Label(composite, SWT.NONE);
		CGroup eGroup = new CGroup(composite, SWT.NONE);
		eGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		eGroup.setLayout(new GridLayout());
		eGroup.setText("VLC"); //$NON-NLS-1$
		fileEditor = createFileEditor(eGroup, Messages.VideoPreferencePage_ex_location, ""); //$NON-NLS-1$
		CLink link = new CLink(eGroup, SWT.NONE);
		link.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		link.setText(Messages.VideoPreferencePage_download);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String vlcDownload = System.getProperty(Messages.VideoPreferencePage_vlc_key);
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(vlcDownload));
				} catch (PartInitException e1) {
					// do nothing
				} catch (MalformedURLException e1) {
					// should never happen
				}
			}
		});
		return composite;
	}

	private static FileEditor createFileEditor(Composite parent, String label, String id) {
		FileEditor fileEditor = new FileEditor(parent, SWT.OPEN | SWT.READ_ONLY, label, true, Constants.EXEEXTENSION,
				Constants.EXEFILTERNAMES, null, null, false, true, UiActivator.getDefault().getDialogSettings(SETTINGSID));
		fileEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		return fileEditor;
	}

	@Override
	public String getLabel() {
		return Messages.PagePart_video;
	}

	@Override
	public String getTooltip() {
		return Messages.PagePart_video_tooltip;
	}

	@Override
	public void fillValues() {
		fileEditor.setText(getPreferenceStore().getString(PreferenceConstants.VLCLOCATION));
	}

	@Override
	public void performOk() {
		getPreferenceStore().setValue(PreferenceConstants.VLCLOCATION, fileEditor.getText());
	}

	@Override
	public void performDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.VLCLOCATION,
				preferenceStore.getDefaultString(PreferenceConstants.VLCLOCATION));
	}

	private static IPreferenceStore getPreferenceStore() {
		return VideoActivator.getDefault().getPreferenceStore();
	}

	@Override
	public String validate() {
		String fn = fileEditor.getText();
		if (!fn.isEmpty() && !new File(fn).exists())
			return NLS.bind(Messages.VideoPreferencePage_no_executable, fn);
		return null;
	}

	@Override
	public void performCancel() {
		// do nothing
	}

}
