package com.bdaum.zoom.email.internal.job;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.bdaum.zoom.cat.model.PageLayout_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.common.internal.FileLocator;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.Ticketbox;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.email.internal.Activator;
import com.bdaum.zoom.image.ZImage;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.net.core.job.TransferJob;
import com.bdaum.zoom.operations.jobs.AbstractExportJob;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.DiskFullException;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.PageProcessor;

@SuppressWarnings("restriction")
public class HtmlJob extends AbstractExportJob {

	private static final String STYLES_CSS = "styles.css"; //$NON-NLS-1$
	private PageLayout_typeImpl layout;
	private File targetFile;
	private String collection;
	private final FtpAccount account;
	private boolean ftp;
	private File imageFolder;
	private File tempFolder;
	private String fileName;
	private Meta meta;
	private Date now;
	private int horizontalGap;
	private final String weblink;

	public HtmlJob(List<Asset> assets, int mode, PageLayout_typeImpl layout, File targetFile, FtpAccount account,
			int quality, int jpegQuality, UnsharpMask unsharpMask, String collection, String weblink, IAdaptable adaptable) {
		super(Messages.HtmlJob_export_html, assets, mode, Constants.SCALE_FIXED, 1d, layout.getSize(), unsharpMask,
				jpegQuality, null, false, null, QueryField.SAFETY_RESTRICTED, adaptable);
		this.account = account;
		this.layout = layout;
		this.targetFile = targetFile;
		this.collection = collection;
		this.weblink = weblink;
		this.ftp = targetFile == null;
	}

	private void setup() {
		horizontalGap = layout.getHorizontalGap();
		now = new Date();
		IDbManager dbManager = Core.getCore().getDbManager();
		meta = dbManager.getMeta(true);
		fileName = dbManager.getFileName();
	}

	@Override
	protected IStatus doRun(IProgressMonitor monitor) {
		long startTime = System.currentTimeMillis();
		MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, 0, Messages.PdfJob_pdf_report, null);
		setup();
		File targetFolder;
		if (ftp) {
			try {
				targetFolder = Core.createTempDirectory("FtpTransfer"); //$NON-NLS-1$
				tempFolder = targetFolder;
			} catch (IOException e) {
				status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.HtmlJob_error_exporting_to_ftp, e));
				return status;
			}
		} else {
			targetFile.mkdirs();
			targetFolder = targetFile;
		}
		imageFolder = new File(targetFolder, "images"); //$NON-NLS-1$
		File htmlFile = new File(targetFolder, weblink);
		if (htmlFile.exists())
			BatchUtilities.deleteFileOrFolder(htmlFile);
		if (imageFolder.exists())
			BatchUtilities.deleteFileOrFolder(imageFolder);
		imageFolder.mkdir();
		monitor.beginTask(Messages.HtmlJob_creating_html_page, assets.size() + 1);
		try {
			htmlFile.delete();
			htmlFile.createNewFile();
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(htmlFile)))) {
				writeHtml(writer, status, monitor);
			}
			monitor.worked(1);
			fillImageFolder(monitor, status);
			File css = FileLocator.findFile(Activator.getDefault().getBundle(), STYLES_CSS);
			File out = new File(targetFolder, STYLES_CSS);
			BatchUtilities.copyFile(css, out, null);
		} catch (IOException e) {
			status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.HtmlJob_io_error_generating_html, e));
			return status;
		} catch (URISyntaxException e) {
			// should never happen
		} catch (DiskFullException e) {
			status.add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.HtmlJob_disk_full, e));
		}
		if (ftp)
			new TransferJob(targetFolder.listFiles(), account, true).schedule();
		cleanUp();
		OperationJob.signalJobEnd(startTime);
		try {
			IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
			browser.openURL(htmlFile.toURI().toURL());
		} catch (Exception e) {
			// do nothing
		}
		return status;
	}

	private void fillImageFolder(IProgressMonitor monitor, MultiStatus status) {
		int size = assets.size();
		SubMonitor progress = SubMonitor.convert(monitor, 1000 * (size + 1));
		IVolumeManager vm = Core.getCore().getVolumeManager();
		Ticketbox box = new Ticketbox();
		try {
			int k = 0;
			for (Asset asset : assets) {
				URI uri = vm.findExistingFile(asset, false);
				if (uri != null) {
					File file = null;
					try {
						file = box.obtainFile(uri);
					} catch (IOException e) {
						addErrorStatus(status, NLS.bind(Messages.HtmlJob_download_failed, uri), e);
					}
					if (file != null) {
						File outfile = new File(imageFolder, k + (mode == Constants.FORMAT_WEBP ? ".webp" : ".jpg")); //$NON-NLS-1$ //$NON-NLS-2$
						downScaleImage(status, progress, asset, file, outfile, 1d, ZImage.CROPPED);
						box.cleanup();
					}
				}
				++k;
				if (monitor.isCanceled()) {
					status.add(new Status(IStatus.WARNING, UiActivator.PLUGIN_ID, Messages.HtmlJob_export_cancelled));
					return;
				}
			}
		} finally {
			box.endSession();
		}
	}

	private void writeHtml(Writer writer, MultiStatus status, IProgressMonitor monitor) throws IOException {
		writer.write("<html>\n"); //$NON-NLS-1$
		writer.write(generateHeader());
		writer.write(generateBody());
		writer.write("</html>"); //$NON-NLS-1$
	}

	private String generateBody() {
		StringBuilder sb = new StringBuilder();
		sb.append("<body>\n").append(generateTitle()) //$NON-NLS-1$
				.append(generateSubtitle()).append(generateTable()).append(generateFooter()).append("</body>\n"); //$NON-NLS-1$
		return sb.toString();
	}

	private String generateFooter() {
		String footer = PageProcessor.computeTitle(layout.getFooter(), fileName, now, assets.size(), 1, 1, collection,
				meta);
		return generateDiv(footer, "footer"); //$NON-NLS-1$
	}

	private String generateTitle() {
		StringBuilder sb = new StringBuilder();
		if (layout.getTitle().length() > 0) {
			String title = PageProcessor.computeTitle(layout.getTitle(), fileName, now, assets.size(), 1, 1, collection,
					meta);
			sb.append("\t<h1 align=\"center\">").append(title) //$NON-NLS-1$
					.append("\t</h1>\n"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	private String generateSubtitle() {
		StringBuilder sb = new StringBuilder();
		if (layout.getSubtitle().length() > 0) {
			String title = PageProcessor.computeTitle(layout.getSubtitle(), fileName, now, assets.size(), 1, 1,
					collection, meta);
			sb.append("\t<h2 align=\"center\">").append(title) //$NON-NLS-1$
					.append("\t</h2>\n"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	private String generateTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("\t<table align=\"center\" cellpadding=\"") //$NON-NLS-1$
				.append(horizontalGap).append("\" >\n"); //$NON-NLS-1$
		int columns = layout.getColumns();
		int rows = (assets.size() + columns - 1) / columns;
		for (int i = 0; i < rows; i++)
			sb.append(generateTableRow(i));
		sb.append("\t</table>\n"); //$NON-NLS-1$
		return sb.toString();
	}

	private String generateTableRow(int i) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t<tr>\n"); //$NON-NLS-1$
		int columns = layout.getColumns();
		for (int j = 0; j < columns; j++) {
			int k = i * columns + j;
			if (k < assets.size())
				sb.append(generateTableCell(k));
		}
		sb.append("\t\t</tr>\n"); //$NON-NLS-1$
		return sb.toString();
	}

	private String generateTableCell(int k) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t\t<td>\n"); //$NON-NLS-1$
		sb.append(generateDiv(generateImg(k), "img")); //$NON-NLS-1$
		if (layout.getCaption1().length() > 0)
			sb.append(generateDiv(generateCaption(k, layout.getCaption1()), "caption1")); //$NON-NLS-1$
		if (layout.getCaption2().length() > 0)
			sb.append(generateDiv(generateCaption(k, layout.getCaption2()), "caption2")); //$NON-NLS-1$
		sb.append("\t\t\t</td>\n"); //$NON-NLS-1$
		return sb.toString();
	}

	private String generateCaption(int k, String caption) {
		return PageProcessor.computeCaption(caption, assets.get(k), collection, k, k);
	}

	private String generateDiv(String text, String clazz) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t\t\t<div align=\"center\" class=\"").append(clazz) //$NON-NLS-1$
				.append("\">\n"); //$NON-NLS-1$
		sb.append(text);
		sb.append("\n\t\t\t\t</div>\n"); //$NON-NLS-1$
		return sb.toString();
	}

	private String generateImg(int k) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t\t\t\t\t<img src=\"images/").append(k) //$NON-NLS-1$
				.append((mode == Constants.FORMAT_WEBP ? ".webp\"" : ".jpg\"")); //$NON-NLS-1$ //$NON-NLS-2$
		String alt = layout.getAlt();
		if (alt.length() > 0)
			sb.append(" alt=\"").append(generateCaption(k, alt)).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append("/>\n"); //$NON-NLS-1$
		return sb.toString();
	}

	private String generateHeader() {
		StringBuilder sb = new StringBuilder();
		sb.append("<header>\n").append(generatePageTitle()) //$NON-NLS-1$
				.append(generateStylesheetLink()).append("</header>\n"); //$NON-NLS-1$
		return sb.toString();
	}

	private String generateStylesheetLink() {
		return "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\">\n"; //$NON-NLS-1$
	}

	private String generatePageTitle() {
		String title = PageProcessor.computeTitle(layout.getTitle(), fileName, now, assets.size(), 1, 1, collection,
				meta);
		StringBuilder sb = new StringBuilder();
		sb.append("\t<title>").append(title).append("</title>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return sb.toString();
	}

	private void cleanUp() {
		if (tempFolder != null) {
			BatchUtilities.deleteFileOrFolder(tempFolder);
			tempFolder = null;
		}
	}

}
