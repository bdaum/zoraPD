/*******************************************************************************
 * Copyright (c) 2009, 2011 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum
 *******************************************************************************/
package com.bdaum.zoom.css.internal;

import org.eclipse.swt.graphics.Color;

public interface IExtendedColorModel2 extends IExtendedColorModel {

	void setForegroundColor(Color foregroundColor);

	void setSelectionForegroundColor(Color selectionForegroundColor);

	void setSelectionBackgroundColor(Color selectionBackgroundColor);

	void setBackgroundColor(Color backgroundColor);

	void setTitleForeground(Color color);

	void setTitleBackground(Color color);

}