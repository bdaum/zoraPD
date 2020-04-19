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

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IWorkbenchWindow;
import org.piccolo2d.util.PAffineTransform;
import org.piccolo2d.util.PAffineTransformException;

import com.bdaum.zoom.batch.internal.ExifTool;
import com.bdaum.zoom.batch.internal.IFileWatcher;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.peer.AssetOrigin;
import com.bdaum.zoom.core.internal.peer.ConnectionLostException;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.ui.IFrameListener;
import com.bdaum.zoom.ui.IFrameProvider;
import com.bdaum.zoom.ui.IStateListener;
import com.bdaum.zoom.ui.ITransformListener;
import com.bdaum.zoom.ui.ITransformProvider;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.hover.HoverInfo;
import com.bdaum.zoom.ui.internal.widgets.FadingShell;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class ImageViewer extends AbstractMediaViewer implements UiConstants, IFrameProvider, ITransformProvider {

	private static final long USEPREVIEWTHRESHOLD = 4000000L;
	private static final double MAXZOOM = 4d;
	private static final int INCREMENT = 10;
	private static final int PAGEINCREMENT = 100;
	protected static final int HOTSPOTSIZE = 48;

	private final class InertiaMousePanListener implements Listener {
		double xSpeed, ySpeed;
		double lag = 0.7d;
		double stop = 0.05d;
		boolean mouseDown = false;
		boolean altClick = false;
		Point targetPnt = new Point(0, 0);
		Point mousePnt = new Point(0, 0);
		private long lastTime;
		private int mouseButton;
		private int softness;
		private ScheduledFuture<?> panTask;
		private double previousRatio = Double.NaN;

		public InertiaMousePanListener() {
			softness = Platform.getPreferencesService().getInt(UiActivator.PLUGIN_ID, PreferenceConstants.WHEELSOFTNESS,
					50, null);
			lag = softness * 0.005d + 0.45d;
		}

		public void cancel() {
			if (panTask != null) {
				panTask.cancel(true);
				panTask = null;
			}
			xSpeed = ySpeed = 0d;
		}

		private void performMouseAction(Event e) {
			if (altClick)
				zoom(xSpeed, targetPnt.x, targetPnt.y);
			else
				pan(xSpeed, ySpeed);
		}

		@Override
		public void handleEvent(Event e) {
			switch (e.type) {
			case SWT.MouseMove:
				final double scale = viewTransform.getScale();
				boolean enlarged = scale > wheelListener.getMinScale();
				if (mouseDown) {
					if (softness == 0) {
						setCursorForObject(e, enlarged ? CURSOR_GRABBING : null, CURSOR_MMINUS, CURSOR_MPLUS, null);
						double f = (altClick ? 0.4d / scale : 1d);
						xSpeed = (e.x - mousePnt.x) * f;
						ySpeed = (e.y - mousePnt.y) * f;
						targetPnt.x = e.x;
						targetPnt.y = e.y;
						performMouseAction(e);
						mousePnt.x = e.x;
						mousePnt.y = e.y;
						return;
					}
					long time = ((long) e.time) & 0x00000000FFFFFFFF;
					long dt = time - lastTime;
					if (dt > 0) {
						double f = (altClick ? 40d / scale : 120d / Math.sqrt(scale)) / dt;
						xSpeed = (xSpeed + (e.x - mousePnt.x) * f) / 2;
						ySpeed = (ySpeed + (e.y - mousePnt.y) * f) / 2;
					}
					lastTime = time;
					if (panTask == null && xSpeed != 0 && ySpeed != 0) {
						targetPnt.x = e.x;
						targetPnt.y = e.y;
						panTask = UiActivator.getScheduledExecutorService().scheduleAtFixedRate(() -> {
							if (e.display.isDisposed() || topCanvas.isDisposed())
								xSpeed = ySpeed = 0;
							else {
								e.display.syncExec(() -> {
									if (!topCanvas.isDisposed())
										performMouseAction(e);
								});
								xSpeed *= lag;
								ySpeed *= lag;
							}
							if (Math.abs(xSpeed) < stop && Math.abs(ySpeed) < stop)
								InertiaMousePanListener.this.cancel();
						}, 0L, 60L, TimeUnit.MILLISECONDS);
					}
				}
				if (hotspot.contains(e.x, e.y)) {
					topCanvas.setCursor(e.display.getSystemCursor(SWT.CURSOR_HAND));
					systemCursorSet = true;
					return;
				}
				if (systemCursorSet) {
					topCanvas.setCursor(UiActivator.getDefault().getCursor(e.display, currentCustomCursor));
					systemCursorSet = false;
				}
				mousePnt.x = e.x;
				mousePnt.y = e.y;
				if (mouseDown) {
					e.button = mouseButton;
					String zoomCursor = (xSpeed < 0) ? CURSOR_MMINUS : CURSOR_MPLUS;
					setCursorForObject(e, enlarged ? CURSOR_GRABBING : null, zoomCursor, zoomCursor, null);
				} else {
					String cursor = enlarged ? CURSOR_OPEN_HAND : null;
					setCursorForObject(e, cursor, cursor, cursor, enlarged ? null : CURSOR_MPLUS);
				}
				break;
			case SWT.MouseUp:
				if (!Double.isNaN(previousRatio)) {
					fix(previousRatio, e.x, e.y);
					previousRatio = Double.NaN;
				}
				mouseDown = false;
				String cursor = isEnlarged() ? CURSOR_OPEN_HAND : null;
				setCursorForObject(e, cursor, cursor, cursor, null);
				break;
			case SWT.MouseDown:
				if (hotspot.contains(e.x, e.y))
					fireStateEvent(IStateListener.SYNC);
				else {
					mouseButton = e.button;
					if (e.count == 1 && highResVisible) {
						mouseDown = true;
						targetPnt.x = mousePoint.x = mousePnt.x = e.x;
						targetPnt.y = mousePoint.y = mousePnt.y = e.y;
						altClick = e.button == 3 && modMask == SWT.BUTTON3 || (e.stateMask & modMask) == modMask;
						lastTime = ((long) e.time) & 0x00000000FFFFFFFF;
						lastMouseX = e.x;
						if (isEnlarged())
							setCursorForObject(e, CURSOR_GRABBING, CURSOR_MMINUS, CURSOR_MPLUS, null);
						else {
							setCursorForObject(e, null, CURSOR_MMINUS, CURSOR_MPLUS, null);
							if (e.button == 1) {
								previousRatio = 1 / viewTransform.getScale();
								fix(1, e.x, e.y);
							}
						}
					}
				}
				break;
			case SWT.MouseDoubleClick:
				if (hotspot.contains(e.x, e.y)) {
					fireStateEvent(IStateListener.SYNC);
					fireTransformEvent();
				} else if (highResVisible && e.button == 1) {
					cancel();
					mousePoint.x = e.x;
					mousePoint.y = e.y;
					resetView();
				}
				break;
			}
		}
	}

	public class HighResJob extends Job implements Listener {

		private File imageFile;
		private boolean adv;
		private int cms1;
		private boolean firstCall;

		public HighResJob(File imageFile, boolean advanced, int cms, boolean subSampling) {
			super("ImageLoading"); //$NON-NLS-1$
			this.imageFile = imageFile;
			this.adv = advanced;
			this.cms1 = cms;
			this.firstCall = subSampling
					&& !UiActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.ALWAYSHIGHRES);
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}

		@Override
		public boolean belongsTo(Object family) {
			return Constants.DAEMONS == family;
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			String opId = java.util.UUID.randomUUID().toString();
			IFileWatcher fileWatcher = CoreActivator.getDefault().getFileWatchManager();
			try {
				MultiStatus status = new MultiStatus(UiActivator.PLUGIN_ID, 0,
						Messages.getString("ImageViewer.Image_loading_report"), null); //$NON-NLS-1$
				try {
					boolean secondCallPossible = false;
					final MultiStatus status1 = status;
					if (!firstCall) {
						for (int i = 0; i < 16; i++) {
							if (topShell == null || topShell.isDisposed())
								break;
							display.syncExec(() -> {
								topShell.setAlpha(Math.max(0, topShell.getAlpha() - 16));
							});
							try {
								Thread.sleep(40);
							} catch (InterruptedException ex) {
								// do nothing
							}
						}
						if (image != null) {
							image.dispose();
							image = null;
						}
					}
					image = CoreActivator.getDefault().getHighresImageLoader().loadImage(monitor, status1, imageFile,
							asset.getRotation(), asset.getFocalLengthIn35MmFilm(), null, firstCall ? 1d : 0d, 1d, adv,
							cms1, bwmode, null, cropmode == ZImage.ORIGINAL ? Recipe.NULL : null, fileWatcher, opId,
							null);
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					if (image != null) {
						display.syncExec(() -> {
							if (!topCanvas.isDisposed()) {
								Rectangle area = topCanvas.getClientArea();
								image.develop(monitor, display, cropmode, area.width, area.height, ZImage.SWTIMAGE);
							}
						});
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						int superSamplingFactor = image.getSuperSamplingFactor();
						ibounds = image.getBounds();
						secondCallPossible = firstCall && superSamplingFactor > 1;
						final FadingShell formerShell = (previewShown) ? previewShell : bottomShell;
						fadein(status1, monitor, topShell, topCanvas, formerShell, !secondCallPossible);
						highResVisible = true;
						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
						if (secondCallPossible) {
							if (status1.isOK()) {
								display.syncExec(() -> {
									Shell sh = topShell.getShell();
									Rectangle bounds = sh.getBounds();
									if (highresTooltip == null)
										highresTooltip = new ToolTip(sh, SWT.ICON_WARNING);
									highresTooltip.addListener(SWT.Selection, this);
									UiUtilities.showTooltip(highresTooltip, bounds.x + bounds.width / 3, bounds.y,
											NLS.bind(Messages.getString("ImageViewer.downsampled"), //$NON-NLS-1$
													superSamplingFactor),
											Messages.getString("ImageViewer.click_for_fullres")); //$NON-NLS-1$
								});
								while (!display.isDisposed()) {
									display.syncExec(() -> {
										if (formerShell.isDisposed())
											monitor.setCanceled(true);
										else if (!highresTooltip.isVisible() && highResVisible) {
											formerShell.close();
											monitor.setCanceled(true);
										}
									});
									if (monitor.isCanceled())
										break;
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										break;
									}
								}
							}
						} else
							display.syncExec(() -> {
								if (!formerShell.isDisposed())
									formerShell.close();
							});
					}
				} catch (UnsupportedOperationException e) {
					loadFailed = e.getMessage();
					display.syncExec(() -> {
						if (!bottomCanvas.isDisposed() && bottomShell.isVisible())
							bottomCanvas.redraw();
						if (!previewCanvas.isDisposed() && previewShell.isVisible())
							previewCanvas.redraw();
					});
				}
				return status;
			} finally {
				fileWatcher.stopIgnoring(opId);
			}
		}

		@Override
		public void handleEvent(Event event) {
			highResVisible = false;
			viewTransform = null;
			if (!highresTooltip.isDisposed())
				highresTooltip.setVisible(false);
			highResJob = new HighResJob(file, advanced, cms, false);
			highResJob.schedule();
		}

	}

	public class PreviewJob extends Job {

		private File ifile;

		public PreviewJob(File imageFile) {
			super("PreviewLoading"); //$NON-NLS-1$
			ifile = imageFile;
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}

		@Override
		public boolean belongsTo(Object family) {
			return Constants.DAEMONS == family;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			MultiStatus status = new MultiStatus(UiActivator.PLUGIN_ID, 0,
					Messages.getString("ImageViewer.Preview_loading_report"), null); //$NON-NLS-1$
			try (ExifTool tool = new ExifTool(ifile, true)) {
				previewImage = tool.getPreviewImage(false);
				if (previewImage != null && image == null) {
					ExifTool.fixOrientation(previewImage, asset.getOrientation(), asset.getRotation());
					previewImage.develop(monitor, display, ZImage.UNCROPPED, -1, -1, ZImage.SWTIMAGE);
					previewImage.getSwtImage(display, false, ZImage.UNCROPPED, SWT.DEFAULT, SWT.DEFAULT);
					previewShown = true;
					fadein(status, monitor, previewShell, previewCanvas, bottomShell, true);
					return status;
				}
			} catch (Exception e) {
				// do nothing - ignore preview image
			}
			return status;
		}
	}

	private final class InertiaMouseWheelListener implements MouseWheelListener {
		double nonlinearity = 0.33333333d;
		double currentSpeed;
		double minScale = 0.01d;
		double maxScale = 100d;
		double sensitivity = 0.8d;
		double lag = 0.8d;
		private int softness;
		private ScheduledFuture<?> wheelTask;

		public InertiaMouseWheelListener() {
			softness = UiActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.WHEELSOFTNESS);
			lag = softness * 0.003d + 0.65d;
		}

		public double getMinScale() {
			return minScale;
		}

		public void setMinScale(double minScale) {
			this.minScale = minScale;
		}

		public double getMaxScale() {
			return maxScale;
		}

		public void setMaxScale(double maxScale) {
			this.maxScale = maxScale;
		}

		public void cancel() {
			if (wheelTask != null) {
				wheelTask.cancel(true);
				wheelTask = null;
			}
			currentSpeed = 0d;
		}

		public void mouseScrolled(final MouseEvent e) {
			if (!highResVisible)
				return;
			if (softness == 0) {
				currentSpeed = e.count;
				zoom(currentSpeed, e.x, e.y);
				return;
			}
			currentSpeed += sensitivity * e.count * Math.pow(Math.abs(e.count), nonlinearity);
			if (wheelTask == null && currentSpeed != 0) {
				wheelTask = UiActivator.getScheduledExecutorService().scheduleAtFixedRate(() -> {
					if (e.display.isDisposed())
						currentSpeed = 0;
					else {
						e.display.syncExec(() -> {
							if (!((Canvas) e.widget).isDisposed())
								zoom(currentSpeed, e.x, e.y);
						});
						currentSpeed = currentSpeed * lag;
					}
					if (Math.abs(currentSpeed) < lag)
						InertiaMouseWheelListener.this.cancel();
				}, 0L, 60L, TimeUnit.MILLISECONDS);
			}
		}
	}

	public String loadFailed;
	private static final double EPSILON = 0.005d;
	ZImage image;
	Rectangle ibounds;
	private ZImage previewImage;
	PAffineTransform viewTransform;
	FadingShell bottomShell;
	FadingShell previewShell;
	FadingShell topShell;
	Canvas bottomCanvas;
	File file;
	private TextLayout tlayout;
	HighResJob highResJob;
	Canvas topCanvas;
	private InertiaMouseWheelListener wheelListener;
	private java.awt.Rectangle rectSrc = new java.awt.Rectangle();
	private java.awt.Rectangle rectDst = new java.awt.Rectangle();
	private Point mousePoint = new Point(0, 0);
	boolean advanced;
	int cms;
	Canvas previewCanvas;
	private PreviewJob previewJob;
	boolean highResVisible = false;
	boolean previewShown = false;
	private boolean preview;
	private InertiaMousePanListener panListener;
	private int lastMouseX;
	private int modMask;
	private Shell controlShell;
	private Canvas controlCanvas;
	private String currentCustomCursor;
	private Region controlRegion;
	private Job transferJob;
	private Image bwImage;
	private boolean enlarge;
	private double oldRatio = -1d;
	private double oldScale;
	private int oldKeyCode;
	private long oldTime;
	private int canvasXoffset;
	private int canvasYoffset;
	private ListenerList<IFrameListener> frameListeners = new ListenerList<>();
	private ListenerList<ITransformListener> transformListeners = new ListenerList<>();
	private Rectangle2D currentFrame = new Rectangle2D.Double(0d, 0d, 1d, 1d);
	private boolean sync = false;
	private boolean vertical;
	protected Rectangle hotspot = new Rectangle(0, 0, 0, 0);
	protected boolean systemCursorSet = false;
	protected ToolTip highresTooltip, syncTooltip, helpTooltip, metaTootip;

	void fadein(MultiStatus status, IProgressMonitor monitor, final FadingShell shell, final Canvas canvas,
			final FadingShell formerShell, boolean closeFormer) {
		if (monitor.isCanceled()) {
			status.add(Status.CANCEL_STATUS);
			return;
		}
		if (!canvas.isDisposed())
			display.syncExec(() -> {
				if (!canvas.isDisposed()) {
					canvas.redraw();
					shell.setActive();
				}
			});
		for (int i = 0; i < 16; i++) {
			if (shell.isDisposed())
				break;
			if (monitor.isCanceled())
				break;
			display.syncExec(() -> {
				shell.setAlpha(Math.min(255, shell.getAlpha() + 16));
			});
			try {
				Thread.sleep(60);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		if (!display.isDisposed())
			display.syncExec(() -> {
				if (closeFormer)
					formerShell.close();
				if (!shell.isDisposed()) {
					shell.forceActive();
					shell.forceFocus();
					canvas.setFocus();
				}
			});
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see
	 * com.bdaum.zoom.ui.views.IMediaViewer#init(org.eclipse.ui.IWorkbenchWindow,
	 * org.eclipse.swt.graphics.RGB, int)
	 */
	public void init(IWorkbenchWindow window, int kind, RGB bw, int crop) {
		super.init(window, kind, bw, crop);
		IPreferencesService preferencesService = Platform.getPreferencesService();
		advanced = preferencesService.getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.ADVANCEDGRAPHICS, false,
				null);
		cms = preferencesService.getInt(UiActivator.PLUGIN_ID, PreferenceConstants.COLORPROFILE, ImageConstants.SRGB,
				null);
		preview = preferencesService.getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.PREVIEW, false, null);
		int zoomKey = preferencesService.getInt(UiActivator.PLUGIN_ID, PreferenceConstants.ZOOMKEY, SWT.ALT, null);
		enlarge = preferencesService.getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.ENLARGESMALL, false, null);
		switch (zoomKey) {
		case PreferenceConstants.ZOOMALT:
			modMask = SWT.ALT;
			break;
		case PreferenceConstants.ZOOMRIGHT:
			modMask = SWT.BUTTON3;
			break;
		case PreferenceConstants.ZOOMSHIFT:
			modMask = SWT.SHIFT;
			break;
		default:
			modMask = -1;
			break;
		}
	}

	/*
	 * (nicht-Javadoc)
	 * 
	 * @see com.bdaum.zoom.ui.internal.AbstractKiosk#create()
	 */
	public void create() {
		super.create();
		bottomShell = new FadingShell(createKioskShell(Messages.getString("ImageViewer.lowres_viewer")), false, //$NON-NLS-1$
				-1);
		bottomCanvas = new Canvas(bottomShell.getShell(), SWT.DOUBLE_BUFFERED);
		previewShell = new FadingShell(createKioskShell(Messages.getString("ImageViewer.preview_viewer")), true, //$NON-NLS-1$
				Constants.SLIDE_TRANSITION_FADE);
		previewCanvas = new Canvas(previewShell.getShell(), SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);
		topShell = new FadingShell(createKioskShell(Messages.getString("ImageViewer.highres_viewer")), true, //$NON-NLS-1$
				Constants.SLIDE_TRANSITION_FADE);
		topCanvas = new Canvas(topShell.getShell(), SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);
		controlShell = new Shell(display, SWT.NO_TRIM);
		controlRegion = new Region(display);
		controlRegion.add(new int[] { 4, 1, 44, 1, 47, 4, 47, 43, 44, 46, 4, 46, 1, 43, 1, 4 });
		controlShell.setRegion(controlRegion);
		controlShell.setText(Constants.APPNAME);
		controlShell.setLayout(new FillLayout());
		controlCanvas = new Canvas(controlShell, SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);
		controlCanvas.addListener(SWT.MouseUp, this);
		bottomCanvas.addListener(SWT.KeyDown, this);
		bottomCanvas.addListener(SWT.KeyUp, this);
		previewCanvas.addListener(SWT.KeyDown, this);
		previewCanvas.addListener(SWT.KeyUp, this);
		topCanvas.addListener(SWT.MouseMove, this);
		topCanvas.addListener(SWT.MouseHover, this);
		bottomCanvas.addListener(SWT.MouseMove, this);
		previewCanvas.addListener(SWT.MouseMove, this);
		topCanvas.addListener(SWT.KeyDown, this);
		topCanvas.addListener(SWT.KeyUp, this);
		topCanvas.addListener(SWT.Help, this);
		wheelListener = new InertiaMouseWheelListener();
		wheelListener.setMinScale(1d);
		wheelListener.setMaxScale(MAXZOOM);
		topCanvas.addMouseWheelListener(wheelListener);
		panListener = new InertiaMousePanListener();
		topCanvas.addListener(SWT.MouseDown, panListener);
		topCanvas.addListener(SWT.MouseUp, panListener);
		topCanvas.addListener(SWT.MouseDoubleClick, panListener);
		topCanvas.addListener(SWT.MouseMove, panListener);
		bottomCanvas.addListener(SWT.Paint, this);
		previewCanvas.addListener(SWT.Paint, this);
		topCanvas.addListener(SWT.Paint, this);
		bottomCanvas.redraw();
		controlCanvas.addListener(SWT.Paint, this);
		controlCanvas.redraw();
		controlShell.setBounds(Constants.OSX ? mbounds.x : mbounds.x + mbounds.width - HOTSPOTSIZE, mbounds.y,
				HOTSPOTSIZE, HOTSPOTSIZE);
		controlShell.setVisible(false);
		bottomShell.layout();
		previewShell.layout();
		topShell.layout();
		Ui.getUi().getFrameManager().registerFrameProvider(this);
	}

	@Override
	public void handleEvent(Event e) {
		switch (e.type) {
		case SWT.MouseUp:
			if (e.widget == controlCanvas)
				close();
			break;
		case SWT.MouseMove:
			mouseMove(e);
			break;
		case SWT.MouseHover:
			if (e.widget instanceof Canvas) {
				if (hotspot.contains(e.x, e.y)) {
					Canvas canvas = (Canvas) e.widget;
					if (syncTooltip == null)
						syncTooltip = new ToolTip(canvas.getShell(), SWT.BALLOON);
					syncTooltip.setMessage(Messages.getString("ImageViewer.sync")); //$NON-NLS-1$
					Point pnt = canvas.toDisplay(e.x, e.y);
					syncTooltip.setLocation(pnt);
					syncTooltip.setVisible(true);
				}
			}
			break;
		case SWT.Help:
			if (helpTooltip == null)
				helpTooltip = new ToolTip(topShell.getShell(), SWT.BALLOON | SWT.ICON_INFORMATION);
			UiUtilities.showTooltip(helpTooltip, mbounds.x + 10, mbounds.y + 5,
					Messages.getString("ImageViewer.image_viewer"), //$NON-NLS-1$
					getKeyboardHelp(true));
			break;
		case SWT.Paint:
			if (e.widget == controlCanvas)
				e.gc.drawImage(Icons.closeButton.getImage(), 0, 0);
			else if (e.widget == topCanvas)
				paintTop(e);
			else
				paintBottom(e);
		}
		super.handleEvent(e);
	}

	private void mouseMove(Event e) {
		Canvas canvas = (Canvas) e.widget;
		Rectangle clientArea = canvas.getClientArea();
		if (e.y < clientArea.y + 2 * HOTSPOTSIZE) {
			if (e.x < clientArea.x + 2 * HOTSPOTSIZE && kind != RIGHT)
				showCloseButton(canvas.toDisplay(clientArea.x, clientArea.y));
			else if (e.x > clientArea.x + clientArea.width - 2 * HOTSPOTSIZE && kind != LEFT)
				showCloseButton(canvas.toDisplay(clientArea.x + clientArea.width - HOTSPOTSIZE, clientArea.y));
			else
				controlShell.setVisible(false);
		} else
			controlShell.setVisible(false);
		if (controlShell.getVisible())
			controlShell.forceActive();
	}

	private void showCloseButton(Point pnt) {
		controlShell.setLocation(pnt);
		controlShell.setVisible(true);
	}

	private void paintBottom(Event e) {
		Canvas canvas = (Canvas) e.widget;
		Shell shell = canvas.getShell();
		Rectangle sbnds = canvas.getClientArea();
		CssActivator.getDefault().setColors(shell);
		GC gc = e.gc;
		gc.setBackground(shell.getBackground());
		gc.fillRectangle(sbnds);
		if (!highResVisible) {
			Image im = previewImage != null
					? previewImage.getSwtImage(display, false, ZImage.UNCROPPED, SWT.DEFAULT, SWT.DEFAULT)
					: bwmode != null ? getBwImage(asset, bwmode) : getImage(asset);
			if (im != null) {
				Rectangle ibnds = im.getBounds();
				double factor = Math.min((double) sbnds.width / ibnds.width, (double) sbnds.height / ibnds.height);
				if (!enlarge) {
					double factor2 = Math.min((double) sbnds.width / asset.getWidth(),
							(double) sbnds.height / asset.getHeight());
					if (factor2 > 1d)
						factor /= factor2;
				}
				int w = (int) (ibnds.width * factor);
				int h = (int) (ibnds.height * factor);
				gc.drawImage(im, 0, 0, ibnds.width, ibnds.height, (sbnds.width - w) / 2, (sbnds.height - h) / 2, w, h);
			}
			String volume = asset.getVolume();
			String text;
			if (file == null)
				text = volume == null || volume.trim().isEmpty()
						? Messages.getString("ImageViewer.highres_not_available") //$NON-NLS-1$
						: NLS.bind(Messages.getString("ImageViewer.high_res_image_not_available"), //$NON-NLS-1$
								volume);
			else if (loadFailed != null)
				text = loadFailed;
			else
				text = new StringBuilder()
						.append((previewImage != null ? Messages.getString("ImageViewer.loading_highres") //$NON-NLS-1$
								: Messages.getString("ImageViewer.loading_thumbnail"))) //$NON-NLS-1$
						.append('\n').append('\n').append(getKeyboardHelp(false)).append('\n').toString();
			if (tlayout == null) {
				tlayout = new TextLayout(display);
				tlayout.setAlignment(SWT.CENTER);
				tlayout.setWidth(sbnds.width);
				tlayout.setFont(JFaceResources.getFont(UiConstants.VIEWERFONT));
			}
			tlayout.setText(text);
			Rectangle tbounds = tlayout.getBounds();
			int y = (sbnds.height - tbounds.height) / 2;
			gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
			gc.setFont(tlayout.getFont());
			gc.setAlpha(64);
			Point textExtent = gc.textExtent(text);
			gc.fillRoundRectangle((tbounds.width - textExtent.x) / 2 - 10, y - 5, textExtent.x + 20, tbounds.height, 10,
					10);
			gc.setAlpha(255);
			gc.setForeground(display.getSystemColor((file == null || loadFailed != null) ? SWT.COLOR_RED
					: (previewImage != null) ? SWT.COLOR_CYAN : SWT.COLOR_GREEN));
			tlayout.draw(gc, 0, y);
		}
	}

	private void paintTop(Event e) {
		if (image != null) {
			Rectangle sbounds = topCanvas.getClientArea();
			Shell shell = topShell.getShell();
			CssActivator.getDefault().setColors(shell);
			GC gc = e.gc;
			gc.setBackground(shell.getBackground());
			gc.fillRectangle(sbounds);
			int iwidth = ibounds.width;
			int iheight = ibounds.height;
			if (viewTransform == null) {
				viewTransform = new PAffineTransform();
				double zoomfactor = Math.min((double) sbounds.width / iwidth, (double) sbounds.height / iheight);
				if (!enlarge && zoomfactor > 1d)
					zoomfactor = 1d;
				wheelListener.setMinScale(zoomfactor);
				viewTransform.scale(zoomfactor, zoomfactor);
			}
			rectSrc.setBounds(ibounds.x, ibounds.y, iwidth, iheight);
			viewTransform.transform(rectSrc, rectDst);
			int canvasWidth = Math.min((int) (rectDst.getWidth()), sbounds.width);
			int canvasHeight = Math.min((int) (rectDst.getHeight()), sbounds.height);
			canvasXoffset = Math.max(0, (sbounds.width - canvasWidth) / 2);
			canvasYoffset = Math.max(0, (sbounds.height - canvasHeight) / 2);
			rectSrc.setBounds(0, 0, canvasWidth, canvasHeight);
			viewTransform.inverseTransform(rectSrc, rectDst);
			int cropWidth = Math.min((int) (rectDst.getWidth()), iwidth);
			int cropHeight = Math.min((int) (rectDst.getHeight()), iheight);
			int cropXoffset = Math.max(0, Math.min(iwidth - cropWidth, (int) (rectDst.getX())));
			int cropYoffset = Math.max(0, Math.min(iheight - cropHeight, (int) (rectDst.getY())));
			if (advanced) {
				gc.setAntialias(SWT.ON);
				gc.setInterpolation(SWT.HIGH);
			}
			try {
				image.draw(gc, cropXoffset, cropYoffset, cropWidth, cropHeight, canvasXoffset, canvasYoffset,
						canvasWidth, canvasHeight, cropmode, sbounds.width, sbounds.height, true);
				currentFrame.setFrame((double) cropXoffset / iwidth, (double) cropYoffset / iheight,
						(double) cropWidth / iwidth, (double) cropHeight / iheight);
			} catch (Exception e1) {
				UiActivator.getDefault().logError(Messages.getString("ImageViewer.error_when_resizing"), //$NON-NLS-1$
						e1);
			}
			gc.setBackground(topShell.getShell().getBackground());
			if (canvasXoffset > 0)
				gc.fillRectangle(0, 0, canvasXoffset, sbounds.height);
			if (canvasXoffset + canvasWidth < sbounds.width)
				gc.fillRectangle(canvasXoffset + canvasWidth, 0, sbounds.width - (canvasXoffset + canvasWidth),
						sbounds.height);
			if (canvasYoffset > 0)
				gc.fillRectangle(0, 0, sbounds.width, canvasYoffset);
			if (canvasYoffset + canvasHeight < sbounds.height)
				gc.fillRectangle(0, canvasYoffset + canvasHeight, sbounds.width,
						sbounds.height - (canvasYoffset + canvasHeight));
			if (!transformListeners.isEmpty()) {
				Image img = sync ? Icons.sync32.getImage() : Icons.sync32d.getImage();
				Rectangle bounds = img.getBounds();
				int x, y;
				if (vertical) {
					hotspot.width = bounds.width;
					hotspot.height = bounds.height / 2;
					hotspot.x = x = 0;
					y = kind == LEFT ? sbounds.height - hotspot.height : -hotspot.height;
					hotspot.y = kind == LEFT ? y : 0;
				} else {
					hotspot.width = bounds.width / 2;
					hotspot.height = bounds.height;
					x = kind == LEFT ? sbounds.width - hotspot.width : -hotspot.width;
					hotspot.x = kind == LEFT ? x : 0;
					hotspot.y = y = sbounds.height - 2 * bounds.height;
				}
				gc.setBackground(display.getSystemColor(sync ? SWT.COLOR_GREEN : SWT.COLOR_WIDGET_NORMAL_SHADOW));
				gc.fillOval(x - 2, y - 2, bounds.width + 4, bounds.height + 4);
				gc.drawImage(img, x, y);
			}
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

	private String getKeyboardHelp(boolean help) {
		String msg = NLS.bind(help ? Messages.getString("ImageViewer.use_mouse_wheel2") //$NON-NLS-1$
				: Messages.getString("ImageViewer.use_mouse_wheel"), //$NON-NLS-1$
				modMask == SWT.BUTTON3 ? Messages.getString("ImageViewer.right_mouse_button") //$NON-NLS-1$
						: modMask == SWT.ALT ? Messages.getString("ImageViewer.ALT") //$NON-NLS-1$
								: Messages.getString("ImageViewer.SHIFT"));//$NON-NLS-1$
		if (!transformListeners.isEmpty())
			msg += Messages.getString("ImageViewer.use_capslock"); //$NON-NLS-1$
		return msg;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IImageViewer#open(com.bdaum.zoom.cat.model.asset
	 * .Asset)
	 */
	public void open(Asset[] assets) throws IOException {
		asset = assets[0];
		URI uri = Core.getCore().getVolumeManager().findExistingFile(asset, false);
		File tempFile = null;
		if (uri != null) {
			if (Constants.FILESCHEME.equals(uri.getScheme())) {
				IPeerService peerService = Core.getCore().getPeerService();
				AssetOrigin assetOrigin = peerService != null ? peerService.getAssetOrigin(asset.getStringId()) : null;
				if (peerService != null && assetOrigin != null) {
					try {
						if (peerService.checkCredentials(IPeerService.VIEW, asset.getSafety(), assetOrigin)) {
							transferJob = peerService.scheduleTransferJob(asset, assetOrigin);
							if (transferJob != null) {
								while (true) {
									try {
										tempFile = file = transferJob.getAdapter(File.class);
										Job.getJobManager().join(transferJob, null);
										break;
									} catch (OperationCanceledException e) {
										transferJob = null;
										return;
									} catch (InterruptedException e) {
										// just continue
									}
								}
							}
						} else {
							AcousticMessageDialog.openError(null, Messages.getString("ImageViewer.access_restricted"), //$NON-NLS-1$
									Messages.getString("ImageViewer.rights_not_sufficient")); //$NON-NLS-1$
							return;
						}
					} catch (ConnectionLostException e) {
						AcousticMessageDialog.openError(null, Messages.getString("ImageViewer.connection_lost"), //$NON-NLS-1$
								e.getLocalizedMessage());
						return;
					}
				} else
					file = new File(uri);
			} else
				tempFile = file = Core.download(uri, null);
		}
		if (bottomShell == null)
			create();
		int previewSize = asset.getPreviewSize();
		int mxsize = Math.max(mbounds.width, mbounds.height);
		mousePoint.x = mbounds.width / 2;
		mousePoint.y = mbounds.height / 2;
		boolean usePreview = preview && ((previewSize < 0 || previewSize * 2 >= mxsize) && asset.getRotation() == 0);
		bottomShell.open();
		if (file != null) {
			usePreview &= file.length() > USEPREVIEWTHRESHOLD;
			if (usePreview) {
				Rectangle ibnds = Core.getCore().getImageCache().getImage(asset).getBounds();
				usePreview = (Math.max(ibnds.width, ibnds.height) < mxsize);
			}
			if (usePreview) {
				previewShell.open();
				previewJob = new PreviewJob(file);
				previewJob.schedule();
			}
			topShell.open();
			highResJob = new HighResJob(file, advanced, cms, true);
			highResJob.schedule();
		}
		if (kind == PRIMARY)
			while (!isDisposed())
				if (!display.readAndDispatch())
					display.sleep();
		if (tempFile != null)
			tempFile.delete();
	}

	@Override
	public boolean isDisposed() {
		if (file != null)
			return topShell == null || topShell.isDisposed();
		return bottomShell == null || bottomShell.isDisposed();
	}

	public void releaseKey(Event e) {
		long time = e.time & 0xFFFFFFFFL;
		if (oldKeyCode != e.keyCode || time - oldTime >= 100L) {
			oldTime = time;
			oldKeyCode = e.keyCode;
			if (e.character == Messages.getString("ImageViewer.w").charAt(0)) //$NON-NLS-1$
				fix((double) ibounds.width / topShell.getBounds().width, mousePoint.x, mousePoint.y);
			else if (e.character == Messages.getString("ImageViewer.h").charAt(0)) //$NON-NLS-1$
				fix((double) ibounds.height / topShell.getBounds().height, mousePoint.x, mousePoint.y);
			else
				switch (e.character) {
				case SWT.TAB:
					if (highResVisible)
						close();
					break;
				case '+':
					zoom(2, mousePoint.x, mousePoint.y);
					break;
				case '-':
					zoom(-2, mousePoint.x, mousePoint.y);
					break;
				case ' ':
				case '*':
					resetView();
					break;
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					fix(e.character - '0', mousePoint.x, mousePoint.y);
					break;
				default:
					if ((e.stateMask & SWT.SHIFT) != 0) {
						switch (e.keyCode) {
						case '1':
						case '2':
						case '3':
						case '4':
							fix(1d / (e.keyCode - '0'), mousePoint.x, mousePoint.y);
							break;
						}
						break;
					}
					int f = (e.stateMask & SWT.CONTROL) != 0 ? 5 : 1;
					switch (e.keyCode) {
					case SWT.ESC:
						close();
						break;
					case SWT.ARROW_LEFT:
						pan(-INCREMENT * f, 0);
						break;
					case SWT.ARROW_RIGHT:
						pan(INCREMENT * f, 0);
						break;
					case SWT.ARROW_UP:
						pan(0, -INCREMENT * f);
						break;
					case SWT.ARROW_DOWN:
						pan(0, INCREMENT * f);
						break;
					case SWT.HOME:
						pan(-PAGEINCREMENT * f, 0);
						break;
					case SWT.END:
						pan(PAGEINCREMENT * f, 0);
						break;
					case SWT.PAGE_UP:
						pan(0, -PAGEINCREMENT * f);
						break;
					case SWT.PAGE_DOWN:
						pan(0, PAGEINCREMENT * f);
						break;
					case SWT.F2:
						metadataRequested(asset);
						break;
					case SWT.F4:
						if (kind != PRIMARY)
							fireStateEvent(IStateListener.SYNC);
						break;
					}
					break;
				}
		}
	}

	public boolean close() {
		currentFrame.setFrame(0d, 0d, 1d, 1d);
		fireFrameEvent();
		Ui.getUi().getFrameManager().deregisterFrameProvider(this);
		fireStateEvent(IStateListener.CLOSED);
		if (transferJob != null)
			Job.getJobManager().cancel(transferJob);
		if (previewJob != null)
			previewJob.cancel();
		if (previewImage != null)
			previewImage.dispose();
		if (bwImage != null)
			bwImage.dispose();
		if (highResJob != null)
			highResJob.cancel();
		if (topShell != null) {
			topShell.close();
			topShell = null;
		}
		if (previewShell != null) {
			previewShell.close();
			previewShell = null;
		}
		if (bottomShell != null) {
			bottomShell.close();
			bottomShell = null;
		}
		if (controlShell != null) {
			controlShell.close();
			controlShell = null;
		}
		if (controlRegion != null) {
			controlRegion.dispose();
			controlRegion = null;
		}
		if (tlayout != null) {
			tlayout.dispose();
			tlayout = null;
		}
		if (image != null) {
			image.dispose();
			image = null;
		}
		return super.close();
	}

	private void resetView() {
		wheelListener.cancel();
		panListener.cancel();
		if (isEnlarged()) {
			viewTransform.setToIdentity();
			oldRatio = wheelListener.getMinScale();
			viewTransform.setToScale(oldRatio, oldRatio);
		} else
			fix(1, mousePoint.x, mousePoint.y);
		topCanvas.redraw();
		fireTransformEvent();
		fireFrameEvent();
	}

	private void pan(double xDir, double yDir) {
		viewTransform.translate(xDir, yDir);
		rectSrc.setBounds(ibounds.x, ibounds.y, ibounds.width, ibounds.height);
		viewTransform.transform(rectSrc, rectDst);
		Rectangle clientArea = topCanvas.getClientArea();
		if (rectDst.x > 0)
			viewTransform.translate(-rectDst.x, 0);
		else
			viewTransform.translate(-Math.min(0, rectDst.x + rectDst.width - clientArea.width), 0);
		if (rectDst.y > 0)
			viewTransform.translate(0, -rectDst.y);
		else
			viewTransform.translate(0, -Math.min(0, rectDst.y + rectDst.height - clientArea.height));
		topCanvas.redraw();
		fireTransformEvent();
		fireFrameEvent();
	}

	private void zoom(double factor, double x, double y) {
		zoomDelta(1d + factor * 0.01d, x - viewTransform.getTranslateX(), y - viewTransform.getTranslateY());
	}

	private void fix(double ratio, double x, double y) {
		double scale = viewTransform.getScale();
		double vx = viewTransform.getTranslateX();
		double vy = viewTransform.getTranslateY();
		if (oldRatio == ratio) {
			zoomDelta(oldScale / scale, x - vx, y - vy);
			oldRatio = -1d;
		} else {
			if (oldRatio < 0)
				oldScale = scale;
			oldRatio = ratio;
			zoomDelta(1 / (ratio * scale), x - vx, y - vy);
			if (!isEnlarged())
				oldRatio = -1d;
		}
	}

	private void zoomDelta(double delta, double x, double y) {
		double scale = viewTransform.getScale();
		double newScale = scale * delta;
		double minScale = Math.min(1d / 9, wheelListener.getMinScale());
		if ((newScale >= minScale || scale > minScale) && newScale <= wheelListener.getMaxScale()) {
			newScale = Math.max(newScale, minScale);
			delta = newScale / scale;
			try {
				viewTransform.translate((x - canvasXoffset) / scale, (y - canvasYoffset) / scale);
				viewTransform.scale(delta, delta);
				viewTransform.translate(-(x - canvasXoffset) / scale, -(y - canvasYoffset) / scale);
				topCanvas.redraw();
				fireTransformEvent();
				fireFrameEvent();
			} catch (PAffineTransformException e) {
				// ignore
			}
		}
	}

	private void setCursorForObject(Event e, String surface, String altLeft, String altRight, String dflt) {
		int button = e.button;
		if (button == 3 && modMask == SWT.BUTTON3 || button == 1 && (e.stateMask & modMask) == modMask) {
			setCanvasCursor((e.x >= lastMouseX) ? altRight : altLeft);
			lastMouseX = e.x;
		} else if (button != 0)
			setCanvasCursor(surface);
		else if (button == 0 && dflt != null)
			setCanvasCursor(dflt);
	}

	private void setCanvasCursor(String cursor) {
		if (cursor != currentCustomCursor) {
			currentCustomCursor = cursor;
			topCanvas.setCursor(UiActivator.getDefault().getCursor(display, cursor));
		}
	}

	private void metadataRequested(Asset asset) {
		if (metaTootip == null)
			metaTootip = new ToolTip(topShell.getShell(), SWT.BALLOON | SWT.ICON_INFORMATION);
		UiUtilities.showTooltip(metaTootip, mbounds.x + mbounds.width / 2, mbounds.y + mbounds.height / 2,
				NLS.bind(Messages.getString("SlideShowPlayer.metadata"), asset.getName()), //$NON-NLS-1$
				new HoverInfo(asset, (ImageRegion[]) null).getText());
	}

	public String getName() {
		return Messages.getString("ImageViewer.viewer_name"); //$NON-NLS-1$
	}

	public String getId() {
		return "com.bdaum.zoom.image"; //$NON-NLS-1$
	}

	private static Image getImage(Asset asset) {
		return ImageUtilities.boxedblur(Core.getCore().getImageCache().getImage(asset), 3, 5);
	}

	private boolean isEnlarged() {
		return viewTransform.getScale() > wheelListener.getMinScale() + EPSILON;
	}

	@Override
	public void addFrameListener(IFrameListener listener) {
		frameListeners.add(listener);
	}

	@Override
	public Rectangle2D getCurrentFrame() {
		return currentFrame;
	}

	@Override
	public void removeFrameListener(IFrameListener listener) {
		frameListeners.remove(listener);
	}

	private void fireFrameEvent() {
		for (IFrameListener listener : frameListeners)
			listener.frameChanged(this, asset.getStringId(), currentFrame.getX(), currentFrame.getY(),
					currentFrame.getWidth(), currentFrame.getHeight());
	}

	@Override
	public void addTransformListener(ITransformListener listener) {
		transformListeners.add(listener);
	}

	@Override
	public void removeTransformListener(ITransformListener listener) {
		transformListeners.remove(listener);
	}

	private void fireTransformEvent() {
		if (viewTransform != null && !isDisposed())
			for (ITransformListener listener : transformListeners)
				listener.transformChanged(this, viewTransform.getTranslateX(), viewTransform.getTranslateY(),
						viewTransform.getScale());
	}

	@Override
	public void setTransform(double translateX, double translateY, double scale) {
		if (viewTransform == null || viewTransform.getTranslateX() != translateX
				|| viewTransform.getTranslateY() != translateY || viewTransform.getScale() != scale) {
			if (viewTransform == null)
				viewTransform = new PAffineTransform();
			else
				viewTransform.setToIdentity();
			viewTransform.setScale(scale);
			viewTransform.setOffset(translateX, translateY);
			topCanvas.redraw();
			fireTransformEvent();
			fireFrameEvent();
		}
	}

	@Override
	public void setSync(boolean sync, boolean vertical) {
		this.sync = sync;
		this.vertical = vertical;
		if (topShell.isVisible())
			topCanvas.redraw();
	}

}
