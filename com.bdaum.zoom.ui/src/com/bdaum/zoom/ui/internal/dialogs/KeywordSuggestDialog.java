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
 * (c) 2013 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.SimilarityOptions_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.QueryOptions;
import com.bdaum.zoom.core.internal.ScoredString;
import com.bdaum.zoom.core.internal.lire.Algorithm;
import com.bdaum.zoom.core.internal.lire.ILireService;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;

@SuppressWarnings("restriction")
public class KeywordSuggestDialog extends ZProgressDialog implements SelectionListener {

	public class SuggestJob extends Job {

		public SuggestJob() {
			super(Messages.SuggestKeywordDialog_collecting_keywords);
			setSystem(true);
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == KeywordSuggestDialog.this;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			File indexPath = dbManager.getIndexPath();
			if (indexPath != null) {
				if (options.getMethod() >= 0) {
					int work = 0;
					Map<String, ScoredString> foundAssets = new HashMap<String, ScoredString>();
					final Shell shell = getShell();
					IDbManager dbManager = Core.getCore().getDbManager();
					ICollectionProcessor collectionProcessor = dbManager.createCollectionProcessor(null);
					for (Asset asset : assets) {
						addKeywords(allKeywords, asset, 100);
						options.setAssetId(asset.getStringId());
						String[] keywords = asset.getKeyword();
						options.setKeywords(keywords);
						options.setKeywordWeight(
								(keywords == null || keywords.length == 0) ? 0 : queryOptions.getKeywordWeight());
						List<Asset> set = collectionProcessor
								.processContentSearch(new CriterionImpl(ICollectionProcessor.SIMILARITY, null, options,
										(int) (options.getMinScore() * 100), true), null, null);
						for (Asset foundAsset : set)
							addScoredString(foundAssets, (int) (foundAsset.getScore() * 100), foundAsset.getStringId());
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						++work;
						final int work2 = work;
						if (!shell.isDisposed()) {
							shell.getDisplay().syncExec(() -> {
								if (!shell.isDisposed())
									getProgressBar().setSelection(work2);
							});
						}
					}
					for (ScoredString ss : foundAssets.values())
						addKeywords(allKeywords, dbManager.obtainAsset(ss.getString()), ss.getScore());
					if (!shell.isDisposed()) {
						shell.getDisplay().syncExec(() -> {
							if (!shell.isDisposed()) {
								viewer.setInput(allKeywords.values());
								getProgressBar().setVisible(false);
							}
						});
					}
				}
			}
			return Status.OK_STATUS;
		}

		private void addKeywords(final Map<String, ScoredString> allKeywords, Asset asset, int score) {
			String[] keywords = asset.getKeyword();
			if (keywords != null)
				lp: for (String kw : keywords) {
					if (selectedKeywords != null)
						for (Object selkw : selectedKeywords)
							if (selkw.equals(kw))
								continue lp;
					addScoredString(allKeywords, score, kw);
				}
		}

		private void addScoredString(final Map<String, ScoredString> result, int score, String string) {
			ScoredString scoredString = result.get(string);
			if (scoredString != null) {
				if (scoredString.getScore() < score)
					scoredString.setScore(score);
			} else
				result.put(string, new ScoredString(string, score));
		}

	}

	private final List<Asset> assets;
	private CheckboxTableViewer viewer;
	private final Object[] selectedKeywords;
	private SimilarityOptions_typeImpl options;
	private String[] suggestedKeywords;
	private Map<String, ScoredString> allKeywords = new HashMap<String, ScoredString>();
	private QueryOptions queryOptions;
	private ILireService lireService;
	private RadioButtonGroup sortButtonGroup;

	public KeywordSuggestDialog(Shell parentShell, List<Asset> assets, Object[] selectedKeywords) {
		super(parentShell);
		this.assets = assets;
		this.selectedKeywords = selectedKeywords;
		lireService = Core.getCore().getDbFactory().getLireService(true);
	}

	@Override
	public void create() {
		super.create();
		prepareQueryOptions();
		setTitle(Messages.SuggestKeywordDialog_suggested_keywords);
		updateMessage();
		fillValues();
	}

	private void updateMessage() {
		Algorithm algorithm = lireService.getAlgorithmById(options.getMethod());
		setMessage(NLS.bind(Messages.SuggestKeywordDialog_suggested_on_basis,
				new Object[] { algorithm != null ? algorithm.getName() : Messages.SuggestKeywordDialog_invalid,
						options.getMinScore(), options.getMaxResults() }));
	}

	private void prepareQueryOptions() {
		queryOptions = UiActivator.getDefault().getQueryOptions();
		int method = queryOptions.getMethod();
		int validMethod = -1;
		Algorithm algorithm = lireService.getAlgorithmById(method);
		if (algorithm != null && CoreActivator.getDefault().getCbirAlgorithms().contains(algorithm.getName()))
			validMethod = method;
		if (validMethod < 0 && lireService.ShowConfigureSearch(this, null))
			validMethod = queryOptions.getMethod();
		options = new SimilarityOptions_typeImpl(validMethod, queryOptions.getMaxHits(), queryOptions.getScore() / 100f,
				0, 0, 0, 0, null, queryOptions.getKeywordWeight());
	}

	private void fillValues() {
		setMinMax(0, Math.max(1, assets.size() + 1));
		new SuggestJob().schedule();
	}

	@Override
	protected void buttonPressed(int buttonId) {
		Job.getJobManager().cancel(this);
		super.buttonPressed(buttonId);
	}

	@Override
	protected void okPressed() {
		Object[] checkedElements = viewer.getCheckedElements();
		suggestedKeywords = new String[checkedElements.length];
		for (int i = 0; i < checkedElements.length; i++)
			suggestedKeywords[i] = ((ScoredString) checkedElements[i]).getString();
		super.okPressed();
	}

	@Override
	protected void createCustomArea(Composite comp) {
		comp.setLayout(new GridLayout());
		sortButtonGroup = new RadioButtonGroup(comp, Messages.KeywordSuggestDialog_sort_by, SWT.HORIZONTAL,
				Messages.KeywordSuggestDialog_score, Messages.KeywordSuggestDialog_alpha);
		sortButtonGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		sortButtonGroup.addSelectionListener(this);
		Composite viewerComp = new Composite(comp, SWT.NONE);
		viewerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewerComp.setLayout(new GridLayout(2, false));

		viewer = CheckboxTableViewer.newCheckList(viewerComp, SWT.BORDER | SWT.V_SCROLL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 500;
		viewer.getControl().setLayoutData(layoutData);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());
		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (sortButtonGroup.getSelection() == 0) {
					if (((ScoredString) e1).getScore() > ((ScoredString) e2).getScore())
						return -1;
					if (((ScoredString) e1).getScore() < ((ScoredString) e2).getScore())
						return 1;
				}
				return ((ScoredString) e1).getString().compareTo(((ScoredString) e2).getString());
			}
		});
		AllNoneGroup buttonbar = new AllNoneGroup(viewerComp, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setAllChecked(e.widget.getData() == AllNoneGroup.ALL);
			}
		});
		Label label = new Label(buttonbar, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (lireService != null) {
			Button configureButton = new Button(buttonbar, SWT.PUSH);
			configureButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			configureButton.setText(Messages.SuggestKeywordDialog_configure);
			configureButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (lireService.ShowConfigureSearch(KeywordSuggestDialog.this, null)) {
						Job.getJobManager().cancel(KeywordSuggestDialog.this);
						QueryOptions queryOptions = UiActivator.getDefault().getQueryOptions();
						options.setMaxResults(queryOptions.getMaxHits());
						options.setMinScore(queryOptions.getScore());
						options.setMethod(queryOptions.getMethod());
						updateMessage();
						fillValues();
					}
				}
			});
		}
	}

	public String[] getChosenKeywords() {
		return suggestedKeywords;
	}

	public void widgetSelected(SelectionEvent e) {
		if (allKeywords != null)
			viewer.setInput(allKeywords.values());
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}

}
