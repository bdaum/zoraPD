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
package org.akrogen.tkui.css.swt.properties;

import org.akrogen.tkui.css.core.dom.properties.ICSSPropertyHandler;
import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

/**
 * Abstract CSS Property SWT Handler to check if the <code>element</code>
 * coming from applyCSSProperty and retrieveCSSProperty methods is SWT Control.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public abstract class AbstractCSSPropertySWTHandler implements
		ICSSPropertyHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akrogen.tkui.core.css.dom.properties.ICSSPropertyHandler#applyCSSProperty(java.lang.Object,
	 *      java.lang.String, org.w3c.dom.css.CSSValue, java.lang.String,
	 *      org.akrogen.tkui.core.css.engine.CSSEngine)
	 */
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			// The SWT control is retrieved
			// the apply CSS property can be done.
			this.applyCSSProperty(control, property, value, pseudo, engine);
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akrogen.tkui.core.css.dom.properties.ICSSPropertyHandler#retrieveCSSProperty(java.lang.Object,
	 *      java.lang.String, org.akrogen.tkui.core.css.engine.CSSEngine)
	 */
	public String retrieveCSSProperty(Object element, String property,
			CSSEngine engine) throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			// The SWT control is retrieved
			// the retrieve CSS property can be done.
			return retrieveCSSProperty(control, property, engine);
		}
		return null;
	}

	/**
	 * Apply CSS Property <code>property</code> (ex : background-color) with
	 * CSSValue <code>value</code> (ex : red) into the SWT
	 * <code>control</code> (ex : SWT Text, SWT Label).
	 * 
	 * @param control
	 * @param property
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	protected abstract void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Retrieve CSS value (ex : red) of CSS Property <code>property</code> (ex :
	 * background-color) from the SWT <code>control</code> (ex : SWT Text, SWT
	 * Label).
	 * 
	 * @param control
	 * @param property
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	protected abstract String retrieveCSSProperty(Control control,
			String property, CSSEngine engine) throws Exception;

}
