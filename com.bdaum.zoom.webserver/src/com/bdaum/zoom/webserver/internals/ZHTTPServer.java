package com.bdaum.zoom.webserver.internals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

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
	
	// from https://github.com/mesutpiskin/opencv-live-video-stream-over-http/blob/master/src/main/java/HttpStreamServer.java

	public static void writeVideoHeader(OutputStream out, String boundary) throws IOException {
		out.write(("HTTP/1.0 200 OK\r\n"  //$NON-NLS-1$
				+ "Connection: keep-alive\r\n"  //$NON-NLS-1$
				+ "Max-Age: 0\r\n"  //$NON-NLS-1$
				+ "Expires: 0\r\n"  //$NON-NLS-1$
				+ "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" //$NON-NLS-1$
				+ "Pragma: no-cache\r\n"  //$NON-NLS-1$
				+ "Content-Type: multipart/x-mixed-replace;boundary=" + boundary + "\r\n" //$NON-NLS-1$ //$NON-NLS-2$ 
				+ "\r\n"  //$NON-NLS-1$
				+ "--" + boundary + "\r\n").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$ 
	}

	public void pushImage(OutputStream out, BufferedImage img, String boundary) throws IOException {
		if (img == null)
			return;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "jpg", baos); //$NON-NLS-1$
			byte[] imageBytes = baos.toByteArray();
			out.write(("Content-type: image/jpeg\r\n" + "Content-Length: " + imageBytes.length + "\r\n" + "\r\n") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					.getBytes());
			out.write(imageBytes);
			out.write(("\r\n--" + boundary + "\r\n").getBytes()); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception ex) {
			ServerSocket serv = ZHTTPServer.this.serv;
			Socket socket = serv.accept();
			writeVideoHeader(socket.getOutputStream(), boundary);
		}
	}

}
