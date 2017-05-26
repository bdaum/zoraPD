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

import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.PageLayout_type;
import com.bdaum.zoom.cat.model.PageLayout_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;

public class PrintLayoutDialog extends ZTitleAreaDialog {

	private static final int PREVIEW = 999;
	private static final int BACK = 998;
	private List<Asset> assets;
	private PrinterData data;

	private LayoutComponent layoutComponent;
	private PageLayout_typeImpl result;

	public PrintLayoutDialog(Shell parentShell, PrinterData data, List<Asset> assets) {
		super(parentShell, HelpContextIds.PRINTLAYOUT_DIALOG);
		this.data = data;
		this.assets = assets;
	}

	public PageLayout_typeImpl getResult() {
		return result;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(NLS.bind(
				assets.size() == 1 ? Messages.PrintLayoutDialog_printing_one : Messages.PrintLayoutDialog_printing,
				assets.size()));
		setTitle(Messages.PrintLayoutDialog_page_layout);
		setMessage(Messages.PrintLayoutDialog_please_configure);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		layoutComponent = new LayoutComponent(area, LayoutComponent.PRINT);
		layoutComponent.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fillValues();
		return area;
	}

	private void fillValues() {
		PageLayout_typeImpl layout = dbManager.obtainById(PageLayout_typeImpl.class, Constants.PRINTLAYOUT_ID);
		if (layout == null) {
			layout = new PageLayout_typeImpl(null, LayoutComponent.PRINT, Constants.PT_COLLECTION, Constants.PT_TODAY,
					NLS.bind(Messages.PrintLayoutDialog_n_of_m, Constants.PT_PAGENO, Constants.PT_PAGECOUNT), 0, 4, 20,
					10, 10, 8, 10, 7, Constants.PI_NAME, Constants.PI_CREATIONDATE, "", 2, false, false, //$NON-NLS-1$
					PageLayout_type.format_a4, true, 1.5f, 0.50f, 2, -1);
			layout.setStringId(Constants.PRINTLAYOUT_ID);
		}
		layoutComponent.fillValues(layout);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, PREVIEW, Messages.PrintLayoutDialog_preview, false);
		createButton(parent, BACK, Messages.PrintLayoutDialog_back, false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == PREVIEW)
			showPreview();
		else if (buttonId == BACK) {
			setReturnCode(BACK);
			close();
		} else
			super.buttonPressed(buttonId);
	}

	private void showPreview() {
		PrintPreviewDialog dialog = new PrintPreviewDialog(getShell(), data, layoutComponent.getResult(), assets);
		if (dialog.open() == PrintPreviewDialog.PRINT)
			okPressed();
	}

	@Override
	protected void okPressed() {
		result = layoutComponent.getResult();
		Core.getCore().getDbManager().safeTransaction(null, result);
		super.okPressed();
	}

}
