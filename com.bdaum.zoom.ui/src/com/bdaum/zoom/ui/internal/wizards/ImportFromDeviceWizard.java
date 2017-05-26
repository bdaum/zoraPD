package com.bdaum.zoom.ui.internal.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ImportOperation;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;

@SuppressWarnings("restriction")
public class ImportFromDeviceWizard extends ZWizard implements
		IWorkbenchWizard, IAdaptable {

	public static final String MEDIAID = "com.bdaum.zoom.importFromDevice"; //$NON-NLS-1$
	private static final String FOLDERID = "com.bdaum.zoom.importFromFolder"; //$NON-NLS-1$

	private boolean media;
	private File[] dcims;
	private IWorkbenchWindow window;
	private ImportFileSelectionPage fileSelectionPage;
	private ImportFromDeviceData importData;
	private ImportTargetPage targetPage;
	private ImportRenamingPage renamingPage;
	private ImportAddMetadataPage metaDataPage;
	private boolean eject;
	private File[] files;

	public ImportFromDeviceWizard(File[] files, File[] dcims, boolean media,
			boolean eject) {
		this();
		this.files = files;
		this.dcims = dcims;
		this.media = media;
		this.eject = eject;
		importData = new ImportFromDeviceData(dcims, media, null);
	}

	public ImportFromDeviceWizard() {
		setHelpAvailable(true);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setDialogSettings(UiActivator.getDefault(), media ? MEDIAID : FOLDERID);
		setWindowTitle(Constants.APPLICATION_NAME);
		if (workbench == null)
			workbench = PlatformUI.getWorkbench();
		window = workbench.getActiveWorkbenchWindow();
		if (window == null)
			window = workbench.getWorkbenchWindows()[0];
	}

	@Override
	public void addPages() {
		ImageDescriptor imageDescriptor = media ? Icons.importDevice
				.getDescriptor() : Icons.importNewStructure.getDescriptor();
		fileSelectionPage = new ImportFileSelectionPage(
				Messages.ImportFromDeviceWizard_file_sel, media, eject);
		fileSelectionPage.setImageDescriptor(imageDescriptor);
		addPage(fileSelectionPage);
		targetPage = new ImportTargetPage(
				Messages.ImportFromDeviceWizard_target_sel, media);
		targetPage.setImageDescriptor(imageDescriptor);
		addPage(targetPage);
		renamingPage = new ImportRenamingPage(
				Messages.ImportFromDeviceWizard_file_ren, media);
		renamingPage.setImageDescriptor(imageDescriptor);
		addPage(renamingPage);
		metaDataPage = new ImportAddMetadataPage(
				Messages.ImportFromDeviceWizard_meta, media);
		metaDataPage.setImageDescriptor(imageDescriptor);
		addPage(metaDataPage);
	}

	protected boolean needsAdvancedOptions() {
		return fileSelectionPage.needsAdvancedOptions();
	}

	@Override
	public boolean performFinish() {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				fileSelectionPage.performFinish(importData);
				targetPage.performFinish(importData);
				renamingPage.performFinish(importData);
				metaDataPage.performFinish(importData);
				saveDialogSettings();
				OperationJob.executeOperation(
						new ImportOperation(importData, UiActivator
								.getDefault().createImportConfiguration(
										ImportFromDeviceWizard.this),
								media ? Constants.FILESOURCE_DIGITAL_CAMERA
										: Constants.FILESOURCE_UNKNOWN),
						ImportFromDeviceWizard.this);
			}
		});
		return true;
	}

	/**
	 * @return the importData
	 */
	public ImportFromDeviceData getImportData() {
		return importData;
	}

	/**
	 * @return the dcims
	 */
	public File[] getDcims() {
		return dcims;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter))
			return window.getShell();
		return null;
	}

	public List<File> getSelectedFiles() {
		return fileSelectionPage.getSelectedFiles();
	}

	public void updateAuthor() {
		metaDataPage.updateAuthor();
	}

	/**
	 * @return files
	 */
	public File[] getFiles() {
		if (dcims == null || dcims.length == 0)
			return files;
		List<File> dropped = new ArrayList<File>();
		if (files != null) {
			lp: for (File file : files) {
				File parent = file.getParentFile();
				while (parent != null) {
					for (File dcim : dcims)
						if (dcim.equals(parent))
							continue lp;
					parent = parent.getParentFile();
				}
				dropped.add(file);
			}
		}
		return dropped.toArray(new File[dropped.size()]);
	}

}
