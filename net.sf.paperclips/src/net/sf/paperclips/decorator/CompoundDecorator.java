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
import net.sf.paperclips.internal.Util;

/**
 * Decorates prints with multiple decorators.
 * 
 * @author Matthew Hall
 */
public class CompoundDecorator implements PrintDecorator {
	private final PrintDecorator[] decorators;

	/**
	 * Constructs a CompoundDecorator.
	 * 
	 * @param decorators
	 *            the decorators, in order from innermost to outermost.
	 */
	public CompoundDecorator(PrintDecorator[] decorators) {
		Util.noNulls(decorators);
		this.decorators = defensiveCopy(decorators);
	}

	private PrintDecorator[] defensiveCopy(PrintDecorator[] decorators) {
		return (PrintDecorator[]) decorators.clone();
	}

	public Print decorate(Print target) {
		Print result = target;
		for (int i = 0; i < decorators.length; i++)
			result = decorators[i].decorate(target);
		return result;
	}
}
