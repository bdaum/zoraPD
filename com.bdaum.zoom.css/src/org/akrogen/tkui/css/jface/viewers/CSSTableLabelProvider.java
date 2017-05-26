package org.akrogen.tkui.css.jface.viewers;

import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public abstract class CSSTableLabelProvider extends CSSBaseLabelProvider
		implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {

	public CSSTableLabelProvider(CSSEngine engine, TableViewer tableViewer) {
		super(engine, tableViewer);
	}

	public Color getBackground(Object element, int columnIndex) {
		return getColor(element, "background-color", columnIndex);
	}

	public Font getFont(Object element, int columnIndex) {
		return getFont(element, "font", columnIndex);
	}

	public Color getForeground(Object element, int columnIndex) {
		return getColor(element, "color", columnIndex);
	}

}
