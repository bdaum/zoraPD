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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.ui.internal.views.BookmarkView;
import com.bdaum.zoom.ui.internal.views.CatalogView;
import com.bdaum.zoom.ui.internal.views.DataEntryView;
import com.bdaum.zoom.ui.internal.views.DuplicatesView;
import com.bdaum.zoom.ui.internal.views.ExhibitionView;
import com.bdaum.zoom.ui.internal.views.HistoryView;
import com.bdaum.zoom.ui.internal.views.LightboxView;
import com.bdaum.zoom.ui.internal.views.SlideshowView;
import com.bdaum.zoom.ui.internal.views.TableView;
import com.bdaum.zoom.ui.internal.views.TagCloudView;
import com.bdaum.zoom.ui.internal.views.TrashcanView;
import com.bdaum.zoom.ui.internal.views.WebGalleryView;
import com.bdaum.zoom.ui.internal.views.ZuiView;

@SuppressWarnings("restriction")
public abstract class AbstractPerspective implements IPerspectiveFactory {

	public static final String CHEATSHEET_VIEW = "org.eclipse.ui.cheatsheets.views.CheatSheetView"; //$NON-NLS-1$
	public static final String VSTRIP_VIEW = "com.bdaum.zoom.ui.views.VStripView"; //$NON-NLS-1$
	public static final String HSTRIP_VIEW = "com.bdaum.zoom.ui.views.HStripView"; //$NON-NLS-1$
	public static final String PROGRESS_VIEW = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$
	public static final String LOG_VIEW = "org.eclipse.pde.runtime.LogView"; //$NON-NLS-1$
	public static final String MAP_VIEW = "com.bdaum.zoom.gps.MapView"; //$NON-NLS-1$
	public static final String GPS_VIEW = "com.bdaum.zoom.gps.GPSView"; //$NON-NLS-1$
	public static final String COMPONENTS_VIEW = "com.bdaum.zoom.ui.views.HierarchyViewComponents"; //$NON-NLS-1$
	public static final String COMPOSITES_VIEW = "com.bdaum.zoom.ui.views.HierarchyViewComposites"; //$NON-NLS-1$
	public static final String ORIGINALS_VIEW = "com.bdaum.zoom.ui.views.HierarchyViewOriginals"; //$NON-NLS-1$
	public static final String DERIVATIVES_VIEW = "com.bdaum.zoom.ui.views.HierarchyViewDerivatives"; //$NON-NLS-1$

	protected static final String HIERARCHY_FOLDER = "com.bdaum.zoom.ui.views.HierarchyFolder"; //$NON-NLS-1$
	protected static final String HISTOGRAM_FOLDER = "com.bdaum.zoom.ui.views.HistogramFolder"; //$NON-NLS-1$
	protected static final String TABLE_FOLDER = "com.bdaum.zoom.ui.views.TableFolder"; //$NON-NLS-1$
	protected static final String CATALOG_FOLDER = "com.bdaum.zoom.ui.views.CatalogFolder"; //$NON-NLS-1$
	protected static final String FASTVIEW_FOLDER = "com.bdaum.zoom.ui.views.FastViewFolder"; //$NON-NLS-1$

	private static final String[] MAINVIEWS = new String[] { LightboxView.ID, ZuiView.ID, TableView.ID,
			ExhibitionView.ID, SlideshowView.ID, WebGalleryView.ID, DataEntryView.ID, DuplicatesView.ID, MAP_VIEW };
	
	protected List<String> fastViews = new ArrayList<>(5);

	@Override
	public void createInitialLayout(IPageLayout layout) {
		final Display display = PlatformUI.getWorkbench().getDisplay();
		display.timerExec(1000, () -> {
			if (!display.isDisposed()) {
				IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow != null) {
					IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
					for (String fastViewId : fastViews) {
						IViewReference viewRef = activePage.findViewReference(fastViewId, null);
						if (viewRef != null)
							activePage.setPartState(viewRef, IWorkbenchPage.STATE_MINIMIZED);
					}
				}
			}
		});
	}

	protected abstract String getId();

	protected void fillMainFolder(IFolderLayout folder, String... visible) {
		for (String id : visible)
			folder.addView(id);
		outer: for (String id : MAINVIEWS) {
			for (String idv : visible)
				if (id.equals(idv))
					continue outer;
			folder.addPlaceholder(id);
		}
	}

	protected void addFastViews(IPageLayout layout, int dir, float ratio, String ref) {
		IFolderLayout folder = layout.createFolder(FASTVIEW_FOLDER, dir, ratio, ref);
		folder.addView(LOG_VIEW);
		fastViews.add(LOG_VIEW);
		folder.addView(TrashcanView.ID);
		fastViews.add(TrashcanView.ID);
		folder.addView(BookmarkView.ID);
		fastViews.add(BookmarkView.ID);
		folder.addView(TagCloudView.ID);
		fastViews.add(TagCloudView.ID);
	}

	protected void addCheatSheets(IPageLayout layout, String anchor) {
		layout.addPlaceholder(CHEATSHEET_VIEW, IPageLayout.RIGHT, 0.3f, anchor);
	}

	protected IFolderLayout createCatalogFolder(IPageLayout layout, int dir, float ratio, String ref) {
		IFolderLayout folder = layout.createFolder(CATALOG_FOLDER, dir, ratio, ref);
		folder.addView(CatalogView.ID);
		folder.addView(HistoryView.ID);
		return folder;
	}

}