/*******************************************************************************
 * Copyright (c) 2009, 2018 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum
 *******************************************************************************/
package com.bdaum.zoom.css.internal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import org.akrogen.tkui.css.core.css2.CSS2ColorHelper;
import org.akrogen.tkui.css.core.dom.properties.ICSSPropertyHandler;
import org.akrogen.tkui.css.core.dom.properties.ICSSPropertyHandlerProvider;
import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.core.impl.engine.AbstractCSSEngine;
import org.akrogen.tkui.css.swt.engine.CSSSWTEngineImpl;
import org.akrogen.tkui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;

import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.css.CSSProperties;

/**
 * The activator class controls the plug-in life cycle
 */
public class CssActivator extends AbstractUIPlugin {

	private static class CSSSWTApplyStylesListener {

		public CSSSWTApplyStylesListener(Display display, final CSSEngine engine) {
			display.addListener(SWT.Skin, new Listener() {
				public void handleEvent(Event event) {
					if (engine != null) {
						Widget widget = event.widget;
						if (widget instanceof Control) {
							Shell shell = ((Control) widget).getShell();
							if (shell.getData(CSSProperties.CSS) == null) 
								return;
							//TODO handle selected system dialogs
						}
						engine.applyStyles(widget, false);
						if (widget instanceof Composite)
							CssActivator.inheritBackground((Composite) widget);
					}
				}
			});
		}
	}

	private static final String JFACE = "JFace "; //$NON-NLS-1$

	public static final String PLUGIN_ID = "com.bdaum.zoom.css"; //$NON-NLS-1$

	public static final String DROPINFOLDER = "dropins"; //$NON-NLS-1$

	private static CssActivator plugin;

	private CSSSWTEngineImpl engine;

	private String theme;

	private ListenerList<IThemeListener> listeners = new ListenerList<IThemeListener>();

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext )
	 */

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext )
	 */

	@Override
	public void stop(BundleContext context) throws Exception {
		disposeEngine();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CssActivator getDefault() {
		return plugin;
	}

	public void setTheme(String theme) {
		this.theme = theme;
		if (theme != null) {
			if (engine != null)
				readStyleSheet(null, theme);
			fireThemeChanged();
		}
	}

	/**
	 * Informs listeners about a theme change
	 */
	public void fireThemeChanged() {
		Display current = Display.getCurrent();
		if (current != null) {
			Composite shell = current.getActiveShell();
			while (shell instanceof Shell) {
				setColors(shell);
				shell = shell.getParent();
			}
		}
		for (IThemeListener listener : listeners)
			listener.themeChanged();
	}

	public void setColors(Control control) {
		if (control != null) {
			if (control instanceof Shell)
				control.setData(CSSProperties.CSS, Boolean.TRUE);
			if (theme != null) {
				AbstractCSSEngine engine = getCssEngine(control.getDisplay());
				if (engine != null) {
					engine.applyStyles(control, true);
					if (control instanceof Composite)
						inheritBackground((Composite) control);
				}
			}
		}
	}

	public void addThemeListener(IThemeListener listener) {
		listeners.add(listener);
	}

	public void removeThemeListener(IThemeListener listener) {
		listeners.remove(listener);
	}

	public void updateStatusLine(IStatusLineManager statusLineManager) {
		statusLineManager.update(true);
		while (statusLineManager instanceof SubStatusLineManager)
			statusLineManager = (IStatusLineManager) ((SubStatusLineManager) statusLineManager).getParent();
		setColors(((StatusLineManager) statusLineManager).getControl());
	}

	public AbstractCSSEngine getCssEngine(Display display) {
		if (engine == null && display != null && !display.isDisposed())
			readStyleSheet(display, theme);
		return engine;
	}

	protected static void inheritBackground(Composite c) {
		if (c.getBackgroundMode() != SWT.INHERIT_FORCE) {
			c.setBackgroundMode(SWT.INHERIT_FORCE);
			for (Control child : c.getChildren())
				if (child instanceof Composite)
					inheritBackground((Composite) child);
		}
	}

	@SuppressWarnings("unused")
	private void readStyleSheet(Display display, String sheetname) {
		String filename = sheetname + ".css"; //$NON-NLS-1$
		try {
			File file = findCustomCSS(filename);
			if (!file.exists()) {
				try {
					file = FileLocator.findFile(getBundle(), "/$os$/css/" //$NON-NLS-1$
							+ filename);
					if (file == null) {
						disposeEngine();
						return;
					}
				} catch (URISyntaxException e) {
					// Should never happend
				}
			}
			if (engine == null)
				new CSSSWTApplyStylesListener(display, engine = new CSSSWTEngineImpl(display));
			else
				engine.reset();
			readJfaceColors(file);
			try (FileReader fileReader = new FileReader(file)) {
				engine.parseStyleSheet(fileReader);
			}
		} catch (IOException e) {
			getLog().log(new Status(IStatus.ERROR, PLUGIN_ID,
					NLS.bind(Messages.CssActivator_error_when_parsing, filename), e));
			disposeEngine();
		}
	}

	private static File findCustomCSS(String filename) {
		File installFolder = new File(Platform.getInstallLocation().getURL().getPath());
		File cssFile = new File(new File(installFolder, CssActivator.DROPINFOLDER), filename);
		if (cssFile.exists())
			return cssFile;
		return new File(new File(installFolder.getParent(), CssActivator.DROPINFOLDER), filename);
	}

	private void readJfaceColors(File file) {
		try (FileReader fileReader = new FileReader(file)) {
			char[] buf = new char[1024];
			int len = fileReader.read(buf);
			StringTokenizer st = new StringTokenizer(new String(buf, 0, len), "\n\r"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String line = st.nextToken();
				if (line.startsWith("/*")) //$NON-NLS-1$
					continue;
				if (line.startsWith(JFACE)) {
					ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
					int p = JFACE.length();
					int q = line.length();
					if (line.length() > p && line.charAt(p) == '{')
						++p;
					if (line.charAt(q - 1) == '}')
						--q;
					StringTokenizer st2 = new StringTokenizer(line.substring(p, q), ";"); //$NON-NLS-1$
					while (st2.hasMoreTokens()) {
						String token = st2.nextToken();
						p = token.indexOf(':');
						if (p > 0)
							colorRegistry.put(token.substring(0, p), convertToRGB(token.substring(p + 1)));
					}
					break;
				}
			}
		} catch (IOException e) {
			getLog().log(new Status(IStatus.ERROR, PLUGIN_ID,
					NLS.bind(Messages.CssActivator_error_when_parsing, file.getName()), e));
		}

	}

	private static RGB convertToRGB(String spec) {
		if (spec.startsWith("rgb(") && spec.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
			try {
				StringTokenizer st = new StringTokenizer(spec, "(,) "); //$NON-NLS-1$
				if (st.hasMoreTokens()) {
					st.nextToken();
					if (st.hasMoreTokens()) {
						int red = Integer.parseInt(st.nextToken());
						if (st.hasMoreTokens()) {
							int green = Integer.parseInt(st.nextToken());
							if (st.hasMoreTokens())
								return new RGB(red, green, Integer.parseInt(st.nextToken()));
						}
					}
				}
			} catch (NumberFormatException e) {
				// fall through
			}
		}
		RGBColor rgbColor = CSS2ColorHelper.getRGBColor(spec);
		return (rgbColor != null) ? CSSSWTColorHelper.getRGB(rgbColor) : null;
	}

	private void disposeEngine() {
		if (engine != null) {
			engine.dispose();
			engine = null;
		}
	}

	public void applyExtendedStyle(final Control control, final IBaseColorModel colorModel) {
		AbstractCSSEngine engine = getCssEngine(control.getDisplay());
		if (engine != null) {
			ICSSPropertyHandlerProvider handlerProvider = new ICSSPropertyHandlerProvider() {
				public CSSStyleDeclaration getDefaultCSSStyleDeclaration(CSSEngine engine, Object element,
						CSSStyleDeclaration newStyle, String pseudoE) throws Exception {
					return null;
				}

				public Collection<?> getCSSPropertyHandlers(String property) throws Exception {
					ArrayList<ICSSPropertyHandler> providers = new ArrayList<ICSSPropertyHandler>();
					providers.add(new ICSSPropertyHandler() {
						public String retrieveCSSProperty(Object element, String property, CSSEngine engine)
								throws Exception {
							return null;
						}

						public boolean applyCSSProperty(Object element, String property, CSSValue value, String pseudo,
								CSSEngine engine) throws Exception {
							if (colorModel.applyColorsTo(element)) {
								Display display = control.getDisplay();
								property = property.intern();
								if (colorModel instanceof IColumnLabelColorModel) {
									IColumnLabelColorModel colorModel0 = (IColumnLabelColorModel) colorModel;
									if (CSSProperties.SELECTEDBACKGROUNDCOLOR == property)
										colorModel0.setSelectedBackgroundColor(
												(Color) engine.convert(value, Color.class, display));
									else if (CSSProperties.SELECTEDFOREGROUNDCOLOR == property)
										colorModel0.setSelectedForegroundColor(
												(Color) engine.convert(value, Color.class, display));
									else if (CSSProperties.TOOLTIPBACKGROUNDCOLOR == property)
										colorModel0.setToolTipBackgroundColor(
												(Color) engine.convert(value, Color.class, display));
									else if (CSSProperties.TOOLTIPFOREGROUNDCOLOR == property)
										colorModel0.setToolTipForegroundColor(
												(Color) engine.convert(value, Color.class, display));
									else if (CSSProperties.DISABLEDFOREGROUNDCOLOR == property)
										colorModel0.setDisabledForegroundColor(
												(Color) engine.convert(value, Color.class, display));
								} else if (colorModel instanceof IExtendedColorModel) {
									IExtendedColorModel colorModel1 = (IExtendedColorModel2) colorModel;
									if (CSSProperties.OFFLINECOLOR == property)
										colorModel1
												.setOfflineColor((Color) engine.convert(value, Color.class, display));
									else if (CSSProperties.REMOTECOLOR == property)
										colorModel1.setRemoteColor((Color) engine.convert(value, Color.class, display));
									else if (CSSProperties.TITLECOLOR == property)
										colorModel1.setTitleColor((Color) engine.convert(value, Color.class, display));
									else if (CSSProperties.SELECTEDOFFLINECOLOR == property)
										colorModel1.setSelectedOfflineColor(
												(Color) engine.convert(value, Color.class, display));
									if (colorModel instanceof IExtendedColorModel2) {
										IExtendedColorModel2 colorModel2 = (IExtendedColorModel2) colorModel;
										if (CSSProperties.COLOR == property)
											colorModel2.setForegroundColor(
													(Color) engine.convert(value, Color.class, display));
										else if (CSSProperties.BACKGROUNDCOLOR == property)
											colorModel2.setBackgroundColor(
													(Color) engine.convert(value, Color.class, display));
										else if (CSSProperties.FOREGROUNDCOLOR == property)
											colorModel2.setForegroundColor(
													(Color) engine.convert(value, Color.class, display));
										else if (CSSProperties.TITLECOLOR == property)
											colorModel2.setTitleForeground(
													(Color) engine.convert(value, Color.class, display));
										else if (CSSProperties.TITLEBACKGROUNDCOLOR == property)
											colorModel2.setTitleBackground(
													(Color) engine.convert(value, Color.class, display));
										else if (CSSProperties.BACKGROUNDCOLOR == property)
											colorModel2.setBackgroundColor(
													(Color) engine.convert(value, Color.class, display));
										else if (CSSProperties.SELECTEDCOLOR == property)
											colorModel2.setSelectionForegroundColor(
													(Color) engine.convert(value, Color.class, display));
										else if (CSSProperties.SELECTEDBACKGROUNDCOLOR == property)
											colorModel2.setSelectionBackgroundColor(
													(Color) engine.convert(value, Color.class, display));
									}
								}
								return true;
							}
							return false;
						}

					});
					return providers;
				}
			};
			engine.registerCSSPropertyHandlerProvider(handlerProvider);
			engine.applyStyles(control, false);
			engine.unregisterCSSPropertyHandlerProvider(handlerProvider);
		}
	}

	public String getTheme() {
		return theme;
	}

	public void applyStyles(Object widget, boolean deep) {
		if (widget != null && engine != null)
			engine.applyStyles(widget, deep);
	}
}
