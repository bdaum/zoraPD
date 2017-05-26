package com.bdaum.zoom.ui.internal.wizards;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;

import com.bdaum.zoom.cat.model.meta.WatchedFolderImpl;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.HelpContextIds;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public class FilterPage extends ColoredWizardPage {

	private static class Filter {
		String pattern;
		boolean folder = false;
		boolean rejects = false;

		Filter(String s) {
			if (s.endsWith("/")) { //$NON-NLS-1$
				s = s.substring(0, s.length() - 1);
				folder = true;
			}
			if (s.startsWith("+")) //$NON-NLS-1$
				s = s.substring(1);
			else if (s.startsWith("-")) { //$NON-NLS-1$
				s = s.substring(1);
				rejects = true;
			}
			pattern = s.length() > 0 ? s : "*"; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(rejects ? '-' : '+').append(pattern);
			if (folder)
				sb.append('/');
			return sb.toString();
		}
	}

	private final WatchedFolderImpl watchedFolder;
	private TableViewer viewer;
	private Button addButton;
	private Button removeButton;
	private List<Filter> filters = new ArrayList<Filter>();
	private Button addFolderButton;
	private Button upButton;
	private Button downButton;

	public FilterPage(String pageName, WatchedFolderImpl watchedFolder) {
		super(pageName);
		this.watchedFolder = watchedFolder;
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));

		viewer = new TableViewer(composite, SWT.SINGLE | SWT.V_SCROLL
				| SWT.BORDER);
		viewer.getTable().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		TableViewerColumn col1 = createColumn(
				Messages.FileFilterDialog_pattern, 300);
		col1.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Filter)
					return ((Filter) element).pattern;
				return element.toString();
			}
		});
		col1.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Filter && value instanceof String) {
					((Filter) element).pattern = ((String) value);
					viewer.update(element, null);
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Filter)
					return ((Filter) element).pattern;
				return ""; //$NON-NLS-1$
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				TextCellEditor textCellEditor = new TextCellEditor(viewer
						.getTable());
				textCellEditor.setValidator(new ICellEditorValidator() {
					public String isValid(Object value) {
						String errorMessage = null;
						if (value != null && value.toString().indexOf(';') >= 0)
							errorMessage = Messages.FileFilterDialog_no_semicolon;
						setErrorMessage(errorMessage);
						return errorMessage;
					}
				});
				return textCellEditor;
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		TableViewerColumn col2 = createColumn(Messages.FileFilterDialog_type,
				70);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Filter)
					return ((Filter) element).folder ? Messages.FileFilterDialog_folder
							: Messages.FileFilterDialog_file;
				return element.toString();
			}
		});
		col2.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Filter && value instanceof Boolean) {
					((Filter) element).folder = ((Boolean) value);
					viewer.update(element, null);
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Filter)
					return ((Filter) element).folder;
				return Boolean.FALSE;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new CheckboxCellEditor(viewer.getTable());
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		TableViewerColumn col3 = createColumn(Messages.FileFilterDialog_action,
				90);
		col3.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Filter)
					return ((Filter) element).rejects ? Messages.FileFilterDialog_reject
							: Messages.FileFilterDialog_accept;
				return element.toString();
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		col3.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Filter && value instanceof Boolean) {
					((Filter) element).rejects = ((Boolean) value);
					viewer.update(element, null);
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Filter)
					return ((Filter) element).rejects;
				return Boolean.FALSE;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new CheckboxCellEditor(viewer.getTable());
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		Composite buttonGroup = new Composite(composite, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.BEGINNING, SWT.END, true,
				false));
		buttonGroup.setLayout(new GridLayout(1, false));
		addFolderButton = new Button(buttonGroup, SWT.PUSH);
		addFolderButton.setText(Messages.FilterPage_add_folder);
		addFolderButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				try {
					File parent = new File(new URI(watchedFolder.getUri()));
					String parentPath = parent.getAbsolutePath();
					dialog.setFilterPath(parentPath);
					dialog.setText(Messages.FilterPage_select_subfolder);
					dialog.setMessage(Messages.FilterPage_select_subfolder_msg);
					String subdir = dialog.open();
					if (subdir != null
							&& subdir.length() > parentPath.length() + 1
							&& subdir.startsWith(parentPath)) {
						File folder = new File(subdir);
						String folderPath = folder.getAbsolutePath();
						Filter f = new Filter('-' + folderPath
								.substring(parentPath.length() + 1) + '/');
						filters.add(f);
						viewer.insert(f, -1);
						viewer.setSelection(new StructuredSelection(f));
					}
				} catch (URISyntaxException ex) {
					// Should never happen
				}
			}
		});
		addButton = new Button(buttonGroup, SWT.PUSH);
		addButton.setText(Messages.FileFilterDialog_add);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Filter f = new Filter("+*.*"); //$NON-NLS-1$
				filters.add(f);
				viewer.insert(f, -1);
				viewer.setSelection(new StructuredSelection(f));
			}
		});
		removeButton = new Button(buttonGroup, SWT.PUSH);
		removeButton.setText(Messages.FileFilterDialog_remove);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				if (!selection.isEmpty()) {
					Filter f = (Filter) selection.getFirstElement();
					filters.remove(f);
					viewer.remove(f);
				}
			}
		});
		new Label(buttonGroup, SWT.SEPARATOR | SWT.HORIZONTAL);
		upButton = new Button(buttonGroup, SWT.PUSH);
		upButton.setText(Messages.FilterPage_up);
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				if (!selection.isEmpty()) {
					Filter f = (Filter) selection.getFirstElement();
					int index = filters.indexOf(f);
					if (index > 0) {
						filters.remove(f);
						filters.add(index - 1, f);
						viewer.setInput(filters);
						viewer.setSelection(new StructuredSelection(f));
					}
				}
			}
		});
		downButton = new Button(buttonGroup, SWT.PUSH);
		downButton.setText(Messages.FilterPage_down);
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) viewer
						.getSelection();
				if (!selection.isEmpty()) {
					Filter f = (Filter) selection.getFirstElement();
					int index = filters.indexOf(f);
					if (index < filters.size() - 1) {
						filters.remove(f);
						filters.add(index + 1, f);
						viewer.setInput(filters);
						viewer.setSelection(new StructuredSelection(f));
					}
				}
			}
		});
		setControl(composite);
		setHelp(HelpContextIds.WATCHED_FOLDER_FILTER);
		setTitle(getName());
		setMessage(Messages.FilterPage_filter_page_msg);
		super.createControl(parent);
		fillValues();
		updateButtons();
	}

	private TableViewerColumn createColumn(String lab, int w) {
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText(lab);
		col.getColumn().setWidth(w);
		return col;
	}

	private void fillValues() {
		String s = watchedFolder.getFilters();
		for (String f : Core.fromStringList(s, ";")) //$NON-NLS-1$
			filters.add(new Filter(f));
		viewer.setInput(filters);
	}

	@Override
	protected void validatePage() {
		// do nothing
	}

	public void performFinish() {
		String[] result = new String[filters.size()];
		int i = 0;
		for (Filter f : filters)
			result[i++] = f.toString();
		watchedFolder.setFilters(Core.toStringList(result, "; ")); //$NON-NLS-1$
	}

	protected void updateButtons() {
		Object first = ((IStructuredSelection) viewer.getSelection())
				.getFirstElement();
		if (first == null) {
			removeButton.setEnabled(false);
			upButton.setEnabled(false);
			downButton.setEnabled(false);
		} else {
			removeButton.setEnabled(true);
			int index = filters.indexOf(first);
			upButton.setEnabled(index > 0);
			downButton.setEnabled(index >= 0 && index < filters.size() - 1);
		}

	}

}
