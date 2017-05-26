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

/**
 * This class describes a color shift in the HSL color model
 *
 */
public class ColorShift {

	/**
	 * This class describes a color shift applied only to a sector of the hue
	 * axis
	 *
	 */
	public static class Sector {
		public static final int BELLSIZE = 256;

		private static final int _1F = 255 << 4;
		/**
		 * hue shift
		 */
		public float hue;
		/**
		 * saturation shift
		 */
		public float sat;
		/**
		 * luminosity shift
		 */
		public float lum;
		/**
		 * hue shift as integer scaled by 255*16
		 */
		public int hueInt;
		/**
		 * saturation shift as integer scaled by 255*16
		 */
		public int satInt;
		/**
		 * luminosity shift as integer scaled by 255*16
		 */
		public int lumInt;
		/**
		 * curve matching the hue to the strength of the color shift
		 */
		public int[] bell;
		/**
		 * true if saturation change, false if Tint
		 */
		public int satMode = SAT_MULT;

		/**
		 * Constructor
		 *
		 * @param center
		 *            - center of hue interval where shift applies
		 * @param radius
		 *            - radius of hue interval where shift applies
		 * @param slope
		 *            - slope of hue interval where shift applies
		 * @param hue
		 *            - hue shift
		 * @param sat
		 *            - saturation shift
		 * @param lum
		 *            - luminosity shift
		 * @param satMode
		 *            - saturation modification mode
		 */
		Sector(float center, float radius, float slope, float hue, float sat,
				float lum, int satMode) {
			this.hue = hue;
			this.satMode = satMode;
			while (this.hue < 0.5f)
				this.hue += 1f;
			while (this.hue > 0.5f)
				this.hue -= 1f;
			this.sat = sat;
			this.lum = lum;
			hueInt = (int) (hue * _1F);
			satInt = (int) (sat * _1F);
			lumInt = (int) (lum * _1F);
			this.bell = computeBell(center, radius, slope);
		}

		private static int[] computeBell(float center, float radius, float slope) {
			int h = (int) (center * 255f + 0.5f);
			int r = (int) (radius * 255f + 0.5f);
			int s = Math.max(1, (int) (slope * 255f + 0.5f));
			int[] result = new int[BELLSIZE];
			int index1, index2, index3, index4;
			for (int i = 0; i <= r; i++) {
				index1 = h + i;
				index1 = (index1 + BELLSIZE + BELLSIZE) % BELLSIZE;
				index2 = h - i;
				index2 = (index2 + BELLSIZE + BELLSIZE) % BELLSIZE;
				result[index1] = result[index2] = 256;
			}
			for (int i = 0; i <= s; i++) {
				index3 = h + i + r;
				index3 = (index3 + BELLSIZE + BELLSIZE) % BELLSIZE;
				index4 = h - i - r;
				index4 = (index4 + BELLSIZE + BELLSIZE) % BELLSIZE;
				// result[index3] = result[index4] = 256 - i * i * 1024 / (s *
				// s);
				result[index3] = result[index4] = 256 - i * i * 256 / (s * s);
			}
			return result;
		}

	}

	public static final int SAT_MULT = 0;
	public static final int SAT_ADD = 1;
	public static final int SAT_CROSS = 2;
	public static final int SAT_MULT_DEGRESSIVE = 3;

	public Sector[] sectors;
	public float vibrance;
	public float globHShift;
	public float globS;
	public float globL;
	public boolean glob;
	public boolean isShifting;
	public boolean isColorShift;
	public boolean isColorBoost;
	public float globHOverlay;
	public int satMode;
	public boolean isPreserving = true;

	/**
	 * Constructor
	 *
	 * @param globHShift
	 *            - global hue shift
	 * @param globHOverlay
	 *            - global hue overlay
	 * @param globS
	 *            - global saturation shift
	 * @param satMode
	 *            - saturation modification mode
	 * @param globL
	 *            - global luminosity shift
	 * @param vibrance
	 *            - vibrance shift
	 */
	public ColorShift(float globHShift, float globHOverlay, float globS,
			int satMode, float globL, float vibrance) {
		setGlobalHue(globHShift);
		setGlobalSaturation(globS, satMode);
		setGlobalLuminance(globL);
		setVibrance(vibrance);
		this.globHOverlay = globHOverlay;
	}

	/**
	 * Sets the vibrance shift
	 *
	 * @param vibrance
	 *            : 0 .. 1 .. 2
	 */
	public void setVibrance(float vibrance) {
		this.vibrance = Math.max(0f, vibrance);
		glob |= vibrance != 1f;
		isShifting |= glob;
	}

	/**
	 * Sets the global luminosity shift
	 *
	 * @param globL
	 *            : 0 .. 1 .. 2
	 */
	public void setGlobalLuminance(float globL) {
		this.globL = Math.max(0f, globL);
		glob |= globL != 1f;
		isShifting |= glob;
	}

	/**
	 * Sets the global saturation shift
	 *
	 * @param globS
	 *            : 0 .. 1 .. 2
	 * @param satMode
	 */
	public void setGlobalSaturation(float globS, int satMode) {
		this.globS = Math.max(0f, globS);
		this.satMode = satMode;
		glob |= globS != 1f;
		isShifting |= glob;
		isColorBoost |= globS != 1f;
		isPreserving &= (satMode == SAT_MULT || satMode == SAT_MULT_DEGRESSIVE);
	}

	/**
	 * Sets the global hue shift
	 *
	 * @param globH
	 */
	public void setGlobalHue(float globH) {
		this.globHShift = globH;
		while (this.globHShift < 0.5f)
			this.globHShift += 1f;
		while (this.globHShift > 0.5f)
			this.globHShift -= 1f;
		glob |= globH != 0f;
		isColorShift |= globH != 0f;
	}

	/**
	 * Add a color shift for a specific hue
	 *
	 * @param center
	 *            - center of hue interval where shift applies
	 * @param radius
	 *            - radius of hue interval where shift applies
	 * @param slope
	 *            - slope of hue interval where shift applies
	 * @param hue
	 *            - hue shift
	 * @param sat
	 *            - saturation shift
	 * @param lum
	 *            - luminosity shift
	 * @param saturationMode
	 *            - saturation modification mode
	 */
	public void addSector(float center, float radius, float slope, float hue,
			float sat, float lum, int saturationMode) {
		if (hue != 0f || sat != 1f || lum != 1f) {
			Sector sector = new Sector(center, radius, slope, hue, sat, lum,
					saturationMode);
			if (sectors == null)
				sectors = new Sector[] { sector };
			else {
				int l = sectors.length;
				Sector[] newsectors = new Sector[l + 1];
				System.arraycopy(sectors, 0, newsectors, 0, l);
				newsectors[l] = sector;
				sectors = newsectors;
			}
			isShifting = true;
			isPreserving &= saturationMode == SAT_MULT;
			isColorShift |= hue != 0f;
			isColorBoost |= sat != 0f;
		}
	}

	/**
	 * Retrieve selective shift elements
	 *
	 * @return - an array of color shift sector
	 */
	public Sector[] getSectors() {
		return sectors;
	}
}