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

package com.bdaum.zoom.ui.internal.hover;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolTip;

import com.bdaum.zoom.batch.internal.Daemon;
import com.bdaum.zoom.common.CommonUtilities;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.views.IHoverSubject;
import com.bdaum.zoom.ui.internal.views.ImageRegion;

/**
 * A hovering controller. The controller registers with its subject as a
 * <code>MouseTrackListener<code>. When
 * receiving a mouse hover event, it opens a popup window using the
 * appropriate <code>IGalleryHover</code> to initialize the window's display
 * information. The controller closes the window if the mouse pointer leaves the
 * area for which the display information has been computed.
 * <p>
 */
@SuppressWarnings("restriction")
public class HoveringController implements Listener {

	class WindowManager implements Listener {

		private Object fCoveredObject;
		private ImageRegion[] fCoveredRegions;
		private Control control;
		private Daemon stopper;
		private Daemon delayer;

		/**
		 * Creates a new window manager for the given area.
		 */
		public WindowManager(Control control, Object coveredObject, ImageRegion[] coveredRegions) {
			this.control = control;
			fCoveredObject = coveredObject;
			if (coveredRegions != null) {
				fCoveredRegions = new ImageRegion[coveredRegions.length];
				for (int i = 0; i < coveredRegions.length; i++)
					fCoveredRegions[i] = coveredRegions[i];
			} else
				fCoveredRegions = null;
		}

		/**
		 * Starts watching whether the popup window must be closed.
		 */
		public void start() {
			control.addListener(SWT.MouseDown, this);
			control.addListener(SWT.MouseDoubleClick, this);
			control.addListener(SWT.MouseExit, this);
			control.addListener(SWT.MouseMove, this);
			control.addListener(SWT.Resize, this);
			control.addListener(SWT.Move, this);
			control.addListener(SWT.KeyDown, this);
			control.addListener(SWT.FocusOut, this);
			delayer = new Daemon(Messages.HoveringController_hover_control, -1L) {
				@Override
				protected void doRun(IProgressMonitor monitor) {
					delayer = null;
					if (!control.isDisposed())
						control.getDisplay().asyncExec(() -> {
							if (!control.isDisposed())
								show();
						});
				}
			};
			delayer.schedule(CommonUtilities.getHoverDelay());
		}

		protected void show() {
			toolTip.setVisible(true);
			stopper = new Daemon(Messages.HoveringController_hover_control, -1L) {
				@Override
				protected void doRun(IProgressMonitor monitor) {
					stopper = null;
					if (!control.isDisposed())
						control.getDisplay().asyncExec(() -> {
							if (!control.isDisposed())
								stop();
						});
				}
			};
			stopper.schedule(
					CommonUtilities.computeHoverTime(toolTip.getMessage().length() + toolTip.getText().length()));
		}

		/**
		 * Closes the popup window and stops watching.
		 */
		void stop() {
			if (delayer != null) {
				delayer.cancel();
				delayer = null;
			}
			if (stopper != null) {
				stopper.cancel();
				stopper = null;
			}
			control.removeListener(SWT.MouseDown, this);
			control.removeListener(SWT.MouseDoubleClick, this);
			control.removeListener(SWT.MouseExit, this);
			control.removeListener(SWT.MouseMove, this);
			control.removeListener(SWT.Resize, this);
			control.removeListener(SWT.Move, this);
			control.removeListener(SWT.KeyDown, this);
			control.removeListener(SWT.FocusOut, this);
			install();
			toolTip.setVisible(false);
		}

		@Override
		public void handleEvent(Event e) {
			switch (e.type) {
			case SWT.MouseDown:
			case SWT.MouseDoubleClick:
			case SWT.MouseExit:
			case SWT.KeyDown:
			case SWT.Resize:
			case SWT.Move:
				stop();
				break;
			case SWT.MouseMove:
				Object foundItem = subject.findObject(e);
				ImageRegion[] foundRegions = subject.findAllRegions(e);
				if (foundItem != fCoveredObject || !Arrays.equals(foundRegions, fCoveredRegions))
					stop();
				break;
			case SWT.FocusOut:
				if (subject.getControl() == e.widget) {
					e.display.asyncExec(() -> {
						if (!e.display.isDisposed())
							stop();
					});
				}
				break;
			}

		}
	}

	/** Remembers the previous mouse hover location */
	private Object fHoverObject = null;

	/** The hover information * */
	private IHoverInfo info;

	private IHoverSubject subject;

	private ToolTip toolTip;

	/**
	 * Creates a new hovering controller for the given hovering subject. The
	 * controller registers as mouse listener on the subject. Initially, the popup
	 * window is invisible.
	 *
	 * @param subject
	 *            the subject for which the controller is created. Must implement
	 *            IHoverSubject
	 */
	public HoveringController(IHoverSubject subject) {
		this.subject = subject;
		toolTip = new ToolTip(
				subject.getControl() != null ? subject.getControl().getShell() : subject.getControls()[0].getShell(),
				SWT.BALLOON);
		toolTip.setAutoHide(false);
	}

	/**
	 * Determines the location of the popup window depending on the size of the
	 * covered area and the coordinates at which the window has been requested.
	 * 
	 * @param control
	 *
	 * @param x
	 *            the x coordinate at which the window has been requested
	 * @param y
	 *            the y coordinate at which the window has been requested
	 * @param coveredArea
	 *            graphical area of the hover region
	 * @return the location of the hover popup window
	 */
	private static Point computeWindowLocation(Control control, int x, int y) {
		return control.toDisplay(x + 20, y + 10);
	}

	/**
	 * Disposes this hovering controller
	 */
	public void dispose() {
		if (toolTip != null)
			toolTip.dispose();
	}

	/**
	 * Installs this hovering controller on its viewer.
	 */
	public void install() {
		fHoverObject = null;
		Control control = subject.getControl();
		if (control != null)
			control.addListener(SWT.MouseHover, this);
		else
			for (Control c : subject.getControls())
				c.addListener(SWT.MouseHover, this);
	}

	/**
	 * Opens the hover popup window at the specified location. The window closes if
	 * the mouse pointer leaves the specified area.
	 *
	 * @param control
	 *            the control on which the hover controller was installed
	 * @param coveredArea
	 *            the area about which the hover popup window presents information
	 * @param coveredArea
	 *            a list of annotated subregions
	 * @param location
	 *            the location of the hover popup window will pop up
	 * @param control
	 */
	private void showWindow(Control control, Object coveredObject, ImageRegion[] coveredRegions, Point location) {
		String title = info.getTitle();
		if (title != null)
			toolTip.setText(title);
		toolTip.setMessage(info.getText());
		toolTip.setLocation(location);
		new WindowManager(control, coveredObject, coveredRegions).start();
		uninstall();
	}

	/**
	 * Uninstalls this hovering controller from its subject.
	 */
	public void uninstall() {
		Control control = subject.getControl();
		if (control != null)
			control.removeListener(SWT.MouseHover, this);
		else
			for (Control c : subject.getControls())
				c.removeListener(SWT.MouseHover, this);
	}

	@Override
	public void handleEvent(Event event) {
		if (UiActivator.getDefault().getShowHover()) {
			IGalleryHover hover = subject.getGalleryHover(event);
			if (hover != null) {
				info = hover.getHoverInfo(subject, event);
				if (info != null && !info.getObject().equals(fHoverObject)) {
					fHoverObject = info.getObject();
					if (toolTip != null && !toolTip.isDisposed())
						showWindow((Control) event.widget, fHoverObject, info.getRegions(),
								computeWindowLocation((Control) event.widget, event.x, event.y));
				}
			}
		}
	}

}