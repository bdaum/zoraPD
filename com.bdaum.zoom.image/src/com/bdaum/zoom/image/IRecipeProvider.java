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
package com.bdaum.zoom.image;

import java.util.Map;

import com.bdaum.zoom.image.recipe.Recipe;

/**
 * This interface describes entities that can deliver RAW development recipes.
 *
 */
public interface IRecipeProvider {

	/**
	 * Returns a recipe for the given image URI
	 *
	 * @param uri
	 *            - URI of original image
	 * @param highres
	 *            - true if a high-resolution image is to be developed with the
	 *            recipe
	 * @param focalLengthProvider
	 *            - might be used to obtain the 35 mm equivalent focal length
	 * @param overlayMap
	 *            - a map in which EXIF and IPTC values provided by the recipe
	 *            can be put
	 * @param detectorIds
	 *            - and array of detector IDs to be considered. If null, all
	 *            ACTIVE detectors are considered.
	 * @return - the recipe obtained for the specified image or null
	 */
	Recipe getRecipe(String uri, boolean highres,
			IFocalLengthProvider focalLengthProvider,
			Map<String, String> overlayMap, String[] detectorIds);

	/**
	 * Returns the latest recipe modification timestamp for the given image
	 *
	 * @param uri
	 *            - URI of original image
	 * @param lastMod
	 *            - last modification of image file
	 * @param detectorIds
	 *            - and array of detector IDs to be considered. If null, all
	 *            ACTIVE detectors are considered.
	 * @return - timestamp or 0 if no recipe exists
	 */
	long getLastRecipeModification(String uri, long lastMod,
			String[] detectorIds);
}
