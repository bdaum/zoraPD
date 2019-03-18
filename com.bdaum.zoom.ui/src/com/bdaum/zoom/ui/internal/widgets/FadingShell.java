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

package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.Icons;

public class FadingShell {
	private Shell shell;
	private int alpha;
	private boolean supportsTransparency = false;
	private Region region;
	private final int blendingEffect;

	public FadingShell(Shell shell, boolean fading, int blendingEffect) {
		this.shell = shell;
		if (shell.getImage() == null)
			shell.setImage(Icons.zoraShell.getImage());
		this.blendingEffect = blendingEffect;
		boolean needsRegion = false;
		switch (blendingEffect) {
		case Constants.SLIDE_TRANSITION_EXPAND:
		case Constants.SLIDE_TRANSITION_BLEND_BOTTOMLEFT:
		case Constants.SLIDE_TRANSITION_BLEND_BOTTOMRIGHT:
		case Constants.SLIDE_TRANSITION_BLEND_TOPLEFT:
		case Constants.SLIDE_TRANSITION_BLEND_TOPRIGHT:
		case Constants.SLIDE_TRANSITION_BLEND_LEFT:
		case Constants.SLIDE_TRANSITION_BLEND_RIGHT:
		case Constants.SLIDE_TRANSITION_BLEND_UP:
		case Constants.SLIDE_TRANSITION_BLEND_DOWN:
			needsRegion = true;
			break;
		}
		if (fading) {
			shell.setAlpha(0);
			supportsTransparency = shell.getAlpha() == 0;
			if (!supportsTransparency || needsRegion)
				shell.setRegion(region = new Region());
		}
	}

	public Shell getShell() {
		return shell;
	}

	public void setBounds(Rectangle mbounds) {
		shell.setBounds(mbounds);
	}

	public void layout() {
		shell.layout();
	}

	public void open() {
		shell.open();
	}

	public void close() {
		if (!shell.isDisposed())
			shell.close();
		if (region != null) {
			region.dispose();
			region = null;
		}
	}

	public void setActive() {
		shell.setActive();
	}

	public boolean isDisposed() {
		return shell.isDisposed();
	}

	public boolean isVisible() {
		return shell.isVisible();
	}

	public void setMinimized(boolean minimized) {
		shell.setMinimized(minimized);
	}

	public void forceActive() {
		shell.forceActive();
	}

	public void forceFocus() {
		shell.forceFocus();
	}

	public void setAlpha(int alpha) {
		if (isDisposed())
			return;
		this.alpha = alpha;
		if (supportsTransparency && blendingEffect == Constants.SLIDE_TRANSITION_FADE)
			shell.setAlpha(alpha);
		else {
			shell.setAlpha(255);
			if (region != null) {
				if (alpha >= 255)
					shell.setRegion(null);
				else {
					Rectangle bounds = shell.getBounds();
					int height = bounds.height;
					int width = bounds.width;
					int x;
					int y;
					int w;
					int h;
					switch (blendingEffect) {
					case Constants.SLIDE_TRANSITION_BLEND_LEFT:
						w = Math.max(0, width * alpha / 255);
						x = width - w;
						y = 0;
						h = height;
						break;
					case Constants.SLIDE_TRANSITION_BLEND_RIGHT:
						w = Math.max(0, width * alpha / 255);
						x = 0;
						y = 0;
						h = height;
						break;
					case Constants.SLIDE_TRANSITION_BLEND_DOWN:
						h = Math.max(0, height * alpha / 255);
						x = 0;
						y = 0;
						w = width;
						break;
					case Constants.SLIDE_TRANSITION_BLEND_TOPLEFT:
						w = Math.max(0, width * alpha / 255);
						h = Math.max(0, height * alpha / 255);
						x = width - w;
						y = height - h;
						break;
					case Constants.SLIDE_TRANSITION_BLEND_TOPRIGHT:
						w = Math.max(0, width * alpha / 255);
						h = Math.max(0, height * alpha / 255);
						x = 0;
						y = height - h;
						break;
					case Constants.SLIDE_TRANSITION_BLEND_BOTTOMLEFT:
						w = Math.max(0, width * alpha / 255);
						h = Math.max(0, height * alpha / 255);
						x = width - w;
						y = 0;
						break;
					case Constants.SLIDE_TRANSITION_BLEND_BOTTOMRIGHT:
						w = Math.max(0, width * alpha / 255);
						h = Math.max(0, height * alpha / 255);
						x = 0;
						y = 0;
						break;
					case Constants.SLIDE_TRANSITION_EXPAND:
						w = Math.max(0, width * alpha / 255);
						h = Math.max(0, height * alpha / 255);
						x = (width - w) / 2;
						y = (height - h) / 2;
						break;
					case Constants.SLIDE_TRANSITION_BLEND_UP:
					default:
						h = Math.max(0, height * alpha / 255);
						x = 0;
						y = height - h;
						w = width;
						break;
					}
					region.intersect(x, y, w, h);
					region.add(x, y, w, h);
					shell.setRegion(region);
				}
			}
		}
	}

	public int getAlpha() {
		return alpha;
	}

	public Rectangle getBounds() {
		return shell.getBounds();
	}

	public void setLocation(int x, int y) {
		if (!isDisposed())
			shell.setLocation(x, y);
	}

	public void setFocus() {
		shell.setFocus();
	}

	public Point getLocation() {
		return shell.getLocation();
	}

}
