/*******************************************************************************
 * Copyright (c) 2009-2019 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.image.internal;

import java.awt.RenderingHints;
import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ProfileRGB;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.spi.IIORegistry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.bdaum.zoom.common.CommonConstants;
import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.image.IImportFilterFactory;
import com.bdaum.zoom.image.IVideoService;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.recipe.UnsharpMask;

public class ImageActivator extends Plugin {

	public static final String PLUGIN_ID = "com.bdaum.zoom.image"; //$NON-NLS-1$

	private static ImageActivator plugin;

	private ICC_ProfileRGB[] iccProfiles = new ICC_ProfileRGB[ImageConstants.ICCFILES.length];
	private ColorConvertOp[] COLORCONVERTOP_SRGB2ICC = new ColorConvertOp[ImageConstants.ICCFILES.length];
	private ColorConvertOp[] COLORCONVERTOP_ICC2SRGB = new ColorConvertOp[ImageConstants.ICCFILES.length];
	private Map<String, IImportFilterFactory> importFilters;
	private String customProfile;
	private File tempFolder;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ImageActivator getDefault() {
		return plugin;
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	public void logWarning(String message, Exception e) {
		getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, e));
	}

	public ICC_ProfileRGB getIccProfile(int profile) {
		if (profile == ImageConstants.NOCMS)
			return null;
		if (iccProfiles[profile] == null) {
			if (profile == ImageConstants.CUSTOM) {
				if (customProfile != null)
					try {
						return (ICC_ProfileRGB) ICC_Profile.getInstance(customProfile);
					} catch (IOException e) {
						// fall through
					}
				return null;
			}
			iccProfiles[profile] = loadICCProfile(ImageConstants.ICCFILES[profile]);
		}
		return iccProfiles[profile];
	}

	public void resetCustomProfile(String profile) {
		customProfile = profile;
		iccProfiles[ImageConstants.CUSTOM] = null;
	}

	private static ICC_ProfileRGB loadICCProfile(String name) {
		try {
			String path = FileLocator.findAbsolutePath(ImageActivator.getDefault().getBundle(), name);
			if (path != null)
				return (ICC_ProfileRGB) ICC_Profile.getInstance(path);
		} catch (Exception e) {
			// ignore
		}
		return null;
	}

	public ColorConvertOp computeColorConvertOp(ICC_Profile sourceProfile, int cms) {
		if (cms == ImageConstants.NOCMS)
			return null;
		if (sourceProfile == null || sourceProfile == getIccProfile(ImageConstants.SRGB))
			return cms == ImageConstants.SRGB ? null : getCOLORCONVERTOP_SRGB2ICC(cms);
		ICC_ProfileRGB outputProfile = getIccProfile(cms);
		return (sourceProfile != outputProfile) ? new ColorConvertOp(new ICC_Profile[] { sourceProfile, outputProfile },
				new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY))
				: null;
	}

	public UnsharpMask computeSharpenOp(int degree) {
		return (degree == ImageConstants.SHARPEN_NONE) ? null
				: computeUnsharpMask(1.5f, degree / 100f, Math.max(1, (60 - degree) / 10));
	}

	public UnsharpMask computeUnsharpMask(float radius, float amount, int threshold) {
		return (radius == 0f || amount == 0f) ? null
				: new UnsharpMask(amount, radius, threshold / 255f, 0f, null, UnsharpMask.SHARPEN);
	}

	public ColorConvertOp getCOLORCONVERTOP_SRGB2ICC(int profile) {
		if (profile == ImageConstants.NOCMS || profile == ImageConstants.SRGB)
			return null;
		if (COLORCONVERTOP_SRGB2ICC[profile] == null)
			COLORCONVERTOP_SRGB2ICC[profile] = new ColorConvertOp(
					new ICC_Profile[] { getIccProfile(ImageConstants.SRGB), getIccProfile(profile) },
					new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY));
		return COLORCONVERTOP_SRGB2ICC[profile];
	}

	public ColorConvertOp getCOLORCONVERTOP_ICC2SRGB(int profile) {
		if (profile == ImageConstants.NOCMS || profile == ImageConstants.SRGB)
			return null;
		if (COLORCONVERTOP_ICC2SRGB[profile] == null)
			COLORCONVERTOP_ICC2SRGB[profile] = new ColorConvertOp(
					new ICC_Profile[] { getIccProfile(profile), getIccProfile(ImageConstants.SRGB) },
					new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY));
		return COLORCONVERTOP_ICC2SRGB[profile];
	}

	public Map<String, IImportFilterFactory> getImportFilters() {
		if (importFilters == null) {
			importFilters = new HashMap<String, IImportFilterFactory>();
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID,
					"importFilter"); //$NON-NLS-1$
			for (IExtension extension : extensionPoint.getExtensions())
				for (IConfigurationElement conf : extension.getConfigurationElements())
					try {
						IImportFilterFactory filter = (IImportFilterFactory) conf.createExecutableExtension("class"); //$NON-NLS-1$
						StringTokenizer st = new StringTokenizer(conf.getAttribute("extensions")); //$NON-NLS-1$
						while (st.hasMoreTokens())
							importFilters.put(st.nextToken().toLowerCase(), filter);
					} catch (CoreException e) {
						logError(NLS.bind(Messages.ImageActivator_cannot_create_import_filter,
								conf.getAttribute("name")), e); //$NON-NLS-1$
					}
		}
		return importFilters;
	}

	public Map<String, String> getImportedExtensions() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "importFilter"); //$NON-NLS-1$
		Map<String, String> result = new HashMap<>();
		for (IExtension extension : extensionPoint.getExtensions())
			for (IConfigurationElement conf : extension.getConfigurationElements()) {
				String mime = conf.getAttribute("mimetype").toLowerCase(); //$NON-NLS-1$
				StringTokenizer st = new StringTokenizer(conf.getAttribute("extensions")); //$NON-NLS-1$
				while (st.hasMoreTokens())
					result.put(st.nextToken().toLowerCase(), mime);
			}
		return result;
	}

	public List<String> getImportedNames() {
		List<String> result = new ArrayList<>();
		for (IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "importFilter") //$NON-NLS-1$
				.getExtensions())
			for (IConfigurationElement conf : extension.getConfigurationElements()) {
				StringBuffer sb = new StringBuffer();
				sb.append(conf.getAttribute("name")).append(" ("); //$NON-NLS-1$ //$NON-NLS-2$
				StringTokenizer st = new StringTokenizer(conf.getAttribute("extensions")); //$NON-NLS-1$
				while (st.hasMoreTokens()) {
					sb.append("*.").append(st.nextToken()); //$NON-NLS-1$
					if (st.hasMoreTokens())
						sb.append(", "); //$NON-NLS-1$
				}
				result.add(sb.append(')').toString());
			}
		return result;
	}

	public File createTempFile(String name, String suffix) throws IOException {
		File tempFile = File.createTempFile(name, suffix, getTempFolder());
		tempFile.deleteOnExit();
		return tempFile;
	}

	private File getTempFolder() {
		if (tempFolder == null || !tempFolder.exists()) {
			tempFolder = new File(new StringBuilder().append(System.getProperty("java.io.tmpdir")).append('/') //$NON-NLS-1$
					.append(ImageConstants.APPNAME).append('_').append(System.getProperty("user.name")).toString()); //$NON-NLS-1$
			tempFolder.mkdirs();
			tempFolder.deleteOnExit();
		}
		return tempFolder;
	}

	public void deleteTempFolderAfterShutdown() {
		try {
			deleteFileAfterShutdown(getTempFolder());
		} catch (Exception e) {
			// can't do anything
		}
	}

	public void deleteFileAfterShutdown(File file) {
		try {
			File cp = FileLocator.findFile(getBundle(), "/"); //$NON-NLS-1$
			if (CommonConstants.DEVELOPMENTMODE)
				cp = new File(cp, "bin"); //$NON-NLS-1$
			Runtime.getRuntime().addShutdownHook(new FileDeleter(cp, file));
		} catch (Exception e) {
			// can't do anything
		}
	}

	public void registerImageIOPlugins() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "imageIOplugins"); //$NON-NLS-1$
		IIORegistry registry = IIORegistry.getDefaultInstance();
		for (IExtension extension : extensionPoint.getExtensions())
			for (IConfigurationElement conf : extension.getConfigurationElements())
				try {
					registry.registerServiceProvider(conf.createExecutableExtension("class")); //$NON-NLS-1$
				} catch (CoreException e) {
					logError(NLS.bind(Messages.ImageActivator_cannot_intantiate_imageio_provider,
							conf.getAttribute("name")), e); //$NON-NLS-1$
				}
	}

	public IVideoService getVideoService() {
		BundleContext bundleContext = getBundle().getBundleContext();
		ServiceReference<?> serviceReference = bundleContext.getServiceReference(IVideoService.class.getName());
		return (serviceReference != null) ? (IVideoService) bundleContext.getService(serviceReference) : null;
	}

}
