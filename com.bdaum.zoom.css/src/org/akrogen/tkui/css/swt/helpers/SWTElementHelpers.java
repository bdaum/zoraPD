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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.akrogen.tkui.css.core.dom.CSSStylableElement;
import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.dom.SWTElement;
import org.akrogen.tkui.css.swt.dom.html.SWTHTMLElement;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Element;

/**
 * SWT Helper to link w3c Element with SWT widget.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class SWTElementHelpers {

	public static final String SWT_ELEMENT_KEY = "org.akrogen.tkui.core.css.swt.dom.SWTElement.ELEMENT";
	public static final String SWT_NODELIST_KEY = "org.akrogen.tkui.core.css.swt.dom.SWTElement.NODELIST";

	private static final Class[] ELEMENT_CONSTRUCTOR_PARAM = { Widget.class,
			CSSEngine.class };

	/**
	 * Return the w3c Element linked to the SWT widget.
	 * 
	 * @param widget
	 * @return
	 */
	public static Element getElement(Widget widget, CSSEngine engine,
			Class classElement) throws NoSuchMethodException,
			InvocationTargetException, InstantiationException,
			IllegalAccessException {
		Constructor constructor = classElement
				.getConstructor(ELEMENT_CONSTRUCTOR_PARAM);
		Object[] o = { widget, engine };
		Element newElement = (Element) constructor.newInstance(o);
		return newElement;
	}

	/**
	 * Return the w3c Element linked to the SWT widget.
	 * 
	 * @param widget
	 * @return
	 */
	public static Element getElement(Widget widget, CSSEngine engine) {
		try {
			return getElement(widget, engine, SWTElement.class);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Return the w3c Element linked to the SWT widget.
	 * 
	 * @param widget
	 * @return
	 */
	public static Element getHTMLElement(Widget widget, CSSEngine engine) {
		try {
			return getElement(widget, engine, SWTHTMLElement.class);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Return the SWT Control wich is wrapped to the object <code>element</code>.
	 * 
	 * @param element
	 * @return
	 */
	public static Control getControl(Object element) {
		if (element instanceof Control) {
			return (Control) element;
		} else {
			if (element instanceof CSSStylableElement) {
				CSSStylableElement elt = (CSSStylableElement) element;
				Object widget = elt.getNativeWidget();
				if (widget instanceof Control)
					return (Control) widget;
			}
		}
		return null;
	}
}
