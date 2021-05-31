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
package com.bdaum.zoom.ai.clarifai.internal.preference;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.ai.clarifai.internal.ClarifaiActivator;
import com.bdaum.zoom.ai.internal.preference.AiPreferencePage;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Theme;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.Password;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePagePart;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.CLink;
import com.bdaum.zoom.ui.widgets.NumericControl;

import clarifai2.api.ClarifaiClient;

@SuppressWarnings("restriction")
public class PagePart extends AbstractPreferencePagePart implements Listener {

	private Password apiKeyField;
	private NumericControl conceptField;
	private NumericControl confidenceField;
	private ComboViewer modelCombo;
	private Timer timer = new Timer();
	private AiPreferencePage parentPage;
	private CheckboxButton adultButton;
	private CheckboxButton translateButton;
	private NumericControl aboveField;
	private CheckboxButton knownButton;
	private CheckboxButton faceButton;
	private ComboViewer languageCombo;
	private CheckboxButton celebrityButton;
	private String currentLanguage = "en"; //$NON-NLS-1$
	private boolean updating;
	private TimerTask task;

	@SuppressWarnings("unused")
	@Override
	public Control createPageContents(Composite parent, AbstractPreferencePage parentPage) {
		this.parentPage = (AiPreferencePage) parentPage;
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		((GridLayout) composite.getLayout()).verticalSpacing = 0;
		new Label(composite, SWT.NONE).setText(Messages.PagePart_manage_clarifai_account);
		new Label(composite, SWT.NONE);
		CGroup eGroup = new CGroup(composite, SWT.NONE);
		eGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		eGroup.setLayout(new GridLayout(3, false));
		eGroup.setText(Messages.PagePart_credentials);
		new Label(eGroup, SWT.NONE).setText(Messages.PagePart_apikey);
		apiKeyField = new Password(eGroup, SWT.BORDER);
		apiKeyField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		apiKeyField.addListener(SWT.Modify, this);
		Label label = new Label(eGroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		label.setText(Messages.PagePart_access_token);
		statusField = new Label(eGroup, SWT.WRAP);
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		layoutData.heightHint = 30;
		statusField.setLayoutData(layoutData);
		CLink link = new CLink(eGroup, SWT.NONE);
		link.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
		link.setText(Messages.PagePart_visit_account_page);
		link.addListener(SWT.Selection, this);
		CGroup tGroup = CGroup.create(composite, 1, Messages.PagePart_limits);
		new Label(tGroup, SWT.NONE).setText(Messages.PagePart_model);
		modelCombo = new ComboViewer(tGroup, SWT.READ_ONLY);
		modelCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		modelCombo.setContentProvider(ArrayContentProvider.getInstance());
		modelCombo.setLabelProvider(new LabelProvider());
		modelCombo.setComparator(ZViewerComparator.INSTANCE);
		modelCombo.setInput(CoreActivator.getDefault().getThemes().values());
		ISelectionChangedListener listener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!updating)
					updateButtons();
			}
		};
		modelCombo.addSelectionChangedListener(listener);
		new Label(tGroup, SWT.NONE).setText(Messages.PagePart_language);
		languageCombo = new ComboViewer(tGroup, SWT.READ_ONLY);
		languageCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		languageCombo.setContentProvider(ArrayContentProvider.getInstance());
		languageCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					Locale loc = new Locale((String) element);
					return loc.getDisplayLanguage(loc);
				}
				return super.getText(element);
			}
		});
		languageCombo.addSelectionChangedListener(listener);
		languageCombo.setInput(ClarifaiActivator.LANGUAGES);
		new Label(tGroup, SWT.NONE).setText(Messages.PagePart_max_concepts);
		conceptField = new NumericControl(tGroup, SWT.BORDER);
		conceptField.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		conceptField.setMaximum(20);
		conceptField.setMinimum(1);
		new Label(tGroup, SWT.NONE).setText(Messages.PagePart_min_confidence);
		confidenceField = new NumericControl(tGroup, SWT.BORDER);
		confidenceField.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		confidenceField.setMaximum(99);
		confidenceField.setMinimum(0);
		Composite kwcomp = new Composite(tGroup, SWT.NONE);
		kwcomp.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		kwcomp.setLayout(layout);
		new Label(kwcomp, SWT.NONE).setText(Messages.PagePart_mark_above);
		knownButton = WidgetFactory.createCheckButton(kwcomp, Messages.PagePart_mark_known,
				new GridData(SWT.END, SWT.CENTER, false, false), Messages.PagePart_mark_known_tooltip);
		aboveField = new NumericControl(tGroup, SWT.BORDER);
		aboveField.setLayoutData(new GridData(SWT.END, SWT.CENTER, true, false));
		aboveField.setMaximum(100);
		aboveField.setMinimum(0);
		
		Composite buttonArea1 = new Composite(tGroup, SWT.NONE);
		buttonArea1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		buttonArea1.setLayout(new GridLayout(3, false));
		adultButton = WidgetFactory.createCheckButton(buttonArea1, Messages.PagePart_check_adult,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false), Messages.PagePart_porno);
		faceButton = WidgetFactory.createCheckButton(buttonArea1, Messages.PagePart_detect_faces, null,
				Messages.PagePart_detect_faces_tooltip);
		celebrityButton = WidgetFactory.createCheckButton(buttonArea1, Messages.PagePart_detect_celebrities, null,
				Messages.PagePart_detect_celebrities_tooltip);
		Label sep = new Label(tGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		translateButton = WidgetFactory.createCheckButton(tGroup, Messages.PagePart_translate,
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1), Messages.PagePart_translate_tooltip);
		return composite;
	}
	
	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Selection:
			String vlcDownload = System.getProperty("com.bdaum.zoom.clarifai"); //$NON-NLS-1$
			try {
				PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(vlcDownload));
			} catch (PartInitException e1) {
				// do nothing
			} catch (MalformedURLException e1) {
				// should never happen
			}
			return;
		case SWT.Modify:
			if (enabled)
				checkCredentials(parentPage);
		}
	}

	protected void verifyAccountCredenials(int time) {
		disposeResources();
		task = new TimerTask() {
			@Override
			public void run() {
				String msg;
				boolean error;
				ClarifaiClient client = ClarifaiActivator.getDefault().getClient();
				if (client != null) {
					error = false;
					msg = Messages.PagePart_verified;
				} else {
					msg = Messages.PagePart_check_credentials;
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

	private void disposeResources() {
		if (task != null) {
			task.cancel();
			task = null;
		}
		// ClarifaiActivator.getDefault().disposeClient();
	}

	@Override
	public void fillValues() {
		apiKeyField.removeListener(SWT.Modify, this);
		IPreferenceStore preferenceStore = getPreferenceStore();
		apiKeyField.setText(preferenceStore.getString(PreferenceConstants.APIKEY));
		conceptField.setSelection(preferenceStore.getInt(PreferenceConstants.MAXCONCEPTS));
		confidenceField.setSelection(preferenceStore.getInt(PreferenceConstants.MINCONFIDENCE));
		aboveField.setSelection(preferenceStore.getInt(PreferenceConstants.MARKABOVE));
		knownButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.MARKKNOWNONLY));
		adultButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.ADULTCONTENTS));
		faceButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.FACES));
		celebrityButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.CELEBRITIES));
		translateButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.TRANSLATE));
		ClarifaiActivator activator = ClarifaiActivator.getDefault();
		activator.setAccountCredentials(apiKeyField.getText().trim());
		verifyAccountCredenials(100);
		modelCombo.setSelection(new StructuredSelection(ClarifaiActivator.getDefault().getTheme()));
		currentLanguage = preferenceStore.getString(PreferenceConstants.LANGUAGE);
		languageCombo.setSelection(new StructuredSelection(currentLanguage));
		apiKeyField.addListener(SWT.Modify, this);
	}

	@Override
	public void setEnabled(boolean enabled) {
		boolean wasEnabled = enabled;
		super.setEnabled(enabled);
		apiKeyField.setEnabled(enabled);
		conceptField.setEnabled(enabled);
		confidenceField.setEnabled(enabled);
		aboveField.setEnabled(enabled);
		modelCombo.getCombo().setEnabled(enabled);
		languageCombo.getCombo().setEnabled(enabled);
		knownButton.setEnabled(enabled);
		adultButton.setEnabled(enabled);
		faceButton.setEnabled(enabled);
		celebrityButton.setEnabled(enabled);
		translateButton.setEnabled(enabled);
		if (enabled && !wasEnabled) {
			validate();
			checkCredentials(parentPage);
		}
	}

	@Override
	public void performOk() {
		disposeResources();
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.APIKEY, apiKeyField.getText());
		preferenceStore.setValue(PreferenceConstants.MAXCONCEPTS, conceptField.getSelection());
		preferenceStore.setValue(PreferenceConstants.MINCONFIDENCE, confidenceField.getSelection());
		preferenceStore.setValue(PreferenceConstants.MARKABOVE, aboveField.getSelection());
		preferenceStore.setValue(PreferenceConstants.MARKKNOWNONLY, knownButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.ADULTCONTENTS, adultButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.FACES, faceButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.CELEBRITIES, celebrityButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.TRANSLATE,
				translateButton.isEnabled() && translateButton.getSelection());
		Theme theme = (Theme) modelCombo.getStructuredSelection().getFirstElement();
		if (theme != null)
			preferenceStore.setValue(PreferenceConstants.THEME, theme.getId());
		String lang = null;
		if (languageCombo.getControl().isEnabled())
			lang = (String) languageCombo.getStructuredSelection().getFirstElement();
		preferenceStore.setValue(PreferenceConstants.LANGUAGE, lang != null ? lang : "en"); //$NON-NLS-1$
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
		preferenceStore.setValue(PreferenceConstants.TRANSLATE,
				preferenceStore.getDefaultBoolean(PreferenceConstants.TRANSLATE));
		preferenceStore.setValue(PreferenceConstants.THEME,
				preferenceStore.getDefaultString(PreferenceConstants.THEME));
		preferenceStore.setValue(PreferenceConstants.LANGUAGE,
				preferenceStore.getDefaultString(PreferenceConstants.LANGUAGE));
		fillValues();
	}

	private static IPreferenceStore getPreferenceStore() {
		return ClarifaiActivator.getDefault().getPreferenceStore();
	}

	@Override
	public String getLabel() {
		return "&Clarifai"; //$NON-NLS-1$
	}

	@Override
	public void performCancel() {
		disposeResources();
	}

	@Override
	public String validate() {
		if (parentPage.isEnabled()) {
			if (apiKeyField.getText().isEmpty())
				return Messages.PagePart_both_must_be_set;
		}
		return super.validate();
	}

	protected void checkCredentials(AbstractPreferencePage parentPage) {
		parentPage.validate();
		if (validate() == null) {
			ClarifaiActivator.getDefault().setAccountCredentials(apiKeyField.getText().trim());
			verifyAccountCredenials(500);
		}
	}

	@Override
	public void updateButtons() {
		updating = true;
		try {
			if (languageCombo.getControl().isEnabled()) {
				String lang = (String) languageCombo.getStructuredSelection().getFirstElement();
				if (lang != null)
					currentLanguage = lang;
			}
			Theme theme = (Theme) modelCombo.getStructuredSelection().getFirstElement();
			boolean multilingual = true;
			boolean maytranslate = true;
			if (theme != null)
				multilingual = ClarifaiActivator.getDefault().isMultilingual(theme.getId());
			if (multilingual) {
				if (!languageCombo.getControl().isEnabled()) {
					languageCombo.getControl().setEnabled(true);
					languageCombo.setSelection(new StructuredSelection(currentLanguage));
				}
				String lang = (String) languageCombo.getStructuredSelection().getFirstElement();
				maytranslate = (lang == null || "en".equals(lang)); //$NON-NLS-1$
			} else if (languageCombo.getControl().isEnabled()) {
				languageCombo.setSelection(new StructuredSelection("en")); //$NON-NLS-1$
				languageCombo.getControl().setEnabled(false);
			}
			translateButton.setEnabled(enabled && maytranslate && parentPage.canTranslate());
		} finally {
			updating = false;
		}
	}

}
