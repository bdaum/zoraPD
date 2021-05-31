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
 * (c) 2009-2021 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.views;

import java.awt.geom.Rectangle2D;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.BagChange;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.IFrameListener;
import com.bdaum.zoom.ui.IFrameProvider;
import com.bdaum.zoom.ui.IZoomActionConstants;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.ColorFilterDialog;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

public class PreviewView extends ImageView implements IFrameListener {

	public static final String ID = "com.bdaum.zoom.ui.views.PreviewView"; //$NON-NLS-1$
	private static final String CUE = "cue"; //$NON-NLS-1$
	private static final String BW = "bs"; //$NON-NLS-1$
	private static final String HISTO = "histo"; //$NON-NLS-1$
	protected Canvas canvas;
	private Asset currentItem, selectedItem;
	private Action cueToggleAction, bwToggleAction, showHistogramAction, setFilterAction;
	private boolean bw, rightClick, cue;
	private RGB filter;
	private Image bwImage;
	private AssetSelection assetSelection = AssetSelection.EMPTY;
	private Rectangle2D currentFrame = new Rectangle2D.Double(0d, 0d, 1d, 1d);
	private CaptionManager captionManager = new CaptionManager();
	private boolean showHisto = true;
	private HistogramManager histogramManager = new HistogramManager();
	private Rectangle histoRegion;
	private TimerTask task;
	private Timer timer = new Timer();
	private int clicks;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if (memento != null) {
			Boolean b = memento.getBoolean(CUE);
			cue = b != null && b;
			b = memento.getBoolean(BW);
			bw = b != null && b;
			b = memento.getBoolean(HISTO);
			showHisto = b != null && b;
		}
		captionManager.init(memento);
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putBoolean(CUE, cue);
		memento.putBoolean(BW, bw);
		memento.putBoolean(HISTO, showHisto);
		captionManager.saveState(memento);
	}

	@Override
	public void createPartControl(Composite parent) {
		filter = StringConverter.asRGB(Platform.getPreferencesService().getString(UiActivator.PLUGIN_ID,
				PreferenceConstants.BWFILTER, null, null), new RGB(64, 128, 64));
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);
		canvas = new Canvas(composite, SWT.DOUBLE_BUFFERED);
		captionManager.setTarget(canvas);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(canvas, HelpContextIds.PREVIEW_VIEW);
		canvas.addListener(SWT.Paint, this);
		canvas.addListener(SWT.MouseUp, this);
		canvas.redraw();
		addKeyListener();
		addExplanationListener(true);
		makeActions(getViewSite().getActionBars());
		installListeners();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		addDragDropSupport(false, false, true);
		installHoveringController();
		Ui.getUi().getFrameManager().addFrameListener(this);
		updateActions(true);
	}

	@Override
	public void handleEvent(Event e) {
		if (e.type == SWT.Paint) {
			paintControl(e);
			return;
		}
		if (e.type == SWT.MouseUp) {
			if ((e.stateMask & SWT.BUTTON3) != 0)
				rightClick = true;
			else if ((e.stateMask & SWT.BUTTON1) != 0 && histoRegion != null && histoRegion.contains(e.x, e.y)) {
				if (rightClick) {
					rightClick = false;
					return;
				}
				clicks = e.count;
				if (task != null) {
					task.cancel();
					task = null;
				}
				if (clicks == 1) {
					task = new TimerTask() {
						@Override
						public void run() {
							if (!canvas.isDisposed())
								canvas.getDisplay().asyncExec(() -> {
									try {
										if (!canvas.isDisposed() && clicks == 1)
											popupHistogram();
									} catch (PartInitException e) {
										// should not happen
									}
								});
						}
					};
					timer.schedule(task, 300);
					e.doit = false;
				}
			}
		}
		if (e.doit)
			captionManager.handleEvent(e);
		if (e.doit)
			super.handleEvent(e);
	}
	
	private void popupHistogram() throws PartInitException {
		HistogramView hView = (HistogramView) getSite().getPage().showView(HistogramView.ID);
		if (hView != null)
			hView.setBw(bw);
	}

	@Override
	protected void makeActions(IActionBars bars) {
		super.makeActions(bars);
		cueToggleAction = new Action(Messages.getString("PreviewView.cue"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				cue = !cue;
				updateActions(false);
			}
		};
		cueToggleAction.setImageDescriptor(Icons.cue.getDescriptor());
		cueToggleAction.setToolTipText(Messages.getString("PreviewView.cue_tooltip")); //$NON-NLS-1$
		cueToggleAction.setChecked(cue);

		bwToggleAction = new Action(Messages.getString("PreviewView.as_grayscale"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void runWithEvent(Event event) {
				if ((event.stateMask & SWT.CTRL) != 0)
					setFilterAction.run();
				else {
					bw = !bw;
					refresh();
				}
				super.runWithEvent(event);
			}
		};
		bwToggleAction.setImageDescriptor(Icons.bw.getDescriptor());
		bwToggleAction.setToolTipText(Messages.getString("PreviewView.switch_preview")); //$NON-NLS-1$

		setFilterAction = new Action(Messages.getString("PreviewView.set_bw_filter")) { //$NON-NLS-1$
			@Override
			public void run() {
				ColorFilterDialog dialog = new ColorFilterDialog(getSite().getShell());
				dialog.setRGB(filter);
				dialog.setText(Messages.getString("PreviewView.set_bw_filter")); //$NON-NLS-1$
				if (dialog.open() == ColorFilterDialog.OK) {
					RGB rgb = dialog.getRgb();
					if (rgb.red + rgb.green + rgb.blue == 0)
						rgb.red = rgb.green = rgb.blue = 1;
					BatchUtilities.putPreferences(InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID),
							PreferenceConstants.BWFILTER, StringConverter.asString(filter = rgb));
					setFilterButtonColor();
				}
				if (bw)
					refresh();
			}
		};
		setFilterButtonColor();
		setFilterAction.setToolTipText(Messages.getString("PreviewView.set_bw_filter_tooltip")); //$NON-NLS-1$
		showHistogramAction = new Action(Messages.getString("PreviewView.histo"), IAction.AS_CHECK_BOX) { //$NON-NLS-1$
			@Override
			public void run() {
				showHisto = !showHisto;
				refresh();
			}
		};
		showHistogramAction.setImageDescriptor(Icons.histo.getDescriptor());
		showHistogramAction.setToolTipText(Messages.getString("PreviewView.histo_tooltip")); //$NON-NLS-1$
		captionManager.makeActions();
	}

	private void setFilterButtonColor() {
		setFilterAction.setImageDescriptor(new ImageDescriptor() {
			@Override
			public ImageData getImageData() {
				Display display = getSite().getShell().getDisplay();
				Image image = new Image(display, 16, 16);
				GC gc = new GC(image);
				gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
				gc.fillRectangle(image.getBounds());
				Color color = new Color(display, filter);
				gc.setBackground(color);
				gc.fillOval(1, 1, 14, 14);
				ImageData data = image.getImageData();
				color.dispose();
				gc.dispose();
				image.dispose();
				return data;
			}
		});
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (RGB.class.equals(adapter))
			return bw ? filter : null;
		if (Boolean.class.equals(adapter))
			return Boolean.valueOf(bw);
		return super.getAdapter(adapter);
	}

	@Override
	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(cueToggleAction);
		manager.add(bwToggleAction);
		manager.add(setFilterAction);
		manager.add(showHistogramAction);
		manager.add(new Separator());
		manager.add(viewImageAction);
		manager.add(editAction);
		manager.add(editWithAction);
		manager.add(new Separator());
		manager.add(addVoiceNoteAction);
		manager.add(playVoiceNoteAction);
		manager.add(new Separator());
		manager.add(rotateRightAction);
		manager.add(rotateLeftAction);
		manager.add(new Separator());
		manager.add(ratingAction);
		manager.add(colorCodeAction);
		manager.add(new Separator());
		manager.add(showInFolderAction);
		manager.add(showInTimeLineAction);
		manager.add(new Separator());
		manager.add(addBookmarkAction);
		manager.add(refreshAction);
		manager.add(deleteAction);
		manager.add(new Separator());
		captionManager.fillMenu(manager);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void fillContextMenu(IMenuManager manager) {
		updateActions(true);
		boolean readOnly = dbIsReadonly();
		captionManager.fillMenu(manager);
		manager.add(new Separator(IZoomActionConstants.MB_SUBMENUS));
		fillEditAndSearchGroup(manager, readOnly);
		fillVoiceNote(manager, readOnly);
		fillMetaData(manager, readOnly);
		fillRelationsGroup(manager);
		manager.add(new Separator(IZoomActionConstants.MB_SUBMENUS));
		fillRotateGroup(manager, readOnly);
		fillShowAndDeleteGroup(manager, readOnly);
		super.fillAdditions(manager);
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {
		manager.add(cueToggleAction);
		manager.add(bwToggleAction);
		manager.add(showHistogramAction);
		manager.add(new Separator());
		manager.add(playVoiceNoteAction);
		manager.add(viewImageAction);
		manager.add(editAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void hookDoubleClickAction() {
		canvas.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				fireSelectionChanged();
				event.data = PreviewView.this;
				viewImageAction.runWithEvent(event);
			}
		});
	}

	@Override
	public void updateActions(boolean force) {
		if (isVisible() || force) {
			boolean enabled = selectedItem != null;
			bwToggleAction.setEnabled(enabled);
			cueToggleAction.setEnabled(enabled);
			cueToggleAction.setImageDescriptor(cue ? Icons.cue.getDescriptor() : Icons.nocue.getDescriptor());
			setFilterAction.setEnabled(enabled);
			showHistogramAction.setEnabled(enabled);
			super.updateActions(force);
		}
	}

	protected void fireSelectionChanged() {
		SelectionChangedEvent selectionChangedEvent = new SelectionChangedEvent(this, getSelection());
		for (Object object : listeners.getListeners())
			((ISelectionChangedListener) object).selectionChanged(selectionChangedEvent);
		updateActions(false);
	}

	@Override
	public void setFocus() {
		if (canvas != null)
			canvas.setFocus();
	}

	public void paintControl(Event e) {
		GC gc = e.gc;
		Rectangle clientArea = canvas.getClientArea();
		int width = clientArea.width;
		int height = clientArea.height;
		if (currentItem == null) {
			gc.setBackground(canvas.getBackground());
			gc.fillRectangle(clientArea);
			String text = Messages.getString("PreviewView.nothing_selected"); //$NON-NLS-1$
			gc.setForeground(canvas.getForeground());
			Point tx = gc.textExtent(text);
			gc.drawText(text, (width - tx.x) / 2, (height - tx.y) / 2, true);
		} else {
			Image image = bw ? getBwImage(currentItem, filter) : getImage(currentItem);
			Rectangle bounds = image.getBounds();
			double xscale = ((float) width) / bounds.width;
			double yscale = ((float) height) / bounds.height;
			double scale = Math.min(xscale, yscale);
			int targetWidth = (int) (bounds.width * scale + 0.5d);
			int targetHeight = (int) (bounds.height * scale + 0.5d);
			if (Platform.getPreferencesService().getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.ADVANCEDGRAPHICS,
					false, null)) {
				gc.setAntialias(SWT.ON);
				gc.setInterpolation(SWT.HIGH);
			}
			int offy = (height - targetHeight) / 2;
			int offx = (width - targetWidth) / 2;
			gc.drawImage(image, 0, 0, bounds.width, bounds.height, offx, offy, targetWidth, targetHeight);
			if (currentFrame.getWidth() != 1d || currentFrame.getHeight() != 1d) {
				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_CYAN));
				gc.setLineWidth(2);
				gc.drawRectangle((int) (currentFrame.getX() * targetWidth + offx),
						(int) (currentFrame.getY() * targetHeight + offy),
						(int) (currentFrame.getWidth() * targetWidth), (int) (currentFrame.getHeight() * targetHeight));
			}
			if (showHisto) {
				int ovh = captionManager.getOverlayHeight(gc);
				int w3 = width * 2 / 7;
				int h3 = height * 2 / 7;
				int x = width - w3 - 10;
				int y = (ovh > 0 ? offy + targetHeight - ovh : height) - h3 - 10;
				gc.setAlpha(64);
				gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
				gc.fillRectangle(x, y, w3, h3);
				gc.setAlpha(144);
				gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
				gc.drawRectangle(x - 1, y - 1, w3 + 2, h3 + 2);
				histogramManager.paint(gc, x, y, w3, h3, bw ? HistogramManager.CONTRAST : HistogramManager.DARK, true,
						true, !bw, !bw, !bw, false);
				histoRegion = new Rectangle(x, y, w3, h3);
			} else
				histoRegion = null;
			captionManager.handleOverlay(gc, offx, offy, targetWidth, targetHeight, 144, 180);
		}
	}

	private Image getBwImage(Asset asset, RGB filter) {
		if (bwImage == null) {
			Image image = getImage(asset);
			Device device = image.getDevice();
			ImageData imageData = image.getImageData();
			image.dispose();
			ImageUtilities.convert2Bw(imageData, filter);
			bwImage = new Image(device, imageData);
		}
		return bwImage;
	}

	public ISelection getSelection() {
		return (currentItem != null) ? new StructuredSelection(currentItem) : StructuredSelection.EMPTY;
	}

	public void setSelection(ISelection selection) {
		// do nothing
	}

	public Object findObject(Event e) {
		return findObject(e.x, e.y);
	}

	public Object findObject(int x, int y) {
		return currentItem;
	}

	public Control getControl() {
		return canvas;
	}

	@Override
	public void refresh() {
		if (showHisto)
			histogramManager.recalculate(currentItem == null ? null : getImage(currentItem), false);
		captionManager.refresh();
		disposeBwImage();
		canvas.redraw();
		updateActions(false);
	}

	private void disposeBwImage() {
		if (bwImage != null) {
			bwImage.dispose();
			bwImage = null;
		}
	}

	@Override
	public void dispose() {
		disposeBwImage();
		super.dispose();
	}

	public AssetSelection getAssetSelection() {
		if (currentItem == null)
			return AssetSelection.EMPTY;
		if (assetSelection.getFirstElement() != currentItem)
			assetSelection = new AssetSelection(currentItem);
		return assetSelection;
	}

	@Override
	protected int getSelectionCount(boolean local) {
		return currentItem == null || local && currentItem.getFileState() == IVolumeManager.PEER ? 0 : 1;
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
		assetSelection = getNavigationHistory().getSelectedAssets();
		selectedItem = currentItem = assetSelection.getFirstElement();
		boolean changed = previous != currentItem;
		if (changed) {
			currentFrame.setFrame(0d, 0d, 1d, 1d);
			captionManager.updateCaption(currentItem);
		}
		return changed;
	}

	@Override
	public void cueChanged(Object o) {
		Asset previous = currentItem;
		if (cue && previous != (currentItem = o instanceof Asset ? (Asset) o : selectedItem))
			asyncRefresh();
	}

	@Override
	public void assetsModified(BagChange<Asset> changes, QueryField node) {
		if (node == null) {
			if (changes == null)
				asyncRefresh();
			else if (currentItem != null) {
				if (changes.wasModified(currentItem))
					asyncRefresh();
				else if (changes.wasRemoved(currentItem)) {
					currentItem = null;
					asyncRefresh();
				}
			}
		}
	}

	@Override
	public void catalogClosed(int mode) {
		if (mode == CatalogListener.NORMAL) {
			currentItem = null;
			asyncRefresh();
		}
	}

	private void asyncRefresh() {
		if (!canvas.isDisposed())
			getSite().getShell().getDisplay().asyncExec(() -> {
				if (!canvas.isDisposed())
					refresh();
			});
	}

	@Override
	protected RGB getBWmode() {
		return bw ? filter : null;
	}

	@Override
	public Object getContent() {
		return currentItem;
	}

	public boolean cursorOverImage(int x, int y) {
		return currentItem != null;
	}

	public IAssetProvider getAssetProvider() {
		return null;
	}

	@Override
	public void frameChanged(IFrameProvider provider, String assetId, double x, double y, double w, double h) {
		if (!canvas.isDisposed()
				&& (currentFrame.getX() != x || currentFrame.getY() != y || currentFrame.getWidth() != w
						|| currentFrame.getHeight() != h)
				&& currentItem != null && (assetId == null || currentItem.getStringId().equals(assetId))) {
			currentFrame.setFrame(x, y, w, h);
			if (isVisible())
				canvas.redraw();
		}
	}

}