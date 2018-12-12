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
 * (c) 2016 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.bdaum.zoom.cat.model.MigrationPolicy;
import com.bdaum.zoom.cat.model.MigrationPolicyImpl;
import com.bdaum.zoom.cat.model.MigrationPolicy_type;
import com.bdaum.zoom.cat.model.MigrationRule;
import com.bdaum.zoom.cat.model.MigrationRuleImpl;
import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.Format;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.program.BatchConstants;
import com.bdaum.zoom.ui.dialogs.AcousticMessageDialog;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.widgets.FileEditor;
import com.bdaum.zoom.ui.widgets.CGroup;

public class MigrateDialog extends ZTitleAreaDialog {

	private static final int LOAD = 9999;
	private static final int SAVE = 9998;
	private static final String SETTINGSID = "com.bdaum.zoom.migrateDialog"; //$NON-NLS-1$
	private File catFile;
	private List<MigrationRule> rules = new ArrayList<MigrationRule>();
	private FileEditor fileEditor;
	private TableViewer viewer;
	private Button editPatternButton;
	private Button removePatternButton;
	private Button downButton;
	private Button upButton;
	private String targetCatalog;
	private boolean dirty;
	private Combo fileSeparatorCombo;
	private int fileSepearatorPolicy = Constants.WIN32 ? 1 : 2;

	public MigrateDialog(Shell parentShell, File catFile) {
		super(parentShell, HelpContextIds.MIGRATE_DIALOG);
		this.catFile = catFile;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText(Messages.MigrateDialog_migration_details);
		setTitle(Messages.MigrateDialog_migrate_catalog);
		setMessage(Messages.MigrateDialog_migrate_catalog_explanation);
		fillValues();
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		CGroup fileGroup = new CGroup(composite, SWT.NONE);
		fileGroup.setText(Messages.MigrateDialog_save_as);
		fileGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
		fileGroup.setLayout(new GridLayout());
		String filename = catFile.getName();
		String migrated = "migrated_" + filename; //$NON-NLS-1$
		String parentFile = catFile.getParent();
		File migratedFile = new File(parentFile, migrated);
		fileEditor = new FileEditor(fileGroup, SWT.SAVE, "", false, //$NON-NLS-1$
				new String[] { "*" + BatchConstants.CATEXTENSION }, //$NON-NLS-1$
				new String[] { "ZoRaPD Catalog (*" //$NON-NLS-1$
						+ BatchConstants.CATEXTENSION + ")" }, //$NON-NLS-1$
				parentFile, migratedFile.getPath(), false, getDialogSettings(UiActivator.getDefault(), SETTINGSID));
		CGroup tableGroup = new CGroup(composite, SWT.NONE);
		tableGroup.setText(Messages.MigrateDialog_transformation);
		tableGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
		tableGroup.setLayout(new GridLayout());
		new Label(tableGroup, SWT.NONE).setText(Messages.MigrateDialog_file_separator_policy);
		fileSeparatorCombo = new Combo(tableGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		fileSeparatorCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fileSeparatorCombo.setItems(new String[] { Messages.MigrateDialog_do_nothing, Messages.MigrateDialog_toSlash,
				Messages.MigrateDialog_toBackslash });
		fileSeparatorCombo.select(fileSepearatorPolicy);
		new Label(tableGroup, SWT.NONE).setText(Messages.MigrateDialog_rules);
		new Label(tableGroup, SWT.NONE).setText(Messages.MigrateDialog_rules_explanation);
		Composite tableComp = new Composite(tableGroup, SWT.NONE);
		tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = layout.verticalSpacing = 0;
		tableComp.setLayout(layout);
		viewer = new TableViewer(tableComp, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 300;
		viewer.getTable().setLayoutData(layoutData);
		final TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setWidth(350);
		col1.getColumn().setText(Messages.MigrateDialog_source_pattern);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MigrationRule)
					return ((MigrationRule) element).getSourcePattern();
				return element.toString();
			}
		});
		final TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setWidth(220);
		col2.getColumn().setText(Messages.MigrateDialog_target_pattern);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MigrationRule)
					return ((MigrationRule) element).getTargetPattern();
				return super.getText(element);
			}
		});
		final TableViewerColumn col3 = new TableViewerColumn(viewer, SWT.NONE);
		col3.getColumn().setWidth(100);
		col3.getColumn().setText(Messages.MigrateDialog_target_volume);
		col3.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof MigrationRule)
					return ((MigrationRule) element).getTargetVolume();
				return element.toString();
			}
		});
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		Composite buttonComp = new Composite(tableComp, SWT.NONE);
		buttonComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonComp.setLayout(new GridLayout(1, false));
		Button addPatternButton = new Button(buttonComp, SWT.PUSH);
		addPatternButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		addPatternButton.setText(Messages.MigrateDialog_add_pattern);
		addPatternButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RegExDialog dialog = new RegExDialog(getShell(), null, null, false,
						fileSeparatorCombo.getSelectionIndex(), -1);
				if (dialog.open() == RegExDialog.OK) {
					MigrationRule result = dialog.getResult();
					rules.add(result);
					viewer.add(result);
					viewer.setSelection(new StructuredSelection(result));
					updateButtons();
					dirty = true;
				}
			}
		});
		Button addFolderButton = new Button(buttonComp, SWT.PUSH);
		addFolderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(getShell());
				dirDialog.setText(Messages.MigrateDialog_select_folder);
				String dir = dirDialog.open();
				if (dir != null) {
					if (!dir.endsWith(File.separator))
						dir += File.separator;
					if (Constants.WIN32) {
						int i = dir.indexOf(':');
						if (i == 1) {
							String volume = Core.getCore().getVolumeManager().getVolumeForFile(new File(dir));
							if (volume != null)
								dir = volume + dir.substring(1);
						}
					}
					char filesep;
					fileSepearatorPolicy = fileSeparatorCombo.getSelectionIndex();
					switch (fileSepearatorPolicy) {
					case 1:
						filesep = '/';
						break;
					case 2:
						filesep = '\\';
						break;
					default:
						filesep = File.separatorChar;
						break;
					}
					String sourcePattern = dir + "(.*)"; //$NON-NLS-1$
					MigrationRule rule = new MigrationRuleImpl(sourcePattern.replaceAll("\\\\", "\\\\\\\\"), //$NON-NLS-1$ //$NON-NLS-2$
							Messages.MigrateDialog_target_folder + filesep + "$1", null); //$NON-NLS-1$
					RegExDialog regexDialog = new RegExDialog(getShell(), rule, dir, true, fileSepearatorPolicy,
							Messages.MigrateDialog_target_folder.length());
					if (regexDialog.open() == RegExDialog.OK) {
						MigrationRule result = regexDialog.getResult();
						rules.add(result);
						viewer.add(result);
						viewer.setSelection(new StructuredSelection(result));
						updateButtons();
						dirty = true;
					}
				}
			}
		});
		addFolderButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		addFolderButton.setText(Messages.MigrateDialog_add_folder);
		editPatternButton = new Button(buttonComp, SWT.PUSH);
		editPatternButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		editPatternButton.setText(Messages.MigrateDialog_edit_pattern);
		editPatternButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MigrationRule policy = (MigrationRule) viewer.getStructuredSelection().getFirstElement();
				RegExDialog regexDialog = new RegExDialog(getShell(), policy, null, false,
						fileSeparatorCombo.getSelectionIndex(), -1);
				if (regexDialog.open() == RegExDialog.OK) {
					MigrationRule result = regexDialog.getResult();
					policy.setSourcePattern(result.getSourcePattern());
					policy.setTargetPattern(result.getTargetPattern());
					viewer.update(policy, null);
					updateButtons();
					dirty = true;
				}
			}
		});
		removePatternButton = new Button(buttonComp, SWT.PUSH);
		removePatternButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		removePatternButton.setText(Messages.MigrateDialog_remove_pattern);
		removePatternButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object policy = viewer.getStructuredSelection().getFirstElement();
				rules.remove(policy);
				viewer.remove(policy);
				updateButtons();
				dirty = true;
			}
		});
		Label sep = new Label(buttonComp, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		upButton = new Button(buttonComp, SWT.PUSH);
		upButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		upButton.setText(Messages.MigrateDialog_up);
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MigrationRule policy = (MigrationRule) viewer.getStructuredSelection().getFirstElement();
				int i = rules.indexOf(policy);
				if (i > 0) {
					rules.remove(i);
					rules.add(i - 1, policy);
					viewer.setInput(rules);
					viewer.setSelection(new StructuredSelection(policy));
					updateButtons();
				}
			}
		});
		downButton = new Button(buttonComp, SWT.PUSH);
		downButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		downButton.setText(Messages.MigrateDialog_down);
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MigrationRule policy = (MigrationRule) viewer.getStructuredSelection().getFirstElement();
				int i = rules.indexOf(policy);
				if (i < rules.size() - 1) {
					rules.remove(i);
					rules.add(i + 1, policy);
					viewer.setInput(rules);
					viewer.setSelection(new StructuredSelection(policy));
					updateButtons();
				}
			}
		});
		return area;
	}

	private void fillValues() {
		viewer.setInput(rules);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, LOAD, Messages.MigrateDialog_load, false);
		createButton(parent, SAVE, Messages.MigrateDialog_save_policy, false);
		super.createButtonsForButtonBar(parent);
	}

	private void updateButtons() {
		IStructuredSelection selection = viewer.getStructuredSelection();
		boolean enabled = !selection.isEmpty();
		removePatternButton.setEnabled(enabled);
		editPatternButton.setEnabled(enabled);
		if (enabled) {
			int i = rules.indexOf(selection.getFirstElement());
			downButton.setEnabled(i < rules.size() - 1);
			upButton.setEnabled(i > 0);
		} else {
			downButton.setEnabled(false);
			upButton.setEnabled(false);
		}
		String errorMessage = null;
		if (fileEditor.getText().isEmpty())
			errorMessage = Messages.MigrateDialog_define_target_cat_file;
		else if (rules.isEmpty())
			errorMessage = Messages.MigrateDialog_define_one_pattern;
		else {
			File file = new File(fileEditor.getText());
			long freeSpace = Core.getCore().getVolumeManager().getRootFile(file).getUsableSpace();
			long catSize = (long) (dbManager.getFile().length() * 1.05d);
			if (freeSpace < catSize)
				errorMessage = NLS.bind(Messages.MigrateDialog_not_enough_free_space,
						Format.sizeFormatter.toString(freeSpace), Format.sizeFormatter.toString(catSize));
		}
		setErrorMessage(errorMessage);
		getButton(OK).setEnabled(errorMessage == null);
		getButton(SAVE).setEnabled(errorMessage == null);
		getButton(LOAD).setEnabled(!dbManager.obtainObjects(MigrationPolicy.class).isEmpty());
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case LOAD:
			if (dirty && !AcousticMessageDialog.openQuestion(getShell(), Messages.MigrateDialog_load_migration_policy,
					Messages.MigrateDialog_abandon_current_policy))
				return;
			List<MigrationPolicy> set = dbManager.obtainObjects(MigrationPolicy.class);
			if (!set.isEmpty()) {
				List<MigrationPolicy> policies = new ArrayList<MigrationPolicy>(set);
				ListDialog dialog = new ListDialog(getShell()) {
					@Override
					public void create() {
						super.create();
						getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
							public void selectionChanged(SelectionChangedEvent event) {
								updateDeleteButton();
							}
						});
					}

					@Override
					protected void createButtonsForButtonBar(Composite parent) {
						createButton(parent, 9999, Messages.MigrateDialog_remove, false);
						super.createButtonsForButtonBar(parent);
					}

					@Override
					protected void buttonPressed(int buttonId) {
						if (buttonId == 9999) {
							TableViewer tableViewer = getTableViewer();
							IStructuredSelection selection = tableViewer.getStructuredSelection();
							if (!selection.isEmpty()) {
								MigrationPolicy policy = (MigrationPolicy) selection.getFirstElement();
								tableViewer.remove(policy);
								List<Object> toBeDeleted = new ArrayList<Object>(policy.getRule().length + 1);
								toBeDeleted.addAll(Arrays.asList(policy.getRule()));
								toBeDeleted.add(policy);
								dbManager.safeTransaction(toBeDeleted, null);
								updateDeleteButton();
								return;
							}
						} else
							super.buttonPressed(buttonId);
					}

					private void updateDeleteButton() {
						getButton(9999).setEnabled(!getTableViewer().getSelection().isEmpty());
					}
				};
				dialog.setContentProvider(ArrayContentProvider.getInstance());
				dialog.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());
				dialog.setMessage(Messages.MigrateDialog_available_policies);
				dialog.setTitle(Messages.MigrateDialog_migration_policies);
				dialog.setInput(policies);
				if (dialog.open() == ListSelectionDialog.OK) {
					Object[] result = dialog.getResult();
					if (result != null && result.length > 0) {
						MigrationPolicy policy = (MigrationPolicy) result[0];
						rules = new ArrayList<MigrationRule>(Arrays.asList(policy.getRule()));
						fileEditor.setText(policy.getTargetCatalog());
						String fsp = policy.getFileSeparatorPolicy();
						fileSepearatorPolicy = MigrationPolicy_type.fileSeparatorPolicy_tOSLASH.equals(fsp) ? 1
								: MigrationPolicy_type.fileSeparatorPolicy_tOBACKSLASH.equals(fsp) ? 2 : 0;
						fileSeparatorCombo.select(fileSepearatorPolicy);
						viewer.setInput(rules);
						updateButtons();
						dirty = false;
					}
				}
			}
			return;
		case SAVE:
			File file = new File(fileEditor.getText());
			String filename = file.getName();
			if (filename.endsWith(BatchConstants.CATEXTENSION))
				filename = filename.substring(0, filename.length() - BatchConstants.CATEXTENSION.length());
			InputDialog dialog = new InputDialog(getShell(), Messages.MigrateDialog_save_migration_policy,
					Messages.MigrateDialog_specify_name, filename, null);
			if (dialog.open() == InputDialog.OK) {
				String name = dialog.getValue();
				fileSepearatorPolicy = fileSeparatorCombo.getSelectionIndex();
				String fsp = convertFileSeparatorPolicy();
				MigrationPolicy policy;
				set = dbManager.obtainObjects(MigrationPolicy.class, "name", name, QueryField.EQUALS); //$NON-NLS-1$
				List<Object> toBeDeleted = null;
				if (!set.isEmpty()) {
					if (AcousticMessageDialog.openQuestion(getShell(), Messages.MigrateDialog_save_migration_policy,
							Messages.MigrateDialog_policy_already_exists))
						return;
					policy = set.get(0);
					toBeDeleted = new ArrayList<Object>(Arrays.asList(policy.getRule()));
					policy.setTargetCatalog(targetCatalog);
					policy.setRule(rules.toArray(new MigrationRule[rules.size()]));
					policy.setFileSeparatorPolicy(fsp);
				} else {
					policy = new MigrationPolicyImpl(name, fsp, fileEditor.getText());
					policy.setRule(rules.toArray(new MigrationRule[rules.size()]));
				}
				List<Object> toBeStored = new ArrayList<Object>(rules.size() + 1);
				toBeStored.addAll(rules);
				toBeStored.add(policy);
				dbManager.safeTransaction(toBeDeleted, toBeStored);
				dirty = false;
			}
			return;
		}
		super.buttonPressed(buttonId);
	}

	public String convertFileSeparatorPolicy() {
		switch (fileSepearatorPolicy) {
		case 1:
			return MigrationPolicy_type.fileSeparatorPolicy_tOSLASH;
		case 2:
			return MigrationPolicy_type.fileSeparatorPolicy_tOBACKSLASH;
		default:
			return MigrationPolicy_type.fileSeparatorPolicy_nOCHANGE;
		}
	}

	@Override
	protected void okPressed() {
		targetCatalog = fileEditor.getText();
		fileSepearatorPolicy = fileSeparatorCombo.getSelectionIndex();
		super.okPressed();
	}

	/**
	 * @return policies
	 */
	public MigrationPolicy getResult() {
		MigrationPolicyImpl policy = new MigrationPolicyImpl("", convertFileSeparatorPolicy(), targetCatalog); //$NON-NLS-1$
		policy.setRule(rules.toArray(new MigrationRule[rules.size()]));
		return policy;
	}

}
