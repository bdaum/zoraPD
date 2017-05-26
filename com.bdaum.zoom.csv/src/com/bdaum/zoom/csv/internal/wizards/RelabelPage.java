package com.bdaum.zoom.csv.internal.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.csv.internal.HelpContextIds;
import com.bdaum.zoom.ui.wizards.ColoredWizardPage;

public class RelabelPage extends ColoredWizardPage {

	private static final String NEWLABELS = "newLabels"; //$NON-NLS-1$
	private Map<String, String> relabelMap;
	private TableViewer viewer;

	public RelabelPage() {
		super("relabel"); //$NON-NLS-1$
	}

	@SuppressWarnings("unused")
	@Override
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		createViewer(composite);
		fillViewer();
		new Label(composite, SWT.NONE);
		setControl(composite);
		setHelp(HelpContextIds.CSV_WIZARD);
		setTitle(Messages.RelabelPage_relabel);
		setMessage(Messages.RelabelPage_relabel_message);
		super.createControl(parent);
	}

	private void createViewer(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(2, false));
		viewer = new TableViewer(comp, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER);
		Table table = viewer.getTable();
		table.setLayoutData(new GridData(605,400));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableViewerColumn col1 = new TableViewerColumn(viewer, SWT.NONE);
		col1.getColumn().setWidth(300);
		col1.getColumn().setText(Messages.RelabelPage_dflt_name);
		col1.setLabelProvider(ZColumnLabelProvider.getDefaultInstance());
		TableViewerColumn col2 = new TableViewerColumn(viewer, SWT.NONE);
		col2.getColumn().setWidth(300);
		col2.getColumn().setText(Messages.RelabelPage_new_name);
		col2.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return relabelMap.get(element);
			}
		});
		col2.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof String) {
					if (value instanceof String
							&& ((String) value).length() > 0) {
						relabelMap.put((String) element, (String) value);
					} else
						relabelMap.remove(element);
				}
				viewer.update(element, null);
			}

			@Override
			protected Object getValue(Object element) {
				String newName = relabelMap.get(element);
				return newName == null ? "" : newName; //$NON-NLS-1$
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getTable());
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setComparator(new ViewerComparator());

	}

	protected void fillViewer() {
		if (relabelMap == null) {
			relabelMap = new HashMap<String, String>(257);
			String newLabels = getDialogSettings().get(NEWLABELS);
			if (newLabels != null) {
				StringTokenizer st = new StringTokenizer(newLabels, "\n"); //$NON-NLS-1$
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					int p = token.indexOf('=');
					if (p > 0)
						relabelMap.put(token.substring(0, p),
								token.substring(p + 1));
				}
			}
		}
		Set<QueryField> filter = ((CsvExportWizard) getWizard()).getFilter();
		List<String> labels = new ArrayList<>();
		for (QueryField qfield : filter) {
			String lab = qfield.getLabel();
			if (qfield.isStruct() && qfield.getCard() == 1) {
				QueryField[] children = null;
				QueryField parent = QueryField.getStructParent(qfield.getType());
				if (parent != null)
					children = parent.getChildren();
				if (children != null)
					for (QueryField detailField : children)
						labels.add(lab + ' ' + detailField.getLabel());
			} else
				labels.add(lab);
		}
		viewer.setInput(labels);

	}

	@Override
	protected void validatePage() {
		// do nothing
	}

	@Override
	public boolean isPageComplete() {
		return true;
	}

	public Map<String, String> getRelabelMap() {
		saveSettings(relabelMap);
		return relabelMap;
	}

	private void saveSettings(Map<String, String> relabelMap) {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : relabelMap.entrySet()) {
			if (sb.length() > 0)
				sb.append('\n');
			sb.append(entry.getKey() + '=' + entry.getValue());
		}
		getDialogSettings().put(NEWLABELS, sb.toString());

	}

}
