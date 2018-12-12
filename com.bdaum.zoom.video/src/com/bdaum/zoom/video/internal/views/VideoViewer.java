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
package com.bdaum.zoom.video.internal.views;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.ImportState;
import com.bdaum.zoom.core.internal.peer.AssetOrigin;
import com.bdaum.zoom.core.internal.peer.ConnectionLostException;
import com.bdaum.zoom.core.internal.peer.IPeerService;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.UpdateThumbnailOperation;
import com.bdaum.zoom.ui.IStateListener;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.views.AbstractMediaViewer;
import com.bdaum.zoom.ui.internal.widgets.FadingShell;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.video.internal.VideoActivator;
import com.bdaum.zoom.video.internal.widgets.VideoControl;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;

@SuppressWarnings("restriction")
public class VideoViewer extends AbstractMediaViewer implements PaintListener {

	private static final String[] VLC_ARGS = { // "--intf", "dummy", /* no
			// interface */
			// "--vout", "dummy", /* we don't want video (output) */
			// "--noaudio", /* we don't want audio (decoding) */
			"--no-video-title-show", /* nor the filename displayed *///$NON-NLS-1$
			"--no-stats", /* no stats *///$NON-NLS-1$
			"--no-sub-autodetect-file", /* we don't want subtitles *///$NON-NLS-1$
			// "--no-inhibit", /* we don't want interfaces */
			// "--no-disable-screensaver", /* we don't want interfaces */
			"--no-snapshot-preview", /* no blending in dummy vout *///$NON-NLS-1$
	};

	public boolean running;
	static final int TICK = 17;
	private Canvas topCanvas;
	private Shell topShell;
	private Image image;
	private TextLayout tlayout;
	private String message;
	private String errorMessage;
	private FadingShell controlShell;
	private int mouseMovements;
	private PlayingThread playingThread;
	private byte[] thumbnailJpeg;
	private Job transferJob;
	private File tempFile;
	private boolean enlarge;
	private boolean addNoise;
	private boolean soundOn = true;
	private int currentVolume = 100;
	private MediaPlayerFactory factory;

	public void init(IWorkbenchWindow window, int kind, RGB bw, int cropmode) {
		super.init(window, kind, bw, cropmode);
		errorMessage = null;
		message = null;
		mouseMovements = 0;
		running = false;
		IPreferencesService preferencesService = Platform.getPreferencesService();
		enlarge = preferencesService.getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.ENLARGESMALL, false, null);
		addNoise = preferencesService.getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.ADDNOISE, true, null);
	}

	private void redraw() {
		if (!display.isDisposed())
			display.asyncExec(() -> {
				if (!topCanvas.isDisposed())
					topCanvas.redraw();
			});
	}

	public void create() {
		super.create();
		topShell = createKioskShell(getName());
		topCanvas = new Canvas(topShell, SWT.DOUBLE_BUFFERED);
		topCanvas.addPaintListener(this);
		topCanvas.addKeyListener(this);
		createTransparentCursor();
		topCanvas.setCursor(transparentCursor);
		topCanvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (running && ++mouseMovements > 3) {
					if (controlShell == null) {
						if (playingThread != null && !playingThread.isPaused())
							playingThread.pause();
						showControl();
					}
					mouseMovements = 0;
				}
			}
		});
		CssActivator.getDefault().setColors(topShell);
	}

	private void showControl() {
		if (!topShell.isDisposed()) {
			display.asyncExec(() -> {
				if (!topShell.isDisposed())
					doShowControl();
			});
		}
	}

	public void open(Asset[] assets) throws IOException {
		asset = assets[0];
		URI uri = Core.getCore().getVolumeManager().findExistingFile(asset, false);
		if (uri != null) {
			if (Constants.FILESCHEME.equals(uri.getScheme())) {
				IPeerService peerService = Core.getCore().getPeerService();
				AssetOrigin assetOrigin = peerService != null ? peerService.getAssetOrigin(asset.getStringId()) : null;
				if (peerService != null && assetOrigin != null) {
					try {
						if (peerService.checkCredentials(IPeerService.VIEW, asset.getSafety(), assetOrigin)) {
							transferJob = peerService.scheduleTransferJob(asset, assetOrigin);
							if (transferJob != null) {
								try {
									tempFile = transferJob.getAdapter(File.class);
									uri = tempFile.toURI();
									Job.getJobManager().join(transferJob, null);
								} catch (OperationCanceledException e) {
									transferJob = null;
									return;
								} catch (InterruptedException e) {
									Job.getJobManager().cancel(transferJob);
									transferJob = null;
									return;
								}
							}
						} else {
							AcousticMessageDialog.openError(null, Messages.VideoViewer_access_restricted,
									Messages.VideoViewer_rights_not_sufficient);
							return;
						}
					} catch (ConnectionLostException e) {
						AcousticMessageDialog.openError(null, Messages.VideoViewer_connection_lost,
								e.getLocalizedMessage());
						return;
					}
				}
			} else
				return;
		} else
			return;
		message = Messages.VideoViewer_enlarged_thumbnail;
		if (topShell == null)
			create();
		topShell.open();
		factory = VideoActivator.getDefault().vlcCheck(topShell, VLC_ARGS);
		if (factory == null)
			return;
		playingThread = new PlayingThread(uri, factory, new ThreadUINotifier() {
			public void updateFrame(ImageData imageData, long position, long seek) {
				message = null;
				running = true;
				if (imageData != null) {
					if (bwmode != null)
						ImageUtilities.convert2Bw(imageData, bwmode);
					if (image != null)
						image.dispose();
					image = new Image(display, imageData);
					redraw();
				}
			}

			public void applyStop() {
				close();
			}

			public void applyPause() {
				showControl();
			}

			public void showError(String msg) {
				errorMessage = msg;
				redraw();
			}

		});
		if (!playingThread.startVideoThread()) {
			String volume = asset.getVolume();
			errorMessage = volume == null || volume.trim().isEmpty() ? Messages.VideoViewer_no_stream
					: NLS.bind(Messages.VideoViewer_currently_not_available, volume);
		}
	}

	public boolean close() {
		fireStateEvent(IStateListener.CLOSED);
		if (playingThread != null)
			playingThread.stopPlaying();
		if (transferJob != null)
			Job.getJobManager().cancel(transferJob);
		if (tempFile != null)
			tempFile.delete();
		if (factory != null)
			factory.release();
		if (controlShell != null) {
			controlShell.close();
			controlShell = null;
		}
		if (topShell != null) {
			topShell.close();
			topShell = null;
		}
		if (image != null) {
			image.dispose();
			image = null;
		}
		if (tlayout != null) {
			tlayout.dispose();
			tlayout = null;
		}
		if (thumbnailJpeg != null) {
			OperationJob.executeOperation(new UpdateThumbnailOperation(asset, thumbnailJpeg), VideoViewer.this);
			thumbnailJpeg = null;
		}
		return super.close();
	}

	public void paintControl(PaintEvent e) {
		GC gc = e.gc;
		gc.setBackground(topShell.getBackground());
		Rectangle area = topCanvas.getClientArea();
		gc.fillRectangle(area);
		double factor2 = 1d;
		boolean thumb = false;
		if (image == null) {
			thumb = true;
			image = Core.getCore().getImageCache().getImage(asset);
			if (addNoise)
				image = ImageUtilities.applyNoise(image);
			factor2 = Math.max(1d,
					Math.min((double) area.width / asset.getWidth(), (double) area.height / asset.getHeight()));
		}
		Rectangle bounds = image.getBounds();
		double factor = Math.min((double) area.width / bounds.width, (double) area.height / bounds.height);
		if (thumb)
			factor /= factor2;
		else if (!enlarge && factor > 1d)
			factor = 1d;
		int w = (int) (bounds.width * factor);
		int h = (int) (bounds.height * factor);
		gc.drawImage(image, 0, 0, bounds.width, bounds.height, (area.width - w) / 2, (area.height - h) / 2, w, h);
		if (message != null || errorMessage != null) {
			if (tlayout == null) {
				tlayout = new TextLayout(display);
				tlayout.setAlignment(SWT.CENTER);
				tlayout.setWidth(area.width);
				tlayout.setFont(JFaceResources.getFont(UiConstants.VIEWERFONT));
			}
			tlayout.setText(errorMessage != null ? errorMessage : message);
			Rectangle tbounds = tlayout.getBounds();
			gc.setForeground(display.getSystemColor(errorMessage != null ? SWT.COLOR_DARK_RED : SWT.COLOR_DARK_GREEN));
			int y = (area.height - tbounds.height) / 2;
			tlayout.draw(gc, 1, y + 1);
			gc.setForeground(display.getSystemColor(errorMessage != null ? SWT.COLOR_RED : SWT.COLOR_GREEN));
			tlayout.draw(gc, 0, y);
		}
	}

	public void releaseKey(KeyEvent e) {
		switch (e.character) {
		case ' ':
			if (playingThread != null && playingThread.isPaused())
				playingThread.resumePlaying();
			closeControl();
			break;
		default:
			switch (e.keyCode) {
			case SWT.ESC:
				if (playingThread != null)
					playingThread.stopPlaying();
				close();
				break;
			}
			break;
		}
	}

	private void doShowControl() {
		topCanvas.setCursor(null);
		final Shell shell = new Shell(topShell, SWT.TOOL);
		controlShell = new FadingShell(shell, true, Constants.SLIDE_TRANSITION_FADE);
		shell.setLayout(new FillLayout());
		final VideoControl videoControl = new VideoControl(shell, SWT.NONE);
		if (playingThread != null) {
			long currentPosition = playingThread.getCurrentPosition();
			long duration = playingThread.getDuration();
			videoControl.setPosition((int) ((double) currentPosition / duration * 1000d + 0.5d));
			videoControl.setLoudness(currentVolume);
			videoControl.setSound(soundOn);
			updateInfo(videoControl, currentPosition, duration);
		}
		videoControl.addListener(new Listener() {
			@Override
			public void handleEvent(Event event) {
				switch (event.detail & VideoControl.EVENTTYPES) {
				case VideoControl.PLAY:
					closeControl();
					if (playingThread != null && playingThread.isPaused())
						playingThread.resumePlaying();
					break;
				case VideoControl.STOP:
					close();
					break;
				case VideoControl.SNAP:
					if (image != null) {
						Meta meta = Core.getCore().getDbManager().getMeta(true);
						int width = ImportState.computeThumbnailWidth(meta.getThumbnailResolution());
						int height = width / 4 * 3;
						ZImage zImage = new ZImage(image, null);
						if (zImage.height > zImage.width) {
							int www = height;
							height = width;
							width = www;
						}
						zImage.setScaling(width, height, false, ImportState.MCUWidth, null);
						ByteArrayOutputStream out = new ByteArrayOutputStream(20000);
						try {
							zImage.saveToStream(null, true, ZImage.CROPMASK, SWT.DEFAULT, SWT.DEFAULT, out,
									meta.getWebpCompression() ? ZImage.IMAGE_WEBP : SWT.IMAGE_JPEG,
									meta.getJpegQuality());
							thumbnailJpeg = out.toByteArray();
						} catch (IOException e1) {
							// should never happen
						}
					}
					break;
				case VideoControl.SOUND:
					if (playingThread != null) {
						soundOn = (event.detail & VideoControl.VOLUMEMASK) > 0;
						playingThread.setVolume(soundOn ? currentVolume : 0);
					}
					break;
				case VideoControl.LOUDNESS:
					if (playingThread != null)
						playingThread.setVolume(currentVolume = event.detail & VideoControl.VOLUMEMASK);
					break;
				default:
					if (playingThread != null) {
						long duration = playingThread.getDuration();
						playingThread.seek((long) (event.detail / 1000d * duration + 0.5d));
						updateInfo(videoControl, playingThread.getCurrentPosition(), duration);
					}
					break;
				}			}
		});
		shell.setText(asset.getName());
		shell.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				releaseKey(e);
			}
		});
		shell.pack();
		Point size = shell.getSize();
		Rectangle bounds = topShell.getBounds();
		shell.setLocation(bounds.x + (bounds.width - size.x) / 2, bounds.y + bounds.height - size.y);
		CssActivator.getDefault().setColors(controlShell.getShell());
		controlShell.setAlpha(0);
		controlShell.open();
		controlShell.forceActive();
		controlShell.forceFocus();
		controlShell.setFocus();
		for (int i = 1; i <= 10; i++) {
			if (i > 1)
				sleepTick(TICK);
			controlShell.setAlpha(i * 15);
		}
	}

	private static void updateInfo(VideoControl videoControl, long currentPosition, long duration) {
		videoControl
				.setInfo(NLS.bind(Messages.VideoViewer_n_of_m, formatPeriod(currentPosition), formatPeriod(duration)));
	}

	private static String formatPeriod(long time) {
		StringBuilder sb = new StringBuilder();
		int hours = (int) (time / 3600000L);
		if (hours != 0) {
			sb.append(hours).append(':');
			time -= hours * 3600000L;
		}
		int minutes = (int) (time / 60000L);
		if (hours != 0 || minutes != 0) {
			sb.append(hours != 0 ? String.valueOf(minutes + 100).substring(1) : minutes).append(':');
			time -= minutes * 60000L;
		}
		int seconds = (int) (time / 1000L);
		if (hours != 0 || minutes != 0 || seconds != 0) {
			sb.append(hours != 0 || minutes != 0 ? String.valueOf(seconds + 100).substring(1) : seconds).append(':');
			time -= seconds * 1000L;
		}
		return sb.append('.').append(String.valueOf(time + 1000).substring(1)).toString();
	}

	protected void closeControl() {
		if (controlShell != null && !controlShell.isDisposed()) {
			int alpha = controlShell.getAlpha();
			while (alpha > 15) {
				controlShell.setAlpha(alpha -= 15);
				sleepTick(TICK);
			}
			controlShell.close();
			for (int i = 0; i < 60; i++)
				sleepTick(TICK);
			controlShell = null;
		}
		mouseMovements = 0;
	}

	public boolean canHandleRemote() {
		return true;
	}

	@Override
	public boolean isDisposed() {
		return topShell == null;
	}
	
}
