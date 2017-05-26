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
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.audio.AudioCapture;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.internal.widgets.ZDialog;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class VoiceNoteDialog extends ZDialog {

	private static final int CHANNELS = 1;
	private static final int RECORD = 0;
	private static final int STOP = 1;
	private static final int IMPORT = 2;
	private static final String SETTINGSID = "com.bdaum.zoom.addVoiceNote"; //$NON-NLS-1$
	private static final String SOUNDFILE = "soundFile"; //$NON-NLS-1$
	private static final String REPLAY = "replay"; //$NON-NLS-1$
	private static final int DELETE = 3;
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
	private boolean delete;
	private Button deleteButton;
	private boolean deleteVoiceNote;
	private boolean remote;
	private boolean replaying;

	public VoiceNoteDialog(Shell parentShell, List<Asset> assets) {
		super(parentShell);
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		for (Asset asset : assets) {
			if (volumeManager.findVoiceFile(asset) != null)
				delete = true;
			if (volumeManager.findExistingFile(asset, true) == null)
				remote = true;
		}
	}

	@Override
	public void create() {
		super.create();
		fillValues();
		getShell().setText(Messages.VoiceNoteDialog_add_voice_note);
	}

	private void fillValues() {
		settings = UiActivator.getDefault().getDialogSettings(SETTINGSID);
		try {
			replay = settings.getBoolean(REPLAY);
			replayButton.setSelection(replay);
		} catch (NumberFormatException e) {
			// ignore
		}
		recordButton.setSelection(replay);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (delete) {
			deleteButton = createButton(parent, DELETE, "", true); //$NON-NLS-1$
			deleteButton.setImage(Icons.delete32.getImage());
			deleteButton
					.setToolTipText(Messages.VoiceNoteDialog_delete_voicenote);
		}
		recordButton = createButton(parent, RECORD, "", true); //$NON-NLS-1$
		recordButton.setImage(Icons.record.getImage());
		recordButton
				.setToolTipText(Messages.VoiceNoteDialog_record_instant_note);
		stopButton = createButton(parent, STOP, "", true); //$NON-NLS-1$
		stopButton.setImage(Icons.stop.getImage());
		stopButton
				.setToolTipText(Messages.VoiceNoteDialog_stop_recording_cancel);
		importButton = createButton(parent, IMPORT, "", true); //$NON-NLS-1$
		importButton.setImage(Icons.folder32.getImage());
		importButton.setToolTipText(Messages.VoiceNoteDialog_attach_voice_note);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayout(new GridLayout());
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));

		text = new Label(comp, SWT.READ_ONLY | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		text.setText(Messages.VoiceNoteDialog_ready);
		replayButton = WidgetFactory.createCheckButton(comp,
				Messages.VoiceNoteDialog_replay_Recording, null);
		return area;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case DELETE:
			deleteVoiceNote = true;
			close();
			break;
		case RECORD:
			recordButton.setEnabled(false);
			importButton.setEnabled(false);
			try {
				record();
			} catch (LineUnavailableException e1) {
				UiActivator.getDefault().logError(
						Messages.VoiceNoteDialog_cannot_record, e1);
				recordButton.setEnabled(true);
				importButton.setEnabled(true);
				return;
			}
			text.setText(Messages.VoiceNoteDialog_recording);
			recordJob = new Daemon(Messages.VoiceNoteDialog_record_voicenote,
					1000L) {
				private int seconds = 0;

				@Override
				protected void doRun(IProgressMonitor monitor) {
					++seconds;
					if (!text.isDisposed())
						text.getDisplay().asyncExec(new Runnable() {
							public void run() {
								if (!text.isDisposed())
									text.setText(NLS
											.bind(Messages.VoiceNoteDialog_recording_n_secs,
													seconds));
							}
						});
				}
			};
			recordJob.schedule(1000L);
			break;
		case STOP:
			if (replaying)
				UiActivator.getDefault().stopAudio();
			else {
				if (recorder != null)
					recorder.stopCapture();
				if (recordJob != null)
					Job.getJobManager().cancel(recordJob);
				saveAndClose();
			}
			break;
		case IMPORT:
			if (askOverwrite())
				break;
			IDialogSettings settings = UiActivator.getDefault()
					.getDialogSettings(SETTINGSID);
			String file = openFileDialog(settings, SWT.OPEN);
			if (file != null) {
				settings.put(SOUNDFILE, file);
				try {
					settings.save(UiActivator.getDefault().getStateLocation()
							.toString()
							+ "/dialog_settings.xml"); //$NON-NLS-1$
				} catch (IllegalStateException e) {
					UiActivator.getDefault().logError(
							Messages.VoiceNoteDialog_internal_error_writing, e);
				} catch (IOException e) {
					UiActivator.getDefault().logError(
							Messages.VoiceNoteDialog_io_error_writing, e);
				}
				outputFile = new File(file);
				sourceUri = outputFile.toURI().toString();
				targetUri = sourceUri;
				saveAndClose();
			}
			break;
		}
	}

	private void saveAndClose() {
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
				UiActivator.getDefault().playSoundfile(
						outputFile.toURI().toURL(), new Runnable() {
							public void run() {
								replayButton.getDisplay().asyncExec(
										new Runnable() {
											public void run() {
												close();
											}
										});
							}
						});
				return;
			} catch (MalformedURLException e) {
				// should not happen
			}
		}
		close();
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
		if (delete) {
			if (askOverwrite())
				return;
		}
		if (remote) {
			boolean r = AcousticMessageDialog
					.openQuestion(
							getShell(),
							Messages.VoiceNoteDialog_add_voice_file,
							Messages.VoiceNoteDialog_the_voice_file_cannot_be_stored_alongside);
			if (r) {
				IDialogSettings settings = UiActivator.getDefault()
						.getDialogSettings(SETTINGSID);
				String file = openFileDialog(settings, SWT.SAVE);
				if (file != null) {
					try {
						outputFile = ImageActivator.getDefault()
								.createTempFile("Voice", ".wav"); //$NON-NLS-1$ //$NON-NLS-2$
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
				outputFile = ImageActivator.getDefault().createTempFile(
						"Voice", ".wav"); //$NON-NLS-1$ //$NON-NLS-2$
				sourceUri = outputFile.toURI().toString();
				targetUri = "."; //$NON-NLS-1$
				outputFile.delete();

			} catch (IOException e) {
				return;
			}
		}
		IPreferencesService preferencesService = Platform
				.getPreferencesService();
		float samplingRate = (float) preferencesService.getDouble(
				UiActivator.PLUGIN_ID, PreferenceConstants.AUDIOSAMPLINGRATE,
				PreferenceConstants.AUDIO22KHZ, null);
		int bitDepth = preferencesService.getInt(UiActivator.PLUGIN_ID,
				PreferenceConstants.AUDIOBITDEPTH,
				PreferenceConstants.AUDIO8BIT, null);
		AudioFormat audioFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED, samplingRate, bitDepth,
				CHANNELS, ((bitDepth + 7) / 8) * CHANNELS, samplingRate, false);
		DataLine.Info info = new DataLine.Info(TargetDataLine.class,
				audioFormat);
		TargetDataLine targetDataLine = null;
		targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
		targetDataLine.open(audioFormat);
		AudioFileFormat.Type targetType = AudioFileFormat.Type.WAVE;
		recorder = new AudioCapture(targetDataLine, targetType, outputFile);
		recorder.startCapture();
	}

	public boolean askOverwrite() {
		if (delete) {
			boolean r = AcousticMessageDialog.openQuestion(getShell(),
					Messages.VoiceNoteDialog_add_voice_file,
					Messages.VoiceNoteDialog_voice_notes_exist);
			if (!r) {
				close();
				return true;
			}
		}
		return false;
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

}
