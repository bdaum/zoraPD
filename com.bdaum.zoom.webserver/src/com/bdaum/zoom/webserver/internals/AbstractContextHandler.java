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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.eclipse.jface.preference.IPreferenceStore;

import com.bdaum.zoom.program.HtmlEncoderDecoder;

import net.freeutils.httpserver.HTTPServer.ContextHandler;
import net.freeutils.httpserver.HTTPServer.Response;

public abstract class AbstractContextHandler implements ContextHandler {

	protected IPreferenceStore preferenceStore;

	public AbstractContextHandler() {
		preferenceStore = WebserverActivator.getDefault().getPreferenceStore();
	}

	protected int sendDynamicContent(Response resp, String content, String ctype) throws IOException {
		if (content != null) {
			byte[] bytes = content.getBytes(Charset.forName("UTF-8")); //$NON-NLS-1$
			resp.getHeaders().add("Content-Type", ctype); //$NON-NLS-1$
			String etag = "W/\"" + System.currentTimeMillis() + "\""; //$NON-NLS-1$//$NON-NLS-2$
			resp.sendHeaders(200, bytes.length, -1l, etag, ctype, null);
			try (InputStream in = new ByteArrayInputStream(bytes)) { // $NON-NLS-1$
				resp.sendBody(in, bytes.length, null);
			}
			return 0;
		}
		return 404;
	}
	
	protected static String toHtml(String s, boolean brk) {
		return HtmlEncoderDecoder.getInstance().encodeHTML(s, brk);
	}

}