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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;

public class HistogramView extends BasicView {
	public static final String ID = "com.bdaum.zoom.ui.views.HistogramView"; //$NON-NLS-1$
	private static final String SHOW_RED = "showRed"; //$NON-NLS-1$
	private static final String SHOW_GREEN = "showGreen"; //$NON-NLS-1$
	private static final String SHOW_BLUE = "showBlue"; //$NON-NLS-1$
	private static final String SHOW_GREY = "showGrey"; //$NON-NLS-1$
	private static final String WEIGHTED = "weighted"; //$NON-NLS-1$
	private static final String DARK = "dark"; //$NON-NLS-1$
	private Canvas canvas;
	private Asset currentItem;
	private Action redAction, greenAction, blueAction, greyAction, centerAction, backgroundAction, trigger;
	protected boolean showRed = true;
	protected boolean showGrey = true;
	protected boolean showBlue = true;
	protected boolean showGreen = true;
	private boolean weighted, dark;
	private int count;
	private Color darkBg, brightBg;
	private CaptionManager captionManager = new CaptionManager();
	private HistogramManager histogramManager = new HistogramManager();

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
			bool = memento.getBoolean(DARK);
			dark = bool != null && bool;
		}
		captionManager.init(memento);
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			memento.putBoolean(SHOW_RED, showRed);
			memento.putBoolean(SHOW_GREEN, showGreen);
			memento.putBoolean(SHOW_BLUE, showBlue);
			memento.putBoolean(SHOW_GREY, showGrey);
			memento.putBoolean(WEIGHTED, weighted);
			memento.putBoolean(DARK, dark);
			captionManager.saveState(memento);
		}
		super.saveState(memento);
	}

	@Override
	public void createPartControl(Composite parent) {
		darkBg = new Color(parent.getDisplay(), 32, 32, 32);
		brightBg = new Color(parent.getDisplay(), 240, 240, 232);
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		layout.marginTop = 10;
		composite.setLayout(layout);
		canvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
		captionManager.setTarget(canvas);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		canvas.addListener(SWT.Paint, this);
		canvas.addListener(SWT.MouseUp, this);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(canvas, HelpContextIds.HISTOGRAM_VIEW);
		makeActions();
		installListeners();
		contributeToActionBars();
		updateActions(true);
		canvas.redraw();
	}

	@Override
	public void handleEvent(Event e) {
		if (e.type == SWT.Paint) {
			paintControl(e);
			return;
		}
		captionManager.handleEvent(e);
		if (e.doit)
			super.handleEvent(e);
	}

	@Override
	public void dispose() {
		super.dispose();
		darkBg.dispose();
		brightBg.dispose();
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(backgroundAction);
		manager.add(centerAction);
		manager.add(new Separator());
		manager.add(redAction);
		manager.add(greenAction);
		manager.add(blueAction);
		manager.add(greyAction);
		manager.add(new Separator());
		captionManager.fillMenu(manager);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(centerAction);
		manager.add(new Separator());
		manager.add(redAction);
		manager.add(greenAction);
		manager.add(blueAction);
		manager.add(greyAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void makeActions() {
		super.makeActions();
		backgroundAction = new Action(Messages.getString("HistogramView.dark_bg"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				dark = !dark;
				setToolTipText(dark ? Messages.getString("HistogramView.make_bright") //$NON-NLS-1$
						: Messages.getString("HistogramView.make_dark")); //$NON-NLS-1$
				histogramManager.recalculate(currentItem == null ? null : getImage(currentItem), weighted);
				refresh();
			}
		};
		backgroundAction.setImageDescriptor(Icons.bw.getDescriptor());
		centerAction = new Action(Messages.getString("HistogramView.centric"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				weighted = !weighted;
				setImageDescriptor(weighted ? Icons.centric.getDescriptor() : Icons.integral.getDescriptor());
				setToolTipText(weighted ? Messages.getString("HistogramView.centric_tooltip") //$NON-NLS-1$
						: Messages.getString("HistogramView.integral")); //$NON-NLS-1$
				histogramManager.recalculate(currentItem == null ? null : getImage(currentItem), weighted);
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
				trigger = this;
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
				trigger = this;
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
				trigger = this;
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
				trigger = this;
				refresh();
			}
		};
		greyAction.setImageDescriptor(showGrey ? Icons.grayscale.getDescriptor() : Icons.paleGrayscale.getDescriptor());
		greyAction.setToolTipText(showGrey ? Messages.getString("HistogramView.brightness_tooltip") //$NON-NLS-1$
				: Messages.getString("HistogramView.grey_off")); //$NON-NLS-1$
		greyAction.setChecked(showGrey);

		captionManager.makeActions();
	}

	@Override
	public void setFocus() {
		canvas.setFocus();
	}

	private void paintControl(Event e) {
		GC gc = e.gc;
		Rectangle clientArea = canvas.getClientArea();
		Display display = canvas.getDisplay();
		gc.setBackground(dark ? darkBg : brightBg);
		gc.fillRectangle(clientArea);
		int width = clientArea.width;
		int height = clientArea.height;
		if (currentItem != null) {
			histogramManager.paint(gc, 0, 0, width, height, dark, false, showGrey, showRed, showGreen, showBlue);
			captionManager.handleOverlay(e.gc, 0, 0, clientArea.width, clientArea.height, 112, 180);
		} else {
			String text = count > 1 ? Messages.getString("HistogramView.multiple_images_selected") //$NON-NLS-1$
					: Messages.getString("HistogramView.nothing_selected"); //$NON-NLS-1$
			gc.setForeground(display.getSystemColor(dark ? SWT.COLOR_GRAY : SWT.COLOR_DARK_GRAY));
			Point tx = gc.textExtent(text);
			gc.drawText(text, (width - tx.x) / 2, (height - tx.y) / 2, true);
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
			histogramManager.recalculate(currentItem == null ? null : getImage(currentItem), weighted);
			captionManager.updateCaption(currentItem);
			return true;
		}
		return false;
	}

	public Control getControl() {
		return canvas;
	}

	@Override
	public void refresh() {
		captionManager.refresh();
		canvas.redraw();
		updateActions(false);
	}

	@Override
	public void updateActions(boolean force) {
		if (redAction != null && (isVisible() || force)) {
			boolean enabled = currentItem != null;
			redAction.setEnabled(enabled);
			greenAction.setEnabled(enabled);
			blueAction.setEnabled(enabled);
			greyAction.setEnabled(enabled);
			centerAction.setEnabled(enabled);
			if (enabled && !showBlue && !showGreen && !showRed && !showGrey) {
				if (trigger == null || trigger == greyAction) {
					redAction.run();
					greenAction.run();
					blueAction.run();
				} else
					greyAction.run();
			}
			updateActions(-1, -1);
		}
	}

	public ISelection getSelection() {
		return currentItem != null ? new StructuredSelection(currentItem) : StructuredSelection.EMPTY;
	}

	public void setSelection(ISelection selection) {
		// do nothing
	}

	@Override
	protected void updateStatusLine() {
		// do nothing
	}
}