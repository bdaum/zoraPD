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
 * Modifications (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.juploadr.uploadapi.smugrest.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.scohen.juploadr.app.PhotoSet;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.smugrest.SmugmugRestApi;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;

@SuppressWarnings("restriction")
public class AlbumPolicyDialog extends ZTitleAreaDialog {

	private final Session session;
	private ComboViewer viewer;
	private RadioButtonGroup albumGroup;

	public AlbumPolicyDialog(Shell parentShell, Session session) {
		super(parentShell);
		this.session = session;
	}

	@Override
	public void create() {
		super.create();
		setMessage(Messages.AlbumPolicyDialog_please_select_policy);
		fillValues();
		updateButtons();
	}

	private void updateButtons() {
		boolean enabled = !viewer.getSelection().isEmpty();
		getShell().setModified(enabled);
		getButton(IDialogConstants.OK_ID).setEnabled(enabled);
	}

	private void fillValues() {
		CommunityAccount account = session.getAccount();
		int policy = account.getAlbumPolicy();
		albumGroup.setSelection(policy == SmugmugRestApi.SELECT ? 0 : 1);
		PhotoSet dfltAlbum = account.findPhotoset(account.getDefaultAlbum());
		if (dfltAlbum != null)
			viewer.setSelection(new StructuredSelection(dfltAlbum));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		albumGroup = new RadioButtonGroup(area, null, SWT.NONE, Messages.AlbumPolicyDialog_select_by_local_album,
				Messages.AlbumPolicyDialog_use_default_target_album);
		albumGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite viewerGroup = new Composite(area, SWT.NONE);
		viewerGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewerGroup.setLayout(new GridLayout(2, false));

		Label label = new Label(viewerGroup, SWT.NONE);
		label.setText(Messages.AlbumPolicyDialog_default_album);

		viewer = new ComboViewer(viewerGroup, SWT.READ_ONLY);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof PhotoSet)
					return ((PhotoSet) element).getTitle();
				return element.toString();
			}
		});
		viewer.setComparator(new ViewerComparator());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		viewer.setInput(session.getAccount().getPhotosets());
		return area;
	}

	@Override
	protected void okPressed() {
		saveValues();
		session.setAlbumPolicy(albumGroup.getSelection() == 0 ? SmugmugRestApi.SELECT : SmugmugRestApi.USEDEFAULT);
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (!selection.isEmpty())
			session.setDefaultAlbum(((PhotoSet) selection.getFirstElement()));
		super.okPressed();
	}

	private void saveValues() {
		CommunityAccount account = session.getAccount();
		account.setAlbumPolicy(albumGroup.getSelection() == 0 ? SmugmugRestApi.SELECT : SmugmugRestApi.USEDEFAULT);
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (!selection.isEmpty())
			account.setDefaultAlbum(((PhotoSet) selection.getFirstElement()).getTitle());
		account.save();
	}

}
