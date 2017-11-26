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
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.preferences;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.core.internal.IMediaSupport;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.IRawConverter;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.Icons;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.views.IImageViewer;
import com.bdaum.zoom.ui.widgets.CGroup;

@SuppressWarnings("restriction")
public class FileAssociationsPreferencePage extends AbstractPreferencePage
		implements IWorkbenchPreferencePage, Listener {

	public static final String DFLTMAPPINGS = "$dflt$"; //$NON-NLS-1$

	public static final String ID = "com.bdaum.zoom.ui.preferences.FileAssociations"; //$NON-NLS-1$

	private static final String DATA_EDITOR = "editor"; //$NON-NLS-1$

	private static final String DATA_FROM_CONTENT_TYPE = "type"; //$NON-NLS-1$

	protected Table resourceTypeTable;

	protected Button addResourceTypeButton;

	protected Button removeResourceTypeButton;

	protected Table editorTable;

	protected Button addEditorButton;

	protected Button removeEditorButton;

	protected Button defaultEditorButton;

	protected Map<EditorDescriptor, Image> editorsToImages;

	private Button editResourceTypeButton;

	private CheckboxButton rememberLastButton;

	private CheckboxButton exportButton;

	private Map<String, FileEditor> fileEditorMap = new HashMap<String, FileEditor>(5);

	private static final String[] Executable_Filters = BatchConstants.WIN32 ? new String[] { "*.exe", "*.bat", "*.*" } //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			: new String[] { "*" }; //$NON-NLS-1$

	private IImageViewer[] imageViewers;

	private Set<EditorDescriptor> addedEditors = new HashSet<EditorDescriptor>(5);

	private CGroup editorGroup;

	public FileAssociationsPreferencePage() {
		setDescription(Messages.getString("FileAssociationsPreferencePage.how_to_interact")); //$NON-NLS-1$
	}

	@Override
	protected void createPageContents(Composite parent) {
		// define container & its gridding
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		createTabFolder(composite, "Asso"); //$NON-NLS-1$
		CTabItem tabItem0 = UiUtilities.createTabItem(tabFolder, Messages.getString("FileAssociationsPreferencePage.general")); //$NON-NLS-1$
		tabItem0.setControl(createGeneralGroup(tabFolder));
		CTabItem tabItem1 = UiUtilities.createTabItem(tabFolder, Messages.getString("FileAssociationsPreferencePage.file_assos")); //$NON-NLS-1$
		tabItem1.setControl(createAssociationGroup(tabFolder));
		initTabFolder(0);
		setHelp(HelpContextIds.FILE_ASSOCIATIONS_PREFERENCE_PAGE);
		fillValues();
	}

	private Composite createGeneralGroup(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(1, false));
		Composite innerComp = new Composite(comp, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 20;
		innerComp.setLayout(layout);
		innerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		exportButton = WidgetFactory.createCheckButton(innerComp,
				Messages.getString("GeneralPreferencePage.Export_metadata"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		CGroup eGroup = new CGroup(innerComp, SWT.NONE);
		eGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		eGroup.setLayout(new GridLayout());
		eGroup.setText(Messages.getString("FileAssociationsPreferencePage.external_viewers")); //$NON-NLS-1$
		createFileEditor(eGroup, Messages.getString("FileAssociationsPreferencePage.image_viewer"), ""); //$NON-NLS-1$ //$NON-NLS-2$
		imageViewers = UiActivator.getDefault().getImageViewers();
		for (IImageViewer imageViewer : imageViewers)
			createFileEditor(eGroup, imageViewer.getName(), imageViewer.getId());
		return comp;
	}

	private void createFileEditor(Composite parent, String label, String id) {
		FileEditor fileEditor = new FileEditor(parent, SWT.OPEN | SWT.READ_ONLY, label, true, Constants.EXEEXTENSION,
				Constants.EXEFILTERNAMES, null, null, false, true);
		fileEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fileEditorMap.put(id, fileEditor);
	}

	@Override
	protected void doFillValues() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		exportButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.AUTOEXPORT));
		fileEditorMap.get("").setText(preferenceStore //$NON-NLS-1$
				.getString(PreferenceConstants.EXTERNALVIEWER));
		for (IImageViewer viewer : imageViewers)
			fileEditorMap.get(viewer.getId())
					.setText(preferenceStore.getString(PreferenceConstants.EXTERNALMEDIAVIEWER + viewer.getId()));
		fillResourceTypeTable();
		if (resourceTypeTable.getItemCount() > 0)
			resourceTypeTable.setSelection(0);
		fillEditorTable();
		updateEnabledState();
		addedEditors.clear();
	}

	private Composite createAssociationGroup(Composite parent) {
		editorsToImages = new HashMap<EditorDescriptor, Image>(50);
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(1, false));
		// define container & its gridding
		Composite innerComp = new Composite(comp, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		innerComp.setLayout(layout);
		innerComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// layout the top table & its buttons
		CGroup resGroup = UiUtilities.createGroup(innerComp, 2, Messages.getString("FileAssociationsPreferencePage.File_types")); //$NON-NLS-1$

		resourceTypeTable = new Table(resGroup,
				SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		resourceTypeTable.addListener(SWT.Selection, this);
		resourceTypeTable.addListener(SWT.DefaultSelection, this);
		
		int fontHeight = (innerComp.getFont().getFontData())[0].getHeight();
		int displayHeight = innerComp.getDisplay().getPrimaryMonitor().getClientArea().height;

		int availableRows = displayHeight / fontHeight;
		GridData data = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
		data.heightHint = resourceTypeTable.getItemHeight() * (availableRows / 11);
		data.widthHint = 500;
		resourceTypeTable.setLayoutData(data);

		Composite groupComponent = new Composite(resGroup, SWT.NULL);
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginWidth = 0;
		groupLayout.marginHeight = 0;
		groupComponent.setLayout(groupLayout);
		data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		groupComponent.setLayoutData(data);

		addResourceTypeButton = new Button(groupComponent, SWT.PUSH);
		addResourceTypeButton.setText(Messages.getString("FileAssociationsPreferencePage.Add")); //$NON-NLS-1$
		addResourceTypeButton.addListener(SWT.Selection, this);
		addResourceTypeButton.setLayoutData(data);
		setButtonLayoutData(addResourceTypeButton);

		editResourceTypeButton = new Button(groupComponent, SWT.PUSH);
		editResourceTypeButton.setText(Messages.getString("FileAssociationsPreferencePage.edit")); //$NON-NLS-1$
		editResourceTypeButton.addListener(SWT.Selection, this);
		editResourceTypeButton.setLayoutData(data);
		setButtonLayoutData(editResourceTypeButton);

		removeResourceTypeButton = new Button(groupComponent, SWT.PUSH);
		removeResourceTypeButton.setText(Messages.getString("FileAssociationsPreferencePage.Remove")); //$NON-NLS-1$
		removeResourceTypeButton.addListener(SWT.Selection, this);
		setButtonLayoutData(removeResourceTypeButton);

		// Spacer
//		new Label(innerComp, SWT.LEFT);
//		data = new GridData();
//		data.horizontalAlignment = GridData.FILL;
//		data.horizontalSpan = 2;
//		label.setLayoutData(data);
		
		editorGroup = UiUtilities.createGroup(innerComp, 2, Messages.getString("FileAssociationsPreferencePage.editors")); //$NON-NLS-1$

//		// layout the bottom table & its buttons
//		editorLabel = new Label(innerComp, SWT.LEFT);
//		data = new GridData();
//		data.horizontalAlignment = GridData.FILL;
//		data.horizontalSpan = 2;
//		editorLabel.setLayoutData(data);

		editorTable = new Table(editorGroup, SWT.SINGLE | SWT.BORDER);
		editorTable.addListener(SWT.Selection, this);
		editorTable.addListener(SWT.DefaultSelection, this);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = editorTable.getItemHeight() * 6;
		editorTable.setLayoutData(data);

		groupComponent = new Composite(editorGroup, SWT.NULL);
		groupLayout = new GridLayout();
		groupLayout.marginWidth = 0;
		groupLayout.marginHeight = 0;
		groupComponent.setLayout(groupLayout);
		data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		groupComponent.setLayoutData(data);

		addEditorButton = new Button(groupComponent, SWT.PUSH);
		addEditorButton.setText(Messages.getString("FileAssociationsPreferencePage.Add")); //$NON-NLS-1$
		addEditorButton.addListener(SWT.Selection, this);
		addEditorButton.setLayoutData(data);
		setButtonLayoutData(addEditorButton);

		removeEditorButton = new Button(groupComponent, SWT.PUSH);
		removeEditorButton.setText(Messages.getString("FileAssociationsPreferencePage.Remove")); //$NON-NLS-1$
		removeEditorButton.addListener(SWT.Selection, this);
		setButtonLayoutData(removeEditorButton);

		defaultEditorButton = new Button(groupComponent, SWT.PUSH);
		defaultEditorButton.setText(Messages.getString("FileAssociationsPreferencePage.Default")); //$NON-NLS-1$
		defaultEditorButton.addListener(SWT.Selection, this);
		setButtonLayoutData(defaultEditorButton);
		Label sepLabel = new Label(groupComponent, SWT.SEPARATOR | SWT.HORIZONTAL);
		sepLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Button systemEditorButton = new Button(groupComponent, SWT.PUSH);
		systemEditorButton.setText(Messages.getString("FileAssociationsPreferencePage.apply_system_settings")); //$NON-NLS-1$
		systemEditorButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		systemEditorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileEditorMapping selectedResourceType = getSelectedResourceType();
				String[] extensions = selectedResourceType.getExtensions();
				Set<Program> programs = new HashSet<Program>();
				for (String ext : extensions) {
					Program program = Program.findProgram(ext);
					if (program != null)
						programs.add(program);
				}
				for (Program program : programs) {
					EditorDescriptor editor = new EditorDescriptor();
					editor.setProgram(program);
					addEditorToResourceType(editor);
				}
			}
		});
		
		CGroup optionsGroup = UiUtilities.createGroup(innerComp, 1, Messages.getString("FileAssociationsPreferencePage.options")); //$NON-NLS-1$

		rememberLastButton = WidgetFactory.createCheckButton(optionsGroup,
				Messages.getString("FileAssociationsPreferencePage.remember_last"), null); //$NON-NLS-1$
		rememberLastButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileEditorMapping selectedResourceType = getSelectedResourceType();
				selectedResourceType.setRememberLast(rememberLastButton.getSelection());
			}
		});
		return comp;
	}

	/**
	 * Place the existing resource types in the table
	 */
	protected void fillResourceTypeTable() {
		// Populate the table with the items
		for (TableItem item : resourceTypeTable.getItems())
			item.dispose();
		FileEditorMapping[] array = loadFileEditorMappings();
		Arrays.sort(array, new Comparator<FileEditorMapping>() {
			public int compare(FileEditorMapping m1, FileEditorMapping m2) {
				return m1.getLabel().compareTo(m2.getLabel());
			}
		});
		for (int i = 0; i < array.length; i++) {
			FileEditorMapping mapping = array[i];
			mapping = (FileEditorMapping) mapping.clone(); // want a copy
			newResourceTableItem(mapping, i, false);
		}
	}

	public static FileEditorMapping[] loadFileEditorMappings() {
		String xml = Platform.getPreferencesService().getString(UiActivator.PLUGIN_ID,
				PreferenceConstants.FILEASSOCIATION, "", //$NON-NLS-1$
				null);
		if (DFLTMAPPINGS.equals(xml)) // $NON-NLS-1$
			return initFileEditorMappings();
		Reader reader = new StringReader(xml);
		try {
			IMemento memento = XMLMemento.createReadRoot(reader);
			List<FileEditorMapping> mappings = new ArrayList<FileEditorMapping>();
			for (IMemento child : memento.getChildren("fileEditorMapping")) { //$NON-NLS-1$
				FileEditorMapping mapping = new FileEditorMapping(null);
				mapping.loadValues(child);
				mappings.add(mapping);
			}
			return mappings.toArray(new FileEditorMapping[mappings.size()]);
		} catch (WorkbenchException e) {
			// should never happen
		}
		return new FileEditorMapping[0];
	}

	private static FileEditorMapping[] initFileEditorMappings() {
		String[] supportedImageFileExtensions = ImageConstants.getSupportedImageFileExtensionsGroups(true);
		List<FileEditorMapping> list = new ArrayList<FileEditorMapping>();
		for (int i = 0; i < supportedImageFileExtensions.length; i++) {
			String extension = supportedImageFileExtensions[i];
			List<String> extensions = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(extension, "; "); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				String ext = st.nextToken();
				int p = ext.lastIndexOf('.');
				if (p >= 0)
					ext = ext.substring(p + 1);
				extensions.add(ext);
			}
			if (!extensions.isEmpty())
				list.add(new FileEditorMapping(extensions.toArray(new String[extensions.size()])));
		}
		for (IMediaSupport mediaSupport : CoreActivator.getDefault().getMediaSupport())
			list.add(new FileEditorMapping(mediaSupport.getFileExtensions()));
		return list.toArray(new FileEditorMapping[list.size()]);
	}

	/**
	 * This is a hook for subclasses to do special things when the ok button is
	 * pressed. For example reimplement this method if you want to save the
	 * page's data into the preference bundle.
	 */

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.AUTOEXPORT, exportButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.EXTERNALVIEWER, fileEditorMap.get("").getText()); //$NON-NLS-1$
		for (IImageViewer viewer : imageViewers)
			preferenceStore.setValue(PreferenceConstants.EXTERNALMEDIAVIEWER + viewer.getId(),
					fileEditorMap.get(viewer.getId()).getText());
		TableItem[] items = resourceTypeTable.getItems();
		FileEditorMapping[] mappings = new FileEditorMapping[items.length];
		for (int i = 0; i < items.length; i++)
			mappings[i] = (FileEditorMapping) (items[i].getData());
		saveMappings(mappings);
		File currentModule = null;
		IRawConverter rc = BatchActivator.getDefault().getCurrentRawConverter(false);
		if (rc != null) {
			String pathId = rc.getPathId();
			if (pathId != null) {
				String path = preferenceStore.getString(pathId);
				if (path != null && path.length() != 0)
					currentModule = new File(path);
			}
		}
		if (currentModule != null && !currentModule.exists())
			currentModule = null;
		for (EditorDescriptor editor : addedEditors) {
			File editorFile = new File(editor.getPath());
			if (rc != null) {
				File moduleFile = rc.findModule(editorFile.getParentFile());
				if (moduleFile != null && !moduleFile.equals(currentModule)) {
					String msg = NLS.bind(Messages.getString("FileAssociationsPreferencePage.use_dcraw_of_editor"), //$NON-NLS-1$
							editorFile.getName(), Constants.APPLICATION_NAME);
					if (currentModule != null)
						msg += NLS.bind(Messages.getString("FileAssociationsPreferencePage.current_module"), //$NON-NLS-1$
								currentModule);
					else if (!Constants.LINUX)
						msg += NLS.bind(Messages.getString("FileAssociationsPreferencePage.embedded_module"), //$NON-NLS-1$
								Constants.APPNAME);
					else
						msg += Messages.getString("FileAssociationsPreferencePage.os_module"); //$NON-NLS-1$
					if (AcousticMessageDialog.openQuestion(getShell(),
							Messages.getString("FileAssociationsPreferencePage.dcraw_asso"), //$NON-NLS-1$
							msg)) {
						preferenceStore.setValue(rc.getPathId(), moduleFile.getAbsolutePath());
						break;
					}
				}
			}
		}
	}

	@Override
	protected void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(PreferenceConstants.AUTOEXPORT,
				preferenceStore.getDefaultBoolean(PreferenceConstants.AUTOEXPORT));
		preferenceStore.setValue(PreferenceConstants.EXTERNALVIEWER,
				preferenceStore.getDefaultString(PreferenceConstants.EXTERNALVIEWER));
		preferenceStore.setValue(PreferenceConstants.FILEASSOCIATION,
				preferenceStore.getDefaultString(PreferenceConstants.FILEASSOCIATION));
	}

	public static void saveMappings(FileEditorMapping[] mappings) {
		XMLMemento root = XMLMemento.createWriteRoot("fileEditorMappings"); //$NON-NLS-1$
		for (FileEditorMapping mapping : mappings)
			mapping.saveValues(root.createChild("fileEditorMapping")); //$NON-NLS-1$
		Writer writer = new StringWriter();
		try {
			root.save(writer);
			BatchUtilities.putPreferences(InstanceScope.INSTANCE.getNode(UiActivator.PLUGIN_ID),
					PreferenceConstants.FILEASSOCIATION, writer.toString());
		} catch (IOException e) {
			// should never happen
		}
	}

	protected void fillEditorTable() {
		editorTable.removeAll();
		boolean rememberLast = false;
		TableItem[] items = resourceTypeTable.getSelection();
		if (items.length > 0) {
			FileEditorMapping resourceType = (FileEditorMapping) items[0].getData();
			String fileTypes = items[0].getText();
			if (fileTypes.length() > 20)
				fileTypes = fileTypes.substring(0, 20) + "…"; //$NON-NLS-1$
			editorGroup.setText(
					NLS.bind(Messages.getString("FileAssociationsPreferencePage.Associates_editors"), fileTypes)); //$NON-NLS-1$
			if (resourceType != null) {
				rememberLast = resourceType.isRememberLast();
				EditorDescriptor[] array = resourceType.getEditors();
				for (int i = 0; i < array.length; i++) {
					EditorDescriptor editor = array[i];
					TableItem item = new TableItem(editorTable, SWT.NULL);
					item.setData(DATA_EDITOR, editor);
					// Check if it is the default editor
					String defaultString = null;
					if (resourceType.getDefaultEditor() == editor && resourceType.isDeclaredDefaultEditor(editor))
						defaultString = Messages.getString("FileAssociationsPreferencePage.default"); //$NON-NLS-1$
					String text = defaultString != null ? editor.getLabel() + " " //$NON-NLS-1$
							+ defaultString : editor.getLabel();
					if (editor.getProgram() == null) {
						String fileName = editor.getFileName();
						File file = fileName == null ? null : new File(fileName);
						if (file == null || !file.exists())
							text += ' ' + Messages.getString("FileAssociationsPreferencePage.not_installed"); //$NON-NLS-1$
					}
					item.setImage(getImage(editor));
					item.setText(text);
				}
			}
		} else
			editorGroup.setText(Messages.getString("FileAssociationsPreferencePage.editors")); //$NON-NLS-1$
		rememberLastButton.setSelection(rememberLast);
	}

	protected FileEditorMapping getSelectedResourceType() {
		TableItem[] items = resourceTypeTable.getSelection();
		return (items.length > 0) ? (FileEditorMapping) items[0].getData() : null;
	}

	/**
	 * Prompt for editor.
	 */
	public void promptForEditor() {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.PRIMARY_MODAL);
		for (int i = 0; i < Executable_Filters.length - 1; i++)
			Executable_Filters[i] += ";" //$NON-NLS-1$
					+ Executable_Filters[i].toUpperCase();
		dialog.setFilterExtensions(Executable_Filters);
		String result = dialog.open();
		if (result != null) {
			EditorDescriptor editor = EditorDescriptor.createForProgram(result);
			addedEditors.add(editor);
			addEditorToResourceType(editor);
		}
	}

	private void addEditorToResourceType(EditorDescriptor editor) {
		FileEditorMapping selectedResourceType = getSelectedResourceType();
		if (selectedResourceType.addEditor(editor)) {
			int i = editorTable.getItemCount();
			boolean isEmpty = i < 1;
			TableItem item = new TableItem(editorTable, SWT.NULL, i);
			item.setData(DATA_EDITOR, editor);
			if (isEmpty) {
				selectedResourceType.setDefaultEditor(editor);
				item.setText(editor.getLabel() + " " + Messages.getString("FileAssociationsPreferencePage.default")); //$NON-NLS-1$ //$NON-NLS-2$
			} else
				item.setText(editor.getLabel());
			item.setImage(getImage(editor));
			editorTable.setSelection(i);
			editorTable.setFocus();
		}
	}


	/**
	 * Prompt for resource type.
	 *
	 * @param tableItem
	 */
	public void promptForResourceType(int index) {
		FileExtensionDialog dialog = new FileExtensionDialog(getControl().getShell());
		if (index >= 0)
			dialog.setInitialValue(resourceTypeTable.getItem(index).getText());
		if (dialog.open() == Window.OK)
			addResourceType(dialog.getExtensions(), index);
	}

	/**
	 * Add a new resource type to the collection shown in the top of the page.
	 * This is typically called after the extension dialog is shown to the user.
	 *
	 * @param newName
	 *            the new name
	 * @param newExtension
	 *            the new extension
	 */
	public void addResourceType(String[] newExtensions, int replace) {
		// Either a file name or extension must be provided
		Assert.isTrue((newExtensions != null && newExtensions.length != 0));
		FileEditorMapping resourceType;
		TableItem replacedItem = null;
		FileEditorMapping replacedType = null;
		if (replace >= 0) {
			replacedItem = resourceTypeTable.getItem(replace);
			replacedType = (FileEditorMapping) replacedItem.getData();
		}
		TableItem[] items = resourceTypeTable.getItems();
		for (String newExt : newExtensions) {
			for (TableItem tableItem : items) {
				if (tableItem != replacedItem) {
					resourceType = (FileEditorMapping) tableItem.getData();
					String[] extensions = resourceType.getExtensions();
					for (String ext : extensions) {
						if (newExt.equalsIgnoreCase(ext)) {
							AcousticMessageDialog.openInformation(getControl().getShell(),
									NLS.bind(Messages.getString("FileAssociationsPreferencePage.File_type_exists"), //$NON-NLS-1$
											ext),
									Messages.getString("FileAssociationsPreferencePage.An_entry_already_exists")); //$NON-NLS-1$
							return;
						}
					}
				}
			}
		}
		String newExt0 = newExtensions[0];
		int i = 0;
		if (replace < 0) {
			boolean found = false;
			while (i < items.length && !found) {
				resourceType = (FileEditorMapping) items[i].getData();
				int result = newExt0.compareToIgnoreCase(resourceType.getExtensions()[0]);
				if (result < 0)
					found = true;
				else
					i++;
			}
		} else {
			i = replace;
			replacedItem.dispose();
		}
		// Create the new type and insert it
		resourceType = new FileEditorMapping(newExtensions);
		if (replacedType != null) {
			resourceType.setDefaultEditor(replacedType.getDefaultEditor());
			resourceType.setEditorsList(Arrays.asList(replacedType.getEditors()));
		}
		TableItem item = newResourceTableItem(resourceType, i, true);
		resourceTypeTable.setFocus();
		resourceTypeTable.showItem(item);
		fillEditorTable();
	}

	public void handleEvent(Event event) {
		if (event.widget == addResourceTypeButton) {
			promptForResourceType(-1);
		} else if (event.widget == editResourceTypeButton) {
			promptForResourceType(resourceTypeTable.getSelectionIndex());
		} else if (event.widget == removeResourceTypeButton) {
			removeSelectedResourceType();
		} else if (event.widget == addEditorButton) {
			promptForEditor();
		} else if (event.widget == removeEditorButton) {
			removeSelectedEditor();
		} else if (event.widget == defaultEditorButton) {
			setSelectedEditorAsDefault();
		} else if (event.widget == resourceTypeTable) {
			fillEditorTable();
		}

		updateEnabledState();

	}

	/**
	 * Remove the editor from the table
	 */
	public void removeSelectedEditor() {
		TableItem[] items = editorTable.getSelection();
		boolean defaultEditor = editorTable.getSelectionIndex() == 0;
		if (items.length > 0) {
			EditorDescriptor editorDescriptor = (EditorDescriptor) items[0].getData(DATA_EDITOR);
			addedEditors.remove(editorDescriptor);
			getSelectedResourceType().removeEditor(editorDescriptor);
			items[0].dispose(); // Table is single selection
		}
		if (defaultEditor && editorTable.getItemCount() > 0) {
			TableItem item = editorTable.getItem(0);
			// explicitly set the new editor first editor to default
			if (item != null) {
				getSelectedResourceType().setDefaultEditor((EditorDescriptor) item.getData(DATA_EDITOR));
				item.setText(((EditorDescriptor) (item.getData(DATA_EDITOR))).getLabel() + " " //$NON-NLS-1$
						+ Messages.getString("FileAssociationsPreferencePage.default")); //$NON-NLS-1$
			}
		}

	}

	/**
	 * Remove the type from the table
	 */
	public void removeSelectedResourceType() {
		TableItem[] items = resourceTypeTable.getSelection();
		if (items.length > 0)
			items[0].dispose(); // Table is single selection
		// Clear out the editors too
		editorTable.removeAll();
	}

	/**
	 * Add the selected editor to the default list.
	 */
	public void setSelectedEditorAsDefault() {
		TableItem[] items = editorTable.getSelection();
		if (items.length > 0) {
			// First change the label of the old default
			TableItem oldDefaultItem = editorTable.getItem(0);
			oldDefaultItem.setText(((EditorDescriptor) oldDefaultItem.getData(DATA_EDITOR)).getLabel());
			// Now set the new default
			EditorDescriptor editor = (EditorDescriptor) items[0].getData(DATA_EDITOR);
			getSelectedResourceType().setDefaultEditor(editor);
			IContentType fromContentType = (IContentType) items[0].getData(DATA_FROM_CONTENT_TYPE);
			items[0].dispose(); // Table is single selection
			TableItem item = new TableItem(editorTable, SWT.NULL, 0);
			item.setData(DATA_EDITOR, editor);
			if (fromContentType != null)
				item.setData(DATA_FROM_CONTENT_TYPE, fromContentType);
			item.setText(editor.getLabel() + " " + Messages.getString("FileAssociationsPreferencePage.default")); //$NON-NLS-1$ //$NON-NLS-2$
			item.setImage(getImage(editor));
			editorTable.setSelection(new TableItem[] { item });
		}
	}

	/**
	 * Update the enabled state.
	 */
	public void updateEnabledState() {
		// Update enabled state
		boolean resourceTypeSelected = resourceTypeTable.getSelectionIndex() != -1;
		boolean editorSelected = editorTable.getSelectionIndex() != -1;
		editResourceTypeButton.setEnabled(resourceTypeSelected);
		removeResourceTypeButton.setEnabled(resourceTypeSelected);
		editorGroup.setEnabled(resourceTypeSelected);
		addEditorButton.setEnabled(resourceTypeSelected);
		removeEditorButton.setEnabled(editorSelected);
		defaultEditorButton.setEnabled(editorSelected);
	}

	/*
	 * Create a new <code>TableItem</code> to represent the resource type editor
	 * description supplied.
	 */
	protected TableItem newResourceTableItem(FileEditorMapping mapping, int index, boolean selected) {
		Image image = mapping.createImage(getControl().getDisplay());
		TableItem item = new TableItem(resourceTypeTable, SWT.NULL, index);
		if (image != null)
			item.setImage(image);
		item.setText(mapping.getLabel());
		item.setData(mapping);
		if (selected)
			resourceTypeTable.setSelection(index);
		return item;
	}

	/**
	 * Returns the image associated with the given editor.
	 */
	protected Image getImage(EditorDescriptor editor) {
		Image image = editorsToImages.get(editor);
		if (image == null) {
			if (editor.getProgram() != null)
				image = editor.createImage(getControl().getDisplay());
			else {
				String fileName = editor.getFileName();
				File file = fileName == null ? null : new File(fileName);
				if (file != null && file.exists())
					image = editor.createImage(getControl().getDisplay());
			}
			if (image == null)
				image = Icons.error.getImage();
			editorsToImages.put(editor, image);
		}
		return image;
	}

	@Override
	public void applyData(Object data) {
		String ext = ("." + data).toLowerCase(); //$NON-NLS-1$
		int itemCount = resourceTypeTable.getItemCount();
		for (int i = 0; i < itemCount; i++) {
			TableItem item = resourceTypeTable.getItem(i);
			String itemText = item.getText();
			StringTokenizer st = new StringTokenizer(itemText, ";"); //$NON-NLS-1$
			if (st.hasMoreTokens()) {
				String token = st.nextToken().toLowerCase();
				if (token.endsWith(ext)) {
					tabFolder.setSelection(1);
					resourceTypeTable.select(i);
					fillEditorTable();
					break;
				}
			}
		}
	}

	@Override
	protected String doValidate() {
		for (FileEditor fileEditor : fileEditorMap.values()) {
			String fn = fileEditor.getText();
			if (!fn.isEmpty()) {
				File file = new File(fn);
				if (!file.exists())
					return NLS.bind(Messages.getString("GeneralPreferencePage.external_viewer_does_not_exist"), fn); //$NON-NLS-1$
			}
		}
		return null;
	}

}
