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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ai.internal.preference;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.ai.internal.AiActivator;
import com.bdaum.zoom.ai.internal.HelpContextIds;
import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.CLink;

@SuppressWarnings("restriction")
public class AiPreferencePage extends AbstractPreferencePage implements Listener {

	public static final String ID = "com.bdaum.zoom.ai.aiPrefPage"; //$NON-NLS-1$
	private CheckboxButton enableButton;
	private Text keyField;
	private Label statusField;
	private Timer timer = new Timer();
	private ComboViewer languageViewer;
	private boolean enabled;
	private TimerTask task;

	public AiPreferencePage() {
		setDescription(Messages.AiPreferencePage_configure);
	}

	@Override
	public void init(IWorkbench aWorkbench) {
		this.workbench = aWorkbench;
		setPreferenceStore(AiActivator.getDefault().getPreferenceStore());
	}

	@SuppressWarnings("unused")
	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.PREFERENCE_PAGE);
		enableButton = WidgetFactory.createCheckButton(composite, Messages.AiPreferencePage_enable, null);
		enableButton.addListener(SWT.Selection, this);
		// Tab folder
		createTabFolder(composite, "Services"); //$NON-NLS-1$
		createExtensions(tabFolder, "com.bdaum.zoom.ai.aiPrefPage"); //$NON-NLS-1$
		String label = getPreferenceStore().getString(PreferenceConstants.ACTIVEPROVIDER);
		boolean tabinit = false;
		if (label != null) {
			int i = 0;
			for (CTabItem item : tabFolder.getItems()) {
				if (label.equals(item.getText().trim())) {
					tabFolder.setSelection(i);
					tabinit = true;
					break;
				}
				++i;
			}
		}
		new Label(composite, SWT.NONE);
		// Translator
		CGroup eGroup = UiUtilities.createGroup(composite, 3, Messages.AiPreferencePage_0);
		new Label(eGroup, SWT.NONE).setText(Messages.AiPreferencePage_key1_or_key2);
		keyField = new Text(eGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		keyField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		keyField.addListener(SWT.Modify, this);
		new Label(eGroup, SWT.NONE).setText(Messages.AiPreferencePage_status);
		statusField = new Label(eGroup, SWT.WRAP);
		statusField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		new Label(eGroup, SWT.NONE).setText(Messages.AiPreferencePage_translate_to);
		languageViewer = new ComboViewer(eGroup, SWT.READ_ONLY);
		GridData data = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		data.widthHint = 120;
		languageViewer.getCombo().setLayoutData(data);
		languageViewer.setContentProvider(ArrayContentProvider.getInstance());
		languageViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Locale)
					return ((Locale) element).getDisplayLanguage();
				return super.getText(element);
			}
		});
		languageViewer.getCombo().setVisibleItemCount(20);

		CLink link = new CLink(eGroup, SWT.NONE);
		link.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		link.setText(Messages.AiPreferencePage_visit_account);
		link.addListener(SWT.Selection, this);
		if (!tabinit)
			initTabFolder(0);
		fillValues();
		setEnabled(enableButton.getSelection());
		updateButtons();
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Selection:
			if (e.widget == enableButton) {
				setEnabled(enableButton.getSelection());
				updateFields();
			} else {
				String url = System.getProperty("com.bdaum.zoom.msTranslation"); //$NON-NLS-1$
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
				} catch (PartInitException e1) {
					// do nothing
				} catch (MalformedURLException e1) {
					// should never happen
				}
			}
			break;
		case SWT.Modify:
			if (enabled)
				checkCredentials();
			break;
		}
	}

	protected void updateFields() {
		keyField.setEnabled(enabled);
		languageViewer.getCombo().setEnabled(enabled && languageViewer.getCombo().getItemCount() > 1);
	}

	@Override
	protected void setEnabled(boolean b) {
		if (b && !enabled)
			checkCredentials();
		super.setEnabled(b);
		enabled = b;
	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		enableButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.ENABLE));
		keyField.setText(preferenceStore.getString(PreferenceConstants.TRANSLATORKEY));
	}

	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.ENABLE,
				preferenceStore.getDefaultBoolean(PreferenceConstants.ENABLE));
	}

	@Override
	protected void doPerformOk() {
		disposeTimerTask();
		IPreferenceStore preferenceStore = getPreferenceStore();
		boolean enabled = enableButton.getSelection();
		preferenceStore.setValue(PreferenceConstants.ENABLE, enabled);
		if (enabled) {
			preferenceStore.setValue(PreferenceConstants.TRANSLATORKEY, keyField.getText());
			String selectedLanguage = getSelectedLanguage();
			if (selectedLanguage != null)
				preferenceStore.setValue(PreferenceConstants.LANGUAGE, selectedLanguage);
		}
		CTabItem tabItem = tabFolder.getSelection();
		if (tabItem != null)
			preferenceStore.setValue(PreferenceConstants.ACTIVEPROVIDER, tabItem.getText().trim());
	}

	private String getSelectedLanguage() {
		Object element = languageViewer.getStructuredSelection().getFirstElement();
		return element == null ? null : ((Locale) element).getLanguage();
	}

	@Override
	protected void doPerformCancel() {
		disposeTimerTask();
	}

	private void checkCredentials() {
		String key = keyField.getText().trim();
		if (key.isEmpty()) {
			showStatus(Messages.AiPreferencePage_no_app_key, true);
			updateButtons();
		} else {
			showStatus("", false); //$NON-NLS-1$
			AiActivator.getDefault().getClient().setKey(key);
			verifyAccountCredentials(500);
		}
	}

	protected void verifyAccountCredentials(int time) {
		disposeTimerTask();
		task = new TimerTask() {
			@Override
			public void run() {
				boolean error;
				String msg;
				AiActivator activator = AiActivator.getDefault();
				String accessToken = activator.getClient().getAccessToken();
				Locale[] languages = activator.getClient().getLanguages();
				if (accessToken != null) {
					msg = Messages.AiPreferencePage_verified;
					activator.disposeClient();
					error = false;
				} else {
					msg = Messages.AiPreferencePage_login_faild;
					error = true;
				}
				if (!statusField.isDisposed())
					statusField.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (!statusField.isDisposed()) {
								showStatus(msg, error);
								String selectedLanguage = getSelectedLanguage();
								languageViewer.setInput(languages);
								if (selectedLanguage == null)
									selectedLanguage = AiActivator.getDefault().getPreferenceStore()
											.getString(PreferenceConstants.LANGUAGE);
								if (selectedLanguage != null)
									languageViewer.setSelection(
											new StructuredSelection(Locale.forLanguageTag(selectedLanguage)));
								else
									languageViewer.setSelection(new StructuredSelection(Locale.ENGLISH));
								updateFields();
								updateButtons();
							}
						}

					});
			}
		};
		timer.schedule(task, time);
	}

	private void showStatus(String msg, boolean error) {
		statusField.setText(msg);
		statusField.setData(CSSProperties.ID, error ? CSSProperties.ERRORS : null);
		CssActivator.getDefault().setColors(statusField);
	}

	private void disposeTimerTask() {
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean canTranslate() {
		return enabled && statusField.getData(CSSProperties.ID) == null;
	}

}
