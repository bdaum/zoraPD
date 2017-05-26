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

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.swt.PSWTText;
import edu.umd.cs.piccolox.swt.SWTGraphics2D;

public class GreekedPSWTText extends PSWTText {

	private static final long serialVersionUID = -7603666089126261216L;
	private boolean isGreek;
	private int textWidth;
	private int alignment;

	public GreekedPSWTText(String text, Font font) {
		super(text, font);
	}

	/**
	 * Paints this object as greek.
	 *
	 * @param ppc
	 *            The graphics context to paint into.
	 */
	@Override
	public void paintAsGreek(final PPaintContext ppc) {
		isGreek = true;
		final SWTGraphics2D sg2 = (SWTGraphics2D) ppc.getGraphics();
		if (greekColor != null) {
			sg2.setBackground(greekColor);
			String line;
			double y = padding;
			final FontMetrics fontMetrics = sg2.getSWTFontMetrics();
			@SuppressWarnings("unchecked")
			final Iterator<String> lineIterator = lines.iterator();
			while (lineIterator.hasNext()) {
				line = lineIterator.next();
				if (line.length() != 0) {
					int textOffset = padding;
					Point textExtent = textExtent(line);
					if (textWidth != SWT.DEFAULT) {
						if (alignment == SWT.RIGHT)
							textOffset = textWidth - 2 * padding - textExtent.x;
						else if (alignment == SWT.CENTER)
							textOffset = (textWidth - textExtent.x) / 2
									- padding;
					}
					sg2.fillRect(textOffset, y, textExtent.x, textExtent.y);
				}
				y += fontMetrics.getHeight();
			}
		}
	}

	/**
	 * Paints this object normally (show it's text). Note that the entire text
	 * gets rendered so that it's upper left corner appears at the origin of
	 * this local object.
	 *
	 * @param ppc
	 *            The graphics context to paint into.
	 */
	@Override
	public void paintAsText(final PPaintContext ppc) {
		isGreek = false;
		final SWTGraphics2D sg2 = (SWTGraphics2D) ppc.getGraphics();

		if (!isTransparent()) {
			if (getPaint() == null)
				sg2.setBackground(Color.WHITE);
			else
				sg2.setBackground((Color) getPaint());
			sg2.fillRect(0, 0, (int) getWidth(), (int) getHeight());
		}

		sg2.translate(padding, padding);

		sg2.setColor(penColor);
		sg2.setFont(font);
		String line;
		double y = 0;
		final FontMetrics fontMetrics = sg2.getSWTFontMetrics();
		@SuppressWarnings("unchecked")
		final Iterator<String> lineIterator = lines.iterator();
		while (lineIterator.hasNext()) {
			line = lineIterator.next();
			if (line.length() != 0) {
				int textOffset = 0;
				if (textWidth != SWT.DEFAULT) {
					if (alignment == SWT.RIGHT)
						textOffset = textWidth - 2 * padding
								- textExtent(line).x;
					else if (alignment == SWT.CENTER)
						textOffset = (textWidth - textExtent(line).x) / 2
								- padding;
				}
				sg2.drawString(line, textOffset, y, true);
			}
			y += fontMetrics.getHeight();
		}
		sg2.translate(-padding, -padding);
	}

	@Override
	protected void recomputeBounds() {
		final GC gc = new GC(Display.getDefault());

		final Point newBounds;
		if (isTextEmpty()) {
			newBounds = gc.stringExtent(" "); //$NON-NLS-1$
		} else {
			newBounds = calculateTextBounds(gc);
		}

		gc.dispose();

		double textOffset = 0;
		if (textWidth != SWT.DEFAULT) {
			if (alignment == SWT.RIGHT) {
				textOffset = textWidth - 2 * padding - newBounds.x;
			} else if (alignment == SWT.CENTER) {
				textOffset = (textWidth - newBounds.x) / 2 - padding;
			}
		}
		setBounds(translateX + textOffset, translateY, newBounds.x + 2
				* DEFAULT_PADDING, newBounds.y + 2 * DEFAULT_PADDING);
	}

	/**
	 * Calculates the bounds of the text in the box as measured by the given
	 * graphics context and font metrics.
	 *
	 * @param gc
	 *            graphics context from which the measurements are done
	 * @return point representing the dimensions of the text's bounds
	 */
	private Point calculateTextBounds(final GC gc) {
		final SWTGraphics2D g2 = new SWTGraphics2D(gc, gc.getDevice());
		g2.setFont(font);
		final FontMetrics fm = g2.getSWTFontMetrics();
		final Point textBounds = new Point(0, 0);
		boolean firstLine = true;
		@SuppressWarnings("unchecked")
		final Iterator<String> lineIterator = lines.iterator();
		while (lineIterator.hasNext()) {
			String line = lineIterator.next();
			Point lineBounds = gc.stringExtent(line);
			if (firstLine) {
				textBounds.x = lineBounds.x;
				textBounds.y += fm.getAscent() + fm.getDescent()
						+ fm.getLeading();
				firstLine = false;
			} else {
				textBounds.x = Math.max(lineBounds.x, textBounds.x);
				textBounds.y += fm.getHeight();
			}
		}
		return textBounds;
	}

	/**
	 * Determines if this node's text is essentially empty.
	 *
	 * @return true if the text is the empty string
	 */
	private boolean isTextEmpty() {
		return lines.isEmpty() || lines.size() == 1
				&& ((String) lines.get(0)).length() == 0;
	}

	public int getOffsetAtLocation(double x, double y) {
		int sx = -1;
		int ya = 0;
		int offset = 0;
		StringTokenizer st = new StringTokenizer(getText(), "\n\r", true); //$NON-NLS-1$
		loop: while (st.hasMoreTokens()) {
			String line = st.nextToken();
			if (!"\n".equals(line) && !"\r".equals(line)) { //$NON-NLS-1$ //$NON-NLS-2$
				Point tx = textExtent(line);
				int textOffset = -padding;
				if (textWidth != SWT.DEFAULT) {
					if (alignment == SWT.RIGHT)
						textOffset += (textWidth - 2 * padding - tx.x);
					else if (alignment == SWT.CENTER)
						textOffset += (textWidth - tx.x) / 2 - padding;
				}
				if (y >= ya && y < ya + tx.y) {
					StringBuilder sb = new StringBuilder(line);
					while (sb.length() > 0) {
						int p = textExtent(sb.toString()).x;
						if (p + textOffset < x) {
							sx = offset + sb.length();
							break loop;
						}
						sb.setLength(sb.length() - 1);
					}
				}
				ya += tx.y;
			}
			offset += line.length();
		}
		return sx;
	}

	public Point getLocationAtOffset(int offset) {
		Point result = new Point(padding,padding);
		int la = 0;
		String str = getText();
		StringTokenizer st = new StringTokenizer(str, "\n\r", true); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			int lb = la + line.length();
			if (!"\n".equals(line) && !"\r".equals(line)) { //$NON-NLS-1$ //$NON-NLS-2$
				if (offset >= la && offset <= lb) {
					String linesAbove = str.substring(0, la);
					if (linesAbove.length() > 0) {
						Point tx = textExtent(linesAbove);
						result.y += tx.y;
					}
					int textOffset = 0;
					if (textWidth != SWT.DEFAULT) {
						if (alignment == SWT.RIGHT)
							textOffset = (textWidth - 2 * padding - textExtent(line).x);
						else if (alignment == SWT.CENTER)
							textOffset = (textWidth  - textExtent(line).x) / 2 - padding;
					}
					result.x += textOffset;
					result.x += textExtent(line.substring(0, offset-la)).x;
					return result;
				}
			}
			la = lb;
		}
		return null;
	}

	public Point textExtent(String text) {
		final GC gc = new GC(Display.getDefault());
		final SWTGraphics2D g2 = new SWTGraphics2D(gc, Display.getDefault());
		g2.setFont(font);
		final FontMetrics fm = g2.getSWTFontMetrics();
		final Point textBounds = new Point(0, fm.getAscent() + fm.getDescent()
				+ fm.getLeading());

		boolean firstLine = true;
		StringTokenizer st = new StringTokenizer(text, "\n\r"); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			Point lineBounds = gc.textExtent(line);
			if (firstLine) {
				textBounds.x = lineBounds.x;
				firstLine = false;
			} else {
				textBounds.x = Math.max(lineBounds.x, textBounds.x);
				textBounds.y += fm.getHeight();
			}
		}
		gc.dispose();
		return textBounds;
	}

	public boolean isGreek() {
		return isGreek;
	}

	/**
	 * @return the alignment
	 */
	public int getAlignment() {
		return alignment;
	}

	/**
	 * @param alignment
	 *            the alignment to set
	 */
	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	/**
	 * @return the textWidth
	 */
	public int getTextWidth() {
		return textWidth;
	}

	/**
	 * @param textWidth
	 *            the textWidth to set
	 */
	public void setTextWidth(int textWidth) {
		this.textWidth = textWidth;
	}

	public int getLineOffsetAt(int y) {
		if (y < 0 || y >= lines.size())
			return -1;
		int textOffset = 0;
		if (textWidth != SWT.DEFAULT) {
			if (alignment == SWT.RIGHT)
				textOffset = textWidth - 2 * padding
						- textExtent((String) lines.get(y)).x;
			else if (alignment == SWT.CENTER)
				textOffset = (textWidth - textExtent((String) lines.get(y)).x)
						/ 2 - padding;
		}
		return textOffset;
	}

}