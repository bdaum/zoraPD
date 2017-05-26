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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.scohen.juploadr.app.tags.Tag;
import org.scohen.juploadr.uploadapi.Session;

import com.bdaum.juploadr.uploadapi.locrrest.DefaultLocrHandler;
import com.bdaum.juploadr.uploadapi.locrrest.LocrMethod;

public class GetListUser extends LocrMethod {

	private Set<Tag> tags;

	public GetListUser(Session session) {
		super(session);
		this.tags = new HashSet<Tag>();
	}

	
	@Override
	public SortedMap<String, String> getParams() {
		SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("method","get_albums_xml.php?"); //$NON-NLS-1$ //$NON-NLS-2$
        params.put("user_name", getSession().getAccount().getName()); //$NON-NLS-1$
        params.put("count", "500"); //$NON-NLS-1$ //$NON-NLS-2$
        return params;
	}
	
    public void addTags(Set<Tag> newTags) {
        tags.addAll(newTags); 
     }

	
	@Override
	public DefaultLocrHandler getResponseHandler() {
		return new GetListUserHandler(this);
	}

	
	@Override
	public boolean isAuthorized() {
		return true;
	}

	public List<Tag> getTags() {
		List<Tag> sortedTags = new ArrayList<Tag>();
		sortedTags.addAll(tags);
		Collections.sort(sortedTags);
		return sortedTags;
	}

}
