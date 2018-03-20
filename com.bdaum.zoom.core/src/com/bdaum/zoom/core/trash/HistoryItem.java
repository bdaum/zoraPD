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

package com.bdaum.zoom.core.trash;

import java.util.UUID;



/**
 * Generic class for trash items
 */
public class HistoryItem {

	protected String opId;
	protected String id;
	private long timestamp;

	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Constructor
	 * @param opId - ID of operation that created the trash
	 */
	public HistoryItem(String opId) {
		this.opId = opId;
		this.id = UUID.randomUUID().toString(); 
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Default constructor
	 */
	public HistoryItem() {
	}

	/**
	 * Return the ID of the operation that created the trash item
	 * @return - ID of the operation that created the trash item
	 */
	public String getOpId() {
		return opId;
	}

	/**
	 * Return the ID the trash item
	 * @return ID the trash item
	 */
	public String getId() {
		return id;
	}

}