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
package org.akrogen.tkui.css.swt.properties.css2.lazy.font;

import org.akrogen.tkui.css.core.dom.properties.ICSSPropertyHandler2;
import org.akrogen.tkui.css.core.dom.properties.css2.CSS2FontProperties;
import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.helpers.CSSSWTFontHelper;
import org.akrogen.tkui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;

public class CSSPropertyFontSWTHandler2 implements ICSSPropertyHandler2 {

	public static final ICSSPropertyHandler2 INSTANCE = new CSSPropertyFontSWTHandler2();

	public void onAllCSSPropertiesApplyed(Object element, CSSEngine engine)
			throws Exception {
		final Control control = SWTElementHelpers.getControl(element);
		if (control == null)
			return;
		CSS2FontProperties fontProperties =CSSSWTFontHelper.getCSS2FontProperties(
				control, engine.getCSSElementContext(control));
		if (fontProperties == null)
			return;

		Font font = (Font) engine.convert(fontProperties, Font.class, control);
		control.setFont(font);

	}

}
