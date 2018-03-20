package com.bdaum.zoom.ui.internal.actions;

import java.io.File;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import com.bdaum.zoom.cat.model.Bookmark;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.peer.ConnectionLostException;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.views.AbstractGalleryView;
import com.bdaum.zoom.ui.internal.views.CatalogView;

@SuppressWarnings("restriction")
public class GotoBookmarkAction extends Action {
	/**
	 *
	 */
	private Bookmark bookmark;
	private final IAdaptable adaptable;

	public GotoBookmarkAction(Bookmark bookmark, IAdaptable adaptable) {
		super(Messages.GotoBookmarkAction_goto_bookmark_title, Icons.gotoBookmark.getDescriptor());
		this.bookmark = bookmark;
		this.adaptable = adaptable;
		setToolTipText(Messages.GotoBookmarkAction_goto_bookmark_tooltip);
	}

	@Override
	public void run() {
		if (bookmark != null) {
			Asset asset = null;
			IDbManager dbManager = Core.getCore().getDbManager();
			String assetId = bookmark.getAssetId();
			if (bookmark.getCatFile() != null && !bookmark.getCatFile().isEmpty()) {
				IPeerService peerService = Core.getCore().getPeerService();
				if (peerService != null) {
					try {
						asset = peerService.obtainAsset(bookmark.getPeer(), new File(bookmark.getCatFile()),
								bookmark.getAssetId());
					} catch (ConnectionLostException e) {
						AcousticMessageDialog.openInformation(adaptable.getAdapter(Shell.class),
								Messages.GotoBookmarkAction_goto_bookmark_title, e.getLocalizedMessage());
						return;
					} catch (SecurityException e) {
						AcousticMessageDialog.openInformation(adaptable.getAdapter(Shell.class),
								Messages.GotoBookmarkAction_goto_bookmark_title,
								Messages.GotoBookmarkAction_unsufficient_rights);
						return;
					}
				}
			} else
				asset = dbManager.obtainAsset(bookmark.getAssetId());
			if (asset != null) {
				IWorkbenchPage page = adaptable.getAdapter(IWorkbenchPage.class);
				INavigationHistory navigationHistory = UiActivator.getDefault()
						.getNavigationHistory(page.getWorkbenchWindow());
				AbstractGalleryView aView = null;
				SmartCollection localCollection = null;
				boolean shown = false;
				lp: for (AbstractGalleryView gallery : navigationHistory.getOpenGalleries()) {
					if (aView == null)
						aView = gallery;
					IAssetProvider assetProvider = gallery.getAssetProvider();
					SmartCollectionImpl currentCollection = assetProvider.getCurrentCollection();
					if (currentCollection != null) {
						if (currentCollection.getNetwork()) {
							if (assetProvider.indexOf(assetId) >= 0) {
								shown = true;
								break lp;
							}
						} else
							localCollection = currentCollection;
					}
				}
				SmartCollection sm = null;
				String collectionId = bookmark.getCollectionId();
				if (collectionId != null)
					sm = dbManager.obtainById(SmartCollectionImpl.class, collectionId);
				if (sm == null) {
					if (bookmark.getCatFile() != null) {
						if (!shown) {
							SmartCollectionImpl newColl = new SmartCollectionImpl(
									Messages.GotoBookmarkAction_bookmarked, false, false, true, true, null, 0, null, 0,
									null, Constants.INHERIT_LABEL, null, 0, null);
							newColl.addCriterion(
									new CriterionImpl(Constants.OID, null, assetId, QueryField.EQUALS, false));
							navigationHistory.postSelection(new StructuredSelection(newColl));
						}
					} else
						sm = localCollection != null ? localCollection
								: Utilities.obtainFolderCollection(dbManager, asset.getUri(), asset.getVolume());
				}
				if (sm != null)
					try {
						((CatalogView) page.showView(CatalogView.ID)).setSelection(new StructuredSelection(sm), true);
					} catch (PartInitException e) {
						// should never happen
					}
				navigationHistory.postSelection(AssetSelection.EMPTY);
				if (asset != null)
					navigationHistory.postSelection(new AssetSelection(asset));
				page.activate(aView);
			} else
				AcousticMessageDialog.openInformation(adaptable.getAdapter(Shell.class),
						Messages.GotoBookmarkAction_goto_bookmark_title, Messages.GotoBookmarkAction_outdated_bookmark);
		}
	}

	/**
	 * @param bookmark
	 *            das zu setzende Objekt bookmark
	 */
	public void setBookmark(Bookmark bookmark) {
		this.bookmark = bookmark;
	}
}