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
 * (c) 2009-2015 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.widgets;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;

import org.eclipse.swt.SWT;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;

public class TextEventHandler {
	private static final int MINDRAGEVENTS = 2;
	private TextField focus;
	private Color selectedBgColor;
	private int dragged = 0;
	private double startX;
	private double startY;
	private final TraverseHandler traverseHander;

	public TextEventHandler(Color selectedBgColor) {
		this(selectedBgColor, null);
	}

	public TextEventHandler(Color selectedBgColor, TraverseHandler traverseHander) {
		this.selectedBgColor = selectedBgColor;
		this.traverseHander = traverseHander;
	}

	public void keyPressed(PInputEvent event) {
		if (focus != null) {
			switch (event.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				textFocusLost(false);
				break;
			case 13:
				if ((focus.getStyle() & SWT.SINGLE) != 0)
					textFocusLost(true);
				// break;
				//$FALL-THROUGH$
			default:
				if (traverseHander != null && event.getKeyChar() == SWT.TAB) {
					textFocusLost(true);
					traverseHander.traverse(SWT.TAB, event.isShiftDown() ? SWT.SHIFT : 0);
				} else
					focus.keyPressed(event);
				break;
			}
		}
	}

	public void mouseDragged(PInputEvent event) {
		if (focus == event.getPickedNode().getParent()) {
			if (dragged == 0) {
				Point2D position = event.getPositionRelativeTo(focus);
				PDimension delta = event.getDeltaRelativeTo(focus);
				startX = position.getX() - delta.getWidth();
				startY = position.getY() - delta.getHeight();
			}
			if (++dragged > MINDRAGEVENTS)
				processMouseDrag(event);
		}
	}

	public void mouseReleased(PInputEvent event) {
		if (dragged > MINDRAGEVENTS) {
			processMouseDrag(event);
			dragged = 0;
			return;
		}
		dragged = 0;
		if (event.getPickedNode() != null) {
			PNode field = event.getPickedNode().getParent();
			if (event.isLeftMouseButton()) {
				if (field instanceof TextField) {
					processTextEvent(event);
					return;
				}
			} else if (event.isRightMouseButton()
					&& (field instanceof TextField))
				return;
		}
		textFocusLost(true);
	}

	private void processMouseDrag(PInputEvent event) {
		if (focus == event.getPickedNode().getParent()) {
			focus.mouseDragged(startX, startY,
					event.getPositionRelativeTo(focus));
		}
	}

	private void processTextEvent(PInputEvent event) {
		if (focus == event.getPickedNode().getParent())
			focus.mouseReleased(event);
		else {
			textFocusLost(true);
			focus = (TextField) event.getPickedNode().getParent();
			toFront(focus);
			focus.setSelectedBgColor(selectedBgColor);
			focus.setPenColor(focus.getPenColor());
			focus.setFocus(true);
		}
	}

	private static void toFront(PNode node) {
		while (node != null) {
			node.moveToFront();
			node = node.getParent();
		}
	}

	private void textFocusLost(boolean commit) {
		dragged = 0;
		if (focus != null) {
			focus.setFocus(false);
			focus.setPenColor(focus.getPenColor());
			PNode parent = focus.getParent();
			if (commit) {
				while (!(parent instanceof PTextHandler) && parent != null)
					parent = parent.getParent();
				if (parent instanceof PTextHandler)
					((PTextHandler) parent).processTextEvent(focus);
			}
			focus = null;
		}
	}

	public boolean hasFocus() {
		return focus != null;
	}

	public void commit() {
		textFocusLost(true);
	}

	public void setFocus(TextField field) {
		focus = field;
	}

}
