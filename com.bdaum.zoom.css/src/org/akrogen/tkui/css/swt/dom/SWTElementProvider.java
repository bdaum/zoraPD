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
package org.akrogen.tkui.css.swt.dom;

import org.akrogen.tkui.css.core.dom.IElementProvider;
import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Element;

/**
 * {@link IElementProvider} SWT implementation to retrieve w3c Element {@link SWTElement} linked
 * to SWT widget.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class SWTElementProvider implements IElementProvider {

	public static final IElementProvider INSTANCE = new SWTElementProvider();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akrogen.tkui.core.css.dom.IElementProvider#getElement(java.lang.Object)
	 */
	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof Widget) {
			// element is SWT Widget
			Widget widget = (Widget) element;
			// Return the w3c Element linked to the SWT widget.
			return SWTElementHelpers.getElement(widget, engine);
		}
		return null;
	}
}
