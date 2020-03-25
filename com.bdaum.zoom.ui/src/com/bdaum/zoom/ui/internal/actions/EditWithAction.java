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
 * (c) 2009-2019 Berthold Daum  
 */

package com.bdaum.zoom.ui.internal.actions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.bdaum.zoom.cat.model.asset.Asset;
import com.bdaum.zoom.cat.model.asset.AssetImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IVolumeManager;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.job.OperationJob;
import com.bdaum.zoom.operations.internal.ExportMetadataOperation;
import com.bdaum.zoom.operations.internal.SetLastEditorOperation;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.ui.AssetSelection;
import com.bdaum.zoom.ui.dialogs.ZListDialog;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.AssociationWarningDialog;
import com.bdaum.zoom.ui.internal.preferences.EditorDescriptor;
import com.bdaum.zoom.ui.internal.preferences.FileAssociationsPreferencePage;
import com.bdaum.zoom.ui.internal.preferences.FileEditorMapping;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CLink;

@SuppressWarnings("restriction")
public class EditWithAction extends Action {

	protected static final String MIXED = "{mixed}"; //$NON-NLS-1$
	private List<Image> images = new ArrayList<Image>();
	private final IAdaptable adaptable;
	private Shell shell;
	protected Set<EditorDescriptor> defaultEditors;
	protected Set<EditorDescriptor> editors;
	protected String lastEditor;
	protected AssetSelection assetSelection;
	private Set<String> visited;
	private IVolumeManager volumeManager;
	protected boolean shift;

	public EditWithAction(String label, String tooltip, ImageDescriptor image, IAdaptable adaptable) {
		super(label, image);
		setToolTipText(tooltip);
		this.adaptable = adaptable;
		shell = adaptable.getAdapter(Shell.class);
		volumeManager = Core.getCore().getVolumeManager();
	}
	
	@Override
	public void runWithEvent(Event event) {
		shift = (event.stateMask & SWT.SHIFT) != 0;
		assetSelection = adaptable.getAdapter(AssetSelection.class);
		visited = new HashSet<String>(assetSelection.size() * 3 / 2);
		try {
			doRun();
		} finally {
			for (Image image : images)
				image.dispose();
			images.clear();
		}
	}

	private void doRun() {
		if (!assetSelection.isEmpty()) {
			List<String> parms = new ArrayList<String>(assetSelection.size());
			for (Asset asset : assetSelection.getAssets()) {
				URI uri = asset.getFileState() == IVolumeManager.PEER ? null
						: volumeManager.findExistingFile(asset, true);
				if (uri != null)
					parms.add((new File(uri)).getAbsolutePath());
			}
			if (UiActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.AUTOEXPORT))
				exportMetaData();
			List<Asset> errands = computeAllEditors();
			if (!errands.isEmpty())
				UiUtilities.showFilesAreOffline(shell, errands, errands.size() == assetSelection.size(),
						Messages.EditWithAction_files_offline_cannot_be_edited);
			if (errands.size() < assetSelection.size())
				selectAndLaunchEditor(parms);
		}
	}

	protected void selectAndLaunchEditor(List<String> parms) {
		EditorDescriptor editor = showDialog(visited.size() != 1 ? null : (String) visited.toArray()[0]);
		if (editor != null)
			launchEditor(editor, parms, assetSelection.getAssets());
	}

	private void exportMetaData() {
		for (Asset asset : assetSelection.getAssets())
			if (asset.getFileState() != IVolumeManager.PEER && volumeManager.findExistingFile(asset, true) != null) {
				ExportMetadataOperation op = new ExportMetadataOperation(Collections.singletonList(asset),
						UiActivator.getDefault().getExportFilter(),
						UiActivator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.JPEGMETADATA),
						true, false);
				try {
					op.execute(new NullProgressMonitor(), adaptable);
				} catch (ExecutionException e) {
					// should not happen
				}
			}
	}

	private List<Asset> computeAllEditors() {
		lastEditor = null;
		editors = null;
		defaultEditors = null;
		visited.clear();
		List<Asset> errands = new ArrayList<Asset>(assetSelection.size());
		for (Asset asset : assetSelection.getAssets()) {
			URI uri = asset.getFileState() == IVolumeManager.PEER ? null : volumeManager.findExistingFile(asset, true);
			if (uri != null) {
				String ext = BatchUtilities.getTrueFileExtension(uri.toString());
				if (visited.add(ext)) {
					FileEditorMapping mapping = UiActivator.getDefault().getFileEditorMapping(ext);
					computeEditors(mapping);
					computeDefaultEditors(mapping);
					String le = asset.getLastEditor();
					if (mapping != null && mapping.isRememberLast() && le != null && !le.isEmpty()
							&& (le.startsWith(">") || new File(le).exists())) { //$NON-NLS-1$
						if (lastEditor == null)
							lastEditor = le;
						else if (!lastEditor.equals(le))
							lastEditor = MIXED;
					}
				}
			} else
				errands.add(asset);
		}
		return errands;
	}

	private void computeDefaultEditors(FileEditorMapping mapping) {
		if (defaultEditors == null)
			defaultEditors = mapping == null ? new HashSet<EditorDescriptor>(1)
					: getValidEditors(mapping.getDeclaredDefaultEditors());
		else if (mapping != null)
			defaultEditors.retainAll(Arrays.asList(mapping.getDeclaredDefaultEditors()));
	}

	private static Set<EditorDescriptor> getValidEditors(EditorDescriptor[] declaredEditors) {
		Set<EditorDescriptor> validEditors = new HashSet<EditorDescriptor>();
		for (EditorDescriptor editorDescriptor : declaredEditors)
			if (editorDescriptor.getProgram() != null)
				validEditors.add(editorDescriptor);
			else {
				String fileName = editorDescriptor.getFileName();
				File file = fileName == null ? null : new File(fileName);
				if (file != null && file.exists())
					validEditors.add(editorDescriptor);
			}
		return validEditors;
	}

	private void computeEditors(FileEditorMapping mapping) {
		if (editors == null)
			editors = mapping == null ? new HashSet<EditorDescriptor>(1) : getValidEditors(mapping.getEditors());
		else if (mapping != null)
			editors.retainAll(Arrays.asList(mapping.getEditors()));
	}

	protected void launchEditor(EditorDescriptor editor, List<String> parms, List<Asset> list) {
		String editorLocation = editor.getFileName();
		String editorName = editor.getLabel();
		Program program = editor.getProgram();
		if (program != null)
			editorLocation = '>' + editorName;
		List<Asset> assetList = new ArrayList<Asset>();
		if (!Core.getCore().getDbManager().isReadOnly()) {
			for (Asset asset : list)
				if (!editorLocation.equals(asset.getLastEditor()))
					assetList.add(asset);
			if (!assetList.isEmpty())
				OperationJob.executeOperation(
						new SetLastEditorOperation(assetList.toArray(new AssetImpl[assetList.size()]), editorLocation),
						adaptable);
		}
		if (program != null) {
			boolean first = true;
			for (String parm : parms)
				if (!program.execute(parm) && first) {
					first = false;
					Core.getCore().logError(NLS.bind(Messages.EditWithAction_launch_failed, editorName), null);
				}
		} else {
			parms.add(0, editorLocation);
			try {
				Runtime.getRuntime().exec(parms.toArray(new String[parms.size()]));
			} catch (IOException e) {
				Core.getCore().logError(NLS.bind(Messages.EditWithAction_error_when_launching, editorName), e);
			}
		}
	}

	protected EditorDescriptor showDialog(final String ext) {
		if (editors.isEmpty()) {
			if (ext == null)
				new AssociationWarningDialog(shell, Messages.EditWithAction_file_editor,
						Messages.EditWithAction_editor_not_registered, null).open();
			else if (new AssociationWarningDialog(shell, Messages.EditWithAction_file_editor,
					NLS.bind(Messages.EditWithAction_no_external_editor, ext), ext).open() == 0)
				doRun();
			return null;
		}
		final ZListDialog dialog = new ZListDialog(shell, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER) {
			@Override
			protected void createClientContent(Composite comp) {
				CLink link = new CLink(comp, SWT.NONE);
				link.setText(Messages.EditWithAction_Configure_file_assos);
				link.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						PreferencesUtil.createPreferenceDialogOn(getShell(), FileAssociationsPreferencePage.ID,
								new String[0], ext).open();
						computeAllEditors();
						getTableViewer().setInput(editors);
					}
				});
			}
		};
		dialog.setTitle(Messages.EditWithAction_select_one_of);
		dialog.setContentProvider(ArrayContentProvider.getInstance());
		dialog.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EditorDescriptor) {
					EditorDescriptor editor = (EditorDescriptor) element;
					String label = editor.getLabel();
					if (defaultEditors.contains(editor))
						label += " " + Messages.EditWithAction_default; //$NON-NLS-1$
					String fileName = editor.getFileName();
					if (editor.getProgram() == null) {
						File file = (fileName == null) ? null : new File(fileName);
						if (file == null || !file.exists())
							label += Messages.EditWithAction_not_installed;
					}
					return label;
				}
				return null;
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof EditorDescriptor) {
					EditorDescriptor editor = (EditorDescriptor) element;
					Display display = shell.getDisplay();
					Image image = null;
					if (editor.getProgram() == null) {
						String fileName = editor.getFileName();
						File file = (fileName == null) ? null : new File(fileName);
						if (file != null && file.exists())
							image = editor.createImage(display);
					} else
						image = editor.createImage(display);
					if (image == null)
						image = Icons.error.getImage();
					else
						images.add(image);
					return image;
				}
				return null;
			}
		});
		Object[] eds = editors.toArray();
		dialog.setInput(eds);
		if (editors.size() == 1) {
			dialog.create();
			dialog.setSelection(eds[0]);
		}
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if (result != null && result.length > 0)
				return (EditorDescriptor) result[0];
		}
		return null;
	}
}
