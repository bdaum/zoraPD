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