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
 * (c) 2019 Berthold Daum  
 */
 package com.bdaum.zoom.webserver.internals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

import com.bdaum.zoom.common.internal.FileLocator;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.ContextHandler;
import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;

public class PluginContextHandler implements ContextHandler {

	@Override
	public int serve(Request req, Response resp) throws IOException {
		String path = req.getPath();
		int p = path.indexOf('/', 1);
		int q = path.indexOf('/', p+1);
		if (p >= 0 && q > p) {
			String loc = path.substring(q);
			String pluginId = path.substring(p+1, q);
			Bundle bundle = Platform.getBundle(pluginId);
			URL url = findUrl(bundle, loc);
			try {
				File file = new File(url.toURI());
				HTTPServer.serveFileContent(file, req, resp);
			} catch (URISyntaxException e) {
				WebserverActivator.getDefault().logError(NLS.bind(Messages.PluginContextHandler_cannot_load_resrce, path), e);
			}
		}
		return 0;
	}
	
	private static URL findUrl(Bundle bundle, String path) {
		try {
			return FileLocator.findFileURL(bundle, "/$nl$/" + path, true); //$NON-NLS-1$
		} catch (IOException e) {
			WebserverActivator.getDefault().logError(NLS.bind(Messages.PluginContextHandler_error_unpacking_rsrce, path), e);
		}
		return null;
	}


}
