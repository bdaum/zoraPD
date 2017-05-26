package com.bdaum.zoom.ui.internal.views;

import org.eclipse.swt.widgets.Display;

import com.bdaum.zoom.core.QueryField;

public interface IFieldUpdater {

	boolean isDisposed();

	Display getDisplay();

	void updateField(QueryField qfield, FieldEntry fieldEntry);

}
