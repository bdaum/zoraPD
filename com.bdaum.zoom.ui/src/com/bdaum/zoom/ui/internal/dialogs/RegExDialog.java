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
 * (c) 2016 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;

import com.bdaum.zoom.cat.model.MigrationRule;
import com.bdaum.zoom.cat.model.MigrationRuleImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.VolumeManager;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class RegExDialog extends ZTitleAreaDialog {

	/** The size of the dialogs search history. */
	private static final int HISTORY_SIZE = 5;

	private static final String CONTENT_ASSIST_PROPOSALS = "org.eclipse.ui.edit.text.contentAssist.proposals"; //$NON-NLS-1$
	private static final String SETTINGSID = "com.bdaum.zoom.regexDialog"; //$NON-NLS-1$
	private static final String SOURCEHISTORY = "sourceHistory"; //$NON-NLS-1$
	private static final String TARGETHISTORY = "targetHistory"; //$NON-NLS-1$
	private static final String VOLUMEHISTORY = "volumneHistory"; //$NON-NLS-1$
	private static final String TESTPATH = "testPath"; //$NON-NLS-1$

	private String sourcePattern;
	private String targetPattern;
	private boolean targetOnly;
	private Combo fSourceField;
	private Combo fTargetField;
	private List<String> fSourceHistory;
	private List<String> fTargetHistory;
	private List<String> volumeHistory;

	private String source;

	private String target;

	private IDialogSettings dialogSettings;

	private Label targetPathLabel;

	private Label sourcePathLabel;

	private String testPath;

	private String defPath;

	private int fileSeparatorPolicy;

	private int selection;

	private Label targetVolumeLabel;

	private Combo volumeField;

	private String volume;

	private boolean winTarget;

	public RegExDialog(Shell parentShell, MigrationRule rule, String defPath,
			boolean targetOnly, int fileSeparatorPolicy, int selection) {
		super(parentShell, HelpContextIds.REGEX_DIALOG);
		this.defPath = defPath;
		this.targetOnly = targetOnly;
		this.fileSeparatorPolicy = fileSeparatorPolicy;
		this.selection = selection;
		dialogSettings = getDialogSettings(UiActivator.getDefault(), SETTINGSID);
		String history = dialogSettings.get(TARGETHISTORY);
		fTargetHistory = Core.fromStringList(history, "\n"); //$NON-NLS-1$
		history = dialogSettings.get(VOLUMEHISTORY);
		volumeHistory = Core.fromStringList(history, "\n"); //$NON-NLS-1$
		if (!targetOnly) {
			history = dialogSettings.get(SOURCEHISTORY);
			fSourceHistory = Core.fromStringList(history, "\n"); //$NON-NLS-1$
		}
		if (defPath == null)
			testPath = dialogSettings.get(TESTPATH);
		if (rule != null) {
			sourcePattern = rule.getSourcePattern();
			targetPattern = rule.getTargetPattern();
			volume = rule.getTargetVolume();
		}
		switch (fileSeparatorPolicy) {
		case 1:
			winTarget = false;
			break;
		case 2:
			winTarget = true;
			break;
		default:
			winTarget = Constants.WIN32;
			break;
		}

	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Constants.APPNAME);
		setTitle(Messages.RegExDialog_edit_patterns);
		setMessage(Messages.RegExDialog_define_source_and_target);
		updateCombo(sourcePattern, fSourceField, fSourceHistory);
		updateCombo(targetPattern, fTargetField, fTargetHistory);
		if (volumeField != null)
			updateCombo(volume, volumeField, volumeHistory);
		if (selection > 0) {
			fTargetField.addFocusListener(new FocusListener() {
				public void focusLost(FocusEvent e) {
					fTargetField.removeFocusListener(this);
				}

				public void focusGained(FocusEvent e) {
					fTargetField.setSelection(new Point(0, selection));
				}
			});
		}
		if (testPath != null)
			sourcePathLabel.setText(testPath);
		validate();
		evaluateTargetPath();
	}

	private static void updateCombo(String text, Combo combo, List<String> items) {
		if (items != null)
			combo.setItems(items.toArray(new String[items.size()]));
		if (text != null)
			combo.setText(text);
		else if (items != null && !items.isEmpty())
			combo.setText(items.get(0));
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
				evaluateTargetPath();
			}
		};
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE)
				.setText(Messages.RegExDialog_source_pattern);
		// Create the source content assist field
		ComboContentAdapter contentAdapter = new ComboContentAdapter();
		fSourceField = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
		fSourceField.setEnabled(!targetOnly);
		fSourceField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		if (!targetOnly) {
			FindReplaceDocumentAdapterContentProposalProvider findProposer = new FindReplaceDocumentAdapterContentProposalProvider(
					true, false);
			new ContentAssistCommandAdapter(fSourceField, contentAdapter,
					findProposer, CONTENT_ASSIST_PROPOSALS, new char[0], true);
			fSourceField.addModifyListener(listener);
		}
		new Label(composite, SWT.NONE)
				.setText(Messages.RegExDialog_target_pattern);
		// Create the target content assist field
		FindReplaceDocumentAdapterContentProposalProvider replaceProposer = new FindReplaceDocumentAdapterContentProposalProvider(
				false, false);
		fTargetField = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
		new ContentAssistCommandAdapter(fTargetField, contentAdapter,
				replaceProposer, CONTENT_ASSIST_PROPOSALS, new char[0], true);
		fTargetField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		fTargetField.addModifyListener(listener);
		if (winTarget) {
			new Label(composite, SWT.NONE)
					.setText(Messages.RegExDialog_target_volume_name);
			volumeField = new Combo(composite, SWT.DROP_DOWN | SWT.BORDER);
			volumeField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					false));
			volumeField.addModifyListener(listener);
		}
		// Test area
		CGroup testGroup = new CGroup(composite, SWT.NONE);
		testGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		testGroup.setLayout(new GridLayout(3, false));
		testGroup.setText(Messages.RegExDialog_test_area);
		new Label(testGroup, SWT.NONE)
				.setText(Messages.RegExDialog_source_path);
		sourcePathLabel = new Label(testGroup, SWT.NONE);
		sourcePathLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		Button testButton = new Button(testGroup, SWT.PUSH);
		testButton.setText(Messages.RegExDialog_browse);
		testButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
				dialog.setText(Messages.RegExDialog_select_source_file);
				if (testPath != null)
					dialog.setFilterPath(testPath);
				else if (defPath != null)
					dialog.setFilterPath(defPath);
				String path = dialog.open();
				if (path != null) {
					testPath = path;
					if (Constants.WIN32) {
						int i = path.indexOf(':');
						if (i == 1) {
							String volume = Core.getCore().getVolumeManager()
									.getVolumeForFile(new File(path));
							if (volume != null)
								testPath = volume + path.substring(1);
						}
					}
					sourcePathLabel.setText(testPath);
					evaluateTargetPath();
				}
			}
		});
		new Label(testGroup, SWT.NONE)
				.setText(Messages.RegExDialog_target_path);
		targetPathLabel = new Label(testGroup, SWT.NONE);
		targetPathLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1));
		new Label(testGroup, SWT.NONE)
				.setText(Messages.RegExDialog_target_volume);
		targetVolumeLabel = new Label(testGroup, SWT.NONE);
		targetVolumeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 2, 1));
		new Label(composite, SWT.NONE);
		return area;
	}

	protected void evaluateTargetPath() {
		String source = fSourceField.getText();
		String target = fTargetField.getText();
		String volume = fTargetField.isEnabled() ? fTargetField.getText()
				: null;
		if (source.length() > 0) {
			if (testPath != null && testPath.length() > 0) {
				try {
					int flags = Constants.WIN32 ? Pattern.CASE_INSENSITIVE : 0;
					Pattern pattern = Pattern.compile(source, flags);
					Matcher matcher = pattern.matcher(testPath);
					if (matcher.matches()) {
						if (target.length() > 0) {
							StringBuffer sb = new StringBuffer();
							matcher.appendReplacement(sb, target);
							String result = sb.toString();
							switch (fileSeparatorPolicy) {
							case 1:
								result = result.replace('\\', '/');
								break;
							case 2:
								result = result.replace('/', '\\');
								break;
							}
							targetPathLabel.setText(result);
							targetVolumeLabel.setText(volume != null ? volume
									: extractVolume(result));

						} else {
							targetPathLabel
									.setText(Messages.RegExDialog_test_file_not_migrated);
							targetVolumeLabel.setText(""); //$NON-NLS-1$
						}
					} else {
						targetPathLabel
								.setText(Messages.RegExDialog_source_pattern_does_not_match);
						targetVolumeLabel.setText(""); //$NON-NLS-1$
					}
				} catch (PatternSyntaxException e) {
					targetPathLabel.setText(Messages.RegExDialog_syntax_error);
					targetVolumeLabel.setText(""); //$NON-NLS-1$
				}
			} else {
				targetPathLabel
						.setText(Messages.RegExDialog_test_file_undefined);
				targetVolumeLabel.setText(""); //$NON-NLS-1$
			}
		} else {
			targetPathLabel.setText(Messages.RegExDialog_undefined);
			targetVolumeLabel.setText(""); //$NON-NLS-1$
		}
	}

	private static String extractVolume(String migratedPath) {
		if (migratedPath == null)
			return null;
		int i = migratedPath.indexOf(':');
		if (i >= 0)
			return (i < 2) ? null : migratedPath.substring(0, i);
		if (migratedPath.startsWith("/")) //$NON-NLS-1$
			return ((VolumeManager) Core.getCore().getVolumeManager())
					.extractLinuxPath(migratedPath);
		return null;
	}

	protected void validate() {
		String errormessage = null;
		if (volumeField != null && volumeField.getText().length() == 0)
			errormessage = Messages.RegExDialog_specify_target_volume;
		if (!targetOnly) {
			if (fSourceField.getText().length() == 0)
				errormessage = Messages.RegExDialog_specify_source_pattern;
			else {
				try {
					Pattern.compile(fSourceField.getText());
				} catch (PatternSyntaxException e) {
					errormessage = e.getLocalizedMessage();
				}
			}
		}
		setErrorMessage(errormessage);
		getButton(OK).setEnabled(errormessage == null);
	}

	@Override
	protected void okPressed() {
		source = fSourceField.getText();
		if (!targetOnly) {
			updateHistory(source, fSourceHistory);
			dialogSettings.put(SOURCEHISTORY,
					Core.toStringList(fSourceHistory, '\n'));
		}
		target = fTargetField.getText();
		updateHistory(target, fTargetHistory);
		dialogSettings.put(TARGETHISTORY,
				Core.toStringList(fTargetHistory, '\n'));
		if (volumeField != null) {
			volume = volumeField.getText();
			updateHistory(volume, volumeHistory);
			dialogSettings.put(VOLUMEHISTORY,
					Core.toStringList(volumeHistory, '\n'));
		}
		if (testPath != null && testPath.length() > 0)
			dialogSettings.put(TESTPATH, testPath);
		super.okPressed();
	}

	private static void updateHistory(String s, List<String> history) {
		if (s == null || s.length() == 0)
			return;
		int index = history.indexOf(s);
		if (index != 0) {
			if (index != -1)
				history.remove(index);
			history.add(0, s);
			if (history.size() > HISTORY_SIZE)
				history.remove(HISTORY_SIZE);
		}
	}

	public MigrationRule getResult() {
		return new MigrationRuleImpl(source, target, volume);
	}
}
