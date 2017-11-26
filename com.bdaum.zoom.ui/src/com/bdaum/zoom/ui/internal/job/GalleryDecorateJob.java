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

package com.bdaum.zoom.ui.internal.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.widgets.Display;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.trash.Trash;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.views.AbstractGalleryView;

public class GalleryDecorateJob extends DecorateJob {

	private final Gallery gallery;
	private GalleryItem[] items;
	private final AbstractGalleryView view;
	private GalleryItem[] groups;
	private GalleryItem group;

	private Runnable groupRunnable = new Runnable() {
		public void run() {
			if (!gallery.isDisposed())
				groups = gallery.getItems();
		}
	};
	private Runnable itemRunnable = new Runnable() {
		public void run() {
			if (group != null && !group.isDisposed())
				items = group.getItems();
		}
	};
	private Runnable assetRunnable = new Runnable() {
		public void run() {
			if (items != null)
				for (GalleryItem item : items) {
					if (item != null && !item.isDisposed()) {
						Asset asset = (AssetImpl) item.getData(UiConstants.ASSET);
						if (asset == null) {
							Trash trash = (Trash) item.getData(UiConstants.TRASH);
							if (trash != null)
								asset = trash.getAsset();
						}
						if (asset != null && asset.getFileState() != IVolumeManager.PEER) {
							int newFileState = volumeManager.determineFileState(asset);
							if (asset.getFileState() != newFileState) {
								asset.setFileState(newFileState);
								gallery.redraw(item);
							}
						}
					}
				}
			items = null;
		}
	};

	public GalleryDecorateJob(AbstractGalleryView view, Gallery gallery) {
		super(Messages.GalleryDecorateJob_decorate_gallery);
		this.view = view;
		this.gallery = gallery;
	}

	@Override
	protected boolean mayRun() {
		return view.isVisible() && view.getRefreshing() <= 0;
	}

	@Override
	protected void doRun(IProgressMonitor monitor) {
		if (!gallery.isDisposed()) {
			Display display = gallery.getDisplay();
			if (!display.isDisposed())
				display.syncExec(groupRunnable);
			if (groups != null) {
				for (int i = 0; i < groups.length; i++) {
					group = groups[i];
					if (!display.isDisposed())
						display.syncExec(itemRunnable);
					if (items != null && items.length > 0 && items[0] != null && !gallery.isDisposed() && mayRun()) {
						display.asyncExec(assetRunnable);
						if (monitor.isCanceled())
							break;
					}
				}
				groups = null;
			}
		}
	}

}
