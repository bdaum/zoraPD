package com.bdaum.zoom.net.communities.ui;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.scohen.juploadr.uploadapi.AuthException;
import org.scohen.juploadr.uploadapi.CommunicationException;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.net.communities.CommunitiesActivator;
import com.bdaum.zoom.net.communities.CommunityApi;

public abstract class AbstractCommunityExportWizard extends Wizard implements
		IExportWizard, IExecutableExtension {

	private static final String _SETTINGSID = "com.bdaum.zoom.exportCommunityProperties."; //$NON-NLS-1$
	protected IConfigurationElement configElement;
	protected String communityName;
	protected ImageDescriptor imageDescriptor;
	protected List<Asset> assets;
	protected String communityId;
	protected String settingsId;
	protected CommunityApi api;

	public void setInitializationData(IConfigurationElement cfig,
			String propertyName, Object data) {
		String namespaceIdentifier = cfig.getNamespaceIdentifier();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
				.getExtensionPoint(CommunitiesActivator.PLUGIN_ID, "community"); //$NON-NLS-1$
		loop: for (IExtension ext : extensionPoint.getExtensions()) {
			for (IConfigurationElement conf : ext.getConfigurationElements()) {
				if (conf.getNamespaceIdentifier().equals(namespaceIdentifier)) {
					configElement = conf;
					communityName = conf.getAttribute("name"); //$NON-NLS-1$
					communityId = conf.getAttribute("id"); //$NON-NLS-1$
					settingsId = _SETTINGSID + communityId;
					// Icon
					String strIcon = conf.getAttribute("bannerImage");//$NON-NLS-1$
					if (strIcon != null)
						imageDescriptor = AbstractUIPlugin
								.imageDescriptorFromPlugin(namespaceIdentifier,
										strIcon);
					break loop;
				}
			}
		}
	}

	@Override
	public boolean performFinish() {
		WizardPage wizardPage = (WizardPage) getContainer().getCurrentPage();
		try {
			boolean result = doFinish();
			if (!result)
				wizardPage.setErrorMessage(NLS.bind(
						Messages.AbstractCommunityExportWizard_auth_failed,
						communityName));
			return result;
		} catch (AuthException e) {
			wizardPage.setErrorMessage(NLS.bind(
					Messages.AbstractCommunityExportWizard_auth_failed,
					communityName));
			return false;
		} catch (CommunicationException e) {
			wizardPage.setErrorMessage(NLS.bind(
					Messages.CommunityExportWizard_connection_failed,
					communityName));
			return false;
		} catch (Throwable t) {
			CommunitiesActivator
					.getDefault()
					.logError(
							NLS.bind(
									Messages.AbstractCommunityExportWizard_internal_error,
									communityName), t);
			wizardPage.setErrorMessage(NLS.bind(
					Messages.AbstractCommunityExportWizard_internal_error,
					communityName));
			return false;
		}
	}

	protected abstract boolean doFinish() throws CommunicationException,
			AuthException;

	public CommunityApi getApi() {
		return api;
	}

}