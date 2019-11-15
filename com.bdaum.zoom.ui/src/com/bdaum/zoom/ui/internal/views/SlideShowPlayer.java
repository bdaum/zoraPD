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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.batch.internal.ExifTool;
import com.bdaum.zoom.batch.internal.IFileWatcher;
import com.bdaum.zoom.batch.internal.LoaderListener;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.Group;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.slideShow.Slide;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideShowImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Ticketbox;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.css.CSSProperties;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.CompoundOperation;
import com.bdaum.zoom.operations.internal.AddAlbumOperation;
import com.bdaum.zoom.operations.internal.DeleteOperation;
import com.bdaum.zoom.operations.internal.RateOperation;
import com.bdaum.zoom.operations.internal.RotateOperation;
import com.bdaum.zoom.operations.internal.VoiceNoteOperation;
import com.bdaum.zoom.program.IRawConverter;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.AbstractKiosk;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.AlbumSelectionDialog;
import com.bdaum.zoom.ui.internal.dialogs.RatingDialog;
import com.bdaum.zoom.ui.internal.dialogs.SelectSlideDialog;
import com.bdaum.zoom.ui.internal.dialogs.SlideShowSaveDialog;
import com.bdaum.zoom.ui.internal.dialogs.VoiceNoteDialog;
import com.bdaum.zoom.ui.internal.hover.HoverInfo;
import com.bdaum.zoom.ui.internal.hover.HoveringController;
import com.bdaum.zoom.ui.internal.hover.IGalleryHover;
import com.bdaum.zoom.ui.internal.hover.IHoverInfo;
import com.bdaum.zoom.ui.internal.widgets.FadingShell;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class SlideShowPlayer extends AbstractKiosk implements MouseListener, KeyListener {

	static final int TICK = 17;
	static final int LONGTICK = 60;
	static final int NTICKS = (LONGTICK + TICK / 2) / TICK;
	public static final long STARTUPDELAY = 3000L;
	private static final int CACHESIZE = 3;

	private static class Cache extends LinkedHashMap<String, ZImage> {
		private static final long serialVersionUID = -3139446943713462728L;
		private int size;

		public Cache(int size) {
			super(size * 3 / 2);
			this.size = size;
		}

		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<String, ZImage> eldest) {
			if (size() > size) {
				eldest.getValue().dispose();
				return true;
			}
			return false;
		}
	}

	public class PreparationJob extends Job {

		private final int position;
		private final Rectangle bounds;

		public PreparationJob(int position, Rectangle bounds) {
			super(Messages.getString("SlideShowPlayer.slideshow_preparation")); //$NON-NLS-1$
			this.position = position;
			this.bounds = bounds;
			setPriority(Job.INTERACTIVE);
		}

		@Override
		public boolean belongsTo(Object family) {
			return Constants.SLIDESHOW == family;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IDbManager dbManager = Core.getCore().getDbManager();
			if (slides == null)
				slides = new ArrayList<SlideImpl>(dbManager.obtainByIds(SlideImpl.class, slideshow.getEntry()));
			int plannedDuration = 0;
			SlideRequest lastSectionRequest = null;
			IVolumeManager volumeManager = Core.getCore().getVolumeManager();
			SlideRequest previousRequest = null;
			for (SlideImpl slide : slides) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				String assetId = slide.getAsset();
				if (assetId == null) {
					++total;
					int d = slide.getDelay() + slide.getDuration();
					duration += d;
					plannedDuration += d;
					if (plannedDuration > position && firstSlide < 0) {
						firstSlide = requestList.size();
						positionFirstSlide = position + d - plannedDuration;
					}
					SlideRequest request = new SlideRequest(requestList.size(), slide, null, 0, -1, 0);
					lastSectionRequest = request;
					requestList.add(request);
					if (previousRequest != null)
						previousRequest.setOverlap(Math.min(0, slide.getFadeIn() - slide.getDelay()));
					previousRequest = request;
				} else {
					AssetImpl asset = dbManager.obtainAsset(assetId);
					if (asset != null) {
						++total;
						URI uri = volumeManager.findExistingFile(asset, false);
						int d = slide.getDelay() + slide.getDuration();
						plannedDuration += d;
						if (uri != null) {
							duration += d;
							if (plannedDuration > position && firstSlide < 0) {
								firstSlide = requestList.size();
								positionFirstSlide = position + d - plannedDuration;
							}
							SlideRequest request = new SlideRequest(requestList.size(), slide, uri, asset.getRotation(),
									asset.getRating(), slideshow.getFromPreview() ? asset.getPreviewSize() : 0);
							request.addAsset(asset);
							requestList.add(request);
							if (lastSectionRequest != null)
								lastSectionRequest.addAsset(asset);
							if (previousRequest != null)
								previousRequest.setOverlap(Math.min(0, slide.getFadeIn() - slide.getDelay()));
							previousRequest = request;
						} else
							++offline;
					}
				}
			}
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			if (firstSlide >= 0) {
				new ImageProviderJob(bottomShell, bounds, advanced, cms).schedule();
				requests.add(requestList.get(firstSlide));
			}
			return Status.OK_STATUS;
		}

	}

	public class ApplyChangesJob extends CustomJob {

		private List<SlideRequest> reqList;

		public ApplyChangesJob(List<SlideRequest> requestList) {
			super(Messages.getString("SlideShowPlayer.applying_changes")); //$NON-NLS-1$
			this.reqList = requestList;
			setPriority(Job.LONG);
		}

		@Override
		public boolean belongsTo(Object family) {
			return Constants.OPERATIONJOBFAMILY == family || Constants.CRITICAL == family;
		}

		@Override
		protected IStatus runJob(IProgressMonitor monitor) {
			List<Asset> deleteFromCat = new ArrayList<Asset>();
			List<Asset> deleteFromDisk = new ArrayList<Asset>();
			final List<SlideImpl> removeFromShow = new ArrayList<SlideImpl>();
			Map<Integer, List<Asset>> rated = new HashMap<Integer, List<Asset>>();
			Map<Integer, List<Asset>> rotated = new HashMap<Integer, List<Asset>>();
			Map<SmartCollectionImpl, List<Asset>> addedToAlbum = new HashMap<SmartCollectionImpl, List<Asset>>();
			Map<Asset, VoiceNoteOperation> voice = new HashMap<Asset, VoiceNoteOperation>();
			final IDbManager dbManager = Core.getCore().getDbManager();
			final AssetSelection assetSelection = new AssetSelection(reqList.size());
			for (SlideRequest request : reqList) {
				Asset asset = request.getAsset();
				if (asset != null) {
					if (request.isSelected())
						assetSelection.add(asset);
					if (request.isDeleted()) {
						if (request.getDeleteFromDisk())
							deleteFromDisk.add(asset);
						else
							deleteFromCat.add(asset);
						for (List<Asset> ratedList : rated.values())
							ratedList.remove(asset);
						for (List<Asset> rotatedList : rotated.values())
							rotatedList.remove(asset);
						for (List<Asset> albumList : addedToAlbum.values())
							albumList.remove(asset);
						voice.remove(asset);
					} else {
						int rating = request.getRating();
						if (rating != RatingDialog.ABORT && rating != asset.getRating()) {
							deleteFromDisk.remove(asset);
							deleteFromCat.remove(asset);
							for (List<Asset> ratedList : rated.values())
								ratedList.remove(asset);
							List<Asset> ratedList = rated.get(rating);
							if (ratedList == null) {
								ratedList = new ArrayList<Asset>();
								rated.put(rating, ratedList);
							}
							ratedList.add(asset);
						}
						int rot = (request.getRotation() - asset.getRotation()) % 360;
						if (rot != 0) {
							deleteFromDisk.remove(asset);
							deleteFromCat.remove(asset);
							for (List<Asset> rotatedList : rotated.values())
								rotatedList.remove(asset);
							List<Asset> rotatedList = rotated.get(rot);
							if (rotatedList == null) {
								rotatedList = new ArrayList<Asset>();
								rotated.put(rot, rotatedList);
							}
							rotatedList.add(asset);
						}
						SmartCollectionImpl[] albums = request.getAlbums();
						if (albums != null && albums.length > 0) {
							deleteFromDisk.remove(asset);
							deleteFromCat.remove(asset);
							for (List<Asset> albumList : addedToAlbum.values())
								albumList.remove(asset);
							for (SmartCollectionImpl album : albums) {
								List<Asset> albumList = addedToAlbum.get(album);
								if (albumList == null)
									addedToAlbum.put(album, albumList = new ArrayList<Asset>());
								albumList.add(asset);
							}
						}
						if (request.annotationChanged())
							voice.put(asset, new VoiceNoteOperation(asset, request.getSourceURI(),
									request.getTargetURI(), request.getNoteText(), request.getSvg()));
					}
					if (request.isRemoveFromShow())
						removeFromShow.add(request.getSlide());
				}
			}
			if (!assetSelection.isEmpty()) {
				final Shell shell = parentWindow.getShell();
				if (!shell.isDisposed())
					shell.getDisplay().asyncExec(() -> {
						if (!shell.isDisposed())
							Ui.getUi().getNavigationHistory(parentWindow).postSelection(assetSelection);
					});
			}
			if (!removeFromShow.isEmpty() && !slideshow.getAdhoc()) {
				SlideshowView editor = null;
				IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (ww != null) {
					IWorkbenchPage page = ww.getActivePage();
					if (page != null)
						editor = (SlideshowView) UiUtilities.findViewNoRestore(page, SlideshowView.ID);
				}
				if (editor != null && editor.getContent() != slideshow)
					editor = null;
				if (editor == null) {
					dbManager.safeTransaction(() -> {
						for (SlideImpl slide : removeFromShow) {
							dbManager.delete(slide);
							slideshow.removeEntry(slide.getStringId());
						}
						dbManager.store(slideshow);
					});
				} else {
					final SlideshowView view = editor;
					if (!display.isDisposed())
						display.asyncExec(() -> {
							if (!display.isDisposed())
								view.removeSlides(removeFromShow);
						});
				}
				Core.getCore().fireAssetsModified(null, null);
			}
			CompoundOperation compoundOperation = new CompoundOperation(
					Messages.getString("SlideShowPlayer.modifications_during_slideshow")); //$NON-NLS-1$
			if (!deleteFromCat.isEmpty())
				compoundOperation.addOperation(new DeleteOperation(deleteFromCat, false, null, null, null,
						UiActivator.getDefault().createImportConfiguration(SlideShowPlayer.this)));
			if (!deleteFromDisk.isEmpty())
				compoundOperation.addOperation(new DeleteOperation(deleteFromDisk, true, null, null, null,
						UiActivator.getDefault().createImportConfiguration(SlideShowPlayer.this)));
			for (Map.Entry<Integer, List<Asset>> entry : rated.entrySet())
				compoundOperation.addOperation(new RateOperation(entry.getValue(), entry.getKey()));
			for (Map.Entry<Integer, List<Asset>> entry : rotated.entrySet())
				compoundOperation.addOperation(new RotateOperation(entry.getValue(), entry.getKey()));
			for (Map.Entry<SmartCollectionImpl, List<Asset>> entry : addedToAlbum.entrySet())
				compoundOperation.addOperation(new AddAlbumOperation(entry.getKey(), entry.getValue(), null));
			for (VoiceNoteOperation op : voice.values())
				compoundOperation.addOperation(op);
			if (!compoundOperation.isEmpty())
				OperationJob.executeOperation(compoundOperation, SlideShowPlayer.this);
			return Status.OK_STATUS;
		}
	}

	public class ImageProviderJob extends Job implements LoaderListener {

		private MultiStatus status;
		private Shell shell;
		private boolean adv;
		private Rectangle bounds;
		private SlideRequest request;
		private int cms1;

		public ImageProviderJob(Shell shell, Rectangle bounds, boolean advanced, int cms) {
			super(Messages.getString("SlideShowPlayer.image_provider")); //$NON-NLS-1$
			this.shell = shell;
			this.bounds = bounds;
			this.adv = advanced;
			this.cms1 = cms;
			setPriority(Job.LONG);
			setSystem(true);
		}

		@Override
		public boolean belongsTo(Object family) {
			return Constants.SLIDESHOW == family;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			String opId = java.util.UUID.randomUUID().toString();
			CoreActivator activator = CoreActivator.getDefault();
			IFileWatcher fileWatcher = activator.getFileWatchManager();
			status = new MultiStatus(UiActivator.PLUGIN_ID, 0,
					Messages.getString("SlideShowPlayer.image_loading_report"), null); //$NON-NLS-1$
			try (Ticketbox box = new Ticketbox()) {
				while (!monitor.isCanceled()) {
					try {
						request = requests.take();
						if (shell.isDisposed() || request == FINALREQUEST || monitor.isCanceled())
							break;
						SlideImpl slide = request.getSlide();
						ZImage zImage = cache.get(slide.getStringId());
						if (zImage != null) {
							request.setImage(zImage);
							answers.add(request);
							request = null;
							continue;
						}
						int zoom = slide.getZoom();
						float zp = zoom == 0 ? 1f : (1f + zoom / 100f) * 0.631f;
						int preferredWidth = (int) (bounds.width * zp);
						int preferredHeight = (int) (bounds.height * zp);
						ZImage image = null;
						URI uri = request.getUri();
						if (uri == null)
							image = createSectionBreak(shell, request);
						else {
							File file = null;
							File tempFile = null;
							if (Constants.FILESCHEME.equals(uri.getScheme())) {
								file = new File(uri);
								if (slideshow.getVoiceNotes()) {
									URI voiceURI = Core.getCore().getVolumeManager().findVoiceFile(request.getAsset());
									if (voiceURI != null)
										try {
											URL url = voiceURI.toURL();
											request.setVoiceUrl(url);
											request.setVoiceLength((int) ((UiActivator.getDefault()
													.getSoundfileLengthInMicroseconds(url) + 999L) / 1000L));
										} catch (MalformedURLException e) {
											// should not happen
										}
								}
							} else
								try {
									shell.getDisplay().syncExec(() -> {
										if (!text.getText().isEmpty()) {
											text.setText(
													text.getText() + Messages.getString("SlideShowPlayer.downloading")); //$NON-NLS-1$
											bottomCanvas.redraw();
										}
									});
									tempFile = file = box.download(uri);
								} catch (IOException e) {
									status.add(new Status(IStatus.ERROR, UiActivator.PLUGIN_ID,
											NLS.bind(Messages.getString("SlideShowPlayer.download_failed"), //$NON-NLS-1$
													uri),
											e));
								}
							if (file != null) {
								Recipe recipe = null;
								boolean isRawOrDng = ImageConstants.isRaw(file.getName(), true);
								if (isRawOrDng) {
									IRawConverter currentRawConverter = BatchActivator.getDefault()
											.getCurrentRawConverter(false);
									if (currentRawConverter != null)
										recipe = currentRawConverter.getRecipe(file.toURI().toString(), false,
												new IFocalLengthProvider() {
													public double get35mm() {
														return request.getAsset().getFocalLengthIn35MmFilm();
													}
												}, null, null);
								}
								if (monitor.isCanceled())
									break;
								if (recipe == null || recipe == Recipe.NULL) {
									int previewSize = request.getPreviewSize();
									if (previewSize < 0 || previewSize >= Math.max(bounds.width, bounds.height)) {
										try {
											image = getExifTool(file).getPreviewImage(false);
											if (image != null) {
												if (shell.isDisposed())
													break;
												ExifTool.fixOrientation(image, 0, request.getRotation());
												Rectangle nbounds = image.getBounds();
												image.setScaling(nbounds.width, nbounds.height, true, 0, null);
												image.develop(monitor, display, ZImage.UNCROPPED, -1, -1,
														ZImage.SWTIMAGE);
											}
										} catch (Exception e) {
											// ignore and use full size image
										}
										if (monitor.isCanceled())
											break;
									}
								}
								if (image == null) {
									if (monitor.isCanceled())
										break;
									if (recipe != null && recipe != Recipe.NULL)
										shell.getDisplay().syncExec(() -> {
											if (!text.getText().isEmpty()) {
												text.setText(text.getText()
														+ Messages.getString("SlideShowPlayer.developing")); //$NON-NLS-1$
												bottomCanvas.redraw();
											}
										});
									try {
										image = CoreActivator.getDefault().getHighresImageLoader().loadImage(null,
												status, file, request.getRotation(),
												request.getAsset().getFocalLengthIn35MmFilm(),
												new Rectangle(0, 0, preferredWidth, preferredHeight), 1d, 1d, adv, cms1,
												null, null, recipe, fileWatcher, opId, this);
									} catch (Exception e) {
										// ignore
									}
									if (monitor.isCanceled())
										break;
								}
								if (tempFile != null)
									tempFile.delete();
							}
						}
						if (image != null)
							image.develop(monitor, display, ZImage.CROPPED, preferredWidth, preferredHeight,
									ZImage.SWTIMAGE);
						else if (slide.getLayout() != Constants.SLIDE_MAP_LEFT
								&& slide.getLayout() != Constants.SLIDE_MAP_RIGHT)
							image = createIntermediatePlate(shell, request,
									Messages.getString("SlideShowPlayer.image_unavail"), //$NON-NLS-1$
									Messages.getString("SlideShowPlayer.highres_image_not_loaded"), //$NON-NLS-1$
									Constants.SLIDE_THUMBNAILS_BOTTOM);
						request.setImage(image);
						if (monitor.isCanceled())
							break;
						answers.add(request);
						request = null;
					} catch (InterruptedException e) {
						break;
					}
				}
			} finally {
				fileWatcher.stopIgnoring(opId);
			}
			return status;
		}

		private ExifTool getExifTool(File file) {
			if (exifTool == null)
				exifTool = new ExifTool(file, false);
			else
				exifTool.reset(file);
			return exifTool;
		}

		public boolean progress(int tot, int worked) {
			return (request != null) ? request.isAborted() : false;
		}

		private ZImage createSectionBreak(final Shell shl, SlideRequest req) {
			final SlideImpl slide = req.getSlide();
			int layout = slide.getLayout();
			if ((layout == Constants.SLIDE_MAP_LEFT
					|| layout == Constants.SLIDE_MAP_RIGHT) && CoreActivator.getDefault().getGeoService() != null) {
				AssetImpl[] nextAssets = req.getNextAssets();
				for (AssetImpl asset : nextAssets)
					if (!Double.isNaN(asset.getGPSLatitude()) && !Double.isNaN(asset.getGPSLongitude()))
						return null;
			}
			return createIntermediatePlate(shl, req, slide.getCaption() != null ? slide.getCaption() : "", //$NON-NLS-1$
					slide.getDescription() != null ? slide.getDescription() : "", slide.getLayout()); //$NON-NLS-1$
		}

		public ZImage createIntermediatePlate(final Shell shl, SlideRequest req, final String caption,
				final String description, final int slayout) {
			final int w = bounds.width;
			final int h = bounds.height;
			final Display displ = shl.getDisplay();
			Image image = new Image(displ, w, h);
			final GC gc = new GC(image);
			displ.syncExec(() -> {
				gc.setBackground(shl.getBackground());
				gc.fillRectangle(0, 0, w, h);
				gc.setLineWidth(3);
				gc.setForeground(shl.getForeground());
				gc.drawRectangle(0, 0, w - 1, h - 1);
				TextLayout layout = new TextLayout(displ);
				StringBuilder sb = new StringBuilder();
				sb.append(caption);
				int boldLength = sb.length();
				if (!description.isEmpty()) {
					if (sb.length() > 0)
						sb.append("\n\n"); //$NON-NLS-1$
					sb.append(description);
				}
				int tw, th, tx, ty;
				switch (slayout) {
				case Constants.SLIDE_THUMBNAILS_LEFT:
					tw = (int) (w * (0.5d - MARGINWIDTH));
					th = (int) (h * (1d - 2 * MARGINHEIGHT));
					tx = w / 2;
					ty = (int) (h * MARGINHEIGHT);
					break;
				case Constants.SLIDE_THUMBNAILS_RIGHT:
					tw = (int) (w * (0.5d - MARGINWIDTH));
					th = (int) (h * (1d - 2 * MARGINHEIGHT));
					tx = (int) (w * MARGINWIDTH);
					ty = (int) (h * MARGINHEIGHT);
					break;
				case Constants.SLIDE_THUMBNAILS_TOP:
					tw = (int) (w * (1d - 2 * MARGINWIDTH));
					th = (int) (h * (0.5d - MARGINHEIGHT));
					tx = (int) (w * MARGINWIDTH);
					ty = h / 2;
					break;
				case Constants.SLIDE_THUMBNAILS_BOTTOM:
					tw = (int) (w * (1d - 2 * MARGINWIDTH));
					th = (int) (h * (0.5d - MARGINHEIGHT));
					tx = (int) (w * MARGINWIDTH);
					ty = (int) (h * MARGINHEIGHT);
					break;
				default:
					tw = (int) (w * (1d - 2 * MARGINWIDTH));
					th = (int) (h * (1d - 2 * MARGINHEIGHT));
					tx = (int) (w * MARGINWIDTH);
					ty = (int) (h * MARGINHEIGHT);
					break;
				}
				layout.setText(sb.toString());
				layout.setFont(JFaceResources.getFont(UiConstants.VIEWERTITLEFONT));
				layout.setStyle(new TextStyle(JFaceResources.getFont(UiConstants.VIEWERBANNERFONT), null, null), 0,
						boldLength);
				layout.setWidth(tw);
				Rectangle b = layout.getBounds();
				float f = Math.max(0.1f, (float) Math.sqrt((float) b.height / th));
				layout.setWidth((int) (tw * f));
				b = layout.getBounds();
				f = 1f / Math.max((float) b.height / th, (float) b.width / tw);
				Transform t = new Transform(displ);
				t.translate(tx, ty);
				t.scale(f, f);
				gc.setTransform(t);
				layout.draw(gc, 0, 0);
				layout.dispose();
				gc.setTransform(null);
				t.dispose();
			});
			if (req != null) {
				AssetImpl[] assets = req.getNextAssets();
				int l = assets.length;
				if (l > 0) {
					switch (slayout) {
					case Constants.SLIDE_THUMBNAILS_LEFT:
						int cols = Math.min(3, (int) Math.sqrt(l));
						int rows = (l + cols - 1) / cols;
						int maxw = (int) ((w * (0.5d - MARGINWIDTH)) / cols);
						int maxh = (int) ((h * (1d - 2 * MARGINHEIGHT)) / rows);
						int xoff = (int) (w * MARGINWIDTH);
						int yoff = (int) (h * MARGINHEIGHT);
						for (int i = 0; i < assets.length; i++)
							drawThumbnail(gc, assets[i], maxw, maxh, (i % cols) * maxw + xoff,
									(i / cols) * maxh + yoff);
						break;
					case Constants.SLIDE_THUMBNAILS_RIGHT:
						cols = Math.min(3, (int) Math.sqrt(l));
						rows = (l + cols - 1) / cols;
						maxw = (int) ((w * (0.5d - MARGINWIDTH)) / cols);
						maxh = (int) ((h * (1d - 2 * MARGINHEIGHT)) / rows);
						xoff = w / 2;
						yoff = (int) (h * MARGINHEIGHT);
						for (int i = 0; i < assets.length; i++)
							drawThumbnail(gc, assets[i], maxw, maxh, (i % cols) * maxw + xoff,
									(i / cols) * maxh + yoff);
						break;
					case Constants.SLIDE_THUMBNAILS_TOP:
						rows = Math.min(2, (int) Math.sqrt(l));
						cols = (l + rows - 1) / rows;
						maxw = (int) ((w * (1d - 2 * MARGINWIDTH)) / cols);
						maxh = (int) ((h * (0.5d - MARGINHEIGHT)) / rows);
						xoff = (int) (w * MARGINWIDTH);
						yoff = (int) (h * MARGINHEIGHT);
						for (int i = 0; i < assets.length; i++)
							drawThumbnail(gc, assets[i], maxw, maxh, (i % cols) * maxw + xoff,
									(i / cols) * maxh + yoff);
						break;
					case Constants.SLIDE_THUMBNAILS_BOTTOM:
						rows = Math.min(2, (int) Math.sqrt(l));
						cols = (l + rows - 1) / rows;
						maxw = (int) ((w * (1d - 2 * MARGINWIDTH)) / cols);
						maxh = (int) ((h * (0.5d - MARGINHEIGHT)) / rows);
						xoff = (int) (w * MARGINWIDTH);
						yoff = h / 2;
						for (int i = 0; i < assets.length; i++)
							drawThumbnail(gc, assets[i], maxw, maxh, (i % cols) * maxw + xoff,
									(i / cols) * maxh + yoff);
						break;
					}
				}
			}
			gc.dispose();
			return new ZImage(image, null);
		}

		private void drawThumbnail(final GC gc, final AssetImpl asset, final int maxw, final int maxh, final int x,
				final int y) {
			display.syncExec(() -> {
				Image thImage = ImageUtilities.loadThumbnail(display, asset.getJpegThumbnail(),
						Ui.getUi().getDisplayCMS(), SWT.IMAGE_JPEG, true);
				Rectangle thBounds = thImage.getBounds();
				double f = Math.min(maxw * (1d - THUMBNAILSPACE) / thBounds.width,
						maxh * (1d - THUMBNAILSPACE) / thBounds.height);
				gc.drawImage(thImage, 0, 0, thBounds.width, thBounds.height, x, y, (int) (thBounds.width * f),
						(int) (thBounds.height * f));
				thImage.dispose();
			});
		}
	}

	public interface FadingListener {
		void fadeinStarted();

		void fadeinEnded();

		void fadeoutStarted();

		void fadeoutEnded();
	}

	public SmartCollectionImpl[] selectedAlbums;

	public class SingleSlideJob extends Job implements HelpListener {

		public class SlideControl implements IHoverSubject {

			public class ControlHover implements IGalleryHover {

				public IHoverInfo getHoverInfo(IHoverSubject viewer, MouseEvent event) {
					Object ob = viewer.findObject(event);
					if (ob instanceof Integer) {
						String msg = null;
						switch ((Integer) ob) {
						case C_BACK:
							msg = Messages.getString("SlideShowPlayer.one_step_back"); //$NON-NLS-1$
							break;
						case C_PLAY:
							msg = Messages.getString("SlideShowPlayer.continue"); //$NON-NLS-1$
							break;
						case C_STOP:
							msg = Messages.getString("SlideShowPlayer.stop"); //$NON-NLS-1$
							break;
						case C_FORWARD:
							msg = Messages.getString("SlideShowPlayer.one_step_forward"); //$NON-NLS-1$
							break;
						case C_JUMP:
							msg = Messages.getString("SlideShowPlayer.jump_to_slide"); //$NON-NLS-1$
							break;
						case C_ROTCW:
							msg = Messages.getString("SlideShowPlayer.rotate_cw"); //$NON-NLS-1$
							break;
						case C_ROTCCW:
							msg = Messages.getString("SlideShowPlayer.rotate_ccw"); //$NON-NLS-1$
							break;
						case C_DELETE:
							msg = Messages.getString("SlideShowPlayer.delete"); //$NON-NLS-1$
							break;
						case C_RATE:
							msg = Messages.getString("SlideShowPlayer.rate_remove"); //$NON-NLS-1$
							break;
						case C_ALBUM:
							msg = Messages.getString("SlideShowPlayer.add_to_album"); //$NON-NLS-1$
							break;
						case C_SELECTION:
							msg = request.isSelected() ? Messages.getString("SlideShowPlayer.remove_selection") //$NON-NLS-1$
									: Messages.getString("SlideShowPlayer.add_selection"); //$NON-NLS-1$
							break;
						case C_VOICE:
							msg = Messages.getString("SlideShowPlayer.attach_remove_voice_note"); //$NON-NLS-1$
							break;
						}
						if (msg != null)
							return new HoverInfo(msg);
					}
					return null;
				}
			}

			private FadingShell controlShell;
			private HoveringController controller;
			private double factor;
			private Image scaledPanel;

			public SlideControl(FadingShell parentShell) {
				Image controlPanel = (request.getImage() == null ? Icons.slideControl_dark
						: canBeSelected(request) ? Icons.slideControl : Icons.slideControl_nonselect).getImage();
				Rectangle pBounds = parentShell.getBounds();
				final Rectangle iBounds = controlPanel.getBounds();
				factor = (double) pBounds.width / iBounds.width * 6 / 14;
				final int w = (int) (iBounds.width * factor);
				buttonWidth = w / 12;
				final int h = (int) (iBounds.height * factor);
				GC gc = new GC(scaledPanel = new Image(displ, w, h));
				gc.drawImage(controlPanel, 0, 0, iBounds.width, iBounds.height, 0, 0, w, h);
				gc.dispose();
				final Shell shell = new Shell(parentShell.getShell(), SWT.TOOL);
				controlShell = new FadingShell(shell, true, Constants.SLIDE_TRANSITION_FADE);
				shell.setText(slide.getCaption());
				shell.setBackgroundImage(scaledPanel);
				shell.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent e) {
						if (controlShell != null)
							controlShell.setAlpha(225);
						int button = ((int) (e.x / factor + 0.5d)) / 80;
						int seqno = request.getSeqno();
						switch (button) {
						case C_BACK:
							if (seqno > 0)
								startButtonTimer(BACK, slideControl.getLocation());
							break;
						case C_FORWARD:
							if (seqno < requestList.size() - 1) {
								Point p = slideControl.getLocation();
								p.x += 3 * buttonWidth;
								startButtonTimer(FORWARD, p);
							}
							break;
						case C_JUMP:
							Point p = slideControl.getLocation();
							p.x += button * buttonWidth;
							gotoSlide(p, request, RESTART);
							break;
						}
					}

					@Override
					public void mouseUp(MouseEvent e) {
						stopButtonTimer();
						if (selectSlideDialog == null) {
							int button = ((int) (e.x / factor - 1.5d)) / 80;
							switch (button) {
							case C_BACK:
								request.setOperation((request.getSeqno() > 0) ? BACK : NOOP);
								break;
							case C_PLAY:
								request.setPaused(false);
								request.setOperation(CONTINUE);
								closeControl();
								break;
							case C_STOP:
								int cret = showClosePrompt(bottomShell);
								if (cret != CLOSE_CANCEL) {
									setPaused(false);
									request.setOperation(cret == CLOSE_CLOSE ? ABORT : IGNORE);
									closeControl();
								}
								break;
							case C_FORWARD:
								request.setOperation((request.getSeqno() < requestList.size() - 1) ? FORWARD : NOOP);
								break;
							case C_ROTCW:
								rotateClockwise(90);
								break;
							case C_ROTCCW:
								rotateClockwise(270);
								break;
							case C_DELETE:
								if (request.getImage() != null)
									processDelete(bottomShell, controlShell.getShell().getBounds());
								break;
							case C_RATE:
								if (request.getImage() != null)
									rateSlide(controlShell.getShell(), e, RatingDialog.ABORT, false);
								break;
							case C_ALBUM:
								if (request.getImage() != null) {
									Shell sh = controlShell.getShell();
									addToAlbums(sh, sh.toDisplay(e.x, e.y));
								}
								break;
							case C_SELECTION:
								if (request.getImage() != null)
									addToSelection(request, !request.isSelected());
								break;
							case C_VOICE:
								request.setPaused(true);
								recordVoiceNote(button * buttonWidth, request);
								showControl();
								break;
							}
							startTimer();
						}
					}
				});
				controlShell.getShell().addKeyListener(new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						switch (e.character) {
						case ' ':
							request.setPaused(false);
							request.setOperation(CONTINUE);
							closeControl();
							break;
						case SWT.TAB:
							setPaused(true);
							int cret = showClosePrompt(bottomShell);
							if (cret != CLOSE_CANCEL) {
								setPaused(false);
								request.setOperation(cret == CLOSE_CLOSE ? ABORT : IGNORE);
								closeControl();
							} else
								setPaused(false);
							break;
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
							if (request.getImage() != null)
								rateSlide(controlShell.getShell(), null, e.character - '0', true);
							break;
						case 'a':
							setPaused(true);
							Point p = slideControl.getLocation();
							p.x += 4 * buttonWidth;
							addToAlbums(controlShell.getShell(), p);
							setPaused(false);
							break;
						case 'r':
							rotateClockwise(270);
							break;
						case 'R':
							rotateClockwise(90);
							break;
						case 's':
							if (request.getImage() != null)
								addToSelection(request, true);
							break;
						case 'S':
							if (request.getImage() != null)
								addToSelection(request, false);
							break;
						case 'v':
						case 'V':
							request.setPaused(true);
							recordVoiceNote(C_VOICE * buttonWidth, request);
							showControl();
							break;
						default:
							switch (e.keyCode) {
							case SWT.ESC:
								setPaused(true);
								cret = showClosePrompt(bottomShell);
								if (cret != CLOSE_CANCEL) {
									setPaused(false);
									request.setOperation(cret == CLOSE_CLOSE ? ABORT : IGNORE);
									closeControl();
								} else
									setPaused(false);
								break;
							case SWT.ARROW_LEFT:
								if ((e.stateMask & SWT.CONTROL) == SWT.CONTROL) {
									rotateClockwise(270);
									break;
								}
								if (request.getSeqno() > 0) {
									request.setPaused(false);
									request.setOperation(BACK);
									closeControl();
									request.setPaused(true);
								}
								break;
							case SWT.ARROW_RIGHT:
								if ((e.stateMask & SWT.CONTROL) == SWT.CONTROL) {
									rotateClockwise(90);
									break;
								}
								if (request.getSeqno() < requestList.size() - 1) {
									request.setPaused(false);
									request.setOperation(FORWARD);
									closeControl();
									request.setPaused(true);
								}
								break;
							case SWT.ARROW_UP:
								setPaused(true);
								p = slideControl.getLocation();
								p.x += 4 * buttonWidth;
								gotoSlide(p, request, RESTART);
								setPaused(false);
								break;
							case SWT.DEL:
								if ((e.stateMask & (SWT.CONTROL | SWT.ALT)) != 0 && request.getImage() != null)
									processDelete(bottomShell, controlShell.getShell().getBounds());
								break;
							case SWT.F2:
								if (request.getImage() != null) {
									Asset asset = request.getAsset();
									if (asset != null)
										metadataRequested(asset);
								}
							}
							break;
						}
					}
				});
				controlShell.getShell().setBounds(pBounds.x + (pBounds.width - w) / 2,
						pBounds.y + pBounds.height - h - 50, w, h);
				controlShell.setAlpha(0);
				controlShell.open();
				controlShell.forceActive();
				controlShell.forceFocus();
				controlShell.setFocus();
				controller = new HoveringController(this);
				controller.install();
				for (int i = 1; i <= 10; i++) {
					if (i > 1)
						sleepTick(TICK);
					controlShell.setAlpha(i * 15);
				}
			}

			public void dispose() {
				if (scaledPanel != null)
					scaledPanel.dispose();
				if (controller != null)
					controller.uninstall();
				if (controlShell != null && !controlShell.isDisposed()) {
					int alpha = controlShell.getAlpha();
					while (alpha > 15) {
						controlShell.setAlpha(alpha -= 15);
						sleepTick(TICK);
					}
					controlShell.close();
					controlShell = null;
				}
			}

			public boolean isDisposed() {
				return controlShell.isDisposed();
			}

			public Shell getShell() {
				return controlShell.getShell();
			}

			public Point getLocation() {
				return controlShell.getLocation();
			}

			public Control getControl() {
				return getShell();
			}

			public String getTooltip(int mx, int my) {
				return null;
			}

			public Object findObject(MouseEvent event) {
				startTimer();
				return ((int) (event.x / factor - 1.5d)) / 80;
			}

			public ImageRegion[] findAllRegions(MouseEvent event) {
				return null;
			}

			public IGalleryHover getGalleryHover(MouseEvent event) {
				return new ControlHover();
			}

			@Override
			public Control[] getControls() {
				return null;
			}

		}

		private static final int CONTROLTIMEOUT = 3000;
		private int startFrom;
		private Shell parent;
		private SlideImpl slide;
		private double transitionValue;
		private boolean next = false;
		private int mouseMovements = 0;
		private Canvas imageCanvas;
		private FadingShell fadingShell;
		private int startNext;
		private FadingShell titleShell;
		private int titleTime;
		private Display displ;
		private SlideRequest request;
		private boolean pauseAt;
		private SlideRequest finishedRequest;
		private int buttonWidth;
		private SelectSlideDialog selectSlideDialog;
		private int effect;
		private FadingListener listener;
		private ScheduledFuture<?> titleTask;
		private ScheduledFuture<?> buttonTask;
		private ScheduledFuture<?> panelTask;
		private SlideControl slideControl;
		private Point pnt = new Point(0, 0);
		private Rectangle mbounds;
		private boolean inhibitTimer;
		private float zoom = 1f;
		private int ticks;

		public SingleSlideJob(SlideRequest request, int startFrom, int startNext, Shell parent, Rectangle mbounds,
				boolean pauseAt, FadingListener listener) {
			super(request.getSlide().getCaption());
			setPriority(Job.INTERACTIVE);
			setSystem(true);
			this.mbounds = mbounds;
			this.request = request;
			this.slide = request.getSlide();
			this.startFrom = startFrom;
			this.startNext = startNext;
			this.parent = parent;
			this.pauseAt = pauseAt;
			this.listener = listener;
			displ = parent.getDisplay();
		}

		@Override
		public boolean belongsTo(Object family) {
			return Constants.SLIDESHOW == family;
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			finishedRequest = null;
			displ.syncExec(() -> {
				final Shell shell = new Shell(parent, SWT.NO_TRIM);
				effect = slide.getEffect();
				if (effect == Constants.SLIDE_TRANSITION_RANDOM)
					effect = (int) (Math.random() * Constants.SLIDE_TRANSITION_N) + Constants.SLIDE_TRANSITION_START;
				fadingShell = new FadingShell(shell, true, effect);
				KeyListener keyListener = new KeyAdapter() {
					@Override
					public void keyReleased(KeyEvent e) {
						switch (e.character) {
						case SWT.TAB:
							setPaused(true);
							int ret = showClosePrompt(bottomShell);
							if (ret != CLOSE_CANCEL)
								request.setOperation(ret == CLOSE_CLOSE ? ABORT : IGNORE);
							else
								setPaused(false);
							break;
						case ' ':
							if (request.isPaused())
								request.setOperation(CONTINUE);
							request.setPaused(!request.isPaused());
							closeControl();
							if (request.isPaused())
								showMessage(Messages.getString("SlideShowPlayer.pause"), 1500, SWT.CENTER, //$NON-NLS-1$
										UiConstants.VIEWERBANNERFONT, 5);
							break;
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
							if (request.getImage() != null) {
								request.setPaused(true);
								ZImage image = request.getImage();
								if (image != null)
									rateSlide(shell, null, e.character - '0', true);
								closeControl();
								request.setPaused(false);
							}
							break;
						case 'a':
							setPaused(true);
							addToAlbums(shell, null);
							setPaused(false);
							break;
						case 'r':
							rotateClockwise(270);
							break;
						case 'R':
							rotateClockwise(90);
							break;
						case 's':
							if (request.getImage() != null)
								addToSelection(request, true);
							break;
						case 'S':
							if (request.getImage() != null)
								addToSelection(request, false);
							break;
						default:
							switch (e.keyCode) {
							case SWT.ESC:
								setPaused(true);
								int cret = showClosePrompt(bottomShell);
								if (cret != CLOSE_CANCEL)
									request.setOperation(cret == CLOSE_CLOSE ? ABORT : IGNORE);
								else
									setPaused(false);
								break;
							case SWT.ARROW_LEFT:
								if ((e.stateMask & SWT.CONTROL) == SWT.CONTROL) {
									rotateClockwise(270);
									break;
								}
								if (request.getSeqno() > 0) {
									request.setOperation(BACK);
									closeControl();
									request.setPaused(true);
								}
								break;
							case SWT.ARROW_RIGHT:
								if ((e.stateMask & SWT.CONTROL) == SWT.CONTROL) {
									rotateClockwise(90);
									break;
								}
								if (request.getSeqno() < requestList.size() - 1) {
									request.setOperation(FORWARD);
									closeControl();
									request.setPaused(true);
								}
								break;
							case SWT.ARROW_UP:
								setPaused(true);
								gotoSlide(null, request, RESTART);
								setPaused(false);
								break;
							case SWT.DEL:
								if ((e.stateMask & (SWT.CONTROL | SWT.ALT)) != 0 && request.getImage() != null) {
									request.setPaused(true);
									processDelete(shell, null);
									closeControl();
									request.setPaused(false);
								}
								break;
							case SWT.F2:
								if (request.getImage() != null) {
									Asset asset = request.getAsset();
									if (asset != null) {
										request.setPaused(true);
										metadataRequested(asset);
										request.setPaused(false);
									}
								}
							}
							break;
						}
					}
				};
				MouseMoveListener mouseMNoveListener = new MouseMoveListener() {
					public void mouseMove(MouseEvent e) {
						if (panelTask == null && !pauseAt && !ignoreMouseMovements)
							mouseMovements++;
					}
				};
				if (request.getImage() == null)
					fillSectionShell(shell, keyListener, mouseMNoveListener, request.getSlide(),
							request.getNextAssets());
				else
					fillImageShell(shell, keyListener, mouseMNoveListener);
				shell.setFullScreen(true);
				shell.setBackground(parent.getBackground());
				shell.setForeground(parent.getForeground());
				shell.setBounds(mbounds);
				fadingShell.layout();
				fadingShell.open();
				fadingShell.forceActive();
				fadingShell.forceFocus();
				imageCanvas.setFocus();
			});
			try {
				int delay = slide.getDelay();
				int fadein = slide.getFadeIn();
				if (delay > fadein && !pauseAt) {
					int diff = delay - fadein;
					if (startFrom < diff) {
						final int delaySteps = computeSteps(diff - startFrom);
						delLoop: for (int i = 0; i < delaySteps; i++) {
							sleepTick(TICK);
							if (i % NTICKS == 0) {
								switch (checkStatus(monitor)) {
								case ABORT:
								case IGNORE:
									return Status.CANCEL_STATUS;
								case FORWARD:
									startFrom -= ((delaySteps - i) * TICK + duration + fadein);
									break delLoop;
								}
							}
						}
						startFrom = 0;
					} else
						startFrom -= diff;
				}
				Runnable runnable = new Runnable() {
					public void run() {
						Rectangle bounds = fadingShell.getShell().getParent().getBounds();
						switch (effect) {
						case Constants.SLIDE_TRANSITION_MOVE_LEFT:
							double d = 1 - transitionValue;
							fadingShell.setLocation((int) (bounds.x + bounds.width * d + 0.5d), bounds.y);
							fadingShell.setAlpha(transitionValue > 0 ? 255 : 0);
							break;
						case Constants.SLIDE_TRANSITION_MOVE_RIGHT:
							d = transitionValue - 1;
							fadingShell.setLocation((int) (bounds.x + bounds.width * d + 0.5d), bounds.y);
							fadingShell.setAlpha(transitionValue > 0 ? 255 : 0);
							break;
						case Constants.SLIDE_TRANSITION_MOVE_UP:
							d = 1 - transitionValue;
							fadingShell.setLocation(bounds.x, (int) (bounds.y + bounds.height * d + 0.5d));
							fadingShell.setAlpha(transitionValue > 0 ? 255 : 0);
							break;
						case Constants.SLIDE_TRANSITION_MOVE_DOWN:
							d = transitionValue - 1;
							fadingShell.setLocation(bounds.x, (int) (bounds.y + bounds.height * d + 0.5d));
							fadingShell.setAlpha(transitionValue > 0 ? 255 : 0);
							break;
						case Constants.SLIDE_TRANSITION_MOVE_TOPLEFT:
							d = 1 - transitionValue;
							fadingShell.setLocation((int) (bounds.x + bounds.width * d + 0.5d),
									(int) (bounds.y + bounds.height * d + 0.5d));
							fadingShell.setAlpha(transitionValue > 0 ? 255 : 0);
							break;
						case Constants.SLIDE_TRANSITION_MOVE_TOPRIGHT:
							double dx = transitionValue - 1;
							double dy = 1 - transitionValue;
							fadingShell.setLocation((int) (bounds.x + bounds.width * dx + 0.5d),
									(int) (bounds.y + bounds.height * dy + 0.5d));
							fadingShell.setAlpha(transitionValue > 0 ? 255 : 0);
							break;
						case Constants.SLIDE_TRANSITION_MOVE_BOTTOMLEFT:
							dx = 1 - transitionValue;
							dy = transitionValue - 1;
							fadingShell.setLocation((int) (bounds.x + bounds.width * dx + 0.5d),
									(int) (bounds.y + bounds.height * dy + 0.5d));
							fadingShell.setAlpha(transitionValue > 0 ? 255 : 0);
							break;
						case Constants.SLIDE_TRANSITION_MOVE_BOTTOMRIGHT:
							d = transitionValue - 1;
							fadingShell.setLocation((int) (bounds.x + bounds.width * d + 0.5d),
									(int) (bounds.y + bounds.height * d + 0.5d));
							fadingShell.setAlpha(transitionValue > 0 ? 255 : 0);
							break;
						default:
							fadingShell.setAlpha((int) (255 * transitionValue + 0.5d));
							break;
						}
					}
				};
				if (listener != null)
					listener.fadeinStarted();
				if (startFrom < fadein) {
					ignoreMouseMovements = true;
					try {
						final int insteps = computeSteps(fadein);
						inLoop: for (int i = 0; i < insteps; i++) {
							if (startFrom <= 0) {
								if (fadingShell == null || fadingShell.isDisposed())
									break;
								transitionValue = (double) (i + 1) / insteps;
								if (pauseAt && i >= insteps - 1) {
									pauseAt = false;
									request.setPaused(true);
								}
								if (!displ.isDisposed())
									displ.asyncExec(runnable);
								sleepTick(TICK);
								if (i % NTICKS == 0) {
									switch (checkStatus(monitor)) {
									case ABORT:
									case IGNORE:
										return Status.CANCEL_STATUS;
									case FORWARD:
										startFrom -= ((insteps - i) * TICK + duration);
										break inLoop;
									}
								}
							} else
								startFrom -= TICK;
						}
					} finally {
						ignoreMouseMovements = false;
					}
				} else {
					startFrom -= fadein;
					transitionValue = 1d;
					if (!displ.isDisposed())
						displ.asyncExec(runnable);
					if (pauseAt) {
						pauseAt = false;
						request.setPaused(true);
					}
				}
				if (listener != null)
					listener.fadeinEnded();
				int dur = Math.max(slide.getDuration(), request.getVoiceLength());
				if (startFrom < dur) {
					URL voiceUrl = request.getVoiceUri();
					if (voiceUrl != null)
						UiActivator.getDefault().playSoundfile(voiceUrl, null);
					final int dursteps = computeSteps(dur);
					if (slide.getAsset() != null) {
						final int titleDur = Math.min(slideshow.getTitleDisplay(), dur) - startFrom;
						if (titleDur > 0)
							if (!fadingShell.isDisposed()) {
								displ.syncExec(() -> {
									if (!fadingShell.isDisposed()) {
										final String title;
										switch (slideshow.getTitleContent()) {
										case Constants.SLIDE_TITLE_SNO:
											title = NLS.bind("{2} ({0}/{1})", //$NON-NLS-1$
													new Object[] { slide.getSequenceNo(), slideshow.getEntry().size(),
															slide.getCaption() });
											break;
										case Constants.SLIDE_SNOONLY:
											title = NLS.bind("{0}/{1}", slide.getSequenceNo(), slideshow //$NON-NLS-1$
													.getEntry().size());
											break;
										case Constants.SLIDE_TITLE_NE:
											String caption = slide.getCaption();
											Asset asset = request.getAsset();
											if (asset != null) {
												String fileName = Core.getFileName(asset.getUri(), true);
												if (caption.equals(fileName)) {
													title = null;
													break;
												}
											}
											title = caption;
											break;
										default:
											title = slide.getCaption();
											break;
										}
										if (title != null)
											showMessage(title, titleDur, SWT.END, UiConstants.VIEWERTITLEFONT, 2);
									}
								});
							}
					}
					long t1 = System.currentTimeMillis();
					ticks = 1;
					int sleep = TICK;
					durLoop: for (int i = 0; i < dursteps; i += ticks) {
						if (startFrom <= 0)
							sleepTick(sleep);
						if (i % NTICKS == 0) {
							switch (checkStatus(monitor)) {
							case ABORT:
							case IGNORE:
								return Status.CANCEL_STATUS;
							case CONTINUE:
							case FORWARD:
								startFrom -= ((dursteps - i) * TICK);
								break durLoop;
							}
						}
						if (slide.getZoom() > 0) {
							zoom = 1 - ((float) i / dursteps) * slide.getZoom() / 200;
							displ.syncExec(() -> {
								if (!fadingShell.isDisposed())
									imageCanvas.redraw();
							});
							long t2 = System.currentTimeMillis();
							int interval = (int) (t2 - t1);
							t1 = t2;
							ticks = (interval + TICK - 1) / TICK;
							if (ticks < 1)
								ticks = 1;
							sleep = Math.max(2, TICK * ticks - interval);
						}
						startFrom -= TICK;
						if (i * TICK >= startNext && !next) {
							finishedRequest = request;
							next = true;
						}
					}
				} else
					startFrom -= dur;
				if (!next) {
					finishedRequest = request;
					next = true;
				}
				if (finishedRequest != null) {
					done.add(finishedRequest);
					finishedRequest = null;
				}
				if (listener != null)
					listener.fadeoutStarted();
				int fadeout = slide.getFadeOut();
				if (startFrom < fadeout) {
					if (effect == Constants.SLIDE_TRANSITION_FADE) {
						final int outsteps = computeSteps(fadeout);
						for (int i = outsteps - 1; i >= 0; i--) {
							if (startFrom <= 0) {
								if (fadingShell.isDisposed())
									break;
								transitionValue = (double) i / outsteps;
								if (!displ.isDisposed())
									displ.asyncExec(runnable);
								sleepTick(TICK);
								if (i % NTICKS == 0) {
									int ret = checkStatus(monitor);
									if (ret == ABORT || ret == IGNORE)
										return Status.CANCEL_STATUS;
								}
							} else
								startFrom -= TICK;
						}
					} else {
						try {
							Thread.sleep(fadeout);
						} catch (InterruptedException e) {
							// do nothing
						}
						startFrom -= fadeout;
					}
				} else
					startFrom -= fadeout;
				if (listener != null)
					listener.fadeoutEnded();
			} finally {
				displ.syncExec(() -> {
					fadingShell.setAlpha(0);
					fadingShell.close();
					if (request.operation != RESTART)
						request.dispose();
				});
				if (finishedRequest != null) {
					done.add(finishedRequest);
					finishedRequest = null;
				}
			}
			return Status.OK_STATUS;
		}

		private void fillSectionShell(Shell shell, KeyListener keyListener, MouseMoveListener mouseMNoveListener,
				Slide slide, AssetImpl[] assets) {
			shell.setText(slide.getCaption());
			shell.setLayout(new GridLayout(2, true));
			if (slide.getLayout() == Constants.SLIDE_MAP_RIGHT) {
				createTextgroup(shell, slide, keyListener, mouseMNoveListener);
				createMaparea(shell, assets, keyListener, mouseMNoveListener);
			} else {
				createMaparea(shell, assets, keyListener, mouseMNoveListener);
				createTextgroup(shell, slide, keyListener, mouseMNoveListener);
			}
		}

		private void createTextgroup(Shell shell, Slide slide, KeyListener keyListener,
				MouseMoveListener mouseMoveListener) {
			Composite textGroup = new Composite(shell, SWT.NONE);
			textGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			GridLayout layout = new GridLayout();
			layout.marginHeight = layout.marginWidth = 20;
			layout.verticalSpacing = 20;
			textGroup.setLayout(layout);
			textGroup.setBackground(bottomShell.getBackground());
			Label titleLabel = new Label(textGroup, SWT.NONE);
			titleLabel.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false));
			titleLabel.setText(slide.getCaption());
			titleLabel.setAlignment(SWT.CENTER);
			titleLabel.setFont(JFaceResources.getFont(UiConstants.VIEWERBANNERFONT));
			titleLabel.setBackground(bottomShell.getBackground());
			titleLabel.setForeground(bottomShell.getForeground());
			Label descriptionLabel = new Label(textGroup, SWT.WRAP);
			descriptionLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false));
			descriptionLabel.setAlignment(SWT.CENTER);
			descriptionLabel.setText(slide.getDescription());
			descriptionLabel.setFont(JFaceResources.getFont(UiConstants.VIEWERTITLEFONT));
			descriptionLabel.setBackground(bottomShell.getBackground());
			descriptionLabel.setForeground(bottomShell.getForeground());
			titleLabel.addMouseMoveListener(mouseMoveListener);
			titleLabel.addKeyListener(keyListener);
			titleLabel.addHelpListener(SingleSlideJob.this);
			descriptionLabel.addMouseMoveListener(mouseMoveListener);
			descriptionLabel.addKeyListener(keyListener);
			descriptionLabel.addHelpListener(SingleSlideJob.this);
			textGroup.addMouseMoveListener(mouseMoveListener);
			textGroup.addKeyListener(keyListener);
			textGroup.addHelpListener(SingleSlideJob.this);
		}

		private void createMaparea(Shell shell, AssetImpl[] assets, KeyListener keyListener,
				MouseMoveListener mouseMoveListener) {
			Composite mapArea = new Composite(shell, SWT.NONE);
			mapArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			mapArea.setBackground(bottomShell.getBackground());
			mapArea.addMouseMoveListener(mouseMoveListener);
			GridLayout layout = new GridLayout();
			layout.marginHeight = layout.marginWidth = 20;
			mapArea.setLayout(layout);
			Control map = CoreActivator.getDefault().getGeoService().showTrack(mapArea, assets, true);
			if (map != null) {
				map.addMouseMoveListener(mouseMoveListener);
				map.addKeyListener(keyListener);
			}
		}

		private void fillImageShell(Shell shell, KeyListener keyListener, MouseMoveListener mouseMoveListener) {
			shell.setText(slide.getCaption());
			shell.setLayout(new FillLayout());
			imageCanvas = new Canvas(shell, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
			imageCanvas.setCursor(transparentCursor);
			imageCanvas.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					ZImage image = request.getImage();
					Rectangle sbounds = imageCanvas.getClientArea();
					GC gc = e.gc;
					gc.setBackground(shell.getBackground());
					gc.fillRectangle(sbounds);
					if (image != null && !image.isDisposed()) {
						Rectangle ibounds = image.getBounds();
						if (slide.getZoom() == 0)
							image.draw(gc, (sbounds.width - ibounds.width) / 2, (sbounds.height - ibounds.height) / 2,
									ZImage.CROPPED, sbounds.width, sbounds.height);
						else {
							double f = Math.min((double) sbounds.width / ibounds.width,
									(double) sbounds.height / ibounds.height);
							double iwidth = ibounds.width * f;
							double iheight = ibounds.height * f;
							double w = ibounds.width * zoom;
							double h = ibounds.height * zoom;
							double offx = Math.max(0, Math.min(ibounds.width - w,
									ibounds.width * (slide.getZoomX() + 100) / 200 - w / 2));
							double offy = Math.max(0, Math.min(ibounds.height - h,
									ibounds.height * (slide.getZoomY() + 100) / 200 - h / 2));
							image.draw(gc, (int) (offx + 0.5d), (int) (offy + 0.5d), (int) (w + 0.5d), (int) (h + 0.5d),
									(int) ((sbounds.width - iwidth) / 2 + 0.5d),
									(int) ((sbounds.height - iheight) / 2 + 0.5d), (int) (iwidth + 0.5d),
									(int) (iheight + 0.5d), ZImage.CROPPED, sbounds.width, sbounds.height, true);
						}
						if (request.isRemoveFromShow()) {
							String txt = Messages.getString("SlideShowPlayer.deleted"); //$NON-NLS-1$
							gc.setFont(JFaceResources.getFont(UiConstants.VIEWERBANNERFONT));
							int x = gc.textExtent(txt).x;
							gc.setForeground(displ.getSystemColor(SWT.COLOR_WHITE));
							gc.drawText(txt, sbounds.width - x - 5, 5, true);
							gc.setForeground(displ.getSystemColor(SWT.COLOR_RED));
							gc.drawText(txt, sbounds.width - x - 4, 4, true);
						}
					}
				}
			});
			imageCanvas.addMouseMoveListener(mouseMoveListener);
			imageCanvas.addKeyListener(keyListener);
			imageCanvas.addHelpListener(SingleSlideJob.this);
		}

		protected void showMessage(String msg, final int titleDur, int valign, String font, int margins) {
			Rectangle pBounds = fadingShell.getBounds();
			Shell shell = new Shell(fadingShell.getShell(), SWT.NO_TRIM);
			titleShell = new FadingShell(shell, true, Constants.SLIDE_TRANSITION_FADE);
			shell.setText(msg);
			shell.setLayout(new FillLayout());
			final Canvas titleCanvas = new Canvas(shell, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
			GC gc = new GC(titleCanvas);
			gc.setFont(JFaceResources.getFont(font));
			Point tx = gc.textExtent(msg);
			gc.dispose();
			titleCanvas.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					e.gc.setFont(JFaceResources.getFont(font));
					e.gc.drawText(msg, margins, margins);
				}
			});
			titleCanvas.addHelpListener(SingleSlideJob.this);
			int y = pBounds.y;
			switch (valign) {
			case SWT.BEGINNING:
				y += margins;
				break;
			case SWT.CENTER:
				y += (pBounds.height - tx.y) / 2 - margins;
				break;
			default:
				y += pBounds.height - tx.y - margins;
				break;
			}
			shell.setBounds(pBounds.x + (pBounds.width - tx.x) / 2 - margins, y, tx.x + 2 * margins,
					tx.y + 2 * margins);
			titleShell.layout();
			titleShell.setAlpha(0);
			titleShell.open();
			titleShell.forceActive();
			titleCanvas.redraw();
			imageCanvas.setFocus();
			titleTime = 0;
			final int fade = Math.min(500, titleDur / 4);
			final int dur = Math.max(fade * 2 + 100, titleDur);
			titleTask = UiActivator.getScheduledExecutorService().scheduleAtFixedRate(() -> {
				titleTime += TICK;
				if (titleShell != null && !titleShell.isDisposed()) {
					if (titleTime >= dur)
						displ.syncExec(() -> {
							titleTask.cancel(true);
							titleShell.close();
						});
					else if (titleTime <= fade)
						displ.syncExec(() -> {
							titleShell.setAlpha(Math.min(255, titleShell.getAlpha() + (255 * TICK + fade - 1) / fade));
						});
					else if (titleTime > dur - fade)
						displ.syncExec(() -> {
							titleShell.setAlpha(Math.max(0, titleShell.getAlpha() - ((255 * TICK + fade - 1) / fade)));
						});
				}
			}, 0L, TICK, TimeUnit.MILLISECONDS);
		}

		private int checkStatus(IProgressMonitor monitor) {
			int result = doCheckStatus(monitor);
			if (result != NOOP)
				return result;
			while (request.isPaused()) {
				sleepTick(LONGTICK);
				result = doCheckStatus(monitor);
				if (result != NOOP)
					return result;
			}
			return NOOP;
		}

		private int doCheckStatus(IProgressMonitor monitor) {
			if (request.getOperation() != NOOP || monitor.isCanceled()) {
				if (!next)
					finishedRequest = request;
				switch (request.getOperation()) {
				case FORWARD:
					return FORWARD;
				case CONTINUE:
					return CONTINUE;
				default:
					return ABORT;
				}
			}
			if (mouseMovements > 3) {
				request.setPaused(true);
				showControl();
			}
			mouseMovements = 0;
			return NOOP;
		}

		private void showControl() {
			if (!fadingShell.isDisposed())
				displ.syncExec(() -> {
					doShowControl();
				});
		}

		private void doShowControl() {
			if (!fadingShell.isDisposed()) {
				if (imageCanvas != null)
					imageCanvas.setCursor(null);
				slideControl = new SlideControl(fadingShell);
				startTimer();
			}
		}

		private boolean canBeSelected(SlideRequest request) {
			return (request.getImage() == null || request.getSlide().getAsset() == null) ? false
					: !request.isSelected();
		}

		protected void restart(int incr) {
			restartAt(request.getSeqno() + incr);
		}

		protected void restartAt(int pos) {
			request.setOperation((pos >= 0 && pos < requestList.size()) ? pos : NOOP);
		}

		private void startTimer() {
			stopTimer();
			if (!inhibitTimer)
				panelTask = UiActivator.getScheduledExecutorService().schedule(() -> {
					if (!displ.isDisposed())
						displ.syncExec(() -> closeControl());
					if (!displ.isDisposed())
						displ.syncExec(() -> {
							if (imageCanvas != null && !imageCanvas.isDisposed()) {
								imageCanvas.setCursor(transparentCursor);
								imageCanvas.setFocus();
							}
						});
					panelTask = null;
				}, CONTROLTIMEOUT, TimeUnit.MILLISECONDS);
		}

		private void startButtonTimer(final int operation, final Point pos) {
			stopTimer();
			stopButtonTimer();
			buttonTask = UiActivator.getScheduledExecutorService().schedule(() -> {
				if (!displ.isDisposed())
					displ.syncExec(() -> gotoSlide(pos, request, operation));
			}, 700L, TimeUnit.MILLISECONDS);
		}

		private void gotoSlide(Point pos, SlideRequest current, int operation) {
			inhibitTimer = true;
			try {
				stopTimer();
				List<SlideImpl> slideList = new ArrayList<SlideImpl>(requestList.size());
				switch (operation) {
				case BACK:
					for (int i = requestList.size() - 1; i >= 0; i--) {
						SlideRequest r = requestList.get(i);
						if (r == current)
							break;
						slideList.add(r.getSlide());
					}
					break;
				case FORWARD:
					boolean found = false;
					for (SlideRequest r : requestList) {
						if (found)
							slideList.add(r.getSlide());
						if (r == current)
							found = true;
					}
					break;
				default:
					for (SlideRequest r : requestList)
						slideList.add(r.getSlide());
				}
				Shell sh = slideControl != null && !slideControl.isDisposed() ? slideControl.getShell()
						: new Shell(parent, SWT.NO_TRIM);
				selectSlideDialog = new SelectSlideDialog(sh, slideList, false);
				selectSlideDialog.create();
				if (pos != null) {
					pos.y -= selectSlideDialog.getShell().getSize().y;
					selectSlideDialog.getShell().setLocation(pos);
				}
				if (operation != BACK && operation != FORWARD)
					selectSlideDialog.setSelection(current.getSlide());
				int ret = selectSlideDialog.open();
				startTimer();
				if (ret == Window.OK) {
					SlideImpl selectedSlide = selectSlideDialog.getResult();
					for (SlideRequest r : requestList) {
						if (r.getSlide() == selectedSlide) {
							restartAt(r.getSeqno());
							break;
						}
					}
				}
				selectSlideDialog = null;
			} finally {
				inhibitTimer = false;
			}
		}

		protected void closeControl() {
			if (slideControl != null) {
				slideControl.dispose();
				slideControl = null;
			}
			mouseMovements = 0;
		}

		protected void stopTimer() {
			if (panelTask != null) {
				panelTask.cancel(true);
				panelTask = null;
			}
		}

		protected void stopButtonTimer() {
			if (buttonTask != null) {
				buttonTask.cancel(true);
				buttonTask = null;
			}
		}

		public void helpRequested(HelpEvent e) {
			ToolTip toolTip = new ToolTip(displ.getActiveShell(), SWT.BALLOON);
			toolTip.setAutoHide(true);
			pnt.x = pnt.y = 0;
			toolTip.setLocation(pnt);
			toolTip.setText(Messages.getString("SlideShowPlayer.Slideshow")); //$NON-NLS-1$
			toolTip.setMessage(getKeyboardHelp());
			toolTip.setVisible(true);
		}

		private void metadataRequested(Asset asset) {
			ToolTip toolTip = new ToolTip(displ.getActiveShell(), SWT.BALLOON);
			toolTip.setAutoHide(true);
			Rectangle bounds = displ.getPrimaryMonitor().getBounds();
			pnt.x = bounds.width / 2;
			pnt.y = bounds.height / 2;
			toolTip.setLocation(pnt);
			toolTip.setText(NLS.bind(Messages.getString("SlideShowPlayer.metadata"), asset.getName())); //$NON-NLS-1$
			toolTip.setMessage(new HoverInfo(asset, (ImageRegion[]) null).getText());
			toolTip.setVisible(true);
		}

		private void rateSlide(final Control control, MouseEvent event, int rating, boolean autoClose) {
			stopTimer();
			Shell shell = control instanceof Shell ? (Shell) control : control.getShell();
			RatingDialog ratingDialog = new RatingDialog(shell, rating != RatingDialog.ABORT ? rating
					: request.isRemoveFromShow() ? RatingDialog.DELETE : request.getRating(), 1d, false, false);
			if (autoClose)
				ratingDialog.setTimeout(3000);
			ratingDialog.create();
			if (event != null)
				ratingDialog.getShell().setLocation(control.toDisplay(event.x, event.y));
			rating = ratingDialog.open();
			switch (rating) {
			case RatingDialog.DELETE:
				if (request.isDeleted()) {
					request.setDeleted(false);
					request.setDeleteFromDisk(false);
					request.setRemoveFromShow(false);
				} else
					processDelete(bottomShell, shell.getBounds());
				break;
			case RatingDialog.ABORT:
				break;
			default:
				request.setDeleted(false);
				request.setDeleteFromDisk(false);
				request.setRemoveFromShow(false);
				request.setRating(rating);
				break;
			}
		}

		protected void recordVoiceNote(int xoff, SlideRequest request) {
			if (request != null) {
				Asset asset = request.getAsset();
				if (asset != null && asset.getFileState() != IVolumeManager.PEER) {
					UiActivator.getDefault().stopAudio();
					VoiceNoteDialog dialog = new VoiceNoteDialog(fadingShell.getShell(), asset);
					dialog.create();
					Point p = slideControl.getLocation();
					dialog.getShell().setLocation(p.x + xoff, p.y - dialog.getShell().getSize().y);
					if (dialog.open() == VoiceNoteDialog.OK)
						request.setVoiceNote(dialog.getSourceUri(), dialog.getTargetUri(), dialog.getNoteText(),
								dialog.getSvg());
				}
			}
		}

		private void addToAlbums(final Control control, Point pos) {
			stopTimer();
			Shell shell = control instanceof Shell ? (Shell) control : control.getShell();
			IDbManager dbManager = Core.getCore().getDbManager();
			String assetId = request.getSlide().getAsset();
			String[] albums = null;
			if (assetId != null) {
				AssetImpl asset = dbManager.obtainAsset(assetId);
				if (asset != null)
					albums = asset.getAlbum();
			}
			AlbumSelectionDialog addAlbumDialog = new AlbumSelectionDialog(shell, true,
					(albums != null) ? Arrays.asList(albums) : null, selectedAlbums);
			addAlbumDialog.create();
			if (pos != null) {
				pos.y -= addAlbumDialog.getShell().getSize().y;
				addAlbumDialog.getShell().setLocation(pos);
			}
			if (addAlbumDialog.open() == AlbumSelectionDialog.OK) {
				Collection<SmartCollectionImpl> checkedAlbums = addAlbumDialog.getResult();
				request.setAlbums(
						selectedAlbums = checkedAlbums.toArray(new SmartCollectionImpl[checkedAlbums.size()]));
			}
		}

		private void addToSelection(SlideRequest request, boolean select) {
			if (request.getImage() != null && request.getSlide().getAsset() != null) {
				request.setSelected(select);
				closeControl();
				request.setPaused(true);
				doShowControl();
			}
		}

		private void processDelete(Shell shell, Rectangle bounds) {
			SlideImpl thisslide = request.getSlide();
			String assetId = thisslide.getAsset();
			IDbManager dbManager = Core.getCore().getDbManager();
			List<SlideImpl> otherSlides = dbManager.obtainObjects(SlideImpl.class, "asset", assetId, QueryField.EQUALS); //$NON-NLS-1$
			List<ExhibitImpl> exhibits = dbManager.obtainObjects(ExhibitImpl.class, "asset", assetId, //$NON-NLS-1$
					QueryField.EQUALS);
			List<WebExhibitImpl> webexhibits = dbManager.obtainObjects(WebExhibitImpl.class, "asset", assetId, //$NON-NLS-1$
					QueryField.EQUALS);
			if (!(otherSlides.isEmpty() || otherSlides.size() == 1 && otherSlides.contains(thisslide))
					|| !exhibits.isEmpty() || !webexhibits.isEmpty()) {
				AcousticMessageDialog dialog = new AcousticMessageDialog(shell,
						Messages.getString("SlideShowPlayer.delete_image"), //$NON-NLS-1$
						null, // accept
						Messages.getString("SlideShowPlayer.also_used_in_other_presentations"), //$NON-NLS-1$
						AcousticMessageDialog.QUESTION,
						new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 0);
				if (bounds != null)
					dialog.setY(bounds.y);
				dialog.create();
				dialog.setReturnCode(-1);
				if (dialog.open() == 0) {
					request.setDeleted(false);
					request.setRemoveFromShow(true);
					request.setDeleteFromDisk(false);
				}
			} else {
				AcousticMessageDialog dialog = new AcousticMessageDialog(shell,
						Messages.getString("SlideShowPlayer.delete_image"), //$NON-NLS-1$
						null, Messages.getString("SlideShowPlayer.this_will_delete_from_cat_and_slideshow"), //$NON-NLS-1$
						MessageDialog.QUESTION,
						adhoc ? new String[] { Messages.getString("SlideShowPlayer.only_in_catalog"), //$NON-NLS-1$
								Messages.getString("SlideShowPlayer.delete_on_disc_too"), //$NON-NLS-1$
								IDialogConstants.CANCEL_LABEL }
								: new String[] { Messages.getString("SlideShowPlayer.only_in_catalog"), //$NON-NLS-1$
										Messages.getString("SlideShowPlayer.delete_on_disc_too"), //$NON-NLS-1$
										Messages.getString("SlideShowPlayer.remove_only_from_slideshow"), //$NON-NLS-1$
										IDialogConstants.CANCEL_LABEL },
						0);
				if (bounds != null)
					dialog.setY(bounds.y);
				dialog.create();
				dialog.setReturnCode(-1);
				switch (dialog.open()) {
				case 0:
					request.setDeleted(true);
					request.setRemoveFromShow(true);
					request.setDeleteFromDisk(false);
					break;
				case 1:
					request.setDeleted(true);
					request.setRemoveFromShow(true);
					request.setDeleteFromDisk(true);
					break;
				case 2:
					if (!adhoc) {
						request.setDeleted(false);
						request.setRemoveFromShow(true);
						request.setDeleteFromDisk(false);
					}
					break;
				}
			}
		}

		private void rotateClockwise(int angle) {
			ZImage image = request.getImage();
			if (image != null) {
				image.setRotation(image.getRotation() + angle, 1f, 1f);
				int rotation = request.getRotation();
				rotation += angle;
				if (rotation >= 360)
					rotation -= 360;
				request.setRotation(rotation);
				restart(0);
			}
		}

		public void setPaused(boolean paused) {
			if (request != null)
				request.setPaused(paused);
			mouseMovements = 0;
		}

	}

	public class MainLoopJob extends Job {

		private int currentSlide;
		private int startFrom;
		private Shell shell;
		private long timeout;
		private SlideRequest request;

		public MainLoopJob(int firstSlide, int positionFirstSlide, Shell shell, Rectangle mbounds) {
			super(Messages.getString("SlideShowPlayer.slideshow_main_loop")); //$NON-NLS-1$
			timeout = UiActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.INACTIVITYINTERVAL)
					* 60000L;
			setPriority(Job.INTERACTIVE);
			setSystem(true);
			this.currentSlide = firstSlide;
			this.startFrom = positionFirstSlide;
			this.shell = shell;
		}

		@Override
		public boolean belongsTo(Object family) {
			return Constants.SLIDESHOW == family;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			for (int i = 0; i < 300; i++) {
				if (requestList != null && System.currentTimeMillis() - startTime >= STARTUPDELAY)
					break;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			SlideRequest answer = null;
			try {
				boolean pauseAt = false;
				if (currentSlide < requestList.size()) {
					request = requestList.get(currentSlide++);
					show: while (!checkStatus(request, monitor)) {
						SlideImpl slide = null;
						singleSlideJob = null;
						answer = null;
						try {
							answer = answers.poll(180, TimeUnit.SECONDS);
						} catch (InterruptedException e) {
							// should never happen
						}
						if (checkStatus(request, monitor))
							break;
						int d = 0;
						if (answer != null && !shell.isDisposed()) {
							shell.getDisplay().asyncExec(() -> {
								if (!shell.isDisposed()) {
									text.setText(""); //$NON-NLS-1$
									bottomCanvas.redraw();
								}
							});
							slide = answer.getSlide();
							// ZImage image = answer.getImage();
							// if (image != null) {
							d = slide.getDelay() + slide.getDuration() + slide.getFadeOut();
							singleSlideJob = new SingleSlideJob(answer, startFrom,
									slide.getDuration() - answer.getOverlap(), shell, mbounds, pauseAt,
									new FadingListener() {
										public void fadeoutStarted() {
											// do nothing
										}

										public void fadeoutEnded() {
											// do nothing
										}

										public void fadeinStarted() {
											// do nothing
										}

										public void fadeinEnded() {
											preloadNextSlide();
										}
									});
							singleSlideJob.schedule();
							// }
						}
						pauseAt = false;
						if (checkStatus(answer, monitor))
							break;
						startFrom = 0;
						if (singleSlideJob != null) {
							single: while (true) {
								if (checkStatus(answer, monitor))
									break show;
								try {
									SlideRequest receipt = done.poll(d + timeout, TimeUnit.MILLISECONDS);
									int operation = ABORT;
									if (receipt != null) {
										operation = receipt.getOperation();
										receipt.setOperation(NOOP);
									}
									switch (operation) {
									case RESTART:
										if (receipt != null)
											receipt.setPaused(false);
										sleepTick(TICK);
										if (slide != null && receipt != null)
											new SingleSlideJob(receipt, slide.getDelay(),
													slide.getDuration() - receipt.getOverlap(), shell, mbounds, true,
													null).schedule();
										break;
									case FORWARD:
										if (receipt != null)
											receipt.setPaused(false);
										pauseAt = true;
										break single;
									case BACK:
										request = jumpToSlide(request, receipt,
												receipt == null ? 0 : receipt.getSeqno() - 1,
												Messages.getString("SlideShowPlayer.loading_previous_slide")); //$NON-NLS-1$
										pauseAt = true;
										continue show;
									case NOOP:
									case CONTINUE:
										break single;
									case ABORT:
									case IGNORE:
										request.setAborted(true);
										break show;
									default:
										request = jumpToSlide(request, receipt, operation,
												Messages.getString("SlideShowPlayer.loading_selected_slide")); //$NON-NLS-1$
										pauseAt = true;
										continue show;
									}
								} catch (InterruptedException e1) {
									break;
								}
							}
						}
						if (checkStatus(request, monitor))
							break;
						if (currentSlide > requestList.size()) {
							if (slide != null && !shell.isDisposed()) {
								shell.getDisplay().asyncExec(() -> {
									if (!shell.isDisposed()) {
										text.setText(Messages.getString("SlideShowPlayer.end_of_show")); //$NON-NLS-1$
										CssActivator.getDefault().setColors(bottomShell);
										bottomCanvas.redraw();
									}
								});
								for (int i = computeSteps(slide.getFadeOut() + 500) - 1; i >= 0; i--) {
									sleepTick(TICK);
									if (checkStatus(request, monitor))
										break;
								}
							}
							break;
						}
					}
				}
				if (!ignoreChanges)
					new ApplyChangesJob(requestList).schedule();
				if (adhoc && !display.isDisposed())
					display.asyncExec(() -> {
						if (!display.isDisposed())
							promptForSave(requestList);
					});
				return Status.OK_STATUS;
			} catch (Exception e) {
				return new Status(IStatus.ERROR, UiActivator.PLUGIN_ID,
						Messages.getString("SlideShowPlayer.internal_error"), e); //$NON-NLS-1$
			} finally {
				if (exifTool != null) {
					exifTool.close();
					exifTool = null;
				}
				if (answer != null)
					answer.dispose();
				if (!shell.isDisposed())
					shell.getDisplay().asyncExec(() -> {
						if (!shell.isDisposed())
							close();
					});
			}
		}

		private void preloadNextSlide() {
			if (currentSlide < requestList.size()) {
				request = requestList.get(currentSlide);
				requests.add(request);
			}
			++currentSlide;
		}

		private SlideRequest jumpToSlide(SlideRequest req, SlideRequest receipt, int pos, final String message)
				throws InterruptedException {
			// waste currently converted image
			if (currentSlide <= requestList.size()) {
				req.setAborted(true);
				answers.poll(180, TimeUnit.SECONDS);
				req.setAborted(false);
			}
			// jump to slide and ask for another
			currentSlide = pos;
			req = requestList.get(currentSlide++);
			if (!cache.containsKey(req.getSlide().getStringId()) && !shell.isDisposed())
				shell.getDisplay().asyncExec(() -> {
					if (!text.isDisposed()) {
						text.setText(message);
						bottomCanvas.redraw();
					}
				});
			requests.add(req);
			// continue
			if (receipt != null)
				receipt.setPaused(false);
			return req;
		}

		private boolean checkStatus(SlideRequest req, IProgressMonitor monitor) {
			if (req != null) {
				int op = req.getOperation();
				if (op == ABORT || op == IGNORE || monitor.isCanceled())
					return true;
				while (req.isPaused())
					sleepTick(TICK);
			}
			return false;
		}
	}

	private class SlideRequest {
		private SlideImpl slide;
		private URI uri;
		private int rotation;
		private int rating = -1;
		private boolean deleted;
		private int overlap;
		private ZImage image;
		private int operation = NOOP;
		private boolean paused = false;
		private boolean aborted;
		private boolean deleteFromDisk;
		private boolean removeFromShow;
		private int seqno;
		private int previewSize;
		private List<AssetImpl> assets = new ArrayList<AssetImpl>();
		private SmartCollectionImpl[] albums;
		private boolean selected;
		private URL voiceUrl;
		private int voiceLength;
		private String sourceURI;
		private String targetURI;
		private String noteText;
		private String svg;
		private boolean annotationChanged;

		public SlideRequest(int seqno, SlideImpl slide, URI uri, int rotation, int rating, int previewSize) {
			this.seqno = seqno;
			this.slide = slide;
			this.uri = uri;
			this.rotation = rotation;
			this.rating = rating;
			this.previewSize = previewSize;
		}

		public void setVoiceNote(String sourceURI, String targetURI, String noteText, String svg) {
			this.sourceURI = sourceURI;
			this.targetURI = targetURI;
			this.noteText = noteText;
			this.svg = svg;
			annotationChanged = true;
		}

		public void setVoiceLength(int voiceLength) {
			this.voiceLength = voiceLength;
		}

		public void setVoiceUrl(URL voiceUrl) {
			this.voiceUrl = voiceUrl;
		}

		public void dispose() {
			if (image != null) {
				cache.put(slide.getStringId(), image);
				// image.dispose();
				image = null;
			}
		}

		public AssetImpl[] getNextAssets() {
			return assets.toArray(new AssetImpl[assets.size()]);
		}

		public boolean getDeleteFromDisk() {
			return deleteFromDisk;
		}

		public boolean isAborted() {
			return aborted;
		}

		public void setRotation(int rotation) {
			this.rotation = rotation;
		}

		public int getOverlap() {
			return overlap;
		}

		public int getRotation() {
			return rotation;
		}

		public SlideImpl getSlide() {
			return slide;
		}

		public URI getUri() {
			return uri;
		}

		public void setOverlap(int overlap) {
			this.overlap = overlap;
		}

		public ZImage getImage() {
			return image;
		}

		public void setImage(ZImage image) {
			this.image = image;
		}

		public int getRating() {
			return rating;
		}

		public void setRating(int rating) {
			this.rating = rating;
		}

		public boolean isDeleted() {
			return deleted;
		}

		public void setDeleted(boolean deleted) {
			this.deleted = deleted;
		}

		public int getOperation() {
			return operation;
		}

		public void setOperation(int operation) {
			this.operation = operation;
		}

		public boolean isPaused() {
			return paused;
		}

		public void setPaused(boolean paused) {
			this.paused = paused;
		}

		public void setAborted(boolean aborted) {
			this.aborted = aborted;
		}

		public void setDeleteFromDisk(boolean deleteFromDisk) {
			this.deleteFromDisk = deleteFromDisk;
		}

		public int getSeqno() {
			return seqno;
		}

		public int getPreviewSize() {
			return previewSize;
		}

		public void addAsset(AssetImpl asset) {
			assets.add(asset);
		}

		public void setRemoveFromShow(boolean removeFromShow) {
			this.removeFromShow = removeFromShow;
		}

		public boolean isRemoveFromShow() {
			return removeFromShow;
		}

		public SmartCollectionImpl[] getAlbums() {
			return albums;
		}

		public void setAlbums(SmartCollectionImpl[] albums) {
			this.albums = albums;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		public Asset getAsset() {
			return assets.isEmpty() ? null : assets.get(0);
		}

		public URL getVoiceUri() {
			return voiceUrl;
		}

		public int getVoiceLength() {
			return voiceLength;
		}

		public String getSourceURI() {
			return sourceURI;
		}

		public String getTargetURI() {
			return targetURI;
		}

		public String getNoteText() {
			return noteText;
		}

		public String getSvg() {
			return svg;
		}

		public boolean annotationChanged() {
			return annotationChanged;
		}
	}

	// Status
	private static final int NOOP = Integer.MIN_VALUE;
	private static final int BACK = -1;
	private static final int RESTART = -2;
	private static final int FORWARD = -3;
	private static final int CONTINUE = -4;
	private static final int ABORT = Integer.MAX_VALUE;
	private static final int IGNORE = Integer.MAX_VALUE - 1;
	// Commands
	private static final int C_BACK = 0;
	private static final int C_PLAY = 1;
	private static final int C_STOP = 2;
	private static final int C_FORWARD = 3;
	private static final int C_JUMP = 4;
	private static final int C_ROTCW = 5;
	private static final int C_ROTCCW = 6;
	private static final int C_DELETE = 7;
	private static final int C_RATE = 8;
	private static final int C_ALBUM = 9;
	private static final int C_SELECTION = 10;
	private static final int C_VOICE = 11;

	private static final double MARGINHEIGHT = 0.1d;
	private static final double MARGINWIDTH = 0.1d;
	private static final double THUMBNAILSPACE = 0.3d;
	private static SlideRequest FINALREQUEST;
	private static final int CLOSE_CLOSE = 0;
	private static final int CLOSE_IGNORE = 1;
	private static final int CLOSE_CANCEL = 2;

	BlockingQueue<SlideRequest> requests;
	BlockingQueue<SlideRequest> answers;
	BlockingQueue<SlideRequest> done;
	private SlideShowImpl slideshow;
	private Shell bottomShell;
	private Canvas bottomCanvas;
	private TextLayout text;
	private List<SlideImpl> slides;
	private boolean advanced;
	private List<SlideRequest> requestList;
	private int cms;
	private int total;
	private int offline;
	private int duration;
	private int firstSlide;
	private int positionFirstSlide;
	protected boolean adhoc;
	private PreparationJob prepJob;
	protected SingleSlideJob singleSlideJob;
	private long startTime;
	private ExifTool exifTool;
	public Job transferJob;
	public boolean ignoreMouseMovements;
	public boolean ignoreChanges;
	private Cache cache = new Cache(CACHESIZE);

	// State

	public void init(IWorkbenchWindow parentWindow, SlideShowImpl slideshow, List<SlideImpl> slides, boolean adhoc) {
		super.init(parentWindow, PRIMARY);
		FINALREQUEST = new SlideRequest(-1, null, null, 0, -1, 0);
		this.adhoc = adhoc;
		this.slideshow = slideshow;
		this.slides = slides;
		IPreferencesService preferencesService = Platform.getPreferencesService();
		advanced = preferencesService.getBoolean(UiActivator.PLUGIN_ID, PreferenceConstants.ADVANCEDGRAPHICS, false,
				null);
		cms = preferencesService.getInt(UiActivator.PLUGIN_ID, PreferenceConstants.COLORPROFILE, ImageConstants.SRGB,
				null);
	}

	public void promptForSave(List<SlideRequest> reqList) {
		final Set<String> deleted = new HashSet<String>();
		for (SlideRequest request : reqList)
			if (request.isRemoveFromShow())
				deleted.add(request.getSlide().getStringId());
		if (slides.size() > deleted.size()) {
			ToolTip tooltip = new ToolTip(parentWindow.getShell(), SWT.BALLOON);
			tooltip.setText(Messages.getString("SlideShowPlayer.save_show")); //$NON-NLS-1$
			tooltip.setMessage(Messages.getString("SlideShowPlayer.save_show_msg")); //$NON-NLS-1$
			tooltip.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					doPromptForSave(deleted);
				}
			});
			tooltip.setVisible(true);
		}
	}

	private void doPromptForSave(final Set<String> deleted) {
		SlideShowSaveDialog dialog = new SlideShowSaveDialog(parentWindow.getShell());
		if (dialog.open() == Window.OK) {
			final String name = dialog.getName();
			final Group group = dialog.getGroup();
			boolean open = dialog.getOpen();
			final IDbManager db = Core.getCore().getDbManager();
			db.safeTransaction(() -> {
				for (Slide slide : slides)
					if (deleted.contains(slide.toString()))
						slideshow.removeEntry(slide.toString());
					else
						db.store(slide);
				slideshow.setName(name);
				slideshow.setAdhoc(false);
				group.addSlideshow(slideshow.toString());
				slideshow.setGroup_slideshow_parent(group.toString());
				db.store(slideshow);
				db.store(group);
			});
			if (open) {
				IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (ww != null) {
					IWorkbenchPage page = ww.getActivePage();
					if (page != null) {
						BasicView.openPerspective(SlideshowView.SLIDES_PERSPECTIVE);
						try {
							IViewPart view = page.showView(SlideshowView.ID);
							if (view instanceof SlideshowView)
								((SlideshowView) view).setInput(slideshow);
						} catch (PartInitException e) {
							// should never happen
						}
					}
				}
			}
			Core.getCore().fireStructureModified();
		}
	}

	public String getKeyboardHelp() {
		return Messages.getString("SlideShowPlayer.move_mouse_for_panel"); //$NON-NLS-1$
	}

	public void create() {
		super.create();
		bottomShell = createKioskShell(Messages.getString("SlideShowPlayer.slideshow")); //$NON-NLS-1$
		bottomCanvas = new Canvas(bottomShell, SWT.DOUBLE_BUFFERED);
		bottomCanvas.addKeyListener(this);
		bottomCanvas.addMouseListener(this);
		text = new TextLayout(display);
		text.setAlignment(SWT.CENTER);
		text.setFont(JFaceResources.getFont(UiConstants.VIEWERFONT));
		createTransparentCursor();
		bottomCanvas.setCursor(transparentCursor);
		bottomCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Rectangle sbounds = bottomCanvas.getClientArea();
				GC gc = e.gc;
				gc.setBackground(bottomShell.getBackground());
				gc.fillRectangle(sbounds);
				gc.setForeground(bottomShell.getForeground());
				text.setWidth(sbounds.width / 2);
				Rectangle tbounds = text.getBounds();
				text.draw(gc, (sbounds.width - tbounds.width) / 2, (sbounds.height - tbounds.height) / 2);
			}
		});
		bottomShell.setData(CSSProperties.ID, CSSProperties.SLIDESHOW);
		CssActivator.getDefault().setColors(bottomShell);
		text.setText(NLS.bind(slideshow.getAdhoc() ? Messages.getString("SlideShowPlayer.preparing") //$NON-NLS-1$
				: Messages.getString("SlideShowPlayer.preparing_slideshow"), //$NON-NLS-1$
				slideshow.getName()));
		bottomShell.layout();
	}

	public void prepare(final int position) {
		Job.getJobManager().cancel(Constants.SLIDESHOW);
		requests = new LinkedBlockingQueue<SlideRequest>();
		answers = new LinkedBlockingQueue<SlideRequest>();
		done = new LinkedBlockingQueue<SlideRequest>();
		requestList = new ArrayList<SlideRequest>(slides.size());
		total = 0;
		offline = 0;
		duration = 0;
		firstSlide = -1;
		positionFirstSlide = 0;
		prepJob = new PreparationJob(position, mbounds);
		prepJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				try {
					if (!display.isDisposed())
						display.asyncExec(() -> {
							if (!display.isDisposed() && event.getResult().isOK())
								showInfo(position, slideshow.getName());
						});
				} finally {
					UiActivator.getDefault().setSlideShowRunning(false);
				}
			}
		});
		prepJob.schedule();
	}

	private static String formatPeriod(int dur) {
		int hours = dur / 3600000;
		int minutes = (dur - hours * 3600000) / 60000;
		int seconds = (dur - hours * 3600000 - minutes * 60000) / 1000;
		StringBuilder sb = new StringBuilder();
		if (hours != 0)
			sb.append(NLS.bind(Messages.getString("SlideShowPlayer.n_hours"), hours)); //$NON-NLS-1$
		if (minutes != 0 || hours != 0) {
			if (sb.length() > 0)
				sb.append(", "); //$NON-NLS-1$
			sb.append(NLS.bind(Messages.getString("SlideShowPlayer.n_minutes"), minutes)); //$NON-NLS-1$
		}
		if (sb.length() > 0)
			sb.append(", "); //$NON-NLS-1$
		sb.append(NLS.bind(Messages.getString("SlideShowPlayer.n_seconds"), seconds)); //$NON-NLS-1$
		return sb.toString();
	}

	public void open(final int position) {
		UiActivator.getDefault().setSlideShowRunning(true);
		try {
			if (bottomShell == null) {
				create();
				prepare(position);
			}
			bottomShell.open();
			if (!display.isDisposed())
				display.asyncExec(() -> {
					if (!display.isDisposed())
						guiLoop();
				});
		} catch (Exception e) {
			UiActivator.getDefault().setSlideShowRunning(false);
		}
	}

	private void guiLoop() {
		startTime = System.currentTimeMillis();
		bottomCanvas.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
		while (prepJob.getState() == Job.RUNNING && !bottomShell.isDisposed()) {
			while (bottomShell != null && !bottomShell.isDisposed())
				if (!display.readAndDispatch())
					break;
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				break;
			}
		}
		if (firstSlide >= 0)
			new MainLoopJob(firstSlide, positionFirstSlide, bottomShell, mbounds).schedule();
		while (bottomShell != null && !bottomShell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();
	}

	private void showInfo(int position, String name) {
		StringBuilder sb = new StringBuilder(512);
		sb.append(name);
		String description = slideshow.getDescription();
		if (description != null && !description.isEmpty())
			sb.append("\n\n").append(description); //$NON-NLS-1$
		sb.append("\n\n").append(NLS.bind(Messages.getString("SlideShowPlayer.n_slides"), total)); //$NON-NLS-1$ //$NON-NLS-2$
		if (firstSlide < 0)
			sb.append('\n').append(Messages.getString("SlideShowPlayer.all_images_are_offline")); //$NON-NLS-1$
		else {
			if (offline > 0) {
				sb.append('\n');
				if (offline == 1)
					sb.append(Messages.getString("SlideShowPlayer.one_image_is_offline")); //$NON-NLS-1$
				else
					sb.append(NLS.bind(Messages.getString("SlideShowPlayer.n_images_are_offline"), //$NON-NLS-1$
							offline));
			}
			sb.append('\n').append(NLS.bind(Messages.getString("SlideShowPlayer.total_duration"), //$NON-NLS-1$
					formatPeriod(duration)));
			if (position > 0)
				sb.append('\n').append(NLS.bind(Messages.getString("SlideShowPlayer.duration_from_current_position"), //$NON-NLS-1$
						formatPeriod(duration - position)));
		}
		sb.append('\n').append(Messages.getString("SlideShowPlayer.press_esc_to_cancel")); //$NON-NLS-1$
		if (firstSlide >= 0)
			sb.append('\n').append(Messages.getString("SlideShowPlayer.move_mouse_to_obtain_control_panel")); //$NON-NLS-1$
		text.setText(sb.toString());
		text.setStyle(new TextStyle(JFaceResources.getFont(UiConstants.VIEWERTITLEFONT), null, null), 0, name.length());
		bottomCanvas.redraw();
	}

	public boolean close() {
		requests.add(FINALREQUEST);
		if (transferJob != null)
			Job.getJobManager().cancel(transferJob);
		Job.getJobManager().cancel(Constants.SLIDESHOW);
		if (text != null)
			text.dispose();
		if (!bottomShell.isDisposed())
			bottomShell.close();
		for (ZImage image : cache.values())
			image.dispose();
		cache.clear();
		return super.close();
	}

	public void mouseDoubleClick(MouseEvent e) {
		closeWithPrompt();
	}

	public void mouseDown(MouseEvent e) {
		// do nothing
	}

	public void mouseUp(MouseEvent e) {
		// do nothing
	}

	public void keyPressed(KeyEvent e) {
		// do nothing
	}

	public void keyReleased(KeyEvent e) {
		if (e.character == SWT.TAB || e.keyCode == SWT.ESC)
			closeWithPrompt();
	}

	private void closeWithPrompt() {
		if (singleSlideJob != null)
			singleSlideJob.setPaused(true);
		int ret = showClosePrompt(bottomShell);
		if (ret != CLOSE_CANCEL) {
			if (ret == CLOSE_IGNORE)
				ignoreChanges = true;
			close();
		}
	}

	private int showClosePrompt(Shell shell) {
		boolean changes = false;
		for (SlideRequest request : requestList) {
			if (request.isSelected() || request.isDeleted() || request.isRemoveFromShow()) {
				changes = true;
				break;
			}
			Asset asset = request.getAsset();
			if (asset != null) {
				int rating = request.getRating();
				if (rating != RatingDialog.ABORT && rating != asset.getRating()) {
					changes = true;
					break;
				}
				if ((request.getRotation() - asset.getRotation()) % 360 != 0) {
					changes = true;
					break;
				}
			}
			SmartCollectionImpl[] albums = request.getAlbums();
			if (albums != null && albums.length > 0) {
				changes = true;
				break;
			}
			if (request.annotationChanged()) {
				changes = true;
				break;
			}
		}
		AcousticMessageDialog dialog = new AcousticMessageDialog(shell, Messages.getString("SlideShowPlayer.close"), //$NON-NLS-1$
				null, Messages.getString("SlideShowPlayer.really_close"), //$NON-NLS-1$
				MessageDialog.QUESTION, changes ? new String[] { Messages.getString("SlideShowPlayer.close_close"), //$NON-NLS-1$
						Messages.getString("SlideShowPlayer.close_ignore"), //$NON-NLS-1$
						Messages.getString("SlideShowPlayer.close_cancel") } //$NON-NLS-1$
						: new String[] { Messages.getString("SlideShowPlayer.close_close"), //$NON-NLS-1$
								Messages.getString("SlideShowPlayer.close_cancel") }, //$NON-NLS-1$
				CLOSE_CLOSE);
		dialog.create();
		int ret = dialog.open();
		return ret == (changes ? 2 : 1) ? CLOSE_CANCEL : ret;
	}

	int computeSteps(int dur) {
		return (dur + TICK - 1) / TICK;
	}

	@Override
	public boolean isDisposed() {
		return bottomShell.isDisposed();
	}

}
