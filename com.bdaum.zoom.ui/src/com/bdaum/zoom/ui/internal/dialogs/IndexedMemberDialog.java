package com.bdaum.zoom.ui.internal.dialogs;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.aoModeling.runtime.AomObject;
import com.bdaum.zoom.core.IndexedMember;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;

public class IndexedMemberDialog extends ZTitleAreaDialog {

	private ListViewer viewer;
	private IndexedMember[] members;
	private final QueryField qfield;
	private Button editButton;
	private Button addButton;
	private Button removeButton;

	public IndexedMemberDialog(Shell parentShell, IndexedMember[] members, QueryField qfield) {
		super(parentShell);
		this.members = members;
		this.qfield = qfield;
	}

	@Override
	public void create() {
		super.create();
		setTitle(qfield.getLabel());
		setMessage(Messages.IndexedMemberDialog_edit_or_remove);
		fillValues();
		updateButtons();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(2, false));
		viewer = new ListViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IndexedMember)
					return QueryField.serializeStruct(((IndexedMember) element).getValue(),
							Messages.IndexedMemberDialog_click_edit);
				return element.toString();
			}
		});
		viewer.setFilters(new ViewerFilter[] { new ViewerFilter() {
			@Override
			public boolean select(Viewer aViewer, Object parentElement, Object element) {
				return (element instanceof IndexedMember && ((IndexedMember) element).getValue() != null);
			}
		} });
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});
		Composite buttonGroup = new Composite(composite, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buttonGroup.setLayout(new GridLayout(1, false));
		editButton = new Button(buttonGroup, SWT.PUSH);
		editButton.setText(Messages.IndexedMemberDialog_edit);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IndexedMember member = (IndexedMember) viewer.getStructuredSelection().getFirstElement();
				StructEditDialog dialog = new StructEditDialog(editButton.getShell(), (AomObject) member.getValue(),
						qfield);
				if (dialog.open() == Window.OK) {
					for (int i = 0; i < members.length; i++)
						if (members[i] == member) {
							members[i].setValue(dialog.getResult());
							break;
						}
					viewer.setInput(members);
				}
			}
		});
		addButton = new Button(buttonGroup, SWT.PUSH);
		addButton.setText(Messages.IndexedMemberDialog_add);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StructEditDialog dialog = new StructEditDialog(editButton.getShell(), null, qfield);
				if (dialog.open() == Window.OK) {
					IndexedMember[] newMembers = new IndexedMember[members.length + 1];
					System.arraycopy(members, 0, newMembers, 0, members.length);
					newMembers[members.length - 1].setValue(dialog.getResult());
					newMembers[members.length - 1].setIndex(members.length - 1);
					newMembers[members.length] = new IndexedMember(qfield, null, members.length);
					viewer.setInput(members = newMembers);
				}
			}
		});
		removeButton = new Button(buttonGroup, SWT.PUSH);
		removeButton.setText(Messages.IndexedMemberDialog_remove);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IndexedMember member = (IndexedMember) viewer.getStructuredSelection().getFirstElement();
				for (int i = 0; i < members.length; i++) {
					if (members[i] == member) {
						IndexedMember[] newMembers = new IndexedMember[members.length - 1];
						System.arraycopy(members, 0, newMembers, 0, i);
						System.arraycopy(members, i + 1, newMembers, i, members.length - i - 1);
						for (int j = i; j < newMembers.length; j++)
							newMembers[j].setIndex(j);
						viewer.setInput(members = newMembers);
						break;
					}
				}
			}
		});
		return area;
	}

	private void updateButtons() {
		boolean enabled = !viewer.getSelection().isEmpty();
		editButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
	}

	private void fillValues() {
		viewer.setInput(members);
	}

	public IndexedMember[] getResult() {
		return members;
	}

}
