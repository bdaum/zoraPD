/*******************************************************************************
 * Copyright (c) 2010 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors :
 *    Berthold Daum  (berthold.daum@bdaum.de)
 *******************************************************************************/

package org.eclipse.nebula.widgets.gallery;

import org.eclipse.swt.graphics.Image;

public interface IImageProvider {

	Image obtainImage(GalleryItem item);

}
