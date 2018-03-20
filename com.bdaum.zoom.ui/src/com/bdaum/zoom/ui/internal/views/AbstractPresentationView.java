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
 * (c) 2009-2011 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.views;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.operations.UndoRedoActionGroup;
import org.piccolo2d.PCamera;
import org.piccolo2d.PNode;
import org.piccolo2d.event.PDragSequenceEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PInputEventFilter;
import org.piccolo2d.event.PInputEventListener;
import org.piccolo2d.extras.swt.PSWTCanvas;
import org.piccolo2d.extras.swt.PSWTPath;
import org.piccolo2d.extras.swt.PSWTText;
import org.piccolo2d.util.PBounds;
import org.piccolo2d.util.PDimension;
import org.piccolo2d.util.PPaintContext;

import com.bdaum.aoModeling.runtime.IdentifiableObject;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.CatalogAdapter;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.operations.UpdateTextIndexOperation;
import com.bdaum.zoom.css.internal.IExtendedColorModel2;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.IZoomCommandIds;
import com.bdaum.zoom.ui.internal.IPresentationHandler;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.actions.ZoomActionFactory;
import com.bdaum.zoom.ui.internal.hover.HoverInfo;
import com.bdaum.zoom.ui.internal.hover.IGalleryHover;
import com.bdaum.zoom.ui.internal.hover.IHoverInfo;
import com.bdaum.zoom.ui.internal.widgets.AbstractHandle;
import com.bdaum.zoom.ui.internal.widgets.GalleryPanEventHandler;
import com.bdaum.zoom.ui.internal.widgets.GalleryZoomEventHandler;
import com.bdaum.zoom.ui.internal.widgets.GreekedPSWTText;
import com.bdaum.zoom.ui.internal.widgets.InertiaMouseWheelListener;
import com.bdaum.zoom.ui.internal.widgets.PPanel;
import com.bdaum.zoom.ui.internal.widgets.PSWTAssetThumbnail;
import com.bdaum.zoom.ui.internal.widgets.PSWTButton;
import com.bdaum.zoom.ui.internal.widgets.TextEventHandler;
import com.bdaum.zoom.ui.internal.widgets.TextField;
import com.bdaum.zoom.ui.internal.widgets.ZPSWTCanvas;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public abstract class AbstractPresentationView extends BasicView
		implements IExtendedColorModel2, IPresentationHandler, UiConstants, IOperationHistoryListener {

	private static final int PROGRESS_THICKNESS = 5;
	private static final int FORCE_PAN_MASK = InputEvent.SHIFT_MASK | InputEvent.ALT_MASK | InputEvent.CTRL_MASK
			| InputEvent.BUTTON3_MASK;

	protected final class PPresentationPanel extends PSWTPath {

		private static final long serialVersionUID = -4203919566444641490L;

		public PPresentationPanel(float x, float y, float w, float h, Color color, Color strokeColor,
				PInputEventListener inputEventHandler) {
			setPathToRectangle(x, y, w, h);
			setPaint(color);
			if (strokeColor != null)
				setStrokeColor(strokeColor);
			setPickable(true);
			if (inputEventHandler != null)
				addInputEventListener(inputEventHandler);
		}
	}

	protected static abstract class AbstractHandleDragSequenceEventHandler extends PDragSequenceEventHandler {
		protected Point2D oldOffset;
		private final PNode node;
		private final PNode presentationObject;
		private Point2D oldPos;
		protected final boolean relative;
		private final boolean restricted;

		public AbstractHandleDragSequenceEventHandler(PNode node, boolean relative, boolean restricted,
				PNode presentationObject) {
			this.node = node;
			this.relative = relative;
			this.restricted = restricted;
			this.presentationObject = presentationObject;
		}

		@Override
		public boolean acceptsEvent(PInputEvent event, int type) {
			PNode pickedNode2 = event.getPickedNode();
			if (pickedNode2 instanceof PSWTText)
				return false;
			return super.acceptsEvent(event, type);
		}

		@Override
		protected void startDrag(PInputEvent event) {
			super.startDrag(event);
			moveToFront(presentationObject);
			oldOffset = relative ? node.getOffset() : presentationObject.getOffset();
			oldPos = event.getCanvasPosition();
		}

		protected void moveToFront(PNode object) {
			presentationObject.raiseToTop();
			object.raiseToTop();
		}

		@Override
		protected void drag(PInputEvent event) {
			super.drag(event);
			PDimension aDelta = event.getDeltaRelativeTo(presentationObject);
			if (aDelta.getWidth() != 0 || aDelta.getHeight() != 0) {
				if (relative)
					node.offset(aDelta.getWidth(), aDelta.getHeight());
				else
					presentationObject.offset(aDelta.getWidth(), aDelta.getHeight());
			}
		}

		@Override
		protected void endDrag(PInputEvent event) {
			super.endDrag(event);
			Point2D pos = event.getCanvasPosition();
			org.eclipse.swt.graphics.Point dragTolerance = getDragTolerance();
			boolean doDrop = Math.abs(pos.getX() - oldPos.getX()) > dragTolerance.x
					|| Math.abs(pos.getY() - oldPos.getY()) > dragTolerance.y;
			if (relative && restricted) {
				PBounds globalBounds = node.getGlobalBounds();
				ListIterator<?> childrenIterator = presentationObject.getChildrenIterator();
				while (childrenIterator.hasNext()) {
					PNode child = (PNode) childrenIterator.next();
					if (child != node && globalBounds.intersects(child.getGlobalBounds())) {
						doDrop = false;
						break;
					}
				}
			}
			if (doDrop)
				drop(event, node);
			else if (relative)
				node.setOffset(oldOffset);
			else
				presentationObject.setOffset(oldOffset);
		}

		protected abstract void drop(PInputEvent event, PNode nod);

		public abstract org.eclipse.swt.graphics.Point getDragTolerance();
	}

	public static class GalleryHover implements IGalleryHover {

		public IHoverInfo getHoverInfo(IHoverSubject viewer, MouseEvent event) {
			Object object = viewer.findObject(event);
			if (object instanceof PSWTButton)
				return new HoverInfo(object, ((PSWTButton) object).getTooltip());
			if (object instanceof PSWTAssetThumbnail && ((PSWTAssetThumbnail) object).getAsset() != null)
				return new HoverInfo(object, ((PSWTAssetThumbnail) object).getAsset().getName());
			if (object instanceof AbstractHandle)
				return new HoverInfo(object, ((AbstractHandle) object).getTooltip());
			if (object instanceof GreekedPSWTText) {
				GreekedPSWTText field = (GreekedPSWTText) object;
				if (field.isGreek())
					return new HoverInfo(object, field.getText());
			}
			return null;
		}
	}

	protected PSWTCanvas canvas;
	protected java.awt.Rectangle surfaceBounds;
	protected PSWTPath surface;
	private PNode pickedNode;
	protected InertiaMouseWheelListener wheelListener;
	protected TextEventHandler textEventHandler;
	protected List<Image> images = new ArrayList<Image>(200);
	protected int lastMouseX;
	protected boolean mouseDown;
	protected int mouseButton;
	private String currentPresentation;
	private int currentSystemCursor = SWT.CURSOR_ARROW;
	private String currentCustomCursor;
	protected Color offlineColor;
	protected Color remoteColor;
	protected Color titleColor;
	protected Color selectedRemoteColor;
	protected Color foregroundColor;
	protected Color selectionForegroundColor;
	protected Color selectionBackgroundColor;
	protected Color backgroundColor;
	protected Color titleForegroundColor;
	protected Color titleBackgroundColor;
	protected IAction showInFolderAction;
	protected IAction showInTimeLineAction;
	protected IAction synchronizeAction;
	protected IAction gotoExhibitAction;
	protected IAction propertiesAction;
	protected Point2D positionRelativeToCamera;
	protected boolean synchronize;
	private IOperationHistory operationHistory;
	private GalleryPanEventHandler panEventHandler;
	private GalleryZoomEventHandler zoomEventHandler;
	protected double previousMagnification = 1d;
	protected AffineTransform oldTransform;
	protected IAction showInMapAction;
	protected IAction showFullscreenAction;
	private IAction editAction;
	private IAction editWithAction;
	private IAction rotateLeftAction;
	private IAction rotateRightAction;
	private IAction voiceNoteAction;
	private Cursor cue;
	protected Action exhibitPropertiesAction;

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginLeft = 0;
		composite.setLayout(layout);
		canvas = new ZPSWTCanvas(composite, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		setCanvasCursor(null, SWT.CURSOR_APPSTARTING);
		surfaceBounds = computeSurfaceSize(canvas.getDisplay().getPrimaryMonitor().getBounds());
		surface = PSWTPath.createRectangle(surfaceBounds.x, surfaceBounds.y, surfaceBounds.width, surfaceBounds.height);
		canvas.getLayer().addChild(surface);
		canvas.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				mouseDown = true;
				mouseButton = e.button;
				mouseX = lastMouseX = e.x;
				mouseY = e.y;
				setCursorForObject(e, CURSOR_GRABBING, CURSOR_MPLUS, CURSOR_MPLUS);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				mouseX = e.x;
				mouseY = e.y;
				mouseDown = false;
				setCursorForObject(e, CURSOR_OPEN_HAND, CURSOR_OPEN_HAND, CURSOR_OPEN_HAND);
			}
		});
		canvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				mouseX = e.x;
				mouseY = e.y;
				if (mouseDown) {
					e.button = mouseButton;
					setCursorForObject(e, mouseButton > 1 ? CURSOR_OPEN_HAND : CURSOR_GRABBING, CURSOR_MPLUS,
							CURSOR_MMINUS);
				} else
					setCursorForObject(e, CURSOR_OPEN_HAND, CURSOR_OPEN_HAND, CURSOR_OPEN_HAND);
			}
		});
		progressBar = new ProgressIndicator(composite, SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		data.heightHint = PROGRESS_THICKNESS;
		progressBar.setLayoutData(data);
		addKeyListener();
		getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getOperationHistory()
				.addOperationHistoryListener(new IOperationHistoryListener() {
					public void historyNotification(OperationHistoryEvent event) {
						refresh();
					}
				});
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		showFullscreenAction = addAction(ZoomActionFactory.VIEWIMAGE.create(null, this));
		editAction = addAction(ZoomActionFactory.EDIT.create(null, this));
		editWithAction = addAction(ZoomActionFactory.EDITWITH.create(null, this));
		rotateLeftAction = addAction(ZoomActionFactory.ROTATELEFT.create(null, this));
		rotateRightAction = addAction(ZoomActionFactory.ROTATERIGHT.create(null, this));
		showInFolderAction = addAction(ZoomActionFactory.SHOWINFOLDER.create(null, this));
		showInTimeLineAction = addAction(ZoomActionFactory.SHOWINTIMELINE.create(null, this));
		showInMapAction = addAction(ZoomActionFactory.SHOWINMAP.create(null, this));
		voiceNoteAction = addAction(ZoomActionFactory.PLAYVOICENOTE.create(null, this));
		synchronizeAction = new Action(Messages.getString("AbstractPresentationView.synchronize"), //$NON-NLS-1$
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				synchronize = !synchronize;
				synchronize();
			}
		};
		synchronizeAction.setToolTipText(Messages.getString("AbstractPresentationView.synchronize_tooltip")); //$NON-NLS-1$
		synchronizeAction.setImageDescriptor(Icons.sync.getDescriptor());
	}

	protected void synchronize() {
		if (synchronize && pickedNode != null) {
			PNode node = pickedNode.getParent();
			while (node != null) {
				if (node instanceof IPresentationItem) {
					getNavigationHistory().postSelection((AssetSelection) getAdapter(AssetSelection.class));
					break;
				}
				node = node.getParent();
			}
		}
	}

	private void setCursorForObject(MouseEvent e, String surface, String altLeft, String altRight) {
		boolean zooming = false;
		boolean panning = false;
		int zoomKey = Platform.getPreferencesService().getInt(UiActivator.PLUGIN_ID, PreferenceConstants.ZOOMKEY,
				PreferenceConstants.ZOOMALT, null);
		if (e.button >= 2) {
			zooming = zoomKey == PreferenceConstants.ZOOMRIGHT;
			panning = !zooming;
		} else if (e.button == 1) {
			switch (zoomKey) {
			case PreferenceConstants.ZOOMALT:
				zooming = ((e.stateMask & SWT.ALT) != 0);
				panning = ((e.stateMask & SWT.SHIFT) != 0);
				break;
			case PreferenceConstants.ZOOMSHIFT:
				zooming = ((e.stateMask & SWT.SHIFT) != 0);
				panning = ((e.stateMask & SWT.ALT) != 0);
				break;
			case PreferenceConstants.ZOOMRIGHT:
			case PreferenceConstants.NOZOOM:
				panning = ((e.stateMask & (SWT.ALT | SWT.SHIFT)) != 0);
				break;
			}
		}
		if (zooming) {
			if (Math.abs(lastMouseX - e.x) > 3)
				unhookContextMenu();
			setCanvasCursor(e.x <= lastMouseX ? altRight : altLeft, -1);
			lastMouseX = e.x;
			return;
		}
		if (panning) {
			setCanvasCursor(surface, -1);
			return;
		}
		setObjectCursor(findObject(e), e.button, surface);
		hookContextMenu();
	}

	private void setObjectCursor(PNode obj, int button, String surface) {
		if (obj instanceof PSWTButton)
			setCanvasCursor(null, SWT.CURSOR_HAND);
		else if (obj instanceof PSWTText)
			setCanvasCursor(null, obj.getParent() instanceof TextField ? SWT.CURSOR_IBEAM : SWT.CURSOR_ARROW);
		else if (obj instanceof AbstractHandle)
			setCanvasCursor(null, ((AbstractHandle) obj).getCursorType());
		else if (obj instanceof PSWTAssetThumbnail || obj instanceof PPresentationPanel)
			setCanvasCursor(null, SWT.CURSOR_SIZEALL);
		else
			setCanvasCursor(surface, -1);
	}

	protected void setCanvasCursor(String customCursor, int systemCursor) {
		if (cue != null) {
			canvas.setCursor(cue);
			currentSystemCursor = -1;
			currentCustomCursor = null;
			return;
		}
		if (customCursor == null) {
			if (currentSystemCursor != systemCursor || currentCustomCursor != null) {
				canvas.setCursor(canvas.getDisplay().getSystemCursor(systemCursor));
				currentSystemCursor = systemCursor;
				currentCustomCursor = null;
			}
		} else if (currentCustomCursor != customCursor || currentSystemCursor >= 0) {
			canvas.setCursor(UiActivator.getDefault().getCursor(canvas.getDisplay(), customCursor));
			currentSystemCursor = -1;
			currentCustomCursor = customCursor;
		}
	}

	@Override
	public PNode findObject(MouseEvent event) {
		return findObject(event.x, event.y);
	}

	private PNode findObject(int x, int y) {
		Point2D viewDimension = new Point(x, y);
		canvas.getCamera().localToView(viewDimension);
		ArrayList<PNode> results = new ArrayList<PNode>();
		surface.findIntersectingNodes(
				new java.awt.Rectangle((int) viewDimension.getX(), (int) viewDimension.getY(), 1, 1), results);
		for (PNode object : results)
			if (object instanceof PSWTText || object instanceof PSWTButton || object instanceof AbstractHandle)
				return object;
		for (PNode object : results)
			if (object instanceof PSWTAssetThumbnail)
				return object;
		for (PNode object : results)
			if (object instanceof PPresentationPanel)
				return object;
		for (PNode object : results)
			if (object instanceof PPanel)
				return object;
		return null;
	}

	@Override
	public IGalleryHover getGalleryHover(MouseEvent event) {
		return new GalleryHover();
	}

	@Override
	public void themeChanged() {
		setColor(canvas);
	}

	protected java.awt.Rectangle computeSurfaceSize(Rectangle bounds) {
		return new java.awt.Rectangle(-bounds.width * 10, -bounds.height * 10, bounds.width * 20, bounds.height * 20);
	}

	protected void addCatalogListener(CatalogListener l) {
		Core.getCore().addCatalogListener(l);
	}

	@Override
	public boolean assetsChanged() {
		return false;
	}

	@Override
	public boolean collectionChanged() {
		return false;
	}

	@Override
	public boolean selectionChanged() {
		return false;
	}

	public void setOfflineColor(org.eclipse.swt.graphics.Color c) {
		offlineColor = UiUtilities.getAwtForeground(getControl(), c);
	}

	public void setTitleColor(org.eclipse.swt.graphics.Color c) {
		titleColor = UiUtilities.getAwtForeground(getControl(), c);
	}

	public void setSelectedOfflineColor(org.eclipse.swt.graphics.Color selectedOfflineColor) {
		// do nothing
	}

	public void setRemoteColor(org.eclipse.swt.graphics.Color c) {
		remoteColor = UiUtilities.getAwtForeground(getControl(), c);
	}

	public void setSelectedRemoteColor(org.eclipse.swt.graphics.Color c) {
		selectedRemoteColor = UiUtilities.getAwtForeground(getControl(), c);
	}

	public void setForegroundColor(org.eclipse.swt.graphics.Color c) {
		foregroundColor = UiUtilities.getAwtForeground(getControl(), c);
	}

	public void setSelectionForegroundColor(org.eclipse.swt.graphics.Color c) {
		selectionForegroundColor = UiUtilities.getAwtForeground(getControl(), c);
	}

	public void setSelectionBackgroundColor(org.eclipse.swt.graphics.Color c) {
		selectionBackgroundColor = UiUtilities.getAwtBackground(getControl(), c);
	}

	public void setBackgroundColor(org.eclipse.swt.graphics.Color c) {
		backgroundColor = UiUtilities.getAwtBackground(getControl(), c);
	}

	public void setTitleForeground(org.eclipse.swt.graphics.Color c) {
		titleForegroundColor = UiUtilities.getAwtForeground(getControl(), c);
	}

	public void setTitleBackground(org.eclipse.swt.graphics.Color c) {
		titleBackgroundColor = UiUtilities.getAwtBackground(getControl(), c);
	}

	public boolean applyColorsTo(Object element) {
		return element instanceof PSWTCanvas;
	}

	public ISelection getSelection() {
		return StructuredSelection.EMPTY;
	}

	public void setSelection(ISelection sel) {
		// do nothing
	}

	public void setInput(IdentifiableObject obj) {
		if (obj != null) {
			cleanUp();
			currentPresentation = obj.getStringId();
		}
		if (currentSystemCursor == SWT.CURSOR_APPSTARTING)
			setCanvasCursor(null, SWT.CURSOR_ARROW);
	}

	public Control getControl() {
		return canvas;
	}

	@Override
	public void setFocus() {
		canvas.setFocus();
		if (currentSystemCursor == SWT.CURSOR_APPSTARTING)
			setCanvasCursor(null, SWT.CURSOR_ARROW);
	}

	protected void hookContextMenu() {
		if (menuMgr == null) {
			menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
			menuMgr.setRemoveAllWhenShown(true);
			menuMgr.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					AbstractPresentationView.this.fillContextMenu(manager);
				}
			});
			canvas.setMenu(menuMgr.createContextMenu(canvas));
			getSite().registerContextMenu(menuMgr, this);
		}
	}

	protected void unhookContextMenu() {
		if (menuMgr != null) {
			menuMgr.removeAll();
			menuMgr.dispose();
			menuMgr = null;
		}
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void addWheelListener(double min, double max) {
		wheelListener = new InertiaMouseWheelListener();
		wheelListener.setMinScale(min);
		wheelListener.setMaxScale(max);
		canvas.addMouseWheelListener(wheelListener);
	}

	private CatalogAdapter catListener = new CatalogAdapter() {

		@Override
		public void assetsModified(final BagChange<Asset> changes, QueryField node) {
			if (node == null) {
				Shell shell = getSite().getShell();
				if (!shell.isDisposed())
					shell.getDisplay().asyncExec(() -> {
						if (!shell.isDisposed()) {
							if (changes != null)
								updatePresentation(changes.getChanged());
							else
								refresh();
						}
					});
			}
		}

		@Override
		public void catalogClosed(int mode) {
			if (mode != CatalogListener.EMERGENCY && mode != CatalogListener.SHUTDOWN)
				setInput(null);
		}
	};
	private int mouseY;
	private int mouseX;
	protected ProgressIndicator progressBar;
	private MenuManager menuMgr;

	protected abstract void updatePresentation(Collection<? extends Asset> assets);

	protected void addCatalogListener() {
		addCatalogListener(catListener);
	}

	protected abstract void setColor(Control canvas);

	protected void show() {
		refresh();
		launchDecorator();
	}

	@Override
	public void refresh() {
		if (canvas != null && !canvas.isDisposed())
			canvas.getDisplay().asyncExec(() -> {
				if (canvas != null && !canvas.isDisposed())
					canvas.redraw();
			});
	}

	@Override
	public void dispose() {
		Core.getCore().removeCatalogListener(catListener);
		if (wheelListener != null) {
			wheelListener.dispose();
			wheelListener = null;
		}
		if (panEventHandler != null) {
			panEventHandler.dispose();
			panEventHandler = null;
		}
		if (zoomEventHandler != null) {
			zoomEventHandler.dispose();
			zoomEventHandler = null;
		}
		cleanUp();
		super.dispose();
	}

	protected void cleanUp() {
		currentPresentation = null;
		for (Image image : images)
			image.dispose();
		images.clear();
	}

	protected void contributeToActionBars() {
		IViewSite viewSite = getViewSite();
		IActionBars bars = viewSite.getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
		new UndoRedoActionGroup(viewSite, undoContext, true).fillActionBars(bars);
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(propertiesAction);
		manager.add(synchronizeAction);
		manager.add(new Separator());
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(propertiesAction);
	}

	protected void setPanAndZoomHandlers(double panSpeedOffset, double zoomSpeedOffset) {
		PNode[] workarea = getWorkArea();
		canvas.removeInputEventListener(canvas.getPanEventHandler());
		int horiztontalMargins = getHoriztontalMargins();
		panEventHandler = new GalleryPanEventHandler(this, workarea, -horiztontalMargins, surfaceBounds.y,
				surfaceBounds.width - horiztontalMargins, surfaceBounds.height + surfaceBounds.y, getPanDirection(),
				FORCE_PAN_MASK, panSpeedOffset);
		canvas.addInputEventListener(panEventHandler);
		canvas.removeInputEventListener(canvas.getZoomEventHandler());
		zoomEventHandler = new GalleryZoomEventHandler(this, workarea, zoomSpeedOffset);
		zoomEventHandler.setMinScale(wheelListener.getMinScale());
		zoomEventHandler.setMaxScale(wheelListener.getMaxScale());
		canvas.addInputEventListener(zoomEventHandler);
	}

	protected void pan(PInputEvent event) {
		PCamera camera = canvas.getCamera();
		PBounds viewBounds = camera.getViewBounds();
		int f = event.isControlDown() ? 5 : 1;
		char keyChar = event.getKeyChar();
		if (keyChar >= ' ')
			handleKeyChar(camera, keyChar, f);
		else {
			int keyCode = event.getKeyCode();
			switch (keyCode) {
			case SWT.ARROW_RIGHT:
				pan(camera, -30 * f, 0);
				break;
			case SWT.ARROW_LEFT:
				pan(camera, 30 * f, 0);
				break;
			case SWT.ARROW_UP:
				pan(camera, 0, 30 * f);
				break;
			case SWT.ARROW_DOWN:
				pan(camera, 0, -30 * f);
				break;
			case SWT.END:
				pan(camera, (int) (-viewBounds.getWidth() / 2) * f, 0);
				break;
			case SWT.HOME:
				pan(camera, (int) (viewBounds.getWidth() / 2) * f, 0);
				break;
			case SWT.PAGE_UP:
				pan(camera, 0, (int) (viewBounds.getHeight() / 2) * f);
				break;
			case SWT.PAGE_DOWN:
				pan(camera, 0, (int) (-viewBounds.getHeight() / 2) * f);
				break;
			default:
				handleKeyChar(camera, (char) (keyCode & 0xff), 5);
				break;
			}
		}
	}

	protected void handleKeyChar(PCamera camera, char keyChar, int f) {
		switch (keyChar) {
		case '+':
			zoom(canvas, 1d + 0.05d * f);
			break;
		case '-':
			zoom(canvas, 1d - 0.05d * f);
			break;
		case '*':
			camera.setViewTransform(new AffineTransform());
			break;
		}
	}

	private void zoom(final PSWTCanvas pcanvas, double scaleDelta) {
		PCamera camera = pcanvas.getCamera();
		double newScale = scaleDelta * camera.getViewScale();
		if (newScale >= wheelListener.getMinScale() && newScale <= wheelListener.getMaxScale()) {
			camera.scaleView(scaleDelta);
			resetTransform();
		}
	}

	private void pan(PCamera camera, double deltaX, double deltaY) {
		PBounds viewBounds = camera.getViewBounds();
		double x = viewBounds.getX() - deltaX;
		PBounds clientArea = getClientAreaReference();
		if (x < clientArea.x && deltaX > 0)
			deltaX = Math.max(0, viewBounds.x - clientArea.x);
		if (x + viewBounds.getWidth() > clientArea.x + clientArea.width && deltaX < 0)
			deltaX = Math.min(0, viewBounds.x + viewBounds.width - (clientArea.x + clientArea.width));
		double y = viewBounds.getY() - deltaY;
		if (y < clientArea.y && deltaY > 0)
			deltaY = Math.max(0, viewBounds.y - clientArea.y);
		if (y + viewBounds.getHeight() > surfaceBounds.y + surfaceBounds.height && deltaY < 0)
			deltaY = Math.min(0, viewBounds.y + viewBounds.height - (clientArea.y + clientArea.height));
		camera.translateView(deltaX, deltaY);
		resetTransform();
	}

	protected abstract PBounds getClientAreaReference();

	protected abstract int getHoriztontalMargins();

	protected abstract int getPanDirection();

	protected abstract PNode[] getWorkArea();

	protected void addDropListener(final Control control) {
		final int ops = DND.DROP_MOVE | DND.DROP_COPY;
		final DropTarget target = new DropTarget(control, ops);
		final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();
		target.setTransfer(new Transfer[] { transfer });
		target.addDropListener(new EffectDropTargetListener(control) {

			@Override
			public void dragEnter(DropTargetEvent event) {
				int detail = event.detail;
				event.detail = DND.DROP_NONE;
				if (!dbIsReadonly())
					for (int i = 0; i < event.dataTypes.length; i++)
						if (transfer.isSupportedType(event.dataTypes[i])) {
							event.currentDataType = event.dataTypes[i];
							event.detail = (detail & ops) == 0 ? DND.DROP_NONE : DND.DROP_COPY;
							break;
						}
				super.dragEnter(event);
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				super.dragOver(event);
				if ((event.detail & DND.DROP_MOVE) != 0) {
					org.eclipse.swt.graphics.Point pc = canvas.toControl(event.x, event.y);
					if (findExhibit(pc, toLocal(pc)) == null)
						event.detail = DND.DROP_COPY;
				}
			}

			public void dragOperationChanged(DropTargetEvent event) {
				int detail = event.detail;
				event.detail = DND.DROP_NONE;
				if (transfer.isSupportedType(event.currentDataType) && !dbIsReadonly())
					event.detail = (detail & ops) == 0 ? DND.DROP_NONE
							: (detail & DND.DROP_COPY) == 0 ? DND.DROP_COPY : DND.DROP_MOVE;
			}

			public void drop(DropTargetEvent event) {
				if (transfer.isSupportedType(event.currentDataType) && !dbIsReadonly()) {
					ISelection selection = transfer.getSelection();
					if (selection instanceof AssetSelection) {
						org.eclipse.swt.graphics.Point pc = canvas.toControl(event.x, event.y);
						dropAssets(selection, pc, toLocal(pc), (event.detail & DND.DROP_MOVE) != 0);
					}
				}
			}

			private Point2D toLocal(org.eclipse.swt.graphics.Point pc) {
				PCamera camera = canvas.getCamera();
				Point2D globalToLocal = new Point(pc.x, pc.y);
				camera.globalToLocal(globalToLocal);
				camera.localToView(globalToLocal);
				surface.parentToLocal(globalToLocal);
				return globalToLocal;
			}
		});
	}

	protected abstract PNode findExhibit(org.eclipse.swt.graphics.Point point, Point2D globalToLocal);

	protected abstract void dropAssets(ISelection selection, org.eclipse.swt.graphics.Point point,
			Point2D globalToLocal, boolean replace);

	public IStatus performOperation(IUndoableOperation op) {
		if (operationHistory == null) {
			operationHistory = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
			operationHistory.addOperationHistoryListener(this);
		}
		op.addContext(undoContext);
		try {
			return operationHistory.execute(op, null, this);
		} catch (ExecutionException e) {
			return new Status(IStatus.ERROR, UiActivator.PLUGIN_ID,
					NLS.bind(Messages.getString("AbstractPresentationView.cannot_execute_operation"), op //$NON-NLS-1$
							.getLabel()),
					e);
		}
	}

	public void storeSafelyAndUpdateIndex(Object toBeDeleted, final Object toBeStored, Object toBeIndexed) {
		final IDbManager dbManager = Core.getCore().getDbManager();
		dbManager.safeTransaction(toBeDeleted, toBeStored);
		if (!dbManager.getMeta(true).getNoIndex()) {
			if (toBeIndexed instanceof String)
				OperationJob.executeOperation(new UpdateTextIndexOperation(new String[] { (String) toBeIndexed }),
						this);
			else if (toBeIndexed instanceof Collection<?>) {
				@SuppressWarnings("unchecked")
				Collection<String> assetIds = (Collection<String>) toBeIndexed;
				if (!assetIds.isEmpty())
					OperationJob.executeOperation(
							new UpdateTextIndexOperation(assetIds.toArray(new String[assetIds.size()])), this);
			}
		}
	}

	protected void updateSurfaceBounds(double w, double h) {
		boolean changed = false;
		PBounds bounds = surface.getBoundsReference();
		double x = bounds.getX();
		if (w > bounds.getWidth() + x) {
			surface.setWidth(w - x);
			surfaceBounds.width = (int) (w - x);
			changed = true;
		}
		double y = bounds.getY();
		if (h > bounds.getHeight() - y) {
			surface.setHeight(h - y);
			surfaceBounds.height = (int) (h - y);
			changed = true;
		}
		if (changed) {
			int horiztontalMargins = getHoriztontalMargins();
			panEventHandler.setSurfaceBounds(-horiztontalMargins, surfaceBounds.y,
					surfaceBounds.width - horiztontalMargins, surfaceBounds.height + surfaceBounds.y);
		}
	}

	public String getCurrentPresentation() {
		return currentPresentation;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPresentationItem.class) {
			PNode par = pickedNode;
			while (par != null && !(par instanceof IPresentationItem))
				par = par.getParent();
			return par;
		} else if (adapter == AssetSelection.class) {
			if (pickedNode != null) {
				IPresentationItem par = (IPresentationItem) getAdapter(IPresentationItem.class);
				if (par != null) {
					String assetId = par.getAssetId();
					if (assetId != null) {
						AssetImpl asset = Core.getCore().getDbManager().obtainAsset(assetId);
						if (asset != null)
							return new AssetSelection(asset);
					}
				}
			}
			return AssetSelection.EMPTY;
		}
		return super.getAdapter(adapter);
	}

	protected PNode getPickedNode() {
		return pickedNode;
	}

	protected void setPickedNode(PNode pickedNode) {
		this.pickedNode = pickedNode;
		synchronize();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.keyCode & SWT.MODIFIER_MASK;
		if (code != 0) {
			boolean zooming = false, panning = false;
			int zoomKey = Platform.getPreferencesService().getInt(UiActivator.PLUGIN_ID, PreferenceConstants.ZOOMKEY,
					PreferenceConstants.ZOOMALT, null);
			switch (zoomKey) {
			case PreferenceConstants.ZOOMALT:
				zooming = code == SWT.ALT;
				panning = code == SWT.SHIFT;
				break;
			case PreferenceConstants.ZOOMSHIFT:
				zooming = code == SWT.SHIFT;
				panning = code == SWT.ALT;
				break;
			case PreferenceConstants.ZOOMRIGHT:
			case PreferenceConstants.NOZOOM:
				panning = (code & (SWT.ALT | SWT.SHIFT)) != 0;
				break;
			}
			if (zooming) {
				setCanvasCursor(CURSOR_MPLUS, -1);
				return;
			}
			if (panning) {
				setCanvasCursor(CURSOR_GRABBING, -1);
				return;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if ((e.keyCode & (SWT.ALT | SWT.SHIFT)) != 0)
			setObjectCursor(findObject(mouseX, mouseY), 0, CURSOR_OPEN_HAND);
	}

	protected void installHandleEventHandlers(final PNode node, boolean relative, boolean restricted,
			final PNode presentationObject) {
		AbstractHandleDragSequenceEventHandler handleDragger = createDragSequenceEventHandler(node, relative,
				restricted, presentationObject);
		handleDragger.setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK, FORCE_PAN_MASK));
		handleDragger.getEventFilter().setMarksAcceptedEventsAsHandled(true);
		handleDragger.getEventFilter().setAcceptsMouseEntered(false);
		handleDragger.getEventFilter().setAcceptsMouseExited(false);
		handleDragger.getEventFilter().setAcceptsMouseMoved(false);
		node.addInputEventListener(handleDragger);
	}

	protected abstract AbstractHandleDragSequenceEventHandler createDragSequenceEventHandler(PNode node,
			boolean relative, boolean restricted, PNode presentationObject);

	public void resetTransform() {
		oldTransform = null;
	}

	protected void endTask() {
		((GridData) progressBar.getLayoutData()).heightHint = 1;
		progressBar.setVisible(false);
		try {
			progressBar.getParent().layout();
		} catch (Exception e) {
			// because of occasional piccolo crash
		}
	}

	protected void beginTask(int n) {
		((GridData) progressBar.getLayoutData()).heightHint = PROGRESS_THICKNESS;
		progressBar.setVisible(true);
		try {
			progressBar.getParent().layout();
		} catch (Exception e) {
			// because of occasional piccolo crash
		}
		progressBar.beginTask(n);
	}

	protected void addCommonContextActions(IMenuManager manager) {
		manager.add(new Separator());
		manager.add(showFullscreenAction);
		if (!dbIsReadonly()) {
			IMenuManager modifyMenu = new MenuManager(Messages.getString("AbstractPresentationView.modify"), "modify"); //$NON-NLS-1$ //$NON-NLS-2$
			manager.add(modifyMenu);
			modifyMenu.add(editAction);
			modifyMenu.add(editWithAction);
			modifyMenu.add(rotateLeftAction);
			modifyMenu.add(rotateRightAction);
			if (exhibitPropertiesAction != null) {
				modifyMenu.add(new Separator());
				modifyMenu.add(exhibitPropertiesAction);
			}
		}
		manager.add(new Separator());
		manager.add(showInFolderAction);
		AssetSelection assetSelection = (AssetSelection) getAdapter(AssetSelection.class);
		if (hasTimeLine())
			manager.add(showInTimeLineAction);
		if (showInMapAction != null && hasGeocode(assetSelection))
			manager.add(showInMapAction);
		if (hasVoiceNote(assetSelection))
			manager.add(voiceNoteAction);
	}

	@Override
	protected void registerCommands() {
		registerCommand(showInFolderAction, IZoomCommandIds.ShowInFolder);
		registerCommand(showInTimeLineAction, IZoomCommandIds.ShowInTimeline);
		registerCommand(showInMapAction, IZoomCommandIds.ShowInMap);
		registerCommand(showFullscreenAction, IZoomCommandIds.ViewImage);
		registerCommand(editAction, IZoomCommandIds.EditCommand);
		registerCommand(editWithAction, IZoomCommandIds.EditWithCommand);
		registerCommand(rotateLeftAction, IZoomCommandIds.RotateAntiClockwiseCommand);
		registerCommand(rotateRightAction, IZoomCommandIds.RotateClockwiseCommand);
		registerCommand(voiceNoteAction, IZoomCommandIds.PlayVoiceNote);
		super.registerCommands();
	}

	@Override
	public void updateActions(boolean force) {
		if (viewActive || force) {
			AssetSelection selection = (AssetSelection) getAdapter(AssetSelection.class);
			boolean writable = !dbIsReadonly();
			int count = selection.size();
			int localCount = selection.getLocalAssets().size();
			boolean one = count == 1;
			boolean localOne = localCount == 1;
			boolean localSelected = localCount > 0;
			editAction.setEnabled(localSelected);
			editWithAction.setEnabled(localSelected);
			showInFolderAction.setEnabled(localOne);
			showInTimeLineAction.setEnabled(localOne && hasTimeLine());
			if (exhibitPropertiesAction != null)
				exhibitPropertiesAction.setEnabled(one);
			voiceNoteAction.setEnabled(localOne && hasVoiceNote(selection));
			rotateLeftAction.setEnabled(localSelected && writable && isMedia(selection, QueryField.PHOTO, true));
			rotateRightAction.setEnabled(localSelected && writable && isMedia(selection, QueryField.PHOTO, true));
			showFullscreenAction.setEnabled(one);
			if (showInMapAction != null)
				showInMapAction.setEnabled(count > 0);
			super.updateActions(count, localCount);
		}
	}


	/**
	 * Set cue cursor
	 *
	 * @param new
	 *            Cue cursor
	 */
	protected void setCue(Cursor cue) {
		if (this.cue != null)
			this.cue.dispose();
		this.cue = cue;
	}

	public void historyNotification(OperationHistoryEvent event) {
		if (event.getEventType() == OperationHistoryEvent.UNDONE
				|| event.getEventType() == OperationHistoryEvent.REDONE)
			refreshAfterHistoryEvent(event.getOperation());
	}

	protected abstract void refreshAfterHistoryEvent(IUndoableOperation operation);

	public abstract String getId();

}