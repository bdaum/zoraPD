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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;

public class SupplementalCategoryDialog extends ZTitleAreaDialog {
	private String[] categories;
	private SupplementalCategoryGroup group;
	private boolean multiple;

	public SupplementalCategoryDialog(Shell parentShell, Object categories,
			boolean multiple) {
		super(parentShell, HelpContextIds.SUPPLEMENTALCATEGORY_DIALOG);
		this.multiple = multiple;
		this.categories = (String[]) categories;
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.SupplementalCategoryDialog_supplemental_image_categories);
		setMessage(Messages.SupplementalCategoryDialog_mark_existing
				+ (multiple ? "\n" + Messages.SupplementalCategoryDialog_only_common_categories : "")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		group = new SupplementalCategoryGroup(area, categories);
		return area;
	}

	@Override
	protected void okPressed() {
		group.commit();
		super.okPressed();
	}

	public BagChange<String> getResult() {
		return group.getResult();
	}

}
