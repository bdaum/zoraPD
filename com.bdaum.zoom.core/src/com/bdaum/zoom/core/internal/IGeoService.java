package com.bdaum.zoom.core.internal;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;

public interface IGeoService {
	/**
	 * Shows the location of an asset
	 * @param asset - asset with geo tags
	 * @param external - true if an external web browser is to be used, false if the internal map view is to be used
	 */
	void showLocation(Asset asset, boolean external);

	/**
	 * Displays a map with visible track lines
	 * @param parent - parent composite for the map control
	 * @param assets - assets with geo tags
	 * @param withMarkers - show also markers 
	 * @return the map control
	 */
	Control showTrack(Composite parent,  AssetImpl[] assets, boolean withMarkers);

	/**
	 * Returns code segment loading all the necessary scripts for map operation
	 * @param root - the root URL used for communication between host and map script. Specify "" for no communication
	 * @return code segment
	 */
	String getMapContext(String root);
}
