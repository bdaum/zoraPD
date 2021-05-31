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
 * (c) 2009-2021 Berthold Daum  
 */
package com.bdaum.zoom.email.internal;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;

import com.bdaum.zoom.cat.model.PageLayout_type;
import com.bdaum.zoom.cat.model.PageLayout_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Assetbox;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IAssetProvider;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.email.internal.job.HtmlJob;
import com.bdaum.zoom.email.internal.job.PdfJob;
import com.bdaum.zoom.net.core.ftp.FtpAccount;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.dialogs.LayoutComponent;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class CreatePDFPage extends ColoredWizardPage {

	private static final String PDFLAYOUT_ID = "pdfLayout"; //$NON-NLS-1$
	private static final String HTMLLAYOUT_ID = "htmlLayout"; //$NON-NLS-1$
	private static final float MMPERINCH = 25.4f;
	private List<Asset> assets;
	private LayoutComponent layoutComponent;
	private int commonWidth = -1, commonHeight = -1;
	private final String type;
	private boolean pdf;
	private IAssetProvider assetProvider;
	private String collection;

	public CreatePDFPage(List<Asset> assets, String type) {
		super("main", NLS.bind(Messages.CreatePDFPage_create_x, type), null); //$NON-NLS-1$
		this.assets = assets;
		this.type = type;
		pdf = !"HTML".equals(type); //$NON-NLS-1$
		assetProvider = Core.getCore().getAssetProvider();
		if (assetProvider != null)
			collection = Utilities.getExternalAlbumName(assetProvider.getCurrentCollection());
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = createComposite(parent, 1);
		layoutComponent = new LayoutComponent(comp, pdf ? LayoutComponent.PDF : LayoutComponent.HTML, assets,
				collection);
		fillValues();
		checkImages();
		setControl(comp);
		setHelp(HelpContextIds.PDF_WIZARD);
		int size = assets.size();
		setMessage((size == 1) ? NLS.bind(Messages.CreatePDFPage_compose_one, type)
				: NLS.bind(Messages.CreatePDFPage_Compose_n, size, type));
		setTitle(Messages.CreatePDFPage_layout);
		super.createControl(parent);
	}

	private void checkImages() {
		try (Assetbox box = new Assetbox(assets, null, true)) {
			for (File file : box)
				if (file != null) {
					Asset asset = box.getAsset();
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
				}
			setErrorMessage(box.getErrorMessage());
		}
	}

	private void fillValues() {
		String id = pdf ? PDFLAYOUT_ID : HTMLLAYOUT_ID;
		IDbManager dbManager = Core.getCore().getDbManager();
		PageLayout_typeImpl layout = dbManager.obtainById(PageLayout_typeImpl.class, id);
		if (layout == null) {
			String footer = pdf ? "- " + Constants.PT_PAGENO + " -" //$NON-NLS-1$ //$NON-NLS-2$
					: "� " + new GregorianCalendar().get(Calendar.YEAR) + " " + dbManager.getMeta(true).getOwner(); //$NON-NLS-1$ //$NON-NLS-2$
			layout = new PageLayout_typeImpl(null, pdf ? LayoutComponent.PDF : LayoutComponent.HTML,
					Constants.PT_COLLECTION, Constants.PT_TODAY, footer, 240, pdf ? 4 : 5, 20, 10, pdf ? 10 : 20, 8, 10,
					7, Constants.PI_NAME, Constants.PI_CREATIONDATE, Constants.PI_NAME, 2, false, false,
					PageLayout_type.format_a4, true, 1.5f, 0.26f, 2, 75);
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
		if (assetProvider != null) {
			PageLayout_typeImpl layout = layoutComponent.getResult();
			Core.getCore().getDbManager().safeTransaction(null, layout);
			if (pdf) {
				PdfJob job = new PdfJob(assets, layout, file, w.getQuality(), w.getJpegQuality(),
						Ui.getUi().getDisplayCMS(), w.getUnsharpMask(), collection);
				if (w instanceof EmailPDFWizard)
					job.setEmailData(((EmailPDFWizard) w).getEmailData());
				job.schedule();
			} else {
				String weblink = w.getWeblink();
				if (weblink == null || weblink.isEmpty())
					weblink = "index.html"; //$NON-NLS-1$
				new HtmlJob(assets, w.getMode(), layout, file, account, w.getQuality(), w.getJpegQuality(),
						w.getUnsharpMask(), collection, weblink, this).schedule();
			}
		}
		return true;
	}

	public float getImageSize() {
		PageLayout_type layout = layoutComponent.getResult();
		float horizontalGap = layout.getHorizontalGap() / MMPERINCH * 72;
		float useableWidth = PdfJob.computeFormat(layout).getWidth()
				- (layout.getLeftMargin() + layout.getRightMargin()) / MMPERINCH * 72;
		float cellWidth = (useableWidth + horizontalGap) / layout.getColumns();
		return cellWidth - horizontalGap - layout.getKeyLine() * 0.5f;
	}

	@Override
	protected String validate() {
		if (assets.isEmpty())
			return Messages.PDFTargetFilePage_no_image_selected;
		return null;
	}

}
