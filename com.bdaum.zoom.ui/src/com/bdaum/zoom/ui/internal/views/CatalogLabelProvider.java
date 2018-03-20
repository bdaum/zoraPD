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
 * (c) 2012-2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.views;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.aoModeling.runtime.AomList;
import com.bdaum.zoom.cat.model.SimilarityOptions_type;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.Criterion;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.GroupImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterion;
import com.bdaum.zoom.cat.model.group.exhibition.Exhibition;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShow;
import com.bdaum.zoom.cat.model.group.webGallery.WebGallery;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.Icons.Icon;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;

public class CatalogLabelProvider extends ZColumnLabelProvider {

	private static final int TOOLTIPIMAGESIZE = 60;

	private Image thumb;

	private final IAdaptable adaptable;

	public CatalogLabelProvider(IAdaptable adaptable) {
		this.adaptable = adaptable;
	}

	@Override
	public void dispose() {
		disposeThumb();
		super.dispose();
	}

	@Override
	public String getText(Object obj) {
		if (obj instanceof SmartCollection)
			return ((SmartCollection) obj).getName();
		if (obj instanceof SlideShow)
			return ((SlideShow) obj).getName();
		if (obj instanceof Exhibition)
			return ((Exhibition) obj).getName();
		if (obj instanceof WebGallery)
			return ((WebGallery) obj).getName();
		if (obj instanceof GroupImpl)
			return ((GroupImpl) obj).getName();
		return obj.toString();
	}

	@Override
	public Image getToolTipImage(Object element) {
		disposeThumb();
		if (UiActivator.getDefault().getShowHover() && element instanceof SmartCollection) {
			SmartCollection sm = (SmartCollection) element;
			if (sm.getAlbum()) {
				if (sm.getSystem())
					return thumb = UiUtilities.getFace(adaptable.getAdapter(Shell.class).getDisplay(), sm,
							TOOLTIPIMAGESIZE, 0, null);
				return thumb = getAlbumImage(sm);
			}
			if (!sm.getSystem()) {
				List<Criterion> crits = sm.getCriterion();
				for (Criterion criterion : crits) {
					if (ICollectionProcessor.SIMILARITY.equals(criterion.getField()))
						return thumb = getCritImage(criterion);
					break;
				}
			}
		}
		return null;
	}

	private void disposeThumb() {
		if (thumb != null) {
			thumb.dispose();
			thumb = null;
		}
	}

	private Image getCritImage(Criterion criterion) {
		SimilarityOptions_type options = (SimilarityOptions_type) criterion.getValue();
		if (options != null) {
			ImageData data = ImageUtilities.loadThumbnailData(options.getPngImage(), Ui.getUi().getDisplayCMS(),
					SWT.IMAGE_PNG);
			if (data != null)
				return new Image(adaptable.getAdapter(Shell.class).getDisplay(),
						ImageUtilities.downSample(data, TOOLTIPIMAGESIZE, TOOLTIPIMAGESIZE, 0));
		}
		return null;
	}

	private Image getAlbumImage(SmartCollection sm) {
		List<String> assetIds = sm.getAsset();
		if (assetIds != null) {
			IDbManager dbManager = Core.getCore().getDbManager();
			for (String assetId : assetIds) {
				Asset asset = dbManager.obtainAsset(assetId);
				Image image = asset == null ? null : Core.getCore().getImageCache().getImage(asset);
				if (image != null)
					return new Image(adaptable.getAdapter(Shell.class).getDisplay(),
							ImageUtilities.downSample(image.getImageData(), TOOLTIPIMAGESIZE, TOOLTIPIMAGESIZE, 0));
			}
		}
		return null;
	}

	@Override
	public String getToolTipText(Object element) {
		if (!UiActivator.getDefault().getShowHover())
			return null;
		StringBuilder tooltip = new StringBuilder();
		if (element instanceof Group) {
			if (element == CatalogView.WASTEBASKET)
				return Messages.getString("CatalogLabelProvider.deleted_entries_go_here"); //$NON-NLS-1$
			String anno = ((Group) element).getAnnotations();
			if (anno != null && !anno.isEmpty()) {
				if (Constants.GROUP_ID_AUTO.equals(((Group) element).getStringId()))
					tooltip.append(Messages.getString("CatalogLabelProvider.rules_defined")); //$NON-NLS-1$
				else
					tooltip.append(Messages.getString("CatalogLabelProvider.filter_set")); //$NON-NLS-1$
			}
		} else if (element instanceof SmartCollection) {
			SmartCollection sm = (SmartCollection) element;
			if (sm.getSystem()) {
				List<Criterion> crits = sm.getCriterion();
				if (!crits.isEmpty()) {
					Criterion criterion = crits.get(0);
					if (criterion.getField().equals(QueryField.RATING.getKey()))
						return QueryField.RATING.formatScalarValue(criterion.getValue());
				}
			}
			appendDescription(tooltip, sm.getDescription());
			if (sm.getAlbum()) {
				if (tooltip.length() > 0)
					tooltip.append('\n');
				tooltip.append(NLS.bind(Messages.getString("CatalogView.album_with_n_images"), sm.getAsset().size())); //$NON-NLS-1$
			} else if (!sm.getSystem() && !UiUtilities.isImport(sm)) {
				if (tooltip.length() > 0)
					tooltip.append('\n');
				tooltip.append(UiUtilities.composeContentDescription(sm, "\n", false)); //$NON-NLS-1$
			}
			AomList<SortCriterion> sortCriteria = sm.getSortCriterion();
			if (!sortCriteria.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (SortCriterion sortCriterion : sortCriteria) {
					QueryField qf = QueryField.findQueryField(sortCriterion.getField());
					if (qf != null) {
						if (sb.length() > 0)
							sb.append(", "); //$NON-NLS-1$
						sb.append(qf.getLabel());
					}
				}
				if (tooltip.length() > 0)
					tooltip.append('\n');
				tooltip.append(NLS.bind(Messages.getString("CatalogView.sorted_by"), sb)); //$NON-NLS-1$
			}
			if (sm.getNetwork()) {
				if (tooltip.length() > 0)
					tooltip.append('\n');
				tooltip.append(Messages.getString("CatalogView.network_search")); //$NON-NLS-1$
			}
		} else if (element instanceof SlideShow)
			appendDescription(tooltip, ((SlideShow) element).getDescription());
		else if (element instanceof Exhibition) {
			String description = ((Exhibition) element).getDescription();
			if (description == null)
				description = ((Exhibition) element).getInfo();
			appendDescription(tooltip, description);
		} else if (element instanceof WebGallery)
			appendDescription(tooltip, ((WebGallery) element).getDescription());
		if (tooltip.length() > 0) {
			tooltip.insert(0, '\n');
			tooltip.insert(0, getText(element));
			return tooltip.toString();
		}
		return null;
	}

	private static void appendDescription(StringBuilder tooltip, String des) {
		if (des != null)
			tooltip.append(des.trim());
	}

	@Override
	public Image getImage(Object obj) {
		if (obj instanceof SmartCollectionImpl) {
			Icon icon = UiUtilities.getSmartCollectionIcon((SmartCollectionImpl) obj);
			if (icon == null)
				return null;
			int cc = ((SmartCollectionImpl) obj).getColorCode() - 1;
			return (cc <= Constants.COLOR_UNDEFINED) ? icon.getImage() : icon.getColorOverlay(cc);
		}
		if (obj instanceof SlideShow)
			return Icons.slideshow.getImage();
		if (obj instanceof Exhibition)
			return Icons.exhibition.getImage();
		if (obj instanceof WebGallery)
			return Icons.webGallery.getImage();
		if (obj instanceof GroupImpl)
			return (obj == CatalogView.WASTEBASKET ? Icons.wastebasket
					: ((GroupImpl) obj).getAnnotations() == null ? Icons.group : Icons.groupfiltered).getImage();
		return null;
	}

}