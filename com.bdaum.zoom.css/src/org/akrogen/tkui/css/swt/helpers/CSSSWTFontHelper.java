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
package org.akrogen.tkui.css.swt.helpers;

import org.akrogen.tkui.css.core.css2.CSS2FontHelper;
import org.akrogen.tkui.css.core.css2.CSS2FontPropertiesHelpers;
import org.akrogen.tkui.css.core.css2.CSS2PrimitiveValueImpl;
import org.akrogen.tkui.css.core.dom.properties.css2.CSS2FontProperties;
import org.akrogen.tkui.css.core.dom.properties.css2.CSS2FontPropertiesImpl;
import org.akrogen.tkui.css.core.engine.CSSElementContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * CSS SWT Font Helper to :
 * <ul>
 * <li>get CSS2FontProperties from Font of SWT Control.</li>
 * <li>get Font of SWT Control from CSS2FontProperties.</li>
 * </ul>
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class CSSSWTFontHelper {

	/**
	 * Get CSS2FontProperties from Control stored into Data of Control. If
	 * CSS2FontProperties doesn't exist, create it from Font of Control and
	 * store it into Data of Control.
	 * 
	 * @param control
	 * @return
	 */
	public static CSS2FontProperties getCSS2FontProperties(Control control,
			CSSElementContext context) {
		// Search into Data of Control if CSS2FontProperties exist.
		CSS2FontProperties fontProperties = CSS2FontPropertiesHelpers
				.getCSS2FontProperties(context);
		if (fontProperties == null) {
			// CSS2FontProperties doesn't exist, create it
			Font font = control.getFont();
			fontProperties = getCSS2FontProperties(font);
			// store into ClientProperty the CSS2FontProperties
			CSS2FontPropertiesHelpers.setCSS2FontProperties(fontProperties,
					context);
		}
		return fontProperties;
	}

	/**
	 * Build CSS2FontProperties from SWT Font.
	 * 
	 * @param font
	 * @return
	 */
	public static CSS2FontProperties getCSS2FontProperties(Font font) {
		// Create CSS Font Properties
		FontData fontData = getFirstFontData(font);
		CSS2FontProperties fontProperties = new CSS2FontPropertiesImpl();
		// Update font-family
		String fontFamily = getFontFamily(font);
		fontProperties.setFamily(new CSS2PrimitiveValueImpl(fontFamily));
		// Update font-size
		int fontSize = fontData.getHeight();
		fontProperties.setSize(new CSS2PrimitiveValueImpl(fontSize));
		// Update font-weight
		String fontWeight = getFontWeight(font);
		fontProperties.setWeight((new CSS2PrimitiveValueImpl(fontWeight)));
		// Update font-style
		String fontStyle = getFontStyle(font);
		fontProperties.setStyle((new CSS2PrimitiveValueImpl(fontStyle)));
		return fontProperties;
	}

	/**
	 * Get CSS2FontProperties from Font of JComponent and store
	 * CSS2FontProperties instance into ClientProperty of JComponent.
	 * 
	 * @param component
	 * @return
	 */
	public static Font getFont(CSS2FontProperties fontProperties,
			Control control) {
		FontData oldFontData = getFirstFontData(control.getFont());
		return getFont(fontProperties, oldFontData, control.getDisplay());
	}

	public static Font getFont(CSS2FontProperties fontProperties,
			FontData oldFontData, Display display) {
		FontData newFontData = getFontData(fontProperties, oldFontData);
		return new Font(display, newFontData);
	}

	/**
	 * Return FontData from {@link CSS2FontProperties}.
	 * 
	 * @param fontProperties
	 * @param control
	 * @return
	 */
	public static FontData getFontData(CSS2FontProperties fontProperties,
			FontData oldFontData) {
		FontData newFontData = new FontData();
		// Style
		int style = getSWTStyle(fontProperties, oldFontData);
		newFontData.setStyle(style);
		// Height
		CSSPrimitiveValue cssFontSize = fontProperties.getSize();
		if (cssFontSize != null) {
			newFontData.setHeight((int) (cssFontSize)
					.getFloatValue(CSSPrimitiveValue.CSS_PT));
		} else {
			if (oldFontData != null)
				newFontData.setHeight(oldFontData.getHeight());
		}
		// Family
		CSSPrimitiveValue cssFontFamily = fontProperties.getFamily();
		if (cssFontFamily != null)
			newFontData.setName(cssFontFamily.getStringValue());
		else {
			if (oldFontData != null)
				newFontData.setName(oldFontData.getName());
		}
		return newFontData;
	}

	/**
	 * Return SWT style Font from {@link CSS2FontProperties}.
	 * 
	 * @param fontProperties
	 * @param control
	 * @return
	 */
	public static int getSWTStyle(CSS2FontProperties fontProperties,
			FontData fontData) {
		if (fontData == null)
			return SWT.NONE;

		int fontStyle = fontData.getStyle();
		// CSS2 font-style
		CSSPrimitiveValue cssFontStyle = fontProperties.getStyle();
		if (cssFontStyle != null) {
			String style = cssFontStyle.getStringValue();
			if ("italic".equals(style)) {
				fontStyle = fontStyle | SWT.ITALIC;
			} else {
				if (fontStyle == (fontStyle | SWT.ITALIC))
					fontStyle = fontStyle ^ SWT.ITALIC;
			}
		}
		// CSS font-weight
		CSSPrimitiveValue cssFontWeight = fontProperties.getWeight();
		if (cssFontWeight != null) {
			String weight = cssFontWeight.getStringValue();
			if ("bold".equals(weight.toLowerCase())) {
				fontStyle = fontStyle | SWT.BOLD;
			} else {
				if (fontStyle == (fontStyle | SWT.BOLD))
					fontStyle = fontStyle ^ SWT.BOLD;
			}
		}
		return fontStyle;
	}

	/**
	 * Return CSS Value font-family from Control Font
	 * 
	 * @param control
	 * @return
	 */
	public static String getFontFamily(Control control) {
		return getFontFamily(control.getFont());
	}

	/**
	 * Return CSS Value font-family from SWT Font
	 * 
	 * @param font
	 * @return
	 */
	public static String getFontFamily(Font font) {
		FontData fontData = getFirstFontData(font);
		return getFontFamily(fontData);
	}

	public static String getFontFamily(FontData fontData) {
		if (fontData != null) {
			String family = fontData.getName();
			return CSS2FontHelper.getFontFamily(family);
		}
		return null;
	}

	/**
	 * Return CSS Value font-size from Control Font
	 * 
	 * @param control
	 * @return
	 */
	public static String getFontSize(Control control) {
		return getFontSize(control.getFont());
	}

	/**
	 * Return CSS Value font-size from SWT Font
	 * 
	 * @param font
	 * @return
	 */
	public static String getFontSize(Font font) {
		FontData fontData = getFirstFontData(font);
		return getFontSize(fontData);
	}

	public static String getFontSize(FontData fontData) {
		if (fontData != null)
			return CSS2FontHelper.getFontSize(fontData.getHeight());
		return null;
	}

	/**
	 * Return CSS Value font-style from SWT Font
	 * 
	 * @param control
	 * @return
	 */
	public static String getFontStyle(Control control) {
		return getFontStyle(control.getFont());
	}

	/**
	 * Return CSS Value font-style from SWT Font
	 * 
	 * @param font
	 * @return
	 */
	public static String getFontStyle(Font font) {
		FontData fontData = getFirstFontData(font);
		return getFontStyle(fontData);
	}

	public static String getFontStyle(FontData fontData) {
		boolean isItalic = false;
		if (fontData != null) {
			isItalic = isItalic(fontData);
		}
		return CSS2FontHelper.getFontStyle(isItalic);
	}

	public static boolean isItalic(FontData fontData) {
		int fontStyle = fontData.getStyle();
		return ((fontStyle | SWT.ITALIC) == fontStyle);
	}

	/**
	 * Return CSS Value font-weight from Control Font
	 * 
	 * @param control
	 * @return
	 */
	public static String getFontWeight(Control control) {
		return getFontWeight(control.getFont());
	}

	/**
	 * Return CSS Value font-weight from Control Font
	 * 
	 * @param font
	 * @return
	 */
	public static String getFontWeight(Font font) {
		FontData fontData = getFirstFontData(font);
		return getFontWeight(fontData);
	}

	public static String getFontWeight(FontData fontData) {
		boolean isBold = false;
		if (fontData != null) {
			isBold = isBold(fontData);
		}
		return CSS2FontHelper.getFontWeight(isBold);
	}

	public static boolean isBold(FontData fontData) {
		int fontStyle = fontData.getStyle();
		return ((fontStyle | SWT.BOLD) == fontStyle);
	}

	/**
	 * Return CSS Value font-family from Control Font
	 * 
	 * @param control
	 * @return
	 */
	public static String getFontComposite(Control control) {
		return getFontComposite(control.getFont());
	}

	/**
	 * Return CSS Value font-family from SWT Font
	 * 
	 * @param font
	 * @return
	 */
	public static String getFontComposite(Font font) {
		FontData fontData = getFirstFontData(font);
		return getFontComposite(fontData);
	}

	public static String getFontComposite(FontData fontData) {
		if (fontData != null) {
			StringBuffer composite = new StringBuffer();
			// font-family
			composite.append(getFontFamily(fontData));
			composite.append(" ");
			// font-size
			composite.append(getFontSize(fontData));
			composite.append(" ");
			// font-weight
			composite.append(getFontWeight(fontData));
			composite.append(" ");
			// font-style
			composite.append(getFontStyle(fontData));
			return composite.toString();
		}
		return null;
	}

	/**
	 * Return first FontData from Control Font.
	 * 
	 * @param control
	 * @return
	 */
	public static FontData getFirstFontData(Control control) {
		Font font = control.getFont();
		if (font == null)
			return null;
		return getFirstFontData(font);
	}

	/**
	 * 
	 * Return first FontData from SWT Font.
	 * 
	 * @param font
	 * @return
	 */
	public static FontData getFirstFontData(Font font) {
		FontData[] fontDatas = font.getFontData();
		if (fontDatas == null || fontDatas.length < 1)
			return null;
		return fontDatas[0];
	}
}
