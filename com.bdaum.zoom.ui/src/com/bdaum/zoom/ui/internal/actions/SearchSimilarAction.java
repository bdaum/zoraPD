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

package com.bdaum.zoom.ui.internal.actions;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RetargetAction;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.db.DbAdapter;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.db.IDbListener;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.dialogs.EditMetaDialog;

public class SearchSimilarAction extends RetargetAction {

	private IAdaptable adaptable;
	private DbAdapter dbListener;
	private IDbManager dbManager = Core.getCore().getDbManager();

	public SearchSimilarAction(String id, String label, String tooltip, ImageDescriptor image, IAdaptable adaptable) {
		super(id, label);
		setImageDescriptor(image);
		setActionHandler(this);
		this.adaptable = adaptable;
		setToolTipText(tooltip);
		dbListener = new DbAdapter() {

			@Override
			public void databaseOpened(IDbManager manager, boolean primary) {
				if (primary) {
					dbManager = manager;
					setEnabled(!manager.getMeta(true).getNoIndex());
				}
			}

			@Override
			public void databaseClosed(IDbManager manager, int mode) {
				if (manager == dbManager && mode != IDbListener.EMERGENCY)
					setEnabled(false);
			}
		};
		ICore core = Core.getCore();
		core.getDbFactory().addDbListener(dbListener);
		setEnabled(!core.getDbManager().getMeta(true).getNoIndex());
	}

	@Override
	public void dispose() {
		Core.getCore().getDbFactory().removeDbListener(dbListener);
		super.dispose();
	}

	@Override
	public void runWithEvent(Event event) {
		run();
	}

	@Override
	public void run() {
		IDbFactory dbFactory = Core.getCore().getDbFactory();
		final Shell shell = adaptable.getAdapter(Shell.class);
		if (dbFactory.getLireServiceVersion() >= 0) {
			if (!dbManager.getMeta(true).getNoIndex()) {
				List<Asset> selectedAssets = adaptable.getAdapter(AssetSelection.class).getAssets();
				Asset asset = selectedAssets.isEmpty() ? null : selectedAssets.get(0);
				dbFactory.getLireService(true).performQuery(asset, adaptable, ICollectionProcessor.SIMILARITY);
			} else if (AcousticMessageDialog.openQuestion(shell, Messages.SearchSimilarAction_similarity_search,
					Messages.SearchSimilarAction_search_not_possible)) {
				BusyIndicator.showWhile(shell.getDisplay(), () -> {
					IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (activeWorkbenchWindow != null) {
						IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
						if (activePage != null) {
							EditMetaDialog mdialog = new EditMetaDialog(shell, activePage,
									Core.getCore().getDbManager(), false, null);
							mdialog.setInitialPage(EditMetaDialog.INDEXING);
							mdialog.open();
						}
					}
				});
			}
		}
	}

}
