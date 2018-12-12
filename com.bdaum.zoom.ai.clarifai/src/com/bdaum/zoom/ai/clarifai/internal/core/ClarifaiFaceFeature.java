/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2018 Berthold Daum  
 */
package com.bdaum.zoom.ai.clarifai.internal.core;

import java.awt.image.BufferedImage;

import com.bdaum.zoom.core.internal.CoreActivator;

import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;

@SuppressWarnings("restriction")
public class ClarifaiFaceFeature extends ClarifaiFeature {
	
	@Override
	public String getFeatureName() {
		return "ClarifaiFace"; //$NON-NLS-1$
	}

	@Override
	public String getFieldName() {
		return "f_clarifaiFace"; //$NON-NLS-1$
	}
			
	protected byte[] toByteArray(float[] data) {
        byte[] result = new byte[data.length * 2-1];
        result[0] = (byte) data[0];
        for (int i = 1, j = 1; i < data.length; i++) {
        	int val = Math.min(65535, Math.max(0, (int) (data[i] * 65535)));
            result[j++] = (byte) (val >> 8); 
            result[j++] = (byte) val; 
        }
        return result;
    }
	
	protected float[] toFloatArray(byte[] data, int offset, int length) {
        float[] result = new float[(length+1) / 2];
        result[0] = data[offset];
        for (int i = 1, j = offset+1; i < result.length; i++)
			result[i] = (((data[j++] & 0xff) << 8) + (data[j++] & 0xff)) / 65535f;
        return result;
    }


	@Override
	public double getDistance(LireFeature vd) {
		if (!(vd instanceof ClarifaiFaceFeature))
			throw new UnsupportedOperationException("Wrong descriptor."); //$NON-NLS-1$
		float[] histogram2 = ((ClarifaiFaceFeature) vd).histogram;
		if (histogram == null || histogram2 == null || histogram.length < 1 || histogram2.length < 1)
			return 100d;
		int size1 = (int) histogram[0], size2 = (int) histogram2[0];
		if (size1 == 0 || size2 == 0)
			return 100d;
		int l = (histogram.length - 1) / size1;
		if (l == 0 || l != (histogram2.length - 1) / size2)
			return 100d;
		float[] v1 = new float[l], v2 = new float[l];
		double sumD = 0;
		for (int ind1 = 0; ind1 < size1; ind1++) {
			System.arraycopy(histogram, 1+ind1*l, v1, 0, l);
			double minD = 100d;
			for (int ind2 = 0; ind2 < size2; ind2++) {
				System.arraycopy(histogram2, 1+ind2*l, v2, 0, l);
				minD = Math.min(minD, MetricsUtils.distL1(v1, v2));
			}
			sumD += minD;
		}
		return sumD / size1;
	}

	@Override
	public void extract(BufferedImage image) {
		histogram = CoreActivator.getDefault().getAiService().getFeatureVector(image,
				"com.bdaum.zoom.ai.provider.clarifai", 1002); //$NON-NLS-1$
	}
}
