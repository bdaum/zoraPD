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
package org.akrogen.tkui.css.core.impl.dom;

import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSS3Properties;

/**
 * w3c {@link CSS3Properties} implementation.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class CSS3PropertiesImpl extends CSS2PropertiesImpl implements
		CSS3Properties {

	public CSS3PropertiesImpl(Object widget, CSSEngine engine) {
		super(widget, engine);
	}
}
