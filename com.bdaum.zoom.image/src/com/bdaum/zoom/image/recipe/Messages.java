/*******************************************************************************
 * Copyright (c) 2009-2010 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/
package com.bdaum.zoom.image.recipe;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.bdaum.zoom.image.recipe.messages"; //$NON-NLS-1$
	public static String Recipe_gray_curve;
	public static String Recipe_as_shot;
	public static String Recipe_auto;
	public static String Recipe_chromatic_abberation;
	public static String Recipe_color_adjustments;
	public static String Recipe_color_balance;
	public static String Recipe_color_boost;
	public static String Recipe_color_curves;
	public static String Recipe_color_shift;
	public static String Recipe_cropped;
	public static String Recipe_detail_enhancements;
	public static String Recipe_exposure_adjustments;
	public static String Recipe_filp_h;
	public static String Recipe_flip_v;
	public static String Recipe_geometry;
	public static String Recipe_gray_scale;
	public static String Recipe_highlight_rec;
	public static String Recipe_lens_distortion;
	public static String Recipe_local_contrast;
	public static String Recipe_manual;
	public static String Recipe_no_adjust;
	public static String Recipe_noise_reduction;
	public static String Recipe_perspective;
	public static String Recipe_rotated;
	public static String Recipe_sharpening;
	public static String Recipe_split_toning;
	public static String Recipe_stops;
	public static String Recipe_tonal_range;
	public static String Recipe_vibrance;
	public static String Recipe_vignette;
	public static String Recipe_white_balance;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
