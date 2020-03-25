package com.bdaum.zoom.ui.internal.wizards;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.bdaum.zoom.cat.model.group.SmartCollectionImpl;
import com.bdaum.zoom.cat.model.meta.LastDeviceImport;
import com.bdaum.zoom.cat.model.meta.LastDeviceImportImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileInput;
import com.bdaum.zoom.core.internal.FileNameExtensionFilter;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.core.internal.operations.AnalogProperties;
import com.bdaum.zoom.core.internal.operations.ImportConfiguration;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.mtp.DeviceInsertionListener;
import com.bdaum.zoom.mtp.StorageObject;
import com.bdaum.zoom.operations.CompoundOperation;
import com.bdaum.zoom.operations.IProfiledOperation;
import com.bdaum.zoom.operations.internal.AddAlbumOperation;
import com.bdaum.zoom.operations.internal.ImportOperation;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

@SuppressWarnings("restriction")
public class ImportFromDeviceWizard extends ZWizard implements IWorkbenchWizard, DeviceInsertionListener {

	public static final String MEDIAID = "com.bdaum.zoom.importFromDevice"; //$NON-NLS-1$
	private static final String FOLDERID = "com.bdaum.zoom.importFromFolder"; //$NON-NLS-1$

	private boolean media;
	private StorageObject[] folders;
	private ImportFileSelectionPage fileSelectionPage;
	private ImportFromDeviceData importData;
	private ImportTargetPage targetPage;
	private ImportRenamingPage renamingPage;
	private ColoredWizardPage lastPage;
	private boolean eject;
	private StorageObject[] files;
	private boolean newStruct;
	private SmartCollectionImpl collection;
	private boolean analog;
	private String errorMessage;
	private LastDeviceImport newDevice;

	public ImportFromDeviceWizard(StorageObject[] files, StorageObject[] folders, boolean media, boolean eject,
			boolean newStruct, SmartCollectionImpl collection, boolean analog) {
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
		super.init(workbench, selection);
		setDialogSettings(UiActivator.getDefault(), media ? MEDIAID : FOLDERID);
		setWindowTitle(Constants.APPLICATION_NAME);
		Core.getCore().getVolumeManager().addDeviceInsertionListener(this);
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
		errorMessage = null;
		cleanUp();
		List<StorageObject> images = new ArrayList<>();
		BusyIndicator.showWhile(getShell().getDisplay(), () -> {
			FileInput fileInput = null;
			try {
				if (fileSelectionPage != null)
					errorMessage = fileSelectionPage.performFinish(importData);
				else {
					FileNameExtensionFilter filter = CoreActivator.getDefault().getFilenameExtensionFilter();
					StorageObject.collectFilteredFiles(files, images, filter, false, null);
					StorageObject.collectFilteredFiles(folders, images, filter, true, null);
					fileInput = new FileInput(images, false);
				}
			} catch (IOException e) {
				errorMessage = Messages.ImportFromDeviceWizard_io_error_scanning;
				return;
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
			Core.getCore().getDbManager().safeTransaction(null, getNewDevice());
			IProfiledOperation op;
			ImportConfiguration config = UiActivator.getDefault()
					.createImportConfiguration(ImportFromDeviceWizard.this);
			if (newStruct) {
				if (fileInput != null)
					importData.setFileInput(fileInput);
				op = analog
						? new ImportOperation(Messages.ImportFromDeviceWizard_import_analog, config, importData,
								analogProps, Constants.FILESOURCE_UNKNOWN)
						: new ImportOperation(importData, config,
								media ? Constants.FILESOURCE_DIGITAL_CAMERA : Constants.FILESOURCE_UNKNOWN);
			} else if (analog)
				op = new ImportOperation(new FileInput(files, false), config, importData, analogProps, null);
			else {
				List<File> foldersToWatch = new ArrayList<File>(folders.length);
				for (StorageObject folder : folders)
					if (folder.isLocal())
						foldersToWatch.add((File) folder.getNativeObject());

				op = new ImportOperation(fileInput, config, importData, null,
						foldersToWatch.toArray(new File[foldersToWatch.size()]));
			}
			CompoundOperation compoundOperation = null;
			if (lastPage instanceof ImportAddMetadataPage && ((ImportAddMetadataPage) lastPage).addToAlbum()) {
				compoundOperation = new CompoundOperation(op.getLabel());
				compoundOperation.addOperation(op);
				compoundOperation.addOperation(new AddAlbumOperation(collection, (ImportOperation) op));
			}
//			String providerId;
//			String modelId;
//			boolean overwrite;
//			int maxRating;
//			if (lastPage instanceof ImportAddMetadataPage) {
//				ImportAddMetadataPage metadataPage = (ImportAddMetadataPage) lastPage;
//				providerId = metadataPage.getProviderId();
//				modelId = metadataPage.getModelId();
//				overwrite = metadataPage.getOverwrite();
//				maxRating = metadataPage.getMaxRating();
//			} else {
//				providerId = analogProps.providerId;
//				modelId = analogProps.modelId;s
//				overwrite = analogProps.overwriteRating;
//				maxRating = analogProps.maxRating;
//			}
//			if (providerId != null) {
//				if (compoundOperation == null) {
//					compoundOperation = new CompoundOperation(op.getLabel());
//					compoundOperation.addOperation(op);
//				}
//				compoundOperation.addOperation(
//						new AutoRatingOperation((ImportOperation) op, providerId, modelId, overwrite, maxRating));
//			}
			OperationJob.executeOperation(compoundOperation != null ? compoundOperation : op, this);
		});
		((WizardDialog) getContainer()).setErrorMessage(errorMessage);
		return errorMessage == null;
	}

	private void cleanUp() {
		Core.getCore().getVolumeManager().removeDeviceInsertionListener(this);
	}

	@Override
	public boolean performCancel() {
		cleanUp();
		return true;
	}

	public ImportFromDeviceData getImportData() {
		return importData;
	}

	public StorageObject[] getDcims() {
		return folders;
	}

	public List<StorageObject> getSelectedFiles() {
		return fileSelectionPage != null ? fileSelectionPage.getSelectedFiles() : Arrays.asList(files);
	}

	public StorageObject getFirstSelectedFile() {
		return fileSelectionPage != null ? fileSelectionPage.getFirstSelectedFile()
				: files != null && files.length > 0 ? files[0] : null;
	}

	public void updateValues() {
		LastDeviceImport currentDevice = getCurrentDevice();
		if (fileSelectionPage != null)
			fileSelectionPage.updateValues(currentDevice);
		if (targetPage != null)
			targetPage.updateValues(currentDevice);
		if (renamingPage != null)
			renamingPage.updateValues(currentDevice);
		if (lastPage instanceof ImportAddMetadataPage)
			((ImportAddMetadataPage) lastPage).updateValues(currentDevice);
	}

	public StorageObject[] getFiles() {
		if (folders == null || folders.length == 0)
			return files;
		List<StorageObject> dropped = new ArrayList<>();
		if (files != null) {
			lp: for (StorageObject file : files) {
				try {
					StorageObject parent = file.getParentObject();
					while (parent != null) {
						for (StorageObject dcim : folders)
							if (dcim.equals(parent))
								continue lp;
						parent = parent.getParentObject();
					}
					dropped.add(file);
				} catch (IOException e) {
					// connection lost?
				}
			}
		}
		return dropped.toArray(new StorageObject[dropped.size()]);
	}

	public String getVolume() {
		StorageObject file = null;
		if (files != null && files.length > 0)
			file = files[0];
		else if (folders != null && folders.length > 0)
			file = folders[0];
		return file != null ? file.getVolume() : null;
	}

	@Override
	public void deviceInserted() {
		// do nothing
	}

	@Override
	public void deviceEjected() {
		IWizardContainer container = getContainer();
		if (container instanceof WizardDialog) {
			fileSelectionPage.cancel();
			WizardDialog wizardDialog = (WizardDialog) container;
			wizardDialog.getShell().getDisplay().asyncExec(() -> {
				if (!wizardDialog.getShell().isDisposed())
					wizardDialog.close();
			});
		}
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage nextPage = super.getNextPage(page);
		if (nextPage == targetPage)
			targetPage.update();
		else if (nextPage == renamingPage)
			renamingPage.update();
		return nextPage;
	}

	public LastDeviceImport getCurrentDevice() {
		String volume = getVolume();
		return volume != null ? Core.getCore().getDbManager().getMeta(true).getLastDeviceImport(volume) : null;
	}

	public LastDeviceImport getNewDevice() {
		if (newDevice == null) {
			LastDeviceImport currentDevice = getCurrentDevice();
			newDevice = currentDevice != null ? currentDevice
					: new LastDeviceImportImpl(getVolume(), 0L, null, null, null, null, null, null, null, null, null,
							null, null, null, null);
		}
		return newDevice;
	}
}
