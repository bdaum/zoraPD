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

package com.bdaum.zoom.ui.internal.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;

public class AudioPreferencePage extends AbstractPreferencePage {
	private ComboViewer rateViewer;
	private ComboViewer bitViewer;
	private CheckboxButton alarmOnPromptButton;
	private CheckboxButton alarmOnFinishButton;

	public AudioPreferencePage() {
		setDescription(Messages.getString("AudioPreferencePage.audio_desct")); //$NON-NLS-1$
	}


	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.AUDIO_PREFERENCE_PAGE);
		createSignalsGroup(composite);
		createVoiceGroup(composite);
		fillValues();
	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		rateViewer.setSelection(new StructuredSelection(preferenceStore
				.getString(PreferenceConstants.AUDIOSAMPLINGRATE)));
		bitViewer.setSelection(new StructuredSelection(preferenceStore
				.getString(PreferenceConstants.AUDIOBITDEPTH)));
		alarmOnPromptButton.setSelection(preferenceStore
				.getBoolean(PreferenceConstants.ALARMONPROMPT));
		alarmOnFinishButton.setSelection(preferenceStore
				.getBoolean(PreferenceConstants.ALARMONFINISH));
	}

	private void createVoiceGroup(Composite composite) {
		CGroup group = createGroup(composite, 2, Messages.getString("AudioPreferencePage.voice_notes")); //$NON-NLS-1$
		final String[] rateOptions = new String[] {
				String.valueOf(PreferenceConstants.AUDIO11KHZ),
				String.valueOf(PreferenceConstants.AUDIO22KHZ),
				String.valueOf(PreferenceConstants.AUDIO44KHZ) };
		final String[] rateLabels = new String[] { "11 kHz", //$NON-NLS-1$
				"22 kHz", //$NON-NLS-1$
				"44 kHz" }; //$NON-NLS-1$
		rateViewer = createComboViewer(group, Messages
				.getString("AudioPreferencePage.sampling_rate"), rateOptions, //$NON-NLS-1$
				rateLabels, false);
		final String[] bitOptions = new String[] {
				String.valueOf(PreferenceConstants.AUDIO8BIT),
				String.valueOf(PreferenceConstants.AUDIO16BIT) };
		final String[] bitLabels = new String[] { "8 Bit", //$NON-NLS-1$
				"16 Bit" }; //$NON-NLS-1$
		bitViewer = createComboViewer(group, Messages
				.getString("AudioPreferencePage.BitsPerSample"), bitOptions, //$NON-NLS-1$
				bitLabels, false);
	}

	private void createSignalsGroup(Composite composite) {
		CGroup group = createGroup(composite, 2, Messages.getString("AudioPreferencePage.signals")); //$NON-NLS-1$
		alarmOnPromptButton = WidgetFactory
				.createCheckButton(
						group,
						Messages
								.getString("AppearancePreferencePage.acoustinc_alarm_on_prompt"), //$NON-NLS-1$
						new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2,
								1));
		alarmOnFinishButton = WidgetFactory
				.createCheckButton(
						group,
						Messages
								.getString("AppearancePreferencePage.acoustic_alarm_after_job"), //$NON-NLS-1$
						new GridData(SWT.BEGINNING, SWT.CENTER, true, true, 2,
								1));
	}


	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.AUDIOSAMPLINGRATE,
				preferenceStore
						.getDefaultInt(PreferenceConstants.AUDIOSAMPLINGRATE));
		preferenceStore.setValue(PreferenceConstants.AUDIOBITDEPTH,
				preferenceStore
						.getDefaultInt(PreferenceConstants.AUDIOBITDEPTH));
		preferenceStore.setValue(PreferenceConstants.ALARMONPROMPT,
				preferenceStore
						.getDefaultBoolean(PreferenceConstants.ALARMONPROMPT));
		preferenceStore.setValue(PreferenceConstants.ALARMONFINISH,
				preferenceStore
						.getDefaultBoolean(PreferenceConstants.ALARMONFINISH));
	}


	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		IStructuredSelection selection = (IStructuredSelection) rateViewer
				.getSelection();
		if (!selection.isEmpty())
			preferenceStore.setValue(PreferenceConstants.AUDIOSAMPLINGRATE,
					(String) selection.getFirstElement());
		selection = (IStructuredSelection) bitViewer.getSelection();
		if (!selection.isEmpty())
			preferenceStore.setValue(PreferenceConstants.AUDIOBITDEPTH,
					(String) selection.getFirstElement());
		preferenceStore.setValue(PreferenceConstants.ALARMONPROMPT,
				alarmOnPromptButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.ALARMONFINISH,
				alarmOnFinishButton.getSelection());
	}


}
