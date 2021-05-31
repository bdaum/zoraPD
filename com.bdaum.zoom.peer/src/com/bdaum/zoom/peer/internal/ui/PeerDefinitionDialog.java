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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.peer.internal.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.peer.internal.PeerActivator;
import com.bdaum.zoom.peer.internal.model.PeerDefinition;
import com.bdaum.zoom.peer.internal.services.ROSGiManager;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class PeerDefinitionDialog extends ZTitleAreaDialog implements Listener {

	private final PeerDefinition currentDefiniton;
	private Text ipField;
	private Spinner portField;
	private CheckboxButton searchButton;
	private CheckboxButton copyButton;
	private CheckboxButton viewButton;
	private PeerDefinition result;
	private final boolean withRights;
	private final boolean location;
	private CheckboxButton voiceButton;
	private final boolean showIncoming;
	private Combo ipCombo;
	private Text nickField;

	public PeerDefinitionDialog(Shell parentShell, PeerDefinition currentDefiniton, boolean location,
			boolean withRights, boolean showIncoming) {
		super(parentShell, withRights ? HelpContextIds.RESTRICTION_DIALOG : HelpContextIds.PEER_DIALOG);
		this.currentDefiniton = currentDefiniton;
		this.location = location;
		this.withRights = withRights;
		this.showIncoming = showIncoming;
	}

	@Override
	public void create() {
		super.create();
		fillValues();
		setTitle(withRights ? Messages.PeerDefinitionDialog_access_restrictions
				: Messages.PeerDefinitionDialog_peer_location);
		setMessage(withRights
				? (location ? Messages.PeerDefinitionDialog_define_name_and_rights
						: Messages.PeerDefinitionDialog_specify_operations)
				: Messages.PeerDefinitionDialog_define_computer_name);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		if (location) {
			Map<String, PeerDefinition> incomingCalls = showIncoming ? PeerActivator.getDefault().getIncomingCalls()
					: null;
			if (incomingCalls == null || incomingCalls.isEmpty()) {
				if (!withRights) {
					new Label(composite, SWT.NONE).setText(Messages.PeerDefinitionDialog_nickname);
					nickField = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
					nickField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				}
				new Label(composite, SWT.NONE).setText(Messages.PeerDefinitionDialog_computer_name);
				ipField = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
				ipField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				ipField.addListener(SWT.Modify, this);
				ipField.selectAll();
				ipField.setFocus();
			} else {
				new Label(composite, SWT.NONE).setText(Messages.PeerDefinitionDialog_computer_name);
				ipCombo = new Combo(composite, SWT.DROP_DOWN);
				ipCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				ipCombo.setVisibleItemCount(8);
				Set<String> itemList = new HashSet<String>();
				for (Map.Entry<String, PeerDefinition> entry : incomingCalls.entrySet())
					if (!entry.getValue().isBlocked())
						itemList.add(entry.getValue().getHost());
				String[] array = itemList.toArray(new String[itemList.size()]);
				Arrays.sort(array);
				ipCombo.setItems(array);
				ipCombo.addListener(SWT.Selection, this);
				ipCombo.addListener(SWT.Modify, this);
			}
			if (!withRights) {
				new Label(composite, SWT.NONE).setText(Messages.PeerDefinitionDialog_port);
				portField = new Spinner(composite, SWT.BORDER);
				portField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
				portField.setMaximum(65535);
				portField.setIncrement(1);
				portField.addListener(SWT.Selection, this);
			}
		}
		if (withRights) {
			CGroup rightsGroup = new CGroup(composite, SWT.NONE);
			rightsGroup.setText(Messages.PeerDefinitionDialog_rights);
			rightsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			rightsGroup.setLayout(new GridLayout(2, false));
			searchButton = WidgetFactory.createCheckButton(rightsGroup, Messages.PeerDefinitionDialog_search, null,
					Messages.PeerDefinitionDialog_search_tooltip);
			viewButton = WidgetFactory.createCheckButton(rightsGroup, Messages.PeerDefinitionDialog_view, null,
					Messages.PeerDefinitionDialog_view_tooltip);
			voiceButton = WidgetFactory.createCheckButton(rightsGroup, Messages.PeerDefinitionDialog_voice_notes, null,
					Messages.PeerDefinitionDialog_voice_notes_tooltip);
			copyButton = WidgetFactory.createCheckButton(rightsGroup, Messages.PeerDefinitionDialog_copy, null,
					Messages.PeerDefinitionDialog_copy_tooltip);
		}
		return area;
	}

	private void fillValues() {
		if (currentDefiniton != null) {
			if (location) {
				if (ipField != null)
					ipField.setText(currentDefiniton.getHost());
				else
					ipCombo.setText(currentDefiniton.getHost());
				if (portField != null)
					portField.setSelection(currentDefiniton.getPort());
				if (nickField != null)
					nickField.setText(currentDefiniton.getNickname() == null ? "" : currentDefiniton.getNickname()); //$NON-NLS-1$
			}
			if (withRights) {
				int rights = currentDefiniton.getRights();
				searchButton.setSelection((rights & IPeerService.SEARCH) != 0);
				viewButton.setSelection((rights & IPeerService.VIEW) != 0);
				voiceButton.setSelection((rights & IPeerService.VOICE) != 0);
				copyButton.setSelection((rights & IPeerService.COPY) != 0);
			}
		} else {
			if (location) {
				if (ipField != null)
					ipField.setText("127.0.0.0"); //$NON-NLS-1$
				else
					ipCombo.setText("127.0.0.0"); //$NON-NLS-1$
				if (portField != null)
					portField.setSelection(9278);
			}
			if (withRights) {
				searchButton.setSelection(true);
				viewButton.setSelection(true);
			}
		}
		validate();
	}

	@Override
	protected void okPressed() {
		result = location
				? new PeerDefinition(nickField != null ? nickField.getText().trim() : null,
						ipField != null ? ipField.getText() : ipCombo.getText(),
						portField != null ? portField.getSelection() : -1)
				: currentDefiniton != null
						? new PeerDefinition(currentDefiniton.getNickname(), currentDefiniton.getHost(),
								currentDefiniton.getPort())
						: null;
		if (viewButton != null && result != null)
			result.setRights((searchButton.getSelection() ? IPeerService.SEARCH : 0)
					+ (viewButton.getSelection() ? IPeerService.VIEW : 0)
					+ (voiceButton.getSelection() ? IPeerService.VOICE : 0)
					+ (copyButton.getSelection() ? IPeerService.COPY : 0));
		super.okPressed();
	}

	@SuppressWarnings("unused")
	private void validate() {
		String errorMessage = null;
		if (location) {
			try {
				String host = ipField != null ? ipField.getText() : ipCombo.getText();
				new URI(ROSGiManager.PROTOCOL + "://" + host); //$NON-NLS-1$
				if (portField != null) {
					int port = portField.getSelection();
					PeerActivator activator = PeerActivator.getDefault();
					if (activator.getLocation().equals(host + ":" + port)) //$NON-NLS-1$
						errorMessage = Messages.PeerDefinitionDialog_own_location_as_peer;
					else if (currentDefiniton == null || !currentDefiniton.getHost().equals(host)
							|| currentDefiniton.getPort() != port) {
						List<PeerDefinition> peers = activator.getPeers();
						for (PeerDefinition peerDefinition : peers)
							if (peerDefinition.getHost().equals(host) && peerDefinition.getPort() == port) {
								errorMessage = Messages.PeerDefinitionDialog_peerDef_already_exists;
								break;
							}
					}
				}
			} catch (URISyntaxException e) {
				errorMessage = Messages.PeerDefinitionDialog_specify_valid_ip;
			}
		}
		getButton(OK).setEnabled(errorMessage == null);
		setErrorMessage(errorMessage);
	}

	public PeerDefinition getResult() {
		return result;
	}

	@Override
	public void handleEvent(Event event) {
		validate();
	}
}
