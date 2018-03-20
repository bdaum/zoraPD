/*******************************************************************************
 * Copyright (c) 2009, 2011 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.rcp.internal;

import java.util.GregorianCalendar;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.BasicSplashHandler;
import org.osgi.framework.Version;

import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;

/**
 * Parses the well known product constants and constructs a splash handler
 * accordingly.
 */
@SuppressWarnings("restriction")
public class ZoomSplashHandler extends BasicSplashHandler {

	private Font font;
	private Color bgColor;

	@Override
	public void init(Shell splash) {
		super.init(splash);
		final boolean traymode = Platform.getPreferencesService().getBoolean(UiActivator.PLUGIN_ID,
				PreferenceConstants.TRAY_MODE, false, null);
		IProduct product = Platform.getProduct();
		Version version = (product != null) ? product.getDefiningBundle().getVersion() : null;
		setProgressRect(new Rectangle(1, 314, 448, 4));
		final String versionString = NLS.bind(Messages.getString("ZoomSplashHandler.version"), version); //$NON-NLS-1$
		final String copyrightString = NLS.bind(Messages.getString("ZoomSplashHandler.copyright"), //$NON-NLS-1$
				new GregorianCalendar().get(GregorianCalendar.YEAR));
		FontData fontData = splash.getDisplay().getSystemFont().getFontData()[0];
		fontData.setHeight(7);
		font = new Font(splash.getDisplay(), fontData);
		bgColor = new Color(splash.getDisplay(), 37, 37, 37);
		getContent().addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (traymode)
					getSplash().setVisible(false);
				else {
					e.gc.setFont(font);
					e.gc.setBackground(bgColor);
					e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_GRAY));
					e.gc.drawText(versionString, 335, 302, true);
					e.gc.drawText(copyrightString, 5, 291, false);
					if (System.currentTimeMillis() % 7 == 0) {
						for (String arg : Platform.getCommandLineArgs())
							if ("-noBirdie".equals(arg)) //$NON-NLS-1$
								return;
						Image birdie = RcpActivator.getImageDescriptor("icons/intro/birdie.png").createImage(e.display); //$NON-NLS-1$
						e.gc.drawImage(birdie, 30, 30);
						birdie.dispose();
					}
				}
			}
		});
	}

	@Override
	public void dispose() {
		if (bgColor != null)
			bgColor.dispose();
		if (font != null)
			font.dispose();
		super.dispose();
	}

}
