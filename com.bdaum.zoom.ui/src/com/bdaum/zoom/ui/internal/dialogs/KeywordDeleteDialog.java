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

import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.widgets.RadioButtonGroup;

public class KeywordDeleteDialog extends ZTitleAreaDialog {

	public static final int REMOVE = 0;
	public static final int LEAVE = 2;
	private final String kw;
	private final List<AssetImpl> assets;
	private int policy = -1;
private RadioButtonGroup buttonGroup;

	public KeywordDeleteDialog(Shell parentShell, final String kw,
			List<AssetImpl> assets) {
		super(parentShell);
		this.kw = kw;
		this.assets = assets;
	}

	@Override
	public void create() {
		super.create();
		setMessage(assets.size() == 1 ? NLS.bind(
				Messages.KeywordDeleteDialog_used_in_one_image,
				kw) : NLS.bind(Messages.KeywordDeleteDialog_keyword_is_used,
				kw, assets.size()));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		buttonGroup = new RadioButtonGroup(composite, null, SWT.NONE, Messages.KeywordDeleteDialog_remove_keyword, Messages.KeywordDeleteDialog_remove_from_cat);
		buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonGroup.setSelection(0);
		return area;
	}

	public int getPolicy() {
		return policy;
	}

	@Override
	protected void okPressed() {
		policy = buttonGroup.getSelection() == 0 ? REMOVE : LEAVE;
		super.okPressed();
	}
}
