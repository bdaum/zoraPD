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
 * (c) 2009 Berthold Daum  
 */
package com.bdaum.juploadr.uploadapi.locrrest.info;

import java.util.StringTokenizer;

import org.xml.sax.SAXException;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;

public class LocrInfoHandler extends DefaultLocrHandler {

	private LocrImageInfo imageInfo;

	public LocrInfoHandler(Object parent) {
		super(parent);
		imageInfo = (LocrImageInfo) parent;
	}


	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		if ("caption".equals(lastTag)) { //$NON-NLS-1$
			imageInfo.setTitle(new String(ch));
			//    	} else if ("description".equals(lastTag)) { //$NON-NLS-1$
			// imageInfo.setDescription(new String(ch));
		} else if ("url".equals(lastTag)) { //$NON-NLS-1$
			imageInfo.addUrl(new String(ch));
		} else if ("tags".equals(lastTag)) { //$NON-NLS-1$
			String tags = new String(ch);
			StringTokenizer st = new StringTokenizer(tags,","); //$NON-NLS-1$
			while (st.hasMoreTokens())
				imageInfo.addTag(st.nextToken().trim());
		} else if ("privacy".equals(lastTag)) { //$NON-NLS-1$
			String priv = new String(ch);
			imageInfo.setPublic("0".equals(priv)); //$NON-NLS-1$
			if ("500".equals(priv)) { //$NON-NLS-1$
				imageInfo.setFriends(true);
				imageInfo.setFamily(true);
			} else if ("800".equals(priv)) { //$NON-NLS-1$
				imageInfo.setFriends(false);
				imageInfo.setFamily(true);
			} else if ("1000".equals(priv)) { //$NON-NLS-1$
				imageInfo.setFriends(false);
				imageInfo.setFamily(false);
			}
		}
	}
}
