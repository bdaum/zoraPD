/*******************************************************************************
 * Copyright (c) 2011 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.recipes.rt3.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.recipes.rt3.internal.messages"; //$NON-NLS-1$
	public static String Rt3Activator_cannot_execute_locate;
	public static String Rt3Activator_locate_rt;
	public static String RT3Detector_bad_uri;
	public static String RT3Detector_cannot_read_rt_data_file;
	public static String RT3Detector_cannot_read_rt_pp3;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
