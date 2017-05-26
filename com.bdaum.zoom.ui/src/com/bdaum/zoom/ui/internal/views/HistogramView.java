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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;

public class HistogramView extends BasicView implements PaintListener {
	public static final String ID = "com.bdaum.zoom.ui.views.HistogramView"; //$NON-NLS-1$
	private static final int MINTHUMBNAILSIZE = 160 * 120;
	private static final String SHOW_RED = "showRed"; //$NON-NLS-1$
	private static final String SHOW_GREEN = "showGreen"; //$NON-NLS-1$
	private static final String SHOW_BLUE = "showBlue"; //$NON-NLS-1$
	private static final String SHOW_GREY = "showGrey"; //$NON-NLS-1$
	private Canvas canvas;
	private Action redAction;
	private Asset currentItem;
	private int[] reds = new int[256];
	private int[] greens = new int[256];
	private int[] blues = new int[256];
	private int mxvalue;
	private Action greenAction;
	private Action blueAction;
	private Action greyAction;
	protected boolean showRed = true;
	protected boolean showGrey = true;
	protected boolean showBlue = true;
	protected boolean showGreen = true;
	private int count;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			Boolean bool = memento.getBoolean(SHOW_RED);
			if (bool != null)
				showRed = bool;
			bool = memento.getBoolean(SHOW_GREEN);
			if (bool != null)
				showGreen = bool;
			bool = memento.getBoolean(SHOW_BLUE);
			if (bool != null)
				showBlue = bool;
			bool = memento.getBoolean(SHOW_GREY);
			if (bool != null)
				showGrey = bool;
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			memento.putBoolean(SHOW_RED, showRed);
			memento.putBoolean(SHOW_GREEN, showGreen);
			memento.putBoolean(SHOW_BLUE, showBlue);
			memento.putBoolean(SHOW_GREY, showGrey);
		}
		super.saveState(memento);
	}

	@Override
	public void createPartControl(Composite parent) {
		canvas = new Canvas(parent, SWT.DOUBLE_BUFFERED);
		canvas.addPaintListener(this);
		addKeyListener();
		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(canvas, HelpContextIds.HISTOGRAM_VIEW);
		makeActions();
		installListeners(parent);
		contributeToActionBars();
		updateActions();
		canvas.redraw();
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(redAction);
		manager.add(greenAction);
		manager.add(blueAction);
		manager.add(greyAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(redAction);
		manager.add(greenAction);
		manager.add(blueAction);
		manager.add(greyAction);
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		redAction = new Action(
				Messages.getString("HistogramView.Red"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$

			@Override
			public void run() {
				showRed = !showRed;
				refresh();
			}
		};
		redAction.setToolTipText(Messages
				.getString("HistogramView.red_tooltip")); //$NON-NLS-1$
		redAction.setImageDescriptor(Icons.redDot.getDescriptor());
		redAction.setChecked(showRed);
		greenAction = new Action(
				Messages.getString("HistogramView.green"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$

			@Override
			public void run() {
				showGreen = !showGreen;
				refresh();
			}
		};
		greenAction.setToolTipText(Messages
				.getString("HistogramView.green_tooltip")); //$NON-NLS-1$
		greenAction.setImageDescriptor(Icons.greenDot.getDescriptor());
		greenAction.setChecked(showGreen);
		blueAction = new Action(
				Messages.getString("HistogramView.blue"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$

			@Override
			public void run() {
				showBlue = !showBlue;
				refresh();
			}
		};
		blueAction.setToolTipText(Messages
				.getString("HistogramView.blue_tooltip")); //$NON-NLS-1$
		blueAction.setImageDescriptor(Icons.blueDot.getDescriptor());
		blueAction.setChecked(showBlue);
		greyAction = new Action(
				Messages.getString("HistogramView.brightness"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$

			@Override
			public void run() {
				showGrey = !showGrey;
				refresh();
			}
		};
		greyAction.setToolTipText(Messages
				.getString("HistogramView.brightness_tooltip")); //$NON-NLS-1$
		greyAction.setImageDescriptor(Icons.grayscale.getDescriptor());
		greyAction.setChecked(showGrey);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */

	@Override
	public void setFocus() {
		canvas.setFocus();
	}

	public void paintControl(PaintEvent e) {
		Rectangle clientArea = canvas.getClientArea();
		Display display = e.display;
		GC gc = e.gc;
		gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.fillRectangle(clientArea);
		if (mxvalue > 0) {
			int mx = mxvalue;
			int[] greys = null;
			if (showGrey) {
				// 0.299R+0.587G+0.114B
				greys = new int[256];
				for (int i = 0; i < 256; i++)
					mx = Math.max(mx,  greys[i] = (reds[i] * 306 + greens[i] * 601 + blues[i] * 117) >> 10);
			}
			Transform transform = new Transform(display);
			transform.translate(0, clientArea.height);
			float scaleX = clientArea.width / 256f;
			float scaleY = -(float) clientArea.height / mx;
			transform.scale(scaleX, scaleY);
			gc.setTransform(transform);
			if (showGrey)
				drawCurve(gc, greys, display.getSystemColor((showRed
						|| showGreen || showBlue) ? SWT.COLOR_DARK_GRAY
						: SWT.COLOR_BLACK), true, false);
			if (showRed)
				drawCurve(gc, reds, display.getSystemColor(SWT.COLOR_RED),
						!showGrey, true);
			if (showGreen)
				drawCurve(gc, greens, display.getSystemColor(SWT.COLOR_GREEN),
						!showGrey, true);
			if (showBlue)
				drawCurve(gc, blues, display.getSystemColor(SWT.COLOR_BLUE),
						!showGrey, true);
			gc.setTransform(null);
			transform.dispose();
		} else {
			String text = count > 1 ? Messages
					.getString("HistogramView.multiple_images_selected") //$NON-NLS-1$
					: Messages.getString("HistogramView.nothing_selected"); //$NON-NLS-1$
			gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
			Point tx = gc.textExtent(text);
			gc.drawText(text, (clientArea.width - tx.x) / 2,
					(clientArea.height - tx.y) / 2, true);
		}
		gc.setAlpha(255);
		gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
		gc.drawLine(clientArea.width / 3, 0, clientArea.width / 3,
				clientArea.height);
		gc.drawLine(2 * clientArea.width / 3, 0, 2 * clientArea.width / 3,
				clientArea.height);
	}

	private static void drawCurve(GC gc, int[] values, Color color, boolean fill,
			boolean line) {
		int[] points = new int[516];
		int p = 3;
		points[p++] = values[0] + values[1] >> 1;
		for (int i = 1; i < 255; i++) {
			points[p++] = i;
			points[p++] = values[i - 1] + (values[i] << 1) + values[i + 1] >> 2;
		}
		points[512] = 255;
		points[513] = values[254] + values[255] >> 1;
		if (fill) {
			gc.setAlpha(64);
			gc.setBackground(color);
			points[514] = 255;
			gc.fillPolygon(points);
			gc.setAlpha(255);
		}
		if (line) {
			gc.setForeground(color);
			int[] copy = new int[512];
			System.arraycopy(points, 0, copy, 0,
			                 Math.min(points.length, 512));
			gc.drawPolyline(copy);
		}
	}

	@Override
	public boolean collectionChanged() {
		return false;
	}

	@Override
	public boolean selectionChanged() {
		return false;
	}

	@Override
	public boolean assetsChanged() {
		Asset previous = currentItem;
		AssetSelection selectedAssets = getNavigationHistory().getSelectedAssets();
		count = selectedAssets.size();
		currentItem = (count == 1) ? selectedAssets.get(0) : null;
		if (previous != currentItem) {
			mxvalue = 0;
			if (currentItem != null) {
				for (int i = 0; i < 256; i++)
					reds[i] = greens[i] = blues[i] = 0;
				ImageData data = getImage(currentItem).getImageData();
				PaletteData palette = data.palette;
				int yinc = 1;
				int xinc = 1;
				long size = (long) data.height * data.width;
				if (size > MINTHUMBNAILSIZE) {
					yinc = (int) (Math.sqrt(size / (double) MINTHUMBNAILSIZE) + 0.5d);
					xinc = (int) (size / ((double) MINTHUMBNAILSIZE * yinc));
				}
				for (int y = 0; y < data.height; y += yinc) {
					for (int x = 0; x < data.width; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						++reds[rgb.red];
						++greens[rgb.green];
						++blues[rgb.blue];
					}
				}
				for (int i = 0; i < 256; i++)
					mxvalue = Math.max(mxvalue,
							Math.max(reds[i], Math.max(greens[i], blues[i])));
			}
			return true;
		}
		return false;
	}

	public Control getControl() {
		return canvas;
	}

	@Override
	public void refresh() {
		canvas.redraw();
	}

	@Override
	public void updateActions() {
		if (redAction == null)
			return;
		boolean enabled = mxvalue > 0;
		redAction.setEnabled(enabled);
		greenAction.setEnabled(enabled);
		blueAction.setEnabled(enabled);
		greyAction.setEnabled(enabled);
		updateActions(-1, -1);
	}

	public ISelection getSelection() {
		return (currentItem != null) ? new StructuredSelection(currentItem)
				: StructuredSelection.EMPTY;
	}

	public void setSelection(ISelection selection) {
		// do nothing
	}

	@Override
	protected void updateStatusLine() {
		// do nothing
	}
}