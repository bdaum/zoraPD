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
package org.akrogen.tkui.css.swt.properties.css2;

import org.akrogen.tkui.css.core.dom.properties.CSSBorderProperties;
import org.akrogen.tkui.css.core.dom.properties.ICSSPropertyHandler2;
import org.akrogen.tkui.css.core.dom.properties.css2.AbstractCSSPropertyBorderHandler;
import org.akrogen.tkui.css.core.dom.properties.css2.ICSSPropertyBorderHandler;
import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.core.impl.dom.properties.CSSBorderPropertiesImpl;
import org.akrogen.tkui.css.swt.CSSSWTConstants;
import org.akrogen.tkui.css.swt.helpers.CSSSWTHelpers;
import org.akrogen.tkui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyBorderSWTHandler extends
		AbstractCSSPropertyBorderHandler implements ICSSPropertyHandler2 {

	public final static ICSSPropertyBorderHandler INSTANCE = new CSSPropertyBorderSWTHandler();

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			Composite parent = control.getParent();
			if (parent == null)
				return true;
			CSSBorderProperties border = (CSSBorderProperties) control
					.getData(CSSSWTConstants.CONTROL_CSS2BORDER_KEY);
			if (border == null) {
				border = new CSSBorderPropertiesImpl();
				control.setData(CSSSWTConstants.CONTROL_CSS2BORDER_KEY, border);
				parent.addPaintListener(CSSSWTHelpers
						.createBorderPaintListener(control, engine
								.getResourcesRegistry()));
			}
			super.applyCSSProperty(border, property, value, pseudo, engine);
			return true;
		} else {
			if (element instanceof CSSBorderProperties) {
				super
						.applyCSSProperty(element, property, value, pseudo,
								engine);
				return true;
			}
		}
		return false;

	}

	public void onAllCSSPropertiesApplyed(Object element, CSSEngine engine)
			throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			Composite parent = control.getParent();
			if (parent != null)
				parent.redraw();
		}
	}

}
