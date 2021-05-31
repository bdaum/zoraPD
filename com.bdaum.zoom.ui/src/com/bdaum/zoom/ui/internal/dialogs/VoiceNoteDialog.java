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
 * (c) 2009-2021 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.xml.sax.SAXException;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.batch.internal.Daemon;
import com.bdaum.zoom.batch.internal.ExifTool;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.program.IRawConverter;
import com.bdaum.zoom.ui.IStateListener;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.IKiosk;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.audio.AudioCapture;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.paint.DrawExample;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class VoiceNoteDialog extends ZResizableDialog implements IKiosk, Listener {

	private static final String DRAWING_BOUNDS = "DrawingBounds"; //$NON-NLS-1$
	private static final String VOICE_BOUNDS = "VoiceBounds"; //$NON-NLS-1$

	public class PrepareImageJob extends Job {

		private Asset asset;
		private Shell shell;

		public PrepareImageJob(Shell shell, Asset asset) {
			super(Messages.VoiceNoteDialog_prepare_image);
			setSystem(true);
			setPriority(Job.INTERACTIVE);
			this.shell = shell;
			this.asset = asset;
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == VoiceNoteDialog.this;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			URI uri;
			try {
				uri = new URI(asset.getUri());
			} catch (URISyntaxException e1) {
				return Status.CANCEL_STATUS;
			}
			if (Constants.FILESCHEME.equals(uri.getScheme())) {
				File file = new File(uri);
				if (file != null) {
					Recipe recipe = null;
					boolean isRawOrDng = ImageConstants.isRaw(file.getName(), true);
					if (isRawOrDng) {
						IRawConverter currentRawConverter = BatchActivator.getDefault().getCurrentRawConverter(false);
						if (currentRawConverter != null) {
							recipe = currentRawConverter.getRecipe(file.toURI().toString(), false,
									new IFocalLengthProvider() {
										public double get35mm() {
											return asset.getFocalLengthIn35MmFilm();
										}
									}, null, null);
							currentRawConverter.unget();
						}
					}
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					if (recipe == null || recipe == Recipe.NULL) {
						int previewSize = asset.getPreviewSize();
						if (previewSize < 0 || previewSize >= Math.max(bounds.width, bounds.height)) {
							try (ExifTool exifTool = new ExifTool(file, true)) {
								ZImage zimage = exifTool.getPreviewImage(false);
								if (zimage != null) {
									if (getShell().isDisposed())
										return Status.CANCEL_STATUS;
									ExifTool.fixOrientation(zimage, 0, asset.getRotation());
									Rectangle nbounds = zimage.getBounds();
									zimage.setScaling(nbounds.width, nbounds.height, true, 0, null);
									image = zimage.getSwtImage(shell.getDisplay(), true, ZImage.CROPPED, -1, -1);
								}
							} catch (Exception e) {
								// ignore and use full size image
							}
						}
					}
				}
				if (image == null)
					image = Core.getCore().getImageCache().getImage(asset);
				shell.getDisplay().asyncExec(() -> {
					if (shell.isDisposed())
						paintExample.setBackgroundImage(image, w, h);
				});
				if (svg != null && !svg.isEmpty())
					shell.getDisplay().asyncExec(() -> {
						if (shell.isDisposed())
							try {
								paintExample.importSvg(svg, true);
							} catch (IOException | ParserConfigurationException | SAXException e) {
								UiActivator.getDefault().logError(Messages.VoiceNoteDialog_unable_to_create_drawing, e);
							}
					});
			}
			return Status.OK_STATUS;
		}
	}

	private static final int CHANNELS = 1;
	private static final int IMPORT = 9999;
	private static final int COPY = 9998;
	private static final String SETTINGSID = "com.bdaum.zoom.addVoiceNote"; //$NON-NLS-1$
	private static final String VOICE_NOTE_DIALOG = "VoiceNoteDialog"; //$NON-NLS-1$
	private static final String SOUNDFILE = "soundFile"; //$NON-NLS-1$
	private static final String TEXTFILE = "textFile"; //$NON-NLS-1$
	private static final String SVGFILE = "svgFile"; //$NON-NLS-1$
	private static final String ACTIVETAB = "activeTab"; //$NON-NLS-1$
	private static final int VOICE = 0;
	private static final int TEXT = 1;
	private static final int DRAWING = 2;
	private Button stopButton;
	private Button recordButton;
	private Button importButton;
	private Label text;
	private AudioCapture recorder;
	private String sourceUri;
	private Daemon recordJob;
	private IDialogSettings settings;
	private File outputFile;
	private String targetUri;
	private Button deleteButton;
	private boolean replaying;
	private CheckedText note;
	private String noteText;
	private CTabFolder tabFolder;
	private boolean dirty;
	private URI voiceFileUri;
	private String svg;
	private Button replayButton;
	private Button copyButton;
	protected boolean recording;
	private boolean remoteAsset;
	private boolean remoteVoice;
	private DrawExample paintExample;
	private Image image;
	private PrepareImageJob job;
	private Asset asset;
	Point fDrawingSize;
	private int w = 320;
	private int h = 320;
	private IDialogSettings fSettings;
	private Button exportButton;
	private Rectangle bounds;
	private Date creationDate;

	public VoiceNoteDialog(Shell parentShell, Asset asset) {
		super(parentShell);
		this.asset = asset;
		settings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		voiceFileUri = volumeManager.findVoiceFile(asset);
		remoteAsset = volumeManager.findExistingFile(asset, true) == null;
		String compound = asset.getVoiceFileURI();
		if (compound != null) {
			int p = compound.indexOf('\f');
			String origVoiceFileUri = null;
			if (p >= 0) {
				origVoiceFileUri = compound.substring(0, p);
				int q = compound.indexOf('\f', p + 1);
				if (q != 0) {
					noteText = compound.substring(p + 1, q);
					svg = compound.substring(q + 1);
				}
			} else if (compound.startsWith("?")) { //$NON-NLS-1$
				noteText = compound.substring(1);
			} else
				origVoiceFileUri = compound;
			remoteVoice = origVoiceFileUri != null && !origVoiceFileUri.isEmpty() && voiceFileUri == null;
		}
		fSettings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
	}

	@Override
	public void init(IWorkbenchWindow parentWindow, int kind) {
		// do nothing
	}

	@Override
	public void create() {
		super.create();
		creationDate = new Date();
		bounds = UiActivator.getDefault().getSecondaryMonitorBounds(getShell());
		UiActivator.getDefault().registerKiosk(this, bounds);
		job = new PrepareImageJob(getShell(), asset);
		job.schedule();
		fillValues();
		updateButtons();
	}

	private void fillValues() {
		if (noteText != null)
			note.setText(noteText);
		updateTextfield();
		dirty = false;
	}

	@Override
	protected void updateBounds(Shell s) {
		if (tabFolder.getSelectionIndex() != DRAWING)
			fNewBounds = s.getBounds();
		else
			fDrawingSize = s.getSize();
	}

	private void updateTextfield() {
		text.setText(voiceFileUri != null ? Messages.VoiceNoteDialog_voicenote_present
				: remoteVoice ? Messages.VoiceNoteDialog_voicenote_offline : Messages.VoiceNoteDialog_no_voicenote);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		copyButton = createButton(parent, COPY, "Copy", true); //$NON-NLS-1$
		copyButton.setToolTipText(Messages.VoiceNoteDialog_copy_current_annotations);
		super.createButtonsForButtonBar(parent);
		importButton = createButton(parent, IMPORT, "Import", true); //$NON-NLS-1$
		importButton.setToolTipText(Messages.VoiceNoteDialog_attach_external);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		tabFolder = new CTabFolder(area, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		tabFolder.setSimple(false);
		UiUtilities.createTabItem(tabFolder, Messages.VoiceNoteDialog_voice_note,
				Messages.VoiceNoteDialog_record_remove_voice_notes).setControl(createVoiceGroup(tabFolder));
		UiUtilities
				.createTabItem(tabFolder, Messages.VoiceNoteDialog_text_note, Messages.VoiceNoteDialog_edit_text_notes)
				.setControl(createTextGroup(tabFolder));
		UiUtilities.createTabItem(tabFolder, Messages.VoiceNoteDialog_drawing,
				Messages.VoiceNoteDialog_create_overlayed_drawing).setControl(createDrawingGroup(tabFolder));
		tabFolder.addListener(SWT.Selection, this);
		try {
			tabFolder.setSelection(settings.getInt(ACTIVETAB));
		} catch (NumberFormatException e) {
			// do nothing
		}
		return area;
	}

	private void resizeDialog() {
		int selectionIndex = tabFolder.getSelectionIndex();
		Shell shell = getShell();
		if (selectionIndex == DRAWING) {
			fNewBounds = shell.getBounds();
			if (fDrawingSize == null)
				fDrawingSize = getInitialSize(DRAWING);
			shell.setSize(fDrawingSize);
			shell.layout(true, true);
		} else {
			shell.setSize(fNewBounds.width, fNewBounds.height);
			shell.layout(true, true);
		}

	}

	@Override
	protected Point doGetInitialSize() {
		return getInitialSize(VOICE);
	}

	private Point getInitialSize(int type) {
		int width = 0;
		int height = 0;
		IDialogSettings bounds = fSettings.getSection(type == DRAWING ? DRAWING_BOUNDS : VOICE_BOUNDS);
		if (bounds == null) {
			if (type == DRAWING) {
				Shell shell = getParentShell();
				if (shell != null) {
					Point parentSize = shell.getSize();
					width = parentSize.x * 3 / 4;
					height = parentSize.y * 3 / 4;
				}
			}
			Point defaultSize = getDefaultSize();
			width = Math.max(width, defaultSize.x);
			height = Math.max(height, defaultSize.y);
		} else {
			try {
				width = bounds.getInt(WIDTH);
			} catch (NumberFormatException e) {
				width = getDefaultSize().x;
			}
			try {
				height = bounds.getInt(HEIGHT);
			} catch (NumberFormatException e) {
				height = getDefaultSize().y;
			}
		}
		return new Point(width, height);
	}

	protected void updateButtons() {
		boolean busy = recording || replaying;
		stopButton.setEnabled(busy);
		recordButton.setEnabled(!busy && !remoteAsset);
		replayButton.setEnabled(!busy && voiceFileUri != null);
		deleteButton.setEnabled(!busy && voiceFileUri != null);
		importButton.setEnabled(!busy);
		copyButton.setEnabled(!busy);
		exportButton.setEnabled(!busy && paintExample.hasVectorFigure());
		getButton(OK).setEnabled(!busy && dirty);
	}

	private Control createDrawingGroup(CTabFolder tabFolder) {
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout());
		paintExample = new DrawExample(composite);
		paintExample.createGUI(composite, w, h);
		Composite toolbar = paintExample.getToolbar();
		exportButton = new Button(toolbar, SWT.PUSH);
		exportButton.setLayoutData(new GridData(SWT.CENTER, SWT.END, true, true));
		exportButton.setToolTipText(Messages.VoiceNoteDialog_export_drawing);
		exportButton.setImage(Icons.save.getImage());
		exportButton.addListener(SWT.Selection, this);
		paintExample.setDefaults();
		paintExample.addListener(SWT.Modify, this);
		return composite;
	}

	protected void exportSvg() {
		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setFilterExtensions(new String[] { "*.zdrw" }); //$NON-NLS-1$
		dialog.setFilterNames(new String[] { NLS.bind(Messages.VoiceNoteDialog_zdrw, Constants.APPNAME) });
		dialog.setOverwrite(true);
		String textFile = settings.get(SVGFILE);
		if (textFile != null)
			dialog.setFileName(textFile);
		String file = dialog.open();
		if (file != null) {
			settings.put(SVGFILE, file);
			svg = paintExample.exportSvg();
			File outFile = new File(file);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
				writer.write(svg);
			} catch (IOException e) {
				AcousticMessageDialog.openError(getShell(), Messages.VoiceNoteDialog_error_drawing,
						NLS.bind(Messages.VoiceNoteDialog_io_error_drawing, e));
			}
		}
	}

	private Control createTextGroup(CTabFolder tabFolder) {
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout());
		note = new CheckedText(composite, SWT.MULTI | SWT.VERTICAL | SWT.WRAP);
		note.addListener(SWT.Modify, this);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 120;
		note.setLayoutData(layoutData);
		return composite;
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.Modify:
			dirty = true;
			updateButtons();
			return;
		case SWT.Selection:
			if (e.widget == tabFolder) {
				resizeDialog();
				updateButtons();
				if (tabFolder.getSelectionIndex() == DRAWING) {
					try {
						Job.getJobManager().join(VoiceNoteDialog.this, null);
					} catch (OperationCanceledException | InterruptedException ex) {
						// do nothing
					}
					paintExample.repaintSurface();
				}
			} else if (e.widget == exportButton)
				exportSvg();
			else if (e.widget == recordButton) {
				if (voiceFileUri != null && !AcousticMessageDialog.openQuestion(getShell(),
						Messages.VoiceNoteDialog_add_voice_file, Messages.VoiceNoteDialog_overwrite_single))
					return;
				recording = true;
				updateButtons();
				try {
					record();
				} catch (LineUnavailableException e1) {
					UiActivator.getDefault().logError(Messages.VoiceNoteDialog_cannot_record, e1);
					recording = false;
					updateButtons();
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
				updateButtons();
			} else if (e.widget == stopButton) {
				stopAudio();
				remoteVoice = false;
				updateButtons();
				updateTextfield();
			} else if (e.widget == replayButton) {
				try {
					replaying = true;
					updateButtons();
					UiActivator.getDefault().playSoundfile(voiceFileUri.toURL(), () -> {
						replaying = false;
						updateButtons();
					});
				} catch (MalformedURLException e1) {
					// should not happen
				}
				updateButtons();
			} else if (e.widget == deleteButton) {
				voiceFileUri = null;
				dirty = true;
				updateButtons();
			}
		}
	}

	private Control createVoiceGroup(CTabFolder tabFolder) {
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		text = new Label(composite, SWT.READ_ONLY | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		text.setText(Messages.VoiceNoteDialog_ready);
		Composite sidebar = new Composite(composite, SWT.NONE);
		sidebar.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		sidebar.setLayout(new GridLayout(1, false));
		recordButton = createButton(sidebar, Icons.record.getImage(), Messages.VoiceNoteDialog_record_instant_note);
		recordButton.addListener(SWT.Selection, this);

		stopButton = createButton(sidebar, Icons.stop.getImage(), Messages.VoiceNoteDialog_stop_recording);
		stopButton.addListener(SWT.Selection, this);
		replayButton = createButton(sidebar, Icons.replay.getImage(), Messages.VoiceNoteDialog_replay_current);
		replayButton.addListener(SWT.Selection, this);
		deleteButton = createButton(sidebar, Icons.trash16.getImage(), Messages.VoiceNoteDialog_delete_voicenote);
		deleteButton.addListener(SWT.Selection, this);
		return composite;
	}

	private static Button createButton(Composite parent, Image image, String tooltip) {
		Button button = new Button(parent, SWT.PUSH);
		button.setImage(image);
		button.setToolTipText(tooltip);
		return button;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case IMPORT:
			importFile(settings);
			break;
		case COPY:
			copy();
			okPressed();
			break;
		default:
			super.buttonPressed(buttonId);
		}
	}

	private void copy() {
		noteText = note.getText();
		svg = paintExample.exportSvg();
		StringBuilder sb = new StringBuilder();
		sb.append('\f');
		if (targetUri != null)
			sb.append(targetUri);
		sb.append('\f').append(noteText).append('\f');
		if (svg != null)
			sb.append(svg);
		if (sb.length() > 3) {
			Clipboard clipboard = UiActivator.getDefault().getClipboard(getShell().getDisplay());
			if (outputFile == null)
				clipboard.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
			else
				clipboard.setContents(new Object[] { sb.toString(), new String[] { outputFile.toString() } },
						new Transfer[] { TextTransfer.getInstance(), FileTransfer.getInstance() });
		}
	}

	@Override
	protected void okPressed() {
		noteText = note.getText();
		svg = paintExample.exportSvg();
		super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		stopAudio();
		super.cancelPressed();
	}

	protected void stopAudio() {
		if (replaying) {
			UiActivator.getDefault().stopAudio();
			replaying = false;
		}
		if (recording) {
			if (recorder != null)
				recorder.stopCapture();
			if (recordJob != null) {
				Job.getJobManager().cancel(recordJob);
				recordJob = null;
			}
			recording = false;
		}
	}

	private void importFile(IDialogSettings settings) {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		switch (tabFolder.getSelectionIndex()) {
		case VOICE:
			int l = Constants.SupportedSoundFileExtensions.length;
			String[] supportedsoundfileextensions = new String[l];
			for (int i = 0; i < l - 1; i++)
				supportedsoundfileextensions[i] = Constants.SupportedSoundFileExtensions[i] + ";" //$NON-NLS-1$
						+ supportedsoundfileextensions[i].toUpperCase();
			supportedsoundfileextensions[l - 1] = Constants.SupportedSoundFileExtensions[l - 1];
			dialog.setFilterExtensions(supportedsoundfileextensions);
			dialog.setFilterNames(Constants.SupportedSoundFileNames);
			String soundFile = settings.get(SOUNDFILE);
			if (soundFile != null)
				dialog.setFileName(soundFile);
			String file = dialog.open();
			if (file != null) {
				settings.put(SOUNDFILE, file);
				outputFile = new File(file);
				targetUri = sourceUri = outputFile.toURI().toString();
				dirty = true;
				updateButtons();
			}
			return;
		case TEXT:
			dialog.setFilterExtensions(new String[] { "*.txt;*.TXT" }); //$NON-NLS-1$
			dialog.setFilterNames(new String[] { Messages.VoiceNoteDialog_textfiles });
			String textFile = settings.get(TEXTFILE);
			if (textFile != null)
				dialog.setFileName(textFile);
			file = dialog.open();
			if (file != null) {
				settings.put(TEXTFILE, file);
				try {
					String s = readTextfile(file);
					int ret = 1;
					if (!note.getText().isEmpty()) {
						AcousticMessageDialog mdialog = new AcousticMessageDialog(getShell(),
								Messages.VoiceNoteDialog_overwrite_text, null, Messages.VoiceNoteDialog_text_exists,
								MessageDialog.QUESTION_WITH_CANCEL, new String[] { Messages.VoiceNoteDialog_append,
										Messages.VoiceNoteDialog_overwrite, IDialogConstants.CANCEL_LABEL },
								0);
						ret = mdialog.open();
						if (ret == 2)
							return;
					}
					note.setText(ret == 1 ? s : note.getText() + '\n' + s);
					dirty = true;
					updateButtons();
				} catch (IOException e1) {
					note.setText(Messages.VoiceNoteDialog_io_error);
				}
			}
			return;
		case DRAWING:
			dialog.setFilterExtensions(new String[] { "*.zdrw" }); //$NON-NLS-1$
			dialog.setFilterNames(new String[] { NLS.bind(Messages.VoiceNoteDialog_zdrw, Constants.APPNAME) });
			textFile = settings.get(SVGFILE);
			if (textFile != null)
				dialog.setFileName(textFile);
			file = dialog.open();
			if (file != null) {
				settings.put(SVGFILE, file);
				try {
					String s = readTextfile(file);
					int ret = 1;
					if (paintExample.hasVectorFigure()) {
						AcousticMessageDialog mdialog = new AcousticMessageDialog(getShell(),
								Messages.VoiceNoteDialog_overwrite_drawing, null,
								Messages.VoiceNoteDialog_drawing_exists, MessageDialog.QUESTION_WITH_CANCEL,
								new String[] { Messages.VoiceNoteDialog_append, Messages.VoiceNoteDialog_overwrite,
										IDialogConstants.CANCEL_LABEL },
								0);
						ret = mdialog.open();
						if (ret == 2)
							return;
					}
					paintExample.importSvg(s, ret == 1);
					dirty = true;
					updateButtons();
				} catch (IOException e1) {
					AcousticMessageDialog.openError(getShell(), Messages.VoiceNoteDialog_error_reading_drawing,
							NLS.bind(Messages.VoiceNoteDialog_io_error_reading_drawing, e1));
				} catch (ParserConfigurationException | SAXException e) {
					AcousticMessageDialog.openError(getShell(), Messages.VoiceNoteDialog_error_reading_drawing,
							NLS.bind(Messages.VoiceNoteDialog_invalid_drawing, e));
				}
			}
		}
	}

	private static String readTextfile(String path) throws IOException {
		File inFile = new File(path);
		try (BufferedReader reader = new BufferedReader(new FileReader(inFile))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while (true) {
				line = reader.readLine();
				if (line == null)
					break;
				sb.append(line).append('\n');
			}
			return sb.toString();
		}
	}

	private void record() throws LineUnavailableException {
		try {
			outputFile = ImageActivator.getDefault().createTempFile("Voice", ".wav"); //$NON-NLS-1$ //$NON-NLS-2$
			voiceFileUri = outputFile.toURI();
			sourceUri = voiceFileUri.toString();
			targetUri = "."; //$NON-NLS-1$
			outputFile.delete();
		} catch (IOException e) {
			return;
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
		dirty = true;
		updateButtons();
	}

	public String getSourceUri() {
		return sourceUri;
	}

	public String getTargetUri() {
		return targetUri;
	}

	public String getNoteText() {
		return noteText;
	}

	public String getSvg() {
		return svg;
	}

	@Override
	protected String getId() {
		return VOICE_NOTE_DIALOG; // $NON-NLS-1$
	}

	@Override
	protected Point getDefaultSize() {
		return new Point(500, 300);
	}

	@Override
	public boolean close() {
		boolean closed = super.close();
		if (closed) {
			if (fNewBounds != null)
				saveBounds(fSettings, VOICE_BOUNDS, fNewBounds);
			if (fDrawingSize != null) {
				IDialogSettings dialogBounds = settings.getSection(DRAWING_BOUNDS);
				if (dialogBounds == null)
					settings.addSection(dialogBounds = new DialogSettings(DRAWING_BOUNDS));
				dialogBounds.put(WIDTH, fDrawingSize.x);
				dialogBounds.put(HEIGHT, fDrawingSize.y);
			}
			UiActivator.getDefault().registerKiosk(null, bounds);
		}
		return closed;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setBounds(Rectangle bounds) {
		// not used
	}

	@Override
	public boolean isDisposed() {
		Shell shell = getShell();
		return shell == null || shell.isDisposed();
	}

	@Override
	public void addStateListener(IStateListener listener) {
		// do nothing
	}

	@Override
	public void removeStateListener(IStateListener listener) {
		// do nothing
	}

}
