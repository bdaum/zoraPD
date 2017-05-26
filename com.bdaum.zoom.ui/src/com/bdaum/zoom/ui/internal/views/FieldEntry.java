package com.bdaum.zoom.ui.internal.views;

import com.bdaum.zoom.core.QueryField;

public class FieldEntry {

	public static final FieldEntry PENDING = new FieldEntry("...", false, true); //$NON-NLS-1$
	public static final FieldEntry NOTHING = new FieldEntry(
			QueryField.VALUE_NOTHING, false, true);

	public Object value;
	public boolean editable;
	public boolean applicable;

	public FieldEntry(Object value, boolean editable, boolean applicable) {
		this.value = value;
		this.editable = editable;
		this.applicable = applicable;
	}
}