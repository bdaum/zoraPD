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
package com.bdaum.juploadr.uploadapi.locrrest.albums;

import org.scohen.juploadr.uploadapi.ImageUploadApi;
import org.xml.sax.SAXException;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;

public class GetListUserHandler extends DefaultLocrHandler {

	private com.bdaum.juploadr.uploadapi.locrrest.albums.GetListUser parent;

	public GetListUserHandler(Object parent) {
		super(parent);
		this.parent = (GetListUser) parent;
	}


	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String cdata = new String(ch, start, length).trim();
		if (cdata.length() > 0) {
			if (lastTag.equals("tags")) { //$NON-NLS-1$
				parent.addTags(((ImageUploadApi) parent.getSession().getApi()).getTagParser().parse(
						cdata));
			}
		}
	}

}
