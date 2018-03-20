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
 * (c) 2013 Berthold Daum  
 */
package com.bdaum.zoom.peer.internal.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.db.NullDbManager;

@SuppressWarnings("restriction")
public class SharedCatalog {
	private static final NullDbManager NULLDBMANAGER = CoreActivator.NULLDBMANAGER;
	private static final List<PeerDefinition> EMPTY = new ArrayList<PeerDefinition>(
			0);
	private File path;
	private int privacy;
	private IDbManager sharedDbManager = NULLDBMANAGER;
	private List<PeerDefinition> restrictions = null;

	public SharedCatalog(String path, int privacy) {
		this.path = new File(path);
		this.privacy = privacy;
	}

	public int getPrivacy() {
		return privacy;
	}

	public File getPath() {
		return path;
	}

	/**
	 * @param path
	 *            das zu setzende Objekt path
	 */
	public void setPath(File path) {
		this.path = path;
	}

	/**
	 * @param privacy
	 *            das zu setzende Objekt privacy
	 */
	public void setPrivacy(int privacy) {
		this.privacy = privacy;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SharedCatalog)
			return ((SharedCatalog) obj).getPath().equals(path);
		return false;
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public String toString() {
		return path.toString();
	}

	public IDbManager getDbManager() {
		if (!path.exists())
			return NULLDBMANAGER;
		if (sharedDbManager.getFile() == null) {
			IDbManager currentDbManager = Core.getCore().getDbManager();
			File file = currentDbManager.getFile();
			if (file != null && file.equals(path))
				sharedDbManager = currentDbManager;
			else
				sharedDbManager = Core.getCore().getDbFactory()
						.createDbManager(path.toString(), false, true, false);
		}
		return sharedDbManager;
	}

	public void setDbManager(IDbManager manager) {
		sharedDbManager = manager == null ? NULLDBMANAGER
				: manager;
	}

	public IDbManager getDbEntry() {
		return sharedDbManager;
	}

	public void dispose() {
		if (sharedDbManager.getFile() != null) {
			File file = Core.getCore().getDbManager().getFile();
			if (file == null || !file.equals(path))
				sharedDbManager.close(CatalogListener.NORMAL);
			sharedDbManager = NULLDBMANAGER;
		}
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return restrictions == null ? false : restrictions.contains(o);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean addRestriction(PeerDefinition res) {
		if (restrictions == null)
			restrictions = new ArrayList<PeerDefinition>(3);
		else {
			int i = restrictions.indexOf(res);
			if (i >= 0) {
				restrictions.get(i).setRights(res.getRights());
				return true;
			}
		}
		return restrictions.add(res);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return restrictions == null ? false : restrictions.remove(o);
	}

	public List<PeerDefinition> getRestrictions() {
		return restrictions == null ? EMPTY : restrictions;
	}

}