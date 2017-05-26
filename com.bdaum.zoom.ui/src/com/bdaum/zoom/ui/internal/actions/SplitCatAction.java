package com.bdaum.zoom.ui.internal.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.group.exhibition.ExhibitImpl;
import com.bdaum.zoom.cat.model.group.slideShow.SlideImpl;
import com.bdaum.zoom.cat.model.group.webGallery.WebExhibitImpl;
import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.core.CatalogListener;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.db.IDbFactory;
import com.bdaum.zoom.core.db.IDbManager;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.DeleteOperation;
import com.bdaum.zoom.operations.internal.SplitCatOperation;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZProgressMonitorDialog;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.dialogs.EditMetaDialog;
import com.bdaum.zoom.ui.internal.dialogs.SplitCatDialog;

@SuppressWarnings("restriction")
public class SplitCatAction extends AbstractSelectionAction {

	private IAdaptable adaptable;
	private IStatus status;

	public SplitCatAction(IWorkbenchWindow window, String label, String tooltip, ImageDescriptor image,
			IAdaptable adaptable) {
		super(window, label, image);
		setToolTipText(tooltip);
		this.adaptable = adaptable;
	}

	@Override
	public void run() {
		final Shell shell = adaptable.getAdapter(Shell.class);
		final List<Asset> selectedAssets = adaptable.getAdapter(AssetSelection.class).getLocalAssets();
		final int na = selectedAssets.size();
		if (na == 0) {
			AcousticMessageDialog.openInformation(shell, Messages.SplitCatActionDelegate_split_cat,
					Messages.SplitCatActionDelegate_nothing_to_split);
			return;
		}
		BusyIndicator.showWhile(shell.getDisplay(), new Runnable() {
			public void run() {
				Job[] criticalJobs = Job.getJobManager().find(Constants.CRITICAL);
				if (criticalJobs.length > 0) {
					AcousticMessageDialog.openInformation(shell, Messages.MigrateCatActionDelegate_cat_migratiion,
							Messages.SplitCatActionDelegate_cannot_split);
					return;
				}
				final IDbManager dbManager = Core.getCore().getDbManager();
				final Meta oldMeta = dbManager.getMeta(true);
				SplitCatDialog dialog = new SplitCatDialog(shell, oldMeta.getTimeline(), na, !selectedAssets.isEmpty());
				if (dialog.open() == Window.OK) {
					String file = dialog.getFile();
					File catFile = new File(file);
					catFile.delete();
					IDbFactory dbFactory = Core.getCore().getDbFactory();
					final IDbManager newDbManager = dbFactory.createDbManager(file, true, false, false);
					boolean delete = dialog.getDelete();
					if (delete && !AcousticMessageDialog.openConfirm(shell,
							Messages.SplitCatActionDelegate_delete_exported_images,
							Messages.SplitCatActionDelegate_do_you_really_want_to_delete))
						delete = false;
					final String description = dialog.getDescription();
					final String timeline = dialog.getTimeline();
					final String locations = dialog.getLocationOption();

					IRunnableWithProgress runnable1 = new IRunnableWithProgress() {

						public void run(IProgressMonitor monitor)
								throws InvocationTargetException, InterruptedException {
							CoreActivator.getDefault().getFileWatchManager().setPaused(true, this.getClass().toString());
							Job.getJobManager().cancel(Constants.FOLDERWATCH);
							Job.getJobManager().cancel(Constants.SYNCPICASA);
							Core.waitOnJobCanceled(Constants.FOLDERWATCH, Constants.SYNCPICASA);
							SplitCatOperation op = new SplitCatOperation(newDbManager, description, selectedAssets,
									timeline, locations);
							try {
								status = op.execute(monitor, adaptable);
							} catch (ExecutionException e) {
								status = new Status(IStatus.ERROR, UiActivator.PLUGIN_ID,
										Messages.SplitCatActionDelegate_internal_error, e);
							}
						}
					};
					ZProgressMonitorDialog pm = new ZProgressMonitorDialog(shell);
					try {
						pm.run(true, true, runnable1);
						if (!status.isOK()) {
							if (status.getSeverity() == IStatus.ERROR)
								AcousticMessageDialog.openError(shell, Messages.SplitCatActionDelegate_split_catalog,
										NLS.bind(Messages.SplitCatActionDelegate_failed_with_problems,
												status.toString()));
							else if (status.getSeverity() == IStatus.WARNING)
								AcousticMessageDialog.openWarning(shell, Messages.SplitCatActionDelegate_split_catalog,
										NLS.bind(Messages.SplitCatActionDelegate_finished_with_warnings,
												status.toString()));
							if (status.getSeverity() == IStatus.ERROR || status.getSeverity() == IStatus.CANCEL) {
								catFile.delete();
								return;
							}
						}
						new EditMetaDialog(shell, adaptable.getAdapter(IWorkbenchPage.class), newDbManager, false, null)
								.open();
						newDbManager.close(CatalogListener.NORMAL);
						if (delete) {
							List<Asset> toBeDeleted = new ArrayList<Asset>(selectedAssets.size());
							for (Asset asset : selectedAssets) {
								String assetId = asset.getStringId();
								List<SlideImpl> slides = dbManager.obtainObjects(SlideImpl.class, "asset", assetId, //$NON-NLS-1$
										QueryField.EQUALS);
								List<ExhibitImpl> exhibits = dbManager.obtainObjects(ExhibitImpl.class, "asset", //$NON-NLS-1$
										assetId, QueryField.EQUALS);
								List<WebExhibitImpl> webexhibits = dbManager.obtainObjects(WebExhibitImpl.class,
										"asset", assetId, QueryField.EQUALS); //$NON-NLS-1$
								if (slides.isEmpty() && exhibits.isEmpty() && webexhibits.isEmpty())
									toBeDeleted.add(asset);
							}
							OperationJob.executeOperation(
									new DeleteOperation(toBeDeleted, false, null, null, null, null), adaptable);
						}
					} catch (InvocationTargetException e) {
						newDbManager.close(CatalogListener.NORMAL);
						UiActivator.getDefault().logError(Messages.SplitCatActionDelegate_error_when_splitting, e);
						catFile.delete();
					} catch (InterruptedException e) {
						newDbManager.close(CatalogListener.NORMAL);
						catFile.delete();
					} finally {
						UiActivator.getDefault().postCatInit(false);
						CoreActivator.getDefault().getFileWatchManager().setPaused(false, this.getClass().toString());
					}
				}
			}
		});
	}

}
