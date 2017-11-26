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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IWorkbenchWindow;

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
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.hover.HoverInfo;
import com.bdaum.zoom.ui.internal.hover.IHoverInfo;
import com.bdaum.zoom.ui.internal.widgets.FadingShell;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.views.IImageViewer;

import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PAffineTransformException;

@SuppressWarnings("restriction")
public class ImageViewer implements KeyListener, IImageViewer, HelpListener, UiConstants, DisposeListener {

	private static final long USEPREVIEWTHRESHOLD = 4000000L;
	private static final double MAXZOOM = 4d;
	private static final int INCREMENT = 10;
	private static final int PAGEINCREMENT = 100;
	protected static final int HOTSPOTSIZE = 48;
	private static final Point pnt = new Point(0, 0);

	private final class InertiaMousePanListener implements MouseListener, MouseMoveListener {
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

		public void mouseDoubleClick(MouseEvent e) {
			if (highResVisible && e.button == 1) {
				cancel();
				mousePoint.x = e.x;
				mousePoint.y = e.y;
				resetView();
			}
		}

		public void mouseDown(MouseEvent e) {

			mouseButton = e.button;
			if (e.count == 1 && highResVisible) {
				mouseDown = true;
				targetPnt.x = mousePoint.x = mousePnt.x = e.x;
				targetPnt.y = mousePoint.y = mousePnt.y = e.y;
				altClick = e.button == 3 && modMask == SWT.BUTTON3 || (e.stateMask & modMask) == modMask;
				lastTime = ((long) e.time) & 0x00000000FFFFFFFF;
				lastMouseX = e.x;
				boolean enlarged = isEnlarged();
				setCursorForObject(e, enlarged ? CURSOR_GRABBING : null, CURSOR_MMINUS, CURSOR_MPLUS, null);
				if (!enlarged && e.button == 1) {
					previousRatio = 1 / viewTransform.getScale();
					fix(1, e.x, e.y);
				}
			}
		}

		public void mouseUp(MouseEvent e) {
			if (!Double.isNaN(previousRatio)) {
				fix(previousRatio, e.x, e.y);
				previousRatio = Double.NaN;
			}
			mouseDown = false;
			String cursor = isEnlarged() ? CURSOR_OPEN_HAND : null;
			setCursorForObject(e, cursor, cursor, cursor, null);
		}

		public void mouseMove(final MouseEvent e) {
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
		}

		private void performMouseAction(MouseEvent e) {
			if (altClick)
				zoom(xSpeed, targetPnt.x, targetPnt.y);
			else
				pan(xSpeed, ySpeed);
		}

	}

	public class HighResJob extends Job {

		private File imageFile;
		private boolean adv;
		private int cms1;
		private boolean firstCall;
		private ToolTip tooltip;

		public HighResJob(File imageFile, boolean advanced, int cms, boolean subSampling) {
			super("ImageLoading"); //$NON-NLS-1$
			this.imageFile = imageFile;
			this.adv = advanced;
			this.cms1 = cms;
			this.firstCall = subSampling;
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
							if (topShell.isDisposed())
								break;
							display.syncExec(() -> {
								if (!topShell.isDisposed())
									topShell.setAlpha(Math.max(0, topShell.getAlpha() - 16));
							});
							try {
								Thread.sleep(40);
							} catch (InterruptedException ex) {
								// do nothing
							}
						}
						image.dispose();
						image = null;
					}
					image = CoreActivator.getDefault().getHighresImageLoader().loadImage(null, status1, imageFile,
							asset.getRotation(), asset.getFocalLengthIn35MmFilm(), null, firstCall ? 1d : 0d, 1d, adv,
							cms1, bwmode, null, cropmode == ZImage.ORIGINAL ? Recipe.NULL : null, fileWatcher, opId,
							null);
					if (image != null) {
						display.syncExec(() -> {
							if (!topCanvas.isDisposed()) {
								Rectangle area = topCanvas.getClientArea();
								image.develop(monitor, display, cropmode, area.width, area.height);
							}
						});
						int superSamplingFactor = image.getSuperSamplingFactor();
						ibounds = image.getBounds();
						secondCallPossible = firstCall && superSamplingFactor > 1;
						final FadingShell formerShell = (previewShown) ? previewShell : bottomShell;
						fadein(status1, monitor, topShell, topCanvas, formerShell, !secondCallPossible);
						highResVisible = true;
						if (secondCallPossible) {
							if (status1.isOK()) {
								display.syncExec(() -> {
									Shell sh = topShell.getShell();
									Rectangle bounds = sh.getBounds();
									tooltip = new ToolTip(sh, SWT.NONE);
									tooltip.setLocation(bounds.x + bounds.width / 3, bounds.y);
									tooltip.setText(NLS.bind(Messages.getString("ImageViewer.downsampled"), //$NON-NLS-1$
											superSamplingFactor));
									tooltip.setMessage(Messages.getString("ImageViewer.click_for_fullres")); //$NON-NLS-1$
									tooltip.addSelectionListener(new SelectionAdapter() {
										@Override
										public void widgetSelected(SelectionEvent e) {
											highResVisible = false;
											viewTransform = null;
											tooltip.setVisible(false);
											highResJob = new HighResJob(file, advanced, cms, false);
											highResJob.schedule();
										}
									});
									tooltip.setVisible(true);
								});
								boolean[] ret = new boolean[1];
								while (true) {
									if (display.isDisposed())
										break;
									display.syncExec(() -> {
										if (formerShell.isDisposed())
											ret[0] = true;
										else if (!tooltip.isVisible() && highResVisible) {
											formerShell.close();
											ret[0] = true;
										}
									});
									if (ret[0] || monitor.isCanceled())
										break;
									try {
										Thread.sleep(100);
									} catch (InterruptedException e) {
										break;
									}
								}
							}
						} else {
							display.syncExec(() -> {
								if (!formerShell.isDisposed())
									formerShell.close();
							});
						}
					}
				} catch (UnsupportedOperationException e) {
					loadFailed = e.getMessage();
					display.syncExec(() -> {
						if (!bottomCanvas.isDisposed())
							bottomCanvas.redraw();
						if (!previewCanvas.isDisposed())
							previewCanvas.redraw();
					});
				}
				return status;
			} finally {
				fileWatcher.stopIgnoring(opId);
			}
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
			ExifTool tool = new ExifTool(ifile, true);
			try {
				previewImage = tool.getPreviewImage(false);
				if (previewImage != null && image == null) {
					ExifTool.fixOrientation(previewImage, asset.getOrientation(), asset.getRotation());
					previewImage.develop(monitor, display, ZImage.UNCROPPED, -1, -1);
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
			if (wheelkey == PreferenceConstants.WHEELSCROLLONLY)
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

	private static final double EPSILON = 0.005d;
	ZImage image;
	Rectangle ibounds;
	ZImage previewImage;
	PAffineTransform viewTransform;
	private Asset asset;
	private FadingShell bottomShell;
	private FadingShell previewShell;
	private FadingShell topShell;
	private Canvas bottomCanvas;
	private Display display;
	private File file;
	private Font font;
	private TextLayout tlayout;
	private HighResJob highResJob;
	private Canvas topCanvas;
	private InertiaMouseWheelListener wheelListener;
	private java.awt.Rectangle rectSrc = new java.awt.Rectangle();
	private java.awt.Rectangle rectDst = new java.awt.Rectangle();
	private Point mousePoint = new Point(0, 0);
	private boolean advanced;
	private int cms;
	private Canvas previewCanvas;
	private PreviewJob previewJob;
	private boolean highResVisible = false;
	private boolean previewShown = false;
	private boolean preview;
	private InertiaMousePanListener panListener;
	private RGB bwmode;
	private int lastMouseX;
	private int modMask;
	protected Shell controlShell;
	private Canvas controlCanvas;
	private String currentCustomCursor;
	boolean keyDown = false;
	private int cropmode;
	private int wheelkey;
	protected Point gesturePoint = new Point(0, 0);
	protected double previousMagnification = 1d;
	private Region controlRegion;
	private Job transferJob;
	private Image bwImage;
	private boolean enlarge;
	private boolean addNoise;
	private double oldRatio = -1d;
	private double oldScale;
	private int oldKeyCode;
	private long oldTime;
	protected int canvasXoffset;
	protected int canvasYoffset;
	private Shell shell;
	public String loadFailed;

	private void fadein(MultiStatus status, IProgressMonitor monitor, final FadingShell shell, final Canvas canvas,
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
				if (!shell.isDisposed())
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
	 * (non-Javadoc)
	 *
	 * @see
	 * com.bdaum.zoom.ui.views.IImageViewer#init(org.eclipse.swt.widgets.Display ,
	 * boolean)
	 */

	public void init(IWorkbenchWindow window, RGB bw, int crop) {
		this.shell = window.getShell();
		shell.addDisposeListener(this);
		this.display = shell.getDisplay();
		this.bwmode = bw;
		this.cropmode = crop;
		IPreferencesService preferencesService = Platform.getPreferencesService();
		advanced = preferencesService.getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.ADVANCEDGRAPHICS, false,
				null);
		cms = preferencesService.getInt(UiActivator.PLUGIN_ID, PreferenceConstants.COLORPROFILE, ImageConstants.SRGB,
				null);
		preview = preferencesService.getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.PREVIEW, false, null);
		int zoomKey = preferencesService.getInt(UiActivator.PLUGIN_ID, PreferenceConstants.ZOOMKEY, SWT.ALT, null);
		wheelkey = preferencesService.getInt(UiActivator.PLUGIN_ID, PreferenceConstants.WHEELKEY,
				PreferenceConstants.WHEELSHIFTPANS, null);
		enlarge = preferencesService.getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.ENLARGESMALL, false, null);
		addNoise = preferencesService.getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.ADDNOISE, true, null);
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
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IImageViewer#create()
	 */

	public void create() {
		Rectangle mbounds = UiUtilities.getSecondaryMonitorBounds(shell);
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
		controlCanvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				close();
			}
		});
		bottomCanvas.addKeyListener(this);
		previewCanvas.addKeyListener(this);
		MouseMoveListener mouseMoveListener = new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				Canvas canvas = (Canvas) e.widget;
				Rectangle clientArea = canvas.getClientArea();
				if (e.y < clientArea.y + 2 * HOTSPOTSIZE) {
					if (e.x < clientArea.x + 2 * HOTSPOTSIZE)
						showCloseButton(canvas.toDisplay(clientArea.x, clientArea.y));
					else if (e.x > clientArea.x + clientArea.width - 2 * HOTSPOTSIZE)
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
		};
		topCanvas.addMouseMoveListener(mouseMoveListener);
		bottomCanvas.addMouseMoveListener(mouseMoveListener);
		previewCanvas.addMouseMoveListener(mouseMoveListener);
		topCanvas.addKeyListener(this);
		topCanvas.addHelpListener(this);
		wheelListener = new InertiaMouseWheelListener();
		wheelListener.setMinScale(1d);
		wheelListener.setMaxScale(MAXZOOM);
		topCanvas.addMouseWheelListener(wheelListener);
		panListener = new InertiaMousePanListener();
		topCanvas.addMouseListener(panListener);
		topCanvas.addMouseMoveListener(panListener);
		PaintListener listener = new PaintListener() {
			public void paintControl(PaintEvent e) {
				Canvas canvas = (Canvas) e.widget;
				Shell shell = canvas.getShell();
				Rectangle sbnds = canvas.getClientArea();
				CssActivator.getDefault().setColors(shell);
				GC gc = e.gc;
				gc.setBackground(shell.getBackground());
				gc.fillRectangle(sbnds);
				if (!highResVisible) {
					Image im;
					if (previewImage != null)
						im = previewImage.getSwtImage(display, false, ZImage.UNCROPPED, SWT.DEFAULT, SWT.DEFAULT);
					else
						im = bwmode != null ? getBwImage(asset, bwmode) : getImage(asset);
					if (im != null) {
						Rectangle ibnds = im.getBounds();
						double factor = Math.min((double) sbnds.width / ibnds.width,
								(double) sbnds.height / ibnds.height);
						if (!enlarge) {
							double factor2 = Math.min((double) sbnds.width / asset.getWidth(),
									(double) sbnds.height / asset.getHeight());
							if (factor2 > 1d)
								factor /= factor2;
						}
						int w = (int) (ibnds.width * factor);
						int h = (int) (ibnds.height * factor);
						gc.drawImage(im, 0, 0, ibnds.width, ibnds.height, (sbnds.width - w) / 2, (sbnds.height - h) / 2,
								w, h);
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
						text = ((previewImage != null) ? Messages.getString("ImageViewer.loading_highres") : //$NON-NLS-1$
						Messages.getString("ImageViewer.loading_thumbnail")) //$NON-NLS-1$
								+ "\n\n" + getKeyboardHelp(false) + '\n'; //$NON-NLS-1$
					if (tlayout == null) {
						tlayout = new TextLayout(display);
						tlayout.setAlignment(SWT.CENTER);
						tlayout.setWidth(sbnds.width);
						if (font == null) {
							FontData fontData = display.getSystemFont().getFontData()[0];
							fontData.setHeight(18);
							font = new Font(display, fontData);
						}
						tlayout.setFont(font);
					}
					tlayout.setText(text);
					Rectangle tbounds = tlayout.getBounds();
					gc.setForeground(display.getSystemColor((file == null || loadFailed != null) ? SWT.COLOR_DARK_RED
							: (previewImage != null) ? SWT.COLOR_DARK_CYAN : SWT.COLOR_DARK_GREEN));
					int y = (sbnds.height - tbounds.height) / 2;
					tlayout.draw(gc, 1, y + 1);
					gc.setForeground(display.getSystemColor((file == null || loadFailed != null) ? SWT.COLOR_RED
							: (previewImage != null) ? SWT.COLOR_CYAN : SWT.COLOR_GREEN));
					tlayout.draw(gc, 0, y);
				}
			}
		};
		bottomCanvas.addPaintListener(listener);
		previewCanvas.addPaintListener(listener);
		topCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (image != null) {
					Rectangle sbounds = topCanvas.getClientArea();
					Shell shell = topShell.getShell();
					CssActivator.getDefault().setColors(shell);
					GC gc = e.gc;
					gc.setBackground(shell.getBackground());
					gc.fillRectangle(sbounds);
					if (viewTransform == null) {
						viewTransform = new PAffineTransform();
						double zoomfactor = Math.min((double) sbounds.width / ibounds.width,
								(double) sbounds.height / ibounds.height);
						if (!enlarge && zoomfactor > 1d)
							zoomfactor = 1d;
						wheelListener.setMinScale(zoomfactor);
						viewTransform.scale(zoomfactor, zoomfactor);
					}
					rectSrc.setBounds(ibounds.x, ibounds.y, ibounds.width, ibounds.height);
					viewTransform.transform(rectSrc, rectDst);
					int canvasWidth = Math.min((int) (rectDst.getWidth()), sbounds.width);
					int canvasHeight = Math.min((int) (rectDst.getHeight()), sbounds.height);
					canvasXoffset = Math.max(0, (sbounds.width - canvasWidth) / 2);
					canvasYoffset = Math.max(0, (sbounds.height - canvasHeight) / 2);
					rectSrc.setBounds(0, 0, canvasWidth, canvasHeight);
					viewTransform.inverseTransform(rectSrc, rectDst);
					int cropWidth = Math.min((int) (rectDst.getWidth()), ibounds.width);
					int cropHeight = Math.min((int) (rectDst.getHeight()), ibounds.height);
					int maxX = ibounds.width - cropWidth;
					int maxY = ibounds.height - cropHeight;
					int cropXoffset = Math.max(0, Math.min(maxX, (int) (rectDst.getX())));
					int cropYoffset = Math.max(0, Math.min(maxY, (int) (rectDst.getY())));
					if (advanced) {
						gc.setAntialias(SWT.ON);
						gc.setInterpolation(SWT.HIGH);
					}
					try {
						image.draw(gc, cropXoffset, cropYoffset, cropWidth, cropHeight, canvasXoffset, canvasYoffset,
								canvasWidth, canvasHeight, cropmode, sbounds.width, sbounds.height, true);
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

				}

			}
		});
		bottomCanvas.redraw();
		controlCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(Icons.closeButton.getImage(), 0, 0);
			}
		});
		controlCanvas.redraw();
		bottomShell.setBounds(mbounds);
		previewShell.setBounds(mbounds);
		topShell.setBounds(mbounds);
		controlShell.setBounds(Constants.OSX ? mbounds.x : mbounds.x + mbounds.width - HOTSPOTSIZE, mbounds.y,
				HOTSPOTSIZE, HOTSPOTSIZE);
		controlShell.setVisible(false);
		bottomShell.layout();
		previewShell.layout();
		topShell.layout();
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
		return NLS.bind(help ? Messages.getString("ImageViewer.use_mouse_wheel2") //$NON-NLS-1$
				: Messages.getString("ImageViewer.use_mouse_wheel"), //$NON-NLS-1$
				modMask == SWT.BUTTON3 ? Messages.getString("ImageViewer.right_mouse_button") //$NON-NLS-1$
						: modMask == SWT.ALT ? Messages.getString("ImageViewer.ALT") //$NON-NLS-1$
								: Messages.getString("ImageViewer.SHIFT")); //$NON-NLS-1$
	}

	private Shell createKioskShell(String label) {
		Shell shell = new Shell(display, SWT.NO_TRIM);
		shell.setText(Constants.APPNAME + label);
		shell.setFullScreen(true);
		shell.setLayout(new FillLayout());
		return shell;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IImageViewer#open(com.bdaum.zoom.cat.model.asset
	 * .Asset)
	 */

	public void open(Asset a) throws IOException {
		this.asset = a;
		URI uri = Core.getCore().getVolumeManager().findExistingFile(a, false);
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
		int previewSize = a.getPreviewSize();
		Rectangle bounds = display.getPrimaryMonitor().getBounds();
		mousePoint.x = bounds.width / 2;
		mousePoint.y = bounds.height / 2;
		boolean usePreview = preview && ((previewSize < 0 || previewSize * 2 >= Math.max(bounds.width, bounds.height))
				&& a.getRotation() == 0);
		bottomShell.open();
		if (file != null) {
			usePreview &= file.length() > USEPREVIEWTHRESHOLD;
			if (usePreview) {
				Rectangle ibnds = Core.getCore().getImageCache().getImage(a).getBounds();
				usePreview = (Math.max(ibnds.width, ibnds.height) < Math.max(bounds.width, bounds.height));
			}
			if (usePreview) {
				previewShell.open();
				previewJob = new PreviewJob(file);
				previewJob.schedule();
			}
			topShell.open();
			highResJob = new HighResJob(file, advanced, cms, true);
			highResJob.schedule();
			while (topShell != null && !topShell.isDisposed())
				if (!display.readAndDispatch())
					display.sleep();
		} else
			while (bottomShell != null && !bottomShell.isDisposed())
				if (!display.readAndDispatch())
					display.sleep();
		if (tempFile != null)
			tempFile.delete();
	}

	public void keyPressed(KeyEvent e) {
		if (keyDown)
			releaseKey(e);
		keyDown = true;
	}

	public void keyReleased(KeyEvent e) {
		releaseKey(e);
		keyDown = false;
	}

	private void releaseKey(KeyEvent e) {
		long time = e.time & 0xFFFFFFFFL;
		if (oldKeyCode == e.keyCode && time - oldTime < 100L)
			return;
		oldTime = time;
		oldKeyCode = e.keyCode;
		if (e.character == Messages.getString("ImageViewer.w").charAt(0)) { //$NON-NLS-1$
			Rectangle sbounds = topShell.getBounds();
			double factor = (double) ibounds.width / sbounds.width;
			fix(factor, mousePoint.x, mousePoint.y);
		} else if (e.character == Messages.getString("ImageViewer.h").charAt(0)) { //$NON-NLS-1$
			Rectangle sbounds = topShell.getBounds();
			double factor = (double) ibounds.height / sbounds.height;
			fix(factor, mousePoint.x, mousePoint.y);
		} else
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
					viewTransform.translate(-INCREMENT * f, 0);
					topCanvas.redraw();
					break;
				case SWT.ARROW_RIGHT:
					viewTransform.translate(INCREMENT * f, 0);
					topCanvas.redraw();
					break;
				case SWT.ARROW_UP:
					viewTransform.translate(0, -INCREMENT * f);
					topCanvas.redraw();
					break;
				case SWT.ARROW_DOWN:
					viewTransform.translate(0, INCREMENT * f);
					topCanvas.redraw();
					break;
				case SWT.HOME:
					viewTransform.translate(-PAGEINCREMENT * f, 0);
					topCanvas.redraw();
					break;
				case SWT.END:
					viewTransform.translate(PAGEINCREMENT * f, 0);
					topCanvas.redraw();
					break;
				case SWT.PAGE_UP:
					viewTransform.translate(0, -PAGEINCREMENT * f);
					topCanvas.redraw();
					break;
				case SWT.PAGE_DOWN:
					viewTransform.translate(0, PAGEINCREMENT * f);
					topCanvas.redraw();
					break;
				case SWT.F2:
					metadataRequested(asset);
				}
				break;
			}
	}

	public void close() {
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
		if (tlayout != null)
			tlayout.dispose();
		if (font != null)
			font.dispose();
		if (image != null)
			image.dispose();
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
	}

	private void zoom(double factor, double x, double y) {
		double vx = viewTransform.getTranslateX();
		double vy = viewTransform.getTranslateY();
		zoomDelta(1d + factor * 0.01d, x - vx, y - vy);
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
			} catch (PAffineTransformException e) {
				// ignore
			}
		}
	}

	private void setCursorForObject(MouseEvent e, String surface, String altLeft, String altRight, String dflt) {
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
			topCanvas.setCursor(UiActivator.getDefault().getCursor(topCanvas.getDisplay(), cursor));
		}
	}

	public void helpRequested(HelpEvent e) {
		ToolTip toolTip = new ToolTip(topShell.getShell(), SWT.BALLOON);
		toolTip.setAutoHide(true);
		pnt.x = pnt.y = 0;
		toolTip.setLocation(pnt);
		toolTip.setText(Messages.getString("ImageViewer.image_viewer")); //$NON-NLS-1$
		toolTip.setMessage(getKeyboardHelp(true));
		toolTip.setVisible(true);
	}

	private void metadataRequested(Asset asset) {
		Shell shell = topShell.getShell();
		ToolTip toolTip = new ToolTip(shell, SWT.BALLOON);
		toolTip.setAutoHide(true);
		Rectangle bounds = shell.getDisplay().getPrimaryMonitor().getBounds();
		pnt.x = bounds.width / 2;
		pnt.y = bounds.height / 2;
		toolTip.setLocation(pnt);
		toolTip.setText(NLS.bind(Messages.getString("SlideShowPlayer.metadata"), asset.getName())); //$NON-NLS-1$
		IHoverInfo hover = new HoverInfo(asset, null, UiActivator.getDefault().getHoverNodes());
		toolTip.setMessage(hover.getText());
		toolTip.setVisible(true);
	}

	public String getName() {
		return Messages.getString("ImageViewer.viewer_name"); //$NON-NLS-1$
	}

	public String getId() {
		return "com.bdaum.zoom.image"; //$NON-NLS-1$
	}

	public void setId(String id) {
		// do nothing
	}

	public void setName(String name) {
		// do nothing
	}

	public boolean canHandleRemote() {
		return false;
	}

	private Image getImage(Asset asset) {
		Image im = Core.getCore().getImageCache().getImage(asset);
		return (addNoise) ? ImageUtilities.applyNoise(im) : im;
	}

	private boolean isEnlarged() {
		return viewTransform.getScale() > wheelListener.getMinScale() + EPSILON;
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		close();
	}

}
