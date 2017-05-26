package com.bdaum.zoom.ui.internal.commands;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.ui.internal.commands.messages"; //$NON-NLS-1$
	public static String QuickFindControl_click_for_text;
	public static String QuickFindControl_configurable_text;
	public static String QuickFindControl_configure_similarity;
	public static String QuickFindControl_configure;
	public static String QuickFindControl_configure_text;
	public static String QuickFindControl_enter_search_string;
	public static String QuickFindControl_images_similar;
	public static String QuickFindControl_in_progress;
	public static String QuickFindControl_incomplete;
	public static String QuickFindControl_indexing_off;
	public static String QuickFindControl_maxmin;
	public static String QuickFindControl_search_impossible;
	public static String QuickFindControl_text_similarity;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
