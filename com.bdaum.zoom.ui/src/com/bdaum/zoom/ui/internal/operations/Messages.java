package com.bdaum.zoom.ui.internal.operations;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ui.internal.operations.messages"; //$NON-NLS-1$
	public static String ExhibitionPropertiesOperation_set_exhibition_properties;
	public static String SlideshowPropertiesOperation_set_slideshow_properties;
	public static String WebGalleryPropertiesOperation_set_web_gallery_properties;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
