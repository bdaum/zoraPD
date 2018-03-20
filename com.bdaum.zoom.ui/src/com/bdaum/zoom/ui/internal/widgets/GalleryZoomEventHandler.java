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

import java.awt.event.InputEvent;
import java.awt.geom.Point2D;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.piccolo2d.PNode;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.event.PInputEventFilter;
import org.piccolo2d.event.PInputEventListener;

import com.bdaum.zoom.ui.internal.IPresentationHandler;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

public class GalleryZoomEventHandler extends ZZoomEventHandler
		implements PInputEventListener, IPreferenceChangeListener {
	/**
	 *
	 */
	private final PNode[] workarea;
	private Point2D zoomPoint;
	private final IPresentationHandler presentationHandler;
	private final double speedOffset;

	/**
	 * @param animatedGallery
	 */
	public GalleryZoomEventHandler(IPresentationHandler presentationHandler, PNode[] workarea, double speedOffset) {
		this.presentationHandler = presentationHandler;
		this.workarea = workarea;
		this.speedOffset = speedOffset;
		setMouseSpeed();
		setZoomkey();
		InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID).addPreferenceChangeListener(this);
	}

	private void setMouseSpeed() {
		setSpeed(Platform.getPreferencesService().getInt(UiActivator.PLUGIN_ID, PreferenceConstants.MOUSE_SPEED, 10,
				null) + speedOffset);
	}

	private void setZoomkey() {
		PInputEventFilter eventFilter = getEventFilter();
		switch (Platform.getPreferencesService().getInt(UiActivator.PLUGIN_ID, PreferenceConstants.ZOOMKEY,
				PreferenceConstants.ZOOMALT, null)) {
		case PreferenceConstants.ZOOMRIGHT:
			eventFilter.setAndMask(InputEvent.BUTTON3_MASK);
			eventFilter.setNotMask(0);
			break;
		case PreferenceConstants.ZOOMALT:
			eventFilter.setAndMask(InputEvent.ALT_MASK | InputEvent.BUTTON1_MASK);
			eventFilter.setNotMask(0);
			break;
		case PreferenceConstants.ZOOMSHIFT:
			eventFilter.setAndMask(InputEvent.SHIFT_MASK | InputEvent.BUTTON1_MASK);
			eventFilter.setNotMask(0);
			break;
		default:
			eventFilter.setAndMask(0);
			eventFilter.setNotMask(PInputEventFilter.ALL_MODIFIERS_MASK);
			break;
		}
	}

	@Override
	protected void startDrag(PInputEvent event) {
		PNode pickedParent = event.getPickedNode().getParent();
		for (PNode node : workarea)
			if (pickedParent.getParent() == node)
				return;
		super.startDrag(event);
	}

	@Override
	protected void dragActivityFirstStep(PInputEvent event) {
		zoomPoint = event.getPosition();
		super.dragActivityFirstStep(event);
	}

	@Override
	protected void dragActivityStep(PInputEvent event) {
		if (zoomPoint != null) {
			double dx = event.getCanvasPosition().getX() - getMousePressedCanvasPoint().getX();
			if (dx > 5 || dx < -5) {
				super.dragActivityStep(event);
				if (presentationHandler != null)
					presentationHandler.resetTransform();
			}
		}
	}

	public void dispose() {
		InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID).removePreferenceChangeListener(this);
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(PreferenceConstants.ZOOMKEY))
			setZoomkey();
		else if (event.getKey().equals(PreferenceConstants.MOUSE_SPEED))
			setMouseSpeed();
	}
}