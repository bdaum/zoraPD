/*******************************************************************************
 * Copyright (c) 2008, Original authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo ZERR <angelo.zerr@gmail.com>
 *******************************************************************************/
package org.akrogen.tkui.css.swt.properties.css2.lazy.classification;

import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyCursorHandler extends AbstractCSSPropertySWTHandler {

	@Override
	public void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			Cursor cursor = (Cursor) engine.convert(value, Cursor.class,
					control.getDisplay());
			control.setCursor(cursor);
		}
	}

	@Override
	public String retrieveCSSProperty(Control control, String property,
			CSSEngine engine) throws Exception {
		Cursor cursor = control.getCursor();
		return engine.convert(cursor, Cursor.class, null);
	}
}
