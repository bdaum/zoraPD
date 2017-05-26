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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.rcp.internal.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPlaceholderFolderLayout;

import com.bdaum.zoom.ui.internal.views.HistogramView;
import com.bdaum.zoom.ui.internal.views.MetadataView;
import com.bdaum.zoom.ui.internal.views.PreviewView;
import com.bdaum.zoom.ui.internal.views.TableView;

@SuppressWarnings("restriction")
public class TablePerspective extends AbstractPerspective {

	public static final String ID = "com.bdaum.zoom.rcp.TablePerspective"; //$NON-NLS-1$

	public void createInitialLayout(IPageLayout layout) {
		super.createInitialLayout(layout);
		IFolderLayout folder = layout.createFolder(TABLE_FOLDER,
				IPageLayout.RIGHT,  0.9f, IPageLayout.ID_EDITOR_AREA);
		fillMainFolder(folder, TableView.ID, MAP_VIEW);
		IPlaceholderFolderLayout pfolder = layout.createPlaceholderFolder(
				HIERARCHY_FOLDER, IPageLayout.RIGHT, 0.8f, TABLE_FOLDER);
		pfolder.addPlaceholder(COMPONENTS_VIEW);
		pfolder.addPlaceholder(COMPOSITES_VIEW);
		pfolder.addPlaceholder(ORIGINALS_VIEW);
		pfolder.addPlaceholder(DERIVATIVES_VIEW);
		createCatalogFolder(layout, IPageLayout.LEFT, 0.19f, TABLE_FOLDER);
		layout.addView(MetadataView.ID, IPageLayout.RIGHT, 0.76f, TABLE_FOLDER);
		layout.addView(PreviewView.ID, IPageLayout.BOTTOM, 0.7f, CATALOG_FOLDER);
		layout.addView(HistogramView.ID, IPageLayout.BOTTOM, 0.85f,
				MetadataView.ID);
		addCheatSheets(layout, MetadataView.ID);
		addFastViews(layout, IPageLayout.LEFT, 0.7f, CATALOG_FOLDER);
		layout.setEditorAreaVisible(false);
	}
	
	@Override
	protected String getId() {
		return ID;
	}


}
