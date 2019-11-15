package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.cat.model.meta.Meta;
import com.bdaum.zoom.cat.model.meta.WatchedFolder;
import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.FileWatchManager;
import com.bdaum.zoom.core.internal.ImportFromDeviceData;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.mtp.DeviceInsertionListener;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.wizards.WatchedFolderWizard;

@SuppressWarnings("restriction")
public class TetheredDialog extends ZTitleAreaDialog implements ISelectionChangedListener, DeviceInsertionListener {

	private static final int NEW = 9999;
	private TableViewer viewer;
	private WatchedFolder folder;
	private List<WatchedFolder> watchedFolders = new ArrayList<WatchedFolder>(5);
	private List<WatchedFolder> newFolders = new ArrayList<WatchedFolder>(2);

	public TetheredDialog(Shell parentShell) {
		super(parentShell, HelpContextIds.TETHERED_DIALOG);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.TetheredDialog_target_selection);
		setTitleImage(Icons.tether64.getImage());
		setMessage(Messages.TetheredDialog_please_select);
		fillValues();
		validate();
		Core.getCore().getVolumeManager().addDeviceInsertionListener(this);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 15;
		composite.setLayout(layout);
		viewer = new TableViewer(composite, SWT.V_SCROLL | SWT.SINGLE);
		viewer.getTable().setLayoutData(new GridData(600, 200));
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setWidth(580);
		col.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof WatchedFolder)
					return ((WatchedFolder) element).getUri();
				return String.valueOf(element);
			}
		});
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setComparator(new ViewerComparator());
		viewer.addSelectionChangedListener(this);
		return area;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, NEW, Messages.TetheredDialog_new_folder, false);
		super.createButtonsForButtonBar(parent);
	}

	private void validate() {
		if (viewer.getSelection().isEmpty()) {
			setErrorMessage(Messages.TetheredDialog_nothing_selected);
			getButton(OK).setEnabled(false);
		} else {
			setErrorMessage(null);
			getButton(OK).setEnabled(true);
		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == NEW) {
			WatchedFolder folder = new WatchedFolderImpl(null, null, 0L, false, null, true, null, true, 0, null,
					ImportFromDeviceData.SUBFOLDERPOLICY_YEARMONTH, null, null, Constants.FILESOURCE_DIGITAL_CAMERA,
					true);
			WatchedFolderWizard wizard = new WatchedFolderWizard(false, true, true);
			WizardDialog wizardDialog = new WizardDialog(getShell(), wizard);
			wizard.init(null, new StructuredSelection(folder));
			if (wizardDialog.open() == WizardDialog.OK)
				try {
					folder.setStringId(
							Utilities.computeWatchedFolderId(new File(new URI(folder.getUri())), folder.getVolume()));
					watchedFolders.add(folder);
					viewer.setInput(watchedFolders);
					viewer.setSelection(new StructuredSelection(folder), true);
					newFolders.add(folder);
				} catch (URISyntaxException e1) {
					// should never happend
				}
		} else
			super.buttonPressed(buttonId);
	}

	@Override
	public boolean close() {
		Core.getCore().getVolumeManager().removeDeviceInsertionListener(this);
		return super.close();
	}

	@Override
	protected void okPressed() {
		folder = (WatchedFolder) viewer.getStructuredSelection().getFirstElement();
		folder.setTethered(true);
		CoreActivator activator = CoreActivator.getDefault();
		FileWatchManager fileWatchManager = activator.getFileWatchManager();
		Meta meta = dbManager.getMeta(true);
		List<Object> toBeStored = new ArrayList<Object>(5);
		for (WatchedFolder wf : newFolders) {
			toBeStored.add(wf);
			activator.putObservedFolder(folder);
			fileWatchManager.addImageFolder(folder);
			meta.addWatchedFolder(wf.getStringId());
		}
		for (WatchedFolder wf : watchedFolders)
			if (wf != folder && wf.getTethered() && !toBeStored.contains(wf)) {
				wf.setTethered(false);
				toBeStored.add(wf);
			}
		if (!toBeStored.contains(folder))
			toBeStored.add(folder);
		toBeStored.add(meta);
		dbManager.safeTransaction(null, toBeStored);
		super.okPressed();
	}

	private void fillValues() {
		WatchedFolder selectedFolder = null;
		Meta meta = Core.getCore().getDbManager().getMeta(true);
		if (meta.getWatchedFolder() != null) {
			CoreActivator activator = CoreActivator.getDefault();
			for (String id : meta.getWatchedFolder()) {
				WatchedFolder folder = activator.getObservedFolder(id);
				if (folder != null && folder.getTransfer()) {
					watchedFolders.add(folder);
					if (folder.getTethered())
						selectedFolder = folder;
				}
			}
		}
		viewer.setInput(watchedFolders);
		if (selectedFolder != null)
			viewer.setSelection(new StructuredSelection(selectedFolder), true);
	}

	public WatchedFolder getFolder() {
		return folder;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		validate();
	}

	@Override
	public void deviceInserted() {
		// do nothing
	}

	@Override
	public void deviceEjected() {
		getShell().getDisplay().asyncExec(() -> cancelPressed());
	}

}
