package com.bdaum.zoom.ui.internal.preferences;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class VocabPreferencePage extends AbstractPreferencePage {

	public class VocabPage extends Composite {

		private CheckedText textField;
		private Button clearButton;
		private Button loadButton;
		private Button saveButton;
		private Button sortButton;

		@SuppressWarnings("unused")
		public VocabPage(CTabFolder tabFolder, int style, QueryField qfield) {
			super(tabFolder, style);
			setLayout(new GridLayout(2, false));
			textField = new CheckedText(this, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
			textField.setSpellingOptions(8, qfield.getSpellingOptions());
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
			layoutData.heightHint = 300;
			textField.setLayoutData(layoutData);
			textField.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					updateTabButtons();
				}
			});
			Composite buttonArea = new Composite(this, SWT.NONE);
			buttonArea.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
			buttonArea.setLayout(new GridLayout());
			clearButton = new Button(buttonArea, SWT.PUSH);
			clearButton.setText(Messages.getString("VocabPreferencePage.clear")); //$NON-NLS-1$
			clearButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					textField.setText(""); //$NON-NLS-1$
				}
			});
			sortButton = new Button(buttonArea, SWT.PUSH);
			sortButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			sortButton.setText(Messages.getString("VocabPreferencePage.sort")); //$NON-NLS-1$
			sortButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Set<String> set = new HashSet<>(Core.fromStringList(textField.getText(), "\n")); //$NON-NLS-1$
					String[] words = set.toArray(new String[set.size()]);
					Arrays.sort(words, Utilities.KEYWORDCOMPARATOR);
					textField.setText(Core.toStringList(words, "\n")); //$NON-NLS-1$
				}
			});
			new Label(buttonArea, SWT.SEPARATOR | SWT.HORIZONTAL);

			loadButton = new Button(buttonArea, SWT.PUSH);
			loadButton.setText(Messages.getString("VocabPreferencePage.load")); //$NON-NLS-1$
			loadButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean clear = false;
					if (!textField.getText().isEmpty())
						clear = AcousticMessageDialog.openQuestion(getShell(),
								Messages.getString("VocabPreferencePage.load_vocab"), //$NON-NLS-1$
								Messages.getString("VocabPreferencePage.clear_text")); //$NON-NLS-1$
					FileDialog dialog = createFileDialog(SWT.OPEN);
					String filename = dialog.open();
					if (filename != null) {
						try (InputStream in = new BufferedInputStream(new FileInputStream(filename))) {
							StringBuilder sb = new StringBuilder(512);
							if (!clear)
								sb.append(textField.getText());
							try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
								String line;
								while ((line = r.readLine()) != null) {
									if (sb.length() > 0)
										sb.append('\n');
									sb.append(line.trim());
								}
								textField.setText(sb.toString());
							}
						} catch (IOException ex) {
							UiActivator.getDefault().logError(
									NLS.bind(Messages.getString("VocabPreferencePage.error_loading"), filename), ex); //$NON-NLS-1$
						}
					}
				}
			});
			saveButton = new Button(buttonArea, SWT.PUSH);
			saveButton.setText(Messages.getString("VocabPreferencePage.save")); //$NON-NLS-1$
			saveButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = createFileDialog(SWT.SAVE);
					dialog.setFileName("*" + Constants.VOCABFILEEXTENSION); //$NON-NLS-1$
					dialog.setOverwrite(true);
					String filename = dialog.open();
					if (filename != null)
						try {
							File file = new File(filename);
							file.delete();
							file.createNewFile();
							try (BufferedWriter w = new BufferedWriter(
									new OutputStreamWriter(new FileOutputStream(file), "utf-8"))) { //$NON-NLS-1$
								w.write(textField.getText());
							}
						} catch (IOException ex) {
							UiActivator.getDefault().logError(
									NLS.bind(Messages.getString("VocabPreferencePage.error_writing"), filename), ex); //$NON-NLS-1$
						}
				}
			});
			updateTabButtons();
		}

		protected FileDialog createFileDialog(int style) {
			FileDialog dialog = new FileDialog(getShell(), style);
			dialog.setFilterExtensions(VOCABEXTENSIONS);
			dialog.setFilterNames(new String[] {
					Constants.APPNAME + Messages.getString("VocabPreferencePage.vocab_file") //$NON-NLS-1$
							+ Constants.VOCABFILEEXTENSION + ')', Messages.getString("VocabPreferencePage.all_files") }); //$NON-NLS-1$
			return dialog;
		}

		private void updateTabButtons() {
			boolean enabled = !textField.getText().isEmpty();
			saveButton.setEnabled(enabled);
			sortButton.setEnabled(enabled);
		}

		public void setText(String text) {
			textField.setText(text);
		}

		public String getText() {
			return textField.getText();
		}

	}

	private static final String[] VOCABEXTENSIONS = new String[] { "*" //$NON-NLS-1$
			+ Constants.VOCABFILEEXTENSION + ";*" //$NON-NLS-1$
			+ Constants.VOCABFILEEXTENSION.toUpperCase(), "*.*" }; //$NON-NLS-1$

	private VocabPage genreTab;
	private VocabPage sceneTab;
	private VocabPage subjectTab;

	public VocabPreferencePage() {
		super();
	}

	public VocabPreferencePage(String title) {
		super(title);
	}

	public VocabPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@SuppressWarnings("unused")
	@Override
	protected void createPageContents(Composite comp) {
		setHelp(HelpContextIds.VOCAB_PREFERENCE_PAGE);
		noDefaultAndApplyButton();
		new Label(comp, SWT.WRAP).setText(Messages.getString("VocabPreferencePage.new_line")); //$NON-NLS-1$
		new Label(comp, SWT.NONE);
		createTabFolder(comp, "Vocabularies"); //$NON-NLS-1$
		genreTab = createTab(QueryField.IPTC_INTELLECTUAL_GENRE);
		sceneTab = createTab(QueryField.IPTC_SCENECODE);
		subjectTab = createTab(QueryField.IPTC_SUBJECTCODE);
		fillValues();
		initTabFolder(0);
	}

	private VocabPage createTab(QueryField qfield) {
		final VocabPage vocabTab = new VocabPage(tabFolder, SWT.NONE, qfield);
		UiUtilities.createTabItem(tabFolder, qfield.getLabel(), null).setControl(vocabTab);
		return vocabTab;
	}

	@Override
	public void init(IWorkbench wb) {
		super.init(wb);
		setTitle(Messages.getString("VocabPreferencePage.user_defined")); //$NON-NLS-1$
		setMessage(Messages.getString("VocabPreferencePage.edit")); //$NON-NLS-1$
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return UiActivator.getDefault().getPreferenceStore();
	}

	@Override
	protected void doFillValues() {
		fillValues(genreTab, PreferenceConstants.V_GENRE);
		fillValues(sceneTab, PreferenceConstants.V_SCENE);
		fillValues(subjectTab, PreferenceConstants.V_SUBJECT);
		super.doFillValues();
	}

	private void fillValues(VocabPage tab, String id) {
		String text = getPreferenceStore().getString(id);
		if (text != null)
			tab.setText(text);
	}

	@Override
	protected void doPerformOk() {
		saveValues(genreTab, PreferenceConstants.V_GENRE);
		saveValues(sceneTab, PreferenceConstants.V_SCENE);
		saveValues(subjectTab, PreferenceConstants.V_SUBJECT);
	}

	private void saveValues(VocabPage tab, String id) {
		getPreferenceStore().putValue(id, tab.getText());
	}

}
