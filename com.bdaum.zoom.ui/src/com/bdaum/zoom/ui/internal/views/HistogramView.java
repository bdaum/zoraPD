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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.dialogs.ConfigureCaptionDialog;

@SuppressWarnings("restriction")
public class HistogramView extends BasicView implements PaintListener {
	public static final String ID = "com.bdaum.zoom.ui.views.HistogramView"; //$NON-NLS-1$
	private static final int MINTHUMBNAILSIZE = 160 * 120;
	private static final String SHOW_RED = "showRed"; //$NON-NLS-1$
	private static final String SHOW_GREEN = "showGreen"; //$NON-NLS-1$
	private static final String SHOW_BLUE = "showBlue"; //$NON-NLS-1$
	private static final String SHOW_GREY = "showGrey"; //$NON-NLS-1$
	private static final String WEIGHTED = "weighted"; //$NON-NLS-1$
	private Canvas canvas;
	private Asset currentItem;
	private int[] reds = new int[256];
	private int[] greens = new int[256];
	private int[] blues = new int[256];
	private int[] greys = new int[256];
	private int mxvalue;
	private Action redAction, greenAction, blueAction, greyAction, centerAction;
	protected boolean showRed = true;
	protected boolean showGrey = true;
	protected boolean showBlue = true;
	protected boolean showGreen = true;
	private boolean weighted;
	private int count;
	private Label caption;
	private String template;
	private Action configureAction;
	private int alignment;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			Boolean bool = memento.getBoolean(SHOW_RED);
			showRed = bool != null && bool;
			bool = memento.getBoolean(SHOW_GREEN);
			showGreen = bool != null && bool;
			bool = memento.getBoolean(SHOW_BLUE);
			showBlue = bool != null && bool;
			bool = memento.getBoolean(SHOW_GREY);
			showGrey = bool != null && bool;
			bool = memento.getBoolean(WEIGHTED);
			weighted = bool != null && bool;
			template = memento.getString(TEMPLATE);
			if (template == null)
				template = DEFAULTTEMPLATE;
			Integer integer = memento.getInteger(ALIGNMENT);
			alignment = integer != null ? integer : SWT.LEFT;
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			memento.putBoolean(SHOW_RED, showRed);
			memento.putBoolean(SHOW_GREEN, showGreen);
			memento.putBoolean(SHOW_BLUE, showBlue);
			memento.putBoolean(SHOW_GREY, showGrey);
			memento.putBoolean(SHOW_GREY, weighted);
			if (template != null)
				memento.putString(TEMPLATE, template);
			memento.putInteger(ALIGNMENT, alignment);
		}
		super.saveState(memento);
	}

	@SuppressWarnings("unused")
	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.marginTop = 10;
		composite.setLayout(layout);
		canvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		canvas.addPaintListener(this);
		new Label(composite, SWT.NONE);
		caption = new Label(composite, SWT.NONE);
		caption.setLayoutData(new GridData(SWT.FILL, SWT.END, true, false));
		caption.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				configureAction.run();
			}
		});
		PlatformUI.getWorkbench().getHelpSystem().setHelp(canvas, HelpContextIds.HISTOGRAM_VIEW);
		makeActions();
		installListeners(parent);
		contributeToActionBars();
		updateActions(true);
		canvas.redraw();
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(centerAction);
		manager.add(redAction);
		manager.add(greenAction);
		manager.add(blueAction);
		manager.add(greyAction);
		manager.add(new Separator());
		manager.add(configureAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(centerAction);
		manager.add(redAction);
		manager.add(greenAction);
		manager.add(blueAction);
		manager.add(greyAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		centerAction = new Action(Messages.getString("HistogramView.centric"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				weighted = !weighted;
				setImageDescriptor(weighted ? Icons.centric.getDescriptor() : Icons.integral.getDescriptor());
				setToolTipText(weighted ? Messages.getString("HistogramView.centric_tooltip") //$NON-NLS-1$
						: Messages.getString("HistogramView.integral")); //$NON-NLS-1$
				recalculate();
				refresh();
			}
		};
		centerAction.setImageDescriptor(weighted ? Icons.centric.getDescriptor() : Icons.integral.getDescriptor());
		centerAction.setToolTipText(weighted ? Messages.getString("HistogramView.centric_tooltip") //$NON-NLS-1$
				: Messages.getString("HistogramView.integral")); //$NON-NLS-1$
		centerAction.setChecked(weighted);
		redAction = new Action(Messages.getString("HistogramView.Red"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				showRed = !showRed;
				setImageDescriptor(showRed ? Icons.redDot.getDescriptor() : Icons.paleRedDot.getDescriptor());
				setToolTipText(showRed ? Messages.getString("HistogramView.red_tooltip") //$NON-NLS-1$
						: Messages.getString("HistogramView.red_off")); //$NON-NLS-1$
				refresh();
			}
		};
		redAction.setImageDescriptor(showRed ? Icons.redDot.getDescriptor() : Icons.paleRedDot.getDescriptor());
		redAction.setToolTipText(showRed ? Messages.getString("HistogramView.red_tooltip") //$NON-NLS-1$
				: Messages.getString("HistogramView.red_off")); //$NON-NLS-1$
		redAction.setChecked(showRed);
		greenAction = new Action(Messages.getString("HistogramView.green"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				showGreen = !showGreen;
				setImageDescriptor(showGreen ? Icons.greenDot.getDescriptor() : Icons.paleGreenDot.getDescriptor());
				setToolTipText(showGreen ? Messages.getString("HistogramView.green_tooltip") //$NON-NLS-1$
						: Messages.getString("HistogramView.green_off")); //$NON-NLS-1$
				refresh();
			}
		};
		greenAction.setImageDescriptor(showGreen ? Icons.greenDot.getDescriptor() : Icons.paleGreenDot.getDescriptor());
		greenAction.setToolTipText(showGreen ? Messages.getString("HistogramView.green_tooltip") //$NON-NLS-1$
				: Messages.getString("HistogramView.green_off")); //$NON-NLS-1$
		greenAction.setChecked(showGreen);
		blueAction = new Action(Messages.getString("HistogramView.blue"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				showBlue = !showBlue;
				setImageDescriptor(showBlue ? Icons.blueDot.getDescriptor() : Icons.paleBlueDot.getDescriptor());
				setToolTipText(showBlue ? Messages.getString("HistogramView.blue_tooltip") //$NON-NLS-1$
						: Messages.getString("HistogramView.blue_off")); //$NON-NLS-1$
				refresh();
			}
		};
		blueAction.setImageDescriptor(showBlue ? Icons.blueDot.getDescriptor() : Icons.paleBlueDot.getDescriptor());
		blueAction.setToolTipText(showBlue ? Messages.getString("HistogramView.blue_tooltip") //$NON-NLS-1$
				: Messages.getString("HistogramView.blue_off")); //$NON-NLS-1$
		blueAction.setChecked(showBlue);
		greyAction = new Action(Messages.getString("HistogramView.brightness"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				showGrey = !showGrey;
				setImageDescriptor(showGrey ? Icons.grayscale.getDescriptor() : Icons.paleGrayscale.getDescriptor());
				setToolTipText(showGrey ? Messages.getString("HistogramView.brightness_tooltip") //$NON-NLS-1$
						: Messages.getString("HistogramView.grey_off")); //$NON-NLS-1$
				refresh();
			}
		};
		greyAction.setImageDescriptor(showGrey ? Icons.grayscale.getDescriptor() : Icons.paleGrayscale.getDescriptor());
		greyAction.setToolTipText(showGrey ? Messages.getString("HistogramView.brightness_tooltip") //$NON-NLS-1$
				: Messages.getString("HistogramView.grey_off")); //$NON-NLS-1$
		greyAction.setChecked(showGrey);
		configureAction = new Action(Messages.getString("HistogramView.config_caption")) { //$NON-NLS-1$
			@Override
			public void run() {
				ConfigureCaptionDialog dialog = new ConfigureCaptionDialog(getSite().getShell(), template, alignment,
						currentItem);
				if (dialog.open() == ConfigureCaptionDialog.OK) {
					template = dialog.getTemplate();
					alignment = dialog.getAlignment();
					updateCaption();
				}
			}
		};
		configureAction.setToolTipText(Messages.getString("HistogramView.configure_caption_tooltip")); //$NON-NLS-1$
	}

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
			int gmax = 0;
			int gavg = 0;
			int shadows = 0;
			int highlights = 0;
			for (int i = 0; i < 256; i++) {
				greys[i] = (reds[i] * 306 + greens[i] * 601 + blues[i] * 117) >> 10; // 0.299R+0.587G+0.114B
				gavg += greys[i];
				if (i > 0)
					gmax = Math.max(gmax, greys[i - 1] + greys[i]);
			}
			gmax /= 2;
			gavg /= 256;
			for (int i = 0; i < 25; i++)
				shadows = Math.max(shadows, greys[i] + greys[i + 1]);
			shadows /= 2;
			for (int i = 230; i < 255; i++)
				highlights = Math.max(highlights, greys[i] + greys[i + 1]);
			highlights /= 2;
			String message = null;
			if (shadows <= gavg * 0.05) {
				if (highlights > gavg)
					message = Messages.getString("HistogramView.overexpose"); //$NON-NLS-1$
				else if (highlights < gavg * 0.05)
					message = Messages.getString("HistogramView.enhance_contrast"); //$NON-NLS-1$
				else
					message = Messages.getString("HistogramView.darken_shadows"); //$NON-NLS-1$
			} else if (highlights <= gavg * 0.05) {
				if (shadows > gavg)
					message = Messages.getString("HistogramView.underexposed"); //$NON-NLS-1$
				else
					message = Messages.getString("HistogramView.lighten_highlights"); //$NON-NLS-1$
			} else if (highlights > gavg && shadows > gavg)
				message = Messages.getString("HistogramView.compress_contrast"); //$NON-NLS-1$
			Transform transform = new Transform(display);
			transform.translate(0, clientArea.height);
			transform.scale((clientArea.width) / 256f, -(float) clientArea.height / mxvalue);
			gc.setTransform(transform);
			if (showGrey)
				drawCurve(gc, greys,
						display.getSystemColor(
								(showRed || showGreen || showBlue) ? SWT.COLOR_DARK_GRAY : SWT.COLOR_BLACK),
						true, false);
			if (showRed)
				drawCurve(gc, reds, display.getSystemColor(SWT.COLOR_RED), !showGrey, true);
			if (showGreen)
				drawCurve(gc, greens, display.getSystemColor(SWT.COLOR_GREEN), !showGrey, true);
			if (showBlue)
				drawCurve(gc, blues, display.getSystemColor(SWT.COLOR_BLUE), !showGrey, true);
			gc.setTransform(null);
			transform.dispose();
			if (message != null) {
				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_DARK_CYAN));
				TextLayout textLayout = new TextLayout(gc.getDevice());
				textLayout.setText(message);
				textLayout.draw(gc, clientArea.width - textLayout.getBounds().width - 10, 15);
				textLayout.dispose();
			}
		} else {
			String text = count > 1 ? Messages.getString("HistogramView.multiple_images_selected") //$NON-NLS-1$
					: Messages.getString("HistogramView.nothing_selected"); //$NON-NLS-1$
			gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
			Point tx = gc.textExtent(text);
			gc.drawText(text, (clientArea.width - tx.x) / 2, (clientArea.height - tx.y) / 2, true);
		}
		gc.setAlpha(255);
		gc.setForeground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
		gc.drawLine(clientArea.width / 3, 0, clientArea.width / 3, clientArea.height);
		gc.drawLine(2 * clientArea.width / 3, 0, 2 * clientArea.width / 3, clientArea.height);
	}

	private static void drawCurve(GC gc, int[] values, Color color, boolean fill, boolean line) {
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
			points[514] = 255;
			gc.setAlpha(64);
			gc.setBackground(color);
			gc.fillPolygon(points);
			gc.setAlpha(255);
		}
		if (line) {
			gc.setForeground(color);
			int[] copy = new int[512];
			System.arraycopy(points, 2, copy, 0, Math.min(points.length, 512));
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
			recalculate();
			updateCaption();
			return true;
		}
		return false;
	}

	protected void updateCaption() {
		caption.setText(currentItem == null || template == null ? "" //$NON-NLS-1$
				: Utilities.evaluateTemplate(template, Constants.TH_ALL, "", null, -1, -1, -1, null, currentItem, //$NON-NLS-1$
						"", Integer.MAX_VALUE, false)); //$NON-NLS-1$
		caption.setAlignment(alignment);
	}

	protected void recalculate() {
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
			if (weighted) {
				int h1 = data.height * 12 / 100;
				int h2 = data.height * 30 / 100;
				int h3 = data.height * 70 / 100;
				int h4 = data.height * 88 / 100;
				int w1 = data.width * 12 / 100;
				int w2 = data.width * 30 / 100;
				int w3 = data.width * 70 / 100;
				int w4 = data.width * 88 / 100;
				for (int y = h1; y < h2; y += yinc)
					for (int x = w1; x < w4; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						++reds[rgb.red];
						++greens[rgb.green];
						++blues[rgb.blue];
					}
				for (int y = h2; y < h3; y += yinc) {
					for (int x = w1; x < w2; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						++reds[rgb.red];
						++greens[rgb.green];
						++blues[rgb.blue];
					}
					for (int x = w2; x < w3; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						reds[rgb.red] += 2;
						greens[rgb.green] += 2;
						blues[rgb.blue] += 2;
					}
					for (int x = w3; x < w4; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						++reds[rgb.red];
						++greens[rgb.green];
						++blues[rgb.blue];
					}
				}
				for (int y = h3; y < h4; y += yinc)
					for (int x = w1; x < w4; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						++reds[rgb.red];
						++greens[rgb.green];
						++blues[rgb.blue];
					}
			} else
				for (int y = 0; y < data.height; y += yinc)
					for (int x = 0; x < data.width; x += xinc) {
						RGB rgb = palette.getRGB(data.getPixel(x, y));
						++reds[rgb.red];
						++greens[rgb.green];
						++blues[rgb.blue];
					}
			for (int i = 0; i < 255; i++)
				mxvalue = Math.max(mxvalue,
						Math.max(reds[i] + reds[i + 1], Math.max(greens[i] + greens[i + 1], blues[i] + blues[i])));
			mxvalue /= 2;
		}
	}

	public Control getControl() {
		return canvas;
	}

	@Override
	public void refresh() {
		canvas.redraw();
	}

	@Override
	public void updateActions(boolean force) {
		if (redAction != null && (viewActive || force)) {
			boolean enabled = mxvalue > 0;
			redAction.setEnabled(enabled);
			greenAction.setEnabled(enabled);
			blueAction.setEnabled(enabled);
			greyAction.setEnabled(enabled);
			centerAction.setEnabled(enabled);
			updateActions(-1, -1);
		}
	}

	public ISelection getSelection() {
		return (currentItem != null) ? new StructuredSelection(currentItem) : StructuredSelection.EMPTY;
	}

	public void setSelection(ISelection selection) {
		// do nothing
	}

	@Override
	protected void updateStatusLine() {
		// do nothing
	}
}