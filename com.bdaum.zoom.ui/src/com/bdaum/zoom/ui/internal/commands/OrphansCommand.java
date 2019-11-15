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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.commands;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.cat.model.group.CriterionImpl;
import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.ICore;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.ICollectionProcessor;
import com.bdaum.zoom.ui.Ui;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZProgressMonitorDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.actions.Messages;

public class OrphansCommand extends AbstractCommandHandler {

	private List<AssetImpl> result = new ArrayList<AssetImpl>();
	private Set<String> volumes = new HashSet<String>();
	private boolean isCanceled = false;

	@Override
	public void run() {
		AcousticMessageDialog includeDialog = new AcousticMessageDialog(getShell(),
				Messages.FindOrphansActionDelegate_search_orphans, null,
				Messages.FindOrphansActionDelegate_include_off_line, AcousticMessageDialog.QUESTION,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL },
				1);
		final int ret = includeDialog.open();
		if (ret >= 2)
			return;
		isCanceled = false;
		ZProgressMonitorDialog dialog = new ZProgressMonitorDialog(getShell());
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ICore core = Core.getCore();
				IVolumeManager volumeManager = core.getVolumeManager();
				List<AssetImpl> set = core.getDbManager().obtainAssets();
				monitor.beginTask(Messages.FindOrphansActionDelegate_scanning_catalog, set.size());
				for (AssetImpl asset : set) {
					URI uri = volumeManager.findExistingFile(asset, false);
					if (uri == null) {
						if (ret == 0 || !volumeManager.isOffline(asset.getVolume()))
							result.add(asset);
						else {
							String volume = asset.getVolume();
							if (volume != null && !volume.isEmpty())
								volumes.add(volume);
						}
					}
					if (monitor.isCanceled()) {
						isCanceled = true;
						break;
					}
					monitor.worked(1);
				}
				monitor.done();
			}
		};
		if (isCanceled)
			return;
		try {
			String v = null;
			if (!volumes.isEmpty()) {
				String[] vols = volumes.toArray(new String[volumes.size()]);
				Arrays.sort(vols);
				StringBuffer sb = new StringBuffer();
				for (String volume : vols) {
					if (sb.length() > 0)
						sb.append(", "); //$NON-NLS-1$
					sb.append(volume);
				}
				v = sb.toString();
			}
			dialog.run(true, true, runnable);
			if (result.isEmpty()) {
				String message = Messages.FindOrphansActionDelegate_no_orphans_found;
				if (ret == 1 && v != null)
					message += NLS.bind(Messages.FindOrphansActionDelegate_entries_not_checked, v);
				AcousticMessageDialog.openInformation(getShell(), Messages.FindOrphansActionDelegate_orphan_search,
						message);
			} else {
				boolean show = true;
				if (v != null) {
					String message = NLS.bind(Messages.FindOrphansActionDelegate_n_entries_found, result.size());
					if (ret == 1)
						message += NLS.bind(Messages.FindOrphansActionDelegate_entries_not_checked, v);
					show = AcousticMessageDialog.openConfirm(getShell(),
							Messages.FindOrphansActionDelegate_orphan_search, message);
				}
				if (show) {
					SmartCollectionImpl collection = new SmartCollectionImpl(Messages.FindOrphansActionDelegate_orphans,
							true, false, true, false, null, 0, null, 0, null, Constants.INHERIT_LABEL, null, 0, 1, null);
					collection.addCriterion(
							new CriterionImpl(ICollectionProcessor.ORPHANS, null, result, null, QueryField.XREF, false));
					Ui.getUi().getNavigationHistory(getActiveWorkbenchWindow())
							.postSelection(new StructuredSelection(collection));
				}
			}
		} catch (InvocationTargetException e) {
			UiActivator.getDefault().logError(Messages.FindOrphansActionDelegate_error_scanning_for_orphans, e);
		} catch (InterruptedException e) {
			// nothing to do
		}
	}

	@Override
	public void dispose() {
		result.clear();
		volumes.clear();
		super.dispose();
	}

}
