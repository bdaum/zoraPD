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
package org.akrogen.tkui.css.swt.properties.converters;

import org.akrogen.tkui.css.core.dom.properties.converters.AbstractCSSValueConverter;
import org.akrogen.tkui.css.core.dom.properties.converters.ICSSValueConverter;
import org.akrogen.tkui.css.core.dom.properties.converters.ICSSValueConverterConfig;
import org.akrogen.tkui.css.core.dom.properties.css2.CSS2FontProperties;
import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSValue;

/**
 * CSS Value converter to convert :
 * <ul>
 * <li>CSS Value to {@link FontData}</li>.
 * <li>{@link FontData} to String CSS Value</li>
 * </ul>
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class CSSValueSWTFontDataConverterImpl extends AbstractCSSValueConverter {

	public static final ICSSValueConverter INSTANCE = new CSSValueSWTFontDataConverterImpl();

	public CSSValueSWTFontDataConverterImpl(Object toType) {
		super(toType);
	}

	public CSSValueSWTFontDataConverterImpl() {
		super(FontData.class);
	}

	public Object convert(CSSValue value, CSSEngine engine, Object context)
			throws Exception {
		FontData fontData = null;
		if (context != null) {
			if (context instanceof Display) {
				Display display = (Display) context;
				Font font = display.getSystemFont();
				fontData = CSSSWTFontHelper.getFirstFontData(font);
			}
			if (context instanceof Control) {
				Control control = (Control) context;
				Font font = control.getFont();
				fontData = CSSSWTFontHelper.getFirstFontData(font);
			}
		}
		if (fontData != null) {
			if (value instanceof CSS2FontProperties) {
				return CSSSWTFontHelper.getFontData((CSS2FontProperties) value,
						fontData);
			}
		}
		return null;
	}

	public String convert(Object value, CSSEngine engine, Object context,
			ICSSValueConverterConfig config) throws Exception {
		FontData fontData = (FontData) value;
		if (context instanceof String) {
			String property = (String) context;
			if ("font-family".equals(property))
				return CSSSWTFontHelper.getFontFamily(fontData);
			if ("font-size".equals(property))
				return CSSSWTFontHelper.getFontSize(fontData);
			if ("font-style".equals(property))
				return CSSSWTFontHelper.getFontStyle(fontData);
			if ("font-weight".equals(property))
				return CSSSWTFontHelper.getFontWeight(fontData);
			if ("font".equals(property))
				return CSSSWTFontHelper.getFontComposite(fontData);
		}
		return null;

	}

	protected Display getDisplay(Object context) {
		if (context instanceof Display)
			return (Display) context;
		if (context instanceof Control)
			return ((Control) context).getDisplay();
		return null;
	}
}
