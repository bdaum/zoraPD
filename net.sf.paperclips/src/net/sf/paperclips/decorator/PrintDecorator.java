/*
 * Copyright (c) 2006 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Hall - initial API and implementation
 */
package net.sf.paperclips.decorator;

import net.sf.paperclips.Print;

/**
 * Interface for wrapping a print in a decoration. This interface is useful for
 * applying decorations uniformly without having to explicitly call constructors
 * for each item being decorated.
 * 
 * @author Matthew Hall
 */
public interface PrintDecorator {
	/**
	 * Wraps the target in a decoration. The decoration depends on the runtime
	 * class of the decorator.
	 * 
	 * @param target
	 *            the print to wrap with a decoration.
	 * @return the target wrapped in a decoration.
	 */
	public Print decorate(Print target);
}
