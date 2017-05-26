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
package org.akrogen.tkui.css.swt.selectors;

import org.akrogen.tkui.css.core.dom.selectors.IDynamicPseudoClassesHandler;
import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.widgets.Control;

/**
 * Abstract SWT class to manage dynamic pseudo classes handler like (...:focus,
 * ...:hover) with SWT Control.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public abstract class AbstractDynamicPseudoClassesControlHandler implements
		IDynamicPseudoClassesHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akrogen.tkui.core.css.dom.selectors.IDynamicPseudoClassesHandler#intialize(java.lang.Object,
	 *      org.akrogen.tkui.core.css.engine.CSSEngine)
	 */
	public void intialize(final Object element, final CSSEngine engine) {
		final Control control = SWTElementHelpers.getControl(element);
		if (control == null)
			return;
		intialize(control, engine);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akrogen.tkui.core.css.dom.selectors.IDynamicPseudoClassesHandler#dispose(java.lang.Object,
	 *      org.akrogen.tkui.core.css.engine.CSSEngine)
	 */
	public void dispose(Object element, CSSEngine engine) {
		Control control = SWTElementHelpers.getControl(element);
		if (control == null)
			return;
		dispose(control, engine);
	}

	/**
	 * Initialize the SWT <code>control</code>. In this method you can add
	 * SWT Listener to the control.
	 * 
	 * @param control
	 * @param engine
	 */
	protected abstract void intialize(Control control, CSSEngine engine);

	/**
	 * Dispose the SWT <code>control</code>. In this method you can remove
	 * SWT Listener to the control.
	 * 
	 * @param control
	 * @param engine
	 */
	protected abstract void dispose(Control control, CSSEngine engine);

}
