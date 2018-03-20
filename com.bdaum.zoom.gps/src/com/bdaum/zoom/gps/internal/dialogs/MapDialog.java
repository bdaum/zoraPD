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

package com.bdaum.zoom.gps.internal.dialogs;

import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.location.Location;
import com.bdaum.zoom.cat.model.location.LocationImpl;
import com.bdaum.zoom.gps.CoordinatesListener;
import com.bdaum.zoom.gps.geonames.Place;
import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.HelpContextIds;
import com.bdaum.zoom.gps.widgets.IMapComponent;
import com.bdaum.zoom.ui.internal.dialogs.ZResizableDialog;

@SuppressWarnings("restriction")
public class MapDialog extends ZResizableDialog {

	private static final String ID = "com.bdaum.zoom.gps.internal.dialogs.MapDialog"; //$NON-NLS-1$
	private Location current;
	private IMapComponent mapComponent;

	private LocationImpl result;

	public MapDialog(Shell parentShell, Location loc) {
		super(parentShell, HelpContextIds.MAP_DIALOG);
		current = loc;
	}

	@Override
	protected Point getDefaultSize() {
		return new Point(800, 600);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		mapComponent = GpsActivator.getMapComponent(GpsActivator.findCurrentMappingSystem());
		if (mapComponent != null) {
			mapComponent.createComponent(area, true);
			mapComponent.addCoordinatesListener(new CoordinatesListener() {

				public void setCoordinates(String[] assetId, double latitude, double longitude, int zoom, int type,
						String uuid) {
					if (result == null)
						result = new LocationImpl();
					result.setLatitude(latitude);
					result.setLongitude(longitude);
				}
			});
			mapComponent.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
			showLocation(current);
		}
		return area;
	}

	public void showLocation(Location loc) {
		Place[] markerPositions = null;
		Double latitude = (loc == null) ? null : loc.getLatitude();
		Double longitude = (loc == null) ? null : loc.getLongitude();
		if (latitude == null)
			latitude = Double.NaN;
		if (longitude == null)
			longitude = Double.NaN;
		Place mapPosition = new Place(latitude, longitude);
		int initialZoomLevel = 12;
		if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
			if (loc != null) {
				mapPosition.setCountryCode(loc.getCountryISOCode());
				mapPosition.setCountryName(loc.getCountryName());
				mapPosition.setName(loc.getCity());
				mapPosition.setState(loc.getProvinceOrState());
				mapPosition.setStreet(loc.getSublocation());
				if (loc.getCity() == null) {
					initialZoomLevel = 8;
					if (loc.getProvinceOrState() == null)
						initialZoomLevel = 6;
				}
			} else {
				mapPosition.setCountryCode(Locale.getDefault().getCountry());
				initialZoomLevel = 6;
			}
		} else
			markerPositions = new Place[] { new Place(latitude, longitude) };
		mapComponent.setInput(mapPosition, initialZoomLevel, markerPositions, null, null, IMapComponent.ADDLOCATION);
	}

	public Location getResult() {
		return result;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (current != null)
			super.createButtonsForButtonBar(parent);
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	@Override
	protected String getId() {
		return ID;
	}
}
