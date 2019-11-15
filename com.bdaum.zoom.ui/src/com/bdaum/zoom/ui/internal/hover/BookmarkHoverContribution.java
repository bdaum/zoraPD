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
package com.bdaum.zoom.ui.internal.hover;

import java.io.File;
import java.util.Date;

import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.Bookmark;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.internal.peer.IPeerService;

public class BookmarkHoverContribution extends AbstractHoverContribution implements IHoverItem {

	private static final String LABEL = "{label}"; //$NON-NLS-1$
	private static final String COLLECTION = "{collection}"; //$NON-NLS-1$
	private static final String PEER = "{peer}"; //$NON-NLS-1$
	private static final String CATALOG = "{catalog}"; //$NON-NLS-1$
	private static final String CREATIONDATE = "{creationDate}"; //$NON-NLS-1$
	private static final String TARGET = "{target}"; //$NON-NLS-1$
	private static final String METADATA = Constants.HV_METADATA;

	private static final String[] TITLETAGS = new String[] { LABEL, CREATIONDATE, PEER, CATALOG, COLLECTION, TARGET };
	private static final String[] TITLELABELS = new String[] { "Bookmark label", "Creation date", "Network origin", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			"Catalog", "Collection", Messages.BookmarkHoverContribution_target }; //$NON-NLS-1$//$NON-NLS-2$

	private static final String[] TAGS = new String[] { LABEL, CREATIONDATE, PEER, CATALOG, COLLECTION, TARGET,
			METADATA };
	private static final String[] LABELS = new String[] { "Bookmark label", "Creation date", "Network origin", //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			"Catalog", "Collection", Messages.BookmarkHoverContribution_target, //$NON-NLS-1$//$NON-NLS-2$
			Messages.BookmarkHoverContribution_metadata_block };

	@Override
	public boolean supportsTitle() {
		return true;
	}

	@Override
	public String getCategory() {
		return Messages.BookmarkHoverContribution_misc;
	}

	@Override
	public String getName() {
		return Messages.BookmarkHoverContribution_bookmarks;
	}

	@Override
	public String getDescription() {
		return Messages.BookmarkHoverContribution_bookmarks_expl;
	}

	@Override
	public String getDefaultTemplate() {
		return Messages.BookmarkHoverContribution_dflt_template;
	}

	@Override
	public String getDefaultTitleTemplate() {
		return "{label}"; //$NON-NLS-1$
	}

	@Override
	public String[] getItemKeys() {
		return TAGS;
	}

	@Override
	public IHoverItem getHoverItem(String itemkey) {
		return this;
	}

	@Override
	public String getValue(String key, Object object, IHoverContext context) {
		if (object instanceof HoverTestObject) {
			if (LABEL.equals(key))
				return Messages.BookmarkHoverContribution_bookmark1;
			if (PEER.equals(key)) {
				IPeerService peerService = Core.getCore().getPeerService();
				if (peerService != null)
					return Messages.BookmarkHoverContribution_peer1;
			} else if (CATALOG.equals(key))
				return Core.getCore().getDbManager().getFileName();
			else if (COLLECTION.equals(key))
				return Messages.BookmarkHoverContribution_sample_coll;
			else if (CREATIONDATE.equals(key))
				return Format.EMDY_TIME_LONG_FORMAT.get().format(new Date());
			else if (TARGET.equals(key))
				return NLS.bind("{0} {1}", "img2019-234", Messages.BookmarkHoverContribution_offline); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (object instanceof Bookmark) {
			Bookmark bookmark = (Bookmark) object;
			if (LABEL.equals(key))
				return bookmark.getLabel();
			if (PEER.equals(key))
				return bookmark.getPeer();
			if (CATALOG.equals(key)) {
				String catFile = bookmark.getCatFile();
				return catFile == null ? null : new File(catFile).getName();
			}
			ICore core = Core.getCore();
			if (COLLECTION.equals(key)) {
				SmartCollectionImpl sm = core.getDbManager().obtainById(SmartCollectionImpl.class,
						bookmark.getCollectionId());
				return sm == null ? null : sm.getName();
			}
			if (CREATIONDATE.equals(key)) {
				Date createdAt = bookmark.getCreatedAt();
				return createdAt == null ? null : Format.EMDY_TIME_LONG_FORMAT.get().format(createdAt);
			}
			if (TARGET.equals(key)) {
				Asset asset = core.getDbManager().obtainAsset(bookmark.getAssetId());
				if (asset == null)
					return Messages.BookmarkHoverContribution_does_not_exist;
				String st;
				switch (core.getVolumeManager().determineFileState(asset)) {
				case IVolumeManager.REMOTE:
					st = Messages.BookmarkHoverContribution_remote;
					break;
				case IVolumeManager.OFFLINE:
					st = Messages.BookmarkHoverContribution_offline;
					break;
				default:
					st = Messages.BookmarkHoverContribution_online;
					break;
				}
				return NLS.bind("{0} {1}", asset.getName(), st); //$NON-NLS-1$
			}
		}
		return null;
	}

	@Override
	public String[] getTitleItemLabels() {
		return TITLELABELS;
	}

	@Override
	public String[] getItemLabels() {
		return LABELS;
	}

	@Override
	public String[] getTitleItemKeys() {
		return TITLETAGS;
	}

	@Override
	public Object getTarget(Object object) {
		if (object instanceof Bookmark)
			return Core.getCore().getDbManager().obtainAsset(((Bookmark) object).getAssetId());
		else if (object instanceof HoverTestObject)
			return new HoverTestAsset();
		return null;
	}

}
