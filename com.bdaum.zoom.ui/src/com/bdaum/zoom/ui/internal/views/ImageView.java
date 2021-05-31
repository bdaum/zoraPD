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
 * (c) 2009-2021 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.views;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.operations.UndoRedoActionGroup;

import com.bdaum.zoom.cat.model.BookmarkImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IGeoService;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.peer.AssetOrigin;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ColorCodeOperation;
import com.bdaum.zoom.operations.internal.RotateOperation;
import com.bdaum.zoom.operations.internal.SetStatusOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.IMediaUiExtension;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.IZoomActionConstants;
import com.bdaum.zoom.ui.IZoomCommandIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.actions.CopyAction;
import com.bdaum.zoom.ui.internal.actions.GotoBookmarkAction;
import com.bdaum.zoom.ui.internal.actions.PasteAction;
import com.bdaum.zoom.ui.internal.actions.PrintAction;
import com.bdaum.zoom.ui.internal.actions.ZoomActionFactory;
import com.bdaum.zoom.ui.internal.commands.AbstractCatCommandHandler;
import com.bdaum.zoom.ui.internal.commands.OpenCatalogCommand;
import com.bdaum.zoom.ui.internal.dialogs.RatingDialog;
import com.bdaum.zoom.ui.internal.hover.HoverInfo;
import com.bdaum.zoom.ui.internal.hover.IGalleryHover;
import com.bdaum.zoom.ui.internal.hover.IHoverInfo;

@SuppressWarnings("restriction")
public abstract class ImageView extends BasicView implements CatalogListener, IDragHost, IDropHost, IMenuListener {

	public class GalleryHover implements IGalleryHover {

		@SuppressWarnings("unchecked")
		public IHoverInfo getHoverInfo(IHoverSubject viewer, Event event) {
			String tooltip = viewer.getTooltip(event.x, event.y);
			if (tooltip != null)
				return new HoverInfo(tooltip);
			Object ob = viewer.findObject(event);
			Asset asset = null;
			if (ob instanceof Asset)
				asset = (Asset) ob;
			else if (ob instanceof List<?>)
				for (Asset a : (List<Asset>) ob) {
					asset = a;
					break;
				}
			if (asset != null) {
				ImageRegion[] foundRegions = null;
				String[] regionIds = asset.getPerson();
				if (regionIds != null && regionIds.length > 0)
					foundRegions = viewer.findAllRegions(event);
				return new HoverInfo(ob, foundRegions);
			}
			return null;
		}

	}

	private static final int THRESHHOLD = 3;
	private static final String VIEWID = "com.bdaum.zoom.ui.views.ImageView"; //$NON-NLS-1$
	protected IAction editAction;
	protected IAction editWithAction;
	protected IAction viewImageAction;
	protected IAction deleteAction;
	protected IAction showInFolderAction;
	protected IAction showInTimeLineAction;
	protected IAction addVoiceNoteAction;
	protected IAction rotateLeftAction;
	protected IAction rotateRightAction;
	protected IAction ratingAction;
	protected IAction colorCodeAction;
	protected IAction playVoiceNoteAction;
	protected IAction searchSimilarAction;
	protected IAction timeSearchAction;
	protected IAction proximitySearchAction;
	private MenuManager contextMenuMgr;
	protected IAction showDerivativesAction;
	protected IAction showOriginalAction;
	protected IAction showCompositesAction;
	protected IAction showComponentsAction;
	protected IAction copyMetadataAction;
	protected IAction pasteMetadataAction;
	protected IAction timeShiftAction;
	protected IAction refreshAction;
	protected IAction openCatAction;
	protected IAction addBookmarkAction;
	protected IAction addToAlbumAction;
	protected IAction categorizeAction;

	private static final Point pnt = new Point(0, 0);
	private IAction keyDefAction;
	private IAction moveAction;
	private IAction slideshowAction;
	private IAction copyAction;
	private IAction pasteAction;
	private boolean dragging;
	private int ignoreTab;

	protected QueryField[] getHoverNodes() {
		return UiActivator.getDefault().getHoverNodes();
	}

	public void rotate(Asset asset, int degrees) {
		OperationJob.executeOperation(new RotateOperation(Collections.singletonList(asset), degrees), this);
	}

	public void colorCode(Asset asset, int code) {
		OperationJob.executeOperation(new ColorCodeOperation(Collections.singletonList(asset), code), this);
	}

	public void showLocation(Asset asset, boolean external) {
		IGeoService service = CoreActivator.getDefault().getGeoService();
		if (service != null)
			service.showLocation(asset, external);
	}

	public void setStatus(Asset asset, int status) {
		if (status >= 0 && status != asset.getStatus())
			OperationJob.executeOperation(new SetStatusOperation(Collections.singletonList(asset), status), this);
	}

	public void assetsModified(Collection<? extends Asset> assets, QueryField node) {
		// do nothing
	}

	public void catalogModified() {
		// do nothing
	}

	public void applyRules(Collection<? extends Asset> assets, QueryField node) {
		// do nothing
	}

	public void catalogOpened(boolean newDb) {
		// do nothing
	}

	public void catalogClosed(int mode) {
		// do nothing
	}

	public void hierarchyModified() {
		// do nothing
	}

	public void structureModified() {
		// do nothing
	}

	public void bookmarksModified() {
		// do nothing
	}

	public void setCatalogSelection(ISelection selection, boolean forceUpdate) {
		// do nothing
	}

	protected void hookContextMenu() {
		if (contextMenuMgr == null) {
			contextMenuMgr = new MenuManager("#PopupMenu", VIEWID); //$NON-NLS-1$
			contextMenuMgr.setRemoveAllWhenShown(true);
			contextMenuMgr.addMenuListener(this);
			getControl().setMenu(contextMenuMgr.createContextMenu(getControl()));
			getSite().registerContextMenu(contextMenuMgr, this);
			getControl().addListener(SWT.MouseDown, this);
		}
	}

	public void menuAboutToShow(IMenuManager manager) {
		ImageView.this.fillContextMenu(manager);
		manager.updateAll(true);
	}

	protected void unhookContextMenu() {
		if (contextMenuMgr != null) {
			getControl().removeListener(SWT.MouseDown, this);
			contextMenuMgr.removeAll();
			contextMenuMgr.dispose();
			contextMenuMgr = null;
		}
	}

	@Override
	public void handleEvent(Event e) {
		if (e.type == SWT.MouseDown)
			setMousePosition(e.x, e.y);
		super.handleEvent(e);
	}

	public Point getMousePosition() {
		return pnt;
	}

	public Point getMouseDisplayPosition() {
		return getControl().toDisplay(pnt);
	}

	public Point setMousePosition(int x, int y) {
		pnt.x = x;
		pnt.y = y;
		return pnt;
	}

	protected abstract void fillContextMenu(IMenuManager manager);

	protected void fillAdditions(IMenuManager manager) {
		manager.add(new Separator());
		ISelection sel = getSelection();
		if (sel instanceof AssetSelection && ((AssetSelection) sel).size() == 1) {
			Asset asset = ((AssetSelection) sel).getFirstElement();
			if (asset != null && asset.getFileState() == IVolumeManager.PEER) {
				IPeerService peerService = Core.getCore().getDbFactory().getPeerService();
				if (peerService != null) {
					AssetOrigin assetOrigin = peerService.getAssetOrigin(asset.getStringId());
					if (assetOrigin != null && peerService.isLocal(assetOrigin.getLocation())) {
						openCatAction.setText(NLS.bind(Messages.getString("ImageView.open_owning_cat"), //$NON-NLS-1$
								assetOrigin.getCatFile()));
						manager.add(openCatAction);
					}
				}
			}
		}
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected IActionBars contributeToActionBars() {
		IViewSite viewSite = getViewSite();
		IActionBars bars = viewSite.getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
		new UndoRedoActionGroup(viewSite,
				undoContext = PlatformUI.getWorkbench().getOperationSupport().getUndoContext(), true)
						.fillActionBars(bars);
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
		bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), pasteAction);
		bars.setGlobalActionHandler(ActionFactory.PRINT.getId(), new PrintAction(viewSite.getWorkbenchWindow()));
		return bars;
	}

	protected abstract void fillLocalToolBar(IToolBarManager toolBarManager);

	protected abstract void fillLocalPullDown(IMenuManager menuManager);

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (ImageView.class.equals(adapter))
			return this;
		if (AssetSelection.class.equals(adapter))
			return getAssetSelection();
		return super.getAdapter(adapter);
	}

	protected void makeActions(IActionBars bars) {
		super.makeActions();
		addBookmarkAction = addAction(ZoomActionFactory.ADDBOOKMARK.create(bars, this));
		proximitySearchAction = addAction(ZoomActionFactory.PROXIMITY.create(bars, this));
		deleteAction = addAction(ZoomActionFactory.DELETE.create(bars, this));
		refreshAction = addAction(ZoomActionFactory.REFRESH.create(bars, this));
		showInFolderAction = addAction(ZoomActionFactory.SHOWINFOLDER.create(bars, this));
		showInTimeLineAction = addAction(ZoomActionFactory.SHOWINTIMELINE.create(bars, this));
		editAction = addAction(ZoomActionFactory.EDIT.create(bars, this));
		editWithAction = addAction(ZoomActionFactory.EDITWITH.create(bars, this));
		rotateLeftAction = addAction(ZoomActionFactory.ROTATELEFT.create(bars, this));
		rotateRightAction = addAction(ZoomActionFactory.ROTATERIGHT.create(bars, this));
		keyDefAction = addAction(ZoomActionFactory.UPDATEKEYWORDS.create(null, this));
		timeShiftAction = addAction(ZoomActionFactory.TIMESHIFT.create(bars, this));
		ratingAction = addAction(ZoomActionFactory.RATING.create(bars, this));
		colorCodeAction = addAction(ZoomActionFactory.COLORCODE.create(bars, this));
		playVoiceNoteAction = addAction(ZoomActionFactory.PLAYVOICENOTE.create(bars, this));
		addVoiceNoteAction = addAction(ZoomActionFactory.ADDVOICENOTE.create(bars, this));
		searchSimilarAction = addAction(ZoomActionFactory.SEARCHSIMILAR.create(bars, this));
		timeSearchAction = addAction(ZoomActionFactory.TIMESEARCH.create(bars, this));
		viewImageAction = addAction(ZoomActionFactory.VIEWIMAGE.create(bars, this));
		showDerivativesAction = addAction(ZoomActionFactory.SHOWDERIVATIVES.create(bars, this));
		showOriginalAction = addAction(ZoomActionFactory.SHOWORIGINALS.create(bars, this));
		showCompositesAction = addAction(ZoomActionFactory.SHOWCOMPOSITES.create(bars, this));
		showComponentsAction = addAction(ZoomActionFactory.SHOWCOMPONENTS.create(bars, this));
		copyMetadataAction = addAction(ZoomActionFactory.COPYMETADATA.create(bars, this));
		pasteMetadataAction = addAction(ZoomActionFactory.PASTEMETADATA.create(bars, this));
		Clipboard clipboard = getClipboard();
		final IWorkbenchWindow workbenchWindow = getSite().getWorkbenchWindow();
		copyAction = addAction(new CopyAction(workbenchWindow, clipboard));
		pasteAction = addAction(new PasteAction(workbenchWindow, clipboard, ActionFactory.PASTE.getId(),
				Messages.getString("ImageView.paste"))); //$NON-NLS-1$
		moveAction = addAction(ZoomActionFactory.MOVE.create(bars, this));
		openCatAction = new Action(Messages.getString("ImageView.open_owning_cat")) { //$NON-NLS-1$
			@Override
			public void run() {
				Asset asset = getAssetSelection().getFirstElement();
				if (asset != null && asset.getFileState() == IVolumeManager.PEER) {
					IPeerService peerService = Core.getCore().getDbFactory().getPeerService();
					if (peerService != null) {
						AssetOrigin assetOrigin = peerService.getAssetOrigin(asset.getStringId());
						if (assetOrigin != null && peerService.isLocal(assetOrigin.getLocation())) {
							File catFile = assetOrigin.getCatFile();
							if (catFile.exists()) {
								AbstractCatCommandHandler command = new OpenCatalogCommand();
								command.setCatFile(catFile);
								command.init(workbenchWindow);
								command.run();
								new GotoBookmarkAction(new BookmarkImpl("", //$NON-NLS-1$
										asset.getStringId(), null, new Date(), null, null), ImageView.this).run();
							}
						}
					}
				}
			}
		};
		addToAlbumAction = addAction(ZoomActionFactory.ADDTOALBUM.create(bars, this));
		categorizeAction = addAction(ZoomActionFactory.CATEGORIZE.create(bars, this));
		slideshowAction = addAction(ZoomActionFactory.SLIDESHOW.create(bars, this));
	}

	@Override
	protected void registerCommands() {
		registerCommand(proximitySearchAction, IZoomCommandIds.ProximityCommand);
		registerCommand(deleteAction, IZoomCommandIds.DeleteCommand);
		registerCommand(refreshAction, IZoomCommandIds.RefreshCommand);
		registerCommand(showInFolderAction, IZoomCommandIds.ShowInFolder);
		registerCommand(editAction, IZoomCommandIds.EditCommand);
		registerCommand(editWithAction, IZoomCommandIds.EditWithCommand);
		registerCommand(rotateLeftAction, IZoomCommandIds.RotateAntiClockwiseCommand);
		registerCommand(rotateRightAction, IZoomCommandIds.RotateClockwiseCommand);
		registerCommand(keyDefAction, IZoomCommandIds.AddKeywordsCommand);
		registerCommand(timeShiftAction, IZoomCommandIds.TimeShift);
		registerCommand(ratingAction, IZoomCommandIds.Rate);
		registerCommand(colorCodeAction, IZoomCommandIds.ColorCode);
		registerCommand(playVoiceNoteAction, IZoomCommandIds.PlayVoiceNote);
		registerCommand(addVoiceNoteAction, IZoomCommandIds.AddVoiceNote);
		registerCommand(searchSimilarAction, IZoomCommandIds.SearchSimilarCommand);
		registerCommand(timeSearchAction, IZoomCommandIds.TimeSearchCommand);
		registerCommand(viewImageAction, IZoomCommandIds.ViewImage);
		registerCommand(showDerivativesAction, IZoomCommandIds.ShowDerivatives);
		registerCommand(showOriginalAction, IZoomCommandIds.ShowOriginals);
		registerCommand(showCompositesAction, IZoomCommandIds.ShowComposites);
		registerCommand(showComponentsAction, IZoomCommandIds.ShowComponents);
		registerCommand(copyMetadataAction, IZoomCommandIds.CopyMetadataCommand);
		registerCommand(pasteMetadataAction, IZoomCommandIds.PasteMetadataCommand);
		registerCommand(addToAlbumAction, IZoomCommandIds.AddToAlbum);
		registerCommand(categorizeAction, IZoomCommandIds.CategorizeCommand);
		registerCommand(moveAction, IZoomCommandIds.MoveCommand);
		registerCommand(slideshowAction, IZoomCommandIds.AdhocSlideshowCommand);
		registerCommand(showInTimeLineAction, IZoomCommandIds.ShowInTimeline);
		registerCommand(copyAction, IWorkbenchCommandConstants.EDIT_COPY);
		registerCommand(pasteAction, IWorkbenchCommandConstants.EDIT_PASTE);
		registerCommand(addBookmarkAction, IWorkbenchCommandConstants.EDIT_ADD_BOOKMARK);
		registerMediaContributions();
		super.registerCommands();
	}

	protected void registerMediaContributions() {
		for (IMediaUiExtension ext : UiActivator.getDefault().getUiMediaExtensions())
			ext.registerMediaContributions(this);
	}

	@Override
	public void updateActions(boolean force) {
		if (isVisible() || force) {
			int count = getSelectionCount(false);
			int localCount = getSelectionCount(true);
			boolean writable = !dbIsReadonly();
			boolean one = count == 1;
			boolean localOne = localCount == 1;
			boolean selected = count > 0;
			boolean localSelected = localCount > 0;
			boolean multiwrite = localSelected && writable;
			AssetSelection assetSelection = getAssetSelection();
			proximitySearchAction.setEnabled(selected && hasGps());
			deleteAction.setEnabled(multiwrite);
			refreshAction.setEnabled(multiwrite || collectionSelected());
			showInFolderAction.setEnabled(localOne);
			editAction.setEnabled(localSelected);
			editWithAction.setEnabled(localSelected);
			rotateLeftAction.setEnabled(localSelected && writable && isMedia(assetSelection, QueryField.PHOTO, true));
			rotateRightAction.setEnabled(localSelected && writable && isMedia(assetSelection, QueryField.PHOTO, true));
			keyDefAction.setEnabled(
					localSelected && writable && isMedia(assetSelection, QueryField.PHOTO | IMediaSupport.VIDEO, true));
			timeShiftAction.setEnabled(localSelected && writable);
			ratingAction.setEnabled(localSelected && writable);
			colorCodeAction.setEnabled(localSelected && writable);
			playVoiceNoteAction.setEnabled(localOne && hasVoiceNote(assetSelection));
			addVoiceNoteAction.setEnabled(localOne && writable);
			searchSimilarAction.setEnabled(count <= 1);
			timeSearchAction.setEnabled(true);
			viewImageAction.setEnabled(one || count == 2);
			showComponentsAction.setEnabled(localOne);
			showCompositesAction.setEnabled(localOne);
			showDerivativesAction.setEnabled(localOne);
			showOriginalAction.setEnabled(localOne);
			copyMetadataAction.setEnabled(one);
			pasteMetadataAction.setEnabled(multiwrite && testClipboardForMetadata());
			addToAlbumAction.setEnabled(localSelected);
			categorizeAction.setEnabled(localSelected);
			moveAction.setEnabled(selected);
			slideshowAction.setEnabled(selected && isMedia(assetSelection, QueryField.PHOTO, false));
			showInTimeLineAction.setEnabled(localOne && hasTimeLine());
			copyAction.setEnabled(localSelected);
			pasteAction.setEnabled(writable && testClipboardForImages());
			addBookmarkAction.setEnabled(one && writable);
			updateMediaContributions(count, localCount, assetSelection);
			updateActions(count, localCount);
		}
	}

	protected void updateMediaContributions(int count, int localCount, AssetSelection assetSelection) {
		for (IMediaUiExtension ext : UiActivator.getDefault().getUiMediaExtensions())
			ext.updateMediaContributions(this, count, localCount, assetSelection);
	}

	private boolean collectionSelected() {
		CatalogView catView = (CatalogView) UiUtilities.findViewNoRestore(getSite().getPage(), CatalogView.ID);
		if (catView != null)
			for (Iterator<?> iterator = ((IStructuredSelection) catView.getSelection()).iterator(); iterator.hasNext();)
				if (iterator.next() instanceof SmartCollection)
					return true;
		return false;
	}

	private boolean testClipboardForMetadata() {
		Object contents = getClipboardContents(TextTransfer.getInstance());
		if (contents instanceof String) {
			String text = (String) contents;
			if (text.indexOf("xmpmeta") >= 0 //$NON-NLS-1$
					&& text.indexOf("adobe:ns:meta") >= 0) //$NON-NLS-1$
				return true;
		}
		return false;
	}

	private boolean testClipboardForImages() {
		Object contents = getClipboardContents(FileTransfer.getInstance());
		return (contents instanceof String[] && ((String[]) contents).length > 0);
	}

	private Object getClipboardContents(Transfer transfer) {
		return getClipboard().getContents(transfer);
	}

	private Clipboard getClipboard() {
		return UiActivator.getDefault().getClipboard(getSite().getShell().getDisplay());
	}

	public boolean hasGps() {
		int n = 0;
		for (Asset asset : getAssetSelection().getAssets())
			if (++n > THRESHHOLD || (!Double.isNaN(asset.getGPSLatitude()) && !Double.isNaN(asset.getGPSLongitude())))
				return true;
		return false;
	}

	@Override
	protected void installListeners() {
		super.installListeners();
		Core.getCore().addCatalogListener(this);
	}

	@Override
	protected void uninstallListeners() {
		super.uninstallListeners();
		Core.getCore().removeCatalogListener(this);
	}

	@Override
	public IGalleryHover getGalleryHover(Event event) {
		return new GalleryHover();
	}

	protected abstract int getSelectionCount(boolean local);

	@Override
	public void dispose() {
		Core.getCore().removeCatalogListener(this);
		if (searchSimilarAction instanceof RetargetAction)
			((RetargetAction) searchSimilarAction).dispose();
		super.dispose();
	}

	protected void ignoreTab() {
		++ignoreTab;
	}

	@Override
	protected void onKeyUp(Event e) {
		switch (e.character) {
		case SWT.TAB:
			if (ignoreTab > 0) {
				--ignoreTab;
				return;
			}
			if ((e.stateMask & SWT.CTRL) == 0) {
				Event event = new Event();
				event.type = SWT.KeyUp;
				event.stateMask = e.stateMask;
				event.character = e.character;
				event.keyCode = e.keyCode;
				viewImageAction.runWithEvent(event);
			}
			return;
		case 13:
			((e.stateMask & SWT.SHIFT) != 0 ? editWithAction : editAction).run();
			return;
		case SWT.DEL:
			ZoomActionFactory.rate(getAssetSelection().getAssets(), this, RatingDialog.DELETE);
			return;
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
			ZoomActionFactory.rate(getAssetSelection().getAssets(), this, e.character - '0');
			return;
		}
	}

	protected void fillEditAndSearchGroup(IMenuManager manager, boolean readOnly) {
		manager.add(viewImageAction);
		manager.add(editAction);
		manager.add(editWithAction);
		addMediaContributions(manager, IZoomActionConstants.MB_EDIT);
		manager.add(new Separator(IZoomActionConstants.MB_EDIT));
		if (Core.getCore().getDbFactory().getLireServiceVersion() >= 0)
			manager.add(searchSimilarAction);
		manager.add(timeSearchAction);
		manager.add(proximitySearchAction);
		manager.add(new Separator(IZoomActionConstants.MB_SEARCH));
	}

	protected void addMediaContributions(IMenuManager manager, String anchor) {
		for (IMediaUiExtension ext : UiActivator.getDefault().getUiMediaExtensions())
			ext.addMediaContributions(manager, anchor, this);
	}

	protected void fillMetaData(IMenuManager manager, boolean readOnly) {
		manager.add(copyMetadataAction);
		if (!readOnly)
			manager.add(pasteMetadataAction);
		manager.add(new Separator(IZoomActionConstants.MB_META));

	}

	protected void fillVoiceNote(IMenuManager manager, boolean readOnly) {
		if (!readOnly)
			manager.add(addVoiceNoteAction);
		manager.add(playVoiceNoteAction);
		manager.add(new Separator(IZoomActionConstants.MB_VOICE));
	}

	protected void fillRotateGroup(IMenuManager manager, boolean readOnly) {
		if (!readOnly) {
			manager.add(rotateRightAction);
			manager.add(rotateLeftAction);
			manager.add(new Separator());
			manager.add(ratingAction);
			manager.add(colorCodeAction);
			manager.add(new Separator(IZoomActionConstants.MB_ROTATE));
		}
	}

	protected void fillShowAndDeleteGroup(IMenuManager manager, boolean readOnly) {
		manager.add(showInFolderAction);
		manager.add(showInTimeLineAction);
		if (!readOnly) {
			manager.add(new Separator());
			manager.add(addBookmarkAction);
			manager.add(addToAlbumAction);
			manager.add(refreshAction);
			manager.add(deleteAction);
		}
	}

	protected void fillRelationsGroup(IMenuManager manager) {
		MenuManager m = new MenuManager(Messages.getString("ImageView.relations")); //$NON-NLS-1$
		manager.add(m);
		m.add(showDerivativesAction);
		m.add(showOriginalAction);
		m.add(showCompositesAction);
		m.add(showComponentsAction);
	}

	protected RGB getBWmode() {
		return null;
	}

	protected void addExplanationListener(boolean elaborate) {
		Control control = getControl();
		if (control != null && !control.isDisposed()) {
			Listener listener = new Listener() {
				public void handleEvent(Event e) {
					if (e.type == SWT.KeyDown) {
						if (!getSelection().isEmpty()
								&& (e.keyCode == SWT.SHIFT || e.keyCode == SWT.CTRL || e.keyCode == SWT.ALT))
							computeMode(e.stateMask | e.keyCode);
					} else {
						if (e.keyCode == SWT.SHIFT || e.keyCode == SWT.CTRL || e.keyCode == SWT.ALT) {
							int state = e.stateMask - e.keyCode;
							if ((state & (SWT.SHIFT | SWT.CTRL | SWT.ALT)) == 0) {
								if (elaborate)
									setStatusMessage(Messages.getString("ImageView.external_viewer"), false); //$NON-NLS-1$
								else
									updateStatusLine();
							} else
								computeMode(state);
						}
					}
				}

				private void computeMode(int state) {
					if (getSelectionCount(false) == 1) {
						String message = Messages.getString("ImageView.internal_viewer_selected"); //$NON-NLS-1$
						if ((state & SWT.CTRL) != 0)
							message += ((state & SWT.SHIFT) != 0 ? Messages.getString("ImageView.raw_original_mode") : //$NON-NLS-1$
							Messages.getString("ImageView.crop_mode")); //$NON-NLS-1$
						message += (((state & SWT.ALT) != 0) != (getBWmode() != null))
								? Messages.getString("ImageView.bw_mode") //$NON-NLS-1$
								: Messages.getString("ImageView.color_mode"); //$NON-NLS-1$
						setStatusMessage(message, false);
					}
				}
			};
			control.addListener(SWT.KeyDown, listener);
			control.addListener(SWT.KeyUp, listener);
			control.addListener(SWT.MouseExit, new Listener() {
				@Override
				public void handleEvent(Event e) {
					setStatusMessage("", false); //$NON-NLS-1$
				}
			});
		}
	}

	@SuppressWarnings("unused")
	protected void addDragSupport() {
		new AssetDragSourceListener(this, DND.DROP_COPY | DND.DROP_MOVE);
	}

	@SuppressWarnings("unused")
	protected void addDropSupport(boolean images, boolean gps, boolean sound) {
		new ImageDropTargetListener(this, images, gps, sound, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
	}

	protected void addDragDropSupport(boolean images, boolean gps, boolean sound) {
		addDropSupport(images, gps, sound);
		addDragSupport();
	}

	public SmartCollectionImpl getSelectedCollection() {
		INavigationHistory navigationHistory = getNavigationHistory();
		return navigationHistory == null ? null : navigationHistory.getSelectedCollection();
	}

	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}

	public boolean isDragging() {
		return dragging;
	}

	public ImageRegion findBestFaceRegion(int x, int y, boolean all) {
		return null;
	}

}