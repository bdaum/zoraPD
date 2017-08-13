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

package com.bdaum.zoom.email.internal.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

import com.bdaum.zoom.batch.internal.IFileWatcher;
import com.bdaum.zoom.cat.model.PageLayout_type;
import com.bdaum.zoom.cat.model.PageLayout_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Ticketbox;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.email.internal.Activator;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.ImageUtilities;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.internal.ImageActivator;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.ui.internal.dialogs.PageProcessor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@SuppressWarnings("restriction")
public class PdfJob extends CustomJob {

	private static final int PRINTERDPI = 300;
	private static final int SCREENDPI = 120;
	private static final float MMPERINCH = 25.4f;
	private static final int fontSize = 7;
	private static final int titleSize = 11;
	private static final int subtitleSize = 9;
	private static final int footerSize = 8;
	private static final Rectangle[] PAGEFORMATS = new Rectangle[] { PageSize.A0, PageSize.A1, PageSize.A2, PageSize.A3,
			PageSize.A4, PageSize.A5, PageSize.A6, PageSize.B0, PageSize.B1, PageSize.B2, PageSize.B3, PageSize.B4,
			PageSize.B5, PageSize.B6, PageSize.B7, PageSize.LETTER, PageSize.HALFLETTER, PageSize.LEGAL,
			PageSize._11X17 };
	private List<Asset> assets;
	private PageLayout_typeImpl layout;
	private File targetFile;
	private final int quality;
	private final int jpegQuality;
	private float keyLine;
	private float fontLead;
	private int titlePixelSize;
	private int subtitlePixelSize;
	private int subtitleLead;
	private int titleTextSize;
	private float leftMargins;
	private float rightMargins;
	private float topMargins;
	private float bottomMargins;
	private Rectangle format;
	private float cellWidth;
	private double imageWidth;
	private double imageHeight;
	private float cellHeight;
	private float upperWaste;
	private int imagesPerPage;
	private int pages;
	private Date now;
	private Meta meta;
	private String fileName;
	private final int cms;
	private String collection;
	private int rows;
	private float verticalGap;
	private int seqNo = 0;
	private ZImage zimage;
	private float horizontalGap;
	private int titleLead;
	private int footerLead;
	private String subject;
	private String message;
	private List<File> tempFiles = new ArrayList<File>();
	private final UnsharpMask unsharpMask;
	protected String opId = java.util.UUID.randomUUID().toString();
	protected IFileWatcher fileWatcher = CoreActivator.getDefault().getFileWatchManager();
//	private final int scalingMethod;

	public PdfJob(List<Asset> assets, PageLayout_typeImpl layout, File targetFile, int quality, int jpegQuality,
			int cms, UnsharpMask unsharpMask, String collection) {
		super(Messages.PdfJob_Create_x);
		this.jpegQuality = jpegQuality;
//		this.scalingMethod = scalingMethod;
		this.assets = assets;
		this.layout = layout;
		this.targetFile = targetFile;
		this.quality = quality;
		this.cms = cms;
		this.unsharpMask = unsharpMask;
		this.collection = collection;
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.OPERATIONJOBFAMILY == family || Constants.CRITICAL == family;
	}

	@Override
	protected IStatus runJob(IProgressMonitor monitor) {
		long startTime = System.currentTimeMillis();
		MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 0, Messages.PdfJob_pdf_report, null);
		setup();
		monitor.beginTask(NLS.bind(Messages.PdfJob_creating_pdf_pages, pages), assets.size() + 1);
		Document document = createDocument();
		writeDocument(document, status, monitor);
		OperationJob.signalJobEnd(startTime);
		if (subject != null && message != null && !monitor.isCanceled()) {
			IStatus sendStatus = Activator.getDefault().sendMail(null, null, null, subject, message,
					Collections.singletonList(targetFile.toURI().toString()));
			if (!sendStatus.isOK())
				status.add(sendStatus);
		}
		cleanUp();
		if (subject == null)
			Program.launch(targetFile.getAbsolutePath());
		return status;
	}

	private void cleanUp() {
		for (File file : tempFiles)
			file.delete();
		tempFiles.clear();
	}

	private void setup() {
		format = computeFormat(layout);
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

		keyLine = layout.getKeyLine() * 0.5f;
		fontLead = fontSize / 3f;
		titlePixelSize = 0;
		titleLead = 0;
		if (!layout.getTitle().isEmpty()) {
			titlePixelSize = titleSize;
			titleLead = titlePixelSize;
		}
		subtitlePixelSize = 0;
		subtitleLead = 0;
		if (!layout.getSubtitle().isEmpty()) {
			subtitlePixelSize = subtitleSize;
			subtitleLead = subtitlePixelSize;
		}
		titleTextSize = titlePixelSize + titleLead + subtitlePixelSize + subtitleLead;
		footerLead = footerSize;
		int footerTextSize = footerSize + footerLead;
		int textHeight = 0;
		if (!layout.getCaption1().isEmpty())
			textHeight += fontSize + fontLead;
		if (!layout.getCaption2().isEmpty())
			textHeight += fontSize + fontLead;
		leftMargins = layout.getLeftMargin() / MMPERINCH * 72;
		rightMargins = layout.getRightMargin() / MMPERINCH * 72;
		horizontalGap = layout.getHorizontalGap() / MMPERINCH * 72;
		topMargins = layout.getTopMargin() / MMPERINCH * 72;
		bottomMargins = layout.getBottomMargin() / MMPERINCH * 72;
		verticalGap = layout.getVerticalGap() / MMPERINCH * 72;
		float useableWidth = format.getWidth() - leftMargins - rightMargins;
		cellWidth = (useableWidth + horizontalGap) / layout.getColumns();
		float useableHeight = format.getHeight() - topMargins - bottomMargins - titleTextSize - footerTextSize;
		imageWidth = cellWidth - horizontalGap - keyLine;
		float maxImageHeight = useableHeight - textHeight - verticalGap - keyLine;
		imageHeight = imageWidth / minAspectRatio;
		while (imageHeight > maxImageHeight) {
			imageWidth *= 0.99d;
			imageHeight = imageWidth / minAspectRatio;
		}
		cellHeight = (float) (imageHeight + textHeight + verticalGap + keyLine);
		rows = (int) ((useableHeight + verticalGap) / cellHeight);
		float usedSpace = rows * cellHeight;
		upperWaste = (useableHeight - usedSpace) / 2;
		imagesPerPage = layout.getColumns() * rows;
		pages = Math.max(1, (assets.size() + imagesPerPage - 1) / imagesPerPage);
		now = new Date();
		IDbManager dbManager = Core.getCore().getDbManager();
		meta = dbManager.getMeta(true);
		fileName = dbManager.getFileName();
	}

	public static Rectangle computeFormat(PageLayout_typeImpl layout) {
		Rectangle format = PageSize.A4;
		String f = layout.getFormat();
		for (int i = 0; i < PageLayout_type.formatALLVALUES.length; i++)
			if (PageLayout_type.formatALLVALUES[i].equals(f)) {
				format = PAGEFORMATS[i];
				break;
			}
		return (layout.getLandscape()) ? format.rotate() : format;
	}

	private Document createDocument() {
		Document document = new Document();
		document.addCreationDate();
		document.addCreator(Constants.APPLICATION_NAME);
		document.addAuthor(System.getProperty("user.name")); //$NON-NLS-1$
		document.setPageSize(format);
		document.setMargins(Math.max(0, leftMargins - horizontalGap / 2), Math.max(0, rightMargins - horizontalGap / 2),
				topMargins, 0);
		document.setMarginMirroring(layout.getFacingPages());
		return document;
	}

	private void writeDocument(Document document, MultiStatus status, IProgressMonitor monitor) {
		try (FileOutputStream out = new FileOutputStream(targetFile)) {
			PdfWriter.getInstance(document, out);
			document.open();
			for (int i = 0; i < pages; i++) {
				printPage(document, i + 1, status, monitor);
				if (monitor.isCanceled()) {
					status.add(
							new Status(IStatus.WARNING, Activator.PLUGIN_ID, Messages.PdfJob_pdf_creation_cancelled));
					break;
				}
			}
			document.close();
		} catch (FileNotFoundException e) {
			// should not happen
		} catch (DocumentException e) {
			status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.PdfJob_internal_error_when_writing, e));
		} catch (IOException e) {
			status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.PdfJob_io_error_when_writing, e));
		} finally {
			fileWatcher.stopIgnoring(opId);
		}
	}

	private void printPage(Document document, int pageNo, MultiStatus status, IProgressMonitor monitor)
			throws DocumentException, IOException {
		if (pageNo > 1)
			document.newPage();
		document.setPageCount(pageNo);
		final Display display = Display.getDefault();
		int pageItem = 0;
		if (!layout.getTitle().isEmpty()) {
			String title = PageProcessor.computeTitle(layout.getTitle(), fileName, now, assets.size(), pageNo, pages,
					collection, meta);
			Paragraph p = new Paragraph(title,
					FontFactory.getFont(FontFactory.HELVETICA, titleSize, Font.BOLD, BaseColor.DARK_GRAY));
			p.setAlignment(Element.ALIGN_CENTER);
			if (!layout.getSubtitle().isEmpty())
				p.setSpacingAfter(titleLead);
			else
				p.setSpacingAfter(titleLead + upperWaste);
			document.add(p);
		}
		if (!layout.getSubtitle().isEmpty()) {
			String subtitle = PageProcessor.computeTitle(layout.getSubtitle(), fileName, now, assets.size(), pageNo,
					pages, collection, meta);
			Paragraph p = new Paragraph(subtitle,
					FontFactory.getFont(FontFactory.HELVETICA, subtitleSize, Font.NORMAL, BaseColor.DARK_GRAY));
			p.setAlignment(Element.ALIGN_CENTER);
			p.setSpacingAfter(subtitleLead + upperWaste);
			document.add(p);
		}
		PdfPTable table = new PdfPTable(layout.getColumns());
		Ticketbox box = new Ticketbox();
		try {
			for (int i = 0; i < rows; i++) {
				int ni = i * layout.getColumns();
				for (int j = 0; j < layout.getColumns(); j++) {
					int a = (pageNo - 1) * imagesPerPage + ni + j;
					PdfPCell cell;
					if (a >= assets.size() || monitor.isCanceled())
						cell = new PdfPCell();
					else {
						final int dpi = quality == Constants.SCREEN_QUALITY ? SCREENDPI : PRINTERDPI;
						final int pixelWidth = (int) (imageWidth * dpi / 72);
						final int pixelHeight = (int) (imageHeight * dpi / 72);
						Asset asset = assets.get(a);
						zimage = new ZImage(ImageUtilities.loadThumbnail(display, asset.getJpegThumbnail(), cms,
								SWT.IMAGE_JPEG, true), null);
						org.eclipse.swt.graphics.Rectangle bounds = zimage.getBounds();
						IVolumeManager vm = Core.getCore().getVolumeManager();
						URI uri = vm.findExistingFile(asset, false);
						if (uri != null) {
							boolean r = asset.getRotation() % 180 != 0;
							double w = r ? asset.getHeight() : asset.getWidth();
							double h = r ? asset.getWidth() : asset.getHeight();
							double scale = w == 0 || h == 0 ? 1d : Math.min(pixelWidth / w, pixelHeight / h);
							scale = (scale <= 0.5d) ? 0.5d : 1d;
							File file = null;
							try {
								file = box.obtainFile(uri);
							} catch (IOException e) {
								status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
										NLS.bind(Messages.PdfJob_download_failed, uri), e));
							}
							if (file != null) {
								try {
									ZImage hzimage = CoreActivator.getDefault().getHighresImageLoader().loadImage(null,
											status, file, asset.getRotation(), asset.getFocalLengthIn35MmFilm(), null,
											scale, Double.MAX_VALUE, true, ImageConstants.SRGB, null, unsharpMask,
											null, fileWatcher, opId, null);
									if (hzimage != null) {
										zimage.dispose();
										zimage = hzimage;
									}
								} catch (UnsupportedOperationException e) {
									// do nothing
								}
								box.cleanup();
							}
						}
						display.syncExec(() -> {
							int kl = (keyLine > 0) ? (int) Math.max(1, (keyLine * dpi / 144)) : 0;
							org.eclipse.swt.graphics.Rectangle ibounds = zimage.getBounds();
							double factor = Math.min((double) pixelWidth / ibounds.width,
									(double) pixelHeight / ibounds.height);
							int lw = pixelWidth + 2 * kl;
							int lh = pixelHeight + 2 * kl;
							Image newImage = new Image(display, lw, lh);
							GC gc = new GC(newImage);
							try {
								gc.setAntialias(SWT.ON);
								gc.setInterpolation(SWT.HIGH);
								gc.setAdvanced(true);
								gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
								gc.fillRectangle(0, 0, lw, lh);
								int width = (int) (ibounds.width * factor + 2 * kl);
								int height = (int) (ibounds.height * factor + 2 * kl);
								int xoff = (lw - width) / 2;
								int yoff = (lh - height) / 2;
								if (kl > 0) {
									gc.setBackground(display.getSystemColor(SWT.COLOR_DARK_GRAY));
									gc.fillRectangle(xoff, yoff, width, height);
								}
								zimage.draw(gc, 0, 0, ibounds.width, ibounds.height, xoff + kl, yoff + kl,
										width - 2 * kl, height - 2 * kl, ZImage.CROPPED, pixelWidth, pixelHeight,
										false);
							} finally {
								gc.dispose();
								zimage.dispose();
								zimage = new ZImage(newImage, null);
							}
						});
						bounds = zimage.getBounds();
						File jpegFile = ImageActivator.getDefault().createTempFile("PdfImg", ".jpg"); //$NON-NLS-1$//$NON-NLS-2$
						tempFiles.add(jpegFile);
						try (FileOutputStream out = new FileOutputStream(jpegFile)) {
							zimage.saveToStream(monitor, true, ZImage.UNCROPPED, SWT.DEFAULT, SWT.DEFAULT, out,
									SWT.IMAGE_JPEG, jpegQuality);
						}
						zimage.dispose();
						com.itextpdf.text.Image pdfImage = com.itextpdf.text.Image.getInstance(jpegFile.getPath());
						double factor = Math.min(imageWidth / bounds.width, imageHeight / bounds.height);
						float w = (float) (bounds.width * factor);
						float h = (float) (bounds.height * factor);
						pdfImage.setInterpolation(true);
						pdfImage.scaleToFit(w, h);
						cell = new PdfPCell(pdfImage, false);
						cell.setHorizontalAlignment(Element.ALIGN_CENTER);
						monitor.worked(1);
					}
					cell.setFixedHeight((float) imageHeight);
					cell.setBorderWidth(0);
					table.addCell(cell);
				}
				renderCaptions(pageNo, seqNo, pageItem, table, ni, layout.getCaption1());
				renderCaptions(pageNo, seqNo, pageItem, table, ni, layout.getCaption2());
				pageItem += layout.getColumns();
				seqNo += layout.getColumns();
				if (verticalGap > 0 && i < rows - 1) {
					for (int j = 0; j < layout.getColumns(); j++) {
						PdfPCell cell = new PdfPCell();
						cell.setFixedHeight(verticalGap);
						cell.setBorderWidth(0);
						table.addCell(cell);
					}
				}
			}
			table.setWidthPercentage(100f);
			document.add(table);
			if (!layout.getFooter().isEmpty()) {
				String footer = PageProcessor.computeTitle(layout.getFooter(), fileName, now, assets.size(), pageNo,
						pages, collection, meta);
				Paragraph p = new Paragraph(footer,
						FontFactory.getFont(FontFactory.HELVETICA, subtitleSize, Font.NORMAL, BaseColor.DARK_GRAY));
				p.setAlignment(Element.ALIGN_CENTER);
				p.setSpacingBefore(upperWaste / 2 + footerLead);
				document.add(p);
			}
		} finally {
			box.endSession();
		}
	}

	private void renderCaptions(int pageNo, int seqNo, int pageItem, PdfPTable table, int ni, String caption) {
		if (!caption.isEmpty()) {
			for (int j = 0; j < layout.getColumns(); j++) {
				int a = (pageNo - 1) * imagesPerPage + ni + j;
				PdfPCell cell;
				if (a >= assets.size())
					cell = new PdfPCell();
				else {
					String cc = PageProcessor.computeCaption(caption, assets.get(a), collection, seqNo + j + 1,
							pageItem + j + 1);
					Paragraph p = new Paragraph(cc,
							FontFactory.getFont(FontFactory.HELVETICA, fontSize, Font.NORMAL, BaseColor.DARK_GRAY));
					p.setSpacingBefore(0);
					cell = new PdfPCell(p);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				}
				cell.setBorderWidth(0);
				table.addCell(cell);
			}
		}
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
