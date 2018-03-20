/*******************************************************************************
 * Copyright (c) 2009-2010 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.rcp.internal.intro;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.rcp.internal.intro.messages"; //$NON-NLS-1$
	public static String Intro_config_text;
	public static String Intro_config_title;
	public static String Intro_config_tooltip;
	public static String Intro_dictionaries_key;
	public static String Intro_features_text;
	public static String Intro_features_title;
	public static String Intro_features_tooltip;
	public static String Intro_help_text;
	public static String Intro_help_title;
	public static String Intro_help_tooltip;
	public static String Intro_homepage_text;
	public static String Intro_homepage_tooltip;
	public static String Intro_hompage_title;
	public static String Intro_update_text;
	public static String Intro_update_title;
	public static String Intro_update_tooltip;
	public static String Intro_version;
	public static String Intro_welcome;
	public static String Intro_whats_new_text;
	public static String Intro_whats_new_title;
	public static String Intro_whats_new_tooltip;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
