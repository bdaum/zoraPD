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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;

public class KeywordDialog extends ZTitleAreaDialog {

	private static final int SUGGEST = 9999;
	private static final String SETTINGSID = "com.bdaum.zoom.keywordDialog"; //$NON-NLS-1$
	private static final String RECENT = "recent"; //$NON-NLS-1$
	private String[] selectedKeywords;
	private KeywordGroup group;
	private Set<String> selectableKeywords;
	private String title;
	private IDialogSettings settings;
	private List<String> recentKeywords;
	private final List<Asset> selectedAssets;

	public KeywordDialog(Shell parentShell, String title, String[] keywords,
			Set<String> selectableKeywords, List<Asset> selectedAssets) {
		super(parentShell, HelpContextIds.KEYWORD_DIALOG);
		this.title = title;
		this.selectedKeywords = keywords;
		this.selectableKeywords = selectableKeywords;
		this.selectedAssets = selectedAssets;
	}

	@Override
	public void create() {
		super.create();
		setTitle(title);
		setMessage(Messages.KeywordDialog_mark_existing_or_enter_new
				+ (selectedAssets != null && selectedAssets.size() > 1 ? "\n" //$NON-NLS-1$
						+ Messages.KeywordDialog_only_common_keywords : "")); //$NON-NLS-1$
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		recentKeywords = null;
		settings = UiActivator.getDefault().getDialogSettings(SETTINGSID);
		String recent = settings.get(RECENT);
		if (recent != null && !recent.isEmpty()) {
			recentKeywords = new LinkedList<String>();
			StringTokenizer st = new StringTokenizer(recent, "\n"); //$NON-NLS-1$
			while (st.hasMoreTokens())
				recentKeywords.add(st.nextToken());
		}
		Composite comp = (Composite) super.createDialogArea(parent);
		group = new KeywordGroup(comp, selectedKeywords, selectableKeywords,
				recentKeywords, false);
		return comp;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (selectedAssets != null && !dbManager.getMeta(true).getNoIndex()
				&& dbManager.getIndexPath() != null)
			createButton(parent, SUGGEST, Messages.KeywordDialog_suggest, false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == SUGGEST) {
			if (selectedAssets != null) {
				KeywordSuggestDialog dialog = new KeywordSuggestDialog(
						getShell(), selectedAssets, selectedKeywords);
				if (dialog.open() == Dialog.OK)
					group.addSelectedKeywords(dialog.getChosenKeywords());
				return;
			}
		} else
			super.buttonPressed(buttonId);
	}

	@Override
	protected void okPressed() {
		group.commit();
		recentKeywords = group.getRecentKeywords();
		if (recentKeywords != null)
			settings.put(RECENT, Core.toStringList(recentKeywords, '\n'));
		super.okPressed();
	}

	public BagChange<String> getResult() {
		return group.getResult();
	}
}
