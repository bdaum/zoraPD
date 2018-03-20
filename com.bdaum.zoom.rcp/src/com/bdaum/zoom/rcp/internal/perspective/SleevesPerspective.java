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

import com.bdaum.zoom.ui.internal.views.MetadataView;
import com.bdaum.zoom.ui.internal.views.ZuiView;

@SuppressWarnings("restriction")
public class SleevesPerspective extends AbstractPerspective {

	private static final String SLEEVES_FOLDER = "com.bdaum.zoom.ui.views.SleevesFolder"; //$NON-NLS-1$
	public static final String ID = "com.bdaum.zoom.rcp.SleevesPerspective"; //$NON-NLS-1$

	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);
		IFolderLayout folder = layout.createFolder(SLEEVES_FOLDER,
				IPageLayout.RIGHT,  0.9f, IPageLayout.ID_EDITOR_AREA);
		fillMainFolder(folder, ZuiView.ID, MAP_VIEW);
		IPlaceholderFolderLayout pfolder = layout.createPlaceholderFolder(
				HIERARCHY_FOLDER, IPageLayout.RIGHT, 0.8f, SLEEVES_FOLDER);
		pfolder.addPlaceholder(COMPONENTS_VIEW);
		pfolder.addPlaceholder(COMPOSITES_VIEW);
		pfolder.addPlaceholder(ORIGINALS_VIEW);
		pfolder.addPlaceholder(DERIVATIVES_VIEW);
		createCatalogFolder(layout, IPageLayout.LEFT, 0.19f, SLEEVES_FOLDER);
		layout.addView(MetadataView.ID, IPageLayout.RIGHT, 0.76f,
				SLEEVES_FOLDER);
		addCheatSheets(layout, MetadataView.ID);
		addFastViews(layout, IPageLayout.RIGHT, 0.7f, SLEEVES_FOLDER);
		layout.setEditorAreaVisible(false);
	}

	@Override
	protected String getId() {
		return ID;
	}

}
