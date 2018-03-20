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

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.piccolo2d.PCamera;
import org.piccolo2d.extras.swt.PSWTCanvas;

import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

public final class InertiaMouseWheelListener implements MouseWheelListener, IPreferenceChangeListener {
	private double nonlinearity = 0.33333333d;
	private double currentSpeed;
	private double minScale = 0.01d;
	private double maxScale = 100d;
	private double acceleration = 0.03d;
	private double lag = 0.8d;
	private double sensitivity = 0.5d;
	Point2D pntSrc = new Point();
	private int softness;
	private ScheduledFuture<?> wheelTask;

	public InertiaMouseWheelListener() {
		setSoftness();
		InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID).addPreferenceChangeListener(this);
	}

	private void setSoftness() {
		softness = UiActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.WHEELSOFTNESS);
		lag = softness * 0.003d + 0.65d;
		if (softness == 0)
			cancel();
	}

	public double getMinScale() {
		return minScale;
	}

	public void setMinScale(double minScale) {
		this.minScale = minScale;
	}

	public double getMaxScale() {
		return maxScale;
	}

	public void setMaxScale(double maxScale) {
		this.maxScale = maxScale;
	}

	public void cancel() {
		if (wheelTask != null) {
			wheelTask.cancel(true);
			wheelTask = null;
		}
		currentSpeed = 0d;
	}

	public void mouseScrolled(final MouseEvent e) {
		if (softness == 0) {
			currentSpeed = e.count;
			performWheelAction(e);
		} else {
			currentSpeed += sensitivity * e.count * Math.pow(Math.abs(e.count), nonlinearity);
			if (wheelTask == null && currentSpeed != 0) {
				wheelTask = UiActivator.getScheduledExecutorService().scheduleAtFixedRate(() -> {
					if (e.display.isDisposed())
						currentSpeed = 0;
					else {
						e.display.syncExec(() -> performWheelAction(e));
						currentSpeed = currentSpeed * lag;
					}
					if (Math.abs(currentSpeed) < lag)
						InertiaMouseWheelListener.this.cancel();
				}, 0L, 60L, TimeUnit.MILLISECONDS);
			}
		}
	}

	private void performWheelAction(final MouseEvent e) {
		PSWTCanvas canvas = (PSWTCanvas) e.widget;
		if (!canvas.isDisposed()) {
			PCamera camera = canvas.getCamera();
			double scaleDelta = (1d + (acceleration * currentSpeed));
			double newScale = scaleDelta * camera.getViewScale();
			if (newScale >= minScale && newScale <= maxScale) {
				pntSrc.setLocation(e.x, e.y);
				Point2D pntDst = camera.localToView(pntSrc);
				camera.scaleViewAboutPoint(scaleDelta, pntDst.getX(), pntDst.getY());
			}
		}
	}

	public void dispose() {
		cancel();
		InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID).removePreferenceChangeListener(this);
	}

	public void preferenceChange(PreferenceChangeEvent event) {
		if (event.getKey().equals(PreferenceConstants.WHEELSOFTNESS))
			setSoftness();
	}

}