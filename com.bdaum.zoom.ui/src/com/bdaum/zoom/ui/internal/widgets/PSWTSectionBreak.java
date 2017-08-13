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

package com.bdaum.zoom.ui.internal.widgets;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;

import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.core.Constants;

import edu.umd.cs.piccolox.swt.PSWTCanvas;

public class PSWTSectionBreak extends ZPSWTImage {
	/**
	 *
	 */
	private static final long serialVersionUID = -5539327360971870974L;
	private final SlideImpl slide;
	private final PSWTCanvas canvas;

	public PSWTSectionBreak(PSWTCanvas canvas, SlideImpl slide) {
		super(canvas, createSectionImage(canvas, slide));
		this.canvas = canvas;
		this.slide = slide;
	}

	private static Image createSectionImage(PSWTCanvas canvas, SlideImpl slide) {
		Display display = canvas.getDisplay();
		Rectangle bounds = display.getPrimaryMonitor().getBounds();
		int mx = Math.max(bounds.width, bounds.height);
		double fac = 640d / mx;
		int w = (int) (bounds.width * fac);
		int h = (int) (bounds.height * fac);
		Image image = new Image(display, w, h);
		int tw, th, tx, ty;
		int iw, ih, ix, iy;
		switch (slide.getLayout()) {
		case Constants.SLIDE_THUMBNAILS_LEFT:
			tw = w * 4 / 10;
			th = h * 8 / 10;
			tx = w / 2;
			ty = h / 10;
			iw = w * 3 / 10;
			ih = th;
			ix = w / 10;
			iy = ty;
			break;
		case Constants.SLIDE_THUMBNAILS_RIGHT:
			tw = w * 4 / 10;
			th = h * 8 / 10;
			tx = w / 10;
			ty = h / 10;
			iw = w * 3 / 10;
			ih = th;
			ix = w * 6 / 10;
			iy = ty;
			break;
		case Constants.SLIDE_THUMBNAILS_TOP:
			tw = w * 8 / 10;
			th = h * 4 / 10;
			tx = w / 10;
			ty = h / 2;
			iw = tw;
			ih = h * 3 / 10;
			ix = tx;
			iy = h / 10;
			break;
		case Constants.SLIDE_THUMBNAILS_BOTTOM:
			tw = w * 8 / 10;
			th = h * 4 / 10;
			tx = w / 10;
			ty = h / 10;
			iw = tw;
			ih = h * 3 / 10;
			ix = tx;
			iy = h * 6 / 10;
			break;
		default:
			tw = w * 8 / 10;
			th = h * 8 / 10;
			tx = w / 10;
			ty = h / 10;
			iw = ih = ix = iy = 0;
			break;
		}
		GC gc = new GC(image);
		gc.setBackground(canvas.getBackground());
		gc.fillRectangle(0, 0, w, h);
		gc.setLineWidth(3);
		gc.setForeground(canvas.getForeground());
		gc.drawRectangle(0, 0, w - 1, h - 1);
		TextLayout layout = new TextLayout(display);
		StringBuilder sb = new StringBuilder();
		if (slide.getCaption() != null)
			sb.append(slide.getCaption());
		int boldLength = sb.length();
		if (slide.getDescription() != null
				&& !slide.getDescription().isEmpty()) {
			if (sb.length() > 0)
				sb.append("\n\n"); //$NON-NLS-1$
			sb.append(slide.getDescription());
		}
		layout.setText(sb.toString());
		layout.setStyle(new TextStyle(JFaceResources.getBannerFont(), null,
				null), 0, boldLength);
		layout.setWidth(tw);
		Rectangle b = layout.getBounds();
		float f = Math.max(0.1f, (float) Math.sqrt((float) b.height / th));
		layout.setWidth((int) (tw * f));
		b = layout.getBounds();
		f = 1f / Math.max((float) b.height / th, (float) b.width / tw);
		Transform t = new Transform(display);
		t.translate(tx, ty);
		t.scale(f, f);
		gc.setTransform(t);
		layout.draw(gc, 0, 0);
		layout.dispose();
		gc.setTransform(null);
		t.dispose();
		int s = Math.max(ih, iw) / 10;
		gc.setBackground(canvas.getForeground());
		switch (slide.getLayout()) {
		case Constants.SLIDE_THUMBNAILS_RIGHT:
		case Constants.SLIDE_THUMBNAILS_LEFT:
			for (int i = 0; i < 5; i++) {
				int y = i * 2 * s + s / 2 + iy;
				for (int j = 0; j < 3; j++) {
					int x = j * 2 * s + s + ix;
					gc.fillRectangle(x, y, s, s);
				}
			}
			break;
		case Constants.SLIDE_THUMBNAILS_TOP:
		case Constants.SLIDE_THUMBNAILS_BOTTOM:
			for (int i = 0; i < 6; i++) {
				int x = i * 18 * s / 10 + ix;
				int y = s + iy;
				gc.fillRectangle(x, y, s, s);
			}
			break;
		}
		gc.dispose();
		return image;
	}


	@Override
	public Image getImage() {
		Image image = super.getImage();
		if (image.isDisposed()) {
			image = createSectionImage(canvas, slide);
			setImage(image);
		}
		return image;
	}

}
