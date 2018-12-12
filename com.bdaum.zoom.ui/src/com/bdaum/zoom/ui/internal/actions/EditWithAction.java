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
 * (c) 2009 Berthold Daum  
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
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
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

	private static final String MIXED = "{mixed}"; //$NON-NLS-1$
	protected List<Image> images = new ArrayList<Image>();
	private final IAdaptable adaptable;
	private Shell shell;

	public EditWithAction(String label, String tooltip, ImageDescriptor image, IAdaptable adaptable) {
		super(label, image);
		setToolTipText(tooltip);
		this.adaptable = adaptable;
		shell = adaptable.getAdapter(Shell.class);
	}

	private void disposeImages() {
		for (Image image : images)
			image.dispose();
		images.clear();
	}

	@Override
	public void run() {
		BusyIndicator.showWhile(shell.getDisplay(), () -> doRun());
	}

	private void doRun() {
		boolean autoExport = Platform.getPreferencesService().getBoolean(UiActivator.PLUGIN_ID,
				PreferenceConstants.AUTOEXPORT, true, null);
		AssetSelection assetSelection = (adaptable.getAdapter(AssetSelection.class));
		if (!assetSelection.isEmpty()) {
			String lastEditor = null;
			Set<String> visited = new HashSet<String>();
			Set<EditorDescriptor> editors = null;
			Set<EditorDescriptor> defaultEditors = null;
			List<String> parms = new ArrayList<String>(assetSelection.size());
			IVolumeManager volumeManager = Core.getCore().getVolumeManager();
			List<Asset> errands = new ArrayList<Asset>(assetSelection.size());
			for (Asset asset : assetSelection) {
				URI uri = asset.getFileState() == IVolumeManager.PEER ? null
						: volumeManager.findExistingFile(asset, true);
				if (uri != null) {
					if (autoExport) {
						ExportMetadataOperation op = new ExportMetadataOperation(Collections.singletonList(asset),
								UiActivator.getDefault().getExportFilter(), UiActivator.getDefault()
										.getPreferenceStore().getBoolean(PreferenceConstants.JPEGMETADATA),
								true, false);
						try {
							op.execute(new NullProgressMonitor(), adaptable);
						} catch (ExecutionException e) {
							// should not happen
						}
					}
					String path = (new File(uri)).getAbsolutePath();
					parms.add(path);
					String ext = BatchUtilities.getTrueFileExtension(path);
					if (visited.add(ext)) {
						FileEditorMapping mapping = UiActivator.getDefault().getFileEditorMapping(ext);
						editors = computeEditors(editors, mapping);
						defaultEditors = computeDefaultEditors(defaultEditors, editors, mapping);
						String le = asset.getLastEditor();
						if (mapping != null && mapping.isRememberLast() && le != null && !le.isEmpty()) {
							if (le.startsWith(">") || new File(le).exists()) { //$NON-NLS-1$
								if (lastEditor == null)
									lastEditor = le;
								else if (!lastEditor.equals(le))
									lastEditor = MIXED;
							}
						}
					}
				} else
					errands.add(asset);
			}
			if (!errands.isEmpty())
				UiUtilities.showFilesAreOffline(shell, errands, errands.size() == assetSelection.size(),
						Messages.EditWithAction_files_offline_cannot_be_edited);
			if (errands.size() == assetSelection.size())
				return;
			if (isDefault() && lastEditor != null && lastEditor != MIXED) {
				EditorDescriptor editor = null;
				if (lastEditor.startsWith(">")) { //$NON-NLS-1$
					String pname = lastEditor.substring(1);
					Program[] programs = Program.getPrograms();
					for (Program program : programs)
						if (pname.equals(program.getName())) {
							editor = EditorDescriptor.createForProgram(program);
							break;
						}
				} else
					editor = EditorDescriptor.createForProgram(lastEditor);
				if (editor != null)
					launchEditor(editor, parms, assetSelection.getAssets());
			} else {
				EditorDescriptor editor = showDialog(editors, defaultEditors,
						visited.size() != 1 ? null : (String) visited.toArray()[0]);
				if (editor != null)
					launchEditor(editor, parms, assetSelection.getAssets());
			}
		}
		disposeImages();
	}

	protected Set<EditorDescriptor> computeDefaultEditors(Set<EditorDescriptor> defaultEditors,
			Set<EditorDescriptor> allowedEditors, FileEditorMapping mapping) {
		if (defaultEditors == null)
			defaultEditors = mapping == null ? new HashSet<EditorDescriptor>(1)
					: getValidEditors(mapping.getDeclaredDefaultEditors());
		else
			defaultEditors.retainAll(Arrays.asList(mapping.getDeclaredDefaultEditors()));
		return defaultEditors;
	}

	protected Set<EditorDescriptor> getValidEditors(EditorDescriptor[] declaredEditors) {
		Set<EditorDescriptor> defaultEditors;
		defaultEditors = new HashSet<EditorDescriptor>();
		for (EditorDescriptor editorDescriptor : declaredEditors) {
			if (editorDescriptor.getProgram() != null) {
				defaultEditors.add(editorDescriptor);
			} else {
				String fileName = editorDescriptor.getFileName();
				File file = fileName == null ? null : new File(fileName);
				if (file != null && file.exists()) {
					defaultEditors.add(editorDescriptor);
				}
			}
		}
		return defaultEditors;
	}

	protected Set<EditorDescriptor> computeEditors(Set<EditorDescriptor> editors, FileEditorMapping mapping) {
		if (editors == null)
			editors = mapping == null ? new HashSet<EditorDescriptor>(1) : getValidEditors(mapping.getEditors());
		else
			editors.retainAll(Arrays.asList(mapping.getEditors()));
		return editors;
	}

	private void launchEditor(EditorDescriptor editor, List<String> parms, List<Asset> list) {
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

	private EditorDescriptor showDialog(final Set<EditorDescriptor> editors, final Set<EditorDescriptor> defaultEditors,
			final String ext) {
		if (!editors.isEmpty()) {
			if (isDefault() && defaultEditors.size() == 1)
				return (EditorDescriptor) defaultEditors.toArray()[0];
			final ZListDialog dialog = new ZListDialog(shell, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER) {
				@Override
				protected void createClientContent(Composite comp) {
					CLink link = new CLink(comp, SWT.NONE);
					link.setText(Messages.EditWithAction_Configure_file_assos);
					link.addListener(new Listener() {
						@Override
						public void handleEvent(Event event) {
							PreferencesUtil.createPreferenceDialogOn(getShell(), FileAssociationsPreferencePage.ID,
									new String[0], ext).open();
							getTableViewer().setInput(
									computeEditors(null, UiActivator.getDefault().getFileEditorMapping(ext)).toArray());
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
								images.add(image = editor.createImage(display));
						} else
							images.add(image = editor.createImage(display));
						if (image == null)
							image = Icons.error.getImage();
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
		} else if (ext == null)
			new AssociationWarningDialog(shell, Messages.EditWithAction_file_editor,
					Messages.EditWithAction_editor_not_registered, null).open();
		else if (new AssociationWarningDialog(shell, Messages.EditWithAction_file_editor,
				NLS.bind(Messages.EditWithAction_no_external_editor, ext), ext).open() == 0)
			run();
		return null;
	}

	protected boolean isDefault() {
		return false;
	}

}
