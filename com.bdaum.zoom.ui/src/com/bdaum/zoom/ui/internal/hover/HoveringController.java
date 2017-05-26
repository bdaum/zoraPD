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

package com.bdaum.zoom.ui.internal.hover;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolTip;

import com.bdaum.zoom.batch.internal.Daemon;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.views.IHoverSubject;
import com.bdaum.zoom.ui.internal.views.ImageRegion;

/**
 * A text hovering controller. The controller registers with the text widget's
 * control as a <code>MouseTrackListener<code>. When
 * receiving a mouse hover event, it opens a popup window using the
 * appropriate <code>IGalleryHover</code> to initialize the window's display
 * information. The controller closes the window if the mouse pointer leaves the
 * area for which the display information has been computed.
 * <p>
 */
@SuppressWarnings("restriction")
public class HoveringController extends MouseTrackAdapter {

	private static final int MINDURATION = 1000;
	private static final int ADDITIONALHOVERTIMEPERCHARACTER = 25;

	/**
	 * The window closer.
	 */
	class WindowCloser extends MouseTrackAdapter
			implements MouseListener, MouseMoveListener, ControlListener, KeyListener, FocusListener {

		private Object fCoveredObject;
		private ImageRegion[] fCoveredRegions;
		private Control control;

		/**
		 * Creates a new window closer for the given area.
		 */
		public WindowCloser(Control control, Object coveredObject, ImageRegion[] coveredRegions) {
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
			control.addMouseListener(this);
			control.addMouseMoveListener(this);
			control.addMouseTrackListener(this);
			control.addControlListener(this);
			control.addKeyListener(this);
			control.addFocusListener(this);
			toolTip.setVisible(true);
			if (MINDURATION > 0) {
				final int msecs = MINDURATION + (toolTip.getMessage().length() + toolTip.getText().length())
						* ADDITIONALHOVERTIMEPERCHARACTER;
				new Daemon(Messages.HoveringController_hover_control, -1L) {
					@Override
					protected void doRun(IProgressMonitor monitor) {
						if (!control.isDisposed())
							control.getDisplay().asyncExec(new Runnable() {
								public void run() {
									if (!control.isDisposed())
										stop();
								}
							});

					}
				}.schedule(msecs);
			}
		}

		/**
		 * Closes the popup window and stops watching.
		 */
		void stop() {
			control.removeMouseListener(this);
			control.removeMouseMoveListener(this);
			control.removeMouseTrackListener(this);
			control.removeControlListener(this);
			control.removeKeyListener(this);
			control.removeFocusListener(this);
			install();
			toolTip.setVisible(false);
		}

		/*
		 * @see MouseMoveListener#mouseMove
		 */

		public void mouseMove(MouseEvent event) {
			Object foundItem = subject.findObject(event);
			ImageRegion[] foundRegions = subject.findAllRegions(event);
			if (foundItem != fCoveredObject || !Arrays.equals(foundRegions, fCoveredRegions))
				stop();
		}

		/*
		 * @see MouseListener#mouseUp(MouseEvent)
		 */

		public void mouseUp(MouseEvent event) {
			// do nothing
		}

		/*
		 * @see MouseListener#mouseDown(MouseEvent)
		 */

		public void mouseDown(MouseEvent event) {
			stop();
		}

		/*
		 * @see MouseListener#mouseDoubleClick(MouseEvent)
		 */

		public void mouseDoubleClick(MouseEvent event) {
			stop();
		}

		/*
		 * @see MouseTrackAdapter#mouseExit(MouseEvent)
		 */

		@Override
		public void mouseExit(MouseEvent event) {
			stop();
		}

		/*
		 * @see ControlListener#controlResized(ControlEvent)
		 */

		public void controlResized(ControlEvent event) {
			stop();
		}

		/*
		 * @see ControlListener#controlMoved(ControlEvent)
		 */

		public void controlMoved(ControlEvent event) {
			stop();
		}

		/*
		 * @see KeyListener#keyReleased(KeyEvent)
		 */

		public void keyReleased(KeyEvent event) {
			// do nothing
		}

		/*
		 * @see KeyListener#keyPressed(KeyEvent)
		 */

		public void keyPressed(KeyEvent event) {
			stop();
		}

		/*
		 * @see FocusListener#focusLost(FocusEvent)
		 */

		public void focusLost(FocusEvent event) {
			if (subject.getControl() == event.widget) {
				event.display.asyncExec(new Runnable() {

					public void run() {
						stop();
					}
				});
			}
		}

		/*
		 * @see FocusListener#focusGained(FocusEvent)
		 */

		public void focusGained(FocusEvent event) {
			// do nothing
		}
	}

	/** The popup window shell */
	// Shell fWindowShell;
	/** The label shown in the popup window shell */

	// private Canvas fWindowLabel;
	/** Remembers the previous mouse hover location */
	private Object fHoverObject = null;

	/** The hover information * */
	private IHoverInfo info;

	private IHoverSubject subject;

	private ToolTip toolTip;

	/**
	 * Creates a new hovering controller for the given hovering subject. The
	 * controller registers as mouse listener on the subject. Initially, the
	 * popup window is invisible.
	 *
	 * @param subject
	 *            the subject for which the controller is created. Must
	 *            implement IHoverSubject
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
			control.addMouseTrackListener(this);
		else {
			Control[] controls = subject.getControls();
			for (Control c : controls)
				c.addMouseTrackListener(this);
		}
	}

	/*
	 * @see MouseTrackAdapter#mouseHover
	 */

	@Override
	public void mouseHover(MouseEvent event) {
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

	/**
	 * Opens the hover popup window at the specified location. The window closes
	 * if the mouse pointer leaves the specified area.
	 *
	 * @param control
	 *            the control on which the hover controller was installed
	 * @param coveredArea
	 *            the area about which the hover popup window presents
	 *            information
	 * @param coveredArea
	 *            a list of annotated subregions
	 * @param location
	 *            the location of the hover popup window will pop up
	 * @param control 
	 */
	private void showWindow( Control control, Object coveredObject, ImageRegion[] coveredRegions, Point location) {
		toolTip.setText(info.getTitle());
		toolTip.setMessage(info.getText());
		toolTip.setLocation(location);
		new WindowCloser(control, coveredObject, coveredRegions).start();
		uninstall();
	}

	/**
	 * Uninstalls this hovering controller from its text viewer.
	 */
	public void uninstall() {
		Control canvas = subject.getControl();
		if (canvas != null && !canvas.isDisposed())
			canvas.removeMouseTrackListener(this);
	}

}