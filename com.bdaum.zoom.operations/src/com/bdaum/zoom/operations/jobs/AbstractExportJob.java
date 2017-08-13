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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */
package com.bdaum.zoom.operations.jobs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMeta;
import com.adobe.xmp.XMPMetaFactory;
import com.bdaum.zoom.batch.internal.IFileWatcher;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.operations.internal.job.SourceAndTarget;
import com.bdaum.zoom.operations.internal.xmp.XMPUtilities;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;

@SuppressWarnings("restriction")
public abstract class AbstractExportJob extends CustomJob {

	protected double scale;
	protected int mode;
	protected List<Asset> assets;
	protected int maxSize;
	protected final Set<QueryField> xmpFilter;
	protected final IAdaptable adaptable;
	protected final boolean createWatermark;
	protected final String copyright;
	protected final UnsharpMask umask;
	protected String opId = java.util.UUID.randomUUID().toString();
	protected IFileWatcher fileWatcher = CoreActivator.getDefault().getFileWatchManager();
	private int jpegQuality;
//	private int scalingMethod;
	private int sizing;

	/**
	 * @param name
	 *            - job name
	 * @param assets
	 *            - images to be exported
	 * @param mode
	 *            - scaling mode (Constants.SCALE_ORIGINALS,
	 *            Constants.SCALE_PREVIEW, Constants.SCALE_FIXED)
	 * @param scale
	 *            - scaling factor
	 * @param maxSize
	 *            - maximum size in pixels
	 * @param umask
	 *            - unsharp mask
	 * @param jpegQuality
	 *            - the JPEG compression quality (1..100)
	 * @param xmpFilter
	 *            - set of XMP properties to be exported with each image, or
	 *            null
	 * @param createWatermark
	 *            - true if watermark is to be created
	 * @param copyright
	 *            - copyright notice for watermark
	 * @param rating
	 *            - privacy level (QueryField.SAFETY_SAFE,
	 *            QueryField.SAFETY_MODERATE, QueryField.SAFETY_RESTRICTED)
	 * @param adaptable
	 *            - Adaptable instance answering at least Shell.class
	 */
	public AbstractExportJob(String name, List<Asset> assets, int mode, int sizing, double scale, int maxSize,
			UnsharpMask umask, int jpegQuality, Set<QueryField> xmpFilter, boolean createWatermark, String copyright,
			int rating, IAdaptable adaptable) {
		super(name);
		this.sizing = sizing;
		this.umask = umask;
//		this.scalingMethod = scalingMethod;
		this.jpegQuality = jpegQuality;
		if (rating == QueryField.SAFETY_RESTRICTED)
			this.assets = assets;
		else {
			List<Asset> filteredAssets = new ArrayList<Asset>();
			for (Asset asset : assets)
				if (asset.getSafety() <= rating)
					filteredAssets.add(asset);
			this.assets = filteredAssets;
		}
		this.mode = mode;
		this.scale = scale;
		this.maxSize = maxSize;
		this.xmpFilter = xmpFilter;
		this.createWatermark = createWatermark;
		this.copyright = copyright;
		this.adaptable = adaptable;
		setPriority(Job.LONG);
	}

	@Override
	protected IStatus runJob(IProgressMonitor monitor) {
		try {
			return doRun(monitor);
		} finally {
			fileWatcher.stopIgnoring(opId);
		}
	}

	/**
	 * Performs the operation
	 *
	 * @param monitor
	 *            - progress monitor
	 * @return - result status
	 */
	protected abstract IStatus doRun(IProgressMonitor monitor);

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */

	@Override
	public boolean belongsTo(Object family) {
		return Constants.OPERATIONJOBFAMILY == family || Constants.CRITICAL == family;
	}

	protected SourceAndTarget copyImage(MultiStatus status, Asset asset, File file, File outfile,
			IProgressMonitor monitor) {
		try {
			BatchUtilities.copyFile(file, outfile, monitor);
			return new SourceAndTarget(asset, outfile, null);
		} catch (IOException e) {
			status.add(new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID,
					NLS.bind(Messages.getString("AbstractExportJob.io_error_when_copying"), file), e)); //$NON-NLS-1$
		} catch (DiskFullException e) {
			status.add(new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID,
					Messages.getString("AbstractExportJob.disk_full"), e)); //$NON-NLS-1$
		}
		return null;
	}

	protected SourceAndTarget downScaleImage(MultiStatus status, SubMonitor progress, Asset asset, File file,
			File outfile, double maxFactor, int cropMode) {
		return downScaleImage(status, progress, asset, file, outfile, maxFactor, cropMode, ImageConstants.SRGB);
	}

	protected SourceAndTarget downScaleImage(MultiStatus status, SubMonitor progress, Asset asset, File file,
			File outfile, double maxFactor, int cropMode, int cms) {
		double s = scale;
		if (sizing == Constants.SCALE_FIXED)
			s = Math.min(1, (double) maxSize / Math.max(asset.getWidth(), asset.getHeight()));
		ZImage zimage = null;
		if (file != null) {
			try {
				zimage = CoreActivator.getDefault().getHighresImageLoader().loadImage(progress.newChild(1000), status,
						file, asset.getRotation(), asset.getFocalLengthIn35MmFilm(), null, s, maxFactor, true,
						ImageConstants.SRGB, null, umask, null, fileWatcher, opId, null);
			} catch (UnsupportedOperationException e) {
				// do nothing
			}
		} else {
			zimage = new ZImage(ImageUtilities.loadThumbnail(Display.getDefault(), asset.getJpegThumbnail(), cms,
					SWT.IMAGE_JPEG, true), null);
			s = zimage.setScaling(maxSize, maxSize, true, 0, null); //, ZImage.SCALE_DEFAULT);
			progress.worked(1000);
		}
		if (zimage != null) {
			if (createWatermark) {
				Image image = zimage.getSwtImage(Display.getDefault(), true, cropMode, SWT.DEFAULT, SWT.DEFAULT);
				String c = asset.getCopyright();
				if (c == null || c.isEmpty())
					c = copyright;
				Image outImage = ImageUtilities.addWatermark(image, c);
				if (outImage != image) {
					image.dispose();
					image = outImage;
				}
				ZImage watermarked = new ZImage(image, zimage.getFileName());
				zimage.dispose(image);
				zimage = watermarked;
				cropMode = ZImage.UNCROPPED;
			}
			Rectangle bounds = zimage.getBounds();
			try {
				if (outfile != null)
					outfile.createNewFile();
				else
					outfile = ImageActivator.getDefault().createTempFile("Img", //$NON-NLS-1$
							mode == Constants.FORMAT_WEBP ? ".webp" : ".jpg"); //$NON-NLS-1$ //$NON-NLS-2$
				try (FileOutputStream out = new FileOutputStream(outfile)) {
					if (xmpFilter != null && !xmpFilter.isEmpty() && mode == Constants.FORMAT_JPEG) {
						ByteArrayOutputStream bout = new ByteArrayOutputStream();
						zimage.saveToStream(null, true, cropMode, SWT.DEFAULT, SWT.DEFAULT, bout, SWT.IMAGE_JPEG,
								jpegQuality);
						byte[] bytes = bout.toByteArray();
						try {
							XMPUtilities.configureXMPFactory();
							XMPMeta xmpMeta = XMPMetaFactory.create();
							ByteArrayOutputStream mout = new ByteArrayOutputStream();
							XMPUtilities.writeProperties(xmpMeta, asset, xmpFilter, false);
							XMPMetaFactory.serialize(xmpMeta, mout);
							byte[] metadata = mout.toByteArray();
							bytes = XMPUtilities.insertXmpIntoJPEG(bytes, metadata);
						} catch (XMPException e) {
							addErrorStatus(status, NLS.bind(Messages.getString("AbstractExportJob.xmp_error"), file), //$NON-NLS-1$
									e);
						}
						out.write(bytes);
					} else
						zimage.saveToStream(null, true, cropMode, SWT.DEFAULT, SWT.DEFAULT, out,
								mode == Constants.FORMAT_WEBP ? ZImage.IMAGE_WEBP : SWT.IMAGE_JPEG, jpegQuality);
					return new SourceAndTarget(asset, outfile, bounds);
				}
			} catch (FileNotFoundException e) {
				// should not happen
			} catch (IOException e) {
				addErrorStatus(status, NLS.bind(Messages.getString("AbstractExportJob.io_error"), file), e); //$NON-NLS-1$
			} finally {
				zimage.dispose();
			}
		}
		return null;
	}

	public static void addErrorStatus(MultiStatus status, String message, Throwable t) {
		status.add(new Status(IStatus.ERROR, CoreActivator.PLUGIN_ID, message, t));
	}

	public static File makeUniqueTargetFile(File targetFolder, URI uri, int mode) {
		String name = Core.getFileName(uri, false);
		return BatchUtilities.makeUniqueFile(targetFolder, name,
				mode == Constants.FORMAT_ORIGINAL ? Core.getFileName(uri, true).substring(name.length())
						: mode == Constants.FORMAT_WEBP ? ".webp" : ".jpg"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}