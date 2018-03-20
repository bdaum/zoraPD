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

package com.bdaum.zoom.net.ui.internal.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.net.ui.internal.preferences.messages"; //$NON-NLS-1$
	public static String InternetPreferencePage_Common_Internet_settings;
	public static String InternetPreferencePage_connection_timeout;
	public static String InternetPreferencePage_Direct_connection;
	public static String InternetPreferencePage_edit;
	public static String InternetPreferencePage_edit_tooltip;
	public static String InternetPreferencePage_error_setting_proxy;
	public static String InternetPreferencePage_ftp_accounts;
	public static String InternetPreferencePage_http_ftp;
	public static String InternetPreferencePage_http_proxy;
	public static String InternetPreferencePage_http_tooltip;
	public static String InternetPreferencePage_HTTP_Proxy;
	public static String InternetPreferencePage_Manual_config;
	public static String InternetPreferencePage_new;
	public static String InternetPreferencePage_new_tooltip;
	public static String InternetPreferencePage_Port;
	public static String InternetPreferencePage_remove;
	public static String InternetPreferencePage_remove_tooltip;
	public static String InternetPreferencePage_System_proxy_config;
	public static String InternetPreferencePage_Timeout;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
