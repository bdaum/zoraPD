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

import java.awt.geom.Point2D.Double;

import org.eclipse.swt.graphics.RGB;

import Jama.Matrix;

public class PerspectiveCorrection {

	/**
	 * fill color for empty areas
	 */
	public final RGB fillColor;
	/**
	 * transformation matrix
	 */
	public final Matrix transformation;
	/**
	 * focal length : image diagonal
	 */
	public final double flen;
	/**
	 * final coordinate shift (normalized to -0.5 ... +0.5)
	 */
	public Double finalTranslation;

	/**
	 * @param transformation
	 *            - transformation matrix
	 * @param flen
	 *            - focal length : image diagonal
	 * @param fillColor
	 *            - fill color for empty areas
	 */
	public PerspectiveCorrection(Matrix transformation, double flen,
			RGB fillColor) {
		this.transformation = transformation;
		this.flen = flen;
		this.fillColor = fillColor;
	}

	/**
	 * Tests if this transformation has any effect
	 *
	 * @return - true if the perspective transformation has any effect
	 */
	public boolean isCorrecting() {
		if (finalTranslation != null
				&& (finalTranslation.x != 0d || finalTranslation.y != 0d))
			return true;
		for (int i = 0; i < transformation.getColumnDimension(); i++) {
			for (int j = 0; j < transformation.getRowDimension(); j++) {
				if (i == j) {
					if (transformation.get(i, j) != 1d)
						return true;
				} else {
					if (transformation.get(i, j) != 0d)
						return true;
				}
			}
		}
		return false;
	}

}
