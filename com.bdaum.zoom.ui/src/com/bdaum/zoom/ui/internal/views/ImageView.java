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
 * (c) 2009-2016 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.views;

import java.io.File;
import java.util.ArrayList;
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
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;
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
import com.bdaum.zoom.core.internal.peer.AssetOrigin;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ColorCodeOperation;
import com.bdaum.zoom.operations.internal.RotateOperation;
import com.bdaum.zoom.operations.internal.SetStatusOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.IZoomActionConstants;
import com.bdaum.zoom.ui.IZoomCommandIds;
import com.bdaum.zoom.ui.internal.UiActivator;
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
public abstract class ImageView extends BasicView implements CatalogListener, IDragHost, IDropHost {

	public static class GalleryHover implements IGalleryHover {

		@SuppressWarnings("unchecked")
		public IHoverInfo getHoverInfo(IHoverSubject viewer, MouseEvent event) {
			String tooltip = viewer.getTooltip(event.x, event.y);
			if (tooltip != null)
				return new HoverInfo(tooltip);
			Object ob = viewer.findObject(event);
			Asset asset = null;
			if (ob instanceof Asset)
				asset = (Asset) ob;
			else if (ob instanceof List<?>) {
				for (Asset a : (List<Asset>) ob) {
					asset = a;
					break;
				}
			}
			if (asset != null) {
				ImageRegion[] foundRegions = null;
				String[] regionIds = asset.getPerson();
				if (regionIds != null && regionIds.length > 0)
					foundRegions = viewer.findAllRegions(event);
				return new HoverInfo(ob, foundRegions, UiActivator.getDefault().getHoverNodes());
			}
			return null;
		}
	}

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
	protected IAction selectAllAction;
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
	private IAction keyDefAction;
	private IAction moveAction;
	private IAction slideshowAction;
	private IAction copyAction;
	private IAction pasteAction;

	public void rotate(Asset asset, int degrees) {
		OperationJob.executeOperation(new RotateOperation(Collections.singletonList(asset), degrees), this);
	}

	public void colorCode(Asset asset, int code) {
		List<Asset> assets = new ArrayList<Asset>(1);
		assets.add(asset);
		OperationJob.executeOperation(new ColorCodeOperation(assets, code), this);
	}

	public void showLocation(Asset asset, boolean external) {
		IGeoService service = CoreActivator.getDefault().getGeoService();
		if (service != null)
			service.showLocation(asset, external);
	}

	public void setStatus(Asset asset, int status) {
		if (status >= 0 && status != asset.getStatus()) {
			OperationJob.executeOperation(new SetStatusOperation(Collections.singletonList(asset), status), this);
		}
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
			contextMenuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
			contextMenuMgr.setRemoveAllWhenShown(true);
			contextMenuMgr.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					ImageView.this.fillContextMenu(manager);
					manager.updateAll(true);
				}
			});
			Menu menu = contextMenuMgr.createContextMenu(getControl());
			getControl().setMenu(menu);
			getSite().registerContextMenu(contextMenuMgr, this);
		}
	}

	protected void unhookContextMenu() {
		if (contextMenuMgr != null) {
			contextMenuMgr.removeAll();
			contextMenuMgr.dispose();
			contextMenuMgr = null;
		}
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

	protected void contributeToActionBars() {
		IViewSite viewSite = getViewSite();
		IActionBars bars = viewSite.getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
		IWorkbenchOperationSupport operationSupport = PlatformUI.getWorkbench().getOperationSupport();
		undoContext = operationSupport.getUndoContext();
		UndoRedoActionGroup undoRedoGroup = new UndoRedoActionGroup(viewSite, undoContext, true);
		undoRedoGroup.fillActionBars(bars);
		IWorkbenchWindow workbenchWindow = viewSite.getWorkbenchWindow();
		bars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
		String pasteId = ActionFactory.PASTE.getId();
		bars.setGlobalActionHandler(pasteId, pasteAction);
		bars.setGlobalActionHandler(ActionFactory.PRINT.getId(), new PrintAction(workbenchWindow));
		if (selectAllAction != null)
			bars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), selectAllAction);
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
		Clipboard clipboard = UiActivator.getDefault().getClipboard(getSite().getShell().getDisplay());
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
								// OpenCatAction openCatAction = new
								// OpenCatAction();
								// openCatAction.setFile(catFile);
								// openCatAction.init(getSite().getWorkbenchWindow());
								// openCatAction.run(this);
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
		super.registerCommands();
	}

	protected abstract void selectAll();

	protected abstract void selectNone();

	@Override
	public void updateActions() {
		int count = getSelectionCount(false);
		int localCount = getSelectionCount(true);
		if (viewActive) {
			boolean writable = !dbIsReadonly();
			boolean one = count == 1;
			boolean localOne = localCount == 1;
			boolean selected = count > 0;
			boolean localSelected = localCount > 0;
			boolean multi = localSelected && writable;
			proximitySearchAction.setEnabled(selected && hasGps());
			deleteAction.setEnabled(multi);
			refreshAction.setEnabled(multi || collectionSelected());
			showInFolderAction.setEnabled(localOne);
			editAction.setEnabled(localSelected);
			editWithAction.setEnabled(localSelected);
			rotateLeftAction.setEnabled(localSelected && writable);
			rotateRightAction.setEnabled(localSelected && writable);
			keyDefAction.setEnabled(localSelected && writable);
			timeShiftAction.setEnabled(localSelected && writable);
			ratingAction.setEnabled(localSelected && writable);
			colorCodeAction.setEnabled(localSelected && writable);
			playVoiceNoteAction.setEnabled(one && hasVoiceNote(getAssetSelection()));
			addVoiceNoteAction.setEnabled(localOne && writable);
			searchSimilarAction.setEnabled(count <= 1);
			timeSearchAction.setEnabled(true);
			viewImageAction.setEnabled(one);
			showComponentsAction.setEnabled(localOne);
			showCompositesAction.setEnabled(localOne);
			showDerivativesAction.setEnabled(localOne);
			showOriginalAction.setEnabled(localOne);
			copyMetadataAction.setEnabled(one);
			pasteMetadataAction.setEnabled(multi && testClipboardForMetadata());
			addToAlbumAction.setEnabled(localSelected);
			categorizeAction.setEnabled(localSelected);
			moveAction.setEnabled(selected);
			slideshowAction.setEnabled(true);
			showInTimeLineAction.setEnabled(localOne && hasTimeLine());
			copyAction.setEnabled(localSelected);
			pasteAction.setEnabled(writable && testClipboardForImages());
			addBookmarkAction.setEnabled(one && writable);
		}
		updateActions(count, localCount);
	}

	private boolean collectionSelected() {
		CatalogView catView = (CatalogView) getSite().getPage().findView(CatalogView.ID);
		if (catView != null) {
			Iterator<?> iterator = ((IStructuredSelection) catView.getSelection()).iterator();
			while (iterator.hasNext()) {
				if (iterator.next() instanceof SmartCollection)
					return true;
			}
		}
		return false;
	}

	private boolean testClipboardForMetadata() {
		Clipboard clipboard = UiActivator.getDefault().getClipboard(getSite().getShell().getDisplay());
		Object contents = clipboard.getContents(TextTransfer.getInstance());
		if (contents instanceof String) {
			String text = (String) contents;
			if (text.indexOf("xmpmeta") >= 0 //$NON-NLS-1$
					&& text.indexOf("adobe:ns:meta") >= 0) //$NON-NLS-1$
				return true;
		}
		return false;
	}

	private boolean testClipboardForImages() {
		Object contents = UiActivator.getDefault().getClipboard(getSite().getShell().getDisplay())
				.getContents(FileTransfer.getInstance());
		return (contents instanceof String[] && ((String[]) contents).length > 0);
	}

	private static final int THRESHHOLD = 3;
	private boolean dragging;
	// private PasteAction pasteHandler;
	private int ignoreTab;

	public boolean hasGps() {
		int n = 0;
		for (Asset asset : getAssetSelection())
			if (++n > THRESHHOLD || (!Double.isNaN(asset.getGPSLatitude()) && !Double.isNaN(asset.getGPSLongitude())))
				return true;
		return false;
	}

	@Override
	protected void installListeners(Composite parent) {
		super.installListeners(parent);
		Core.getCore().addCatalogListener(this);
	}

	@Override
	public IGalleryHover getGalleryHover(MouseEvent event) {
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
	public void keyReleased(KeyEvent e) {
		super.keyReleased(e);
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
			if ((e.stateMask & SWT.SHIFT) != 0)
				editWithAction.run();
			else
				editAction.run();
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
			// break;
		}
	}

	protected void fillEditAndSearchGroup(IMenuManager manager, boolean readOnly) {
		manager.add(viewImageAction);
		manager.add(editAction);
		manager.add(editWithAction);
		manager.add(new Separator(IZoomActionConstants.MB_EDIT));
		manager.add(searchSimilarAction);
		manager.add(timeSearchAction);
		manager.add(proximitySearchAction);
		manager.add(new Separator(IZoomActionConstants.MB_SEARCH));
	}

	protected void fillMetaData(IMenuManager manager, boolean readOnly) {
		if (!readOnly) {
			manager.add(copyMetadataAction);
			manager.add(pasteMetadataAction);
		} else
			manager.add(copyMetadataAction);
		manager.add(new Separator(IZoomActionConstants.MB_META));

	}

	protected void fillVoiceNote(IMenuManager manager, boolean readOnly) {
		if (!readOnly) {
			manager.add(addVoiceNoteAction);
			manager.add(playVoiceNoteAction);
		} else
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

	protected void addExplanationListener() {
		Control control = getControl();
		if (control != null && !control.isDisposed()) {
			control.addKeyListener(new KeyListener() {

				public void keyPressed(KeyEvent e) {
					if (!getSelection().isEmpty()
							&& (e.keyCode == SWT.SHIFT || e.keyCode == SWT.CTRL || e.keyCode == SWT.ALT))
						computeMode(e.stateMask | e.keyCode);
				}

				private void computeMode(int state) {
					if (getSelectionCount(false) == 1) {
						String message = Messages.getString("ImageView.internal_viewer_selected"); //$NON-NLS-1$
						if ((state & SWT.CTRL) != 0) {
							if ((state & SWT.SHIFT) != 0)
								message += Messages.getString("ImageView.raw_original_mode"); //$NON-NLS-1$
							else
								message += Messages.getString("ImageView.crop_mode"); //$NON-NLS-1$
						}
						message += (((state & SWT.ALT) != 0) != (getBWmode() != null))
								? Messages.getString("ImageView.bw_mode") //$NON-NLS-1$
								: Messages.getString("ImageView.color_mode"); //$NON-NLS-1$
						setStatusMessage(message, false);
					}
				}

				public void keyReleased(KeyEvent e) {
					if (e.keyCode == SWT.SHIFT || e.keyCode == SWT.CTRL || e.keyCode == SWT.ALT) {
						int state = e.stateMask - e.keyCode;
						if ((state & (SWT.SHIFT | SWT.CTRL | SWT.ALT)) == 0)
							updateStatusLine();
						else
							computeMode(state);
					}
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

	/*
	 * (nicht-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.internal.views.IDropHost#getSelectedCollection()
	 */
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