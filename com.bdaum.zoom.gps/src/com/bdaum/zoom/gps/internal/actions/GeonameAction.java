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
 * (c) 2016 Berthold Daum  
 */

package com.bdaum.zoom.gps.internal.actions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.bdaum.zoom.gps.internal.GpsActivator;
import com.bdaum.zoom.gps.internal.GpsConfiguration;
import com.bdaum.zoom.gps.internal.dialogs.TrackpointDialog;
import com.bdaum.zoom.gps.internal.operations.GeotagOperation;
import com.bdaum.zoom.gps.internal.preferences.GpsPreferencePage;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.actions.AbstractViewAction;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.gps.IGpsParser;
import com.bdaum.zoom.ui.gps.IWaypointCollector;
import com.bdaum.zoom.ui.gps.RasterCoordinate;
import com.bdaum.zoom.ui.gps.Trackpoint;
import com.bdaum.zoom.ui.gps.Waypoint;

public class GeonameAction extends AbstractViewAction {

	private static final String[] EMPTY = new String[0];
	private static final long ONEMINUTE = 60000L;
	private List<Trackpoint> trackpoints = new ArrayList<Trackpoint>(250);
	private Map<RasterCoordinate, Waypoint> waypoints;

	public GeonameAction() {
		setImageDescriptor(
				GpsActivator.imageDescriptorFromPlugin(GpsActivator.PLUGIN_ID, "/icons/geoname.gif")); //$NON-NLS-1$
	}

	@Override
	public void setEnabled(boolean readOnly, int imageCount, int localImageCount) {
		setEnabled(!readOnly && localImageCount != 0);
	}

	@Override
	public void run() {
		run(null, adaptable);
	}

	public void run(final File[] files, IAdaptable info) {
		if (info == null)
			return;
		final Shell shell = info.getAdapter(Shell.class);
		AssetSelection assetSelection = info.getAdapter(AssetSelection.class);
		String[] assetIds = assetSelection == null ? null : assetSelection.getLocalAssetIds();
		if ((assetIds == null || assetIds.length == 0)
				&& !AcousticMessageDialog.openConfirm(shell, Messages.getString("GeoNamingAction.Geonaming"), //$NON-NLS-1$
						Messages.getString("GeoNamingAction.Nothing_selected"))) //$NON-NLS-1$
			return;
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell, GpsPreferencePage.ID, EMPTY,
				"onAction"); //$NON-NLS-1$
		if (dialog.open() != PreferenceDialog.OK)
			return;
		Trackpoint[] pnts = new Trackpoint[0];
		final GpsConfiguration gpsConfiguration = GpsActivator.getDefault().createGpsConfiguration();
		if (files != null) {
			gpsConfiguration.excludeNoGoAreas = true;
			BusyIndicator.showWhile(shell.getDisplay(), () -> {
				for (int i = 0; i < files.length; i++) {
					if (gpsConfiguration.useWaypoints)
						try {
							IWaypointCollector collector = Ui.getUi().getWaypointCollector(files[i]);
							if (collector != null) {
								if (waypoints == null)
									waypoints = new HashMap<RasterCoordinate, Waypoint>(201);
								try (InputStream in1 = new BufferedInputStream(new FileInputStream(files[i]))) {
									collector.collect(in1, waypoints);
								}
							}
						} catch (FileNotFoundException e1) {
							showError(shell, NLS.bind(Messages.getString("GeotagAction.parsing_error"), //$NON-NLS-1$
									files[i]), e1);
						} catch (IOException e2) {
							showError(shell, NLS.bind(Messages.getString("GeotagAction.io_error_waypoints"), //$NON-NLS-1$
									files[i]), e2);
						} catch (ParseException e3) {
							showError(shell, NLS.bind(Messages.getString("GeotagAction.parsing_error"), //$NON-NLS-1$
									files[i]), e3);
						}
					try {
						IGpsParser parser = Ui.getUi().getGpsParser(files[i]);
						if (parser == null) {
							GpsActivator.getDefault()
									.logError(NLS.bind(Messages.getString("GeotagAction.no_parser"), //$NON-NLS-1$
											files[i]), null);
							continue;
						}
						try (InputStream in2 = new BufferedInputStream(new FileInputStream(files[i]))) {
							parser.parse(in2, trackpoints);
						}
					} catch (FileNotFoundException e4) {
						// should never happen
					} catch (ParseException e5) {
						showError(shell, NLS.bind(Messages.getString("GeotagAction.parsing_error"), //$NON-NLS-1$
								files[i]), e5);
					} catch (IOException e6) {
						showError(shell, NLS.bind(Messages.getString("GeotagAction.io_error"), //$NON-NLS-1$
								files[i]), e6);
					}
				}
			});
			pnts = trackpoints.toArray(new Trackpoint[trackpoints.size()]);
			Arrays.sort(pnts);
			if (!trackpoints.isEmpty() && info != null && gpsConfiguration.edit) {
				TrackpointDialog tdialog = new TrackpointDialog(shell, pnts, gpsConfiguration.tolerance * ONEMINUTE);
				if (tdialog.open() != TrackpointDialog.OK)
					return;
				pnts = tdialog.getResult();
			}
		}
		OperationJob.executeOperation(new GeotagOperation(pnts, assetIds, gpsConfiguration), info);
	}

	public void showError(Shell shell, String message, Throwable t) {
		AcousticMessageDialog.openError(shell, Messages.getString("GeoNamingAction.error_parsing_gpx"), //$NON-NLS-1$
				t == null ? message : NLS.bind("{0}: {1}", message, t)); //$NON-NLS-1$
	}

}
