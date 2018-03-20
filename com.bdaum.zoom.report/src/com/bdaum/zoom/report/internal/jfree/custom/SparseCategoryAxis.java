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
 * (c) 2017 Berthold Daum  
 */
package com.bdaum.zoom.report.internal.jfree.custom;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.text.G2TextMeasurer;
import org.jfree.text.TextBlock;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;

public class SparseCategoryAxis extends CategoryAxis {

	private static final long serialVersionUID = -9084199063790125884L;
	private int nth;

	/**
	 * @param nth
	 *            - every nth tick gets a label
	 */
	public SparseCategoryAxis(int nth) {
		super();
		this.nth = nth;
	}

	public SparseCategoryAxis(String label, int nth) {
		super(label);
		this.nth = nth;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge) {
		List ticks = super.refreshTicks(g2, state, dataArea, edge);
		Iterator iterator = ticks.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			iterator.next();
			if (i++ % nth != 0)
				iterator.remove();
		}
		return ticks;
	}

	@Override
	protected TextBlock createLabel(@SuppressWarnings("rawtypes") Comparable category, float width, RectangleEdge edge,
			Graphics2D g2) {
		return TextUtilities.createTextBlock(category.toString(), getTickLabelFont(category),
				getTickLabelPaint(category), width * nth, 1, new G2TextMeasurer(g2));
	}

}
