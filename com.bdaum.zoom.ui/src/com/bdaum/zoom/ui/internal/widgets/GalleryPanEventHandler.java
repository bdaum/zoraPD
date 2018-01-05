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

import java.awt.geom.Point2D;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.piccolo2d.PCamera;
import org.piccolo2d.PNode;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PInputEventListener;
import org.piccolo2d.event.PPanEventHandler;
import org.piccolo2d.extras.swt.PSWTText;
import org.piccolo2d.util.PBounds;
import org.piccolo2d.util.PDimension;

import com.bdaum.zoom.ui.internal.IPresentationHandler;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

public class GalleryPanEventHandler extends PPanEventHandler implements
		PInputEventListener, IPreferenceChangeListener {

	private static final double PAN_SENSITIVITY = 0.2d;
	private static final double ACCEL = 10d;
	private double speed = PAN_SENSITIVITY;
	public static final int HOR = 1;
	public static final int VER = 2;
	public static final int BOTH = 3;

	private PNode[] workarea;
	private final int dir;
	private int forcePanMask = 0;
	private final IPresentationHandler presentationHandler;
	private int left;
	private int top;
	private int right;
	private int bottom;
	private final double speedOffset;

	/**
	 * @param animatedGallery
	 */
	public GalleryPanEventHandler(IPresentationHandler presentationHandler,
			PNode[] workarea, int left, int top, int right, int bottom,
			int dir, int forcePanMask, double speedOffset) {
		this.presentationHandler = presentationHandler;
		this.workarea = workarea;
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
		this.dir = dir;
		this.forcePanMask = forcePanMask;
		this.speedOffset = speedOffset;
		setMouseSpeed();
		InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID)
				.addPreferenceChangeListener(this);

	}

	private void setMouseSpeed() {
		speed = PAN_SENSITIVITY
				* Math.pow(
						2d,
						(Platform.getPreferencesService().getInt(
								UiActivator.PLUGIN_ID,
								PreferenceConstants.MOUSE_SPEED, 10, null) + speedOffset) / 3d);
	}

	@Override
	protected void pan(PInputEvent e) {
		if ((e.getModifiers() & forcePanMask) == 0) {
			PNode pickedNode = e.getPickedNode();
			if (pickedNode instanceof PSWTText
					&& pickedNode.getParent() instanceof TextField)
				return;
			for (PNode node : workarea)
				if (pickedNode.getParent() == node)
					return;
		}
		PDimension d = e.getDelta();
		boolean ctrl = e.isControlDown();
		boolean shft = e.isShiftDown();
		double deltaX = ((dir & HOR) != 0 || (ctrl && shft)) ? d.width * speed
				: 0;
		double deltaY = ((dir & VER) != 0 || (ctrl && shft)) ? d.height * speed
				: 0;
		if (ctrl && !shft) {
			deltaX *= ACCEL;
			deltaY *= ACCEL;
		}
		PCamera camera = e.getCamera();
		PBounds viewBounds = camera.getViewBounds();
		double x = viewBounds.getX() - deltaX;
		if (x < left && deltaX > 0)
			return;
		if (x + viewBounds.getWidth() > right && deltaX < 0)
			return;
		double y = viewBounds.getY() - deltaY;
		if (y < top && deltaY > 0)
			return;
		if (y + viewBounds.getHeight() > bottom && deltaY < 0)
			return;
		Point2D l = e.getPosition();
		if (camera.getViewBounds().contains(l)) {
			camera.translateView(deltaX, deltaY);
			if (presentationHandler != null)
				presentationHandler.resetTransform();
		}
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(PreferenceConstants.MOUSE_SPEED))
			setMouseSpeed();
	}

	public void dispose() {
		InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID)
				.removePreferenceChangeListener(this);
	}

	/**
	 * @param surfaceBounds
	 *            the surfaceBounds to set
	 */
	public void setSurfaceBounds(int left, int top, int right, int bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
}