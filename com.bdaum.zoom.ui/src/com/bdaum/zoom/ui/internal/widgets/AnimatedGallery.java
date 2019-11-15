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
package com.bdaum.zoom.ui.internal.widgets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.piccolo2d.PCamera;
import org.piccolo2d.PNode;
import org.piccolo2d.activities.PActivity;
import org.piccolo2d.activities.PTransformActivity;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.extras.swt.PSWTCanvas;
import org.piccolo2d.extras.swt.PSWTImage;
import org.piccolo2d.extras.swt.PSWTPath;
import org.piccolo2d.extras.swt.PSWTText;
import org.piccolo2d.util.PAffineTransform;
import org.piccolo2d.util.PBounds;
import org.piccolo2d.util.PPaintContext;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.Region;
import com.bdaum.zoom.cat.model.asset.RegionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollection;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.locationShown.LocationShownImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.IPostProcessor2;
import com.bdaum.zoom.core.IScoreFormatter;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.core.internal.db.AssetEnsemble;
import com.bdaum.zoom.css.internal.CssActivator;
import com.bdaum.zoom.css.internal.IExtendedColorModel2;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageStore;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.internal.CaptionProcessor;
import com.bdaum.zoom.ui.internal.CaptionProcessor.CaptionConfiguration;
import com.bdaum.zoom.ui.internal.IPresentationHandler;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.ColorCodeDialog;
import com.bdaum.zoom.ui.internal.dialogs.RatingDialog;
import com.bdaum.zoom.ui.internal.dialogs.StatusDialog;
import com.bdaum.zoom.ui.internal.views.AssetContainer;
import com.bdaum.zoom.ui.internal.views.ImageRegion;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

@SuppressWarnings("restriction")
public class AnimatedGallery implements IExtendedColorModel2, IPresentationHandler {

	private final class GalleryPBasicInputEventHandler extends PBasicInputEventHandler {
		private final PSWTCanvas pcanvas;
		private int eat;
		private boolean keyDown;
		private boolean textEvent = false;

		private GalleryPBasicInputEventHandler(PSWTCanvas canvas) {
			this.pcanvas = canvas;
		}

		@Override
		public void keyPressed(PInputEvent event) {
			textEvent = textEventHandler == null ? false : textEventHandler.hasFocus();
			if (textEvent)
				textEventHandler.keyPressed(event);
			else {
				if (keyDown)
					releaseKey(pcanvas, event);
				keyDown = true;
			}
		}

		@Override
		public void keyReleased(PInputEvent event) {
			if (!textEvent) {
				releaseKey(pcanvas, event);
				keyDown = false;
			}
			textEvent = false;
		}

		private void releaseKey(final PSWTCanvas pswtCanvas, PInputEvent event) {
			int f = event.isControlDown() ? 5 : 1;
			switch (event.getKeyCode()) {
			case KeyEvent.VK_SPACE:
				if (slides.length > 0) {
					int min;
					if (event.isShiftDown()) {
						min = 0;
						for (Asset asset : selectedSlides) {
							Integer index = galleryMap.get(asset);
							if (index != null)
								min = Math.max(min, index);
						}
						--min;
						if (min < 0)
							min = slides.length - 1;
					} else {
						min = Integer.MAX_VALUE;
						for (Asset asset : selectedSlides) {
							Integer index = galleryMap.get(asset);
							if (index != null)
								min = Math.min(min, index);
						}
						++min;
						if (min >= slides.length)
							min = 0;
					}
					PGalleryItem slide = slides[min];
					if (slide != null) {
						oldAssetSelection = null;
						goToSlide(slide);
						lastSingleClick = slide;
					}
				}
				break;
			case SWT.ARROW_RIGHT:
				if (event.isShiftDown())
					nav(1, 0);
				else
					pan(pswtCanvas, -30 * f, 0);
				break;
			case SWT.ARROW_LEFT:
				if (event.isShiftDown())
					nav(-1, 0);
				else
					pan(pswtCanvas, 30 * f, 0);
				break;
			case SWT.ARROW_UP:
				if (event.isShiftDown())
					nav(0, -1);
				else
					pan(pswtCanvas, 0, 30 * f);
				break;
			case SWT.ARROW_DOWN:
				if (event.isShiftDown())
					nav(0, 1);
				else
					pan(pswtCanvas, 0, -30 * f);
				break;
			case SWT.END:
				pan(pswtCanvas, (int) (-pswtCanvas.getCamera().getViewBounds().getWidth() / 2) * f, 0);
				break;
			case SWT.HOME:
				pan(pswtCanvas, (int) (pswtCanvas.getCamera().getViewBounds().getWidth() / 2) * f, 0);
				break;
			case SWT.PAGE_UP:
				pan(pswtCanvas, 0, (int) (pswtCanvas.getCamera().getViewBounds().getHeight() / 2) * f);
				break;
			case SWT.PAGE_DOWN:
				pan(pswtCanvas, 0, (int) (-pswtCanvas.getCamera().getViewBounds().getHeight() / 2) * f);
				break;
			default:
				char c = event.getKeyChar();
				switch (c) {
				case 13:
				case SWT.TAB:
					fireKeyEvent(c, event.getKeyCode(),
							(event.isShiftDown() ? SWT.SHIFT : 0) | (event.isControlDown() ? SWT.CTRL : 0));
					break;
				case '+':
					zoom(pswtCanvas, 1.05d);
					break;
				case '-':
					zoom(pswtCanvas, 0.95d);
					break;
				case '*':
					event.getCamera().setViewTransform(new AffineTransform());
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case SWT.DEL:
					fireKeyEvent(c, event.getKeyCode(), 0);
					break;
				}
				break;
			}
		}

		@Override
		public void mouseDragged(PInputEvent event) {
			textEventHandler.mouseDragged(event);
		}

		@Override
		public void mouseReleased(PInputEvent event) {
			if (textEventHandler != null)
				textEventHandler.mouseReleased(event);
			if (eat > 0) {
				eat--;
				return;
			}
			int stateMask = event.isControlDown() ? SWT.CTRL : 0;
			if (event.isAltDown())
				stateMask |= SWT.ALT;
			if (event.isShiftDown())
				stateMask |= SWT.SHIFT;
			PNode picked = event.getPickedNode();
			if (picked.getParent() instanceof PGalleryItem) {
				PGalleryItem currentSlide = (PGalleryItem) picked.getParent();
				if (isSelected(currentSlide.getAsset())) {
					if (currentSlide.isRatingStar(picked)) {
						Asset asset = currentSlide.getAsset();
						if (asset != null) {
							RatingDialog dialog = new RatingDialog(pcanvas.getShell(), asset.getRating(), 0.6d, true,
									true);
							initContextDialog(dialog, pcanvas, event);
							event.setHandled(true);
							eat++;
							int rating = dialog.open();
							if (rating != RatingDialog.ABORT)
								fireOperationEvent(picked, stateMask, RATE, rating);
						}
					} else if (currentSlide.isColorCodeRectangle(picked)) {
						Asset asset = currentSlide.getAsset();
						if (asset != null) {
							ColorCodeDialog dialog = new ColorCodeDialog(pcanvas.getShell(), asset.getColorCode());
							initContextDialog(dialog, pcanvas, event);
							event.setHandled(true);
							eat++;
							int code = dialog.open();
							if (code >= Constants.COLOR_UNDEFINED)
								fireOperationEvent(picked, stateMask, CODE, code);
						}
					} else if (currentSlide.isLocationPin(picked)) {
						event.setHandled(true);
						fireOperationEvent(picked, stateMask, SHOWLOCATION, 0);
					} else if (currentSlide.isDoneMark(picked)) {
						Asset asset = currentSlide.getAsset();
						if (asset != null) {
							StatusDialog dialog = new StatusDialog(pcanvas.getShell(), asset.getStatus());
							initContextDialog(dialog, pcanvas, event);
							event.setHandled(true);
							eat++;
							int status = dialog.open();
							if (status > Constants.STATE_UNDEFINED)
								fireOperationEvent(picked, stateMask, STATUS, status);
						}
					} else if (currentSlide.isRotate90(picked)) {
						event.setHandled(true);
						fireOperationEvent(picked, stateMask, ROTATE, 90);
					} else if (currentSlide.isRotate270(picked)) {
						event.setHandled(true);
						fireOperationEvent(picked, stateMask, ROTATE, 270);
					} else if (currentSlide.isVoicenote(picked)) {
						event.setHandled(true);
						fireOperationEvent(picked, stateMask, VOICENOTE, 0);
					}
				}
			} else if (picked.getParent() == slideBar && picked instanceof PGalleryItem && !event.isHandled()
					&& event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() == 1 && !event.isAltDown()) {
				picked.raiseToTop();
				oldAssetSelection = null;
				PGalleryItem item = (PGalleryItem) picked;
				if (event.isShiftDown()) {
					if (lastSingleClick != null) {
						if (getOrder(item, lastSingleClick))
							select(item, lastSingleClick);
						else
							select(lastSingleClick, item);
					}
				} else if (item.isSelected()) {
					if (event.isControlDown())
						deselect(item, 1000);
					else {
						Point2D position = event.getCanvasPosition();
						ImageRegion imageRegion = getBestFaceRegion((int) position.getX(), (int) position.getY(), false,
								item.slide);
						if (imageRegion != null)
							fireRegionEvent(stateMask, imageRegion);
						else
							goToSlide(null);
						lastSingleClick = null;
					}
				} else {
					if (event.isControlDown())
						select(item, 1000);
					else {
						selectedSlides.remove(item.getAsset());
						goToSlide(item);
					}
					lastSingleClick = item;
				}
				fireSelectionEvent(1, picked, stateMask);
			} else if (event.getClickCount() == 2) {
				if (picked.getParent() == slideBar)
					fireSelectionEvent(2, picked, stateMask);
				else
					resetPanAndZoom(event.getCamera());
			}
		}

		private void initContextDialog(Dialog dialog, final PSWTCanvas pswtCanvas, PInputEvent event) {
			dialog.create();
			PCamera camera = pswtCanvas.getLayer().getCamera(0);
			PAffineTransform transform = camera.getViewTransform();
			Point2D p2 = transform.transform(event.getPosition(), null);
			dialog.getShell().setLocation(pswtCanvas.toDisplay((int) p2.getX(), (int) p2.getY()));
		}
	}

	@SuppressWarnings("serial")
	public static class PGalleryRegion extends PSWTPath {

		private static final Ellipse2D.Float TEMP_OVAL = new Ellipse2D.Float();
		private static final Rectangle2D.Float TEMP_RECTANGLE = new Rectangle2D.Float();
		private ImageRegion imageRegion;
		private boolean tempPaintChange;

		private PGalleryRegion(Shape shape) {
			super(shape);
		}

		public static PGalleryRegion createRegion(int x, int y, int width, int height, java.awt.Paint regionColor) {
			PGalleryRegion region;
			if (height < 0) {
				TEMP_OVAL.setFrame(x - width / 2, y - width / 2, width, width);
				region = new PGalleryRegion(TEMP_OVAL);
			} else {
				TEMP_RECTANGLE.setFrame(x, y, width, height);
				region = new PGalleryRegion(TEMP_RECTANGLE);
			}
			region.setStrokeColor(regionColor);
			region.setPaint(null);
			region.setPickable(false);
			region.setVisible(true);
			return region;
		}

		public void setImageRegion(ImageRegion imageRegion) {
			this.imageRegion = imageRegion;
		}

		public ImageRegion getImageRegion() {
			return imageRegion;
		}

		/**
		 * Force intersection test to return true inspite of paint being null
		 * 
		 * @param aBounds
		 *            bounds being tested for intersection
		 * @return true if path visibly crosses bounds
		 */
		public boolean intersects(final Rectangle2D aBounds) {
			tempPaintChange = true;
			try {
				setPaint(Color.gray);
				return super.intersects(aBounds);
			} finally {
				setPaint(null);
				tempPaintChange = false;
			}
		}

		/*
		 * Don't invalidate for paint changes from intersects()
		 */
		@Override
		public void invalidatePaint() {
			if (!tempPaintChange)
				super.invalidatePaint();
		}

		/*
		 * Don't report for paint changes from intersects()
		 */
		@Override
		protected void firePropertyChange(int propertyCode, String propertyName, Object oldValue, Object newValue) {
			if (!tempPaintChange)
				super.firePropertyChange(propertyCode, propertyName, oldValue, newValue);
		}

		public double getDistanceFromRegionCenter(int x, int y) {
			Point2D pntSrc = new Point(x, y);
			getParent().globalToLocal(pntSrc);
			PBounds bounds = getBounds();
			double dx = bounds.x + bounds.width / 2 - pntSrc.getX();
			double dy = bounds.y + bounds.height / 2 - pntSrc.getY();
			return dx * dx + dy * dy;
		}
	}

	@SuppressWarnings("serial")
	public class PGalleryItem extends PNode implements AssetContainer, PTextHandler, TraverseHandler {

		private static final int LOWER_THUMBNAIL_HMARGINS = 5;
		private static final int UPPER_THUMBNAIL_HMARGINS = 8;

		private PSWTImage shadow;
		private PSWTAssetThumbnail slide;
		private List<PSWTText> frameLabels;
		private PSWTImage colorCodeRectangle;
		private PSWTImage ratingStar;
		private PAffineTransform selectTransform;
		private PAffineTransform deselectTransform;
		private Asset asset;
		private PSWTImage rotate90Arrow;
		private PSWTImage rotate270Arrow;
		private TextField caption;
		private PSWTImage speakerNode;
		private PSWTImage doneMark;
		private final int index;
		private PAffineTransform multiTransform;
		private String captionText;
		private ZPSWTImage mediaIcon;
		private ZPSWTImage locationPin;
		private int defaultFontSize = JFaceResources.getDefaultFont().getFontData()[0].getHeight();

		public PGalleryItem(PSWTCanvas canvas, int index, final Asset asset) {
			this.index = index;
			this.asset = asset;
			captionText = computeCaption(asset);
			if (shadowImage == null)
				shadowImage = Icons.shadow.getImage();
			shadow = new ZPSWTImage(canvas, shadowImage);
			slide = new PSWTAssetThumbnail(canvas, imageSource, asset);
			PBounds bounds = slide.getBoundsReference();
			slide.setPickable(false);
			double x = bounds.getX();
			double y = bounds.getY();
			double width = bounds.getWidth();
			double height = bounds.getHeight();
			shadow.setBounds(x + 15, y + 15, width, height);
			setBounds(x, y, width + 5, height + 5);
			shadow.setPickable(false);
			addChild(shadow);
			addChild(slide);
			// overlay region frames
			if (asset != null) {
				if (showRegions > 0) {
					String[] regionIds = asset.getPerson();
					if (regionIds != null && regionIds.length > 0) {
						String fontFamily = "Arial"; //$NON-NLS-1$
						int fontsize = 11;
						frameLabels = new ArrayList<PSWTText>(2 * regionIds.length);
						IDbManager dbManager = Core.getCore().getDbManager();
						List<RegionImpl> regionList = dbManager.obtainObjects(RegionImpl.class, "asset_person_parent", //$NON-NLS-1$
								asset.getStringId(), QueryField.EQUALS);
						int i = 0;
						int rotation = asset.getRotation();
						rectangleList.clear();
						for (RegionImpl region : regionList) {
							if (i++ > showRegions)
								break;
							String regionid = region.getStringId();
							Rectangle frame = UiUtilities.computeFrame(regionid, 0, 0, (int) width, (int) height,
									rotation);
							if (frame != null && !UiUtilities.isDoubledRegion(rectangleList, frame)) {
								rectangleList.add(frame);
								String type = region.getType();
								PGalleryRegion frameFill = PGalleryRegion.createRegion(frame.x, frame.y, frame.width,
										frame.height, regionColor);
								slide.addChild(frameFill);
								Color color = Color.RED;
								String name = "?"; //$NON-NLS-1$
								if (type == null || type.isEmpty() || Region.type_face.equals(type)
										|| Region.type_pet.equals(type)) {
									String albumId = region.getAlbum();
									SmartCollectionImpl album = albumId == null ? null
											: dbManager.obtainById(SmartCollectionImpl.class, albumId);
									if (album != null)
										name = album.getName();
									if (personId == null || personId.equals(albumId))
										color = Color.YELLOW;
								} else if (Region.type_barCode.equals(type)) {
									name = region.getAlbum();
									color = Color.GREEN;
								}
								createFrameLabel(frameFill, frame.x + 5, frame.y + 3, fontFamily, fontsize,
										Color.DARK_GRAY, name);
								createFrameLabel(frameFill, frame.x + 4, frame.y + 2, fontFamily, fontsize, color,
										name);
								frameFill.setImageRegion(new ImageRegion(frame, regionid,
										type == null ? Region.type_face : type, name, asset));
							}
						}
					}
				}
			}
			// Decoration
			addMediaIcon(canvas, asset, x, y, width);
			addCaption(canvas, asset, captionText, x, y, width, height, captionConfiguration.labelAlignment);
			addRatingStar(canvas, asset, x, y, width);
			addDoneMark(canvas, asset, x, y, width);
			double x1 = addColorCode(canvas, asset);
			addLocationPin(canvas, asset, x1, y);
			addRotateButtons(canvas, asset, width, height);
			addVoiceNote(canvas, asset, width, height);
			textEventHandler = new TextEventHandler(UiUtilities.getAwtBackground(canvas, selectionBackgroundColor),
					this);
		}

		private void createFrameLabel(PSWTPath path, double x, double y, String fontFamily, int fontsize,
				Color penColor, String text) {
			PSWTText label = new PSWTText(text, new Font(fontFamily, Font.PLAIN, Math.max(fontsize - 2, 4)));
			label.setPenColor(penColor);
			label.setOffset(x, y);
			label.setPickable(false);
			label.setGreekThreshold(2);
			label.setTransparent(true);
			label.setVisible(false);
			path.addChild(label);
			frameLabels.add(label);
		}

		private void addCaption(PSWTCanvas pswtCanvas, final Asset anAsset, String text, double x, double y,
				double width, double height, int alignment) {
			boolean readOnly = anAsset.getFileState() != IVolumeManager.ONLINE
					|| captionConfiguration != null && captionConfiguration.showLabel != Constants.TITLE_LABEL;
			caption = new TextField(text, (int) width, UiUtilities.getAwtFont(pswtCanvas, null, java.awt.Font.BOLD,
					captionConfiguration.showLabel == Constants.CUSTOM_LABEL ? captionConfiguration.labelFontsize
							: defaultFontSize),
					UiUtilities.getAwtForeground(pswtCanvas, null), null, true,
					(alignment == 0 ? SWT.LEFT : alignment == 2 ? SWT.RIGHT : SWT.CENTER) | SWT.SINGLE
							| (readOnly ? SWT.READ_ONLY : SWT.NONE));
			caption.setSelectedBgColor(UiUtilities.getAwtBackground(pswtCanvas, selectionBackgroundColor));
			caption.setGreekThreshold(6);
			caption.setOffset(x, y + height);
			caption.setPickable(false);
			caption.setVisible(false);
			if (!readOnly) {
				caption.setValidator(new IInputValidator() {
					public String isValid(String newText) {
						return QueryField.NAME.isValid(newText, anAsset);
					}
				});
				caption.addErrorListener(titleVerifyListener);
			}
			addChild(caption);
		}

		private void addDoneMark(PSWTCanvas pswtCanvas, Asset anAsset, double x, double y, double width) {
			if (showDoneMark && anAsset != null) {
				int status = anAsset.getStatus();
				String mimeType = anAsset.getMimeType();
				if (status == Constants.STATE_READY
						|| status == Constants.STATE_CORRECTED && ImageConstants.IMAGE_X_RAW.equals(mimeType)) {
					Image icon = Icons.done.getImage();
					addChild(doneMark = new ZPSWTImage(pswtCanvas, icon));
					doneMark.setOffset(width + x, y - icon.getBounds().height);
					doneMark.setName(Messages.AnimatedGallery_raw_recipe_applies);
				}
			}
		}

		private void addVoiceNote(PSWTCanvas pswtCanvas, Asset anAsset, double width, double height) {
			if (showVoicenoteButton && anAsset != null) {
				Image speakerIcon = UiUtilities.getAnnotationIcon(anAsset);
				if (speakerIcon != null) {
					Rectangle ibounds = speakerIcon.getBounds();
					addChild(speakerNode = new ZPSWTImage(pswtCanvas, speakerIcon));
					speakerNode.setOffset((width - ibounds.width) / 2, height - ibounds.height);
					speakerNode.setName(AssetEnsemble.hasVoiceNote(anAsset) ? Messages.AnimatedGallery_play_voicenote
							: AssetEnsemble.getNoteText(anAsset));
				}
			}
		}

		private void addRotateButtons(PSWTCanvas pswtCanvas, Asset anAsset, double width, double height) {
			if (showRotateButtons && anAsset != null) {
				Image rotate90icon = Icons.rotate90.getImage();
				Rectangle bounds5 = rotate90icon.getBounds();
				addChild(rotate90Arrow = new ZPSWTImage(pswtCanvas, rotate90icon));
				rotate90Arrow.setOffset(LOWER_THUMBNAIL_HMARGINS, height - bounds5.height - LOWER_THUMBNAIL_HMARGINS);
				rotate90Arrow.setName(Messages.AnimatedGallery_rotate_right);
				Image rotate270icon = Icons.rotate270.getImage();
				Rectangle bounds6 = rotate270icon.getBounds();
				addChild(rotate270Arrow = new ZPSWTImage(pswtCanvas, rotate270icon));
				rotate270Arrow.setOffset(width - bounds6.width - LOWER_THUMBNAIL_HMARGINS,
						height - bounds6.height - LOWER_THUMBNAIL_HMARGINS);
				rotate270Arrow.setName(Messages.AnimatedGallery_rotate_left);
			}
		}

		private double addColorCode(PSWTCanvas pswtCanvas, Asset anAsset) {
			if (showColorCode && anAsset != null) {
				int colorCode = anAsset.getColorCode();
				if (colorCode == Constants.COLOR_UNDEFINED) {
					IPostProcessor2[] processors = Core.getCore().getDbFactory().getAutoColoringProcessors();
					if (processors != null)
						for (int i = 0; i < processors.length; i++)
							if (processors[i] != null && processors[i].accept(anAsset)) {
								colorCode = i;
								break;
							}
				}
				addChild(colorCodeRectangle = new ZPSWTImage(pswtCanvas, Icons.toSwtColors(colorCode)));
				colorCodeRectangle.setOffset(UPPER_THUMBNAIL_HMARGINS, UPPER_THUMBNAIL_HMARGINS);
				colorCodeRectangle.setName(Messages.AnimatedGallery_color_code);
				PBounds bounds = colorCodeRectangle.getBoundsReference();
				return bounds.width + bounds.x;
			}
			return 0d;
		}

		private void addLocationPin(PSWTCanvas pswtCanvas, Asset anAsset, double x, double y) {
			if (showLocation && anAsset != null) {
				boolean gps = !(Double.isNaN(asset.getGPSLatitude()) || Double.isNaN(asset.getGPSLatitude()));
				if (!gps)
					gps = !Core.getCore().getDbManager()
							.obtainStructForAsset(LocationShownImpl.class, asset.getStringId(), false).isEmpty();
				addChild(locationPin = new ZPSWTImage(pswtCanvas,
						gps ? Icons.location.getImage() : Icons.nolocation.getImage()));
				locationPin.setOffset(x + UPPER_THUMBNAIL_HMARGINS, y + UPPER_THUMBNAIL_HMARGINS);
				locationPin.setName(
						gps ? Messages.AnimatedGallery_location_present : Messages.AnimatedGallery_no_location);
			}
		}

		private void addRatingStar(PSWTCanvas pswtCanvas, Asset anAsset, double x, double y, double width) {
			if (RATING_NO != showRating && anAsset != null) {
				int rating = anAsset.getRating();
				double rfactor;
				Image icon;
				if (rating > RatingDialog.UNDEF) {
					if (RATING_SIZE == showRating) {
						if (rating > 0) {
							rfactor = (rating + 0.6d) / 5.6d;
							icon = Icons.rating61.getImage();
						} else {
							rfactor = 1.6d / 5.6d;
							icon = Icons.rating60.getImage();
						}
					} else {
						switch (rating) {
						case 0:
							icon = Icons.rating60.getImage();
							break;
						case 1:
							icon = Icons.rating61.getImage();
							break;
						case 2:
							icon = Icons.rating62.getImage();
							break;
						case 3:
							icon = Icons.rating63.getImage();
							break;
						case 4:
							icon = Icons.rating64.getImage();
							break;
						default:
							icon = Icons.rating65.getImage();
							break;
						}
						rfactor = 2.6d / 5.6d;
					}
				} else {
					rfactor = 3.6d / 5.6d;
					icon = Icons.rating6u.getImage();
				}
				addChild(ratingStar = new ZPSWTImage(pswtCanvas, icon));
				ratingStar.scale(rfactor);
				ratingStar.setOffset(width + x - (icon.getBounds().width * rfactor) - UPPER_THUMBNAIL_HMARGINS, y);
				ratingStar.setName(Messages.AnimatedGallery_rating);
			}
		}

		private void addMediaIcon(PSWTCanvas pswtCanvas, Asset anAsset, double x, double y, double width) {
			IMediaSupport mediaSupport = CoreActivator.getDefault().getMediaSupport(anAsset.getFormat());
			if (mediaSupport != null) {
				Image icon = mediaSupport.getIcon40();
				if (icon != null) {
					double rfactor = 2.6d / 5.6d;
					Rectangle bounds4 = icon.getBounds();
					double h = bounds4.height * rfactor;
					addChild(mediaIcon = new ZPSWTImage(pswtCanvas, icon));
					mediaIcon.setPickable(false);
					mediaIcon.scale(rfactor);
					mediaIcon.setOffset(width + x - bounds4.width * rfactor - UPPER_THUMBNAIL_HMARGINS, y + 3 * h / 2);
					mediaIcon.setName(mediaSupport.getName());
				}
			}
		}

		private String computeCaption(Asset anAsset) {
			return (anAsset != null && captionConfiguration.showLabel != Constants.NO_LABEL)
					? captionProcessor.computeImageCaption(anAsset, scoreFormatter, null, null,
							captionConfiguration.getLabelTemplate(), false)
					: ""; //$NON-NLS-1$
		}

		public void setSelectTransform() {
			selectTransform = getTransform();
			showDecoration(true);
		}

		public void showDecoration(boolean isSelected) {
			if (colorCodeRectangle != null)
				colorCodeRectangle
						.setVisible(isSelected || asset != null && asset.getColorCode() != Constants.COLOR_UNDEFINED);
			if (locationPin != null)
				locationPin.setVisible(
						isSelected || !(Double.isNaN(asset.getGPSLatitude()) || Double.isNaN(asset.getGPSLatitude())));
			if (ratingStar != null)
				ratingStar.setVisible(isSelected || asset != null && asset.getRating() > 0);
			if (rotate90Arrow != null)
				rotate90Arrow.setVisible(isSelected);
			if (rotate270Arrow != null)
				rotate270Arrow.setVisible(isSelected);
			shadow.setVisible(isSelected);
			if (frameLabels != null)
				for (PSWTText label : frameLabels)
					label.setVisible(isSelected);
			caption.setVisible(isSelected);
		}

		public void setMultiTransform() {
			multiTransform = getTransform();
			showDecoration(true);
		}

		public void setDeselectTransform() {
			deselectTransform = getTransform();
			showDecoration(false);
		}

		public void deselect(int msec, boolean hideCover) {
			PTransformActivity animateToTransform = animateToTransform(deselectTransform, msec);
			if (hideCover)
				animateToTransform.setDelegate(new PActivity.PActivityDelegate() {
					public void activityStepped(PActivity activity) {
						// do nothing
					}

					public void activityStarted(PActivity activity) {
						// do nothing
					}

					public void activityFinished(PActivity activity) {
						try {
							slideBar.removeChild(cover);
						} catch (Exception e) {
							// do nothing
						}
					}
				});
			showDecoration(false);
		}

		public void select(int msec, boolean cutTheTallPoppies) {
			PTransformActivity animateToTransform;
			switch (selectedSlides.size()) {
			case 0:
			case 1:
				animateToTransform = animateToTransform(selectTransform, msec);
				break;
			case 2:
				animateToTransform = animateToTransform(multiTransform, msec);
				if (cutTheTallPoppies)
					for (Asset anAsset : selectedSlides) {
						if (anAsset != this.asset) {
							Integer ix = galleryMap.get(anAsset);
							if (ix != null)
								slides[ix].select(msec, false);
						}
					}
				break;
			default:
				animateToTransform = animateToTransform(multiTransform, msec);
				break;
			}
			animateToTransform.setDelegate(new PActivity.PActivityDelegate() {

				public void activityStepped(PActivity activity) {
					// do nothing
				}

				public void activityStarted(PActivity activity) {
					// do nothing
				}

				public void activityFinished(PActivity activity) {
					switch (asset.getFileState()) {
					case IVolumeManager.OFFLINE:
						if (offlineColor != null)
							caption.setPenColor(offlineColor);
						break;
					case IVolumeManager.REMOTE:
					case IVolumeManager.PEER:
						if (remoteColor != null)
							caption.setPenColor(remoteColor);
						break;
					default:
						caption.setPenColor(UiUtilities.getAwtForeground(canvas, null));
						break;
					}
					caption.setVisible(true);
					showDecoration(true);
				}
			});
		}

		public Asset getAsset() {
			return asset;
		}

		public boolean isSelected() {
			return selectedSlides.contains(asset);
		}

		public boolean isRatingStar(PNode picked) {
			return picked == ratingStar;
		}

		public boolean isDoneMark(PNode picked) {
			return picked == doneMark;
		}

		public boolean isColorCodeRectangle(PNode picked) {
			return picked == colorCodeRectangle;
		}

		public boolean isLocationPin(PNode picked) {
			return picked == locationPin;
		}

		public boolean isRotate90(PNode picked) {
			return picked == rotate90Arrow;
		}

		public boolean isRotate270(PNode picked) {
			return picked == rotate270Arrow;
		}

		public boolean isVoicenote(PNode picked) {
			return picked == speakerNode;
		}

		public int getIndex() {
			return index;
		}

		public void update(Asset anAsset) {
			removeAllChildren();
			slide = new PSWTAssetThumbnail(canvas, imageSource, anAsset);
			PBounds bounds = slide.getBoundsReference();
			double x = bounds.getX();
			double y = bounds.getY();
			double width = bounds.getWidth();
			double height = bounds.getHeight();
			shadow.setBounds(x + 15, y + 15, width, height);
			setBounds(x, y, width + 5, height + 5);
			addChild(shadow);
			addChild(slide);
			slide.setPickable(false);
			caption.setText(computeCaption(anAsset));
			addChild(caption);
			addRatingStar(canvas, anAsset, x, y, width);
			addDoneMark(canvas, anAsset, x, y, width);
			addColorCode(canvas, anAsset);
			addRotateButtons(canvas, anAsset, width, height);
			addVoiceNote(canvas, anAsset, width, height);
		}

		public void processTextEvent(TextField focus) {
			String text = focus.getText();
			int p = captionText.lastIndexOf('.');
			if (p >= 0 && text.lastIndexOf('.') < 0)
				text += captionText.substring(p);
			if (focus.isValid() && !captionText.equals(text))
				fireModifyEvent(asset, text);
		}

		public boolean traverse(char keyCharacter, int stateMask) {
			if (keyCharacter == SWT.TAB) {
				nav((stateMask & SWT.SHIFT) != 0 ? -1 : 1, 0);
				getControl().getDisplay().asyncExec(() -> {
					if (!getControl().isDisposed())
						focusOnTitle();
				});
				return true;
			}
			return false;
		}

		public void setFocusTo(TextField field, boolean focussed) {
			field.setFocus(focussed);
			textEventHandler.setFocus(field);
		}

	}

	public static final int RATE = 1;

	public static final int CODE = 2;

	public static final int ROTATE = 3;

	public static final int VOICENOTE = 4;

	public static final int STATUS = 5;

	public static final int SHOWLOCATION = 6;

	public static final int REGION = 7;

	private static final PGalleryItem[] NOSLIDES = new PGalleryItem[0];

	private static final int RATING_NO = 0;
	private static final int RATING_SIZE = 1;
	private static final int RATING_COUNT = 2;

	private static final double PAN_SENSITIVITY = 0.2d;
	private static final double VACCEL = 5d;
	private static final double HACCEL = 2.5d;

	private Color offlineColor;
	private Color remoteColor;

	PSWTPath slideBar;
	protected PGalleryItem[] slides = NOSLIDES;
	private PSWTCanvas canvas;
	private int columns = 6;
	private int slideSize;
	protected TreeSet<Asset> selectedSlides = new TreeSet<Asset>(new Comparator<Asset>() {
		public int compare(Asset a1, Asset a2) {
			Integer i1 = galleryMap.get(a1);
			Integer i2 = galleryMap.get(a2);
			return (i1 == null || i2 == null) ? 0 : i1.compareTo(i2);
		}
	});
	private List<Asset> collection;
	private int count;
	private PSWTPath surface;
	java.awt.Rectangle surfaceBounds;
	private List<Listener> listeners = new ArrayList<Listener>(3);
	private GalleryZoomEventHandler zoomHandler;
	private InertiaMouseWheelListener wheelListener;
	private PSWTImage cover;
	Image shadowImage;
	protected boolean showRotateButtons;
	protected boolean showColorCode;
	protected int showRating;
	protected boolean showLocation;
	protected boolean showVoicenoteButton;
	protected boolean showDoneMark;
	private int showRegions;
	private GalleryPanEventHandler panHandler;
	private Map<Asset, Integer> galleryMap = new HashMap<Asset, Integer>();
	private ImageStore imageSource;

	private AssetSelection oldAssetSelection;

	private IScoreFormatter scoreFormatter;

	private PGalleryItem focussedSlide;

	protected PGalleryItem lastSingleClick;

	private final int thumbSize;

	protected TextEventHandler textEventHandler;

	private org.eclipse.swt.graphics.Color selectionBackgroundColor;

	private org.eclipse.swt.graphics.Color foregroundColor;

	private org.eclipse.swt.graphics.Color backgroundColor;

	public VerifyListener titleVerifyListener;

	private String personId;

	private final Point2D pntSrc = new Point(0, 0);

	private Color regionColor;

	protected double previousMagnification = 1d;

	protected AffineTransform oldTransform;

	private List<Rectangle> rectangleList = new ArrayList<>();

	private final CaptionProcessor captionProcessor = new CaptionProcessor(Constants.TH_ALL);

	private CaptionConfiguration captionConfiguration = captionProcessor.computeCaptionConfiguration();

	public AnimatedGallery(final PSWTCanvas canvas, int slideSize, int thumbSize, ImageStore imageSource) {
		this.thumbSize = thumbSize;
		this.imageSource = imageSource;
		setPreferences();
		this.canvas = canvas;
		this.slideSize = slideSize;
		RGB rgb = JFaceResources.getColorRegistry().getRGB(Constants.APPCOLOR_REGION_FACE);
		regionColor = new Color(rgb.red, rgb.green, rgb.blue);
		canvas.setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
		Rectangle bounds = canvas.getDisplay().getPrimaryMonitor().getBounds();
		surfaceBounds = new java.awt.Rectangle(-bounds.width * 10, -bounds.height * 10, bounds.width * 20,
				bounds.height * 20);
		surface = PSWTPath.createRectangle(surfaceBounds.x, surfaceBounds.y, surfaceBounds.width, surfaceBounds.height);
		slideBar = PSWTPath.createRectangle(0, 0, columns * slideSize, slideSize);
		cover = new ZPSWTImage(canvas, Icons.transparentCover.getImage());
		cover.setPickable(false);
		themeChanged();
		canvas.getLayer().addChild(surface);
		surface.addChild(slideBar);
		// TODO is this needed?
		// canvas.addGestureListener(new GestureListener() {
		//
		// public void gesture(GestureEvent e) {
		// if ((e.detail & SWT.GESTURE_BEGIN) != 0) {
		// if (e.magnification == 1d) {
		// previousMagnification = e.magnification;
		// wheelListener.pause();
		// }
		// } else if ((e.detail & SWT.GESTURE_MAGNIFY) != 0) {
		// zoom(canvas,
		// Math.sqrt(e.magnification / previousMagnification));
		// previousMagnification = e.magnification;
		// } else if ((e.detail & SWT.GESTURE_PAN) != 0) {
		// pan(canvas, e.xDirection, e.yDirection);
		// } else if ((e.detail & SWT.GESTURE_END) != 0) {
		// wheelListener.restart();
		// }
		// }
		// });

		PBasicInputEventHandler eventHandler = new GalleryPBasicInputEventHandler(canvas);
		canvas.getRoot().getDefaultInputManager().setKeyboardFocus(eventHandler);
		canvas.addInputEventListener(eventHandler);
		setEventHandlers();
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (collection != null)
					fillSlidebar(collection);
			}
		});
		titleVerifyListener = new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				fireErrorEvent(e);
			}
		};
	}

	private void setEventHandlers() {
		PNode[] workArea = new PNode[] { slideBar };
		canvas.removeInputEventListener(canvas.getPanEventHandler());
		canvas.addInputEventListener(panHandler = new GalleryPanEventHandler(this, workArea, surfaceBounds.x,
				surfaceBounds.y, surfaceBounds.width + surfaceBounds.x, surfaceBounds.height + surfaceBounds.y,
				GalleryPanEventHandler.BOTH, InputEvent.ALT_MASK | InputEvent.BUTTON1_MASK, -3, PAN_SENSITIVITY, HACCEL,
				VACCEL));
		canvas.removeInputEventListener(canvas.getZoomEventHandler());
		canvas.addInputEventListener(zoomHandler = new GalleryZoomEventHandler(this, workArea, -10));
		canvas.addMouseWheelListener(wheelListener = new InertiaMouseWheelListener());
	}

	protected void select(PGalleryItem item1, PGalleryItem item2) {
		Integer from = galleryMap.get(item1.getAsset());
		Integer to = galleryMap.get(item2.getAsset());
		if (from != null && to != null) {
			Set<Asset> toBeDeselected = new HashSet<Asset>(selectedSlides);
			for (int i = from; i <= to; i++) {
				if (slides[i] == null)
					makeSlide(columns, i, collection.get(i));
				select(slides[i], 500);
				toBeDeselected.remove(slides[i].getAsset());
			}
			for (Asset asset : toBeDeselected) {
				Integer index = galleryMap.get(asset);
				if (index != null)
					deselect(slides[index], 500);
			}
		}
	}

	protected boolean getOrder(PGalleryItem item1, PGalleryItem item2) {
		Integer index1 = galleryMap.get(item1.getAsset());
		Integer index2 = galleryMap.get(item2.getAsset());
		if (index1 != null && index2 != null)
			return index1 < index2;
		return false;
	}

	protected void nav(int i, int j) {
		if (focussedSlide != null) {
			Integer index = galleryMap.get(focussedSlide.getAsset());
			if (index != null) {
				int ni = index.intValue();
				ni += i + j * columns;
				if (ni >= 0 && ni < slides.length) {
					PGalleryItem slide = slides[ni];
					if (slide == null)
						slide = makeSlide(columns, ni, collection.get(ni));
					goToSlide(lastSingleClick = slide);
				}
			}
		}
	}

	protected void setPreferences() {
		IPreferenceStore preferenceStore = applyPreferences();
		preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				if (PreferenceConstants.SHOWROTATEBUTTONS.equals(property)
						|| PreferenceConstants.SHOWCOLORCODE.equals(property)
						|| PreferenceConstants.SHOWRATING.equals(property)
						|| PreferenceConstants.SHOWLOCATION.equals(property)
						|| PreferenceConstants.SHOWDONEMARK.equals(property)
						|| PreferenceConstants.SHOWVOICENOTE.equals(property)
						|| PreferenceConstants.MAXREGIONS.equals(property))
					applyPreferences();
			}
		});
	}

	protected IPreferenceStore applyPreferences() {
		final IPreferenceStore preferenceStore = UiActivator.getDefault().getPreferenceStore();
		showRotateButtons = preferenceStore.getBoolean(PreferenceConstants.SHOWROTATEBUTTONS);
		showColorCode = !PreferenceConstants.COLORCODE_NO
				.equals(preferenceStore.getString(PreferenceConstants.SHOWCOLORCODE));
		showLocation = preferenceStore.getBoolean(PreferenceConstants.SHOWLOCATION);
		String rating = preferenceStore.getString(PreferenceConstants.SHOWRATING);
		showRating = PreferenceConstants.SHOWRATING_NO.equals(rating) ? RATING_NO
				: PreferenceConstants.SHOWRATING_COUNT.equals(rating) ? RATING_COUNT : RATING_SIZE;
		showDoneMark = preferenceStore.getBoolean(PreferenceConstants.SHOWDONEMARK);
		showVoicenoteButton = preferenceStore.getBoolean(PreferenceConstants.SHOWVOICENOTE);
		showRegions = preferenceStore.getInt(PreferenceConstants.MAXREGIONS);
		return preferenceStore;
	}

	public void setMinScale(double minScale) {
		wheelListener.setMinScale(minScale);
		zoomHandler.setMinScale(minScale);
	}

	public void setMaxScale(double maxScale) {
		wheelListener.setMaxScale(maxScale);
		zoomHandler.setMaxScale(maxScale);
	}

	public double getMinScale() {
		return wheelListener.getMinScale();
	}

	public double getMaxScale() {
		return wheelListener.getMaxScale();
	}

	public void deselect(PGalleryItem slide, int msec) {
		if (selectedSlides.remove(slide.getAsset())) {
			slide.deselect(msec, selectedSlides.isEmpty());
			fireSelectionEvent(1, slide, 0);
		}
	}

	public void select(PGalleryItem slide, int msec) {
		if (!slide.isSelected()) {
			if (getSelectionCount() == 0) {
				PBounds bBounds = slideBar.getBoundsReference();
				slideBar.addChild(cover);
				cover.setBounds(0d, 0d, bBounds.getWidth(), bBounds.getHeight());
			}
			selectedSlides.add(slide.getAsset());
			slide.raiseToTop();
			slide.select(msec, true);
		}
	}

	void goToSlide(PGalleryItem slide) {
		Asset asset = slide == null ? null : slide.getAsset();
		if (getSelectionCount() != 1 || asset == null || !isSelected(asset)) {
			int l = selectedSlides.size();
			if (l > 0) {
				int i = 0;
				for (Asset a : selectedSlides) {
					Integer index = galleryMap.get(a);
					if (index != null) {
						PGalleryItem item = slides[index];
						if (item != null)
							item.deselect(1000, (++i) == l && slide == null);
					}
				}
				selectedSlides.clear();
				focussedSlide = null;
			}
			if (slide != null) {
				selectedSlides.add(asset);
				oldAssetSelection = null;
				if (l == 0) {
					PBounds bBounds = slideBar.getBoundsReference();
					slideBar.addChild(cover);
					cover.setBounds(0d, 0d, bBounds.getWidth(), bBounds.getHeight());
				}
				slide.raiseToTop();
				slide.select(1000, true);
				focussedSlide = slide;
			}
		}
		for (Asset a : selectedSlides) {
			Integer index = galleryMap.get(a);
			if (index != null) {
				PGalleryItem item = slides[index];
				if (item != null) {
					fireSelectionEvent(1, item, 0);
					return;
				}
			}
		}
		fireSelectionEvent(1, null, 0);
	}

	public void themeChanged() {
		CssActivator.getDefault().applyStyles(canvas, true);
		Color newPaint = UiUtilities.getAwtBackground(canvas, backgroundColor);
		surface.setPaint(newPaint);
		slideBar.setPaint(newPaint);
		slideBar.setStrokeColor(UiUtilities.getAwtForeground(canvas, foregroundColor));
		CssActivator.getDefault().applyExtendedStyle(canvas, this);
	}

	public void setOfflineColor(org.eclipse.swt.graphics.Color c) {
		// do nothing
	}

	public void setSelectedOfflineColor(org.eclipse.swt.graphics.Color c) {
		offlineColor = UiUtilities.getAwtForeground(canvas, c);
	}

	public void setRemoteColor(org.eclipse.swt.graphics.Color c) {
		// not used
	}

	public void setTitleColor(org.eclipse.swt.graphics.Color c) {
		// not used
	}

	public void setSelectedRemoteColor(org.eclipse.swt.graphics.Color c) {
		remoteColor = UiUtilities.getAwtForeground(canvas, c);
	}

	public boolean applyColorsTo(Object element) {
		return element instanceof PSWTCanvas;
	}

	public void setCollection(List<Asset> list, boolean reset) {
		this.collection = list;
		count = (list == null) ? 0 : list.size();
		clearSlidebar();
		slides = new PGalleryItem[count];
		PBounds bounds = slideBar.getBoundsReference();
		if (count == 0)
			slideBar.setBounds(bounds.getX(), bounds.getY(), columns * slideSize, bounds.getHeight());
		else {
			int rows = (count + columns - 1) / columns;
			slideBar.setBounds(bounds.getX(), bounds.getY(), columns * slideSize, rows * slideSize);
		}
		slideBar.repaint();
		galleryMap.clear();
		if (reset)
			resetPanAndZoom(canvas.getCamera());
		fillSlidebar(list);
	}

	private void fillSlidebar(List<Asset> list) {
		if (list != null) {
			ArrayList<PGalleryItem> selected = new ArrayList<PGalleryItem>(getSelectionCount());
			for (int i = 0; i < list.size(); i++)
				if (slides[i] == null && testSlide(columns, i)) {
					PGalleryItem selectedSlide = makeSlide(columns, i, list.get(i));
					if (selectedSlide != null)
						selected.add(selectedSlide);
				}
			for (PGalleryItem galleryItem : selected)
				galleryItem.raiseToTop();
		}
	}

	public void update(Collection<? extends Asset> assets) {
		for (Asset asset : assets) {
			Integer i = galleryMap.get(asset);
			if (i != null) {
				PGalleryItem item = slides[i];
				if (item != null)
					item.update(asset);
			}
		}
	}

	public void setColumns(int columns) {
		this.columns = columns;
		PBounds bounds = slideBar.getBoundsReference();
		int cnt = slides.length;
		if (cnt > 0) {
			int rows = (cnt + columns - 1) / columns;
			clearSlidebar();
			slides = new PGalleryItem[cnt];
			slideBar.setBounds(bounds.getX(), bounds.getY(), columns * slideSize, rows * slideSize);
			slideBar.repaint();
			fillSlidebar(collection);
		} else {
			slideBar.setBounds(bounds.getX(), bounds.getY(), columns * slideSize, bounds.getHeight());
			slideBar.repaint();
		}
	}

	private boolean testSlide(int cols, int i) {
		int availableSpace = slideSize * 9 / 10;
		int margins = (slideSize - availableSpace) / 2;
		PCamera camera = canvas.getCamera();
		PBounds viewBounds = camera.getViewBounds();
		int x = i % cols;
		int y = i / cols;
		PBounds itemBounds = new PBounds(margins + x * slideSize, margins + y * slideSize, availableSpace,
				availableSpace);
		return viewBounds.intersects(itemBounds);
	}

	private PGalleryItem makeSlide(int cols, int i, Asset asset) {
		int availableSpace = slideSize * 9 / 10;
		int margins = (slideSize - availableSpace) / 2;
		int x = i % cols;
		int y = i / cols;
		PGalleryItem slide = new PGalleryItem(canvas, i, asset);
		slide.offset(x * slideSize + slideSize / 4, y * slideSize + slideSize / 3);
		double size = Math.max(slide.getHeight(), slide.getWidth());
		double thumbFactor = thumbSize / size;
		slide.scale(thumbFactor);
		slide.setSelectTransform();
		slide.setTransform(new AffineTransform());
		double scaleFactor = availableSpace / size;
		double multiScaleFactor = (scaleFactor + 1d) * 0.5d;
		slide.scale(multiScaleFactor);
		slide.offset(
				margins + x * slideSize + slideSize / 4 + (availableSpace - slide.getWidth() * multiScaleFactor) / 2,
				margins + y * slideSize + slideSize / 3 + (availableSpace - slide.getHeight() * multiScaleFactor) / 2);
		slide.setMultiTransform();
		slide.setTransform(new AffineTransform());
		slide.scale(scaleFactor);
		slide.offset(margins + x * slideSize + (availableSpace - slide.getWidth() * scaleFactor) / 2,
				margins + y * slideSize + (availableSpace - slide.getHeight() * scaleFactor) / 2);
		slide.setDeselectTransform();
		slideBar.addChild(0, slide);
		if (slides.length <= i) {
			PGalleryItem[] copy = new PGalleryItem[Math.max(i + 1, slides.length * 2)];
			System.arraycopy(slides, 0, copy, 0, slides.length);
			slides = copy;
		}
		slides[i] = slide;
		if (asset != null) {
			galleryMap.put(asset, i);
			if (isSelected(asset))
				select(slide, 100);
		}
		return slide;
	}

	private void clearSlidebar() {
		try {
			slideBar.removeAllChildren();
		} catch (SWTException e) {
			// Ignore
		}
		slides = NOSLIDES;
		selectedSlides.clear();
	}

	public boolean isDisposed() {
		return canvas.isDisposed();
	}

	public void dispose() {
		if (wheelListener != null) {
			wheelListener.dispose();
			wheelListener = null;
		}
		if (panHandler != null) {
			panHandler.dispose();
			panHandler = null;
		}
		if (zoomHandler != null) {
			zoomHandler.dispose();
			zoomHandler = null;
		}
		clearSlidebar();
	}

	public void addListener(Listener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void addSelectionListener(SelectionListener e) {
		// do nothing
	}

	public void removeListener(Object o) {
		listeners.remove(o);
	}

	private void fireSelectionEvent(int cnt, PNode picked, int stateMask) {
		Event e = new Event();
		e.type = (cnt == 1) ? SWT.Selection : SWT.MouseDoubleClick;
		e.data = picked;
		e.stateMask = stateMask;
		fireEvent(e);
	}

	private void fireKeyEvent(char character, int keycode, int stateMask) {
		Event e = new Event();
		e.type = SWT.KeyUp;
		e.stateMask = stateMask;
		e.character = character;
		e.keyCode = keycode;
		fireEvent(e);
	}

	private void fireOperationEvent(PNode picked, int stateMask, int type, int value) {
		PGalleryItem parent = (PGalleryItem) picked.getParent();
		Asset asset = parent.getAsset();
		Event e = new Event();
		e.type = SWT.SetData;
		e.data = asset;
		e.stateMask = stateMask;
		e.detail = type;
		e.keyCode = value;
		fireEvent(e);
	}

	private void fireRegionEvent(int stateMask, ImageRegion imageRegion) {
		Event e = new Event();
		e.type = SWT.SetData;
		e.data = imageRegion;
		e.stateMask = stateMask;
		e.detail = REGION;
		e.keyCode = 0;
		fireEvent(e);
	}

	private void fireModifyEvent(Asset asset, String newTitle) {
		Event e = new Event();
		e.type = SWT.Modify;
		e.data = asset;
		e.text = newTitle;
		fireEvent(e);
	}

	private void fireErrorEvent(VerifyEvent e) {
		Event ev = new Event();
		ev.data = e.data;
		ev.text = e.text;
		ev.type = SWT.Verify;
		fireEvent(ev);
	}

	private void fireEvent(Event ev) {
		ev.display = canvas.getDisplay();
		ev.widget = canvas;
		ev.time = (int) System.currentTimeMillis();
		for (Listener listener : listeners)
			listener.handleEvent(ev);
	}

	public AssetSelection getSelection() {
		if (oldAssetSelection != null)
			return oldAssetSelection;
		AssetSelection sel = new AssetSelection(selectedSlides.size());
		for (Asset asset : selectedSlides) {
			Integer index = galleryMap.get(asset);
			if (index != null) {
				PGalleryItem item = slides[index];
				if (item != null) {
					sel.add(item.getAsset());
					continue;
				}
			}
			sel.add(asset);
		}
		return sel;
	}

	public void setSelection(AssetSelection assetSelection, IAssetProvider provider) {
		oldAssetSelection = assetSelection;
		if (assetSelection.isPicked()) {
			for (Iterator<Asset> iterator = selectedSlides.iterator(); iterator.hasNext();) {
				Asset asset = iterator.next();
				if (!assetSelection.isSelected(asset)) {
					Integer index = galleryMap.get(asset);
					if (index != null) {
						PGalleryItem item = slides[index];
						if (item != null) {
							deselect(item, 200);
							continue;
						}
					}
					iterator.remove();
				}
			}
			int cnt = 0;
			for (Asset asset : assetSelection) {
				if (!selectedSlides.contains(asset)) {
					Integer i = galleryMap.get(asset);
					int index = -1;
					if (i != null)
						index = i;
					else if (provider != null) {
						index = provider.indexOf(asset);
						if (index >= 0)
							makeSlide(columns, index, asset);
					}
					if (index >= 0 && slides[index] != null) {
						if (cnt == 0)
							revealSlide(asset);
						select(slides[index], 1000);
						continue;
					}
					selectedSlides.add(asset);
				} else if (cnt == 0)
					revealSlide(asset);
				++cnt;
			}
		} else
			for (Asset asset : galleryMap.keySet()) {
				if (!selectedSlides.contains(asset)) {
					Integer index = galleryMap.get(asset);
					if (slides[index] != null)
						select(slides[index], 1000);
				}
			}
	}

	private void revealSlide(Asset asset) {
		Integer i = galleryMap.get(asset);
		if (i != null) {
			PGalleryItem slide = slides[i];
			Point2D r1c = slide.getBounds().getCenter2D();
			slide.localToGlobal(r1c);
			PCamera camera = canvas.getCamera();
			Point2D r2c = camera.getBounds().getCenter2D();
			camera.localToGlobal(r2c);
			pan(canvas, (int) (r2c.getX() - r1c.getX()), (int) (r2c.getY() - r1c.getY()));
			slide.raiseToTop();
		}
	}

	private boolean isSelected(Asset asset) {
		if (oldAssetSelection != null && !oldAssetSelection.isPicked())
			return true;
		return selectedSlides.contains(asset);
	}

	public String getTooltip(int x, int y) {
		ArrayList<PNode> nodes = findMatchingNodes(x, y);
		for (PNode node : nodes) {
			String name = node.getName();
			if (name != null)
				return name;
		}
		return null;
	}

	public Asset getItem(int x, int y) {
		ArrayList<PNode> nodes = findMatchingNodes(x, y);
		for (PNode node : nodes) {
			if (node instanceof PGalleryItem)
				return ((PGalleryItem) node).getAsset();
			if (node instanceof PSWTImage) {
				PNode parent = node.getParent();
				if (parent instanceof PGalleryItem)
					return ((PGalleryItem) parent).getAsset();
			}
		}
		return null;
	}

	/**
	 * @param x
	 *            - x-coordinate canvas
	 * @param y
	 *            - y-coordinate canvas
	 * @return list of matching nodes
	 */
	private ArrayList<PNode> findMatchingNodes(int x, int y) {
		PCamera camera = canvas.getCamera();
		pntSrc.setLocation(x, y);
		camera.localToView(pntSrc);
		ArrayList<PNode> results = new ArrayList<PNode>();
		slideBar.findIntersectingNodes(new java.awt.Rectangle((int) pntSrc.getX(), (int) pntSrc.getY(), 1, 1), results);
		return results;
	}

	/**
	 * @param x
	 *            - x-coordinate canvas
	 * @param y
	 *            - y-coordinate canvas
	 * @param owner
	 *            - owning thumbnail or null
	 * @return list of matching regions
	 */
	public PGalleryRegion[] getRegions(int x, int y, PSWTAssetThumbnail owner) {
		ArrayList<PNode> nodes = findMatchingNodes(x, y);
		List<PGalleryRegion> result = new ArrayList<>(nodes.size());
		for (PNode node : nodes)
			if (node instanceof PGalleryRegion && (owner == null || node.getParent() == owner))
				result.add(((PGalleryRegion) node));
		return result.toArray(new PGalleryRegion[result.size()]);
	}

	public int getSelectionCount() {
		return (oldAssetSelection != null) ? oldAssetSelection.size() : selectedSlides.size();
	}

	public void setScoreFormatter(IScoreFormatter scoreFormatter) {
		this.scoreFormatter = scoreFormatter;
	}

	public Display getDisplay() {
		return canvas.getDisplay();
	}

	public void setCursor(Cursor cursor) {
		canvas.setCursor(cursor);
	}

	public void addMouseListener(MouseListener listener) {
		canvas.addMouseListener(listener);
	}

	public void addMouseMoveListener(MouseMoveListener listener) {
		canvas.addMouseMoveListener(listener);
	}

	public void addKeyListener(KeyListener listener) {
		canvas.addKeyListener(listener);
	}

	public void removeMouseListener(MouseListener listener) {
		canvas.removeMouseListener(listener);
	}

	public void removeMouseMoveListener(MouseMoveListener listener) {
		canvas.removeMouseMoveListener(listener);
	}

	public void removeKeyListener(KeyListener listener) {
		canvas.removeKeyListener(listener);
	}

	public Control getControl() {
		return canvas;
	}

	public void setFocus() {
		canvas.setFocus();
	}

	public Shell getShell() {
		return canvas.getShell();
	}

	public PGalleryItem[] getItems() {
		return slides;
	}

	public void setForegroundColor(org.eclipse.swt.graphics.Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public void setSelectionForegroundColor(org.eclipse.swt.graphics.Color selectionForegroundColor) {
		// do nothing
	}

	public void setSelectionBackgroundColor(org.eclipse.swt.graphics.Color selectionBackgroundColor) {
		this.selectionBackgroundColor = selectionBackgroundColor;
	}

	public void setBackgroundColor(org.eclipse.swt.graphics.Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public void setTitleForeground(org.eclipse.swt.graphics.Color color) {
		// do nothing
	}

	public void setTitleBackground(org.eclipse.swt.graphics.Color color) {
		// do nothing
	}

	public void setPersonFilter(String personId) {
		this.personId = personId;
	}

	private void resetPanAndZoom(PCamera camera) {
		wheelListener.cancel();
		if (oldTransform == null) {
			oldTransform = camera.getViewTransform();
			camera.setViewTransform(new AffineTransform());
		} else {
			camera.setViewTransform(oldTransform);
			resetTransform();
		}
	}

	private void pan(final PSWTCanvas pswtCanvas, int dx, int dy) {
		PCamera camera = pswtCanvas.getCamera();
		PBounds viewBounds = camera.getViewBounds();
		double x = viewBounds.getX() - dx;
		if (x < surfaceBounds.x && dx > 0)
			return;
		if (x + viewBounds.getWidth() > surfaceBounds.x + surfaceBounds.width && dx < 0)
			return;
		double y = viewBounds.getY() - dy;
		if (y < surfaceBounds.y && dy > 0)
			return;
		if (y + viewBounds.getHeight() > surfaceBounds.y + surfaceBounds.height && dy < 0)
			return;
		camera.translateView(dx, dy);
		resetTransform();
	}

	private void zoom(final PSWTCanvas pswtCanvas, double scaleDelta) {
		PCamera camera = pswtCanvas.getCamera();
		double newScale = scaleDelta * camera.getViewScale();
		if (newScale >= wheelListener.getMinScale() && newScale <= wheelListener.getMaxScale()) {
			Point2D offset = null;
			for (Asset asset : selectedSlides) {
				Integer index = galleryMap.get(asset);
				if (index != null) {
					PGalleryItem galleryItem = slides[index];
					if (galleryItem != null)
						offset = galleryItem.getOffset();
				}
			}
			if (offset != null)
				camera.scaleViewAboutPoint(scaleDelta, offset.getX(), offset.getY());
			else {
				Rectangle clientArea = pswtCanvas.getClientArea();
				camera.scaleViewAboutPoint(scaleDelta, clientArea.x + clientArea.width / 2,
						clientArea.y + clientArea.height / 2);
			}
			resetTransform();
		}
	}

	public void resetTransform() {
		oldTransform = null;
	}

	protected void focusOnTitle() {
		if (focussedSlide != null && focussedSlide.caption != null)
			focussedSlide.setFocusTo(focussedSlide.caption, true);
	}

	public ImageRegion getBestFaceRegion(int x, int y, boolean all, PSWTAssetThumbnail owner) {
		PGalleryRegion[] regions = getRegions(x, y, owner);
		if (regions != null) {
			ImageRegion bestRegion = getBestRegion(regions, Region.type_face, true, x, y);
			return (bestRegion == null && all) ? getBestRegion(regions, Region.type_face, false, x, y) : bestRegion;
		}
		return null;
	}

	public ImageRegion getBestRegion(PGalleryRegion[] regions, String type, boolean all, int x, int y) {
		ImageRegion foundRegion = null;
		if (regions != null) {
			double minD = Double.MAX_VALUE;
			for (PGalleryRegion region : regions) {
				ImageRegion imageRegion = region.getImageRegion();
				if ((all || imageRegion.name == null || imageRegion.name.equals("?")) //$NON-NLS-1$
						&& (type == null || type.equals(imageRegion.type))) {
					double d = region.getDistanceFromRegionCenter(x, y);
					if (d < minD) {
						minD = d;
						foundRegion = imageRegion;
					}
				}
			}
		}
		return foundRegion;
	}

	public ImageRegion[] findAllRegions(org.eclipse.swt.events.MouseEvent event) {
		PGalleryRegion[] regions = getRegions(event.x, event.y, null);
		ImageRegion[] imageRegions = new ImageRegion[regions.length];
		for (int i = 0; i < regions.length; i++)
			imageRegions[i] = regions[i].getImageRegion();
		return imageRegions;
	}

	public void setContext(SmartCollection coll) {
		captionConfiguration = captionProcessor.computeCaptionConfiguration(coll);
	}

	public CaptionProcessor getCaptionProcessor() {
		return captionProcessor;
	}

}
