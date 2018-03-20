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
 * (c) 2009 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.job;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;

import com.bdaum.zoom.cat.model.PageLayout_typeImpl;
import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.image.recipe.UnsharpMask;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.ui.internal.dialogs.PageProcessor;

public class PrintJob extends CustomJob {

	private List<Asset> assets;
	private PrinterData printerData;
	private PageLayout_typeImpl layout;
	private PageProcessor processor;
	private IAdaptable adaptable;
	private final UnsharpMask umask;

	public PrintJob(PrinterData data, PageLayout_typeImpl layout, List<Asset> assets, UnsharpMask umask,
			IAdaptable adaptable) {
		super(Messages.PrintJob_printing);
		this.printerData = data;
		this.assets = assets;
		this.layout = layout;
		this.umask = umask;
		this.adaptable = adaptable;
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.OPERATIONJOBFAMILY == family || Constants.CRITICAL == family;
	}

	@Override
	protected IStatus runJob(IProgressMonitor monitor) {
		long startTime = System.currentTimeMillis();
		Printer printer = new Printer(printerData);
		try {
			Rectangle bounds = printer.getClientArea();
			processor = new PageProcessor(printerData, layout, assets, bounds.width, bounds.height, 1d, 1d,
					printer.computeTrim(0, 0, 0, 0), printer.getDPI(), ImageConstants.SRGB, umask, adaptable);
			int pages = processor.getPageCount();
			int startPage;
			int endPage;
			if (printerData.scope == PrinterData.PAGE_RANGE) {
				startPage = printerData.startPage;
				endPage = printerData.startPage;
			} else {
				startPage = 1;
				endPage = pages;
			}
			monitor.beginTask(Messages.PrintJob_printing, (endPage - startPage + 1) * printerData.copyCount);
			monitor.subTask(
					NLS.bind(Messages.PrintJob_printing_pages, (endPage - startPage + 1) * printerData.copyCount));
			int outer = printerData.collate ? printerData.copyCount : 1;
			int inner = printerData.collate ? 1 : printerData.copyCount;
			lp: for (int c = 1; c <= outer; c++) {
				printer.startJob(Constants.APPLICATION_NAME);
				for (int i = startPage; i <= endPage; i++)
					for (int cc = 1; cc <= inner; cc++) {
						printer.startPage();
						GC gc = new GC(printer);
						try {
							if (monitor.isCanceled() || processor.render(printer, gc, i, null))
								break lp;
						} finally {
							gc.dispose();
						}
						printer.endPage();
						monitor.worked(1);
					}
				printer.endJob();
			}
		} finally {
			processor.dispose();
			printer.dispose();
			monitor.done();
		}
		OperationJob.signalJobEnd(startTime);
		return Status.OK_STATUS;
	}
}
