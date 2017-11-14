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
 * (c) 2009-2015 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.preferences;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.bdaum.zoom.batch.internal.BatchActivator;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.IRecipeDetector;
import com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter;
import com.bdaum.zoom.core.IRecipeDetector.IRecipeParameter.IRecipeParameterValue;
import com.bdaum.zoom.core.internal.CoreActivator;
import com.bdaum.zoom.image.ImageConstants;
import com.bdaum.zoom.program.BatchUtilities;
import com.bdaum.zoom.program.IRawConverter;
import com.bdaum.zoom.program.IRawConverter.RawProperty;
import com.bdaum.zoom.program.IRawConverter.RawProperty.RawEnum;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.UiUtilities;
import com.bdaum.zoom.ui.internal.dialogs.AllNoneGroup;
import com.bdaum.zoom.ui.internal.job.UpdateRawImagesJob;
import com.bdaum.zoom.ui.internal.widgets.CheckboxButton;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.internal.widgets.WidgetFactory;
import com.bdaum.zoom.ui.preferences.AbstractPreferencePage;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.bdaum.zoom.ui.widgets.CGroup;
import com.bdaum.zoom.ui.widgets.CLink;
import com.bdaum.zoom.ui.widgets.NumericControl;

@SuppressWarnings("restriction")
public class ImportPreferencePage extends AbstractPreferencePage {

	public static final String ID = "com.bdaum.zoom.ui.preferences.ImportPreferencePage"; //$NON-NLS-1$
	public static final String DNG = "dng"; //$NON-NLS-1$
	public static final String RAW = "raw"; //$NON-NLS-1$

	private CGroup basicsGroup, optionsGroup, recipeGroup;

	private StackLayout basicsLayout, optionsLayout;

	private CheckboxButton makerNotesButton, deviceButton, processRecipesButton, synchronizeRecipesButton,
			archiveRecipesButton, showButton, uncompressedButton, linearButton;

	private CheckboxTableViewer recipeViewer;

	private ComboViewer rcViewer, modeviewer;
	
	private FileEditor dngpathEditor;
	
	private Text dngfolderField;

	private Label thumbnailWarning;

	private NumericControl maxSpinner;

	private CTabItem tabItem0, tabItem1, tabItem2;

	protected List<String> selectedRecipeDetectors = new ArrayList<String>();

	protected List<IRecipeDetector> allDetectors;

	private Set<String> previousRecipeDetectors = new HashSet<String>();

	private Map<String, FileEditor> basicsFileEditors = new HashMap<String, FileEditor>(3);

	private Map<String, Composite> optionComps = new HashMap<String, Composite>(3);

	private Map<String, Object> optionProps = new HashMap<String, Object>(15);

	private boolean previousProcessRecipes;

	private String previousUsesRecipes = ""; //$NON-NLS-1$

	private IRawConverter previousRawConverter;

	public ImportPreferencePage() {
		setDescription(Messages.getString("ImportPreferencePage.control_how_images_are_imported")); //$NON-NLS-1$
	}

	@Override
	public void applyData(Object data) {
		if (RAW.equals(data))
			tabFolder.setSelection(tabItem1);
		else if (tabItem2 != null && DNG.equals(data))
			tabFolder.setSelection(tabItem2);
		else
			tabFolder.setSelection(tabItem0);
	}

	@Override
	protected void createPageContents(Composite composite) {
		setHelp(HelpContextIds.IMPORT_PREFERENCE_PAGE);
		createTabFolder(composite, "Import"); //$NON-NLS-1$
		tabItem0 = UiUtilities.createTabItem(tabFolder, Messages.getString("ImportPreferencePage.general")); //$NON-NLS-1$
		tabItem0.setControl(createGeneralGroup(tabFolder));
		tabItem1 = UiUtilities.createTabItem(tabFolder, Messages.getString("ImportPreferencePage.raw_conversion")); //$NON-NLS-1$
		tabItem1.setControl(createRawGroup(tabFolder));
		if (Constants.WIN32 || Constants.OSX) {
			tabItem2 = UiUtilities.createTabItem(tabFolder, Messages.getString("ImportPreferencePage.dng_conversion")); //$NON-NLS-1$
			tabItem2.setControl(createDngGroup(tabFolder));
		}
		createExtensions(tabFolder, "com.bdaum.zoom.ui.preferences.ImportPreferencePage"); //$NON-NLS-1$
		initTabFolder(0);
		fillValues();
		updateRawOptions();
	}

	private Composite createGeneralGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1);
		layoutData.verticalIndent = 15;
		makerNotesButton = WidgetFactory.createCheckButton(composite,
				Messages.getString("ImportPreferencePage.import_maker_notes"), layoutData);//$NON-NLS-1$
		showButton = WidgetFactory.createCheckButton(composite,
				Messages.getString("ImportPreferencePage.show_imported"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));

		Label maxLabel = new Label(composite, SWT.NONE);
		maxLabel.setText(Messages.getString("ImportPreferencePage.max_length_imports")); //$NON-NLS-1$
		maxSpinner = new NumericControl(composite, SWT.NONE);
		maxSpinner.setMaximum(999);
		maxSpinner.setMinimum(1);
		maxSpinner.setIncrement(1);
		deviceButton = WidgetFactory.createCheckButton(composite,
				Messages.getString("ImportPreferencePage.automatically_invoke_import"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		return composite;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doFillValues() {
		BatchActivator batch = BatchActivator.getDefault();
		IRawConverter rc = batch.getCurrentRawConverter(false);
		previousRawConverter = rc;
		IPreferenceStore preferenceStore = getPreferenceStore();
		Map<String, IRawConverter> rawConverters = batch.getRawConverters();
		if (rc != null)
			rcViewer.setSelection(new StructuredSelection(rc));
		for (IRawConverter c : rawConverters.values()) {
			FileEditor fileEditor = basicsFileEditors.get(c.getId());
			if (fileEditor != null) {
				String path = preferenceStore.getString(c.getPathId());
				if (path.isEmpty()) {
					String editorName = c.getName();
					if (editorName != null) {
						FileEditorMapping editorMapping = null;
						for (String rf : ImageConstants.getRawFormatMap().keySet()) {
							editorMapping = UiActivator.getDefault().getFileEditorMapping(rf);
							if (editorMapping != null)
								break;
						}
						if (editorMapping != null)
							for (EditorDescriptor editorDescriptor : editorMapping.getEditors())
								if (editorName.equalsIgnoreCase(editorDescriptor.getLabel())) {
									path = editorDescriptor.getFileName();
									break;
								}
					}
				}
				fileEditor.setText(path);
			}
			for (IRawConverter.RawProperty prop : c.getProperties()) {
				prop.value = preferenceStore.getString(prop.id);
				Object object = optionProps.get(prop.id);
				if (object instanceof ComboViewer) {
					ComboViewer viewer = (ComboViewer) object;
					for (RawEnum rawEnum : (List<RawEnum>) viewer.getInput())
						if (rawEnum.id.equals(prop.value)) {
							viewer.setSelection(new StructuredSelection(rawEnum));
							if (rawEnum.recipe) {
								c.setUsesRecipes(rawEnum.id);
								if (c == rc)
									previousUsesRecipes = rawEnum.id;
							}
							break;
						}
				} else if (object instanceof Spinner)
					try {
						((Spinner) object).setSelection(Integer.parseInt(prop.value));
					} catch (NumberFormatException e) {
						// do nothing
					}
				else if (object instanceof Button)
					((Button) object).setSelection(Boolean.parseBoolean(prop.value));
				else if (object instanceof Text)
					((Text) object).setText(prop.value);
			}
		}
		if (modeviewer != null)
			modeviewer.setSelection(new StructuredSelection(preferenceStore.getString(PreferenceConstants.RAWIMPORT)));
		if (dngpathEditor != null)
			dngpathEditor.setText(preferenceStore.getString(PreferenceConstants.DNGCONVERTERPATH));
		if (uncompressedButton != null)
			uncompressedButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.DNGUNCOMPRESSED));
		if (linearButton != null)
			linearButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.DNGLINEAR));
		if (dngfolderField != null)
			dngfolderField.setText(preferenceStore.getString(PreferenceConstants.DNGFOLDER));
		maxSpinner.setSelection(preferenceStore.getInt(PreferenceConstants.MAXIMPORTS));
		deviceButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.DEVICEWATCH));
		showButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.SHOWIMPORTED));
		makerNotesButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.IMPORTMAKERNOTES));
		if (recipeViewer != null) {
			List<String> configurations = Core
					.fromStringList(preferenceStore.getString(PreferenceConstants.RECIPEDETECTORCONFIGURATIONS), "\n"); //$NON-NLS-1$
			if (configurations != null && allDetectors != null)
				for (String conf : configurations) {
					int p = conf.indexOf(':');
					if (p > 0) {
						String id = conf.substring(0, p);
						for (IRecipeDetector detector : allDetectors) {
							if (detector.getId().equals(id)) {
								StringTokenizer st = new StringTokenizer(conf.substring(p + 1), ";"); //$NON-NLS-1$
								while (st.hasMoreTokens()) {
									String parm = st.nextToken();
									int q = parm.indexOf('=');
									if (q > 0) {
										IRecipeParameter parameter = detector.getParameter(parm.substring(0, q));
										if (parameter != null)
											parameter.setValue(parm.substring(q + 1));
									}
								}
								break;
							}
						}
					}
				}
			selectedRecipeDetectors = Core
					.fromStringList(preferenceStore.getString(PreferenceConstants.RECIPEDETECTORS), "\n"); //$NON-NLS-1$
			previousRecipeDetectors = new HashSet<String>(selectedRecipeDetectors);
			recipeViewer.setInput(allDetectors);
			previousProcessRecipes = preferenceStore.getBoolean(PreferenceConstants.PROCESSRECIPES);
			processRecipesButton.setSelection(previousProcessRecipes);
			archiveRecipesButton.setSelection(preferenceStore.getBoolean(PreferenceConstants.ARCHIVERECIPES));
		}
	}

	@Override
	protected void doUpdateButtons() {
		updateRecipeButtons();
	}

	private Composite createDngGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout());
		CGroup basicsGroup = UiUtilities.createGroup(composite, 2, Messages.getString("ImportPreferencePage.basics")); //$NON-NLS-1$
		final String[] options = new String[] { Constants.RAWIMPORT_BOTH, Constants.RAWIMPORT_ONLYRAW,
				Constants.RAWIMPORT_ONLYDNG, Constants.RAWIMPORT_DNGEMBEDDEDRAW };
		final String[] labels = new String[] { Messages.getString("ImportPreferencePage.Convert_to_DNG_import_both"), //$NON-NLS-1$
				Messages.getString("ImportPreferencePage.raw_files_only"), //$NON-NLS-1$
				Messages.getString("ImportPreferencePage.convert_and_import_DNG"), //$NON-NLS-1$
				Messages.getString("ImportPreferencePage.convert_with_embedded_raw") }; //$NON-NLS-1$
		modeviewer = createComboViewer(basicsGroup, Messages.getString("ImportPreferencePage.import_raw_files"), //$NON-NLS-1$
				options, labels, false);
		modeviewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validate();
			}
		});
		dngpathEditor = new FileEditor(basicsGroup, SWT.OPEN | SWT.READ_ONLY,
				Messages.getString("ImportPreferencePage.location_Adobe_DNG_converter"), //$NON-NLS-1$
				true, Constants.EXEEXTENSION, Constants.EXEFILTERNAMES, null, null, false);
		dngpathEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		dngpathEditor.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validate();
			}
		});
		CLink link = new CLink(basicsGroup, SWT.NONE);
		link.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		link.setText(Messages.getString("ImportPreferencePage.download_dng")); //$NON-NLS-1$
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String vlcDownload = System.getProperty(Messages.getString("ImportPreferencePage.dng_key")); //$NON-NLS-1$
				try {
					IWebBrowser externalBrowser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
					externalBrowser.openURL(new URL(vlcDownload));
				} catch (PartInitException e1) {
					// do nothing
				} catch (MalformedURLException e1) {
					// should never happen
				}
			}
		});
		CGroup optionsGroup = UiUtilities.createGroup(composite, 2, Messages.getString("ImportPreferencePage.options")); //$NON-NLS-1$
		uncompressedButton = WidgetFactory.createCheckButton(optionsGroup,
				Messages.getString("ImportPreferencePage.uncompressed_dng"), //$NON-NLS-1$
				new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		linearButton = WidgetFactory.createCheckButton(optionsGroup,
				Messages.getString("ImportPreferencePage.linear_dng"), new GridData( //$NON-NLS-1$
						SWT.BEGINNING, SWT.CENTER, false, false, 2, 1));
		Label flabel = new Label(optionsGroup, SWT.NONE);
		flabel.setText(Messages.getString("ImportPreferencePage.dng_subfolder")); //$NON-NLS-1$
		dngfolderField = new Text(optionsGroup, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
		GridData layoutData = new GridData(250, SWT.DEFAULT);
		layoutData.horizontalIndent = 20;
		dngfolderField.setLayoutData(layoutData);
		dngfolderField.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				if (BatchUtilities.checkFilename(e.text) > 0)
					e.doit = false;
			}
		});
		return composite;
	}

	private Composite createRawGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		Composite rccomp = new Composite(composite, SWT.NONE);
		rccomp.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		rccomp.setLayout(new GridLayout(2, false));
		new Label(rccomp, SWT.NONE).setText(Messages.getString("ImportPreferencePage.raw_converter2")); //$NON-NLS-1$

		rcViewer = new ComboViewer(rccomp, SWT.NONE);
		rcViewer.setContentProvider(ArrayContentProvider.getInstance());
		rcViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IRawConverter)
					return ((IRawConverter) element).getName();
				return super.getText(element);
			}
		});
		Map<String, IRawConverter> rawConverters = BatchActivator.getDefault().getRawConverters();
		rcViewer.setInput(rawConverters.values());
		rcViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateRawOptions();
				validate();
			}
		});
		basicsGroup = new CGroup(composite, SWT.NONE);
		basicsGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		basicsLayout = new StackLayout();
		basicsGroup.setLayout(basicsLayout);
		basicsGroup.setText(Messages.getString("ImportPreferencePage.converter")); //$NON-NLS-1$
		for (IRawConverter rc : rawConverters.values()) {
			String exec = rc.getExecutable();
			if (!IRawConverter.NONE.equals(exec)) {
				Composite basicsComp = new Composite(basicsGroup, SWT.NONE);
				basicsComp.setLayout(new GridLayout(1, false));
				FileEditor fileEditor = new FileEditor(basicsComp, SWT.OPEN | SWT.READ_ONLY,
						NLS.bind(IRawConverter.OPTIONAL.equals(exec)
								? Messages.getString("ImportPreferencePage.external_executable") //$NON-NLS-1$
								: Messages.getString("ImportPreferencePage.executable"), rc.getName()), //$NON-NLS-1$
						true, Constants.EXEEXTENSION, Constants.EXEFILTERNAMES, null, null, false, true);
				fileEditor.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						validate();
					}
				});
				String msg = rc.getVersionMessage();
				if (msg != null)
					new Label(basicsComp, SWT.NONE).setText(msg);
				basicsFileEditors.put(rc.getId(), fileEditor);
			}
		}
		optionsGroup = new CGroup(composite, SWT.NONE);
		optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		optionsLayout = new StackLayout();
		optionsGroup.setLayout(optionsLayout);
		optionsGroup.setText(Messages.getString("ImportPreferencePage.options2")); //$NON-NLS-1$
		for (IRawConverter rc : rawConverters.values()) {
			final IRawConverter rawConverter = rc;
			List<RawProperty> props = rawConverter.getProperties();
			if (!props.isEmpty()) {
				Composite optionComp = new Composite(optionsGroup, SWT.NONE);
				optionComp.setLayout(new GridLayout(2, false));
				for (RawProperty prop : props) {
					String type = prop.type;
					List<RawEnum> enums = prop.enums;
					if (enums != null && !enums.isEmpty()) {
						new Label(optionComp, SWT.NONE).setText(prop.name);
						ComboViewer viewer = new ComboViewer(optionComp, SWT.NONE);
						viewer.setLabelProvider(new LabelProvider() {
							@Override
							public String getText(Object element) {
								if (element instanceof RawEnum)
									return ((RawEnum) element).value;
								return super.getText(element);
							}
						});
						viewer.setContentProvider(ArrayContentProvider.getInstance());
						viewer.setInput(enums);
						optionProps.put(prop.id, viewer);
						for (RawEnum rawEnum : enums)
							if (rawEnum.recipe) {
								viewer.addSelectionChangedListener(new ISelectionChangedListener() {
									public void selectionChanged(SelectionChangedEvent event) {
										RawEnum e = (RawEnum) ((IStructuredSelection) event.getSelection())
												.getFirstElement();
										rawConverter.setUsesRecipes(e == null ? "" //$NON-NLS-1$
												: e.id);
										updateThumbnailWarning();
									}
								});
								break;
							}
					} else if ("int".equals(type)) { //$NON-NLS-1$
						new Label(optionComp, SWT.NONE).setText(prop.name);
						Spinner field = new Spinner(optionComp, SWT.BORDER);
						if (prop.max != null)
							try {
								field.setMaximum(Integer.parseInt(prop.max));
							} catch (NumberFormatException e) {
								// do nothing
							}
						if (prop.min != null)
							try {
								field.setMinimum(Integer.parseInt(prop.min));
							} catch (NumberFormatException e) {
								// do nothing
							}
						optionProps.put(prop.id, field);
					} else if ("boolean".equals(type)) //$NON-NLS-1$
						optionProps.put(prop.id, WidgetFactory.createCheckButton(optionComp, prop.name,
								new GridData(SWT.BEGINNING, SWT.CENTER, true, false, 2, 1)));
					else {
						new Label(optionComp, SWT.NONE).setText(prop.name);
						optionProps.put(prop.id, new Text(optionComp, SWT.BORDER));
					}
				}
				optionComps.put(rc.getId(), optionComp);
			}
		}

		allDetectors = CoreActivator.getDefault().getRecipeDetectors();
		if (allDetectors != null && !allDetectors.isEmpty())
			createRecipeGroup(composite);
		thumbnailWarning = new Label(composite, SWT.NONE);
		thumbnailWarning.setData("id", "errors"); //$NON-NLS-1$ //$NON-NLS-2$
		thumbnailWarning.setText(Messages.getString("ImportPreferencePage.raw_thumbnail_warning")); //$NON-NLS-1$
		archiveRecipesButton = WidgetFactory.createCheckButton(composite,
				Messages.getString("ImportPreferencePage.archive_recipes"), new GridData(SWT.BEGINNING, SWT.CENTER, //$NON-NLS-1$
						false, false, 2, 1));
		synchronizeRecipesButton = WidgetFactory.createCheckButton(composite,
				Messages.getString("ImportPreferencePage.immediate_update"), new GridData(SWT.BEGINNING, SWT.CENTER, //$NON-NLS-1$
						false, false, 2, 1));
		return composite;
	}

	protected void updateRawOptions() {
		IRawConverter rc = (IRawConverter) ((IStructuredSelection) rcViewer.getSelection()).getFirstElement();
		if (rc != null) {
			String id = rc.getId();
			FileEditor fe = basicsFileEditors.get(id);
			if (fe != null) {
				Composite parent = fe.getParent();
				basicsLayout.topControl = parent;
				basicsGroup.setVisible(true);
				basicsGroup.layout();
			} else
				basicsGroup.setVisible(false);
			Composite composite = optionComps.get(id);
			if (composite != null) {
				optionsLayout.topControl = composite;
				optionsGroup.setVisible(true);
				optionsGroup.layout();
			} else
				optionsGroup.setVisible(false);
			if (recipeGroup != null)
				recipeGroup.setVisible(rc.isDetectors());
			updateThumbnailWarning();
		}
	}

	@SuppressWarnings("unused")
	private void createRecipeGroup(Composite composite) {
		recipeGroup = UiUtilities.createGroup(composite, 2, Messages.getString("ImportPreferencePage.recipe_detectors")); //$NON-NLS-1$
		recipeViewer = CheckboxTableViewer.newCheckList(recipeGroup,
				SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		recipeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableViewerColumn col1 = new TableViewerColumn(recipeViewer, SWT.NONE);
		col1.getColumn().setWidth(250);
		col1.getColumn().setText(Messages.getString("ImportPreferencePage.raw_converter")); //$NON-NLS-1$
		col1.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IRecipeDetector)
					return ((IRecipeDetector) element).getName();
				return super.getText(element);
			}
		});
		TableViewerColumn col2 = new TableViewerColumn(recipeViewer, SWT.NONE);
		col2.getColumn().setWidth(350);
		col2.getColumn().setText(Messages.getString("ImportPreferencePage.configuration")); //$NON-NLS-1$
		col2.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IRecipeDetector) {
					List<IRecipeParameter> parameters = ((IRecipeDetector) element).getParameters();
					StringBuffer sb = new StringBuffer();
					for (IRecipeParameter parm : parameters) {
						if (sb.length() > 0)
							sb.append("; "); //$NON-NLS-1$
						String value = parm.getLabel(parm.getValue());
						if (value != null)
							sb.append(parm.getName()).append(":").append(value); //$NON-NLS-1$
					}
					return sb.toString();
				}
				return super.getText(element);
			}
		});
		col2.setEditingSupport(new EditingSupport(recipeViewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof IRecipeDetector && value instanceof Integer) {
					List<IRecipeParameter> parameters = ((IRecipeDetector) element).getParameters();
					if (!parameters.isEmpty()) {
						IRecipeParameter parameter = parameters.get(0);
						List<IRecipeParameterValue> values = parameters.get(0).getValues();
						int i = ((Integer) value).intValue();
						if (i >= 0 && i < values.size()) {
							parameter.setValue(values.get(i).getId());
							recipeViewer.update(element, null);
						}
					}
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof IRecipeDetector) {
					List<IRecipeParameter> parameters = ((IRecipeDetector) element).getParameters();
					if (!parameters.isEmpty()) {
						IRecipeParameter parameter = parameters.get(0);
						List<IRecipeParameterValue> values = parameters.get(0).getValues();
						String value = parameter.getValue();
						int i = 0;
						for (IRecipeParameterValue v : values) {
							if (v.getId().equals(value))
								return i;
							++i;
						}
					}
				}
				return -1;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				if (element instanceof IRecipeDetector) {
					List<IRecipeParameter> parameters = ((IRecipeDetector) element).getParameters();
					if (!parameters.isEmpty()) {
						List<IRecipeParameterValue> values = parameters.get(0).getValues();
						String[] labs = new String[values.size()];
						int i = 0;
						for (IRecipeParameterValue value : values)
							labs[i++] = value.getLabel();
						return new ComboBoxCellEditor(recipeViewer.getTable(), labs);
					}
				}
				return null;
			}

			@Override
			protected boolean canEdit(Object element) {
				if (element instanceof IRecipeDetector && recipeViewer.getChecked(element)) {
					List<IRecipeParameter> parameters = ((IRecipeDetector) element).getParameters();
					return !parameters.isEmpty();
				}
				return false;
			}
		});
		recipeViewer.getTable().setHeaderVisible(true);
		recipeViewer.getTable().setLinesVisible(true);
		recipeViewer.setContentProvider(ArrayContentProvider.getInstance());
		recipeViewer.setCheckStateProvider(new ICheckStateProvider() {
			public boolean isGrayed(Object element) {
				return false;
			}

			public boolean isChecked(Object element) {
				if (element instanceof IRecipeDetector)
					return selectedRecipeDetectors.contains(((IRecipeDetector) element).getId());
				return false;
			}
		});
		recipeViewer.setComparator(new ViewerComparator());
		recipeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateRecipeButtons();
			}
		});
		recipeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object element = event.getElement();
				if (element instanceof IRecipeDetector) {
					String id = ((IRecipeDetector) element).getId();
					if (event.getChecked()) {
						selectedRecipeDetectors.add(id);
						processRecipesButton.setSelection(true);
					} else {
						selectedRecipeDetectors.remove(id);
						if (selectedRecipeDetectors.isEmpty())
							processRecipesButton.setSelection(false);
					}
					recipeViewer.setInput(allDetectors);
					updateRecipeButtons();
					updateRecipeOptionButtons();
				}
			}
		});
		new AllNoneGroup(recipeGroup, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				recipeViewer.setAllChecked(e.widget.getData() == AllNoneGroup.ALL);
				updateRecipeButtons();
			}
		});
		processRecipesButton = WidgetFactory.createCheckButton(recipeGroup,
				Messages.getString("ImportPreferencePage.process_recipes"), new GridData(SWT.BEGINNING, SWT.CENTER, //$NON-NLS-1$
						false, false, 2, 1));
		processRecipesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateThumbnailWarning();
			}

		});
		synchronizeRecipesButton = WidgetFactory.createCheckButton(recipeGroup,
				Messages.getString("ImportPreferencePage.immediate_update"), new GridData(SWT.BEGINNING, SWT.CENTER, //$NON-NLS-1$
						false, false, 2, 1));
		synchronizeRecipesButton.setVisible(false);
	}

	private void updateRecipeOptionButtons() {
		IRawConverter rc = (IRawConverter) ((IStructuredSelection) rcViewer.getSelection()).getFirstElement();
		boolean enabled = rc != null && rc.isDetectors() && processRecipesButton.getSelection();
		String usesRecipes = rc == null ? "" : rc.getUsesRecipes(); //$NON-NLS-1$
		archiveRecipesButton.setVisible(enabled || !usesRecipes.isEmpty());
		Set<String> newSelection = new HashSet<String>(selectedRecipeDetectors);
		boolean sync = rc != null && (rc != previousRawConverter || !usesRecipes.equals(previousUsesRecipes)
				|| (rc.isDetectors() && (previousProcessRecipes != processRecipesButton.getSelection()
						|| !previousRecipeDetectors.equals(newSelection) && processRecipesButton.getSelection())));
		synchronizeRecipesButton.setVisible(sync);
	}

	private void updateThumbnailWarning() {
		IRawConverter rc = (IRawConverter) ((IStructuredSelection) rcViewer.getSelection()).getFirstElement();
		boolean visible = rc != null && (!rc.getUsesRecipes().isEmpty()
				|| (rc.isDetectors() && processRecipesButton.getEnabled() && processRecipesButton.getSelection()))
				&& Core.getCore().getDbManager().getMeta(true).getThumbnailFromPreview();
		thumbnailWarning.setVisible(visible);
		updateRecipeOptionButtons();
	}

	protected void updateRecipeButtons() {
		if (processRecipesButton != null)
			processRecipesButton.setEnabled(!selectedRecipeDetectors.isEmpty());
		updateThumbnailWarning();
	}

	/**
	 * The default button has been pressed.
	 */

	@Override
	public void doPerformDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		Map<String, IRawConverter> rawConverters = BatchActivator.getDefault().getRawConverters();
		for (IRawConverter rc : rawConverters.values()) {
			String path = rc.getPathId();
			String dflt = preferenceStore.getDefaultString(path);
			preferenceStore.setValue(path, dflt);
			BatchUtilities.putPreferences(path, dflt);
			for (IRawConverter.RawProperty prop : rc.getProperties()) {
				dflt = preferenceStore.getDefaultString(prop.id);
				preferenceStore.setValue(prop.id, dflt);
				BatchUtilities.putPreferences(prop.id, dflt);
			}
		}
		preferenceStore.setValue(PreferenceConstants.RAWIMPORT,
				preferenceStore.getDefaultString(PreferenceConstants.RAWIMPORT));
		preferenceStore.setValue(PreferenceConstants.DNGCONVERTERPATH,
				preferenceStore.getDefaultString(PreferenceConstants.DNGCONVERTERPATH));
		preferenceStore.setValue(PreferenceConstants.DNGUNCOMPRESSED,
				preferenceStore.getDefaultBoolean(PreferenceConstants.DNGUNCOMPRESSED));
		preferenceStore.setValue(PreferenceConstants.DNGLINEAR,
				preferenceStore.getDefaultBoolean(PreferenceConstants.DNGLINEAR));
		preferenceStore.setValue(PreferenceConstants.DERIVERELATIONS,
				preferenceStore.getDefaultString(PreferenceConstants.DERIVERELATIONS));
		preferenceStore.setValue(PreferenceConstants.DNGFOLDER,
				preferenceStore.getDefaultString(PreferenceConstants.DNGFOLDER));
		preferenceStore.setValue(PreferenceConstants.DEVICEWATCH,
				preferenceStore.getDefaultString(PreferenceConstants.DEVICEWATCH));
		preferenceStore.setValue(PreferenceConstants.MAXIMPORTS,
				preferenceStore.getDefaultInt(PreferenceConstants.MAXIMPORTS));
		preferenceStore.setValue(PreferenceConstants.RECIPEDETECTORS,
				preferenceStore.getDefaultString(PreferenceConstants.RECIPEDETECTORS));
		preferenceStore.setValue(PreferenceConstants.PROCESSRECIPES,
				preferenceStore.getDefaultBoolean(PreferenceConstants.PROCESSRECIPES));
		preferenceStore.setValue(PreferenceConstants.ARCHIVERECIPES,
				preferenceStore.getDefaultBoolean(PreferenceConstants.ARCHIVERECIPES));
		preferenceStore.setValue(PreferenceConstants.AUTORULES,
				preferenceStore.getDefaultString(PreferenceConstants.AUTORULES));
	}

	@Override
	protected void doPerformOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		IRawConverter c = (IRawConverter) ((IStructuredSelection) rcViewer.getSelection()).getFirstElement();
		if (c != null)
			BatchActivator.getDefault().setCurrentRawConverter(c);
		Map<String, IRawConverter> rawConverters = BatchActivator.getDefault().getRawConverters();
		for (IRawConverter rc : rawConverters.values()) {
			FileEditor fileEditor = basicsFileEditors.get(rc.getId());
			if (fileEditor != null) {
				String text = fileEditor.getText();
				preferenceStore.setValue(rc.getPathId(), text);
				BatchUtilities.putPreferences(rc.getPathId(), text);
				rc.setPath(text);
			}
			List<RawProperty> props = rc.getProperties();
			for (RawProperty prop : props) {
				Object object = optionProps.get(prop.id);
				if (object instanceof ComboViewer) {
					RawEnum rawEnum = (RawEnum) ((IStructuredSelection) ((ComboViewer) object).getSelection())
							.getFirstElement();
					if (rawEnum != null)
						prop.value = rawEnum.id;
				} else if (object instanceof Spinner)
					prop.value = String.valueOf(((Spinner) object).getSelection());
				else if (object instanceof Button)
					prop.value = String.valueOf(((Button) object).getSelection());
				else if (object instanceof Text)
					prop.value = ((Button) object).getText();
				preferenceStore.setValue(prop.id, prop.value);
				BatchUtilities.putPreferences(prop.id, prop.value);
			}
		}

		if (modeviewer != null) {
			IStructuredSelection selection = (IStructuredSelection) modeviewer.getSelection();
			if (!selection.isEmpty())
				preferenceStore.setValue(PreferenceConstants.RAWIMPORT, (String) selection.getFirstElement());
		}
		if (dngpathEditor != null)
			preferenceStore.setValue(PreferenceConstants.DNGCONVERTERPATH, dngpathEditor.getText());
		if (uncompressedButton != null)
			preferenceStore.setValue(PreferenceConstants.DNGUNCOMPRESSED, uncompressedButton.getSelection());
		if (linearButton != null)
			preferenceStore.setValue(PreferenceConstants.DNGLINEAR, linearButton.getSelection());
		if (dngfolderField != null)
			preferenceStore.setValue(PreferenceConstants.DNGFOLDER, dngfolderField.getText());
		preferenceStore.setValue(PreferenceConstants.MAXIMPORTS, maxSpinner.getSelection());
		preferenceStore.setValue(PreferenceConstants.DEVICEWATCH, deviceButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.SHOWIMPORTED, showButton.getSelection());
		preferenceStore.setValue(PreferenceConstants.IMPORTMAKERNOTES, makerNotesButton.getSelection());
		StringBuilder sb = new StringBuilder();
		if (recipeViewer != null && recipeGroup.isVisible()) {
			if (allDetectors != null) {
				List<String> configList = new ArrayList<String>(allDetectors.size());
				for (IRecipeDetector detector : allDetectors) {
					sb.setLength(0);
					for (IRecipeParameter parm : detector.getParameters()) {
						if (sb.length() > 0)
							sb.append(';');
						else
							sb.append(detector.getId()).append(':');
						sb.append(parm.getId()).append('=').append(parm.getValue());
					}
					configList.add(sb.toString());
				}
				preferenceStore.setValue(PreferenceConstants.RECIPEDETECTORCONFIGURATIONS,
						Core.toStringList(configList, '\n'));
			}
			if (selectedRecipeDetectors != null)
				preferenceStore.setValue(PreferenceConstants.RECIPEDETECTORS,
						Core.toStringList(selectedRecipeDetectors, '\n'));
			preferenceStore.setValue(PreferenceConstants.ARCHIVERECIPES, archiveRecipesButton.getSelection());
			preferenceStore.setValue(PreferenceConstants.PROCESSRECIPES, processRecipesButton.getSelection());
		}
		if (synchronizeRecipesButton.isVisible() && synchronizeRecipesButton.getSelection())
			new UpdateRawImagesJob(c != previousRawConverter).schedule(200);
	}

	@Override
	protected String doValidate() {
		if (modeviewer != null) {
			IStructuredSelection selection = (IStructuredSelection) modeviewer.getSelection();
			if (!selection.isEmpty() && !Constants.RAWIMPORT_ONLYRAW.equals(selection.getFirstElement())) {
				String fn = dngpathEditor.getText();
				if (!fn.isEmpty() && !new File(fn).exists())
					return Messages.getString("ImportPreferencePage.dng_converter_does_not_exist"); //$NON-NLS-1$
			}
		}
		IRawConverter c = (IRawConverter) ((IStructuredSelection) rcViewer.getSelection()).getFirstElement();
		if (c != null) {
			String fn = basicsFileEditors.get(c.getId()).getText();
			if (!fn.isEmpty()) {
				if (!new File(fn).exists())
					return NLS.bind(Messages.getString("ImportPreferencePage.external_dcraw_does_not_exist"), //$NON-NLS-1$
							c.getName());
			} else if ("required".equals(c.getExecutable())) //$NON-NLS-1$
				return NLS.bind(Messages.getString("ImportPreferencePage.specify_path"), c.getName()); //$NON-NLS-1$
		} else
			return Messages.getString("ImportPreferencePage.select_raw"); //$NON-NLS-1$
		return null;
	}

}
