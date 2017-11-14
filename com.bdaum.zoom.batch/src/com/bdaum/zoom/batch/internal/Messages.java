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

package com.bdaum.zoom.batch.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.batch.internal.messages"; //$NON-NLS-1$
	public static String BatchActivator_error_when_reading;
	public static String BatchActivator_no_dcraw_module_installed;
	public static String BatchActivator_cannot_create_raw_converter;
	public static String BatchActivator_conversion_ended_with_error;
	public static String BatchActivator_could_not_locate_exiftool;
	public static String BatchActivator_error_stream;
	public static String BatchActivator_Error_when_launching;
	public static String BatchActivator_IOerror_converting;
	public static String BatchActivator_No_file_conversion;
	public static String BatchActivator_output_stream;
	public static String BatchActivator_time_limit_exceeded;
	public static String ExifTool_Bad_ICC_profile;
	public static String ExifTool_error_fetching_binary_data;
	public static String ExifTool_error_fetching_metadata;
	public static String ExifTool_error_launching_error_tool;
	public static String ExifTool_error_sending_commands;
	public static String ExifTool_errort_fetching_binary_data;
	public static String ExifTool_exiftool_cleanup;
	public static String ExifTool_Interna_error_fetching_metadata;
	public static String ExifTool_Internal_error_fetching_ICC;
	public static String ExifTool_Internal_error_fetching_preview;
	public static String ExifTool_timeout;
	public static String StreamCapture_unsupported_code_page;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
