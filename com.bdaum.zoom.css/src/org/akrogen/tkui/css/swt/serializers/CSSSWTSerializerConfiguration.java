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
package org.akrogen.tkui.css.swt.serializers;

import org.akrogen.tkui.css.core.serializers.CSSSerializerConfiguration;

/**
 * {@link CSSSerializerConfiguration} configuration used to get style of SWT control.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class CSSSWTSerializerConfiguration extends CSSSerializerConfiguration {

	public static final CSSSerializerConfiguration INSTANCE = new CSSSWTSerializerConfiguration();

	public CSSSWTSerializerConfiguration() {
		super.addAttributeFilter("style");
	}

}
