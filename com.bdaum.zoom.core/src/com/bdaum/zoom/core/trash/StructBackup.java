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

import java.util.ArrayList;
import java.util.List;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.core.db.IDbManager;

public class StructBackup extends HistoryItem {

	protected IIdentifiableObject element;
	protected List<IIdentifiableObject> relations = new ArrayList<IIdentifiableObject>();



	/**
	 * Default constructor
	 */
	public StructBackup() {
	}

	/**
	 * @param opId - ID of operation that created the trash
	 * @param element - trashed element
	 */
	public StructBackup(String opId, IIdentifiableObject element) {
		super(opId);
		this.element = element;
	}

	/**
	 * Adds a new object to the trashed structure
	 * @param obj - new object
	 */
	public void add(IdentifiableObject obj) {
		relations.add(obj);
	}

	/**
	 * Deletes the trashed items and puts itself into the trash bin
	 * @param dbManager - database manager
	 */
	public void performDelete(IDbManager dbManager) {
		dbManager.storeTrash(this);
		for (IIdentifiableObject rel : relations)
			dbManager.delete(rel);
		dbManager.delete(element);
	}

	/**
	 * Restores the trash item
	 * @param dbManager - database manager
	 */
	public void restore(IDbManager dbManager) {
		for (IIdentifiableObject rel : relations)
			dbManager.store(rel);
		dbManager.store(element);
		dbManager.deleteTrash(this);
	}

}
