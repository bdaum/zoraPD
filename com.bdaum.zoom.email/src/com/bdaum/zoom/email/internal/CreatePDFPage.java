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
package com.bdaum.zoom.email.internal;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.cat.model.PageLayout_type;
import com.bdaum.zoom.cat.model.PageLayout_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.Ticketbox;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.email.internal.job.HtmlJob;
import com.bdaum.zoom.email.internal.job.PdfJob;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.dialogs.LayoutComponent;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;
import com.itextpdf.text.Rectangle;

@SuppressWarnings("restriction")
public class CreatePDFPage extends ColoredWizardPage {

	private static final String PDFLAYOUT_ID = "pdfLayout"; //$NON-NLS-1$
	private static final String HTMLLAYOUT_ID = "htmlLayout"; //$NON-NLS-1$
	private static final float MMPERINCH = 25.4f;
	private List<Asset> assets;
	private LayoutComponent layoutComponent;
	private int commonWidth = -1;
	private int commonHeight = -1;
	private final String type;
	private boolean pdf;

	public CreatePDFPage(List<Asset> assets, String type) {
		super("main", NLS.bind(Messages.CreatePDFPage_create_x, type), null); //$NON-NLS-1$
		this.assets = assets;
		this.type = type;
		pdf = !"HTML".equals(type); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = createComposite(parent, 1);
		layoutComponent = new LayoutComponent(comp, pdf ? LayoutComponent.PDF
				: LayoutComponent.HTML);
		fillValues();
		checkImages();
		setControl(comp);
		setHelp(HelpContextIds.PDF_WIZARD);
		int size = assets.size();
		String msg = (size == 1) ? NLS.bind(Messages.CreatePDFPage_compose_one,
				type) : NLS.bind(Messages.CreatePDFPage_Compose_n, size, type);
		setMessage(msg);
		setTitle(Messages.CreatePDFPage_layout);
		super.createControl(parent);
	}

	private void checkImages() {
		final Set<String> volumes = new HashSet<String>();
		final List<String> errands = new ArrayList<String>();
		IVolumeManager volumeManager = Core.getCore().getVolumeManager();
		for (Asset asset : assets) {
			URI uri = volumeManager.findFile(asset);
			if (uri != null) {
				if (volumeManager.findExistingFile(asset, false) != null) {
					if (commonWidth >= 0) {
						if (commonWidth != asset.getWidth())
							commonWidth = Integer.MAX_VALUE;
					} else
						commonWidth = asset.getWidth();
					if (commonHeight >= 0) {
						if (commonHeight != asset.getHeight())
							commonHeight = Integer.MAX_VALUE;
					} else
						commonHeight = asset.getHeight();
				} else {
					String volume = asset.getVolume();
					if (volume != null && !volume.isEmpty())
						volumes.add(volume);
					errands.add(uri.toString());
				}
			}
		}
		setErrorMessage(Ticketbox.computeErrorMessage(errands, volumes));
	}

	private void fillValues() {
		String id = pdf ? PDFLAYOUT_ID : HTMLLAYOUT_ID;
		IDbManager dbManager = Core.getCore().getDbManager();
		PageLayout_typeImpl layout = dbManager.obtainById(
				PageLayout_typeImpl.class, id);
		if (layout == null) {
			String footer;
			if (pdf)
				footer = "- " + Constants.PT_PAGENO + " -"; //$NON-NLS-1$ //$NON-NLS-2$
			else {
				GregorianCalendar cal = new GregorianCalendar();
				footer = "© " + cal.get(Calendar.YEAR) + " " + dbManager.getMeta(true).getOwner(); //$NON-NLS-1$ //$NON-NLS-2$
			}
			layout = new PageLayout_typeImpl(null, pdf ? LayoutComponent.PDF
					: LayoutComponent.HTML, Constants.PT_COLLECTION,
					Constants.PT_TODAY, footer, 240, pdf ? 4 : 5, 20, 10,
					pdf ? 10 : 20, 8, 10, 7, Constants.PI_NAME,
					Constants.PI_CREATIONDATE, Constants.PI_NAME, 2, false,
					false, PageLayout_type.format_a4, true, 1.5f, 0.26f, 2, 75);
			layout.setStringId(id);
		}
		layoutComponent.fillValues(layout);
	}

	public boolean finish() {
		IPdfWizard w = (IPdfWizard) getWizard();
		File file = w.getTargetFile();
		FtpAccount account = w.getFtpAccount();
		if (file == null && account == null)
			return false;
		ICore core = Core.getCore();
		IAssetProvider assetProvider = core.getAssetProvider();
		if (assetProvider != null) {
			String collection = Utilities.getExternalAlbumName(assetProvider
					.getCurrentCollection());
			PageLayout_typeImpl layout = layoutComponent.getResult();
			core.getDbManager().safeTransaction(null, layout);
			if (pdf) {
				PdfJob job = new PdfJob(assets, layout, file, w.getQuality(),
						w.getJpegQuality(), Ui.getUi().getDisplayCMS(),
						w.getUnsharpMask(), collection);
				if (w instanceof EmailPDFWizard) {
					job.setSubject(((EmailPDFWizard) w).getSubject());
					job.setMessage(((EmailPDFWizard) w).getMessage());
				}
				job.schedule();
			} else {
				String weblink = w.getWeblink();
				if (weblink == null || weblink.isEmpty())
					weblink = "index.html"; //$NON-NLS-1$
				int mode = w.getMode();
				new HtmlJob(assets, mode, layout, file, account, w.getQuality(),
						w.getJpegQuality(), w.getUnsharpMask(),
						collection, weblink, this)
						.schedule();
			}
		}
		return true;
	}

	public float getImageSize() {
		PageLayout_typeImpl layout = layoutComponent.getResult();
		Rectangle format = PdfJob.computeFormat(layout);
		float leftMargins = layout.getLeftMargin() / MMPERINCH * 72;
		float rightMargins = layout.getRightMargin() / MMPERINCH * 72;
		float horizontalGap = layout.getHorizontalGap() / MMPERINCH * 72;
		float useableWidth = format.getWidth() - leftMargins - rightMargins;
		float cellWidth = (useableWidth + horizontalGap) / layout.getColumns();
		float keyLine = layout.getKeyLine() * 0.5f;
		float imageSize = cellWidth - horizontalGap - keyLine;
		return imageSize;
	}

	@Override
	protected void validatePage() {
	}

}
