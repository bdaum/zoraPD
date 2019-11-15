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

/*******************************************************************************
 * Derived from DefaultGalleryItemRenderer.
 * Modifications by Berthold Daum (bd)
 *
 *
 * Copyright (c) 2006-2007 Nicolas Richeton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors :
 *    Nicolas Richeton (nicolas.richeton@gmail.com) - initial API and implementation
 *    Richard Michalsky - bugs 195415,  195443
 *    Berthold Daum - adapted to ZoRaPD
 *
 *******************************************************************************/

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.nebula.widgets.gallery.AbstractGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.Gallery;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.nebula.widgets.gallery.RendererHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.ui.internal.UiConstants;
import com.bdaum.zoom.ui.internal.UiUtilities;

/**
 * <p>
 * Default item renderer used by the Gallery widget. Supports single line text,
 * image, drop shadows and decorators.
 * </p>
 * <p>
 * Decorator images can be set with {@link GalleryItem#setData(String, Object)}
 * by using the following keys :
 * </p>
 * <ul>
 * <li>org.eclipse.nebula.widget.gallery.bottomLeftOverlay</li>
 * <li>org.eclipse.nebula.widget.gallery.bottomRightOverlay</li>
 * <li>org.eclipse.nebula.widget.gallery.topLeftOverlay</li>
 * <li>org.eclipse.nebula.widget.gallery.topRightOverlay</li>
 * </ul>
 * <p>
 * Supported types are org.eclipse.swt.Image for one single decorator and
 * org.eclipse.swt.Image[] for multiple decorators.
 * </p>
 * <p>
 * NOTE: THIS WIDGET AND ITS API ARE STILL UNDER DEVELOPMENT.
 * </p>
 *
 * @author Nicolas Richeton (nicolas.richeton@gmail.com)
 * @contributor Richard Michalsky (bugs 195415, 195443)
 * @contributor Peter Centgraf (bugs 212071, 212073)
 * @contributor Berthold Daum (adapted for ZoRa)
 */
public class LightboxGalleryItemRenderer extends AbstractGalleryItemRenderer {

	/**
	 * Stores colors used in drop shadows
	 */
	protected ArrayList<Color> dropShadowsColors = new ArrayList<Color>();

	// Renderer parameters
	private boolean dropShadows = false;

	int dropShadowsSize = 0;

	Color selectionForegroundColor;

	Color selectionBackgroundColor;

	Color foregroundColor, backgroundColor;

	boolean showLabels = true;

	boolean showRoundedSelectionCorners = true;

	int selectionRadius = 15;

	private Color offlineColor;

	private Color selectedOfflineColor;

	private Color remoteColor;

	private Color selectedRemoteColor;

	private int showRegions;

	private String persId;

	private ArrayList<ImageRegion> regions;

	private Color white;

	private boolean showRegionNames;

	private int alignment;

	/**
	 * Returns current label state : enabled or disabled
	 *
	 * @return true if labels are enabled.
	 * @see LightboxGalleryItemRenderer#setShowLabels(boolean)
	 *
	 */
	public boolean isShowLabels() {
		return showLabels;
	}

	/**
	 * Enables / disables labels at the bottom of each item.
	 *
	 * @param showLabels
	 * @see LightboxGalleryItemRenderer#isShowLabels()
	 *
	 */
	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}

	public LightboxGalleryItemRenderer(Gallery gallery) {
		this.gallery = gallery;
		Display display = gallery.getDisplay();
		backgroundColor = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);

		selectionForegroundColor = display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
		selectionBackgroundColor = display.getSystemColor(SWT.COLOR_LIST_SELECTION);
		
		white = display.getSystemColor(SWT.COLOR_WHITE);

		// Create drop shadows colours
		createColors();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.nebula.widgets.gallery.AbstractGalleryItemRenderer#draw(org
	 * .eclipse.swt.graphics.GC, org.eclipse.nebula.widgets.gallery.GalleryItem,
	 * int, int, int, int, int)
	 */

	@Override
	public void draw(GC gc, GalleryItem item, int index, int x, int y, int width, int height) {
		Image itemImage = item.getImage();
		Color itemBackgroundColor = item.getBackground();
		// Color itemForegroundColor = item.getForeground(); // bd
		Color itemForegroundColor = foregroundColor; // bd
		if (itemForegroundColor == null)
			itemForegroundColor = item.getForeground();
		if (itemForegroundColor == null)
			itemForegroundColor = gallery.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND);

		// Set up the GC

		gc.setFont(item.getFont());

		// Create some room for the label.
		int useableHeight = height;
		int fontHeight = 0;
		if (item.getText() != null && !EMPTY_STRING.equals(item.getText()) && this.showLabels) {
			fontHeight = gc.getFontMetrics().getHeight();
			useableHeight -= fontHeight + 2;
		}

		int imageWidth = 0;
		int imageHeight = 0;
		int xShift = 0;
		int yShift = 0;
		Point size = null;

		if (itemImage != null) {
			Rectangle itemImageBounds = itemImage.getBounds();
			imageWidth = itemImageBounds.width;
			imageHeight = itemImageBounds.height;

			size = RendererHelper.getBestSize(imageWidth, imageHeight, width - 8 - 2 * this.dropShadowsSize,
					useableHeight - 8 - 2 * this.dropShadowsSize);

			xShift = RendererHelper.getShift(width, size.x);
			yShift = RendererHelper.getShift(useableHeight, size.y);

			if (dropShadows) {
				Color c = null;
				for (int i = this.dropShadowsSize - 1; i >= 0; i--) {
					c = dropShadowsColors.get(i);
					gc.setForeground(c);

					gc.drawLine(x + width + i - xShift - 1, y + dropShadowsSize + yShift, x + width + i - xShift - 1,
							y + useableHeight + i - yShift);
					gc.drawLine(x + xShift + dropShadowsSize, y + useableHeight + i - yShift - 1,
							x + width + i - xShift, y - 1 + useableHeight + i - yShift);
				}
			}
		}

		// Draw background (rounded rectangles)
		if (selected || itemBackgroundColor != null) {
			// Set colors
			if (selected) {
				gc.setBackground(selectionBackgroundColor);
				gc.setForeground(selectionBackgroundColor);
			} else if (itemBackgroundColor != null) {
				gc.setBackground(itemBackgroundColor);
			}

			// Draw
			if (showRoundedSelectionCorners) {
				gc.fillRoundRectangle(x, y, width, useableHeight, selectionRadius, selectionRadius);
			} else {
				gc.fillRectangle(x, y, width, height);
			}

			if (item.getText() != null && !EMPTY_STRING.equals(item.getText()) && showLabels) {
				gc.fillRoundRectangle(x, y + height - fontHeight, width, fontHeight, selectionRadius, selectionRadius);
			}
		}
		Asset asset = (Asset) item.getData(UiConstants.ASSET);
		// Draw image
		if (itemImage != null && size != null && size.x > 0 && size.y > 0) {
			int xs = x + xShift;
			int ys = y + yShift;
			if (item.getData(UiConstants.CARD) != null) { // bd
				gc.drawImage(itemImage, 0, 0, imageWidth, imageHeight, xs + 2, ys + 2, size.x, size.y);
				gc.setForeground(white);
				gc.drawRectangle(xs + 2, ys + 2, size.x, size.y);
				gc.drawImage(itemImage, 0, 0, imageWidth, imageHeight, xs - 2, ys - 2, size.x, size.y);
				gc.drawRectangle(xs - 2, ys - 2, size.x, size.y);
			} else
				gc.drawImage(itemImage, 0, 0, imageWidth, imageHeight, xs, ys, size.x, size.y);
			// overlay region frames // bd
			regions = null;
			if (showRegions > 0)
				regions = UiUtilities.drawRegions(gc, asset, xs, ys, size.x, size.y, showRegionNames, showRegions, false,
						persId);
		}

		// Draw label
		if (item.getText() != null && !EMPTY_STRING.equals(item.getText()) && showLabels) {
			// Set colors
			int offlineStatus = asset == null ? IVolumeManager.OFFLINE : asset.getFileState();
			if (selected) {
				switch (offlineStatus) { // bd
				case IVolumeManager.REMOTE:
				case IVolumeManager.PEER:
					if (selectedRemoteColor != null)
						gc.setForeground(selectedRemoteColor);
					break;
				case IVolumeManager.OFFLINE:
					if (selectedOfflineColor != null)
						gc.setForeground(selectedOfflineColor);
					break;
				default:
					gc.setForeground(selectionForegroundColor);
					break;
				}
				gc.setBackground(selectionBackgroundColor);
			} else {
				// Not selected, use item values or defaults.
				// Background
				gc.setBackground(itemBackgroundColor != null ? itemBackgroundColor : backgroundColor);
				switch (offlineStatus) { // bd
				case IVolumeManager.REMOTE:
				case IVolumeManager.PEER:
					if (remoteColor != null)
						gc.setForeground(remoteColor);
					break;
				case IVolumeManager.OFFLINE:
					if (offlineColor != null)
						gc.setForeground(offlineColor);
					break;
				default:
					gc.setForeground(itemForegroundColor);
					break;
				}
			}

			// Create label
			String text = RendererHelper.createLabel(item.getText(), gc, width - 10);

			// Draw
			int shift = 0;
			if (alignment != 0) {
				shift = RendererHelper.getShift(width, gc.textExtent(text).x);
				if (alignment == 2)
					shift <<= 1;
			}
			gc.drawText(text, x + shift, y + height - fontHeight, true);
		}
	}

	public void setDropShadowsSize(int dropShadowsSize) {
		this.dropShadowsSize = dropShadowsSize;
		freeDropShadowsColors();
		createColors();
		// TODO: force redraw

	}

	private void createColors() {
		if (dropShadowsSize > 0) {
			int step = 125 / dropShadowsSize;
			// Create new colors
			Display display = gallery.getDisplay();
			for (int i = dropShadowsSize - 1; i >= 0; i--) {
				int value = 255 - i * step;
				dropShadowsColors.add(new Color(display, value, value, value));
			}
		}
	}

	private void freeDropShadowsColors() {
		// Free colors :
		{
			Iterator<Color> i = this.dropShadowsColors.iterator();
			while (i.hasNext()) {
				Color c = i.next();
				if (c != null)
					c.dispose();
			}
		}
	}

	public boolean isDropShadows() {
		return dropShadows;
	}

	public void setDropShadows(boolean dropShadows) {
		this.dropShadows = dropShadows;
	}

	public int getDropShadowsSize() {
		return dropShadowsSize;
	}

	/**
	 * Returns the font used for drawing item label or <tt>null</tt> if system font
	 * is used.
	 *
	 * @return the font
	 * @deprecated Use {@link Gallery#getFont()}
	 */
	@Deprecated
	public Font getFont() {
		return gallery.getFont();
	}

	/**
	 * Set the font for drawing item label or <tt>null</tt> to use system font.
	 *
	 * @param font
	 *            the font to set
	 * @deprecated Use {@link Gallery#setFont(Font)}
	 */
	@Deprecated
	public void setFont(Font font) {
		gallery.setFont(font);
	}

	@Override
	public void dispose() {
		freeDropShadowsColors();
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IExtendedColorModel2#setForegroundColor(org.
	 * eclipse .swt.graphics.Color)
	 */
	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public Color getSelectionForegroundColor() {
		return selectionForegroundColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IExtendedColorModel2#setSelectionForegroundColor
	 * (org.eclipse.swt.graphics.Color)
	 */
	public void setSelectionForegroundColor(Color selectionForegroundColor) {
		this.selectionForegroundColor = selectionForegroundColor;
	}

	public Color getSelectionBackgroundColor() {
		return selectionBackgroundColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IExtendedColorModel2#setSelectionBackgroundColor
	 * (org.eclipse.swt.graphics.Color)
	 */
	public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
		this.selectionBackgroundColor = selectionBackgroundColor;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IExtendedColorModel2#setBackgroundColor(org.
	 * eclipse .swt.graphics.Color)
	 */
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public boolean isShowRoundedSelectionCorners() {
		return this.showRoundedSelectionCorners;
	}

	public void setShowRoundedSelectionCorners(boolean showRoundedSelectionCorners) {
		this.showRoundedSelectionCorners = showRoundedSelectionCorners;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IExtendedColorModel2#setOfflineColor(org.eclipse
	 * .swt.graphics.Color)
	 */
	public void setOfflineColor(Color offlineColor) {
		this.offlineColor = offlineColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IExtendedColorModel2#setSelectedOfflineColor(
	 * org.eclipse.swt.graphics.Color)
	 */
	public void setSelectedOfflineColor(Color selectedOfflineColor) {
		this.selectedOfflineColor = selectedOfflineColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IExtendedColorModel2#setRemoteColor(org.eclipse
	 * .swt.graphics.Color)
	 */
	public void setRemoteColor(Color remoteColor) {
		this.remoteColor = remoteColor;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.bdaum.zoom.ui.views.IExtendedColorModel2#setSelectedRemoteColor(org
	 * .eclipse.swt.graphics.Color)
	 */
	public void setSelectedRemoteColor(Color selectedRemoteColor) {
		this.selectedRemoteColor = selectedRemoteColor;
	}

	public void setShowRegions(int showRegions, boolean showRegionNames) {
		this.showRegions = showRegions;
		this.showRegionNames = showRegionNames;
	}

	public void setPersonFilter(String persId) {
		this.persId = persId;
	}

	public ImageRegion[] getRegions() {
		return regions == null ? null : regions.toArray(new ImageRegion[regions.size()]);
	}

	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}
}
