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

package com.bdaum.zoom.ui.internal.hover;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.bdaum.zoom.cat.model.Bookmark;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.Region;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.asset.TrackRecordImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IFormatter;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.peer.AssetOrigin;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.views.ImageRegion;

@SuppressWarnings("restriction")
public class HoverInfo implements IHoverInfo {

	private Object object;
	private ImageRegion[] regions;
	private QueryField[] queryFields;
	private String info;

	/**
	 * @param object
	 *            - asset, list of assets
	 * @param regions
	 *            - regionIds
	 * @param queryFields
	 */
	public HoverInfo(Object object, ImageRegion[] regions, QueryField[] queryFields) {
		this.object = object;
		this.regions = regions;
		this.queryFields = queryFields;
	}

	/**
	 * @param object
	 *            - asset, list of assets
	 * @param tooltip
	 *            - hover text
	 */
	public HoverInfo(Object object, String tooltip) {
		this.object = object;
		info = tooltip;
	}

	/**
	 * @param tooltip
	 *            - hover text
	 */
	public HoverInfo(String tooltip) {
		info = tooltip;
		this.object = tooltip;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bdaum.zoom.ui.internal.hover.IHoverInfo#getText()
	 */
	@Override
	public String getText() {
		if (info == null) {
			if (object instanceof Asset || object instanceof List<?>) {
				StringBuilder sb = new StringBuilder(512);
				if (object instanceof Asset)
					compileOrigin(sb);
				if (regions != null)
					compileRegionData(regions, sb);
				compileAssetData(object, sb);
				info = sb.toString();
			}
		}
		return info;
	}

	private void compileOrigin(StringBuilder sb) {
		IPeerService peerService = Core.getCore().getPeerService();
		if (peerService != null) {
			AssetOrigin assetOrigin = peerService.getAssetOrigin(((Asset) object).getStringId());
			if (assetOrigin != null) {
				sb.append(Messages.HoverInfo_origin);
				String peer = assetOrigin.getLocation();
				File catFile = assetOrigin.getCatFile();
				if (peerService.isLocal(peer))
					sb.append(catFile.getName());
				else
					sb.append(peer).append(", ").append(catFile.getName()); //$NON-NLS-1$
				sb.append('\n');
			}
		}
	}

	private static void compileRegionData(ImageRegion[] imageRegions, StringBuilder sb) {
		LinkedList<String> lines = new LinkedList<String>();
		IDbManager dbManager = Core.getCore().getDbManager();
		for (ImageRegion imageRegion : imageRegions) {
			List<RegionImpl> regions = Core.getCore().getDbManager().obtainObjects(RegionImpl.class, false,
					"asset_person_parent", //$NON-NLS-1$
					imageRegion.owner.getStringId(), QueryField.EQUALS, Constants.OID, imageRegion.regionId,
					QueryField.EQUALS);
			if (!regions.isEmpty()) {
				Region region = regions.get(0);
				String albumId = region.getAlbum();
				String name = "?"; //$NON-NLS-1$
				String description = ""; //$NON-NLS-1$
				if (albumId != null) {
					SmartCollectionImpl album = dbManager.obtainById(SmartCollectionImpl.class, albumId);
					if (album != null) {
						name = album.getName();
						if (album.getDescription() != null && !album.getDescription().equals(name))
							description = album.getDescription();
					}
				}
				if (region.getDescription() != null && region.getDescription().length() > 0) {
					if (description.length() > 0)
						description += "; "; //$NON-NLS-1$
					description += region.getDescription();
				}
				StringBuilder lsb = new StringBuilder();
				lsb.append(name);
				if (description.length() > 0)
					lsb.append(" - ").append(description); //$NON-NLS-1$
				String line = lsb.toString();
				lines.remove(line);
				lines.add(line);
			}
			Collections.sort(lines);
		}
		for (String line : lines)
			sb.append("  [").append(line).append("]\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@SuppressWarnings("unchecked")
	private void compileAssetData(Object o, StringBuilder sb) {
		int flags = 0;
		if (o instanceof Asset)
			flags |= collectFlags((Asset) o);
		else
			for (Asset asset : (List<Asset>) o)
				flags |= collectFlags(asset);
		IDbManager dbManager = Core.getCore().getDbManager();
		for (QueryField qfield : queryFields) {
			if (!qfield.testFlags(flags))
				continue;
			Object value = (o instanceof Asset) ? qfield.obtainFieldValue((Asset) o)
					: qfield.obtainFieldValue((List<Asset>) o, null);
			String text;
			if (qfield == QueryField.TRACK && value != null) {
				StringBuilder sbt = new StringBuilder();
				String[] ids = (String[]) value;
				List<TrackRecordImpl> records = new ArrayList<TrackRecordImpl>(ids.length);
				for (int i = 0; i < ids.length; i++) {
					TrackRecordImpl record = dbManager.obtainById(TrackRecordImpl.class, ids[i]);
					if (record != null)
						records.add(record);
				}
				if (records.size() > 1)
					Collections.sort(records, new Comparator<TrackRecordImpl>() {
						public int compare(TrackRecordImpl t1, TrackRecordImpl t2) {
							return t2.getExportDate().compareTo(t1.getExportDate());
						}
					});
				int j = 0;
				for (TrackRecordImpl t : records) {
					if (sbt.length() > 0)
						sbt.append('\n');
					IFormatter formatter = QueryField.TRACK.getFormatter();
					sbt.append('\t').append(formatter.toString(t));
					if (++j > 16)
						break;
				}
				text = sbt.toString();
			} else
				text = qfield.value2text(value, ""); //$NON-NLS-1$
			if (text != null && text.length() > 0 && text != Format.MISSINGENTRYSTRING) {
				if (sb.length() > 0)
					sb.append('\n');
				String label = qfield.getLabel();
				if (text.length() > 0) {
					if (qfield.getUnit() != null)
						text += ' ' + qfield.getUnit();
					else if (value instanceof String[] && ((String[]) value).length > 0)
						text = UiUtilities.addExplanation(qfield, (String[]) value, text);
				}
				sb.append(label).append(": ").append(text); //$NON-NLS-1$
			}
		}
	}

	private static int collectFlags(Asset asset) {
		IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(asset.getFormat());
		return (mediaSupport != null) ? mediaSupport.getPropertyFlags() : QueryField.PHOTO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.bdaum.zoom.ui.internal.hover.IHoverInfo#getTitle()
	 */
	@Override
	public String getTitle() {
		if (object instanceof AssetImpl)
			return ((AssetImpl) object).getName();
		if (object instanceof Bookmark)
			return ((Bookmark) object).getLabel();
		// if (object instanceof SmartCollectionImpl)
		// return ((SmartCollectionImpl) object).getName();
		// if (object instanceof Exhibition)
		// return ((Exhibition) object).getName();
		// if (object instanceof SlideShow)
		// return ((SlideShow) object).getName();
		// if (object instanceof WebGallery)
		// return ((WebGallery) object).getName();
		return ""; //$NON-NLS-1$
	}

	@Override
	public Object getObject() {
		return object;
	}

	@Override
	public ImageRegion[] getRegions() {
		return regions;
	}

}