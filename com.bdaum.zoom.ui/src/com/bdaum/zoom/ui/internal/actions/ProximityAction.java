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

package com.bdaum.zoom.ui.internal.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.dialogs.ProximityDialog;

public class ProximityAction extends Action {
	protected static final AssetImpl[] NOASSETS = new AssetImpl[0];

	private final IAdaptable adaptable;

	private Shell shell;

	public ProximityAction(String label, String tooltip, ImageDescriptor image, IAdaptable adaptable) {
		super(label, image);
		this.adaptable = adaptable;
		shell = adaptable.getAdapter(Shell.class);
		setToolTipText(tooltip);
	}

	@Override
	public void run() {
		AssetSelection selection = adaptable.getAdapter(AssetSelection.class);
		double lat = 0;
		double lon = 0;
		int n = 0;
		for (Asset asset : selection.getAssets())
			if (!Double.isNaN(asset.getGPSLatitude()) && !Double.isNaN(asset.getGPSLongitude())) {
				lat += asset.getGPSLatitude();
				double longitude = asset.getGPSLongitude();
				if (lon < 0 && longitude > 0)
					longitude -= 360;
				else if (lon > 0 && longitude < 0)
					longitude += 360;
				lon += longitude;
				n++;
			}
		if (n == 0) {
			AcousticMessageDialog.openInformation(shell, Messages.ProximityAction_proximity_search,
					Messages.ProximityAction_no_geotagged_image);
			return;
		}
		ProximityDialog dialog = new ProximityDialog(shell);
		if (dialog.open() == Window.OK) {
			double distance = dialog.getResult();
			lat /= n;
			lon /= n;
			double mx = 0;
			char unit = Core.getCore().getDbFactory().getDistanceUnit();
			for (Asset asset : selection.getAssets())
				if (!Double.isNaN(asset.getGPSLatitude()) && !Double.isNaN(asset.getGPSLongitude()))
					mx = Math.max(mx, Core.distance(lat, lon, asset.getGPSLatitude(), asset.getGPSLongitude(), unit));
			Object[] values = new Object[] { lat, lon, (distance + mx), unit };
			SmartCollectionImpl coll = new SmartCollectionImpl(Messages.ProximityAction_proximity_search, false, false,
					true, dialog.isNetworked(), null, 0, null, 0, null, Constants.INHERIT_LABEL, null, 0, 1, null);
			coll.addCriterion(new CriterionImpl(QueryField.EXIF_GPSLOCATIONDISTANCE.getKey(), null, values,
					null, QueryField.NOTGREATER, false));
			coll.addSortCriterion(new SortCriterionImpl(QueryField.EXIF_GPSLOCATIONDISTANCE.getKey(), null, false));
			coll.setSmartCollection_subSelection_parent(dialog.getParentCollection());
			Ui.getUi().getNavigationHistory(adaptable.getAdapter(IWorkbenchWindow.class))
					.postSelection(new StructuredSelection(coll));
		}
	}

}
