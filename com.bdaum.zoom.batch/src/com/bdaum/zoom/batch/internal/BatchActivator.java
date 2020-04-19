/*******************************************************************************
 * Copyright (c) 2009 Berthold Daum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Berthold Daum - initial API and implementation
 *******************************************************************************/

package com.bdaum.zoom.batch.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;
import com.bdaum.zoom.program.IConverter;
import com.bdaum.zoom.program.IRawConverter;
import com.bdaum.zoom.program.IRawConverter.RawProperty.RawEnum;

@SuppressWarnings("restriction")
public class BatchActivator extends Plugin {

	/**
	 * Utility class for grabbing process outputs.
	 */
	private static class ByteArrayStreamGrabber extends Thread {
		private final InputStream inputStream;
		private byte[] data;
		private String kind;
		private boolean abort;

		public ByteArrayStreamGrabber(final InputStream inputStream, String label, String kind) {
			super(label);
			this.inputStream = inputStream;
			this.kind = kind;
		}

		@Override
		public void run() {
			try {
				byte[] buffer = new byte[4096];
				while (!abort) {
					int read = inputStream.read(buffer);
					if (read < 0)
						break;
					if (data == null) {
						data = new byte[read];
						System.arraycopy(buffer, 0, data, 0, read);
					} else {
						byte[] newData = new byte[data.length + read];
						System.arraycopy(data, 0, newData, 0, data.length);
						System.arraycopy(buffer, 0, newData, data.length, read);
						data = newData;
					}
					if (read < buffer.length)
						break;
				}

			} catch (final IOException e) {
				BatchActivator.getDefault().logError(NLS.bind(Messages.BatchActivator_error_when_reading, kind), e);
			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}

		public byte[] getData() {
			return data;
		}

		public void abort() {
			abort = true;
		}
	}

	public static final String PLUGIN_ID = "com.bdaum.zoom.batch"; //$NON-NLS-1$

	private static BatchActivator plugin;
	private static boolean fastExit = true;

	private String[] exifToolLocation;
	private Map<String, List<IRawConverter>> currentRawConverters = new HashMap<>();
	private boolean rawQuestionAsked;
	private int rawConverterCount;
	private String currentRawConverterId;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static BatchActivator getDefault() {
		return plugin;
	}

	public void logError(String message, Throwable e) {
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}

	public void logWarning(String message, Exception e) {
		getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, e));
	}

	@SuppressWarnings("finally")
	public File convertFile(File file, String converter, String location, Options options, boolean useTempFile,
			IFileWatcher filewatcher, String opId, IProgressMonitor monitor) throws ConversionException {
		BundleContext bundleContext = getBundle().getBundleContext();
		ServiceReference<?> reference = null;
		try {
			ServiceReference<?>[] serviceReferences = bundleContext.getServiceReferences(IConverter.class.getName(),
					new StringBuilder().append("(") //$NON-NLS-1$
							.append(IConverter.CONVERTER).append("=") //$NON-NLS-1$
							.append(converter).append(")").toString()); //$NON-NLS-1$
			if (serviceReferences != null && serviceReferences.length > 0) {
				reference = serviceReferences[0];
				IConverter c = (IConverter) bundleContext.getService(reference);
				if (c == null)
					throw new ConversionException(NLS.bind(Messages.BatchActivator_not_instantiated, converter));
				c.setConverterLocation(location);
				File out = c.setInput(file, options);
				if (out == null)
					return null;
				File backup = null;
				if (useTempFile) {
					if (out.exists())
						try {
							BatchUtilities.moveFile(out,
									backup = ImageActivator.getDefault().createTempFile("Convert", null), monitor); //$NON-NLS-1$
						} catch (DiskFullException e) {
							throw new ConversionException(NLS.bind(Messages.BatchActivator_No_file_conversion, file),
									e);
						}
				} else
					out.delete();
				if (monitor != null && monitor.isCanceled())
					return null;
				String[] parms = c.getParms(options);
				String data = null;
				File temp = null;
				ConversionException cex = null;
				try {
					if (parms == null)
						throw new ConversionException(Messages.BatchActivator_no_dcraw_module_installed);
					synchronized (currentRawConverters) {
						if (filewatcher != null)
							filewatcher.ignore(out, opId);
						data = executeCommand(parms, c.getInputDirectory(), converter, IStatus.INFO, IStatus.INFO,
								180000L, null, monitor);
					}
					if (monitor != null && monitor.isCanceled())
						return null;
					BatchUtilities.yield();
					if (useTempFile && out.exists())
						BatchUtilities.moveFile(out, temp = ImageActivator.getDefault().createTempFile("Convert", null), //$NON-NLS-1$
								monitor);
				} catch (ConversionException e) {
					cex = e;
				} finally {
					if (useTempFile) {
						try {
							if (filewatcher != null)
								filewatcher.ignore(out, opId);
							BatchUtilities.moveFile(backup, out, monitor);
						} catch (DiskFullException e) {
							throw new ConversionException(NLS.bind(Messages.BatchActivator_No_file_conversion, file),
									e);
						}
						if (cex != null)
							throw cex;
						if (data == null)
							return null;
						if (temp != null && temp.exists())
							return temp;
					} else {
						if (cex != null)
							throw cex;
						if (data == null)
							return null;
						if (out.exists())
							return out;
					}
					throw new ConversionException(NLS.bind(Messages.BatchActivator_No_file_conversion, file));
				}
			}
		} catch (InvalidSyntaxException e) {
			// should never happen
		} catch (IOException e) {
			throw new ConversionException(NLS.bind(Messages.BatchActivator_IOerror_converting, file), e);
		} finally {
			if (reference != null)
				bundleContext.ungetService(reference);
		}
		return null;
	}

	public static String executeCommand(String[] parms, File dir, String label, int logLevel, int errorLevel,
			long timeout, String charsetName, IProgressMonitor monitor) throws ConversionException {
		try {
			return BatchUtilities.executeCommand(parms, dir, label, logLevel, errorLevel, 1, timeout, charsetName,
					monitor);
		} catch (ExecutionException e1) {
			throw new ConversionException(NLS.bind(Messages.BatchActivator_conversion_ended_with_error, label),
					e1.getCause());
		} catch (IOException e) {
			throw new ConversionException(NLS.bind(Messages.BatchActivator_Error_when_launching, label), e);
		}
	}

	public static byte[] executeBinaryCommand(String[] parms, String label, int errorLevel, long timeout,
			String charsetName) throws ConversionException {
		ByteArrayStreamGrabber inputGrabber = null;
		StreamCapture errorGrabber = null;
		try {
			Process process = Runtime.getRuntime().exec(parms);
			inputGrabber = new ByteArrayStreamGrabber(process.getInputStream(), label,
					Messages.BatchActivator_output_stream);
			if (errorLevel != IStatus.OK) {
				errorGrabber = new StreamCapture(process.getErrorStream(), charsetName, label,
						Messages.BatchActivator_error_stream, errorLevel);
				errorGrabber.start();
			}
			inputGrabber.start();
			try {
				int ret = process.waitFor();
				if (ret != 0) {
					inputGrabber.join(timeout);
					return inputGrabber.getData();
				}
				if (errorGrabber != null)
					throw new ConversionException(NLS.bind(Messages.BatchActivator_conversion_ended_with_error,
							new Object[] { label, ret, errorGrabber.getData().trim() }));
			} catch (InterruptedException e) {
				if (errorGrabber != null)
					throw new ConversionException(NLS.bind(Messages.BatchActivator_time_limit_exceeded,
							new Object[] { label, errorGrabber.getData().trim(), timeout / 1000 }), e);
			}
		} catch (IOException e) {
			throw new ConversionException(NLS.bind(Messages.BatchActivator_Error_when_launching, label), e);
		} finally {
			if (inputGrabber != null)
				inputGrabber.abort();
			if (errorGrabber != null)
				errorGrabber.abort();
		}
		return null;
	}

	public void runScript(String[] parms, boolean wait) throws IOException, ExecutionException {
		parms[0] = locate(parms[0]);
		if (wait)
			BatchUtilities.executeCommand(parms, null, Messages.BatchActivator_run_script, IStatus.OK, IStatus.ERROR, 2,
					1000L, "UTF-8", null); //$NON-NLS-1$
		else
			Runtime.getRuntime().exec(parms, null, null);
	}

	public String locate(String path) {
		try {
			return FileLocator.findAbsolutePath(getBundle(), path);
		} catch (Exception e) {
			return null;
		}
	}

	public String[] locateExifTool() {
		if (exifToolLocation == null) {
			try {
				String loc = BatchConstants.WIN32 ? FileLocator.findAbsolutePath(getBundle(), "/exiftool.exe") : //$NON-NLS-1$
						FileLocator.findAbsolutePath(getBundle(), "/exiftool/exiftool"); //$NON-NLS-1$
				if (loc == null)
					throw new FileNotFoundException();
				exifToolLocation = BatchConstants.WIN32 ? new String[] { loc } : new String[] { "perl", loc }; //$NON-NLS-1$
			} catch (IOException e) {
				logError(Messages.BatchActivator_could_not_locate_exiftool, e);
				return null;
			}
		}
		return exifToolLocation;
	}

	public static boolean isFastExit() {
		return fastExit;
	}

	public static void setFastExit(boolean fastExit) {
		BatchActivator.fastExit = fastExit;
	}

	public synchronized IRawConverter getRawConverter(String rcId, boolean withDefault, boolean reserve) {
		IRawConverter result = null;
		if (rcId != null)
			synchronized (rcId) {
				List<IRawConverter> list = currentRawConverters.get(rcId);
				if (list != null && !list.isEmpty())
					result = list.remove(list.size() - 1);
				if (result == null)
					for (IExtension ext : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "rawConverter") //$NON-NLS-1$
							.getExtensions())
						for (IConfigurationElement config : ext.getConfigurationElements()) {
							String id = config.getAttribute("id"); //$NON-NLS-1$
							if (id.equals(rcId)) {
								String name = config.getAttribute("name"); //$NON-NLS-1$
								try {
									IRawConverter converter = (IRawConverter) config.createExecutableExtension("class"); //$NON-NLS-1$
									converter.setId(id);
									converter.setName(name);
									converter.setDefault(Boolean.parseBoolean(config.getAttribute("isDefault"))); //$NON-NLS-1$
									configureConverter(config, converter);
									result = converter;
									if (!reserve)
										ungetRawConverter(converter);
									break;
								} catch (CoreException e) {
									logError(NLS.bind(Messages.BatchActivator_cannot_create_raw_converter, name), e);
								}
							}
						}
				if (result == null && withDefault) {
					list = currentRawConverters.get(rcId);
					if (list != null && !list.isEmpty())
						result = list.remove(list.size() - 1);
				}
				if (result == null && withDefault)
					for (IExtension ext : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "rawConverter") //$NON-NLS-1$
							.getExtensions())
						for (IConfigurationElement config : ext.getConfigurationElements()) {
							boolean dflt = Boolean.parseBoolean(config.getAttribute("isDefault")); //$NON-NLS-1$
							if (dflt) {
								String name = config.getAttribute("name"); //$NON-NLS-1$
								try {
									String id = config.getAttribute("id"); //$NON-NLS-1$
									IRawConverter converter = (IRawConverter) config.createExecutableExtension("class"); //$NON-NLS-1$
									converter.setId(id);
									converter.setName(name);
									converter.setDefault(dflt);
									configureConverter(config, converter);
									result = converter;
									if (!reserve)
										ungetRawConverter(converter);
									break;
								} catch (CoreException e) {
									logError(NLS.bind(Messages.BatchActivator_cannot_create_raw_converter, name), e);
								}
							}
						}
			}
		return result;
	}

	private void configureConverter(IConfigurationElement config, IRawConverter converter) {
		converter.setExecutable(config.getAttribute("executable")); //$NON-NLS-1$
		converter.setDetectors(Boolean.parseBoolean(config.getAttribute("detectors"))); //$NON-NLS-1$
		converter.setPathId(config.getAttribute("path")); //$NON-NLS-1$
		converter.setSecondaryId(++rawConverterCount);
		for (IConfigurationElement propElement : config.getChildren()) {
			IRawConverter.RawProperty prop = new IRawConverter.RawProperty(propElement.getAttribute("id"), //$NON-NLS-1$
					propElement.getAttribute("name"), //$NON-NLS-1$
					propElement.getAttribute("type"), //$NON-NLS-1$
					propElement.getAttribute("default"), //$NON-NLS-1$
					propElement.getAttribute("min"), //$NON-NLS-1$
					propElement.getAttribute("max")); //$NON-NLS-1$
			converter.addProperty(prop);
			IConfigurationElement[] enumsElements = propElement.getChildren();
			if (enumsElements.length > 0) {
				List<RawEnum> enums = new ArrayList<IRawConverter.RawProperty.RawEnum>();
				for (IConfigurationElement enumElement : enumsElements)
					enums.add(prop.new RawEnum(enumElement.getAttribute("id"), //$NON-NLS-1$
							enumElement.getAttribute("value"), //$NON-NLS-1$
							Boolean.parseBoolean(enumElement.getAttribute("recipe")))); //$NON-NLS-1$
				prop.enums = enums;
			}
		}
	}

	public void ungetRawConverter(IRawConverter rc) {
		if (rc != null) {
			String rcid = rc.getId();
			List<IRawConverter> list = currentRawConverters.get(rcid);
			if (list == null)
				currentRawConverters.put(rcid, list = new ArrayList<IRawConverter>(3));
			list.add(rc);
		}
	}

	public List<IRawConverter> getRawConverters() {
		List<IRawConverter> rawConverters = new ArrayList<IRawConverter>(3);
		for (IExtension ext : Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "rawConverter") //$NON-NLS-1$
				.getExtensions())
			for (IConfigurationElement config : ext.getConfigurationElements())
				rawConverters.add(getRawConverter(config.getAttribute("id"), false, false)); //$NON-NLS-1$
		return rawConverters;
	}

	/**
	 * Returns an instance of the current raw converter type Instances must be put
	 * back by calling unget() on the instance
	 * 
	 * @param force
	 *            true if existing instances are discarded
	 * @return
	 */
	public synchronized IRawConverter getCurrentRawConverter(boolean force) {
		IPreferencesService preferencesService = Platform.getPreferencesService();
		if (force)
			currentRawConverters.clear();
		if (force || currentRawConverterId == null)
			currentRawConverterId = preferencesService.getString(PLUGIN_ID, PreferenceConstants.RAWCONVERTER, null,
					null);
		IRawConverter rc = getRawConverter(currentRawConverterId, true, true);
		if (rc != null) {
			String path = preferencesService.getString(PLUGIN_ID, rc.getPathId(), null, null);
			if (path != null)
				rc.setPath(path.trim());
			for (IRawConverter.RawProperty prop : rc.getProperties())
				prop.value = preferencesService.getString(PLUGIN_ID, prop.id, null, null);
		}
		return rc;
	}

	public void setCurrentRawConverterId(String id) {
		currentRawConverterId = id;
		BatchUtilities.putPreferences(PreferenceConstants.RAWCONVERTER, id);
	}

	public boolean isRawQuestionAsked() {
		return rawQuestionAsked;
	}

	public void setRawQuestionAsked(boolean rawQuestionAsked) {
		this.rawQuestionAsked = rawQuestionAsked;
	}
}
