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
 * (c) 2014 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ui.internal.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEffect;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.UiConstants;

/**
 * Drop target effect (sticky image)
 */
public class AssetDropTargetEffect extends DropTargetEffect implements PaintListener {
	private static final int MULTDIST = 5;
	private Shell shell;
	private Image image;
	private int mode = -1;
	private Point pos = new Point(0, 0);
	private double magnification = 0.3d;
	private String caption = null;
	private Region region;

	public AssetDropTargetEffect(IDragHost host, Control control) {
		super(control);
		AssetSelection sel = host.getAssetSelection();
		if (sel != null && !sel.isEmpty()) {
			if (sel.size() > 1)
				caption = " " + String.valueOf(sel.size()) + " "; //$NON-NLS-1$ //$NON-NLS-2$
			image = Core.getCore().getImageCache().getImage(sel.getFirstElement());
			Rectangle bounds = image.getBounds();
			magnification = UiConstants.ANIMATION_SIZE
					/ Math.sqrt(bounds.width * bounds.width + bounds.height * bounds.height);
		}
	}

	public void setMode(final int mode) {
		this.mode = mode;
		setImageVisible(mode != DND.DROP_NONE);
	}

	private void setImageVisible(final boolean visible) {
		if (shell == null && visible && image != null) {
			shell = new Shell(getControl().getDisplay(), SWT.ON_TOP | SWT.NO_TRIM);
			shell.setVisible(false);
			shell.setAlpha(UiConstants.DRAG_ANIMATION_ALPHA);
			shell.addPaintListener(this);
			final org.eclipse.swt.graphics.Rectangle bounds = image.getBounds();
			int w = (int) (bounds.width * magnification) + 2;
			int h = (int) (bounds.height * magnification) + 2;
			if (caption != null) {
				if (region != null)
					region.dispose();
				region = new Region(shell.getDisplay());
				region.add(0, 0, w, h);
				region.add(MULTDIST, MULTDIST, w, h);
				shell.setRegion(region);
				w += MULTDIST;
				h += MULTDIST;
			}
			shell.setSize(w, h);
			shell.redraw();
		}
		if (shell != null)
			shell.setVisible(visible);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.dnd.DropTargetAdapter#dragOver(org.eclipse.swt.dnd
	 * .DropTargetEvent)
	 */
	@Override
	public void dragOver(final DropTargetEvent event) {
		if (event.detail != DND.DROP_NONE) {
			pos.x = event.x + UiConstants.ANIMATION_XOFFSET;
			pos.y = event.y + UiConstants.ANIMATION_YOFFSET;
		}
		if (mode != event.detail)
			setMode(event.detail);
		getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				if (mode != DND.DROP_NONE && shell != null && !shell.isDisposed())
					shell.setLocation(pos);
			}
		});

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt
	 * .events.PaintEvent)
	 */
	public void paintControl(final PaintEvent event) {
		if (image != null) {
			if (mode != DND.DROP_NONE) {
				org.eclipse.swt.graphics.Rectangle imagebounds = image.getBounds();
				GC gc = event.gc;
				int w = (int) (imagebounds.width * magnification);
				int h = (int) (imagebounds.height * magnification);
				gc.setBackground(event.display.getSystemColor(SWT.COLOR_WHITE));
				if (caption != null) {
					gc.fillRectangle(MULTDIST, MULTDIST, w + 2, h + 2);
					gc.drawImage(image, 0, 0, imagebounds.width, imagebounds.height, MULTDIST + 1, MULTDIST + 1, w, h);
				}
				gc.fillRectangle(0, 0, w + 2, h + 2);
				gc.drawImage(image, 0, 0, imagebounds.width, imagebounds.height, 1, 1, w, h);
				if (caption != null) {
					gc.setBackground(event.display.getSystemColor(SWT.COLOR_BLUE));
					gc.setForeground(event.display.getSystemColor(SWT.COLOR_WHITE));
					Point textExtent = gc.textExtent(caption);
					gc.drawText(String.valueOf(caption), (w - textExtent.x) / 2 + 1, (h - textExtent.y) / 2 + 1, false);
				}
			}
		}
	}

	public void dispose() {
		if (shell != null)
			shell.close();
		if (region != null)
			region.dispose();
	}
}