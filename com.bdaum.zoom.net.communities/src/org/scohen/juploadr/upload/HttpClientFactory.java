/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 * It is an adaptation of the equally named file from the jUploadr project (http://sourceforge.net/projects/juploadr/)
 * (c) 2004 Steve Cohen and others
 * 
 * jUploadr is licensed under the GNU Library or Lesser General Public License (LGPL).
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
 * Modifications (c) 2009 Berthold Daum  
 */


package org.scohen.juploadr.upload;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.IConfigurationElement;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.net.communities.CommunityAccount;
import com.bdaum.zoom.net.ui.internal.NetActivator;

/**
 * @author steve
 * @author bdaum connected to Eclipse Proxy Service
 */
@SuppressWarnings("restriction")
public class HttpClientFactory {

	private static final String HTTP = "http"; //$NON-NLS-1$

	public static HttpClient getHttpClient(CommunityAccount account) {
		HttpClient client = new HttpClient(
				new MultiThreadedHttpConnectionManager());
		Protocol http;
		if (account.isBandwidthLimited()) {
			http = new Protocol(HTTP,
					new BandwidthLimitingProtocolSocketFactory(account
							.getBandwidth()), 80);
		} else {
			http = new Protocol(HTTP, new DefaultProtocolSocketFactory(), 80);
		}

		Protocol.registerProtocol(HTTP, http);
		NetActivator activator = NetActivator.getDefault();
		IProxyService proxyService = activator
				.getProxyService();
		String home = ((IConfigurationElement) account.getConfiguration()
				.getParent()).getAttribute("home"); //$NON-NLS-1$
		home = Core.furnishWebUrl(home);
		IProxyData proxyData = null;
		try {
			IProxyData[] select = proxyService.select(new URI(home));
			if (select.length > 0)
				proxyData = select[0];
		} catch (URISyntaxException e) {
			activator.logError(Messages.HttpClientFactory_bad_uri_for_proxy, e);
		} finally {
			activator.ungetProxyService(proxyService);
		}
		if (proxyData != null && proxyData.getHost() != null) {
			String proxyHost = proxyData.getHost();
			String proxyPassword = proxyData.getPassword();
			String proxyUsername = proxyData.getUserId();
			int proxyPort = proxyData.getPort();
			HostConfiguration hc = client.getHostConfiguration();
			if (proxyPort < 0)
				hc.setHost(proxyHost);
			else
				hc.setProxy(proxyHost, proxyPort);
			if (proxyData.isRequiresAuthentication()
					&& proxyUsername.length() > 0) {
				Credentials creds = new UsernamePasswordCredentials(
						proxyUsername, proxyPassword);
				client.getParams().setAuthenticationPreemptive(true);
				AuthScope scope = new AuthScope(proxyHost, proxyPort);
				client.getState().setProxyCredentials(scope, creds);
			}
		}
		client.getHttpConnectionManager().getParams().setConnectionTimeout(
				60000);
		client.getHttpConnectionManager().getParams().setSoTimeout(60000);
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(0, false));
		return client;
	}
}