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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

import edu.umd.cs.piccolox.swt.PSWTCanvas;

/**
 * An antialiased version of PSWTCanvas
 * 
 * @author bdaum
 * 
 */
public class ZPSWTCanvas extends PSWTCanvas {

	public ZPSWTCanvas(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public void paintComponent(GC gc, int x, int y, int w, int h) {
		gc.setAntialias(SWT.ON);
		super.paintComponent(gc, x, y, w, h);
	}
}
