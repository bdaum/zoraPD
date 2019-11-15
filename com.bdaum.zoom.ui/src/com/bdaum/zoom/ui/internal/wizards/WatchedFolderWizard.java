package com.bdaum.zoom.ui.internal.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;

public class WatchedFolderWizard extends ZWizard implements IWorkbenchWizard {

	private WatchedFolderImpl watchedFolder;
	private WatchedFolderSelectionPage folderSelectionPage;
	private WatchedFolderTargetPage targetPage;
	private WatchedFolderRenamingPage renamingPage;
	private TransferPage transferPage;
	private FilterPage filterPage;
	private boolean typeChoice;
	private boolean subfolderChoice;
	private boolean metaPage;

	public WatchedFolderWizard(boolean typeChoice, boolean subfolderChoice, boolean metaPage) {
		this.typeChoice = typeChoice;
		this.subfolderChoice = subfolderChoice;
		this.metaPage = metaPage;
		setHelpAvailable(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		setWindowTitle(Constants.APPLICATION_NAME);
		setDialogSettings(UiActivator.getDefault(), ImportFromDeviceWizard.MEDIAID);
		watchedFolder = (WatchedFolderImpl) selection.getFirstElement();
	}

	@Override
	public void addPages() {
		ImageDescriptor imageDescriptor = Icons.watchedFolder.getDescriptor();
		folderSelectionPage = new WatchedFolderSelectionPage(Messages.WatchedFolderWizard_folder_selection,
				watchedFolder, typeChoice, subfolderChoice);
		folderSelectionPage.setImageDescriptor(imageDescriptor);
		addPage(folderSelectionPage);
		transferPage = new TransferPage(Messages.WatchedFolderWizard_transfer_parameters, watchedFolder);
		transferPage.setImageDescriptor(imageDescriptor);
		addPage(transferPage);
		filterPage = new FilterPage(Messages.WatchedFolderWizard_file_filters, watchedFolder);
		filterPage.setImageDescriptor(imageDescriptor);
		addPage(filterPage);
		targetPage = new WatchedFolderTargetPage(Messages.WatchedFolderWizard_target_selection, watchedFolder);
		targetPage.setImageDescriptor(imageDescriptor);
		addPage(targetPage);
		renamingPage = new WatchedFolderRenamingPage(Messages.WatchedFolderWizard_file_renaming, watchedFolder);
		renamingPage.setImageDescriptor(imageDescriptor);
		addPage(renamingPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == folderSelectionPage)
			return folderSelectionPage.isTransfer() ? metaPage ? transferPage : targetPage : filterPage;
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
		if (page == targetPage && !metaPage)
			return folderSelectionPage;
		return super.getPreviousPage(page);
	}

	@Override
	public boolean canFinish() {
		if (folderSelectionPage.isTransfer())
			return folderSelectionPage.isPageComplete() && transferPage.isPageComplete() && targetPage.isPageComplete()
					&& renamingPage.isPageComplete();
		return folderSelectionPage.isPageComplete() && filterPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		folderSelectionPage.performFinish();
		if (folderSelectionPage.isTransfer()) {
			if (metaPage)
				transferPage.performFinish();
		} else
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
