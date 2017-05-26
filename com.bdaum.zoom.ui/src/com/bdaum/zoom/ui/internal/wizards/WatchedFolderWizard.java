package com.bdaum.zoom.ui.internal.wizards;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;

public class WatchedFolderWizard extends ZWizard implements IWorkbenchWizard,
		IAdaptable {

	private WatchedFolderImpl watchedFolder;
	private IWorkbenchWindow window;
	private WatchedFolderSelectionPage folderSelectionPage;
	private WatchedFolderTargetPage targetPage;
	private WatchedFolderRenamingPage renamingPage;
	private TransferPage transferPage;
	private FilterPage filterPage;

	public WatchedFolderWizard() {
		setHelpAvailable(true);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter))
			return window.getShell();
		return null;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Constants.APPLICATION_NAME);
		setDialogSettings(UiActivator.getDefault(),
				ImportFromDeviceWizard.MEDIAID);
		if (workbench == null)
			workbench = PlatformUI.getWorkbench();
		window = workbench.getActiveWorkbenchWindow();
		if (window == null)
			window = workbench.getWorkbenchWindows()[0];
		watchedFolder = (WatchedFolderImpl) selection.getFirstElement();
	}

	@Override
	public void addPages() {
		ImageDescriptor imageDescriptor = Icons.watchedFolder.getDescriptor();
		folderSelectionPage = new WatchedFolderSelectionPage(
				Messages.WatchedFolderWizard_folder_selection, watchedFolder);
		folderSelectionPage.setImageDescriptor(imageDescriptor);
		addPage(folderSelectionPage);
		transferPage = new TransferPage(
				Messages.WatchedFolderWizard_transfer_parameters, watchedFolder);
		transferPage.setImageDescriptor(imageDescriptor);
		addPage(transferPage);
		filterPage = new FilterPage(Messages.WatchedFolderWizard_file_filters,
				watchedFolder);
		filterPage.setImageDescriptor(imageDescriptor);
		addPage(filterPage);
		targetPage = new WatchedFolderTargetPage(
				Messages.WatchedFolderWizard_target_selection, watchedFolder);
		targetPage.setImageDescriptor(imageDescriptor);
		addPage(targetPage);
		renamingPage = new WatchedFolderRenamingPage(
				Messages.WatchedFolderWizard_file_renaming, watchedFolder);
		renamingPage.setImageDescriptor(imageDescriptor);
		addPage(renamingPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == folderSelectionPage)
			return folderSelectionPage.isTransfer() ? transferPage : filterPage;
		if (page == transferPage)
			return targetPage;
		if (page == filterPage)
			return null;
		return super.getNextPage(page);
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (page == filterPage)
			return folderSelectionPage;
		return super.getPreviousPage(page);
	}

	@Override
	public boolean canFinish() {
		if (folderSelectionPage.isTransfer())
			return folderSelectionPage.isPageComplete()
					&& transferPage.isPageComplete()
					&& targetPage.isPageComplete()
					&& renamingPage.isPageComplete();
		return folderSelectionPage.isPageComplete()
				&& filterPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		folderSelectionPage.performFinish();
		if (folderSelectionPage.isTransfer())
			transferPage.performFinish();
		else
			filterPage.performFinish();
		targetPage.performFinish();
		if (folderSelectionPage.isTransfer())
			renamingPage.performFinish();
		saveDialogSettings();
		return true;
	}

	public WatchedFolder getResult() {
		return watchedFolder;
	}

}
