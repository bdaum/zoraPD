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
package com.bdaum.zoom.lal.internal.lire.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.QueryOptions;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.FindInNetworkGroup;
import com.bdaum.zoom.ui.internal.widgets.SearchResultGroup;

@SuppressWarnings("restriction")
public class ConfigureSimilaritySearchDialog extends ZTitleAreaDialog {
	private FindInNetworkGroup findInNetworkGroup;

	private SearchResultGroup searchResultGroup;

	private QueryOptions options;

	public ConfigureSimilaritySearchDialog(Shell parentShell) {
		super(parentShell, HelpContextIds.CONFIGURESEARCH_DIALOG);
		options = UiActivator.getDefault().getQueryOptions();
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.ConfigureTextSearchDialog_configure_searchfield);
		setMessage(Messages.ConfigureTextSearchDialog_specify_parameters);
	}

	@SuppressWarnings("unused")
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		final Composite composite = new Composite(area, SWT.NONE);
		final GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 10;
		composite.setLayout(gridLayout);
		if (Core.getCore().isNetworked())
			findInNetworkGroup = new FindInNetworkGroup(composite);
		searchResultGroup = new SearchResultGroup(composite, SWT.NONE, true, true, true, getButton(OK), null);
		new Label(composite, SWT.NONE);
		fillValues();
		return area;
	}

	private void fillValues() {
		searchResultGroup.fillValues(options.getScore(), options.getMaxHits(), options.getMethod(), options.getKeywordWeight());
		if (findInNetworkGroup != null)
			findInNetworkGroup.setSelection(options.isNetworked());
		searchResultGroup.setFocus();
	}

	@Override
	protected void okPressed() {
		options.setNetworked(findInNetworkGroup == null ? false : findInNetworkGroup
				.getSelection());
		options.setScore(searchResultGroup.getScore());
		options.setMaxHits(searchResultGroup.getMaxNumber());
		options.setMethod(searchResultGroup.getSelectedAlgorithm().getId());
		options.setKeywordWeight(searchResultGroup.getKeywordWeight());
		super.okPressed();
	}

}
