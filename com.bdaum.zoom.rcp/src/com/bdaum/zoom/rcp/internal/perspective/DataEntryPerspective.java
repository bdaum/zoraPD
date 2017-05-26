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

import com.bdaum.zoom.ui.internal.views.DataEntryView;
import com.bdaum.zoom.ui.internal.views.PreviewView;

@SuppressWarnings("restriction")
public class DataEntryPerspective extends AbstractPerspective {

	public static final String ID = "com.bdaum.zoom.rcp.DataEntryPerspective"; //$NON-NLS-1$
	private static final String DATAENTRY_FOLDER = "com.bdaum.zoom.ui.views.DataEntryFolder"; //$NON-NLS-1$

	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);
		IFolderLayout folder = layout.createFolder(DATAENTRY_FOLDER,
				IPageLayout.RIGHT,  0.9f, IPageLayout.ID_EDITOR_AREA);
		fillMainFolder(folder, DataEntryView.ID, MAP_VIEW);
		createCatalogFolder(layout, IPageLayout.LEFT, 0.19f,
				DATAENTRY_FOLDER);
		layout.addView(HSTRIP_VIEW, IPageLayout.BOTTOM, 0.76f,
				DATAENTRY_FOLDER);
		layout.addView(PreviewView.ID, IPageLayout.BOTTOM, 0.7f, CATALOG_FOLDER);
		addCheatSheets(layout, DATAENTRY_FOLDER);
		addFastViews(layout, IPageLayout.LEFT, 0.7f, CATALOG_FOLDER);
		layout.setEditorAreaVisible(false);
	}
	
	@Override
	protected String getId() {
		return ID;
	}


}
