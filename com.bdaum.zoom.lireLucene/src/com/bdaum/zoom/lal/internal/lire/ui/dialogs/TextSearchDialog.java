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
package com.bdaum.zoom.lal.internal.lire.ui.dialogs;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.TextSearchOptions_typeImpl;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.internal.QueryOptions;
import com.bdaum.zoom.core.internal.lucene.ParseException;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.FindInNetworkGroup;
import com.bdaum.zoom.ui.internal.dialogs.FindWithinGroup;
import com.bdaum.zoom.ui.internal.widgets.SearchResultGroup;

@SuppressWarnings("restriction")
public class TextSearchDialog extends ZTitleAreaDialog {
	private static final String SETTINGSID = "com.bdaum.zoom.textSearchDialog"; //$NON-NLS-1$
	private static final String HISTORY = "history"; //$NON-NLS-1$

	private SmartCollectionImpl result;
	private Combo combo;
	private FindWithinGroup findWithinGroup;
	private boolean adhoc = true;
	private TextSearchOptions_typeImpl options;
	private boolean networked;
	private FindInNetworkGroup findInNetworkGroup;

	private IDialogSettings settings;
	private SearchResultGroup searchResultGroup;
	private SmartCollection currentCollection;
	private final String text;

	public TextSearchDialog(Shell parentShell,
			SmartCollection currentCollection, String text) {
		super(parentShell, HelpContextIds.TEXT_SEARCH_DIALOG);
		this.currentCollection = currentCollection;
		this.text = text;
		settings = UiActivator.getDefault().getDialogSettings(SETTINGSID);
		QueryOptions queryOptions = UiActivator.getDefault().getQueryOptions();
		if (currentCollection != null) {
			adhoc = currentCollection.getAdhoc();
			networked = currentCollection.getNetwork();
			Criterion criterion = currentCollection.getCriterion(0);
			if (criterion != null
					&& criterion.getValue() instanceof TextSearchOptions_typeImpl)
				options = (TextSearchOptions_typeImpl) criterion.getValue();
		} else
			networked = queryOptions.isNetworked();
		if (options == null) {
			options = dbManager.obtainById(TextSearchOptions_typeImpl.class,
					Constants.TEXTSEARCHOPTIONS_ID);
			if (options == null) {
				options = new TextSearchOptions_typeImpl(
						"", queryOptions.getMaxHits(), queryOptions.getScore() / 100f); //$NON-NLS-1$
				options.setStringId(Constants.TEXTSEARCHOPTIONS_ID);
			}
		}
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.TextSearchDialog_text_search);
		if (Job.getJobManager().find(Constants.INDEXING).length > 0)
			setMessage(Messages.TextSearchDialog_indexing_in_progress + '\n'
					+ Messages.TextSearchDialog_specify_a_search_string,
					IMessageProvider.WARNING);
		else
			setMessage(Messages.TextSearchDialog_specify_a_search_string);
		fillValues();
		updateButtons();
	}

	private void updateButtons() {
		boolean valid = validate();
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			getShell().setModified(valid);
			okButton.setEnabled(valid);
		}
	}

	private boolean validate() {
		String s = combo.getText();
		if (s.isEmpty()) {
			setErrorMessage(Messages.TextSearchDialog_please_enter_a_search_string);
			return false;
		}
		try {
			Core.getCore().getDbFactory().getLuceneService().parseLuceneQuery(s);
		} catch (ParseException e) {
			setErrorMessage(Messages.TextSearchDialog_bad_query_expression);
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite composite = new Composite(area, SWT.NONE);
		final GridData gd_composite = new GridData();
		gd_composite.verticalIndent = 15;
		composite.setLayoutData(gd_composite);
		final GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 10;
		composite.setLayout(gridLayout);
		new Label(composite, SWT.NONE)
				.setText(Messages.TextSearchDialog_query_string);
		combo = new Combo(composite, SWT.NONE);
		String[] items = settings.getArray(HISTORY);
		if (items == null)
			items = new String[0];
		combo.setItems(items);
		if (combo.getItemCount() > 0)
			combo.setText(combo.getItem(0));
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtons();
			}
		});
		final GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		gd_combo.widthHint = 350;
		combo.setLayoutData(gd_combo);
		if (adhoc)
			findWithinGroup = new FindWithinGroup(composite);
		if (Core.getCore().isNetworked())
			findInNetworkGroup = new FindInNetworkGroup(composite);
		searchResultGroup = new SearchResultGroup(composite, SWT.NONE, false,
				true, false, null, null);
		return area;
	}

	private void fillValues() {
		if (options.getQueryString() != null
				&& !options.getQueryString().isEmpty()) {
			String queryString = options.getQueryString();
			combo.setText(queryString);
			combo.setSelection(new Point(0, queryString.length()));
			validate();
		}
		combo.setFocus();
		searchResultGroup.fillValues(
				(int) (options.getMinScore() * 100 + 0.5f),
				options.getMaxResults(), -1, 0);
		if (findWithinGroup != null) {
			if (currentCollection != null)
				findWithinGroup.setSelection(currentCollection
						.getSmartCollection_subSelection_parent() != null);
			else
				findWithinGroup.fillValues(settings);
		}
		if (findInNetworkGroup != null)
			findInNetworkGroup.setSelection(networked);
		if (text != null) {
			combo.setText(text);
			validate();
		}
	}

	@Override
	protected void okPressed() {
		int maxNumber = searchResultGroup.getMaxNumber();
		int score = searchResultGroup.getScore();
		String s = combo.getText();
		result = new SmartCollectionImpl(s
				+ NLS.bind(Messages.TextSearchDialog_maxmin, maxNumber, score),
				false, false, adhoc, findInNetworkGroup == null ? false
						: findInNetworkGroup.getSelection(), null, 0, null, 0, null, null);
		result.addCriterion(new CriterionImpl(ICollectionProcessor.TEXTSEARCH,
				null, new TextSearchOptions_typeImpl(
						s, maxNumber, score / 100f), score, false));
		if (findWithinGroup != null) {
			result.setSmartCollection_subSelection_parent(findWithinGroup
					.getParentCollection());
			findWithinGroup.saveValues(settings);
		}
		if (findInNetworkGroup != null)
			findInNetworkGroup.saveValues(settings);
		settings.put(HISTORY, UiUtilities.updateComboHistory(combo));
		options.setMaxResults(maxNumber);
		options.setMinScore(score / 100f);
		options.setQueryString(s);
		dbManager.safeTransaction(null, options);
		super.okPressed();
	}

	public SmartCollectionImpl getResult() {
		return result;
	}

}
