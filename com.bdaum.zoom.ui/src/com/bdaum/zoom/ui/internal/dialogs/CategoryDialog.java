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

import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;


public class CategoryDialog extends ZTitleAreaDialog {
	private Object category;
	private CategoryGroup group;
	private int style;

	public CategoryDialog(Shell parentShell, Object category, int style) {
		super(parentShell, HelpContextIds.CATEGORY_DIALOG);
		this.category = category;
		this.style = style;
	}

	
	@Override
	public void create() {
		super.create();
		setTitle(Messages.CategoryDialog_image_categories);
		setMessage(Messages.CategoryDialog_mark_existing_or_enter_new);
	}

	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);
		group = new CategoryGroup(comp, category, style);
		return comp;
	}
	
	
	@Override
	protected void okPressed() {
		group.commit();
		super.okPressed();
	}

	public Object getResult() {
		return group.getResult();
	}


}
