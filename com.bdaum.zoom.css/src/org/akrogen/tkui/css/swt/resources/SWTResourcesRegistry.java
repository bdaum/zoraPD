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
package org.akrogen.tkui.css.swt.resources;

import org.akrogen.tkui.css.core.resources.AbstractResourcesRegistry;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * SWT Resources Registry to cache SWT Resource like Color, Cursor and Font and
 * dispose it.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class SWTResourcesRegistry extends AbstractResourcesRegistry {

//	private static Log logger = LogFactory.getLog(SWTResourcesRegistry.class);

	public SWTResourcesRegistry(Display display) {
		if (display == null)
			return;
		// When SWT Display will dispose, all SWT resources stored
		// into cache will be dispose it too.
		display.addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event event) {
				dispose();
			}
		});
	}

	@Override
	public Object getResource(Object type, Object key) {
		Object resource = super.getResource(type, key);
		if (resource != null) {
			// test if resource is disposed
			if (isDisposed(resource)) {
				// SWT Resource is disposed
				// unregister it.
				super.unregisterResource(resource);
				return null;
			}
		}
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akrogen.tkui.core.css.resources.AbstractResourcesRegistry#registerResource(java.lang.String,
	 *      java.lang.Object, java.lang.Object)
	 */
	@Override
	public void registerResource(Object type, Object key, Object resource) {
		if (resource == null)
			return;
//		if (logger.isDebugEnabled()) {
//			if (resource instanceof Color) {
//				logger.debug("Cache SWT Color key=" + key);
//			} else if (resource instanceof Cursor) {
//				logger.debug("Cache SWT Cursor key=" + key);
//			} else if (resource instanceof Font) {
//				logger.debug("Cache SWT Font key=" + key);
//			} else if (resource instanceof Image) {
//				logger.debug("Cache SWT Image key=" + key);
//			} else
//				logger.debug("Cache Resource key=" + key);
//		}
		super.registerResource(type, key, resource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akrogen.tkui.core.css.resources.AbstractResourcesRegistry#disposeResource(java.lang.Object,
	 *      java.lang.String, java.lang.Object)
	 */
	@Override
	public void disposeResource(Object type, String key, Object resource) {
		// Dispose SWT Resource
		if (resource instanceof Color) {
			((Color)resource).dispose();
//			if (logger.isDebugEnabled())
//				logger.debug("Dispose SWT Color key=" + key);
		} else if (resource instanceof Cursor) {
			((Cursor)resource).dispose();
//			if (logger.isDebugEnabled())
//				logger.debug("Dispose SWT Cursor key=" + key);
		} else if (resource instanceof Font) {
			((Font)resource).dispose();
//			if (logger.isDebugEnabled())
//				logger.debug("Dispose SWT Font key=" + key);
		} else if (resource instanceof Image) {
			((Image) resource).dispose();
//			if (logger.isDebugEnabled())
//				logger.debug("Dispose SWT Image key=" + key);
		} 
//		else if (logger.isDebugEnabled())
//			logger.debug("Dispose Resource key=" + key);
	}

	protected boolean isDisposed(Object resource) {
		if (resource instanceof Color) {
			return ((Color) resource).isDisposed();
		} else if (resource instanceof Font) {
			return ((Font) resource).isDisposed();
		} else if (resource instanceof Image) {
			return ((Image) resource).isDisposed();
		} else if (resource instanceof Cursor) {
			return ((Cursor) resource).isDisposed();
		}
		return false;
	}
}
