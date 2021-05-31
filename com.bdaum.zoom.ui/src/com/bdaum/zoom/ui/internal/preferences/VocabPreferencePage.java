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
 * (c) 2011-2021 Berthold Daum  
 */
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
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.codes.CodeParser;
import com.bdaum.zoom.ui.internal.dialogs.TopicContentProvider;
import com.bdaum.zoom.ui.internal.dialogs.TopicLabelProvider;
import com.bdaum.zoom.ui.internal.views.ZColumnViewerToolTipSupport;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class VocabPreferencePage extends AbstractPreferencePage {

	public static class VocabPage extends Composite implements Listener {

		private CheckedText textField;
		private Button clearButton;
		private Button loadButton;
		private Button saveButton;
		private Button sortButton;
		private ColumnViewer[] topicViewer = new ColumnViewer[3];

		@SuppressWarnings("unused")
		public VocabPage(CTabFolder tabFolder, int style, QueryField qfield) {
			super(tabFolder, style);
			int type = (Integer) qfield.getEnumeration();
			setLayout(new GridLayout(2, false));
			SashForm sashform = new SashForm(this, SWT.HORIZONTAL);
			Composite tcomp = new Composite(sashform, SWT.NONE);
			tcomp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			GridLayout layout = new GridLayout(1, false);
			layout.marginHeight = layout.marginWidth = 0;
			tcomp.setLayout(layout);
			Label label = new Label(tcomp, SWT.NONE);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			label.setText(type == QueryField.GENRECODES ? Messages.getString("VocabPreferencePage.each_word") //$NON-NLS-1$
					: Messages.getString("VocabPreferencePage.each_code")); //$NON-NLS-1$
			textField = new CheckedText(tcomp, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
			textField.setSpellingOptions(8, qfield.getSpellingOptions());
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
			layoutData.heightHint = 350;
			textField.setLayoutData(layoutData);
			textField.addListener(SWT.Modify, this);
			Composite vcomp = new Composite(sashform, SWT.NONE);
			vcomp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			layout = new GridLayout(1, false);
			layout.marginHeight = layout.marginWidth = 0;
			vcomp.setLayout(layout);
			createTopicViewer(vcomp, type);
			sashform.setWeights(new int[] { 1, 2 });
			Composite buttonArea = new Composite(this, SWT.NONE);
			buttonArea.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
			buttonArea.setLayout(new GridLayout());
			new Label(buttonArea, SWT.NONE);
			clearButton = new Button(buttonArea, SWT.PUSH);
			clearButton.setText(Messages.getString("VocabPreferencePage.clear")); //$NON-NLS-1$
			clearButton.addListener(SWT.Selection, this);
			sortButton = new Button(buttonArea, SWT.PUSH);
			sortButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			sortButton.setText(Messages.getString("VocabPreferencePage.sort")); //$NON-NLS-1$
			sortButton.addListener(SWT.Selection, this);
			new Label(buttonArea, SWT.SEPARATOR | SWT.HORIZONTAL);
			loadButton = new Button(buttonArea, SWT.PUSH);
			loadButton.setText(Messages.getString("VocabPreferencePage.load")); //$NON-NLS-1$
			loadButton.addListener(SWT.Selection, this);
			saveButton = new Button(buttonArea, SWT.PUSH);
			saveButton.setText(Messages.getString("VocabPreferencePage.save")); //$NON-NLS-1$
			saveButton.addListener(SWT.Selection, this);
			updateTabButtons();
		}

		private void createTopicViewer(Composite composite, int type) {
			CodeParser codeParser = UiActivator.getDefault().getCodeParser(type);
			ExpandCollapseGroup expandCollapseGroup = null;
			if (codeParser.hasSubtopics()) {
				Composite ecomp = new Composite(composite, SWT.NONE);
				ecomp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				GridLayout layout = new GridLayout(2, false);
				layout.marginHeight = layout.marginWidth = 0;
				ecomp.setLayout(layout);
				Label label = new Label(ecomp, SWT.NONE);
				label.setText(Messages.getString("VocabPreferencePage.predfined")); //$NON-NLS-1$
				expandCollapseGroup = new ExpandCollapseGroup(ecomp, SWT.NONE);
				TreeViewer treeViewer = new TreeViewer(composite,
						SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
				expandCollapseGroup.setViewer((TreeViewer) topicViewer[type]);
				treeViewer.getTree().setLinesVisible(true);
				treeViewer.getTree().setHeaderVisible(true);
				if (!codeParser.isByName())
					createTreeColumn(treeViewer, 65, Messages.getString("VocabPreferencePage.code"), 0); //$NON-NLS-1$
				createTreeColumn(treeViewer, 130, Messages.getString("VocabPreferencePage.name"), 1); //$NON-NLS-1$
				createTreeColumn(treeViewer, codeParser.isByName() ? 300 : 250, Messages.getString("VocabPreferencePage.expl"), 2); //$NON-NLS-1$
				UiUtilities.installDoubleClickExpansion(treeViewer);
				topicViewer[type] = treeViewer;
			} else {
				Label label = new Label(composite, SWT.NONE);
				label.setText(Messages.getString("VocabPreferencePage.predfined")); //$NON-NLS-1$
				TableViewer tableViewer = new TableViewer(composite,
						SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
				tableViewer.getTable().setLinesVisible(true);
				tableViewer.getTable().setHeaderVisible(true);
				if (!codeParser.isByName())
					createTableColumn(tableViewer, 65, Messages.getString("VocabPreferencePage.code"), 0); //$NON-NLS-1$
				createTableColumn(tableViewer, 130, Messages.getString("VocabPreferencePage.name"), 1); //$NON-NLS-1$
				createTableColumn(tableViewer, codeParser.isByName() ? 300 : 250, Messages.getString("VocabPreferencePage.expl"), 2); //$NON-NLS-1$
				topicViewer[type] = tableViewer;
			}
			GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
			layoutData.heightHint = 350;
			topicViewer[type].getControl().setLayoutData(layoutData);
			topicViewer[type].setContentProvider(new TopicContentProvider());
			topicViewer[type].setComparator(ZViewerComparator.INSTANCE);
			ZColumnViewerToolTipSupport.enableFor(topicViewer[type]);
			topicViewer[type].setInput(codeParser.loadCodes());
		}

		private static void createTreeColumn(TreeViewer treeViewer, int width, String text, int index) {
			TreeViewerColumn col = new TreeViewerColumn(treeViewer, SWT.NONE);
			col.getColumn().setWidth(width);
			col.getColumn().setText(text);
			col.setLabelProvider(new TopicLabelProvider(index));
		}

		private static void createTableColumn(TableViewer tableViewer, int width, String text, int index) {
			TableViewerColumn col = new TableViewerColumn(tableViewer, SWT.NONE);
			col.getColumn().setWidth(width);
			col.getColumn().setText(text);
			col.setLabelProvider(new TopicLabelProvider(index));
		}

		@Override
		public void handleEvent(Event e) {
			switch (e.type) {
			case SWT.Modify:
				updateTabButtons();
				return;
			case SWT.Selection:
				if (e.widget == clearButton)
					textField.setText(""); //$NON-NLS-1$
				else if (e.widget == sortButton) {
					Set<String> set = new HashSet<>(Core.fromStringList(textField.getText(), "\n")); //$NON-NLS-1$
					String[] words = set.toArray(new String[set.size()]);
					Arrays.parallelSort(words, Utilities.KEYWORDCOMPARATOR);
					textField.setText(Core.toStringList(words, "\n")); //$NON-NLS-1$
				} else if (e.widget == loadButton) {
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
				} else if (e.widget == saveButton) {
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
			}
		}

		protected FileDialog createFileDialog(int style) {
			FileDialog dialog = new FileDialog(getShell(), style);
			dialog.setFilterExtensions(VOCABEXTENSIONS);
			dialog.setFilterNames(new String[] {
					Constants.APPNAME + Messages.getString("VocabPreferencePage.vocab_file") //$NON-NLS-1$
							+ Constants.VOCABFILEEXTENSION + ')',
					Messages.getString("VocabPreferencePage.all_files") }); //$NON-NLS-1$
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
