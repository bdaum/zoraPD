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

import org.piccolo2d.extras.swt.PSWTPath;
import org.piccolo2d.extras.swt.SWTGraphics2D;
import org.piccolo2d.util.PPaintContext;

/**
 * 
 * A PSWTMPath with linewidth and transparency support
 * Needs patched version of SWTGraphics2D
 * @author bdaum
 *
 */
public class ZPSWTPath extends PSWTPath {
	
	private static final long serialVersionUID = -1387654478887460746L;
	private double lineWidth;

	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}
	@Override
	protected void paint(PPaintContext paintContext) {
		((SWTGraphics2D) paintContext.getGraphics()).setLineWidth(lineWidth);
		super.paint(paintContext);
	}
}
