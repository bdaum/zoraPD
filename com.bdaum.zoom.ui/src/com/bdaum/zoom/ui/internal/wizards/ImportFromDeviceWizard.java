package com.bdaum.zoom.ui.internal.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.FileNameExtensionFilter;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.core.internal.operations.AnalogProperties;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.CompoundOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.operations.internal.AddAlbumOperation;
import com.bdaum.zoom.operations.internal.AutoRatingOperation;
import com.bdaum.zoom.operations.internal.ImportOperation;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ImportFromDeviceWizard extends ZWizard implements IWorkbenchWizard, IAdaptable {

	public static final String MEDIAID = "com.bdaum.zoom.importFromDevice"; //$NON-NLS-1$
	private static final String FOLDERID = "com.bdaum.zoom.importFromFolder"; //$NON-NLS-1$

	private boolean media;
	private File[] folders;
	private IWorkbenchWindow window;
	private ImportFileSelectionPage fileSelectionPage;
	private ImportFromDeviceData importData;
	private ImportTargetPage targetPage;
	private ImportRenamingPage renamingPage;
	private ColoredWizardPage lastPage;
	private boolean eject;
	private File[] files;
	private boolean newStruct;
	private SmartCollectionImpl collection;
	private boolean analog;

	public ImportFromDeviceWizard(File[] files, File[] folders, boolean media, boolean eject, boolean newStruct,
			SmartCollectionImpl collection, boolean analog) {
		this();
		this.files = files;
		this.folders = folders;
		this.media = media;
		this.eject = eject;
		this.newStruct = newStruct;
		this.collection = collection;
		this.analog = analog;
		importData = new ImportFromDeviceData(folders, media, null);
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
		ImageDescriptor imageDescriptor = media ? Icons.importDevice.getDescriptor()
				: Icons.importNewStructure.getDescriptor();
		if (newStruct) {
			if (folders != null) {
				fileSelectionPage = new ImportFileSelectionPage(Messages.ImportFromDeviceWizard_file_sel, media, eject);
				fileSelectionPage.setImageDescriptor(imageDescriptor);
				addPage(fileSelectionPage);
			}
			targetPage = new ImportTargetPage(Messages.ImportFromDeviceWizard_target_sel, media);
			targetPage.setImageDescriptor(imageDescriptor);
			addPage(targetPage);
			renamingPage = new ImportRenamingPage(Messages.ImportFromDeviceWizard_file_ren, media);
			renamingPage.setImageDescriptor(imageDescriptor);
			addPage(renamingPage);
		}
		lastPage = analog ? new ImportAnalogPropertiesPage(Messages.ImportFromDeviceWizard_analog_props)
				: new ImportAddMetadataPage(Messages.ImportFromDeviceWizard_meta, media, collection, newStruct);
		lastPage.setImageDescriptor(imageDescriptor);
		addPage(lastPage);
	}

	protected boolean needsAdvancedOptions() {
		return fileSelectionPage.needsAdvancedOptions();
	}

	@Override
	public boolean performFinish() {
		BusyIndicator.showWhile(getShell().getDisplay(), () -> {
			FileInput fileInput = null;
			if (fileSelectionPage != null)
				fileSelectionPage.performFinish(importData);
			else {
				List<File> images = new ArrayList<>();
				FileNameExtensionFilter filter = CoreActivator.getDefault().getFilenameExtensionFilter();
				Utilities.collectFilteredFiles(files, images, filter);
				Utilities.collectFilteredFiles(folders, images, filter);
				fileInput = new FileInput(images, false);
			}
			if (targetPage != null)
				targetPage.performFinish(importData);
			if (renamingPage != null)
				renamingPage.performFinish(importData);
			AnalogProperties analogProps = null;
			if (lastPage instanceof ImportAddMetadataPage)
				((ImportAddMetadataPage) lastPage).performFinish(importData);
			else {
				((ImportAnalogPropertiesPage) lastPage).performFinish();
				analogProps = ((ImportAnalogPropertiesPage) lastPage).getResult();
			}
			saveDialogSettings();
			IProfiledOperation op;
			ImportConfiguration config = UiActivator.getDefault()
					.createImportConfiguration(ImportFromDeviceWizard.this);
			if (newStruct) {
				if (fileInput != null)
					importData.setFileInput(fileInput);
				if (analog)
					op = new ImportOperation(Messages.ImportFromDeviceWizard_import_analog, config, importData,
							analogProps, Constants.FILESOURCE_UNKNOWN);
				else
					op = new ImportOperation(importData, config,
							media ? Constants.FILESOURCE_DIGITAL_CAMERA : Constants.FILESOURCE_UNKNOWN);
			} else if (analog)
				op = new ImportOperation(new FileInput(files, false), config, analogProps, null);
			else
				op = new ImportOperation(fileInput, config, null, folders);
			CompoundOperation compoundOperation = null;
			if (lastPage instanceof ImportAddMetadataPage && ((ImportAddMetadataPage) lastPage).addToAlbum()) {
				compoundOperation = new CompoundOperation(op.getLabel());
				compoundOperation.addOperation(op);
				compoundOperation.addOperation(new AddAlbumOperation(collection, (ImportOperation) op));
			}
			String providerId;
			String modelId;
			boolean overwrite;
			int maxRating;
			if (lastPage instanceof ImportAddMetadataPage) {
				ImportAddMetadataPage metadataPage = (ImportAddMetadataPage) lastPage;
				providerId = metadataPage.getProviderId();
				modelId = metadataPage.getModelId();
				overwrite = metadataPage.getOverwrite();
				maxRating = metadataPage.getMaxRating();
			} else {
				providerId = analogProps.providerId;
				modelId = analogProps.modelId;
				overwrite = analogProps.overwriteRating;
				maxRating = analogProps.maxRating;
			}
			if (providerId != null) {
				if (compoundOperation == null) {
					compoundOperation = new CompoundOperation(op.getLabel());
					compoundOperation.addOperation(op);
				}
				compoundOperation.addOperation(
						new AutoRatingOperation((ImportOperation) op, providerId, modelId, overwrite, maxRating));
			}
			OperationJob.executeOperation(compoundOperation != null ? compoundOperation : op, this);
		});
		return true;
	}

	public ImportFromDeviceData getImportData() {
		return importData;
	}

	public File[] getDcims() {
		return folders;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		if (Shell.class.equals(adapter))
			return window.getShell();
		return null;
	}

	public List<File> getSelectedFiles() {
		return fileSelectionPage != null ? fileSelectionPage.getSelectedFiles() : Arrays.asList(files);
	}

	public void updateAuthor() {
		((ImportAddMetadataPage) lastPage).updateAuthor();
	}

	public File[] getFiles() {
		if (folders == null || folders.length == 0)
			return files;
		List<File> dropped = new ArrayList<File>();
		if (files != null) {
			lp: for (File file : files) {
				File parent = file.getParentFile();
				while (parent != null) {
					for (File dcim : folders)
						if (dcim.equals(parent))
							continue lp;
					parent = parent.getParentFile();
				}
				dropped.add(file);
			}
		}
		return dropped.toArray(new File[dropped.size()]);
	}

	public String getVolume() {
		File file = null;
		if (files != null && files.length > 0)
			file = files[0];
		else if (folders != null && folders.length > 0)
			file = folders[0];
		return file != null ? Core.getCore().getVolumeManager().getVolumeForFile(file) : null;
	}

}
