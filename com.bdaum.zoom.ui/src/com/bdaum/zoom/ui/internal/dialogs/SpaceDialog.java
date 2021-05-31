package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.actions.Messages;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;

public class SpaceDialog extends ZTitleAreaDialog implements Listener {

	private final int remoteImages;
	private final int externalImages;
	private final int localImages;
	private final int voiceFiles;
	private final int recipes;
	private final long imageSize;
	private final long catSize;
	private OutputTargetGroup output;
	private File targetFile;
	private CheckboxButton catReadonlyButton;
	private CheckboxButton fileReadonlyButton;
	private boolean fileReadonly;
	private boolean catReadonly;

	public SpaceDialog(Shell parentShell, int remoteImages, int externalImages, int localImages, int voiceFiles,
			int recipes, long imageSize, long catSize) {
		super(parentShell, HelpContextIds.ARCHIVE_DIALOG);
		this.remoteImages = remoteImages;
		this.externalImages = externalImages;
		this.localImages = localImages;
		this.voiceFiles = voiceFiles;
		this.recipes = recipes;
		this.imageSize = imageSize;
		this.catSize = catSize;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Constants.APPLICATION_NAME);
		setTitle(Messages.ArchiveAction_archive_selection);
		setMessage(Messages.ArchiveAction_select_an_archive_dest);
		validate();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite comp = new Composite(area, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		Label statLabel = new Label(comp, SWT.NONE);
		statLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		statLabel.setFont(JFaceResources.getHeaderFont());
		statLabel.setText(Messages.ArchiveAction_statistics);
		new Label(comp, SWT.NONE).setText(Messages.ArchiveAction_remote_images);
		new Label(comp, SWT.NONE).setText(String.valueOf(remoteImages));
		new Label(comp, SWT.NONE).setText(Messages.ArchiveAction_external_images);
		new Label(comp, SWT.NONE).setText(String.valueOf(externalImages));
		new Label(comp, SWT.NONE).setText(Messages.ArchiveAction_local_images);
		new Label(comp, SWT.NONE).setText(String.valueOf(localImages));
		new Label(comp, SWT.NONE).setText(Messages.ArchiveAction_voice_viles);
		new Label(comp, SWT.NONE).setText(String.valueOf(voiceFiles));
		new Label(comp, SWT.NONE).setText(Messages.ArchiveAction_recipes);
		new Label(comp, SWT.NONE).setText(String.valueOf(recipes));
		Label sizeLabel = new Label(comp, SWT.NONE);
		sizeLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		sizeLabel.setFont(JFaceResources.getHeaderFont());
		sizeLabel.setText(Messages.ArchiveAction_required_space);
		new Label(comp, SWT.NONE).setText(Messages.ArchiveAction_images);
		new Label(comp, SWT.NONE).setText(Format.sizeFormatter.format(imageSize));
		new Label(comp, SWT.NONE).setText(Messages.ArchiveAction_catalog);
		new Label(comp, SWT.NONE).setText(Format.sizeFormatter.format(catSize));
		new Label(comp, SWT.NONE).setText(Messages.ArchiveAction_total);
		new Label(comp, SWT.NONE).setText(Format.sizeFormatter.format(imageSize + catSize));
		output = new OutputTargetGroup(comp, new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1), this, false, false,
				false);
		catReadonlyButton = WidgetFactory.createCheckButton(comp, Messages.ArchiveAction_cat_readonly, null);
		fileReadonlyButton = WidgetFactory.createCheckButton(comp, Messages.ArchiveAction_files_readonly, null);
		return area;
	}

	public void validate() {
		String localFolder = output.getLocalFolder();
		String errorMessage = null;
		if (localFolder == null || localFolder.isEmpty())
			errorMessage = Messages.ArchiveAction_specify_output;
		else {
			File folder = new File(localFolder);
			if (!folder.exists())
				errorMessage = Messages.ArchiveAction_destination_does_not_exist;
			else {
				File[] list = folder.listFiles();
				if (list != null && list.length > 0)
					errorMessage = Messages.ArchiveAction_destination_must_be_empty;
				else {
					long freeSpace = Core.getCore().getVolumeManager().getRootFile(folder).getUsableSpace();
					long total = imageSize + catSize;
					if (freeSpace < total)
						errorMessage = NLS.bind(Messages.ArchiveAction_output_dest_too_small,
								Format.sizeFormatter.format(freeSpace), Format.sizeFormatter.format(total));
				}
			}
		}
		setErrorMessage(errorMessage);
		getButton(OK).setEnabled(errorMessage == null);
	}

	@Override
	protected void okPressed() {
		targetFile = new File(output.getLocalFolder());
		catReadonly = catReadonlyButton.getSelection();
		fileReadonly = fileReadonlyButton.getSelection();
		super.okPressed();
	}

	public File getTargetFile() {
		return targetFile;
	}

	/**
	 * @return fileReadonly
	 */
	public boolean isFileReadonly() {
		return fileReadonly;
	}

	/**
	 * @return catReadonly
	 */
	public boolean isCatReadonly() {
		return catReadonly;
	}

	public void handleEvent(Event e) {
		validate();
	}

}