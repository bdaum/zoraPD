/*******************************************************************************
 * Copyright (c) 2017 Berthold Daum.
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

import com.bdaum.zoom.ui.internal.views.ExhibitionView;
import com.bdaum.zoom.ui.internal.views.PreviewView;
import com.bdaum.zoom.ui.internal.views.SlideshowView;
import com.bdaum.zoom.ui.internal.views.WebGalleryView;

@SuppressWarnings("restriction")
public class PresentationPerspective extends AbstractPerspective {

	private static final String PRESENTATION_FOLDER = "com.bdaum.zoom.ui.views.PresentationFolder"; //$NON-NLS-1$
	public static final String ID = "com.bdaum.zoom.PresentationPerspective"; //$NON-NLS-1$

	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);
		IFolderLayout folder = layout.createFolder(PRESENTATION_FOLDER,
				IPageLayout.RIGHT, 0.9f, IPageLayout.ID_EDITOR_AREA);
		fillMainFolder(folder, ExhibitionView.ID, SlideshowView.ID, WebGalleryView.ID);
		createCatalogFolder(layout, IPageLayout.LEFT, 0.19f, PRESENTATION_FOLDER);
		layout.addView(HSTRIP_VIEW, IPageLayout.BOTTOM, 0.76f,
				PRESENTATION_FOLDER);
		layout.addView(PreviewView.ID, IPageLayout.BOTTOM, 0.7f, CATALOG_FOLDER);
		addCheatSheets(layout, PRESENTATION_FOLDER);
		addFastViews(layout, IPageLayout.RIGHT, 0.7f, PRESENTATION_FOLDER);
		layout.setEditorAreaVisible(false);
	}

	@Override
	protected String getId() {
		return ID;
	}
}
