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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.batch.internal.Daemon;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.audio.AudioCapture;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class VoiceNoteDialog extends ZTitleAreaDialog {

	private static final int CHANNELS = 1;
	private static final int RECORD = 0;
	private static final int STOP = 1;
	private static final int IMPORT = 2;
	private static final int NOTE = 3;
	private static final int DELETE = 4;
	private static final int CANCEL = 5;
	private static final String SETTINGSID = "com.bdaum.zoom.addVoiceNote"; //$NON-NLS-1$
	private static final String SOUNDFILE = "soundFile"; //$NON-NLS-1$
	private static final String REPLAY = "replay"; //$NON-NLS-1$
	private Button stopButton;
	private Button recordButton;
	private Button importButton;
	private Label text;
	private AudioCapture recorder;
	private String sourceUri;
	private Daemon recordJob;
	private CheckboxButton replayButton;
	private IDialogSettings settings;
	private boolean replay = true;
	private File outputFile;
	private String targetUri;
	private boolean existingVoiceNotes;
	private Button deleteButton;
	private boolean deleteVoiceNote;
	private boolean remote;
	private boolean replaying;
	private Button noteButton;
	private StackLayout stackLayout;
	private Composite voiceComp;
	private Composite noteComp;
	private CheckedText note;
	private Composite stack;
	private String noteText;
	private Button cancelButton;
	private boolean textNotes;
	private boolean multi;
	private boolean mixed;

	public VoiceNoteDialog(Shell parentShell, List<Asset> assets, boolean textNotes) {
		super(parentShell);
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		this.textNotes = textNotes;
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		multi = assets.size() > 1;
		for (Asset asset : assets) {
			if (!mixed) {
				String voiceFileURI = asset.getVoiceFileURI();
				if (voiceFileURI != null && voiceFileURI.startsWith("?")) { //$NON-NLS-1$
					if (noteText == null)
						noteText = voiceFileURI;
					else if (!noteText.equals(voiceFileURI))
						mixed = true;
					continue;
				}
			}
			if (volumeManager.findVoiceFile(asset) != null)
				existingVoiceNotes = true;
			if (volumeManager.findExistingFile(asset, true) == null)
				remote = true;
		}
	}

	@Override
	public void create() {
		super.create();
		fillValues();
		setTitle(Messages.VoiceNoteDialog_add_voice_note);
		setMessage(textNotes ? Messages.VoiceNoteDialog_voice_note_msg : Messages.VoiceNoteDialog_voice_text_note_msg);
	}

	private void fillValues() {
		try {
			replay = settings.getBoolean(REPLAY);
			replayButton.setSelection(replay);
		} catch (NumberFormatException e) {
			// ignore
		}
		recordButton.setSelection(replay);
		if (noteText != null && textNotes) {
			if (mixed) {
				note.setHint(Messages.VoiceNoteDialog_mixed_contents);
				cancelButton.setFocus();
			} else
				note.setText(noteText.substring(1));
			stackLayout.topControl = noteComp;
			stack.layout(true, true);
			noteButton.setEnabled(false);
		} else {
			stackLayout.topControl = voiceComp;
			stack.layout(true, true);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (existingVoiceNotes) {
			deleteButton = createButton(parent, DELETE, "", true); //$NON-NLS-1$
			deleteButton.setImage(Icons.trash32.getImage());
			deleteButton.setToolTipText(Messages.VoiceNoteDialog_delete_voicenote);
		}
		recordButton = createButton(parent, RECORD, "", true); //$NON-NLS-1$
		recordButton.setImage(Icons.record.getImage());
		recordButton.setToolTipText(Messages.VoiceNoteDialog_record_instant_note);
		stopButton = createButton(parent, STOP, "", true); //$NON-NLS-1$
		stopButton.setImage(Icons.stop.getImage());
		stopButton.setToolTipText(Messages.VoiceNoteDialog_stop_recording_cancel);
		importButton = createButton(parent, IMPORT, "", true); //$NON-NLS-1$
		importButton.setImage(Icons.folder32.getImage());
		importButton.setToolTipText(Messages.VoiceNoteDialog_attach_voice_note);
		if (textNotes) {
			noteButton = createButton(parent, NOTE, "", true); //$NON-NLS-1$
			noteButton.setImage(Icons.note32.getImage());
			noteButton.setToolTipText(Messages.VoiceNoteDialog_enter_text);
		}
		cancelButton = createButton(parent, CANCEL, "", true); //$NON-NLS-1$
		cancelButton.setImage(Icons.cancel32.getImage());
		cancelButton.setToolTipText(Messages.VoiceNoteDialog_cancel);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		stack = new Composite(area, SWT.NONE);
		stack.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);
		voiceComp = new Composite(stack, SWT.NONE);
		voiceComp.setLayout(new GridLayout());
		text = new Label(voiceComp, SWT.READ_ONLY | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		text.setText(Messages.VoiceNoteDialog_ready);
		replayButton = WidgetFactory.createCheckButton(voiceComp, Messages.VoiceNoteDialog_replay_Recording, null);
		noteComp = new Composite(stack, SWT.NONE);
		noteComp.setLayout(new GridLayout());
		if (textNotes) {
			new Label(noteComp, SWT.NONE).setText(Messages.VoiceNoteDialog_text_note);
			note = new CheckedText(noteComp, SWT.MULTI | SWT.VERTICAL | SWT.WRAP);
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
			layoutData.heightHint = 120;
			note.setLayoutData(layoutData);
		}
		return area;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case DELETE:
			deleteVoiceNote = true;
			noteText = null;
			okPressed();
			break;
		case RECORD:
			if (askOverwrite(buttonId))
				break;
			noteText = null;
			stackLayout.topControl = voiceComp;
			stack.layout(true, true);
			recordButton.setEnabled(false);
			importButton.setEnabled(false);
			if (noteButton != null)
				noteButton.setEnabled(true);
			try {
				record();
			} catch (LineUnavailableException e1) {
				UiActivator.getDefault().logError(Messages.VoiceNoteDialog_cannot_record, e1);
				recordButton.setEnabled(true);
				importButton.setEnabled(true);
				return;
			}
			text.setText(Messages.VoiceNoteDialog_recording);
			recordJob = new Daemon(Messages.VoiceNoteDialog_record_voicenote, 1000L) {
				private int seconds = 0;

				@Override
				protected void doRun(IProgressMonitor monitor) {
					++seconds;
					if (!text.isDisposed())
						text.getDisplay().asyncExec(() -> {
							if (!text.isDisposed())
								text.setText(NLS.bind(Messages.VoiceNoteDialog_recording_n_secs, seconds));
						});
				}
			};
			recordJob.schedule(1000L);
			break;
		case STOP:
			stopAudio();
			if (!replaying)
				saveAndClose();
			break;
		case IMPORT:
			if (askOverwrite(buttonId))
				break;
			stackLayout.topControl = voiceComp;
			stack.layout(true, true);
			String file = openFileDialog(settings, SWT.OPEN);
			if (file != null) {
				noteText = null;
				settings.put(SOUNDFILE, file);
				UiActivator.getDefault().saveDialogSettings();
				outputFile = new File(file);
				sourceUri = outputFile.toURI().toString();
				targetUri = sourceUri;
				saveAndClose();
			}
			break;
		case NOTE:
			if (askOverwrite(buttonId))
				break;
			stopAudio();
			noteButton.setEnabled(false);
			stackLayout.topControl = noteComp;
			stack.layout(true, true);
			break;
		case CANCEL:
			stopAudio();
			cancelPressed();
			break;
		}
	}

	protected void stopAudio() {
		if (replaying)
			UiActivator.getDefault().stopAudio();
		else {
			if (recorder != null)
				recorder.stopCapture();
			if (recordJob != null)
				Job.getJobManager().cancel(recordJob);

		}
	}

	private void saveAndClose() {
		if (stackLayout.topControl == voiceComp) {
			noteText = null;
			replay = replayButton.getSelection();
			settings.put(REPLAY, replay);
			if (replay && outputFile != null && outputFile.exists()) {
				if (deleteButton != null)
					deleteButton.setEnabled(false);
				recordButton.setEnabled(false);
				importButton.setEnabled(false);
				replayButton.setEnabled(false);
				replaying = true;
				try {
					UiActivator.getDefault().playSoundfile(outputFile.toURI().toURL(),
							() -> replayButton.getDisplay().asyncExec(() -> {
								if (!replayButton.isDisposed())
									okPressed();
							}));
					return;
				} catch (MalformedURLException e) {
					// should not happen
				}
			}
		} else {
			if (mixed) {
				cancelPressed();
				return;
			}
			noteText = '?' + note.getText();
		}
		okPressed();
	}

	private String openFileDialog(IDialogSettings settings, int style) {
		FileDialog dialog = new FileDialog(getShell(), style);
		String[] supportedsoundfileextensions = Constants.SupportedSoundFileExtensions;
		for (int i = 0; i < supportedsoundfileextensions.length - 1; i++)
			supportedsoundfileextensions[i] += ";" //$NON-NLS-1$
					+ supportedsoundfileextensions[i].toUpperCase();
		dialog.setFilterExtensions(supportedsoundfileextensions);
		dialog.setFilterNames(Constants.SupportedSoundFileNames);
		String soundFile = settings.get(SOUNDFILE);
		dialog.setOverwrite(style == SWT.SAVE);
		if (soundFile != null)
			dialog.setFileName(soundFile);
		return dialog.open();
	}

	private void record() throws LineUnavailableException {
		if (remote) {
			if (AcousticMessageDialog.openQuestion(getShell(), Messages.VoiceNoteDialog_add_voice_file,
					Messages.VoiceNoteDialog_the_voice_file_cannot_be_stored_alongside)) {
				String file = openFileDialog(settings, SWT.SAVE);
				if (file != null) {
					try {
						outputFile = ImageActivator.getDefault().createTempFile("Voice", ".wav"); //$NON-NLS-1$ //$NON-NLS-2$
						sourceUri = outputFile.toURI().toString();
						outputFile.delete();
						targetUri = new File(file).toURI().toString();
					} catch (IOException e) {
						return;
					}
				}
			}
		} else {
			try {
				outputFile = ImageActivator.getDefault().createTempFile("Voice", ".wav"); //$NON-NLS-1$ //$NON-NLS-2$
				sourceUri = outputFile.toURI().toString();
				targetUri = "."; //$NON-NLS-1$
				outputFile.delete();
			} catch (IOException e) {
				return;
			}
		}
		IPreferencesService preferencesService = Platform.getPreferencesService();
		float samplingRate = (float) preferencesService.getDouble(UiActivator.PLUGIN_ID,
				PreferenceConstants.AUDIOSAMPLINGRATE, PreferenceConstants.AUDIO22KHZ, null);
		int bitDepth = preferencesService.getInt(UiActivator.PLUGIN_ID, PreferenceConstants.AUDIOBITDEPTH,
				PreferenceConstants.AUDIO8BIT, null);
		AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, samplingRate, bitDepth, CHANNELS,
				((bitDepth + 7) / 8) * CHANNELS, samplingRate, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
		TargetDataLine targetDataLine = null;
		targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
		targetDataLine.open(audioFormat);
		AudioFileFormat.Type targetType = AudioFileFormat.Type.WAVE;
		recorder = new AudioCapture(targetDataLine, targetType, outputFile);
		recorder.startCapture();
	}

	public boolean askOverwrite(int buttonId) {
		String t = null;
		if (note != null && buttonId != NOTE)
			t = note.getText();
		return ((t != null || existingVoiceNotes) && !AcousticMessageDialog.openQuestion(getShell(),
				Messages.VoiceNoteDialog_add_voice_file,
				multi ? Messages.VoiceNoteDialog_voice_notes_exist : Messages.VoiceNoteDialog_overwrite_single));
	}

	public String getSourceUri() {
		return sourceUri;
	}

	public String getTargetUri() {
		return targetUri;
	}

	/**
	 * @return deleteVoiceNote
	 */
	public boolean isDeleteVoiceNote() {
		return deleteVoiceNote;
	}

	public String getNoteText() {
		return noteText;
	}

}
