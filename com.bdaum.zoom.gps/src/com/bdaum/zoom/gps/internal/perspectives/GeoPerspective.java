/*******************************************************************************
 * Copyright (c) 2009-2018 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.gps.internal.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;

import com.bdaum.zoom.gps.internal.views.GPSView;
import com.bdaum.zoom.rcp.internal.perspective.AbstractPerspective;
import com.bdaum.zoom.ui.internal.views.PreviewView;

@SuppressWarnings("restriction")
public class GeoPerspective extends AbstractPerspective {

	public static final String ID = "com.bdaum.zoom.rcp.GeoPerspective"; //$NON-NLS-1$
	private static final String GEO_FOLDER = "com.bdaum.zoom.ui.views.GpsGeoFolder"; //$NON-NLS-1$
	private static final String MAP_FOLDER = "com.bdaum.zoom.ui.views.MapFolder"; //$NON-NLS-1$

	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);
		layout.addView(VSTRIP_VIEW, IPageLayout.LEFT, 0.25f,
				IPageLayout.ID_EDITOR_AREA);
		createCatalogFolder(layout, IPageLayout.LEFT, 0.65f, VSTRIP_VIEW);
		IFolderLayout geofolder = layout.createFolder(GEO_FOLDER,
				IPageLayout.BOTTOM, 0.75f, CATALOG_FOLDER);
		geofolder.addView(PreviewView.ID);
		geofolder.addView(GPSView.ID);
		IFolderLayout folder = layout.createFolder(MAP_FOLDER,
				IPageLayout.RIGHT, 0.9f, IPageLayout.ID_EDITOR_AREA);
		fillMainFolder(folder, MAP_VIEW);
		addCheatSheets(layout, MAP_FOLDER);
		addFastViews(layout, IPageLayout.RIGHT, 0.7f, MAP_FOLDER);
		layout.setEditorAreaVisible(false);
	}

}
