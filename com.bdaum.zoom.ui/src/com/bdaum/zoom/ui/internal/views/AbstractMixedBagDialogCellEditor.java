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
 * (c) 2014 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.views;

import java.util.List;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.cat.model.asset.Asset;

public abstract class AbstractMixedBagDialogCellEditor extends DialogCellEditor {

	protected String[] commonItems;
	protected List<Asset> assets;

	public AbstractMixedBagDialogCellEditor() {
		super();
	}

	public AbstractMixedBagDialogCellEditor(Composite parent) {
		super(parent);
	}

	public AbstractMixedBagDialogCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	public void setCommonItems(String[] commonItems, List<Asset> assets) {
		this.commonItems = commonItems;
		this.assets = assets;
	}

}