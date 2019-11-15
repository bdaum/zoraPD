package com.bdaum.zoom.webserver.internals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.webserver.PreferenceConstants;

import net.freeutils.httpserver.HTTPServer;

public class ZHTTPServer extends HTTPServer {

	public ZHTTPServer(int port) {
		super(port);
		statuses[415] = "Unsupported Media Type"; //$NON-NLS-1$
	}

	protected void serve(Request req, Response resp) throws IOException {
		// get context handler to handle request
		ContextHandler handler = req.getContext().getHandlers().get(req.getMethod());
		if (handler == null) {
			sendError(resp, 404);
			return;
		}
		// serve request
		int status = 404;
		// add directory index if necessary
		String path = req.getPath();
		if (path.endsWith("/")) { //$NON-NLS-1$
			String index = req.getVirtualHost().getDirectoryIndex();
			if (index != null) {
				req.setPath(path + index);
				status = handler.serve(req, resp);
				req.setPath(path);
			}
		}
		if (status == 404)
			status = handler.serve(req, resp);
		if (status > 0)
			sendError(resp, status);
	}

	private static void sendError(Response resp, int status) throws IOException {
		String content;
		if (status > 1000) {
			status -= 1000;
			content = NLS.bind("{0} - {1}", status, statuses[status]); //$NON-NLS-1$
		} else {
			String page = status < 400 ? PreferenceConstants.STATUS : PreferenceConstants.ERROR;
			Map<String, String> substitutions = new HashMap<String, String>(5);
			substitutions.put("{$appName}", Constants.APPLICATION_NAME); //$NON-NLS-1$
			substitutions.put("{$status}", String.valueOf(status)); //$NON-NLS-1$
			substitutions.put("{$statusMsg}", statuses[status]); //$NON-NLS-1$
			content = WebserverActivator.getDefault().compilePage(page, substitutions);
		}
		resp.send(status, content);
	}

}
