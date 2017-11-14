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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.juploadr.uploadapi.locrrest.upload;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xml.sax.SAXException;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;

public class GetUploadStatusHandler extends DefaultLocrHandler {

	private GetUploadStatus parent;

	public GetUploadStatusHandler(Object parent) {
		super(parent);
		this.parent = (GetUploadStatus) parent;
	}

	@Override
	public void characters(char[] chars, int start, int end) throws SAXException {
		String cdata = new String(chars, start, end).trim();
		if (!cdata.trim().isEmpty()) {
			if ("name".equals(lastTag)) { //$NON-NLS-1$
				parent.setUsername(cdata);
			} else if ("pro_account_expiration_date".equals(lastTag)) { //$NON-NLS-1$
				Date expiration_date = null;
				try {
					expiration_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cdata); //$NON-NLS-1$
				} catch (ParseException e) {
					try {
						expiration_date = new SimpleDateFormat("yyyy-MM-dd").parse(cdata); //$NON-NLS-1$
					} catch (ParseException e1) {
						// ignore
					}
				}
				if (expiration_date != null)
					parent.setPro(expiration_date.compareTo(new Date()) >= 0);
			} else if ("upload_limit".equals(lastTag)) { //$NON-NLS-1$
				parent.setMaxBandwidth(Long.parseLong(cdata));
			} else if ("upload_remaining".equals(lastTag)) { //$NON-NLS-1$
				parent.setUsedBandwidth(parent.getMaxBandwidth() - Long.parseLong(cdata));
			}
			parent.setMaxFilesize(Long.MAX_VALUE);
			parent.setMaxVideosize(Long.MAX_VALUE);
		}
	}

}
