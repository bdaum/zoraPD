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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.bdaum.zoom.cat.model.Bookmark;
import com.bdaum.zoom.cat.model.BookmarkImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.VoiceNoteOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.GotoBookmarkAction;
import com.bdaum.zoom.ui.internal.actions.ZoomActionFactory;
import com.bdaum.zoom.ui.internal.hover.HoverManager;

@SuppressWarnings("restriction")
public class BookmarkView extends ViewPart implements CatalogListener, IDragHost, IDropHost {

	private final class BookmarkDropTargetListener extends EffectDropTargetListener {
		private final FileTransfer fileTransfer;
		private final int ops;
		private LocalSelectionTransfer selectionTransfer;

		public BookmarkDropTargetListener(int ops) {
			super(getControl());
			this.ops = ops;
			final DropTarget target = new DropTarget(getControl(), ops);
			fileTransfer = FileTransfer.getInstance();
			selectionTransfer = LocalSelectionTransfer.getTransfer();
			target.setTransfer(new Transfer[] { fileTransfer, selectionTransfer });
			target.addDropListener(this);
		}

		@Override
		public void dragEnter(DropTargetEvent event) {
			int detail = event.detail;
			event.detail = DND.DROP_NONE;
			if (!isDragging() && !Core.getCore().getDbManager().isReadOnly())
				for (int i = 0; i < event.dataTypes.length; i++)
					if (fileTransfer.isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
						if ((detail & ops) != 0) {
							IDragHost dragHost = UiActivator.getDefault().getDragHost();
							if (dragHost == null || !(dragHost instanceof AbstractGalleryView)) {
								event.detail = DND.DROP_COPY;
								break;
							}
						}
					} else if (selectionTransfer.isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
						event.detail = (detail & ops) == 0 ? DND.DROP_NONE : DND.DROP_COPY;
						break;
					}
			super.dragEnter(event);
		}

		public void dragOperationChanged(DropTargetEvent event) {
			int detail = event.detail;
			event.detail = DND.DROP_NONE;
			if (!Core.getCore().getDbManager().isReadOnly() && (detail & ops) != 0
					&& (fileTransfer.isSupportedType(event.currentDataType)
							|| selectionTransfer.isSupportedType(event.currentDataType)))
				event.detail = DND.DROP_COPY;
		}

		public void drop(DropTargetEvent event) {
			if (fileTransfer.isSupportedType(event.currentDataType) && !Core.getCore().getDbManager().isReadOnly()) {
				String[] filenames = (String[]) event.data;
				if (filenames.length > 0) {
					File soundFile = null;
					for (int j = 0; j < filenames.length; j++) {
						File file = new File(filenames[j]);
						if (Constants.SOUNDFILEFILTER.accept(file)) {
							soundFile = file;
							break;
						}
					}
					if (soundFile != null)
						importSound(event.x, event.y, soundFile);
				}
			} else if (selectionTransfer.isSupportedType(event.currentDataType)) {
				ISelection selection = selectionTransfer.getSelection();
				if (selection instanceof AssetSelection)
					dropAssets((AssetSelection) selection);
			}
		}

	}

	private static class BookmarkToolTipSupport extends DefaultToolTip {
//		private final SimpleDateFormat sf = new SimpleDateFormat(Messages.getString("BookmarkView.date_format")); //$NON-NLS-1$
		private final ColumnViewer viewer;
		private Image image;
		private Color bgColor;

		protected BookmarkToolTipSupport(ColumnViewer viewer, int style, boolean manualActivation) {
			super(viewer.getControl(), style, manualActivation);
			this.viewer = viewer;
			setHideDelay(10000);
			setPopupDelay(500);
			bgColor = new Color(viewer.getControl().getDisplay(), 250, 250, 230);
			setBackgroundColor(bgColor);
			setShift(new Point(15, 20));
		}

		@Override
		protected boolean shouldCreateToolTip(Event event) {
			return UiActivator.getDefault().getShowHover() && super.shouldCreateToolTip(event);
		}

		@Override
		protected Composite createToolTipContentArea(Event event, Composite parent) {
			Bookmark b = findObject(event.x, event.y);
			HoverManager hoverManager = UiActivator.getDefault().getHoverManager();
			String text = hoverManager.getHoverText("com.bdaum.zoom.ui.hover.bookmark", b, null); //$NON-NLS-1$
			String title = hoverManager.getHoverTitle("com.bdaum.zoom.ui.hover.bookmark", b, null); //$NON-NLS-1$
			Composite area = new Composite(parent, SWT.NONE);
			area.setBackground(bgColor);
			area.setLayout(new GridLayout(2, false));
			CLabel imageLabel = new CLabel(area, getStyle(event));
			imageLabel.setBackground(bgColor);
			if (image != null) {
				image.dispose();
				image = null;
			}
			Image thumbnail = ImageUtilities.loadThumbnail(parent.getDisplay(), b.getJpegImage(),
					Ui.getUi().getDisplayCMS(), SWT.IMAGE_JPEG, true);
			if (thumbnail != null)
				imageLabel.setImage(image = ImageUtilities.scaleSWT(thumbnail, 160, 160, true, 0, true, null));
			Composite rightArea = new Composite(area, SWT.NONE);
			rightArea.setBackground(bgColor);
			rightArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			rightArea.setLayout(new GridLayout(1, false));
			CLabel titleLabel = new CLabel(rightArea, getStyle(event));
			titleLabel.setBackground(bgColor);
			titleLabel.setText(title);
			titleLabel.setFont(JFaceResources.getBannerFont());
			CLabel description = new CLabel(rightArea, getStyle(event));
			description.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
			description.setBackground(bgColor);
			description.setText(text);
			return area;
		}

		private Bookmark findObject(int x, int y) {
			ViewerCell cell = viewer.getCell(new Point(x, y));
			if (cell != null && cell.getElement() instanceof Bookmark)
				return (Bookmark) cell.getElement();
			return null;
		}

		@Override
		public void deactivate() {
			if (bgColor != null) {
				bgColor.dispose();
				bgColor = null;
			}
			if (image != null) {
				image.dispose();
				image = null;
			}
			super.deactivate();
		}
	}

	public static final String ID = "com.bdaum.zoom.ui.views.BookmarksView"; //$NON-NLS-1$

	private static final int[] COLUMNWIDTHS = new int[] { 200, 150, 70 };

	TableViewer viewer;
	private GotoBookmarkAction gotoBookmarkAction;
	private Action deleteAction;
	private List<Image> images = new ArrayList<Image>();
	protected TableColumn sortColumn;
	protected int sortDirection;
	private boolean isDirty;
	private boolean isVisible;
	private boolean dragging;

	private IPartListener2 partListener = new IPartListener2() {

		public void partActivated(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == BookmarkView.this) {
				isVisible = true;
				show();
			}
		}

		public void partBroughtToTop(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == BookmarkView.this) {
				isVisible = true;
				show();
			}
		}

		public void partClosed(IWorkbenchPartReference partRef) {
			// do nothing
		}

		public void partDeactivated(IWorkbenchPartReference partRef) {
			// do nothing
		}

		public void partHidden(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == BookmarkView.this) {
				isVisible = false;
			}
		}

		public void partInputChanged(IWorkbenchPartReference partRef) {
			// do nothing
		}

		public void partOpened(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == BookmarkView.this) {
				refresh();
				updateActions();
				isVisible = true;
			}
		}

		public void partVisible(IWorkbenchPartReference partRef) {
			if (partRef.getPart(false) == BookmarkView.this) {
				isVisible = true;
				show();
			}
		}
	};

	private void show() {
		if (isDirty) {
			refresh();
			updateActions();
			isDirty = false;
		}
	}

	public void dropAssets(final AssetSelection selection) {
		IAction action = ZoomActionFactory.ADDBOOKMARK.create(null, new IAdaptable() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public Object getAdapter(Class adapter) {
				if (AssetSelection.class.equals(adapter))
					return selection;
				return BookmarkView.this.getAdapter(adapter);
			}
		});
		action.run();
	}

	private void importSound(int x, int y, File sound) {
		Point coord = getControl().toControl(x, y);
		Object obj = findObject(coord.x, coord.y);
		if (obj instanceof Asset && ((Asset) obj).getFileState() != IVolumeManager.PEER) {
			String uri = sound.toURI().toString();
			OperationJob.executeOperation(new VoiceNoteOperation((Asset) obj, uri, uri, null, null), this);
		}
	}

	private void refresh() {
		disposeImages();
		viewer.setInput(this);
	}

	private void addPartListener() {
		getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);
	}

	@SuppressWarnings("unused")
	@Override
	public void createPartControl(final Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		area.setLayout(new GridLayout(1, false));
		viewer = new TableViewer(area, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), HelpContextIds.BOOKMARK_VIEW);
		final TableViewerColumn col1 = createColumn(viewer, Messages.getString("BookmarkView.image"), COLUMNWIDTHS[0]); //$NON-NLS-1$
		final ColumnLabelProvider assetLabelProvider = new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Bookmark) {
					Bookmark bookmark = (Bookmark) element;
					String label = bookmark.getLabel();
					String peer = bookmark.getPeer();
					String catFile = bookmark.getCatFile();
					return catFile != null && !catFile.isEmpty() ? (peer != null) ? NLS.bind("{0} ({1}, {2})", //$NON-NLS-1$
							new Object[] { label, peer, new File(catFile).getName() })
							: NLS.bind("{0} ({1})", label, //$NON-NLS-1$
									new File(catFile).getName())
							: label;
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof Bookmark) {
					byte[] jpegImage = ((Bookmark) element).getJpegImage();
					if (jpegImage != null && jpegImage.length > 0) {
						Image image = ImageUtilities.loadThumbnail(getSite().getShell().getDisplay(), jpegImage,
								Ui.getUi().getDisplayCMS(), SWT.IMAGE_JPEG, false);
						Image smallImage = ImageUtilities.scaleSWT(image, 24, 24, true, 0, true,
								parent.getBackground());
						images.add(smallImage);
						return smallImage;
					}
				}
				return null;
			}

		};
		col1.setLabelProvider(assetLabelProvider);
		col1.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Bookmark && value instanceof String) {
					((Bookmark) element).setLabel((String) value);
					Core.getCore().getDbManager().safeTransaction(null, element);
					viewer.update(element, null);
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Bookmark)
					return ((Bookmark) element).getLabel();
				return null;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getTable());
			}

			@Override
			protected boolean canEdit(Object element) {
				return element instanceof Bookmark;
			}
		});
		final TableViewerColumn col2 = createColumn(viewer, Messages.getString("BookmarkView.collection"), //$NON-NLS-1$
				COLUMNWIDTHS[1]);
		final ICore core = Core.getCore();
		final ColumnLabelProvider collectionLabelProvider = new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof Bookmark) {
					SmartCollectionImpl sm = core.getDbManager().obtainById(SmartCollectionImpl.class,
							((Bookmark) element).getCollectionId());
					return sm == null ? "" : sm.getName(); //$NON-NLS-1$
				}
				return super.getText(element);
			}
		};
		col2.setLabelProvider(collectionLabelProvider);
		final TableViewerColumn col3 = createColumn(viewer, Messages.getString("BookmarkView.created"), //$NON-NLS-1$
				COLUMNWIDTHS[2]);
		final ColumnLabelProvider dateLabelProvider = new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof Bookmark) {
					Date createdAt = ((Bookmark) element).getCreatedAt();
					return createdAt == null ? "" : Format.MDY_TIME_LONG_FORMAT.get().format(createdAt); //$NON-NLS-1$
				}
				return super.getText(element);
			}
		};
		col3.setLabelProvider(dateLabelProvider);
		Table table = viewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(new IStructuredContentProvider() {
			public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
				// do nothing
			}

			public void dispose() {
				disposeImages();
			}

			public Object[] getElements(Object inputElement) {
				final IDbManager dbManager = Core.getCore().getDbManager();
				List<BookmarkImpl> bookmarks = dbManager.obtainObjects(BookmarkImpl.class);
				List<BookmarkImpl> existingBookmarks = new ArrayList<BookmarkImpl>(50);
				final List<Object> tobeDeleted = new ArrayList<Object>();
				for (BookmarkImpl bookmark : bookmarks) {
					if (bookmark.getCatFile() != null && !bookmark.getCatFile().isEmpty())
						existingBookmarks.add(bookmark);
					else if (!dbManager.exists(AssetImpl.class, bookmark.getAssetId()))
						tobeDeleted.add(bookmark);
					else
						existingBookmarks.add(bookmark);
				}
				if (!tobeDeleted.isEmpty())
					dbManager.safeTransaction(tobeDeleted, null);
				return existingBookmarks.toArray();
			}
		});
		viewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer aViewer, Object e1, Object e2) {
				if (sortColumn == col3.getColumn()) {
					Date d1 = ((Bookmark) e1).getCreatedAt();
					Date d2 = ((Bookmark) e2).getCreatedAt();
					return d1 == null || d2 == null ? 0
							: (sortDirection == SWT.DOWN) ? d1.compareTo(d2) : d2.compareTo(d1);
				}
				ColumnLabelProvider labelProvider;
				if (sortColumn == col1.getColumn())
					labelProvider = assetLabelProvider;
				else
					labelProvider = collectionLabelProvider;
				String s1 = labelProvider.getText(e1);
				String s2 = labelProvider.getText(e2);
				return (sortDirection == SWT.DOWN) ? s1.compareToIgnoreCase(s2) : s2.compareToIgnoreCase(s1);
			}
		});
		addPartListener();
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateActions();
			}
		});
		new ColumnLayoutManager(viewer, COLUMNWIDTHS, null);
		switchSort(table, col1.getColumn());
		new AssetDragSourceListener(this, DND.DROP_COPY | DND.DROP_MOVE);
		new BookmarkDropTargetListener(DND.DROP_MOVE | DND.DROP_COPY);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		installHoveringController();
		core.addCatalogListener(this);
		catalogOpened(false);
	}

	private TableViewerColumn createColumn(final TableViewer tViewer, String lab, int w) {
		final TableViewerColumn column = new TableViewerColumn(tViewer, SWT.NONE);
		column.getColumn().setText(lab);
		column.getColumn().setWidth(w);
		column.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				switchSort(tViewer.getTable(), column.getColumn());
			}
		});
		return column;
	}

	protected void switchSort(Table table, TableColumn column) {
		if (sortColumn == column)
			sortDirection = sortDirection == SWT.DOWN ? SWT.UP : SWT.DOWN;
		else {
			sortColumn = column;
			sortDirection = SWT.DOWN;
		}
		table.setSortColumn(column);
		table.setSortDirection(sortDirection);
		disposeImages();
		viewer.refresh();
	}

	private void disposeImages() {
		for (Image image : images)
			image.dispose();
		images.clear();
	}

	private void updateActions() {
		if (!viewer.getControl().isDisposed()) {
			boolean enabled = !viewer.getStructuredSelection().isEmpty();
			gotoBookmarkAction.setEnabled(enabled);
			deleteAction.setEnabled(enabled);
		}
	}

	private void makeActions() {
		gotoBookmarkAction = new GotoBookmarkAction(null, this);
		deleteAction = new Action(Messages.getString("BookmarkView.delete_bookmark"), Icons.delete //$NON-NLS-1$
				.getDescriptor()) {
			@Override
			public void run() {
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (!selection.isEmpty()) {
					Bookmark bookmark = (Bookmark) selection.getFirstElement();
					Core.getCore().getDbManager().safeTransaction(bookmark, null);
					int i = 0;
					while (true) {
						Object element = viewer.getElementAt(i++);
						if (element == null || element == bookmark)
							break;
					}
					Object nextSelection = viewer.getElementAt(i);
					if (nextSelection == null && i >= 2)
						nextSelection = viewer.getElementAt(i - 2);
					viewer.remove(bookmark);
					if (nextSelection != null)
						viewer.setSelection(new StructuredSelection(nextSelection));
					updateActions();
				}
			}
		};
		deleteAction.setToolTipText(Messages.getString("BookmarkView.delete_bookmark_tooltip")); //$NON-NLS-1$
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BookmarkView.this.fillContextMenu(manager);
			}
		});
		getControl().setMenu(menuMgr.createContextMenu(getControl()));
		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void fillContextMenu(IMenuManager menuManager) {
		updateActions();
		menuManager.add(gotoBookmarkAction);
		menuManager.add(new Separator());
		menuManager.add(deleteAction);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (!selection.isEmpty()) {
					gotoBookmarkAction.setBookmark((Bookmark) selection.getFirstElement());
					gotoBookmarkAction.run();
				}
			}
		});
	}

	protected void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(gotoBookmarkAction);
		toolBarManager.add(deleteAction);
	}

	private void fillLocalPullDown(IMenuManager menuManager) {
		menuManager.add(gotoBookmarkAction);
		menuManager.add(new Separator());
		menuManager.add(deleteAction);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@SuppressWarnings("unused")
	protected void installHoveringController() {
		new BookmarkToolTipSupport(viewer, ToolTip.RECREATE, false);
	}

	public void selectBookmark(Bookmark bookmark) {
		viewer.setSelection(new StructuredSelection(bookmark), true);
	}

	public void assetsModified(BagChange<Asset> changes, QueryField node) {
		if (changes == null || changes.hasRemoved())
			updateViewer();
	}

	public void applyRules(Collection<? extends Asset> assets, QueryField node) {
		// do nothing
	}

	public void catalogClosed(int mode) {
		if (mode == CatalogListener.NORMAL)
			updateViewer();
	}

	public void catalogOpened(boolean newDb) {
		updateViewer();
	}

	public void hierarchyModified() {
		// do nothing
	}

	public void structureModified() {
		updateViewer();
	}

	public void bookmarksModified() {
		updateViewer();
	}

	public void setCatalogSelection(ISelection selection, boolean forceUpdate) {
		// do nothing
	}

	private void updateViewer() {
		if (isVisible) {
			final Shell shell = getSite().getShell();
			if (!shell.isDisposed())
				shell.getDisplay().asyncExec(() -> {
					if (!shell.isDisposed()) {
						refresh();
						updateActions();
					}
				});
		} else
			isDirty = true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchPage.class)
			return getSite().getPage();
		return super.getAdapter(adapter);
	}

	public ISelection getSelection() {
		return viewer.getSelection();
	}

	public boolean cursorOverImage(int x, int y) {
		return true;
	}

	public AssetSelection getAssetSelection() {
		ISelection selection = getSelection();
		IDbManager dbManager = Core.getCore().getDbManager();
		AssetSelection assetSelection = new AssetSelection(((IStructuredSelection) selection).size());
		Iterator<?> iterator = ((IStructuredSelection) selection).iterator();
		while (iterator.hasNext()) {
			String assetId = ((Bookmark) iterator.next()).getAssetId();
			AssetImpl asset = assetId == null ? null : dbManager.obtainAsset(assetId);
			if (asset != null)
				assetSelection.add(asset);
		}
		return assetSelection;
	}

	public Control getControl() {
		return viewer.getControl();
	}

	public Object findObject(MouseEvent e) {
		return findObject(e.x, e.y);
	}

	public Object findObject(int x, int y) {
		TableItem item = viewer.getTable().getItem(new Point(x, y));
		if (item != null) {
			Object data = item.getData();
			if (data instanceof Bookmark) {
				String assetId = ((Bookmark) data).getAssetId();
				return assetId == null ? null : Core.getCore().getDbManager().obtainAsset(assetId);
			}
		}
		return null;
	}

	public IAssetProvider getAssetProvider() {
		return null;
	}

	public SmartCollectionImpl getSelectedCollection() {
		return null;
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
