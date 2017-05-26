package com.bdaum.zoom.ui.internal.job;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.ui.internal.views.FieldEntry;
import com.bdaum.zoom.ui.internal.views.IFieldUpdater;
import com.bdaum.zoom.ui.internal.views.Messages;

public class SupplyPropertyJob extends Job {

	private final QueryField qfield;
	private final List<Asset> selectedAssets;
	private final IFieldUpdater updater;

	public SupplyPropertyJob(QueryField qfield, List<Asset> selectedAssets,
			IFieldUpdater updater) {
		super(
				NLS.bind(
						Messages.getString("AbstractPropertiesView.supply_property"), qfield)); //$NON-NLS-1$
		this.qfield = qfield;
		this.selectedAssets = selectedAssets;
		this.updater = updater;
		setSystem(true);
		setPriority(INTERACTIVE);
	}

	@Override
	public boolean belongsTo(Object family) {
		return Constants.PROPERTYPROVIDER == family;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		final Object value = qfield.obtainFieldValue(selectedAssets, monitor);
		final boolean editable = qfield.isEditable(selectedAssets);
		final boolean applicable = qfield.isApplicable(selectedAssets);
		if (!updater.isDisposed()) {
			updater.getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (!updater.isDisposed()) {
						updater.updateField(qfield, new FieldEntry(value,
								editable, applicable));
					}
				}
			});
		}
		return Status.OK_STATUS;
	}
}