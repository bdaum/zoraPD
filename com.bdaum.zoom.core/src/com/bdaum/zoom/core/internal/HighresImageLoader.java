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

package com.bdaum.zoom.core.internal;

import java.awt.image.ColorConvertOp;
import java.io.File;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.batch.internal.ConversionException;
import com.bdaum.zoom.batch.internal.Daemon;
import com.bdaum.zoom.batch.internal.ExifTool;
import com.bdaum.zoom.batch.internal.IFileWatcher;
import com.bdaum.zoom.batch.internal.LoaderListener;
import com.bdaum.zoom.batch.internal.Options;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.image.IFocalLengthProvider;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.recipe.Recipe;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.IRawConverter;

@SuppressWarnings("restriction")
public class HighresImageLoader {
	public class CleanerJob extends Daemon {
		public CleanerJob() {
			super(Messages.HighresImageLoader_cleaning_temp, BatchConstants.MAXTEMPFILEAGE * 2);
		}

		@Override
		protected void doRun(IProgressMonitor monitor) {
			cleanDirectory();
		}
	}

	public static class ImageEntry {

		private final File converted;
		private long lastAccess;

		public ImageEntry(File converted) {
			this.converted = converted;
			lastAccess = System.currentTimeMillis();
		}

		/**
		 * @return the converted
		 */
		public File getConverted() {
			lastAccess = System.currentTimeMillis();
			return converted;
		}

		public long getLastAccess() {
			return lastAccess;
		}
	}

	public class ImageKey {

		private final File original;
		private final Options options;
		private long lastModified;
		private final String rawConverterId;
		private final int secondaryId;

		public ImageKey(File original, Options options, String rawConverterId, int secondaryId) {
			this.options = options;
			this.rawConverterId = rawConverterId;
			this.secondaryId = secondaryId;
			lastModified = BatchUtilities.getImageFileModificationTimestamp(this.original = original);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ImageKey)
				return ((ImageKey) obj).original.equals(original) && ((ImageKey) obj).options.equals(options)
						&& ((ImageKey) obj).rawConverterId.equals(rawConverterId) && ((ImageKey) obj).secondaryId == secondaryId;
			return false;
		}

		@Override
		public int hashCode() {
			return ((original.hashCode() * 31 + options.hashCode()) * 31 + rawConverterId.hashCode()) * 31 + secondaryId;
		}

		public boolean isOutdated(ImageEntry entry) {
			return !entry.getConverted().exists()
					|| BatchUtilities.getImageFileModificationTimestamp(original) > lastModified;
		}

	}

	private long timestamp;
	private boolean PROFILING = false;
	private int total;
	private int worked;
	private Hashtable<ImageKey, ImageEntry> imageDirectory = new Hashtable<HighresImageLoader.ImageKey, ImageEntry>(
			257);

	protected HighresImageLoader() {
		new CleanerJob().schedule(BatchConstants.MAXTEMPFILEAGE * 2);
	}

	/**
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the user. It
	 *            is the caller's responsibility to call done() on the given
	 *            monitor. Accepts null, indicating that no progress should be
	 *            reported and that the operation cannot be cancelled.
	 * @param status
	 *            the status object receiving error messages
	 * @param file
	 *            the image file being displayed
	 * @param rotation
	 *            - rotation of the image
	 * @param flen35mm
	 *            - focal length equivalent to 35mm film
	 * @param bounds
	 *            - desired size, if null, scalingFactor applies; to scale only for
	 *            height, set bounds.width negative; to scale only for width, set
	 *            bounds.height negative. Note that the absolute values of width and
	 *            height are also used to determine the orientation
	 * @param scalingFactor
	 *            factor applies if bounds == null set to 0 for full resolution
	 *            without subsampling
	 * @param maxFactor
	 *            maxFactor limits the scaling factor when bounds != null
	 * @param advanced
	 *            set true for advanced graphics (quality interpolation)
	 * @param cms
	 *            specifies the output color profile (ImageConstants.ARGB,
	 *            ImageConstants.SRGB, ImageConstants.NOCMS)
	 * @param bw
	 *            set to an RGB value to force b&w rendering using this value as
	 *            filter
	 * @param umask
	 *            unsharp mask for output sharpening or null
	 * @param fileWatcher
	 *            filewatcher instance for blocking files to be watched
	 * @param opId
	 *            unique operation ID
	 * @param listener
	 *            loader listener to watch for loading events or null
	 * @param recipeOrRecipeProvider
	 *            RAW recipe or a RAW recipe provider or null
	 * @return the loaded image
	 * @throws UnsupportedOperationException
	 *             if file format is not supported
	 */
	public ZImage loadImage(IProgressMonitor monitor, MultiStatus status, File file, int rotation,
			final double flen35mm, Rectangle bounds, double scalingFactor, double maxFactor, boolean advanced, int cms,
			RGB bw, UnsharpMask umask, Recipe recipe, IFileWatcher fileWatcher, String opId, LoaderListener listener)
			throws UnsupportedOperationException {
		IRawConverter rawConverter = null;
		try {
			total = 1000;
			worked = 0;
			if (monitor != null)
				monitor.beginTask(Messages.HighresImageLoader_Loading_image, total);
			if (bw != null)
				cms = ImageConstants.NOCMS;
			Recipe rawRecipe = null;
			String name = file.getName();
			int p = name.lastIndexOf('.');
			String uriString = file.toURI().toString();
			String extension = (p >= 0) ? name.substring(p + 1).toLowerCase()
					: ImageUtilities.detectImageFormat(uriString);
			if (!ImageConstants.getAllFormats().contains(extension))
				throw new UnsupportedOperationException(
						NLS.bind(Messages.HighresImageLoader_image_format_not_supported, file));
			File workfile = file;
			boolean isRawOrDng = ImageConstants.isRaw(name, true);
			profile(null);
			if (isRawOrDng) {
				BatchActivator activator = BatchActivator.getDefault();
				rawConverter = activator.getCurrentRawConverter(false);
				if (rawConverter == null || !rawConverter.isValid()) {
					if (!activator.isRawQuestionAsked()) {
						rawConverter = Core.getCore().getDbFactory().getErrorHandler().showRawDialog(null);
						activator.setRawQuestionAsked(true);
					}
					if (rawConverter == null || !rawConverter.isValid()) {
						addErrorStatus(status, Messages.HighresImageLoader_no_raw_converter, null);
						return null;
					}
				}
				if (rawConverter.isDetectors()) {
					rawRecipe = recipe != null ? recipe
							: rawConverter.getRecipe(uriString, false, new IFocalLengthProvider() {
								public double get35mm() {
									return flen35mm;
								}
							}, null, null);
					if (rawRecipe == Recipe.NULL)
						rawRecipe = null;
				}
				Options options = new Options();
				int factor = rawConverter.deriveOptions(rawRecipe, options,
						bounds != null || scalingFactor >= 0.5d || scalingFactor == 0
								|| (rawRecipe != null && rawRecipe.getCropping() != null) ? IRawConverter.HIGH
										: IRawConverter.MEDIUM);
				scalingFactor *= factor;
				if (rawRecipe != null) {
					rawRecipe.setScaling(scalingFactor > 0 ? (float) scalingFactor : 1f);
					rawRecipe.setSampleFactor(factor);
				}
				// if (cms == ImageConstants.ARGB) // better to work in the camera color space,
				// not all raw converts support color conversion
				// options.put(IConverter.ADOBE_RGB, Boolean.TRUE);
				workfile = null;
				ImageKey key = new ImageKey(file, options, rawConverter.getId(), rawConverter.getSecondaryId());
				ImageEntry entry = imageDirectory.get(key);
				if (entry != null) {
					if (key.isOutdated(entry))
						imageDirectory.remove(key);
					else
						workfile = entry.getConverted();
				}
				if (workfile == null) {
					try {
						workfile = activator.convertFile(file, rawConverter.getId(), rawConverter.getPath(), options,
								true, fileWatcher, opId, monitor);
						if (workfile == null) {
							addErrorStatus(status, NLS.bind(Messages.HighresImageLoader_DCRAWconversion_failed, file),
									null);
							return null;
						}
						imageDirectory.put(key, new ImageEntry(workfile));
						if (reportProgress(monitor, Messages.HighresImageLoader_DCRAW_conversion, listener))
							return null;
					} catch (ConversionException e) {
						addErrorStatus(status, NLS.bind(Messages.HighresImageLoader_DCRAWconversion_failed, file), e);
						return null;
					}
				}
				extension = "tif"; //$NON-NLS-1$
			}
			ColorConvertOp cop = cms != ImageConstants.NOCMS ? computeColorConvertOp(workfile, cms) : null;
			try {
				return ZImage.loadImage(workfile, extension, rotation, bounds, scalingFactor, maxFactor, advanced, bw,
						umask, cop, rawRecipe);
			} catch (OutOfMemoryError e) {
				addErrorStatus(status, NLS.bind(Messages.HighresImageLoader_Not_enough_memory_to_open, file), e);
				return null;
			} catch (Exception e) {
				addErrorStatus(status, NLS.bind(Messages.HighresImageLoader_error_loading_image, file), e);
				return null;
			}
		} finally {
			CoreActivator.getDefault().ungetHighresImageLoader(this);
			if (rawConverter != null)
				rawConverter.unget();
		}
	}

	private static ColorConvertOp computeColorConvertOp(File workfile, int cms) {
		try (ExifTool exifTool = new ExifTool(workfile, true)) {
			return ImageActivator.getDefault().computeColorConvertOp(exifTool.getICCProfile(), cms);
		}
	}

	private boolean reportProgress(IProgressMonitor monitor, String profile, LoaderListener listener) {
		if (profile != null)
			profile(profile);
		int incr = (total - worked) / 2;
		worked += incr;
		if (monitor != null)
			monitor.worked(incr);
		if (listener != null && listener.progress(total, worked)) {
			if (monitor != null)
				monitor.setCanceled(true);
			return true;
		}
		return monitor != null && monitor.isCanceled();
	}

	private void profile(String label) {
		if (PROFILING) {
			long time = System.currentTimeMillis();
			System.out.println(label == null ? "-------------" : label + ": " + (time - timestamp)); //$NON-NLS-1$ //$NON-NLS-2$
			timestamp = time;
		}
	}

	private static void addErrorStatus(MultiStatus status, String message, Throwable t) {
		status.add(new Status(IStatus.ERROR, BatchActivator.PLUGIN_ID, message, t));
	}

	@SuppressWarnings("unchecked")
	protected void cleanDirectory() {
		long maxAge = System.currentTimeMillis() - BatchConstants.MAXTEMPFILEAGE;
		for (Entry<ImageKey, ImageEntry> entry : imageDirectory.entrySet()
				.toArray(new Entry[imageDirectory.entrySet().size()])) {
			ImageEntry imageEntry = entry.getValue();
			if (imageEntry.getLastAccess() < maxAge) {
				File converted = imageEntry.getConverted();
				if (converted.exists()) {
					if (converted.delete())
						imageDirectory.remove(entry.getKey());
				} else
					imageDirectory.remove(entry.getKey());
			}
		}
	}

}
