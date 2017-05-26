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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.PageLayout_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.internal.widgets.ZDialog;

public class PrintPreviewDialog extends ZDialog implements PaintListener,
		IAdaptable {

	public static final int PRINT = 99;
	private static final int BACKWARD = 98;
	private static final int FORWARD = 97;
	private PageLayout_typeImpl layout;
	private Canvas canvas;
	private PrinterData printerData;
	private List<Asset> assets;
	private int w;
	private int h;
	private int pageNo;
	private PageProcessor processor;
	private Image preview;

	public PrintPreviewDialog(Shell parentShell, PrinterData data,
			PageLayout_typeImpl layout, List<Asset> assets) {
		super(parentShell);
		this.printerData = data;
		this.layout = layout;
		this.assets = assets;
	}

	@Override
	public void create() {
		super.create();
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		canvas = new Canvas(area, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		Printer printer = new Printer(printerData);
		Rectangle bounds = printer.getClientArea();
		Rectangle trim = printer.computeTrim(0, 0, 0, 0);
		Point printerDpi = printer.getDPI();
		printer.dispose();
		Display display = parent.getDisplay();
		Point displayDpi = display.getDPI();
		Rectangle displayBounds = display.getPrimaryMonitor().getClientArea();
		double physWidthDisplay = (double) displayBounds.width / displayDpi.x;
		double physHeightDisplay = (double) (displayBounds.height - 150) / displayDpi.y;
		double physWidthPrinter = (double) bounds.width / printerDpi.x;
		double physHeightPrinter = (double) bounds.height / printerDpi.y;
		double physFactor = Math.min(
				1d,
				Math.min(physWidthDisplay / physWidthPrinter, physHeightDisplay
						/ physHeightPrinter));
		double factorX = physFactor * displayDpi.x / printerDpi.x;
		double factorY = physFactor * displayDpi.y / printerDpi.y;
		w = (int) (bounds.width * factorX);
		h = (int) (bounds.height * factorY);
		layoutData.heightHint = h;
		layoutData.widthHint = w;
		canvas.setLayoutData(layoutData);
		pageNo = printerData.scope == PrinterData.PAGE_RANGE ? printerData.startPage
				: 1;
		processor = new PageProcessor(printerData, layout, assets, w, h,
				factorX, factorY, trim, printerDpi, Ui.getUi().getDisplayCMS(),
				null, this);
		canvas.addPaintListener(this);
		canvas.redraw();
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, BACKWARD, "<", false); //$NON-NLS-1$
		createButton(parent, FORWARD, ">", false); //$NON-NLS-1$
		createButton(parent, PRINT, Messages.PrintPreviewDialog_Print, false);
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case FORWARD:
			pageNo++;
			disposePreview();
			updateButtons();
			canvas.redraw();
			break;
		case BACKWARD:
			pageNo--;
			disposePreview();
			updateButtons();
			canvas.redraw();
			break;
		case PRINT:
			setReturnCode(PRINT);
			close();
			break;

		default:
			super.buttonPressed(buttonId);
			break;
		}
	}

	private void updateButtons() {
		int start = 1;
		int end = processor.getPageCount();
		if (printerData.scope == PrinterData.PAGE_RANGE) {
			start = printerData.startPage;
			end = printerData.endPage;
		}
		getButton(BACKWARD).setEnabled(pageNo > start);
		getButton(FORWARD).setEnabled(pageNo < end);
		getShell().setText(
				NLS.bind(Messages.PrintPreviewDialog_Print_preview, pageNo
						- start + 1, end - start + 1));
	}

	public void paintControl(PaintEvent e) {
		Image image = getPreview(e);
		if (image == null)
			close();
		Rectangle area = ((Canvas) e.widget).getClientArea();
		e.gc.drawImage(image, area.x, area.y);
	}

	private Image getPreview(PaintEvent e) {
		if (preview == null) {
			Rectangle area = ((Canvas) e.widget).getClientArea();
			preview = new Image(e.display, area.width, area.height);
			GC gc = new GC(preview);
			try {
				if (processor.render(e.display, gc, pageNo, null)) {
					preview.dispose();
					preview = null;
				}
			} finally {
				gc.dispose();
			}
		}
		return preview;
	}

	@Override
	public boolean close() {
		disposePreview();
		processor.dispose();
		return super.close();
	}

	private void disposePreview() {
		if (preview != null) {
			preview.dispose();
			preview = null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter))
			return getShell();
		return null;
	}
}
