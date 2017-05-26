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

package com.bdaum.zoom.ui.internal.views;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.SortCriterionImpl;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.AssetProvider;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.css.internal.IExtendedColorModel;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.IZoomActionConstants;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.ConfigureColumnsDialog;
import com.bdaum.zoom.ui.internal.hover.IGalleryHover;
import com.bdaum.zoom.ui.internal.job.DecorateJob;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

import edu.umd.cs.piccolox.swt.PSWTCanvas;

@SuppressWarnings("restriction")
public class TableView extends AbstractGalleryView implements IExtendedColorModel {

	public static final String ID = "com.bdaum.zoom.ui.views.TableView"; //$NON-NLS-1$

	private static final String KEY = "key"; //$NON-NLS-1$

	private static final String COLUMNWIDTH = "COLUMNWIDTH"; //$NON-NLS-1$

	private class TableDecorateJob extends DecorateJob {

		private TableViewer viewer;
		private TableItem[] items;
		private final TableView view;

		private Runnable itemRunnable = new Runnable() {
			public void run() {
				Table table = viewer.getTable();
				if (!table.isDisposed())
					items = table.getItems();
			}
		};

		private Runnable assetRunnable = new Runnable() {
			public void run() {
				Table table = viewer.getTable();
				for (TableItem item : items) {
					if (item != null && !item.isDisposed()) {
						AssetImpl asset = (AssetImpl) item.getData();
						if (asset != null) {
							switch (volumeManager.determineFileState(asset)) {
							case IVolumeManager.REMOTE:
								if (remoteColor != null)
									item.setForeground(remoteColor);
								break;
							case IVolumeManager.OFFLINE:
								if (offlineColor != null)
									item.setForeground(offlineColor);
								break;
							default:
								item.setForeground(table.getForeground());
								break;
							}
						}
					}
				}
				table.redraw();
				items = null;
			}
		};

		public TableDecorateJob(TableView view, final TableViewer viewer) {
			super(Messages.getString("TableView.decorate_table_view")); //$NON-NLS-1$
			this.view = view;
			this.viewer = viewer;
		}

		@Override
		protected boolean mayRun() {
			return view.isVisible() && refreshing <= 0 && super.mayRun();
		}

		@Override
		protected void doRun(IProgressMonitor monitor) {
			final Table table = viewer.getTable();
			if (!table.isDisposed()) {
				Display display = table.getDisplay();
				display.syncExec(itemRunnable);
				if (items != null && items.length > 0 && items[0] != null && mayRun())
					display.asyncExec(assetRunnable);
			}
		}
	}

	private class ThumbnailLabelProvider extends OwnerDrawLabelProvider {

		@Override
		protected void measure(Event event, Object element) {
			event.width = thumbsize;
			event.height = thumbsize;
		}
		
		@Override
		protected void erase(Event event, Object element) {
			// do nothing
		}

		@Override
		protected void paint(Event event, Object element) {
			Image image = getImage(element);
			if (image != null) {
				GC gc = event.gc;
				Rectangle bounds = image.getBounds();
				int width = thumbsize; // event.width;
				int height = thumbsize; // event.height;
				double fac = Math.min(((float) width) / bounds.width, ((float) height) / bounds.height);
				int w = (int) (bounds.width * fac);
				int h = (int) (bounds.height * fac);
				gc.drawImage(image, 0, 0, bounds.width, bounds.height, event.x + (width - w) / 2,
						event.y + (width - h) / 2, w, h);
			}
		}
	}

	private class MetaDataLabelProvider extends ZColumnLabelProvider {

		private final QueryField qfield;

		public MetaDataLabelProvider(QueryField qfield) {
			this.qfield = qfield;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof Asset) {
				Asset a = (Asset) element;
				IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(a.getFormat());
				if (mediaSupport != null && !qfield.testFlags(mediaSupport.getPropertyFlags()))
					return Messages.getString("TableView.na"); //$NON-NLS-1$
				if (qfield == QueryField.SCORE)
					return (scoreFormatter != null) ? scoreFormatter.format(a.getScore()) : ""; //$NON-NLS-1$
				String text = qfield.value2text(qfield.obtainFieldValue(a), ""); //$NON-NLS-1$
				if (text != null) {
					String unit = qfield.getUnit();
					if (unit != null)
						text += ' ' + unit;
					return text;
				}
				return null;
			}
			return element.toString();
		}

	}

	public class ViewEditingSupport extends EditingSupport {

		private final QueryField qfield;

		public ViewEditingSupport(ColumnViewer viewer, QueryField qfield) {
			super(viewer);
			this.qfield = qfield;
		}

		@Override
		protected boolean canEdit(Object element) {
			if (dbIsReadonly())
				return false;
			if (element instanceof AssetImpl)
				return qfield.isEditable((AssetImpl) element);
			return false;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			if (element instanceof AssetImpl)
				return determineCellEditor(qfield, (Composite) getViewer().getControl(), 32);
			return null;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof Asset) {
				Object fieldValue = qfield.obtainFieldValue((Asset) element);
				if (qfield.getCard() == QueryField.CARD_MODIFIABLEBAG)
					return fieldValue;
				if (fieldValue instanceof Integer && qfield.getEnumeration() == null)
					return String.valueOf(fieldValue);
				if (fieldValue instanceof Double) {
					NumberFormat nf = NumberFormat.getInstance();
					if (qfield.getType() == QueryField.T_CURRENCY) {
						int digits = Format.getCurrencyDigits();
						nf.setMaximumFractionDigits(digits);
						nf.setMinimumFractionDigits(digits);
					} else
						nf.setMaximumFractionDigits(qfield.getMaxlength());
					return nf.format(fieldValue);
				}
				return fieldValue;
			}
			return null;
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof Asset && value != null) {
				if (qfield.getCard() == QueryField.CARD_MODIFIABLEBAG) {
					if (value instanceof BagChange)
						updateAssets(value, qfield.obtainFieldValue((Asset) element), qfield);
					return;
				}
				switch (qfield.getType()) {
				case QueryField.T_INTEGER:
				case QueryField.T_POSITIVEINTEGER:
					if (!(value instanceof Integer))
						value = Integer.valueOf(Integer.parseInt(value.toString()));
					break;
				case QueryField.T_FLOAT:
				case QueryField.T_POSITIVEFLOAT:
				case QueryField.T_CURRENCY:
					if (!(value instanceof Double)) {
						NumberFormat nf = NumberFormat.getInstance();
						nf.setMaximumFractionDigits(8);
						try {
							value = new Double(nf.parse(value.toString()).doubleValue());
						} catch (ParseException e) {
							return;
						}
					}
				}
				updateAssetsIfNecessary(qfield, value, qfield.obtainFieldValue((Asset) element));
			}
		}
	}

	private static final String SORT_COLUMN = "sortColumn"; //$NON-NLS-1$

	private static final String SORT_DIRECTION = "sortDirection"; //$NON-NLS-1$

	private TableViewer gallery;

	private String sortColumn;

	private int sortDirection = SWT.DOWN;

	private TableViewerColumn imageColumn;

	private Map<String, Integer> columnWidths = new HashMap<String, Integer>();

	protected Widget selectedColumn;

	private TableViewerColumn scoreColumn;

	private Color offlineColor;

	private Color remoteColor;

	private List<QueryField> displayedFields = new ArrayList<QueryField>();

	private int thumbsizeLowWatermark;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			sortColumn = memento.getString(SORT_COLUMN);
			try {
				Integer i = memento.getInteger(SORT_DIRECTION);
				sortDirection = i != null ? i : 1;
			} catch (Exception e) {
				// do nothing
			}
			IMemento child = memento.getChild(COLUMNWIDTH);
			if (child != null)
				for (String key : child.getAttributeKeys())
					columnWidths.put(key, child.getInteger(key));
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			if (sortColumn != null) {
				memento.putString(SORT_COLUMN, sortColumn);
				memento.putInteger(SORT_DIRECTION, sortDirection);
			}
			IMemento child = memento.getChild(COLUMNWIDTH);
			if (child == null)
				child = memento.createChild(COLUMNWIDTH);
			for (Map.Entry<String, Integer> entry : columnWidths.entrySet())
				child.putInteger(entry.getKey(), entry.getValue());
		}
		super.saveState(memento);
	}

	@Override
	public void createPartControl(Composite parent) {
		thumbsizeLowWatermark = thumbsize;
		createGallery(parent, false);
		makeActions(getViewSite().getActionBars());
		installListeners(parent);
		// Hover
		installHoveringController();
		addCueListener();
		// Contributions
		contributeToActionBars();
		setDecorator(gallery.getControl(), new TableDecorateJob(this, gallery));
		setSortCriterion();
		updateActions();
		InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID)
				.addPreferenceChangeListener(new IPreferenceChangeListener() {
					public void preferenceChange(PreferenceChangeEvent event) {
						if (PreferenceConstants.TABLECOLUMNS.equals(event.getKey())) {
							recreateGallery();
						}
					}
				});
	}

	private void createGallery(Composite parent, boolean recreate) {
		gallery = new TableViewer(parent, SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(gallery.getControl(), HelpContextIds.TABLE_VIEW);
		themeChanged();
		gallery.setContentProvider(new ILazyContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// do nothing
			}

			public void dispose() {
				// do nothing
			}

			public void updateElement(int index) {
				IAssetProvider assetProvider = getAssetProvider();
				if (assetProvider != null) {
					Asset asset = assetProvider.getAsset(index);
					if (asset != null) {
						synchronized (gallery) {
							gallery.replace(asset, index);
						}
					}
				}
			}
		});
		final Table table = gallery.getTable();
		if (!recreate)
			setAppStarting(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		// Construct columns
		List<String> props = new ArrayList<String>();
		imageColumn = new TableViewerColumn(gallery, SWT.NONE);
		TableColumn icolumn = imageColumn.getColumn();
		icolumn.setWidth(thumbsize);
		icolumn.setResizable(false);
		icolumn.setText(Messages.getString("TableView.configure")); //$NON-NLS-1$
		imageColumn.setLabelProvider(new ThumbnailLabelProvider());
		icolumn.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ConfigureColumnsDialog dialog = new ConfigureColumnsDialog(getSite().getShell());
				dialog.create();
				dialog.getShell().setLocation(gallery.getControl().toDisplay(e.x, e.y));
				dialog.open();
			}
		});
		props.add("$"); //$NON-NLS-1$
		QueryField scoreField = QueryField.SCORE;
		scoreColumn = createColumn(table, scoreField, Messages.getString("TableView.score"), 50, //$NON-NLS-1$
				new MetaDataLabelProvider(
						scoreField));
		props.add(scoreField.getKey());
		StringTokenizer st = new StringTokenizer(
				UiActivator.getDefault().getPreferenceStore().getString(PreferenceConstants.TABLECOLUMNS), "\n"); //$NON-NLS-1$
		displayedFields.clear();
		while (st.hasMoreTokens()) {
			String id = st.nextToken();
			QueryField qfield = QueryField.findQueryField(id);
			if (qfield != null) {
				displayedFields.add(qfield);
				createColumn(table, qfield, qfield.getLabel(), 120, new MetaDataLabelProvider(qfield));
				props.add(id);
			}
		}
		gallery.setColumnProperties(props.toArray(new String[props.size()]));
		gallery.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (refreshing <= 0) {
					stopAudio();
					selection = doGetAssetSelection();
					fireSelection();
				}
			}
		});
		ColumnViewerToolTipSupport.enableFor(gallery);
		addKeyListener();
		addGestureListener(gallery.getTable());
		addExplanationListener();
		addDragDropSupport();
		hookContextMenu();
		hookDoubleClickAction();
	}

	private TableViewerColumn createColumn(final Table table, QueryField qfield, String label, int defaultWidth,
			CellLabelProvider labelProvider) {
		final String key = qfield.getKey();
		TableViewerColumn tableViewerColumn = new TableViewerColumn(gallery, SWT.NONE);
		final TableColumn column = tableViewerColumn.getColumn();
		column.setText(label);
		column.setData(KEY, key);
		Integer w = columnWidths.get(key);
		column.setWidth(w != null ? w : defaultWidth);
		column.setResizable(true);
		column.addControlListener(new ControlAdapter() {

			@Override
			public void controlResized(ControlEvent e) {
				int width = column.getWidth();
				if (width > 0)
					columnWidths.put(key, width);
			}
		});
		column.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (refreshing > 0)
					return;
				switchSort(table, key, column);
			}
		});
		tableViewerColumn.setLabelProvider(labelProvider);
		if (key.equals(sortColumn)) {
			table.setSortColumn(column);
			table.setSortDirection(sortDirection);
		}
		if (qfield.getEditable() != QueryField.EDIT_NEVER && qfield.getKey() != null
				&& !(qfield.isStruct() && qfield.getCard() != 1) && qfield.getType() != QueryField.T_NONE)
			tableViewerColumn.setEditingSupport(new ViewEditingSupport(gallery, qfield));
		return tableViewerColumn;
	}

	private void switchSort(final Table table, final String key, final TableColumn column) {
		if (key.equals(sortColumn)) {
			switch (sortDirection) {
			case SWT.DOWN:
				sortDirection = SWT.UP;
				break;
			default:
				sortDirection = SWT.DOWN;
				break;
			}
		} else {
			sortColumn = key;
			sortDirection = SWT.DOWN;
		}
		table.setSortColumn(column);
		table.setSortDirection(sortDirection);
		setSortCriterion();
	}

	private void setSortCriterion() {
		if (sortColumn != null)
			getNavigationHistory().sortChanged(new SortCriterionImpl(sortColumn, null, sortDirection == SWT.UP));
	}

	@Override
	public void sortChanged() {
		redrawCollection(null, null);
		fireSelection();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		super.keyReleased(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// do nothing
	}

	protected void hookDoubleClickAction() {
		gallery.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				IStructuredSelection sel = (IStructuredSelection) gallery.getSelection();
				Event event = new Event();
				event.data = sel.getFirstElement();
				event.stateMask = e.stateMask;
				viewImageAction.runWithEvent(event);
			}
		});
	}

	@Override
	protected void makeActions(IActionBars bars) {
		createScaleContributionItem(MINTHUMBSIZE, MAXTHUMBSIZE / 2);
		super.makeActions(bars);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		// if (toggleCollapseAction != null) {
		// manager.add(toggleCollapseAction);
		// manager.add(new Separator());
		// }
		boolean readOnly = dbIsReadonly();
		fillEditAndSearchGroup(manager, readOnly);
		fillRotateGroup(manager, readOnly);
		fillVoiceNote(manager, readOnly);
		fillMetaData(manager, readOnly);
		fillRelationsGroup(manager);
		manager.add(new Separator(IZoomActionConstants.MB_SUBMENUS));
		fillShowAndDeleteGroup(manager, readOnly);
		super.fillAdditions(manager);
	}

	@Override
	public void setFocus() {
		gallery.getControl().setFocus();
	}

	@Override
	protected void fireSizeChanged() {
		Object firstElement = ((IStructuredSelection) getSelection()).getFirstElement();
		Object item = null;
		if (firstElement instanceof AssetImpl)
			item = firstElement;
		firstElement = ((IStructuredSelection) getSelection()).getFirstElement();
		if (thumbsizeLowWatermark > thumbsize)
			recreateGallery();
		else {
			imageColumn.getColumn().setWidth(thumbsize);
			gallery.refresh();
		}
		thumbsizeLowWatermark = Math.min(thumbsizeLowWatermark, thumbsize);
		if (firstElement != null)
			gallery.reveal(item);
	}

	private void recreateGallery() {
		try {
			++refreshing;
			ISelection sel = gallery.getSelection();
			unhookContextMenu();
			hoveringController.uninstall();
			gallery.setItemCount(0);
			Table oldTable = gallery.getTable();
			oldTable.setVisible(false);
			Composite parent = oldTable.getParent();
			for (TableColumn column : oldTable.getColumns())
				column.dispose();
			oldTable.dispose();
			createGallery(parent, true);
			gallery.setItemCount(getAssetProvider().getAssetCount());
			parent.layout();
			installHoveringController();
			gallery.setSelection(sel);
			gallery.getControl().setFocus();
		} finally {
			--refreshing;
		}
	}

	public boolean cursorOverImage(int x, int y) {
		ViewerCell cell = gallery.getCell(new Point(x, y));
		return (cell != null) ? (cell.getElement() instanceof AssetImpl) : false;
	}

	@Override
	public void themeChanged() {
		CssActivator.getDefault().applyExtendedStyle(gallery.getTable(), this);
	}

	public void setOfflineColor(org.eclipse.swt.graphics.Color c) {
		offlineColor = c;
	}

	public void setSelectedOfflineColor(Color selectedOfflineColor) {
		// do nothing
	}

	public void setRemoteColor(org.eclipse.swt.graphics.Color c) {
		remoteColor = c;
	}

	public void setTitleColor(org.eclipse.swt.graphics.Color c) {
		// not used
	}

	public void setSelectedRemoteColor(Color selectedRemoteColor) {
		// do nothing
	}

	public boolean applyColorsTo(Object element) {
		return element instanceof PSWTCanvas;
	}

	@Override
	public IAssetProvider getAssetProvider() {
		IAssetProvider assetProvider = Core.getCore() == null ? null : Core.getCore().getAssetProvider();
		return assetProvider instanceof AssetProvider ? ((AssetProvider) assetProvider).getSlave(ID) : null;
	}

	@Override
	protected boolean doRedrawCollection(Collection<? extends Asset> assets, QueryField node) {
		thumbsizeLowWatermark = thumbsize;
		if (gallery == null || gallery.getControl().isDisposed())
			return false;
		if (assets == null) {
			AssetSelection oldSelection = (AssetSelection) (selection == null ? AssetSelection.EMPTY : selection);
			if (fetchAssets()) {
				IAssetProvider assetProvider = getAssetProvider();
				scoreFormatter = assetProvider.getScoreFormatter();
				TableColumn scolumn = scoreColumn.getColumn();
				if (scoreFormatter != null) {
					scolumn.setText(scoreFormatter.getLabel());
					scolumn.setResizable(true);
					scolumn.setWidth(50);
					Integer w = columnWidths.get(QueryField.SCORE.getKey());
					scolumn.setWidth(w != null ? w : 50);
				} else {
					scolumn.setWidth(0);
					scolumn.setResizable(false);
				}
				gallery.setItemCount(assetProvider.getAssetCount());
				gallery.refresh();
				if (oldSelection.isPicked()) {
					gallery.setSelection(oldSelection);
					selection = new AssetSelection(oldSelection.toList());
				} else {
					selection = new AssetSelection(assetProvider);
					gallery.getTable().selectAll();
				}
			}
		} else {
			String[] props = null;
			if (node != null) {
				final List<String> nodes = new ArrayList<String>();
				QueryField.Visitor qfVisitor = new QueryField.Visitor() {
					@Override
					public void doVisitorWork(QueryField qf) {
						String key = qf.getKey();
						if (key != null)
							nodes.add(key);
					}
				};
				qfVisitor.visit(node);
				props = nodes.toArray(new String[nodes.size()]);
			}
			gallery.update(assets.toArray(), props);
		}
		return true;
	}

	@Override
	protected void setAssetSelection(AssetSelection assetSelection) {
		selection = assetSelection;
		if (assetSelection.isPicked())
			gallery.setSelection(assetSelection, true);
		else
			gallery.getTable().selectAll();
		fireSelection();
	}

	public Object findObject(MouseEvent e) {
		return findObject(e.x, e.y);
	}

	public Object findObject(int x, int y) {
		ViewerCell cell = gallery.getCell(new Point(x, y));
		return (cell != null && cell.getElement() instanceof AssetImpl) ? (AssetImpl) cell.getElement() : null;
	}

	public AssetSelection getAssetSelection() {
		if (selection instanceof AssetSelection)
			return (AssetSelection) selection;
		return doGetAssetSelection();
	}

	protected AssetSelection doGetAssetSelection() {
		IStructuredSelection s = (IStructuredSelection) gallery.getSelection();
		AssetSelection sel = new AssetSelection(s.size());
		for (Object item : s.toArray())
			sel.add((Asset) item);
		return sel;
	}

	@Override
	protected int getSelectionCount(boolean local) {
		if (local) {
			int i = 0;
			@SuppressWarnings("unchecked")
			Iterator<Object> iterator = ((IStructuredSelection) gallery.getSelection()).iterator();
			while (iterator.hasNext()) {
				Asset a = (Asset) iterator.next();
				if (a.getFileState() != IVolumeManager.PEER && ++i >= 2)
					return i;
			}
			return i;
		}
		return ((IStructuredSelection) getSelection()).size();
	}

	public Control getControl() {
		return gallery.getControl();
	}

	@Override
	public boolean assetsChanged() {
		INavigationHistory navigationHistory = getNavigationHistory();
		if (navigationHistory != null) {
			AssetSelection selectedAssets = navigationHistory.getSelectedAssets();
			if (selectedAssets.isPicked()) {
				gallery.setSelection(selectedAssets);
				if (!selectedAssets.isEmpty())
					gallery.reveal(selectedAssets.get(0));
			} else
				gallery.getTable().selectAll();
			selection = selectedAssets;
			return true;
		}
		return false;
	}

	@Override
	public void catalogModified() {
		IAssetProvider assetProvider = getAssetProvider();
		if (assetProvider != null)
			assetProvider.resetProcessor();
		super.catalogModified();
	}

	@Override
	public void assetsModified(final Collection<? extends Asset> assets, final QueryField node) {
		Shell shell = getSite().getShell();
		if (!shell.isDisposed()) {
			boolean invalid = CoreActivator.getDefault().resetInfrastructure(getAssetProvider(), assets, node);
			Display display = shell.getDisplay();
			if (assets == null || invalid) {
				display.asyncExec(new Runnable() {

					public void run() {
						redrawCollection(assets, node);
						if (selection instanceof AssetSelection) {
							Object firstElement = ((AssetSelection) selection).getFirstElement();
							if (firstElement != null)
								gallery.reveal(firstElement);
						}
					}
				});
			} else if (node == null || affectsDisplayedFields(node)) {
				display.asyncExec(new Runnable() {
					public void run() {
						for (Asset asset : assets)
							gallery.update(asset, null);
					}
				});
			}
			display.asyncExec(new Runnable() {
				public void run() {
					updateActions();
				}
			});
		}
	}

	private boolean affectsDisplayedFields(QueryField node) {
		for (QueryField field : displayedFields)
			if (QueryField.belongsTo(field, node))
				return true;
		return false;
	}

	public void setSelection(ISelection selection) {
		assetsChanged();
	}

	@Override
	public IGalleryHover getGalleryHover(MouseEvent event) {
		return (event.x > gallery.getTable().getColumn(0).getWidth()) ? null : new GalleryHover();
	}

	@Override
	protected void setDefaultPartName() {
		setPartName(Messages.getString("TableView.table")); //$NON-NLS-1$
	}

}
