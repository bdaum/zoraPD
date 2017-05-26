/*******************************************************************************
 * Copyright (c) 2008, Original authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo ZERR <angelo.zerr@gmail.com>
 *******************************************************************************/
package org.akrogen.tkui.css.swt.engine;

import org.akrogen.tkui.css.core.engine.CSSElementContext;
import org.akrogen.tkui.css.core.impl.engine.CSSEngineImpl;
import org.akrogen.tkui.css.core.resources.IResourcesRegistry;
import org.akrogen.tkui.css.swt.dom.SWTElementProvider;
import org.akrogen.tkui.css.swt.properties.converters.CSSValueSWTColorConverterImpl;
import org.akrogen.tkui.css.swt.properties.converters.CSSValueSWTCursorConverterImpl;
import org.akrogen.tkui.css.swt.properties.converters.CSSValueSWTFontConverterImpl;
import org.akrogen.tkui.css.swt.properties.converters.CSSValueSWTFontDataConverterImpl;
import org.akrogen.tkui.css.swt.properties.converters.CSSValueSWTGradientConverterImpl;
import org.akrogen.tkui.css.swt.properties.converters.CSSValueSWTImageConverterImpl;
import org.akrogen.tkui.css.swt.properties.converters.CSSValueSWTRGBConverterImpl;
import org.akrogen.tkui.css.swt.resources.SWTResourcesRegistry;
import org.akrogen.tkui.css.swt.selectors.DynamicPseudoClassesSWTFocusHandler;
import org.akrogen.tkui.css.swt.selectors.DynamicPseudoClassesSWTHoverHandler;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

/**
 * CSS SWT Engine implementation which configure CSSEngineImpl to apply styles
 * to SWT widgets.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public abstract class AbstractCSSSWTEngineImpl extends CSSEngineImpl implements
		DisposeListener {

	protected Display display;

	public AbstractCSSSWTEngineImpl(Display display) {
		this(display, false);
	}

	public AbstractCSSSWTEngineImpl(Display display, boolean lazyApplyingStyles) {
		this.display = display;

		// Register SWT Element Provider to retrieve
		// w3c Element SWTElement coming from SWT widget.
		super.setElementProvider(SWTElementProvider.INSTANCE);

		/** Initialize CSS Property Handlers * */

		this.initializeCSSPropertyHandlers();

		/** Initialize Dynamic pseudo classes * */

		// Register SWT Focus Handler
		super.registerDynamicPseudoClassHandler("focus",
				DynamicPseudoClassesSWTFocusHandler.INSTANCE);
		// Register SWT Hover Handler
		super.registerDynamicPseudoClassHandler("hover",
				DynamicPseudoClassesSWTHoverHandler.INSTANCE);

		/** Initialize SWT CSSValue converter * */

		// Register SWT RGB CSSValue Converter
		super.registerCSSValueConverter(CSSValueSWTRGBConverterImpl.INSTANCE);
		// Register SWT Color CSSValue Converter
		super.registerCSSValueConverter(CSSValueSWTColorConverterImpl.INSTANCE);
		// Register SWT Gradient CSSValue Converter
		super.registerCSSValueConverter(CSSValueSWTGradientConverterImpl.INSTANCE);
		// Register SWT Cursor CSSValue Converter
		super.registerCSSValueConverter(CSSValueSWTCursorConverterImpl.INSTANCE);
		// Register SWT Font CSSValue Converter
		super.registerCSSValueConverter(CSSValueSWTFontConverterImpl.INSTANCE);
		// Register SWT FontData CSSValue Converter
		super.registerCSSValueConverter(CSSValueSWTFontDataConverterImpl.INSTANCE);
		// Register SWT Image CSSValue Converter
		super.registerCSSValueConverter(CSSValueSWTImageConverterImpl.INSTANCE);

		if (lazyApplyingStyles) {
			new CSSSWTApplyStylesListener(Display.findDisplay(Thread
					.currentThread()), this);
		}
	}

	protected abstract void initializeCSSPropertyHandlers();

	@Override
	public IResourcesRegistry getResourcesRegistry() {
		IResourcesRegistry resourcesRegistry = super.getResourcesRegistry();
		if (resourcesRegistry == null) {
			super.setResourcesRegistry(new SWTResourcesRegistry(display));
		}
		return super.getResourcesRegistry();
	}

	@Override
	protected void cacheElementContext(Object nativeWidget, // bd1
			CSSElementContext elementContext) { // bd1
		if (nativeWidget instanceof Widget) { // bd1
			super.cacheElementContext(nativeWidget, elementContext); // bd1
			((Widget) nativeWidget).addDisposeListener(this); // bd1
		}// bd1
	}// bd1

	public void widgetDisposed(DisposeEvent e) { // bd1
		getElementsContext().remove(e.widget); // bd1
	}// bd1

}
