/* Copyright 2009 Berthold Daum

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.bdaum.zoom.net.core.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	public static final String PLUGIN_ID = "com.bdaum.zoom.net.core"; //$NON-NLS-1$

	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static InputStream openStream(String url, int timeout)
			throws MalformedURLException, IOException, HttpException {
		URL u = new URL(url);
		URLConnection con = u.openConnection();
		con.setConnectTimeout(timeout);
		con.setReadTimeout(timeout);
		try {
			return new BufferedInputStream(con.getInputStream());
		} catch (IOException e) {
			int responseCode = -1;
			if (con instanceof HttpURLConnection)
				responseCode = ((HttpURLConnection) con).getResponseCode();
			else if (con instanceof HttpsURLConnection)
				responseCode = ((HttpsURLConnection) con).getResponseCode();
			if (responseCode >= 0 && responseCode != HttpURLConnection.HTTP_OK)
				throw new HttpException(getStatusText(responseCode), e);
			throw e;
		}
	}

	public static String getStatusText(int responseCode) {
		String statusText = HttpStatus.getStatusText(responseCode);
		if (statusText == null)
			statusText = "Unknown cause";//$NON-NLS-1$
		return statusText + " (" + responseCode + ')'; //$NON-NLS-1$
	}

}
