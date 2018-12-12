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
 * (c) 2009-2018 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.trash.HistoryItem;
import com.bdaum.zoom.core.trash.Trash;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.EmptyTrashOperation;
import com.bdaum.zoom.operations.internal.RestoreOperation;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.actions.ZoomActionFactory;

@SuppressWarnings("restriction")
public class TrashcanView extends LightboxView {

	private final class TrashGalleryPaintListener implements Listener {
		private final LightboxGalleryItemRenderer ir;

		private TrashGalleryPaintListener(LightboxGalleryItemRenderer ir) {
			this.ir = ir;
		}

		public void handleEvent(Event e) {
			GalleryItem item = (GalleryItem) e.item;
			if (item.isDisposed() || item.getImage() == null)
				return;
			Trash trash = (Trash) item.getData(UiConstants.TRASH);
			if (trash == null)
				return;
			if (item.getImage().isDisposed())
				item.setImage(getImage(trash.getAsset()));
			boolean isSelected = false;
			if (selection != null)
				for (Iterator<?> iterator = selection.iterator(); !isSelected && iterator.hasNext();)
					isSelected = iterator.next() == trash;
			ir.setSelected(isSelected);
			ir.draw(e.gc, item, e.index, e.x, e.y, e.width, e.height);
		}
	}

	public static final String ID = "com.bdaum.zoom.ui.views.TrashcanView"; //$NON-NLS-1$

	private static final GalleryItem[] EMPTYITEMS = new GalleryItem[0];

	private Action restoreAction;
	private Action emptyTrashcanAction;
	private List<Trash> trashSet;
	private IAction sortModeAction;
	protected boolean sortMode;

	private Action removeAction;

	public TrashcanView() {
		thumbsize = 96;
		folding = false;
	}

	@Override
	protected void addGalleryPaintListener() {
		gallery.addListener(SWT.PaintItem, new TrashGalleryPaintListener(itemRenderer));
	}

	@Override
	public boolean assetsChanged() {
		return true;
	}

	@Override
	protected void setHelp(int orientation) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(gallery, HelpContextIds.TRASHCAN_VIEW);
	}

	@Override
	protected void hookSelectionService() {
		// do nothing
	}

	@Override
	protected void addDragDropSupport() {
		// do nothing
	}

	@Override
	protected void hookDoubleClickAction(Control control) {
		// do nothing
	}

	@Override
	protected void addMouseListener() {
		// do nothing
	}

	@Override
	public void assetsModified(Collection<? extends Asset> assets, QueryField node) {
		if (node == null) {
			Shell shell = getSite().getShell();
			if (!shell.isDisposed())
				shell.getDisplay().asyncExec(() -> {
					if (!shell.isDisposed())
						refresh();
				});
		}
	}

	@Override
	public void catalogModified() {
		Shell shell = getSite().getShell();
		if (!shell.isDisposed())
			shell.getDisplay().asyncExec(() -> {
				if (!shell.isDisposed()) {
					refresh();
					updateActions(false);
				}
			});
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.character) {
		case SWT.TAB:
		case '+':
		case '-':
			super.keyReleased(e);
			break;
		}
	}

	@Override
	protected void makeActions(IActionBars bars) {
		createScaleContributionItem(MINTHUMBSIZE, MAXTHUMBSIZE);
		selectionActionCluster = ZoomActionFactory.createSelectionActionCluster(this, this);

		sortModeAction = new Action(Messages.getString("TrashcanView.sort_by_name"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				sortMode = isChecked();
				if (isChecked()) {
					sortModeAction.setText(Messages.getString("TrashcanView.sort_by_date")); //$NON-NLS-1$
					sortModeAction.setToolTipText(Messages.getString("TrashcanView.sort_items_by_deletion_date")); //$NON-NLS-1$
				} else {
					sortModeAction.setText(Messages.getString("TrashcanView.sort_by_name")); //$NON-NLS-1$
					sortModeAction.setToolTipText(Messages.getString("TrashcanView.sort_items_by_name")); //$NON-NLS-1$
				}
				redrawCollection(null, null);
			}
		};
		sortModeAction.setToolTipText(Messages.getString("TrashcanView.sort_items_by_name")); //$NON-NLS-1$
		sortModeAction.setImageDescriptor(Icons.alphab_sort.getDescriptor());

		restoreAction = new Action(Messages.getString("TrashcanView.restore"), //$NON-NLS-1$
				Icons.trashrestoreSmall.getDescriptor()) {
			@Override
			public void run() {
				if (selection != null && selection.getFirstElement() instanceof HistoryItem)
					OperationJob.executeOperation(
							new RestoreOperation(selection2Trash(),
									UiActivator.getDefault().createImportConfiguration(TrashcanView.this)),
							TrashcanView.this);
			}
		};
		restoreAction.setToolTipText(Messages.getString("TrashcanView.restore_selected_items")); //$NON-NLS-1$
		removeAction = new Action(Messages.getString("TrashcanView.remove"), Icons.delete.getDescriptor()) { //$NON-NLS-1$
			@Override
			public void run() {
				if (selection != null && selection.getFirstElement() instanceof HistoryItem)
					if (AcousticMessageDialog.openQuestion(getViewSite().getShell(),
							Messages.getString("TrashcanView.remove_trash"), //$NON-NLS-1$
							Messages.getString("TrashcanView.really_remove"))) //$NON-NLS-1$
						OperationJob.executeOperation(new EmptyTrashOperation(selection2Trash()), TrashcanView.this);
			}

		};
		removeAction.setToolTipText(Messages.getString("TrashcanView.remove_tooltip")); //$NON-NLS-1$

		emptyTrashcanAction = new Action(Messages.getString("TrashcanView.empty_trashcan"), Icons.cleartrash //$NON-NLS-1$
				.getDescriptor()) {
			@Override
			public void run() {
				OperationJob.executeOperation(new EmptyTrashOperation(), TrashcanView.this);
			}
		};
		emptyTrashcanAction.setToolTipText(Messages.getString("TrashcanView.clear_trashcan_and_delete_all")); //$NON-NLS-1$
	}

	protected Trash[] selection2Trash() {
		Trash[] hist = new Trash[selection.size()];
		int i = 0;
		for (Iterator<?> it = selection.iterator(); it.hasNext();)
			hist[i++] = (Trash) it.next();
		return hist;
	}

	@Override
	public void updateActions(boolean force) {
		if (restoreAction != null && (isVisible() || force)) {
			restoreAction.setEnabled(gallery.getSelection().length > 0 && !dbIsReadonly());
			emptyTrashcanAction.setEnabled(trashSet != null && !trashSet.isEmpty());
		}
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
		manager.add(sortModeAction);
		manager.add(new Separator());
		manager.add(restoreAction);
		manager.add(emptyTrashcanAction);
		manager.add(scaleContributionItem);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(sortModeAction);
		manager.add(new Separator());
		manager.add(restoreAction);
		manager.add(emptyTrashcanAction);
		manager.add(new Separator());
		selectionActionCluster.addToMenuManager(manager);
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		updateActions(true);
		if (!dbIsReadonly()) {
			manager.add(restoreAction);
			manager.add(removeAction);
		}
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		GalleryItem[] sel = gallery.getSelection();
		HistoryItem[] items = new HistoryItem[sel.length];
		for (int i = 0; i < sel.length; i++)
			items[i] = (HistoryItem) sel[i].getData(UiConstants.TRASH);
		selection = new StructuredSelection(items);
		fireSelection();
	}

	@Override
	public void selectAll() {
		BusyIndicator.showWhile(getSite().getShell().getDisplay(), () -> {
			GalleryItem group = gallery.getItem(0);
			GalleryItem[] items = group.getItems();
			HistoryItem[] hist = new HistoryItem[items.length];
			for (int i = 0; i < items.length; i++)
				hist[i] = (HistoryItem) items[i].getData(UiConstants.TRASH);
			gallery.setSelection(items);
			if (items.length > 0)
				gallery.showItem(items[0]);
			selection = new StructuredSelection(hist);
		});
	}

	@Override
	public void selectNone() {
		gallery.setSelection(EMPTYITEMS);
		selection = StructuredSelection.EMPTY;
	}

	@Override
	public void revertSelection() {
		GalleryItem[] selectedItems = gallery.getSelection();
		GalleryItem group = gallery.getItem(0);
		GalleryItem[] items = group.getItems();
		Set<GalleryItem> all = new HashSet<>(Arrays.asList(items));
		for (GalleryItem galleryItem : selectedItems)
			all.remove(galleryItem);
		GalleryItem[] newItems = all.toArray(new GalleryItem[all.size()]);
		HistoryItem[] hist = new HistoryItem[newItems.length];
		for (int i = 0; i < newItems.length; i++)
			hist[i] = (HistoryItem) newItems[i].getData(UiConstants.TRASH);
		gallery.setSelection(newItems);
		if (newItems.length > 0)
			gallery.showItem(newItems[0]);
		selection = new StructuredSelection(hist);
	}

	@Override
	public void select(int type, boolean shift, boolean alt) {
		GalleryItem[] newItems;
		if (alt || shift) {
			GalleryItem[] selectedItems = gallery.getSelection();
			Set<GalleryItem> currentSelection = new HashSet<>(Arrays.asList(selectedItems));
			if (alt)
				currentSelection.removeAll(select(type));
			else
				currentSelection.addAll(select(type));
			newItems = currentSelection.toArray(new GalleryItem[currentSelection.size()]);
		} else {
			List<GalleryItem> select = select(type);
			newItems = select.toArray(new GalleryItem[select.size()]);
		}
		HistoryItem[] hist = new HistoryItem[newItems.length];
		for (int i = 0; i < newItems.length; i++)
			hist[i] = (HistoryItem) newItems[i].getData(UiConstants.TRASH);
		gallery.setSelection(newItems);
		if (newItems.length > 0)
			gallery.showItem(newItems[0]);
		selection = new StructuredSelection(hist);
	}

	private List<GalleryItem> select(int type) {
		GalleryItem group = gallery.getItem(0);
		GalleryItem[] items = group.getItems();
		List<GalleryItem> result = new ArrayList<>(items.length);
		for (GalleryItem item : items) {
			Trash trash = (Trash) item.getData(UiConstants.TRASH);
			Asset asset = trash.getAsset();
			if (asset != null && testAsset(type, asset))
				result.add(item);
		}
		return result;
	}

	@Override
	public boolean redrawCollection(Collection<? extends Asset> assets, QueryField node) {
		if (gallery == null || gallery.isDisposed())
			return false;
		trashSet = Core.getCore().getDbManager().obtainTrash(sortMode);
		gallery.clearAll();
		gallery.redraw();
		return true;
	}

	@Override
	public void handleEvent(Event event) {
		final GalleryItem item = (GalleryItem) event.item;
		if (item.getParentItem() == null)
			// It's a group
			item.setItemCount(trashSet == null ? 0 : trashSet.size());
		else {
			// It's an item
			int index = item.getParentItem().indexOf(item);
			Trash trashItem = trashSet.get(index);
			item.setImage(getImage(trashItem.getAsset()));
			item.setText(trashItem.getName());
			item.setData(UiConstants.TRASH, trashItem);
		}
	}

	@Override
	protected void updateStatusLine() {
		if (trashSet != null)
			setStatusMessage(NLS.bind(Messages.getString("AbstractGalleryView.n_images"), String //$NON-NLS-1$
					.valueOf(trashSet.size()), String.valueOf(selection == null ? 0 : selection.size())), false);
	}

	@Override
	protected void editTitleArea(GalleryItem item, Rectangle bounds) {
		// Do nothing
	}

}