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

package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class FindInNetworkGroup {

	private static final String FIND_INNETWORK = "findInNetwork"; //$NON-NLS-1$
	private CheckboxButton checkButton;
	private Composite composite;

	public FindInNetworkGroup(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		Layout layout = parent.getLayout();
		if (layout instanceof GridLayout)
			layoutData.horizontalSpan = ((GridLayout) layout).numColumns;
		composite.setLayoutData(layoutData);
		composite.setLayout(new GridLayout(2, false));
		checkButton = WidgetFactory.createCheckButton(composite,
				Messages.FindInNetworkGroup_find_in_network, null);
		IPeerService peerService = Core.getCore().getPeerService();
		if (peerService != null) {
			checkButton.setEnabled(peerService.hasPeerPeerProviders());
			int onlinePeerCount = peerService.getOnlinePeerCount();
			String msg;
			switch (onlinePeerCount) {
			case 0:
				msg = Messages.FindInNetworkGroup_no_peer;
				break;
			case 1:
				msg = Messages.FindInNetworkGroup_on_peer;
				break;
			default:
				msg = NLS.bind(Messages.FindInNetworkGroup_n_peers, onlinePeerCount);
				break;
			}
			checkButton.setToolTipText(msg);
		}
	}

	public void setBounds(int x, int y) {
		composite.setBounds(x, y, 200, 30);
		composite.layout();
	}

	public void fillValues(IDialogSettings settings) {
		checkButton.setSelection(settings.getBoolean(FIND_INNETWORK));
	}

	public void saveValues(IDialogSettings settings) {
		settings.put(FIND_INNETWORK, checkButton.getSelection());
	}

	public boolean getSelection() {
		return checkButton.getEnabled() && checkButton.getSelection();
	}

	public void setSelection(boolean selected) {
		checkButton.setSelection(selected);
	}

	public void setEnabled(boolean enabled) {
		checkButton.setEnabled(enabled);
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Button#addSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
	public void addSelectionListener(SelectionListener listener) {
		checkButton.addSelectionListener(listener);
	}

	/**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Button#removeSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
	public void removeSelectionListener(SelectionListener listener) {
		checkButton.removeSelectionListener(listener);
	}

}
