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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.nebula.widgets.gallery.EnhancedGalleryGroupRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.IImageProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.batch.internal.Daemon;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.operations.internal.dup.AbstractDuplicatesProvider;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.INavigationHistory;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;

@SuppressWarnings("restriction")
public class DuplicatesView extends AbstractLightboxView implements Listener,
		PaintListener {

	public static final String ID = "com.bdaum.zoom.ui.views.DuplicatesView"; //$NON-NLS-1$
	private AbstractDuplicatesProvider duplicatesProvider;
	private Map<Asset, Point> galleryMap = new HashMap<Asset, Point>();
	private int busyCounter;
	private String itemType = Messages.getString("DuplicatesView.duplicates"); //$NON-NLS-1$
	private Daemon busyJob;

	@Override
	public void createPartControl(Composite parent) {
		// Create gallery
		setPreferences();
		gallery = new Gallery(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.VIRTUAL
				| SWT.MULTI);
		gallery.setBackground(parent.getDisplay().getSystemColor(
				SWT.COLOR_WHITE));
		gallery.setHigherQualityDelay(300);
		gallery.setLowQualityOnUserAction(true);
		setHelp();
		// Renderers
		groupRenderer = new EnhancedGalleryGroupRenderer(new IImageProvider() {

			public Image obtainImage(GalleryItem item) {
				AssetImpl asset = (AssetImpl) item.getData(ASSET);
				return asset == null ? null : getImage(asset);
			}
		});
		((EnhancedGalleryGroupRenderer) groupRenderer).setMaxImageHeight(48);
		((EnhancedGalleryGroupRenderer) groupRenderer).setMaxImageWidth(48);
		groupRenderer.setMinMargin(3);
		itemRenderer = new LightboxGalleryItemRenderer(gallery);
		configureItemRenderer(gallery);
		gallery.setGroupRenderer(groupRenderer);
		gallery.setVirtualGroups(true);
		gallery.setVirtualGroupDefaultItemCount(2);
		// Custom draw
		gallery.setItemRenderer(null);
		addGalleryPaintListener();
		gallery.addListener(SWT.SetData, this);
		// Actions
		makeActions(getViewSite().getActionBars());
		installListeners(parent);
		redrawCollection(null, null);
		gallery.addSelectionListener(this);
		installInfrastructure(false, 3000);
	}

	@Override
	public String getTooltip(int mx, int my) {
		GalleryItem item = gallery.getGroup(new Point(mx, my));
		return (item != null && !item.isExpanded()) ? NLS.bind(
				Messages.getString("DuplicatesView.n_images"), //$NON-NLS-1$
				item.getItemCount()) : null;
	}

	@Override
	public void showBusy(boolean busy) {
		super.showBusy(busy);
		disposeBusyIndicator();
		if (!gallery.isDisposed()) {
			if (busy) {
				gallery.addPaintListener(this);
				busyJob = new Daemon(
						Messages.getString("DuplicatesView.update_duplicates"), 250L) { //$NON-NLS-1$
					@Override
					protected void doRun(IProgressMonitor monitor) {
						Shell shell = getSite().getShell();
						if (shell != null && !shell.isDisposed()) {
							shell.getDisplay().asyncExec(() -> {
								if (!gallery.isDisposed())
									gallery.redraw();
							});
						}
					}
				};
				busyJob.schedule();
			} else
				gallery.redraw();
		}
	}

	private void disposeBusyIndicator() {
		if (busyJob != null) {
			Job.getJobManager().cancel(busyJob);
			busyJob = null;
		}
		if (!gallery.isDisposed())
			gallery.removePaintListener(this);
	}

	public void paintControl(PaintEvent e) {
		Rectangle clientArea = gallery.getClientArea();
		String text = NLS
				.bind(Messages
						.getString("DuplicatesView.collection_duplicates"), itemType); //$NON-NLS-1$
		GC gc = e.gc;
		gc.setBackground(gallery.getBackground());
		gc.setForeground(gallery.getForeground());
		Point textExtent = gc.textExtent(text);
		if (busyCounter > 5)
			busyCounter = 0;
		text += ".....".substring(0, busyCounter); //$NON-NLS-1$
		gc.drawText(text, (clientArea.width - textExtent.x) / 2,
				(clientArea.height - textExtent.y) / 2);
		++busyCounter;
	}

	protected void setHelp() {
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(gallery, HelpContextIds.DUPLICATES_VIEW);
	}

	@Override
	protected void makeActions(IActionBars bars) {
		createScaleContributionItem(MINTHUMBSIZE, MAXTHUMBSIZE);
		super.makeActions(bars);
	}

	@Override
	public void fillLocalToolBar(IToolBarManager manager) {
		manager.add(scaleContributionItem);
		manager.add(editAction);
		manager.add(editWithAction);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(editAction);
		manager.add(editWithAction);
	}

	@Override
	public boolean assetsChanged() {
		INavigationHistory navigationHistory = getNavigationHistory();
		if (navigationHistory != null && !gallery.isDisposed()) {
			AssetSelection selectedAssets = navigationHistory
					.getSelectedAssets();
			if (selectedAssets.isPicked()) {
				List<GalleryItem> items = new ArrayList<GalleryItem>(
						selectedAssets.size());
				for (Asset asset : selectedAssets.getAssets()) {
					GalleryItem item = getGalleryItem(asset);
					if (item != null)
						items.add(item);
				}
				GalleryItem[] selectedItems = items
						.toArray(new GalleryItem[items.size()]);
				gallery.setSelection(selectedItems);
				if (selectedItems.length > 0)
					gallery.showItem(selectedItems[0]);
			} else
				gallery.selectAll();
			selection = selectedAssets;
			return true;
		}
		return false;
	}

	@Override
	protected GalleryItem getGalleryItem(Asset asset) {
		Point p = galleryMap.get(asset);
		return (p != null) ? gallery.getItem(p.x).getItem(p.y) : null;
	}

	public void setSelection(ISelection sel) {
		assetsChanged();
	}

	@Override
	protected void setAssetSelection(AssetSelection assetSelection) {
		selection = assetSelection;
		if (assetSelection.isEmpty()) {
			gallery.setSelection(NOITEM);
		} else if (assetSelection.isPicked()) {
			List<Asset> assets = assetSelection.getAssets();
			List<GalleryItem> items = new ArrayList<GalleryItem>(assets.size());
			// Force creation of items because of lazy operation
			for (Asset asset : assets) {
				GalleryItem item = getGalleryItem(asset);
				if (item != null)
					items.add(item);
			}
			gallery.setSelection(items.toArray(new GalleryItem[items.size()]));
			if (!items.isEmpty())
				gallery.showItem(items.get(0));
		} else
			gallery.selectAll();
		fireSelection();
	}

	@Override
	protected boolean doRedrawCollection(Collection<? extends Asset> assets,
			QueryField node) {
		if (gallery == null || gallery.isDisposed())
			return false;
		int size = duplicatesProvider == null ? 0 : duplicatesProvider
				.getDuplicateSetCount();
		gallery.setItemCount(size);
		if (size >= 0) {
			if (assets == null) {
				gallery.clearAll();
				gallery.redraw();
				AssetSelection oldSelection = (AssetSelection) (selection == null ? AssetSelection.EMPTY
						: selection);
				if (oldSelection.isPicked()) {
					List<Asset> selectedAssets = new ArrayList<Asset>(
							oldSelection.size());
					List<GalleryItem> items = new ArrayList<GalleryItem>(
							oldSelection.size());
					for (Asset asset : oldSelection.getAssets()) {
						GalleryItem item = getGalleryItem(asset);
						if (item != null) {
							items.add(item);
							selectedAssets.add(asset);
						}
					}
					GalleryItem[] selectedItems = items
							.toArray(new GalleryItem[items.size()]);
					galleryMap.clear();
					gallery.setSelection(selectedItems);
					selection = new AssetSelection(selectedAssets);
				} else {
					galleryMap.clear();
					gallery.selectAll();
					selection = new AssetSelection(getAssetProvider());
				}
			} else {
				if (assets.size() == 1) {
					GalleryItem item = getGalleryItem(assets.iterator().next());
					if (item != null) {
						if (node == null)
							item.setImage(placeHolder);
						gallery.redraw(item);
					}
				} else {
					if (node == null)
						for (Asset asset : assets) {
							GalleryItem item = getGalleryItem(asset);
							if (item != null)
								item.setImage(placeHolder);
						}
					gallery.redraw();
				}
			}
		}
		return true;
	}

	public void handleEvent(final Event event) {
		Runnable runnable = new Runnable() {
			public void run() {
				final GalleryItem item = (GalleryItem) event.item;
				GalleryItem parentItem = item.getParentItem();
				if (parentItem == null) {
					// It's a group
					int index = event.index;
					int assetCount = duplicatesProvider.getAssetCount(index);
					gallery.setItemCount(duplicatesProvider
							.getDuplicateSetCount());
					if (assetCount > 0) {
						item.setItemCount(assetCount);
						int deleted = 0;
						boolean decorated = false;
						for (int j = 0; j < assetCount; j++) {
							Asset asset = duplicatesProvider.getAsset(index, j);
							if (asset != null) {
								if (!decorated) {
									item.setData(ASSET, asset);
									item.setText(asset.getName());
									Image image = getImage(asset);
									item.setImage(image);
									decorated = false;
								}
							} else
								deleted++;
						}
						if (deleted == assetCount)
							item.setText(1, Messages
									.getString("DuplicatesView.all_images_deleted")); //$NON-NLS-1$
						else if (deleted == 1)
							item.setText(1, Messages.getString("DuplicatesView.one_image_deleted")); //$NON-NLS-1$
						else if (deleted > 1)
							item.setText(1, NLS.bind(Messages
											.getString("DuplicatesView.some_images_deleted"), deleted)); //$NON-NLS-1$
					} else {
						item.setItemCount(0);
						item.setText(NLS.bind(
								Messages.getString("DuplicatesView.no_more_duplicates"), itemType)); //$NON-NLS-1$
					}
					item.setExpanded(false);
				} else {
					// It's an item
					int parentIndex = gallery.indexOf(parentItem);
					int index = event.index;
					Asset asset = duplicatesProvider.getAsset(parentIndex,
							index);
					// item.setImage(getImage(asset));
					item.setImage(placeHolder);
					if (asset != null) {
						item.setData(ASSET, asset);
						galleryMap.put(asset, new Point(parentIndex, index));
					}
					// setItemText(item, asset);
				}
			}
		};
		BusyIndicator.showWhile(getSite().getShell().getDisplay(), runnable);
	}

	@Override
	protected void setItemText(final GalleryItem item, Asset asset,
			Integer cardinality) {
		if (asset != null) {
			StringBuilder sb = new StringBuilder(asset.getUri());
			int p = sb.lastIndexOf("/"); //$NON-NLS-1$
			if (p >= 0)
				sb.delete(0, p + 1);
			item.setText(sb.toString());
		} else
			item.setText(Messages.getString("DuplicatesView.deleted")); //$NON-NLS-1$
	}

	public void setDuplicatesProvider(AbstractDuplicatesProvider provider) {
		gallery.redraw();
		duplicatesProvider = provider;
		String title = duplicatesProvider.getLabel();
		setPartName(title);
		if (provider.getDuplicateSetCount() <= 0)
			AcousticMessageDialog
					.openInformation(
							getSite().getShell(),
							NLS.bind(
									Messages.getString("DuplicatesView.find_duplicates"), itemType), NLS.bind(Messages.getString("DuplicatesView.no_duplicates_found"), itemType)); //$NON-NLS-1$ //$NON-NLS-2$
		else
			redrawCollection(null, null);
	}

	public void reset() {
		gallery.setItemCount(0);
		gallery.clearAll();
		galleryMap.clear();
		gallery.redraw();
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
		setPartName(itemType);
	}

	@Override
	protected void scroll(int dist) {
		ScrollBar scrollBar = gallery.getVerticalBar();
		if (scrollBar == null)
			scrollBar = gallery.getHorizontalBar();
		if (scrollBar != null) {
			int pos = scrollBar.getSelection();
			scrollBar.setSelection(dist + pos);
		}
	}

	@Override
	protected void editTitleArea(GalleryItem item, Rectangle bounds) {
		// Do nothing
	}

	@Override
	protected void setDefaultPartName() {
		// do nothing
	}

}
