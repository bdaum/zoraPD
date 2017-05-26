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
package org.akrogen.tkui.css.swt.engine;

import org.eclipse.swt.widgets.Display;

/**
 * CSS SWT Engine implementation which configure CSSEngineImpl to apply styles
 * to SWT widgets with lazy handler strategy.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class CSSSWTLazyHandlerEngineImpl extends AbstractCSSSWTEngineImpl {

	public CSSSWTLazyHandlerEngineImpl(Display display) {
		super(display);
	}

	public CSSSWTLazyHandlerEngineImpl(Display display,
			boolean lazyApplyingStyles) {
		super(display, lazyApplyingStyles);
	}

	@Override
	protected void initializeCSSPropertyHandlers() {
		super
				.registerPackage("org.akrogen.tkui.css.swt.properties.css2.lazy.classification");
		super
				.registerPackage("org.akrogen.tkui.css.swt.properties.css2.lazy.border");
		super
				.registerPackage("org.akrogen.tkui.css.swt.properties.css2.lazy.font");
		super
				.registerPackage("org.akrogen.tkui.css.swt.properties.css2.lazy.background");
		super
				.registerPackage("org.akrogen.tkui.css.swt.properties.css2.lazy.text");

	}

}
