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

package com.bdaum.zoom.ui.internal.commands;

import java.util.List;
import java.util.Stack;

import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.Icons.Icon;
import com.bdaum.zoom.ui.internal.NavigationHistory.HistoryItem;

/**
 * @author berth
 *
 */
public class BackwardControl extends AbstractHistoryControl {

	public BackwardControl() {
		super("com.bdaum.zoom.ui.BackCommand"); //$NON-NLS-1$
	}

	public BackwardControl(String id) {
		super(id);
	}

	@Override
	public void historyChanged() {
		if (label != null && !label.isDisposed())
			label.setEnabled(navigationHistory.canGoBack());
	}

	protected List<HistoryItem> getHistoryItems() {
		Stack<HistoryItem> backwardList = navigationHistory.getBackwardList();
		return backwardList.subList(0, backwardList.size() - 1);
	}

	protected void goTo(final HistoryItem historyItem) {
		navigationHistory.back(historyItem);
	}

	@Override
	protected Icon getIcon() {
		return Icons.backwards;
	}

	@Override
	protected void goTo() {
		navigationHistory.back();
	}

}
