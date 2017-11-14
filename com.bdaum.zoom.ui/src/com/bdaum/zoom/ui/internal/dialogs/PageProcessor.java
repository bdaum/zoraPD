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

package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.batch.internal.IFileWatcher;
import com.bdaum.zoom.cat.model.PageLayout_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Ticketbox;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class PageProcessor {
	private static final double MMPERINCH = 25.4d;
	private static final int fontSize = 8;
	private static final int titleSize = 12;
	private static final int subtitleSize = 10;
	private static final int footerSize = 9;

	private PageLayout_typeImpl layout;
	private List<Asset> assets;
	private int w;
	private int h;
	private int pages;
	private Font titleFont;
	private int topMargins;
	private Font subtitleFont;
	private int titlePixelSize;
	private int subtitleLead;
	private int footerPixelSize;
	private int bottomMargins;
	private Font footerFont;
	private int subtitlePixelSize;
	private int imagesPerPage;
	private double imageWidth;
	private double imageHeight;
	private int cellWidth;
	private int cellHeight;
	private int leftMargins;
	private int titleTextSize;
	private Font textFont;
	private int fontPixelSize;
	private int fontLead;
	private int upperWaste;
	private Date now;
	private Meta meta;
	private String fileName;
	private String collection = ""; //$NON-NLS-1$
	private int keyLine;
	private IAdaptable adaptable;
	private int cms;
	private int rightMargins;
	private int seqNo = 0;
	private final UnsharpMask umask;
	protected String opId = java.util.UUID.randomUUID().toString();
	protected IFileWatcher fileWatcher = CoreActivator.getDefault().getFileWatchManager();
	protected boolean aborted;
	private ZImage swtImage;
	private Map<URI, File> tempFiles = new HashMap<URI, File>();
	private double iDpi;
	private double dpiX;
	private double dpiY;

	public PageProcessor(PrinterData printerData, PageLayout_typeImpl layout, List<Asset> assets, int w, int h,
			double factorX, double factorY, Rectangle trim, Point printerDpi, int cms, UnsharpMask umask,
			IAdaptable adaptable) {
		this.umask = umask;
		this.adaptable = adaptable;
		this.layout = layout;
		this.assets = assets;
		this.w = w;
		this.h = h;
		this.cms = cms;
		dpiX = printerDpi.x * factorX;
		dpiY = printerDpi.y * factorY;
		iDpi = Math.min(dpiX, dpiY);
		double minAspectRatio = Double.MAX_VALUE;
		double maxAspectRatio = 0;
		for (Asset asset : assets) {
			Double aspectRatio = (Double) QueryField.ASPECTRATIO.obtainFieldValue(asset);
			if (aspectRatio != null) {
				maxAspectRatio = Math.max(maxAspectRatio, aspectRatio);
				minAspectRatio = Math.min(minAspectRatio, aspectRatio);
			}
		}
		minAspectRatio = (minAspectRatio + maxAspectRatio) / 2;
		keyLine = (int) (layout.getKeyLine() * iDpi / 144);
		fontPixelSize = (int) (fontSize * iDpi / 72);
		fontLead = fontPixelSize / 3;
		titlePixelSize = 0;
		int titleLead = 0;
		if (!layout.getTitle().isEmpty()) {
			titlePixelSize = (int) (titleSize * iDpi / 72);
			titleLead = titlePixelSize;
		}
		subtitlePixelSize = 0;
		subtitleLead = 0;
		if (!layout.getSubtitle().isEmpty()) {
			subtitlePixelSize = (int) (subtitleSize * iDpi / 72);
			subtitleLead = subtitlePixelSize;
		}
		titleTextSize = titlePixelSize + titleLead + subtitlePixelSize + subtitleLead;
		footerPixelSize = (int) (footerSize * iDpi / 72);
		int footerLead = footerPixelSize;
		int footerTextSize = footerPixelSize + footerLead;
		int textHeight = 0;
		if (!layout.getCaption1().isEmpty())
			textHeight += fontPixelSize + fontLead;
		if (!layout.getCaption2().isEmpty())
			textHeight += fontPixelSize + fontLead;
		int horizontalGap = (int) (layout.getHorizontalGap() * iDpi / MMPERINCH);
		leftMargins = (int) Math.max(layout.getLeftMargin() * iDpi / MMPERINCH, -trim.x * factorX * iDpi / dpiX);
		rightMargins = (int) Math.max(layout.getRightMargin() * iDpi / MMPERINCH,
				(trim.width + trim.x) * factorX * iDpi / dpiX);
		topMargins = (int) Math.max(layout.getTopMargin() * iDpi / MMPERINCH, -trim.y * factorY * iDpi / dpiY);
		bottomMargins = (int) Math.max(layout.getBottomMargin() * iDpi / MMPERINCH,
				(trim.height + trim.y) * factorY * iDpi / dpiY);
		int verticalGap = (int) (layout.getVerticalGap() * iDpi / MMPERINCH);
		int useableWidth = w - leftMargins - rightMargins;
		cellWidth = (useableWidth + horizontalGap) / layout.getColumns();
		float useableHeight = h - topMargins - bottomMargins - titleTextSize - footerTextSize;
		imageWidth = cellWidth - horizontalGap - keyLine;
		float maxImageHeight = useableHeight - textHeight - verticalGap - keyLine;
		imageHeight = imageWidth / minAspectRatio;
		while (imageHeight > maxImageHeight) {
			imageWidth *= 0.99d;
			imageHeight = imageWidth / minAspectRatio;
		}
		cellHeight = (int) (imageHeight + textHeight + verticalGap + keyLine);
		int rows = (int) ((useableHeight + verticalGap) / cellHeight);
		int usedSpace = rows * cellHeight;
		upperWaste = (int) ((useableHeight - usedSpace) / 2);
		imagesPerPage = layout.getColumns() * rows;
		int size = assets.size();
		pages = (size + imagesPerPage - 1) / imagesPerPage;
		now = new Date();
		ICore core = Core.getCore();
		IDbManager dbManager = core.getDbManager();
		meta = dbManager.getMeta(true);
		fileName = dbManager.getFileName();
		IAssetProvider assetProvider = core.getAssetProvider();
		if (assetProvider != null)
			collection = Utilities.getExternalAlbumName(assetProvider.getCurrentCollection());
	}

	private ZImage loadHighresImage(final Device device, Ticketbox box, SubMonitor progress, final Asset asset) {
		URI uri = Core.getCore().getVolumeManager().findExistingFile(asset, false);
		if (uri != null) {
			MultiStatus status = new MultiStatus(UiActivator.PLUGIN_ID, 0, Messages.PageProcessor_page_rending_report,
					null);
			File file = null;
			if (Constants.FILESCHEME.equals(uri.getScheme()))
				file = new File(uri);
			else {
				file = tempFiles.get(uri);
				if (file == null) {
					try {
						file = box.download(uri);
						tempFiles.put(uri, file);
					} catch (IOException e) {
						status.add(new Status(IStatus.ERROR, UiActivator.PLUGIN_ID,
								NLS.bind(Messages.PageProcessor_download_failed, uri), e));
					}
				}
			}
			if (file != null) {
				int pixelWidth = (int) (imageWidth * iDpi / 25.4d);
				int pixelHeight = (int) (imageHeight * iDpi / 25.4d);
				boolean r = asset.getRotation() % 180 != 0;
				double w = r ? asset.getHeight() : asset.getWidth();
				double h = r ? asset.getWidth() : asset.getHeight();
				double scale = w == 0 || h == 0 ? 1d : (2 * pixelWidth <= w && 2 * pixelHeight <= h) ? 0.5d : 1d;
				ZImage hrImage = null;
				try {
					hrImage = CoreActivator.getDefault().getHighresImageLoader().loadImage(
							progress != null ? progress.newChild(1000) : null, status, file, asset.getRotation(),
							asset.getFocalLengthIn35MmFilm(), null, scale, Double.MAX_VALUE, true, ImageConstants.SRGB,
							null, umask, null, fileWatcher, opId, null);
					if (status.isOK())
						UiActivator.getDefault().getLog().log(status);
				} catch (UnsupportedOperationException e) {
					// Do nothing
				}
				return hrImage;
			}
		}
		return null;
	}

	public boolean render(final Device device, final GC gc, final int pageNo, final IProgressMonitor jmon) {
		if (imageWidth <= 0) {
			AcousticMessageDialog.openError(adaptable.getAdapter(Shell.class), Messages.PageProcessor_printing_error,
					Messages.PageProcessor_margins_too_large);
			return true;
		}
		if (pageNo > pages)
			return true;
		aborted = false;
		Color white = device.getSystemColor(SWT.COLOR_WHITE);
		gc.setBackground(white);
		gc.fillRectangle(0, 0, w, h);
		final Image canvas = new Image(device, (int) (w * iDpi / dpiX), (int) (h * iDpi / dpiY));
		Rectangle cBounds = canvas.getBounds();
		final GC iGc = new GC(canvas);
		try {
			iGc.setBackground(white);
			iGc.setForeground(device.getSystemColor(SWT.COLOR_DARK_GRAY));
			iGc.fillRectangle(cBounds);
			final int size = assets.size();
			if (!layout.getTitle().isEmpty()) {
				if (titleFont == null)
					titleFont = createFont(device, titleSize, SWT.BOLD);
				iGc.setFont(titleFont);
				String title = computeTitle(layout.getTitle(), fileName, now, size, pageNo, pages, collection, meta);
				iGc.drawText(title, (cBounds.width - iGc.textExtent(title).x) / 2, topMargins, true);
			}
			if (!layout.getSubtitle().isEmpty()) {
				if (subtitleFont == null)
					subtitleFont = createFont(device, subtitleSize, SWT.NORMAL);
				iGc.setFont(subtitleFont);
				String subtitle = computeTitle(layout.getSubtitle(), fileName, now, size, pageNo, pages, collection,
						meta);
				iGc.drawText(subtitle, (cBounds.width - iGc.textExtent(subtitle).x) / 2,
						topMargins + titlePixelSize + subtitleLead, true);
			}
			if (!layout.getFooter().isEmpty()) {
				if (footerFont == null)
					footerFont = createFont(device, footerSize, SWT.NORMAL);
				iGc.setFont(footerFont);
				String footer = computeTitle(layout.getFooter(), fileName, now, size, pageNo, pages, collection, meta);
				iGc.drawText(footer, (cBounds.width - iGc.textExtent(footer).x) / 2,
						cBounds.height - bottomMargins - footerPixelSize, true);
			}
			final Ticketbox box = new Ticketbox();
			Runnable runnable = new Runnable() {
				public void run() {
					SubMonitor progress = jmon == null ? null : SubMonitor.convert(jmon, 1000 * size);
					try {
						if (progress != null)
							progress.beginTask(NLS.bind(Messages.PageProcessor_rendering_images, imagesPerPage),
									1000 * imagesPerPage);
						final int pixelWidth = (int) (imageWidth * iDpi / 72);
						final int pixelHeight = (int) (imageHeight * iDpi / 72);
						int pageItem = 0;
						int i = 0;
						lp: while (true) {
							for (int j = 0; j < layout.getColumns(); j++) {
								if (progress != null && progress.isCanceled())
									break lp;
								int n = i * layout.getColumns() + j;
								if (n >= imagesPerPage)
									break lp;
								int a = (pageNo - 1) * imagesPerPage + n;
								if (a >= size)
									break lp;
								++pageItem;
								++seqNo;
								if (swtImage != null) {
									swtImage.dispose();
									swtImage = null;
								}
								Asset asset = assets.get(a);
								if (device instanceof Printer)
									swtImage = loadHighresImage(device, box, progress, asset);
								if (swtImage == null)
									swtImage = new ZImage(ImageUtilities.loadThumbnail(device, asset.getJpegThumbnail(),
											cms, SWT.IMAGE_JPEG, true), null);
								Rectangle ibounds = swtImage.getBounds();
								double factor = Math.min(imageWidth / ibounds.width, imageHeight / ibounds.height);
								int iw = (int) (ibounds.width * factor);
								int ih = (int) (ibounds.height * factor);
								int cx;
								if (layout.getFacingPages() && pageNo % 2 == 0)
									cx = cellWidth * j + rightMargins;
								else
									cx = cellWidth * j + leftMargins;
								int cy = cellHeight * i + topMargins + titleTextSize + upperWaste;
								int ix = cx + (cellWidth - iw) / 2;
								int iy = cy + (cellHeight - ih) / 2;
								if (keyLine > 0) {
									iGc.setLineWidth(keyLine);
									iGc.drawRectangle(ix - (keyLine + 1) / 2, iy - (keyLine + 2) / 2, iw + keyLine,
											ih + keyLine);
								}
								swtImage.draw(iGc, 0, 0, ibounds.width, ibounds.height - 1, ix, iy, iw, ih,
										ZImage.CROPPED, pixelWidth, pixelHeight, false);
								if (!layout.getCaption1().isEmpty()) {
									if (textFont == null)
										textFont = createFont(device, fontSize, SWT.NORMAL);
									iGc.setFont(textFont);
									String caption1 = computeCaption(layout.getCaption1(), Constants.PI_ALL, asset,
											collection, seqNo, pageItem);
									int tx = (cx + (cellWidth - iGc.textExtent(caption1).x) / 2);
									int ty = (iy + ih + fontLead + keyLine);
									iGc.drawText(caption1, tx, ty, true);
								}
								if (!layout.getCaption2().isEmpty()) {
									if (textFont == null)
										textFont = createFont(device, fontSize, SWT.NORMAL);
									iGc.setFont(textFont);
									String caption2 = computeCaption(layout.getCaption2(), Constants.PI_ALL, asset,
											collection, seqNo, pageItem);
									int tx = (cx + (cellWidth - iGc.textExtent(caption2).x) / 2);
									int ty = (iy + ih + 2 * fontLead + fontPixelSize + keyLine);
									iGc.drawText(caption2, tx, ty, true);
								}
								swtImage.dispose();
								swtImage = null;
							}
							++i;
						}
					} finally {
						fileWatcher.stopIgnoring(opId);
						box.endSession();
						aborted = progress != null && progress.isCanceled();
					}
				}
			};
			if (device instanceof Display)
				BusyIndicator.showWhile((Display) device, runnable);
			else
				runnable.run();
			gc.drawImage(canvas, 0, 0, cBounds.width, cBounds.height, 0, 0, w, h);
		} finally {
			iGc.dispose();
			canvas.dispose();
		}
		return aborted;
	}

	public static String computeTitle(String template, String catname, Date date, int count, int pageNo, int pages,
			String collection, Meta meta) {
		StringBuilder sb = new StringBuilder(template);
		for (String tv : Constants.PT_ALL)
			while (true) {
				int p = sb.indexOf(tv);
				if (p < 0)
					break;
				replaceTitleContent(sb, p, tv, catname, date, count, pageNo, pages, collection, meta);
			}
		return sb.toString();
	}

	private static void replaceTitleContent(StringBuilder sb, int p, String tv, String catname, Date date, int count,
			int pageNo, int pages, String collection, Meta meta) {
		String value = ""; //$NON-NLS-1$
		if (tv == Constants.PT_CATALOG)
			value = catname;
		else if (tv == Constants.PT_TODAY)
			value = new SimpleDateFormat(Messages.PageProcessor_yyyymd).format(date);
		else if (tv == Constants.PT_COUNT)
			value = String.valueOf(count);
		else if (tv == Constants.PT_PAGENO)
			value = String.valueOf(pageNo);
		else if (tv == Constants.PT_PAGECOUNT)
			value = String.valueOf(pages);
		else if (tv == Constants.PT_COLLECTION)
			value = collection;
		else if (tv == Constants.PT_USER)
			value = System.getProperty("user.name"); //$NON-NLS-1$
		else if (tv == Constants.PT_OWNER)
			value = meta.getOwner();
		sb.replace(p, p + tv.length(), value);
	}

	public static String computeCaption(String template, String[] variables, Asset asset, String collection,
			int sequenceNo, int pageItem) {
		return Utilities.evaluateTemplate(template, Constants.PI_ALL, null, null, 0, pageItem, sequenceNo, null, asset,
				collection, Integer.MAX_VALUE, false);
	}

	private static Font createFont(Device device, int size, int style) {
		return new Font(device, device.getSystemFont().getFontData()[0].getName(), size, style);
	}

	public int getPageCount() {
		return pages;
	}

	public void dispose() {
		if (swtImage != null)
			swtImage.dispose();
		if (titleFont != null)
			titleFont.dispose();
		if (subtitleFont != null)
			subtitleFont.dispose();
		if (footerFont != null)
			footerFont.dispose();
		if (textFont != null)
			textFont.dispose();
		for (File tempFile : tempFiles.values())
			tempFile.delete();
	}

	public int getImagesPerPage() {
		return imagesPerPage;
	}

}
