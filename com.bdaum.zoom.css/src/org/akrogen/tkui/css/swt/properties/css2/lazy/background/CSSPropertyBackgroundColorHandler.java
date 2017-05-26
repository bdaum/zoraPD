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
package org.akrogen.tkui.css.swt.properties.css2.lazy.background;

import org.akrogen.tkui.css.core.dom.properties.Gradient;
import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.akrogen.tkui.css.swt.properties.GradientBackgroundListener;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyBackgroundColorHandler extends
		AbstractCSSPropertySWTHandler {

	@Override
	public void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			Color newColor = (Color) engine.convert(value, Color.class, control
					.getDisplay());
			if (control instanceof CTabFolder && "selected".equals(pseudo)) {
				((CTabFolder) control).setSelectionBackground(newColor);
			} else {
				control.setBackground(newColor);
			}
		} else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			Gradient grad = (Gradient) engine.convert(value, Gradient.class,
					control.getDisplay());
			GradientBackgroundListener.handle(control, grad);
		}

	}

	@Override
	public String retrieveCSSProperty(Control control, String property,
			CSSEngine engine) throws Exception {
		Color color = control.getBackground();
		return engine.convert(color, Color.class, null);
	}

}
