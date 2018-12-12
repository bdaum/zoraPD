/*******************************************************************************
 * Copyright (c) 2009 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.rcp.internal.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPlaceholderFolderLayout;

import com.bdaum.zoom.ui.internal.views.CalendarView;
import com.bdaum.zoom.ui.internal.views.ExifView;
import com.bdaum.zoom.ui.internal.views.HistogramView;
import com.bdaum.zoom.ui.internal.views.IPTCView;
import com.bdaum.zoom.ui.internal.views.LightboxView;
import com.bdaum.zoom.ui.internal.views.PreviewView;
import com.bdaum.zoom.ui.internal.views.PropertiesView;

@SuppressWarnings("restriction")
public class LightboxPerspective extends AbstractPerspective {

	private static final String LIGHTBOX_FOLDER = "com.bdaum.zoom.ui.views.LightboxFolder"; //$NON-NLS-1$
	public static final String ID = "com.bdaum.zoom.rcp.LightboxPerspective"; //$NON-NLS-1$

	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);
		IFolderLayout folder = layout.createFolder(LIGHTBOX_FOLDER, IPageLayout.RIGHT, 0.7f,
				IPageLayout.ID_EDITOR_AREA);
		fillMainFolder(folder, LightboxView.ID, MAP_VIEW, CalendarView.ID);
		IPlaceholderFolderLayout pfolder = layout.createPlaceholderFolder(HIERARCHY_FOLDER, IPageLayout.RIGHT, 0.8f,
				LIGHTBOX_FOLDER);
		pfolder.addPlaceholder(COMPONENTS_VIEW);
		pfolder.addPlaceholder(COMPOSITES_VIEW);
		pfolder.addPlaceholder(ORIGINALS_VIEW);
		pfolder.addPlaceholder(DERIVATIVES_VIEW);
		createCatalogFolder(layout, IPageLayout.LEFT, 0.22f, LIGHTBOX_FOLDER);
		layout.addView(PropertiesView.ID, IPageLayout.BOTTOM, 0.8f, LIGHTBOX_FOLDER);
		layout.addView(ExifView.ID, IPageLayout.RIGHT, 0.33f, PropertiesView.ID);
		layout.addView(IPTCView.ID, IPageLayout.RIGHT, 0.5f, ExifView.ID);
		IFolderLayout hfolder = layout.createFolder(HISTOGRAM_FOLDER, IPageLayout.BOTTOM, 0.65f, CATALOG_FOLDER);
		hfolder.addView(PreviewView.ID);
		hfolder.addView(HistogramView.ID);
		addCheatSheets(layout, LIGHTBOX_FOLDER);
		addFastViews(layout, IPageLayout.RIGHT, 0.7f, LIGHTBOX_FOLDER);
		layout.setEditorAreaVisible(false);
	}

}
