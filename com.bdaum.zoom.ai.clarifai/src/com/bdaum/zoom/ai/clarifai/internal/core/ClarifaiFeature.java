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
 * (c) 2017 Berthold Daum  
 */
package com.bdaum.zoom.ai.clarifai.internal.core;

import java.awt.image.BufferedImage;
import java.security.ProviderException;

import com.bdaum.zoom.core.internal.CoreActivator;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;

@SuppressWarnings("restriction")
public class ClarifaiFeature implements GlobalFeature {

	public float[] histogram;

	@Override
	public String getFeatureName() {
		return "Clarifai"; //$NON-NLS-1$
	}

	@Override
	public String getFieldName() {
		return "f_clarifai"; //$NON-NLS-1$
	}

	@Override
	public byte[] getByteArrayRepresentation() {
		if (histogram == null)
			throw new ProviderException(Messages.Clarifai_service_disabled);
		if (histogram.length == 0)
			throw new ProviderException(Messages.Clarifai_request_refused);
		return toByteArray(histogram);
	}
	
	protected byte[] toByteArray(float[] data) {
        byte[] result = new byte[data.length * 2];
        for (int i = 0, j = 0; i < data.length; i++) {
        	int val = Math.min(65535, Math.max(0, (int) (data[i] * 65535)));
            result[j++] = (byte) (val >> 8); 
            result[j++] = (byte) val; 
        }
        return result;
    }

	@Override
	public void setByteArrayRepresentation(byte[] in) {
		histogram = toFloatArray(in, 0, in.length);
	}
	
	protected float[] toFloatArray(byte[] data, int offset, int length) {
        float[] result = new float[length / 2];
        for (int i = 0, j = offset; i < result.length; i++)
			result[i] = (((data[j++] & 0xff) << 8) + (data[j++] & 0xff)) / 65535f;
        return result;
    }

	@Override
	public void setByteArrayRepresentation(byte[] in, int offset, int length) {
		histogram = toFloatArray(in, offset, length);
	}
	
	@Override
	public double getDistance(LireFeature vd) {
		if (!(vd instanceof ClarifaiFeature))
			throw new UnsupportedOperationException("Wrong descriptor."); //$NON-NLS-1$
		float[] histogram2 = ((ClarifaiFeature) vd).histogram;
		if (histogram == null || histogram2 == null || histogram.length != histogram2.length)
			return 100d;
		return MetricsUtils.distL1(histogram, histogram2);
	}

	@Override
	public double[] getFeatureVector() {
		if (histogram != null) {
			double[] result = new double[histogram.length];
			for (int i = 0; i < histogram.length; i++)
				result[i] = histogram[i];
			return result;
		}
		return null;
	}

	@Override
	public void extract(BufferedImage image) {
		histogram = CoreActivator.getDefault().getAiService().getFeatureVector(image,
				"com.bdaum.zoom.ai.provider.clarifai", 1001); //$NON-NLS-1$
	}

}
