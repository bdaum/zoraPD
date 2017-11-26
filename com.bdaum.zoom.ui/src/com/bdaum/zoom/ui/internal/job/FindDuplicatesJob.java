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

package com.bdaum.zoom.ui.internal.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.job.CustomJob;
import com.bdaum.zoom.operations.internal.dup.AbstractDuplicatesProvider;
import com.bdaum.zoom.operations.internal.dup.CombinedDuplicatesProvider;
import com.bdaum.zoom.operations.internal.dup.ExposureDataDuplicatesProvider;
import com.bdaum.zoom.operations.internal.dup.FileNameDuplicatesProvider;
import com.bdaum.zoom.operations.internal.dup.SimilarityDuplicatesProvider;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.views.DuplicatesView;

@SuppressWarnings("restriction")
public class FindDuplicatesJob extends CustomJob {

	private int method;
	private final IWorkbenchWindow window;
	private DuplicatesView duplicatesView;
	private final boolean ignoreDerivates;
	private final int kind;
	private final IDbManager dbManager;
	private final boolean withExtension;

	public FindDuplicatesJob(IWorkbenchWindow window, int kind, int method,
			boolean ignoreDerivates, boolean withExtension) {
		super(Messages.FindDuplicatesJob_find_duplicates);
		this.window = window;
		this.withExtension = withExtension;
		this.dbManager = Core.getCore().getDbManager();
		this.kind = kind;
		this.method = method;
		this.ignoreDerivates = ignoreDerivates;
		setUser(false);
		setPriority(Job.LONG);
	}


	@Override
	public boolean belongsTo(Object family) {
		return Constants.DAEMONS == family || Constants.DUPLICATES == family;
	}

	@Override
	protected IStatus runJob(IProgressMonitor monitor) {
		final IWorkbenchPage activePage = window.getActivePage();
		if (activePage == null)
			return Status.CANCEL_STATUS;
		Display display = window.getShell().getDisplay();
		display.syncExec(() -> {
			try {
				IViewPart view = activePage.showView(DuplicatesView.ID);
				if (view instanceof DuplicatesView) {
					duplicatesView = (DuplicatesView) view;
					duplicatesView.reset();
					duplicatesView.showBusy(true);
					duplicatesView.setItemType(Messages.FindDuplicatesJob_duplicates);
				}
			} catch (PartInitException e) {
				// ignore
			}
		});
		if (duplicatesView == null)
			return new Status(IStatus.ERROR, UiActivator.PLUGIN_ID,
					Messages.FindDuplicatesJob_internal_error);
		AbstractDuplicatesProvider duplicatesProvider;
		switch (kind) {
		case Constants.DUPES_BYFILENAME:
			duplicatesProvider = new FileNameDuplicatesProvider(dbManager, withExtension);
			break;
		case Constants.DUPES_BYEXPOSUREDATA:
			duplicatesProvider = new ExposureDataDuplicatesProvider(dbManager);
			break;
		case Constants.DUPES_BYSIMILARITY:
			duplicatesProvider = new SimilarityDuplicatesProvider(dbManager, method);
			break;
		default:
			duplicatesProvider = new CombinedDuplicatesProvider(dbManager, method);
			break;
		}
		duplicatesProvider.setIgnoreDerivates(ignoreDerivates);
		duplicatesProvider.findDuplicates(monitor);
		final AbstractDuplicatesProvider provider = duplicatesProvider;
		if (!monitor.isCanceled() && !display.isDisposed() && !window.getShell().isDisposed())
			display.syncExec(() -> {
				try {
					DuplicatesView view = (DuplicatesView) activePage
							.showView(DuplicatesView.ID);
					view.showBusy(false);
					view.setDuplicatesProvider(provider);
				} catch (PartInitException e) {
					// ignore
				}
			});
		return Status.OK_STATUS;
	}

}
