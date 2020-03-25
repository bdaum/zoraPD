/*******************************************************************************
 * Copyright (c) 2010 Ugo Sangiorgi and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Ugo Sangiorgi <ugo.sangiorgi@gmail.com> - Initial contribution
 *  Daoen Pan <http://code.google.com/u/gr8vyguy/> - Original code
 *  Berthold Daum - Updated for Eclipse 4
 *******************************************************************************/
package org.ugosan.eclipse.fullscreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.BundleContext;

import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.ZUiPlugin;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

public class FullScreenActivator extends ZUiPlugin {

	public static final String ID = "org.ugosan.eclipse.fullscreen"; //$NON-NLS-1$
	// public static final String HIDE_MENU_BAR = "hide_menu_bar"; //$NON-NLS-1$
	// public static final String HIDE_STATUS_BAR = "hide_status_bar";
	// //$NON-NLS-1$

	private static FullScreenActivator INSTANCE;
	private Map<Shell, List<Control>> controlLists;
	private Map<Shell, Menu> menuBars;

	public static FullScreenActivator getDefault() {
		return INSTANCE;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		controlLists = new HashMap<>();
		menuBars = new HashMap<>();
		INSTANCE = this;
	}

	public void stop(BundleContext context) throws Exception {
		INSTANCE = null;
		controlLists.clear();
		controlLists = null;
		menuBars.clear();
		menuBars = null;
		super.stop(context);
	}

	/**
	 * Set the workbench window full screen state.
	 *
	 * @param window
	 *            the workbench window
	 * @param fullScreen
	 *            new full screen state
	 */
	public void setFullScreen(Shell mainShell, boolean fullScreen) {
		if (mainShell == null || mainShell.isDisposed())
			return;

		if (fullScreen) {
			List<Control> controls = hideTrimControls(mainShell);
			controlLists.put(mainShell, controls);
			if (getHideMenuBar()) {
				Menu menuBar = mainShell.getMenuBar();
				mainShell.setMenuBar(null);
				menuBars.put(mainShell, menuBar);
			}

		} else {
			showTrimControls(mainShell);
			controlLists.remove(mainShell);
			Menu menuBar = mainShell.getMenuBar();
			if (menuBar == null) {
				menuBar = menuBars.get(mainShell);
				mainShell.setMenuBar(menuBar);
				menuBars.remove(mainShell);
			}
		}

		mainShell.setFullScreen(fullScreen);
		mainShell.layout();
	}

	private void showTrimControls(Shell mainShell) {
		List<Control> controls = controlLists.get(mainShell);
		if (controls != null)
			for (int i = 0; i < controls.size(); i++)
				controls.get(i).setVisible(true);
	}

	private static List<Control> hideTrimControls(Shell mainShell) {
		List<Control> controls = new ArrayList<>();
		Control[] children = mainShell.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control child = children[i];
			if (child.isDisposed() || !child.isVisible())
				continue;
			if (child instanceof Canvas)
				continue;
			if (child instanceof Composite) {
				Layout layout = ((Composite) child).getLayout();
				boolean hiddem = getHideStatusBar() && layout != null
						&& layout.getClass().toString().contains("TrimBar"); //$NON-NLS-1$
				child.setVisible(!hiddem);
				controls.add(child);
				continue;
			}
			// org.eclipse.jface.action.StatusLine is an internal class
			// the only way to hide it is by getting its name in string form
			// TODO: find a more elegant way to do fetch the status line
			// if (!
			//
			// getHideStatusBar() &&
			// child.getClass().toString().contains("StatusLine")) {
			// //$NON-NLS-1$
			// child.setVisible(true);
			// } else {
			// child.setVisible(false);
			// }
			// controls.add(child);
		}
		return controls;

	}

	private static boolean getHideMenuBar() {
		// Preferences preferences = Platform.getPreferencesService()
		// .getRootNode().node(InstanceScope.SCOPE).node(ID);
		// return preferences.getBoolean(HIDE_MENU_BAR, false);
		return UiActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.HIDE_MENU_BAR);
	}

	private static boolean getHideStatusBar() {
		return UiActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.HIDE_STATUS_BAR);
		// Preferences preferences = Platform.getPreferencesService()
		// .getRootNode().node(InstanceScope.SCOPE).node(ID);
		// return preferences.getBoolean(HIDE_STATUS_BAR, false);
	}
}
