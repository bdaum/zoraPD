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
package com.bdaum.juploadr.uploadapi.locrrest.albums;

import org.scohen.juploadr.app.PhotoSet;
import org.xml.sax.SAXException;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;

public class CreateAlbumResponseHandler extends DefaultLocrHandler {

	private PhotoSet set;

	public CreateAlbumResponseHandler(Object parent) {
		super(parent);
		set = (PhotoSet) parent;
	}
	
    
    @Override
	public void characters(char[] chars, int start, int end)
    		throws SAXException {
    	super.characters(chars, start, end);
        String cdata = new String(chars, start, end).trim();
        if (!cdata.isEmpty()) {
            if ("name".equals(lastTag)) //$NON-NLS-1$
				set.setTitle(cdata);
			else if ("description".equals(lastTag)) //$NON-NLS-1$
				set.setDescription(cdata);
			else if ("id".equals(lastTag)) //$NON-NLS-1$
				set.setId(cdata);
			else if ("thumbnail_id".equals(lastTag)) //$NON-NLS-1$
				set.setPrimaryPhotoId(cdata);
			else if ("user_url".equals(lastTag)) //$NON-NLS-1$
				set.setUrl(cdata);
			else if ("photos_count".equals(lastTag)) //$NON-NLS-1$
				try {
					set.setNumberOfPhotos(Integer.parseInt(cdata));
				} catch (NumberFormatException e) {
					// ignore
				}
        }

    }

}
