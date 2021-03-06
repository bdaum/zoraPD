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

import org.akrogen.tkui.css.core.dom.properties.CSSBorderProperties;
import org.akrogen.tkui.css.core.resources.IResourcesRegistry;
import org.akrogen.tkui.css.swt.CSSSWTConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * SWT Helper to transform CSS w3c object (org.w3c.dom.css.RGBColor....) into
 * SWT object (org.eclipse.swt.graphics.Color...).
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class CSSSWTHelpers {

	/*--------------- SWT Font Helper -----------------*/

	/*--------------- SWT Cursor Helper -----------------*/

	/*--------------- SWT Image Helper -----------------*/

	/*--------------- SWT Border Helper -----------------*/

	public static PaintListener createBorderPaintListener(
			final Control control, final IResourcesRegistry resourcesRegistry) {
		return new PaintListener() {
			public void paintControl(PaintEvent e) {
				CSSBorderProperties border = (CSSBorderProperties) control
						.getData(CSSSWTConstants.CONTROL_CSS2BORDER_KEY);
				if (border == null)
					return;
				int width = border.getWidth();
				GC gc = e.gc;
				CSSPrimitiveValue value = border.getColor();
				if (value == null)
					return;
				Color color = CSSSWTColorHelper.getSWTColor(value, control
						.getDisplay());
				if (color != null)
					gc.setForeground(color);
				Rectangle rect = control.getBounds();
				if (width == 0) {
					Rectangle rect1 = new Rectangle(rect.x - width, rect.y
							- width, rect.width + 2 * width, rect.height + 2
							* width);
					gc.fillRectangle(rect1);
				} else {
					String borderStyle = border.getStyle();
					// Top Line
					gc.setLineStyle(getLineStyle(borderStyle));
					gc.setLineWidth(width);
					gc.drawLine(rect.x, rect.y - 1, rect.width + 2 * width,
							rect.y - 1);
					// Bottom Line
					gc.setLineStyle(getLineStyle(borderStyle));
					gc.setLineWidth(width);
					gc.drawLine(rect.x, rect.y + rect.height + 1, rect.width
							+ 2 * width, rect.y + rect.height + 1);
					// Left Line
					gc.setLineStyle(getLineStyle(borderStyle));
					gc.setLineWidth(width);
					gc.drawLine(rect.x - 1, rect.y - 1, rect.x - 1, rect.y
							+ rect.height + 1);
					// Right Line
					gc.setLineStyle(getLineStyle(borderStyle));
					gc.setLineWidth(width);
					gc.drawLine(rect.width + 2 * width, rect.y - 1, rect.width
							+ 2 * width, rect.y + rect.height + 1);
				}
			}
		};
	}

	public static int getLineStyle(String borderStyle) {
		if (borderStyle == null)
			return SWT.LINE_SOLID;
		// one hidden dotted dashed solid double groove ridge inset outset
		if ("dashed".equals(borderStyle)) {
			return SWT.LINE_DASH;
		}
		if ("dotted".equals(borderStyle)) {
			return SWT.LINE_DOT;
		}
		return SWT.LINE_SOLID;
	}
}
