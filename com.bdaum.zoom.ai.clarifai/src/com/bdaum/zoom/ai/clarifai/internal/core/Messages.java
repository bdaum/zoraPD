/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ai.clarifai.internal.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ai.clarifai.internal.core.messages"; //$NON-NLS-1$
	public static String Clarifai_request_refused;
	public static String Clarifai_service_disabled;
	public static String ClarifaiServiceProvider_download_failed;
	public static String ClarifaiServiceProvider_image_rating;
	public static String ClarifaiServiceProvider_loading_failed;
	public static String ClarifaiServiceProvider_network_error;
	public static String ClarifaiServiceProvider_not_set_or_wrong;
	public static String ClarifaiServiceProvider_pending;
	public static String ClarifaiServiceProvider_proposals;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
