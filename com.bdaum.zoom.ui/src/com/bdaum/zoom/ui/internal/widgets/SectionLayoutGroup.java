package com.bdaum.zoom.ui.internal.widgets;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolTip;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;

@SuppressWarnings("restriction")
public class SectionLayoutGroup implements Listener {

	private static final String[] THUMBNAILSGEO = new String[] { Messages.SectionLayoutGroup_text_only,
			Messages.SectionLayoutGroup_thumbs_left, Messages.SectionLayoutGroup_thumbs_right,
			Messages.SectionLayoutGroup_thumbs_top, Messages.SectionLayoutGroup_thumbs_bottom,
			Messages.SectionLayoutGroup_map_left, Messages.SectionLayoutGroup_map_right };

	private static final String[] THUMBNAILS = new String[] { Messages.SectionLayoutGroup_text_only,
			Messages.SectionLayoutGroup_thumbs_left, Messages.SectionLayoutGroup_thumbs_right,
			Messages.SectionLayoutGroup_thumbs_top, Messages.SectionLayoutGroup_thumbs_bottom };

	private Combo thumbnailField;

	private List<String> assetIds;

	public SectionLayoutGroup(Composite parent, int columns) {
		new Label(parent, SWT.NONE).setText(Messages.SectionLayoutGroup_layout);
		thumbnailField = new Combo(parent, SWT.DROP_DOWN);
		thumbnailField.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, columns, 1));
		thumbnailField.setItems(CoreActivator.getDefault().getGeoService() == null ? THUMBNAILS : THUMBNAILSGEO);
		thumbnailField.addListener(SWT.Selection, this);
	}

	public void setSelection(int layout) {
		if (layout < 0 || layout >= thumbnailField.getItemCount())
			layout = 0;
		thumbnailField.select(layout);
		validate();
	}

	public int getSelection() {
		return thumbnailField.getSelectionIndex();
	}

	@Override
	public void handleEvent(Event event) {
		validate();
	}

	private void validate() {
		int selection = getSelection();
		if ((selection == Constants.SLIDE_MAP_LEFT || selection == Constants.SLIDE_MAP_RIGHT) && !hasGeoAssets()) {
			thumbnailField.getDisplay().timerExec(400, () -> {
				ToolTip tooltip = new ToolTip(thumbnailField.getShell(), SWT.BALLOON | SWT.ICON_INFORMATION);
				Point loc = thumbnailField.toDisplay(thumbnailField.getLocation());
				UiUtilities.showTooltip(tooltip, loc.x, loc.y, Messages.SectionLayoutGroup_no_map, Messages.SectionLayoutGroup_no_geotagged);
			});
		}
	}

	private boolean hasGeoAssets() {
		if (assetIds != null) {
			IDbManager dbManager = Core.getCore().getDbManager();
			for (String assetId : assetIds) {
				Asset asset = dbManager.obtainAsset(assetId);
				if (asset != null && !Double.isNaN(asset.getGPSLatitude()) && !Double.isNaN(asset.getGPSLongitude()))
					return true;
			}
		}
		return false;
	}

	public void setAssets(List<String> assetIds) {
		this.assetIds = assetIds;
	}

}
