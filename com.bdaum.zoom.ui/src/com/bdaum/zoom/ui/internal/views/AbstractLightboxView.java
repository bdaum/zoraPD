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
 * (c) 2009-2017 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.gallery.AbstractGridGroupRenderer;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryGroupRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.asset.Region;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.IPostProcessor2;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.css.internal.IExtendedColorModel2;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.ZoomActionFactory;
import com.bdaum.zoom.ui.internal.dialogs.ColorCodeDialog;
import com.bdaum.zoom.ui.internal.dialogs.RatingDialog;
import com.bdaum.zoom.ui.internal.dialogs.StatusDialog;
import com.bdaum.zoom.ui.internal.job.GalleryDecorateJob;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public abstract class AbstractLightboxView extends AbstractGalleryView
		implements SelectionListener, IExtendedColorModel2 {

	public static class DecoJob extends Job {
		public static final Object ID = "ThumbDeco"; //$NON-NLS-1$
		private GalleryItem currentItem;
		private Gallery gallery;

		public DecoJob(Gallery gallery, GalleryItem currentItem) {
			super(Messages.getString("AbstractLightboxView.deco_thumb")); //$NON-NLS-1$
			this.gallery = gallery;
			this.currentItem = currentItem;
			setSystem(true);
			setPriority(Job.INTERACTIVE);
		}

		@Override
		public boolean belongsTo(Object family) {
			return family == ID;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (currentItem != null && !gallery.isDisposed())
				gallery.getDisplay().asyncExec(() -> {
					if (!gallery.isDisposed())
						gallery.redraw(currentItem);
				});
			return Status.OK_STATUS;
		}
	}

	protected static final String ROT = "rot"; //$NON-NLS-1$
	protected static final int LOWER_THUMBNAIL_HMARGINS = 5;
	protected static final int UPPER_THUMBNAIL_HMARGINS = 8;
	protected static final int UPPER_THUMBNAIL_SPACING = 5;
	protected static final int COLORSPOT = 17;
	protected static final Integer R90 = 90;
	protected static final Integer R270 = 270;
	private static final Point pnt = new Point(0, 0);
	private static final int RATING_NO = 0;
	private static final int RATING_SIZE = 1;
	private static final int RATING_COUNT = 2;

	protected final class GalleryPaintListener implements Listener {

		private final LightboxGalleryItemRenderer ir;

		private GalleryPaintListener(LightboxGalleryItemRenderer ir) {
			this.ir = ir;
		}

		public void handleEvent(Event e) {
			GalleryItem item = (GalleryItem) e.item;
			if (item.isDisposed() || item.getImage() == null)
				return;
			AssetImpl asset = (AssetImpl) item.getData(ASSET);
			if (item.getImage() == placeHolder) {
				item.setImage(getImage(asset));
				setItemText(item, asset, (Integer) item.getData(CARD));
			} else if (item.getImage().isDisposed())
				item.setImage(getImage(asset));
			if (asset == null)
				return;
			ISelection sel = getSelection();
			boolean isSelected = (sel instanceof AssetSelection && ((AssetSelection) sel).isSelected(asset));
			boolean hasMouse = item == currentItem;
			ir.setSelected(isSelected);
			GC gc = e.gc;
			int x = e.x;
			int y = e.y;
			int width = e.width;
			int height = e.height;
			// Regions
			ir.setShowRegions(showRegions, hasMouse || isSelected);
			SmartCollectionImpl selectedCollection = getNavigationHistory().getSelectedCollection();
			ir.setPersonFilter(selectedCollection == null ? null
					: selectedCollection.getSystem() && selectedCollection.getAlbum() ? selectedCollection.getStringId()
							: null);
			ir.draw(gc, item, e.index, x, y, width, height);
			item.setData(REGIONS, ir.getRegions());
			// Decoration
			double factor = (64 + width) / 512d;
			int th = (int) (LOWER_THUMBNAIL_HMARGINS * factor + 0.5d);
			factor = (4 * factor + 1) / 5;
			Rectangle bounds0 = null;
			Rectangle bounds1 = null;
			Rectangle bounds2 = null;
			Rectangle bounds3 = null;
			Rectangle bounds4 = null;
			Rectangle bounds5 = null;
			Rectangle bounds6 = null;
			Rectangle bounds7 = null;
			Rectangle bounds8 = null;
			int fontHeight = gc.getFontMetrics().getHeight();
			IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(asset.getFormat());
			if (mediaSupport != null) {
				Image mediaIcon = mediaSupport.getIcon40();
				if (mediaIcon != null) {
					Rectangle bounds = mediaIcon.getBounds();
					int w = (int) (bounds.width * factor + 0.5d);
					int h = (int) (bounds.height * factor + 0.5d);
					gc.drawImage(mediaIcon, bounds.x, bounds.y, bounds.width, bounds.height, width + x - w - th,
							y + th + h * 3 / 4, w, h);
				}
			}
			if (hasMouse) {
				if ((!showExpandCollapseButton || !folding || item.getData(CARD) == null)
						&& asset.getFileState() == IVolumeManager.ONLINE) {
					int titleHeight = fontHeight + 5;
					bounds0 = new Rectangle(x, y + height - titleHeight + 4, width, titleHeight - 4);
				}
				if (showRotateButtons) {
					Object rot = item.getData(ROT);
					Image rotate270icon = ((rot == R270) ? Icons.rotate270f
							: (isSelected) ? Icons.rotate270 : Icons.rotate270d).getImage();
					bounds1 = rotate270icon.getBounds();
					int w = (int) (bounds1.width * factor + 0.5d);
					int h = (int) (bounds1.height * factor + 0.5d);
					int x1 = width + x - w - th;
					int y1 = y + height - h - fontHeight;
					gc.drawImage(rotate270icon, bounds1.x, bounds1.y, bounds1.width, bounds1.height, x1, y1, w, h);
					bounds1.x = x1 - 5;
					bounds1.y = y1 - 5;
					bounds1.width = w + 10;
					bounds1.height = h + 10;
					Image rotate90icon = ((rot == R90) ? Icons.rotate90f
							: (isSelected) ? Icons.rotate90 : Icons.rotate90d).getImage();
					bounds2 = rotate90icon.getBounds();
					int x2 = x + th;
					gc.drawImage(rotate90icon, bounds2.x, bounds2.y, bounds2.width, bounds2.height, x2, y1, w, h);
					bounds2.x = x2 - 5;
					bounds2.y = y1 - 5;
					bounds2.width = w + 10;
					bounds2.height = h + 10;
				}
			}
			// Voicenote
			String voiceFileURI = asset.getVoiceFileURI();
			if (showVoicenoteButton && voiceFileURI != null && !voiceFileURI.isEmpty()) {
				Image speakerIcon = (voiceFileURI.startsWith("?") ? Icons.note : Icons.speaker).getImage(); //$NON-NLS-1$
				Rectangle ibounds = speakerIcon.getBounds();
				bounds5 = new Rectangle(x + (width - (int) (ibounds.width * factor + 0.5d)) / 2,
						y + height - (int) (ibounds.height * factor + 0.5d) - fontHeight,
						(int) (ibounds.width * factor + 0.5d), (int) (ibounds.height * factor + 0.5d));
				gc.drawImage(speakerIcon, ibounds.x, ibounds.y, ibounds.width, ibounds.height, bounds5.x, bounds5.y,
						bounds5.width, bounds5.height);
			}
			th = (int) (UPPER_THUMBNAIL_HMARGINS * factor + 0.5d);
			int w = (int) (COLORSPOT * factor + 0.5d);
			// ColorCode
			if (showColorCode) {
				int colorCode = asset.getColorCode();
				if (colorCode == Constants.COLOR_UNDEFINED) {
					IPostProcessor2[] processors = Core.getCore().getDbFactory().getAutoColoringProcessors();
					if (processors != null)
						for (int i = 0; i < processors.length; i++)
							if (processors[i] != null && processors[i].accept(asset)) {
								colorCode = i;
								break;
							}
				}
				if (hasMouse || colorCode != Constants.COLOR_UNDEFINED) {
					bounds3 = new Rectangle(x + th, y + th, w, w);
					Image colorCodeIcon = Icons.toSwtColors(colorCode);
					Rectangle ibounds = colorCodeIcon.getBounds();
					gc.drawImage(colorCodeIcon, ibounds.x, ibounds.y, ibounds.width, ibounds.height, bounds3.x,
							bounds3.y, bounds3.width, bounds3.height);
				}
			}
			// Location
			if (showLocation) {
				boolean gps = !(Double.isNaN(asset.getGPSLatitude()) || Double.isNaN(asset.getGPSLatitude()));
				if (!gps)
					gps = !Core.getCore().getDbManager()
							.obtainStructForAsset(LocationShownImpl.class, asset.getStringId(), false).isEmpty();
				if (gps || hasMouse) {
					Image locationIcon = (gps ? Icons.location : Icons.nolocation).getImage();
					bounds8 = locationIcon.getBounds();
					w = (int) (bounds8.width * factor + 0.5d);
					int h = (int) (bounds8.height * factor + 0.5d);
					int x1 = x;
					if (bounds3 != null)
						x1 += bounds3.width + UPPER_THUMBNAIL_SPACING + th;
					int y1 = y + th;
					gc.drawImage(locationIcon, bounds8.x, bounds8.y, bounds8.width, bounds8.height, x1, y1, w, h);
					bounds8.x = x1;
					bounds8.y = y1;
					bounds8.width = w;
					bounds8.height = h;
				}
			}
			// Rating
			if (RATING_NO != showRating) {
				int rating = asset.getRating();
				if (rating > 0) {
					double rfactor;
					Image ratingIcon;
					if (RATING_SIZE == showRating) {
						rfactor = factor * (rating + 0.6d) / 5.6d;
						ratingIcon = Icons.rating61.getImage();
					} else {
						rfactor = factor * 2.6d / 5.6d;
						switch (rating) {
						case 1:
							ratingIcon = Icons.rating61.getImage();
							break;
						case 2:
							ratingIcon = Icons.rating62.getImage();
							break;
						case 3:
							ratingIcon = Icons.rating63.getImage();
							break;
						case 4:
							ratingIcon = Icons.rating64.getImage();
							break;
						default:
							ratingIcon = Icons.rating65.getImage();
							break;
						}
					}
					bounds4 = ratingIcon.getBounds();
					w = (int) (bounds4.width * rfactor + 0.5d);
					int h = (int) (bounds4.height * rfactor + 0.5d);
					int x1 = width + x - w - th;
					int y1 = y + th - h / 4;
					gc.drawImage(ratingIcon, bounds4.x, bounds4.y, bounds4.width, bounds4.height, x1, y1, w, h);
					bounds4.x = x1;
					bounds4.y = y1;
					bounds4.width = w;
					bounds4.height = h;
				} else if (hasMouse) {
					double rfactor = factor * 0.64d;
					Image ratingEmptyIcon = Icons.rating60.getImage();
					bounds4 = ratingEmptyIcon.getBounds();
					w = (int) (bounds4.width * rfactor + 0.5d);
					int h = (int) (bounds4.height * rfactor + 0.5d);
					int x1 = width + x - w - th;
					int y1 = y + th - h / 4;
					gc.drawImage(ratingEmptyIcon, bounds4.x, bounds4.y, bounds4.width, bounds4.height, x1, y1, w, h);
					bounds4.x = x1;
					bounds4.y = y1;
					bounds4.width = w;
					bounds4.height = h;
				}
			}
			// Done mark
			if (showDoneMark) {
				int status = asset.getStatus();
				String mimeType = asset.getMimeType();
				if (status == Constants.STATE_READY
						|| status == Constants.STATE_CORRECTED && ImageConstants.IMAGE_X_RAW.equals(mimeType)) {
					Image doneIcon = Icons.done.getImage();
					bounds7 = doneIcon.getBounds();
					w = (int) (bounds7.width * factor + 0.5d);
					int h = (int) (bounds7.height * factor + 0.5d);
					int x1 = width + x - w;
					if (bounds4 != null)
						x1 -= bounds4.width + UPPER_THUMBNAIL_SPACING + th;
					int y1 = y + th - h / 3;
					gc.drawImage(doneIcon, bounds7.x, bounds7.y, bounds7.width, bounds7.height, x1, y1, w, h);
					bounds7.x = x1;
					bounds7.y = y1;
					bounds7.width = w;
					bounds7.height = h;
				}
			}
			// Expand/Collapse
			if (showExpandCollapseButton && folding && hasMouse) {
				Image expIcon = null;
				if (item.getData(CARD) != null)
					expIcon = Icons.expand.getImage();
				else {
					GalleryItem parentItem = item.getParentItem();
					int index = parentItem.indexOf(item);
					if (index >= 0) {
						GalleryItem nextItem = parentItem.getItem(index + 1);
						if (nextItem != null) {
							AssetImpl nextAsset = (AssetImpl) nextItem.getData(ASSET);
							if (nextAsset != null && nextAsset.getName().equals(asset.getName()))
								expIcon = Icons.collaps.getImage();
						}
					}
				}
				if (expIcon != null) {
					bounds6 = expIcon.getBounds();
					int x1 = x + (width - w) / 2;
					int y1 = y + th / 2;
					w = (int) (bounds6.width * factor + 0.5d);
					int h = (int) (bounds6.height * factor + 0.5d);
					gc.drawImage(expIcon, bounds6.x, bounds6.y, bounds6.width, bounds6.height, x1, y1, w, h);
					bounds6.x = x1;
					bounds6.y = y1;
					bounds6.width = w;
					bounds6.height = h;
				}
			}
			item.setData(HOTSPOTS,
					new Hotspots(bounds3, bounds8, bounds4, bounds2, bounds1, bounds5, bounds6, bounds7, bounds0));
		}
	}

	protected static class Hotspots {
		private Rectangle colorSpot, locationPin, rating, rotate90, rotate270;
		private Rectangle voiceNotes, expandCollapse;
		private final Rectangle doneMark;
		private final Rectangle titleArea;

		public Hotspots(Rectangle colorSpot, Rectangle locationSpot, Rectangle rating, Rectangle rotate90,
				Rectangle rotate270, Rectangle voiceNotes, Rectangle expandCollapse, Rectangle doneMark,
				Rectangle titleArea) {
			super();
			this.colorSpot = colorSpot;
			this.locationPin = locationSpot;
			this.rating = rating;
			this.rotate90 = rotate90;
			this.rotate270 = rotate270;
			this.voiceNotes = voiceNotes;
			this.expandCollapse = expandCollapse;
			this.doneMark = doneMark;
			this.titleArea = titleArea;
		}

		public boolean isColorSpot(int x, int y) {
			return (colorSpot == null) ? false : colorSpot.contains(x, y);
		}

		public boolean isLocationPin(int x, int y) {
			return (locationPin == null) ? false : locationPin.contains(x, y);
		}

		public boolean isRating(int x, int y) {
			return (rating == null) ? false : rating.contains(x, y);
		}

		public boolean isRotate90(int x, int y) {
			return (rotate90 == null) ? false : rotate90.contains(x, y);
		}

		public boolean isRotate270(int x, int y) {
			return (rotate270 == null) ? false : rotate270.contains(x, y);
		}

		public boolean isVoicenotes(int x, int y) {
			return (voiceNotes == null) ? false : voiceNotes.contains(x, y);
		}

		public boolean isExpandCollapse(int x, int y) {
			return (expandCollapse == null) ? false : expandCollapse.contains(x, y);
		}

		public boolean isDoneMark(int x, int y) {
			return (doneMark == null) ? false : doneMark.contains(x, y);
		}

		public boolean isTitleArea(int x, int y) {
			return (getTitleArea() == null) ? false : getTitleArea().contains(x, y);
		}

		public boolean isHotspot(int x, int y) {
			return isRotate90(x, y) || isRotate270(x, y) || isRating(x, y) || isColorSpot(x, y) || isVoicenotes(x, y)
					|| isExpandCollapse(x, y) || isDoneMark(x, y);
		}

		public String getTooltip(int x, int y, Asset asset) {
			if (isColorSpot(x, y))
				return Messages.getString("AbstractLightboxView.color_code"); //$NON-NLS-1$
			if (isDoneMark(x, y))
				return Messages.getString("AbstractLightboxView.raw_recipe_applies"); //$NON-NLS-1$
			if (isExpandCollapse(x, y))
				return Messages.getString("AbstractLightboxView.toggle_state"); //$NON-NLS-1$
			if (isRating(x, y))
				return Messages.getString("AbstractLightboxView.rating"); //$NON-NLS-1$
			if (isRotate270(x, y))
				return Messages.getString("AbstractLightboxView.rotate_left"); //$NON-NLS-1$
			if (isRotate90(x, y))
				return Messages.getString("AbstractLightboxView.rotate_right"); //$NON-NLS-1$
			if (isLocationPin(x, y))
				return !(Double.isNaN(asset.getGPSLatitude()) || Double.isNaN(asset.getGPSLatitude()))
						? Messages.getString("AbstractLightboxView.location_data") //$NON-NLS-1$
						: Messages.getString("AbstractLightboxView.no_location_data"); //$NON-NLS-1$
			if (isVoicenotes(x, y)) {
				String voiceFileURI = asset.getVoiceFileURI();
				return voiceFileURI.startsWith("?") ? voiceFileURI.substring(1) //$NON-NLS-1$
						: Messages.getString("AbstractLightboxView.play_voicenote"); //$NON-NLS-1$
			}
			return null;
		}

		public Rectangle getTitleArea() {
			return titleArea;
		}
	}

	protected Gallery gallery;
	protected AbstractGridGroupRenderer groupRenderer = null;
	protected boolean shift;
	protected LightboxGalleryItemRenderer itemRenderer;
	protected GalleryItem focussedItem;
	protected boolean showRotateButtons;
	protected boolean showColorCode;
	protected boolean showLocation;
	protected int showRating;
	protected boolean showVoicenoteButton;
	protected Image placeHolder;
	private boolean showExpandCollapseButton;
	private int showRegions;
	private boolean showDoneMark;
	protected int showLabelDflt;
	protected String labelTemplateDflt;
	protected int labelFontsizeDflt;
	private GalleryItem currentItem;

	protected static final GalleryItem[] NOITEM = new GalleryItem[0];

	protected abstract void setItemText(final GalleryItem item, Asset asset, Integer cardinality);

	protected void addGalleryPaintListener() {
		placeHolder = new Image(gallery.getDisplay(), getImage(null), SWT.IMAGE_GRAY);
		gallery.addListener(SWT.PaintItem, new GalleryPaintListener(itemRenderer));
	}

	@Override
	protected void fireSizeChanged() {
		if (groupRenderer != null) {
			Object firstElement = ((IStructuredSelection) getSelection()).getFirstElement();
			GalleryItem item = focussedItem;
			if (firstElement instanceof Asset)
				item = getGalleryItem(((Asset) firstElement));
			groupRenderer.setItemSize(thumbsize, thumbsize);
			if (item != null)
				gallery.showItem(item);
		}
	}

	protected abstract GalleryItem getGalleryItem(Asset asset);

	@Override
	public void dispose() {
		if (placeHolder != null)
			placeHolder.dispose();
		if (mouseWheelListener != null)
			mouseWheelListener.cancel();
		super.dispose();
	}

	@Override
	public void setFocussedItem(MouseEvent e) {
		if (e == null)
			focussedItem = null;
		else {
			pnt.x = e.x;
			pnt.y = e.y;
			focussedItem = gallery.getItem(pnt);
		}
	}

	protected void hookDoubleClickAction(Control control) {
		control.addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				pnt.x = event.x;
				pnt.y = event.y;
				GalleryItem item = gallery.getItem(pnt);
				if (item != null) {
					Hotspots hotSpots = (Hotspots) item.getData(HOTSPOTS);
					if (!hotSpots.isHotspot(event.x, event.y)) {
						event.data = item.getData(ASSET);
						viewImageAction.runWithEvent(event);
					}
				}
			}
		});
	}

	protected void addMouseListener() {
		gallery.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				GalleryItem[] items = gallery.getSelection();
				for (GalleryItem item : items) {
					Asset asset = (Asset) item.getData(ASSET);
					if (asset != null) {
						ImageRegion foundRegion = findBestFaceRegion(item, e.x, e.y, false);
						if (foundRegion != null) {
							editRegionName(foundRegion);
							break;
						}
						Hotspots hotSpots = (Hotspots) item.getData(HOTSPOTS);
						if (hotSpots != null) {
							if (hotSpots.isRotate270(e.x, e.y)) {
								rotate(asset, 270);
								item.setData(ROT, R270);
								gallery.redraw(item);
								break;
							}
							if (hotSpots.isRotate90(e.x, e.y)) {
								rotate(asset, 90);
								item.setData(ROT, R90);
								gallery.redraw(item);
								break;
							}
							if (hotSpots.isRating(e.x, e.y)) {
								RatingDialog dialog = new RatingDialog(getSite().getShell(), asset.getRating(), 0.5d,
										true, true);
								dialog.create();
								dialog.getShell().setLocation(gallery.toDisplay(e.x, e.y));
								ZoomActionFactory.rate(Collections.singletonList(asset), AbstractLightboxView.this,
										dialog.open());
								break;
							}
							if (hotSpots.isDoneMark(e.x, e.y)) {
								StatusDialog dialog = new StatusDialog(getSite().getShell(), asset.getStatus());
								dialog.create();
								dialog.getShell().setLocation(gallery.toDisplay(e.x, e.y));
								setStatus(asset, dialog.open());
								break;
							}
							if (hotSpots.isColorSpot(e.x, e.y)) {
								ColorCodeDialog dialog = new ColorCodeDialog(getSite().getShell(),
										asset.getColorCode());
								dialog.create();
								dialog.getShell().setLocation(gallery.toDisplay(e.x, e.y));
								int code = dialog.open();
								if (code >= Constants.COLOR_UNDEFINED)
									colorCode(asset, code);
								break;
							}
							if (hotSpots.isLocationPin(e.x, e.y)) {
								showLocation(asset, (e.stateMask & SWT.SHIFT) == SWT.SHIFT);
								break;
							}
							if (hotSpots.isVoicenotes(e.x, e.y)) {
								String voiceFileURI = asset.getVoiceFileURI();
								if (voiceFileURI != null && !voiceFileURI.startsWith("?")) //$NON-NLS-1$
									UiActivator.getDefault().playVoicenote(asset);
								break;
							}
							if (hotSpots.isExpandCollapse(e.x, e.y)) {
								if (item.getData(CARD) != null)
									expandedSet.add(asset);
								else
									expandedSet.remove(asset);
								redrawCollection(null, null);
								break;
							}
							if (hotSpots.isTitleArea(e.x, e.y)) {
								editTitleArea(item, hotSpots.getTitleArea());
								break;
							}
						}
					}
				}
			}
		});
	}

	protected abstract void editTitleArea(GalleryItem item, Rectangle bounds);

	public boolean cursorOverImage(int mx, int my) {
		return findObject(mx, my) != null;
	}

	public Control getControl() {
		return gallery;
	}

	@Override
	public String getTooltip(int mx, int my) {
		pnt.x = mx;
		pnt.y = my;
		GalleryItem item = gallery.getItem(pnt);
		if (item == null)
			return null;
		Hotspots hotspots = (Hotspots) item.getData(HOTSPOTS);
		return hotspots == null ? null : hotspots.getTooltip(mx, my, (Asset) item.getData(ASSET));
	}

	@Override
	public Object findObject(MouseEvent e) {
		return findObject(e.x, e.y);
	}

	public Object findObject(int x, int y) {
		pnt.x = x;
		pnt.y = y;
		GalleryItem item = gallery.getItem(pnt);
		handleMouseOver(item);
		if (item == null)
			return null;
		Asset asset = (Asset) item.getData(ASSET);
		Integer card = (Integer) item.getData(CARD);
		if (card != null) {
			int c = card.intValue();
			List<Asset> assets = new ArrayList<Asset>(c);
			if (asset != null)
				assets.add(asset);
			Gallery parent = item.getParent();
			int index = parent.indexOf(item);
			int assetIndex = foldingIndex[index];
			IAssetProvider assetProvider = getAssetProvider();
			for (int i = 1; i < c; i++) {
				Asset nextAsset = assetProvider.getAsset(assetIndex + i);
				if (nextAsset != null)
					assets.add(nextAsset);
			}
			return assets;
		}
		return asset;
	}

	private void handleMouseOver(GalleryItem item) {
		if (currentItem != item) {
			GalleryItem oldItem = currentItem;
			currentItem = item;
			if (oldItem != null)
				gallery.redraw(oldItem);
			cancelJobs(DecoJob.ID);
			new DecoJob(gallery, currentItem).schedule(250);
		}
	}

	@Override
	public ImageRegion[] findAllRegions(MouseEvent event) {
		pnt.x = event.x;
		pnt.y = event.y;
		GalleryItem item = gallery.getItem(pnt);
		if (item != null) {
			ImageRegion[] regions = (ImageRegion[]) item.getData(REGIONS);
			if (regions != null)
				return ImageRegion.extractMatchingRegions(regions, event.x, event.y);
		}
		return null;
	}

	@Override
	public ImageRegion findBestFaceRegion(int x, int y, boolean all) {
		pnt.x = x;
		pnt.y = y;
		GalleryItem item = gallery.getItem(pnt);
		if (item != null)
			return findBestFaceRegion(item, x, y, all);
		return null;
	}

	private static ImageRegion findBestFaceRegion(GalleryItem item, int x, int y, boolean all) {
		ImageRegion[] regions = (ImageRegion[]) item.getData(REGIONS);
		if (regions != null) {
			ImageRegion bestRegion = ImageRegion.getBestRegion(ImageRegion.extractMatchingRegions(regions, x, y),
					Region.type_face, true, x, y);
			if (bestRegion == null && all)
				bestRegion = ImageRegion.getBestRegion(regions, Region.type_face, false, x, y);
			return bestRegion;
		}
		return null;
	}

	protected AssetSelection doGetAssetSelection() {
		GalleryItem[] items = gallery.getOrderedSelection();
		AssetSelection sel = new AssetSelection(items.length);
		for (GalleryItem item : items) {
			AssetImpl asset = (AssetImpl) item.getData(ASSET);
			if (asset != null)
				sel.add(asset);
		}
		return sel;
	}

	@Override
	protected int getSelectionCount(boolean local) {
		if (local) {
			int i = 0;
			for (GalleryItem galleryItem : gallery.getSelection()) {
				Asset a = (Asset) galleryItem.getData(ASSET);
				if (a == null || a.getFileState() != IVolumeManager.PEER)
					if (++i >= 2)
						return i;
			}
			return i;
		}
		return gallery.getSelectionCount();
	}

	public AssetSelection getAssetSelection() {
		return selection instanceof AssetSelection ? (AssetSelection) selection : doGetAssetSelection();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */

	@Override
	public void setFocus() {
		gallery.setFocus();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		// do nothing
	}

	public void widgetSelected(SelectionEvent e) {
		if (refreshing <= 0) {
			stopAudio();
			selection = doGetAssetSelection();
			fireSelection();
		}
	}

	protected void setPreferences() {
		IPreferenceStore preferenceStore = applyPreferences();
		preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				if (PreferenceConstants.SHOWLABEL.equals(property)
						|| PreferenceConstants.THUMBNAILTEMPLATE.equals(property)
						|| PreferenceConstants.LABELFONTSIZE.equals(property)
						|| PreferenceConstants.SHOWROTATEBUTTONS.equals(property)
						|| PreferenceConstants.SHOWCOLORCODE.equals(property)
						|| PreferenceConstants.SHOWRATING.equals(property)
						|| PreferenceConstants.SHOWLOCATION.equals(property)
						|| PreferenceConstants.SHOWDONEMARK.equals(property)
						|| PreferenceConstants.SHOWVOICENOTE.equals(property)
						|| PreferenceConstants.SHOWEXPANDCOLLAPSE.equals(property)
						|| PreferenceConstants.MAXREGIONS.equals(property)) {
					applyPreferences();
					refresh();
				}
			}
		});
	}

	protected IPreferenceStore applyPreferences() {
		final IPreferenceStore preferenceStore = UiActivator.getDefault().getPreferenceStore();
		showLabelDflt = preferenceStore.getInt(PreferenceConstants.SHOWLABEL);
		labelTemplateDflt = preferenceStore.getString(PreferenceConstants.THUMBNAILTEMPLATE);
		labelFontsizeDflt = preferenceStore.getInt(PreferenceConstants.LABELFONTSIZE);
		showRotateButtons = preferenceStore.getBoolean(PreferenceConstants.SHOWROTATEBUTTONS);
		showColorCode = !PreferenceConstants.COLORCODE_NO
				.equals(preferenceStore.getString(PreferenceConstants.SHOWCOLORCODE));
		showLocation = preferenceStore.getBoolean(PreferenceConstants.SHOWLOCATION);
		String rating = preferenceStore.getString(PreferenceConstants.SHOWRATING);
		showRating = PreferenceConstants.SHOWRATING_NO.equals(rating) ? RATING_NO
				: PreferenceConstants.SHOWRATING_COUNT.equals(rating) ? RATING_COUNT : RATING_SIZE;
		showDoneMark = preferenceStore.getBoolean(PreferenceConstants.SHOWDONEMARK);
		showVoicenoteButton = preferenceStore.getBoolean(PreferenceConstants.SHOWVOICENOTE);
		showExpandCollapseButton = preferenceStore.getBoolean(PreferenceConstants.SHOWEXPANDCOLLAPSE);
		showRegions = preferenceStore.getInt(PreferenceConstants.MAXREGIONS);
		return preferenceStore;
	}

	@Override
	public void themeChanged() {
		applyStyle(gallery);
		GalleryItem group = gallery.getItem(0);
		if (group != null)
			for (GalleryItem item : group.getItems())
				if (item != null)
					gallery.redraw(item);
	}

	protected void applyStyle(Gallery gal) {
		CssActivator.getDefault().applyExtendedStyle(gal, this);
	}

	public void setOfflineColor(Color c) {
		itemRenderer.setOfflineColor(c);
	}

	public void setRemoteColor(Color c) {
		itemRenderer.setRemoteColor(c);
	}

	public void setTitleColor(Color c) {
		// not used
	}

	public boolean applyColorsTo(Object element) {
		return element instanceof Gallery;
	}

	public void setForegroundColor(Color foregroundColor) {
		itemRenderer.setForegroundColor(foregroundColor);
	}

	public void setSelectionForegroundColor(Color selectionForegroundColor) {
		itemRenderer.setSelectionForegroundColor(selectionForegroundColor);
	}

	public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
		itemRenderer.setSelectionBackgroundColor(selectionBackgroundColor);
	}

	public void setBackgroundColor(Color backgroundColor) {
		itemRenderer.setBackgroundColor(backgroundColor);
	}

	public void setSelectedOfflineColor(Color selectedOfflineColor) {
		itemRenderer.setSelectedOfflineColor(selectedOfflineColor);
	}

	public void setSelectedRemoteColor(Color selectedRemoteColor) {
		itemRenderer.setSelectedRemoteColor(selectedRemoteColor);
	}

	public void setTitleForeground(Color color) {
		if (groupRenderer instanceof DefaultGalleryGroupRenderer)
			((DefaultGalleryGroupRenderer) groupRenderer).setTitleForeground(color);
	}

	public void setTitleBackground(Color color) {
		if (groupRenderer instanceof DefaultGalleryGroupRenderer)
			((DefaultGalleryGroupRenderer) groupRenderer).setTitleBackground(color);
	}

	protected void installInfrastructure(boolean mouseWheel, int delay) {
		// Mouse, Keys
		addMouseListener();
		if (mouseWheel)
			addMouseWheelListener(gallery);
		addKeyListener();
		addGestureListener(gallery);
		addExplanationListener(false);
		// Drop-Unterstützung
		addDragDropSupport();
		// Hover
		installHoveringController();
		addCueListener();
		// Contributions
		hookContextMenu();
		hookDoubleClickAction(gallery);
		contributeToActionBars();
		setDecorator(gallery, new GalleryDecorateJob(this, gallery));
		updateActions(false);
	}

}