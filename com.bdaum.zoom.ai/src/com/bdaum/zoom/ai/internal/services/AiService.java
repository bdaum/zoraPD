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
 * (c) 2016 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.ai.internal.services;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.bdaum.zoom.ai.internal.AiActivator;
import com.bdaum.zoom.ai.internal.preference.AiPreferencePage;
import com.bdaum.zoom.ai.internal.preference.PreferenceConstants;
import com.bdaum.zoom.core.internal.ai.IAiService;
import com.bdaum.zoom.core.internal.ai.Prediction;
import com.bdaum.zoom.core.internal.lire.Algorithm;

public class AiService implements IAiService {

	/**
	 * Called when service is activated.
	 */
	protected void activate() {
		AiActivator.getDefault().logInfo(Messages.AiService_service_activated);
	}

	/**
	 * Called when service is deactivated.
	 */
	protected void deactivate() {
		AiActivator.getDefault().logInfo(Messages.AiService_service_deactivated);
	}

	@Override
	public Prediction predict(byte[] jpeg, String serviceId) {
		if (isEnabled()) {
			IAiServiceProvider provider = AiActivator.getDefault().getServiceProvider(serviceId);
			if (provider != null)
				return provider.predict(jpeg);
			return new Prediction("", null, null, //$NON-NLS-1$
					new Status(IStatus.ERROR, AiActivator.PLUGIN_ID, Messages.AiService_provider_not_found));
		}
		return new Prediction("", null, null, //$NON-NLS-1$
				new Status(IStatus.INFO, AiActivator.PLUGIN_ID, Messages.AiService_deactivated));
	}

	@Override
	public float getMarkAbove(String serviceId) {
		if (isEnabled()) {
			IAiServiceProvider provider = AiActivator.getDefault().getServiceProvider(serviceId);
			if (provider != null)
				return provider.getMarkAbove();
		}
		return 0.99f;
	}

	@Override
	public boolean getMarkKnownOnly(String serviceId) {
		if (isEnabled()) {
			IAiServiceProvider provider = AiActivator.getDefault().getServiceProvider(serviceId);
			if (provider != null)
				return provider.getMarkKnownOnly();
		}
		return true;
	}

	@Override
	public String[] getProviderIds() {
		IAiServiceProvider[] providers = AiActivator.getDefault().getServiceProviders();
		String[] ids = new String[providers.length];
		for (int i = 0; i < ids.length; i++)
			ids[i] = providers[i].getId();
		return ids;
	}

	@Override
	public String[] getProviderNames() {
		IAiServiceProvider[] providers = AiActivator.getDefault().getServiceProviders();
		String[] names = new String[providers.length];
		for (int i = 0; i < names.length; i++)
			names[i] = providers[i].getName();
		return names;
	}

	@Override
	public boolean isEnabled() {
		return AiActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.ENABLE);
	}

	@Override
	public boolean configure(Shell shell) {
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(shell, AiPreferencePage.ID, null, null);
		return dialog.open() == PreferenceDialog.OK;
	}

	@Override
	public void dispose(String providerId) {
		IAiServiceProvider provider = AiActivator.getDefault().getServiceProvider(providerId);
		if (provider != null)
			provider.dispose();

	}

	@Override
	public boolean checkAdultContents(String providerId) {
		if (isEnabled()) {
			IAiServiceProvider provider = AiActivator.getDefault().getServiceProvider(providerId);
			if (provider != null)
				return provider.checkAdultContent();
		}
		return false;
	}

	@Override
	public boolean checkFaces(String providerId) {
		if (isEnabled()) {
			IAiServiceProvider provider = AiActivator.getDefault().getServiceProvider(providerId);
			if (provider != null)
				return provider.checkFaces();
		}
		return false;
	}

	@Override
	public boolean generateDescription(String providerId) {
		if (isEnabled()) {
			IAiServiceProvider provider = AiActivator.getDefault().getServiceProvider(providerId);
			if (provider != null)
				return provider.generateDescription();
		}
		return false;
	}

	@Override
	public String getTitle(String providerId) {
		if (isEnabled()) {
			IAiServiceProvider provider = AiActivator.getDefault().getServiceProvider(providerId);
			if (provider != null)
				return provider.getTitle();
		}
		return null;
	}

	@Override
	public int getLatency(String providerId) {
		if (isEnabled()) {
			IAiServiceProvider provider = AiActivator.getDefault().getServiceProvider(providerId);
			if (provider != null)
				return provider.getLatency();
		}
		return -1;
	}

	@Override
	public float[] getFeatureVector(BufferedImage image, String serviceId) {
		if (isEnabled()) {
			IAiServiceProvider provider = AiActivator.getDefault().getServiceProvider(serviceId);
			if (provider != null)
				return provider.getFeatureVector(image);
		}
		return null;
	}

	@Override
	public boolean hasProvider(String id) {
		IAiServiceProvider[] providers = AiActivator.getDefault().getServiceProviders();
		for (IAiServiceProvider provider : providers)
			if (provider.getId().equals(id))
				return true;
		return false;
	}

	@Override
	public boolean isAccountValid(String providerId) {
		if (isEnabled()) {
			IAiServiceProvider provider = AiActivator.getDefault().getServiceProvider(providerId);
			if (provider != null)
				return provider.isAccountValid();
		}
		return false;
	}

	@Override
	public Algorithm[] getLireAlgorithms() {
		List<Algorithm> algs = new ArrayList<>(3);
		IAiServiceProvider[] providers = AiActivator.getDefault().getServiceProviders();
		for (IAiServiceProvider provider : providers) {
			Algorithm alg = provider.getAlgorithm();
			if (alg != null)
				algs.add(alg);
		}
		return algs.toArray(new Algorithm[algs.size()]);
	}

	@Override
	public Class<?> getFeature(String providerId) {
		if (isEnabled()) {
			IAiServiceProvider provider = AiActivator.getDefault().getServiceProvider(providerId);
			if (provider != null)
				return provider.getFeature();
		}
		return null;
	}

}
