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

import org.eclipse.swt.graphics.Color;

public interface IColumnLabelColorModel extends IBaseColorModel {
	void setDisabledForegroundColor(Color c);

	void setToolTipBackgroundColor(Color c);
	
	void setToolTipForegroundColor(Color c);

	void setSelectedBackgroundColor(Color c);

	void setSelectedForegroundColor(Color c);

}
