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
 * (c) 2012 Berthold Daum  
 */
package com.bdaum.zoom.db.internal;

import java.util.Set;

import com.bdaum.zoom.core.db.IDbManager;

public interface IQueryExtension {
	/**
	 * Queries for field in media extensions
	 * @param dbManager - database manager
	 * @param field - field key
	 * @param relation - relation for query
	 * @param value - value for query
	 * @param idSet - IDs of found assets are added to this set - in case of null a new set will be created
	 * @return new or updated idSet
	 */
	Set<String> query(IDbManager dbManager, String field, int relation,
			Object value, Set<String> idSet);

}
