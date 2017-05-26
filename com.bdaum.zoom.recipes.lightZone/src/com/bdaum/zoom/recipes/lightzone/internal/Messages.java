/*******************************************************************************
 * Copyright (c) 2014 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.recipes.lightzone.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.recipes.lightzone.internal.messages"; //$NON-NLS-1$
	public static String JpegSegmentReader_expected_jpeg_segment;
	public static String JpegSegmentReader_io_exception;
	public static String JpegSegmentReader_not_a_jpeg_file;
	public static String JpegSegmentReader_size_exceeds;
	public static String JpegSegmentReader_size_negative;
	public static String RelationDetector_cannot_parse;
	public static String UpdateDerivedDialog_dont_ask_again;
	public static String UpdateDerivedDialog_keeping_consistent;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
