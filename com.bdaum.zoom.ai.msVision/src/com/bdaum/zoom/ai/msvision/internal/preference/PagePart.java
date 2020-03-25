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
package com.bdaum.zoom.ai.msvision.internal.preference;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.ai.internal.preference.AiPreferencePage;
import com.bdaum.zoom.ai.msvision.internal.MsVisionActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePagePart;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.CLink;
import com.bdaum.zoom.ui.widgets.NumericControl;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;

@SuppressWarnings("restriction")
public class PagePart extends AbstractPreferencePagePart implements Listener {

	private Text keyField;
	private NumericControl conceptField;
	private NumericControl confidenceField;
	private Timer timer = new Timer();
	private AiPreferencePage parentPage;
	private CheckboxButton adultButton;
	private CheckboxButton faceButton;
	private CheckboxButton celebrityButton;
	private CheckboxButton descriptionButton;
	private CheckboxButton translateCatButton;
	private CheckboxButton translateTagButton;
	private CheckboxButton translateDescriptionButton;
	private NumericControl aboveField;
	private CheckboxButton knownButton;
	private TimerTask task;

	@SuppressWarnings("unused")
	@Override
	public Control createPageContents(Composite parent, AbstractPreferencePage parentPage) {
		this.parentPage = (AiPreferencePage) parentPage;
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		((GridLayout) composite.getLayout()).verticalSpacing = 0;
		new Label(composite, SWT.NONE).setText("Microsoft Computer Vision API"); //$NON-NLS-1$
		new Label(composite, SWT.NONE);
		CGroup eGroup = UiUtilities.createGroup(composite, 2, Messages.PagePart_account_credentials);
		new Label(eGroup, SWT.NONE).setText(Messages.PagePart_key1_or_key2);
		keyField = new Text(eGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		keyField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		keyField.addListener(SWT.Modify, this);
		new Label(eGroup, SWT.NONE).setText(Messages.PagePart_status);
		statusField = new Label(eGroup, SWT.WRAP);
		statusField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		CLink link = new CLink(eGroup, SWT.NONE);
		link.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		link.setText(Messages.PagePart_visit_account);
		link.addListener(SWT.Selection, this);
		CGroup tGroup = UiUtilities.createGroup(composite, 2, Messages.PagePart_usage);
		new Label(tGroup, SWT.NONE).setText(Messages.PagePart_max_proposals);
		conceptField = new NumericControl(tGroup, SWT.BORDER);
		conceptField.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		conceptField.setMaximum(20);
		conceptField.setMinimum(1);
		new Label(tGroup, SWT.NONE).setText(Messages.PagePart_min_core);
		confidenceField = new NumericControl(tGroup, SWT.BORDER);
		confidenceField.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		confidenceField.setMaximum(99);
		confidenceField.setMinimum(0);
		new Label(tGroup, SWT.NONE).setText(Messages.PagePart_mark_above);
		aboveField = new NumericControl(tGroup, SWT.BORDER);
		aboveField.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		aboveField.setMaximum(100);
		aboveField.setMinimum(0);
		knownButton = WidgetFactory.createCheckButton(tGroup, Messages.PagePart_mark_known,
				new GridData(SWT.END, SWT.CENTER, false, false, 2, 1), Messages.PagePart_mark_known_tooltip);

		Composite buttonArea1 = new Composite(tGroup, SWT.NONE);
		buttonArea1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		buttonArea1.setLayout(new GridLayout(4, false));
		adultButton = WidgetFactory.createCheckButton(buttonArea1, Messages.PagePart_sfw, null,
				Messages.PagePart_sfw_tooltip);
		faceButton = WidgetFactory.createCheckButton(buttonArea1, Messages.PagePart_faces, null,
				Messages.PagePart_faces_tooltip);
		celebrityButton = WidgetFactory.createCheckButton(buttonArea1, Messages.PagePart_celebs, null,
				Messages.PagePart_celebs_tooltip);
		descriptionButton = WidgetFactory.createCheckButton(buttonArea1, Messages.PagePart_description, null,
				Messages.PagePart_description_tooltip);
		descriptionButton.addListener(SWT.Selection, this);
		Label sep = new Label(tGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		Composite buttonArea2 = new Composite(tGroup, SWT.NONE);
		buttonArea2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		buttonArea2.setLayout(new GridLayout(3, false));
		translateCatButton = WidgetFactory.createCheckButton(buttonArea2, Messages.PagePart_translate_cats, null);
		translateTagButton = WidgetFactory.createCheckButton(buttonArea2, Messages.PagePart_translate_tags, null);
		translateDescriptionButton = WidgetFactory.createCheckButton(buttonArea2,
				Messages.PagePart_translate_description, null);
		return composite;
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Selection:
			if (e.widget == descriptionButton)
				updateButtons();
			else {
				String url = System.getProperty("com.bdaum.zoom.msVision"); //$NON-NLS-1$
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
				checkCredentials(parentPage);
			break;
		}
	}

	protected void verifyAccountCredenials(int time) {
		disposeTimerTask();
		task = new TimerTask() {
			@Override
			public void run() {
				String msg;
				boolean error;
				MsVisionActivator activator = MsVisionActivator.getDefault();
				VisionServiceRestClient client = activator.getClient();
				if (client != null) {
					msg = Messages.PagePart_verified;
					activator.disposeClient();
					error = false;
				} else {
					msg = Messages.PagePart_login_failed;
					error = true;
				}
				if (!statusField.isDisposed())
					statusField.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (!statusField.isDisposed())
								showStatus(msg, error);
						}
					});
			}
		};
		timer.schedule(task, time);
	}

	private void disposeTimerTask() {
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	@Override
	public void fillValues() {
		keyField.removeListener(SWT.Modify, this);
		IPreferenceStore preferenceStore = getPreferenceStore();
		keyField.setText(preferenceStore.getString(PreferenceConstants.KEY));
		conceptField.setSelection(preferenceStore.getInt(PreferenceConstants.MAXCONCEPTS));
		confidenceField.setSelection(preferenceStore.getInt(PreferenceConstants.MINCONFIDENCE));
		aboveField.setSelection(preferenceStore.getInt(PreferenceConstants.MARKABOVE));
		knownButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.MARKKNOWNONLY));
		adultButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.ADULTCONTENTS));
		faceButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.FACES));
		celebrityButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.CELEBRITIES));
		descriptionButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.DESCRIPTION));
		translateDescriptionButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.TRANSLATE_DESCRIPTION));
		translateCatButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.TRANSLATE_CATEGORIES));
		translateTagButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.TRANSLATE_TAGS));
		MsVisionActivator activator = MsVisionActivator.getDefault();
		activator.setAccountCredentials(keyField.getText().trim());
		verifyAccountCredenials(100);
		keyField.addListener(SWT.Modify, this);
		updateButtons();
	}

	@Override
	public void setEnabled(boolean enabled) {
		boolean wasEnabled = enabled;
		super.setEnabled(enabled);
		keyField.setEnabled(enabled);
		conceptField.setEnabled(enabled);
		confidenceField.setEnabled(enabled);
		aboveField.setEnabled(enabled);
		knownButton.setEnabled(enabled);
		adultButton.setEnabled(enabled);
		faceButton.setEnabled(enabled);
		celebrityButton.setEnabled(enabled);
		descriptionButton.setEnabled(enabled);
		translateCatButton.setEnabled(enabled);
		translateDescriptionButton.setEnabled(enabled && descriptionButton.getSelection());
		translateTagButton.setEnabled(enabled);
		if (enabled && !wasEnabled)
			checkCredentials(parentPage);
	}

	public void updateButtons() {
		boolean canTranslate = enabled && parentPage.canTranslate();
		translateDescriptionButton.setEnabled(canTranslate && descriptionButton.getSelection());
		translateCatButton.setEnabled(canTranslate);
		translateTagButton.setEnabled(canTranslate);
	}

	@Override
	public void performOk() {
		disposeTimerTask();
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.KEY, keyField.getText());
		preferenceStore.setValue(PreferenceConstants.MAXCONCEPTS, conceptField.getSelection());
		preferenceStore.setValue(PreferenceConstants.MINCONFIDENCE, confidenceField.getSelection());
		preferenceStore.setValue(PreferenceConstants.MARKABOVE, aboveField.getSelection());
		preferenceStore.setValue(PreferenceConstants.MARKKNOWNONLY, knownButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.ADULTCONTENTS, adultButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.FACES, faceButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.CELEBRITIES, celebrityButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.DESCRIPTION, descriptionButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.TRANSLATE_CATEGORIES,
				translateCatButton.isEnabled() && translateCatButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.TRANSLATE_DESCRIPTION,
				translateDescriptionButton.isEnabled() && translateDescriptionButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.TRANSLATE_TAGS,
				translateTagButton.isEnabled() && translateTagButton.getSelection());
	}

	@Override
	public void performDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.MAXCONCEPTS,
				preferenceStore.getDefaultInt(PreferenceConstants.MAXCONCEPTS));
		preferenceStore.setValue(PreferenceConstants.MINCONFIDENCE,
				preferenceStore.getDefaultInt(PreferenceConstants.MINCONFIDENCE));
		preferenceStore.setValue(PreferenceConstants.MARKABOVE,
				preferenceStore.getDefaultInt(PreferenceConstants.MARKABOVE));
		preferenceStore.setValue(PreferenceConstants.MARKKNOWNONLY,
				preferenceStore.getDefaultBoolean(PreferenceConstants.MARKKNOWNONLY));
		preferenceStore.setValue(PreferenceConstants.ADULTCONTENTS,
				preferenceStore.getDefaultBoolean(PreferenceConstants.ADULTCONTENTS));
		preferenceStore.setValue(PreferenceConstants.FACES,
				preferenceStore.getDefaultBoolean(PreferenceConstants.FACES));
		preferenceStore.setValue(PreferenceConstants.CELEBRITIES,
				preferenceStore.getDefaultBoolean(PreferenceConstants.CELEBRITIES));
		preferenceStore.setValue(PreferenceConstants.DESCRIPTION,
				preferenceStore.getDefaultBoolean(PreferenceConstants.DESCRIPTION));
		preferenceStore.setValue(PreferenceConstants.TRANSLATE_CATEGORIES,
				preferenceStore.getDefaultBoolean(PreferenceConstants.TRANSLATE_CATEGORIES));
		preferenceStore.setValue(PreferenceConstants.TRANSLATE_DESCRIPTION,
				preferenceStore.getDefaultBoolean(PreferenceConstants.TRANSLATE_DESCRIPTION));
		preferenceStore.setValue(PreferenceConstants.TRANSLATE_TAGS,
				preferenceStore.getDefaultBoolean(PreferenceConstants.TRANSLATE_TAGS));
		fillValues();
	}

	private static IPreferenceStore getPreferenceStore() {
		return MsVisionActivator.getDefault().getPreferenceStore();
	}

	@Override
	public String getLabel() {
		return "&MS Vision"; //$NON-NLS-1$
	}

	@Override
	public void performCancel() {
		disposeTimerTask();
	}

	protected void checkCredentials(AbstractPreferencePage parentPage) {
		parentPage.validate();
		if (validate() == null) {
			String key = keyField.getText().trim();
			if (key.isEmpty()) {
				showStatus(Messages.PagePart_no_app_key, true);
				return;
			}
			showStatus("", false); //$NON-NLS-1$
			MsVisionActivator.getDefault().setAccountCredentials(key);
			verifyAccountCredenials(500);
		}
	}

}
