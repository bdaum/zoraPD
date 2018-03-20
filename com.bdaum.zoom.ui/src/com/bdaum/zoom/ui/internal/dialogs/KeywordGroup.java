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
 * (c) 2009-2017 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ISpellCheckingService;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.fileMonitor.internal.filefilter.WildCardFilter;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.ZViewerComparator;
import com.bdaum.zoom.ui.internal.VocabManager;
import com.bdaum.zoom.ui.internal.operations.ModifyMetaOperation;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.CheckedText;
import com.bdaum.zoom.ui.internal.widgets.ExpandCollapseGroup;
import com.bdaum.zoom.ui.internal.widgets.FilterField;
import com.bdaum.zoom.ui.internal.widgets.FlatGroup;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class KeywordGroup implements IAdaptable {

	private static final Object[] EMPTY = new Object[0];

	public class KeywordContentProvider implements ITreeContentProvider {

		private Map<Character, List<String>> chapters;
		private String[] applieds;
		private String[] availables;
		private boolean isFlat;

		public void dispose() {
			chapters = null;
			applieds = null;
			availables = null;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			dispose();
			Arrays.sort(applieds = appliedKeywords.toArray(new String[appliedKeywords.size()]),
					UiUtilities.stringComparator);
			isFlat = radioGroup.isFlat();
		}

		public Object[] getElements(Object inputElement) {
			return (String[]) inputElement;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement == APPLIED)
				return applieds;
			if (parentElement == AVAILABLE) {
				if (isFlat)
					return getAvailables();
				return getChapters().keySet().toArray();
			} else if (parentElement instanceof Character) {
				List<String> elements = chapters.get(parentElement);
				if (elements != null)
					return elements.toArray();
			}
			return EMPTY;
		}

		public Object getParent(Object element) {
			if (element == AVAILABLE || element == APPLIED)
				return null;
			if (element instanceof Character)
				return AVAILABLE;
			if (Arrays.binarySearch(applieds, element) >= 0)
				return APPLIED;
			if (isFlat)
				return AVAILABLE;
			return Character.toUpperCase(((String) element).charAt(0));
		}

		public boolean hasChildren(Object parentElement) {
			if (parentElement == APPLIED)
				return !appliedKeywords.isEmpty();
			if (parentElement == AVAILABLE) {
				if (isFlat)
					return getAvailables().length > 0;
				return !getChapters().keySet().isEmpty();
			} else if (parentElement instanceof Character) {
				List<String> elements = getChapters().get(parentElement);
				if (elements != null)
					return !elements.isEmpty();
			}
			return false;
		}

		protected Map<Character, List<String>> getChapters() {
			if (chapters == null) {
				chapters = new HashMap<Character, List<String>>(60);
				for (String kw : getAvailables())
					if (!kw.isEmpty()) {
						Character chapterTitle = Character.toUpperCase(kw.charAt(0));
						List<String> elements = chapters.get(chapterTitle);
						if (elements == null)
							chapters.put(chapterTitle, elements = new ArrayList<String>());
						elements.add(kw);
					}
			}
			return chapters;
		}

		protected String[] getAvailables() {
			if (availables == null)
				Arrays.sort(availables = filterKeywords(availableKeywords), UiUtilities.stringComparator);
			return availables;
		}

	}

	private static final int LIMIT = 8;
	private static final String SETTINGSID = "com.bdaum.zoom.keyGroup"; //$NON-NLS-1$
	private static final String AVAILABLE = Messages.KeywordGroup_available;
	private static final String APPLIED = Messages.KeywordGroup_applied;
	private static final String[] ROOT = new String[] { APPLIED, AVAILABLE };
	private CheckboxTreeViewer viewer;
	private CheckedText newKeywordField;
	private List<String> recentKeywords;
	private CheckboxTableViewer recentViewer;
	private Button importButton;
	private Set<String> externalKeywords;
	private Meta meta;
	public Set<String> appliedKeywords = new HashSet<String>(17);
	public Set<String> availableKeywords;
	private final String[] selectedKeywords;
	private BagChange<String> result;
	private FlatGroup radioGroup;
	private KeywordVerifyListener keywordVerifyListener;
	private boolean excludeGeographic;
	private boolean tags;
	private Set<String> geographic;
	private VocabManager vocabManager;
	private CLabel vocabLabel;
	private String synonym;
	private String replacement;

	public KeywordGroup(Composite area, String[] selectedKeywords, Set<String> predefinedKeywords,
			List<String> recentKeywords, boolean tags) {
		this.selectedKeywords = selectedKeywords;
		this.recentKeywords = recentKeywords;
		this.tags = tags;
		if (tags) {
			meta = Core.getCore().getDbManager().getMeta(true);
			externalKeywords = new HashSet<String>(predefinedKeywords);
			externalKeywords.removeAll(meta.getKeywords());
		} else
			vocabManager = new VocabManager(Core.getCore().getDbManager().getMeta(true).getVocabularies(), this);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		if (recentKeywords != null) {
			Set<String> recentSet = new HashSet<String>(recentKeywords);
			recentSet.retainAll(predefinedKeywords);
			if (!recentSet.isEmpty()) {
				CGroup recentGroup = new CGroup(composite, SWT.NONE);
				recentGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				recentGroup.setText(tags ? Messages.KeywordGroup_recent_tags : Messages.KeywordGroup_recent_keywords);
				recentGroup.setLayout(new GridLayout());
				recentViewer = CheckboxTableViewer.newCheckList(recentGroup, SWT.NO_SCROLL | SWT.V_SCROLL);
				GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
				layoutData.heightHint = 100;
				recentViewer.getControl().setLayoutData(layoutData);
				recentViewer.setContentProvider(ArrayContentProvider.getInstance());
				recentViewer.setComparator(ZViewerComparator.INSTANCE);
				recentViewer.setLabelProvider(new KeywordLabelProvider(vocabManager, null));
				ColumnViewerToolTipSupport.enableFor(recentViewer);
				recentViewer.setInput(recentSet);
				if (selectedKeywords != null)
					for (String kw : selectedKeywords)
						if (kw != null && recentKeywords.contains(kw))
							recentViewer.setChecked(kw, true);
				recentViewer.addCheckStateListener(new ICheckStateListener() {
					public void checkStateChanged(CheckStateChangedEvent event) {
						String element = (String) event.getElement();
						boolean checked = event.getChecked();
						if (checked) {
							appliedKeywords.add(element);
							availableKeywords.remove(element);
						} else {
							appliedKeywords.remove(element);
							availableKeywords.add(element);
						}
						updateKeywordViewer(element);
					}
				});
			}
		}
		CGroup keywordsGroup = new CGroup(composite, SWT.NONE);
		keywordsGroup.setText(tags ? Messages.KeywordGroup_all_tags : Messages.KeywordGroup_all_keywords);
		keywordsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		keywordsGroup.setLayout(new GridLayout(3, false));
		radioGroup = new FlatGroup(keywordsGroup, SWT.NONE, UiActivator.getDefault().getDialogSettings(SETTINGSID),
				"hierarchicalKeywords"); //$NON-NLS-1$
		radioGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1));
		radioGroup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateKeywordViewer(((IStructuredSelection) viewer.getSelection()).getFirstElement());
			}
		});
		ExpandCollapseGroup expandCollapseGroup = new ExpandCollapseGroup(keywordsGroup, SWT.NONE);
		final FilterField filterField = new FilterField(keywordsGroup);
		GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, true, false, tags ? 3 : 2, 1);
		layoutData.widthHint = 300;
		filterField.setLayoutData(layoutData);
		filterField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				viewer.setInput(viewer.getInput());
				viewer.expandAll();
			}
		});
		if (!tags) {
			final CheckboxButton excludeButton = WidgetFactory.createCheckButton(keywordsGroup,
					Messages.KeywordGroup_exclude_geographic, new GridData(SWT.END, SWT.CENTER, true, false));
			excludeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					excludeGeographic = excludeButton.getSelection();
					updateKeywordViewer(null);
				}
			});
		}
		viewer = new CheckboxTreeViewer(keywordsGroup, SWT.NO_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.VIRTUAL);
		expandCollapseGroup.setViewer(viewer);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		layoutData.heightHint = tags ? 200 : 300;
		viewer.getControl().setLayoutData(layoutData);
		viewer.setContentProvider(new KeywordContentProvider());
		KeywordLabelProvider labelProvider = new KeywordLabelProvider(vocabManager, ROOT) {
			@Override
			public Font getFont(Object element) {
				if (externalKeywords != null && externalKeywords.contains(element))
					return JFaceResources.getBannerFont();
				return super.getFont(element);
			}
		};
		viewer.setLabelProvider(labelProvider);
		viewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
			@Override
			public boolean select(Viewer aViewer, Object parentElement, Object element) {
				if (element == AVAILABLE || element == APPLIED || element instanceof Character)
					return true;
				WildCardFilter filter = filterField.getFilter();
				return filter == null || filter.accept(labelProvider.getText(element));
			}
		} });
		UiUtilities.installDoubleClickExpansion(viewer);
		ColumnViewerToolTipSupport.enableFor(viewer);
		availableKeywords = new HashSet<String>(predefinedKeywords);
		if (selectedKeywords != null)
			for (String kw : selectedKeywords)
				if (kw != null && predefinedKeywords.contains(kw))
					appliedKeywords.add(kw);
		availableKeywords.removeAll(appliedKeywords);
		keywordVerifyListener = new KeywordVerifyListener(viewer);
		updateKeywordViewer(null);
		if (externalKeywords != null) {
			importButton = new Button(keywordsGroup, SWT.PUSH);
			importButton.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
			importButton.setText(Messages.KeywordGroup_import_into_catalog);
			if (!externalKeywords.isEmpty())
				importButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Set<String> newKeywords = meta.getKeywords();
						newKeywords.addAll(externalKeywords);
						ModifyMetaOperation op = new ModifyMetaOperation(meta, false, null, null, null, null, null,
								null, null, null, meta.getCumulateImports(), null, null, newKeywords, null, null,
								meta.getThumbnailFromPreview(), null, null, meta.getFolderWatchLatency(),
								meta.getPauseFolderWatch(), meta.getReadonly(), meta.getAutoWatch(), meta.getSharpen(),
								meta.getWebpCompression(), meta.getJpegQuality(), meta.getNoIndex(), meta.getLocale(),
								meta.getCbirAlgorithms(), meta.getIndexedTextFields(), meta.getPersonsToKeywords(),
								null, meta.getVocabularies());
						OperationJob.executeOperation(op, KeywordGroup.this);
						importButton.setEnabled(false);
						availableKeywords.addAll(externalKeywords);
						externalKeywords.clear();
						updateKeywordViewer(null);
					}
				});
			else
				importButton.setEnabled(false);
		}
		CGroup newKeywordsGroup = new CGroup(composite, SWT.NONE);
		newKeywordsGroup.setText(tags ? Messages.KeywordGroup_new_tags : Messages.KeywordGroup_new_keywords);
		boolean hasVocab = vocabManager != null && vocabManager.getVocabTree() != null;
		newKeywordsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		newKeywordsGroup.setLayout(new GridLayout(hasVocab ? 2 : 1, false));
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 50;
		newKeywordField = new CheckedText(newKeywordsGroup, SWT.BORDER | SWT.MULTI);
		newKeywordField.setLayoutData(data);
		newKeywordField.setSpellingOptions(8, ISpellCheckingService.KEYWORDOPTIONS);
		if (hasVocab) {
			Button button = new Button(newKeywordsGroup, SWT.PUSH);
			button.setLayoutData(new GridData(SWT.END, SWT.FILL, false, false));
			button.setText(Messages.KeywordGroup_vocab);
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ViewVocabDialog dialog = new ViewVocabDialog(composite.getShell(), vocabManager.getVocabTree(),
							null, true);
					if (dialog.open() == ViewVocabDialog.OK) {
						addSelectedKeywords(dialog.getResult());
						updateVocabLabel();
					}
				}
			});
			vocabLabel = new CLabel(newKeywordsGroup, SWT.NONE);
			vocabLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			vocabLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					if (synonym != null) {
						StringTokenizer st = new StringTokenizer(newKeywordField.getText(), "\n"); //$NON-NLS-1$
						StringBuilder sb = new StringBuilder();
						while (st.hasMoreTokens()) {
							String token = st.nextToken().trim();
							if (token.equals(synonym))
								token = replacement;
							if (token != null) {
								if (sb.length() > 0)
									sb.append('\n');
								sb.append(token);
							}
						}
						newKeywordField.removeVerifyListener(keywordVerifyListener);
						newKeywordField.setText(sb.toString());
						newKeywordField.addVerifyListener(keywordVerifyListener);
						updateVocabLabel();
					}
				}
			});
			newKeywordField.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					updateVocabLabel();
				}
			});
		}
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object e = event.getElement();
				if (e instanceof String) {
					String element = (String) e;
					boolean checked = event.getChecked();
					if (recentViewer != null)
						recentViewer.setChecked(element, checked);
					if (checked) {
						if (element != APPLIED && element != AVAILABLE) {
							appliedKeywords.add(element);
							availableKeywords.remove(element);
						}
					} else if (element == APPLIED) {
						availableKeywords.addAll(appliedKeywords);
						appliedKeywords.clear();
					} else if (element != AVAILABLE) {
						appliedKeywords.remove(element);
						availableKeywords.add(element);
					}
					updateKeywordViewer(element);
					if (recentViewer != null)
						recentViewer.setCheckedElements(appliedKeywords.toArray());
				} else
					updateKeywordViewer(e);
			}
		});
		StringBuilder sb = new StringBuilder();
		if (selectedKeywords != null)
			for (String kw : QueryField.getKeywordFilter().filter(selectedKeywords))
				if (kw != null && !predefinedKeywords.contains(kw)) {
					if (sb.length() > 0)
						sb.append('\n');
					sb.append(kw);
				}
		newKeywordField.setText(sb.toString());
		newKeywordField.addVerifyListener(keywordVerifyListener);
		newKeywordField.setFocus();
	}

	protected void updateVocabLabel() {
		synonym = null;
		replacement = null;
		String text = newKeywordField.getText();
		if (!text.isEmpty()) {
			StringTokenizer st = new StringTokenizer(text, "\n"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				String vocab = vocabManager.getVocab(token);
				if (vocab == null) {
					vocabLabel.setText(NLS.bind(Messages.KeywordGroup_not_contained_in_vocab, token));
					vocabLabel.setImage(Icons.warning.getImage());
					synonym = token;
					return;
				}
				if (!vocab.equals(token)) {
					vocabLabel.setText(NLS.bind(Messages.KeywordGroup_rather_use_x, vocab, token));
					vocabLabel.setImage(Icons.info.getImage());
					synonym = token;
					replacement = vocab;
					return;
				}
			}
		}
		vocabLabel.setText(""); //$NON-NLS-1$
		vocabLabel.setImage(null);
	}

	public Set<String> getGeographicKeywords() {
		if (geographic == null) {
			geographic = new HashSet<>();
			List<LocationImpl> locations = Core.getCore().getDbManager().obtainObjects(LocationImpl.class);
			for (LocationImpl location : locations)
				Utilities.extractKeywords(location, geographic);
		}
		return geographic;
	}

	protected void updateKeywordViewer(Object reveal) {
		Object[] expandedElements = viewer.getExpandedElements();
		viewer.setInput(ROOT);
		viewer.setCheckedElements(appliedKeywords.toArray());
		if (!appliedKeywords.isEmpty())
			viewer.setChecked(APPLIED, true);
		keywordVerifyListener.setKeywords(filterKeywords(availableKeywords));
		if (expandedElements.length == 0)
			viewer.expandToLevel(2);
		else
			viewer.setExpandedElements(expandedElements);
		if (reveal != null)
			viewer.reveal(reveal);
	}

	private String[] filterKeywords(Collection<String> keywords) {
		if (tags || !excludeGeographic)
			return keywords.toArray(new String[keywords.size()]);
		Set<String> geographic = getGeographicKeywords();
		List<String> filtered = new ArrayList<>(keywords.size());
		for (String kw : keywords)
			if (!geographic.contains(kw))
				filtered.add(kw);
		return filtered.toArray(new String[filtered.size()]);
	}

	public void commit() {
		Set<String> newKeywords = new HashSet<String>(appliedKeywords);
		StringTokenizer st = new StringTokenizer(newKeywordField.getText(), "\n"); //$NON-NLS-1$
		while (st.hasMoreTokens())
			newKeywords.add(st.nextToken().trim());
		if (!newKeywords.isEmpty()) {
			if (recentKeywords == null)
				recentKeywords = new LinkedList<String>();
			for (String kw : newKeywords)
				if (!recentKeywords.contains(kw))
					recentKeywords.add(kw);
			while (recentKeywords.size() > LIMIT)
				recentKeywords.remove(0);
		}
		String[] display = newKeywords.toArray(new String[newKeywords.size()]);
		Arrays.sort(display, Utilities.KEYWORDCOMPARATOR);

		List<String> oldKeywords = selectedKeywords == null ? new ArrayList<String>(0)
				: Arrays.asList(selectedKeywords);
		Set<String> added = new HashSet<String>(newKeywords);
		added.removeAll(oldKeywords);
		Set<String> removed = new HashSet<String>(oldKeywords);
		removed.removeAll(newKeywords);
		result = new BagChange<String>(added, null, removed, display);
		radioGroup.saveSettings();
		UiActivator.getDefault().saveDialogSettings();
	}

	public List<String> getRecentKeywords() {
		return recentKeywords;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter)) {
			Shell shell = viewer.getControl().getShell();
			while (shell.getParent() != null)
				shell = (Shell) shell.getParent();
			return shell;
		}
		return null;
	}

	public BagChange<String> getResult() {
		return result;
	}

	public void addSelectedKeywords(String[] chosenKeywords) {
		for (String kw : chosenKeywords) {
			if (appliedKeywords.contains(kw))
				continue;
			if (availableKeywords.contains(kw)) {
				appliedKeywords.add(kw);
				availableKeywords.remove(kw);
				updateKeywordViewer(kw);
				if (recentViewer != null)
					recentViewer.setCheckedElements(appliedKeywords.toArray());
			} else {
				String text = newKeywordField.getText();
				if (!text.isEmpty())
					text += '\n';
				text += kw;
				newKeywordField.removeVerifyListener(keywordVerifyListener);
				newKeywordField.setText(text);
				newKeywordField.addVerifyListener(keywordVerifyListener);
			}
		}

	}

}
