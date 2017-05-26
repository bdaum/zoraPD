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
package org.akrogen.tkui.css.swt.engine.html;

/**
 * CSS SWT Engine implementation which configure CSSEngineImpl to apply styles
 * to SWT widgets with static handler strategy and manage HTML selector.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
import org.akrogen.tkui.css.swt.dom.html.SWTHTMLElementProvider;
import org.akrogen.tkui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.swt.widgets.Display;

public class CSSSWTHTMLEngineImpl extends CSSSWTEngineImpl {

	public CSSSWTHTMLEngineImpl(Display display) {
		this(display, false);
	}

	public CSSSWTHTMLEngineImpl(Display display, boolean lazyApplyingStyles) {
		super(display, lazyApplyingStyles);
		// Register SWT HTML Element Provider to retrieve
		// w3c Element SWTHTMLElement coming from SWT widget.
		super.setElementProvider(SWTHTMLElementProvider.INSTANCE);
	}
}
