/*
 * Copyright (c) 2008-2010, Piccolo2D project, http://piccolo2d.org
 * Copyright (c) 1998-2008, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * None of the name of the University of Maryland, the name of the Piccolo2D project, or the names of its
 * contributors may be used to endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.bdaum.zoom.ui.internal.widgets;

import java.awt.event.InputEvent;
import java.awt.geom.Point2D;

import org.piccolo2d.PCamera;
import org.piccolo2d.event.PDragSequenceEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PInputEventFilter;

/**
 * <b>ZoomEventhandler</b> provides event handlers for basic zooming of the
 * canvas view with the right (third) button. The interaction is that the
 * initial mouse press defines the zoom anchor point, and then moving the mouse
 * to the right zooms with a speed proportional to the amount the mouse is moved
 * to the right of the anchor point. Similarly, if the mouse is moved to the
 * left, the the view is zoomed out.
 * <P>
 * On a Mac with its single mouse button one may wish to change the standard
 * right mouse button zooming behavior. This can be easily done with the
 * PInputEventFilter. For example to zoom with button one and shift you would do
 * this:
 * <P>
 * <code>
 * <pre>
 * zoomEventHandler.getEventFilter().setAndMask(InputEvent.BUTTON1_MASK |
 *                                              InputEvent.SHIFT_MASK);
 * </pre>
 * </code>
 * <P>
 *
 * @version 1.0
 * @author Jesse Grosjean
 */
public class ZZoomEventHandler extends PDragSequenceEventHandler {

	/**
	 * A constant used to adjust how sensitive the zooming will be to mouse
	 * movement. The larger the number, the more each delta pixel will affect
	 * zooming.
	 */
	private static final double ZOOM_SENSITIVITY = 0.001;
	private double speed = ZOOM_SENSITIVITY;
	private double minScale = 0;
	private double maxScale = Double.MAX_VALUE;
	private Point2D viewZoomPoint;
	private long lastActivityTime;

	/**
	 * Creates a new zoom handler.
	 */
	public ZZoomEventHandler() {
		super();
		setEventFilter(new PInputEventFilter(InputEvent.BUTTON3_MASK));
	}

	// ****************************************************************
	// Zooming
	// ****************************************************************

	/**
	 * Returns the minimum view magnification factor that this event handler is
	 * bound by. The default is 0.
	 *
	 * @return the minimum camera view scale
	 */
	public double getMinScale() {
		return minScale;
	}

	/**
	 * Sets the minimum view magnification factor that this event handler is bound
	 * by. The camera is left at its current scale even if <code>minScale</code> is
	 * larger than the current scale.
	 *
	 * @param minScale
	 *            the minimum scale, must not be negative.
	 */
	public void setMinScale(final double minScale) {
		this.minScale = minScale;
	}

	/**
	 * Returns the maximum view magnification factor that this event handler is
	 * bound by. The default is Double.MAX_VALUE.
	 *
	 * @return the maximum camera view scale
	 */
	public double getMaxScale() {
		return maxScale;
	}

	/**
	 * Sets the speed for zooming
	 * 
	 * @param speed
	 *            -10 ... 0 ... +10
	 */
	public void setSpeed(double speed) {
		this.speed = ZOOM_SENSITIVITY * Math.pow(2d, speed / 3d);
	}

	/**
	 * Sets the maximum view magnification factor that this event handler is bound
	 * by. The camera is left at its current scale even if <code>maxScale</code> is
	 * smaller than the current scale. Use Double.MAX_VALUE to specify the largest
	 * possible scale.
	 *
	 * @param maxScale
	 *            the maximum scale, must not be negative.
	 */
	public void setMaxScale(final double maxScale) {
		this.maxScale = maxScale;
	}

	/**
	 * Records the start point of the zoom. Used when calculating the delta for zoom
	 * speed.
	 *
	 * @param event
	 *            event responsible for starting the zoom interaction
	 */
	@Override
	protected void dragActivityFirstStep(final PInputEvent event) {
		lastActivityTime = event.getWhen() & 0xFFFFFFFFL;
		viewZoomPoint = event.getPosition();
		super.dragActivityFirstStep(event);
	}

	/**
	 * Updates the current zoom periodically, regardless of whether the mouse has
	 * moved recently.
	 *
	 * @param event
	 *            contains information about the current state of the mouse
	 */
	@Override
	protected void dragActivityStep(final PInputEvent event) {
		long when = event.getWhen() & 0xFFFFFFFFL;
		if (when == lastActivityTime)
			return;
		lastActivityTime = when;
		final PCamera camera = event.getCamera();
		Point2D lastMousePoint = getMousePressedCanvasPoint();
		double scaleDelta = 1.0 + speed * (event.getCanvasPosition().getX() - lastMousePoint.getX());
		lastMousePoint.setLocation(event.getCanvasPosition());
		final double currentScale = camera.getViewScale();
		final double newScale = currentScale * scaleDelta;
		if (newScale < minScale)
			scaleDelta = minScale / currentScale;
		if (maxScale > 0 && newScale > maxScale)
			scaleDelta = maxScale / currentScale;
		camera.scaleViewAboutPoint(scaleDelta, viewZoomPoint.getX(), viewZoomPoint.getY());
	}
}
