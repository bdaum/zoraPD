/*******************************************************************************
 * Copyright (c) 2010-2015 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors :
 *    Berthold Daum  (berthold.daum@bdaum.de)
 *******************************************************************************/

package org.eclipse.nebula.widgets.gallery;

import java.text.MessageFormat;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

public class EnhancedGalleryGroupRenderer extends DefaultGalleryGroupRenderer {

	private final IImageProvider imageProvider;

	public EnhancedGalleryGroupRenderer(IImageProvider imageProvider) {
		super();
		this.imageProvider = imageProvider;
	}

	@Override
	protected int getGroupHeight(GalleryItem group) {
		validateImage(group);
		return super.getGroupHeight(group);
	}

	private void validateImage(GalleryItem group) {
		Image image = group.getImage();
		if (image != null && image.isDisposed())
			group.setImage(imageProvider.obtainImage(group));
	}

	@Override
	protected String getGroupTitle(GalleryItem group) {
		String text1 = group.getText(1);
		String text = group.getText();
		int itemCount = group.getItemCount();
		return text1 == null || text1.length() == 0 ? MessageFormat.format(
				"{0} ({1})", text, itemCount) : MessageFormat.format(
				"{0} ({1} - {2})", text, itemCount, text1);

	}

	@Override
	protected void drawGroup(GC gc, GalleryItem group, int x, int y, int clipX,
			int clipY, int clipWidth, int clipHeight) {
		validateImage(group);
		super.drawGroup(gc, group, x, y, clipX, clipY, clipWidth, clipHeight);
	}
}
