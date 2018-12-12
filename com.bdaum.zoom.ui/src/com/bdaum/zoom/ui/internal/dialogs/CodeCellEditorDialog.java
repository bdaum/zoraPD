package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.internal.codes.CodeParser;
import com.bdaum.zoom.ui.internal.codes.Topic;

@SuppressWarnings("restriction")
public class CodeCellEditorDialog extends AbstractListCellEditorDialog {

	private ListViewer viewer;
	private final CodeParser codeParser;
	private Button removeButton;
	private Button upButton;
	private Button downButton;
	private QueryField qfield;

	public CodeCellEditorDialog(Shell parentShell, Object value, QueryField qfield) {
		super(parentShell, value, qfield);
		this.qfield = qfield;
		this.codeParser = UiActivator.getDefault().getCodeParser((Integer) qfield.getEnumeration());
	}

	@Override
	public void create() {
		super.create();
		setMessage(Messages.CodeCellEditorDialog_add_remove_codes);
		updateButtons();
	}

	private void updateButtons() {
		IStructuredSelection selection = viewer.getStructuredSelection();
		if (selection.isEmpty()) {
			removeButton.setEnabled(false);
			upButton.setEnabled(false);
			downButton.setEnabled(false);
		} else {
			removeButton.setEnabled(true);
			String[] old = (String[]) value;
			String element = (String) selection.getFirstElement();
			for (int i = 0; i < old.length; i++)
				if (element.equals(old[i])) {
					upButton.setEnabled(i > 0);
					downButton.setEnabled(i < old.length - 1);
					break;
				}
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		viewer = new ListViewer(composite, SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					if (codeParser.canParse()) {
						Topic topic = codeParser.findTopic((String) element);
						if (topic != null)
							return new StringBuilder((String) element).append(" (").append(topic.getName()) //$NON-NLS-1$
									.append(')').toString();
					}
				}
				return element.toString();
			}
		});
		viewer.setInput(value);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		Composite buttonGroup = new Composite(composite, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, false, false));
		buttonGroup.setLayout(new GridLayout());
		Button addButton = new Button(buttonGroup, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		addButton.setText(Messages.CodeCellEditorDialog_add);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] old = (String[]) value;
				CodesDialog dialog = new CodesDialog(getShell(), qfield, null, old);
				if (dialog.open() == OK) {
					String newCode = dialog.getResult();
					viewer.setInput(value = Utilities.addToStringArray(newCode, old, false));
					viewer.setSelection(new StructuredSelection(newCode));
					updateButtons();
				}
			}
		});
		removeButton = new Button(buttonGroup, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		removeButton.setText(Messages.CodeCellEditorDialog_remove);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (!selection.isEmpty()) {
					String element = (String) selection.getFirstElement();
					String[] old = (String[]) value;
					for (int i = 0; i < old.length; i++) {
						if (element.equals(old[i])) {
							String[] newValue = new String[old.length - 1];
							System.arraycopy(old, 0, newValue, 0, i);
							System.arraycopy(old, i + 1, newValue, i, newValue.length - i);
							viewer.setInput(value = newValue);
							updateButtons();
							break;
						}
					}
				}
			}
		});
		upButton = new Button(buttonGroup, SWT.PUSH);
		upButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		upButton.setText(Messages.CodeCellEditorDialog_up);
		upButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (!selection.isEmpty()) {
					String element = (String) selection.getFirstElement();
					String[] old = (String[]) value;
					for (int i = 0; i < old.length; i++) {
						if (element.equals(old[i])) {
							if (i > 0) {
								String replaced = old[i - 1];
								old[i - 1] = old[i];
								old[i] = replaced;
								viewer.setInput(value = old);
								viewer.setSelection(selection);
								updateButtons();
							}
							break;
						}
					}
				}
			}
		});
		downButton = new Button(buttonGroup, SWT.PUSH);
		downButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		downButton.setText(Messages.CodeCellEditorDialog_down);
		downButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (!selection.isEmpty()) {
					String element = (String) selection.getFirstElement();
					String[] old = (String[]) value;
					for (int i = 0; i < old.length; i++) {
						if (element.equals(old[i])) {
							if (i < old.length - 1) {
								String replaced = old[i + 1];
								old[i + 1] = old[i];
								old[i] = replaced;
								viewer.setInput(value = old);
								viewer.setSelection(selection);
								updateButtons();
							}
							break;
						}
					}
				}
			}
		});
		return area;
	}

}