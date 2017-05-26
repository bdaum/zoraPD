/*******************************************************************************
 * Copyright (c) 2009 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.net.ui.internal;

import com.bdaum.zoom.ui.internal.IHelpContexts;

@SuppressWarnings("restriction")
public interface HelpContextIds  extends IHelpContexts{
	public static final String PREFIX = NetActivator.PLUGIN_ID + "."; //$NON-NLS-1$

	/* Pages */
	public static final String INTERNET_PREFERENCE_PAGE = PREFIX
			+ "internet" + PAGE_POSTFIX; //$NON-NLS-1$

}
