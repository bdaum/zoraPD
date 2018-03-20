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

import org.eclipse.swt.graphics.Image;
import org.piccolo2d.extras.swt.PSWTCanvas;
import org.piccolo2d.extras.swt.PSWTImage;

/**
 * A PSWTImage version that does not dispose received images but leaves that to
 * the client.
 * 
 * @author bdaum
 * 
 */
public class ZPSWTImage extends PSWTImage {

	private static final long serialVersionUID = -4496227339667796156L;

	public ZPSWTImage(PSWTCanvas canvas, Image image) {
		super(canvas, image);
	}

	public ZPSWTImage(PSWTCanvas canvas, String fileName) {
		super(canvas);
		throw new UnsupportedOperationException(fileName);
	}

	public ZPSWTImage(PSWTCanvas canvas) {
		super(canvas);
	}

	@Override
	public void setImage(String filePath) {
		throw new UnsupportedOperationException(filePath);
	}

	@Override
	protected void disposeImage() {
		// do nothing
	}
}
